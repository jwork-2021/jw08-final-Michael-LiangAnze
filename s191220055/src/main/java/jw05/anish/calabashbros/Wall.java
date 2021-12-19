package jw05.anish.calabashbros;

import jw05.asciiPanel.AsciiPanel;

public class Wall extends Thing {

    public Wall(World world) {
        super(AsciiPanel.cyan, (char) 177, world);
        // super(AsciiPanel.green, (char) 24, world);
    }

}
