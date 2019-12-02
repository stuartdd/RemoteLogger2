/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package geom;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author dev
 */
public class RectAndPointTest {

    @Test
    public void rectPointTest()  {
        Rect r = new Rect(5, 10, 15, 20);
        assertEquals(5, r.getX());
        assertEquals(10, r.getY());
        assertEquals(15, r.getW());
        assertEquals(20, r.getH());
        assertEquals(19, r.getMaxX());
        assertEquals(29, r.getMaxY());

        assertEquals("Rect{x=5, y=10, w=15, h=20}", r.toString());

        assertEquals("Point{x=5, y=10}", r.getPoint().toString());

        assertEquals("Point{x=19, y=29}", r.getPointBR().toString());

        Point p1 = new Point(5, 10);
        Point p2 = new Point(19, 29);
        assertTrue(p1.equals(r.getPoint()));
        assertTrue(r.getPoint().equals(p1));
        assertTrue(p2.equals(r.getPointBR()));
        assertTrue(r.getPointBR().equals(p2));

        assertFalse(p1.equals(p2));
        assertFalse(p2.equals(p1));

    }

}
