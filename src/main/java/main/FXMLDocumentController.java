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

import client.Client11;
import common.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import main.dialogs.FXMLSettingsDialog;
import main.dialogs.SimpleDialogs;
import main.fields.BeanProperty;
import main.fields.FXMLFieldChangeListener;
import main.fields.FXMLFieldCollection;
import packaged.PackagedManager;
import packaged.PackagedRequest;
import server.ServerConfig;
import server.ServerExpectations;
import server.ServerManager;
import server.ServerState;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

/**
 *
 */
public class FXMLDocumentController implements Initializable, Notifier {

    private static final String LOGS_TAB_FX_ID = "logs";
    private static final String HTTP_CLIENT_TAB_FX_ID = "httpClient";

    @FXML
    private TabPane mainTabbedPane;

    @FXML
    private VBox vBoxLogsControlList;

    @FXML
    private Label labelStatus;

    @FXML
    private Label serverStateLabel;

    @FXML
    private ListView clientSplitPaneNamesList;
    private ClientSplitPaneNamesListListener clientSplitPaneNamesListListener = new ClientSplitPaneNamesListListener();
    @FXML
    private ScrollPane scrollPaneLogLines;

    @FXML
    private ScrollPane clientSplitPaneScrollLeft;

    @FXML
    private ScrollPane clientSplitPaneScrollRight;
    FXMLFieldCollection packageRequestFieldCollection;

    @FXML
    private VBox clientSplitPaneVBoxRight;

    @FXML
    private TextArea textAreaLogLines;

    @FXML
    private Button buttonStartStopServer;

    @FXML
    private ChoiceBox serverChoiceBox;
    private ChoiceBoxPortSelectionListener serverChoiceBoxListener;

    private Integer currentSelectedServerPort = -1;
    private PackagedRequest currentPackagedRequest;
    private boolean configDataHasChanged = false;
    private Map<Integer, CheckBox> logCheckBoxesByPort = new HashMap<>();
    private Map<String, Object> configChangesLog = new TreeMap<>();

    @FXML
    public void handleClearLogsButton() {
        if (SimpleDialogs.alertOkCancel(Main.getPoint(), "Clear logs", "Erase all log data!", "Press OK to continue")) {
            Main.getLogLines().clear();
            Main.controllerNotification(new Notification(-1, Action.UPDATE_LOG, "Log  Cleared"));
        }
    }

    @FXML
    public void handleCloseApplicationButton() {
        Main.closeApplication(0);
    }

    @FXML
    public void handleClientRequestEditButton() {
        System.out.println("EDIT");
    }

