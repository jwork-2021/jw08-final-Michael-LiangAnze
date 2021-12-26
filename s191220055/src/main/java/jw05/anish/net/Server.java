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
import jw05.anish.calabashbros.CannonballList;
import jw05.anish.calabashbros.Creature;
import jw05.anish.calabashbros.Player;
import jw05.anish.calabashbros.World;
import java.awt.Color;

public class Server {
	private Selector selector;
	private World world;
	private Map map;
	private Client serverOwner;
	private InetSocketAddress listenAddress;
	private final int PORT = 9093;
	private boolean isRunning = false;
	private SocketAddress serverOwnerSocketAddress = null;

	// status about game
	private boolean waiting = false;
	private boolean gaming = false;
	private boolean gameover = false;
	private int playerNum = 0;
	private ArrayList<PlayerInfo> playerSourceList = new ArrayList<PlayerInfo>();
	private CannonballList cannonballList = null;
	private int id = 0;

	// io
	ByteBuffer readBuffer = ByteBuffer.allocate(1024);
	ByteBuffer writeBuffer = ByteBuffer.allocate(1024);

	public Server(int port, World world, Map map) {
		listenAddress = new InetSocketAddress("localhost", PORT); // 只能是本地
		this.world = world;
		this.map = map;
		this.cannonballList = new CannonballList(1, 600, map, world);
		cannonballList.setServer(this);
		playerSourceList.add(new PlayerInfo(new Player(Color.red, 0, 1, 8, world, map, null), id,
				new Tuple<Integer, Integer>(3, 17), Color.red,null));
		playerSourceList.add(new PlayerInfo(new Player(Color.green, 0, 1, 8, world, map, null), id,
				new Tuple<Integer, Integer>(4, 17), Color.green,null));
		playerSourceList.add(new PlayerInfo(new Player(Color.yellow, 0, 1, 8, world, map, null), id,
				new Tuple<Integer, Integer>(5, 17), Color.yellow,null));
		playerSourceList.add(new PlayerInfo(new Player(Color.blue, 0, 1, 8, world, map, null), id,
				new Tuple<Integer, Integer>(6, 17), Color.blue,null));

		try {
			startServer();
		} catch (Exception e) {
			System.out.println("fail to start server");
			e.printStackTrace();
		}
	}

	public void setServerOwner(Client c) {
		this.serverOwner = c;
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
				} catch (Exception e) {
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
			System.out.println("server:player from:"+getSocketAddress(key)+" left server");
			for(PlayerInfo pi:playerSourceList){
				if(pi.playerAddress == getSocketAddress(key)){
					pi.isAsssign = false;
					pi.playerAddress = null;
					this.playerNum--;
					NetInfo ni = new NetInfo("playerLeave",pi.id);
					broadcastToAllClient(ni.toString(), getSocketAddress(key));
					break;
				}
			}
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
		// System.out.println("Got: "+ inputLine);

		// 读完再写回去
		handleInputFromClient(key, inputLine);
	}

