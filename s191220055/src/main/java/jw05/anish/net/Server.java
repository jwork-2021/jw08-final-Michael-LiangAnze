package jw05.anish.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import jw05.anish.map.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import jw05.anish.algorithm.Tuple;
import jw05.anish.calabashbros.Player;
import jw05.anish.calabashbros.World;
import java.awt.Color;

public class Server {
	private Selector selector;
	private World world;
	private Map map;
	private InetSocketAddress listenAddress;
	private final int PORT = 9093;
	private boolean isRunning = false;

	// status about game
	private boolean waiting = false;
	private boolean gaming = false;
	private boolean gameover = false;
	private int playerNum = 0;
	private ArrayList<PlayerInfo> playerList= new ArrayList<PlayerInfo>();
	private int id = 0;
	// io
	ByteBuffer readBuffer = ByteBuffer.allocate(1024);
	ByteBuffer writeBuffer = ByteBuffer.allocate(1024);

	public Server(int port, World world, Map map) {
		listenAddress = new InetSocketAddress("localhost", PORT); // 只能是本地
		this.world = world;
		this.map = map;
		playerList.add(new PlayerInfo(new Player(Color.red, 0, 1, 4, world, map, null), id, new Tuple<Integer, Integer>(3,17), Color.red));
		playerList.add(new PlayerInfo(new Player(Color.green, 0, 1, 4, world, map, null), id, new Tuple<Integer, Integer>(4,17), Color.green));
		playerList.add(new PlayerInfo(new Player(Color.yellow, 0, 1, 4, world, map, null), id, new Tuple<Integer, Integer>(5,17), Color.yellow));
		playerList.add(new PlayerInfo(new Player(Color.blue, 0, 1, 4, world, map, null), id, new Tuple<Integer, Integer>(6,17), Color.blue));

		try {
			startServer();
		} catch (Exception e) {
			System.out.println("fail to start server");
			e.printStackTrace();
		}
	}

	private void startServer() throws IOException {
		this.selector = Selector.open();
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);

		// bind server socket channel to port
		serverChannel.socket().bind(listenAddress);
		serverChannel.register(this.selector, SelectionKey.OP_ACCEPT);

		System.out.println("Server started on port >> " + PORT);
		isRunning = true;
		Runnable serverRunnable = new Runnable() {
			@Override
			public void run() {
				try {
					while (isRunning) {
						// wait for events
						int readyCount = selector.select();
						if (readyCount == 0) {
							continue;
						}
						// process selected keys...
						Set<SelectionKey> readyKeys = selector.selectedKeys();
						Iterator iterator = readyKeys.iterator();
						while (iterator.hasNext()) {
							SelectionKey key = (SelectionKey) iterator.next();
							iterator.remove();

							if (!key.isValid()) {
								continue;
							}
							if (key.isAcceptable()) { // Accept client connections
								accept(key);
							} else if (key.isReadable()) { // Read from client
								read(key);
							}
						}
					}
				} 
				catch (Exception e) {
					e.printStackTrace();
				}

			}
		};
		new Thread(serverRunnable, "serverThread").start();
	}

	// accept client connection
	private void accept(SelectionKey key) throws IOException {

		// check if can be accepted
		if (this.playerNum < 4) {
			ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
			SocketChannel channel = serverChannel.accept();
			channel.configureBlocking(false);
			Socket socket = channel.socket();
			SocketAddress remoteAddr = socket.getRemoteSocketAddress();
			System.out.println("Connected to: " + remoteAddr);
			channel.register(this.selector, SelectionKey.OP_READ);
			// this.playerNum++;
		}
	}

	// read from the socket channel
	private void read(SelectionKey key) throws IOException {
		SocketChannel channel = (SocketChannel) key.channel();
		readBuffer.clear();
		int numRead = -1;
		try {
			numRead = channel.read(readBuffer);
		} catch (SocketException e) {
			System.out.println("fail to read");
		}

		if (numRead == -1) {
			Socket socket = channel.socket();
			SocketAddress remoteAddr = socket.getRemoteSocketAddress();
			System.out.println("Connection closed by client: " + remoteAddr);
			channel.close();
			key.cancel();
			return;
		}

		byte[] data = new byte[numRead];
		System.arraycopy(readBuffer.array(), 0, data, 0, numRead);
		String inputLine = new String(data);
		System.out.println("Got: "+ inputLine);

		// 读完再写回去
		handleInputFromClient(key,inputLine);
	}

	private void handleInputFromClient(SelectionKey key,String s){
		String[]infoFromClient = s.split(" ");
		if(infoFromClient.length == 0){
			return;
		}
		switch(infoFromClient[0]){
			case "setThing": { // 是设置新物品
				
			};break;
			case "moveThing":{ // 是移动
			  
			} ;break;
			case "launchCannonball":{
				
			};break;
			
			case "admitToJoin":{
				  
			};break;
			case "playerJoin":{
				if(playerNum < 4){ // allow
					// assign info
					PlayerInfo i = getAvailablePlayer();
					world.put(i.player, i.pos);
					playerNum++;
					// attention:send all players in playerList to this client
					for(PlayerInfo temp:playerList){
						if(temp.isAsssign){
							NetInfo n = new NetInfo("admitToJoin",temp.id,temp.pos,temp.color);
							write(key, n.toString());
						}
					}
					//output
					// System.out.println("A new player join in the game");
				}
			};break;
			case "startGame":{
				
			};break;
			case "gameOver":{
				
			};break;
		}
	}

	private void write(SelectionKey key, String s) {
		System.out.println("write to client:"+s);
		SocketChannel channel = (SocketChannel) key.channel();
		writeBuffer.clear();
		writeBuffer.put(s.getBytes());
		writeBuffer.flip();
		try {
			// 回写数据
			channel.write(writeBuffer);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void broadcastToAllClient(String s) {

	}

	private PlayerInfo getAvailablePlayer(){
		for(PlayerInfo p:playerList){
			if(!p.isAsssign){
				p.isAsssign = true;
				p.id = id++;
				return p;
			}
		}
		return null;
	}
}
