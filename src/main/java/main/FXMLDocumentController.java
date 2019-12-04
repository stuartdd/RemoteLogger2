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
import common.ConfigData;
import common.Notification;
import common.Notifier;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import server.ServerManager;

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
    private Tab connectionsTab;

    @FXML
    private Label label;

    @FXML
    private Label serverStateLabel;

    @FXML
    private Button buttonStartStopServer;

    @FXML
    private ChoiceBox portsChoiceBox;
    @FXML
    private ChoiceBox serverChoiceBox;

    private GraphicsContext connectionCanvasGraphics;
    private GraphicsContext gc;
    /*
    Colours for each line
     */
    private Color[] colours = new Color[]{Color.RED, Color.GREEN, Color.BLUE, Color.WHITE};
    /*
    Scaling values for each line
     */
    private double[] scales = new double[]{4000, 4000, 2000, 1500};

    private int currentSelectedServerPort = -1;

    @FXML
    public void handleCloseApplicationButton() {
        Main.closeApplication();
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

    public void setserverChoiceBoxColour() {
        serverStateLabel.setText(ServerManager.getServer(currentSelectedServerPort).getServerState().getInfo());
        switch (ServerManager.getServer(currentSelectedServerPort).getServerState()) {
            case SERVER_STARTING:
            case SERVER_STOPPING:
                buttonStartStopServer.setDisable(true);
                serverChoiceBox.setBackground(new Background(new BackgroundFill(Color.PINK, CornerRadii.EMPTY, Insets.EMPTY)));
                break;
            case SERVER_STOPPED:
                buttonStartStopServer.setDisable(false);
                buttonStartStopServer.setText("Start");
                serverChoiceBox.setBackground(new Background(new BackgroundFill(Color.LIGHTGREEN, CornerRadii.EMPTY, Insets.EMPTY)));
                break;
            case SERVER_RUNNING:
                buttonStartStopServer.setDisable(false);
                buttonStartStopServer.setText("Stop");
                serverChoiceBox.setBackground(new Background(new BackgroundFill(Color.GREENYELLOW, CornerRadii.EMPTY, Insets.EMPTY)));
                break;
            case SERVER_FAIL:
                buttonStartStopServer.setDisable(false);
                buttonStartStopServer.setText("Start");
                serverChoiceBox.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
                break;
            case SERVER_PENDING:
                buttonStartStopServer.setDisable(false);
                buttonStartStopServer.setText("Start");
                serverChoiceBox.setBackground(new Background(new BackgroundFill(Color.BLUE, CornerRadii.EMPTY, Insets.EMPTY)));
                break;
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        int port = initPortChoiceBox();
        ServerManager.autoStartServers();
        serverPortSelectionChanged(port);
    }

    private int initPortChoiceBox() {
        serverChoiceBox.setItems(FXCollections.observableArrayList(ServerManager.portList()));
        serverChoiceBox.getSelectionModel().select(0);
        serverChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (oldValue != newValue) {
                    Integer port = (Integer) serverChoiceBox.getItems().get(newValue.intValue());
                    Main.forwardNotification(new Notification(port, Action.SERVER_SELECTED, null, "Selected server port [" + port + "]").withData("port", port));
                }
            }
        });
        return ServerManager.portList().get(0);
    }

    /**
     * The canvas (graph plot) is contained inside connectionsAnchorPane.
     * connectionsAnchorPane has a height and a width property that can cbe
     * listened to. When it changes we can call a method
     */
    private void initTheCanvas() {
        /*
        Add a listener to the Width property that sets the canvas to the same width
         */
        connectionsAnchorPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            /*
            Width has changed. Update the canvas width!
             */
            connectionCanvasWidth = newVal.doubleValue();
            connectionCanvas.setWidth(connectionCanvasWidth);
            connectionCanvasGraphics = connectionCanvas.getGraphicsContext2D();
        });
        /*
        Add a listener to the height property that sets the canvas to the same height
         */
        connectionsAnchorPane.heightProperty().addListener((obs, oldVal, newVal) -> {
            /*
            Height has changed. Update the canvas height!
            Note canvas starts 50 pixels from the top of the connectionsAnchorPane
             */
            connectionCanvasHeight = newVal.doubleValue() - 50;
            connectionCanvas.setLayoutY(50);
            connectionCanvas.setHeight(connectionCanvasHeight);
            connectionCanvasGraphics = connectionCanvas.getGraphicsContext2D();
        });
    }

    @Override
    public void notifyAction(Notification notification) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                switch (notification.getAction()) {
                    case SERVER_SELECTED:
                        serverPortSelectionChanged((Integer) notification.getData("port"));
                        break;
                }
                label.setText(notification.getMessage());
            }
        });
    }

    @Override
    public void log(int port, String message) {
        System.out.println(ConfigData.getInstance().timeStamp(System.currentTimeMillis()) + " [" + port + "] " + message);
    }

    @Override
    public void log(int port, Throwable throwable) {
        System.out.println(ConfigData.getInstance().timeStamp(System.currentTimeMillis()) + " [" + port + "] " + throwable.getMessage());
    }

    @Override
    public void log(int port, String message, Throwable throwable) {
        System.out.println(ConfigData.getInstance().timeStamp(System.currentTimeMillis()) + " [" + port + "] " + message + ": " + throwable.getMessage());
    }

}
