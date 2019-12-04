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

import com.sun.net.httpserver.HttpExchange;
import common.ServerException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import template.Template;

/**
 *
 * @author stuar
 */
public class MockResponse {

    private final String responseBody;
    private final int status;
    private final Map<String, String> headers;

    public MockResponse(String responseBody, int status, Map<String, String> headers) {
        this.responseBody = (responseBody == null ? "" : responseBody);
        this.status = status;
        this.headers = (headers == null ? new HashMap<>() : headers);
    }

    public void respond(HttpExchange he, Map<String, Object> map) {
        respond(he, status, responseBody, headers, map);
    }

    public static void respond(HttpExchange he, int status, String response, Map<String, String> headers, Map<String, Object> map) {
        if (headers != null) {
            for (Map.Entry<String, String> s : headers.entrySet()) {
                he.getResponseHeaders().add(s.getKey(), Template.parse(s.getValue(), map, true));
            }
        }
        String responseTemplated;
        if (response == null) {
            responseTemplated = "";
        } else {
            if (map != null) {
                responseTemplated = Template.parse(response, map, true);
            } else {
                responseTemplated = response;
            }
        }
        OutputStream os = he.getResponseBody();
        try {
            he.sendResponseHeaders(status, responseTemplated.length());
            os.write(responseTemplated.getBytes());
            os.flush();
        } catch (IOException ex) {
            throw new ServerException("Failed to write '" + responseTemplated + "' to output stream ", status, ex);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException ex) {
                    throw new ServerException("Failed to close output stream ", status, ex);
                }
            }
        }
    }

    public int getStatus() {
        return status;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public static MockResponse notFound() {
        return new MockResponse("Not Found", 404, new HashMap<>());
    }

    public static MockResponse undefined(String method) {
        if (method.equalsIgnoreCase("GET")) {
            return new MockResponse("Response is undefined", 200, new HashMap<>());
        } else {
            return new MockResponse("Response is undefined", 201, new HashMap<>());
        }
    }
}
