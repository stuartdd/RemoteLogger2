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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import main.dialogs.Dialogs;

/**
 * @author Stuart
 */
public class FXMLStringField extends FXMLField implements ChangeListener<String> {

    private TextField textField;
    private Button fileButton;
    private boolean hasFileButton;

    public FXMLStringField(Stage stage, String id, BeanPropertyWrapper beanPropertyWrapper, String propertyName, boolean readOnly, FXMLFieldChangeListener changeListener) throws IOException {
        super(stage, id, "String", beanPropertyWrapper, propertyName, readOnly, changeListener);
        hasFileButton = false;
        for (Node c : getPane().getChildren()) {
            if (c instanceof TextField) {
                textField = (TextField) c;
                textField.setText(getBeanProperty().getDisplayValue().toString());
                textField.textProperty().addListener(this);
            } else {
                if ((c instanceof Button) && (c.getId().equals("buttonFile"))) {
                    fileButton = (Button) c;
                    if (getBeanProperty().isTypeId("file")) {
                        hasFileButton = true;
                        String ext = getBeanProperty().getFlag("ext", "json");
                        String desc = getBeanProperty().getFlag("desc", "File type *." + ext);
                        textField.setEditable(false);
                        fileButton.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent actionEvent) {
                                String oldName = textField.getText();
                                String newName = Dialogs.fileChooser(getStage(), "Select: " + beanPropertyWrapper.getBeanProperty(propertyName).getDescription(), oldName, desc, ext);
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
    public void doLayout() {
        if (textField == null) {
            return;
        }
        super.doLayout();
        double fWidth = getFieldWidth();
        double lWidth = getLabelWidth();
        textField.setLayoutX(lWidth + 10);
        if (hasFileButton) {
            setControlWidth(textField, fWidth - 130);
            setControlWidth(fileButton, 40);
            fileButton.setVisible(true);
            fileButton.setLayoutX((lWidth + fWidth) - 110);
        } else {
            if (fileButton!=null) {
                fileButton.setVisible(false);
            }
            setControlWidth(textField, fWidth - 20);
        }
    }

    @Override
    protected void doRevert() {
        textField.setText(getBeanProperty().getDisplayValue().toString());
    }


    @Override
    public void destroy() {
        textField.textProperty().removeListener(this);
        removeCommonNodes();
        removeNode(textField);
        removeNode(fileButton);
    }

    @Override
    public void changed(ObservableValue<? extends String> arg0, String oldValue, String newValue) {
        if (!newValue.equals(oldValue) && (!isReadOnly())) {
            try {
                textField.textProperty().removeListener(this);
                try {
                    validateChange(oldValue, newValue);
                    getBeanProperty().setUpdatedValue(newValue);
                    notifyChange("Property '" + getBeanProperty().getDescription() + "' updated");
                } catch (Exception e) {
                    getBeanProperty().setErrorValue(newValue);
                    notifyChange("!ERROR '" + getBeanProperty().getDescription() + "' Reason: " + e.getMessage());
                }
                textField.setText(getBeanProperty().getDisplayValue().toString());
            } finally {
                textField.textProperty().addListener(this);
            }
        }
    }

}
