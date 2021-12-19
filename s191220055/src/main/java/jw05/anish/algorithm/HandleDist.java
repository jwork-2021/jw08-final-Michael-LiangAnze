package jw05.anish.algorithm;
import java.util.LinkedList;
import java.util.Queue;
import jw05.anish.map.Map;

public class HandleDist {
    int mapSize;
    int[][] dist = null;
    Map map;
    int[][] mapList = null; // 对于获得的数组maplist，0为可行，1为墙体，2为生物

    public HandleDist(Map m) {
        this.mapSize = m.getMapSize();
        this.map = m;
        dist = new int[mapSize][mapSize];
        mapList = new int[mapSize][mapSize];
        for (int i = 0; i < mapSize; ++i) {
            for (int j = 0; j < mapSize; ++j) {
                dist[i][j] = -1;
                mapList[i][j] = -1;
            }
        }
    }

    public int[][] getDist() {
        return dist;
    }

    public void output() {
        for (int i = 0; i < mapSize; ++i) {
            for (int temp : dist[i]) {
                System.out.format("%3s ", temp);
            }
            System.out.print('\n');
        }
            
        System.out.print('\n');
    }

    public void resetDist() {
        for (int i = 0; i < mapSize; ++i) {
            for (int j = 0; j < mapSize; ++j) {
                if (mapList[i][j] == 0) {
                    dist[i][j] = -1;
                } else { // 是墙，或者存在生物
                    //dist[i][j] = Integer.MAX_VALUE;
                    dist[i][j]=100;
                }
            }
        }
    }

    public void calculateDist(int beginX, int beginY) {
        Queue<Tuple<Integer, Integer>> Q = new LinkedList<>();
        Q.offer(new Tuple<Integer, Integer>(beginX, beginY));
        Tuple<Integer, Integer> temp;
        int tempx, tempy;
        map.getMapState(mapList);
        // map.outputMap();
        resetDist();
        // output();// 获取地图后，需要重新设置距离
        dist[beginX][beginY] = 0;
        mapList[beginX][beginY]=0;//要将自己的位置设置为可行，否则无法计算路径
        while ((temp = Q.poll()) != null) {// queue is not empty
            // 检查上下左右是否被访问过
            tempx = temp.first;
            tempy = temp.second;
            if (mapList[tempx - 1][tempy] == 0 && dist[tempx - 1][tempy] == -1) {
                dist[tempx - 1][tempy] = dist[tempx][tempy] + 1;
                Q.offer(new Tuple<Integer, Integer>(tempx - 1, tempy));
            }
            if (mapList[tempx + 1][tempy] == 0 && dist[tempx + 1][tempy] == -1) {
                dist[tempx + 1][tempy] = dist[tempx][tempy] + 1;
                Q.offer(new Tuple<Integer, Integer>(tempx + 1, tempy));
            }
            if (mapList[tempx][tempy - 1] == 0 && dist[tempx][tempy - 1] == -1) {
                dist[tempx][tempy - 1] = dist[tempx][tempy] + 1;
                Q.offer(new Tuple<Integer, Integer>(tempx, tempy - 1));
            }
            if (mapList[tempx][tempy + 1] == 0 && dist[tempx][tempy + 1] == -1) {
                dist[tempx][tempy + 1] = dist[tempx][tempy] + 1;
                Q.offer(new Tuple<Integer, Integer>(tempx, tempy + 1));
            }
            // output();
        }
        //output();
        // System.out.println("done");
        
    }

    public int getNextStep(int targetX, int targetY) { // 1 2 3 4 分别代表上下左右
        //注意，因为生物所在位置被设置为不可行
        // 因此不能直接取用dist[targetX][targetX]
        // 应该选用敌人附近最近的一块砖作为移动的目标
        // int distance = dist[targetX][targetY];
        int distance = Integer.MAX_VALUE;
        if(dist[targetX-1][targetY] < distance && dist[targetX-1][targetY] != -1){
            distance=dist[targetX-1][targetY];
            // targetX=targetX-1;
        }
        if(dist[targetX+1][targetY] < distance && dist[targetX+1][targetY] != -1){
            distance=dist[targetX+1][targetY];
            // targetX=targetX+1;
        }
        if(dist[targetX][targetY-1] < distance && dist[targetX][targetY-1] != -1){
            distance=dist[targetX][targetY-1];
            // targetY=targetY-1;
        }
        if(dist[targetX][targetY+1] < distance && dist[targetX][targetY+1] != -1){
            distance=dist[targetX][targetY+1];
            // targetY=targetY+1;
        }
        int res = 0;
        distance = distance + 1;
        dist[targetX][targetY] = distance;
        while (distance > 0) {
            distance = distance - 1;
            // 选择dist为distance-1的,可移动的一块砖进行移动
            if (dist[targetX][targetY - 1] == distance && mapList[targetX][targetY - 1] == 0) {
                targetY = targetY - 1;
                res = distance == 0 ? 1 : 0;
            } else if (dist[targetX][targetY + 1] == distance && mapList[targetX][targetY + 1] == 0) {
                targetY = targetY + 1;
                res = distance == 0 ? 2 : 0;
            } else if (dist[targetX + 1][targetY] == distance && mapList[targetX + 1][targetY] == 0) {
                targetX = targetX + 1;
                res = distance == 0 ? 3 : 0;
            } else if (dist[targetX - 1][targetY] == distance && mapList[targetX - 1][targetY] == 0) {
                targetX = targetX - 1;
                res = distance == 0 ? 4 : 0;
            }
        }
        return res;
    }

}