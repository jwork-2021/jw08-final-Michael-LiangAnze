package jw05.anish.map;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import jw05.anish.algorithm.Tuple;
import jw05.anish.calabashbros.Cannonball;
import jw05.anish.calabashbros.Creature;
import jw05.anish.calabashbros.Floor;
import jw05.anish.calabashbros.MapItem;
import jw05.anish.calabashbros.Player;
import jw05.anish.calabashbros.Thing;
import jw05.anish.calabashbros.World;
import jw05.anish.net.Server;
import jw05.asciiPanel.AsciiPanel;

public class Map {
    private int[][] map;
    private final int mapSize = 40;
    private String mapFile;
    private Lock lock = null;
    private ArrayList<Creature> creatureList;
    World world;
    int idCount = 0;
    MapUpdateRecorder recoreder = null;
    Server server = null;

    public Map(World world, boolean isRecord) {
        this.mapFile = (new File("")).getAbsolutePath() + "\\src\\main\\java\\jw05\\anish\\map\\map1.txt";
        this.map = new int[mapSize][mapSize];// 0为可行，1为玩家、炮弹、或者敌人，其余为地图元素
        lock = new ReentrantLock(); // 可重入锁，防止冲突
        this.world = world;// 每次一修改地图的状态，马上对world修改，防止出现问题
        if (isRecord) {
            recoreder = new MapUpdateRecorder();
        }
    }

    public void setServer(Server s){
        this.server = s;
    }
    
    public String getMapFile() {
        return this.mapFile;
    }

    public void setCreatureList(ArrayList<Creature> creatureList) {
        this.creatureList = creatureList;
    }

    public void loadMap(){
        int i = 0;
        Scanner s = null;
        String str = null;
        try {
            s = new Scanner(new BufferedReader(new FileReader(mapFile)));
            while (s.hasNextLine()) {
                str = s.nextLine();
                String[] line = str.split(" ");
                for (int j = 0; j < mapSize; ++j) {
                    map[j][i] = Integer.parseInt(line[j]);
                }
                i++;
            }
            for (i = 0; i < mapSize; i++) {
                for (int j = 0; j < mapSize; j++) {
                    switch (map[i][j]) {
                        case 1://1为石墙
                            world.put(new MapItem(new Color(220, 220, 220), 177, this.world),
                                    new Tuple<Integer, Integer>(i, j));
                            break;
                        case 2:// 2为水
                            world.put(new MapItem(new Color(30, 144, 255), 156, this.world),
                                    new Tuple<Integer, Integer>(i, j));
                            break;
                        case 3:// 3为树1
                            world.put(new MapItem(AsciiPanel.green, 24, this.world), new Tuple<Integer, Integer>(i, j));
                            break;
                        case 4:// 4为门
                            world.put(new MapItem(new Color(255, 193, 37), 35, this.world),
                                    new Tuple<Integer, Integer>(i, j));
                            break;
                        case 5:// 5为树2
                            world.put(new MapItem(AsciiPanel.green, 6, this.world), new Tuple<Integer, Integer>(i, j));
                            break;
                        case 6:// 6为草
                            world.put(new MapItem(AsciiPanel.green, 231, this.world),
                                    new Tuple<Integer, Integer>(i, j));
                            break;
                        case 7:// 7为原木
                            world.put(new MapItem(new Color(222, 184, 135), 22, this.world),
                                    new Tuple<Integer, Integer>(i, j));
                            break;
                        case 8:// 8为沙
                            world.put(new MapItem(new Color(255, 250, 205), 176, this.world),
                                    new Tuple<Integer, Integer>(i, j));
                            break;
                        case 9:// 9为帐篷
                            world.put(new MapItem(new Color(139, 69, 19), 65, this.world),
                                    new Tuple<Integer, Integer>(i, j));
                            break;
                    }
                }
            }
        }
        catch(IOException e){
            System.err.println("Map file not found\n");
            System.exit(-1);
        } 
        finally {
            if (s != null) {
                s.close();
            }
        }
    }

    public void outputMap() {
        for (int i = 0; i < mapSize; ++i) {
            for (int j = 0; j < mapSize; ++j) {
                System.out.print(map[j][i] + " ");
            }
            System.out.print('\n');
        }
        System.out.print('\n');
    }

