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
import common.Util;
import geom.Point;
import java.awt.AWTException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import main.dialogs.Dialogs;
import main.dialogs.FXMLYesNoCancelDialog;
import server.ServerConfig;
import server.ServerManager;

/**
 *
 */
public class Main extends Application {

    private static Stage mainStage;
    private static Scene mainScene;
    private static FXMLDocumentController controller;
    private final static LogLines LOG_LINES = new LogLines(100);
    private final static UiNotifier UI_NOTIFIER = new UiNotifier();

    private static String configFileName;

    public static LogLines getLogLines() {
        return LOG_LINES;
    }

    static boolean controllerNotification(Notification notification) {
        if (waitForController(1000)) {
            switch (notification.getAction()) {
                case RELOAD_RESTART_SERVERS:
                    ConfigData.load("config.json");
                    controller.notifyAction(new Notification(notification.getPort(), Action.CONFIG_RELOAD, null, "Reloaded Config Data"));
                case RESTART_SERVERS:
                    if (initServers(2000)) {
                        controller.notifyAction(notification);
                    } else {
                        controller.notifyAction(new Notification(notification.getPort(), Action.ERROR, null, "Failed to Re-Start servers!"));
                    }
                    break;
                case START_STOP_SERVER:
                    if (ServerManager.isServerRunning(notification.getPort())) {
                        ServerManager.stopServer(notification.getPort());
                    } else {
                        ServerManager.startServer(notification.getPort());
                    }
                default:
                    controller.notifyAction(notification);
            }
        } else {
            return false;
        }
        return true;
    }

    public static Stage getStage() {
        return mainStage;
    }

    /**
     * Start the application.
     *
     * We store stage and scene for later so we can get size and position data
     * from them.
     *
     * @param stage The Stage generated by JavaFX
     *
     * @throws Exception
     */
    @Override
    public void start(Stage stage) throws Exception {
        /*
        Save the reference to the stage
         */
        mainStage = stage;
        /*
        Set what happens when an 'application close' event is triggered.
         */
        mainStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                /*
                Clean up and exit the application
                 */
                closeApplication(0);
            }
        });
        /*
        Use the loader to load the window controls
         */
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXMLDocument.fxml"));
        Parent root = loader.load();

        stage.setX(ConfigData.getInstance().getX());
        stage.setY(ConfigData.getInstance().getY());
        stage.setHeight(ConfigData.getInstance().getHeight());
        stage.setWidth(ConfigData.getInstance().getWidth());

        /*
        Save a reference to the controller for later.
        This is so the serial monitor can pass action messages to it.
         */
        controller = loader.getController();
        UI_NOTIFIER.setNotifier(controller);
        LOG_LINES.setNotifier(UI_NOTIFIER);
        /*
        Save a reference to the scene for later.
         */
        mainScene = new Scene(root);

        stage.setScene(mainScene);
        stage.show();
    }

    /**
     * Close the application.The serial port monitor should be closed
     * properly.Then the Platform (JavaFX) must be told to exit
     *
     * Then we terminate the Java VM
     *
     * @param returnCode
     */
    public static void closeApplication(int returnCode) {
        if (controller != null) {
            FXMLYesNoCancelDialog.RESP resp = FXMLYesNoCancelDialog.load(controller.getConfigChanges(), mainStage, "Exit Application", controller.hasConfigDataHasChanged()).showAndWait();
            switch (resp) {
                case NO:
                    loadConfig();
                case YES:
                    if (ConfigData.canWriteToFile()) {
                        ConfigData.getInstance().setX(mainStage.getX());
                        ConfigData.getInstance().setY(mainStage.getY());
                        ConfigData.getInstance().setWidth(mainStage.getWidth());
                        ConfigData.getInstance().setHeight(mainStage.getHeight());
                        try {
                            ConfigData.store();
                        } catch (IOException ex) {
                            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                            returnCode = 1;
                        }
                    }
                    break;
                case CANCEL:
                    return;
            }
        }
        stopAndExit(returnCode);
    }

    private static void stopAndExit(int returnCode) {
        if (!stopServers(5000)) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Failed to stop servers. Time out!");
            returnCode = 1;
        }
        Platform.exit();
        System.exit(returnCode);
    }

    /**
     * @param args the command line arguments
     *
     * Currently just the config file name
     */
    public static void main(String[] args) throws AWTException {
        configFileName = "config.json";
        /*
        Check we have a config file name. Abort if not.
         */
        if (args.length == 0) {
            exitWithHelp("Requires a properties (configuration) file");
        }
        loadConfig();
        if (initServers(2000)) {
            launch(args);
        } else {
            System.err.println("ERROR Failed to start servers. Timed out!");
            Platform.exit();
            System.exit(1);
        }
    }

    /**
     * This method returns the rectangle that is the effective application
     * window (inside the borders)
     *
     * The values are screen coordinates not application coordinates
     *
     * @return a rectangle
     */
    public static Point getPoint() {
        return new Point((int) mainStage.getX(), (int) mainStage.getY());
    }

    /**
     * Print an error message and exit the app with help.
     *
     * @param m The error message
     */
    public static void exitWithHelp(String m) {
        System.out.println("Error:" + m + "\n"
                + "Application requires the following parameters:\n"
                + "  The name of a json configuration file!");
        System.exit(1);
    }

    private static boolean waitForController(long timeOut) {
        long timeToGiveUp = System.currentTimeMillis() + timeOut;
        while ((controller == null) && (System.currentTimeMillis() < timeToGiveUp)) {
            Util.sleep(100);
        }
        return (System.currentTimeMillis() < timeToGiveUp);
    }

    private static boolean stopServers(long timeOut) {
        long timeToGiveUp = System.currentTimeMillis() + timeOut;
        ServerManager.stopAllServers();
        while ((ServerManager.countServersRunning() > 0) && (System.currentTimeMillis() < timeToGiveUp)) {
            Util.sleep(100);
        }
        return (System.currentTimeMillis() < timeToGiveUp);
    }

    private static boolean initServers(long timeOut) {
        if (!stopServers(timeOut)) {
            return false;
        }
        ServerManager.clear();
        for (Map.Entry<String, ServerConfig> sc : ConfigData.getInstance().getServers().entrySet()) {
            ServerManager.addServer(sc.getKey(), sc.getValue(), UI_NOTIFIER, LOG_LINES);
        }
        ServerManager.autoStartServers();
        return true;
    }

    private static void loadConfig() {
        ConfigData.load(configFileName);
    }
}
