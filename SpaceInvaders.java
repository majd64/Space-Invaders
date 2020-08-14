/*
 * SpaceInvaders.java
 * Majd Hailat
 * Creates jframe and jpanel
 * sets screen size and handles game termination, starts the timer that calls main methods (in game panel), creates new game object
 */
import java.awt.event.*;
import javax.swing.*;
public class SpaceInvaders extends JFrame implements ActionListener{
    Timer myTimer;   
    GamePanel game;
    public static final int SCREENWIDTH = 750;
    public static final int SCREENHEIGHT = 792;
    
    //Sets up jframe, creates timer object and starts timer, handles frame
    public SpaceInvaders(){
        super("Space Invaders");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(SCREENWIDTH,SCREENHEIGHT);
        myTimer = new Timer(10, this);   // trigger every 100 ms
        myTimer.start();
        game = new GamePanel();
        add(game);
        setResizable(false);
        setVisible(true);
    }
    //calls move method for game panel to handle all game logic
    //calls method that draws the game
    public void actionPerformed(ActionEvent evt){
        if(game!= null && game.ready){
            game.move();
            game.repaint();
        }           
    }
    //creates new jframe
    public static void main(String[] arguments) {
        SpaceInvaders frame = new SpaceInvaders();      
    }
}