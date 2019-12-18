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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * @author Stuart
 */
public class FXMLIntegerField extends FXMLField implements ChangeListener<String> {

    private TextField textField;
    private int lowerbound = Integer.MIN_VALUE;
    private int upperbound = Integer.MAX_VALUE;

    public FXMLIntegerField(Stage stage, int id, BeanWrapper beanWrapper, String propertyName, Integer value, boolean readOnly, FXMLFieldChangeListener changeListener) throws IOException {
        super(stage, id, "String", beanWrapper, propertyName, readOnly, changeListener);
        for (Node c : getPane().getChildren()) {
            if (c instanceof TextField) {
                textField = (TextField) c;
                if (value != null) {
                    textField.setText(value.toString());
                } else {
                    textField.setText("null");
                }
                textField.textProperty().addListener(this);
            } else {
                if (c instanceof Button) {
                    ((Button) c).setVisible(false);
                }
            }
        }
        lowerbound = getBeanPropertyDescription().getIntFlag("min", 0);
        upperbound = getBeanPropertyDescription().getIntFlag("max", lowerbound + 1000);
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
                        setError(true);
                        textField.setText("");
                        notifyChange("!ERROR: A value between " + lowerbound + " and " + upperbound + " is required. Value is " + oldValue);
                        return;
                    } else {
                        i = Integer.parseInt(newValue);
                    }
                    if ((i < lowerbound) || (i > upperbound)) {
                        setError(true);
                        textField.setText(oldValue);
                        notifyChange("!ERROR: Value [" + newValue + "] must be between " + lowerbound + " and " + upperbound);
                    } else {
                        setError(false);
                        getBeanWrapper().setValue(getPropertyName(), i);
                        textField.setText("" + i);
                        notifyChange("Property " + getPropertyName() + " updated to:" + newValue);
                    }
                } catch (NumberFormatException e) {
                    setError(true);
                    textField.setText(oldValue);
                    notifyChange("!ERROR: Value [" + newValue + "] is not a valid integer");
                }
            } finally {
                textField.textProperty().addListener(this);
            }
        }
    }

}
