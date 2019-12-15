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
import common.CommonLogger;
import common.Util;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author 802996013
 */
public class Client {

    public enum Method {
        GET, PUT, POST
    }

    private static final String NL = System.getProperty("line.separator");

    private final ClientConfig config;
    private final CommonLogger logger;

    public Client(ClientConfig config, CommonLogger logger) {
        this.logger = logger;
        this.config = config;
    }

    public ClientResponse send(String path, String body, String methodName) {
        if (methodName == null) {
            return send(path, body, Method.GET);
        } else {
            Method method = Method.valueOf(methodName);
            if (method == null) {
                return send(path, body, Method.GET);
            }
            return send(path, body, method);
        }
    }

    public ClientResponse send(String path, String body, Method methodIn) {
        Method method;
        if (methodIn == null) {
            method = Method.GET;
        } else {
            method = methodIn;
        }
        String fullHost;
        if (path != null) {
            if (path.startsWith("/")) {
                fullHost = config.getHost()
                        + (config.getPort() == null ? "" : ":" + config.getPort())
                        + path;
            } else {
                fullHost = config.getHost()
                        + (config.getPort() == null ? "" : ":" + config.getPort())
                        + "/" + path;
            }
        } else {
            fullHost = config.getHost() + (config.getPort() == null ? "" : ":" + config.getPort());
        }

        URL obj;
        DataOutputStream wr = null;
        BufferedReader in = null;
        HttpURLConnection con;
        try {
            obj = new URL(fullHost);
            con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod(method.toString());
            for (Map.Entry<String, String> s : config.getHeaders().entrySet()) {
                con.setRequestProperty(s.getKey(), s.getValue());
            }
            if ((body == null) || (body.trim().length() == 0)) {
                con.setDoOutput(false);
            } else {
                con.setDoOutput(true);
                wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(body);
                wr.flush();
                wr.close();
                wr = null;
                if (logger != null) {
                    logger.log(new LogLine(-1, "REQUEST: \nBODY START ----------\n" + body + "\nBODY END   ----------"));
                }
            }
            if (logger != null) {
                logger.log(new LogLine(-1, "REQUEST: " + method.toString() + " TO:" + fullHost));
            }
            int responseCode;
            int connectionFailes = 0;
            while (true) {
                try {
                    responseCode = con.getResponseCode();
                    break;
                } catch (ConnectException ex) {
                    connectionFailes++;
                    if (logger != null) {
                        logger.log(new LogLine(-1, "CLIENT: Connection Failed [" + connectionFailes + "] " + fullHost));
                    }
                    if (connectionFailes > 5) {
                        throw ex;
                    }
                    Util.sleep(500);
                }
            }

            InputStream is = null;
            try {
                is = con.getInputStream();
            } catch (IOException fnfe) {
                is = con.getErrorStream();
                if (is == null) {
                    if (logger != null) {
                        logger.log(new LogLine(-1, "CLIENT RESP: [" + responseCode + "]: No Response"));
                    }
                    return new ClientResponse(responseCode, "");
                }
            }
            in = new BufferedReader(new InputStreamReader(is));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
                response.append(NL);
            }
            in.close();
            in = null;
            Map<String, String> headers = new HashMap<>();
            for (Map.Entry<String, List<String>> s : con.getHeaderFields().entrySet()) {
                if (s.getKey() == null) {
                    headers.put(listToString(s.getValue(), ';'), "");
                } else {
                    headers.put(s.getKey(), listToString(s.getValue(), ';'));
                }
            }
            if (logger != null) {
                logger.log(new LogLine(-1, "CLIENT RESP:  [" + responseCode + "]:" + response.toString().trim()));
            }
            return new ClientResponse(responseCode, response.toString().trim(), headers);
        } catch (ClientException | IOException e) {
            if (logger != null) {
                logger.log(new LogLine(-1, "CLIENT: Failed to send to:" + fullHost, e));
            }
            throw new ClientException("CLIENT: Failed to send to:" + fullHost, e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (wr != null) {
                    wr.close();
                }
            } catch (Throwable e) {
            }
        }
    }

    private String listToString(List<String> list, char delim) {
        StringBuilder sb = new StringBuilder();
        int mark = 0;
        for (String s : list) {
            sb.append(s);
            mark = sb.length();
            sb.append(delim);
        }
        sb.setLength(mark);
        return sb.toString();
    }
}
