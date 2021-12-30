package jw05.anish.map;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import jw05.anish.algorithm.Tuple;
import jw05.anish.calabashbros.Cannonball;
import jw05.anish.calabashbros.Creature;
import jw05.anish.calabashbros.Floor;
import jw05.anish.calabashbros.Player;
import jw05.anish.calabashbros.Reward;
import jw05.anish.calabashbros.Shooter;
import jw05.anish.calabashbros.SworksMan;
import jw05.anish.calabashbros.World;

import java.awt.Color;

public class MapUpdateRecorder {
    String demoFile;
    ArrayList<MapUpdateInfo> infolist;
    private Lock lock = null;

    public MapUpdateRecorder() {
        long time = System.currentTimeMillis();
        demoFile = "demo-" + String.valueOf(time) + ".txt";
        infolist = new ArrayList<MapUpdateInfo>();
        lock = new ReentrantLock();
    }

    public void playDemo(String demoFile, Map map, World world) {
        ArrayList<Creature> creatureList = new ArrayList<Creature>();
        ArrayList<Player> playerList = new ArrayList<Player>();
        map.setCreatureList(creatureList);
        Runnable demoRunnable = new Runnable() {
            @Override
            public void run() {
                String line;
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(demoFile)); // 读取demo文件
                    while ((line = reader.readLine()) != null) { // 读取一行指令
                        // System.out.println(line);
                        String[] lineInfo = line.split(" ");
                        switch (lineInfo[0]) {
                            case "setThing": {
                                // 获取位置信息
                                String[] posInfo = lineInfo[3].split(",");
                                Tuple<Integer, Integer> pos = new Tuple<Integer, Integer>(Integer.parseInt(posInfo[0]),
                                        Integer.parseInt(posInfo[1]));
                                // 类型信息
                                int type = 1;
                                // 获取颜色信息
                                String[] colorInfo = lineInfo[6].split(",");
                                Color color = new Color(Integer.parseInt(colorInfo[0]), Integer.parseInt(colorInfo[1]),
                                        Integer.parseInt(colorInfo[2]));
                                // 获取字符下标
                                char glyph = (char) Integer.parseInt(lineInfo[5]);
                                // 获取设置后的新id
                                int newId = Integer.parseInt(lineInfo[4]);

                                // 创建物品
                                switch (lineInfo[2]) {
                                    case "player": {
                                        Player player = new Player(color, 0, 100, 6, world, map, null);
                                        player.setId(newId);
                                        creatureList.add(player);
                                        playerList.add(player);
                                        map.setThing(pos, type, player);
                                        world.updateOnlineGamingInfo(playerList, -2);
                                    }
                                        ;
                                        break;
                                    case "sworksMan": {
                                        SworksMan sworksMan = new SworksMan(0, 100, 0, 0, 3, world, map, null, 0, 0, 0,
                                                0);
                                        sworksMan.setId(newId);
                                        creatureList.add(sworksMan);
                                        map.setThing(pos, type, sworksMan);
                                    }
                                        ;
                                        break;
                                    case "shooter": {
                                        Shooter shooter = new Shooter(0, 100, 2, world, map, null, null, 0, 0, 0, 0);
                                        shooter.setId(newId);
                                        creatureList.add(shooter);
                                        map.setThing(pos, type, shooter);
                                    }
                                        ;
                                        break;
                                    case "reward": {
                                        Reward reward = new Reward(color, (int) glyph, world);
                                        reward.setId(newId);
                                        map.setThing(pos, type, reward);
                                    }
                                        ;
                                        break;
                                    case "cannonball": {
                                        Cannonball cannonball = new Cannonball(0, 0, world);
                                        cannonball.setId(newId);
                                        map.setThing(pos, type, cannonball);
                                        if(world.get(pos.first, pos.second).getType().equals("player")){
                                            world.updateOnlineGamingInfo(playerList, -2);
                                        }   
                                    }
                                        ;
                                        break;
                                }       
                            }
                                ;
                                break;
                            case "moveThing": {
                                // get pos
                                String[] beginPosInfo = lineInfo[3].split(",");
                                String[] destPosInfo = lineInfo[4].split(",");
                                Tuple<Integer, Integer> beginPos = new Tuple<Integer, Integer>(
                                        Integer.parseInt(beginPosInfo[0]), Integer.parseInt(beginPosInfo[1]));
                                Tuple<Integer, Integer> destPos = new Tuple<Integer, Integer>(
                                        Integer.parseInt(destPosInfo[0]), Integer.parseInt(destPosInfo[1]));

                                // get type
                                String type = world.get(destPos.first, destPos.second).getType();
                                // move thing
                                map.moveThing(beginPos, destPos);
                                if(type.equals("player")){
                                    world.updateOnlineGamingInfo(playerList, -2);
                                }
                            }
                                ;
                                break;
                            case "beAttacked": {
                                int id = Integer.parseInt(lineInfo[1]);
                                for (Creature c : creatureList) {
                                    if (c.getId() == id) {
                                        c.beAttack(1);
                                        if (c.getHp() <= 0) {
                                            map.setThing(c.getPos(), 0, new Floor(world));
                                        }
                                        break;
                                    }
                                }
                                world.updateOnlineGamingInfo(playerList, -2);
                            }
                                ;
                                break;
                        }
                        Thread.sleep(100);
                    }
                    reader.close();
                    System.out.println("finish replaying demo");
                    world.setWorldState(5);

                } catch (Exception e) {
                    System.out.println("Fail to replay demo");
                    e.printStackTrace();
                    // System.out.println();
                }
            }
        };
        new Thread(demoRunnable, "Demo thread").start();

    }

    public void AddMoveThingInfo(int id, String itemType, Tuple<Integer, Integer> beginPos,
            Tuple<Integer, Integer> destPos, int newIdAfterSet) {
        lock.lock();
        infolist.add(new MapUpdateInfo(id, itemType, "moveThing", beginPos, destPos, newIdAfterSet));
        lock.unlock();
    }

    public void AddSetThingInfo(int id, String itemType, Tuple<Integer, Integer> beginPos,
            Tuple<Integer, Integer> destPos, int newIdAfterSet, int glyph, Color color) {
        lock.lock();
        infolist.add(new MapUpdateInfo(id, itemType, "setThing", beginPos, destPos, newIdAfterSet, glyph, color));
        lock.unlock();
    }

    public void AddCloseAttackInfo(int id) {
        lock.lock();
        infolist.add(new MapUpdateInfo(id, "beAttacked"));
        lock.unlock();
    }

    public void AddLaunchAttackInfo(int id, int direction) {
        lock.lock();
        infolist.add(new MapUpdateInfo(id, direction, "launch"));
        lock.unlock();
    }

    public void saveRecord() {
        try {
            BufferedWriter writer;
            writer = new BufferedWriter(new FileWriter(demoFile));
            Iterator i = infolist.iterator();
            while (i.hasNext()) {
                writer.write(i.next().toString() + '\n');
                writer.flush();
            }
            writer.close();
        } catch (Exception e) {
            System.out.println("fail to save demo");
        }
    }
}
