/*
 * Ship.java
 * Majd Hailat
 * Spaceship that flies above the enemies
 * tracks ship position, handles ship movement, checks when the ship is dead, draws the ship
 */
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.applet.*;
import java.io.*;
class Ship implements ActionListener{
    private static int x = -200;//starting x pos of ship
    public static final int y = 150,//y pos of ship
    SPEEDX = 1,//horizontal pixel movement (speed)
    WIDTH = 45, HEIGHT = 21,//width and height of ship hit box
    FIRSTSHOTPOINTS = 300;//number of points given when killed with first attempt
    private static boolean alive = false;//if the ship is active
    private static Image shipPic = new ImageIcon("images/ship.png").getImage();//picture of the ship
    private static Timer timer;//timer to move the ship as long as its alive
    //creating timer
    public Ship(){
        timer = new Timer(6, this);
    }
    //conyinuously moves ship horizontally and checks when it reaches the end of the screen
    public void actionPerformed(ActionEvent evt){
        x += SPEEDX;
        if (x >= SpaceInvaders.SCREENWIDTH){
            end();
        }
    }
    //called by the game panel when its time for the ship to come  
    public static void start(){
        timer.start();//starting timer
        if (alive == false){
            alive = true;
        }
    }
    //called when the ship is either killed or reached the end of the screen
    public static void end(){
        if (alive){
            x = -200;//resetting pos
            GamePanel.resetShots();//resetting the number of shots counter for the game panel
            timer.stop();
            alive = false;
        }
    }
    //returns true if the ship is alive (moving)
    public static boolean isAlive(){
        return alive;
    } 
    //returns the hit box of the ship
    public static Rectangle getRect(){
        Rectangle shipRect = new Rectangle(x, y, WIDTH, HEIGHT);
        return shipRect;
    }
    //draws the ship
    public static void drawShip(Graphics g){
            g.drawImage(shipPic, x, y, null);
    }
}