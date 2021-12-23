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
        if(!isServerOwner){ //看不到服务器
            switch(infoFromServer[0]){
                case "setThing": { // 是设置新物品
                    
                };break;
                case "moveThing":{ // 是移动
                  
                } ;break;
                case "launchCannonball":{
                    
                };break;
                
                case "admitToJoin":{
                    String[]posInfo = infoFromServer[2].split(",");
                    String[]colorInfo = infoFromServer[3].split(",");
                    Tuple<Integer,Integer>pos = new Tuple<Integer,Integer>(Integer.parseInt(posInfo[0]),Integer.parseInt(posInfo[1]));
                    Color c = new Color(Integer.parseInt(colorInfo[0]),Integer.parseInt(colorInfo[1]),Integer.parseInt(colorInfo[2]));
                    Player player = new Player(c, 0, 1, 4, world, map, null);
					world.put(player, pos);
                };break;
                case "playerJoin":{
                    
                };break;
                case "startGame":{
                    
                };break;
                case "gameOver":{
                    
                };break;
            }
        }
    }

    public void handleKeyEvent(KeyEvent key) {
        System.out.println("sending key:"+key.getKeyCode());
        if (world.getWorldState() == 7){ //等待界面
            switch (key.getKeyCode()) {
                case KeyEvent.VK_W:
                    this.writeToServer("W");
                    break;
                case KeyEvent.VK_S:
                this.writeToServer("S");
                    break;
                case KeyEvent.VK_A:
                this.writeToServer("A");
                    break;
                case KeyEvent.VK_D:
                this.writeToServer("D");
                    break;
                case KeyEvent.VK_SPACE:
                this.writeToServer("SPACE");
            }
        }
    }

    private void writeToServer(String s){
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
        System.out.println("write to server:"+s);
    }

    private void startReadFromServer(){ // 从服务器读取信息

        //当前尚未成功加入对局
        System.out.println("trying to join the game");
        writeToServer("playerJoin");
        readBuffer.clear();
        if(!joinInGame){ //尚未加入游戏
            try{
                int readNum = client.read(readBuffer);
                if(readNum != -1){
                    readBuffer.flip();
                    Charset charset = Charset.forName("utf-8");
                    String line = charset.decode(readBuffer).toString();
                    // System.out.println("get:"+ line);
                    String[]lineInfo = line.split(" ");
                    if(lineInfo[0].equals("admitToJoin")){ //同意加入对局
                        this.joinInGame = true;
                        this.handleInputFromServer(lineInfo);
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
                            System.out.println("get info from server:"+line); //仅作输出
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
