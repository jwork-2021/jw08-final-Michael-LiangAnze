package jw05.anish.net;

import jw05.anish.algorithm.Tuple;
import jw05.anish.calabashbros.Player;
import java.awt.Color;

public class PlayerInfo {
    public Player player;
    public int id;
    public Tuple<Integer,Integer>pos;
    public Color color;
    public boolean isAsssign = false;

    public PlayerInfo(Player player,int id,Tuple<Integer,Integer>pos,Color color){
        this.player = player;
        this.id = id;
        this.pos = pos;
        this.color = color;
    }
}
