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
import java.util.Calendar;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;


/**
 * FXML Controller class
 *
 * @author Raouf
 */
public class StatsController implements Initializable {

    Connection connection = null;
    
    @FXML
    private LineChart<?, ?> chart;
    
    Calendar cal = Calendar.getInstance();
    int m = cal.get(Calendar.MONTH)+1;
    
    String Currentmonth;
    String Currentyear= Integer.toString(cal.get(Calendar.YEAR));
    @FXML
    private Text recette;

    @FXML
    private Text depTxt;

    @FXML
    private Text netTxt;
    
    
    @FXML
    private HBox parPeriode;

    @FXML
    private HBox parMois;
    @FXML
    private ChoiceBox<String> monthChoice;
    @FXML
    private Spinner<Integer> yearSpinner;
    
    public ObservableList<session>recetteData=FXCollections.observableArrayList();
    
    
    public String getCurrentMonth() {
        String[] monthName = {"Janvier","Février","Mars","Avril","Mai",
            "Juin","Juillet","Août","Septembre",
            "Octobre","Novembre ","Décembre"};

        Calendar cal = Calendar.getInstance();
        String month = monthName[cal.get(Calendar.MONTH)];
        
        return month;
    }
    
    public int getCurrentYear(){
    Calendar cal = Calendar.getInstance();
    int year  = cal.get(Calendar.YEAR);
    return year;
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
       
        try {
            if (m < 10){
                Currentmonth = "0"+Integer.toString(m);
            }else{
                Currentmonth = Integer.toString(m);
            }
            remplirChart(Currentmonth, Currentyear);
            monthChoice.getItems().addAll("Janvier","Février","Mars","Avril",
                    "Mai","Juin","Juillet","Août","Septembre",
                    "Octobre","Novembre ","Décembre");
            monthChoice.setValue(getCurrentMonth());
            monthChoice.getSelectionModel()
                    .selectedItemProperty()
                    .addListener( (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                        
                        try {
                            String a = Integer.toString(monthChoice.getSelectionModel().selectedIndexProperty().getValue()+1);
                            if((monthChoice.getSelectionModel().selectedIndexProperty().getValue()+1) < 10){
                                remplirChart("0"+a, Currentyear);
                                System.out.println("0"+a+"/"+Currentyear);
                            }else{
                                remplirChart(a, Currentyear);
                                System.out.println(a+"/"+Currentyear);
                            }
                            
                        } catch (SQLException ex) {
                            Logger.getLogger(StatsController.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (ClassNotFoundException ex) {
                            Logger.getLogger(StatsController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        
                    } );
            SpinnerValueFactory<Integer> gradesValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(2010,2040,getCurrentYear());
            yearSpinner.setValueFactory(gradesValueFactory);
            yearSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
                
                try {
                    Currentyear = Integer.toString(newValue);
                     String a = Integer.toString(monthChoice.getSelectionModel().selectedIndexProperty().getValue()+1);
                            if((monthChoice.getSelectionModel().selectedIndexProperty().getValue()+1) < 10){
                                remplirChart("0"+a, Currentyear);
                                System.out.println("0"+a+"/"+Currentyear);
                            }else{
                                remplirChart(a, Currentyear);
                                System.out.println(a+"/"+Currentyear);
                            }
                            
                } catch (SQLException ex) {
                    Logger.getLogger(StatsController.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(StatsController.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            });
        } catch (SQLException ex) {
            Logger.getLogger(StatsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(StatsController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    
    
    
    

    void remplirChart(String month, String year) throws SQLException, ClassNotFoundException {
        
       int rec = 0;
       int dep = 0;
       String req = "SELECT SUM(Payment) FROM session WHERE strftime('%m', `dateSession`) = '"+month+"' and strftime('%Y', `dateSession`) = '"+year+"'";
       connection=dbConnect.getDataBase();
     
            PreparedStatement st = null;
            ResultSet rs = null; 
            st = connection.prepareStatement(req);
            rs = st.executeQuery();
         
            rec = rs.getInt(1);
         recette.setText(rec+"");
        
         
        req = "SELECT  SUM(montant) FROM depense WHERE strftime('%m', `dateDep`) = '"+month+"' and strftime('%Y', `dateDep`) = '"+year+"'";
        connection=dbConnect.getDataBase();
        PreparedStatement st2 = null;
        ResultSet rs2 = null; 
            st2 = connection.prepareStatement(req);
            rs2 = st2.executeQuery();
         dep = rs2.getInt(1);
         depTxt.setText(dep+"");
         
        netTxt.setText((rec-dep)+"");
        st2.close();
        rs2.close();
        st.close();
        rs.close();
        connection.close();
        
        chart.getData().removeAll(chart.getData());
         chart.getData().removeAll();
        XYChart.Series series = new XYChart.Series(); 
        series.setName("Revenues");
        
        
        connection=dbConnect.getDataBase();
             st = null;
             rs = null; 
            st = connection.prepareStatement("SELECT dateSession, heureFin, SUM(Payment) as res\n" +
"FROM session \n" +
"WHERE strftime('%m', `dateSession`) = '"+month+"'"
        + "and strftime('%Y', `dateSession`) = '"+year+"' \n" +
"GROUP BY dateSession");

            rs = st.executeQuery();
         
         while (rs.next()) {         
             series.getData().add(new XYChart.Data(rs.getString("dateSession"), rs.getInt("res"))); 
         }
         st.close();
         rs.close();
         connection.close();
        
        chart.getData().add(series);
        
        for (XYChart.Series<?, ?> s : chart.getData()) {
            for (XYChart.Data<?, ?> d : s.getData()) {   
                bindTooltip(d.getNode(), new Tooltip(      
                        d.getXValue().toString() + "\n" +
                                  d.getYValue() + " DA")
                );
            }
        }
        
    }
    
    
     public void remplirChartVisit(String dateReq) throws SQLException, ClassNotFoundException{
         
       /* 
        SELECT dateSession, heureFin, SUM(Payment)
FROM session 
WHERE strftime('%m', `dateSession`) = '08' and heureFin between '12:00' and '23:59'
GROUP BY dateSession
        
            connection=dbConnect.getDataBase();
            PreparedStatement st = null;
            ResultSet rs = null; 
            st = connection.prepareStatement(req);
            rs = st.executeQuery();
         
         while (rs.next()) {         
             series.getData().add(new XYChart.Data(rs.getString("Date"), rs.getInt("NumberVisits"))); 
         }
         st.close();
         rs.close();
         connection.close();
        chart.getData().add(series); 
       for (XYChart.Series<?, ?> s : chart.getData()) {
            for (XYChart.Data<?, ?> d : s.getData()) {   
                bindTooltip(d.getNode(), new Tooltip(      
                        d.getXValue().toString() + "\n" +
                                "Nombre de Visites : " + d.getYValue())
                );
            }
        }*/
    }
    
     @FXML
    void moisRadio(ActionEvent event) {
        parMois.setVisible(true);  
        parPeriode.setVisible(false);
    }

    @FXML
    void periodeRadio(ActionEvent event) {
        parMois.setVisible(false);  
        parPeriode.setVisible(true);
    }
    
    
    public static void bindTooltip(final Node node, final Tooltip tooltip){
   node.setOnMouseMoved(new EventHandler<MouseEvent>(){
      @Override  
      public void handle(MouseEvent event) {
         // +15 moves the tooltip 15 pixels below the mouse cursor;
         // if you don't change the y coordinate of the tooltip, you
         // will see constant screen flicker
         tooltip.show(node, event.getScreenX(), event.getScreenY() + 15);
      }
   });  
   node.setOnMouseExited(new EventHandler<MouseEvent>(){
      @Override
      public void handle(MouseEvent event){
         tooltip.hide();
      }
   });
}
    
}
