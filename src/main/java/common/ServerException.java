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

public class ServerException extends RuntimeException {

    private int status = 200;

    public ServerException(String message, int status) {
        super(message);
        this.status = status;
    }

    public ServerException(String message, int status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public int getStatus() {
        if ((getCause() != null) && (getCause() instanceof ServerException)) {
            return ((ServerException) getCause()).getStatus();
        }
        return status;
    }

    @Override
    public String getMessage() {
        return "Status=" + getStatus() + "] " + super.getMessage(); 
    }

}
