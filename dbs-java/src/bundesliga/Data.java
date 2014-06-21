package bundesliga;

public class Data {
  public String DB_URL;
  public String DB_NAME;
  public String USR;
  public String PW;
  public String FU_DBS;
  
  public Data() {
    DB_URL  = "jdbc:mysql://localhost:3306/";
    DB_NAME = "bundesliga";
    USR     = "franz";
    PW      = "bundesliga";
    FU_DBS  = "FU_BL";
  }
}
