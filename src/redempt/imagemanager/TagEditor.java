package redempt.imagemanager;

import java.util.Arrays;
import java.util.List;

import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class TagEditor extends Stage {
	
	private String tags;
	private String name;
	private MainWindow window;
	
	public TagEditor(String name, MainWindow window) {
		this.window = window;
		this.name = name;
		List<String> tags = window.tags.get(name);
		String combine;
		if (tags != null) {
			combine = String.join(" ", tags);
		} else {
			combine = "";
		}
		StackPane pane = new StackPane();
		TextArea text = new TextArea(combine);
		text.addEventHandler(KeyEvent.KEY_RELEASED, (e) -> {
			TagEditor.this.tags = text.getText();
			refresh();
		});
		text.setPrefWidth(300);
		text.setPrefHeight(200);
		text.setWrapText(true);
		pane.getChildren().add(text);
		Scene scene = new Scene(pane);
		this.setScene(scene);
		this.setTitle("Edit tags");
		this.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, (e) -> {
			window.saveTags();
		});
		scene.getStylesheets().add("dark-theme.css");
		this.show();
	}
	
	private void refresh() {
		List<String> tags = Arrays.asList(this.tags.split(" "));
		window.tags.put(name, tags);
	}
	
}