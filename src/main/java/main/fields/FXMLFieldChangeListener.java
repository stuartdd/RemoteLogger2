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

/**
 *
 * @author Stuart
 */
public interface FXMLFieldChangeListener {

    void changed(BeanProperty propertyDescription, String id, String message);

    void validate(BeanProperty propertyDescription, String id, Object oldValue, Object newValue);

    void remove(Object newValue);

    Object add(String cloneId, String toId, String EntityName);
}
