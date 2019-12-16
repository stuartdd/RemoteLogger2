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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import main.dialogs.Dialogs;
import java.io.IOException;

/**
 * @author Stuart
 */
public class FXMLStringField extends FXMLField implements ChangeListener<String> {

    private TextField textField;
    private Button fileButton;

    public FXMLStringField(Stage stage, BeanWrapper beanWrapper, String propertyName, String value, boolean readOnly, FXMLFieldChangeListener changeListener) throws IOException {
        super(stage, "String", beanWrapper, propertyName, readOnly, changeListener);
        for (Node c : getPane().getChildren()) {
            if (c instanceof TextField) {
                textField = (TextField) c;
                if (value != null) {
                    textField.setText(value);
                } else {
                    textField.setText("null");
                }
                textField.textProperty().addListener(this);
            } else {
                if (c instanceof Button) {
                    fileButton = (Button) c;
                    if (getBeanPropertyDescription().getAdditional().toLowerCase().contains("file")) {
                        fileButton.setVisible(true);
                        textField.setEditable(false);
                        fileButton.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent actionEvent) {
                                String oldName = ((String) beanWrapper.getValue(propertyName));
                                String newName = Dialogs.fileChooser(getStage(), "Select: "+beanWrapper.getBeanPropertyDescription(propertyName).getDescription(), oldName, "JSON", "json");
                                if ((newName != null) && (!newName.equals(oldName))) {
                                    textField.setText(newName);
                                }
                            }
                        });
                    } else {
                        textField.setEditable(true);
                        fileButton.setVisible(false);
                    }
                }
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
        if (!newValue.equals(oldValue) && (!isReadOnly())) {
            if (notifyChange(false, "Property " + getPropertyName() + " updated")) {
                getBeanWrapper().setValue(getPropertyName(), newValue);
            } else {
                throw new FXMLBeanFieldLoaderException("XXX");
            }
        }
    }

}
