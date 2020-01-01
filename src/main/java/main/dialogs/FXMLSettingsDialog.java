package main.dialogs;

import common.PropertyDataWithAnnotations;
import geom.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
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
import main.Main;
import main.fields.BeanProperty;
import main.fields.FXMLBeanFieldLoaderException;
import main.fields.FXMLFieldChangeListener;
import main.fields.FXMLFieldCollection;

public class FXMLSettingsDialog implements FXMLFieldChangeListener {

    private Stage modalStage;
    private FXMLFieldCollection fieldCollection;
    private Map<String, PropertyDataWithAnnotations> beans;
    private boolean acceptChanges = false;
    private boolean updated;
    private boolean addRemove;
    private String headingTemplate;
    private String entityName;
    private FXMLFieldChangeListener listener;
    private ChoiceBoxSelectionListener choiceBoxSelectionListener;
    private List<String> removedIds = new ArrayList<>();

    @FXML
    public VBox vBoxSettings;

    @FXML
    public Label labelStatus;

    @FXML
    public ScrollPane scrollPaneSettings;

    @FXML
    public Button doneButton;

    @FXML
    public ChoiceBox removeIdDropdown;

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
    public void handleAddButton() {
        String toId = SimpleDialogs.textInputDialog(0, 0, "Add " + entityName, "Enter the unique id for the " + entityName, "ID:", "Port Number");
        if ((toId != null) && (toId.trim().length()>0)) {
            if (listener != null) {
                try {
                this.add(null, toId, entityName);
                } catch (Exception e) {
                    SimpleDialogs.errorDialog(0, 0, "Add "+entityName, "Error adding a "+entityName, e.getMessage().substring(1));
                }
            }
        }
    }

    public static FXMLSettingsDialog load(Stage parent, Map<String, PropertyDataWithAnnotations> beans, String headingTemplate, String title, String entityName, FXMLFieldChangeListener listener) {
        FXMLSettingsDialog controller = createController("/FXMLSettingsDocument.fxml", parent, title);
        controller.init(beans, headingTemplate, entityName, listener, true);
        return controller;
    }

    public static FXMLSettingsDialog load(Stage parent, PropertyDataWithAnnotations bean, String headingTemplate, String title, String entityName, FXMLFieldChangeListener listener) {
        FXMLSettingsDialog controller = createController("/FXMLSettingsDocument.fxml", parent, title);
        Map<String, PropertyDataWithAnnotations> beans = new HashMap<>();
        beans.put(bean.getClass().getSimpleName(), bean);
        controller.init(beans, headingTemplate, entityName, listener, false);
        return controller;
    }

