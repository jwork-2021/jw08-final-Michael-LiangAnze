package jw05;


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import jw05.anish.algorithm.HandleDist;
import jw05.anish.algorithm.Tuple;
import jw05.anish.calabashbros.Cannonball;
import jw05.anish.calabashbros.World;
import jw05.anish.map.Map;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    public void testAlg(){
        Tuple<Integer,Integer>t = new Tuple<Integer,Integer>(0,1);
        assertEquals(String.valueOf(t.first), String.valueOf(0));
        assertEquals(String.valueOf(t.second), String.valueOf(1));
    }

    public void testHandleDist(){
        Map m = new Map(new World(),false);
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
}
