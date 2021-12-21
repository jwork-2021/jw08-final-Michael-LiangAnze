package jw05.anish.map;

import jw05.anish.algorithm.Tuple;
import java.awt.Color;

public class MapUpdateInfo { // 地图每更新一次状态后输出的log信息
    boolean moveOrSet;// 移动物品或者设置物品,0是移动，1是设置
    int id; // 物品id
    boolean itemType; // 物品类型，0是生物，1是炮弹

    Tuple<Integer, Integer> beginPos;// 移动开始的位置
    Tuple<Integer, Integer> destPos;// 移动结束的位置
	
    int newIdAfterSet = -1;// 设置成功后获取的id

    int glyph = -1; // 在字符表中的下标
    Color color;// 颜色

    public MapUpdateInfo(int id, boolean itemType, boolean moveOrSet, Tuple<Integer, Integer> beginPos,
            Tuple<Integer, Integer> destPos, int newIdAfterSet) {
        this.id = id;
        this.itemType = itemType;
        this.moveOrSet = moveOrSet;
        this.beginPos = beginPos;
        this.destPos = destPos;
        this.newIdAfterSet = newIdAfterSet;
    }

    public MapUpdateInfo(int id, boolean itemType, boolean moveOrSet, Tuple<Integer, Integer> beginPos,
            Tuple<Integer, Integer> destPos, int newIdAfterSet, int glyph, Color color) {
        this.id = id;
        this.itemType = itemType;
        this.moveOrSet = moveOrSet;
        this.beginPos = beginPos;
        this.destPos = destPos;
        this.newIdAfterSet = newIdAfterSet;
        this.glyph = glyph;
        this.color = color;
    }

    public void Output() {
        System.out.println(this.toString());
    }

    @Override
    public String toString() {
        String line;
        if (!this.moveOrSet) { // 是移动
            line = String.valueOf("moveThing") + ' ' +
                    String.valueOf(id) + ' ' +
                    String.valueOf(itemType) + ' ' +
                    String.valueOf(beginPos.first) + ',' + String.valueOf(beginPos.second) + ' ' +
                    String.valueOf(destPos.first) + ',' + String.valueOf(destPos.second) + ' ';
        } else { // 是设置新物品
            line = String.valueOf("setThing") + ' ' +
                    String.valueOf(id) + ' ' +
                    String.valueOf(itemType) + ' ' +
                    String.valueOf(beginPos.first) + ',' + String.valueOf(beginPos.second) + ' ' +
                    String.valueOf(newIdAfterSet) + ' ' +
                    String.valueOf(glyph) + ' ' +
                    String.valueOf(this.color.getRed()) + ',' + String.valueOf(this.color.getGreen()) + ','
                    + String.valueOf(this.color.getBlue());
        }
        return line;
    }
}
