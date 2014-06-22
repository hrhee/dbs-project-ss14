package bundesliga;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class BL {
  public Connection conn_src;
  public Connection conn_dst;
  public Data       d;
  public String [] tableName;

  public enum Liga {
    NULL, NR, VERB, ERST, MEISTER, REKORDSP, SPIELE_REK
  }
  
  public enum Spiel {
    NULL, ID, TAG, DATE, UHR, HEIM, GAST, TH, TG
  }
  
  public enum Spieler {
    SID, VID, TNR, SNAME, LAND, SP, T, VOR
  }
  
  public enum Verein {
    VID, NAME, L
  }

  public BL() {
    conn_src  = null;
    conn_dst  = null;
    d         = new Data();
    tableName = new String [6];
    
    tableName[0] = "Liga";
    tableName[1] = "Verein";
    tableName[2] = "Spieler";
    tableName[3] = "Spiel";
    tableName[4] = "Spielt_fuer";
    tableName[5] = "Spielt_in";
  }

  public void init_src() {
    System.out.println("-------- MySQL JDBC Connection init ------------");

    try {
      Class.forName("com.mysql.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      System.out.println("Warning: MySQL JDBC Driver not found");
      e.printStackTrace();
      return;
    }

    // System.out.println("MySQL JDBC Driver Registered!");

    try {
      conn_src = DriverManager.getConnection(d.DB_URL + d.DB_NAME, d.USR, d.PW);

    } catch (SQLException e) {
      System.out.println("Connection Failed! Check output console");
      e.printStackTrace();
      return;
    }

    if (conn_src != null) {
      System.out
          .println("Connection to database " + d.DB_NAME + " established");
    } else {
      System.out.println("Failed to make connection!");
    }
  }

  public void init_dst() {
    System.out.print("Create Database "+d.FU_DBS);
    try {
      conn_dst = DriverManager.getConnection(d.DB_URL, d.USR, d.PW);

    } catch (SQLException e) {
      System.out.println("Connection Failed! Check output console");
      e.printStackTrace();
      return;
    }

    try {
      Statement stmt = conn_dst.createStatement();
      String sql = "CREATE DATABASE IF NOT EXISTS " + d.FU_DBS;
      stmt.executeUpdate(sql);
      System.out.println("...done.");
      this.close_st(stmt);

    } catch (SQLException e) {
      System.out.println("Warning: Could not create statement");
      e.printStackTrace();
      return;
    }
    
    if (conn_dst!=null) {
      try {
        conn_dst.close();
      } catch (SQLException e) {
        System.out.println("Warning: Could not close connection");
        e.printStackTrace();
        return;
      }
    }
    
    connect(d.FU_DBS);
  }
  
  public void deinit() throws SQLException {
    Boolean b = false; 
    if ( this.conn_src != null ) {
      this.conn_src.close();
      b = true;
    }
    if ( this.conn_dst != null ) {
      this.conn_dst.close();
      b = true;
    } else {
      b = false;
    }
    
    if ( b ) System.out.println("All connections closed.");
  }

  public ResultSet cr_rs(Statement stmt, String sql) throws SQLException {
    ResultSet rs = stmt.executeQuery(sql);
    return rs;
  }

  public void close_rs(ResultSet rset) throws SQLException {
    if (rset != null) {
      rset.close();
    }
  }

  public void close_st(Statement st) throws SQLException {
    if (st != null) {
      st.close();
    }
  }
  
  public void connect( String s) {
    try {
      conn_dst = DriverManager.getConnection(d.DB_URL + s, d.USR, d.PW);

    } catch (SQLException e) {
      System.out.println("Connection Failed! Check output console");
      e.printStackTrace();
      return;
    }
    
    if (conn_dst != null) {
      System.out
          .println("Connection to database " + s + " established");
    } else {
      System.out.println("Failed to make connection!");
    }

  }
  
  public void dropTables_dst() {
    for ( int i=5; i>=0; i-- ) {
      try {
        Statement stmt = conn_dst.createStatement();
        String sql = "DROP TABLE IF EXISTS " + tableName[i];
        System.out.print("Drop Table '"+tableName[i]+"'");
        stmt.executeUpdate( sql );
        System.out.println("...done.");
        this.close_st(stmt);
      } catch (SQLException e) {
        System.out.println("Warning: Could not drop table");
        e.printStackTrace();
        return;
      }
    }
  }
  
  public void createTables_dst() {
    String [] sql  = new String[6];
    String [] alt  = new String[6];
    sql[0]  = "CREATE TABLE IF NOT EXISTS `Liga` (`Id` int(1) NOT NULL, `Name` varchar(40) COLLATE utf8_unicode_ci NOT NULL, PRIMARY KEY (`Id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";
    sql[1]  = "CREATE TABLE IF NOT EXISTS `Verein` (`Id` int(11) NOT NULL, `Name` varchar(255) COLLATE utf8_unicode_ci NOT NULL, PRIMARY KEY (`Id`) ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";
    sql[2]  = "CREATE TABLE IF NOT EXISTS `Spieler` ( `Id` int(11) NOT NULL, `Name` varchar(255) COLLATE utf8_unicode_ci NOT NULL, `TrikotNr` int(11) NOT NULL, `Heimatland` varchar(255) COLLATE utf8_unicode_ci NOT NULL, PRIMARY KEY (`Id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";
    sql[3]  = "CREATE TABLE IF NOT EXISTS `Spiel` ( `Id` int(11) NOT NULL, `Spieltag` int(11) NOT NULL, `Datum` date NOT NULL, `Uhrzeit` time NOT NULL, `ToreHeim` int(11) NOT NULL, `ToreAus` int(11) NOT NULL, `Heim` int(11) NOT NULL, `Aus` int(11) NOT NULL, PRIMARY KEY (`Id`), KEY `Heim` (`Heim`), KEY `Aus` (`Aus`)) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";
    sql[4]  = "CREATE TABLE IF NOT EXISTS `Spielt_fuer` ( `SId` int(11) NOT NULL, `VId` int(11) NOT NULL, `Saison` int(11) NOT NULL, `Tore` int(11) NOT NULL, `TrikotNr` int(11) NOT NULL, PRIMARY KEY (`SId`,`VId`), KEY `VereinKey` (`VId`)) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";
    sql[5]  = "CREATE TABLE IF NOT EXISTS `Spielt_in` ( `LId` int(1) NOT NULL, `VId` int(11) NOT NULL, `Saison` int(11) NOT NULL, PRIMARY KEY (`LId`,`VId`)) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    alt[0] = "";
    alt[1] = "";
    alt[2] = "";
    alt[3] = "ALTER TABLE `Spiel` ADD CONSTRAINT `AusKey` FOREIGN KEY (`Aus`) REFERENCES `Verein` (`Id`) ON DELETE NO ACTION ON UPDATE NO ACTION, ADD CONSTRAINT `HeimKey` FOREIGN KEY (`Heim`) REFERENCES `Verein` (`Id`) ON DELETE NO ACTION ON UPDATE NO ACTION;";
    alt[4] = "ALTER TABLE `Spielt_fuer` ADD CONSTRAINT `VereinKey` FOREIGN KEY (`VId`) REFERENCES `Verein` (`Id`) ON DELETE NO ACTION ON UPDATE NO ACTION, ADD CONSTRAINT `SpielerKey` FOREIGN KEY (`SId`) REFERENCES `Spieler` (`Id`) ON DELETE NO ACTION ON UPDATE NO ACTION;";
    alt[5] = "ALTER TABLE `Spielt_in` ADD CONSTRAINT `VerKey` FOREIGN KEY (`VId`) REFERENCES `Verein` (`Id`), ADD CONSTRAINT `LigaKey` FOREIGN KEY (`LId`) REFERENCES `Liga` (`Id`) ON DELETE NO ACTION ON UPDATE NO ACTION;";
    
    for ( int i=0; i<6; i++ ) {
      try {
        Statement stmt = conn_dst.createStatement();
        stmt.executeUpdate(sql[i]);
        System.out.print("Create Table '"+tableName[i]+"'");
        System.out.println("...done.");
        this.close_st(stmt);
      } catch (SQLException e) {
        System.out.println("Warning: Could not create table");
        e.printStackTrace();
        return;
      }
      
      if ( alt[i].length() != 0 ) {
        try {
          Statement stmt = conn_dst.createStatement();
          System.out.print("Alter Table '"+tableName[i]+"'");
          stmt.executeUpdate(alt[i]);
          System.out.println("...done.");
          this.close_st(stmt);
        } catch (SQLException e) {
          System.out.println("Warning: Could not create table");
          e.printStackTrace();
          return;
        }
      }
    }
  }

  public void fillTables_dst() throws SQLException {
    Statement stmt_src = this.conn_src.createStatement();
    Statement stmt_dst = this.conn_dst.createStatement();
    
    System.out.print("Fill Table Liga...");
    ResultSet rset_src = this.cr_rs(stmt_src, "SELECT * FROM Liga;");
    while (rset_src.next()) {
      String sql = "INSERT INTO Liga ";
             sql+= "VALUES ( '"+rset_src.getInt   (1)+"'";
             sql+= ", '"       +rset_src.getString(2)+"'";
             sql+= " )";
      stmt_dst.executeUpdate(sql);
    }
    System.out.println("done.");
    
    System.out.print("Fill Table Verein...");
    rset_src = this.cr_rs(stmt_src, "SELECT * FROM Verein;");
    while (rset_src.next()) {
      String sql = "INSERT INTO Verein ";
             sql+= "VALUES ( '"+rset_src.getInt   (1)+"'";
             sql+= ", '"       +rset_src.getString(2)+"'";
             sql+= " )";
      stmt_dst.executeUpdate(sql);
    }
    System.out.println("done.");
    
    System.out.print("Fill Table Spieler...");
    rset_src = this.cr_rs(stmt_src, "SELECT * FROM Spieler;");
    while (rset_src.next()) {
      String sql = "INSERT INTO Spieler ";
             sql+= "VALUES ( '"+rset_src.getInt   (1)+"'";
             sql+= ", '"       +rset_src.getString(4)+"'";
             sql+= ", '"       +rset_src.getInt   (3)+"'";
             sql+= ", '"       +rset_src.getString(5)+"'";
             sql+= " )";
      stmt_dst.executeUpdate(sql);
    }
    System.out.println("done.");
    
    System.out.print("Fill Table Spiel...");
    rset_src = this.cr_rs(stmt_src, "SELECT * FROM Spiel;");
    while (rset_src.next()) {
      String sql = "INSERT INTO Spiel ";
             sql+= "VALUES ( '"+rset_src.getInt   (1)+"'";
             sql+= ", '"       +rset_src.getInt   (2)+"'";
             sql+= ", '"       +rset_src.getDate  (3)+"'";
             sql+= ", '"       +rset_src.getTime  (4)+"'";
             sql+= ", '"       +rset_src.getInt   (7)+"'";
             sql+= ", '"       +rset_src.getInt   (8)+"'";
             sql+= ", '"       +rset_src.getInt   (5)+"'";
             sql+= ", '"       +rset_src.getInt   (6)+"'";
             sql+= " )";
      //System.out.println(sql);
      stmt_dst.executeUpdate(sql);
    }
    System.out.println("done.");
    
    System.out.print("Fill Table Spielt_fuer...");
    String query = "Select * ";
           query += "From Spieler, Verein ";
           query += "Where Spieler.Vereins_ID = Verein.V_ID;";
    rset_src = this.cr_rs(stmt_src, query );
    while (rset_src.next()) {
      String sql = "INSERT INTO Spielt_fuer ";
             sql+= "VALUES ( '"+rset_src.getInt   (1)+"'";
             sql+= ", '"       +rset_src.getInt   (2)+"'";
             sql+= ", '51'";
             sql+= ", '"       +rset_src.getInt   (7)+"'";
             sql+= ", '"       +rset_src.getInt   (3)+"'";
             sql+= " )";
      //System.out.println(sql);
      stmt_dst.executeUpdate(sql);
    }
    System.out.println("done.");
    
    System.out.print("Fill Table Spielt_in...");
    query =  "Select Liga_Nr, V_ID ";
    query += "From Liga, Verein ";
    //System.out.println(query);
    rset_src = this.cr_rs(stmt_src, query );
    while (rset_src.next()) {
      String sql = "INSERT INTO Spielt_in ";
             sql+= "VALUES ( '"+rset_src.getInt   (1)+"'";
             sql+= ", '"       +rset_src.getInt   (2)+"'";
             sql+= ", '51'";
             sql+= " )";
      //System.out.println(sql);
      stmt_dst.executeUpdate(sql);
    }
    System.out.println("done.");
    
    this.close_rs(rset_src);
    this.close_st(stmt_src);
    this.close_st(stmt_dst);
  }
  
  public static void main(String[] args) throws SQLException {
    BL dbs = new BL();

    dbs.init_src();
    dbs.init_dst();

    dbs.dropTables_dst();
    dbs.createTables_dst();
    dbs.fillTables_dst();
    
    dbs.deinit();
  }
}
