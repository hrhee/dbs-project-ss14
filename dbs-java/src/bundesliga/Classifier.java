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

  public int[]     aErg;
  public int[]     aSpt;
  public int[]     aT3s;
  public int[]     aT1s;
  public int[]     aGt3s;
  public int[]     aGt1s;
  public int[]     aN5s;
  public int[]     aN1s;
  public double[]  aTD5s;
  public boolean[] aHeim;
  public int[]     aHt1s;
  public int[]     aHt3s;
  public int[]     aE1s;
  
  public static int N   = 64;
  public static int OFF = 10;
  public static int NV  = 56;


  
  public Classifier() {
    conn  = null;
    d     = new Data();
    aErg  = new int[N];
    aSpt  = new int[N];
    aT3s  = new int[N];
    aT1s  = new int[N];
    aGt3s = new int[N];
    aGt1s = new int[N];
    aN5s  = new int[N];
    aN1s  = new int[N];
    aTD5s = new double[N];
    aHeim = new boolean[N];
    aHt1s = new int[N];
    aHt3s = new int[N];
    aE1s  = new int[N];
    
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
    //System.out.println(sql1);
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
  
  public void writeToFile( String name, String content ) {
    try {
      File file = new File( d.RES + name + ".arff");

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
  
  public String writeHead(  String name, String arff ) {
    
    arff += "@relation " + name + "\n";
    arff += "\n";
    arff += "@attribute T3S  numeric\n";
    arff += "@attribute GT3S numeric\n";
    arff += "@attribute N5S  numeric\n";
    arff += "@attribute D5S  numeric\n";
    arff += "@attribute Heim {true, false}\n";
    arff += "@attribute Ht3s numeric\n";
    arff += "@attribute E1s  numeric\n";
    arff += "@attribute ergebnis {-1, 0, 1}\n";
    arff += "\n\n@data\n";
    return arff;
  }
  
  public String writeData() {
    String arrf = "";
    int start = OFF+1;
    for (int i = start; i < N; i++) {
      if (aSpt[i] != 0) {
        arrf += aT3s  [i] + ", "; // T3s
        arrf += aGt3s [i] + ", "; // Gt3s
        arrf += aN5s  [i] + ", "; // N5s
        arrf += aTD5s [i] + ", "; // D5s
        arrf += aHeim [i] + ", "; // D5s
        arrf += aHt3s [i] + ", "; // Ht3s
        arrf += aE1s  [i] + ", "; // E1s
        arrf += aErg  [i] + "\n"; // erg
      }
    }
    return arrf;
  }
  
  public void calcFeatures(int vid) {
    try {
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

      while (rset.next()) {
        int spielTag = rset.getInt(1);
        int vereinId = rset.getInt(2);
        int heimId   = rset.getInt(4);
        int ausId    = rset.getInt(5);
        int toreHeim = rset.getInt(6);
        int toreAus  = rset.getInt(7);
        
        int     erg   = 0;
        int     t1s   = 0;
        int     gt1s  = 0;
        int     n1s   = 0;
        boolean bHeim = false;
        int     ht1s  = 0;
        
        if (vereinId == heimId) {
          t1s   = toreHeim; // Spiel.ToreHeim
          gt1s  = toreAus;
          bHeim = true;
          ht1s  = toreHeim;
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
          t1s   = toreAus; // Spiel.ToreAus
          gt1s  = toreHeim;
          bHeim = false;
          ht1s  = 0;
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
        aHeim[spielTag + OFF] = bHeim;
        aHt1s[spielTag + OFF] = ht1s;
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
        
        aHt3s[i] = 0;
        for ( int j=1; j<=6; j++ ) {
          if (aHeim[i-j]) aHt3s[i] += aHt1s[i-j] - aHt1s[i-j-1];
        }
        
        aE1s[i] = aErg[i-1];
      } 
    } catch (SQLException e) {
        e.printStackTrace();
        return;
    }
  }
  
  public void writeArrf() {
    for (int vid=1; vid<=NV; vid++ ) {
      String name    = "";
      String content = "";
      try {
        name = this.getName(vid);
        this.calcFeatures( vid );
      } catch (SQLException e) {
        e.printStackTrace();
        return;
      }
      
      content += this.writeHead(name, content);
      content += this.writeData();
      this.writeToFile( name, content );
    }
    return;
  }
  
  public void writeArrf(String name) {
    
    String content = "";
    content += this.writeHead(name, content);
    for ( int vid=1; vid<=NV; vid++ ) {
      this.calcFeatures( vid );
      content += this.writeData();
    }
    this.writeToFile( name, content );
    
    return;
  }
  
  public static void main(String[] args) {
    Classifier csf = new Classifier();
    csf.init();

    csf.writeArrf();
    csf.writeArrf("all");
    
    csf.deinit();
  }

}
