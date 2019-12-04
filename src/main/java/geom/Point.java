/*
 * Copyright (C) 2019 Stuart Davies
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package geom;
/**
 * I would use the AWT Point but it uses all Doubles. This is simpler as it uses int's
 * 
 * I have made it immutable.
 * 
 */
import java.awt.MouseInfo;

/**
 *
 * @author dev
 */
public class Point {

    public final int x, y;

    /**
     * Return a Point that is the mouse position!
     * @return A new Point
     */
    public static Point currentPos() {
        java.awt.Point p = MouseInfo.getPointerInfo().getLocation();
        return new Point(p.x, p.y);
    }

    /**
     * Create an immutable Point
     * @param x The x location
     * @param y The y location
     */
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Create a new point offset by x and y.
     * <pre>
     * We cannot return the original point as it is immutable.
     * </pre>
     * @param x The x offset + or -
     * @param y The y offset + or -
     * @return The new Point.
     */
    public Point move(int x, int y) {
        return new Point(this.x + x, this.y + y);
    }

    /**
     * Useful for diagnostics
     * @return 
     */
    @Override
    public String toString() {
        return "Point{" + "x=" + x + ", y=" + y + '}';
    }

    /**
     * Generated by the IDE.
     * 
     * @return The unique hash code for this object.
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + this.x;
        hash = 67 * hash + this.y;
        return hash;
    }

    /**
     * Generated by the IDE.
     * 
     * @return true if the objects are equal in all respects.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Point other = (Point) obj;
        if (this.x != other.x) {
            return false;
        }
        if (this.y != other.y) {
            return false;
        }
        return true;
    }
    
    
}