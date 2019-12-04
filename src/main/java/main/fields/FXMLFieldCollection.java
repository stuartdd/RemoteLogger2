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

import common.PropertyDataWithAnnotations;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.layout.VBox;

/**
 *
 * @author Stuart
 */
public class FXMLFieldCollection {

    FXMLBooleanField inputField;

    public FXMLFieldCollection(VBox vBoxConnections, Map<String, PropertyDataWithAnnotations> data) {
        try {
            for (Map.Entry<String, PropertyDataWithAnnotations> obj : data.entrySet()) {
                vBoxConnections.getChildren().add((new FXMLHeadingField(obj.getKey() + ":")).getPane());
                BeanWrapper beanWrapper = new BeanWrapper(obj.getValue());
                for (String prop : beanWrapper.getPropertyList()) {
                    if (beanWrapper.getParameterType(prop).equals(int.class) || beanWrapper.getParameterType(prop).equals(Integer.class))  {
                        FXMLIntegerField intField = new FXMLIntegerField(beanWrapper.getDescription(prop), (Integer)beanWrapper.getValue(prop));
                        vBoxConnections.getChildren().add(intField.getPane());
                    }
                    if (beanWrapper.getParameterType(prop).equals(boolean.class) || beanWrapper.getParameterType(prop).equals(Boolean.class))  {
                        FXMLBooleanField intField = new FXMLBooleanField(beanWrapper.getDescription(prop), (Boolean)beanWrapper.getValue(prop));
                        vBoxConnections.getChildren().add(intField.getPane());
                    }
                    if (beanWrapper.getParameterType(prop).equals(String.class))  {
                        FXMLStringField intField = new FXMLStringField(beanWrapper.getDescription(prop), (String)beanWrapper.getValue(prop));
                        vBoxConnections.getChildren().add(intField.getPane());
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(FXMLFieldCollection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
