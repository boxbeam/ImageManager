package redempt.imagemanager;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Optional;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;

public class ImageContextMenu extends ContextMenu {
	
	public ImageContextMenu(Image image, String name, MainWindow window) {
		MenuItem copy = new MenuItem("Copy to clipboard");
		copy.setOnAction((e) -> {
			java.awt.Image img = SwingFXUtils.fromFXImage(image, null);
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(new ImageTransferable(img), null);
		});
		MenuItem delete = new MenuItem("Delete image");
		delete.setOnAction((e) -> {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Confirm deletion");
			alert.setHeaderText("Delete image?");
			alert.setContentText("Are you sure you want to delete this image?");
			alert.getDialogPane().getStylesheets().add("dark-theme.css");
			Optional<ButtonType> button = alert.showAndWait();
			if (button.isPresent() && button.get() == ButtonType.OK) {
				window.removeImage(image, true);
			}
		});
		MenuItem editTags = new MenuItem("Edit tags");
		editTags.setOnAction((e) -> {
			new TagEditor(name, window);
		});
		this.getItems().add(copy);
		this.getItems().add(delete);
		this.getItems().add(editTags);
	}
	
private static class ImageTransferable implements Transferable {
		
		private java.awt.Image image;
		
		public ImageTransferable(java.awt.Image image) {
			this.image = image;
		}
		
		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[] {DataFlavor.imageFlavor};
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return flavor == DataFlavor.imageFlavor;
		}

		@Override
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
			if (isDataFlavorSupported(flavor)) {
				return image;
			} else {
				throw new UnsupportedFlavorException(flavor);
			}
		}
		
	}
	
}
