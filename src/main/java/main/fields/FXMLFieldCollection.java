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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 *
 * @author Stuart
 */
public class FXMLFieldCollection {

    
    
    private List<FXMLField> fields = new ArrayList<>();
    private Map<String, FXMLHeadingField> headings = new HashMap<>();
    
    private final VBox container;

    public FXMLFieldCollection(VBox container, Map<String, PropertyDataWithAnnotations> data, boolean ro, String headingTemplate, FXMLFieldChangeListener changeListener) {
        this.container = container;
        try {
            for (Map.Entry<String, PropertyDataWithAnnotations> obj : data.entrySet()) {
                BeanWrapper beanWrapper = new BeanWrapper(obj.getValue());
                String h = headingTemplate.replaceAll("%\\{id\\}", obj.getKey());
                FXMLHeadingField heading = new FXMLHeadingField(obj.getKey(), h, changeListener);
                headings.put(obj.getKey(), heading);
                fields.add(heading);
                for (String prop : beanWrapper.getPropertyList()) {
                    BeanPropertyDescription desc = beanWrapper.getBeanPropertyDescription(prop);
                    if (desc.isDefined()) {
                        Class parameterType = desc.getParameterType();
                        if (parameterType.equals(int.class) || parameterType.equals(Integer.class)) {
                            fields.add(new FXMLIntegerField(beanWrapper, prop, (Integer) beanWrapper.getValue(prop), ro, changeListener));
                        }
                        if (parameterType.equals(boolean.class) || parameterType.equals(Boolean.class)) {
                            fields.add(new FXMLBooleanField(beanWrapper, prop, (Boolean) beanWrapper.getValue(prop), ro, changeListener));
                        }
                        if (parameterType.equals(String.class)) {
                            fields.add(new FXMLStringField(beanWrapper, prop, (String) beanWrapper.getValue(prop), ro, changeListener));
                        }
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

    public void setHeadingColour(String id, Color c) {
        if (headings.isEmpty()) {
            return;
        }
        FXMLHeadingField heading = headings.get(id);
        if (heading == null) {
            return;
        }
        heading.setBackgroundColor(c);
    }
    
    public void destroy() {
        for (FXMLField field : fields) {
            field.destroy();
            this.container.getChildren().remove(field.getPane());
        }
        fields = new ArrayList<>();
        headings = new HashMap<>();
    }
}
