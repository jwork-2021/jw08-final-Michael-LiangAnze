package jw05.anish.calabashbros;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.awt.Color;
import jw05.anish.algorithm.Tuple;
import jw05.anish.map.Map;

public class Shooter extends Creature implements Runnable {
    private int rank;
    Player target;
    private int cd;// 冷却时间
    private int sleepTime;
    Random random;
    private final Color alertOnColor = new Color(255, 0, 0);
    private final Color alertOffColor = new Color(162, 45, 95);
    CannonballList cannonballList;
    int cannonDamage;
    private int x1, y1, x2, y2;
    private int[][] areaMap;

    public Shooter(int rank, int speed, int hp, World world, Map map, Player enemy, CannonballList cannonballList,
            int x1, int y1, int x2, int y2) {
        super(new Color(162, 45, 95), (char) 1, world);
        this.initIcon(1, 11);
        this.rank = rank;
        this.speed = speed;
        this.hp = hp;
        this.map = map;
        target = enemy;
        this.cannonballList = cannonballList;
        random = new Random();
        this.sleepTime = 1000 / speed * 50;
        this.cd = 1;
        cannonDamage = cannonballList.getDamage();
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

        // for (int i = 0; i < mapSize; ++i) {
        //     for (int j = 0; j < mapSize; ++j) {
        //         if (i < x1 || i > x2 || j < y1 || j > y2) {
        //             areaMap[i][j] = 1; // 不可见
        //         }
        //     }
        // }
        // 不同于剑客，射手应当看到完整的地图，不然无法开火
    }

    public int getRank() {
        return this.rank;
    }

    @Override
    public String toString() {
        return String.valueOf(this.rank);
    }

    void outputArea(){
        int mapSize = 40;
        for (int i = 0; i < mapSize; ++i) {
            for (int j = 0; j < mapSize;++j){
                System.out.print(areaMap[i][j]+" ");
            }
            System.out.print('\n');
        }
    }
    public void setTarget(Player c) {
        this.target = c;
    }

    private int isFire(Tuple<Integer, Integer> curPos, Tuple<Integer, Integer> targetPos) {
        // if (targetPos.first < x1 || targetPos.first > x2 || targetPos.second < y1 || targetPos.second > y2) {
        //     return 0;
        // }
        boolean test = true;
        if (curPos.first == targetPos.first) {
            if (curPos.second < targetPos.second) { // 在上面
                for (int i = curPos.second + 1; i < targetPos.second; i++) {
                    if (areaMap[curPos.first][i] != 0) {
                        test = false;
                        break;
                    }
                }
                if (test) {
                    return 1;
                } else {
                    return 0;
                }
            } else {
                for (int i = targetPos.second + 1; i < curPos.second; i++) {
                    if (areaMap[curPos.first][i] != 0) {
                        test = false;
                        break;
                    }
                }
                if (test) {
                    return 2;
                } else {
                    return 0;
                }
            }
        } else if (curPos.second == targetPos.second) {
            if (curPos.first < targetPos.first) {
                for (int i = curPos.first + 1; i < targetPos.first; i++) {
                    if (areaMap[i][curPos.second] != 0) {
                        test = false;
                        break;
                    }
                }
                if (test) {
                    return 4;
                } else {
                    return 0;
                }
            } else {
                // outputArea();
                for (int i = targetPos.first + 1; i < curPos.first; i++) {
                    if (areaMap[i][curPos.second] != 0) {
                        test = false;
                        break;
                    }
                }
                if (test) {        
                    return 3;
                } else {
                    return 0;
                }
            }
            // return curPos.first < targetPos.first?4:3;
        }

        return 0;
    }

    private void attack(Tuple<Integer, Integer> curPos, int direction) {
        this.setIcon(direction);
        switch(direction){
            case 1:cannonballList.addCannonball(new Tuple<Integer, Integer>(curPos.first, curPos.second + 1), direction);break;
            case 2:cannonballList.addCannonball(new Tuple<Integer, Integer>(curPos.first, curPos.second - 1), direction);break;
            case 3:cannonballList.addCannonball(new Tuple<Integer, Integer>(curPos.first - 1, curPos.second), direction);break;
            case 4:cannonballList.addCannonball(new Tuple<Integer, Integer>(curPos.first + 1, curPos.second), direction);break;
        }
    }

    @Override
    public synchronized void beAttack(int damage) {
        this.hp -= damage;
    }

    private void randomWalk() {
        int d;
        boolean flag;
        Tuple<Integer, Integer> pos = this.getPos();
        while (true) {
            flag = false;
            d = random.nextInt(4) + 1;

            if (d == 1) {
                flag = areaMap[pos.first][pos.second + 1] == 0 && pos.second < y2? true : false;
            } else if (d == 2) {
                flag = areaMap[pos.first][pos.second - 1] == 0 && pos.second > y1? true : false;
            } else if (d == 3) {
                flag = areaMap[pos.first - 1][pos.second] == 0 && pos.first > x1? true : false;
            } else if (d == 4) {
                flag = areaMap[pos.first + 1][pos.second] == 0 && pos.first < x2? true : false;
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
        int targetStepDirection = 0; // 下一步的走向
        Tuple<Integer, Integer> curPos = null, targetPos = null;

        while (world.getWorldState() < 2 && this.getHp() > 0) {
            if (target != null) {
                curPos = this.getPos();
                targetPos = target.getPos();
                if ((targetStepDirection = isFire(curPos, targetPos)) != 0) { // 处于攻击位置
                    setOnAlert(); // 警惕
                    if (cd == 5) { // 冷却时间结束
                        if (target.getHp() > 0) {
                            attack(curPos, targetStepDirection);
                            cd = 4;
                        } else { // 目标消灭
                            target = null;
                            setOffAlert();
                        }
                    }
                } else {// do nothing
                    setOffAlert();
                    randomWalk();
                }
            } else {
                randomWalk();
            }
            // 处理cd
            if (cd != 5 && cd != 0) {
                cd--;
            } else if (cd == 0) {
                cd = 5;
            }

            try {
                TimeUnit.MILLISECONDS.sleep(sleepTime);
            } catch (InterruptedException e) {

            }
        }
    }
}
