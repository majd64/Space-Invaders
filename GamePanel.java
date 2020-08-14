/*
 * GamePanel.java
 * Majd Hailat
 * Handles all game logic for space invaders: loads all assets, creates all objects (player, enemies, etc), sets up objetcs on screen
 * starts the game, ends the game, manges which mode is being played, sets and reads highscore, tracks lives, score and level
 * draws all graphics, handles player input (movement, shooting, etc), moves objects, handles all collision (except base collision)
 * calls the space ship when needed.
 */
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.applet.*;
import java.io.*;
import java.util.Scanner;
class GamePanel extends JPanel{
    private boolean []keys;//stores clicked keys
    public boolean ready = false;

    private static Player player = new Player();//the main laser controlled by the player
    private static Bullet playerBullet;//The players bullet object 
    private static ArrayList<Enemy>enemies = new ArrayList<Enemy>();//holds all enemies, they get deleted when they die
    private static Ship ship = new Ship();//the spaceship that flies over the screen
    private static Base[]bases = new Base[4];//the 4 shelters
    
    private static File playerShoot = new File("sound/shoot.wav");//loading sound files
    private static File invaderKilled = new File("sound/invaderkilled.wav");
    private static File shipCalled = new File("sound/ship.wav");
    private static File explosion = new File("sound/explosion.wav");
    private static AudioClip playerShootSound, invaderKilledSound, shipCalledSound, explosionSound;
    Font font = null;
    
    private static int score,
    highScore,
    livesLeft,
    numberOfShots,//the amount of shots the player has shot since the last space ship arrival
    level;
    public static final int SHOTSTOCALLSHIP = 22,//the number of shots the player must shoot (not hit) for the spaceship to start
    TOTENEMIES = 55;//total number of starting enemies
    private static double timeElapsed = 0,//increases every itteration
    moveEnemiesTimer,//the timer for which the enmeis are ahifted over
    moveEnemiesTimerLen,//the length of time that the timer must reach 
    tempPauseTimer,//the timer for the momentary pause after the player dies
    shootDelayTimer;//the timer of the delay between player shots
    private static final double tempPauseTimerLen = 0.045,//the length of time for which the timer must reach
    shootDelayTimerLen = 0.045;//the length of time for which the timer must reach
    private static boolean gameOn = false,//is off when the start screen is visible, and on when the game is playing
    tempPause = false,//if the game is in the pause after the player dies(enabled when player dies, disabled with timer)
    extremeMode = false;//different game mode -> much harder
    private static String highScoreFileName;//the name of the file that contains high socre, changes based on game mode
    public static final Rectangle gameOverLine = new Rectangle (0, 700, 890, 3);//the line that the enemies must reach for the game to be over
    
    public GamePanel(){
        keys = new boolean[KeyEvent.KEY_LAST+1];
        addKeyListener(new moveListener());
        try{//loading sound
            playerShootSound = Applet.newAudioClip(playerShoot.toURL());
            invaderKilledSound = Applet.newAudioClip(invaderKilled.toURL());
            shipCalledSound = Applet.newAudioClip(shipCalled.toURL());
            explosionSound = Applet.newAudioClip(explosion.toURL());
        }catch(Exception e){}
        InputStream is = GamePanel.class.getResourceAsStream("space_invaders.ttf");//loading font
        try{
            font = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(20f);
        }catch(Exception ex){System.out.println(ex);} 
    }
    
    public void addNotify() {
        super.addNotify();
        requestFocus();
        ready = true;
    }

    //sets the game up for a fresh start (resets variables, calls starting functions)
    //takes in boolean that determines if the game is being reset entirley or if its the next level
    public void setup(boolean nextLevel){
        try{//checking game mode and retrieving proper high score
            if (extremeMode){
                highScoreFileName = "HSEMODE.txt";
            }else{
                highScoreFileName = "HS.txt";
            }
            BufferedReader reader = new BufferedReader(new FileReader(highScoreFileName));
            highScore = Integer.parseInt(reader.readLine());
            reader.close();
        }catch (Exception ex) {System.out.println(ex);}
        if (!nextLevel){//start of the game or restarting game
            level = 1;
            score = 0;
            if (!extremeMode){//only giving extra lives if regular mode
                livesLeft = 2;
            }else{
                livesLeft = 0;
            }
        }else{//next level
            level ++;
        }
        moveEnemiesTimerLen = 0.01 - (Math.pow(level, 1.3)/3000);//adjusting enemies move timer based on level
        //resetting all other variables
        numberOfShots = 0;
        playerBullet = null;
        enemies.clear();
        Ship.end();
        Enemy.setDir(Enemy.RIGHT);
        player.setX(Player.ogX);
        loadEnemies();
        if (!extremeMode){//only allowing bases for normal mode
            loadBases();
        }
    }
    
