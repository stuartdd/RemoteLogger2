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

    public LogLine(long time, int port, String message) {
        this.time = time;
        this.port = port;
        this.message = message;
        this.exception = null;
    }

    public LogLine(int port, String message) {
        this.time = System.currentTimeMillis();
        this.port = port;
        this.message = message;
        this.exception = null;
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

    public int getPort() {
        return port;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return ConfigData.getInstance().timeStamp(time) + (port < 0 ? "" : " [" + port + "] ") + message;
    }

}
