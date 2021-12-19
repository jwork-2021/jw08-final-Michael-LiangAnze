package jw05.anish.calabashbros;

import java.awt.Color;

public class Cannonball extends Thing {

    private int direction;

    public Cannonball(int direction, int damage, World world) {
        super(Color.red, (char) 249, world);
        this.direction = direction;
    }

    public int getDirection(){
        return this.direction;
    }
}