    private static final Image bg = new ImageIcon("images/bg.png").getImage();
    private static final Image logo = new ImageIcon("images/logo.png").getImage();
    //draws background, text, and all game objects.
    public void paintComponent(Graphics g){
        g.setFont(font);
        g.drawImage(bg, 0, 0, null);//bg image
        if (gameOn){
            g.setColor(Color.cyan);
            g.fillRect((int)gameOverLine.getX(), (int)gameOverLine.getY(), (int)gameOverLine.getWidth(), (int)gameOverLine.getHeight());//drawing bottom line
            player.draw(g);
            for (Enemy enemy : enemies){//drawing every enemy and enemy bullet
                enemy.draw(g);
                enemy.drawBullets(g);
            }
            if (playerBullet != null){//checking if there is a player bullet and drawing it
                playerBullet.draw(g);
            }
            if (!extremeMode){//drawing bases bases on game mode
                for (Base base : bases){
                   base.drawBase(g);
                }
            }
            ship.drawShip(g);//drawing ship
            for (int l = 0; l < livesLeft; l++){//drawing the player laser images that indicate lives left
                g.drawImage(Player.playerPic, l*60+10, 730, null);
            }
            g.setColor(Color.green);
            g.drawString("Score  "+score, 50, 50);//adding score, highscore and level text to screen
            g.drawString("Level  "+level, 580, 745);
            g.setColor(Color.cyan);
            g.drawString("High Score  "+highScore, 200, 50);
        }else{//game is not on (start screen)
            g.setColor(Color.cyan);
            g.drawImage(logo, 48, 250, null);
            g.drawString("Press Space to Start", 240, 600);//adding start text to start screen
            g.setColor(Color.red);
            g.drawString("Press E for Extreme Mode", 220, 650);
        }
    }

    private static final double timeIncrement = 0.001;
    //handles pauses and controls if the game is running or not. also listens for the player to start the game
    public void move(){ 
        timeElapsed += timeIncrement;
        final boolean normalModeSelected = keys[KeyEvent.VK_SPACE],
        extremeModeSelected = keys[KeyEvent.VK_E];
        if (!gameOn){
            if (normalModeSelected){//checking if the player started the game 
                gameOn = true;
                extremeMode = false;//setting game mode to normal
                setup(false);//setting up the game
                keys[KeyEvent.VK_SPACE] = false;
            }
            else if (extremeModeSelected){//player selected extremem mode
                gameOn = true;
                extremeMode = true;//setting game mode to etreme
                setup(false);
            }
        }
        else if (tempPause && timeElapsed > tempPauseTimer + tempPauseTimerLen){//checking if pause timer has finished
            tempPause = false;//un pausing game
        }
        else if (gameOn && !tempPause){//checking if game is allowed to run
            gameRunning();
        }
    }
    
