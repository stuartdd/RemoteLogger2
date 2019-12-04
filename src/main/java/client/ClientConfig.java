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
package client;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author 802996013
 */
public class ClientConfig {

    private static final String USER_AGENT = "Mozilla/5.0";
    private static final String HOST = "http://localhost";
    private static final String ACCEPT_LANGUAGE = "en-US,en;q=0.5";

    private String host = HOST;
    private String userAgent = USER_AGENT;
    private String acceptLang = ACCEPT_LANGUAGE;
    private Integer port = null;
    private Map<String, String> headers;

    public ClientConfig(int port) {
        this.port = port;
    }

    public ClientConfig(String host) {
        this.host = host;
        this.headers = null;
    }

    public ClientConfig(String host, Integer port) {
        this.host = host;
        this.port = port;
        this.headers = null;
    }

    public ClientConfig(String host, Integer port, Map<String, String> headers) {
        this.host = host;
        this.port = port;
        this.headers = headers;
    }
    
    public ClientConfig(String host, Map<String, String> headers) {
        this.host = host;
        this.port = null;
        this.headers = headers;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getAcceptLang() {
        return acceptLang;
    }

    public void setAcceptLang(String acceptLang) {
        this.acceptLang = acceptLang;
    }

    public Map<String, String> getHeaders() {
        if (headers == null) {
            Map<String, String> tempHeaders = new HashMap<>();
            tempHeaders.put("User-Agent", getUserAgent());
            tempHeaders.put("Accept-Language", getAcceptLang());
            return tempHeaders;
        }
        return headers;
    }

    @Override
    public String toString() {
        return "ClientConfig{" + "host=" + host + ", port=" + port + '}';
    }
    
    
}
