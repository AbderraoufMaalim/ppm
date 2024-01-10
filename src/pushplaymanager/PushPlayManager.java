package pushplaymanager;

import java.awt.Dimension;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author Raouf
 */
public class PushPlayManager extends Application {
    private double xOffset = 0;
    private double yOffset = 0;
   Connection connection = null;
    
    @Override
    public void start(Stage stage) throws IOException, SQLException, ClassNotFoundException {
    	connection = dbConnect.getConnection();
        Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        Parent root = FXMLLoader.load(getClass().getResource("main.fxml"));
        //Scene scene = new Scene(root,screenSize.getWidth()*0.9,screenSize.getHeight()*0.9 ); 
        Scene scene = new Scene(root, Color.TRANSPARENT); 
        stage.initStyle(StageStyle.DECORATED.UNDECORATED);
        stage.getIcons().add(new Image("/images/ppm.png"));
        root.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            }
        });
        root.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
                stage.setOpacity(0.9f);
            }
        });
        root.setOnDragDone((e) -> {
            stage.setOpacity(1.0f);
        });
        root.setOnMouseReleased((e) -> {
            stage.setOpacity(1.0f);
        });
        
        stage.setScene(scene);
        stage.show();
    }
    
    

    public static void main(String[] args) {
        launch(args);
    }
    
}