    public void getMapState(int[][] buffer) { // 旨在获取地图一瞬间的状态
        lock.lock();
        for (int i = 0; i < mapSize; ++i) {
            for (int j = 0; j < mapSize; ++j) {
                buffer[i][j] = map[i][j];
            }
        }
        lock.unlock();
    }

    public int getMapSize() {
        return mapSize;
    }

    private void cleanBlock(Tuple<Integer, Integer> pos) {
        world.put(new Floor(world), pos);
    }

    // 攻击产生的伤害应当发生在以下两个函数
    // 1.炮弹刚好产生在目标所在的地方，目标立即收到伤害
    // 2.炮弹刚好移动到目标的位置，目标立即受到伤害

    public synchronized boolean moveThing(Tuple<Integer, Integer> beginPos, Tuple<Integer, Integer> destPos) {
        boolean res = false;
        lock.lock();
        Thing t = world.get(beginPos.first, beginPos.second);
        String type = t.getType();
        if (recoreder != null) { // 确定录像
            recoreder.AddMoveThingInfo(world.get(beginPos.first, beginPos.second).getId(), type, beginPos, destPos, -1);
        }
        if (map[destPos.first][destPos.second] == 0) {// 允许移动
            int temp = map[beginPos.first][beginPos.second];
            map[beginPos.first][beginPos.second] = map[destPos.first][destPos.second];
            map[destPos.first][destPos.second] = temp;
            world.swapPos(beginPos, destPos);
            res = true;
        } else { // 该位置不可移动
            if (type.equals("cannonball")) { // 该物体为炮弹
                map[beginPos.first][beginPos.second] = 0; // 将炮弹位置清空
                cleanBlock(beginPos); // 将炮弹位置清空
                int index = -1;
                Tuple<Integer, Integer> tempPos;
                for (int i = 0; i < creatureList.size(); ++i) {
                    tempPos = creatureList.get(i).getPos();
                    if (destPos.first == tempPos.first && destPos.second == tempPos.second) {
                        creatureList.get(i).beAttack(1);
                        if(server != null){
                            Cannonball c = (Cannonball)t;
                            server.addPlayerScore(c.getOwner());
                        }
                        if (creatureList.get(i).getHp() <= 0) { // 被攻击生物死亡
                            cleanBlock(tempPos); // 清理格子
                            map[tempPos.first][tempPos.second] = 0;// 清空坐标
                            index = i;
                        }
                        break;
                    }
                }
                if (index != -1) { // 移除一个生物
                    creatureList.remove(index);

                    // 下面确定游戏状态及录像保存
                    if (world.getWorldState() < 6) { // 单机模式
                        boolean isSave = false; // 游戏结束，保存录像
                        if (creatureList.size() == 1 && creatureList.get(0).getType().equals("player")) {// 情况一，只剩下玩家一人，此为单机模式胜利条件
                            world.setWorldState(2); // 胜利
                            isSave = true;
                        } else { // 情况二，没有玩家，失败
                            boolean remain = false;
                            for (Creature c : creatureList) {
                                if (c.getType().equals("player")) {
                                    remain = true;
                                    break;
                                }
                            }
                            if (!remain) {
                                world.setWorldState(3);
                                isSave = true;
                            }
                        }
                        if (isSave && recoreder != null) {
                            recoreder.saveRecord();
                        }
                    } else { // 多人模式
                        if (creatureList.size() == 1) {
                            world.setWorldState(9);
                        }

                    }
                }
            } else if (type.equals("player")) {// 该物体为玩家，判断玩家移动的目标位置是否有奖励，有则移动并获奖
                // System.out.println("moving a blocked polayer");
                // if (map[destPos.first][destPos.second] == 99) { // 是奖励
                if (world.get(destPos.first, destPos.second).getType().equals("reward")) {// 是奖励
                    // 首先清空位置
                    map[destPos.first][destPos.second] = 0;
                    world.put(new Floor(world), destPos);
                    // 然后移动玩家
                    int temp = map[beginPos.first][beginPos.second];
                    map[beginPos.first][beginPos.second] = map[destPos.first][destPos.second];
                    map[destPos.first][destPos.second] = temp;
                    world.swapPos(beginPos, destPos);
                    // 然后对玩家更新信息
                    Player p;
                    Tuple<Integer, Integer> tempPos;
                    for (int i = 0; i < creatureList.size(); ++i) {
                        tempPos = creatureList.get(i).getPos();
                        if (destPos.first == tempPos.first && destPos.second == tempPos.second) {
                            p = (Player) creatureList.get(i);
                            p.getReward();
                            break;
                        }
                    }
                }
            }
            res = false;
        }
        lock.unlock();
        return res;
    }

