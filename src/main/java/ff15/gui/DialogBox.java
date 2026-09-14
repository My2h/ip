package ff15.gui;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

/**
 * One turn of the conversation. The two sides are deliberately not symmetric:
 * this is a chat between a person and a program, not between two people, so
 * the user's lines sit on the right as plain bubbles with no picture, while
 * FF15's replies sit on the left beside its face, the way a messaging app
 * shows the other party.
 *
 * <p>The box is its own root and its own controller, so one FXML file describes
 * every dialog box in the window.
 */
public class DialogBox extends HBox {
    @FXML
    private Label dialog;
    @FXML
    private ImageView displayPicture;

    private DialogBox(String text) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainWindow.class.getResource("/view/DialogBox.fxml"));
            fxmlLoader.setController(this);
            fxmlLoader.setRoot(this);
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        dialog.setText(text);
    }

    /** Returns a dialog box for something the user typed: right-aligned, with no picture. */
    public static DialogBox getUserDialog(String text) {
        DialogBox box = new DialogBox(text);
        box.getChildren().remove(box.displayPicture);
        box.setAlignment(Pos.TOP_RIGHT);
        box.getStyleClass().add("user-box");
        box.dialog.getStyleClass().add("user-label");
        return box;
    }

    /** Returns a dialog box for something FF15 replied: left-aligned, beside its face. */
    public static DialogBox getFf15Dialog(String text, Image image) {
        DialogBox box = new DialogBox(text);
        box.displayPicture.setImage(image);
        box.getChildren().setAll(box.displayPicture, box.dialog);
        box.setAlignment(Pos.TOP_LEFT);
        box.getStyleClass().add("reply-box");
        box.dialog.getStyleClass().add("reply-label");
        return box;
    }
}
