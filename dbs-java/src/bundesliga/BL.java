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
    tableName[4] = "spielt_fuer";
    tableName[5] = "spielt_in";
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
    System.out.println("Create Database "+d.FU_DBS);
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

  }
  
  public void dropTables() {
    for ( int i=5; i>=0; i-- ) {
      try {
        Statement stmt = conn_dst.createStatement();
        String s = "DROP TABLE " + tableName[i];
        System.out.println(s);
        stmt.executeUpdate( "DROP TABLE " + tableName[i] );
        System.out.println("Table '"+tableName[i]+"' dropped.");
        this.close_st(stmt);
      } catch (SQLException e) {
        System.out.println("Warning: Could not drop table");
        e.printStackTrace();
        return;
      }
    }
  }
  
  public void createTables() {
    String [] sql  = new String[6];
    String [] alt  = new String[6];
    sql[0]  ="CREATE TABLE IF NOT EXISTS `Liga` (`Id` int(11) NOT NULL, `Name` varchar(40) COLLATE utf8_unicode_ci NOT NULL, PRIMARY KEY (`Id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";
    sql[1]  ="CREATE TABLE IF NOT EXISTS `Verein` (`Id` int(11) NOT NULL, `Name` varchar(255) COLLATE utf8_unicode_ci NOT NULL, PRIMARY KEY (`Id`) ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";
    sql[2]  ="CREATE TABLE IF NOT EXISTS `Spieler` ( `Id` int(11) NOT NULL, `Name` varchar(255) COLLATE utf8_unicode_ci NOT NULL, `TrikotNr` int(11) NOT NULL, `Heimatland` varchar(255) COLLATE utf8_unicode_ci NOT NULL, PRIMARY KEY (`Id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";
    sql[3]  = "CREATE TABLE IF NOT EXISTS `Spiel` ( `Id` int(11) NOT NULL, `Spieltag` int(11) NOT NULL, `Datum` date NOT NULL, `Uhrzeit` time NOT NULL, `ToreHeim` int(11) NOT NULL, `ToreAus` int(11) NOT NULL, `Heim` int(11) NOT NULL, `Aus` int(11) NOT NULL, PRIMARY KEY (`Id`), KEY `Heim` (`Heim`), KEY `Aus` (`Aus`)) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";
    sql[4]  = "CREATE TABLE IF NOT EXISTS `spielt_fuer` ( `SId` int(11) NOT NULL, `VId` int(11) NOT NULL, `Saison` date NOT NULL, `Tore` int(11) NOT NULL, `TrikotNr` int(11) NOT NULL, PRIMARY KEY (`SId`,`VId`), KEY `VereinKey` (`VId`)) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";
    sql[5]  = "CREATE TABLE IF NOT EXISTS `spielt_in` ( `LId` int(11) NOT NULL, `VId` int(11) NOT NULL, `Saison` date NOT NULL, PRIMARY KEY (`LId`,`VId`)) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    alt[0] = "";
    alt[1] = "";
    alt[2] = "";
    alt[3] = "ALTER TABLE `Spiel` ADD CONSTRAINT `AusKey` FOREIGN KEY (`Aus`) REFERENCES `Verein` (`Id`) ON DELETE NO ACTION ON UPDATE NO ACTION, ADD CONSTRAINT `HeimKey` FOREIGN KEY (`Heim`) REFERENCES `Verein` (`Id`) ON DELETE NO ACTION ON UPDATE NO ACTION;";
    alt[4] = "ALTER TABLE `spielt_fuer` ADD CONSTRAINT `VereinKey` FOREIGN KEY (`VId`) REFERENCES `Verein` (`Id`) ON DELETE NO ACTION ON UPDATE NO ACTION, ADD CONSTRAINT `SpielerKey` FOREIGN KEY (`SId`) REFERENCES `Spieler` (`Id`) ON DELETE NO ACTION ON UPDATE NO ACTION;";
    alt[5] = "ALTER TABLE `spielt_in` ADD CONSTRAINT `VerKey` FOREIGN KEY (`VId`) REFERENCES `Verein` (`Id`), ADD CONSTRAINT `LigaKey` FOREIGN KEY (`LId`) REFERENCES `Liga` (`Id`) ON DELETE NO ACTION ON UPDATE NO ACTION;";
    
    for ( int i=0; i<6; i++ ) {
      try {
        Statement stmt = conn_dst.createStatement();
        stmt.executeUpdate(sql[i]);
        System.out.println("Table '"+tableName[i]+"' created.");
        this.close_st(stmt);
      } catch (SQLException e) {
        System.out.println("Warning: Could not create table");
        e.printStackTrace();
        return;
      }
      
      if ( alt[i].length() != 0 ) {
        try {
          Statement stmt = conn_dst.createStatement();
          stmt.executeUpdate(alt[i]);
          System.out.println("Table '"+tableName[i]+"' altered.");
          this.close_st(stmt);
        } catch (SQLException e) {
          System.out.println("Warning: Could not create table");
          e.printStackTrace();
          return;
        }
      }
    }
  }

  public static void main(String[] args) throws SQLException {
    BL dbs = new BL();

    dbs.init_src();
    dbs.init_dst();
    dbs.connect(dbs.d.FU_DBS);
    dbs.dropTables();
    dbs.createTables();
    
    Statement stmt = dbs.conn_src.createStatement();
    ResultSet rset1 = dbs.cr_rs(stmt, "SELECT * FROM Liga;");

    while (rset1.next()) {
      System.out.print(rset1.getInt(Liga.NR.ordinal()) + ", ");
      System.out.print(rset1.getString(Liga.VERB.ordinal()) + ", ");
      //System.out.print(rset1.getDate(Liga.ERST.ordinal()) + ", ");
      System.out.print(rset1.getInt(Liga.MEISTER.ordinal()) + ", ");
      //System.out.print(rset1.getString(Liga.REKORDSP.ordinal()) + ", ");
      System.out.println(rset1.getInt(Liga.SPIELE_REK.ordinal()));
    }

    dbs.close_rs(rset1);
    dbs.close_st(stmt);



  }
}
