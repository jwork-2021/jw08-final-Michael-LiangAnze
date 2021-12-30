package jw05;


import java.awt.Color;
import java.util.concurrent.locks.ReentrantLock;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import jw05.anish.algorithm.HandleDist;
import jw05.anish.algorithm.Tuple;
import jw05.anish.calabashbros.Cannonball;
import jw05.anish.calabashbros.CannonballList;
import jw05.anish.calabashbros.Creature;
import jw05.anish.calabashbros.Floor;
import jw05.anish.calabashbros.MapItem;
import jw05.anish.calabashbros.Player;
import jw05.anish.calabashbros.Reward;
import jw05.anish.calabashbros.Shooter;
import jw05.anish.calabashbros.SworksMan;
import jw05.anish.calabashbros.Thing;
import jw05.anish.calabashbros.Tile;
import jw05.anish.calabashbros.Wall;
import jw05.anish.calabashbros.World;
import jw05.anish.map.Map;
import jw05.anish.map.MapUpdateInfo;
import jw05.anish.screen.ScreenInfo;
import jw05.asciiPanel.AsciiPanel;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    World w = new World();
    Map m = new Map(w,false);

    public void testAlg(){
        Tuple<Integer,Integer>t = new Tuple<Integer,Integer>(0,1);
        assertEquals(String.valueOf(t.first), String.valueOf(0));
        assertEquals(String.valueOf(t.second), String.valueOf(1));
    }

    public void testHandleDist(){
        m.loadMap();
        // m.outputMap();
        HandleDist hd = new HandleDist(m);
        hd.calculateDist(18,3);
        // hd.output();
        
        int [][] dist = hd.getDist();
        assertEquals(String.valueOf(dist[18][7]), String.valueOf(4));
        assertEquals(String.valueOf(hd.getNextStep(18, 7)), String.valueOf(1));
    }

    public void testCannonball(){
        Cannonball c = new Cannonball(1, 1, new World(),99);
        assertEquals(String.valueOf(c.getDirection()), String.valueOf(1));
        assertEquals(String.valueOf(c.getOwner()), String.valueOf(99));
        assertEquals(String.valueOf(c.getType()), "cannonball");
    }

    public void testCannonballList(){
        CannonballList cl = new CannonballList(1, 2, m, w);
        assertEquals(String.valueOf(1), String.valueOf(cl.getDamage()));
    }

    public void testFloor(){
        Floor f1 = new Floor(w);
        Floor f2 = new Floor(w,new Color(120,120,1));
        assertEquals(String.valueOf(f1.getType()), String.valueOf("floor"));
        assertEquals(String.valueOf(f1.getColor()), String.valueOf(Color.gray));

        assertEquals(String.valueOf(f2.getType()), String.valueOf("floor"));
        assertEquals(String.valueOf(f2.getColor()), String.valueOf(new Color(120,120,1)));
    }

    public void testMapItem(){
        MapItem mi1 = new MapItem(new Color(120,120,120), 1, w);
        assertEquals(String.valueOf((int)mi1.getGlyph()), String.valueOf(1));
        assertEquals(String.valueOf(mi1.getType()), String.valueOf("mapItem"));
        assertEquals(String.valueOf(mi1.getColor()), String.valueOf(new Color(120,120,120)));
    }

    public void testPlayer(){
        Player p = new Player(Color.red, 1, 1, 1, w, m, new CannonballList(1, 1, m, w));
        assertEquals(String.valueOf(p.getType()), String.valueOf("player"));
        p.setId(12);
        assertEquals(String.valueOf(p.getId()), String.valueOf(12));
        p.setInfo(1, 0);
        assertEquals(String.valueOf(p.getHp()), String.valueOf(1));
        assertEquals(String.valueOf(p.getScore()), String.valueOf(0));
        p.beAttack(1);
        assertEquals(String.valueOf(p.getHp()), String.valueOf(0));
        p.getReward();
        assertEquals(String.valueOf(p.getScore()), String.valueOf(1));
    }

    public void testReward(){
        Reward r = new Reward(Color.red, 1, w);
        assertEquals(String.valueOf(r.getType()), String.valueOf("reward"));
        assertEquals(String.valueOf(r.getColor()), String.valueOf(Color.red));
        assertEquals(String.valueOf((int)r.getGlyph()), String.valueOf(1));
    }

    public void testShooter(){
        Shooter s = new Shooter(1, 1, 1, w, m, null, null, 0, 0, 1, 1);
        assertEquals(String.valueOf(s.getRank()), String.valueOf(1));
        s.setOnAlert();
        assertEquals(String.valueOf(s.getColor()), String.valueOf(new Color(255, 0, 0)));
        s.setOffAlert();
        assertEquals(String.valueOf(s.getColor()), String.valueOf(new Color(162, 45, 95)));
        s.beAttack(1);
        assertEquals(String.valueOf(s.getHp()), String.valueOf(0));
    }

    public void testSworksMan(){
        SworksMan s = new SworksMan(1, 1, 1, 1, 3, w, m, null, 0, 0, 1, 1);
        assertEquals(String.valueOf(s.getRank()), String.valueOf(1));
        s.setOnAlert();
        assertEquals(String.valueOf(s.getColor()), String.valueOf(new Color(255, 255, 0)));
        s.setOffAlert();
        assertEquals(String.valueOf(s.getColor()), String.valueOf(new Color(130, 137, 24)));
        s.beAttack(1);
        assertEquals(String.valueOf(s.getHp()), String.valueOf(2));
    }

    public void testThing(){
        Thing t = new Thing(Color.red, 'r', w);
        assertEquals(String.valueOf(t.getColor()), String.valueOf(Color.red));
        assertEquals(String.valueOf(t.getType()), String.valueOf("null"));
        t.setId(2);
        assertEquals(String.valueOf(t.getId()), String.valueOf(2));
        t.setType("player");
        assertEquals(String.valueOf(t.getType()), String.valueOf("player"));
        t.setType("testThing");
        assertEquals(String.valueOf(t.getType()), String.valueOf("player"));
        t.changeColor(Color.gray);
        assertEquals(String.valueOf(t.getColor()), String.valueOf(Color.gray));
        assertEquals(String.valueOf(t.getGlyph()), String.valueOf('r'));
        t.initIcon(123, 456);
        assertEquals(String.valueOf((int)t.getGlyph()), String.valueOf(114));
        t.setIcon(3);
        assertEquals(String.valueOf((int)t.getGlyph()), String.valueOf(123));
        t.setIcon(4);
        assertEquals(String.valueOf((int)t.getGlyph()), String.valueOf(456));
        t.setTile(new Tile<>());
        t.setPos(new Tuple<Integer,Integer>(1,2));
        assertEquals(String.valueOf(t.getPos()), String.valueOf(new Tuple<Integer,Integer>(1,2)));
    }

    public void testTile(){
        Tile t = new Tile<>(1, 2);
        assertEquals(String.valueOf(t.getPos()), String.valueOf(new Tuple<Integer,Integer>(1,2)));
        t.setPos(new Tuple<Integer,Integer>(3,4));
        assertEquals(String.valueOf(t.getPos()), String.valueOf(new Tuple<Integer,Integer>(3,4)));
    }

    public void testWall(){
        Wall wall = new Wall(w);
        assertEquals(String.valueOf(wall.getType()), String.valueOf("wall"));
        assertEquals(String.valueOf((int)wall.getGlyph()), String.valueOf(177));
        assertEquals(String.valueOf(wall.getColor()), String.valueOf(AsciiPanel.cyan));
    }

    public void testWorld(){
        w.setWorldState(1);
        assertEquals(String.valueOf(w.getWorldState()), String.valueOf(1));
        assertEquals(String.valueOf(w.getWorldSize()), String.valueOf(40));
        w.put(new Floor(w), new Tuple<Integer,Integer>(1,2));
        assertEquals(String.valueOf(w.get(1,2).getType()), String.valueOf("floor"));
        w.put(new Wall(w), new Tuple<Integer,Integer>(3,4));
        w.swapPos(new Tuple<Integer,Integer>(1,2), new Tuple<Integer,Integer>(3,4));
        assertEquals(String.valueOf(w.get(3,4).getType()), String.valueOf("floor"));
        assertEquals(String.valueOf(w.get(1,2).getType()), String.valueOf("wall"));
    }
    
    public void testMap(){
        assertEquals(String.valueOf(m.getMapSize()), String.valueOf(40));
        m.setThing(new Tuple<Integer,Integer>(1,2), 1, new Floor(w));
        assertTrue(!m.setThing(new Tuple<Integer,Integer>(1,2), 1, new Floor(w)));
    }

    public void testMapUpdateInfo(){
        MapUpdateInfo mui = new MapUpdateInfo(1, "beAttacked");
        assertEquals(String.valueOf(mui.toString()), String.valueOf("beAttacked 1"));
    }

    public void testScreenInfo(){
        ScreenInfo si = new ScreenInfo(w, Color.red, 1);
        assertEquals(String.valueOf(si.getColor()), String.valueOf(Color.red));
    }
}
