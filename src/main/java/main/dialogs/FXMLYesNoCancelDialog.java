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

import java.io.IOException;
import java.util.Map;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import main.fields.FXMLBeanFieldLoaderException;

/**
 *
 * @author Stuart
 */
public class FXMLYesNoCancelDialog {

    public enum RESP {
        YES, NO, CANCEL
    }

    @FXML
    public ScrollPane scrollPaneTextArea;

    @FXML
    public TextArea textAreaList;

    @FXML
    public Button noButton;

    private Stage modalStage;
    private RESP resp = RESP.CANCEL;

    @FXML
    public void handleYesButton() {
        resp = RESP.YES;
        close();
    }

    public void handleNoButton() {
        resp = RESP.NO;
        close();
    }

    public void handleCancelButton() {
        resp = RESP.CANCEL;
        close();
    }

    public RESP showAndWait() {
        modalStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                close();
            }
        });
        modalStage.showAndWait();
        return resp;
    }

    public static FXMLYesNoCancelDialog load(Map<String, Object> data, Window parent, String title, boolean askSaveChanges) {
        FXMLYesNoCancelDialog controller = new FXMLYesNoCancelDialog();
        FXMLLoader loader = new FXMLLoader(FXMLYesNoCancelDialog.class.getResource("/FXMLListOptionsDialog.fxml"));
        loader.setController(controller);
        try {
            Parent root = loader.load();
            controller.setModalStage(new Stage());
            Scene scene = new Scene(root);
            controller.getModalStage().setScene(scene);
            controller.getModalStage().initOwner(parent);
            controller.getModalStage().initModality(Modality.APPLICATION_MODAL);
            controller.getModalStage().setTitle(title);
            controller.init(data, askSaveChanges);
            return controller;
        } catch (IOException e) {
            throw new FXMLBeanFieldLoaderException("Failed to load 'FXMLListOptionsDialog.fxml' from resources", e);
        }
    }

    private void setModalStage(Stage stage) {
        this.modalStage = stage;
    }

    private Stage getModalStage() {
        return modalStage;
    }

    private void init(Map<String, Object> data, boolean askSaveChanges) {
        scrollPaneTextArea.setFitToHeight(true);
        scrollPaneTextArea.setFitToWidth(true);
        noButton.setVisible(askSaveChanges);
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> s : data.entrySet()) {
            sb.append(s.getKey()).append(':').append(s.getValue().toString()).append("\n");
        }
        if (sb.length() == 0) {
            textAreaList.setText("No changes have been made");
        } else {
            textAreaList.setText(sb.toString());
        }
    }

    private void close() {
        modalStage.close();
    }

}
