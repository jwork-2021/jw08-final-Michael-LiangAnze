package jw05.anish.calabashbros;

import java.awt.Color;

public class Floor extends Thing {

    public Floor(World world, Color c) {
        super(c, (char) 0, world);
        this.type = "floor";
    }

    public Floor(World world) {
        super(Color.gray, (char) 0, world);
        this.type = "floor";
    }
}
