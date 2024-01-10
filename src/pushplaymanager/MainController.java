/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pushplaymanager;

import java.awt.Image;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
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
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;




public class MainController implements Initializable {
	private Connection connection;
    private double xOffset = 0;
    private double yOffset = 0;
    

    @FXML
    private AnchorPane anchor;
    @FXML
    private AnchorPane anchor2;
    @FXML
    private AnchorPane anchor3;
    @FXML
    private AnchorPane anchor4;
    @FXML
    private AnchorPane anchor5;
    @FXML
    private BorderPane mainPane;
    
    @FXML
    public  Label bonjour;
    
    @FXML
    private Button btnPatient;
    @FXML
    private Button btnHistorique;
    @FXML
    private Button btnRecette;
    @FXML
    private Button btnVisite;
    @FXML
    private Button btnMedecin;
    @FXML
    private ImageView imgMedecin;
    @FXML
    private MenuButton setting;
    @FXML 
    private Menu reset = new Menu("Réinisialiser");
    
    
    public static MainController instance;
	public MainController() {
		instance =this;
	}
	public static MainController getInstance(){
		return instance;
	}
    
    
        @FXML
    private void exit(ActionEvent event) throws IOException {
        //fenÃªtre de confirmation de la dÃ©connexion
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Êtes-vous sûr de vouloir quitter?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
        alert.showAndWait();
        //si on clique sur oui:
        if (alert.getResult() == ButtonType.YES) {
           ((Node)(event.getSource())).getScene().getWindow().hide();
         System.exit(0); 
        }
    }
    
    @FXML
    private void deco(ActionEvent event) throws IOException {
        //fenÃªtre de confirmation de la dÃ©connexion
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Êtes-vous sûr de vouloir vous déconnecter?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
        alert.showAndWait();
        //si on clique sur oui:
        if (alert.getResult() == ButtonType.YES) {

         //ouvrir la fenetre Login style undecorated et possiblilitÃ© de faire bouger la fenÃªtre
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login.fxml"));
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
            }
        });  
	stage.setScene(new Scene(root1)); 
	stage.show();
        //fermer la fenÃªtre Actuelle (MainScreen)
        ((Node)(event.getSource())).getScene().getWindow().hide();
        }	
    }
    
    @FXML
    private void minimize(ActionEvent event) throws IOException {
    Stage stage =(Stage)mainPane.getScene().getWindow();
    stage=(Stage)((Button)event.getSource()).getScene().getWindow();
    stage.setIconified(true);
    }
    
    @FXML
    private void openPatient(ActionEvent event) throws IOException {
        resetBtnColors();
        btnPatient.getStyleClass().add("btnFocusPatient");
        anchor3.setVisible(true);
        anchor2.setVisible(false);
        anchor4.setVisible(false);
    }
    
    
    @FXML
    private void openDashboard(ActionEvent event) throws IOException {
        opendashboard2();
    }
    
    private void opendashboard2() throws IOException {
        resetBtnColors();
        btnMedecin.getStyleClass().add("btnFocusUser");
        anchor2.setVisible(true);
        anchor3.setVisible(false);
        anchor4.setVisible(false);
        anchor5.setVisible(false);
    }
    
                @FXML
    private void openVisite(ActionEvent event) throws IOException, ParseException {
        resetBtnColors();
        btnVisite.getStyleClass().add("btnFocusVisit");
	anchor3.setVisible(true);
        anchor2.setVisible(false);
        anchor4.setVisible(false);
        anchor5.setVisible(false);
        HistoriqueController.getInstance().refresh(event);
    }
    
    
     @FXML
    private void openRecettes(ActionEvent event) throws IOException {
        resetBtnColors();
        btnRecette.getStyleClass().add("btnFocusRecette");
        anchor2.setVisible(false);
        anchor3.setVisible(false);
        anchor4.setVisible(false);
        anchor5.setVisible(true);
    }
    
    
                        @FXML
    private void openHistoriqueVisite(ActionEvent event) throws IOException {
        resetBtnColors();
        btnHistorique.getStyleClass().add("btnFocusHostoric");
        anchor2.setVisible(false);
        anchor3.setVisible(false);
        anchor5.setVisible(false);
        anchor4.setVisible(true);
    }
    
    private void resetBtnColors(){
        btnMedecin.getStyleClass().removeAll("btnFocusUser"); 
        btnMedecin.getStyleClass().add("MainButton");
        btnHistorique.getStyleClass().removeAll("btnFocusHostoric"); 
        btnHistorique.getStyleClass().add("MainButton");
        btnRecette.getStyleClass().removeAll("btnFocusRecette"); 
        btnRecette.getStyleClass().add("MainButton");
        btnVisite.getStyleClass().removeAll("btnFocusVisit"); 
        btnVisite.getStyleClass().add("MainButton");
        btnPatient.getStyleClass().removeAll("btnFocusPatient"); 
        btnPatient.getStyleClass().add("MainButton");
    }
    
		@FXML public void SettingsButtonsAction(ActionEvent event) {
		    	
		    	try {
				    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Settings/Warning.fxml"));
				            Parent root = (Parent) fxmlLoader.load();
				            Stage stage = new Stage();
				            stage.setScene(new Scene(root));  
				            stage.initStyle(StageStyle.DECORATED.UNDECORATED);
				            stage.setAlwaysOnTop(true);
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
			            }
			        });  
				stage.show();    
				} catch(Exception e) {
				       e.printStackTrace();
				}
		    	
		    }
	
		
		public void ExecuteQuery(String query) throws ClassNotFoundException, SQLException{
	    	 connection = dbConnect.getDataBase();
	    	 PreparedStatement preparedStatement = null;
	    	 ResultSet rs = null;
	    	 try {
	    		 preparedStatement = connection.prepareStatement(query);
	    		 preparedStatement.execute();
	    		 preparedStatement.close();
			} catch (Exception e) {
				System.err.println(e);
			}finally{
				connection.close();
			}
	     }
		
		@FXML public void AboutButtonsAction(ActionEvent event) {
	    	
	    	try {
			    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Settings/About.fxml"));
			            Parent root = (Parent) fxmlLoader.load();
			            Stage stage = new Stage();
			            stage.setScene(new Scene(root));  
			            stage.initStyle(StageStyle.DECORATED.UNDECORATED);
			            stage.setAlwaysOnTop(true);
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
		            }
		        });  
			stage.show();    
			} catch(Exception e) {
			       e.printStackTrace();
			}
	    	
	    }
		
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            opendashboard2();
        AnchorPane pane3 = FXMLLoader.load(getClass().getResource("historique.fxml"));
        anchor3.getChildren().setAll(pane3);
        anchor3.setVisible(false);
        
        
        AnchorPane pane4 = FXMLLoader.load(getClass().getResource("stock.fxml"));
        anchor4.getChildren().setAll(pane4);
        anchor4.setVisible(false);
        
        AnchorPane pane5 = FXMLLoader.load(getClass().getResource("stats.fxml"));
        anchor5.getChildren().setAll(pane5);
        anchor5.setVisible(false);
        
        
        AnchorPane pane2 = FXMLLoader.load(getClass().getResource("sessions.fxml"));
        anchor2.getChildren().setAll(pane2);
        
        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }    
    
     public static void WriteInFile(String str) 
    		  throws IOException {

    		}
}
