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

import jw05.anish.calabashbros.World;
import jw05.anish.map.Map;

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

    // 状态与world中的一致
    // 状态6：多人游戏等待玩家界面（服务器对应的玩家
    // 状态7：多人游戏等待玩家界面（客户端对应的玩家
    // 状态9：多人游戏对战界面
    // 状态10：多人游戏结束界面

    public Client(String serverIp,int serverPort,World world, Map map) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.world = world;
        this.map = map;
        establishConnection();
    }

    private void establishConnection(){
        
        try{
            // 建立连接
            serverAddress = new InetSocketAddress(serverIp, serverPort);
            client = SocketChannel.open(); 
            // client.configureBlocking(false);
            System.out.println("Successfully connect to server");
            connect = true;

            // 设置复用器 
            // selector = Selector.open();
            // client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE); 
            client.connect(serverAddress);
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

    public void startGameAsClient() {

    }

    public void handleKeyEvent(KeyEvent key) {
        System.out.println("sending key:"+key.getKeyCode());
        if (true){
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

            }
        }
    }

    private void startReadFromServer(){ // 从服务器读取信息
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
                            System.out.println(line); //仅作输出
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
