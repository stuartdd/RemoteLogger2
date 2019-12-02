/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package geom;

/**
 * @author dev
 */
public class Reading {

    private static final double TOO_DEGREES = (180/Math.PI);
    private static final int SERIES_X = 0;
    private static final int SERIES_Y = 1;
    private static final int SERIES_NS = 2;
    private static final int SERIES_WE = 3;
    private static final int SERIES_LA = 4;
    private final int[] series;
    private final boolean b1;
    private final boolean b2;
    private final long timestamp;

    public Reading(int x, int y, int ns, int we, boolean b1, boolean b2) {
        series = new int[5];
        series[SERIES_X] = x;
        series[SERIES_Y] = y;
        series[SERIES_NS] = ns;
        series[SERIES_WE] = we;

        this.b1 = b1;
        this.b2 = b2;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "Reading{" + "x=" + getX() + ", y=" + getY() + ", ns=" + getNS() + ", we=" + getWE() + ", la=" + getLatency()+ " b1=" + b1 + ", b2=" + b2 + "}";
    }

    public static Reading parse(String data) {
        String[] values = data.split("\\,");
        if (values.length != 6) {
            System.err.println("Invalid sensor data ["+data+"]");
            return null;
        }
        try {
            int x = parseInt(values[SERIES_X], "X", data);
            int y = parseInt(values[SERIES_Y], "Y", data);
            int ns = parseInt(values[SERIES_NS], "NS", data);
            int we = parseInt(values[SERIES_WE], "WE", data);
            boolean b1 = parseBool(values[3], "B1", data);
            boolean b2 = parseBool(values[4], "B2", data);
            return new Reading(x, y, ns, we, b1, b2);
        } catch (DataException ex) {
            System.err.println("Invalid sensor data ["+data+"] "+ex.getMessage());
            return null;
        }
    }

    private static int parseInt(String value, String data, String name) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException nfe) {
            throw new DataException("Invalid integer read: value[" + value + "] name[" + name + "] data[" + data + "]", nfe);
        }
    }

    private static boolean parseBool(String value, String data, String name) {
        if (value.trim().startsWith("1")) {
            return true;
        }
        return false;
    }

    public void setLatency(long l) {
        series[SERIES_LA] = (int) l;
    }
    
    public int getLatency() {
        return series[SERIES_LA];
    }
    
    
    public int getPolarDegrees() {
        return (int)Math.round(Math.atan2(getWE(), getNS()) * TOO_DEGREES);

    }

    public boolean isB1() {
        return b1;
    }

    public boolean isB2() {
        return b2;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getX() {
        return series[SERIES_X];
    }

    public int getY() {
        return series[SERIES_Y];
    }
    
    public int getNS() {
        return series[SERIES_NS];
    }
    
    public int getWE() {
        return series[SERIES_WE];
    }

    public String getFormattedX() {
        return formatNum(getX());
    }

    public String getFormattedY() {
        return formatNum(getY());
    }
    
    public String getFormattedNS() {
        return formatNum(getNS());
    }
    
    public String getFormattedWE() {
        return formatNum(getWE());
    }
    
    public String getFormattedLatency() {
        return formatNum(getLatency());
    }

    public int[] getSeries() {
        return series;
    }

    public static String formatNum(int num) {
        String s;
        long abs = Math.abs(num);
        if (abs < 10) {
            s = "000"+abs;
        } else {
            if (abs < 100) {
                s = "00"+abs;
            } else {
                if (abs < 1000) {
                    s = "0"+abs;
                } else {
                    s = "" + abs;
                }
            }
        }
        if (num <0) {
            return "-"+s;
        }
        return " "+s;
    }
}
