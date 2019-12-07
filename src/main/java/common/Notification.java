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

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Stuart
 */
public class Notification {

    private static String format;

    private final long time;
    private final int port;
    private final Action action;
    private Map<String, Object> actionData;
    private final String message;

    public Notification(int port, Action action, Map<String, Object> actionOn, String message) {
        this.time = System.currentTimeMillis();
        this.port = port;
        this.action = action;
        this.actionData = actionOn;
        this.message = message;
    }

    public Notification(int port, Action action, String message) {
        this.time = System.currentTimeMillis();
        this.port = port;
        this.action = action;
        this.actionData = new HashMap<>();
        this.message = message;
    }

    public Notification(int port, Action action, String id, Object actionOn, String message) {
        this.time = System.currentTimeMillis();
        this.port = port;
        this.action = action;
        this.actionData = new HashMap<>();
        this.actionData.put(id, actionOn);
        this.message = message;
    }

    public String toString(String dev) {
        return ConfigData.getInstance().timeStamp(time) + " [+] "
                + (port < 0 ? "" : " [" + port + "]")
                + (action == null ? " " : " [" + action.name() + "]")
                + (dev == null ? " " : " [" + dev + "] ")
                + message;
    }

    @Override
    public String toString() {
        return toString(null);
    }

    public Notification withData(String name, Object value) {
        if (actionData == null) {
            actionData = new HashMap<>();
        }
        actionData.put(name, value);
        return this;
    }

    public static String getFormat() {
        return format;
    }

    public long getTime() {
        return time;
    }

    public int getPort() {
        return port;
    }

    public Action getAction() {
        return action;
    }

    public Map<String, Object> getActionOn() {
        return actionData;
    }

    public String getMessage() {
        return message;
    }

    public Object getData(String name) {
        if (actionData == null) {
            return null;
        }
        return actionData.get(name);
    }

}
