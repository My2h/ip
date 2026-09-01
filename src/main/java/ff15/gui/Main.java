package ff15.gui;

import ff15.FF15;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * The JavaFX application behind FF15's window. JavaFX hands it a stage to fill;
 * everything the user sees is built onto that stage from here.
 *
 * <p>The window is a chat log above a composer: a scrolling {@link VBox} of
 * dialog boxes, with a text field and a send button anchored along the bottom.
 * Typed lines go to a real {@link FF15}, so the window drives the same chatbot
 * the console session does.
 */
public class Main extends Application {
    private final FF15 ff15 = new FF15();

    private final Image userImage = new Image(this.getClass().getResourceAsStream("/images/troll-face.png"));
    private final Image ff15Image = new Image(this.getClass().getResourceAsStream("/images/michael-scott.png"));

    private ScrollPane scrollPane;
    private VBox dialogContainer;
    private TextField userInput;
    private Button sendButton;

    @Override
    public void start(Stage stage) {
        dialogContainer = new VBox();

        scrollPane = new ScrollPane();
        scrollPane.setContent(dialogContainer);

        userInput = new TextField();
        sendButton = new Button("Send");

        AnchorPane mainLayout = new AnchorPane();
        mainLayout.getChildren().addAll(scrollPane, userInput, sendButton);

        stage.setScene(new Scene(mainLayout));
        stage.show();

        formatWindow(stage, mainLayout);

        sendButton.setOnMouseClicked(event -> handleUserInput());
        userInput.setOnAction(event -> handleUserInput());

        // Keep the newest message in view as the conversation grows.
        dialogContainer.heightProperty().addListener(observable -> scrollPane.setVvalue(1.0));

        showFf15Reply(ff15.getStartupMessage());
    }

    /** Sizes and positions everything in the window, now that the stage exists. */
    private void formatWindow(Stage stage, AnchorPane mainLayout) {
        stage.setTitle("FF15");
        stage.setResizable(false);
        stage.setMinHeight(600.0);
        stage.setMinWidth(400.0);

        mainLayout.setPrefSize(400.0, 600.0);

        scrollPane.setPrefSize(385, 535);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setVvalue(1.0);
        scrollPane.setFitToWidth(true);

        dialogContainer.setPrefHeight(Region.USE_COMPUTED_SIZE);

        userInput.setPrefWidth(325.0);
        sendButton.setPrefWidth(55.0);

        AnchorPane.setTopAnchor(scrollPane, 1.0);
        AnchorPane.setBottomAnchor(userInput, 1.0);
        AnchorPane.setLeftAnchor(userInput, 1.0);
        AnchorPane.setBottomAnchor(sendButton, 1.0);
        AnchorPane.setRightAnchor(sendButton, 1.0);
    }

    /**
     * Shows what the user typed, then what FF15 said back, and empties the text
     * field ready for the next line.
     */
    private void handleUserInput() {
        String input = userInput.getText();
        dialogContainer.getChildren().add(
                DialogBox.getUserDialog(new Label(input), new ImageView(userImage)));
        showFf15Reply(ff15.getResponse(input));
        userInput.clear();
    }

    /** Adds one of FF15's replies to the conversation. */
    private void showFf15Reply(String reply) {
        dialogContainer.getChildren().add(
                DialogBox.getFf15Dialog(new Label(reply), new ImageView(ff15Image)));
    }
}
