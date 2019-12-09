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
package main.dialogs;

import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
/**
 *
 * @author Stuart
 */
public class Dialogs {
    
    public static boolean alertOkCancel(double x, double y, String ti, String txt, String ht) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Alert:" + ti);
        alert.setHeaderText(txt);
        alert.setContentText(ht);
        alert.setX(x + 50);
        alert.setY(y + 50);
        Optional<ButtonType> result = alert.showAndWait();
        return result.get() == ButtonType.OK;
    }

    public static void errorDialog(double x, double y, String ti, String txt, String ht) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(ti);
        alert.setHeaderText(txt);
        alert.setContentText(ht);
        alert.setX(x + 50);
        alert.setY(y + 50);
        alert.showAndWait();
    }

    public static String textInputDialog(double x, double y, String ti, String ht, String prompt, String txt) {
        TextInputDialog textDialog = new TextInputDialog(txt);
        textDialog.setTitle(ti);
        textDialog.setHeaderText(ht);
        textDialog.setContentText(prompt);
        textDialog.setX(x + 50);
        textDialog.setY(y + 50);
        Optional<String> result = textDialog.showAndWait();
        if (result.isPresent()) {
            String res = result.get();
            if (res.equals(txt)) {
                return null;
            }
            return res;
        }
        return null;
    }

}
