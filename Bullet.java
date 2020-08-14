/*
 * Bullet.java
 * Majd Hailat
 * Bullets used by the player and enemies
 * creates bullet objects, handles bullet movement, checks when bullets are offscreen, draws bullets   
 */
import java.awt.*;
class Bullet{
    private int x, y,//x and y pos of bullet on screen
    width, height,//width and height of bullet
    speed,//the number of pixels in which the bullet travels for every interval of time (negative val to move up, pos val to move down)
    damage;//the amount of damage bullet does; determines amount of damage to be delt on base (used by base clas)
    private Color color;//color of the bullet
    //constructs bullet: takes initial x and y pos, takes width and height, takes vertical speed, damage and color
    public Bullet(int x, int y, int width, int height, int speed, int damage, Color color){
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speed = speed;
        this.color = color;
        this.damage = damage;
    }
    //moves bullet "speed" amount of pixels horizontally
    public void moveBullet(){
        y += speed;
    }
    //returns true if bullet is off the screen and false otherwise
    public boolean offScreen(){
        if (y <= 0 || y >= SpaceInvaders.SCREENHEIGHT){//checking if its abive or below the boundries of the screen 
            return true;
        }
        return false;
    }
    //returns bullet damage
    public int getDamage(){
        return damage;
    }
    //returns bullet hitbox
    public Rectangle getRect(){
        Rectangle rect = new Rectangle(x, y, width, height);
        return rect;
    }
    //drawa bullet
    public void draw(Graphics g){
        g.setColor(color);
        g.fillRect(x, y, width, height);
    }
}