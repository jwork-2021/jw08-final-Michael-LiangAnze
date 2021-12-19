package jw05.anish.screen;

import jw05.anish.calabashbros.Thing;
import jw05.anish.calabashbros.World;
import java.awt.Color;

public class ScreenInfo extends Thing{
    public ScreenInfo(World world,Color c, int charNum) {
        super(c, (char) charNum, world);
    }
}
