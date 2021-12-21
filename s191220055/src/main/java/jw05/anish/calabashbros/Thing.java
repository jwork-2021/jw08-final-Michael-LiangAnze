package jw05.anish.calabashbros;

import java.awt.Color;
import jw05.anish.algorithm.Tuple;

public class Thing {

    int id = -1;// 物体应当具备id，默认为-1

    public void setId(int id) {
        this.id = id;
    }

    public int getId(){
        return this.id;
    }

    String type = "null";//monster，player,cannonball

    public void setType(String s){
        if(s.equals("monster") || s.equals("player")){
            type = s;
        }
    }

    public String getType(){
        return type;
    }

    protected World world;

    public Tile<? extends Thing> tile;

    private int leftIcon = -1, rightIcon = -1;

    public void initIcon(int l, int r) {
        leftIcon = l;
        rightIcon = r;
    }

    public void setIcon(int direction) {
        if (leftIcon != -1 && rightIcon != -1) {
            switch (direction) {
                case 3:
                    glyph = (char) leftIcon;
                    break;
                case 4:
                    glyph = (char) rightIcon;
                    break;
            }
        }

    }

    public Tuple<Integer, Integer> getPos() {
        return this.tile.getPos();
    }

    public void setPos(Tuple<Integer, Integer> pos) {
        this.tile.setPos(pos);
    }

    public void setTile(Tile<? extends Thing> tile) {
        this.tile = tile;
    }

    public Thing(Color color, char glyph, World world) {
        this.color = color;
        this.glyph = glyph;
        this.world = world;
    }

    private Color color;

    public Color getColor() {
        return this.color;
    }

    public void changeColor(Color c) {
        this.color = c;
    }

    private char glyph;

    public char getGlyph() {
        return this.glyph;
    }
}
