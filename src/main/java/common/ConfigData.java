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
package common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import config.Config;
import java.beans.BeanProperty;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import json.JsonUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import server.ServerConfig;

public class ConfigData extends Config implements PropertyDataWithAnnotations {

    private Map<String, ServerConfig> servers = new HashMap<>();
    private String packagedRequestsFile;
    private String selectedPackagedRequestName;
    private String timeFormat;
    private Integer defaultPort;
    private boolean includeHeaders = true;
    private boolean includeBody = true;
    private double x;
    private double y;
    private double width;
    private double height;

    @JsonIgnore
    private DateTimeFormatter ts;

    private static FileData fileData;
    private static ConfigData instance;


    public static ConfigData getInstance() {
        if (instance == null) {
            instance = new ConfigData();
            instance.setWidth(600);
            instance.setHeight(600);
            instance.setX(0);
            instance.setY(0);
            instance.setDefaultPort(0);
        }
        return instance;
    }

    public Map<String, ServerConfig> getServers() {
        return servers;
    }


    public void setServers(Map<String, ServerConfig> servers) {
        this.servers = servers;
    }

    @BeanProperty(description = "Packaged Requests File | validation=pak,type=file,desc=Json Expectation File,ext=json")
    public String getPackagedRequestsFile() {
        return packagedRequestsFile;
    }

    public void setPackagedRequestsFile(String packagedRequestsFile) {
        this.packagedRequestsFile = packagedRequestsFile;
    }

    @BeanProperty(description = "Packaged Requests Name")
    public String getSelectedPackagedRequestName() {
        return selectedPackagedRequestName;
    }

    public void setSelectedPackagedRequestName(String selectedPackagedRequestName) {
        this.selectedPackagedRequestName = selectedPackagedRequestName;
    }

    @BeanProperty(description = "Default Port | validation=defport")
    public Integer getDefaultPort() {
        return defaultPort;
    }

    public void setDefaultPort(Integer defaultPort) {
        this.defaultPort = defaultPort;
    }

     public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    @BeanProperty(description = "Include header data in logs")
    public boolean isIncludeHeaders() {
        return includeHeaders;
    }

    public void setIncludeHeaders(boolean includeHeaders) {
        this.includeHeaders = includeHeaders;
    }

    @BeanProperty(description = "Include http body data in logs")
    public boolean isIncludeBody() {
        return includeBody;
    }

    public void setIncludeBody(boolean includeBody) {
        this.includeBody = includeBody;
    }

    @BeanProperty(description = "Log Date Time format (eg HH:mm:ss.SSS)")
    public String getTimeFormat() {
        if (timeFormat == null) {
            return "HH:mm:ss.SSS";
        }
        return timeFormat;
    }

    public void setTimeFormat(String timeFormat) {
        this.timeFormat = timeFormat;
    }

    public String timeStamp(long time) {
        DateTime dt = new DateTime(time);
        if (ts == null) {
            ts = DateTimeFormat.forPattern(getTimeFormat());
        }
        return dt.toString(ts);
    }

    public ServerConfig serverWithDefaultConfig() {
        ServerConfig sc = new ServerConfig();
        ServerConfig scClone = serversList()[0];
        sc.setExpectationsFile(scClone.getExpectationsFile());
        sc.setTimeToClose(scClone.getTimeToClose());
        sc.setAutoStart(false);
        sc.setLogProperties(true);
        sc.setShowPort(true);
        sc.setVerbose(true);
        return sc;
    }

    public ServerConfig[] serversList() {
        ServerConfig[] list = new ServerConfig[serverCount()];
        int i = 0;
        for (ServerConfig sc:getServers().values()) {
            list[i] = sc;
            i++;
        }
        return list;
    }

    public static void store() throws IOException {
        File f = new File(fileData.getFileName());
        String s = JsonUtils.toJsonFormatted(ConfigData.instance);
        Files.write(f.toPath(), s.getBytes(StandardCharsets.UTF_8));
    }

    public static void load(String fileName) {
        fileData = Util.readFile(fileName);
        instance = (ConfigData) Config.configFromJson(ConfigData.class, fileData.getContent());
        if (!instance.getServers().containsKey(instance.getDefaultPort().toString())) {
            for (String port:instance.getServers().keySet()) {
                instance.setDefaultPort(Integer.parseInt(port));
                break;
            }
        }
    }

    public int serverCount() {
        return servers.size();
    }

}
