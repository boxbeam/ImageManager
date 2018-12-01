package redempt.imagemanager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TagList extends Stage {
	
	private String filter = "";
	private VBox pane;
	private ScrollPane scroll;
	private Map<String, Integer> tags;
	private MainWindow window;
	
	public TagList(MainWindow window) {
		this.window = window;
		this.setTitle("Tags");
		this.setResizable(false);
		this.setHeight(500);
		pane = new VBox();
		scroll = new ScrollPane();
		scroll.setContent(pane);
		pane.setPrefWidth(250);
		pane.setAlignment(Pos.TOP_LEFT);
		Scene scene = new Scene(scroll);
		
		TextField field = new TextField();
		field.setPromptText("Filter");
		field.setPrefWidth(250);
		field.addEventHandler(KeyEvent.KEY_RELEASED, (e) -> {
			filter = field.getText();
			refresh();
		});
		pane.getChildren().add(field);
		
		tags = new HashMap<>();
		for (Entry<String, List<String>> entry : window.tags.entrySet()) {
			for (String tag : entry.getValue()) {
				if (tags.containsKey(tag)) {
					tags.put(tag, tags.get(tag) + 1);
				} else {
					tags.put(tag, 1);
				}
			}
		}
		
		refresh();
		
		this.setScene(scene);
		scene.getStylesheets().add("dark-theme.css");
		this.show();
	}
	
	private void refresh() {
		pane.getChildren().removeIf((c) -> c instanceof Label);
		for (Entry<String, Integer> entry : tags.entrySet()) {
			if (!entry.getKey().contains(filter)) {
				continue;
			}
			Label label = new Label(entry.getKey() + " - " + entry.getValue());
			label.setTranslateX(10);
			label.addEventHandler(MouseEvent.MOUSE_CLICKED, (e) -> {
				window.setFilter((window.getFilter().trim() + " " + entry.getKey()).trim());
				window.refreshImages();
			});
			pane.getChildren().add(label);
		}
	}
	
}
