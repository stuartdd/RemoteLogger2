/*
 * Copyright (C) 2018 stuartdd
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
 */
package server;

import common.CommonLogger;
import common.ConfigDataException;
import common.LogLine;
import common.Notifier;
import common.PropertyDataWithAnnotations;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author stuart
 */
public class ServerManager {

    private static Map<Integer, Server> servers = new ConcurrentHashMap<>();
 
    public static void clear() {
        servers = new ConcurrentHashMap<>();
    }
    
    public static void removeServer(String id) {
        servers.remove(Integer.parseInt(id));
    }

    public static void addServer(String portStr, ServerConfig config, Notifier serverNotifier, CommonLogger logger) {
        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException ex) {
            throw new ConfigDataException("Port number [" + portStr + "] is invalid");
        }
        servers.put(port, new Server(port, config, null, serverNotifier, logger));
    }

    public static boolean isServerRunning(int port) {
        Server server = servers.get(port);
        if (server != null) {
            return server.isRunning();
        }
        return false;
    }

    public static ServerState getServerState(int port) {
        Server server = servers.get(port);
        if (server != null) {
            return server.getServerState();
        }
        return ServerState.SERVER_STOPPED;
    }

    public static void startServer(int port) {
        Server server = servers.get(port);
        if (server != null) {
            server.start();
        }
    }

    public static void stopServer(int port) {
        Server server = servers.get(port);
        if (server != null) {
            server.stop(false);
        }
    }

    public static void stopAllServers() {
        for (Server server : servers.values()) {
            server.stop(true);
        }
    }

    public static void autoStartServers() {
        for (Server server : servers.values()) {
            if (server.isAutoStart()) {
                if (!server.start()) {
                    if (server.getLogger()!=null) {
                        server.getLogger().log(new LogLine(server.getPort(), "Server failed to start (Time out)"));
                    }
                }
            }
        }
    }
    
    public static void setShowPort(int port, boolean selected) {
         Server server = servers.get(port);
        if (server != null) {
            server.setShowPort(selected);
        }
    }

    public static boolean isShowPort(int port) {
        Server server = servers.get(port);
        if (server != null) {
            return server.isShowPort();
        }
        return false;
    }
    
    public static void setAutoStart(int port, boolean selected) {
        Server server = servers.get(port);
        if (server != null) {
            server.setAutoStart(selected);
        }
    }

    public static boolean isAutoStart(int port) {
        Server server = servers.get(port);
        if (server != null) {
            return server.isAutoStart();
        }
        return false;
    }

    public static int countServersRunning() {
        int count = 0;
        for (Server server : servers.values()) {
            if (server.isRunning()) {
                count++;
            }
        }
        return count;
    }

    public static boolean hasPort(int port) {
        return servers.containsKey(port);
    }

    public static int[] ports() {
        int[] in = new int[servers.size()];
        int pos = 0;
        for (Integer port : servers.keySet()) {
            in[pos] = port;
            pos++;
        }
        Arrays.sort(in);
        return in;
    }

    public static Map<String, PropertyDataWithAnnotations> serverConfigDataMap() {
        Map<String, PropertyDataWithAnnotations> ret = new HashMap<>();
        for (Map.Entry<Integer, Server> s:servers.entrySet()) {
            ret.put(s.getKey().toString(), s.getValue().getServerConfig());
        }
        return ret;
    }

    
    public static int[] portListSorted() {
        int[] pl = new int[servers.size()];
        int pos = 0;
        for (Integer key:servers.keySet()) {
            pl[pos] = key;
            pos++;
        }
        Arrays.sort(pl);
        return pl;
    }
    
    public static List<Integer> portList() {
        List<Integer> l = new ArrayList<>();
        for (int p : portListSorted()) {
            l.add(p);
        }
        return l;
    }

    public static Server getServer(int port) {
        return servers.get(port);
    }

    public static ServerExpectations getExpectationManager(int port) {
        Server server = getServer(port);
        if (server == null) {
            return null;
        }
        return server.getServerExpectations();
    }

    public static Map<String, PropertyDataWithAnnotations> getExpectationsMap(int port) {
        Server server = getServer(port);
        if (server == null) {
            return null;
        }
        if (server.getServerExpectations() == null) {
            return null;
        }
        return server.getServerExpectations().getExpectationsDataMap();
    }


}
