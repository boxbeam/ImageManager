package redempt.imagemanager;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PasswordChanger extends Stage {
	
	public PasswordChanger(MainWindow window) {
		this.setTitle("Change password");
		VBox pane = new VBox();
		pane.setPrefWidth(300);
		Scene scene = new Scene(pane);
		PasswordField password = new PasswordField();
		pane.getChildren().add(password);
		Button finish = new Button("Finish");
		finish.setOnAction((e) -> {
			String pass = password.getText();
			byte[] key = EncryptionManager.hash(pass);
			window.setKey(key);
			window.saveTags();
			window.saveImages();
			PasswordChanger.this.close();
		});
		pane.getChildren().add(finish);
		finish.setPrefWidth(300);
		
		this.setScene(scene);
		scene.getStylesheets().add("dark-theme.css");
		this.show();
	}
	
}