    public synchronized boolean setThing(Tuple<Integer, Integer> pos, int type, Thing t) {
        boolean res = false;
        lock.lock();
        if (map[pos.first][pos.second] == 0) { // 位置为空
            map[pos.first][pos.second] = type;
            world.put(t, pos);
            res = true;
        } else { // 位置不为空
            int index = -1;
            if (t.getType().equals("cannonball")) {
                Tuple<Integer, Integer> tempPos;
                for (int i = 0; i < creatureList.size(); ++i) {
                    tempPos = creatureList.get(i).getPos();
                    if (pos.first == tempPos.first && pos.second == tempPos.second) {
                        creatureList.get(i).beAttack(1);
                        if(server != null){
                            Cannonball c = (Cannonball)t;
                            server.addPlayerScore(c.getOwner());
                        }
                        if (creatureList.get(i).getHp() <= 0) {
                            cleanBlock(tempPos);
                            map[tempPos.first][tempPos.second] = 0;// 清空坐标
                            index = i;
                        }
                        break;
                    }
                }
                if (index != -1) { // 移除一个生物
                    creatureList.remove(index);

                    // 下面确定游戏状态及录像保存
                    if (world.getWorldState() < 6) {// 单人模式
                        boolean isSave = false; // 游戏结束，保存录像
                        if (creatureList.size() == 1 && creatureList.get(0).getType().equals("player")) {// 情况一，只剩下玩家一人，此为单机模式胜利条件
                            world.setWorldState(2); // 胜利
                            isSave = true;
                        } else { // 情况二，没有玩家，失败
                            boolean remain = false;
                            for (Creature c : creatureList) {
                                if (c.getType().equals("player")) {
                                    remain = true;
                                    break;
                                }
                            }
                            if (!remain) {
                                world.setWorldState(3);
                                isSave = true;
                            }
                        }
                        if (isSave && recoreder != null) {
                            recoreder.saveRecord();
                        }
                    }
                    else{ //多人模式
                        if (creatureList.size() == 1) {
                            world.setWorldState(9);
                        }
                    }
                }
            } else if (world.getWorldState() == 4) { // demo模式，强制设置
                map[pos.first][pos.second] = type;
                world.put(t, pos);
                res = true;
            }
        }

        // 分配id
        if (res) {
            t.setId(idCount);
            idCount++;
        }
        // 记录信息并输出
        if (recoreder != null) {
            recoreder.AddSetThingInfo(-1, t.getType(), pos, null, t.getId(), (int) t.getGlyph(),
                    t.getColor());
        }

        lock.unlock();
        return res;
    }

    public void playerBeAttacked(int id) { // 只用于单人模式记录近战伤害
        lock.lock();
        Tuple<Integer, Integer> tempPos;
        int index = -1;
        for (int i = 0; i < creatureList.size(); ++i) {
            if (creatureList.get(i).getId() == id) {
                tempPos = creatureList.get(i).getPos();
                creatureList.get(i).beAttack(1);
                if (creatureList.get(i).getHp() <= 0) {
                    index = i;
                    cleanBlock(tempPos);
                    map[tempPos.first][tempPos.second] = 0;// 清空坐标
                }
                break;
            }
        }
        if (recoreder != null) {
            recoreder.AddCloseAttackInfo(id); // 添加记录
        }
        if (index != -1) { // 移除一个生物
            creatureList.remove(index);
            // 下面确定游戏状态及录像保存
            boolean remain = true; // 游戏结束，保存录像
            for (Creature c : creatureList) {
                if (c.getType().equals("player")) {
                    remain = false; // 还有玩家，未结束
                    break;
                }
            }
            if (remain) { // 没有玩家，保存录像
                world.setWorldState(3);
                if (recoreder != null) {
                    recoreder.saveRecord();
                }
            }
        }

        lock.unlock();
    }
}
