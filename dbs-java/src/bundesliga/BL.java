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
	public Data d;
	
	public enum Liga { NULL, NR, VERB, ERST, MEISTER, REKORDSP, SPIELE_REK }
	
	public BL() {
		conn_src = null;
		conn_dst = null;
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
	 
		//System.out.println("MySQL JDBC Driver Registered!");
	 
		try {
			conn_src = DriverManager.getConnection(d.DB_URL+d.DB_NAME, d.USR, d.PW );
	 
		} catch (SQLException e) {
			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
			return;
		}
	 
		if (conn_src != null) {
			System.out.println("Connection to database "+ d.DB_NAME +" established");
		} else {
			System.out.println("Failed to make connection!");
		}
    }
    
    public void init_dst() {
    	System.out.println("Create my Database");
		try {
			conn_dst = DriverManager.getConnection(d.DB_URL, d.USR, d.PW );
	 
		} catch (SQLException e) {
			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
			return;
		}
		
		try { 
			Statement stmt = conn_dst.createStatement();
			String    sql  = "CREATE DATABASE IF NOT EXISTS " + d.FU_DBS;
			stmt.executeUpdate(sql);
			System.out.println("Database created successfully...");
			this.close_st(stmt);
			
		} catch (SQLException e) {
			System.out.println("Warning: Could not create statement");
			e.printStackTrace();
			return;
		}
    }
    
    public ResultSet cr_rs ( Statement stmt, String sql ) throws SQLException {
    	ResultSet rs = stmt.executeQuery( sql );
    	return rs;
    }
    
    public void close_rs ( ResultSet rset ) throws SQLException {
		if (rset != null) {
			rset.close();
		}
    }
    
    public void close_st ( Statement st ) throws SQLException {
		if (st != null) {
			st.close();
		}
    }
    
	public static void main(String[] args) throws SQLException {
		BL dbs = new BL();
		
		dbs.init_src();
		
		Statement stmt  = dbs.conn_src.createStatement();
		
		ResultSet rset1 = dbs.cr_rs(stmt, "SELECT * FROM Liga;");

		
		
		while (rset1.next()){
			System.out.print  (rset1.getInt   (Liga.NR        .ordinal()) + ", ");
			System.out.print  (rset1.getString(Liga.VERB      .ordinal()) + ", ");
			System.out.print  (rset1.getDate  (Liga.ERST      .ordinal()) + ", ");
			System.out.print  (rset1.getInt   (Liga.MEISTER   .ordinal()) + ", ");
			System.out.print  (rset1.getString(Liga.REKORDSP  .ordinal()) + ", ");
			System.out.println(rset1.getInt   (Liga.SPIELE_REK.ordinal())       );
		}
		
		dbs.close_rs( rset1 );
        dbs.close_st( stmt  );
		
        
        dbs.init_dst();
		
		
		
		

	}
}

