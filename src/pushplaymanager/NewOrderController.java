/* To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pushplaymanager;

import com.jfoenix.controls.JFXTextField;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import pushplaymanager.CaissePcController;


/**
 * FXML Controller class
 *
 * @author Administrator
 */
public class NewOrderController implements Initializable {
Connection connection = null;

@FXML
 Button closeBtn;

public static NewOrderController instance;
	public NewOrderController(){
		instance =this;
	}
	public static NewOrderController getInstance(){
		return instance;
	}


@FXML
     TableView<produit> tableProduct;
        @FXML
    private TableColumn<produit,Integer> id;   
        @FXML
    private TableColumn<produit,String> nomProduit; 
                @FXML
    private TableColumn<produit,String> typeProduit;           
                @FXML
    private TableColumn<produit,Integer> quantity; 
                @FXML
    private TableColumn<produit,Integer> prix;
                @FXML
    private TableColumn<produit, HBox> addSpinner;
                @FXML
    private TableColumn<produit, ImageView> image;
                
     private ChangeListener<String> nameListener;
     public ObservableList<produit>newData=FXCollections.observableArrayList();
         
     @FXML JFXTextField search;
     
     @FXML
    private ChoiceBox<String> choiceBox;
      
    int sizeOfCurrentPost = 0;
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    try {
        CaisseTableController.isNewOrderOpen = true;
        CaissePcController.isNewOrderOpen = true;
        CaissePlayController.isNewOrderOpen = true;
        CaisseChaiseController.isNewOrderOpen = true;
        remplirTab("SELECT * FROM produit ORDER BY idProduit ASC");
        
        search.requestFocus();
        FilteredList<produit> filteredData = new FilteredList<>(SessionsController.getInstance().newOrderData, p -> true);
        
       
        
        //Creation d'un listener 
        nameListener = (observable, oldValue, newValue) -> {
            filteredData.setPredicate(produit -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                if (produit.getNomProduit().toLowerCase().indexOf(lowerCaseFilter) != -1) {
                    return true; // Filter matches first name.
                }else if (Integer.toString(produit.getIdProduit()).toLowerCase().indexOf(lowerCaseFilter) != -1) {
                    return true; // Filter matches last name.
                }
                return false; // Does not match.
            });
        };
        //Ajouter le listener au TextField de recherche
        search.textProperty().addListener(nameListener);
        SortedList<produit> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableProduct.comparatorProperty());
        tableProduct.setItems(sortedData);
        
    } catch (IOException ex) {
        Logger.getLogger(NewOrderController.class.getName()).log(Level.SEVERE, null, ex);
    }
    
     choiceBox.getItems().addAll("All ","Nourriture ","Boisson","Chicha");
        choiceBox.setValue("All");
        /*
        choiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
        System.out.println(choiceBox.getItems().get((Integer) number2));
        
          try {
              remplirTab("SELECT * FROM produit WHERE typeProduit = '"+choiceBox.getItems().get((Integer) number2)+"' ORDER BY idProduit ASC");
          } catch (IOException ex) {
              Logger.getLogger(NewOrderController.class.getName()).log(Level.SEVERE, null, ex);
          }
      }

    });*/
        
    }  
    
    
    public void remplirTab(String req) throws FileNotFoundException, IOException{
        long startTime = System.nanoTime();
       tableProduct.getItems().clear();
       id.setCellValueFactory(new PropertyValueFactory<produit,Integer>("idProduit"));
       nomProduit.setCellValueFactory(new PropertyValueFactory<produit,String>("nomProduit"));
       typeProduit.setCellValueFactory(new PropertyValueFactory<produit,String>("typeProduit"));
       //quantity.setCellValueFactory(new PropertyValueFactory<produit,Integer>("quantity"));
       prix.setCellValueFactory(new PropertyValueFactory<produit,Integer>("prix"));
       addSpinner.setCellValueFactory(new PropertyValueFactory<produit,HBox>("addSpinner"));
       image.setCellValueFactory(new PropertyValueFactory<>("imgView"));
       tableProduct.setItems(SessionsController.getInstance().newOrderData);
       long stopTime = System.nanoTime();
       System.out.println("Temps = "+(stopTime - startTime));
  }
    
    @FXML
    public void clickItem(MouseEvent event) throws IOException{
        long startTime = System.nanoTime();

long stopTime = System.nanoTime();
System.out.println("Temps = "+(stopTime - startTime));
        if (event.getClickCount() == 2) {   /**/
        }   
    }
    
    public void CloseWindow (ActionEvent event){
		((Node)(event.getSource())).getScene().getWindow().hide();
                CaisseTableController.isNewOrderOpen = false;
                CaissePcController.isNewOrderOpen = false;
                CaissePlayController.isNewOrderOpen = false;
                CaisseChaiseController.isNewOrderOpen = false;
	}
    
}
