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

import com.sun.net.httpserver.HttpServer;
import common.Action;
import common.CommonLogger;
import common.LogLine;
import common.Notification;
import common.Util;
import java.io.IOException;
import java.net.InetSocketAddress;

public class ServerThread extends Thread {

    private ServerState serverState = ServerState.SERVER_STOPPED;
    private final Server theServer;
    private boolean running;
    private boolean serverCanRun;
    private int timeToClose;
    private final ServerExpectationHandler serverExpectationHandler;
    private final ControlHandler controlHandler;

    public ServerThread(Server server) {
        this.theServer = server;
        this.timeToClose = server.getTimeToClose();
        this.running = false;
        this.serverCanRun = true;
        this.serverExpectationHandler = new ServerExpectationHandler(server);
        this.controlHandler = new ControlHandler(server);
        newState(ServerState.SERVER_PENDING, "");
    }

    @Override
    public void run() {
        running = true;
        serverCanRun = true;
        HttpServer httpServer;
        try {
            newState(ServerState.SERVER_STARTING, "");
            httpServer = bind(theServer.getPort(), theServer.getLogger());
            httpServer.createContext("/control", controlHandler);
            httpServer.createContext("/", serverExpectationHandler);
            httpServer.setExecutor(null); // creates a default executor
            httpServer.start();
        } catch (IOException ex) {
            newState(ServerState.SERVER_FAIL, ex.getMessage());
            return;
        }
        newState(ServerState.SERVER_RUNNING, null);
        do {
            Util.sleep(50);
        } while (serverCanRun);
        newState(ServerState.SERVER_STOPPING, " Time to close:" + timeToClose);
        httpServer.stop(timeToClose);
        newState(ServerState.SERVER_STOPPED, null);
        running = false;
    }

    private HttpServer bind(int port, CommonLogger logger) throws IOException {
        int tries = 0;
        HttpServer newServer = null;
        IOException exception = null;
        while ((tries < 20) && (newServer == null)) {
            try {
                newServer = HttpServer.create(new InetSocketAddress(port), 0);
            } catch (IOException ex) {
                exception = ex;
                if (logger != null) {
                    logger.log(new LogLine(port, "Server Port in use. Try " + tries));
                }
            }
            tries++;
            Util.sleep(100);
        }
        if ((newServer == null) && (exception != null)) {
            throw exception;
        }
        return newServer;
    }

    private synchronized void newState(ServerState state, String additional) {
        serverState = state;
        theServer.getLogger().log(new LogLine(theServer.getPort(), state.getInfo() + (additional == null ? "" : ". " + additional)));
        if (theServer.getServerNotifier() != null) {   
            Notification notification = new Notification(theServer.getPort(), Action.SERVER_STATE, state.getInfo() + (additional == null ? "" : ". " + additional)).
                    withData("state", state).
                    withData("server", theServer);
            theServer.getServerNotifier().notifyAction(notification);
        }
    }

    public ServerState getServerState() {
        return serverState;
    }

    public boolean isRunning() {
        return running;
    }

    public void stopServer(boolean now) {
        if (theServer.getLogger()!= null) {
            theServer.getLogger().log(new LogLine(theServer.getPort(), "STOP-SERVER Via stopServer()"));
        }
        if (now) {
            timeToClose = 0;
        } else {
            timeToClose = theServer.getTimeToClose();
        }
        serverCanRun = false;
    }

    void setCallBackClass(ServerCallbackHandler responseHandler) {
        if (serverExpectationHandler != null) {
            serverExpectationHandler.setCallBackClass(responseHandler);
        }
    }

    ServerCallbackHandler getCallBackClass() {
        if (serverExpectationHandler != null) {
            return serverExpectationHandler.getCallBackClass();
        }
        return null;
    }

}
