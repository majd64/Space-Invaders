/*
 * Player.java
 * Majd Hailat
 * The main laser - controlled by the player
 * tracks laser pos, handles laser movement and speed, draws player
 */
import java.awt.*;
import javax.swing.*;
class Player{
    private static int x = 350;//x position of the player
    public static final int y = 655,//y pos of player 
    ogX = 350,//the default x pos of player 
    SPEEDX = 3,//the horizontal pixel movement of player (aka speed)
    WIDTH = 45, HEIGHT = 28, //width and height of player hit box
    RIGHT = 1, LEFT = 2;//direction of player movement
    public static final Image playerPic = new ImageIcon("images/player.png").getImage();//loading player image
    //takes in a direction then moves the player according to that direction
    public static void move(int dir){
        if (dir == RIGHT){
            x += SPEEDX;
        }else{
            x -= SPEEDX;
        }
    }
    //returns x position of player
    public static int getX(){
        return x;
    }
    //takes in x position and sets the player x position
    public static void setX(int mX){
        x = mX;
    }
    //returns y position of player
    public static int getY(){
        return y;
    }
    //returns Rectangle object of the player
    public static Rectangle getRect(){
        Rectangle playerRect = new Rectangle(x, y, WIDTH, HEIGHT);
        return (playerRect);
    }
    //draws player
    public static void draw(Graphics g){
        g.drawImage(playerPic, x, y, null);
    }
} 