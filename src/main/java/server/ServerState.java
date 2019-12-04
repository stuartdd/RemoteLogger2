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

/**
 *
 * @author stuart
 */
public enum ServerState {
    SERVER_PENDING("Server is waiting to start"),
    SERVER_STARTING("Server is Starting"),
    SERVER_RUNNING("Server is Running"),
    SERVER_STOPPING("Server is Stopping"),
    SERVER_STOPPED("Server is Stopped"),
    SERVER_FAIL("Server error");
    private String info;

    private ServerState(String info) {
        this.info = info;
    }
    public String getInfo() {
        return info;
    }

    @Override
    public String toString() {
        return name() + ":"+info;
    }
    
}
