package pushplaymanager;

import com.jfoenix.controls.JFXButton;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
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
import java.sql.Statement;
import java.sql.Time;
import java.text.DecimalFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import static pushplaymanager.CaisseTableController.bindTooltip;


/**
 * FXML Controller class
 *
 * @author Raouf
 */
public class SessionsController implements Initializable {
static Connection connection = null;

/*
Change Tarife 
*/
int tarifeMulti = 520;
int tarifePC = 120;
static int  tarifePsNormal = 370;
/*
Change Tarife 
*/

    @FXML
     Text chichaText;
    @FXML
     Text chichaTextPs;
    @FXML
     Text chichaTextPc;
    
    @FXML
    Text chichaTextChaise;
    
    @FXML
    Text moneyText;
    
    


DateTimeFormatter formater = DateTimeFormatter.ofPattern("HH:mm:ss");
public static boolean isCaisseOpen = false;

 public static SessionsController instance;
	public SessionsController(){
		instance =this;
	}
	public static SessionsController getInstance(){
		return instance;
	}

        
ObservableList<produit>data=FXCollections.observableArrayList();   
ObservableList<produit>dataPlay=FXCollections.observableArrayList();  
ObservableList<produit>dataTable=FXCollections.observableArrayList();
ObservableList<produit>dataChaise=FXCollections.observableArrayList(); 


ObservableList<session>dataSaveSession=FXCollections.observableArrayList(); 

private double xOffset = 0;
private double yOffset = 0;

private CaissePcController caissePc;
private CaissePlayController caissePlay;
private CaisseTableController caisseTable;
private CaisseChaiseController caisseChaise;
           
@FXML  GridPane gridPlay;
@FXML  GridPane gridTest;
@FXML  GridPane gridTable;
@FXML  GridPane gridChaise;

@FXML
private Label display;

@FXML Pane pane1;


public ObservableList<produit>newOrderData=FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    try {
        gridTest.setTranslateX(0);
        gridTest.setTranslateY(0);
        
        for (int i = 1; i < 3 ; i++) {
            for (int j = 0; j < 5 ; j++) {
                try {
                    addPane(i,j);
                } catch (IOException ex) {
                    Logger.getLogger(SessionsController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        for (int i = 0; i < 3 ; i++) {
            for (int j = 1; j < 3 ; j++) {
                try {
                    addPanePlay(i,j);
                } catch (IOException ex) {
                    Logger.getLogger(SessionsController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        for (int i = 0; i < 6 ; i++) {
            for (int j = 0; j < 3 ; j++) {
                try {
                    addPaneTable(i,j);
                } catch (IOException ex) {
                    Logger.getLogger(SessionsController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        for (int i = 0; i < 7 ; i++) {
            try {
                addPaneChaise(i);
            } catch (IOException ex) {
                Logger.getLogger(SessionsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
      /*  
        dataTable.addListener(new ListChangeListener<produit>() {
            @Override
            public void onChanged(javafx.collections.ListChangeListener.Change<? extends produit> c) {
                System.out.println("Changed on " + c);
                if(c.next()){
                    System.out.println(c.getFrom());
                }
            }
            
        });*/
        
        refreshDb();
        
    } catch (SQLException ex) {
        Logger.getLogger(SessionsController.class.getName()).log(Level.SEVERE, null, ex);
    } catch (ClassNotFoundException ex) {
        Logger.getLogger(SessionsController.class.getName()).log(Level.SEVERE, null, ex);
    } catch (FileNotFoundException ex) {
        Logger.getLogger(SessionsController.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IOException ex) {
        Logger.getLogger(SessionsController.class.getName()).log(Level.SEVERE, null, ex);
    }
    
    }   
    
    public static int pFajitas=300, pmilkshake = 300, pFruit=100, pnutella=150, pAdalia=750, pFruits=200, pMixt=650, pCheeseCh=250, pCascade=350, pCheeseB=300, pFrittes=100, pCafe=100,
            pTacos=400, pMenthe=550, pCrepe=250,pCanette=100,pCanetteG=150,pMokassarat=400, pEau=40, pSghira=40, pTeaM= 80,pCocktail=300, pTeaL= 100, pPancakes=300, pCappuccino = 150, pCappuccinoM=250 ;
    
     public void refreshDb() throws SQLException, ClassNotFoundException, FileNotFoundException, IOException{
         connection=dbConnect.getDataBase();
        
        PreparedStatement st = null;
        ResultSet rs = null;
        st = connection.prepareStatement("SELECT * FROM produit ORDER BY idProduit ASC");
        rs = st.executeQuery();
        newOrderData.clear();
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
            newOrderData.add(new produit(rs.getInt(1),rs.getString(2),rs.getString(3),rs.getInt(4),rs.getInt(5), imagex));
        }
        st.close();
        rs.close();
        connection.close();
         
        newOrderData.forEach(product -> {
                        if (product.getNomProduit().equals("Cappuccino Simple")){
                            pCappuccino = product.getPrix();
                        }
                        if (product.getNomProduit().equals("Mokassarat")){
                            pMokassarat = product.getPrix();
                        }
                        if (product.getNomProduit().equals("Canette")){
                            pCanette = product.getPrix();
                        }
                        if (product.getNomProduit().equals("CanetteG")){
                            pCanetteG = product.getPrix();
                        }
                        if (product.getNomProduit().equals("Cocktail")){
                            pCocktail = product.getPrix();
                        }
                        if (product.getNomProduit().equals("Cappuccino Milka")){
                            pCappuccinoM = product.getPrix();
                        }
                        if (product.getNomProduit().equals("Milk-Shake")){
                            pmilkshake = product.getPrix();
                        }
                        if (product.getNomProduit().equals("Thé Maison")){
                            pTeaM = product.getPrix();
                        }
                        if (product.getNomProduit().equals("Fajitas")){
                            pFajitas = product.getPrix();
                        }
                        if (product.getNomProduit().equals("Chicha adalia")){
                            pAdalia = product.getPrix();
                        }
                        if (product.getNomProduit().equals("+ 3 Fruits")){
                            pFruits = product.getPrix();
                        }
                        if (product.getNomProduit().equals("+ 1 Fruit")){
                            pFruit = product.getPrix();
                        }
                        if (product.getNomProduit().equals("nutella")){
                            pnutella = product.getPrix();
                        }
                        if (product.getNomProduit().equals("Chicha mixte")){
                            pMixt = product.getPrix();
                        }
                        if (product.getNomProduit().equals("Cheese Chicken")){
                            pCheeseCh = product.getPrix();
                        }
                        if (product.getNomProduit().equals("Cascade")){
                            pCascade = product.getPrix();
                        }
                        if (product.getNomProduit().equals("Cheese Burger")){
                            pCheeseB = product.getPrix();
                        }
                        if (product.getNomProduit().equals("Frites")){
                            pFrittes = product.getPrix();
                        }
                        if (product.getNomProduit().equals("café")){
                            pCafe = product.getPrix();
                        }
                        if (product.getNomProduit().equals("Tacos")){
                            pTacos = product.getPrix();
                        }
                        if (product.getNomProduit().equals("Chicha Menthe")){
                            pMenthe = product.getPrix();
                        }
                        if (product.getNomProduit().equals("Crêpe Simple")){
                            pCrepe = product.getPrix();
                        }
                        if (product.getNomProduit().equals("eau 1.5L")){
                            pEau = product.getPrix();
                        }
                        if (product.getNomProduit().equals("eau 0.5L")){
                            pSghira = product.getPrix();
                        }
                        if (product.getNomProduit().equals("Pancakes/Gauffres")){
                            pPancakes = product.getPrix();
                        }
                        });
     }
 
    
 private void addPane(int i, int j) throws IOException {
        DecimalFormat formatter = new DecimalFormat("00");
        Pane pane = new Pane();
        JFXButton sessionStop = new JFXButton();
        sessionStop.getStyleClass().add("cursorHand");
        JFXButton sessionStart = new JFXButton();
        sessionStart.getStyleClass().add("cursorHand");
        JFXButton sessionSettings = new JFXButton();
        sessionSettings.getStyleClass().add("cursorHand");
        
        long[] secondsCounter = {0}; 
        int[] minCounter = {0};
        int[] hCounter = {0};
        int[] amount = {0};
        String[] heureD = {""};
      

        pane.getStyleClass().add("pcPaneOff");
        pane.setPrefSize(130,110);
        gridTest.add(pane, i,j);
       
        int numPoste = gridTest.getRowIndex(pane)*2+gridTest.getColumnIndex(pane);
        Text text = new Text("PC: "+numPoste);
        text.setFont(Font.font("Josefin Sans", FontWeight.BOLD, 18));
        text.setFill(Color.WHITE);
        pane.getChildren().add(text);
        text.setX(32); 
        text.setY(30);
        
        Text timeDisplay = new Text("00:00");
        timeDisplay.setFont(Font.font("Josefin Sans", 16));
        timeDisplay.setFill(Color.WHITE);
        pane.getChildren().add(timeDisplay);
        timeDisplay.setX(10); 
        timeDisplay.setY(60);
        
        
        Circle red = new Circle(5, 0, 12);
        red.setFill(Color.web("#edc600"));
        pane.getChildren().add(red);
        red.setVisible(false);
        
        
        Text productCountLbl = new Text("02");
        productCountLbl.setFont(Font.font("Berlin Sans FB Demi", FontWeight.BOLD, 18));
        productCountLbl.setFill(Color.web("#70001c"));
        pane.getChildren().add(productCountLbl);
        productCountLbl.setX(0); 
        productCountLbl.setY(5);
        productCountLbl.setVisible(false);
        
             
      
        Text amountDisplay = new Text("00 DA");
        amountDisplay.setFont(Font.font("Josefin Sans", FontWeight.BOLD, 18));
        amountDisplay.setFill(Color.WHITE);
        pane.getChildren().add(amountDisplay);
        amountDisplay.setX(10); 
        amountDisplay.setY(83);
        
        
        Boolean[] running = {false};
       
    
    AnimationTimer timer = new AnimationTimer() {
    private long timestamp;
    private long fraction = 0;
      
    
    @Override
    public void start() {
        // current time adjusted by remaining time from last run
        timestamp = System.currentTimeMillis() - 0;
        super.start();
        running[0] = true;
        
        LocalTime heureDebut = LocalTime.now();
        String timeString = heureDebut.format(formater); 
        heureD[0] = timeString;
    }

    @Override
    public void stop() {
        super.stop();
        // save leftover time not handled with the last update
        //fraction = System.currentTimeMillis() - timestamp;
        running[0] = false;
    }

  

    @Override
    public void handle(long now) {
        long newTime = System.currentTimeMillis();
        if (timestamp + 1000 <= newTime) {
            long deltaT = (newTime - timestamp) / 1000;
            secondsCounter[0] += deltaT;
            if (secondsCounter[0] >= 60){
                minCounter[0] ++;
                secondsCounter[0] = 0;
            }
            if (minCounter[0] >= 60){
                hCounter[0] ++;
                minCounter[0] = 0;
            } 
            timestamp += 1000 * deltaT;
            
            /*String uniqueID = UUID.randomUUID().toString();
            System.out.println(uniqueID);*/
            String hFormated = formatter.format(hCounter[0]);
            String minFormated = formatter.format(minCounter[0]);
            timeDisplay.setText(hFormated+":"+minFormated);
            amount[0] = (int) (minCounter[0])*tarifePC/60 + hCounter[0]*tarifePC;
            amountDisplay.setText(amount[0]+" DA");
            if(CaissePcController.getInstance() != null && CaissePcController.getInstance().numPoste == numPoste ){
              CaissePcController.getInstance().amountDisplay.setText(amount[0]+" DA"); 
              CaissePcController.getInstance().timeDisplay.setText(hFormated+":"+minFormated);
              CaissePcController.getInstance().secondsDisplay.setText(formatter.format(secondsCounter[0]));
              CaissePcController.getInstance().displayTotal(amount[0]);
            }      
        }
    }
};
    
  
    
        pane.getChildren().add(sessionStart);
        sessionStart.setTranslateX(86); 
        sessionStart.setTranslateY(15);  
        sessionStart.setPrefSize(30,25);
    FontAwesomeIcon iconStart = new FontAwesomeIcon();
        iconStart.setIconName("PLAY");
        iconStart.setSize("18");
        iconStart.setFill(Color.WHITE);
        sessionStart.setGraphic(iconStart);
    FontAwesomeIcon iconPause = new FontAwesomeIcon();
        iconPause.setIconName("PAUSE");
        iconPause.setSize("18");
        iconPause.setFill(Color.WHITE);
      
    FontAwesomeIcon iconPauseBig = new FontAwesomeIcon();
        iconPauseBig.setIconName("PAUSE");
        iconPauseBig.setSize("25");
        iconPauseBig.setFill(Color.WHITE);    
    FontAwesomeIcon iconStartBig = new FontAwesomeIcon();
        iconStartBig.setIconName("PLAY");
        iconStartBig.setSize("25");
        iconStartBig.setFill(Color.WHITE);

    
        pane.getChildren().add(sessionSettings);
        sessionSettings.setTranslateX(81); 
        sessionSettings.setTranslateY(77);  
        sessionSettings.setPrefSize(30,23);
    FontAwesomeIcon iconSettings = new FontAwesomeIcon();
        iconSettings.setIconName("SHOPPING_CART");
        iconSettings.setSize("23");
        iconSettings.setFill(Color.WHITE);
        sessionSettings.setGraphic(iconSettings);
        
    

    sessionSettings.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!isCaisseOpen){
                    try {
            	    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("caissePc.fxml"));
            	            Parent root = (Parent) fxmlLoader.load();
            	            caissePc = fxmlLoader.getController();
            	            caissePc.fillFields(numPoste, secondsCounter[0],minCounter[0],hCounter[0], amount[0], heureD[0], running[0]);
                            //System.out.print(heureD[0]);
            	            Stage stage = new Stage();
            	            stage.setScene(new Scene(root));  
            	            stage.initStyle(StageStyle.DECORATED.UNDECORATED);
            	            stage.setAlwaysOnTop(true);    
                            //Open Stage in a specific position
                            stage.setX(20); 
                            stage.setY(100); 
                            
                          stage.setOnCloseRequest(eventt -> {
                                    isCaisseOpen = false;
                                    
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
	} catch(Exception e) {
	       e.printStackTrace();
	}
            }
                
            }
        });

    sessionStart.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {  
                if (!running[0]) { 
                        pane.getStyleClass().removeAll("pcPaneOff");
                        pane.getStyleClass().removeAll("pcPanePause"); 
                        pane.getStyleClass().add("pcPaneOn");
                        sessionStart.setGraphic(iconPause);
                        timer.start();
                        sessionStop.setDisable(false);
                        if(CaissePcController.getInstance() != null && CaissePcController.getInstance().numPoste == numPoste ){
                             CaissePcController.getInstance().sessionStop.setDisable(false);
                             CaissePcController.getInstance().sessionStart.setGraphic(iconPauseBig);
                        }
                        
                }else{
                    pane.getStyleClass().removeAll("pcPaneOn");
                    pane.getStyleClass().removeAll("pcPaneOff");
                    pane.getStyleClass().add("pcPanePause");
                    sessionStart.setGraphic(iconStart);
                    timer.stop();
                    if(CaissePcController.getInstance() != null && CaissePcController.getInstance().numPoste == numPoste ){
                             CaissePcController.getInstance().sessionStart.setGraphic(iconStartBig);
                        }
                   // caissePc.fillFields(event, numPoste, secondsCounter[0],minCounter[0],hCounter[0]);
                }  
            }
        });
    
    
        pane.getChildren().add(sessionStop);
        sessionStop.setTranslateX(86); 
        sessionStop.setTranslateY(45);  
        sessionStop.setPrefSize(30,25);
        sessionStop.setDisable(true);
    FontAwesomeIcon iconStop = new FontAwesomeIcon();
        iconStop.setIconName("STOP");
        iconStop.setSize("18");
        iconStop.setFill(Color.WHITE);
        sessionStop.setGraphic(iconStop);
        
    sessionStop.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Alert alert1 = new Alert(Alert.AlertType.CONFIRMATION, "êtes-vous sûr de vouloir arrêter la session ?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
               ((Stage) alert1.getDialogPane().getScene().getWindow()).setAlwaysOnTop(true);
               alert1.showAndWait();
                    if (alert1.getResult() == ButtonType.YES){
                        pane.getStyleClass().removeAll("pcPaneOn");
                        pane.getStyleClass().removeAll("pcPanePause"); 
                        pane.getStyleClass().add("pcPaneOff");
                        sessionStart.setGraphic(iconStart);
                        secondsCounter[0] = 0;
                        minCounter[0] = 0;
                        hCounter[0] = 0;
                        timer.stop();
                        sessionStop.setDisable(true);
                        timeDisplay.setText(formatter.format(minCounter[0])+":"+formatter.format(secondsCounter[0]));
                        amount[0] = 0;
                        amountDisplay.setText("00 DA");
                        
                        
                        int[] chichaCount = {0};
                        chichaCount[0] = Integer.parseInt(chichaTextPc.getText());
                    
                        
                        //Chicha count
                        data.forEach(product -> {
                        if (product.getCurrent() == numPoste && (product.getNomProduit().equals("Chicha fakher") || product.getNomProduit().equals("Chicha adalia") || product.getNomProduit().equals("Chicha mélange"))){
                            System.out.println(product.getBuyQuant());
                            chichaCount[0] -= product.getBuyQuant();
                            chichaTextPc.setText(Integer.toString(chichaCount[0]));
                        }
                        });
                        

                        
                        data.removeIf(p -> p.getCurrent() == numPoste);
                        
                        

                        if(CaissePcController.getInstance() != null && CaissePcController.getInstance().numPoste == numPoste ){
                            CaissePcController.getInstance().amountDisplay.setText(amount[0]+" DA"); 
                            CaissePcController.getInstance().timeDisplay.setText(formatter.format(hCounter[0])+":"+formatter.format(minCounter[0]));
                            CaissePcController.getInstance().secondsDisplay.setText(formatter.format(secondsCounter[0]));
                            CaissePcController.getInstance().displayTotal(amount[0]);
                            CaissePcController.getInstance().sessionStop.setDisable(true);
                            CaissePcController.getInstance().sessionStart.setGraphic(iconStartBig);
                        }
                        
                        productCountLbl.setVisible(false);
                        red.setVisible(false);
                       /*data.forEach(product -> {
                        if (product.getCurrent() == numPoste) System.out.print("yep bruh");
                        });
                        data.removeIf(p -> p.getNomProduit().equals("creppes") && p.getCurrent() == numPoste);
                        */
                }
            }
        }); 
    
    
    pane.setOnMouseClicked(new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent mouseEvent) {
            if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
                if(mouseEvent.getClickCount() == 1){
                    if (!running[0]) {
                        pane.getStyleClass().removeAll("pcPaneOff");
                        pane.getStyleClass().removeAll("pcPanePause"); 
                        pane.getStyleClass().add("pcPaneOn");
                        sessionStart.setGraphic(iconPause);
                        timer.start();
                        sessionStop.setDisable(false);
                        if(CaissePcController.getInstance() != null && CaissePcController.getInstance().numPoste == numPoste ){
                             CaissePcController.getInstance().sessionStop.setDisable(false);
                             CaissePcController.getInstance().sessionStart.setGraphic(iconPauseBig);
                        }
                    }else{
                        pane.getStyleClass().removeAll("pcPaneOn");
                    pane.getStyleClass().removeAll("pcPaneOff");
                    pane.getStyleClass().add("pcPanePause");
                        sessionStart.setGraphic(iconStart);
                        timer.stop();
                        if(CaissePcController.getInstance() != null && CaissePcController.getInstance().numPoste == numPoste ){
                             CaissePcController.getInstance().sessionStart.setGraphic(iconStartBig);
                        }
                        
                    }
                }
            }
        }
        });
    
    FontAwesomeIcon iconPc = new FontAwesomeIcon();
        iconPc.setIconName("DESKTOP");
        iconPc.setSize("18");
        iconPc.setFill(Color.WHITE);
        pane.getChildren().add(iconPc);
        iconPc.setX(5); 
        iconPc.setY(30);
        
        
        
    //END PANE PC    
    }

  
 
 private void addPanePlay(int i, int j) throws IOException {
        DecimalFormat formatter = new DecimalFormat("00");
        Pane pane = new Pane();
        JFXButton sessionStop = new JFXButton();
        sessionStop.getStyleClass().add("cursorHand");
        JFXButton sessionStart = new JFXButton();
        sessionStart.getStyleClass().add("cursorHand");
        JFXButton sessionSettings = new JFXButton();
        sessionSettings.getStyleClass().add("cursorHand");
        
        long[] secondsCounter = {0}; 
        int[] minCounter = {0};
        int[] hCounter = {0};
        int[] amount = {0};

        int[] tarifePost = {tarifePsNormal};
        
        String[] heureD = {""};
        
        
        
        

        pane.getStyleClass().add("psPaneOff");
        pane.setPrefSize(120,110);
        gridPlay.add(pane, i+1,j);
       
        int numPoste = gridPlay.getRowIndex(pane)-2+gridPlay.getColumnIndex(pane)*2;
        Text text = new Text("PS: "+numPoste);
        if (numPoste == 6) {
           text = new Text("VIP");
        }
        text.setFont(Font.font("Josefin Sans", FontWeight.BOLD, 18));
        text.setFill(Color.WHITE);
        pane.getChildren().add(text);
        text.setX(32); 
        text.setY(30);
        
        Text timeDisplay = new Text("00:00");
        timeDisplay.setFont(Font.font("Josefin Sans", 16));
        timeDisplay.setFill(Color.WHITE);
        pane.getChildren().add(timeDisplay);
        timeDisplay.setX(10); 
        timeDisplay.setY(60);
        
        
        Circle red = new Circle(5, 0, 12);
        red.setFill(Color.web("#edc600"));
        pane.getChildren().add(red);
        red.setVisible(false);
        
        
        Text productCountLbl = new Text("02");
        productCountLbl.setFont(Font.font("Berlin Sans FB Demi", FontWeight.BOLD, 18));
        productCountLbl.setFill(Color.web("#70001c"));
        pane.getChildren().add(productCountLbl);
        productCountLbl.setX(0); 
        productCountLbl.setY(5);
        productCountLbl.setVisible(false);
        
             
      
        Text amountDisplay = new Text("00 DA");
        amountDisplay.setFont(Font.font("Josefin Sans", FontWeight.BOLD, 18));
        amountDisplay.setFill(Color.WHITE);
        pane.getChildren().add(amountDisplay);
        amountDisplay.setX(10); 
        amountDisplay.setY(83);
        
        
        Boolean[] running = {false};
    //___________Context_Menu______________________________________________________________
        ContextMenu contextMenu = new ContextMenu();
        MenuItem menuItem1 = new MenuItem("Choice 1");
        MenuItem menuItem2 = new MenuItem("Choice 2");
        MenuItem menuItem3 = new MenuItem("Choice 3");

        menuItem3.setOnAction((event) -> {
            System.out.println("Choice 3 clicked!");
        });
        contextMenu.getItems().addAll(menuItem1,menuItem2,menuItem3);
        pane.setOnMousePressed(new EventHandler<MouseEvent>() {
    @Override
    public void handle(MouseEvent event) {
        if (event.isSecondaryButtonDown()) {
            contextMenu.show(pane, event.getScreenX(), event.getScreenY());
            }
        }
    });
        
    //____________________________________________________________________________________   
    
    AnimationTimer timer = new AnimationTimer() {
    private long timestamp;
    private long fraction = 0;
      
    
    @Override
    public void start() {
        // current time adjusted by remaining time from last run
        timestamp = System.currentTimeMillis() - 0;
        super.start();
        running[0] = true;
        
        LocalTime heureDebut = LocalTime.now();
        String timeString = heureDebut.format(formater); 
        heureD[0] = timeString;
    }

    @Override
    public void stop() {
        super.stop();
        // save leftover time not handled with the last update
        //fraction = System.currentTimeMillis() - timestamp;
        running[0] = false;
    }

  

    @Override
    public void handle(long now) {
        long newTime = System.currentTimeMillis();
        if (timestamp + 1000 <= newTime) {
            long deltaT = (newTime - timestamp) / 1000;
            secondsCounter[0] += deltaT;
            if (secondsCounter[0] >= 60){
                minCounter[0] ++;
                secondsCounter[0] = 0;
            }
            if (minCounter[0] >= 60){
                hCounter[0] ++;
                minCounter[0] = 0;
            } 
            timestamp += 1000 * deltaT;
            
            String hFormated = formatter.format(hCounter[0]);
            String minFormated = formatter.format(minCounter[0]);
            String sFormated = formatter.format(secondsCounter[0]);
            timeDisplay.setText(hFormated+":"+minFormated);
           
            //Turn into seconds
          //timeDisplay.setText(minFormated+":"+sFormated);
          //amount[0] = (int) (secondsCounter[0])*tarifePost[0]/60 + minCounter[0]*tarifePost[0];
          
            amount[0] = (int) (minCounter[0])*tarifePost[0]/60 + hCounter[0]*tarifePost[0];
            amountDisplay.setText(amount[0]+" DA");
            if(CaissePlayController.getInstance() != null && CaissePlayController.getInstance().numPoste == numPoste ){
              CaissePlayController.getInstance().amountDisplay.setText(amount[0]+" DA"); 
              CaissePlayController.getInstance().timeDisplay.setText(hFormated+":"+minFormated);
              //Turn into seconds
             // CaissePlayController.getInstance().timeDisplay.setText(minFormated+":"+sFormated);
              CaissePlayController.getInstance().secondsDisplay.setText(formatter.format(secondsCounter[0]));
              CaissePlayController.getInstance().displayTotal(amount[0]);
            }      
        }
    }
};
    
  
    
        pane.getChildren().add(sessionStart);
        sessionStart.setTranslateX(80); 
        sessionStart.setTranslateY(15);  
        sessionStart.setPrefSize(30,25);
    FontAwesomeIcon iconStart = new FontAwesomeIcon();
        iconStart.setIconName("PLAY");
        iconStart.setSize("18");
        iconStart.setFill(Color.WHITE);
        sessionStart.setGraphic(iconStart);
    FontAwesomeIcon iconPause = new FontAwesomeIcon();
        iconPause.setIconName("PAUSE");
        iconPause.setSize("18");
        iconPause.setFill(Color.WHITE);
      
    FontAwesomeIcon iconPauseBig = new FontAwesomeIcon();
        iconPauseBig.setIconName("PAUSE");
        iconPauseBig.setSize("25");
        iconPauseBig.setFill(Color.WHITE);    
    FontAwesomeIcon iconStartBig = new FontAwesomeIcon();
        iconStartBig.setIconName("PLAY");
        iconStartBig.setSize("25");
        iconStartBig.setFill(Color.WHITE);

    
        pane.getChildren().add(sessionSettings);
        sessionSettings.setTranslateX(80); 
        sessionSettings.setTranslateY(77);  
        sessionSettings.setPrefSize(30,23);
    FontAwesomeIcon iconSettings = new FontAwesomeIcon();
        iconSettings.setIconName("SHOPPING_CART");
        iconSettings.setSize("23");
        iconSettings.setFill(Color.WHITE);
        sessionSettings.setGraphic(iconSettings);
        
    

    sessionSettings.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!isCaisseOpen){
                    try {
            	    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("caissePlay.fxml"));
            	            Parent root = (Parent) fxmlLoader.load();
            	            caissePlay = fxmlLoader.getController();
                            amount[0] = (int) (minCounter[0])*tarifePost[0]/60 + hCounter[0]*tarifePost[0];
            	            caissePlay.fillFields(numPoste, secondsCounter[0],minCounter[0],hCounter[0], amount[0], heureD[0], running[0], tarifePost[0]);
                            //System.out.print(heureD[0]);
            	            Stage stage = new Stage();
            	            stage.setScene(new Scene(root));  
            	            stage.initStyle(StageStyle.DECORATED.UNDECORATED);
            	            stage.setAlwaysOnTop(true);    
                            //Open Stage in a specific position
                            stage.setX(20); 
                            stage.setY(30); 
                            
                          stage.setOnCloseRequest(eventt -> {
                                    isCaisseOpen = false;
                                    
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
	} catch(Exception e) {
	       e.printStackTrace();
	}
            }
                
            }
        });

    sessionStart.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {  
                if (!running[0]) { 
                        pane.getStyleClass().removeAll("psPaneOff");
                        pane.getStyleClass().removeAll("psPanePause"); 
                        pane.getStyleClass().add("psPaneOn");
                        sessionStart.setGraphic(iconPause);
                        timer.start();
                        sessionStop.setDisable(false);
                        if(CaissePlayController.getInstance() != null && CaissePlayController.getInstance().numPoste == numPoste ){
                             CaissePlayController.getInstance().sessionStop.setDisable(false);
                             CaissePlayController.getInstance().sessionStart.setGraphic(iconPauseBig);
                        }
                        
                }else{
                    pane.getStyleClass().removeAll("psPaneOn");
                    pane.getStyleClass().removeAll("psPaneOff");
                    pane.getStyleClass().add("psPanePause");
                    sessionStart.setGraphic(iconStart);
                    timer.stop();
                    if(CaissePlayController.getInstance() != null && CaissePlayController.getInstance().numPoste == numPoste ){
                             CaissePlayController.getInstance().sessionStart.setGraphic(iconStartBig);
                        }
                   // caissePlay.fillFields(event, numPoste, secondsCounter[0],minCounter[0],hCounter[0]);
                }  
            }
        });
    
    
        pane.getChildren().add(sessionStop);
        sessionStop.setTranslateX(80); 
        sessionStop.setTranslateY(45);  
        sessionStop.setPrefSize(30,25);
        sessionStop.setDisable(true);
    FontAwesomeIcon iconStop = new FontAwesomeIcon();
        iconStop.setIconName("STOP");
        iconStop.setSize("18");
        iconStop.setFill(Color.WHITE);
        sessionStop.setGraphic(iconStop);
        
