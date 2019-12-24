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
package main;

import common.*;
import geom.Point;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import main.dialogs.Dialogs;
import main.fields.BeanPropertyDescription;
import main.fields.FXMLFieldChangeListener;
import main.fields.FXMLFieldCollection;
import server.ServerExpectations;
import server.ServerManager;
import server.ServerState;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 *
 */
public class FXMLDocumentController implements Initializable, Notifier {

    private static final String SERVER_TAB_FX_ID = "servers";
    private static final String LOGS_TAB_FX_ID = "logs";
    private static final String EXPECTATIONS_TAB_FX_ID = "expectations";


    @FXML
    private AnchorPane connectionsAnchorPane;

    @FXML
    private Canvas connectionCanvas;

    @FXML
    private TabPane mainTabbedPane;

    @FXML
    private VBox vBoxServers;
    private FXMLFieldCollection serverFieldCollection;

    @FXML
    private VBox vBoxExpectations;
    private FXMLFieldCollection expectationsFieldCollection;

    @FXML
    private VBox vBoxLogsControlList;

    @FXML
    private Label labelStatus;

    @FXML
    private Label serverStateLabel;

    @FXML
    private ScrollPane scrollPaneLogLines;
    @FXML
    private TextArea textAreaLogLines;

    @FXML
    private Button buttonStartStopServer;
    @FXML
    private Button buttonSaveConfigChanges;
    @FXML
    private Button buttonReloadConfigChanges;

    @FXML
    private ChoiceBox serverChoiceBox;

    private Integer currentSelectedServerPort = -1;
    private String currentTabId = null;
    private boolean configDataHasChanged = false;
    private Map<Integer, CheckBox> logCheckBoxesByPort = new HashMap<>();

    @FXML
    public void handleClearLogsButton() {
        Point r = Main.getPoint();
        if (Dialogs.alertOkCancel(r.x, r.y, "Clear logs", "Erase all log data!", "Press OK to continue")) {
            Main.getLogLines().clear();
        }
    }

    @FXML
    public void handleCloseApplicationButton() {
        Main.closeApplication(0, configDataHasChanged);
    }


    @FXML
    public void handleButtonStartStopServer() {
        Main.controllerNotification(new Notification(currentSelectedServerPort, Action.START_STOP_SERVER, "Start/Stop Server"));
    }

    @FXML
    public void handleButtonSaveConfigChanges() {
        Main.controllerNotification(new Notification(currentSelectedServerPort, Action.RESTART_SERVERS, null, "Restarting Servers"));
    }

    @FXML
    public void handleButtonReloadConfigChanges() {
        Main.controllerNotification(new Notification(currentSelectedServerPort, Action.RELOAD_RESTART_SERVERS, null, "Restarting Servers"));
    }

    public boolean hasConfigDataHasChanged() {
        return configDataHasChanged;
    }

    public void tabSelectionChanged(Tab newTab, Tab oldTab) {
        if (oldTab != null) {
            if (newTab == oldTab) {
                return;
            }
            if (oldTab.getId().equalsIgnoreCase(SERVER_TAB_FX_ID)) {
                serverFieldCollection.destroy();
            }
            if (oldTab.getId().equalsIgnoreCase(LOGS_TAB_FX_ID)) {
                updateTheLogs(true);
            }
            if (oldTab.getId().equalsIgnoreCase(EXPECTATIONS_TAB_FX_ID)) {
                expectationsFieldCollection.destroy();
            }
            currentTabId = null;
        }
        if (newTab.getId().equalsIgnoreCase(SERVER_TAB_FX_ID)) {
            initializeServerDataEditTab();
        }
        if (newTab.getId().equalsIgnoreCase(LOGS_TAB_FX_ID)) {
            updateTheLogs(false);
        }
        if (newTab.getId().equalsIgnoreCase(EXPECTATIONS_TAB_FX_ID)) {
            initializeExpectationsDataEditTab();
        }
        currentTabId = newTab.getId();
    }

