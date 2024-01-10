/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pushplaymanager;

import com.jfoenix.controls.JFXButton;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import static pushplaymanager.produit.bindTooltip;

/**
 *
 * @author Administrator
 */
public class session {
    Connection connection = null;
    private double xOffset = 0;
    private double yOffset = 0;
    
    
   private int idSession;
   private String dateSession;
   private String timeSession;
   private String durationSession;
   private int amountSession;
   private int payment;
   private String typeSession;
   private String noteSession;
   private int numPost;
   private int current;
   
   private DetailsSessionController detailsSession;
   
   private HBox actions;



    public session(int idSession, String durationSession, int amountSession, String typeSession, int current) {
        this.idSession = idSession;
        this.durationSession = durationSession;
        this.amountSession = amountSession;
        this.typeSession = typeSession;
        this.current = current;
    }
    
    
     public session(int idSession, String dateSession, String timeSession, String typeSession, int payment,  String noteSession, int numPost) {
        this.idSession = idSession;
        this.dateSession = dateSession;
        this.timeSession = timeSession;
        this.payment = payment;
        this.typeSession = typeSession;
        this.noteSession = noteSession;
        this.numPost = numPost;
        
        
        JFXButton deleteBtn = new JFXButton();
                deleteBtn.setPrefWidth(30);
		Image image = new Image(getClass().getResourceAsStream("/images/delete.png"),16,16,false,false);
		deleteBtn.setGraphic(new ImageView(image));
                deleteBtn.setCursor(Cursor.HAND);
                //deleteBtn.getStylesheets().add("/style/username.css");
                //deleteBtn.getStyleClass().add("addBtn");
                bindTooltip(deleteBtn,new Tooltip("Supprimer la vente"));
		deleteBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Alert alert1 = new Alert(Alert.AlertType.CONFIRMATION, "êtes-vous sûr de vouloir Supprimer la session?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
               ((Stage) alert1.getDialogPane().getScene().getWindow()).setAlwaysOnTop(true);
               alert1.showAndWait();
                    if (alert1.getResult() == ButtonType.YES){
                    
                    try {
                        connection = dbConnect.getDataBase();
                        PreparedStatement preparedStatement=null;
                        String query="delete from session where idSession="+getIdSession()+";";
                        preparedStatement=connection.prepareStatement(query);
                        preparedStatement.executeUpdate();
                        preparedStatement.close();
                        HistoriqueController.getInstance().refresh(event);
                        
                    } catch (SQLException ex) {
                        Logger.getLogger(session.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(session.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(session.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ParseException ex) {
                        Logger.getLogger(session.class.getName()).log(Level.SEVERE, null, ex);
                    }
                        
                    
                    }
            }
         });
                
        JFXButton detailsBtn = new JFXButton();
                detailsBtn.setPrefWidth(30);
		Image imageDetails = new Image(getClass().getResourceAsStream("/images/records.png"),16,16,false,false);
		detailsBtn.setGraphic(new ImageView(imageDetails));
                detailsBtn.setCursor(Cursor.HAND);
                //detailsBtn.getStylesheets().add("/style/username.css");
                //detailsBtn.getStyleClass().add("addBtn");
                bindTooltip(detailsBtn,new Tooltip("Détails de la vente"));
		detailsBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("detailsSession.fxml"));
                    Parent root = (Parent) fxmlLoader.load();
                    
                    detailsSession= fxmlLoader.getController();
                    detailsSession.fillFields(getIdSession());
                    detailsSession.idSessionTxt.setText(getIdSession()+"");
                    detailsSession.note.setText(getNoteSession());
                    detailsSession.dateTxt.setText(getDateSession());
                    detailsSession.timeTxt.setText(getTimeSession());
                    detailsSession.typeTxt.setText(getTypeSession());
                    detailsSession.numTxt.setText(getNumPost()+"");
                    detailsSession.paymentTxt.setText(getPayment()+"");
                    
                    Stage stage = new Stage();
                    stage.setScene(new Scene(root));
                    stage.initStyle(StageStyle.DECORATED.UNDECORATED);
                    stage.setAlwaysOnTop(true);
                    
                    //Open Stage in a specific position
                    stage.setX(500);
                    stage.setY(100);
                    
                    stage.setOnCloseRequest(eventt -> {
                       // isNewOrderOpen = false;  
                    });
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
                    
                } catch (IOException ex) {
                    Logger.getLogger(session.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
         });        
                
        
        this.actions = new HBox(detailsBtn , deleteBtn);
	actions.setAlignment(Pos.CENTER);
        actions.setPrefWidth(250);
	actions.setSpacing(12); 
    }

    public HBox getActions() {
        return actions;
    }

    public void setActions(HBox Actions) {
        this.actions = Actions;
    }
    
     
     
    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public int getNumPost() {
        return numPost;
    }

    public void setNumPost(int numPost) {
        this.numPost = numPost;
    }

    
    
    public int getIdSession() {
        return idSession;
    }

    public void setIdSession(int idSession) {
        this.idSession = idSession;
    }

    public String getDurationSession() {
        return durationSession;
    }

    public void setDurationSession(String durationSession) {
        this.durationSession = durationSession;
    }

    public int getAmountSession() {
        return amountSession;
    }

    public void setAmountSession(int amountSession) {
        this.amountSession = amountSession;
    }

    public String getTypeSession() {
        return typeSession;
    }

    public void setTypeSession(String typrSession) {
        this.typeSession = typrSession;
    }

    public String getDateSession() {
        return dateSession;
    }

    public void setDateSession(String dateSession) {
        this.dateSession = dateSession;
    }

    public String getTimeSession() {
        return timeSession;
    }

    public void setTimeSession(String timeSession) {
        this.timeSession = timeSession;
    }

    public String getNoteSession() {
        return noteSession;
    }

    public void setNoteSession(String noteSession) {
        this.noteSession = noteSession;
    }

    public int getPayment() {
        return payment;
    }

    public void setPayment(int payment) {
        this.payment = payment;
    }

    
    
}
