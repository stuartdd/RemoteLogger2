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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.TextField;

/**
 *
 * @author Stuart
 */
public class FXMLIntegerField extends FXMLField implements ChangeListener<String> {

    private TextField textField;

    public FXMLIntegerField(BeanWrapper beanWrapper, String propertyName, Integer value, boolean readOnly, FXMLFieldChangeListener changeListener) throws IOException {
        super("String", beanWrapper, propertyName, readOnly, changeListener);
        for (Node c : getPane().getChildren()) {
            if (c instanceof TextField) {
                textField = (TextField) c;
                if (value != null) {
                    textField.setText(value.toString());
                } else {
                    textField.setText("null");
                }
                textField.textProperty().addListener(this);
            }
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
        setColor();
        if (!newValue.equals(oldValue)) {
            try {
                Integer i = Integer.parseInt(newValue);
                getBeanWrapper().setValue(getPropertyName(), i);
                notifyChange(false);
            } catch (NumberFormatException e) {
                setColor(ERROR_COLOR);
                notifyChange(true);
            }
        }
    }

}
