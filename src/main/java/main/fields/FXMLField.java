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

import java.io.InputStream;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * @author Stuart
 */
public abstract class FXMLField implements Comparable {

    static final Color ERROR_COLOR = Color.PINK;
    static final Color BG_COLOR = Color.LIGHTGREEN;
    static final Color HEADING_COLOR = Color.LIGHTGREEN;

    private final Stage stage;
    private final Pane pane;
    private final BeanPropertyWrapper beanPropertyWrapper;
    private final BeanProperty beanProperty;
    private final boolean readOnly;
    private final FXMLFieldChangeListener changeListener;
    private final String propertyName;
    private final String id;
    private Label label = null;
    private Button buttonRevert;

    private static Image imageView;

    static {
        InputStream input = FXMLField.class.getResourceAsStream("/revert.png");
        if (input != null) {
            imageView = new Image(input);
        } else {
            imageView = null;
        }
    }

    public FXMLField(Stage stage, String id, String fieldType, BeanPropertyWrapper beanPropertyWrapper, String propertyName, boolean readOnly, FXMLFieldChangeListener changeListener) {
        this.stage = stage;
        this.id = id;
        this.propertyName = propertyName;
        if (beanPropertyWrapper != null) {
            this.beanPropertyWrapper = beanPropertyWrapper;
            this.beanProperty = beanPropertyWrapper.getBeanProperty(this.propertyName);
        } else {
            this.beanProperty = null;
            this.beanPropertyWrapper = null;
        }
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
                if (beanProperty != null) {
                    label.setText(beanProperty.getDescription());
                } else {
                    label.setText(propertyName);
                }
                label.setLayoutX(5);
                setControlBackgroundColor(label, BG_COLOR);
            }
            if ((c instanceof Button) && (c.getId().equals("buttonRevert"))) {
                buttonRevert = (Button) c;
                buttonRevert.setDisable(true);
                buttonRevert.setTooltip(new Tooltip("Revert to original value"));
                setControlBackgroundColor(buttonRevert, BG_COLOR);
                if (imageView != null) {
                    buttonRevert.setGraphic(new ImageView(imageView));
                }
                buttonRevert.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        revert();
                    }
                });
            }
        }
    }

    private void revert() {
        if (beanProperty != null) {
            beanProperty.revert();
        }
        doRevert();
    }

    protected abstract void doRevert();

    public final void setControlWidth(Control control, double w) {
        control.setMinWidth(w);
        control.setPrefWidth(w);
        control.setMaxWidth(w);
    }

    public final void setControlBackgroundColor(Control control, Color c) {
        if (control != null) {
            control.setBackground(new Background(new BackgroundFill(c, CornerRadii.EMPTY, Insets.EMPTY)));
        } else {
            if (label != null) {
                label.setBackground(new Background(new BackgroundFill(c, CornerRadii.EMPTY, Insets.EMPTY)));
            }
        }
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof FXMLField) {
            return label.getText().compareTo(((FXMLField) o).getLabel().getText());
        }
        return -1;
    }

    public double getFieldWidth() {
        return (stage.getWidth() / 6) * 4;
    }

    public double getLabelWidth() {
        return ((stage.getWidth() / 6) * 2) + 50;
    }

    public void doLayout() {
        if (label == null) {
            return;
        }
        if (buttonRevert != null) {
            setControlWidth(label, getLabelWidth() - 33);
            setControlWidth(buttonRevert, 35);
            buttonRevert.setLayoutX(getLabelWidth() - 30);
        } else {
            setControlWidth(label, getLabelWidth());
        }
    }

    public boolean isError() {
        if (beanProperty != null) {
            return beanProperty.isError();
        }
        return false;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public BeanProperty getBeanProperty() {
        return beanProperty;
    }

    public final Pane getPane() {
        return pane;
    }

    public Stage getStage() {
        return stage;
    }

    public String getId() {
        return id;
    }

    public String getIdString() {
        return "" + id;
    }

    public Label getLabel() {
        return label;
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
        removeNode(label);
        removeNode(buttonRevert);
    }

    public void notifyChange(String message) {
        if (beanProperty != null) {
            if (changeListener != null) {
                changeListener.changed(beanProperty, getId(), message);
            }
            if (beanProperty.isError()) {
                setControlBackgroundColor(label, ERROR_COLOR);
                setControlBackgroundColor(buttonRevert, ERROR_COLOR);
                if (buttonRevert != null) {
                    buttonRevert.setDisable(false);
                }
            } else {
                setControlBackgroundColor(label, BG_COLOR);
                setControlBackgroundColor(buttonRevert, BG_COLOR);
                if (buttonRevert != null) {
                    buttonRevert.setDisable(!beanProperty.isUpdated());
                }
            }
        }
    }

    public void validateChange(Object oldValue, Object newValue) {
        if (changeListener != null) {
            changeListener.validate(beanProperty, getId(), oldValue, newValue);
        }
    }

    public abstract void destroy();

    public BeanPropertyWrapper getBeanPropertyWrapper() {
        return beanPropertyWrapper;
    }
}
