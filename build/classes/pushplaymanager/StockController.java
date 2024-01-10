/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pushplaymanager;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
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
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

/**
 * FXML Controller class
 *
 * @author Raouf
 */
public class StockController implements Initializable {
Connection connection = null;


public static StockController instance;
	public StockController(){
		instance =this;
	}
	public static StockController getInstance(){
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
    private TableColumn<produit, HBox> action;
                @FXML
    private TableColumn<produit, ImageView> image;
                
     private ChangeListener<String> nameListener;
     public ObservableList<produit>newData=FXCollections.observableArrayList();
     
     @FXML
    private JFXTextField productName;
     @FXML
    private JFXTextField search;

    @FXML
    private Spinner<Integer> spinnerPrice;

    @FXML
    private Spinner<Integer> spinnerQuantity;

    @FXML
    private ChoiceBox<String> choiceBox;

    @FXML
    private ImageView productImage;
    File file; 
    File tempFile = new File("temp.png");
 
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    try {
        remplirTab("SELECT * FROM produit ORDER BY idProduit ASC");
        
        SpinnerValueFactory<Integer> gradesValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0,50000000,0,10);
        spinnerPrice.setValueFactory(gradesValueFactory);
        spinnerPrice.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                spinnerPrice.increment(0);
            }
        });
        
        
        SpinnerValueFactory<Integer> gradesValue = new SpinnerValueFactory.IntegerSpinnerValueFactory(0,50000000,0,10);
        spinnerQuantity.setValueFactory(gradesValue);
        spinnerQuantity.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                spinnerQuantity.increment(0);
            }
        });
        
        choiceBox.getItems().addAll("Nourriture ","Boisson","Chicha");
        choiceBox.setValue("Nourriture");
    } catch (IOException ex) {
        Logger.getLogger(StockController.class.getName()).log(Level.SEVERE, null, ex);
    }
        
        
        
        
    }    
    
    public void remplirTab(String req) throws FileNotFoundException, IOException{   
        tableProduct.setItems(newData);
        tableProduct.getItems().clear();
         try {
         connection=dbConnect.getDataBase();
     
            PreparedStatement st = null;
            ResultSet rs = null; 
            st = connection.prepareStatement(req);
            rs = st.executeQuery();
         
         while (rs.next()) {  
                        InputStream is = rs.getBinaryStream("img");
                    OutputStream os = new FileOutputStream(new File("photo.jpg"));
                    byte[]content = new byte[1024];
                    int size = 0;
                    while((size=is.read(content))!= -1)
                    {
                        os.write(content,0,size);
                    }
                    
                    os.close();
                    is.close();
                    Image imagex = new Image("file:photo.jpg",250,250,true,true);
                    newData.add(new produit(rs.getInt(1),rs.getString(2),rs.getString(3),rs.getInt(4),rs.getInt(5), imagex));  
         }
            st.close();
            rs.close();
            id.setCellValueFactory(new PropertyValueFactory<produit,Integer>("idProduit"));
            nomProduit.setCellValueFactory(new PropertyValueFactory<produit,String>("nomProduit"));
            typeProduit.setCellValueFactory(new PropertyValueFactory<produit,String>("typeProduit"));
            quantity.setCellValueFactory(new PropertyValueFactory<produit,Integer>("quantity"));
            prix.setCellValueFactory(new PropertyValueFactory<produit,Integer>("prix"));
            action.setCellValueFactory(new PropertyValueFactory<>("Actions"));
            image.setCellValueFactory(new PropertyValueFactory<>("imgView"));

           tableProduct.setItems(newData);
           
           connection.close();
           
          
      } catch (SQLException ex) {
         Logger.getLogger(NewOrderController.class.getName()).log(Level.SEVERE, null, ex);
     } catch (ClassNotFoundException ex) {
         Logger.getLogger(NewOrderController.class.getName()).log(Level.SEVERE, null, ex);
     }
         
         FilteredList<produit> filteredData = new FilteredList<>(newData, p -> true);
		
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
  }
    

    
    
        @FXML
    public void clickItem(MouseEvent event){
    if (event.getClickCount() == 1) {   
     productName.setText(tableProduct.getSelectionModel().getSelectedItem().getNomProduit());
     spinnerQuantity.getValueFactory().setValue(tableProduct.getSelectionModel().getSelectedItem().getQuantity());
     spinnerPrice.getValueFactory().setValue(tableProduct.getSelectionModel().getSelectedItem().getPrix());
     choiceBox.setValue(tableProduct.getSelectionModel().getSelectedItem().getTypeProduit());
     productImage.setImage(tableProduct.getSelectionModel().getSelectedItem().getImageP());
        }   
    }

    
    @FXML
    private void update(ActionEvent event) throws IOException, SQLException, ClassNotFoundException {
         Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "confirmer les modifications?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.YES) {
            tableProduct.setEditable(true);
    int selectedIndex = tableProduct.getSelectionModel().getSelectedIndex();
    produit i = tableProduct.getSelectionModel().getSelectedItem();
    String newDesi=productName.getText();
    int newQuant=spinnerQuantity.getValue();
    int newPrix =spinnerPrice.getValue();
    String newCategory=choiceBox.getSelectionModel().getSelectedItem().toString();
      
    if (selectedIndex >= 0) {
        connection=dbConnect.getConnection();
        PreparedStatement preparedStatement;
        
        
        if(file != null){
          preparedStatement = connection.prepareStatement("UPDATE `produit` SET `nomProduit` = '"+newDesi+"', `typeProduit` = '"+newCategory+"', `quantity` = '"+newQuant+"', `prix` = '"+newPrix+"', `img` = ? WHERE `produit`.`idProduit` = "+i.getIdProduit()+";");
         FileInputStream fin = new FileInputStream(file);
         int len = (int)file.length(); 
         preparedStatement.setBinaryStream(1,fin,len);
        }else{
          preparedStatement = connection.prepareStatement("UPDATE `produit` SET `nomProduit` = '"+newDesi+"', `typeProduit` = '"+newCategory+"', `quantity` = '"+newQuant+"', `prix` = '"+newPrix+"' WHERE `produit`.`idProduit` = "+i.getIdProduit()+";");
        }
         
            
            
        preparedStatement.execute();
        connection.close();
        remplirTab("SELECT * FROM produit ORDER BY idProduit ASC");
         SessionsController.getInstance().refreshDb();
        
    } else {
        // Nothing selected.
        Alert alert2= new Alert(Alert.AlertType.WARNING);
        //alert.initOwner(Admins.getPrimaryStage());
        alert2.setTitle("Pas De Selection");
        alert2.setHeaderText("Aucune Case sélectionnée");
        alert2.setContentText("veuillez sélectionner un Produit dans la table.");
        alert2.showAndWait();
    }
        }
        
}
    
  
    @FXML
