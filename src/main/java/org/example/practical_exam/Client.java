package org.example.practical_exam;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Client extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("My Chat Application");
        stage.setResizable(false);
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/org/example/practical_exam/Client.fxml"))));
        stage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
