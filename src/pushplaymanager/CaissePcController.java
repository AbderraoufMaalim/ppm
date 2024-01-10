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
import java.util.Arrays;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.AnimationTimer;
import static javafx.beans.binding.Bindings.length;
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
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
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
import static jdk.nashorn.internal.objects.ArrayBufferView.length;
import static jdk.nashorn.internal.objects.NativeRegExp.source;
import static pushplaymanager.SessionsController.isCaisseOpen;

/**
 * FXML Controller class
 *
 * @author Raouf
 */
public class CaissePcController implements Initializable {
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


int chichaCount = Integer.parseInt(SessionsController.getInstance().chichaTextPc.getText());



    public static CaissePcController instance;
	public CaissePcController(){
		instance =this;
	}
	public static CaissePcController getInstance(){
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
 @FXML private TextField totalPriceText;
 
 
 long[] secondsCounter = {0}; 
 int[] minCounter = {0};
 int[] hCounter = {0};
 String heureDebut;


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        isCaisseOpen = true;
        
        nomProduit.setCellValueFactory(new PropertyValueFactory<produit,String>("nomProduit"));
        prix.setCellValueFactory(new PropertyValueFactory<produit,Integer>("prix"));
        buyQuant.setCellValueFactory(new PropertyValueFactory<produit,Integer>("buyQuant"));
        action.setCellValueFactory(new PropertyValueFactory<>("Actions"));
        image.setCellValueFactory(new PropertyValueFactory<>("imgView"));
        
        
     
     
   
        
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
           Pane nn = (Pane) SessionsController.getInstance().gridTest.getChildren().get(reverseNum(numPoste));
           JFXButton btnStart = (JFXButton) nn.getChildren().get(5);
           btnStart.fire();
    }
    
    @FXML
    void arreter(ActionEvent event) {
           Pane nn = (Pane) SessionsController.getInstance().gridTest.getChildren().get(reverseNum(numPoste));
           JFXButton btnStop = (JFXButton) nn.getChildren().get(7);
           btnStop.fire();
    }
   
   int productCount = 0;
   boolean check = false;
   
   public void addProduct(String nom, int price, int current, int tertib, int quant, Image imgP, String type){
        check = false;
        SessionsController.getInstance().data.forEach(product -> {
           if (product.getNomProduit().equals(nom) && product.getCurrent()== current){
               check = true;              
               product.setBuyQuant(quant+product.getBuyQuant());
               product.setPrix(price+product.getPrix());
           }
           
        }); 
        
        
        //________chicha count____________________________________
        if (type.equals("Chicha")){
        chichaCount = Integer.parseInt(SessionsController.getInstance().chichaTextPc.getText());
        chichaCount ++;
        SessionsController.getInstance().chichaTextPc.setText(Integer.toString(chichaCount));
        }
        
        if(check == false){
          SessionsController.getInstance().data.add(new produit(nom, price, current, tertib, quant, imgP, type));  
        }
 
        nomProduit.setCellValueFactory(new PropertyValueFactory<produit,String>("nomProduit"));
        prix.setCellValueFactory(new PropertyValueFactory<produit,Integer>("prix"));
        action.setCellValueFactory(new PropertyValueFactory<>("Actions"));
        buyQuant.setCellValueFactory(new PropertyValueFactory<produit,Integer>("buyQuant"));
        image.setCellValueFactory(new PropertyValueFactory<>("imgView"));
        

        tableProduct.setItems(SessionsController.getInstance().data);
        tableProduct.setItems(tableProduct.getItems().filtered(log -> log.getCurrent() == numPoste));
        //_____refresh TableView____________________________
        tableProduct.refresh();
        
        displayTotal(sessionAmount);
        
       //___________Notification__System_____________________________________________ 
        Pane nn = (Pane) SessionsController.getInstance().gridTest.getChildren().get(reverseNum(numPoste));
           Text txt = (Text) nn.getChildren().get(3);
           Circle red = (Circle) nn.getChildren().get(2);
           txt.setVisible(true);
           red.setVisible(true);
           productCount = 0;
          SessionsController.getInstance().data.forEach(product -> {
           if (product.getCurrent()== current){          
               productCount += product.getBuyQuant();
           }
        }); 
        txt.setText(String.valueOf(productCount));  
      //______________________________________________________________________________
   }
   
