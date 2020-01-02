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
package main;

import common.*;
import server.ServerConfig;
import server.ServerManager;

import java.util.Map;

/**
 *
 * @author Stuart
 */
public class MainHeadless implements Notifier, CommonLogger {
    
 
    /**
     * @param args the command line arguments
     *
     * Currently just the config file name
     */
   public static void main(String[] args) {
        Object notifier = new MainHeadless();
        ConfigData.load("config.json");
        for (Map.Entry<String, ServerConfig> sc : ConfigData.getInstance().getServers().entrySet()) {
            ServerManager.addServer(sc.getKey(), (Notifier)notifier, (CommonLogger)notifier);
        }
        ServerManager.autoStartServers();
        while (ServerManager.countServersRunning() > 0) {
            Util.sleep(100);
        }
    }

    @Override
    public void notifyAction(Notification notification) {
        System.out.println("ACT:" + ConfigData.getInstance().timeStamp(notification.getTime()) + " [" + notification.getPort() + "] [" + notification.getAction().name() + "] " + notification.getMessage());
    }

    @Override
    public void log(Loggable l) {
        System.out.println("LOG:"+l.toString());
    }

}
