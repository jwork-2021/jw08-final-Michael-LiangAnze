package jw05.anish.net;

import jw05.anish.algorithm.Tuple;
import jw05.anish.calabashbros.Player;
import java.awt.Color;
import java.net.SocketAddress;

public class PlayerInfo {
    public Player player;
    public int id;
    public Tuple<Integer,Integer>pos;
    public Color color;
    public boolean isAsssign = false;
    public SocketAddress playerAddress;

    public PlayerInfo(Player player,int id,Tuple<Integer,Integer>pos,Color color,SocketAddress playerAddress){
        this.player = player;
        this.id = id;
        this.pos = pos;
        this.color = color;
        this.playerAddress = playerAddress;
    }
}
