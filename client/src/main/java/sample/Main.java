package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{

        AnchorPane pane;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/authWindow.fxml"));
        loader.setController(new AuthController(primaryStage));
        pane = loader.load();
        // Create a scene and place it in the stage
        Scene scene = new Scene(pane);
        primaryStage.setTitle("ToDoshki - Вход"); // Set the stage title
        primaryStage.setResizable(false);
        primaryStage.setScene(scene); // Place the scene in the stage
        primaryStage.show(); // Display the stage
    }


    public static void main(String[] args) {
        launch(args);
    }
}
