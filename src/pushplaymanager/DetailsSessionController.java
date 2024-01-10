/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pushplaymanager;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import static pushplaymanager.SessionsController.connection;

/**
 * FXML Controller class
 *
 * @author Raouf
 */
public class DetailsSessionController implements Initializable {

    public static DetailsSessionController instance;
	public DetailsSessionController(){
		instance =this;
	}
	public static DetailsSessionController getInstance(){
		return instance;
	}

        @FXML
     Label idSessionTxt;
        @FXML
     Text dateTxt;

    @FXML
     Text timeTxt;

    @FXML
     Text typeTxt;

    @FXML
     Text numTxt;

    @FXML
     Text paymentTxt;
    
    
        
    int idSess;
        
    @FXML
     TableView<vente> tableVentes;
        @FXML
    private TableColumn<vente,Integer> idVente;   
        @FXML
    private TableColumn<vente,String> nomProduit; 
                @FXML
    private TableColumn<vente,String> typeProduit;           
                @FXML
    private TableColumn<vente,Integer> quant; 
                @FXML
    private TableColumn<vente,Integer> prixVente;
               
                
                
   public ObservableList<vente>newData=FXCollections.observableArrayList();

   @FXML
    TextArea note;
                
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
    }    
    
    public void remplirTab(String req){
        try {
            tableVentes.getItems().clear();
            
            connection=dbConnect.getDataBase();
            
            PreparedStatement st = null;
            ResultSet rs = null; 
            st = connection.prepareStatement(req);
            rs = st.executeQuery();
            
            while (rs.next()) {
                newData.add(new vente(rs.getInt(1),rs.getString(2),rs.getString(3),rs.getInt(4),rs.getInt(5),rs.getInt(6)));
            }
            st.close();
            rs.close();
            
            idVente.setCellValueFactory(new PropertyValueFactory<vente,Integer>("idVente"));
            nomProduit.setCellValueFactory(new PropertyValueFactory<vente,String>("nomProduit"));
            typeProduit.setCellValueFactory(new PropertyValueFactory<vente,String>("typeProduit"));
            prixVente.setCellValueFactory(new PropertyValueFactory<vente,Integer>("prixVente"));
            quant.setCellValueFactory(new PropertyValueFactory<vente,Integer>("quant"));

            tableVentes.setItems(newData);
            
            connection.close();
        } catch (SQLException ex) {
            Logger.getLogger(DetailsSessionController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DetailsSessionController.class.getName()).log(Level.SEVERE, null, ex);
        }
           
    }
    
     public void fillFields (int id){
         String sql;
        sql = "SELECT idVente, nomProduit, typeProduit, quant,"
                + " prixVente, idSession FROM vente"
                + " WHERE idSession = "+id+" ORDER BY idVente DESC";
        remplirTab(sql);
        
     }
     
     
     
     
     
     
    
    public void CloseWindow (ActionEvent event){
		((Node)(event.getSource())).getScene().getWindow().hide();
	}
    
}
