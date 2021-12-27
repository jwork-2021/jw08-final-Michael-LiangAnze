package jw05.anish.net;

import jw05.anish.algorithm.Tuple;
import java.awt.Color;

public class NetInfo {
    String actionType = null;
    int id = -1;        // 设置成功后获取的id
    String itemType = null; // 物品类型,字符串表示

    Tuple<Integer, Integer> beginPos = null;// 移动开始的位置
    Tuple<Integer, Integer> destPos = null;// 移动结束的位置

    int glyph = -1; // 在字符表中的下标
    Color color = null;// 颜色

    int direction = 0;

    //设置物品setThing
    public NetInfo(String actionType,String itemType, int id, Tuple<Integer, Integer> pos,int glyph, Color color) {
        this.actionType = actionType;
        this.itemType = itemType;
        this.id = id;
        this.beginPos = pos;
        this.glyph = glyph;
        this.color = color;
    }

    //移动物品moveThing
    public NetInfo(String actionType,Tuple<Integer, Integer> beginPos,Tuple<Integer, Integer> destPos) {
        this.actionType = actionType;
        // this.itemType = itemType;
        this.beginPos = beginPos;
        this.destPos = destPos;
    }

    //发射炮弹launchCannonball
    public NetInfo(String actionType,Tuple<Integer, Integer> beginPos,int direction,int ownerId){
        this.actionType = actionType;
        // this.itemType = itemType;
        this.beginPos = beginPos;
        this.direction = direction;
        this.id = ownerId;
    }

    //获准加入admitToJoin 
    public NetInfo(String actionType, int id,Tuple<Integer, Integer> beginPos,Color color) { 
        this.actionType = actionType;
        this.id = id;
        this.beginPos = beginPos;
        this.color = color;
    }

    // 玩家加入playerJoin / 请求开始游戏startGameRequest / 开始游戏startGame / 游戏结束gameOver
    public NetInfo(String actionType) { 
        this.actionType = actionType;
    }

    //玩家加分
    public NetInfo(String actionType,int id){
        this.actionType = actionType;
        this.id = id;
    }

    public void Output() {
        System.out.println(this.toString());
    }

    @Override
    public String toString() {
        String line = "null";
            switch(this.actionType){
            case "setThing": { // 是设置新物品
                line = String.valueOf("setThing") + ' ' +
                        String.valueOf(itemType) + ' ' +
                        String.valueOf(id) + ' ' +
                        String.valueOf(beginPos.first) + ',' + String.valueOf(beginPos.second) + ' ' +
                        String.valueOf(glyph) + ' ' +
                        String.valueOf(this.color.getRed()) + ',' + String.valueOf(this.color.getGreen()) + ','
                        + String.valueOf(this.color.getBlue());
            };break;
            case "moveThing":{ // 是移动
                line = String.valueOf("moveThing") + ' ' +
                        String.valueOf(beginPos.first) + ',' + String.valueOf(beginPos.second) + ' ' +
                        String.valueOf(destPos.first) + ',' + String.valueOf(destPos.second) + ' ';
            } ;break;
            case "launchCannonball":{
                line = String.valueOf("launchCannonball") + ' ' +
                        String.valueOf(beginPos.first) + ',' + String.valueOf(beginPos.second) + ' ' +
                        String.valueOf(direction)+ ' ' +
                        String.valueOf(id);
            };break;
            
            case "admitToJoin":{
                line = String.valueOf("admitToJoin") + ' ' +
                        String.valueOf(id)+ ' ' +
                        String.valueOf(beginPos.first) + ',' + String.valueOf(beginPos.second)+ ' '+
                        String.valueOf(this.color.getRed()) + ',' + String.valueOf(this.color.getGreen()) + ','
                        + String.valueOf(this.color.getBlue());
                        
            };break;
            case "addScore":
            case "playerLeave":
            case "gameOver":{
                line = this.actionType + ' ' +
                        String.valueOf(id);
            };break;
            case "playerJoin":
            case "refuseToJoin":
            case "startGame":
            case "startGameRequest":
            case "resetGame":{
                line = String.valueOf(actionType);
            };break;
        }
        line += "<>";// 分隔符
        return line;
    }
}
