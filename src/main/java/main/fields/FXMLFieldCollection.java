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
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Stuart
 */
public class FXMLFieldCollection {

    private List<FXMLField> fields = new ArrayList<>();
    private Map<String, FXMLHeadingField> headings = new HashMap<>();
    private Map<String, BeanPropertyWrapper> beanPropertyWrapperMap = new HashMap<>();
    private Stage mainStage;

    private final VBox container;

    public FXMLFieldCollection(Stage stage, VBox container, Map<String, PropertyDataWithAnnotations> data, boolean ro, String headingTemplate, FXMLFieldChangeListener changeListener) {
        this.mainStage = stage;
        this.container = container;
        try {
            List<String> sortedKeys = new ArrayList<>();
            for (String key : data.keySet()) {
                sortedKeys.add(key);
            }
            sortedKeys.sort(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareTo(o2);
                }
            });

            for (String key : sortedKeys) {
                BeanPropertyWrapper beanPropertyWrapper = new BeanPropertyWrapper(data.get(key));
                beanPropertyWrapperMap.put(key, beanPropertyWrapper);
                String h = headingTemplate.replaceAll("%\\{id\\}", key.toString());
                h = h.replaceAll("%\\{type\\}", key);
                FXMLHeadingField heading = new FXMLHeadingField(stage, key, h, changeListener);
                headings.put(key, heading);
                fields.add(heading);
                for (String prop : beanPropertyWrapper.getPropertyNameList()) {
                    Class parameterType = beanPropertyWrapper.getBeanProperty(prop).getParameterType();
                    if (parameterType.equals(int.class) || parameterType.equals(Integer.class)) {
                        fields.add(new FXMLIntegerField(stage, key, beanPropertyWrapper, prop, (Integer) beanPropertyWrapper.getInitialValue(prop), ro, changeListener));
                    }
                    if (parameterType.equals(boolean.class) || parameterType.equals(Boolean.class)) {
                        fields.add(new FXMLBooleanField(stage, key, beanPropertyWrapper, prop, (Boolean) beanPropertyWrapper.getInitialValue(prop), ro, changeListener));
                    }
                    if (parameterType.equals(String.class)) {
                        fields.add(new FXMLStringField(stage, key, beanPropertyWrapper, prop, (String) beanPropertyWrapper.getInitialValue(prop), ro, changeListener));
                    }
                }
            }
            for (FXMLField field : fields) {
                this.container.getChildren().add(field.getPane());
            }
        } catch (IOException ex) {
            Logger.getLogger(FXMLFieldCollection.class.getName()).log(Level.SEVERE, null, ex);
        }
        stage.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                doLayout();
            }
        });
        doLayout();
    }

    public void doLayout() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                for (FXMLField field : fields) {
                    field.doLayout();
                }
                container.layout();
            }
        });
    }

    public boolean isError() {
        for (FXMLField field : fields) {
            if (field.isError()) {
                return true;
            }
        }
        return false;
    }

    public void setHeadingColour(String id, Color c) {
        if (headings.isEmpty()) {
            return;
        }
        FXMLHeadingField heading = headings.get(id);
        if (heading == null) {
            return;
        }
        heading.setControlBackgroundColor(null, c);
    }

    public int updateAllValues() {
        int count = 0;
        for (Map.Entry<String, BeanPropertyWrapper> bp : beanPropertyWrapperMap.entrySet()) {
            count = count + bp.getValue().updateAllValues();
        }
        return count;
    }

    public boolean destroy() {
        for (FXMLField field : fields) {
            field.destroy();
            this.container.getChildren().remove(field.getPane());
        }
        fields = new ArrayList<>();
        headings = new HashMap<>();
        beanPropertyWrapperMap = new HashMap<>();
        return true;
    }
}
