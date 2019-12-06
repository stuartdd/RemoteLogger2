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

import java.io.IOException;
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

/**
 *
 * @author Stuart
 */
public abstract class FXMLField {

    static final Color ERROR_COLOR = Color.PINK;
    static final Color RO_COLOR = Color.WHITE;
    static final Color BG_COLOR = Color.LIGHTGREEN;
    static final Color HEADING_COLOR = Color.LIGHTGREEN;

    private final Pane pane;
    private final BeanWrapper beanWrapper;
    private final String propertyName;
    private final boolean readOnly;
    private final FXMLFieldChangeListener changeListener;
    private Label label = null;
    private Separator separator = null;

    public FXMLField(String type, BeanWrapper beanWrapper, String propertyName, boolean readOnly, FXMLFieldChangeListener changeListener) {
        this.beanWrapper = beanWrapper;
        this.propertyName = propertyName;
        this.readOnly = readOnly;
        this.changeListener = changeListener;
        String fileName = "/FXML" + type + "Field.fxml";
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
                    label.setText(beanWrapper.getDescription(propertyName));
                }
                label.setLayoutX(5);
            }
            if (c instanceof Separator) {
                separator = (Separator) c;
            }
        }
        setColor();
    }

    public final Pane getPane() {
        return pane;
    }

    public final void setColor() {
        if (label != null) {
            if (readOnly) {
                label.setBackground(pane.getBackground());
            } else {
                label.setBackground(new Background(new BackgroundFill(BG_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));
            }
        }
    }

    public void setColor(Color c) {
        if (label != null) {
            label.setBackground(new Background(new BackgroundFill(c, CornerRadii.EMPTY, Insets.EMPTY)));
        }
    }

    public Label getLabel() {
        return label;
    }

    public Separator getSeparator() {
        return separator;
    }

    public BeanWrapper getBeanWrapper() {
        return beanWrapper;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setWidth(double width) {
        if ((separator != null) && (width > 0)) {
            separator.setMinWidth(width);
            separator.setPrefWidth(width);
        }
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

    public void notifyChange(boolean error) {
        if (changeListener != null) {
            changeListener.changed(error);
        }
    }
    
    public abstract void destroy();

}
