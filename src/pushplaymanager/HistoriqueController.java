/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pushplaymanager;

import com.jfoenix.controls.JFXDatePicker;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import static pushplaymanager.SessionsController.connection;

/**
 * FXML Controller class
 *
 * @author Raouf
 */
public class HistoriqueController implements Initializable {
    private double xOffset = 0;
    private double yOffset = 0;
  
private Stage newWindowStage;
public static HistoriqueController instance;
	public HistoriqueController(){
		instance =this;
	}
	public static HistoriqueController getInstance(){
		return instance;
	}
   
    
    @FXML
     TableView<session> tableSessions;
        @FXML
    private TableColumn<session,Integer> id;   
        @FXML
    private TableColumn<session,String> date; 
                @FXML
    private TableColumn<session,String> time;           
                @FXML
    private TableColumn<session,String> type; 
                @FXML
    private TableColumn<session,Integer> num; 
                @FXML
    private TableColumn<session,Integer> payment;
                @FXML
    private TableColumn<session,String> note; 
                @FXML
    private TableColumn<session, HBox> actions;
                
                
                @FXML
    private TableView<depense> tableDep;

    @FXML
    private TableColumn<depense, Integer> idDep;

    @FXML
    private TableColumn<depense, String> dateDep;

    @FXML
    private TableColumn<depense, String> timeDep;

    @FXML
    private TableColumn<depense, Integer> montantDep;

    @FXML
    private TableColumn<depense, String> noteDep;

    @FXML
    private TableColumn<depense, HBox> actionsDep;
                
                @FXML
    private JFXDatePicker datePicker;
                
    public ObservableList<session>newData=FXCollections.observableArrayList();
    public ObservableList<depense>depData=FXCollections.observableArrayList();
    
        @FXML
    private Text recette;
        @FXML
    private Text lyoum;
        @FXML
    private Text lbareh;
        
        @FXML
    private Text chichaTxt;

    @FXML
    private Text mintTxt;

    @FXML
    private Text boissonTxt;

    
     @FXML
    private Text depTxt;

    @FXML
    private Text netTxt;
        
    String globalDateSearch = getCurrentDate();
    
    //"SELECT SUM(vente.quant) FROM vente, session WHERE vente.idSession = session.idSession AND session.dateSession='2021-08-02' AND vente.typeProduit='Chicha';";
                
    @Override
    public void initialize(URL url, ResourceBundle rb) {
     lyoum.setText(getCurrentDate()); 
     lbareh.setText(reduceOneDay(getCurrentDate())); 

        try {

        
        remplirTab(globalDateSearch);
        remplirDep(globalDateSearch);
        
        } catch (IOException ex) {
            Logger.getLogger(HistoriqueController.class.getName()).log(Level.SEVERE, null, ex);
        }
       
    }    
    
    public String getCurrentDate(){
    String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    return today;
}
    
