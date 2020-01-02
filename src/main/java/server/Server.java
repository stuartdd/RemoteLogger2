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
import common.ConfigData;
import common.Notifier;
import common.Util;

/**
 * @author stuart
 */
public class Server {

    private final Integer port;
    private final CommonLogger logger;
    private final Notifier serverNotifier;
    private final ServerCallbackHandler callbackHandler;
    private final ServerStatistics serverStatistics;
    private ServerThread serverThread;

    public Server(Integer port, ServerCallbackHandler callbackHandler, Notifier serverNotifier, CommonLogger logger) {
        ServerConfig serverConfig = ConfigData.getInstance().getServers().get(port.toString());
        if (serverConfig == null) {
            throw new ServerConfigException("Server serverConfig is null");
        }
        this.serverThread = null;
        this.port = port;
        this.logger = logger;
        this.serverNotifier = serverNotifier;
        this.callbackHandler = callbackHandler;
        this.serverStatistics = new ServerStatistics();
        ServerExpectationManager.loadExpectation(port, serverConfig.getExpectationsFile(), logger);
    }

    public CommonLogger getLogger() {
        return logger;
    }

    public Notifier getServerNotifier() {
        return serverNotifier;
    }

    public int getPort() {
        return port;
    }

    public int getTimeToClose() {
        return getServerConfig().getTimeToClose();
    }

    public ServerConfig getServerConfig() {
        ServerConfig sc = ConfigData.getInstance().getServers().get(port.toString());
        if (sc == null) {
            throw new ServerConfigException("Enable to find server configuration (ServerConfig) data for port "+port);
        }
        return sc;
     }

    public ServerCallbackHandler getCallbackHandler() {
        if (serverThread != null) {
            return serverThread.getCallBackClass();
        }
        return null;
    }

    public void setCallBackHandler(ServerCallbackHandler responseHandler) {
        if (serverThread != null) {
            serverThread.setCallBackClass(responseHandler);
        }
    }

    public ServerExpectations getServerExpectations() {
        return ServerExpectationManager.getExpectations(getServerConfig().getExpectationsFile());
    }

    public ServerStatistics getServerStatistics() {
        return serverStatistics;
    }

    public boolean start() {
        serverThread = new ServerThread(this);
        serverThread.start();
        for (int i = 0; i < 10; i++) {
            if (serverThread.isRunning()) {
                return true;
            }
            Util.sleep(100);
        }
        return false;
    }

    public void stop(boolean now) {
        if (serverThread != null) {
            serverThread.stopServer(now);
        }
    }

    public boolean isRunning() {
        if (serverThread != null) {
            return serverThread.isRunning();
        }
        return false;
    }

    public ServerState getServerState() {
        if (serverThread != null) {
            return serverThread.getServerState();
        }
        return ServerState.SERVER_STOPPED;
    }

    public void setAutoStart(boolean selected) {
        getServerConfig().setAutoStart(selected);
    }

    public boolean isAutoStart() {
        return getServerConfig().isAutoStart();
    }

    public void setShowPort(boolean selected) {
        getServerConfig().setShowPort(selected);
    }

    public void setLogProperties(boolean selected) {
        getServerConfig().setLogProperties(selected);
    }

    public boolean isShowPort() {
        return getServerConfig().isShowPort();
    }

    public boolean isLogProperties() {
        return getServerConfig().isLogProperties();
    }


    @Override
    public String toString() {
        return "Server{" + "port=" + port + '}';
    }

}
