/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pushplaymanager;


import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXRadioButton;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import static java.awt.print.Printable.NO_SUCH_PAGE;
import static java.awt.print.Printable.PAGE_EXISTS;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.FileNotFoundException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.AnimationTimer;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.print.Printer;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javax.swing.ImageIcon;
import static jdk.nashorn.internal.objects.NativeRegExp.source;
import static pushplaymanager.SessionsController.isCaisseOpen;
import static pushplaymanager.SessionsController.pAdalia;
import static pushplaymanager.SessionsController.pCafe;
import static pushplaymanager.SessionsController.pCheeseB;
import static pushplaymanager.SessionsController.pCrepe;
import static pushplaymanager.SessionsController.pEau;
import static pushplaymanager.SessionsController.pFajitas;
import static pushplaymanager.SessionsController.pFruits;
import static pushplaymanager.SessionsController.pFruit;
import static pushplaymanager.SessionsController.pnutella;
import static pushplaymanager.SessionsController.pMenthe;
import static pushplaymanager.SessionsController.pMixt;
import static pushplaymanager.SessionsController.pmilkshake;
import static pushplaymanager.SessionsController.pPancakes;
import static pushplaymanager.SessionsController.pCheeseCh;
import static pushplaymanager.SessionsController.pSghira;
import static pushplaymanager.SessionsController.pTacos;
import static pushplaymanager.SessionsController.pFrittes;
import static pushplaymanager.SessionsController.pCascade;
import static pushplaymanager.SessionsController.pTeaM;
import static pushplaymanager.SessionsController.pTeaL;
import static pushplaymanager.SessionsController.pCappuccino;
import static pushplaymanager.SessionsController.pCappuccinoM;
import static pushplaymanager.SessionsController.pCocktail;
import static pushplaymanager.SessionsController.pCanette;
import static pushplaymanager.SessionsController.pCanetteG;
import static pushplaymanager.SessionsController.pMokassarat;
/**
 * FXML Controller class
 *
 * @author Raouf
 */
public class CaissePlayController implements Initializable {
Connection connection = null;
    private double xOffset = 0;
    private double yOffset = 0;
    public static boolean isNewOrderOpen = false;
    public int numPoste;
    public boolean running = true;
    
    DateTimeFormatter formater = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    @FXML
    private TableView<produit> tableProduct;
        @FXML
    private TableColumn<produit,String> nomProduit;      
                @FXML
    private TableColumn<produit,Integer> buyQuant; 
                @FXML
    private TableColumn<produit,Integer> prixUnit;
                @FXML
    private TableColumn<produit,Integer> prix;
                @FXML
    private TableColumn<produit, HBox> action;
                @FXML
    private TableColumn<produit, ImageView> image;
                
     private ChangeListener<String> nameListener;
     public ObservableList<produit>newData=FXCollections.observableArrayList();
     
     
 @FXML
 JFXButton sessionStart;

@FXML
 JFXButton sessionStop;




int chichaCount = Integer.parseInt(SessionsController.getInstance().chichaTextPs.getText());
int moneyCount = Integer.parseInt(SessionsController.getInstance().moneyText.getText());


@FXML
private JFXRadioButton tarifNormalRadio;
@FXML
private JFXRadioButton tarifMultiRadio;

    public static CaissePlayController instance;
	public CaissePlayController(){
		instance =this;
	}
	public static CaissePlayController getInstance(){
		return instance;
	}
        
 DecimalFormat formatter = new DecimalFormat("00");
 
 @FXML public Text pcTxt;
 @FXML   Text timeDisplay;
 @FXML   Text amountDisplay;
 @FXML   Text secondsDisplay;

 @FXML private Text totalCons;
 
 private int sessionAmount=0;
 private int totalPrice=0;
 @FXML  TextField totalPriceText;
 
 
 long[] secondsCounter = {0}; 
 int[] minCounter = {0};
 int[] hCounter = {0};
 String heureDebut;
 
 
 /* 
 Save Session Nodes 
 */
 
 
  @FXML
 private Pane paneS1;
 
 @FXML
    private Text timeDisplayS1;

    @FXML
    private Text amountDisplayS1;

    @FXML
    private Text multiS1;
   private int amountSaved=0;
 
 /* 
 ___________________________ 
 */


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        isCaisseOpen = true;
        
        nomProduit.setCellValueFactory(new PropertyValueFactory<produit,String>("nomProduit"));
        prix.setCellValueFactory(new PropertyValueFactory<produit,Integer>("prix"));
        buyQuant.setCellValueFactory(new PropertyValueFactory<produit,Integer>("buyQuant"));
        action.setCellValueFactory(new PropertyValueFactory<>("Actions"));
        image.setCellValueFactory(new PropertyValueFactory<>("imgView"));
        
        
       
        
        tarifNormalRadio.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                Pane nn = (Pane) SessionsController.getInstance().gridPlay.getChildren().get(numPoste-1);
                JFXButton btnMulti = (JFXButton) nn.getChildren().get(10);
                JFXButton btnNormal = (JFXButton) nn.getChildren().get(11);
           