    private static FXMLSettingsDialog createController(String FXMLFileNmae, Window parent, String title) {
        FXMLSettingsDialog controller = new FXMLSettingsDialog();
        FXMLLoader loader = new FXMLLoader(FXMLSettingsDialog.class.getResource(FXMLFileNmae));
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

    private FXMLFieldCollection init(Map<String, PropertyDataWithAnnotations> beans, String headingTemplate, String entityName, FXMLFieldChangeListener listener, boolean addRemove) {
        this.beans = beans;
        this.headingTemplate = headingTemplate;
        this.entityName = entityName;
        this.listener = listener;
        this.choiceBoxSelectionListener = new ChoiceBoxSelectionListener(beans);
        this.updated = false;
        this.addRemove = addRemove;
        this.scrollPaneSettings.setFitToHeight(true);
        this.scrollPaneSettings.setFitToWidth(true);
        this.addFlowPane.setVisible(addRemove);
        this.removeFlowPane.setVisible(addRemove);
        initFieldCollection(beans);
        initRemoveIdDropdown(beans);
        return this.fieldCollection;
    }

    public void initFieldCollection(Map<String, PropertyDataWithAnnotations> beans) {
        if (this.fieldCollection != null) {
            this.fieldCollection.destroy();
        }
        this.fieldCollection = new FXMLFieldCollection(modalStage, vBoxSettings, beans, false, headingTemplate, entityName, this);
    }

    public void initRemoveIdDropdown(Map<String, PropertyDataWithAnnotations> beans) {
        if (addRemove) {
            List<String> l = new ArrayList<>();
            l.add("Remove!");
            for (String s : beans.keySet()) {
                l.add(s);
            }
            removeIdDropdown.getSelectionModel().selectedItemProperty().removeListener(choiceBoxSelectionListener);
            removeIdDropdown.setItems(FXCollections.observableArrayList(l));
            removeIdDropdown.getSelectionModel().select(0);
            removeIdDropdown.getSelectionModel().selectedItemProperty().addListener(this.choiceBoxSelectionListener);
            removeIdDropdown.setDisable(beans.size() < 2);
        }
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

    public int updateAllValues(Map<String, Object> configChanges, String id) {
        int updatedCount = this.fieldCollection.updateAllValues(configChanges, id);
        close();
        return updatedCount;
    }

    public void close() {
        modalStage.close();
    }

    public Stage getModalStage() {
        return modalStage;
    }

    public Map<String, PropertyDataWithAnnotations> getBeans() {
        return beans;
    }

    public List<String> getRemovedIds() {
        return removedIds;
    }

    public void setModalStage(Stage modalStage) {
        this.modalStage = modalStage;
    }

    public boolean isAcceptChanges() {
        return acceptChanges;
    }

    public String getEntityName() {
        return entityName;
    }

    public boolean isUpdated() {
        return fieldCollection.countUpdates() != 0;
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
    public void validate(BeanProperty propertyDescription, String id, Object oldValue, Object newValue) {
        if (listener != null) {
            try {
                listener.validate(propertyDescription, id, oldValue, newValue);
            } catch (Exception e) {
                setStatus(e.getMessage());
                throw e;
            } finally {
                doneButton.setDisable(fieldCollection.isError());
            }
        }
    }

    @Override
    public Object add(String cloneId, String toId, String entityName) {
        if (listener != null) {
            try {
                return listener.add(cloneId, toId, entityName);
            } catch (Exception e) {
                setStatus(e.getMessage());
                throw e;
            } finally {
                doneButton.setDisable(fieldCollection.isError());
            }
        }
        return null;
    }

    @Override
    public void remove(Object removeValue) {
        if (removeValue != null) {
            String removeValueString = removeValue.toString();
            if (listener != null) {
                try {
                    listener.remove(removeValue);
                    beans.remove(removeValueString);
                    initRemoveIdDropdown(beans);
                    initFieldCollection(beans);
                    removedIds.add(removeValueString);
                    listener.changed(new BeanProperty(removeValueString, null, "", Object.class), removeValueString, "REMOVED - " + headingTemplate.replaceAll("%\\{id\\}", removeValue.toString()));
                    setStatus("Removed " + entityName + ": " + removeValue);
                } catch (Exception e) {
                    initRemoveIdDropdown(beans);
                    Point r = Main.getPoint();
                    SimpleDialogs.errorDialog(r.x, r.y, "REMOVAL:", "Cannot remove " + entityName + " entities:", e.getMessage().substring(1));
                    setStatus(e.getMessage());
                } finally {
                    doneButton.setDisable(fieldCollection.isError());
                }
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

    private class ChoiceBoxSelectionListener implements ChangeListener<String> {

        private final Map<String, PropertyDataWithAnnotations> beans;

        public ChoiceBoxSelectionListener(Map<String, PropertyDataWithAnnotations> beans) {
            this.beans = beans;
        }

        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            Point r = Main.getPoint();
            if (isUpdated()) {
                SimpleDialogs.errorDialog(r.x, r.y, "REMOVAL:", "Cannot remove " + entityName + " entities. Previous updates have been made", "Accept or Cancel the existing changes first");
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        initRemoveIdDropdown(beans);
                        setStatus("!" + entityName + " entities cannot be removed while existing changes are pending:");
                    }
                });
            } else {
                if (SimpleDialogs.alertOkCancel(r.x, r.y, "Remove Selected " + entityName + ":", "Remove: " + newValue.toString(), "Press OK to REMOVE this item from the list")) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            remove(newValue);
                        }
                    });
                }
            }
        }
    }
}
