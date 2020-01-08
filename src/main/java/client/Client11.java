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

import common.CommonLogger;
import common.LogLine;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;


/**
 * @author 802996013
 */
public class Client11 {

    private Client11() {
    }

    public static ClientResponse send(String id, String host, Integer port, String path, String methodName, String body, Map<String, String> headers, int timeoutSeconds, CommonLogger logger) {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        HttpRequest.Builder builder;
        if (methodName.equalsIgnoreCase("post")) {
            builder = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(body == null ? "" : body));
        } else if (methodName.equalsIgnoreCase("put")) {
            builder = HttpRequest.newBuilder().PUT(HttpRequest.BodyPublishers.ofString(body == null ? "" : body));
        } else if (methodName.equalsIgnoreCase("delete")) {
            builder = HttpRequest.newBuilder().DELETE();
        } else {
            builder = HttpRequest.newBuilder().GET();
        }
        for (Map.Entry<String, String> es : headers.entrySet()) {
            builder.header(es.getKey(), es.getValue());
        }


        try {
            HttpRequest request = builder
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create((host == null ? "" : host + (port == null ? "" : ":" + port.toString())) + (path == null ? "" : "/" + path)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return new ClientResponse(response.statusCode(), response.body(), response.headers());
        } catch (Exception ex) {
            if (logger != null) {
                logger.log(new LogLine((port == null ? -1 : port), "Request [ "+id+" ] failed with" , ex));
            } else {
                ex.printStackTrace();
            }
        }
        return null;
    }
}
