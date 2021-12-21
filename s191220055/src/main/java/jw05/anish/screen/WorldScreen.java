package jw05.anish.screen;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jw05.anish.calabashbros.Player;
import jw05.anish.calabashbros.Shooter;
import jw05.anish.calabashbros.SworksMan;
import jw05.anish.algorithm.Tuple;
import jw05.anish.calabashbros.Reward;
import jw05.anish.calabashbros.CannonballList;
import jw05.anish.calabashbros.Creature;
import jw05.anish.calabashbros.MapItem;
import jw05.anish.calabashbros.World;
import jw05.asciiPanel.AsciiPanel;
import jw05.anish.map.Map;
import jw05.anish.map.MapUpdateRecorder;

public class WorldScreen implements Screen {

    private World world;
    private Map map;
    private Player player;
    private CannonballList cannonballList;
    private ArrayList<Creature> creatureList;
    private ExecutorService exec;
    private int idCount = 0;
    private String ipOrDemoFile;

    public WorldScreen(boolean isOnline, boolean isDemo, String ipOrDemoFile) {
        System.out.println("get args:" + isOnline + " " + isDemo + " " + ipOrDemoFile);
        this.ipOrDemoFile = ipOrDemoFile;
        world = new World();
        if (isDemo) { // 播放demo
            demoScreen();
        } else if (!isOnline) { // 单人模式
            world.setWorldState(0);
        } else { // 多人模式
            System.exit(-1);
        }
    }

