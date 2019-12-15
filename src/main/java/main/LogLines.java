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

import common.Action;
import common.LogLine;
import common.Loggable;
import common.CommonLogger;
import common.Notification;
import main.dialogs.Dialogs;

/**
 *
 * @author Stuart
 */
public class LogLines implements CommonLogger {

    private Loggable head;
    private Loggable tail;
    private int count;
    private int max;
    private int lineNo = 1;
    private UiNotifier notifier;

    public LogLines(int max) {
        this.max = max;
        head = new LogLine(-1, "Start");
        tail = head;
        count = 1;
        lineNo = 1;
    }

    public synchronized void log(Loggable l) {
        l.setLineNo(lineNo);
        tail.setNext(l);
        tail = l;
        count++;
        lineNo++;
        if (count > max) {
            if (head.getNext() != null) {
                head = head.getNext();
                count--;
            }
        }
        if (notifier != null) {
            notifier.notifyAction(new Notification(l.getPort(), Action.UPDATE_LOG, "LOG"));
        }
    }

    public String get(int port) {
        StringBuilder sb = new StringBuilder();
        Loggable ll = head;
        while (ll != null) {
            if ((port < 0) || (ll.getPort() == port) || (ll.getPort() < 0)) {
                sb.append(ll.toString()).append("\n");
            }
            ll = ll.getNext();
        }
        return sb.toString();
    }

    public UiNotifier getNotifier() {
        return notifier;
    }

    public void setNotifier(UiNotifier notifier) {
        this.notifier = notifier;
    }

    public void clear() {
        head = new LogLine(-1, "Clear");
        tail = head;
        count = 1;
        lineNo = 1;
        if (notifier != null) {
            notifier.notifyAction(new Notification(-1, Action.UPDATE_LOG, "LOG"));
        }
    }
}
