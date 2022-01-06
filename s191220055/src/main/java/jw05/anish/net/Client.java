package jw05.anish.net;

import java.awt.event.KeyEvent;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

import jw05.anish.algorithm.Tuple;
import jw05.anish.calabashbros.Cannonball;
import jw05.anish.calabashbros.Creature;
import jw05.anish.calabashbros.Floor;
import jw05.anish.calabashbros.Player;
import jw05.anish.calabashbros.World;
import jw05.anish.map.Map;
import java.awt.Color;

public class Client {
    private String serverIp;
    private int serverPort;
    private World world;
    private Map map;

    private SocketChannel client;
    private InetSocketAddress serverAddress;
    private ByteBuffer readBuffer = ByteBuffer.allocate(1024);
    private ByteBuffer writeBuffer = ByteBuffer.allocate(1024);
    private Boolean connect = false;// 是否连接了服务器
    private Boolean joinInGame = false; // 是否加入了对局
    private boolean isServerOwner = false;

    private ArrayList<Player> playerList = new ArrayList<Player>();
    private ArrayList<Creature> creatureList = new ArrayList<Creature>();
    private HashMap<Integer,Tuple<Integer,Integer>>originalPos = new HashMap<Integer,Tuple<Integer,Integer>>();

    // player info
    private Tuple<Integer, Integer> playerPos = null;
    private Color playerColor = null;
    private Player player = null;
    private int playerDirection = 1;

    // control
    private long lastMoveTime = -1;
    private long lastShootTime = -1;

    // 状态与world中的一致
    // 状态6：多人游戏等待玩家界面（服务器对应的玩家
    // 状态7：多人游戏等待玩家界面（客户端对应的玩家
    // 状态9：多人游戏对战界面
    // 状态10：多人游戏结束界面

