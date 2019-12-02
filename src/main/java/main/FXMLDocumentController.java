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
import java.util.Timer;

/**
 *
 */
public class FXMLDocumentController implements Initializable {

    @FXML
    private Canvas canvas1;
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
    private ChoiceBox portsChoiceBox;

    private GraphicsContext connectionCanvasGraphics;
    private GraphicsContext gc;
    private Timer displayTimer = new Timer();
    /*
    Colours for each line
     */
    private Color[] colours = new Color[]{Color.RED, Color.GREEN, Color.BLUE, Color.WHITE};
    /*
    Scaling values for each line
     */
    private double[] scales = new double[]{4000, 4000, 2000, 1500};


    /*
    Every message read from the serial port is sent here.

     We create a Reading object. If get errors from reading it the Reading object returned is null.

     We record the time differenct for each message to give us the latency for the reading
     */
    public boolean messageReceived(String message) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
            }
        });
        /*
        return false to stop the serial port reader!
         */
        return true;
    }

    /*
    Set the status text in a JFX Thread
     */
    public boolean setStatus(String message) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                label.setText(message);
            }
        });
        return true;
    }


    @Override
    public void initialize(URL url, ResourceBundle rb) {
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

}
