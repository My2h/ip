package ff15.gui;

import ff15.FF15;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Controller for the main window. It owns the controls declared in
 * {@code MainWindow.fxml} and turns each line the user sends into a pair of
 * dialog boxes: theirs, then FF15's reply.
 */
public class MainWindow extends AnchorPane {
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox dialogContainer;
    @FXML
    private TextField userInput;
    @FXML
    private Button sendButton;

    private final Image userImage = new Image(this.getClass().getResourceAsStream("/images/troll-face.png"));
    private final Image ff15Image = new Image(this.getClass().getResourceAsStream("/images/michael-scott.png"));

    private FF15 ff15;

    /** Keeps the newest message in view as the conversation grows past the window. */
    @FXML
    public void initialize() {
        scrollPane.vvalueProperty().bind(dialogContainer.heightProperty());
    }

    /** Gives the window the chatbot to send the user's lines to, and shows its greeting. */
    public void setFf15(FF15 chatbot) {
        ff15 = chatbot;
        showFf15Reply(ff15.getStartupMessage());
    }

    /**
     * Shows what the user typed, then what FF15 said back, and empties the text
     * field ready for the next line.
     */
    @FXML
    private void handleUserInput() {
        String input = userInput.getText();
        dialogContainer.getChildren().add(DialogBox.getUserDialog(input, userImage));
        showFf15Reply(ff15.getResponse(input));
        userInput.clear();

        if (ff15.isFinished()) {
            endSession();
        }
    }

    /**
     * Closes the window once the user has said goodbye. The controls are disabled
     * straight away so nothing more can be typed, but the window lingers for a
     * moment first, otherwise it vanishes before the farewell can be read.
     */
    private void endSession() {
        userInput.setDisable(true);
        sendButton.setDisable(true);

        PauseTransition farewellPause = new PauseTransition(Duration.seconds(1.5));
        farewellPause.setOnFinished(event -> Platform.exit());
        farewellPause.play();
    }

    /** Adds one of FF15's replies to the conversation. */
    private void showFf15Reply(String reply) {
        dialogContainer.getChildren().add(DialogBox.getFf15Dialog(reply, ff15Image));
    }
}
