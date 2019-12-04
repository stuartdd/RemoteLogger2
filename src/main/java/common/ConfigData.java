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
import common.FileData;
import common.Util;
import config.Config;
import config.Config;
import java.util.HashMap;
import java.util.Map;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import server.ServerConfig;

public class ConfigData extends Config {

    private Map<String, ServerConfig> servers = new HashMap<>();
    private String packagedRequestsFile;
    private String selectedPackagedRequestName;
    private String logDateFormat;
    private String timeFormat;
    private int defaultPort;
    private boolean includeHeaders = true;
    private boolean includeBody = true;
    private boolean includeEmpty = false;
    private boolean showTime = true;
    private boolean showPort = true;
    private double x;
    private double y;
    private double width;
    private double height;
    private double[] expDividerPos;
    private double[] packDividerPos;

    @JsonIgnore
    private DateTimeFormatter ts;

    private static String writeFileName;
    private static String readFileName;
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

    public static boolean isLoadedFromFile() {
        return (readFileName != null);
    }

    public static boolean canWriteToFile() {
        return (writeFileName != null);
    }

    public static void load(String fileName) {
        FileData fd = Util.readFile(fileName);
        if (fd.isReadFromFile()) {
            writeFileName = fd.getFileName();
        }
        readFileName = fd.getFileName();
        instance = (ConfigData) Config.configFromJson(ConfigData.class, fd.getContent());
    }

    public Map<String, ServerConfig> getServers() {
        return servers;
    }

    public void setServers(Map<String, ServerConfig> servers) {
        this.servers = servers;
    }

    public String getPackagedRequestsFile() {
        return packagedRequestsFile;
    }

    public void setPackagedRequestsFile(String packagedRequestsFile) {
        this.packagedRequestsFile = packagedRequestsFile;
    }

    public String getSelectedPackagedRequestName() {
        return selectedPackagedRequestName;
    }

    public void setSelectedPackagedRequestName(String selectedPackagedRequestName) {
        this.selectedPackagedRequestName = selectedPackagedRequestName;
    }

    public String timeStamp(long time) {
        DateTime dt = new DateTime(time);
        if (ts == null) {
            ts = DateTimeFormat.forPattern(getTimeFormat());
        }
        return dt.toString(ts);
    }

    public String getLogDateFormat() {
        return logDateFormat;
    }

    public void setLogDateFormat(String logDateFormat) {
        this.logDateFormat = logDateFormat;
    }

    public int getDefaultPort() {
        return defaultPort;
    }

    public void setDefaultPort(int defaultPort) {
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

    public double[] getExpDividerPos() {
        return expDividerPos;
    }

    public void setExpDividerPos(double[] expDividerPos) {
        this.expDividerPos = expDividerPos;
    }

    public double[] getPackDividerPos() {
        return packDividerPos;
    }

    public void setPackDividerPos(double[] packDividerPos) {
        this.packDividerPos = packDividerPos;
    }

    public boolean isIncludeHeaders() {
        return includeHeaders;
    }

    public void setIncludeHeaders(boolean includeHeaders) {
        this.includeHeaders = includeHeaders;
    }

    public boolean isIncludeBody() {
        return includeBody;
    }

    public void setIncludeBody(boolean includeBody) {
        this.includeBody = includeBody;
    }

    public boolean isIncludeEmpty() {
        return includeEmpty;
    }

    public void setIncludeEmpty(boolean includeEmpty) {
        this.includeEmpty = includeEmpty;
    }

    public String getTimeFormat() {
        if (timeFormat == null) {
            return "HH:mm:ss.SSS";
        }
        return timeFormat;
    }

    public void setTimeFormat(String timeFormat) {
        this.timeFormat = timeFormat;
    }

    public boolean isShowTime() {
        return showTime;
    }

    public void setShowTime(boolean showTime) {
        this.showTime = showTime;
    }

    public boolean isShowPort() {
        return showPort;
    }

    public void setShowPort(boolean showPort) {
        this.showPort = showPort;
    }

    public static String writeFileName() {
        return writeFileName;
    }

    public static String readFileName() {
        return readFileName;
    }


}
