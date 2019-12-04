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

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import common.Action;
import common.Notification;
import common.Notifier;

public class ControlHandler implements HttpHandler {

    private final int port;
    private final Server server;
    private final Notifier serverNotifier;

    public ControlHandler(Server server) {
        this.server = server;
        this.port = server.getPort();
        this.serverNotifier = server.getServerNotifier();
    }

    @Override
    public void handle(HttpExchange he) throws IOException {
        if (he.getRequestURI().toString().contains("/stop")) {
            if (serverNotifier != null) {
                serverNotifier.notifyAction(new Notification(port, Action.SERVER_STATE, null, "Server on port " + port + " is shutting down"));
            }
            ServerManager.stopServer(port);

            String response = "Server on port " + port + " will stop";
            he.sendResponseHeaders(200, response.length());
            try (OutputStream os = he.getResponseBody()) {
                os.write(response.getBytes());
            }
        } else {
            String response = "control/?";
            he.sendResponseHeaders(404, response.length());
            try (OutputStream os = he.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
}
