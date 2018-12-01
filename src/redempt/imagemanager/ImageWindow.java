package redempt.imagemanager;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class ImageWindow extends Stage {
	
	public ImageWindow(Image image, String name, MainWindow window) {
		ImageView view = new ImageView(image);
		double largest = Math.max(view.getImage().getWidth(), view.getImage().getHeight());
		largest = 900 / largest;
		view.setImage(scale(image, largest));
		this.setHeight(view.getImage().getHeight() * view.getScaleY());
		this.setWidth(view.getImage().getWidth() * view.getScaleX());
		StackPane pane = new StackPane();
		pane.getChildren().add(view);
		Scene scene = new Scene(pane);
		view.setOnContextMenuRequested((e) -> {
			new ImageContextMenu(image, name, window).show(ImageWindow.this, e.getScreenX(), e.getScreenY());
		});
		this.setScene(scene);
		this.setTitle("Image");
		this.setResizable(false);
		scene.getStylesheets().add("dark-theme.css");
		this.show();
	}
	
	private Image scale(Image image, double scale) {
		BufferedImage img = SwingFXUtils.fromFXImage(image, null);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			ImageIO.write(img, "png", out);
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] bytes = out.toByteArray();
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		return new Image(in, image.getWidth() * scale, image.getHeight() * scale, false, true);
	}
	
}
