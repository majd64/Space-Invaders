/*
 * Base.java
 * Majd Hailat
 * The shelters that the player can hide under
 * Handles the complex creation of a base, handles the destruction, draws the base onto the screen
 */
import java.util.*;
import java.awt.*;
class Base{
    private ArrayList<Rectangle>rects = new ArrayList<Rectangle>();//stores every tiny rectangle (piece) that make up the base
    //takes in a x, y position and a width and height for each individual piece that makes up the base
    public Base(int sX, int sY, int pieceWidth, int pieceHeight){
        for (int y = sY; y+pieceHeight <= sY+12; y+= pieceHeight){//builds the top triangle of the base 
            for (int x = sX - (y-sY); x <= sX+48 + (y-sY); x+= pieceWidth){//setting x and y position of every piece based on specified piece size
                Rectangle piece = new Rectangle(x, y, pieceWidth, pieceHeight);//creating rectangle (aka. piece)
                rects.add(piece);//storing piece
            }
        }
        for (int y = sY+12; y+pieceHeight <= sY+24; y+= pieceHeight){//builds the rectangle below the triangle
            for (int x = sX-12; x <= sX+60; x+= pieceWidth){
                Rectangle piece = new Rectangle(x, y, pieceWidth, pieceHeight);
                rects.add(piece);
            }
        }
        for (int y = sY+24; y+pieceHeight <= sY+36; y+= pieceHeight){//builds left-middle side of base
            for (int x = sX-12; x <= sX+12 - (y- sY-24); x+= pieceWidth){ 
                Rectangle piece = new Rectangle(x, y, pieceWidth, pieceHeight);
                rects.add(piece);
            }
        }
        for (int y = sY+24; y+pieceHeight <= sY+36; y+= pieceHeight){//builds right-middle side of base
            for (int x = sX+36 + (y- sY-24); x <= sX+60; x+= pieceWidth){
                Rectangle piece = new Rectangle(x, y, pieceWidth, pieceHeight);
                rects.add(piece);
            }
        } 
        for (int y = sY+36; y+pieceHeight <= sY+54; y+= pieceHeight){//builds left leg of base
            for (int x = sX-12; x <= sX+3; x+= pieceWidth){
                Rectangle piece = new Rectangle(x, y, pieceWidth, pieceHeight);
                rects.add(piece);
            }
        }
        for (int y = sY+36; y+pieceHeight <= sY+54; y+= pieceHeight){//builds right leg of base
            for (int x = sX+45; x <= sX+60; x+= pieceWidth){
                Rectangle piece = new Rectangle(x, y, pieceWidth, pieceHeight);
                rects.add(piece);
            }
        }
    }
    
    //takes a bullet object and deals damage (deletes certain pieces) based on a formula 
    //utilizes a bullets damage parameter to determine size of damage
    //returns true if there was a collision and false if there was not
    //this method acts as the "intersects" method in the game panel but instead checks for intersection with every little piece
    public boolean hit(Bullet bullet){
        int garunteedHeightRange = bullet.getDamage();//the number of rectangles directly in front of a bullet that will be destroyed
        int possibleHeightRange = bullet.getDamage()*2;//the number of rectangles in front of bullet that could be destroyed based on randomness
        int garunteedWidthRange = bullet.getDamage()/2;//the number of rectangles to the sides of a bullet that will be destroyed
        int possibleWidthRange = bullet.getDamage();//the number of rectangles to the sides of a bullet that could be destroyed based on randomness
        final int possibleDamageChancePercentage = 15;//the chance (out of 100) that a bullet outside of the garunteed damage area will be destroyed
        
        ArrayList<Rectangle>removeRects = new ArrayList<Rectangle>();//rectangles that have been destoryed, will be removed after loops
        for (Rectangle rect : rects){//itterating through every tiny piece in the base
            if (bullet.getRect().intersects(rect)){//checking if the bullet intersects the piece
                removeRects.add(rect);//adding intersected piece
                for (Rectangle r : rects){//itterating through all pieces again
                    //checking which pieces are in the garunteed to be destroyed zone of imapct
                    if (Math.abs((int)rect.getY()-(int)r.getY()) <= garunteedHeightRange && Math.abs((int)rect.getX()-(int)r.getX()) <= garunteedWidthRange){
                        removeRects.add(r);//deleting bullets
                    }
                    //bullets that are not garunteed destruction: checking if they are in the possible destruction zone
                    else if (Math.abs((int)rect.getY()-(int)r.getY()) <= possibleHeightRange && Math.abs((int)rect.getX()-(int)r.getX()) <= possibleWidthRange){
                        if (GamePanel.randint(1,100) <= possibleDamageChancePercentage){//generating a random number that could allow the bullet to be 
                            //destroyed based on the percentage specified above
                            removeRects.add(r);//adding piece to deleted pieces list
                        }
                    }
                }
                for (Rectangle r : removeRects){//itterating through deleted pieces list and deleting them
                    rects.remove(r);
                }
                return true;//returning true because there was a collision
            }
        }
        return false;//no collision
    }
    //draws the base
    public void drawBase(Graphics g){
        for (Rectangle rect : rects){
            g.setColor(Color.cyan);
            g.fillRect((int)rect.getX(), (int)rect.getY(), (int)rect.getWidth(), (int)rect.getHeight());
        }
    }
    //returns the list of pieces that make up the base
    public ArrayList<Rectangle> getRects(){
        return rects;
    }
}