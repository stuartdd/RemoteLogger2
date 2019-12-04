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
public class ClientResponse {

    private int status;
    private String body;
    private Map<String, String> headers = new HashMap<>();

    public ClientResponse(int status, String body) {
        this.status = status;
        this.body = body;
    }

    public ClientResponse(int status, String body, Map<String, String> headers) {
        this.status = status;
        this.body = body;
        this.headers = headers;
    }

    public int getStatus() {
        return status;
    }

    public String getBody() {
        return body;
    }

    public String getHeader(String key) {
        return headers.get(key);
    }

    public String getHeaderIgnoreCase(String key) {
        for (Map.Entry<String, String> es : headers.entrySet()) {
            if (es.getKey().equalsIgnoreCase(key)) {
                return es.getValue();
            }
        }
        return null;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public String toString() {
        return "ClientResponse{" + "status=" + status + ", body=" + body + '}';
    }

}