     public void remplirTab(String dateReq) throws FileNotFoundException, IOException{
         
        String req = "SELECT idSession, dateSession, heureFin, sessionType,"
                + " Payment, Comment, numPost FROM session"
                + " WHERE((dateSession = '"+dateReq+"' and heureFin between '12:00' and '23:59') "
                + "OR (dateSession = '"+addOneDay(dateReq)+"' and heureFin between '00:00' and '11:59')) ORDER BY idSession DESC";
       tableSessions.getItems().clear();
         try {
         connection=dbConnect.getDataBase();
     
            PreparedStatement st = null;
            ResultSet rs = null; 
            st = connection.prepareStatement(req);
            rs = st.executeQuery();
         
         while (rs.next()) {  
                    newData.add(new session(rs.getInt(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getInt(5),rs.getString(6),rs.getInt(7)));  
         }

            id.setCellValueFactory(new PropertyValueFactory<session,Integer>("idSession"));
            date.setCellValueFactory(new PropertyValueFactory<session,String>("dateSession"));
            time.setCellValueFactory(new PropertyValueFactory<session,String>("timeSession"));
            type.setCellValueFactory(new PropertyValueFactory<session,String>("typeSession"));
            num.setCellValueFactory(new PropertyValueFactory<session,Integer>("numPost"));
            payment.setCellValueFactory(new PropertyValueFactory<session,Integer>("payment"));
            note.setCellValueFactory(new PropertyValueFactory<session,String>("noteSession"));
            actions.setCellValueFactory(new PropertyValueFactory<session,HBox>("actions"));

            
           tableSessions.setItems(newData);
           
           int total = 0 ;
        for (session item : tableSessions.getItems()) {
            total = total + item.getPayment();
            }
        recette.setText(total+"");
        
        int total_recu = Integer.parseInt(depTxt.getText());  
        netTxt.setText(total-total_recu+"");
        
        
        String sqlQu = "SELECT SUM(vente.quant) FROM vente, session WHERE "
                + "vente.idSession = session.idSession AND ((session.dateSession='"+dateReq+"' "
                + "and session.heureFin between '12:00' and '23:59') OR (session.dateSession='"+addOneDay(dateReq)+"' "
                + "and session.heureFin between '00:00' and '11:59')) AND vente.typeProduit='Chicha';";

        st = connection.prepareStatement(sqlQu);
        rs = st.executeQuery();
        
        chichaTxt.setText(""+rs.getInt(1));
        
        sqlQu = "SELECT SUM(vente.quant) FROM vente, session WHERE "
                + "vente.idSession = session.idSession AND ((session.dateSession='"+dateReq+"' "
                + "and session.heureFin between '12:00' and '23:59') OR (session.dateSession='"+addOneDay(dateReq)+"' "
                + "and session.heureFin between '00:00' and '11:59')) AND vente.typeProduit='Boisson';";
        
        st = connection.prepareStatement(sqlQu);
        rs = st.executeQuery();
        
        boissonTxt.setText(""+rs.getInt(1));
        
        sqlQu = "SELECT SUM(vente.quant) FROM vente, session WHERE "
                + "vente.idSession = session.idSession AND ((session.dateSession='"+dateReq+"' "
                + "and session.heureFin between '12:00' and '23:59') OR (session.dateSession='"+addOneDay(dateReq)+"' "
                + "and session.heureFin between '00:00' and '11:59')) AND vente.nomProduit='Chicha Menthe';";
        
        st = connection.prepareStatement(sqlQu);
        rs = st.executeQuery();
        
        mintTxt.setText(""+rs.getInt(1));
        
        st.close();
        rs.close();
           connection.close();
      } catch (SQLException ex) {
         Logger.getLogger(NewOrderController.class.getName()).log(Level.SEVERE, null, ex);
     } catch (ClassNotFoundException ex) {
         Logger.getLogger(NewOrderController.class.getName()).log(Level.SEVERE, null, ex);
     }
  }
     
      public void remplirDep(String dateReq) throws FileNotFoundException, IOException{
         
        String req = "SELECT idDep, dateDep, timeDep, montant, comment"
                + " FROM depense"
                + " WHERE((dateDep = '"+dateReq+"' and timeDep between '12:00' and '23:59') "
                + "OR (dateDep = '"+addOneDay(dateReq)+"' and timeDep between '00:00' and '11:59')) ORDER BY idDep DESC";
       tableDep.getItems().clear();
         try {
         connection=dbConnect.getDataBase();
     
            PreparedStatement st = null;
            ResultSet rs = null; 
            st = connection.prepareStatement(req);
            rs = st.executeQuery();
         
         while (rs.next()) {  
                    depData.add(new depense(rs.getInt(1),rs.getString(2),rs.getString(3),rs.getInt(4),rs.getString(5)));  
         }

            idDep.setCellValueFactory(new PropertyValueFactory<depense,Integer>("idDep"));
            dateDep.setCellValueFactory(new PropertyValueFactory<depense,String>("dateDep"));
            timeDep.setCellValueFactory(new PropertyValueFactory<depense,String>("timeDep"));
            montantDep.setCellValueFactory(new PropertyValueFactory<depense,Integer>("montant"));
            noteDep.setCellValueFactory(new PropertyValueFactory<depense,String>("comment"));
            actionsDep.setCellValueFactory(new PropertyValueFactory<depense,HBox>("actions"));

            
           tableDep.setItems(depData);
           
           int total = 0 ;
        for (depense item : tableDep.getItems()) {
            total = total + item.getMontant();
            }
        
        depTxt.setText(total+"");
        int total_recu = Integer.parseInt(recette.getText());  
        netTxt.setText(total_recu-total+"");
        st.close();
        rs.close();
           connection.close();
      } catch (SQLException ex) {
         Logger.getLogger(NewOrderController.class.getName()).log(Level.SEVERE, null, ex);
     } catch (ClassNotFoundException ex) {
         Logger.getLogger(NewOrderController.class.getName()).log(Level.SEVERE, null, ex);
     }
    }
     
     public String getCurrentTime(){
    Date date = new Date();
    date.compareTo(date);
    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");  
    return formatter.format(date);
}
      public String addOneDay(String date) {
          
    return LocalDate.parse(date).plusDays(1).toString();
  }
       public String reduceOneDay(String date) {
          
    return LocalDate.parse(date).minusDays(1).toString();
  }
     

     
         @FXML
    void refresh(ActionEvent event) throws IOException, ParseException {
        remplirTab(globalDateSearch);
        remplirDep(globalDateSearch);
    }
    
    
        @FXML
    void dateSearch(ActionEvent event) throws IOException {
        LocalDate dateToLookFor = datePicker.getValue();
        globalDateSearch = dateToLookFor.toString();
        
        remplirTab(globalDateSearch);
        remplirDep(globalDateSearch);
    }
    
    
    @FXML
    void lyoum(ActionEvent event) throws IOException, ParseException {
        
        SimpleDateFormat sdformat = new SimpleDateFormat("HH:mm");
        Date time2 = sdformat.parse("12:00");
        Date currentTime = sdformat.parse(getCurrentTime());
        LocalDate localDate = LocalDate.parse(getCurrentDate());
        if (currentTime.compareTo(time2) < 0){
          //  System.out.println("ray fatet 00:00");
        }
        datePicker.setValue(localDate);
        lyoum.setText(localDate+"");  
        /*globalDateSearch = getCurrentDate();
        String query = "SELECT idSession, dateSession, heureFin, sessionType,"
                + " Payment, Comment, numPost FROM session"
                + " WHERE((dateSession = '"+globalDateSearch+"' and heureFin between '12:00' and '23:59') "
                + "OR (dateSession = '"+addOneDay(globalDateSearch)+"' and heureFin between '00:00' and '11:59')) ORDER BY idSession DESC";
        
        remplirTab(query);*/
    }
    
    @FXML
    void lbareh(ActionEvent event) throws IOException, ParseException {
        LocalDate localDate = LocalDate.parse(reduceOneDay(getCurrentDate()));
        datePicker.setValue(localDate);
        lbareh.setText(localDate+"");  
    }
    
     @FXML
    void nextDate(ActionEvent event) {
        LocalDate localDate = LocalDate.parse(addOneDay(globalDateSearch));
        datePicker.setValue(localDate);
        lbareh.setText(localDate+"");  
    }


    @FXML
    void previousDate(ActionEvent event) {
        LocalDate localDate = LocalDate.parse(reduceOneDay(globalDateSearch));
        datePicker.setValue(localDate);
        lbareh.setText(localDate+"");  
    }
    
    
    @FXML
    void openDep(ActionEvent event) throws IOException {
        
            if (newWindowStage == null || !newWindowStage.isShowing()){
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("depense.fxml"));
            Parent root = (Parent) fxmlLoader.load();
            newWindowStage  = new Stage();
            
                    newWindowStage.setScene(new Scene(root));
                    newWindowStage.initStyle(StageStyle.DECORATED.UNDECORATED);
                    newWindowStage.setAlwaysOnTop(true);
                    
                    //Open Stage in a specific position
                    newWindowStage.setX(500);
                    newWindowStage.setY(100);
                    
                    newWindowStage.setOnCloseRequest(eventt -> {
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
                            newWindowStage.setX(event.getScreenX() - xOffset);
                            newWindowStage.setY(event.getScreenY() - yOffset);
                        }
                    });
                
                
                    newWindowStage.show(); 
                }else{
                    newWindowStage.toFront();
                }
                    
    }
    
    
     
}
