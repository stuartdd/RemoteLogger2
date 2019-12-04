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
package mockServer;

import common.Util;
import expectations.ExpChain;
import expectations.Expectation;
import expectations.Expectations;
import json.JsonUtils;
import server.Server;
import server.ServerConfig;
import server.ServerStatistics;
import server.ServerCallbackHandler;

/**
 *
 * @author stuart
 */
public class MockServer {

    private Server server;

    public MockServer(int port, ServerCallbackHandler responseHandler, String expectationFile, boolean verbose) {
        server = new Server(port, new ServerConfig(Expectations.fromFile(expectationFile), 0, verbose, verbose), responseHandler, new MockServerNotifier());
    }

    public MockServer(int port, ServerCallbackHandler responseHandler, Expectations expectations, boolean verbose) {
        server = new Server(port, new ServerConfig(expectations, 0, verbose, verbose), responseHandler, new MockServerNotifier());
    }

    public MockServer(int port, ServerCallbackHandler responseHandler, boolean verbose) {
        server = new Server(port, new ServerConfig(new Expectations(), 0, verbose, verbose), responseHandler, new MockServerNotifier());
    }

    public void setCallBackClass(ServerCallbackHandler responseHandler) {
        if (server != null) {
            server.setCallBackHandler(responseHandler);
        }
    }

    public boolean isRunning() {
        if (server != null) {
            return server.isRunning();
        }
        return false;
    }

    public MockServer start() {
        if (server != null) {
            server.start();
        }
        int count = 0;
        while (!server.isRunning() && (count < 10000)) {
            Util.sleep(2);
            count++;
        }
        Util.sleep(2);
        if (!server.isRunning()) {
            throw new MockServerTimeoutException("Timeout waiting for server to start");
        }
        return this;
    }

    public void stop() {
        if (server != null) {
            server.stop();
        }
        int count = 0;
        while (server.isRunning() && (count < 10000)) {
            Util.sleep(2);
            count++;
        }
        Util.sleep(2);
        if (server.isRunning()) {
            throw new MockServerTimeoutException("Timeout waiting for server to stop");
        }
    }

    public ServerStatistics getServerStatistics() {
        return server.getServerStatistics();
    }
         
    public static MockServerBuilder add(ExpChain exp) {
        return new MockServerBuilder().add(exp);
    }
    
    public static MockServerBuilder addAll(String json) {
        Expectations list = (Expectations) JsonUtils.beanFromJson(Expectations.class, json);
        return new MockServerBuilder(list);
    }
    
    public static MockServerBuilder addAll(Expectations expectations) {
        return new MockServerBuilder(expectations);
    }
    
    public static MockServerBuilder add(Expectation expectation) {
        return new MockServerBuilder(expectation);
    }

    public static MockServerBuilder add(String json) {
        Expectation exp = (Expectation) JsonUtils.beanFromJson(Expectation.class, json);
        return new MockServerBuilder(exp);
    }
    
    public static MockServerBuilder fromfile(String jsonFile) {
        Expectations exp = Expectations.fromFile(jsonFile);
        return new MockServerBuilder(exp);
    }

}
