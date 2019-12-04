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
package expectations;

import common.Util;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author 802996013
 */
public class MultiStringMatch {

    private final List<StringMatcher> list = new ArrayList<>();
    private final char delim;
    
    public MultiStringMatch(String in, char delim) {
        this.delim = delim;
        if (in == null) {
            return;
        }
        List<String> strings = Util.split(in, delim);
        for (String st : strings) {
            list.add(new StringMatcher(st));
        }
    }

    boolean match(Object with) {
        if (with == null) {
            return false;
        }
        if (list.isEmpty()) {
            return true;
        }
        List<String> strings = Util.split(with.toString(), delim);
        if (strings.size()!=list.size()) {
            return false;
        }       
        for (int i=0; i< strings.size();i++) {
            if (!list.get(i).match(strings.get(i))) {
                return false;
            }
        }
        return true;
    }

}
