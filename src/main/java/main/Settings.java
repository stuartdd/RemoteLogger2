package main;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import main.fields.FXMLBeanFieldLoaderException;

import java.io.IOException;

public class Settings {
    private static Stage stage;

    public Settings() {
    }

     public static void load() throws IOException {
         Settings controller = new Settings();

         // Inflate FXML
         FXMLLoader loader = new FXMLLoader(Settings.class.getResource("controller/login/Login.fxml"));
         loader.setController(controller);
         Parent root = loader.load();
         // Create scene
         stage = new Stage();
         Scene scene = new Scene(root);
         stage.setScene(scene);
     }
}
