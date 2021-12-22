package jw05.anish.map;

import jw05.anish.algorithm.Tuple;
import java.awt.Color;

public class MapUpdateInfo { // 地图每更新一次状态后输出的log信息
    String actionType;// 移动物品或者设置物品,0是移动，1是设置
    int id; // 物品id
    String itemType; // 物品类型,字符串表示

    Tuple<Integer, Integer> beginPos;// 移动开始的位置
    Tuple<Integer, Integer> destPos;// 移动结束的位置
	
    int newIdAfterSet = -1;// 设置成功后获取的id

    int glyph = -1; // 在字符表中的下标
    Color color;// 颜色

    public MapUpdateInfo(int id, String itemType, String actionType, Tuple<Integer, Integer> beginPos,
            Tuple<Integer, Integer> destPos, int newIdAfterSet) {
        this.id = id;
        this.actionType = actionType;
        this.itemType = itemType;
        this.beginPos = beginPos;
        this.destPos = destPos;
        this.newIdAfterSet = newIdAfterSet;
    }

    public MapUpdateInfo(int id, String itemType, String actionType, Tuple<Integer, Integer> beginPos,
            Tuple<Integer, Integer> destPos, int newIdAfterSet, int glyph, Color color) {
        this.id = id;
        this.actionType = actionType;
        this.itemType = itemType;
        this.beginPos = beginPos;
        this.destPos = destPos;
        this.newIdAfterSet = newIdAfterSet;
        this.glyph = glyph;
        this.color = color;
    }

    
    public MapUpdateInfo(int id, String actionType) {
        this.id = id;
        this.actionType = actionType;
        this.itemType = null;
        this.beginPos = null;
        this.destPos = null;
        this.newIdAfterSet = -1;
        this.glyph = -1;
        this.color = null;
    }

    public void Output() {
        System.out.println(this.toString());
    }

    @Override
    public String toString() {
        String line = "null";
            switch(this.actionType){
            case "moveThing":{ // 是移动
                line = String.valueOf(actionType) + ' ' +
                        String.valueOf(id) + ' ' +
                        String.valueOf(itemType) + ' ' +
                        String.valueOf(beginPos.first) + ',' + String.valueOf(beginPos.second) + ' ' +
                        String.valueOf(destPos.first) + ',' + String.valueOf(destPos.second) + ' ';
            } ;break;
            case "setThing": { // 是设置新物品
                line = String.valueOf(actionType) + ' ' +
                        String.valueOf(id) + ' ' +
                        String.valueOf(itemType) + ' ' +
                        String.valueOf(beginPos.first) + ',' + String.valueOf(beginPos.second) + ' ' +
                        String.valueOf(newIdAfterSet) + ' ' +
                        String.valueOf(glyph) + ' ' +
                        String.valueOf(this.color.getRed()) + ',' + String.valueOf(this.color.getGreen()) + ','
                        + String.valueOf(this.color.getBlue());
            };break;
            case "beAttacked":{
                line = String.valueOf(actionType) + ' ' +
                        String.valueOf(id);
            };break;
        }
        return line;
    }
}
