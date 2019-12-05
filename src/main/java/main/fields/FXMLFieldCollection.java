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

    
    
    private List<FXMLField> fields = new ArrayList<>();
    private final VBox container;

    public FXMLFieldCollection(VBox container, Map<String, PropertyDataWithAnnotations> data, boolean ro) {
        this.container = container;
        try {
            for (Map.Entry<String, PropertyDataWithAnnotations> obj : data.entrySet()) {
                BeanWrapper beanWrapper = new BeanWrapper(obj.getValue());
                fields.add((new FXMLHeadingField(obj.getKey() + ":")));
                for (String prop : beanWrapper.getPropertyList()) {
                    if (beanWrapper.getParameterType(prop).equals(int.class) || beanWrapper.getParameterType(prop).equals(Integer.class)) {
                        fields.add(new FXMLIntegerField(beanWrapper, prop, (Integer)beanWrapper.getValue(prop),ro));
                    }
                    if (beanWrapper.getParameterType(prop).equals(boolean.class) || beanWrapper.getParameterType(prop).equals(Boolean.class)) {
                        fields.add(new FXMLBooleanField(beanWrapper, prop, (Boolean) beanWrapper.getValue(prop),ro));
                    }
                    if (beanWrapper.getParameterType(prop).equals(String.class)) {
                        fields.add(new FXMLStringField(beanWrapper, prop, (String) beanWrapper.getValue(prop),ro));
                    }
                }
            }
            for (FXMLField field : fields) {
                this.container.getChildren().add(field.getPane());
            }
        } catch (IOException ex) {
            Logger.getLogger(FXMLFieldCollection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setWidth(double width) {
        if (width > 0) {
            for (FXMLField field : fields) {
                field.setWidth(width);
            }
        }
    }

    public void destroy() {
        for (FXMLField field : fields) {
            field.destroy();
            this.container.getChildren().remove(field.getPane());
        }
        fields = new ArrayList<>();
    }
}