    public void serverPortSelectionChanged(int newServerPort) {
        if (currentSelectedServerPort > 0) {
            if (newServerPort == currentSelectedServerPort) {
                return;
            }
        }
        if (newServerPort > 0) {
            currentSelectedServerPort = newServerPort;
            setServerChoiceBoxColour();
            if (currentTabId.equals(EXPECTATIONS_TAB_FX_ID) && (expectationsFieldCollection != null)) {
                if (expectationsFieldCollection.destroy()) {
                    initializeExpectationsDataEditTab();
                };

            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setStatus("OK");
        configDataHasChanged = false;
        int port = initializePortChoiceBox();
        Tab tab = initializeTabsPane(0);
        initialiseLogTabPanel();
        serverPortSelectionChanged(port);
        tabSelectionChanged(tab, null);
    }

    /**
     * Call once to set up the logs tab.
     * <p>
     * Calls updateTheLogs to refresh the log content
     */
    private void initialiseLogTabPanel() {
        scrollPaneLogLines.setFitToHeight(true);
        scrollPaneLogLines.setFitToWidth(true);
        logCheckBoxesByPort = new HashMap<>();
        updateTheLogs(true);
    }

    /**
     * Call each time we update the logs.
     * initialiseLogPanelCheckBoxes ensures that the log check boxes are aligned with the config data and selected.
     * <p>
     * Set the text to the current log content
     *
     * @param clear To clear the text (not the logs)
     */
    private void updateTheLogs(boolean clear) {
        initialiseLogPanelCheckBoxes(clear);
        if (clear) {
            textAreaLogLines.setText("");
        } else {
            textAreaLogLines.setText(Main.getLogLines().get());
        }
    }

    /**
     * Called to add check boxes (one per server) to the log tab
     *
     * @param clear true to remove (reset) the panel
     */
    private void initialiseLogPanelCheckBoxes(boolean clear) {
        for (CheckBox cb : logCheckBoxesByPort.values()) {
            vBoxLogsControlList.getChildren().remove(cb);
        }
        logCheckBoxesByPort.clear();
        Main.getLogLines().clearFilter();
        if (!clear) {
            for (int p : ServerManager.ports()) {
                CheckBox cb = new CheckBox("Show:" + p);
                cb.setUserData(p);
                cb.setSelected(ServerManager.getServer(p).isShowPort());
                cb.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
                        if (!arg1.equals(arg2)) {
                            int selectedPort = (Integer) cb.getUserData();
                            ServerManager.getServer(selectedPort).setShowPort(arg2);
                            configDataHasChanged = true;
                            Main.getLogLines().filter(selectedPort, arg2);
                            Main.controllerNotification(new Notification(selectedPort, Action.UPDATE_LOG, "Log " + selectedPort + (arg2 ? "Included" : "Excluded")));
                        }
                    }
                });
                Main.getLogLines().filter(p, ServerManager.getServer(p).isShowPort());
                logCheckBoxesByPort.put(p, cb);
                vBoxLogsControlList.getChildren().add(cb);
            }
        }
    }


    /**
     * Call once to set up the TAB pane. Defined initial selection and change listeners.
     *
     * @param index
     * @return The current selection index.
     */
    private Tab initializeTabsPane(int index) {
        mainTabbedPane.getSelectionModel().select(index);
        mainTabbedPane.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                Tab tabNew = mainTabbedPane.getTabs().get(newValue.intValue());
                Tab tabOld = mainTabbedPane.getTabs().get(oldValue.intValue());
                Main.controllerNotification(new Notification(currentSelectedServerPort, Action.TAB_SELECTED, null, "Selected tab [" + tabNew.getText() + "]").withData("newTab", tabNew).withData("oldTab", tabOld));
            }
        });
        return mainTabbedPane.getTabs().get(index);
    }

    @FXML
    public void handleSettingsButton() {
        try {
            ConfigSettingsDummy configSettingsDummy = new ConfigSettingsDummy(ConfigData.getInstance());
            FXMLSettingsController settingsController = FXMLSettingsController.load(Main.getStage(), configSettingsDummy, new FXMLFieldChangeListener() {
                        @Override
                        public void changed(BeanPropertyDescription propertyDescription, String id, String message) {
                        }

                        @Override
                        public void validate(BeanPropertyDescription propertyDescription, String id, Object oldValue, Object newvalue) {
                            if (propertyDescription.isValidationId("defport")) {
                                if (ServerManager.hasPort(Util.parseInt((String) newvalue, "Should be s valid port number"))) {
                                    return;
                                }
                                throw new DataValidationException("Must be an existing server port: " + ServerManager.portList().toString());
                            } else {
                                if (propertyDescription.isValidationId("pak")) {
                                    new packaged.PackagedManager((String) newvalue);
                                }
                            }
                            return;
                        }

                        @Override
                        public void select(String id) {
                        }
                    }
            );
            boolean accept = settingsController.showAndWait();
            if (accept) {
                if (settingsController.isUpdated()) {
                    configSettingsDummy.commit();
                    configDataHasChanged = true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeExpectationsDataEditTab() {
        expectationsFieldCollection = new FXMLFieldCollection(Main.getStage(), vBoxExpectations, ServerManager.getExpectationsMap(currentSelectedServerPort), false, "%{id}:", new FXMLFieldChangeListener() {
            @Override
            public void changed(BeanPropertyDescription propertyDescription, String id, String message) {

            }

            @Override
            public void validate(BeanPropertyDescription propertyDescription, String id, Object oldValue, Object newvalue) {

            }

            @Override
            public void select(String id) {

            }
        });
    }

    /**
     * Call to set up the server data edit
     * <p>
     * Should be done each time the tab is selected.
     */
    private void initializeServerDataEditTab() {
        buttonSaveConfigChanges.setDisable(!configDataHasChanged);
        buttonReloadConfigChanges.setDisable(!configDataHasChanged);
        serverFieldCollection = new FXMLFieldCollection(Main.getStage(), vBoxServers, ServerManager.serverConfigDataMap(), false, "Server %{id}:", new FXMLFieldChangeListener() {
            @Override
            public void changed(BeanPropertyDescription propertyDescription, String id, String message) {
                if (serverFieldCollection.isError()) {
                    buttonSaveConfigChanges.setDisable(true);
                    buttonReloadConfigChanges.setDisable(false);
                } else {
                    configDataHasChanged = true;
                    buttonSaveConfigChanges.setDisable(false);
                    buttonReloadConfigChanges.setDisable(false);
                }
                setStatus(message);
            }

            @Override
            public void validate(BeanPropertyDescription propertyDescription, String id, Object oldValue, Object newValue) {
                if (propertyDescription.isValidationId("exp")) {
                    try {
                        new ServerExpectations(Util.parseInt(id, "Server port"), (String) newValue);
                    } catch (Exception e) {
                        throw new DataValidationException(e.getMessage());
                    }
                }
            }

            @Override
            public void select(String id) {
                for (Object o : serverChoiceBox.getItems()) {
                    if (o.toString().equals(id)) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                serverChoiceBox.getSelectionModel().select(o);
                            }
                        });
                        break;
                    }
                }
            }
        });

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                for (Integer p : ServerManager.ports()) {
                    serverFieldCollection.setHeadingColour(p.toString(), colorForServerState(ServerManager.getServerState(p)));
                }
            }
        });
    }

    /**
     * Call once to set up the server port drop down (ChoiceBox)
     *
     * @return the current port (first in  th eserver list).
     */
    private int initializePortChoiceBox() {
        serverChoiceBox.setItems(FXCollections.observableArrayList(ServerManager.portList()));
        for (Object s : serverChoiceBox.getItems()) {
            if (s.equals(ConfigData.getInstance().getDefaultPort())) {
                serverChoiceBox.getSelectionModel().select(s);
            }
        }
        serverChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (oldValue != newValue) {
                    Integer port = (Integer) serverChoiceBox.getItems().get(newValue.intValue());
                    Main.controllerNotification(new Notification(port, Action.SERVER_SELECTED, null, "Selected server port [" + port + "]").withData("port", port));
                }
            }
        });
        return ServerManager.portList().get(serverChoiceBox.getSelectionModel().getSelectedIndex());
    }

    public void notifyAction(Notification notification) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                switch (notification.getAction()) {
                    case CONFIG_RELOAD:
                        configDataHasChanged = false;
                        if (serverFieldCollection != null) {
                            serverFieldCollection.destroy();
                        }
                        initializeServerDataEditTab();
                        break;
                    case SERVER_SELECTED:
                        serverPortSelectionChanged((Integer) notification.getData("port"));
                        break;
                    case TAB_SELECTED:
                        tabSelectionChanged((Tab) notification.getData("newTab"), (Tab) notification.getData("oldTab"));
                        break;
                    case UPDATE_LOG:
                        updateTheLogs(false);
                        break;
                    case SERVER_STATE:
                        setServerChoiceBoxColour();
                        break;
                }
                setStatus(notification.getMessage());
            }
        });
    }

    /**
     * Set the app status text.
     * <p>
     * A ! as the first char will paint the background PINK
     *
     * @param message The status text
     */
    private void setStatus(String message) {
        if (message.startsWith("!")) {
            labelStatus.setBackground(new Background(new BackgroundFill(Color.PINK, CornerRadii.EMPTY, Insets.EMPTY)));
            labelStatus.setText(message.substring(1));
        } else {
            labelStatus.setBackground(new Background(new BackgroundFill(Color.LIGHTGREEN.brighter(), CornerRadii.EMPTY, Insets.EMPTY)));
            labelStatus.setText(message);
        }
    }

    /**
     * Call whenever a server changes status.
     * <p>
     * Updates the port drop down colour. The start stop button status and the status text
     * <p>
     * If the server data edit page is set up then sets the heading colour for that server
     */
    private void setServerChoiceBoxColour() {
        serverStateLabel.setText(ServerManager.getServer(currentSelectedServerPort).getServerState().getInfo());
        switch (ServerManager.getServer(currentSelectedServerPort).getServerState()) {
            case SERVER_STOPPED:
            case SERVER_FAIL:
                setServerDetail(currentSelectedServerPort, false, "Start");
                break;
            case SERVER_RUNNING:
                setServerDetail(currentSelectedServerPort, false, "Stop");
                break;
            default:
                setServerDetail(currentSelectedServerPort, true, "Wait");
        }
        if (serverFieldCollection != null) {
            serverFieldCollection.setHeadingColour(currentSelectedServerPort.toString(), colorForServerState(ServerManager.getServer(currentSelectedServerPort).getServerState()));
        }
    }

    private void setServerDetail(int port, boolean disabled, String text) {
        buttonStartStopServer.setDisable(disabled);
        buttonStartStopServer.setText(text);
        serverChoiceBox.setBackground(new Background(new BackgroundFill(colorForServerState(ServerManager.getServerState(port)), CornerRadii.EMPTY, Insets.EMPTY)));
    }

    private Color colorForServerState(ServerState state) {
        if (state != null) {
            switch (state) {
                case SERVER_STOPPED:
                    return Color.LIGHTGREEN;
                case SERVER_RUNNING:
                    return Color.GREENYELLOW;
                case SERVER_FAIL:
                    return Color.RED;
            }
        }
        return Color.PINK;
    }


}