    sessionStop.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Alert alert1 = new Alert(Alert.AlertType.CONFIRMATION, "êtes-vous sûr de vouloir arrêter la session ?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
               ((Stage) alert1.getDialogPane().getScene().getWindow()).setAlwaysOnTop(true);
               alert1.showAndWait();
                    if (alert1.getResult() == ButtonType.YES){
                        pane.getStyleClass().removeAll("psPaneOn");
                        pane.getStyleClass().removeAll("psPanePause"); 
                        pane.getStyleClass().add("psPaneOff");
                        sessionStart.setGraphic(iconStart);
                        secondsCounter[0] = 0;
                        minCounter[0] = 0;
                        hCounter[0] = 0;
                        timer.stop();
                        sessionStop.setDisable(true);
                        timeDisplay.setText(formatter.format(minCounter[0])+":"+formatter.format(secondsCounter[0]));
                        amount[0] = 0;
                        amountDisplay.setText("00 DA");
                        
                       int[] chichaCount = {0};
                        chichaCount[0] = Integer.parseInt(chichaTextPs.getText());
                        int[] moneyCount = {0};
                        moneyCount[0] = Integer.parseInt(moneyText.getText());
                        
                    
                        dataPlay.forEach(product -> {
                        if (product.getCurrent() == numPoste) {
                            moneyCount[0] -= product.getPrix();
                            moneyText.setText(Integer.toString(moneyCount[0]));
                           if (product.getTypeProduit().equals("Chicha")){
                            chichaCount[0] -= product.getBuyQuant();
                            chichaTextPs.setText(Integer.toString(chichaCount[0]));
                        }
                        }
                        });
                        
                        
                        

                        
                        dataPlay.removeIf(p -> p.getCurrent() == numPoste);

                        if(CaissePlayController.getInstance() != null && CaissePlayController.getInstance().numPoste == numPoste ){
                            CaissePlayController.getInstance().amountDisplay.setText(amount[0]+" DA"); 
                            CaissePlayController.getInstance().timeDisplay.setText(formatter.format(hCounter[0])+":"+formatter.format(minCounter[0]));
                            CaissePlayController.getInstance().secondsDisplay.setText(formatter.format(secondsCounter[0]));
                            CaissePlayController.getInstance().displayTotal(amount[0]);
                            CaissePlayController.getInstance().sessionStop.setDisable(true);
                            CaissePlayController.getInstance().sessionStart.setGraphic(iconStartBig);
                        }
                        
                        productCountLbl.setVisible(false);
                        red.setVisible(false);
                        
                }
            }
        }); 
    
    
    pane.setOnMouseClicked(new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent mouseEvent) {
            if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
                if(mouseEvent.getClickCount() == 1){
                    if (!running[0]) {
                        pane.getStyleClass().removeAll("psPaneOff");
                        pane.getStyleClass().removeAll("psPanePause"); 
                        pane.getStyleClass().add("psPaneOn");
                        sessionStart.setGraphic(iconPause);
                        timer.start();
                        sessionStop.setDisable(false);
                        if(CaissePlayController.getInstance() != null && CaissePlayController.getInstance().numPoste == numPoste ){
                             CaissePlayController.getInstance().sessionStop.setDisable(false);
                             CaissePlayController.getInstance().sessionStart.setGraphic(iconPauseBig);
                        }
                    }else{
                        pane.getStyleClass().removeAll("psPaneOn");
                    pane.getStyleClass().removeAll("psPaneOff");
                    pane.getStyleClass().add("psPanePause");
                        sessionStart.setGraphic(iconStart);
                        timer.stop();
                        if(CaissePlayController.getInstance() != null && CaissePlayController.getInstance().numPoste == numPoste ){
                             CaissePlayController.getInstance().sessionStart.setGraphic(iconStartBig);
                        }
                        
                    }
                }
            }
        }
        });       
    

        
   ImageView iconPlay = new ImageView("images/playstation.png");
    pane.getChildren().add(iconPlay);
        iconPlay.setX(5); 
        iconPlay.setY(10);
        
    Text isItMulti = new Text("Multi");
        isItMulti.setFont(Font.font("System",FontWeight.BOLD, FontPosture.ITALIC, 17));
        isItMulti.setFill(Color.YELLOW);
        pane.getChildren().add(isItMulti);
        isItMulti.setX(76); 
        isItMulti.setY(12);  
        isItMulti.setVisible(false);
   
   
        
   JFXButton makeItMulti = new JFXButton();
   pane.getChildren().add(makeItMulti);
   makeItMulti.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                tarifePost[0] = tarifeMulti;
               amount[0] = (int) (minCounter[0])*tarifePost[0]/60 + hCounter[0]*tarifePost[0];
              //   amount[0] = (int) (secondsCounter[0])*tarifePost[0]/60 + minCounter[0]*tarifePost[0];
                amountDisplay.setText(amount[0]+" DA");
                CaissePlayController.getInstance().amountDisplay.setText(amount[0]+" DA"); 
                CaissePlayController.getInstance().displayTotal(amount[0]);
                isItMulti.setVisible(true);
                System.out.println("poste: "+numPoste+" is on Multiplayer Mode, tarife= "+tarifePost[0]);
            }
        });
   makeItMulti.setVisible(false);
   
   JFXButton makeItNormal = new JFXButton();
   pane.getChildren().add(makeItNormal);
   makeItNormal.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                tarifePost[0] = tarifePsNormal;
                amount[0] = (int) (minCounter[0])*tarifePost[0]/60 + hCounter[0]*tarifePost[0];
                //amount[0] = (int) (secondsCounter[0])*tarifePost[0]/60 + minCounter[0]*tarifePost[0];
                amountDisplay.setText(amount[0]+" DA");
                CaissePlayController.getInstance().amountDisplay.setText(amount[0]+" DA"); 
                CaissePlayController.getInstance().displayTotal(amount[0]);
                System.out.println("poste: "+numPoste+" is on Normal Mode, tarife= "+tarifePost[0]);
                isItMulti.setVisible(false);
            }
        });
   makeItNormal.setVisible(false);
   
   
   
   
    Text displayS1 = new Text("false");
        pane.getChildren().add(displayS1);
        displayS1.setVisible(false);
       
   JFXButton resetCounter = new JFXButton();
   pane.getChildren().add(resetCounter);
   resetCounter.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                        secondsCounter[0] = 0;
                        minCounter[0] = 0;
                        hCounter[0] = 0;
                        timeDisplay.setText(formatter.format(minCounter[0])+":"+formatter.format(secondsCounter[0]));
                        //timeDisplay.setText(formatter.format(hCounter[0])+":"+formatter.format(minCounter[0]));
                        amount[0] = 0;
                        amountDisplay.setText("00 DA");

                        if(CaissePlayController.getInstance() != null && CaissePlayController.getInstance().numPoste == numPoste ){
                            CaissePlayController.getInstance().amountDisplay.setText(amount[0]+" DA"); 
                            CaissePlayController.getInstance().timeDisplay.setText(formatter.format(hCounter[0])+":"+formatter.format(minCounter[0]));
                            CaissePlayController.getInstance().secondsDisplay.setText(formatter.format(secondsCounter[0]));
                            CaissePlayController.getInstance().displayTotal(amount[0]);
                        }
            }
        });
   resetCounter.setVisible(false);
   
   
   ImageView multiIcon = new ImageView("images/save.png");
    pane.getChildren().add(multiIcon);
        multiIcon.setX(63); 
        multiIcon.setY(47);
        multiIcon.setFitHeight(20);
        multiIcon.setFitWidth(20);
        multiIcon.setVisible(false);
        
  
  
  JFXButton addTime = new JFXButton();
   pane.getChildren().add(addTime);
   addTime.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                        minCounter[0] += 1;
                        timeDisplay.setText(formatter.format(hCounter[0])+":"+formatter.format(minCounter[0]));

             amount[0] = (int) (minCounter[0])*tarifePost[0]/60 + hCounter[0]*tarifePost[0];
            amountDisplay.setText(amount[0]+" DA");
            if(CaissePlayController.getInstance() != null && CaissePlayController.getInstance().numPoste == numPoste ){
              CaissePlayController.getInstance().amountDisplay.setText(amount[0]+" DA"); 
              CaissePlayController.getInstance().timeDisplay.setText(formatter.format(hCounter[0])+":"+formatter.format(minCounter[0]));
              CaissePlayController.getInstance().secondsDisplay.setText(formatter.format(secondsCounter[0]));
              CaissePlayController.getInstance().displayTotal(amount[0]);
            }
            }
        });
   addTime.setVisible(false);
   
    JFXButton reduceTime = new JFXButton();
   pane.getChildren().add(reduceTime);
   reduceTime.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
            if (minCounter[0] > 0){
                        minCounter[0] -= 1;
                        timeDisplay.setText(formatter.format(hCounter[0])+":"+formatter.format(minCounter[0]));

             amount[0] = (int) (minCounter[0])*tarifePost[0]/60 + hCounter[0]*tarifePost[0];
            amountDisplay.setText(amount[0]+" DA");
            if(CaissePlayController.getInstance() != null && CaissePlayController.getInstance().numPoste == numPoste ){
              CaissePlayController.getInstance().amountDisplay.setText(amount[0]+" DA"); 
              CaissePlayController.getInstance().timeDisplay.setText(formatter.format(hCounter[0])+":"+formatter.format(minCounter[0]));
              CaissePlayController.getInstance().secondsDisplay.setText(formatter.format(secondsCounter[0]));
              CaissePlayController.getInstance().displayTotal(amount[0]);
                        }
                    }
            }
        });
   reduceTime.setVisible(false);
   
   
   JFXButton wait = new JFXButton();
   pane.getChildren().add(wait);
   wait.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
  
            }
        });
   wait.setVisible(false);
   
   //END Pane PS 
    }
 
 
 private void addPaneTable(int i, int j) throws IOException {
        Pane pane = new Pane();
        JFXButton sessionSettings = new JFXButton();
        sessionSettings.getStyleClass().add("cursorHand");
        
        int[] amount = {0};
        

        pane.getStyleClass().add("tablePane");
        pane.setPrefSize(120,70);
        gridTable.add(pane, i+1,j);
       
        int numPoste = gridTable.getRowIndex(pane)*6+gridTable.getColumnIndex(pane);
        Text text = new Text("Table: "+numPoste);
        text.setFont(Font.font("Josefin Sans", FontWeight.BOLD, 18));
        text.setFill(Color.WHITE);
        pane.getChildren().add(text);
        text.setX(40); 
        text.setY(25);
        
        
        Circle red = new Circle(5, 0, 12);
        red.setFill(Color.web("#edc600"));
        pane.getChildren().add(red);
        red.setVisible(false);
        
        
        Text productCountLbl = new Text("02");
        productCountLbl.setFont(Font.font("Berlin Sans FB Demi", FontWeight.BOLD, 18));
        productCountLbl.setFill(Color.web("#70001c"));
        pane.getChildren().add(productCountLbl);
        productCountLbl.setX(0); 
        productCountLbl.setY(5);
        productCountLbl.setVisible(false);
        
             
      
        Text amountDisplay = new Text("0 DA");
        amountDisplay.setFont(Font.font("Josefin Sans", FontWeight.BOLD, 18));
        amountDisplay.setFill(Color.WHITE);
        pane.getChildren().add(amountDisplay);
        amountDisplay.setX(10); 
        amountDisplay.setY(60);
        

       
    
    
  
    
        pane.getChildren().add(sessionSettings);
        sessionSettings.setTranslateX(80); 
        sessionSettings.setTranslateY(38);  
        sessionSettings.setPrefSize(30,23);
    FontAwesomeIcon iconSettings = new FontAwesomeIcon();
        iconSettings.setIconName("SHOPPING_CART");
        iconSettings.setSize("23");
        iconSettings.setFill(Color.WHITE);
        sessionSettings.setGraphic(iconSettings);
        
    
        TextArea noteTxt = new TextArea("");
        pane.getChildren().add(noteTxt);
        noteTxt.setVisible(false);
   
         //___________Context_Menu______________________________________________________________
        ContextMenu contextMenu = new ContextMenu();
        MenuItem menuItem1 = new MenuItem("Choice 1");
        MenuItem menuItem2 = new MenuItem("Choice 2");
        MenuItem menuItem3 = new MenuItem("Choice 3");

        menuItem3.setOnAction((event) -> {
            System.out.println("Choice 3 clicked!");
        });
        contextMenu.getItems().addAll(menuItem1,menuItem2,menuItem3);
        pane.setOnMousePressed(new EventHandler<MouseEvent>() {
    @Override
    public void handle(MouseEvent event) {
        if (event.isSecondaryButtonDown()) {
            contextMenu.show(pane, event.getScreenX(), event.getScreenY());
            }
        }
    });
        
    //____________________________________________________________________________________   
        

    sessionSettings.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!isCaisseOpen){
                    try {
            	    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("caisseTable.fxml"));
            	            Parent root = (Parent) fxmlLoader.load();
            	            caisseTable = fxmlLoader.getController();
            	            caisseTable.fillFields(numPoste,noteTxt.getText());
                            //System.out.print(heureD[0]);
            	            Stage stage = new Stage();
            	            stage.setScene(new Scene(root));  
            	            stage.initStyle(StageStyle.DECORATED.UNDECORATED);
            	            stage.setAlwaysOnTop(true);    
                            //Open Stage in a specific position
                            stage.setX(30); 
                            stage.setY(30); 
                            
                          stage.setOnCloseRequest(eventt -> {
                                    isCaisseOpen = false;
                                    
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
	} catch(Exception e) {
	       e.printStackTrace();
	}
            }
                
            }
        });

    
    
   
    
    pane.setOnMouseClicked(new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent mouseEvent) {
            if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
                if(mouseEvent.getClickCount() == 1){
                    
                }
            }
        }
        });   

    
  
   

    

        
   ImageView iconPlay = new ImageView("images/table.png");
    pane.getChildren().add(iconPlay);
        iconPlay.setX(5); 
        iconPlay.setY(2);
        

        
    Tooltip tooltip = new Tooltip();
    tooltip.setStyle("-fx-font: normal bold 10 Langdon; "
    + "-fx-background-color: WHITE; "
    + "-fx-text-fill: #b56d1b;");

    pane.setOnMouseMoved(new EventHandler<MouseEvent>(){
      @Override  
      public void handle(MouseEvent event) {
        if(!noteTxt.getText().isEmpty()){
            tooltip.show(pane, event.getScreenX(), event.getScreenY() + 15);
        }
      }
   });  
   pane.setOnMouseExited(new EventHandler<MouseEvent>(){
      @Override
      public void handle(MouseEvent event){
         tooltip.hide();
      }
   });
   
   noteTxt.textProperty().addListener(new ChangeListener<String>() {
    @Override
    public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
        tooltip.setText(newValue);
        }
    });
       
   