    public Client(String serverIp, int serverPort, boolean isServerOwner, World world, Map map) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.isServerOwner = isServerOwner;
        this.world = world;
        this.map = map;
        map.setCreatureList(creatureList);
        establishConnection();
    }

    private void establishConnection() {

        serverAddress = new InetSocketAddress(serverIp, serverPort);
        try{
            client = SocketChannel.open();
            client.connect(serverAddress);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        Thread.sleep(3000);
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                    if(joinInGame == false){
                        System.out.println("fail to connect to:" + serverAddress.toString()+" ,maybe server is full right now");
                        System.exit(-1);
                    }

                }
            },"check if connect successfully").start();
            connect = true;
        }
        catch(Exception e){
            System.out.println("fail to connect to:" + serverAddress.toString());
            e.printStackTrace();
            System.exit(-1);
        }

        try{
            startReadFromServer();
        }
        catch(Exception e){
            System.out.println("fail to read from server");
            System.exit(-1);
        }
        System.out.println("Successfully connect to server");
    }

    public void handleInputFromServer(String[] infoFromServer) {
        // System.out.print("client:handling info from server:");
        // for (String s : infoFromServer) {
        //     System.out.print(s + " ");
        // }
        // System.out.print("\n");

        if (!isServerOwner) { // 看不到服务器
            switch (infoFromServer[0]) {
                case "launchCannonball": {    
                    String[] posInfo = infoFromServer[1].split(",");
                    Tuple<Integer, Integer> pos = new Tuple<Integer, Integer>(Integer.parseInt(posInfo[0]),
                        Integer.parseInt(posInfo[1]));
                    int directionInfo = Integer.parseInt(infoFromServer[2]);
                    int ownerId = Integer.parseInt(infoFromServer[3]);
                    Cannonball c = new Cannonball(directionInfo, 1, world,ownerId);
                    map.setThing(pos, 1, c);
                    world.updateOnlineGamingInfo(playerList, this.player.getId());
                }
                    ;
                    break;
                case "gameOver": {

                }
                    ;
                    break;
            }
        } 
        // shared info by both client and server
        switch (infoFromServer[0]) {
            case "setThing": { // 是设置新物品
                String itemType = infoFromServer[1];
                int tempId = Integer.parseInt(infoFromServer[2]);
                String[] posInfo = infoFromServer[3].split(",");
                Tuple<Integer, Integer> pos = new Tuple<Integer, Integer>(Integer.parseInt(posInfo[0]),
                        Integer.parseInt(posInfo[1]));
                // int tempGlyph = Integer.parseInt(infoFromServer[4]);
                String[] colorInfo = infoFromServer[5].split(",");
                Color tempColor = new Color(Integer.parseInt(colorInfo[0]), Integer.parseInt(colorInfo[1]),
                        Integer.parseInt(colorInfo[2]));
                switch (itemType) {
                    case "player": {
                        Player tempPlayer = new Player(tempColor, 0, 100, 6, world, map, null);
                        tempPlayer.setId(tempId);
                        map.setThing(pos, 1, tempPlayer);
                        creatureList.add(tempPlayer);
                        playerList.add(tempPlayer);
                        world.updateOnlineGamingInfo(playerList, this.player.getId());

                        originalPos.put(tempPlayer.getId(), pos);
                    }
                }
            }
                ;
                break;
            case "moveThing": { // 是移动
                // get pos
                String[] beginPosInfo = infoFromServer[1].split(",");
                String[] destPosInfo = infoFromServer[2].split(",");
                Tuple<Integer, Integer> beginPos = new Tuple<Integer, Integer>(
                        Integer.parseInt(beginPosInfo[0]), Integer.parseInt(beginPosInfo[1]));
                Tuple<Integer, Integer> destPos = new Tuple<Integer, Integer>(
                        Integer.parseInt(destPosInfo[0]), Integer.parseInt(destPosInfo[1]));
                String tempItemType = world.get(beginPos.first, beginPos.second).getType();
                if (!isServerOwner) {
                    boolean test = map.moveThing(beginPos, destPos);
                    if(!test && tempItemType.equals("cannonball")){ // cannonball fail to move 
                        world.updateOnlineGamingInfo(playerList,player.getId());
                    }
                }
                if (beginPos.first == this.playerPos.first && beginPos.second == this.playerPos.second) {
                    this.playerPos = destPos;// player had been moved
                }

            }
                ;
                break;
            case "admitToJoin": {
                String[] posInfo = infoFromServer[2].split(",");
                String[] colorInfo = infoFromServer[3].split(",");
                playerPos = new Tuple<Integer, Integer>(Integer.parseInt(posInfo[0]), Integer.parseInt(posInfo[1]));
                playerColor = new Color(Integer.parseInt(colorInfo[0]), Integer.parseInt(colorInfo[1]),
                        Integer.parseInt(colorInfo[2]));
                player = new Player(playerColor, 0, 1, 6, world, map, null);
                int id = Integer.parseInt(infoFromServer[1]);
                joinInGame = true;
                map.setThing(playerPos, 1, player);
                player.setId(id);
                creatureList.add(player);
                playerList.add(player);
                originalPos.put(player.getId(), playerPos);
                if(!isServerOwner){
                    world.setOtherInfo("STATE:WAITING TO START...");
                }
                else{
                    world.setOtherInfo("PRESS ENTER TO START GAME");
                }
                world.updateOnlineGamingInfo(playerList,  id);
            }
                ;
                break;
            case "playerLeave":{
                int id = Integer.parseInt(infoFromServer[1]);
                for(Player p:playerList){
                    if(p.getId() == id){
                        map.setThing(p.getPos(), 0, new Floor(world));
                        playerList.remove(p);
                        break;
                    }
                }
                // 
                for(Creature c:creatureList){
                    if(c.getId() == id){
                        creatureList.remove(c);
                        break;
                    }
                }
                world.updateOnlineGamingInfo(playerList, player.getId());
            };break;
            case "addScore":{
                int id = Integer.parseInt(infoFromServer[1]);
                for(Player p:playerList){
                    if(p.getId() == id){
                        p.addScore();
                        world.updateOnlineGamingInfo(playerList, player.getId());
                        break;
                    }
                }
            };break;
            case "startGame": {
                world.setWorldState(8);
                world.setOtherInfo("STATE:GAMING");
                world.updateOnlineGamingInfo(playerList, player.getId());
            }
                ;
                break;
            case "gameOver":{
                world.setOtherInfo("STATE:GAME OVER");
                world.updateOnlineGamingInfo(playerList, player.getId());
                int winnerId = Integer.parseInt(infoFromServer[1]);
                if(winnerId == this.player.getId()){
                    world.setWorldState(9);
                }
                else{
                    world.setWorldState(10);
                }

            };break;
            case "resetGame":{
                world.setGamingWorld();
                if(isServerOwner){
                    world.setWorldState(6);
                }
                else{
                    world.setWorldState(7);
                }
                // System.out.print("in");
                map.loadMap();
                playerDirection = 1;
                playerPos = originalPos.get(player.getId());
                map.setThing(playerPos, 1, player);
                for(Player p:playerList){
                    if(p.getId() != player.getId()){
                        map.setThing(originalPos.get(p.getId()), 1, p);
                    }
                    if(!creatureList.contains(((Creature)p))){
                        creatureList.add(p);
                    }
                    p.setInfo(6, 0);
                }
                if(!isServerOwner){
                    world.setOtherInfo("STATE:WAITING TO START...");
                }
                else{
                    world.setOtherInfo("PRESS ENTER TO START GAME");
                }
                world.updateOnlineGamingInfo(playerList, player.getId());
            };break;
        }
    }

    public void handleKeyEvent(KeyEvent key) {
        switch (world.getWorldState()) {
            case 6:
            case 7: {
                if (isServerOwner) {
                    switch (key.getKeyCode()) {
                        case KeyEvent.VK_ENTER: {
                            if(this.creatureList.size() > 1){
                                world.setWorldState(8);
                                writeToServer("startGameRequest");
                            }
                            else{
                                world.setOtherInfo("CAN NOT START GAME WITH 1 PLAYER");
                                world.updateOnlineGamingInfo(playerList, player.getId());
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try{
                                            Thread.sleep(1000);
                                        }
                                        catch(Exception e){
                                            e.printStackTrace();
                                        }
                                        world.setOtherInfo("PRESS ENTER TO START GAME");
                                        world.updateOnlineGamingInfo(playerList, player.getId());
                                    }
                                }).start();
                            }
                        }
                            ;
                            break;
                    }
                }
            }
                ;
                break;
            case 8: {// clinet
                NetInfo n;
                switch (key.getKeyCode()) {
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_W: {
                        playerDirection = 1;
                        if(System.currentTimeMillis() - lastMoveTime < 50){
                            break;
                        }
                        lastMoveTime = System.currentTimeMillis();
                        n = new NetInfo("moveThing", playerPos,
                                new Tuple<Integer, Integer>(playerPos.first, playerPos.second - 1));
                        this.writeToServer(n.toString());
                    }
                        ;
                        break;
                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_S: {
                        playerDirection = 2;
                        if(System.currentTimeMillis() - lastMoveTime < 50){
                            break;
                        }
                        lastMoveTime = System.currentTimeMillis();
                        n = new NetInfo("moveThing", playerPos,
                                new Tuple<Integer, Integer>(playerPos.first, playerPos.second + 1));
                        this.writeToServer(n.toString());
                    }
                        ;
                        break;
                    case KeyEvent.VK_LEFT:
                    case KeyEvent.VK_A: {
                        playerDirection = 3;
                        if(System.currentTimeMillis() - lastMoveTime < 50){
                            break;
                        }
                        lastMoveTime = System.currentTimeMillis();
                        n = new NetInfo("moveThing", playerPos,
                                new Tuple<Integer, Integer>(playerPos.first - 1, playerPos.second));
                        this.writeToServer(n.toString());
                    }
                        ;
                        break;
                    case KeyEvent.VK_RIGHT:
                    case KeyEvent.VK_D: {
                        playerDirection = 4;
                        if(System.currentTimeMillis() - lastMoveTime < 50){
                            break;
                        }
                        lastMoveTime = System.currentTimeMillis();
                        n = new NetInfo("moveThing", playerPos,
                                new Tuple<Integer, Integer>(playerPos.first + 1, playerPos.second));
                        this.writeToServer(n.toString());
                    }
                        ;
                        break;
                    case KeyEvent.VK_SPACE:
                        // this.writeToServer("SPACE");
                        if(System.currentTimeMillis() - lastShootTime < 400){
                            break;
                        }
                        lastShootTime = System.currentTimeMillis();
                        switch (playerDirection) {
                            case 1:
                                n = new NetInfo("launchCannonball",
                                        new Tuple<Integer, Integer>(playerPos.first, playerPos.second - 1),
                                        2,this.player.getId());
                                this.writeToServer(n.toString());
                                break;
                            case 2:
                                n = new NetInfo("launchCannonball",
                                        new Tuple<Integer, Integer>(playerPos.first, playerPos.second + 1),
                                        1,this.player.getId());
                                this.writeToServer(n.toString());
                                break;
                            case 3:
                                n = new NetInfo("launchCannonball",
                                        new Tuple<Integer, Integer>(playerPos.first - 1, playerPos.second),
                                        playerDirection,this.player.getId());
                                this.writeToServer(n.toString());
                                break;
                            case 4:
                                n = new NetInfo("launchCannonball",
                                        new Tuple<Integer, Integer>(playerPos.first + 1, playerPos.second),
                                        playerDirection,this.player.getId());
                                this.writeToServer(n.toString());
                                break;
                        }
                }
            }
                ;
                break;
            case 9:
        }
    }

    public ArrayList<Player>getPlayerList(){
        return this.playerList;
    }

    private void writeToServer(String s) {
        // System.out.println("client:write to server:" + s);
        writeBuffer.clear();
        writeBuffer.put(s.getBytes());
        writeBuffer.flip();
        try {
            client.write(writeBuffer);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                client.close();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    private void startReadFromServer() { // 从服务器读取信息

        // 当前尚未成功加入对局
        System.out.println("client:trying to join the game");
        writeToServer("playerJoin");
        readBuffer.clear();
        if (!joinInGame) { // 尚未加入游戏
            try {
                int readNum = client.read(readBuffer);
                if (readNum != -1) {
                    readBuffer.flip();
                    Charset charset = Charset.forName("utf-8");
                    String line = charset.decode(readBuffer).toString();

                    String[] lineInfo = line.split("<>");
                    lineInfo = lineInfo[0].split(" ");
                    if (lineInfo[0].equals("admitToJoin")) { // 同意加入对局
                        this.joinInGame = true;
                        this.handleInputFromServer(lineInfo);
                        // System.out.println("successfully");
                    }
                    else if (lineInfo[0].equals("refuseToJoin")){ // 不同意加入对局
                        System.out.println("Refused to join for server is now full");
                        System.exit(-1);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 加入游戏成功
        Runnable r = new Runnable() {
            @Override
            public void run() {
                String line;
                int readNum = -1;
                while (connect) { // 连接状态
                    readBuffer.clear();
                    try {
                        readNum = client.read(readBuffer);
                        if (readNum == -1) {
                            // 读取失败
                            System.out.println("fail to read from server");
                        } else {
                            // 读取成功，作处理
                            readBuffer.flip();
                            Charset charset = Charset.forName("utf-8");
                            line = charset.decode(readBuffer).toString();
                            String[]lineInfo = line.split("<>");
                            for(String info:lineInfo){
                                handleInputFromServer(info.split(" "));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        world.setOtherInfo("FAIL TO CONNECT TO SERVER");
                        System.out.println("fail to connect to:"+serverAddress.toString());
                        world.updateOnlineGamingInfo(playerList, player.getId());
                        connect = false;
                    }
                }
            }

        };
        new Thread(r, "listen thread in client").start();
    }
}
