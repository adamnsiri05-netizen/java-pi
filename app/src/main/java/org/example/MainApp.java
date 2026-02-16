package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org.example/main_view.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        // Load CSS stylesheet programmatically (resource in same package)
        String css = getClass().getResource("/org.example/styles.css").toExternalForm();
        scene.getStylesheets().add(css);
        stage.setTitle("Messagerie");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
