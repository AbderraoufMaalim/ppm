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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import static pushplaymanager.CaisseTableController.isNewOrderOpen;


public class produit {
    Connection connection;
    private double xOffset = 0;
    private double yOffset = 0;
    
    public CaissePcController caissePcController;
    public CaissePlayController caissePlayController;
    public CaisseTableController caisseTableController;
    public CaisseChaiseController caisseChaiseController;
    public ValidateSingleController valide;
    
    private NewOrderController newOrderController;
    int sizeOfCurrentPost = 0;
    
        private int idProduit;
        private String nomProduit;
        private String typeProduit;
        private int quantity;
        private int prix;
        private int current;
        private int buyQuant;
        private Image imageP;
        private HBox Actions;
        private HBox addSpinner;
        private ImageView imgView;
        private int tertib;

    public produit(int idProduit, String nomProduit, String typeProduit, int quantity, int prix, Image imageP) {
        this.idProduit = idProduit;
        this.nomProduit = nomProduit;
        this.typeProduit = typeProduit;
        this.quantity = quantity;
        this.prix = prix;
        this.imageP = imageP;
        
        
        
        imgView = new ImageView();
        imgView.setFitWidth(40);
        imgView.setFitHeight(60);
        imgView.setImage(imageP);
        
        
        Spinner<Integer> spinner = new Spinner<Integer>();
        int initialValue = 1;
        SpinnerValueFactory<Integer> valueFactory = //
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, initialValue);
        spinner.setValueFactory(valueFactory);
        
        JFXButton addBtn = new JFXButton();
                addBtn.setPrefWidth(50);
		Image image = new Image(getClass().getResourceAsStream("/images/add.png"),20,20,false,false);
		addBtn.setGraphic(new ImageView(image));
                addBtn.getStylesheets().add("/style/username.css");
                addBtn.getStyleClass().add("addBtn");
                bindTooltip(addBtn,new Tooltip("Ajouter à la caisse"));
		addBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                if (caissePcController.getInstance() != null ){
                Stage stagePc;
                stagePc=(Stage) caissePcController.getInstance().timeDisplay.getScene().getWindow();
                if(stagePc.isShowing()){
                    
                    String name = getNomProduit();
                    String type = getTypeProduit();
                    int quant = spinner.getValue();
                    int price = getPrix()*quant;
                    int current = caissePcController.getInstance().numPoste;
                    sizeOfCurrentPost = 0;
                    SessionsController.getInstance().data.forEach(product -> {
                        if (product.getCurrent() == current){
                            sizeOfCurrentPost++;
                        }          

                    });
                    int tertib = sizeOfCurrentPost;
                    caissePcController.getInstance().addProduct(name, price, current, tertib, quant, imageP, type);
                    
                    }
                }
                if(caissePlayController.getInstance() != null) {
                Stage stagePlay;
                stagePlay=(Stage) caissePlayController.getInstance().timeDisplay.getScene().getWindow();
                if (stagePlay.isShowing()){
                    String name = getNomProduit();
                    String type = getTypeProduit();
                    int quant = spinner.getValue();
                    int price = getPrix()*quant;
                    int current = caissePlayController.getInstance().numPoste;
                    sizeOfCurrentPost = 0;
                    SessionsController.getInstance().dataPlay.forEach(product -> {
                        if (product.getCurrent() == current){
                            sizeOfCurrentPost++;
                        }          
                    });
                    int tertib = sizeOfCurrentPost;
            
                    caissePlayController.getInstance().addProduct(name, price, current, tertib, quant, imageP, type);
                    }
                }
                
                if(caisseTableController.getInstance() != null) {
                Stage stageTable;
                stageTable=(Stage) caisseTableController.getInstance().pcTxt.getScene().getWindow();
                if (stageTable.isShowing()){
                    String name = getNomProduit();
                    String type = getTypeProduit();
                    int quant = spinner.getValue();
                    int price = getPrix()*quant;
                    int current = caisseTableController.getInstance().numPoste;
                    sizeOfCurrentPost = 0;
                    SessionsController.getInstance().dataTable.forEach(product -> {
                        if (product.getCurrent() == current){
                            sizeOfCurrentPost++;
                        }          
                    });
                    int tertib = sizeOfCurrentPost;
                    System.out.println("le type du produit est :"+getTypeProduit());
                    caisseTableController.getInstance().addProduct(name, price, current, tertib, quant, imageP, type);
                    }
                }
                
