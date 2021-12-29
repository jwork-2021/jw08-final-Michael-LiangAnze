package jw05.anish.screen;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.text.html.HTMLDocument.BlockElement;

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
import jw05.anish.net.Client;
import jw05.anish.net.Server;

public class WorldScreen implements Screen {

    //
    private World world;
    private Map map;
    private Player player;
    private CannonballList cannonballList;
    private ArrayList<Creature> creatureList;
    private ExecutorService exec;
    
    //初始化游戏获得参数
    private boolean isOnline = false;
    private boolean isRecord = false;
    private boolean ifPlayDemo = false;
    private boolean clientOrServer = false; //默认为客户端
    private String ip = null;
    private String demoFile = null;

    //网络相关
    private int port;
    private Client client= null;
    private Server server;

    public WorldScreen(String[]args) { //接受传入的参数
        // 对传入的参数进行解析
        parseArgs(args);
        
        //其余参数初始化
        world = new World();

        if(args.length == 0){//没有参数，默认单人游戏
            System.out.println("Standalone game");
            world.setWorldState(0);
            rulesScreen();
        }
        else if(!ifPlayDemo && isRecord && !isOnline){ //录制单人游戏
            System.out.println("Standalone game with record");
            world.setWorldState(0);
            rulesScreen();
        }
        else if(ifPlayDemo){ //播放demo
            System.out.println("Replaying demo");
            demoScreen();
        }
        else if(isOnline){ // 在线游戏
            rulesScreen();
            if(!clientOrServer){ //客户端
                world.setWorldState(7);
                onlineGameScreen();
                this.client = new Client(ip,port,false,world, map);
                // System.out.println("start game as client");
            }
            else{ // 服务器端
                // world.setWorldState(6);
                world.setWorldState(6);
                onlineGameScreen();
                this.server = new Server(port,world, map);
                map.setServer(server);
                this.client = new Client("localhost",port,true,world, map);
                this.server.setServerOwner(this.client);
                // System.out.println("start game as server");
            }
        }
        else{
            System.out.println("Wrong arguments!");
            System.exit(-1);
        }
    }

    private void parseArgs(String[]args){
        for(String s:args){
            if(s.substring(0, 1).equals("-")){
                s = s.toLowerCase();
            }
        }
        //是否录制
        for(String temp:args){
            if(temp.equals("-record")){
                this.isRecord = true;
                break;
            }
        }
        //是否播放demo
        for(int i = 0;i < args.length;++i){
            if(args[i].equals("-demo") && i != args.length - 1){ 
                this.ifPlayDemo = true;
                this.demoFile = args[i + 1];
                break;
            }
        }

        // 是否为客户端
        for(int i = 0;i < args.length;++i){
            if(args[i].equals("-client")){ // 在线模式
                if(i >= args.length - 2){
                    System.out.println("lack of argument:server ip,server port");
                    System.exit(-1);
                }
                this.isOnline = true;
                clientOrServer = false;
                this.ip = args[i + 1];
                this.port = Integer.parseInt(args[i + 2]);
                break;
            }
        }
        //是否为服务器端
        for(int i = 0;i < args.length;++i){
            if(args[i].equals("-server") && this.ip == null){ 
                System.out.println(args.length);
                if(i == args.length - 1){
                    System.out.println("lack of argument:port");
                    System.exit(-1);
                }
                this.isOnline = true;
                this.clientOrServer = true;
                this.port = Integer.parseInt(args[i + 1]);
                break;
            }
        }
        // 处理矛盾情况
        if(ifPlayDemo && isRecord){
            isRecord = false;
        }
    }
    private void loadMapFile(boolean isRecord) {
            map = new Map(world,isRecord);
            map.loadMap();
    }

    @Override
    public void rulesScreen() {
        world.setRulesWorld();
    }

    @Override
    public void standAloneGameScreen() {
        world.setGamingWorld();
        loadMapFile(isRecord);
        creatureList = new ArrayList<Creature>();
        map.setCreatureList(creatureList);
        cannonballList = new CannonballList(1, 300, map, world);

        // 创建人物和道具
        player = new Player(new Color(0, 245, 255), 1, 500, 6, world, map, cannonballList);

        Shooter shooter1 = new Shooter(1, 100, 2, world, map, player, cannonballList,
        18, 1, 27, 10);
        Shooter shooter2 = new Shooter(1, 100, 2, world, map, player, cannonballList,
        30, 8, 37, 10);
        Shooter shooter3 = new Shooter(1, 100, 2, world, map, player, cannonballList,
        27, 18, 37, 21);

        SworksMan sworksMan1 = new SworksMan(1, 150, 8,1, 3, world,
        map,player,2,2,17,11);
        SworksMan sworksMan2 = new SworksMan(1, 150, 8,1, 3, world,
        map,player,31,1,38,6);
        SworksMan sworksMan3 = new SworksMan(1, 150, 9,1, 3, world,
        map,player,1,23,16,37);
        SworksMan sworksMan4 = new SworksMan(1, 150, 8,1, 3, world,
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
    public void standAloneGameOverScreen() {
        world.setStandAloneGameOverWorld();
    }

    @Override
    public void onlineGameWinScreen() {
        world.setOnlineGameWinWorld();
        
    }

    @Override
    public void onlineGameLostScreen() {
        world.setOnlineGameLostWorld();
    }
    @Override
    public void demoScreen() {
        world.setWorldState(4);
        world.setGamingWorld();
        loadMapFile(false);
        MapUpdateRecorder mur = new MapUpdateRecorder();
        mur.playDemo(this.demoFile, this.map,this.world);
    }

    @Override
    public void onlineGameScreen() {
        world.setGamingWorld();
        loadMapFile(isRecord);
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
                standAloneGameScreen();
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
        else if(world.getWorldState() > 5){
            this.client.handleKeyEvent(key);
        }
        
        return this;
    }
}