//End Pane Table
}
 
 
  private void addPaneChaise(int i) throws IOException {
        Pane pane = new Pane();
        JFXButton sessionSettings = new JFXButton();
        sessionSettings.getStyleClass().add("cursorHand");
        
        int[] amount = {0};
        

        pane.getStyleClass().add("chaisePane");
        pane.setPrefSize(120,70);
        gridChaise.add(pane, i+1,0);
       
        //int numPoste = gridChaise.getRowIndex(pane)*1+gridChaise.getColumnIndex(pane);
        int numPoste = i+1;
        Text text = new Text("Chaise: "+numPoste);
        text.setFont(Font.font("Josefin Sans", FontWeight.BOLD, 18));
        text.setFill(Color.WHITE);
        pane.getChildren().add(text);
        text.setX(35); 
        text.setY(25);
        
        
        Circle red = new Circle(5, 0, 12);
        red.setFill(Color.web("#edc600"));
        pane.getChildren().add(red);
        red.setVisible(false);
        
        
        Text productCountLbl = new Text("02");
        productCountLbl.setFont(Font.font("Berlin Sans FB Demi", FontWeight.BOLD, 18));
        productCountLbl.setFill(Color.web("#70001c"));
        pane.getChildren().add(productCountLbl);
        productCountLbl.setX(0); 
        productCountLbl.setY(5);
        productCountLbl.setVisible(false);
        
             
      
        Text amountDisplay = new Text("0 DA");
        amountDisplay.setFont(Font.font("Josefin Sans", FontWeight.BOLD, 18));
        amountDisplay.setFill(Color.WHITE);
        pane.getChildren().add(amountDisplay);
        amountDisplay.setX(15); 
        amountDisplay.setY(60);
        

       
    
    
  
    
        pane.getChildren().add(sessionSettings);
        sessionSettings.setTranslateX(80); 
        sessionSettings.setTranslateY(38);  
        sessionSettings.setPrefSize(30,23);
    FontAwesomeIcon iconSettings = new FontAwesomeIcon();
        iconSettings.setIconName("SHOPPING_CART");
        iconSettings.setSize("23");
        iconSettings.setFill(Color.WHITE);
        sessionSettings.setGraphic(iconSettings);
        
        TextArea noteTxt = new TextArea("");
        pane.getChildren().add(noteTxt);
        noteTxt.setVisible(false);
        

    sessionSettings.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!isCaisseOpen){
                    try {
            	    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("caisseChaise.fxml"));
            	            Parent root = (Parent) fxmlLoader.load();
            	            caisseChaise = fxmlLoader.getController();
            	            caisseChaise.fillFields(numPoste, noteTxt.getText());
                            //System.out.print(heureD[0]);
            	            Stage stage = new Stage();
            	            stage.setScene(new Scene(root));  
            	            stage.initStyle(StageStyle.DECORATED.UNDECORATED);
            	            stage.setAlwaysOnTop(true);    
                            //Open Stage in a specific position
                            stage.setX(50); 
                            stage.setY(20); 
                            
                          stage.setOnCloseRequest(eventt -> {
                                    isCaisseOpen = false;
                                    
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
	} catch(Exception e) {
	       e.printStackTrace();
	}
            }
                
            }
        });

     
    
   
    

        
   ImageView iconPlay = new ImageView("images/chairIcon.png");
    pane.getChildren().add(iconPlay);
        iconPlay.setX(7); 
        iconPlay.setY(8);
        iconPlay.setFitHeight(25);
        iconPlay.setFitWidth(20);
        
        
        
        Tooltip tooltip = new Tooltip();
    tooltip.setStyle("-fx-font: normal bold 10 Langdon; "
    + "-fx-background-color: WHITE; "
    + "-fx-text-fill: #b56d1b;");

    pane.setOnMouseMoved(new EventHandler<MouseEvent>(){
      @Override  
      public void handle(MouseEvent event) {
        if(!noteTxt.getText().isEmpty()){
            tooltip.show(pane, event.getScreenX(), event.getScreenY() + 15);
        }
      }
   });  
   pane.setOnMouseExited(new EventHandler<MouseEvent>(){
      @Override
      public void handle(MouseEvent event){
         tooltip.hide();
      }
   });
   
   noteTxt.textProperty().addListener(new ChangeListener<String>() {
    @Override
    public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
        tooltip.setText(newValue);
        }
    });
    
    }
    
}
