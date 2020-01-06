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

import common.DataValidationException;
import java.io.IOException;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * @author Stuart
 */
public class FXMLIntegerField extends FXMLField implements ChangeListener<String> {

    private TextField textField;
    private int lowerbound = Integer.MIN_VALUE;
    private int upperbound = Integer.MAX_VALUE;

    public FXMLIntegerField(Stage stage, VBox vbox, String id, BeanPropertyWrapper beanPropertyWrapper, String propertyName, String entityName, boolean readOnly, FXMLFieldChangeListener changeListener) throws IOException {
        super(stage, vbox, id, "String", beanPropertyWrapper, propertyName, entityName, readOnly, changeListener);
        for (Node c : getPane().getChildren()) {
            if (c instanceof TextField) {
                textField = (TextField) c;
                textField.setText(getBeanProperty().getDisplayValue().toString());
                if (!readOnly) {
                    textField.textProperty().addListener(this);
                }
                textField.setEditable(!readOnly);
            } else {
                if ((c instanceof Button) && (!c.getId().equals("buttonRevert"))) {
                    ((Button) c).setVisible(false);
                }
            }
        }
        lowerbound = getBeanProperty().getIntFlag("min", 0);
        upperbound = getBeanProperty().getIntFlag("max", lowerbound + Integer.MAX_VALUE);
    }

    @Override
    protected void doRevert() {
        Object initial = getBeanProperty().getInitialValue();
        if (initial == null) {
            textField.setText("");
        } else {
            textField.setText(initial.toString());
        }
    }

    @Override
    public void doLayout() {
        if (textField == null) {
            return;
        }
        super.doLayout();
        double fWidth = getFieldWidth() / 4;
        setControlWidth(textField, fWidth);
        if (isReadOnly()) {
            textField.setLayoutX(getLabelWidth() - 20);
        } else {
            textField.setLayoutX(getLabelWidth() + 10);
        }
    }

    @Override
    public void destroy() {
        textField.textProperty().removeListener(this);
        removeCommonNodes();
        removeNode(textField);
    }

    @Override
    public void changed(ObservableValue<? extends String> arg0, String oldValue, String newValue) {
        if (!newValue.equals(oldValue) && (!isReadOnly())) {
            try {
                textField.textProperty().removeListener(this);
                try {
                    Integer i;
                    if (newValue.trim().length() == 0) {
                        throw new DataValidationException(getEntityName() + " '" + getPropertyName() + "' requires a value between " + lowerbound + " and " + upperbound + ". Value is " + oldValue);
                    } else {
                        validateChange(oldValue, newValue);
                        i = Integer.parseInt(newValue);
                    }
                    if ((i < lowerbound) || (i > upperbound)) {
                        throw new DataValidationException(getEntityName() + " '" + getPropertyName() + "' must be between " + lowerbound + " and " + upperbound);
                    } else {
                        getBeanProperty().setUpdatedValue(i);
                        notifyChange(getEntityName() + " '" + getPropertyName() + "' updated from: " + getBeanProperty().getInitialValueNotNull() + " to: " + newValue);
                    }
                } catch (NumberFormatException e) {
                    getBeanProperty().setErrorValue(newValue);
                    notifyChange("!ERROR: " + getEntityName() + " '" + getPropertyName() + "' value [" + newValue + "] is not a valid integer");
                } catch (Exception e) {
                    getBeanProperty().setErrorValue(newValue);
                    notifyChange("!ERROR: " + getEntityName() + " '" + getPropertyName() + "' value [" + newValue + "] " + e.getMessage());
                }
                textField.setText(getBeanProperty().getDisplayValue().toString());
            } finally {
                textField.textProperty().addListener(this);
            }
        }
    }

}