	private void handleInputFromClient(SelectionKey key, String s) {
		System.out.println("server:handling:" + s);
		String[] infoFromClient = s.split(" ");
		if (infoFromClient.length == 0) {
			return;
		}
		switch (infoFromClient[0]) {
			case "setThing": { // 是设置新物品

			}
				;
				break;
			case "moveThing": { // 是移动
				String[] beginPosInfo = infoFromClient[1].split(",");
				String[] destPosInfo = infoFromClient[2].split(",");
				Tuple<Integer, Integer> beginPos = new Tuple<Integer, Integer>(Integer.parseInt(beginPosInfo[0]),
						Integer.parseInt(beginPosInfo[1]));
				Tuple<Integer, Integer> destPos = new Tuple<Integer, Integer>(Integer.parseInt(destPosInfo[0]),
						Integer.parseInt(destPosInfo[1]));
				String beginPosType = world.get(beginPos.first, beginPos.second).getType();
				String destPosType = world.get(destPos.first, destPos.second).getType();
				if (this.map.moveThing(beginPos, destPos)) { // server try to move thing and succeed
					broadcastToAllClient(s, null); // send to all players;attention to distinguish serverowner and
													// others
				} else if (beginPosType.equals("cannonball") && destPosType.equals("player")) {
					world.updateOnlineGamingInfo(serverOwner.getPlayerList(), -1);
				}
			}
				;
				break;
			case "launchCannonball": {
				String[] beginPosInfo = infoFromClient[1].split(",");
				Tuple<Integer, Integer> beginPos = new Tuple<Integer, Integer>(Integer.parseInt(beginPosInfo[0]),
						Integer.parseInt(beginPosInfo[1]));
				int directionInfo = Integer.parseInt(infoFromClient[2]);
				int ownerId = Integer.parseInt(infoFromClient[3]);
				// System.out.println(ownerId);
				cannonballList.addCannonball(beginPos, directionInfo,ownerId);
			}
				;
				break;
			case "playerJoin": {
				if (playerNum < 4) { // allow
					// assign info
					PlayerInfo i = getAvailablePlayer();
					i.playerAddress = getSocketAddress(key);
					// world.put(i.player, i.pos); 留给服务器对应的client来设置
					playerNum++;
					// attention:send other players in playerList to this client using "setThing",
					// but send to requester using "adminToJoin"
					NetInfo n = new NetInfo("admitToJoin", i.id, i.pos, i.color);
					write(key, n.toString()); // only use adminToJoin
					for (PlayerInfo temp : playerSourceList) {
						if (temp.isAsssign && temp.id != i.id) { // send other players to the requester
							n = new NetInfo("setThing", "player", temp.id, temp.pos, (int) temp.player.getGlyph(),
									temp.color);
							write(key, n.toString());
						}
					}
					// broadcast the requester to other players
					n = new NetInfo("setThing", "player", i.id, i.pos, (int) i.player.getGlyph(), i.color);
					broadcastToAllClient(n.toString(), getSocketAddress(key));
				}
			}
				;
				break;
			case "startGame": {

			}
				;
				break;
			case "startGameRequest": {
				// only owner can send this message
				this.serverOwnerSocketAddress = getSocketAddress(key); // 记录下这一个地址
				// System.out.println("got request");
				world.setWorldState(8);
				new Thread(this.cannonballList, "cannonballListThread").start();
				gaming = true;
				broadcastToAllClient("startGame", null);
			}
				;
				break;
			case "gameOver": {

			}
				;
				break;
		}
	}

	private void write(SelectionKey key, String s) {
		System.out.println("server:write to client " + key.toString() + " with info:" + s);
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

	private void broadcastToAllClient(String s, SocketAddress exception) {
		if (exception == null) { // send to all players
			for (SelectionKey key : selector.keys()) {
				if (key.channel() instanceof SocketChannel) {
					write(key, s);
				}
			}
		} else {
			for (SelectionKey key : selector.keys()) {
				if (key.channel() instanceof SocketChannel) {
					if (!getSocketAddress(key).equals(exception)) { // dont send to exception
						write(key, s);
					}
				}
			}
		}
	}

	private PlayerInfo getAvailablePlayer() {
		for (PlayerInfo p : playerSourceList) {
			if (!p.isAsssign) {
				p.isAsssign = true;
				p.id = id++;
				return p;
			}
		}
		return null;
	}

	private SocketAddress getSocketAddress(SelectionKey key) {
		SocketChannel sc = (SocketChannel) key.channel();
		Socket socket = sc.socket();
		return socket.getRemoteSocketAddress();
	}

	public void launchCannonball(NetInfo ni){
		broadcastToAllClient(ni.toString(), serverOwnerSocketAddress);
	}

	public void moveCannonball(NetInfo ni) {
		broadcastToAllClient(ni.toString(), serverOwnerSocketAddress);
	}

	public void gameOver() {
		this.gaming = false;
		NetInfo ni = new NetInfo("gameOver");
		broadcastToAllClient(ni.toString(), null);
		new Thread(new Runnable() {
			@Override
			public void run() {
				try{
					Thread.sleep(2000);
				}
				catch(Exception e){
					e.printStackTrace();
				}
				NetInfo ni = new NetInfo("resetGame");
				broadcastToAllClient(ni.toString(), null);
				// for(PlayerInfo pi:playerSourceList){
				// 	if(pi.isAsssign){
				// 		for(SelectionKey key:selector.keys()){
				// 			if(getSocketAddress(key) == pi.playerAddress){
				// 				ni = new NetInfo("admitToJoin", pi.id, pi.pos, pi.color);
				// 				write(key, ni.toString());
				// 				for(PlayerInfo pi_:playerSourceList){
				// 					if(pi_.id != pi.id && pi_.isAsssign){
				// 						ni = new NetInfo("setThing", "player", pi_.id, pi_.pos, (int) pi_.player.getGlyph(),pi_.color);
				// 						write(key, ni.toString());
				// 					}
				// 				}
				// 				break;
				// 			}
				// 		}
				// 	}
				// }
			}
		}).start();
	}

	public void addPlayerScore(int id){
		NetInfo ni = new NetInfo("addScore",id);
		broadcastToAllClient(ni.toString(), null);
	}
}
