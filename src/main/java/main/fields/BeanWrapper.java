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
package main.fields;

import java.beans.BeanProperty;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Stuart
 */
public class BeanWrapper {

    private final Object object;
    private final Map<String, Class> properties = new HashMap<>();

    public BeanWrapper(Object object) {
        this.object = object;
        for (Method m1 : object.getClass().getMethods()) {
            String pName = null;
            String mName = m1.getName();
            if (mName.startsWith("get")) {
                pName = mName.substring(3);
            } else if (mName.startsWith("is")) {
                pName = mName.substring(2);
            } 
            if (pName != null) {
                for (Method m2 : object.getClass().getMethods()) {
                    if (m2.getName().equals("set" + pName)) {
                        if (m2.getParameterTypes().length == 1) {
                            properties.put(pName, m2.getParameterTypes()[0]);
                            break;
                        }
                    }
                }
            }
        }
    }

    public List<String> getPropertyList() {
        List<String> l = new ArrayList<>();
        for (String s : properties.keySet()) {
            l.add(s);
        }
        return l;
    }

    public Class getParameterType(String name) {
        return properties.get(name);
    }
    
    public String getDescription(String name) {
        Method m = findGetter(name);
        BeanProperty bp = m.getAnnotation(BeanProperty.class);
        if (bp == null) {
            return name;
        }
        return bp.description();
    }   

    public Object getValue(String name) {
        try {
            Method m = findGetter(name);
            return m.invoke(object, new Object[]{});
        } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(BeanWrapper.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private Method findGetter(String name) {
        for (Method m:object.getClass().getMethods()) {
            if (m.getName().equals("is"+name)) {
                return m;
            }
            if (m.getName().equals("get"+name)) {
                return m;
            }
        }
        return null;
    }
    
    public void setValue(Object o) {
    }
}
