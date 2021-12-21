package jw05.anish.map;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import jw05.anish.algorithm.Tuple;
import jw05.anish.calabashbros.Creature;
import jw05.anish.calabashbros.Floor;
import jw05.anish.calabashbros.Player;
import jw05.anish.calabashbros.Thing;
import jw05.anish.calabashbros.World;

public class Map {
    private int[][] map;
    private final int mapSize = 40;
    private String mapFile;
    private Lock lock = null;
    private ArrayList<Creature> creatureList;
    World world;
    int idCount = 100; // 避开冲突
    MapUpdateRecorder recoreder;

    public Map(World world) {
        this.mapFile = (new File("")).getAbsolutePath() + "\\src\\main\\java\\jw05\\anish\\map\\map1.txt";
        this.map = new int[mapSize][mapSize];// 0为可行，1为玩家、炮弹、或者敌人，其余为地图元素
        lock = new ReentrantLock(); // 可重入锁，防止冲突
        this.world = world;// 每次一修改地图的状态，马上对world修改，防止出现问题
        recoreder = new MapUpdateRecorder();
    }

    public String getMapFile() {
        return this.mapFile;
    }

    public void setCreatureList(ArrayList<Creature> creatureList) {
        this.creatureList = creatureList;
    }

    public void loadMap() throws IOException {
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
        } finally {
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
        String type = world.get(beginPos.first, beginPos.second).getType(); //获取移动物品的类型
        recoreder.AddInfo(world.get(beginPos.first, beginPos.second).getId(), type, "moveThing", beginPos, destPos,-1);
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
                        if (creatureList.get(i).getHp() <= 0) { // 被攻击生物死亡
                            cleanBlock(tempPos); // 清理格子
                            map[tempPos.first][tempPos.second] = 0;// 清空坐标
                            index = i;
                        }
                        break;
                    }
                }
                if (index != -1) {
                    creatureList.remove(index);
                }
                if (creatureList.size() == 1) {
                    world.setWorldState(2);
                    recoreder.saveRecord();
                }
            } else if (type.equals("player")) {// 该物体为玩家，判断玩家移动的目标位置是否有奖励，有则移动并获奖
                // System.out.println("moving a blocked polayer");
                // if (map[destPos.first][destPos.second] == 99) { // 是奖励
                if(world.get(destPos.first, destPos.second).getType().equals("reward")){// 是奖励
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
                        if (creatureList.get(i).getHp() <= 0) {
                            cleanBlock(tempPos);
                            map[tempPos.first][tempPos.second] = 0;// 清空坐标
                            index = i;
                        }
                        break;
                    }
                }
                if (index != -1) {
                    creatureList.remove(index);
                }
            }
            if (creatureList.size() == 1) {
                world.setWorldState(2);
                recoreder.saveRecord();
            }
        }
        // 记录信息并输出
        recoreder.AddInfo(t.getId(), t.getType(), "setThing", pos, null, res ? idCount++ : -1, (int) t.getGlyph(),
                t.getColor());
        lock.unlock();
        return res;
    }
}
