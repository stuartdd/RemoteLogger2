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
import javafx.event.EventType;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

/**
 *
 * @author Stuart
 */
public class FXMLBooleanField extends FXMLField implements ChangeListener<Boolean> {

    private CheckBox checkBox;

    public FXMLBooleanField(BeanWrapper beanWrapper, String propertyName, Boolean state, boolean readOnly, FXMLFieldChangeListener changeListener) throws IOException {
        super("Boolean", beanWrapper, propertyName, readOnly,changeListener);
        for (Node c : getPane().getChildren()) {
            if (c instanceof CheckBox) {
                checkBox = (CheckBox) c;
                checkBox.setSelected(state);
                checkBox.selectedProperty().addListener(this);
            }
        }
    }

    @Override
    public void destroy() {
        checkBox.selectedProperty().removeListener(this);
        removeCommonNodes();
        removeNode(checkBox);
    }

    @Override
    public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldValue, Boolean newValue) {
        if (!newValue.equals(oldValue) && (!isReadOnly())) {
            getBeanWrapper().setValue(getPropertyName(), newValue);
            notifyChange(false, "Property "+getPropertyName() + " updated");
        }
    }

}