public void AddNewProduct(ActionEvent event) throws SQLException, ClassNotFoundException, FileNotFoundException, IOException {
    if(productName.getText().isEmpty()||spinnerPrice.getValue()== null ||spinnerQuantity.getValue()== null){
	//Un des Champs est vide
	Alert alert = new Alert(Alert.AlertType.WARNING);
	alert.setTitle("champ vide");
	alert.setHeaderText("un des champs est vide");
	alert.setContentText("veuillez Remplir tout les champs");
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
	alert.showAndWait();
	}else{	
            PreparedStatement preparedStatement=null;
            connection=dbConnect.getDataBase();
            int selectedIndex = tableProduct.getSelectionModel().getSelectedIndex();
            produit i = tableProduct.getSelectionModel().getSelectedItem();
            String newDesi=productName.getText();
            int newQuant=spinnerQuantity.getValue();
            int newPrix =spinnerPrice.getValue();
            String newCategory=choiceBox.getSelectionModel().getSelectedItem().toString();
            
            
            preparedStatement=connection.prepareStatement("INSERT INTO produit (nomProduit, typeProduit ,quantity, prix, img ) VALUES ('"+newDesi+"', '"+newCategory+"', "+spinnerQuantity.getValue()+", "+spinnerPrice.getValue()+", ?);)"); 

         if(file == null){
            //System.out.println(SwingFXUtils.fromFXImage(productImage.getImage(), bf));     
           File tempFile = new File("temp.png");
           BufferedImage bImage = SwingFXUtils.fromFXImage(productImage.getImage(), null); 
           ImageIO.write(bImage, "png", tempFile);
           file = tempFile;
         
         } 
         
            FileInputStream fin = new FileInputStream(file);
            int len = (int)file.length(); 
            preparedStatement.setBinaryStream(1,fin,len); 
            file = null;
         
            
            try {
		
		Alert alert=new Alert (Alert.AlertType.INFORMATION);
		alert.setTitle("Information Dialog");
		alert.setHeaderText(null);
		alert.setContentText(newDesi+" a été ajouté.");
                Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                stage.setAlwaysOnTop(true);
		alert.showAndWait();
		preparedStatement.execute();
		preparedStatement.close();			
		connection.close();
                remplirTab("SELECT * FROM produit ORDER BY idProduit ASC");
                 SessionsController.getInstance().refreshDb();
		} catch (Exception e) {
				System.out.println(e);
		}finally{
			
		}
	}
			
    }


    @FXML
    void changeImage(ActionEvent event) throws IOException {
        FileChooser fc = new FileChooser();
           FileChooser.ExtensionFilter ext1 = new FileChooser.ExtensionFilter("JPG files(*.jpg)","*.JPG");
           FileChooser.ExtensionFilter ext2 = new FileChooser.ExtensionFilter("PNG files(*.png)","*.PNG");
           fc.getExtensionFilters().addAll(ext1,ext2);
           
          
           BufferedImage bf;
           Stage stage = (Stage) ((Node)(event.getSource())).getScene().getWindow();
           file = fc.showOpenDialog(stage);
           
           bf = ImageIO.read(file);
           Image image = SwingFXUtils.toFXImage(bf, null);
                
           productImage.setImage(image);           
    }
    
    
    @FXML
    public void clickAnchor(MouseEvent event){
    if (event.getClickCount() == 1 && event.getButton() == MouseButton.SECONDARY){   
     tableProduct.getSelectionModel().clearSelection(); 
     productName.clear();
     search.clear();
     spinnerQuantity.getValueFactory().setValue(0);
     spinnerPrice.getValueFactory().setValue(0);
        }   
    }
    
}
