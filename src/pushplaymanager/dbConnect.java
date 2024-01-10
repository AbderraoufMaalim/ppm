
package pushplaymanager;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 *
 * @author Raouf
 */
public class dbConnect {
    
	static Connection con=null;
    static File Derctory = new File (System.getProperty("user.home")+"\\Push play manager");
    static File DataBaseFile=new  File(Derctory.getAbsolutePath()+"\\pushplay.sqlite");
    static String url ="jdbc:sqlite:"+Derctory.getAbsolutePath()+"\\pushplay.sqlite";
    public static Connection getConnection() throws SQLException, ClassNotFoundException, IOException{
    	Class.forName("org.sqlite.JDBC");
    	if (DataBaseFile.exists()){
			System.out.println("Le fichier exist deja");
			con = DriverManager.getConnection(url); 
		}else{
			
			System.out.println("Le fichier n'exist pas");
			try {
				Derctory.mkdir();
				Class.forName("org.sqlite.JDBC");
				con = DriverManager.getConnection(url);  
				Statement st;
			    st = con.createStatement();
			    st.execute("CREATE TABLE IF NOT EXISTS users (idUsers INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, UserName VARCHAR, Password VARCHAR);");
			    st.execute("INSERT INTO users (UserName, Password) VALUES ('Madani', '0000');");
			    st.execute("INSERT INTO users (UserName, Password) VALUES ('Lamine', '0000');");
                            st.execute("INSERT INTO users (UserName, Password) VALUES ('Charaf', '0000');");
			    st.execute("CREATE TABLE IF NOT EXISTS client (idClient INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE, firstNameClient VARCHAR NOT NULL , lastNameClient VARCHAR, phoneNumberClient VARCHAR, age INTEGER, inscription DATE)");
                            st.execute("INSERT INTO client (firstNameClient, lastNameClient, phoneNumberClient, age, inscription) VALUES ('guest', 'guest','0666666666',99,'01/01/2020');");
			    st.execute("CREATE TABLE IF NOT EXISTS session (idSession INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE, dateSession DATE NOT NULL, heureDebut TIME, heureFin TIME, clientId INTEGER, sessionType VARCHAR, duration VARCHAR, Payment DOUBLE DEFAULT 0, Comment TEXT, client VARCHAR, idUser INTEGER, numPost VARCHAR)");
                            st.execute("CREATE TABLE IF NOT EXISTS dailyprofits (idJour INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE, Date DATE, DailyIncome DOUBLE DEFAULT 0, DailyProfits DOUBLE DEFAULT 0, NumberPatients INTEGER DEFAULT 0, NumberVisits INTEGER DEFAULT 0, idUser INTEGER, Comment TEXT DEFAULT 'Commenter ce jour !')");
			    st.execute("CREATE TABLE IF NOT EXISTS monthlyprofits (IdMonth INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE, Year INTEGR, Month INTEGER, MonthYear VARCHAR, MonthIncome DOUBLE DEFAULT 0, MonthProfits DOUBLE DEFAULT 0, NumberPatients INTEGER DEFAULT 0, NumberVisits INTEGER DEFAULT 0, idUser INTEGER, Comment TEXT  DEFAULT 'Commenter ce mois !');");
			    st.execute("CREATE TABLE IF NOT EXISTS annualprofits (IdYear INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE, Year INTEGER, AnnualIncome DOUBLE DEFAULT 0, AnnualProfits DOUBLE DEFAULT 0, NumberPatients INTEGER DEFAULT 0, NumberVisits INTEGER NOT NULL DEFAULT 0, idUser INTEGER, Comment TEXT NOT NULL DEFAULT 'Commenter cette année !');");
			    st.execute("CREATE TABLE IF NOT EXISTS message (idMsg INTEGER PRIMARY KEY NOT NULL, idEmetteur INTEGER NOT NULL, idRecepteur INTEGER NOT NULL, dateEnvoi DATE NOT NULL, heureEnvoi TIME NOT NULL, statu VARCHAR DEFAULT 'pending', message TEXT NULL);");
                            st.execute("CREATE TABLE IF NOT EXISTS notes (idNote INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL , DateAjout DATE, TimeAjout TIME, idUser INTEGER,NoteNumber INTEGER, Note TEXT);");                
                            st.execute("CREATE TABLE IF NOT EXISTS pc (idPc INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE, numeroPc INTEGER, tarifPc INTEGER);");
                            st.execute("CREATE TABLE IF NOT EXISTS ps (idPs INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE, numeroPs INTEGER, tarifPs INTEGER);");
                            st.execute("CREATE TABLE IF NOT EXISTS tables (idTable INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE, numeroTable INTEGER);");                            
                            st.execute("CREATE TABLE IF NOT EXISTS radioFamily (idFamily INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE,typeRadio VARCHAR, familyName VARCHAR);");
                            st.execute("CREATE TABLE IF NOT EXISTS produit (idProduit INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE,nomProduit VARCHAR, typeProduit VARCHAR, quantity INTEGER, prix INTEGER, img BLOB);");
                
			    st.close();
			} catch(SQLException e){
	            e.printStackTrace();
	        }
			
		}
        return con;
    }
    
    public static Connection getDataBase() throws SQLException, ClassNotFoundException{
        try{
            Class.forName("org.sqlite.JDBC");
        	con = DriverManager.getConnection(url);  
        }catch(SQLException e){
            e.printStackTrace();
        }
        return con;
    }
}
