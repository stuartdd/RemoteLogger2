package main;

import common.PropertyDataWithAnnotations;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import main.fields.BeanProperty;
import main.fields.FXMLBeanFieldLoaderException;
import main.fields.FXMLFieldChangeListener;
import main.fields.FXMLFieldCollection;

public class FXMLSettingsController implements FXMLFieldChangeListener {

    private Stage modalStage;
    private FXMLFieldCollection fieldCollection;
    private Map<String, PropertyDataWithAnnotations> beans;
    private boolean acceptChanges = false;
    private boolean updated;
    private FXMLFieldChangeListener listener;

    @FXML
    public VBox vBoxSettings;

    @FXML
    public Label labelStatus;

    @FXML
    public ScrollPane scrollPaneSettings;

    @FXML
    public Button doneButton;

    @FXML
    public Button removeButton;

    @FXML
    public FlowPane addFlowPane;

    @FXML
    public FlowPane removeFlowPane;

    @FXML
    public Button addButton;

    @FXML
    public void handleDoneButton() {
        acceptChanges = true;
        close();
    }

    @FXML
    public void handleCancelButton() {
        acceptChanges = false;
        close();
    }

    @FXML
    public void handleRemoveButton() {
    }

    @FXML
    public void handleAddButton() {
    }

    public static FXMLSettingsController load(Stage parent, Map<String, PropertyDataWithAnnotations> beans, String headingTemplate, String title, FXMLFieldChangeListener listener) throws IOException {
        FXMLSettingsController controller = createController("/FXMLSettingsDocument.fxml", parent, title);
        controller.init(beans, headingTemplate, listener, true);
        return controller;
    }

    public static FXMLSettingsController load(Stage parent, PropertyDataWithAnnotations bean, String headingTemplate, String title, FXMLFieldChangeListener listener) throws IOException {
        FXMLSettingsController controller = createController("/FXMLSettingsDocument.fxml", parent, title);
        Map<String, PropertyDataWithAnnotations> beans = new HashMap<>();
        beans.put("data", bean);
        controller.init(beans, headingTemplate, listener, false);
        return controller;
    }

    private static FXMLSettingsController createController(String FXMLFileNmae, Window parent, String title) {
        FXMLSettingsController controller = new FXMLSettingsController();
        FXMLLoader loader = new FXMLLoader(FXMLSettingsController.class.getResource(FXMLFileNmae));
        loader.setController(controller);
        try {
            Parent root = loader.load();
            controller.setModalStage(new Stage());
            Scene scene = new Scene(root);
            controller.getModalStage().setScene(scene);
            controller.getModalStage().initOwner(parent);
            controller.getModalStage().initModality(Modality.APPLICATION_MODAL);
            controller.getModalStage().setTitle(title);
            return controller;
        } catch (IOException e) {
            throw new FXMLBeanFieldLoaderException("Failed to load '" + FXMLFileNmae + "' from resources", e);
        }
    }

    private FXMLFieldCollection init(Map<String, PropertyDataWithAnnotations> beans, String headingTemplate, FXMLFieldChangeListener listener, boolean addRemove) {
        this.beans = beans;
        this.listener = listener;
        this.updated = false;
        scrollPaneSettings.setFitToHeight(true);
        scrollPaneSettings.setFitToWidth(true);
        addFlowPane.setVisible(addRemove);
        removeFlowPane.setVisible(addRemove);
        this.fieldCollection = new FXMLFieldCollection(modalStage, vBoxSettings, beans, false, headingTemplate, this);
        return this.fieldCollection;
    }

    public boolean showAndWait() {
        modalStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                close();
            }
        });
        modalStage.showAndWait();
        return acceptChanges;
    }

    public int updateAllValues() {
        int updatedCount = this.fieldCollection.updateAllValues();
        close();
        return updatedCount;
    }

    public void close() {
        modalStage.close();
    }

    public Stage getModalStage() {
        return modalStage;
    }

    public void setModalStage(Stage modalStage) {
        this.modalStage = modalStage;
    }

    public boolean isAcceptChanges() {
        return acceptChanges;
    }

    public boolean isUpdated() {
        return updated;
    }

    @Override
    public void changed(BeanProperty propertyDescription, String id, String message) {
        if (listener != null) {
            try {
                listener.changed(propertyDescription, id, message);
                setStatus(message);
            } catch (Exception e) {
                setStatus(e.getMessage());
                throw e;
            } finally {
                doneButton.setDisable(fieldCollection.isError());
            }
        }
        updated = true;
    }

    @Override
    public void validate(BeanProperty propertyDescription, String id, Object oldValue, Object newvalue) {
        if (listener != null) {
            try {
                listener.validate(propertyDescription, id, oldValue, newvalue);
            } catch (Exception e) {
                setStatus(e.getMessage());
                throw e;
            } finally {
                doneButton.setDisable(fieldCollection.isError());
            }
        }
    }

    private void setStatus(String message) {
        if (message.startsWith("!")) {
            labelStatus.setBackground(new Background(new BackgroundFill(Color.PINK, CornerRadii.EMPTY, Insets.EMPTY)));
            labelStatus.setText(message.substring(1));
        } else {
            labelStatus.setBackground(new Background(new BackgroundFill(Color.LIGHTGREEN.brighter(), CornerRadii.EMPTY, Insets.EMPTY)));
            labelStatus.setText(message);
        }
    }

}
