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
import common.Action;
import common.BodyType;
import common.CommonLogger;
import common.Notification;
import common.Notifier;
import common.Util;
import expectations.Expectation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import mockServer.MockRequest;
import mockServer.MockResponse;

/**
 *
 * @author stuart
 */
public class ServerExpectationHandler implements HttpHandler {

    private final int port;
    private final Server server;
    private ServerCallbackHandler responseHandler;
    private final Notifier serverNotifier;
    private final boolean verbose;

    public ServerExpectationHandler(Server server) {
        this.server = server;
        this.verbose = server.getServerConfig().isVerbose();
        this.port = server.getPort();
        this.responseHandler = server.getCallbackHandler();
        this.serverNotifier = server.getServerNotifier();
    }

    public void setCallBackClass(ServerCallbackHandler responseHandler) {
        this.responseHandler = responseHandler;
    }

    @Override
    public void handle(HttpExchange he) {
        ServerExpectations serverExpectations = server.getServerExpectations();
        this.server.getServerStatistics().inc(ServerStatistics.STAT.REQUEST, true);
        Map<String, Object> map = new TreeMap<>();
        Map<String, String> headers = new HashMap<>();
        Map<String, String> queries = new HashMap<>();
        String body = Util.readStream(he.getRequestBody());
        if (body != null) {
            String bodyTrim = body.trim();
            if (serverNotifier != null) {
                serverNotifier.notifyAction(new Notification(port, Action.LOG_BODY, null, bodyTrim));
            }
            map.put("BODY", bodyTrim);
            map.put("BODY-TYPE", Util.detirmineBodyType(bodyTrim));
            Util.loadPropertiesFromBody(map, bodyTrim);
        } else {
            map.put("BODY-TYPE", BodyType.EMPTY);
        }
        if ((serverNotifier != null) && (verbose)) {
            serverNotifier.notifyAction(new Notification(port, Action.LOG_HEADER, null, "METHOD:" + he.getRequestMethod()));
        }
        map.put("METHOD", he.getRequestMethod());
        String path = Util.trimmedNull(he.getRequestURI().getPath());
        if (path != null) {
            if ((serverNotifier != null) && (verbose)) {
                serverNotifier.notifyAction(new Notification(port, Action.LOG_HEADER, null, "PATH:" + he.getRequestURI().getPath()));
            }
            map.put("PATH", he.getRequestURI().getPath());
            splitIntoMap(map, null, "PATH", '/');
        }
        String query = Util.trimmedNull(he.getRequestURI().getQuery());
        if (query != null) {
            if ((serverNotifier != null) && (verbose)) {
                serverNotifier.notifyAction(new Notification(port, Action.LOG_HEADER, null, "QUERY:" + he.getRequestURI().getQuery()));
            }
            map.put("QUERY", he.getRequestURI().getQuery());
            splitIntoMap(map, queries, "QUERY", '&');
        }

        for (Iterator<String> it = he.getRequestHeaders().keySet().iterator(); it.hasNext();) {
            String head = it.next();
            String value = Util.asString(he.getRequestHeaders().get(head));
            map.put("HEAD." + head, value);
            headers.put(head, value);
            if ((serverNotifier != null) && (verbose)) {
                serverNotifier.notifyAction(new Notification(port, Action.LOG_HEADER, null, "HEADER: " + head + "=" + value));
            }
        }

        Expectation foundExpectation = serverExpectations.findMatchingExpectation(port, map);

        if (responseHandler != null) {
            MockRequest mockRequest = null;
            if (serverExpectations.hasNoExpectations()) {
                mockRequest = new MockRequest(port, map, headers, queries, serverExpectations, null);
            } else {
                if (foundExpectation != null) {
                    mockRequest = new MockRequest(port, map, headers, queries, serverExpectations, foundExpectation);
                }
            }
            if (mockRequest != null) {
                MockResponse mockResponse = responseHandler.handle(mockRequest, map);
                if (mockResponse != null) {
                    mockResponse.respond(he, map);
                    this.server.getServerStatistics().inc(ServerStatistics.STAT.RESPONSE, true);
                    return;
                }
            }
        }
        if (serverExpectations.hasNoExpectations()) {
            MockResponse.respond(he, 404, "No Expectation defined", null, null);
        } else {
            serverExpectations.getResponse(port, he, map, headers, queries, foundExpectation);
        }
        this.server.getServerStatistics().inc(ServerStatistics.STAT.RESPONSE, true);
    }

    private void splitIntoMap(Map<String, Object> map, Map<String, String> queries, String name, char delim) {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        Object s = map.get(name);
        if (s == null) {
            return;
        }
        for (char c : s.toString().trim().toCharArray()) {
            if (c == delim) {
                count = addValueToMap(map, queries, sb, name, count);
            } else {
                sb.append(c);
            }
        }
        addValueToMap(map, queries, sb, name, count++);
    }

    private int addValueToMap(Map<String, Object> map, Map<String, String> queries, StringBuilder sb, String name, int count) {
        if (sb.length() == 0) {
            return count;
        }
        String part = sb.toString().trim();
        int pos = part.indexOf('=');
        if (pos > 0) {
            map.put(name + "." + part.substring(0, pos), part.substring(pos + 1));
            if (queries != null) {
                queries.put(part.substring(0, pos), part.substring(pos + 1));
            }
        } else {
            map.put(name + "[" + count++ + "]", part);
        }
        sb.setLength(0);
        return count;
    }

    ServerCallbackHandler getCallBackClass() {
        return responseHandler;
    }

}
