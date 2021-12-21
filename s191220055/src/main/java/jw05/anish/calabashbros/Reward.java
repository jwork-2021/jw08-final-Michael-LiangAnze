package jw05.anish.calabashbros;
import java.awt.Color;

public class Reward extends Thing{
    public Reward(Color color,int itemId,World world) {
        super(color, (char) itemId, world);//173
        this.type = "reward";
    }
}
