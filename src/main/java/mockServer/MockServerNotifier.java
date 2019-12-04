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

import common.Action;
import common.Notification;
import common.Notifier;
import org.joda.time.DateTime;

/**
 *
 * @author stuart
 */
public class MockServerNotifier implements Notifier {

    @Override
    public void notifyAction(Notification notification) {
        if (!notification.getAction().equals(Action.SERVER_STATE)) {
          System.out.println(getTimeStamp(notification.getTime()) + "Port:" + notification.getPort() + " <MOCK> " + notification.getAction().name() + " " + notification.getMessage());
        }
    }

    @Override
    public void log(int port, String message) {
        if ((message != null) && (message.trim().length() > 0)) {
            System.out.println(getTimeStamp(System.currentTimeMillis()) + "Port:" + port + " <MOCK> " + message);
        }
    }

    @Override
    public void log(int port, Throwable throwable) {
        if (throwable != null) {
            System.out.println(getTimeStamp(System.currentTimeMillis()) + "Port:" + port + " <MOCK> ERROR:" + throwable.getMessage());
        }
    }

    @Override
    public void log(int port, String message, Throwable throwable) {
        if (throwable != null) {
            System.out.println(getTimeStamp(System.currentTimeMillis()) + "Port:" + port + " <MOCK> ERROR:" + message + ": " + throwable.getMessage());
        }
    }

    public String getTimeStamp(long time) {
        return (new DateTime(time)).toString("HH:mm:ss.SSS: ");
    }
}
