/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pushplaymanager;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;

/**
 * FXML Controller class
 *
 * @author Raouf
 */
public class DepenseController implements Initializable {
Connection connection = null;


@FXML
private Text dateTxt;
    
public String date;
    @FXML
    private TextArea comment;

    @FXML
    private Spinner<Integer> spinner;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        
        dateTxt.setText(HistoriqueController.getInstance().globalDateSearch);
        date = HistoriqueController.getInstance().globalDateSearch;
        SpinnerValueFactory<Integer> valueFactory = //
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1000000, 500, 50);
        spinner.setValueFactory(valueFactory);
    }    
    
    public String getCurrentTime(){
    Date date = new Date();
    date.compareTo(date);
    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");  
    return formatter.format(date);
    }
    
    public void valider (ActionEvent event) throws SQLException, ClassNotFoundException, IOException, ParseException{
         PreparedStatement preparedSt=null;
         connection=dbConnect.getDataBase();
        preparedSt=connection.prepareStatement("INSERT INTO depense "
                + "(dateDep, timeDep, montant, comment) "
                + "VALUES('"+date+"','15:00',"+spinner.getValue()+",'"+comment.getText()+"');"); 
        
        preparedSt.execute();
        
        HistoriqueController.getInstance().refresh(event);
        connection.close();
        CloseWindow(event);
    }
    
    
    public void CloseWindow (ActionEvent event){
		((Node)(event.getSource())).getScene().getWindow().hide();
	}
    
}