    public void displayTotal(int amount){
       sessionAmount = amount;
        int total = 0 ;
        for (produit item : tableProduct.getItems()) {
            total = total + item.getPrix();
        }
    totalCons.setText(Integer.toString(total));
    totalPrice = total + sessionAmount;
    totalPriceText.setText(Integer.toString(totalPrice));
    }
   
   
   public void removeProduct(String nom, int price, int current, int tertib, String type){
        check = false;
        SessionsController.getInstance().data.forEach(product -> {
           if (product.getNomProduit().equals(nom) && product.getCurrent()== current){   
               int prixUnit = product.getPrix()/product.getBuyQuant();
               product.setPrix(price-prixUnit);
               product.setBuyQuant(product.getBuyQuant()-1);
               if (product.getPrix() == 0){
                   check = true; 
               }
           }
                
        }); 
        
        
        //________chicha count____________________________________
        
        if (type.equals("Chicha")){
        chichaCount = Integer.parseInt(SessionsController.getInstance().chichaTextPc.getText());
        chichaCount --;
        SessionsController.getInstance().chichaTextPc.setText(Integer.toString(chichaCount));
        }
        
        if(check == true){
                   SessionsController.getInstance().data.removeIf(p -> p.getNomProduit().equals(nom) && p.getCurrent() == numPoste);
         }
        
        nomProduit.setCellValueFactory(new PropertyValueFactory<produit,String>("nomProduit"));
        prix.setCellValueFactory(new PropertyValueFactory<produit,Integer>("prix"));
        action.setCellValueFactory(new PropertyValueFactory<>("Actions"));
        

        tableProduct.setItems(SessionsController.getInstance().data);
        tableProduct.setItems(tableProduct.getItems().filtered(log -> log.getCurrent() == numPoste));
        //_____refresh TableView___________________________
        tableProduct.refresh();
        
        displayTotal(sessionAmount);
        
      //___________Notification__System_____________________________________________ 
        Pane nn = (Pane) SessionsController.getInstance().gridTest.getChildren().get(reverseNum(numPoste));
           Text txt = (Text) nn.getChildren().get(3);
           Circle red = (Circle) nn.getChildren().get(2);
           if(tableProduct.getItems().size() == 0){
               txt.setVisible(false);
               red.setVisible(false);   
           }
           productCount = 0;
          SessionsController.getInstance().data.forEach(product -> {
           if (product.getCurrent()== current){          
               productCount += product.getBuyQuant();
           }
        }); 
        txt.setText(String.valueOf(productCount));  
     //________________________________________________________________________________ 

   }
    
 
   
    public void fillFields (int num, long s, int m, int h, int amount, String heureD, boolean run) throws ClassNotFoundException{
     numPoste = num;
     running = run;
     pcTxt.setText("PC: "+formatter.format(numPoste));
     secondsCounter[0] = s;
     minCounter[0] = m;
     hCounter[0] = h;
     timeDisplay.setText(formatter.format(hCounter[0])+":"+formatter.format(minCounter[0]));
     secondsDisplay.setText(formatter.format(secondsCounter[0]));
     sessionAmount = amount;
     amountDisplay.setText(Integer.toString(sessionAmount)+" DA");
     
     tableProduct.setItems(SessionsController.getInstance().data);
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
     
     Pane nn = (Pane) SessionsController.getInstance().gridTest.getChildren().get(reverseNum(numPoste));
     JFXButton btnStop = (JFXButton) nn.getChildren().get(7);
     if (btnStop.isDisable()){
         sessionStop.setDisable(true);
     }else{
         sessionStop.setDisable(false);
     }
     
    displayTotal(sessionAmount);
    
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
                            stage.setX(688); 
                            stage.setY(100); 
                            
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
                        System.out.println("Poste: "+numPoste);
                        System.out.println("date session: "+java.time.LocalDate.now());
                        System.out.println("heure de début: "+heureDebut);
                        LocalTime heureFin = LocalTime.now();
                        String heureF = heureFin.format(formater); 
                        System.out.println("heure de fin: "+heureF);
                        System.out.println("durée: "+timeDisplay.getText());
                        System.out.println("Id client: 1");
                        System.out.println("type de session: PC");
                        System.out.println("payement: "+totalPriceText.getText());
                        System.out.println("Comment: no comment");
                        System.out.println("client: guest");
                        System.out.println("User id: 1");
        
                      String sql = "INSERT INTO session (dateSession, heureDebut, heureFin, clientId, sessionType, Payment, Comment, client, idUser, duration, numPost) VALUES ('"+getCurrentDate()+"', '"+heureDebut+"', '"+heureF+"', '1', 'PC', '"+totalPriceText.getText()+"', 'No comment', 'Guest', 1,'"+timeDisplay.getText()+"','"+numPoste+"');";

                            nomProduit.setCellValueFactory(new PropertyValueFactory<produit,String>("nomProduit"));
        

                         connection=dbConnect.getDataBase();
                         PreparedStatement preparedStatement = connection.prepareStatement(sql);
                          preparedStatement.execute();
            
                     connection.close();
                         sessionStop.fire();
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
            g2d.drawString(" Session Type:               PC",10,y);y+=yShift;
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



                        



    
}
