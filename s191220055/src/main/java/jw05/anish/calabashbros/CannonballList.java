package jw05.anish.calabashbros;

import java.util.ArrayList;
import jw05.anish.algorithm.Tuple;
import jw05.anish.map.Map;
import jw05.anish.net.NetInfo;
import jw05.anish.net.Server;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CannonballList implements Runnable {
    private int sleepTime;
    private ArrayList<Cannonball> cannonballList = null; // 坐标，炮弹是否有效
    private Map map;
    private int damage;
    private World world;
    private Lock lock;
    private Server server;
    
    public CannonballList(int damage, int speed, Map map, World world) {
        cannonballList = new ArrayList<Cannonball>();
        this.map = map;
        this.sleepTime = 1000 / speed * 50;
        this.damage = damage;
        this.world = world;
        lock = new ReentrantLock();
    }

    public void setServer(Server s){ // for online game
        this.server = s;
    }

    public void addCannonball(Tuple<Integer, Integer> cannonPos, int direction) {
        lock.lock();
        Cannonball temp = new Cannonball(direction, damage, world);
        if (map.setThing(cannonPos, 1, temp)) {
            cannonballList.add(temp);
        }
        lock.unlock();
    }  
    public void addCannonball(Tuple<Integer, Integer> cannonPos, int direction,int ownerId) { // for online game
        lock.lock();
        Cannonball temp = new Cannonball(direction, damage, world,ownerId);
        if (map.setThing(cannonPos, 1, temp)) {
            cannonballList.add(temp);
        }
        // System.out.println(cannonballList.size());
        NetInfo ni = new NetInfo("launchCannonball", cannonPos, direction,ownerId);
        server.launchCannonball(ni);
        lock.unlock();
    } 

    public int getDamage() {
        return damage;
    }

    private void move() {
        lock.lock();
        // map.getMapState(mapList);//获取地图状态
        Tuple<Integer, Integer> curPos = null,nextPos;
        ArrayList<Cannonball> removeList = new ArrayList<Cannonball>();
        for (Cannonball c : cannonballList) {
            curPos = c.getPos();
            switch (c.getDirection()) {
                case 1: {
                    nextPos = new Tuple<Integer, Integer>(curPos.first, curPos.second + 1);
                    if(server != null){
                        NetInfo ni = new NetInfo("moveThing",curPos,nextPos);
                        server.moveCannonball(ni);
                    }
                    if (!map.moveThing(curPos, nextPos)) {// 失败，和玩家一个格子
                        removeList.add(c);
                    }
                }
                    break;
                case 2: {
                    nextPos = new Tuple<Integer, Integer>(curPos.first, curPos.second - 1);
                    if(server != null){
                        NetInfo ni = new NetInfo("moveThing",curPos,nextPos);
                        server.moveCannonball(ni);
                    }
                    if (!map.moveThing(curPos, nextPos)) {
                        removeList.add(c);
                    }
                }
                    break;
                case 3: {
                    nextPos = new Tuple<Integer, Integer>(curPos.first - 1, curPos.second);
                    if(server != null){
                        NetInfo ni = new NetInfo("moveThing",curPos,nextPos);
                        server.moveCannonball(ni);
                    }
                    if (!map.moveThing(curPos, nextPos)) {
                        removeList.add(c);
                    }
                }
                    break;
                case 4: {
                    nextPos = new Tuple<Integer, Integer>(curPos.first + 1, curPos.second);
                    if(server != null){
                        NetInfo ni = new NetInfo("moveThing",curPos,nextPos);
                        server.moveCannonball(ni);
                    }
                    if (!map.moveThing(curPos, nextPos)) {
                        removeList.add(c);
                    }
                }
                    break;

            }
        }
        for (Cannonball i : removeList) {
            cannonballList.remove(i);
        }
        lock.unlock();
        // System.out.println("cur size:"+cannonballList.size());
    }

    @Override
    public void run() {
        while (world.getWorldState() < 2 || world.getWorldState() == 8) {
            if (cannonballList.size() != 0) {
                move();
            }
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {

            }
        }
    }
}
