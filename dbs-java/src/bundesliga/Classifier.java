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
  int[][]           lf1;
  
  public static int N   = 64;
  public static int X   = 10;
  public static int OFF = 10;
  public static int MAX = 34;

  
  public Classifier() {
    conn = null;
    d    = new Data();
    
    lf1 = new int[N][];
    for (int i = 0; i < N; i++) {
      lf1[i] = new int[X];
    }
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

      System.out.println("Done");

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
        int[] tmp = new int[X];

        tmp[0] = spielTag; // Spiel.Spieltag
        tmp[1] = vereinId; // Verein.Id
        
        
        // tmp[2] Ergebnis
        // tmp[3] Anzahl der Tore pro Spieltag
        // tmp[4] Anzahl der Gegentore pro Spieltag
        // tmp[5] Niederlange am Spieltag
        
        if (vereinId == heimId) {
          tmp[3] = toreHeim; // Spiel.ToreHeim
          tmp[4] = toreAus;
          if (toreHeim > toreAus) {
            tmp[2] = 1;
            tmp[5] = 0;
          }
          else if (toreAus > toreHeim) {
            tmp[2] = -1;
            tmp[5] = 1;
          }
          else {
            tmp[2] = 0;
            tmp[5] = 0;
          }
        } else if (vereinId == ausId) {
          tmp[3] = toreAus; // Spiel.ToreAus
          tmp[4] = toreHeim;
          if (toreAus > toreHeim) {
            tmp[2] = 1;
            tmp[5] = 0;
          }
          else if (toreHeim > toreAus) {
            tmp[2] = -1;
            tmp[5] = 1;
          }
          else {
            tmp[2] = 0;
            tmp[5] = 0;
          }
        }

        lf1[spielTag + OFF][0] = tmp[0]; // Spieltag
        lf1[spielTag + OFF][1] = tmp[2]; // ergebnis
        lf1[spielTag + OFF][2] = tmp[3]; // Tore pro Spieltag
        //lf1[spielTag + OFF][3] = Tore letzte drei Spiele
        lf1[spielTag + OFF][4] = tmp[4]; // Gegentore pro Spieltag
        // lf1[spielTag + OFF][5] = Gegentore letzte drei Spiele
        lf1[spielTag + OFF][6] = tmp[5]; // Niederlage am Spieltag
        // lf1spielTag + OFF][7] = Anzahl Niederlagen in den letzten fünf Spielen
      }

      int start = OFF + 1;

      for (int i = start; i < N; i++) {
        // Anzahl der Tore in den letzten drei Spielen
        lf1[i][3] = lf1[i - 1][2] + lf1[i - 2][2] + lf1[i - 3][2];
        // Anzahl der Gegentore in den letzten drei Spielen
        lf1[i][5] = lf1[i - 1][4] + lf1[i - 2][4] + lf1[i - 3][4];
        lf1[i][7] = 0;
        for ( int j=1; j<=5; j++) {
          lf1[i][7] += lf1[i-j][6];
        }
        String output = " Erg : " + lf1[i][1];
        output += " T3S : "       + lf1[i][3];
        output += " GT3S : "      + lf1[i][5];
        output += " N5S : "       + lf1[i][7];
        System.out.println(output);
        if (lf1[i][0] != 0) {
          content += lf1[i][3] + ", "; // T3Sp
          content += lf1[i][5] + ", "; // T3Sp
          content += lf1[i][7] + ", "; // N5S
          content += lf1[i][1] + "\n"; // erg
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
