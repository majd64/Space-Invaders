/*
 * Enemy.java
 * Majd Hailat
 * The enemies that the player attempts to kill
 * Tracks enemy pos, creates enemies with different attributes, handles enemy movement, creates enemy bullets 
 * handles bullet movement, provids game with appropriate points for each enemy, draws enemy and enemy bullets
 */
import java.util.*;
import java.awt.*;
import javax.swing.*;
class Enemy{
    private int type,//enemy type determines the picture of the enemy, the type of bullet they shoot and the amount of points they give off
    typeInd,//same as enemy type but use to access items in arrays (0 1 or 2, where 0 is type 1 and so on)
    x,y;//x and y position of enemy on screen
    private boolean tempDisabled = false;//when an enemy is killed but they still have active bullets, they are disabled until they no longer have bullets
    private Image enemyPic;//the enemy picture
    private ArrayList<Bullet>bullets = new ArrayList<Bullet>();//holds all enemy bulletsm
    public int WIDTH;//enemy width
    public static final int HEIGHT = 25,//enemy height (all enemies are same height)
    RIGHT = 1, LEFT = 2,//directions for enemy movement
    SPEEDX = 2,//number of pixels the enemy shifts horizontally when called to move
    SHIFTY = 45,//number of pixels all enemies shift down when 1 enemy reaches the end
    MAXBULLETS = 3,//max number of bullets an enemy can have at 1 time
    TYPE1 = 1, TYPE2 = 2, TYPE3 = 3;//different enemy types
    public static final int []IMAGEWIDTH = new int []{40, 38, 25};//the widths of the enemies based on enemy type
    private static int dir = RIGHT;//the horizontal direction of all enemies
    
    //takes the type of enemy and the starting x and y position
    public Enemy(int type, int x, int y){
        this.type = type;
        typeInd = type - 1;
        this.x = x;
        this.y = y;
        enemyPic = new ImageIcon("images/enemy"+type+".png").getImage();//setting proper image based on type
        WIDTH = IMAGEWIDTH[typeInd];
    }
    
    public static final Rectangle enemyBoundryRight = new Rectangle (SpaceInvaders.SCREENWIDTH, 0, 5, SpaceInvaders.SCREENHEIGHT); //the left and right boundries that trigger the change in direction fior enemies
    public static final Rectangle enemyBoundryLeft = new Rectangle (0, 0, 5, SpaceInvaders.SCREENHEIGHT);
    //takes list of all enemies, moves all enemies horizontally, checks when enemies reach the edges
    //flips enemy direction and shifts all enemies down check if the enemies reach the bottom
    public static void moveEnemies(ArrayList<Enemy>enemies){
        for (int e = 0; e < enemies.size(); e++){
            Enemy enemy = enemies.get(e);
            enemy.move();//moving all enemies
            if (enemy.isDisabled()){//checking if the enemy has been killed but still had bullets on screen
                if (enemy.getBullets().size() == 0){//waiting for their bullet to end
                    enemies.remove(enemy);//deleting enemy
                }
            }
            if ((enemy.getRect().intersects(enemyBoundryRight) || enemy.getRect().intersects(enemyBoundryLeft))){//checking if any enemy reached the end
               Enemy.flipDir();//flipping direction for ALL enemies
               for (Enemy i : enemies){//shifting all enemies down 
                   i.shiftDown();
               }
            }
            if (enemy.getRect().intersects(GamePanel.gameOverLine)){//checking if any enemy reached the bottom
                GamePanel.endGame();
            }
        }
    }
    
    //takes in list of all enemies, the range in which they're bullet drop chance will be compared and the current level 
    public static void generateEnemyBullets(ArrayList<Enemy>enemies, int bulletDropChanceRange, int level){
        int bulletDropChanceRangeReduction = level*30;
        for (Enemy enemy : enemies){
            int chance = GamePanel.randint(1, bulletDropChanceRange-bulletDropChanceRangeReduction);//generating random number (based on enemy)
            if (chance <= enemy.getBulletDropChance()){//checking if bullet will be generated
                enemy.newBullet();
            }
        }
    }
    
    //moves enemy based on the classes speed and direction
    public void move(){
        if (!tempDisabled){
            if (dir == RIGHT){
                x += SPEEDX;
            }else{
                x -= SPEEDX;
            }
        }
    }
    //shifts enemy down 
    public void shiftDown(){
        y += SHIFTY;
    }
    //changes the direction of all enemies to the opposite direction
    public static void flipDir(){
        if (dir == RIGHT){
            dir = LEFT;
        }else{
            dir = RIGHT;
        }  
    }
    //takes a direction (1 or 2) and sets the direction
    public static void setDir(int newDir){
        if (dir == RIGHT || dir == LEFT){
            dir = newDir;
        }
    }
    //returns the x pos of an enemy
    public int getX(){
        return x;
    }
    //returns the y pos of an enemy
    public int getY(){
        return y;
    }
    //returns the enemy type
    public int getType(){
        return type;
    }
    
    public final int[]points = new int[]{10, 20, 30};//number of points each enemy gives off based on type
    //returns number of points based on type
    public int getPoints(){
        return points[typeInd];
    }
    
    //bullet parameters based on the type of the enemy
    public final int[] bWidth = new int[]{4, 5, 4};
    public final int[] bHeight = new int[]{10, 10, 15};
    public final int[] bSpeed = new int[]{3, 5, 7};
    public final int[] bDamage = new int[]{7, 8, 9};
    public final Color[] bCol = new Color[]{Color.yellow, Color.cyan, Color.magenta};
    //creates a new bullet and adds it to array list of bullets
    public void newBullet(){
        if (bullets.size() < MAXBULLETS && !tempDisabled){//checking if the enemy is allowed to shoot
            bullets.add(new Bullet(x, y, bWidth[typeInd], bHeight[typeInd], bSpeed[typeInd], bDamage[typeInd], bCol[typeInd]));
        }
    }
    
    public final int[] bDropChance = new int[]{5,5,2};//chance, out of 10000, that the enemy will drop a bullet every itteration
    //returns the chance that an enemy can create a bullet 
    public int getBulletDropChance(){
        return bDropChance[typeInd];
    }
    
    //moves all of a particular enemies bullets and checks if the bullet has went off screen to delete it
    public void moveBullets(){
        for (int b = 0; b < bullets.size(); b++){
            bullets.get(b).moveBullet();
            if (bullets.get(b).offScreen()){
                bullets.remove(bullets.get(b));
            }
        }
    } 
    //returns enemies bullet array
    public ArrayList<Bullet> getBullets(){
        return bullets;
    }
    //deletes all bullets
    public void clearBullets(){
        bullets.clear();
    }
    //draws all enemy bullets 
    public void drawBullets(Graphics g){
        for (Bullet bullet : bullets){
            bullet.draw(g);
        }
    }
    //returns enemy hit box
    public Rectangle getRect(){
        Rectangle enemyRect = new Rectangle(x, y, WIDTH, HEIGHT);
        return enemyRect;
    }
    //disbales enemy until they can get deleted
    public void disable(){
        tempDisabled = true;
    }
    //returns true if enemy is disabled
    public boolean isDisabled(){
        return tempDisabled;
    }
    //draws enemy
    public void draw(Graphics g){
        if (tempDisabled == false){
            g.drawImage(enemyPic, x, y, null);
        }
    }
}