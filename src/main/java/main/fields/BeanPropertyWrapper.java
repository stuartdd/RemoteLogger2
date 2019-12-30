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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Stuart
 */
public class BeanPropertyWrapper {

    private final Object object;
    private final Map<String, BeanProperty> properties = new HashMap<>();

    public BeanPropertyWrapper(Object object) {
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
                            String desc = getDescription(pName);
                            if (desc != null) {
                                properties.put(pName, new BeanProperty(pName, getDataViaGetter(pName), desc, m2.getParameterTypes()[0]));
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    int countUpdates() {
        int count = 0;
        for (Map.Entry<String, BeanProperty> es : properties.entrySet()) {
            if (es.getValue().isUpdated()) {
                count++;
            }
        }
        return count;
    }

    public int updateAllValues(Map<String, Object> configChanges, String id) {
        int count = 0;
        for (Map.Entry<String, BeanProperty> es : properties.entrySet()) {
            if (es.getValue().isUpdated()) {
                setDataViaSetter(es.getKey(), es.getValue().getUpdatedValue());
                configChanges.put(id + "[" + es.getKey() + "]", es.getValue().getDescription() + " --> " + es.getValue().getUpdatedValue());
                count++;
            }
        }
        return count;
    }

    public boolean hasErrors() {
        for (BeanProperty bp : properties.values()) {
            if (bp.isError()) {
                return true;
            }
        }
        return false;
    }

    public List<String> getPropertyNameList() {
        List<String> l = new ArrayList<>();
        for (String s : properties.keySet()) {
            l.add(s);
        }
        return l;
    }

    public BeanProperty getBeanProperty(String name) {
        return properties.get(name);
    }

    public Object getInitialValue(String name) {
        return getBeanProperty(name).getInitialValue();
    }

    public void setUpdatedValue(String name, Object newValue) {
        getBeanProperty(name).setUpdatedValue(newValue);
    }

    private String getDescription(String name) {
        Method m = findGetter(name);
        java.beans.BeanProperty bp = m.getAnnotation(java.beans.BeanProperty.class);
        if (bp == null) {
            m = findSetter(name);
            bp = m.getAnnotation(java.beans.BeanProperty.class);
            if (bp == null) {
                return null;
            }
        }
        return bp.description();
    }

    private void setDataViaSetter(String name, Object o) {
        try {
            Method m = findSetter(name);
            m.invoke(object, new Object[]{o});
        } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(BeanPropertyWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private Object getDataViaGetter(String name) {
        try {
            Method m = findGetter(name);
            return m.invoke(object, new Object[]{});
        } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(BeanPropertyWrapper.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private Method findGetter(String name) {
        for (Method m : object.getClass().getMethods()) {
            if (m.getName().equals("is" + name)) {
                return m;
            }
            if (m.getName().equals("get" + name)) {
                return m;
            }
        }
        return null;
    }

    private Method findSetter(String name) {
        for (Method m : object.getClass().getMethods()) {
            if (m.getName().equals("set" + name)) {
                return m;
            }
        }
        return null;
    }

}
