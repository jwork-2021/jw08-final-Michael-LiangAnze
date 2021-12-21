package jw05.anish.calabashbros;

import java.util.ArrayList;
import jw05.anish.algorithm.Tuple;
import jw05.anish.map.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CannonballList implements Runnable {
    private int sleepTime;
    private ArrayList<Cannonball> cannonballList = null; // 坐标，炮弹是否有效
    Map map;
    private int damage;
    World world;
    private Lock lock;

    public CannonballList(int damage, int speed, Map map, World world) {
        cannonballList = new ArrayList<Cannonball>();
        this.map = map;
        this.sleepTime = 1000 / speed * 50;
        this.damage = damage;
        this.world = world;
        lock = new ReentrantLock();
    }

    public void addCannonball(Tuple<Integer, Integer> cannonPos, int direction) {
        lock.lock();
        Cannonball temp = new Cannonball(direction, damage, world);
        if (map.setThing(cannonPos, 1, temp, true)) {
            cannonballList.add(temp);
        }
        lock.unlock();
    }

    public int getDamage() {
        return damage;
    }

    private void move() {
        lock.lock();
        // map.getMapState(mapList);//获取地图状态
        Tuple<Integer, Integer> pos = null;
        ArrayList<Cannonball> removeList = new ArrayList<Cannonball>();
        for (Cannonball c : cannonballList) {
            pos = c.getPos();
            switch (c.getDirection()) {
                case 1: {
                    if (!map.moveThing(pos, new Tuple<Integer, Integer>(pos.first, pos.second + 1), true)) {// 失败，和玩家一个格子
                        removeList.add(c);
                    }
                }
                    break;
                case 2: {
                    if (!map.moveThing(pos, new Tuple<Integer, Integer>(pos.first, pos.second - 1), true)) {
                        removeList.add(c);
                    }
                }
                    break;
                case 3: {
                    if (!map.moveThing(pos, new Tuple<Integer, Integer>(pos.first - 1, pos.second), true)) {
                        removeList.add(c);
                    }
                }
                    break;
                case 4: {
                    if (!map.moveThing(pos, new Tuple<Integer, Integer>(pos.first + 1, pos.second), true)) {
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
        while (world.getWorldState() < 2) {
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
