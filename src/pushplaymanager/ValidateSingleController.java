/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pushplaymanager;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.text.Text;

/**
 * FXML Controller class
 *
 * @author Raouf
 */
public class ValidateSingleController implements Initializable {
    Connection connection = null;

      @FXML
    private Text valideTxt;
      @FXML
    private Spinner<Integer> spinner;
    String sql;
    String nameProduct;
    String typeProduct;
    int priceProduct;
    int quantProduct;
    int numP;

    
         
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
    }    
    
      public void CloseWindow (ActionEvent event){
	((Node)(event.getSource())).getScene().getWindow().hide();
	}
      
      public void Valider (ActionEvent event) throws SQLException, ClassNotFoundException{
         
          String heureF = CaisseTableController.instance.getCurrentTime(); 
    String date = CaisseTableController.instance.getCurrentDate(); 
    
        connection=dbConnect.getDataBase();
        
        sql = "INSERT INTO session (dateSession, heureDebut, heureFin, clientId, sessionType, "
                + "Payment, Comment, client, idUser, duration, numPost) VALUES "
                + "('"+date+"', '', '"+heureF+"', '1', 'Table', '"+priceProduct*spinner.getValue()+"', 'Table "+numP+"', "
                + "'Guest', 1, '' ,'"+numP+"');";
        
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.execute();  
        
        sql= "SELECT last_insert_rowid();";
        ResultSet rs = null;
        
        preparedStatement = connection.prepareStatement(sql);
            
        rs = preparedStatement.executeQuery();
        System.out.println(rs.getInt(1));
        int idSess = rs.getInt(1);
        
        connection=dbConnect.getDataBase();
                PreparedStatement preparedSt= connection.prepareStatement("INSERT INTO vente "
                        + "(nomProduit, typeProduit, prixVente, quant, idSession) "
                        + "VALUES('"+nameProduct+"','"+typeProduct+"', "+priceProduct*spinner.getValue()+" ,"+spinner.getValue()+", "+idSess+");");
                
                preparedSt.execute();
                
         connection.close();
          
          
	 System.out.print("Validé avec succes");
         ((Node)(event.getSource())).getScene().getWindow().hide();
	}
      
      public void fillFields (int payment,String name,int quant,int numPoste,String type){
         
	 System.out.println("payment "+payment);
         System.out.println("name "+name);
         System.out.println("numPoste "+numPoste);
         valideTxt.setText("Valider "+name+"?");
         
         
         nameProduct=name;
         typeProduct=type;
         priceProduct = payment/quant;
         numP= numPoste;
         
         SpinnerValueFactory<Integer> valueFactory = //
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, quant, 1, 1);
        spinner.setValueFactory(valueFactory);
        
	}
    
}
