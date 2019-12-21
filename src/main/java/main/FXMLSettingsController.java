package main;

import common.PropertyDataWithAnnotations;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import main.fields.BeanPropertyDescription;
import main.fields.FXMLFieldChangeListener;
import main.fields.FXMLFieldCollection;

public class FXMLSettingsController implements FXMLFieldChangeListener {
    private Stage modalStage;
    private FXMLFieldCollection connectionsFieldCollection;
    private PropertyDataWithAnnotations bean;
    private boolean acceptChanges = false;
    private boolean updated;
    private FXMLFieldChangeListener listener;

    @FXML
    public VBox vBoxSettings;

    @FXML
    public ScrollPane scrollPaneSettings;

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

    public boolean acceptChanges() {
        return acceptChanges;
    }

    public static FXMLSettingsController load(Stage parent, PropertyDataWithAnnotations bean, FXMLFieldChangeListener listener) throws IOException {
        FXMLSettingsController controller = new FXMLSettingsController();
        FXMLLoader loader = new FXMLLoader(FXMLSettingsController.class.getResource("/FXMLSettingsDocument.fxml"));
        loader.setController(controller);
        Parent root = loader.load();
        controller.setModalStage(new Stage());
        Scene scene = new Scene(root);
        controller.getModalStage().setScene(scene);
        controller.getModalStage().initOwner(parent);
        controller.getModalStage().initModality(Modality.APPLICATION_MODAL);
        controller.init(bean, listener);
        return controller;
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

    public void close() {
        modalStage.close();
    }

    private FXMLFieldCollection init(PropertyDataWithAnnotations bean, FXMLFieldChangeListener listener) {
        this.bean = bean;
        this.listener = listener;
        this.updated = false;
        scrollPaneSettings.setFitToHeight(true);
        scrollPaneSettings.setFitToWidth(true);
        Map<Integer, PropertyDataWithAnnotations> map = new HashMap<>();
        map.put(-1, bean);
        this.connectionsFieldCollection = new FXMLFieldCollection(Main.getStage(), vBoxSettings, map, false, "Settings:", this);
        return this.connectionsFieldCollection;
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
    public void changed(BeanPropertyDescription propertyDescription, Integer id, String message) {
        if (listener!=null) {
            listener.changed(propertyDescription, id, message);
        }
        updated = true;
    }

    @Override
    public String validate(BeanPropertyDescription propertyDescription, Integer id, Object oldValue, Object newvalue) {
        if (listener!=null) {
            return validate(propertyDescription, id, oldValue, newvalue);
        }        
        return null;
    }

    @Override
    public void select(String id) {
        if (listener!=null) {
            listener.select(id);
        }
    }

}


