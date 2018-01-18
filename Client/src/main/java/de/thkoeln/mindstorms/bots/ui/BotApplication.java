package de.thkoeln.mindstorms.bots.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * BotApplication
 */
public class BotApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(Objects.requireNonNull(FXMLLoader.getDefaultClassLoader().getResource("fxml/bot_ui.fxml")));
        Parent root = fxmlLoader.load();
        Controller controller = fxmlLoader.getController();
        controller.setController(Integer.parseInt(getParameters().getRaw().get(0)));
        primaryStage.setTitle("KI Praktikum");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
}
