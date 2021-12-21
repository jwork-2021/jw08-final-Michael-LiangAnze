package jw05.anish.calabashbros;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.awt.Color;
import jw05.anish.algorithm.HandleDist;
import jw05.anish.algorithm.Tuple;
import jw05.anish.map.Map;

public class SworksMan extends Creature implements Runnable {
    private int rank;
    Player target;
    int detectnDistance;
    int damage;
    int sleepTime;
    private int cd;
    Random random;
    private final Color alertOnColor = new Color(255, 255, 0);
    private final Color alertOffColor = new Color(130, 137, 24);
    private int x1, y1, x2, y2;
    private int[][] areaMap;

    public SworksMan(int rank, int speed, int detectnDistance, int damage, int hp, World world, Map map, Player enemy,
            int x1, int y1, int x2, int y2) {
        super(new Color(170, 177, 24), (char) 2, world);
        this.initIcon(2, 12);
        this.rank = rank;
        this.speed = speed;
        this.detectnDistance = detectnDistance;
        this.map = map;
        this.hp = hp;
        target = enemy;
        this.cd = 4;
        random = new Random();
        this.damage = damage;
        this.sleepTime = 1000 / speed * 50;
        setArea(x1, y1, x2, y2);

        this.type = "monster";
    }

    public void setArea(int x1, int y1, int x2, int y2) {
        if (x1 > x2 || y1 > y2) {
            System.out.println("invalid area");
        } else {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }
        int mapSize = map.getMapSize();
        areaMap = new int[mapSize][mapSize];
        map.getMapState(areaMap);

        for (int i = 0; i < mapSize; ++i) {
            for (int j = 0; j < mapSize; ++j) {
                if (i < x1 || i > x2 || j < y1 || j > y2) {
                    areaMap[i][j] = 1; // 不可见
                }
            }
        }

    }

    public int getRank() {
        return this.rank;
    }

    @Override
    public String toString() {
        return String.valueOf(this.rank);
    }

    @Override
    public synchronized void beAttack(int damage) {
        this.hp -= damage;
    }

    private boolean reachTarget(int beginX, int beginY, int targetX, int targetY) {
        if (beginX == targetX) {
            if (beginY == targetY - 1 || beginY == targetY + 1) {
                return true;
            } else {
                return false;
            }
        } else if (beginY == targetY) {
            if (beginX == targetX - 1 || beginX == targetX + 1) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public void setTarget(Player c) {
        this.target = c;
    }

    private boolean enemyComing(Tuple<Integer, Integer> curPos, Tuple<Integer, Integer> targetPos) { // 用以寻找敌人
        return Math.abs(targetPos.first - curPos.first) <= detectnDistance
                && Math.abs(targetPos.second - curPos.second) <= detectnDistance && targetPos.first >= x1
                && targetPos.first <= x2 && targetPos.second >= y1 && targetPos.second <= y2;
    }

    private void randomWalk() {
        int d;
        boolean flag;
        Tuple<Integer, Integer> pos = this.getPos();
        while (true) {
            flag = false;
            d = random.nextInt(4) + 1;
            switch (d) {
                case 1:
                    flag = areaMap[pos.first][pos.second + 1] == 0 ? true : false;
                    break;
                case 2:
                    flag = areaMap[pos.first][pos.second - 1] == 0 ? true : false;
                    break;
                case 3:
                    flag = areaMap[pos.first - 1][pos.second] == 0 ? true : false;
                    break;
                case 4:
                    flag = areaMap[pos.first + 1][pos.second] == 0 ? true : false;
                    break;
            }
            if (flag) {
                if (moveTo(d)) {
                    break;
                }
            }
        }
    }

    @Override
    public void setOnAlert() {
        changeColor(alertOnColor);
    }

    @Override
    public void setOffAlert() {
        changeColor(alertOffColor);
    }

    @Override
    public void run() { // 该生物的移动、攻击都由run发起
        HandleDist hd = new HandleDist(map);
        int nextStepDirection = 0; // 下一步的走向
        Tuple<Integer, Integer> curPos = null, targetPos = null;

        while (world.getWorldState() < 2 && this.getHp() > 0) {
            if (target != null) {
                curPos = getPos();
                targetPos = target.getPos();
                if (enemyComing(curPos, targetPos)) { // enemy coming, go attack
                    setOnAlert(); // 警惕
                    if (!reachTarget(curPos.first, curPos.second, targetPos.first, targetPos.second)) {
                        // 使用的是同一刻的地图，在calculateDist方法中获取
                        hd.calculateDist(curPos.first, curPos.second); // step1:从当前坐标开始计算到其余位置的最短距离
                        nextStepDirection = hd.getNextStep(targetPos.first, targetPos.second);// step2: 1 2 3 4 代表上下左右
                        moveTo(nextStepDirection);
                    } else {
                        if (target.getHp() >= 0) {
                            if (cd == 4) {
                                target.beAttack(damage);
                                cd = 3;
                            }
                        } else {
                            target = null; // 目标消灭
                        }
                    }
                } else {// do nothing
                    setOffAlert();// 解除警惕
                    randomWalk();
                }
            } else {
                setOffAlert();
                randomWalk();
            }

            // 处理冷却时间
            if (cd != 4 && cd != 0) {
                cd--;
            } else if (cd == 0) {
                cd = 4;
            }

            try {
                TimeUnit.MILLISECONDS.sleep(sleepTime);
            } catch (InterruptedException e) {

            }
        }
    }
}