    //main game logic for when the game is running
    //calls all collision methods, generates enemy and player bullets, handles player movement, enemy movement and all bullet movements
    public void gameRunning(){
        //collision methods being called
        playerBulletEnemyCollision();
        playerBulletShipCollision();
        enemyBulletCollision();
        if (!extremeMode){//only handle bases of normal mode
            playerBulletBaseCollision();
            enemyBaseCollision();
        }
        //checking for space ship
        checkForShip();
        //checking if the player wants to end the game
        if(keys[KeyEvent.VK_ESCAPE]){
            endGame();
        }
        //check for player shooting
        if (keys[KeyEvent.VK_SPACE] && playerBullet == null && timeElapsed > shootDelayTimer + shootDelayTimerLen){
            shootDelayTimer = timeElapsed;//setting delay between shots 
            final int bulletShift = 20, bulletWidth = 4, bulletHeight = 14, bulletSpeed = -7, bulletDamage = 9;//creating new bullet
            playerBullet = new Bullet(player.getX()+bulletShift, player.getY(), bulletWidth, bulletHeight, bulletSpeed, bulletDamage, Color.green);
            numberOfShots ++;
            try{
               playerShootSound.play();
            }catch(Exception e){}
        }
        //checking for player movement
        if(keys[KeyEvent.VK_RIGHT] &&  player.getX()+player.WIDTH < SpaceInvaders.SCREENWIDTH){
           player.move(player.RIGHT);
        }
        if(keys[KeyEvent.VK_LEFT] && player.getX() > 0){
           player.move(player.LEFT);
        }
        //moving player bullet and checking if it reached the end of the screen
        if (playerBullet != null){
           playerBullet.moveBullet();
           if (playerBullet.offScreen()){
               playerBullet = null;
           }
        }
        
        //moving enemies
        if (timeElapsed > moveEnemiesTimer + moveEnemiesTimerLen){//checking if its time to shift enemies horizontally
            Enemy.moveEnemies(enemies);
            moveEnemiesTimer = timeElapsed;//resetting move timer
        }
        //calling enemy bullet generation method
        int bulletDropChanceRange;//the range in which a bullet will be generated (smaller range = higher chance of spawn)
        if (extremeMode){//setting bullet spawn difficulty based on game mode
            bulletDropChanceRange = 5000; 
        }else{
            bulletDropChanceRange = 10000;
        }
        Enemy.generateEnemyBullets(enemies, bulletDropChanceRange, level);
        //moving enemy bullets
        for (Enemy enemy: enemies){//moving all enemy bullets
            enemy.moveBullets();
        }
    }

    //called when the played dies, checks if the game is over or if the player has more lives
    //checks and sets highscore if beaten
    public void playerDied(){
        try{
            explosionSound.play();
        }catch(Exception e){}
        if (livesLeft == 0){//checking for game over
            endGame();
        }else{   
            //setting player up for new life
            Player.setX(Player.ogX);
            livesLeft -= 1;
            for (Enemy enemy : enemies){
                enemy.clearBullets();
            }
            playerBullet = null;
            tempPauseTimer = timeElapsed;
            tempPause = true;
        }
    }
    
    //called when either the player dies, or clicks escape
    public static void endGame(){
        if (score > highScore){//checking if high score was beaten -> resetting high score to new score
            try{
                PrintWriter file = new PrintWriter(new File(highScoreFileName));
                file.println(score);
                file.close();
            }catch(IOException ex){System.out.println(ex);}
        }
        gameOn = false; 
    }

    //checks if its time for the spaceship to fly by, and starts the ship object
    public void checkForShip(){
        if (numberOfShots == SHOTSTOCALLSHIP && !Ship.isAlive()){//checking if its time for the spaceship to come based on shot count
            try{
                shipCalledSound.play();
            }catch(Exception e){}
            ship.start();
        }
    }
    
    //resets shot counter, called when the spaceship is killed or passes the screen
    public static void resetShots(){
        numberOfShots = 0;
    }
   
