package redempt.imagemanager;

import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class MainWindow extends Stage {
	
	private byte[] key;
	protected Map<String, List<String>> tags = new HashMap<>();
	private Map<Image, String> images = new HashMap<>();
	
	private HBox columnContainer = new HBox();
	private VBox pane = new VBox();
	private VBox[] columns;
//	private VBox c1 = new VBox();
//	private VBox c2 = new VBox();
	private GridPane buttons = new GridPane();
	private TextField field;
	protected String filter = "";
	private double cycle = 0;
	
	public MainWindow(byte[] key) {
		this.key = key;
		try {
			if (Files.exists(ImageManager.tags)) {
				String file = new String(EncryptionManager.decrypt(Files.readAllBytes(ImageManager.tags), key));
				String[] split = file.split("\n");
				for (int i = 1; i < split.length; i++) {
					String line = split[i];
					if (!line.contains(":")) {
						continue;
					}
					String name = line.split(":")[0];
					String[] tags = split[i].substring(split[i].indexOf(":") + 1).split(" ");
					List<String> tagList = new ArrayList<>();
					for (String tag : tags) {
						tagList.add(tag);
					}
					this.tags.put(name, tagList);
				}
			} else {
				saveTags();
			}
			Files.list(ImageManager.folder).forEach((p) -> {
				if (p.toString().endsWith(".dat")) {
					return;
				}
				try {
					images.put(new Image(new ByteArrayInputStream(EncryptionManager.decrypt(Files.readAllBytes(p), key))), p.getFileName().toString());
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.setTitle("Images");
		this.setHeight(900);
		this.setWidth(1000);
		ScrollPane root = new ScrollPane();
//		pane = new HBox();
//		pane.setAlignment(Pos.TOP_LEFT);
//		c1.setMaxWidth(230);
//		c2.setMaxWidth(230);
//		c1.setAlignment(Pos.TOP_LEFT);
//		c2.setAlignment(Pos.TOP_LEFT);
//		c1.setSpacing(10);
//		c2.setSpacing(10);
//		pane.setSpacing(10);
//		pane.getChildren().add(c1);
//		pane.getChildren().add(c2);
		root.setContent(columnContainer);
		pane.getChildren().add(buttons);
		pane.getChildren().add(root);
		pane.setSpacing(5);
		root.setStyle("-fx-background-color: transparent; -fx-border-color: black; -fx-border-width: 1 0 0 0;");
		columnContainer.setSpacing(10);
		columnContainer.setPadding(new Insets(10, 0, 0, 0));
		
		buttons.setHgap(10);
		buttons.setVgap(10);
		Button add = new Button("Paste image");
		add.setOnAction((e) -> {
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			Transferable transferable = clipboard.getContents(null);
			if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
				try {
					Image image = awtImageToFX((java.awt.Image) transferable.getTransferData(DataFlavor.imageFlavor));
					addImage(image, true);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		add.setPrefWidth(220);
		buttons.add(add, 0, 0);
		
		Button changePass = new Button("Change password");
		changePass.setOnAction((e) -> {
			new PasswordChanger(this);
		});
		changePass.setPrefWidth(220);
		buttons.add(changePass, 0, 1);
		
		field = new TextField();
		field.setPromptText("Tags");
		field.addEventHandler(KeyEvent.KEY_PRESSED, (e) -> {
			if (e.getCode() == KeyCode.ENTER) {
				e.consume();
				MainWindow.this.filter = field.getText();
				refreshImages();
			}
		});
		field.setPrefWidth(220);
		buttons.add(field, 1, 0);
		
		Button tagList = new Button("Tags");
		tagList.setOnAction((e) -> {
			tags.keySet().removeIf((c) -> !images.containsValue(c));
			new TagList(this);
		});
		tagList.setPrefWidth(220);
		buttons.add(tagList, 1, 1);
		
		refreshImages();
		Scene scene = new Scene(pane);
		this.setScene(scene);
		this.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, (e) -> {
			saveTags();
			System.exit(0);
		});
		this.setResizable(false);
		scene.getStylesheets().add("dark-theme.css");
		this.show();
	}
	
	protected List<String> getTags(Image image) {
		String name = images.get(image);
		return tags.containsKey(name) ? tags.get(name) : new ArrayList<>();
	}
	
	public String getFilter() {
		return field.getText();
	}
	
	public void setFilter(String filter) {
		this.filter = filter;
		field.setText(filter);
	}
	
	private void addImage(Image image, boolean refresh) {
		String name = UUID.randomUUID().toString();
		images.put(image, name);
		Path path = ImageManager.folder.resolve(name);
		BufferedImage buffered = SwingFXUtils.fromFXImage(image, null);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try {
			ImageIO.write(buffered, "png", stream);
			byte[] bytes = stream.toByteArray();
			stream.close();
			bytes = EncryptionManager.encrypt(bytes, key);
			Files.write(path, bytes, StandardOpenOption.CREATE);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (refresh) {
			new ImageWindow(image, name, this);
			refreshImages();
		}
	}
	
	private void addImage(Image image, String name) {
		images.put(image, name);
		Path path = ImageManager.folder.resolve(name);
		BufferedImage buffered = SwingFXUtils.fromFXImage(image, null);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try {
			ImageIO.write(buffered, "png", stream);
			byte[] bytes = stream.toByteArray();
			stream.close();
			bytes = EncryptionManager.encrypt(bytes, key);
			Files.write(path, bytes, StandardOpenOption.CREATE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void removeImage(Image image, boolean refresh) {
		try {
			Files.delete(ImageManager.folder.resolve(images.get(image)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		tags.remove(images.remove(image));
		if (refresh) {
			refreshImages();
		}
	}
	
	protected void setKey(byte[] key) {
		this.key = key;
	}
	
	protected void refreshImages() {
		
		double cycle = Math.random();
		this.cycle = cycle;
//		c1.getChildren().removeIf((n) -> n instanceof ImageView);
//		c2.getChildren().removeIf((n) -> n instanceof ImageView);
		columnContainer.getChildren().clear();
		columns = new VBox[(int) this.getWidth() / 250];
		AtomicInteger[] heights = new AtomicInteger[columns.length];
		for (int i = 0; i < heights.length; i++) {
			columns[i] = new VBox();
			columnContainer.getChildren().add(columns[i]);
			columns[i].setSpacing(10);
		}
		List<String> tags = filter.equals("") ? new ArrayList<>() : new ArrayList<>(Arrays.asList(filter.split(" ")));
		List<String> negate = new ArrayList<>(tags);
		tags.removeIf((s) -> s.startsWith("-"));
		negate.removeAll(tags);
		negate.replaceAll((s) -> s.substring(1));
		
		Thread t = new Thread(() -> {
			for (Image image : images.keySet()) {
				if (cycle != MainWindow.this.cycle) {
					return;
				}
				long time = System.currentTimeMillis();
				List<String> imageTags = getTags(image);
				Thread thread = new Thread(() -> {
					if (!(imageTags.containsAll(tags))) {
						return;
					}
					for (String tag : negate) {
						if (imageTags.contains(tag)) {
							return;
						}
					}
					ImageView view = adjust(new ImageView(image), 235, images.get(image));
					int min = -1;
					int col = -1;
					for (int i = 0; i < heights.length; i++) {
						if (heights[i] == null) {
							heights[i] = new AtomicInteger(0);
						}
						if (heights[i].get() < min || min == -1) {
							min = heights[i].get();
							col = i;
						}
					}
					int column = col;
					heights[column].addAndGet((int) view.getImage().getHeight());
//					if (!column) {
//						c1height.addAndGet((int) view.getImage().getHeight());
//					} else {
//						c2height.addAndGet((int) view.getImage().getHeight());
//					}
					try {
						Thread.sleep((long) (Math.random() * Math.max(1000 - (System.currentTimeMillis() - time), 0)));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					Platform.runLater(() -> {
						if (cycle != MainWindow.this.cycle) {
							return;
						}
						columns[column].getChildren().add(view);
					});
				});
				thread.start();
			}
		});
		t.start();
		
//		c1.autosize();
//		c2.autosize();
	}
	
	protected void saveImages() {
		AtomicInteger num = new AtomicInteger(images.size());
		for (Image image : new ArrayList<Image>(images.keySet())) {
			Thread thread = new Thread(() -> {
				try {
					Files.delete(ImageManager.folder.resolve(images.get(image)));
					addImage(image, images.get(image));
					num.decrementAndGet();
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			thread.start();
		}
		while (num.get() > 0) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	protected void saveTags() {
		String combine = "--tags--\n";
		for (Entry<String, List<String>> entry : tags.entrySet()) {
			String s = entry.getKey() + ":" + String.join(" ", entry.getValue()).replace("\n", "") + "\n";
			if (images.containsValue(entry.getKey())) {
				combine += s;
			}
		}
		try {
			Files.write(ImageManager.tags, EncryptionManager.encrypt(combine.getBytes(), key), StandardOpenOption.CREATE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private ImageView adjust(ImageView image, double width, String name) {
		double ratio = width / image.getImage().getWidth();
		if (image.getImage().isError()) {
			this.removeImage(image.getImage(), false);
		}
		image.setImage(scale(image.getImage(), ratio));
		image.setPreserveRatio(true);
		image.setSmooth(true);
		image.addEventHandler(MouseEvent.MOUSE_CLICKED, (e) -> {
			if (e.getButton() == MouseButton.PRIMARY) {
				new ImageWindow(getImage(name), name, MainWindow.this);
			}
			if (e.getButton() == MouseButton.SECONDARY) {
				new ImageContextMenu(getImage(name), name, this).show(this, e.getScreenX(), e.getScreenY());
			}
		});
		image.autosize();
		return image;
	}
	
	public Image getImage(String name) {
		for (Entry<Image, String> entry : images.entrySet()) {
			if (entry.getValue().equals(name)) {
				return entry.getKey();
			}
		}
		return null;
	}
	
	private Image scale(Image image, double scale) {
		BufferedImage img = SwingFXUtils.fromFXImage(image, null);
		java.awt.Image scaled = img.getScaledInstance((int) (img.getWidth() * scale), (int) (img.getHeight() * scale), BufferedImage.SCALE_SMOOTH);
		BufferedImage blank = new BufferedImage((int) (img.getWidth() * scale), (int) (img.getHeight() * scale), BufferedImage.TYPE_INT_RGB);
		blank.getGraphics().drawImage(scaled, 0, 0, null);
		return SwingFXUtils.toFXImage(blank, null);
	}
	
    private static javafx.scene.image.Image awtImageToFX(java.awt.Image image) throws Exception {
        if (!(image instanceof RenderedImage)) {
            BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
            Graphics g = bufferedImage.createGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();

            image = bufferedImage;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write((RenderedImage) image, "png", out);
        out.flush();
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        return new javafx.scene.image.Image(in);
    }
	
}
