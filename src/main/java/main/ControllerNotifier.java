/*
 * Copyright (C) 2019 Stuart Davies
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
 *
 */
package main;

import common.LogLine;
import common.Notification;
import common.Notifier;
import java.util.LinkedList;
import java.util.Queue;

/**
 *
 * @author Stuart
 */
public class ControllerNotifier implements Notifier {

    private FXMLDocumentController controller;
    private final Queue<Object> queue = new LinkedList<>();

    public void setController(FXMLDocumentController controller) {
        this.controller = controller;
    }

    @Override
    public void notifyAction(Notification notification) {
        sendToController(notification);
    }

    @Override
    public void log(LogLine logLine) {
        sendToController(logLine);
    }

    private synchronized void sendToController(Object obj) {
        queue.add(obj);
        if (controller != null) {
            while (!queue.isEmpty()) {
                Object o = queue.poll();
                if (o instanceof LogLine) {
                    controller.log((LogLine) o);
                } else if (o instanceof Notification) {
                    controller.notifyAction((Notification) o);
                }
            }
        }
    }
}
