package jw05.anish.net;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

import jw05.anish.algorithm.Tuple;
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
    private Selector selector = null;
    private ByteBuffer readBuffer = ByteBuffer.allocate(1024);
    private ByteBuffer writeBuffer = ByteBuffer.allocate(1024);
    private Boolean connect = false;// 是否连接了服务器
    private Boolean joinInGame = false; //是否加入了对局
    private boolean isServerOwner = false;

    // player info
    private Tuple<Integer,Integer>playerPos = null;
    private Color playerColor = null;
    private Player player = null;

    // 状态与world中的一致
    // 状态6：多人游戏等待玩家界面（服务器对应的玩家
    // 状态7：多人游戏等待玩家界面（客户端对应的玩家
    // 状态9：多人游戏对战界面
    // 状态10：多人游戏结束界面

    public Client(String serverIp,int serverPort,boolean isServerOwner, World world, Map map) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.isServerOwner = isServerOwner;
        this.world = world;
        this.map = map;
        establishConnection();
    }

    private void establishConnection(){
        
        try{
            // 建立连接
            serverAddress = new InetSocketAddress(serverIp, serverPort);
            client = SocketChannel.open(serverAddress); 
            System.out.println("Successfully connect to server");
            connect = true;
            // 监听服务器
            startReadFromServer();
        }
        catch(ConnectException e){
            System.out.println("fail to connect to:"+serverAddress.toString());
        }
        catch(IOException e){
            System.out.println("server had been shut down");
        }
        
    }

    public void handleInputFromServer(String[]infoFromServer) {
        System.out.print("client:handling info from server:");
        for(String s:infoFromServer){
            System.out.print(s+" ");
        }
        System.out.print("\n");

        if(!isServerOwner){ //看不到服务器
            switch(infoFromServer[0]){
                case "moveThing":{ // 是移动
                    // get pos
                    String[] beginPosInfo = infoFromServer[1].split(",");
                    String[] destPosInfo = infoFromServer[2].split(",");
                    Tuple<Integer, Integer> beginPos = new Tuple<Integer, Integer>(
                            Integer.parseInt(beginPosInfo[0]), Integer.parseInt(beginPosInfo[1]));
                    Tuple<Integer, Integer> destPos = new Tuple<Integer, Integer>(
                            Integer.parseInt(destPosInfo[0]), Integer.parseInt(destPosInfo[1]));
                    map.moveThing(beginPos, destPos);
                    if(beginPos.first == this.playerPos.first && beginPos.second == this.playerPos.second){
                        this.playerPos = destPos;// player had been moved
                    }
    
                } ;break;
                case "launchCannonball":{
                    
                };break;
                case "gameOver":{
                    
                };break;
            }
        }
        else{ // owner
            switch(infoFromServer[0]){
                case "moveThing":{ // 是移动
                    // get pos
                    System.out.println("yes");
                    String[] beginPosInfo = infoFromServer[1].split(",");
                    String[] destPosInfo = infoFromServer[2].split(",");
                    Tuple<Integer, Integer> beginPos = new Tuple<Integer, Integer>(
                            Integer.parseInt(beginPosInfo[0]), Integer.parseInt(beginPosInfo[1]));
                    Tuple<Integer, Integer> destPos = new Tuple<Integer, Integer>(
                            Integer.parseInt(destPosInfo[0]), Integer.parseInt(destPosInfo[1]));
                    if(beginPos.first == this.playerPos.first && beginPos.second == this.playerPos.second){
                        this.playerPos = destPos;// player had been moved
                    }
    
                } ;break;
            }
        }
        // shared info by both client and server
        switch(infoFromServer[0]){
            case "setThing": { // 是设置新物品
                String itemType = infoFromServer[1];
                int tempId = Integer.parseInt(infoFromServer[2]);
                String[]posInfo = infoFromServer[3].split(",");
                Tuple<Integer,Integer>pos = new Tuple<Integer,Integer>(Integer.parseInt(posInfo[0]),Integer.parseInt(posInfo[1]));
                int tempGlyph = Integer.parseInt(infoFromServer[4]);
                String[]colorInfo = infoFromServer[5].split(",");
                Color tempColor = new Color(Integer.parseInt(colorInfo[0]),Integer.parseInt(colorInfo[1]),Integer.parseInt(colorInfo[2]));
                switch(itemType){
                    case "player": {
                        Player player = new Player(tempColor, 0, 100, 4, world, map, null);
                        player.setId(tempId);
                        player.setId(tempId);
                        // creatureList.add(player);
                        map.setThing(pos, 1, player);
                    }
                }
            };break;
            case "admitToJoin":{
                String[]posInfo = infoFromServer[2].split(",");
                String[]colorInfo = infoFromServer[3].split(",");
                playerPos = new Tuple<Integer,Integer>(Integer.parseInt(posInfo[0]),Integer.parseInt(posInfo[1]));
                playerColor = new Color(Integer.parseInt(colorInfo[0]),Integer.parseInt(colorInfo[1]),Integer.parseInt(colorInfo[2]));
                player = new Player(playerColor, 0, 1, 4, world, map, null);
                joinInGame = true;
                map.setThing(playerPos, 1, player);
            };break;
            case "startGame":{
                world.setWorldState(8);
                // System.out.println(world.getWorldState());
            };break;
        }
    }

    public void handleKeyEvent(KeyEvent key) {
        // System.out.println("sending key:"+key.getKeyCode());
        // System.out.println(1);
        // System.out.println(isServerOwner);
        // System.out.println(key.getKeyCode() == KeyEvent.VK_ENTER);
        switch(world.getWorldState()){
            case 6:
            case 7:{
                    if(isServerOwner){
                        switch(key.getKeyCode() ){
                            case KeyEvent.VK_ENTER:{
                                world.setWorldState(8);
                                writeToServer("startGameRequest");
                            };break;
                        }
                    }  
                };break;
            case 8:{//clinet
                // System.out.print("sent move");
                NetInfo n;
                switch (key.getKeyCode()) {
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_W :{
                        n = new NetInfo("moveThing", playerPos, new Tuple<Integer,Integer>(playerPos.first,playerPos.second - 1));
                        this.writeToServer(n.toString());
                    };break;   
                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_S:{
                        n = new NetInfo("moveThing", playerPos, new Tuple<Integer,Integer>(playerPos.first,playerPos.second + 1));
                        this.writeToServer(n.toString());
                    }; break;
                    case KeyEvent.VK_LEFT:
                    case KeyEvent.VK_A:{
                        n = new NetInfo("moveThing", playerPos, new Tuple<Integer,Integer>(playerPos.first-1,playerPos.second));
                        this.writeToServer(n.toString());
                    };break;
                    case KeyEvent.VK_RIGHT:
                    case KeyEvent.VK_D:{
                        n = new NetInfo("moveThing", playerPos, new Tuple<Integer,Integer>(playerPos.first+1,playerPos.second));
                        this.writeToServer(n.toString());
                    };break;
                    case KeyEvent.VK_SPACE:
                    this.writeToServer("SPACE");
                }
            };break;
            case 9:
        }
    }

    private void writeToServer(String s){
        System.out.println("client:write to server:"+s);
        writeBuffer.clear();
        writeBuffer.put(s.getBytes());
        writeBuffer.flip();
        try{
            client.write(writeBuffer);
        }
        catch(Exception e){
            e.printStackTrace();
            try{
                client.close();
            }
            catch(Exception e1){
                e1.printStackTrace();
            }
        }
    }

    private void startReadFromServer(){ // 从服务器读取信息

        //当前尚未成功加入对局
        System.out.println("client:trying to join the game");
        writeToServer("playerJoin");
        readBuffer.clear();
        if(!joinInGame){ //尚未加入游戏
            try{
                int readNum = client.read(readBuffer);
                if(readNum != -1){
                    readBuffer.flip();
                    Charset charset = Charset.forName("utf-8");
                    String line = charset.decode(readBuffer).toString();
                    
                    String[]lineInfo = line.split(" ");
                    if(lineInfo[0].equals("admitToJoin")){ //同意加入对局
                        this.joinInGame = true;
                        this.handleInputFromServer(lineInfo);
                        // System.out.println("successfully");
                    }
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }

        //加入游戏成功
        Runnable r = new Runnable() {
            @Override
            public void run() {
                String line;
                int readNum = -1;
                while(connect){ // 连接状态
                    readBuffer.clear();
                    try{
                        readNum = client.read(readBuffer);
                        if(readNum == -1){
                            // 读取失败
                            System.out.println("fail to read");
                        }
                        else{
                            //读取成功，作处理
                            readBuffer.flip();
                            Charset charset = Charset.forName("utf-8");
                            line = charset.decode(readBuffer).toString();
                            handleInputFromServer(line.split(" "));
                        }
                    }
                    catch(Exception e){
                        e.printStackTrace();
                        connect = false;
                    }
                }
            }

        };
        new Thread(r,"listen thread in client").start();
    }
}
