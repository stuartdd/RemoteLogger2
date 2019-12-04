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
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

/**
 *
 * @author Stuart
 */
public class FXMLBooleanField {

    private Pane pane;
    private Label label;
    private CheckBox checkBox;

    public FXMLBooleanField(String text, Boolean state) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXMLBooleanField.fxml"));
        pane = loader.load();
        for (Node c:pane.getChildren()) {
            if (c instanceof Label) {
                label = (Label)c;
                label.setText(text);
            }
            if (c instanceof CheckBox) {
                checkBox = (CheckBox)c;
                checkBox.setSelected(state);
            }
        }
    }

    public Pane getPane() {
        return pane;
    }

}