    @FXML
    public void handleClientRequestSendButton() {
        if (currentPackagedRequest != null) {
            mainTabbedPane.getSelectionModel().select(getTabIndexForId(LOGS_TAB_FX_ID));
            Client11.send(
                    currentPackagedRequest.getName(),
                    currentPackagedRequest.getHost(),
                    currentPackagedRequest.getPort(),
                    currentPackagedRequest.getPath(),
                    currentPackagedRequest.getMethod(),
                    currentPackagedRequest.getBodyFinal(null),
                    currentPackagedRequest.getHeaders(),
                    currentPackagedRequest.getTimeoutSeconds(),
                    Main.getLogLines()
            );
        }
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

    @FXML
    public void handleServersButton() {
        FXMLSettingsDialog settingsController = FXMLSettingsDialog.load(Main.getStage(), ServerManager.serverConfigDataMap(), "Server %{id}:", "Basic Server Settings", "Server", new FXMLFieldChangeListener() {
            @Override
            public void changed(BeanProperty propertyDescription, String id, String message) {
            }

            @Override
            public void validate(BeanProperty propertyDescription, String id, Object oldValue, Object newValue) {
                if (propertyDescription.isValidationId("exp")) {
                    new ServerExpectations((String) newValue, null);
                }
            }

            @Override
            public void remove(Object newValue) {
                int port = Integer.parseInt(newValue.toString());
                if (!ServerManager.getServerState(port).equals(ServerState.SERVER_STOPPED)) {
                    throw new DataValidationException("!Cannot remove a Server that is running");
                }
            }

            @Override
            public Object add(String cloneId, String toId, String entityName) {
                int portNumber;
                try {
                    portNumber = Integer.parseInt(toId);
                } catch (Exception e) {
                    throw new DataValidationException("!" + entityName + " 'id' must be a valid integer port number");
                }
                if (ServerManager.hasPort(portNumber)) {
                    throw new DataValidationException("!" + entityName + " 'id' must be a 'Unique' port number");
                }
                ServerConfig sc = ConfigData.getInstance().serverWithDefaultConfig();
                FXMLSettingsDialog newController = FXMLSettingsDialog.load(Main.getStage(), sc, "Server " + portNumber + ":", "New Server Settings", "Server", this);
                if (newController.showAndWait()) {
                    return sc;
                }
                return null;
            }

        });
        boolean accept = settingsController.showAndWait();
        if (accept) {
            int count = settingsController.updateAllValues(configChangesLog, "SERVER:  ");
            for (String id : settingsController.getRemovedIds()) {
                ServerManager.removeServer(id);
                ConfigData.getInstance().getServers().remove(id);
                initializePortChoiceBox();
                configChangesLog.put("SERVER:  [" + id + "]:REMOVED", "Server with port [" + id + "]");
                configDataHasChanged = true;
            }
            for (Map.Entry<String, PropertyDataWithAnnotations> e : settingsController.getAddedIds().entrySet()) {
                ConfigData.getInstance().getServers().put(e.getKey(), (ServerConfig) e.getValue());
                Main.addServers(Integer.parseInt(e.getKey()), (ServerConfig) e.getValue());
                initializePortChoiceBox();
                configChangesLog.put("SERVER:  [" + e.getKey() + "]:ADDED", "Server with port [" + e.getKey() + "]");
                configDataHasChanged = true;
            }
            if (count > 0) {
                configDataHasChanged = true;
                initialiseLogPanelCheckBoxes(false);
                updateTheLogs(false);
                setStatus("[ " + count + " ] Change(s) have been applied");
            } else {
                setStatus("No changes were made");
            }
        } else {
            setStatus("Changes cancelled");
        }
    }

    @FXML
    public void handleSettingsButton() {
        FXMLSettingsDialog settingsController = FXMLSettingsDialog.load(Main.getStage(), ConfigData.getInstance(), "Settings", "Application Setting", "Setting", new FXMLFieldChangeListener() {
            @Override
            public void changed(BeanProperty propertyDescription, String id, String message) {
            }

            @Override
            public void validate(BeanProperty propertyDescription, String id, Object oldValue, Object newvalue) {
                if (propertyDescription.isValidationId("defport")) {
                    if (ServerManager.hasPort(Util.parseInt((String) newvalue, "Should be a valid port number"))) {
                        return;
                    }
                    throw new DataValidationException("Must be an existing server port: " + ServerManager.portList().toString());
                }
                if (propertyDescription.isValidationId("pak")) {
                    PackagedManager.packagedManagerLoad((String) newvalue);
                }
                return;
            }

            @Override
            public void remove(Object newValue) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Object add(String cloneId, String toId, String EntityName) {
                return null;
            }
        });
        boolean accept = settingsController.showAndWait();
        if (accept) {
            int count = settingsController.updateAllValues(configChangesLog, "SETTINGS:");
            if (count > 0) {
                configDataHasChanged = true;
                setStatus("[ " + count + " ] Change(s) have been applied");
            } else {
                setStatus("No changes were made");
            }
        } else {
            setStatus("Changes cancelled");
        }
    }

    public boolean hasConfigDataHasChanged() {
        return configDataHasChanged;
    }

    public void tabSelectionChanged(Tab newTab, Tab oldTab) {
        if (oldTab != null) {
            if (newTab == oldTab) {
                return;
            }
            if (oldTab.getId().equalsIgnoreCase(LOGS_TAB_FX_ID)) {
                updateTheLogs(true);
            }
        }
        if (newTab.getId().equalsIgnoreCase(LOGS_TAB_FX_ID)) {
            updateTheLogs(false);
        }
        if (newTab.getId().equalsIgnoreCase(HTTP_CLIENT_TAB_FX_ID)) {
            initialiseClientTabPanel();
        }
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
     * Call once to set up the logs tab.
     * <p>
     * Calls updateTheLogs to refresh the log content
     */
    private void initialiseClientTabPanel() {
        clientSplitPaneNamesList.getSelectionModel().selectedItemProperty().removeListener(clientSplitPaneNamesListListener);
        clientSplitPaneScrollLeft.setFitToHeight(true);
        clientSplitPaneScrollLeft.setFitToWidth(true);
        clientSplitPaneScrollRight.setFitToHeight(true);
        clientSplitPaneScrollRight.setFitToWidth(true);
        clientSplitPaneNamesList.setItems(FXCollections.observableArrayList(PackagedManager.getRequestNamesList()));
        if (currentPackagedRequest != null) {
            clientSplitPaneNamesList.getSelectionModel().select(currentPackagedRequest.getName());
            initialiseClientDetailDisplay(currentPackagedRequest.getName());
        } else {
            clientSplitPaneNamesList.getSelectionModel().select(0);
            initialiseClientDetailDisplay(clientSplitPaneNamesList.getSelectionModel().getSelectedItem().toString());
        }
        clientSplitPaneNamesList.getSelectionModel().selectedItemProperty().addListener(clientSplitPaneNamesListListener);
    }

    public void initialiseClientDetailDisplay(String name) {
        if (packageRequestFieldCollection != null) {
            packageRequestFieldCollection.destroy();
        }
        currentPackagedRequest = PackagedManager.getPackagedRequest(name);
        Map<String, PropertyDataWithAnnotations> m = new HashMap<>();
        m.put(name, currentPackagedRequest);
        packageRequestFieldCollection = new FXMLFieldCollection(Main.getStage(), clientSplitPaneVBoxRight, m, true, name, "Packaged Request", null);
    }

    /**
     * Call each time we update the logs. initialiseLogPanelCheckBoxes ensures
     * that the log check boxes are aligned with the config data and selected.
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
     * Call once to set up the TAB pane. Defined initial selection and change
     * listeners.
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

    /**
     * Call once to set up the server port drop down (ChoiceBox)
     *
     * @return the current port (first in the server list).
     */
    private int initializePortChoiceBox() {
        if (serverChoiceBoxListener != null) {
            serverChoiceBox.getSelectionModel().selectedIndexProperty().removeListener(serverChoiceBoxListener);
        } else {
            serverChoiceBoxListener = new ChoiceBoxPortSelectionListener();
        }

        serverChoiceBox.setItems(FXCollections.observableArrayList(ServerManager.portList()));
        for (Object s : serverChoiceBox.getItems()) {
            if (s.equals(ConfigData.getInstance().getDefaultPort())) {
                serverChoiceBox.getSelectionModel().select(s);
            }
        }
        serverChoiceBox.getSelectionModel().selectedIndexProperty().addListener(serverChoiceBoxListener);
        return ServerManager.portList().get(serverChoiceBox.getSelectionModel().getSelectedIndex());
    }

    public void notifyAction(Notification notification) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                switch (notification.getAction()) {
                    case PACKAGE_REQUEST_SELECTED:
                        initialiseClientDetailDisplay((String) notification.getData("name"));
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
     * Updates the port drop down colour. The start stop button status and the
     * status text
     * <p>
     * If the server data edit page is set up then sets the heading colour for
     * that server
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

    private int getTabIndexForId(String id) {
        int index = 0;
        for (Tab t : mainTabbedPane.getTabs()) {
            if (t.getId().equals(id)) {
                return index;
            }
            index++;
        }
        return 0;
    }

    public Map<String, Object> getConfigChangesLog(Map<String, Object> prefix) {
        for (Map.Entry<String, Object> s : configChangesLog.entrySet()) {
            prefix.put(s.getKey(), s.getValue());
        }
        return prefix;
    }

    private class ChoiceBoxPortSelectionListener implements ChangeListener<Number> {

        @Override
        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
            if (oldValue != newValue) {
                Integer port = (Integer) serverChoiceBox.getItems().get(newValue.intValue());
                Main.controllerNotification(new Notification(port, Action.SERVER_SELECTED, null, "Selected server port [" + port + "]").withData("port", port));
            }
        }
    }

    private class ClientSplitPaneNamesListListener implements ChangeListener<String> {
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            Main.controllerNotification(new Notification(-1, Action.PACKAGE_REQUEST_SELECTED, null, "Selected server port [" + newValue + "]").withData("name", newValue));
        }
    }
}
