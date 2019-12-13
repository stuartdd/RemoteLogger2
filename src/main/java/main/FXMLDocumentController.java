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

import common.Action;
import common.LogLine;
import common.Notification;
import common.Notifier;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import main.fields.BeanPropertyDescription;
import main.fields.FXMLFieldChangeListener;
import main.fields.FXMLFieldCollection;
import server.ServerManager;
import server.ServerState;

/**
 *
 */
public class FXMLDocumentController implements Initializable, Notifier {

    @FXML
    private AnchorPane connectionsAnchorPane;
    @FXML
    private Canvas connectionCanvas;
    private double connectionCanvasWidth;
    private double connectionCanvasHeight;
    @FXML
    private TabPane mainTabbedPane;

    @FXML
    private VBox vBoxConnections;
    private FXMLFieldCollection connectionsFieldCollection;

    @FXML
    private Label label;

    @FXML
    private Label serverStateLabel;

    @FXML
    private Button buttonStartStopServer;
    @FXML
    private Button buttonSaveConfigChanges;
    @FXML
    private Button buttonReloadConfigChanges;

    @FXML
    private ChoiceBox serverChoiceBox;

    private int currentSelectedServerPort = -1;
    private boolean configDataHasChanged = false;

    @FXML
    public void handleCloseApplicationButton() {
        Main.closeApplication(0,configDataHasChanged);
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
            if (oldTab.getId().equalsIgnoreCase("connections")) {
                connectionsFieldCollection.destroy();
            }
            /*
            If any changes to state then save them to disk
             */
            System.out.println("CLOSE " + oldTab.getText());
        }
        if (newTab.getId().equalsIgnoreCase("connections")) {
            initializeConnectionDataTab();
        }

        System.out.println("NEW " + newTab.getText());
    }

    public void serverPortSelectionChanged(int newServerPort) {
        if (currentSelectedServerPort > 0) {
            if (newServerPort == currentSelectedServerPort) {
                return;
            }
        }
        /*
        If any changes to state then save them to disk
         */
        if (newServerPort > 0) {
            currentSelectedServerPort = newServerPort;
            setserverChoiceBoxColour();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configDataHasChanged = false;
        int port = initializePortChoiceBox();
        Tab tab = initializeTabPanel(0);
        serverPortSelectionChanged(port);
        tabSelectionChanged(tab, null);
    }

    private Tab initializeTabPanel(int index) {
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

    private void initializeConnectionDataTab() {
        buttonSaveConfigChanges.setDisable(!configDataHasChanged);
        buttonReloadConfigChanges.setDisable(!configDataHasChanged);
        connectionsFieldCollection = new FXMLFieldCollection(vBoxConnections, ServerManager.serverConfigData(), false, "Server %{id}:", new FXMLFieldChangeListener() {
            @Override
            public void changed(BeanPropertyDescription propertyDescription, boolean error, String message) {
                if (error) {
                    buttonSaveConfigChanges.setDisable(true);
                    buttonReloadConfigChanges.setDisable(true);
                } else {
                    configDataHasChanged = true;
                    buttonSaveConfigChanges.setDisable(!configDataHasChanged);
                    buttonReloadConfigChanges.setDisable(!configDataHasChanged);
                }
                label.setText(message);
            }

            @Override
            public void select(String id) {
                for (Object o:serverChoiceBox.getItems()) {
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
                for (int p: ServerManager.ports()) {
                    connectionsFieldCollection.setHeadingColour(""+p, colorForServerState(ServerManager.getServerState(p)));
                }               
            }
        });

    }

    private int initializePortChoiceBox() {
        serverChoiceBox.setItems(FXCollections.observableArrayList(ServerManager.portList()));
        serverChoiceBox.getSelectionModel().select(0);
        serverChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (oldValue != newValue) {
                    Integer port = (Integer) serverChoiceBox.getItems().get(newValue.intValue());
                    Main.controllerNotification(new Notification(port, Action.SERVER_SELECTED, null, "Selected server port [" + port + "]").withData("port", port));
                }
            }
        });
        return ServerManager.portList().get(0);
    }

    @Override
    public void notifyAction(Notification notification) {
        System.out.println(notification.toString());
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                switch (notification.getAction()) {
                    case CONFIG_RELOAD:
                        configDataHasChanged = false;
                        if (connectionsFieldCollection != null) {
                            connectionsFieldCollection.destroy();
                        }
                        initializeConnectionDataTab();
                        break;
                    case SERVER_SELECTED:
                        serverPortSelectionChanged((Integer) notification.getData("port"));
                        break;
                    case TAB_SELECTED:
                        tabSelectionChanged((Tab) notification.getData("newTab"), (Tab) notification.getData("oldTab"));
                        break;
                    case SERVER_STATE:
                        setserverChoiceBoxColour();
                        if (connectionsFieldCollection!=null) {
                            connectionsFieldCollection.setHeadingColour(""+notification.getPort(), colorForServerState((ServerState)notification.getData("state")));            
                        }
                        break;
                }
                label.setText(notification.getMessage());
            }
        });
    }

    @Override
    public void log(LogLine ll) {
        System.out.println(ll.toString());
    }

    private void setserverChoiceBoxColour() {
        serverStateLabel.setText(ServerManager.getServer(currentSelectedServerPort).getServerState().getInfo());
        switch (ServerManager.getServer(currentSelectedServerPort).getServerState()) {
            case SERVER_STOPPED:
                setServerDetail(currentSelectedServerPort, false, "Start");
                break;
            case SERVER_RUNNING:
                setServerDetail(currentSelectedServerPort, false, "Stop");
                break;
            case SERVER_FAIL:
                setServerDetail(currentSelectedServerPort, false, "Start");
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


}
