package bundesliga;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
//import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class Classifier {
  public Connection conn;
  public Data       d;
//  int[][]           lf1;
  int[]             aErg;
  int[]             aSpt;
  int[]             aT3s;
  int[]             aT1s;
  int[]             aGt3s;
  int[]             aGt1s;
  int[]             aN5s;
  int[]             aN1s;
  double[]          aTD5s;
  
  public static int N   = 64;
  public static int X   = 10;
  public static int OFF = 10;
  public static int MAX = 34;

  
  public Classifier() {
    conn = null;
    d    = new Data();
    
//    lf1 = new int[N][];
//    for (int i = 0; i < N; i++) {
//      lf1[i] = new int[X];
//    }
    
    aErg  = new int[N];
    aSpt  = new int[N];
    aT3s  = new int[N];
    aT1s  = new int[N];
    aGt3s = new int[N];
    aGt1s = new int[N];
    aN5s  = new int[N];
    aN1s  = new int[N];
    aTD5s  = new double[N];
  }
  
  public void init() {
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
      conn = DriverManager.getConnection(d.DB_URL + d.FU_DBS, d.USR, d.PW);

    } catch (SQLException e) {
      System.out.println("Connection Failed! Check output console");
      e.printStackTrace();
      return;
    }

    if (conn != null) {
      System.out
          .println("Connection to database " + d.FU_DBS + " established");
    } else {
      System.out.println("Failed to make connection!");
    }
  }
  
  public void deinit() {
    Boolean b = false; 
    try {
      if ( this.conn != null ) {
        this.conn.close();
        b = true;
      }
    } catch (SQLException e) {
      System.out.println("Failed to close connection");
      e.printStackTrace();
      return;
    }
    
    if ( b ) System.out.println("Connection closed.");
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
  
  public String getName( int vid ) throws SQLException {
    Statement stmt1 = this.conn.createStatement();
    String sql1 = "SELECT Verein.Name FROM Verein ";
    sql1 += "WHERE Verein.Id=" + vid;
    System.out.println(sql1);
    ResultSet rset1 = stmt1.executeQuery(sql1);
    
    String name="";
    while (rset1.next()) {
      name = rset1.getString(1);
    }
    name = name.replace(' ', '_');
    close_st( stmt1 );
    close_rs( rset1 );
    
    return name;
  }
  
  public void writeContent( String name, String content ) {
    try {
      File file = new File("./result/" + name + ".arff");

      if (!file.exists()) {
        file.createNewFile();
      }

      FileWriter fw = new FileWriter(file.getAbsoluteFile());
      BufferedWriter bw = new BufferedWriter(fw);
      bw.write(content);
      bw.close();

      System.out.println("Writing File "+ name+".arff ...done");

    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  public void writeArrf(int vid) {
    try {
      String name = getName(vid);
      Statement stmt = this.conn.createStatement();
      String sql = "Select Spiel.Spieltag, Verein.Id, Verein.Name, ";
      sql += "Spiel.Heim, Spiel.Aus, Spiel.ToreHeim, Spiel.ToreAus ";
      sql += "From Spiel, Verein ";
      sql += "WHERE Verein.Id = ";
      sql += "" + vid;
      sql += " AND ";
      sql += "( Verein.Id = Spiel.Heim OR Verein.Id = Spiel.Aus ) ";
      sql += "ORDER BY Spiel.Spieltag ";

      ResultSet rset = stmt.executeQuery(sql);

      String content = "@relation " + name + "\n";
      content += "\n";
      content += "@attribute T3S numeric\n";
      content += "@attribute GT3S numeric\n";
      content += "@attribute N5S numeric\n";
      content += "@attribute D5S numeric\n";
      content += "@attribute ergebnis {-1, 0, 1}\n";
      content += "\n\n@data\n";

      while (rset.next()) {
        int spielTag = rset.getInt(1);
        int vereinId = rset.getInt(2);
        int heimId   = rset.getInt(4);
        int ausId    = rset.getInt(5);
        int toreHeim = rset.getInt(6);
        int toreAus  = rset.getInt(7);
        
        String output = "";

        output += "Spieltag: " + spielTag          + ", "; // Spiel.Spieltag
        output += "VId: "      + vereinId          + ", "; // Verein.Id
        output += "VName: "    + rset.getString(3) + ", "; // Verein.Name
        output += "HeimId: "   + heimId            + ", "; // Spiel.Heim
        output += "AusId: "    + ausId             + ", "; // Spiel.Aus
        output += "ToreHeim: " + toreHeim          + ", "; // Spiel.ToreHeim
        output += "ToreAus: "  + toreAus; // Spiel.ToreAus
        
        System.out.println(output);

        int erg  = 0;
        int t1s  = 0;
        int gt1s = 0;
        int n1s  = 0;
        
        if (vereinId == heimId) {
          t1s  = toreHeim; // Spiel.ToreHeim
          gt1s = toreAus;
          if (toreHeim > toreAus) {
            erg = 1;
            n1s = 0;
          }
          else if (toreAus > toreHeim) {
            erg = -1;
            n1s = 1;
          }
          else {
            erg = 0;
            n1s = 0;
          }
        } else if (vereinId == ausId) {
          t1s  = toreAus; // Spiel.ToreAus
          gt1s = toreHeim;
          if (toreAus > toreHeim) {
            erg = 1;
            n1s = 0;
          }
          else if (toreHeim > toreAus) {
            erg = -1;
            n1s = 1;
          }
          else {
            erg = 0;
            n1s = 0;
          }
        }
        
        aSpt [spielTag + OFF] = spielTag;
        aErg [spielTag + OFF] = erg;
        aT1s [spielTag + OFF] = t1s;
        aGt1s[spielTag + OFF] = gt1s;
        aN1s [spielTag + OFF] = n1s;
        
      }

      int start = OFF + 1;

      for (int i = start; i < N; i++) {
        aT3s [i] = aT1s [i-1] + aT1s [i-2] + aT1s [i-3];
        aGt3s[i] = aGt1s[i-1] + aGt1s[i-2] + aGt1s[i-3];

        aN5s[i]  = 0;
        for ( int j=1; j<=5; j++) {
          aN5s[i] += aN1s[i-j];
        }
        
        aTD5s[i] = 0.0;
        for ( int j=1; j<=5; j++ ) {
          aTD5s[i] += aT1s[i-j] - aT1s[i-j-1];
          //System.out.println( aSpt[i-j] +" "+ aSpt[i-j-1] );
        }
        aTD5s[i] /= 5;

        String output = "Tag: " + aSpt[i];
        output += " Erg : " + aErg[i];
        output += " T3S : "       + aT3s[i];
        output += " GT3S : "      + aGt3s[i];
        output += " N5S : "       + aN5s[i];
        output += " D5S : "       + aTD5s[i];
        System.out.println(output);

        if (aSpt[i] != 0) {
          content += aT3s [i] + ", "; // T3Sp
          content += aGt3s[i] + ", "; // T3Sp
          content += aN5s [i] + ", "; // N5S
          content += aTD5s [i] + ", "; // D5S
          content += aErg [i] + "\n"; // erg
        }
      }

      this.writeContent( name, content );
      
    } catch (SQLException e) {
      e.printStackTrace();
      return;
    }
  }
  
  public static void main(String[] args) {
    Classifier csf = new Classifier();
    csf.init();
    
    int vid = 1;
    csf.writeArrf(vid);
    
    csf.deinit();
  }

}
