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
import common.LogLine;
import common.Notification;
import common.Notifier;
import common.Util;
import geom.Rect;
import java.awt.AWTException;
import java.io.IOException;
import java.util.Map;
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
import server.ServerConfig;
import server.ServerManager;

/**
 *
 */
public class Main extends Application implements Notifier {

    private static Stage mainStage;
    private static Scene mainScene;
    private static FXMLDocumentController controller;
    private static String configFileName;

    static void forwardNotification(Notification notification) {
        int count = 10;
        while ((controller == null) && (count > 0)) {
            Util.sleep(100);
            count--;
        }
        if (count == 0) {
            return;
        }
        switch (notification.getAction()) {
            case RELOAD_RESTART_SERVERS:
                ConfigData.load("config.json");
                controller.notifyAction(new Notification(notification.getPort(), Action.CONFIG_RELOAD, null, "Reloaded Config Data"));
            case RESTART_SERVERS:
                if (!stopServers(5000)) {
                    controller.notifyAction(new Notification(notification.getPort(), Action.ERROR, null, "Failed to stop ALL servers!"));
                } else {
                    initServers();
                    controller.notifyAction(notification);
                }
                break;
            default:
                controller.notifyAction(notification);
        }
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
                closeApplication();
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

        /*
        Save a reference to the scene for later.
         */
        mainScene = new Scene(root);

//        stage.setTitle(ConfigData.getValue("app.name", "Main"));
        stage.setScene(mainScene);
        stage.show();
    }

    /**
     * Close the application.
     *
     * The serial port monitor should be closed properly.
     *
     * Then the Platform (JavaFX) must be told to exit
     *
     * Then we terminate the Java VM
     */
    public static void closeApplication() {
        if (ConfigData.canWriteToFile()) {
            ConfigData.getInstance().setX(mainStage.getX());
            ConfigData.getInstance().setY(mainStage.getY());
            ConfigData.getInstance().setWidth(mainStage.getWidth());
            ConfigData.getInstance().setHeight(mainStage.getHeight());
            try {
                ConfigData.store();
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (!stopServers(5000)) {
            System.out.println("Failed to stop ALL servers. Waited 5 seconds!");
        }
        Platform.exit();
        System.exit(0);
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
        initServers();

        launch(args);
        /*
        Load it and abort is an error occurs
         */

    }

    /**
     * This method returns the rectangle that is the effective application
     * window (inside the borders)
     *
     * The values are screen coordinates not application coordinates
     *
     * @return a rectangle
     */
    public static Rect getRectangle() {
        double x = mainStage.getX() + mainScene.getX();
        double y = mainStage.getY() + mainScene.getY();
        return new Rect((int) x, (int) y, (int) mainScene.getWidth(), (int) mainScene.getHeight());
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

    @Override
    public void notifyAction(Notification notification) {
        if (controller != null) {
            controller.notifyAction(notification);
        } else {
            System.out.println(ConfigData.getInstance().timeStamp(notification.getTime()) + " [" + notification.getPort() + "] [" + notification.getAction().toString() + "] " + notification.getMessage());
        }
    }

    @Override
    public void log(int port, String message) {
        System.out.println("+++++ " + message);
        LogLines.add(new LogLine(port, message));
    }

    @Override
    public void log(int port, Throwable throwable) {
        System.out.println("+++++ " + throwable.getMessage());
        LogLines.add(new LogLine(port, null, throwable));
    }

    @Override
    public void log(int port, String message, Throwable throwable) {
        System.out.println("+++++ " + message + ": " + throwable.getMessage());
        LogLines.add(new LogLine(port, message, throwable));
    }

    private static boolean stopServers(long timeOut) {
        long timeToGiveUp = System.currentTimeMillis() + timeOut;
        ServerManager.stopAllServers();
        while ((ServerManager.countServersRunning() > 0) && (System.currentTimeMillis() < timeToGiveUp)) {
            Util.sleep(50);
        }
        return (System.currentTimeMillis() < timeToGiveUp);
    }

    private static void initServers() {
        ServerManager.clear();
        for (Map.Entry<String, ServerConfig> sc : ConfigData.getInstance().getServers().entrySet()) {
            ServerManager.addServer(sc.getKey(), sc.getValue(), new Main());
        }
        ServerManager.autoStartServers();
    }

    private static void loadConfig() {
        ConfigData.load(configFileName);

    }
}
