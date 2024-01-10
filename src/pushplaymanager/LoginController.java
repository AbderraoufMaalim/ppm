/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pushplaymanager;

import com.jfoenix.controls.JFXButton;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * FXML Controller class
 *
 * @author Raouf
 */
public class LoginController implements Initializable {
public static String firstNameUser;
public static String lastNameUser;
public static int userId;

Connection connection = null;
private int selection = 0;
public String username = "null";
        
@FXML
private JFXButton btnUser1, btnUser2;

@FXML
private TextField name;  
@FXML
private PasswordField password;
@FXML
private Label label;
  
            
    private String test(int id) throws IOException, SQLException, ClassNotFoundException {
        String nameUser=" ";
        String sql = "SELECT * FROM users WHERE idUsers = "+id+";";
        connection = dbConnect.getDataBase();
        PreparedStatement st = null;
        ResultSet rs = null;
        st = connection.prepareStatement(sql);
        rs = st.executeQuery();
        if (rs.next()) {        
            firstNameUser = rs.getString("FirstNameUser");
            lastNameUser = rs.getString("LastNameUser");
            userId = rs.getInt("idUsers");
            nameUser = rs.getString("UserName");   
            password.requestFocus();
        }
        st.close();
        rs.close();
        connection.close();
        return nameUser;
    }
    
    @FXML
    private void user1(ActionEvent event) throws IOException, SQLException, ClassNotFoundException {
        label.setText("");
        selection = 1;
        username = test(selection);
        name.setText("Dr. "+username);
        btnUser1.getStyleClass().add("userFocus");
        btnUser2.getStyleClass().removeAll("userFocus");
    }
    
    @FXML
    private void user2(ActionEvent event) throws IOException, SQLException, ClassNotFoundException {
        label.setText("");
        selection = 2;
        username = test(selection);
        name.setText("Dr. "+username);
        btnUser2.getStyleClass().add("userFocus");
    btnUser1.getStyleClass().removeAll("userFocus");
    }
    
        @FXML
    private void exit(ActionEvent event) throws IOException {
        //fenêtre de confirmation de la déconnexion
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to Exit?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
        alert.showAndWait();
        //si on clique sur oui:
        if (alert.getResult() == ButtonType.YES) {
           ((Node)(event.getSource())).getScene().getWindow().hide();
         System.exit(0); 
        }
    }
    
    
    private double xOffset = 0;
    private double yOffset = 0;
            @FXML
    private void connect(ActionEvent event) throws IOException, SQLException, ClassNotFoundException {
        String pass = password.getText().toString();
        String sql = "SELECT * FROM users WHERE username = ? and password = ?";
        connection=dbConnect.getDataBase();
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ResultSet resultSet = null;
     if (!name.getText().isEmpty()){
        preparedStatement.setString(1, username);
        preparedStatement.setString(2, pass);
        resultSet = preparedStatement.executeQuery();
        
        if (resultSet.next()) {
            
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main.fxml"));
	Parent root1 = (Parent) fxmlLoader.load();
	Stage stage = new Stage();
        stage.initStyle(StageStyle.DECORATED.UNDECORATED); 
        root1.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            }
        });
        root1.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
                stage.setOpacity(0.9f);
            }
        });  
        root1.setOnDragDone((e) -> {
            stage.setOpacity(1.0f);
        });
        root1.setOnMouseReleased((e) -> {
            stage.setOpacity(1.0f);
        });
        
	stage.setScene(new Scene(root1)); 
	stage.show();
        ((Node)(event.getSource())).getScene().getWindow().hide(); 
        }else{
            label.setText("mot de passe incorrect");
        }
      }else{ 
            label.setText("veuillez sélectionner un utilisateur");
        }
        preparedStatement.close();
        resultSet.close();
        connection.close();
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
       // btnUser1.setText("Dr. "+test(1));
       // btnUser2.setText("Dr. "+test(2));
       
        
    }    
    
}
