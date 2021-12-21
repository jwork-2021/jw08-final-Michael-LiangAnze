package jw05.anish.map;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;

import jw05.anish.algorithm.Tuple;
import jw05.anish.calabashbros.Thing;
import jw05.anish.calabashbros.World;

import java.awt.Color;
public class MapUpdateRecorder {
    String demoFile;
    ArrayList<MapUpdateInfo> infolist;

    public MapUpdateRecorder() {
        long time = System.currentTimeMillis();
        demoFile = "demo-" + String.valueOf(time) + ".txt";
        infolist = new ArrayList<MapUpdateInfo>();
    }

    public void playDemo(String demoFile,Map map,World world){
        Runnable demoRunnable = new Runnable() {
            @Override
            public void run() {
                String line;
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(demoFile)); // 读取demo文件
                    while ((line = reader.readLine()) != null) { //读取一行指令
                        System.out.println(line);
                        String[]lineInfo = line.split(" ");
                        // for(String s:lineInfo){
                        //     System.out.print(s+" ");
                        // }
                        switch(lineInfo[0]){
                            case "setThing":{
                                // 获取位置信息
                                String[]posInfo = lineInfo[3].split(",");
                                Tuple<Integer,Integer> pos = new Tuple<Integer,Integer>(Integer.parseInt(posInfo[0]),Integer.parseInt(posInfo[1]));
                                //类型信息
                                int type = 1;
                                //获取颜色信息
                                String[]colorInfo = lineInfo[6].split(",");
                                Color color = new Color(Integer.parseInt(colorInfo[0]),Integer.parseInt(colorInfo[1]),Integer.parseInt(colorInfo[2]));
                                //获取字符下标
                                char glyph = (char)Integer.parseInt(lineInfo[5]);
                                //获取设置后的新id
                                int newId = Integer.parseInt(lineInfo[4]);
                                // 创建物品
                                Thing t = new Thing(color,glyph,world);
                                //判断是否为炮弹
                                Boolean isCannonball = lineInfo[2].equals("true")?true:false;
                                map.setThing(pos, type, t, isCannonball);
                            };break;
                            case "moveThing":{
                                
                            };break;
                        }
                        Thread.sleep(100);// 10 fps
                    }
                    reader.close();
                    System.out.println("finish playing demo");      
                    world.setWorldState(5);
                    
                } catch (Exception e) {
                    System.out.println("Fail to replay demo");
                }    
            }
        };
        new Thread(demoRunnable, "Demo thread").start();
        

    }


    public void AddInfo(int id, boolean itemType, boolean moveOrSet, Tuple<Integer, Integer> beginPos,
            Tuple<Integer, Integer> destPos, int newIdAfterSet) {
        infolist.add(new MapUpdateInfo(id, itemType, moveOrSet, beginPos, destPos, newIdAfterSet));
    }

    public void AddInfo(int id, boolean itemType, boolean moveOrSet, Tuple<Integer, Integer> beginPos,
        Tuple<Integer, Integer> destPos, int newIdAfterSet, int glyph, Color color) {
    infolist.add(new MapUpdateInfo(id, itemType, moveOrSet, beginPos, destPos, newIdAfterSet,glyph,color));
    }

    public void saveRecord(){
        try{
            BufferedWriter writer;
            writer = new BufferedWriter(new FileWriter(demoFile));
            Iterator i = infolist.iterator();
            while(i.hasNext()){
                writer.write(i.next().toString() + '\n') ;
                writer.flush();
            }
            writer.close();
        }
        catch(Exception e){
            System.out.println("fail to save demo");
        }
    }
}
