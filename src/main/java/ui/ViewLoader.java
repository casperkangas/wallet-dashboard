package ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.io.IOException;

public class ViewLoader {
    
    public static Parent loadView(String path) {
        try {
            FXMLLoader loader = new FXMLLoader(ViewLoader.class.getResource(path));
            return loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load view: " + path, e);
        }
    }
    
    public static <T> T getController(String path) {
        try {
            FXMLLoader loader = new FXMLLoader(ViewLoader.class.getResource(path));
            loader.load();
            return loader.getController();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load controller for: " + path, e);
        }
    }
    
    public static ViewResult loadAndGetController(String path) {
        try {
            FXMLLoader loader = new FXMLLoader(ViewLoader.class.getResource(path));
            Parent view = loader.load();
            Object controller = loader.getController();
            return new ViewResult(view, controller);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load view and controller for: " + path, e);
        }
    }

    public record ViewResult(Parent view, Object controller) {}
}
