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

import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Map;

/**
 * @author Stuart
 */
public abstract class FXMLField {

    static final Color ERROR_COLOR = Color.PINK;
    static final Color RO_COLOR = Color.WHITE;
    static final Color BG_COLOR = Color.LIGHTGREEN;
    static final Color HEADING_COLOR = Color.LIGHTGREEN;

    private final Stage stage;
    private final Pane pane;
    private final BeanWrapper beanWrapper;
    private final boolean readOnly;
    private final FXMLFieldChangeListener changeListener;
    private final String propertyName;
    private final Integer id;
    private Label label = null;
    private Separator separator = null;
    private boolean error;

    public FXMLField(Stage stage, Integer id, String fieldType, BeanWrapper beanWrapper, String propertyName, boolean readOnly, FXMLFieldChangeListener changeListener) {
        this.stage = stage;
        this.id = id;
        this.beanWrapper = beanWrapper;
        this.propertyName = propertyName;
        this.readOnly = readOnly;
        this.changeListener = changeListener;
        String fileName = "/FXML" + fieldType + "Field.fxml";
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fileName));
            pane = loader.load();
        } catch (Exception e) {
            throw new FXMLBeanFieldLoaderException("Failed to load:" + fileName, e);
        }
        for (Node c : getPane().getChildren()) {
            if (c instanceof Label) {
                label = (Label) c;
                if (beanWrapper == null) {
                    label.setText(propertyName);
                } else {
                    label.setText(getBeanPropertyDescription().getDescription());
                }
                label.setLayoutX(5);
            }
            if (c instanceof Separator) {
                separator = (Separator) c;
            }
        }
        setError(false);
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        if (label != null) {
            if (error) {
                setBackgroundColor(ERROR_COLOR);
            } else {
                if (readOnly) {
                    label.setBackground(pane.getBackground());
                } else {
                    setBackgroundColor(BG_COLOR);
                }
            }
        }
        this.error = error;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public BeanPropertyDescription getBeanPropertyDescription() {
        return beanWrapper.getBeanPropertyDescription(getPropertyName());
    }

    public final Pane getPane() {
        return pane;
    }

    public Stage getStage() {
        return stage;
    }

    public Integer getId() {
        return id;
    }

    public String getIdString() {
        return ""+id;
    }

    public Label getLabel() {
        return label;
    }

    public Separator getSeparator() {
        return separator;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void removeNode(Node n) {
        if (n != null) {
            pane.getChildren().remove(n);
        }
    }

    public void removeCommonNodes() {
        removeNode(separator);
        removeNode(label);
    }

    public void notifyChange(String message) {
        if (changeListener != null) {
            changeListener.changed(getBeanWrapper().getBeanPropertyDescription(getPropertyName()), getId(), message);
        }
    }

    public void validateChange(Object oldValue, Object newValue) {
        if (changeListener != null) {
            changeListener.validate(getBeanWrapper().getBeanPropertyDescription(getPropertyName()), getId(), oldValue, newValue);
        }
    }

    public abstract void destroy();

    public BeanWrapper getBeanWrapper() {
        return beanWrapper;
    }

    public void setBackgroundColor(Color c) {
        if (label != null) {
            label.setBackground(new Background(new BackgroundFill(c, CornerRadii.EMPTY, Insets.EMPTY)));
        }
    }

}