    //checks collision between the player bullet and any enemy.
    //when enemy is hit: removes bulletm increases score, speeds up enemies, and checks if the player killed all enemies
    public void playerBulletEnemyCollision(){
        if (playerBullet != null){
            for (int e = 0; e < enemies.size(); e++){
                Enemy enemy = enemies.get(e);
                if (playerBullet.getRect().intersects(enemy.getRect()) && !enemy.isDisabled()){//checking if player bullet hit any enemy
                    try{
                        invaderKilledSound.play();
                    }catch(Exception ex){}
                    playerBullet = null;
                    score += enemy.getPoints();
                    final double expScalar = 1.4, magScalar = 700000;
                    moveEnemiesTimerLen -= (Math.pow(TOTENEMIES-enemies.size(), expScalar)/magScalar);//increasing the enemy speed based on small algorithm
                    if (enemy.getBullets().size() != 0){//checing if enemy has any bullets on screen
                        enemy.disable();//temporairly hiding and disabling (not deleting) enemy until their bullet reaches the bottom
                    }else{
                        enemies.remove(enemy);//deleting enemy
                    }  
                    if (enemies.size() == 0){//checking if the player won by calling all enemies
                        setup(true);
                    }
                    break;
                }
            }
        }
    }
    //checks collision between the player bullet and the spaceship
    //if hit: deletes player bullet, increases score and ends the spaceship
    public void playerBulletShipCollision(){ 
        if (playerBullet != null){
            if (playerBullet.getRect().intersects(ship.getRect())){
                try{
                    explosionSound.play();
                }
                catch(Exception e){}
                playerBullet = null;
                if (numberOfShots == SHOTSTOCALLSHIP + 1){//checking if the player killed the spaceship with their first shot
                    score += Ship.FIRSTSHOTPOINTS;
                }else{
                    score += Math.round(randint(Ship.FIRSTSHOTPOINTS-100,Ship.FIRSTSHOTPOINTS)/10)*10;
                }
                resetShots();
                ship.end();
            }
        }
    }
    //checks collision between the player bullet and the 4 bases
    //when hit: deletes player bullet and calls method to explode part of the base
    public void playerBulletBaseCollision(){
        if (playerBullet != null){
            for (Base base : bases){
                if (base.hit(playerBullet)){//checking if player bullet collided with the base
                    playerBullet = null;
                    break;
                }
            }
        }
    }
    //checks collision between enemy bullet and the player as well as the bases 
    //when player is hit: calls player dies method, and deletes enemy bullet
    //when base is hit: calls base explosion method, and deletes enemy bullet
    public void enemyBulletCollision(){
        boolean killed = false;//if the player has been shot (can only execute player death after all for loops)
        for (Enemy enemy: enemies){
            ArrayList<Bullet>deletedBullets = new ArrayList<Bullet>();
            for (Bullet enemyBullet : enemy.getBullets()){
                if (player.getRect().intersects(enemyBullet.getRect())){//checking if any enemy bullet hit the player
                    deletedBullets.add(enemyBullet);//temporary storing bullet for deletion after for loops
                    killed = true;
                    break;
                }
                if (!extremeMode){
                    for (Base base : bases){
                        ArrayList<Rectangle>rects = base.getRects();
                        if (base.hit(enemyBullet)){//checking if enemy bullet hit a base
                            deletedBullets.add(enemyBullet);
                            break;
                        } 
                    }   
                }
            }
            for (Bullet bullet : deletedBullets){//deleting all enemy bullets that had a collision
                enemy.getBullets().remove(bullet);
            }
            deletedBullets.clear();
        }
        if (killed){
            playerDied();
        }
    }
    //checks collision between the enemies and bases when the enemies gets really low, removes part of the base that gets hit
    public void enemyBaseCollision(){
        for (Enemy enemy : enemies){
            for (Base base : bases){
                ArrayList<Rectangle>rects = base.getRects();
                for (int r = 0; r < rects.size(); r++){
                    if (rects.get(r).intersects(enemy.getRect())){//checking if enemy collides with and piece of the base
                        rects.remove(rects.get(r));
                    }
                }
            }
        }
    }
    
    private static final int BX = 150, BY = 600, PIECEWIDTH = 3, PIECEHEIGHT = 3, SPACING = 140;
    //Creates 4 base objects at the correct position
    public void loadBases(){
        for (int baseNum = 0; baseNum <= 3; baseNum++){
            Base base = new Base(BX+(baseNum*SPACING), BY, PIECEWIDTH, PIECEHEIGHT);//creating base object
            bases[baseNum] = base;//adding base to arraylist
        }
    }
    //creates 55 enemies with the correct type, and position on screen
    public void loadEnemies(){
        int x = 115, y = 200, row = 1, enemyType = 0;
        for (int i = 0; i < TOTENEMIES; i++){//creating 55 enemies
            if (i % 11 == 0 && i != 0){//checking if new row should start
                x = 115;//resetting x
                y += 45;//increasing y 
                row += 1;//keeping track of row
            }
            if (extremeMode){//if extreme mode, all enemies will be the "hardest" enemy
                enemyType = Enemy.TYPE3;
            }else{
                if (row == 1){//setting enemy type based on their row
                    enemyType = Enemy.TYPE3;
                }
                else if (row == 2 || row == 3){
                    enemyType = Enemy.TYPE2;
                }
                else if (row == 4 || row ==5){
                    enemyType = Enemy.TYPE1;
                }
            }
            Enemy enemy = new Enemy(enemyType, x, y);//creating new enemy object and adding to array list
            enemies.add(enemy);
            x+= 47;//shifting x position for next enemy
        }
    }

    //modified randint method, from and including min to and including max
    public static int randint(int min, int max) {
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }
    class moveListener implements KeyListener{
        public void keyTyped(KeyEvent e) {}
        public void keyPressed(KeyEvent e) {
            keys[e.getKeyCode()] = true;
        }
        public void keyReleased(KeyEvent e) {
            keys[e.getKeyCode()] = false;
        }
    }    
}