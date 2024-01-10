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
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import static pushplaymanager.produit.bindTooltip;


public class depense {
    Connection connection = null;
    
    private int idDep;
    private String dateDep;
    private String timeDep;
    private int montant;
    private String comment;
    private HBox actions;

    public depense(int idDep, String dateDep, String timeDep, int montant, String comment) {
        this.idDep = idDep;
        this.dateDep = dateDep;
        this.timeDep = timeDep;
        this.montant = montant;
        this.comment = comment;
        
        
        
        JFXButton deleteBtn = new JFXButton();
                deleteBtn.setPrefWidth(30);
		Image image = new Image(getClass().getResourceAsStream("/images/delete.png"),16,16,false,false);
		deleteBtn.setGraphic(new ImageView(image));
                deleteBtn.setCursor(Cursor.HAND);
                //deleteBtn.getStylesheets().add("/style/username.css");
                //deleteBtn.getStyleClass().add("addBtn");
                bindTooltip(deleteBtn,new Tooltip("Supprimer"));
		deleteBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Alert alert1 = new Alert(Alert.AlertType.CONFIRMATION, "êtes-vous sûr de vouloir Supprimer?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
               ((Stage) alert1.getDialogPane().getScene().getWindow()).setAlwaysOnTop(true);
               alert1.showAndWait();
                    if (alert1.getResult() == ButtonType.YES){
                    
                    try {
                        connection = dbConnect.getDataBase();
                        PreparedStatement preparedStatement=null;
                        String query="delete from depense where idDep="+getIdDep()+";";
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
        
        this.actions = new HBox(deleteBtn);
	actions.setAlignment(Pos.CENTER);
        actions.setPrefWidth(250);
	actions.setSpacing(12); 
    }

    public int getIdDep() {
        return idDep;
    }

    public void setIdDep(int idDep) {
        this.idDep = idDep;
    }

    public String getDateDep() {
        return dateDep;
    }

    public void setDateDep(String dateDep) {
        this.dateDep = dateDep;
    }

    public String getTimeDep() {
        return timeDep;
    }

    public void setTimeDep(String timeDep) {
        this.timeDep = timeDep;
    }

    public int getMontant() {
        return montant;
    }

    public void setMontant(int montant) {
        this.montant = montant;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public HBox getActions() {
        return actions;
    }

    public void setActions(HBox actions) {
        this.actions = actions;
    }

    
    
    
    
    
}
