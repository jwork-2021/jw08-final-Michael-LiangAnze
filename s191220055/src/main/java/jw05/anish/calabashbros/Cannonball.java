package jw05.anish.calabashbros;

import java.awt.Color;

public class Cannonball extends Thing {

    private int direction;
    private int ownerId; // for online

    public Cannonball(int direction, int damage, World world) {
        super(Color.red, (char) 249, world);
        this.direction = direction;
        this.type = "cannonball";
    }

    public Cannonball(int direction, int damage, World world,int ownerId) {
        super(Color.red, (char) 249, world);
        this.direction = direction;
        this.type = "cannonball";
        this.ownerId = ownerId;
    }

    public int getOwner(){
        return this.ownerId;
    }
    public int getDirection(){
        return this.direction;
    }
}
