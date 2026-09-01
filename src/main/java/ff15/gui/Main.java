package ff15.gui;

import java.io.IOException;

import ff15.FF15;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * The JavaFX application behind FF15's window. JavaFX hands it a stage to fill;
 * this class loads the window described in {@code MainWindow.fxml} onto that
 * stage and hands its controller a real {@link FF15} to talk to, so the window
 * drives the same chatbot the console session does.
 */
public class Main extends Application {
    private final FF15 ff15 = new FF15();

    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/view/MainWindow.fxml"));
            AnchorPane root = fxmlLoader.load();

            stage.setScene(new Scene(root));
            stage.setTitle("FF15");
            stage.setMinHeight(220);
            stage.setMinWidth(417);

            fxmlLoader.<MainWindow>getController().setFf15(ff15);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