    private void loadMapFile() {
        try {
            map = new Map(world);
            map.loadMap();
            // System.out.println(map.getMapFile());
            int mapSize = map.getMapSize();
            int[][] tempMap = new int[mapSize][mapSize];
            map.getMapState(tempMap);
            for (int i = 0; i < mapSize; i++) {
                for (int j = 0; j < mapSize; j++) {
                    // 对于地图物件的规定：
                    // 必须在1~9之间
                    // 1为石墙
                    // 2为水
                    // 3为树1
                    // 4为门
                    // 5为树2
                    // 6为草
                    // 7为原木
                    // 8为沙
                    // 9为帐篷
                    switch (tempMap[i][j]) {
                        case 1:
                            world.put(new MapItem(new Color(220, 220, 220), 177, this.world),
                                    new Tuple<Integer, Integer>(i, j));
                            break;
                        case 2:
                            world.put(new MapItem(new Color(30, 144, 255), 156, this.world),
                                    new Tuple<Integer, Integer>(i, j));
                            break;
                        case 3:
                            world.put(new MapItem(AsciiPanel.green, 24, this.world), new Tuple<Integer, Integer>(i, j));
                            break;
                        case 4:
                            world.put(new MapItem(new Color(255, 193, 37), 35, this.world),
                                    new Tuple<Integer, Integer>(i, j));
                            break;
                        case 5:
                            world.put(new MapItem(AsciiPanel.green, 6, this.world), new Tuple<Integer, Integer>(i, j));
                            break;
                        case 6:
                            world.put(new MapItem(AsciiPanel.green, 231, this.world),
                                    new Tuple<Integer, Integer>(i, j));
                            break;
                        case 7:
                            world.put(new MapItem(new Color(222, 184, 135), 22, this.world),
                                    new Tuple<Integer, Integer>(i, j));
                            break;
                        case 8:
                            world.put(new MapItem(new Color(255, 250, 205), 176, this.world),
                                    new Tuple<Integer, Integer>(i, j));
                            break;
                        case 9:
                            world.put(new MapItem(new Color(139, 69, 19), 65, this.world),
                                    new Tuple<Integer, Integer>(i, j));
                            break;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Map file not found\n");
            System.exit(-1);
        }
    }

    @Override
    public void rulesScreen() {
        world.setRulesWorld();
    }

    private int assignId() {
        int temp = idCount;
        idCount++;
        return temp;
    }

    @Override
    public void gamingScreen() {
        world.setGamingWorld();
        loadMapFile();
        creatureList = new ArrayList<Creature>();
        map.setCreatureList(creatureList);
        cannonballList = new CannonballList(1, 300, map, world);

        // 创建人物和道具
        player = new Player(new Color(0, 245, 255), 1, 500, 4, world, map, cannonballList);

        Shooter shooter1 = new Shooter(1, 100, 1, world, map, player, cannonballList,
        18, 1, 27, 10);
        Shooter shooter2 = new Shooter(1, 100, 1, world, map, player, cannonballList,
        30, 8, 37, 10);
        Shooter shooter3 = new Shooter(1, 100, 1, world, map, player, cannonballList,
        27, 18, 37, 21);

        SworksMan sworksMan1 = new SworksMan(1, 150, 8,1, 2, world,
        map,player,2,2,17,11);
        SworksMan sworksMan2 = new SworksMan(1, 150, 8,1, 2, world,
        map,player,31,1,38,6);
        SworksMan sworksMan3 = new SworksMan(1, 150, 9,1, 2, world,
        map,player,1,23,16,37);
        SworksMan sworksMan4 = new SworksMan(1, 150, 8,1, 2, world,
        map,player,24,24,36,37);

        Reward reward1 = new Reward(new Color(255, 222, 173), 224, world);
        Reward reward2 = new Reward(new Color(0, 191, 255), 173, world);
        Reward reward3 = new Reward(new Color(255, 222, 173), 224, world);
        Reward reward4 = new Reward(new Color(255, 222, 173), 224, world);
        Reward reward5 = new Reward(new Color(255, 222, 173), 224, world);
        Reward reward6 = new Reward(new Color(0, 191, 255), 173, world);

        // 将生物添加到队列中
        creatureList.add(player);

        creatureList.add(shooter1);
        creatureList.add(shooter2);
        creatureList.add(shooter3);

        creatureList.add(sworksMan1);
        creatureList.add(sworksMan2);
        creatureList.add(sworksMan3);
        creatureList.add(sworksMan4);

        // 为生物分配id
        player.setId(assignId());

        shooter1.setId(assignId());
        shooter2.setId(assignId());
        shooter3.setId(assignId());

        sworksMan1.setId(assignId());
        sworksMan2.setId(assignId());
        sworksMan3.setId(assignId());
        sworksMan4.setId(assignId());

        // 创建位置
        Tuple<Integer, Integer> playerPos = new Tuple<Integer, Integer>(20, 20);

        Tuple<Integer, Integer> shooter1Pos = new Tuple<Integer, Integer>(24, 6);
        Tuple<Integer, Integer> shooter2Pos = new Tuple<Integer, Integer>(32, 9);
        Tuple<Integer, Integer> shooter3Pos = new Tuple<Integer, Integer>(33, 19);

        Tuple<Integer, Integer> sworksMan1Pos = new Tuple<Integer, Integer>(10, 7);
        Tuple<Integer, Integer> sworksMan2Pos = new Tuple<Integer, Integer>(33, 3);
        Tuple<Integer, Integer> sworksMan3Pos = new Tuple<Integer, Integer>(10, 30);
        Tuple<Integer, Integer> sworksMan4Pos = new Tuple<Integer, Integer>(31, 30);

        Tuple<Integer, Integer> reward1Pos = new Tuple<Integer, Integer>(6, 6);
        Tuple<Integer, Integer> reward2Pos = new Tuple<Integer, Integer>(37, 2);
        Tuple<Integer, Integer> reward3Pos = new Tuple<Integer, Integer>(31, 14);
        Tuple<Integer, Integer> reward4Pos = new Tuple<Integer, Integer>(6, 34);
        Tuple<Integer, Integer> reward5Pos = new Tuple<Integer, Integer>(31, 31);
        Tuple<Integer, Integer> reward6Pos = new Tuple<Integer, Integer>(18, 18);

        // 设置地图及世界,是否需要和上一部分互换位置？
        map.setThing(playerPos, 1, player);

        map.setThing(shooter1Pos, 1,shooter1);
        map.setThing(shooter2Pos, 1,shooter2);
        map.setThing(shooter3Pos, 1,shooter3);

        map.setThing(sworksMan1Pos, 1,sworksMan1);
        map.setThing(sworksMan2Pos, 1,sworksMan2);
        map.setThing(sworksMan3Pos, 1,sworksMan3);
        map.setThing(sworksMan4Pos, 1,sworksMan4);

        map.setThing(reward1Pos, 99, reward1);
        map.setThing(reward2Pos, 99, reward2);
        map.setThing(reward3Pos, 99, reward3);
        map.setThing(reward4Pos, 99, reward4);
        map.setThing(reward5Pos, 99, reward5);
        map.setThing(reward6Pos, 99, reward6);

        // 设置并启动线程
        exec = Executors.newCachedThreadPool();
        exec.execute(new Thread(player));
        exec.execute(new Thread(cannonballList));
        exec.execute(new Thread(shooter1));
        exec.execute(new Thread(shooter2));
        exec.execute(new Thread(shooter3));

        exec.execute(new Thread(sworksMan1));
        exec.execute(new Thread(sworksMan2));
        exec.execute(new Thread(sworksMan3));
        exec.execute(new Thread(sworksMan4));
    }

    @Override
    public void gameOverScreen() {
        world.setGameOverWorld();
    }

    @Override
    public void demoScreen() {
        world.setWorldState(4);
        world.setGamingWorld();
        loadMapFile();
        MapUpdateRecorder mur = new MapUpdateRecorder();
        mur.playDemo(this.ipOrDemoFile, this.map,this.world);
    }

    @Override
    public int getScreenState() {
        return world.getWorldState();
    }

    @Override
    public ExecutorService getThreadPool() {
        return exec;
    }

    @Override
    public void displayOutput(AsciiPanel terminal) {

        for (int x = 0; x < World.WIDTH; x++) {
            for (int y = 0; y < World.HEIGHT; y++) {
                terminal.write(world.get(x, y).getGlyph(), x, y, world.get(x, y).getColor());
            }
        }
    }

    @Override
    public Screen releaseKey() {
        if (world.getWorldState() == 1) {
            player.resetDirection();
        }
        return this;
    }

    @Override
    public Screen respondToUserInput(KeyEvent key) {
        if (world.getWorldState() == 0) { // 开始界面
            if (key.getKeyCode() == KeyEvent.VK_ENTER) {
                world.setWorldState(1);
                gamingScreen();
            }
        } else if (world.getWorldState() == 1) {
            switch (key.getKeyCode()) {
                case KeyEvent.VK_W:
                    player.movePlayer(2);
                    break;
                case KeyEvent.VK_S:
                    player.movePlayer(1);
                    break;
                case KeyEvent.VK_A:
                    player.movePlayer(3);
                    break;
                case KeyEvent.VK_D:
                    player.movePlayer(4);
                    break;
                case KeyEvent.VK_SPACE:
                    player.setAttackState();
            }
        }
        return this;
    }
}
