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
 *
 * @author Stuart
 */
public class LogLine {

    private final long time;
    private final int port;
    private final String message;
    private final Throwable exception;

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
        return "";
    }

    @Override
    public String toString() {
        return toString(null);
    }

    public String toString(String dev) {
        return ConfigData.getInstance().timeStamp(time) + " [-] " + (hasPort() ? " [" + port + "]" : " ") + (dev == null ? " " : " [" + dev + "] ") + getMessage() + (exception == null ? "." : "."+exception.getMessage());
    }

}
