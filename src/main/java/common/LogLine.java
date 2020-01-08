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
package common;

/**
 * @author Stuart
 */
public class LogLine implements Loggable {

    private int lineNo = 0;
    private final long time;
    private final int port;
    private final String message;
    private final Throwable exception;
    private Loggable next;

    public LogLine(int port, String message) {

        this.time = System.currentTimeMillis();
        this.port = port;
        this.message = message;
        this.exception = null;
    }

    public LogLine(int port, Throwable ex) {
        this.time = System.currentTimeMillis();
        this.port = port;
        this.message = null;
        this.exception = ex;
    }

    public LogLine(int port, String message, Throwable ex) {
        this.time = System.currentTimeMillis();
        this.port = port;
        this.message = message;
        this.exception = ex;
    }

    public int getLineNo() {
        return lineNo;
    }

    public void setLineNo(int lineNo) {
        this.lineNo = lineNo;
    }

    public long getTime() {
        return time;
    }

    public boolean hasException() {
        return (exception != null);
    }

    public boolean hasPort() {
        return (port > 0);
    }

    public int getPort() {
        return port;
    }

    public Loggable getNext() {
        return next;
    }

    public void setNext(Loggable next) {
        this.next = next;
    }

    public boolean hasMessage() {
        if (message == null) {
            return false;
        }
        if (message.trim().length() == 0) {
            return false;
        }
        return true;
    }

    public String getMessage() {
        if (hasMessage()) {
            return message;
        }
        return null;
    }

    @Override
    public String toString() {
        return toString(null);
    }

    public String toString(String dev) {
        return pad(lineNo) + " | "
                + ConfigData.getInstance().timeStamp(time) + " [-] "
                + (hasPort() ? " [" + port + "] " : "")
                + (dev == null ? "" : " [" + dev + "] ")
                + (getMessage() == null ? "" : getMessage() + ". ")
                + (exception == null ? "" : "EXCEPTION: " + exception.getClass().getSimpleName() + ": " + exception.getMessage());
    }

    private String pad(int n) {
        if (n < 10) {
            return "00000" + n;
        } else if (n < 100) {
            return "0000" + n;
        } else if (n < 1000) {
            return "000" + n;
        } else if (n < 10000) {
            return "00" + n;
        } else if (n < 100000) {
            return "0" + n;
        } else {
            return "" + n;
        }
    }
}
