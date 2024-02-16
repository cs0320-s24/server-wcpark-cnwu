package edu.brown.cs.student.Server;

import static spark.Spark.after;

import org.checkerframework.checker.units.qual.C;
import spark.Spark;
public class Server {
  static final int port = 3232;
  private String filepath;
  private boolean headers;
  private final ICensusDataSource source;
  public Server(ICensusDataSource source){
    this.source = source;
    this.filepath = null;
    Spark.port(port);
    after(
        (request, response) -> {
          response.header("Access-Control-Allow-Origin", "*");
          response.header("Access-Control-Allow-Methods", "*");
        });

    ICensusDataSource originalCensusHandler = new CensusAPIHandler();

    // Wrap it with the caching layer

    // Use the caching handler for the "broadband" route
    Spark.get("broadband", new CensusAPIHandler());
    Spark.get("loadcsv", new LoadHandler(this));
    Spark.get("viewcsv", new ViewHandler(this));
    Spark.get("searchcsv", new SearchHandler(this));

    Spark.init();
    Spark.awaitInitialization();
    System.out.println("Server started at http://localhost:" + port);
  }

  public void loadFile(String filepath, boolean headers){
    this.filepath = filepath;
    this.headers = headers;
  }
  public String getFile(){
    return this.filepath;
  }
  public boolean getHeaders(){
    return this.headers;
  }
  public static void main(String[] args){
    Server server = new Server(new CensusAPIHandler());
  }
}