                 if (oldValue) { 
                    //SessionsController.tarifePS = 400;
                    btnMulti.fire();
                } else {
                    //SessionsController.tarifePS = 300;
                    btnNormal.fire();
                    
                }
            }
    });
        
        
         tableProduct.setPlaceholder(new Label(" "));
    }    
    
    
    
    public void saveSession (ActionEvent event){
        
            Alert alert1 = new Alert(Alert.AlertType.CONFIRMATION, "Sauvegarder la session?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
               ((Stage) alert1.getDialogPane().getScene().getWindow()).setAlwaysOnTop(true);
               alert1.showAndWait();
                    if (alert1.getResult() == ButtonType.YES){
                  
            SessionsController.getInstance().dataSaveSession.removeIf(s  ->  s.getCurrent() == numPoste);
         
        int idSession = 1;
        String durationSession = timeDisplay.getText();
        String amountSession = amountDisplay.getText();
        String typeSession;
               paneS1.setVisible(true);
               
               timeDisplayS1.setText(durationSession);
               amountDisplayS1.setText(amountSession);
               if (tarifNormalRadio.isSelected()){
                   typeSession = "Normal";
                   multiS1.setText("Normal");
               }else{
                   typeSession = "Multi";
                    multiS1.setText("Multi");
               }
               
               
           SessionsController.getInstance().dataSaveSession.add(new session(idSession, durationSession, sessionAmount, typeSession, numPoste));  
           amountSaved = sessionAmount;
           System.out.println(amountSaved); 
           displayTotal(amountSaved); 
           
           Pane nn = (Pane) SessionsController.getInstance().gridPlay.getChildren().get(numPoste-1);
            JFXButton resetBtn = (JFXButton) nn.getChildren().get(13);
            resetBtn.fire();
                       
           Text displayS1 = (Text) nn.getChildren().get(12);
           displayS1.setText("true");
            
            ImageView multiIcon= (ImageView) nn.getChildren().get(14);
            multiIcon.setVisible(true);
            
           
            
            }
    }
    
    
    
     public void deleteS1 (ActionEvent event){
         amountSaved = 0;
               paneS1.setVisible(false);
               Pane nn = (Pane) SessionsController.getInstance().gridPlay.getChildren().get(numPoste-1);
           Text displayS1 = (Text) nn.getChildren().get(12);
           displayS1.setText("false");
           SessionsController.getInstance().dataSaveSession.removeIf(s  ->  s.getCurrent() == numPoste);
          
           ImageView multiIcon= (ImageView) nn.getChildren().get(14);
            multiIcon.setVisible(false);
            
            displayTotal(sessionAmount); 
	}
    
    
    
    
    public void CloseWindow (ActionEvent event){
        if(NewOrderController.getInstance() != null){
            NewOrderController.getInstance().closeBtn.fire();
        }
                
		((Node)(event.getSource())).getScene().getWindow().hide();
                isCaisseOpen = false;            
	}
    
        @FXML
    void demarrer(ActionEvent event) {
           Pane nn = (Pane) SessionsController.getInstance().gridPlay.getChildren().get(numPoste-1);
           JFXButton btnStart = (JFXButton) nn.getChildren().get(5);
           btnStart.fire();
    }
    
    @FXML
    void arreter(ActionEvent event) {
           Pane nn = (Pane) SessionsController.getInstance().gridPlay.getChildren().get(numPoste-1);
           JFXButton btnStop = (JFXButton) nn.getChildren().get(7);
           btnStop.fire();
           deleteS1(event);
    }
   
   int productCount = 0;
   boolean check = false;
   
   public void addProduct(String nom, int price, int current, int tertib, int quant, Image imgP, String type){
        check = false;
        SessionsController.getInstance().dataPlay.forEach(product -> {
           if (product.getNomProduit().equals(nom) && product.getCurrent()== current){
               check = true;              
               product.setBuyQuant(quant+product.getBuyQuant());
               product.setPrix(price+product.getPrix());
           }
           
        }); 
        
        if(check == false){
          SessionsController.getInstance().dataPlay.add(new produit(nom, price, current, tertib, quant, imgP, type));  
        }
        
        
        //________chicha count____________________________________
        if (type.equals("Chicha")){
        chichaCount = Integer.parseInt(SessionsController.getInstance().chichaTextPs.getText());
        chichaCount ++;
        SessionsController.getInstance().chichaTextPs.setText(Integer.toString(chichaCount));
        }
 
        nomProduit.setCellValueFactory(new PropertyValueFactory<produit,String>("nomProduit"));
        prix.setCellValueFactory(new PropertyValueFactory<produit,Integer>("prix"));
        action.setCellValueFactory(new PropertyValueFactory<>("Actions"));
        buyQuant.setCellValueFactory(new PropertyValueFactory<produit,Integer>("buyQuant"));
        image.setCellValueFactory(new PropertyValueFactory<>("imgView"));
        

        tableProduct.setItems(SessionsController.getInstance().dataPlay);
        tableProduct.setItems(tableProduct.getItems().filtered(log -> log.getCurrent() == numPoste));
        //_____refresh TableView____________________________
        tableProduct.refresh();
        
        displayTotal(sessionAmount);
        
       //___________Notification__System_____________________________________________ 
        Pane nn = (Pane) SessionsController.getInstance().gridPlay.getChildren().get(numPoste-1);
           Text txt = (Text) nn.getChildren().get(3);
           Circle red = (Circle) nn.getChildren().get(2);
           txt.setVisible(true);
           red.setVisible(true);
           productCount = 0;
          SessionsController.getInstance().dataPlay.forEach(product -> {
           if (product.getCurrent()== current){          
               productCount += product.getBuyQuant();
           }
        }); 
        txt.setText(String.valueOf(productCount));  
      //______________________________________________________________________________
      
      moneyCount = Integer.parseInt(SessionsController.getInstance().moneyText.getText());
      moneyCount += price*quant;
      SessionsController.getInstance().moneyText.setText(Integer.toString(moneyCount));
   }
   
    public void displayTotal(int amount){
       sessionAmount = amount;
        int total = 0 ;
        for (produit item : tableProduct.getItems()) {
            total = total + item.getPrix();
        }
    totalCons.setText(Integer.toString(total));
    totalPrice = total + sessionAmount + amountSaved;
    totalPriceText.setText(Integer.toString(totalPrice));
    //System.out.println(totalPrice);
    }
   
    public void changeToMulti(){
       int newAmount = (int) (sessionAmount*1.5);  
       displayTotal(newAmount);
    }
   
   public void removeProduct(String nom, int price, int current, int tertib, String type){
        check = false;
        SessionsController.getInstance().dataPlay.forEach(product -> {
           if (product.getNomProduit().equals(nom) && product.getCurrent()== current){   
               int prixUnit = product.getPrix()/product.getBuyQuant();
               product.setPrix(price-prixUnit);
               product.setBuyQuant(product.getBuyQuant()-1);
               
               moneyCount = Integer.parseInt(SessionsController.getInstance().moneyText.getText());
      moneyCount -= prixUnit;
      SessionsController.getInstance().moneyText.setText(Integer.toString(moneyCount));
      
               if (product.getPrix() == 0){
                   check = true; 
               }
           }
                
        }); 
        
        if(check == true){
                   SessionsController.getInstance().dataPlay.removeIf(p -> p.getNomProduit().equals(nom) && p.getCurrent() == numPoste);
         }
        
        
        //________chicha count____________________________________
        
        if (type.equals("Chicha")){
        chichaCount = Integer.parseInt(SessionsController.getInstance().chichaTextPs.getText());
        chichaCount --;
        SessionsController.getInstance().chichaTextPs.setText(Integer.toString(chichaCount));
        }
        
        nomProduit.setCellValueFactory(new PropertyValueFactory<produit,String>("nomProduit"));
        prix.setCellValueFactory(new PropertyValueFactory<produit,Integer>("prix"));
        action.setCellValueFactory(new PropertyValueFactory<>("Actions"));
        

        tableProduct.setItems(SessionsController.getInstance().dataPlay);
        tableProduct.setItems(tableProduct.getItems().filtered(log -> log.getCurrent() == numPoste));
        //_____refresh TableView___________________________
        tableProduct.refresh();
        
        displayTotal(sessionAmount);
        
      //___________Notification__System_____________________________________________ 
        Pane nn = (Pane) SessionsController.getInstance().gridPlay.getChildren().get(numPoste-1);
           Text txt = (Text) nn.getChildren().get(3);
           Circle red = (Circle) nn.getChildren().get(2);
           if(tableProduct.getItems().size() == 0){
               txt.setVisible(false);
               red.setVisible(false);   
           }
           productCount = 0;
          SessionsController.getInstance().dataPlay.forEach(product -> {
           if (product.getCurrent()== current){          
               productCount += product.getBuyQuant();
           }
        }); 
        txt.setText(String.valueOf(productCount));  
     //________________________________________________________________________________ 
     
   }
    
 
   
    public void fillFields (int num, long s, int m, int h, int amount, String heureD, boolean run, int tarife) throws ClassNotFoundException{
     numPoste = num;
     running = run;
     pcTxt.setText("PS: "+formatter.format(numPoste));
     if (numPoste == 6 ){ 
         pcTxt.setText("PS: VIP");
     }
     secondsCounter[0] = s;
     minCounter[0] = m;
     hCounter[0] = h;
     timeDisplay.setText(formatter.format(hCounter[0])+":"+formatter.format(minCounter[0]));
     secondsDisplay.setText(formatter.format(secondsCounter[0]));
     sessionAmount = amount;
     amountDisplay.setText(Integer.toString(sessionAmount)+" DA");
     
     tableProduct.setItems(SessionsController.getInstance().dataPlay);
     tableProduct.setItems(tableProduct.getItems().filtered(log -> log.getCurrent() == numPoste));
     heureDebut = heureD;
     
     FontAwesomeIcon iconPauseBig = new FontAwesomeIcon();
        iconPauseBig.setIconName("PAUSE");
        iconPauseBig.setSize("25");
        iconPauseBig.setFill(Color.WHITE);    
    FontAwesomeIcon iconStartBig = new FontAwesomeIcon();
        iconStartBig.setIconName("PLAY");
        iconStartBig.setSize("25");
        iconStartBig.setFill(Color.WHITE);
     if (running){
         sessionStart.setGraphic(iconPauseBig);
     }else{
         sessionStart.setGraphic(iconStartBig);
     }
     
     Pane nn = (Pane) SessionsController.getInstance().gridPlay.getChildren().get(numPoste-1);
     JFXButton btnStop = (JFXButton) nn.getChildren().get(7);
     if (btnStop.isDisable()){
         sessionStop.setDisable(true);
     }else{
         sessionStop.setDisable(false);
     }
     
    
    
    if (tarife == 450){
        tarifMultiRadio.setSelected(true);
    }else{
        tarifNormalRadio.setSelected(true);
    }
    
    

    Text txt = (Text) nn.getChildren().get(12);
    if (txt.getText().equals("true")){
        paneS1.setVisible(true);
        
    SessionsController.getInstance().dataSaveSession.forEach(session -> {
           if (session.getCurrent()== numPoste){    
                timeDisplayS1.setText(session.getDurationSession());
                amountDisplayS1.setText(session.getAmountSession()+" DA");
                amountSaved = session.getAmountSession();
               if (session.getTypeSession().equals("Normal")){
                   multiS1.setText("Normal");
               }else{
                   multiS1.setText("Multi");
               }
           }      
        });  
    }
  displayTotal(sessionAmount); 
 
}
    
    @FXML
void plusTemps(ActionEvent event) throws FileNotFoundException {
  Pane nn = (Pane) SessionsController.getInstance().gridPlay.getChildren().get(numPoste-1);
  JFXButton addTimeBtn = (JFXButton) nn.getChildren().get(15);
  addTimeBtn.fire();
}

@FXML
void moinTemps(ActionEvent event) throws FileNotFoundException {
  Pane nn = (Pane) SessionsController.getInstance().gridPlay.getChildren().get(numPoste-1);
  JFXButton reduceTimeBtn = (JFXButton) nn.getChildren().get(16);
  reduceTimeBtn.fire();
}
    
    
    public void openProductsList (ActionEvent event){
            try {
                if (!isNewOrderOpen){
                   FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("newOrder.fxml"));
            	            Parent root = (Parent) fxmlLoader.load();
            	            Stage stage = new Stage();
            	            stage.setScene(new Scene(root));  
            	            stage.initStyle(StageStyle.DECORATED.UNDECORATED);
            	            stage.setAlwaysOnTop(true);
                            
                            //Open Stage in a specific position
                            stage.setX(750); 
                            stage.setY(30); 
                            
                          stage.setOnCloseRequest(eventt -> {
                                    isNewOrderOpen = false;
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
        }
            	       
	} catch(Exception e) {
	       e.printStackTrace();
	}
            
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
    
      @FXML
    public void valide(ActionEvent event) throws SQLException, ClassNotFoundException {
         Alert alert1 = new Alert(Alert.AlertType.CONFIRMATION, "Valider La session? \n Poste: "+numPoste+"\n Payement:  "+totalPriceText.getText(), ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
               ((Stage) alert1.getDialogPane().getScene().getWindow()).setAlwaysOnTop(true);
               alert1.showAndWait();
                    if (alert1.getResult() == ButtonType.YES){
        System.out.println("____________________");
        String heureF = getCurrentTime();
        String payment = totalPriceText.getText();
        
        String sql = "INSERT INTO session (dateSession, heureDebut, heureFin, clientId, sessionType, Payment, Comment, client, idUser, duration, numPost) VALUES ('"+getCurrentDate()+"', '"+heureDebut+"', '"+heureF+"', '1', 'Play', '"+payment+"', ' ', 'Guest', 1,'"+timeDisplay.getText()+"','"+numPoste+"');";
        connection=dbConnect.getDataBase();
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.execute();
            
        sql= "SELECT last_insert_rowid();";
        ResultSet rs = null;
        
        preparedStatement = connection.prepareStatement(sql);
            
        rs = preparedStatement.executeQuery();
        System.out.println(rs.getInt(1));
        int idSess = rs.getInt(1);
        
        SessionsController.getInstance().dataPlay.forEach(product -> {
            if (product.getCurrent() == numPoste){
            try {
                product.getImgView().getImage();
                connection=dbConnect.getDataBase();
                PreparedStatement preparedSt= connection.prepareStatement("INSERT INTO vente "
                        + "(nomProduit, typeProduit, prixVente, quant, idSession) "
                        + "VALUES('"+product.getNomProduit()+"','"+product.getTypeProduit()+"', "+product.getPrix()+" ,"+product.getBuyQuant()+", "+idSess+");");
                
                preparedSt.execute();
                
            } catch (SQLException ex) {
                Logger.getLogger(CaisseTableController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(CaisseTableController.class.getName()).log(Level.SEVERE, null, ex);
            }
          }   
        }); 
        
        connection.close();
        if (sessionStop.isDisabled()) {
            sessionStart.fire();
            sessionStop.fire();
        }else{
            sessionStop.fire();
        }
                    }
    }
    
    
 public Node getNodeByRowColumnIndex (final int row, final int column, GridPane gridPane) {
  Node result = null;
  ObservableList<Node> childrens = gridPane.getChildren();

  for (Node node : childrens) {
    if(gridPane.getRowIndex(node) == row && gridPane.getColumnIndex(node) == column) {
      result = node;
      break;
    }
  }

  return result;
}
 
  public int reverseNum(final int num) {
  int result = 0;
        if (num == 1) result = 0;
        if (num == 2) result = 5;
        if (num == 3) result = 1;
        if (num == 4) result = 6;
        if (num == 5) result = 2;
        if (num == 6) result = 7;
        if (num == 7) result = 3;
        if (num == 8) result = 8;
        if (num == 9) result = 4;
        if (num == 10) result = 9;

  return result;
}
    
    
    
/////////////////////////////////////////////////////Print/////////////////////////////////////
     @FXML
    public void print(ActionEvent event){
        
        PrinterJob pj = PrinterJob.getPrinterJob();        
        pj.setPrintable(new BillPrintable(),getPageFormat(pj));
        try {
             pj.print();
          
        }
         catch (PrinterException ex) {
                 ex.printStackTrace();
        }
    }
    
    
    
 public PageFormat getPageFormat(PrinterJob pj)
{
    
    PageFormat pf = pj.defaultPage();
    Paper paper = pf.getPaper();    

    double middleHeight =8.0;  
    double headerHeight = 2.0;                  
    double footerHeight = 2.0;                  
    double width = convert_CM_To_PPI(8);      //printer know only point per inch.default value is 72ppi
    double height = convert_CM_To_PPI(headerHeight+middleHeight+footerHeight); 
    paper.setSize(width, height);
    paper.setImageableArea(                    
        0,
        10,
        width,            
        height - convert_CM_To_PPI(1)
    );   //define boarder size    after that print area width is about 180 points
            
    pf.setOrientation(PageFormat.PORTRAIT);           //select orientation portrait or landscape but for this time portrait
    pf.setPaper(paper);    

    return pf;
}
        
protected static double convert_CM_To_PPI(double cm) {            
	        return toPPI(cm * 0.393600787);            
}
 
protected static double toPPI(double inch) {            
	        return inch * 72d;            
}

    
public class BillPrintable implements Printable {
    
   
    
    
  public int print(Graphics graphics, PageFormat pageFormat,int pageIndex) 
  throws PrinterException 
  {    
       
      ImageIcon icon=new ImageIcon("temp.png"); 
      int result = NO_SUCH_PAGE;    
        if (pageIndex == 0) {                    
        
            Graphics2D g2d = (Graphics2D) graphics;                    

            double width = pageFormat.getImageableWidth();                    
           
            g2d.translate((int) pageFormat.getImageableX(),(int) pageFormat.getImageableY()); 

            ////////// code by alqama//////////////

            FontMetrics metrics=g2d.getFontMetrics(new Font("Arial",Font.BOLD,7));
        //    int idLength=metrics.stringWidth("000000");
            //int idLength=metrics.stringWidth("00");
            int idLength=metrics.stringWidth("000");
            int amtLength=metrics.stringWidth("000000");
            int qtyLength=metrics.stringWidth("00000");
            int priceLength=metrics.stringWidth("000000");
            int prodLength=(int)width - idLength - amtLength - qtyLength - priceLength-17;

        //    int idPosition=0;
        //    int productPosition=idPosition + idLength + 2;
        //    int pricePosition=productPosition + prodLength +10;
        //    int qtyPosition=pricePosition + priceLength + 2;
        //    int amtPosition=qtyPosition + qtyLength + 2;
            
            int productPosition = 0;
            int discountPosition= prodLength+5;
            int pricePosition = discountPosition +idLength+10;
            int qtyPosition=pricePosition + priceLength + 4;
            int amtPosition=qtyPosition + qtyLength;
            
            
              
        try{
            /*Draw Header*/
            int y=20;
            int yShift = 10;
            int headerRectHeight=15;
            int headerRectHeighta=40;
            
        int max = tableProduct.getItems().size();
        
            
            ///////////////// Product names Get ///////////
            ///////////////// Product names Get ///////////
                
            
            ///////////////// Product price Get ///////////

            ///////////////// Product price Get ///////////
                
             g2d.setFont(new Font("Monospaced",Font.PLAIN,9));
            g2d.drawString("-------------------------------------",12,y);y+=yShift;
            g2d.drawString("      PushPlay Bill Receipt        ",12,y);y+=yShift;
            g2d.drawString("-------------------------------------",12,y);y+=headerRectHeight;
            g2d.drawString("-------------------------------------",10,y);y+=yShift;
            g2d.drawString(" Consommation                 Price   ",10,y);y+=yShift;
            g2d.drawString("-------------------------------------",10,y);y+=headerRectHeight;
            
            int spaceLength;
            String space;
            for (int i = 0; i < max; i++) {
                spaceLength = 30-nomProduit.getCellObservableValue(i).getValue().length();
                space = String.format("%0" + spaceLength + "d", 0).replace('0', ' ');
                g2d.drawString(" "+nomProduit.getCellObservableValue(i).getValue()+space+prix.getCellObservableValue(i).getValue()+" DA",10,y);y+=yShift;
                }
            g2d.drawString("-------------------------------------",10,y);y+=yShift;
            g2d.drawString(" Session Type:               PS",10,y);y+=yShift;
            g2d.drawString(" Gaming session duration:    "+timeDisplay.getText()+" ",10,y);y+=yShift;
            g2d.drawString(" Amount:                     "+sessionAmount+" DA",10,y);y+=yShift;
            g2d.drawString("-------------------------------------",10,y);y+=yShift;
            g2d.drawString(" Total amount: "+totalPrice+" DA            ",10,y);y+=yShift;
            g2d.drawString("-------------------------------------",10,y);y+=yShift;
            g2d.drawString("                               ",10,y);y+=yShift;
            g2d.drawString("             0675636267             ",10,y);y+=yShift;
            g2d.drawString("*************************************",10,y);y+=yShift;
            g2d.drawString("    Thanks for visiting Push Play    ",10,y);y+=yShift;
            g2d.drawString("*************************************",10,y);y+=yShift;
                   
//            g2d.setFont(new Font("Monospaced",Font.BOLD,10));
//            g2d.drawString("Customer Shopping Invoice", 30,y);y+=yShift; 
    }
    catch(Exception r){
    r.printStackTrace();
    }

              result = PAGE_EXISTS;    
          }    
          return result;    
      }
  }

  int sizeOfCurrentPost = 0;
@FXML
void fajitas(ActionEvent event) throws FileNotFoundException {
  Image image = new Image("product icons/fajitas.png"); 
  
  sizeOfCurrentPost = 0;
                    SessionsController.getInstance().data.forEach(product -> {
                        if (product.getCurrent() == numPoste){
                            sizeOfCurrentPost++;
                        }          

                    });
                    int tertib = sizeOfCurrentPost;
                    
  addProduct("Fajitas", pFajitas, numPoste, tertib, 1, image, "Nourriture");
}

@FXML
void adalia(ActionEvent event) throws FileNotFoundException {
  Image image = new Image("product icons/Adalia.png"); 
  
  sizeOfCurrentPost = 0;
                    SessionsController.getInstance().data.forEach(product -> {
                        if (product.getCurrent() == numPoste){
                            sizeOfCurrentPost++;
                        }          

                    });
                    int tertib = sizeOfCurrentPost;
                    
  addProduct("Chicha adalia", pAdalia, numPoste, tertib, 1, image, "Chicha");
}

@FXML
void nutella(ActionEvent event) throws FileNotFoundException {
  Image image = new Image("product icons/nutella.png"); 
  
  sizeOfCurrentPost = 0;
                    SessionsController.getInstance().data.forEach(product -> {
                        if (product.getCurrent() == numPoste){
                            sizeOfCurrentPost++;
                        }          

                    });
                    int tertib = sizeOfCurrentPost;
                    
  addProduct("nutella", pnutella, numPoste, tertib, 1, image, "Nourriture");
}
@FXML
void plusFruit(ActionEvent event) throws FileNotFoundException {
  Image image = new Image("product icons/banana.png"); 
  
  sizeOfCurrentPost = 0;
                    SessionsController.getInstance().data.forEach(product -> {
                        if (product.getCurrent() == numPoste){
                            sizeOfCurrentPost++;
                        }          

                    });
                    int tertib = sizeOfCurrentPost;
                    
  addProduct("+ 1 Fruit", pFruit, numPoste, tertib, 1, image, "Nourriture");
}
@FXML
void plus3Fruits(ActionEvent event) throws FileNotFoundException {
  Image image = new Image("product icons/fruits.png"); 
  
  sizeOfCurrentPost = 0;
                    SessionsController.getInstance().data.forEach(product -> {
                        if (product.getCurrent() == numPoste){
                            sizeOfCurrentPost++;
                        }          

                    });
                    int tertib = sizeOfCurrentPost;
                    
  addProduct("+ 3 Fruits", pFruits, numPoste, tertib, 1, image, "Nourriture");
}
@FXML
void Pancakes(ActionEvent event) throws FileNotFoundException {
  Image image = new Image("product icons/pancakes.png"); 
  
  sizeOfCurrentPost = 0;
                    SessionsController.getInstance().data.forEach(product -> {
                        if (product.getCurrent() == numPoste){
                            sizeOfCurrentPost++;
                        }          

                    });
                    int tertib = sizeOfCurrentPost;
                    
  addProduct("Pancakes/Gauffres", pPancakes, numPoste, tertib, 1, image, "Nourriture");
}

@FXML
void mixt(ActionEvent event) throws FileNotFoundException {
  Image image = new Image("product icons/magic.PNG"); 
  
  sizeOfCurrentPost = 0;
                    SessionsController.getInstance().data.forEach(product -> {
                        if (product.getCurrent() == numPoste){
                            sizeOfCurrentPost++;
                        }          

                    });
                    int tertib = sizeOfCurrentPost;
                    
  addProduct("Chicha mixte", pMixt, numPoste, tertib, 1, image, "Chicha");
}
@FXML
void cheeseChicken(ActionEvent event) throws FileNotFoundException {
  Image image = new Image("product icons/cheese chicken.png"); 
  
  sizeOfCurrentPost = 0;
                    SessionsController.getInstance().data.forEach(product -> {
                        if (product.getCurrent() == numPoste){
                            sizeOfCurrentPost++;
                        }          

                    });
                    int tertib = sizeOfCurrentPost;
                    
  addProduct("Cheese Chicken", pCheeseCh, numPoste, tertib, 1, image, "Nourriture");
}
@FXML
void cascade(ActionEvent event) throws FileNotFoundException {
  Image image = new Image("product icons/cascade.png"); 
  
  sizeOfCurrentPost = 0;
                    SessionsController.getInstance().data.forEach(product -> {
                        if (product.getCurrent() == numPoste){
                            sizeOfCurrentPost++;
                        }          

                    });
                    int tertib = sizeOfCurrentPost;
                    
  addProduct("Cascade", pCascade, numPoste, tertib, 1, image, "Nourriture");
}
@FXML
void cheeseBurger(ActionEvent event) throws FileNotFoundException {
  Image image = new Image("product icons/cheese burger.png"); 
  
  sizeOfCurrentPost = 0;
                    SessionsController.getInstance().data.forEach(product -> {
                        if (product.getCurrent() == numPoste){
                            sizeOfCurrentPost++;
                        }          

                    });
                    int tertib = sizeOfCurrentPost;
                    
  addProduct("Cheese Burger", pCheeseB, numPoste, tertib, 1, image, "Nourriture");
}
@FXML
void frittes(ActionEvent event) throws FileNotFoundException {
  Image image = new Image("product icons/frittes.png"); 
  
  sizeOfCurrentPost = 0;
                    SessionsController.getInstance().data.forEach(product -> {
                        if (product.getCurrent() == numPoste){
                            sizeOfCurrentPost++;
                        }          

                    });
                    int tertib = sizeOfCurrentPost;
                    
  addProduct("Frites", pFrittes, numPoste, tertib, 1, image, "Nourriture");
}

@FXML
void cafe(ActionEvent event) throws FileNotFoundException {
  Image image = new Image("product icons/cafe.png"); 
  
  sizeOfCurrentPost = 0;
                    SessionsController.getInstance().data.forEach(product -> {
                        if (product.getCurrent() == numPoste){
                            sizeOfCurrentPost++;
                        }          

                    });
                    int tertib = sizeOfCurrentPost;
                    
  addProduct("café", pCafe, numPoste, tertib, 1, image, "Boisson");
}

@FXML
void tacos(ActionEvent event) throws FileNotFoundException {
  Image image = new Image("product icons/tacos.png"); 
  
  sizeOfCurrentPost = 0;
                    SessionsController.getInstance().data.forEach(product -> {
                        if (product.getCurrent() == numPoste){
                            sizeOfCurrentPost++;
                        }          

                    });
                    int tertib = sizeOfCurrentPost;
                    
  addProduct("Tacos", pTacos, numPoste, tertib, 1, image, "Nourriture");
}

@FXML
void menthe(ActionEvent event) throws FileNotFoundException {
  Image image = new Image("product icons/fakher.png"); 
  
  sizeOfCurrentPost = 0;
                    SessionsController.getInstance().data.forEach(product -> {
                        if (product.getCurrent() == numPoste){
                            sizeOfCurrentPost++;
                        }          

                    });
                    int tertib = sizeOfCurrentPost;
                    
  addProduct("Chicha Menthe", pMenthe, numPoste, tertib, 1, image, "Chicha");
}

@FXML
void crepe(ActionEvent event) throws FileNotFoundException {
  Image image = new Image("product icons/crepe.png"); 
  
  sizeOfCurrentPost = 0;
                    SessionsController.getInstance().data.forEach(product -> {
                        if (product.getCurrent() == numPoste){
                            sizeOfCurrentPost++;
                        }          

                    });
                    int tertib = sizeOfCurrentPost;
                    
  addProduct("Crêpe Simple", pCrepe, numPoste, tertib, 1, image, "Nourriture");
}
@FXML
void eau(ActionEvent event) throws FileNotFoundException {
  Image image = new Image("product icons/eau.png"); 
  
  sizeOfCurrentPost = 0;
                    SessionsController.getInstance().data.forEach(product -> {
                        if (product.getCurrent() == numPoste){
                            sizeOfCurrentPost++;
                        }          

                    });
                    int tertib = sizeOfCurrentPost;
                    
  addProduct("eau 1.5L", pEau, numPoste, tertib, 1, image, "Eau");
}

@FXML
void sghira(ActionEvent event) throws FileNotFoundException {
  Image image = new Image("product icons/wa.png"); 
  
  sizeOfCurrentPost = 0;
                    SessionsController.getInstance().data.forEach(product -> {
                        if (product.getCurrent() == numPoste){
                            sizeOfCurrentPost++;
                        }          

                    });
                    int tertib = sizeOfCurrentPost;
                    
  addProduct("eau 0.5L", pSghira, numPoste, tertib, 1, image, "Eau");
}
@FXML
void teaMaison(ActionEvent event) throws FileNotFoundException {
  Image image = new Image("product icons/thé maison.png"); 
  
  sizeOfCurrentPost = 0;
                    SessionsController.getInstance().data.forEach(product -> {
                        if (product.getCurrent() == numPoste){
                            sizeOfCurrentPost++;
                        }          

                    });
                    int tertib = sizeOfCurrentPost;
                    
  addProduct("Thé Maison", pTeaM, numPoste, tertib, 1, image, "Boisson");
}
@FXML
void MilkShake(ActionEvent event) throws FileNotFoundException {
  Image image = new Image("product icons/milk-shake.png"); 
  
  sizeOfCurrentPost = 0;
                    SessionsController.getInstance().data.forEach(product -> {
                        if (product.getCurrent() == numPoste){
                            sizeOfCurrentPost++;
                        }          

                    });
                    int tertib = sizeOfCurrentPost;
                    
  addProduct("Milk-Shake", pmilkshake, numPoste, tertib, 1, image, "Boisson");
}
@FXML
void cappuccino(ActionEvent event) throws FileNotFoundException {
  Image image = new Image("product icons/cappuccino.png"); 
  
  sizeOfCurrentPost = 0;
                    SessionsController.getInstance().data.forEach(product -> {
                        if (product.getCurrent() == numPoste){
                            sizeOfCurrentPost++;
                        }          

                    });
                    int tertib = sizeOfCurrentPost;
                    
  addProduct("Cappuccino Simple", pCappuccino, numPoste, tertib, 1, image, "Boisson");
}

@FXML
void cappuccinoM(ActionEvent event) throws FileNotFoundException {
  Image image = new Image("product icons/cappuccinoM.png"); 
  
  sizeOfCurrentPost = 0;
                    SessionsController.getInstance().data.forEach(product -> {
                        if (product.getCurrent() == numPoste){
                            sizeOfCurrentPost++;
                        }          

                    });
                    int tertib = sizeOfCurrentPost;
                    
  addProduct("Cappuccino Milka", pCappuccinoM, numPoste, tertib, 1, image, "Boisson");
}
@FXML
void cocktail(ActionEvent event) throws FileNotFoundException {
  Image image = new Image("product icons/cocktail.png"); 
  
  sizeOfCurrentPost = 0;
                    SessionsController.getInstance().data.forEach(product -> {
                        if (product.getCurrent() == numPoste){
                            sizeOfCurrentPost++;
                        }          

                    });
                    int tertib = sizeOfCurrentPost;
                    
  addProduct("Cocktail", pCocktail, numPoste, tertib, 1, image, "Boisson");
}
@FXML
void canette(ActionEvent event) throws FileNotFoundException {
  Image image = new Image("product icons/canette.png"); 
  
  sizeOfCurrentPost = 0;
                    SessionsController.getInstance().data.forEach(product -> {
                        if (product.getCurrent() == numPoste){
                            sizeOfCurrentPost++;
                        }          

                    });
                    int tertib = sizeOfCurrentPost;
                    
  addProduct("Canette", pCanette, numPoste, tertib, 1, image, "Boisson");
}  
   
@FXML
void canetteG(ActionEvent event) throws FileNotFoundException {
  Image image = new Image("product icons/canetteG.png"); 
  
  sizeOfCurrentPost = 0;
                    SessionsController.getInstance().data.forEach(product -> {
                        if (product.getCurrent() == numPoste){
                            sizeOfCurrentPost++;
                        }          

                    });
                    int tertib = sizeOfCurrentPost;
                    
  addProduct("CanetteG", pCanetteG, numPoste, tertib, 1, image, "Boisson");
}  
    
@FXML
void mokassarat(ActionEvent event) throws FileNotFoundException {
  Image image = new Image("product icons/mokassarat.png"); 
  
  sizeOfCurrentPost = 0;
                    SessionsController.getInstance().data.forEach(product -> {
                        if (product.getCurrent() == numPoste){
                            sizeOfCurrentPost++;
                        }          

                    });
                    int tertib = sizeOfCurrentPost;
                    
  addProduct("Mokassarat", pMokassarat, numPoste, tertib, 1, image, "Nourriture");
}  
   

}