                if(caisseChaiseController.getInstance() != null) {
                Stage stageChaise;
                stageChaise=(Stage) caisseChaiseController.getInstance().totalPriceText.getScene().getWindow();
                if (stageChaise.isShowing()){
                    String name = getNomProduit();
                     String type = getTypeProduit();
                    int quant = spinner.getValue();
                    int price = getPrix()*quant;
                    int current = caisseChaiseController.getInstance().numPoste;
                    sizeOfCurrentPost = 0;
                    SessionsController.getInstance().dataChaise.forEach(product -> {
                        if (product.getCurrent() == current){
                            sizeOfCurrentPost++;
                        }          
                    });
                    int tertib = sizeOfCurrentPost;
            
                    caisseChaiseController.getInstance().addProduct(name, price, current, tertib, quant, imageP, type);
                    }
                }
                
            
            }
        });
                
                JFXButton deleteBtn = new JFXButton();
                deleteBtn.setCursor(Cursor.HAND);
                deleteBtn.setPrefWidth(50);
		Image imageDel = new Image(getClass().getResourceAsStream("/images/deleteBlack.png"),20,20,false,false);
		deleteBtn.setGraphic(new ImageView(imageDel));
                bindTooltip(deleteBtn,new Tooltip("Supprimer"));
		//editbtn.setTooltip();
		deleteBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Alert alert1 = new Alert(Alert.AlertType.CONFIRMATION, "êtes-vous sûr de vouloir Supprimer ce produit?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
               ((Stage) alert1.getDialogPane().getScene().getWindow()).setAlwaysOnTop(true);
               alert1.showAndWait();
                    if (alert1.getResult() == ButtonType.YES){
                    try {
                        connection = dbConnect.getDataBase();
                        PreparedStatement preparedStatement=null;
                        String query="delete from produit where idProduit="+getIdProduit()+";";
                        preparedStatement=connection.prepareStatement(query);
                        //preparedStatement.setString(1,PatientNumber);
                        preparedStatement.executeUpdate();
                        preparedStatement.close();
                        StockController.getInstance().remplirTab("SELECT * FROM produit ORDER BY idProduit ASC");
                        SessionsController.getInstance().refreshDb();
                    } catch (SQLException ex) {
                        Logger.getLogger(produit.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(produit.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(produit.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    }
            }
        });
                
        this.Actions = new HBox(deleteBtn);
	Actions.setAlignment(Pos.CENTER);
        Actions.setPrefWidth(165);
	Actions.setSpacing(10);         
 
                
        
       
        this.addSpinner = new HBox(addBtn,spinner);
	addSpinner.setAlignment(Pos.CENTER);
        addSpinner.setPrefWidth(250);
	addSpinner.setSpacing(5); 
 
    }
    
    
    

    public produit(String nomProduit, int prix, int current, int tertib, int buyQuant, Image imageP, String typeProduit) {
        this.nomProduit = nomProduit;
        this.prix = prix;
        this.current = current;
        this.tertib = tertib;
        this.buyQuant = buyQuant;
        this.typeProduit = typeProduit;
        
        
        imgView = new ImageView();
        imgView.setFitWidth(60);
        imgView.setFitHeight(60);
        imgView.setImage(imageP);
        
         JFXButton deleteBtn = new JFXButton();
                deleteBtn.setPrefWidth(50);
                deleteBtn.setCursor(Cursor.HAND);
		Image image = new Image(getClass().getResourceAsStream("/images/delete.png"),20,20,false,false);
		deleteBtn.setGraphic(new ImageView(image));
                bindTooltip(deleteBtn,new Tooltip("Supprimer"));
		//editbtn.setTooltip();

		deleteBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                
                if (caissePcController.getInstance() != null){
                    Stage stagePc;
                stagePc=(Stage) caissePcController.getInstance().timeDisplay.getScene().getWindow();
                if(stagePc.isShowing()){
                    caissePcController.getInstance().removeProduct(getNomProduit(), getPrix(), getCurrent(),getTertib(),getTypeProduit());
                    }
                }
                
                if (caissePlayController.getInstance() != null){
                    Stage stagePlay;
                stagePlay=(Stage) caissePlayController.getInstance().timeDisplay.getScene().getWindow();
                if (stagePlay.isShowing()){
                     caissePlayController.getInstance().removeProduct(getNomProduit(), getPrix(), getCurrent(),getTertib(),getTypeProduit());
                    }
                }
                
                if(caisseTableController.getInstance() != null) {
                  Stage stageTable;
                stageTable=(Stage) caisseTableController.getInstance().pcTxt.getScene().getWindow();
                if (stageTable.isShowing()){
                     caisseTableController.getInstance().removeProduct(getNomProduit(), getPrix(), getCurrent(),getTertib(),getTypeProduit());
                    }
                }
                
                if(caisseChaiseController.getInstance() != null) {
                  Stage stageChaise;
                stageChaise=(Stage) caisseChaiseController.getInstance().totalPriceText.getScene().getWindow();
                if (stageChaise.isShowing()){
                     caisseChaiseController.getInstance().removeProduct(getNomProduit(), getPrix(), getCurrent(),getTertib(),getTypeProduit());
                    }
                }
                   
            }
        });
                
        JFXButton addBtn = new JFXButton();
        addBtn.setPrefWidth(50);
        addBtn.setCursor(Cursor.HAND);
	Image addImage = new Image(getClass().getResourceAsStream("/images/addWhite.png"),20,20,false,false);
	addBtn.setGraphic(new ImageView(addImage));
        bindTooltip(addBtn,new Tooltip("Ajouter")); 
        
        int prixUnit = getPrix()/getBuyQuant();
        addBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                
                if (caissePcController.getInstance() != null){
                    Stage stagePc;
                stagePc=(Stage) caissePcController.getInstance().timeDisplay.getScene().getWindow();
                if(stagePc.isShowing()){
                    caissePcController.getInstance().addProduct(getNomProduit(), prixUnit, getCurrent(),getTertib(),1, imageP,getTypeProduit());
                    }
                }
                
                if (caissePlayController.getInstance() != null){
                    Stage stagePlay;
                stagePlay=(Stage) caissePlayController.getInstance().timeDisplay.getScene().getWindow();
                if (stagePlay.isShowing()){
                     caissePlayController.getInstance().addProduct(getNomProduit(), prixUnit, getCurrent(),getTertib(),1, imageP,getTypeProduit());
                    }
                }
                
                if(caisseTableController.getInstance() != null) {
                  Stage stageTable;
                stageTable=(Stage) caisseTableController.getInstance().pcTxt.getScene().getWindow();
                if (stageTable.isShowing()){
                     caisseTableController.getInstance().addProduct(getNomProduit(), prixUnit, getCurrent(),getTertib(),1, imageP, getTypeProduit());
                    }
                }
                
                if(caisseChaiseController.getInstance() != null) {
                  Stage stageChaise;
                stageChaise=(Stage) caisseChaiseController.getInstance().totalPriceText.getScene().getWindow();
                if (stageChaise.isShowing()){
                     caisseChaiseController.getInstance().addProduct(getNomProduit(), prixUnit, getCurrent(),getTertib(),1, imageP,getTypeProduit());
                    }
                }
                   
            }
        });
        
        
        JFXButton valideBtn = new JFXButton();
        valideBtn.setPrefWidth(50);
        valideBtn.setCursor(Cursor.HAND);
	Image valideImage = new Image(getClass().getResourceAsStream("/images/valideBtn.png"),20,20,false,false);
	valideBtn.setGraphic(new ImageView(valideImage));
        bindTooltip(valideBtn,new Tooltip("Valider")); 
        
        valideBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Alert alert1 = new Alert(Alert.AlertType.CONFIRMATION, "Valider "+getNomProduit()+"?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
               ((Stage) alert1.getDialogPane().getScene().getWindow()).setAlwaysOnTop(true);
               alert1.showAndWait();
                    if (alert1.getResult() == ButtonType.YES){
                    try {
                        //FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("validateSingle.fxml"));
                        //Parent root = (Parent) fxmlLoader.load();
                        // valide = fxmlLoader.getController();
                        // valide.fillFields(getPrix(),getNomProduit(),getBuyQuant(),getCurrent(),getTypeProduit());
                        
                        int price = getPrix()/getBuyQuant();
                        String sessionType = null;
                         
                        
                        if (caissePlayController.getInstance() != null){
                            Stage stagePlay;
                            stagePlay=(Stage) caissePlayController.getInstance().timeDisplay.getScene().getWindow();
                            if (stagePlay.isShowing()){
                                caissePlayController.getInstance().removeProduct(getNomProduit(), getPrix(), getCurrent(),getTertib(),getTypeProduit());
                                sessionType = "Play";
                            }
                        }
                        if(caisseTableController.getInstance() != null) {
                            Stage stageTable;
                            stageTable=(Stage) caisseTableController.getInstance().pcTxt.getScene().getWindow();
                            if (stageTable.isShowing()){
                                caisseTableController.getInstance().removeProduct(getNomProduit(), getPrix(), getCurrent(),getTertib(),getTypeProduit());
                                sessionType = "Table";
                            }
                        }
                        if(caisseChaiseController.getInstance() != null) {
                            Stage stageChaise;
                            stageChaise=(Stage) caisseChaiseController.getInstance().totalPriceText.getScene().getWindow();
                            if (stageChaise.isShowing()){
                                caisseChaiseController.getInstance().removeProduct(getNomProduit(), getPrix(), getCurrent(),getTertib(),getTypeProduit());
                                sessionType = "Chaise";
                            }
                        }
                       
                        String heureF = getCurrentTime();
                        String date = getCurrentDate();
                        
                        connection=dbConnect.getDataBase();
                        
                        String sql = "INSERT INTO session (dateSession, heureDebut, heureFin, clientId, sessionType, "
                                + "Payment, Comment, client, idUser, duration, numPost) VALUES "
                                + "('"+date+"', '', '"+heureF+"', '1', '"+sessionType+"', '"+price+"', '"+sessionType+" "+getCurrent()+"', "
                                + "'Guest', 1, '' ,'"+getCurrent()+"');";
                        
                        PreparedStatement preparedStatement = connection.prepareStatement(sql);
                        preparedStatement.execute();
                        
                        sql= "SELECT last_insert_rowid();";
                        ResultSet rs = null;
                        
                        preparedStatement = connection.prepareStatement(sql);
                        
                        rs = preparedStatement.executeQuery();
                        int idSess = rs.getInt(1);
                        
                        connection=dbConnect.getDataBase();
                        PreparedStatement preparedSt= connection.prepareStatement("INSERT INTO vente "
                                + "(nomProduit, typeProduit, prixVente, quant, idSession) "
                                + "VALUES('"+getNomProduit()+"','"+getTypeProduit()+"', "+price+" ,1, "+idSess+");");
                        
                        preparedSt.execute();
                        
                        connection.close();
                        
                        
                        System.out.println("Validé avec succes");
                    } catch (SQLException ex) {
                        Logger.getLogger(produit.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(produit.class.getName()).log(Level.SEVERE, null, ex);
                    }
            }
          }
        });
        
                
                
        this.Actions = new HBox(addBtn,valideBtn,deleteBtn);
	Actions.setAlignment(Pos.CENTER);
        Actions.setPrefWidth(300);
	Actions.setSpacing(10);         
 
    }



     public String getCurrentDate(){
    String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    return today;
}

