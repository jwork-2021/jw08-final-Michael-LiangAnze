package jw05.anish.calabashbros;

import jw05.anish.algorithm.Tuple;
import jw05.anish.map.Map;
import java.awt.Color;

public class Creature extends Thing {

    int hp;
    int speed;
    Map map;

    Creature(Color color, char glyph, World world) {
        super(color, glyph, world);
        speed = 100;
        if(this.type == null){
            this.type = "creature";
        }
    }

    public boolean moveTo(Tuple<Integer, Integer> beginPos, Tuple<Integer, Integer> destPos) {
        if (map.moveThing(beginPos, destPos)) { // 检查当前状态是否可以前往，如果可以就移动
            return true;
        } else {
            // System.out.println("blocked!");
            return false;
        }
    }

    public boolean moveTo(int direction) { // 1 2 3 4分别代表上下左右
        this.setIcon(direction);
        Tuple<Integer, Integer> curPos = getPos();
        switch (direction) {
            case 1:
                return moveTo(curPos, new Tuple<Integer, Integer>(curPos.first, curPos.second + 1));
            case 2:
                return moveTo(curPos, new Tuple<Integer, Integer>(curPos.first, curPos.second - 1));
            case 3:
                return moveTo(curPos, new Tuple<Integer, Integer>(curPos.first - 1, curPos.second));
            case 4:
                return moveTo(curPos, new Tuple<Integer, Integer>(curPos.first + 1, curPos.second));
            default:
                System.out.println("direction:" + direction + " is illegal!");
                return false;
        }
    }

    public int getHp() {
        return hp;
    }

    public void beAttack(int damage) {

    }

    public void setOnAlert() {

    }

    public void setOffAlert() {

    }
}
