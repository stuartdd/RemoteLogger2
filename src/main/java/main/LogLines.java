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
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Stuart
 */
public class LogLines {

    private static List<LogLine> logLines = new ArrayList<>();

    public static void add(LogLine l) {
        logLines.add(l);
    }

    public static List<LogLine> get(int port) {
        List<LogLine> temp = new ArrayList<>();
        for (LogLine l : logLines) {
            if (l.getPort() == port) {
                temp.add(l);
            }
        }
        return temp;
    }

}
