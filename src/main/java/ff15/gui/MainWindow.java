package ff15.gui;

import ff15.FF15;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Controller for the main window. It owns the controls declared in
 * {@code MainWindow.fxml} and turns each line the user sends into a pair of
 * dialog boxes: theirs, then FF15's reply.
 *
 * <p>The composer is where the window gets its voice: the hint in the empty
 * text field changes after every message, in the manner of a colleague who
 * cannot quite leave a joke alone.
 */
public class MainWindow extends AnchorPane {
    /**
     * Hints shown in the empty composer, one at a time, in this order. They
     * alternate between something Michael would say and something that actually
     * shows what to type, so the jokes never leave a new user without a clue.
     */
    private static final String[] PROMPTS = {
        "That's what she said. No wait, type a command.",
        "Try: todo read book",
        "I'm not superstitious, but I am a little stitious.",
        "Try: deadline report /by 2026-09-20",
        "Would I rather be feared or loved? Both.",
        "Try: event party /from 2026-09-20 1800 /to 2026-09-20 2200",
        "Ask me anything. Except about the Dundies.",
        "Try: list, mark 1, delete 1",
        "Sometimes I'll start a sentence and I don't even know where it's going.",
        "Try: contact add Pam /phone 91234567",
        "Bears. Beets. Battlestar Galactica. Also, tasks.",
        "Try: find book, or on 2026-09",
    };

    /** Shown once the user has said goodbye and the composer has shut. */
    private static final String FAREWELL_PROMPT = "Clocked out. Don't forget your timesheet.";

    /** How long the window lingers after goodbye, so the farewell can be read. */
    private static final Duration FAREWELL_PAUSE = Duration.seconds(1.5);

    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox dialogContainer;
    @FXML
    private TextField userInput;
    @FXML
    private Button sendButton;

    private final Image ff15Image = new Image(this.getClass().getResourceAsStream("/images/michael-scott.png"));

    private FF15 ff15;
    private int nextPrompt;

    /**
     * Wires up the behaviour the FXML cannot express: the log follows the newest
     * message, the send button only lights up when there is something to send,
     * Escape clears a half-typed line, and the cursor starts in the composer so
     * the first thing the user does can be to type.
     */
    @FXML
    public void initialize() {
        scrollPane.vvalueProperty().bind(dialogContainer.heightProperty());
        sendButton.disableProperty().bind(Bindings.createBooleanBinding(() -> userInput.getText().isBlank(),
                userInput.textProperty()));
        userInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                userInput.clear();
            }
        });
        showNextPrompt();
        Platform.runLater(userInput::requestFocus);
    }

    /** Gives the window the chatbot to send the user's lines to, and shows its greeting. */
    public void setFf15(FF15 chatbot) {
        ff15 = chatbot;
        showFf15Reply(ff15.getStartupMessage());
    }

    /**
     * Shows what the user typed, then what FF15 said back, and empties the text
     * field ready for the next line. A blank line is ignored rather than sent,
     * since Enter still fires here even while the send button is disabled.
     */
    @FXML
    private void handleUserInput() {
        String input = userInput.getText();
        if (input.isBlank()) {
            return;
        }
        dialogContainer.getChildren().add(DialogBox.getUserDialog(input));
        showFf15Reply(ff15.getResponse(input));
        userInput.clear();
        showNextPrompt();

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
        sendButton.disableProperty().unbind();
        sendButton.setDisable(true);
        userInput.setDisable(true);
        userInput.setPromptText(FAREWELL_PROMPT);

        PauseTransition farewellPause = new PauseTransition(FAREWELL_PAUSE);
        farewellPause.setOnFinished(event -> Platform.exit());
        farewellPause.play();
    }

    /** Puts the next hint in the empty composer, starting over once they run out. */
    private void showNextPrompt() {
        userInput.setPromptText(PROMPTS[nextPrompt]);
        nextPrompt = (nextPrompt + 1) % PROMPTS.length;
    }

    /** Adds one of FF15's replies to the conversation, drawn to stand out if it reported an error. */
    private void showFf15Reply(String reply) {
        DialogBox box = ff15.isLastReplyError()
                ? DialogBox.getErrorDialog(reply, ff15Image)
                : DialogBox.getFf15Dialog(reply, ff15Image);
        dialogContainer.getChildren().add(box);
    }
}
