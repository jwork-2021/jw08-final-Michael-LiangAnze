package jw05.anish.calabashbros;

import jw05.anish.algorithm.Tuple;

public class Tile<T extends Thing> {

    private T thing;
    private int xPos;
    private int yPos;

    public T getThing() {
        return thing;
    }

    public void setThing(T thing) {
        this.thing = thing;
        this.thing.setTile(this);
    }

    @Deprecated
    public int getxPos() {
        return xPos;
    }

    public Tuple<Integer,Integer> getPos(){
        return new Tuple<Integer,Integer>(xPos, yPos);
    }

    public void setPos(Tuple<Integer,Integer> t){
        xPos = t.first;
        yPos = t.second;
    }
    
    @Deprecated
    public void setPos(int xPos, int yPos){
        this.xPos = xPos;
        this.yPos = yPos;
    }

    @Deprecated
    public int getyPos() {
        return yPos;
    }


    public Tile() {
        this.xPos = -1;
        this.yPos = -1;
    }

    public Tile(int xPos, int yPos) {
        this.xPos = xPos;
        this.yPos = yPos;
    }

}
