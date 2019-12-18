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

import com.fasterxml.jackson.annotation.JsonIgnore;
import common.PropertyDataWithAnnotations;
import expectations.Expectations;
import java.beans.BeanProperty;

public class ServerConfig implements PropertyDataWithAnnotations {

    private String expectationsFile;
    private boolean autoStart = false;
    private boolean showPort = false;
    private int timeToClose = 1;
    private boolean verbose = true;
    private boolean logProperties = false;

    @JsonIgnore
    private Expectations expectations;


    public ServerConfig(Expectations expectations, int timeToClose, boolean verbose, boolean logProperties) {
        this.expectationsFile = null;
        this.expectations = expectations;
        this.timeToClose = timeToClose;
        this.verbose = verbose;
        this.logProperties = logProperties;
    }

    public ServerConfig() {
    }

    @BeanProperty(description = "Expectation File | validation=exp,type=file,desc=Json Expectation File,ext=json")
    public String getExpectationsFile() {
        return expectationsFile;
    }

    public void setExpectationsFile(String expectationsFile) {
        this.expectationsFile = expectationsFile;
    }

    @BeanProperty(description = "Start on load")
    public boolean isAutoStart() {
        return autoStart;
    }

    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }

    @BeanProperty(description = "Show Port in logs")
    public boolean isShowPort() {
        return showPort;
    }

    public void setShowPort(boolean showPort) {
        this.showPort = showPort;
    }

    @BeanProperty(description = "Time to close server (Seconds) | min=1,max=10")
    public int getTimeToClose() {
        return timeToClose;
    }

    public void setTimeToClose(int timeToClose) {
        this.timeToClose = timeToClose;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public Expectations expectations() {
        return expectations;
    }

    @BeanProperty(description = "Write properties to the log")
    public boolean isLogProperties() {
        return logProperties;
    }

    public void setLogProperties(boolean logProperties) {
        this.logProperties = logProperties;
    }
    
    
    
}
