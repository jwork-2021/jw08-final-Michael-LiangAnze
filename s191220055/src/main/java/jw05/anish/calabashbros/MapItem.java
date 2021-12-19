package jw05.anish.calabashbros;

import java.awt.Color;
public class MapItem extends Thing{
    public MapItem(Color color,int itemId,World world) {
        super(color, (char) itemId, world);
    }
}