public String getCurrentTime(){
    Date date = new Date();
    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");  
    return formatter.format(date);
}

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public Image getImageP() {
        return imageP;
    }

    public void setImageP(Image imageP) {
        this.imageP = imageP;
    }

    
  

    public int getIdProduit() {
        return idProduit;
    }

    public void setIdProduit(int idProduit) {
        this.idProduit = idProduit;
    }

    public String getNomProduit() {
        return nomProduit;
    }

    public void setNomProduit(String nomProduit) {
        this.nomProduit = nomProduit;
    }

    public String getTypeProduit() {
        return typeProduit;
    }

    public void setTypeProduit(String typeProduit) {
        this.typeProduit = typeProduit;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getBuyQuant() {
        return buyQuant;
    }

    public void setBuyQuant(int buyQuant) {
        this.buyQuant = buyQuant;
    }



    public int getPrix() {
        return prix;
    }

    public void setPrix(int prix) {
        this.prix = prix;
    }

    
    public ImageView getImgView() {
        return imgView;
    }

    public void setImgView(ImageView imgView) {
        this.imgView = imgView;
    }

    
    public HBox getActions() {
        return Actions;
    }

    public void setActions(HBox Actions) {
        this.Actions = Actions;
    }

    public HBox getAddSpinner() {
        return addSpinner;
    }

    public void setAddSpinner(HBox addSpinner) {
        this.addSpinner = addSpinner;
    }

    
    public int getTertib() {
        return tertib;
    }

    public void setTertib(int tertib) {
        this.tertib = tertib;
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
