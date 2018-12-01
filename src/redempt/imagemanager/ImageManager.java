package redempt.imagemanager;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class ImageManager extends Application {
	
	public static Path folder;
	public static Path tags;
	private byte[] key;
	
	public static void main(String[] args) {
		try {
			folder = Paths.get(ImageManager.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent().resolve("data");
			tags = folder.resolve("tags.dat");
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		launch();
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		if (!Files.exists(folder)) {
			Files.createDirectories(folder);
		}
		stage.setHeight(250);
		stage.setWidth(400);
		stage.setResizable(false);
		stage.setTitle("Image Manager");
		GridPane pane = new GridPane();
		Scene scene = new Scene(pane);
		PasswordField field = new PasswordField();
		field.setPrefWidth(200);
		pane.setAlignment(Pos.CENTER);
		Label label = new Label("Password: ");
		pane.add(label, 0, 0);
		pane.add(field, 1, 0);
		Button enterButton = new Button("Unlock");
		enterButton.setPrefWidth(100);
		enterButton.setOnAction((e) -> {
			key = EncryptionManager.hash(field.getText());
			field.clear();
			if (Files.exists(tags)) {
				try {
					String contents = new String(EncryptionManager.decrypt(Files.readAllBytes(tags), key));
					String[] split = contents.split("\n");
					if (split[0].equals("--tags--")) {
						stage.hide();
						new MainWindow(key);
					} else {
					}
				} catch (IOException | NullPointerException e1) {
					e1.printStackTrace();
				}
			} else {
				stage.hide();
				new MainWindow(key);
			}
		});
		field.addEventHandler(KeyEvent.KEY_RELEASED, (e) -> {
			if (e.getCode() != KeyCode.ENTER) {
				return;
			}
			key = EncryptionManager.hash(field.getText());
			field.clear();
			if (Files.exists(tags)) {
				try {
					String contents = new String(EncryptionManager.decrypt(Files.readAllBytes(tags), key));
					String[] split = contents.split("\n");
					if (split[0].equals("--tags--")) {
						stage.hide();
						new MainWindow(key);
					} else {
					}
				} catch (IOException | NullPointerException e1) {
					e1.printStackTrace();
				}
			} else {
				stage.hide();
				new MainWindow(key);
			}
		});
		pane.add(enterButton, 1, 1);
		stage.setScene(scene);
		scene.getStylesheets().add("dark-theme.css");
		stage.show();
	}

}
