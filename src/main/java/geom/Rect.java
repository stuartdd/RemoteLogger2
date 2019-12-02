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
 * I would use the AWT Rectangle but it uses all Doubles. This is simpler as it uses int's
 * 
 * I have made it immutable.
 * 
 */
public class Rect {

    private final int x;
    private final int y;
    private final int w;
    private final int h;

    public Rect(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    /**
     * Get top left!
     * @return a Point inside the rect
     */
    public Point getPoint() {
        return new Point(getX(), getY());
    }

    /**
     * Get bottom right!
     * @return a Point inside the rect
     */
    public Point getPointBR() {
        return new Point(getMaxX(), getMaxY());
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getW() {
        return w;
    }

    public int getH() {
        return h;
    }

    /**
     * Get the right hand side. -1 so we stay in the rectangle!
     * @return the max x value inside the rect
     */
    public int getMaxX() {
        return x + w -1;
    }
    
    /**
     * Get the bottom value. -1 so we stay in the rectangle!
     * @return the max y value inside the rect
     */
    public int getMaxY() {
        return y + h -1;
    }

    @Override
    public String toString() {
        return "Rect{" +
                "x=" + x +
                ", y=" + y +
                ", w=" + w +
                ", h=" + h +
                '}';
    }
}
