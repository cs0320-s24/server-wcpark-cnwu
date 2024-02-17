package edu.brown.cs.student.Server;

import static spark.Spark.after;

import spark.Spark;

/**
 * Initializes and configures the server, setting up routes for handling CSV file operations
 * and accessing census data. This class also manages the server's state, including the
 * currently loaded CSV file and its configuration.
 */
public class Server {
  static final int port = 3232;
  private String filepath;
  private boolean headers;
  private final ICensusDataSource source;

  /**
   * Constructs the server with a specified data source for census data.
   * This data source can be a direct handler or a cached wrapper around the handler.
   *
   * @param source The data source to use for fetching census data.
   */
  public Server(ICensusDataSource source){
    this.source = source;
    this.filepath = null;
    Spark.port(port);
    after(
        (request, response) -> {
          response.header("Access-Control-Allow-Origin", "*");
          response.header("Access-Control-Allow-Methods", "*");
        });

    Spark.get("broadband", new CensusAPIHandler());
    Spark.get("loadcsv", new LoadHandler(this));
    Spark.get("viewcsv", new ViewHandler(this));
    Spark.get("searchcsv", new SearchHandler(this));

    Spark.init();
    Spark.awaitInitialization();
    System.out.println("Server started at http://localhost:" + port);
  }

  /**
   * Updates the server's state to reflect the currently loaded CSV file.
   * This method is called to load a new CSV file and specify whether the file contains headers.
   *
   * @param filepath The absolute path to the CSV file that should be loaded.
   * @param headers A boolean indicating if the first row of the CSV file contains column headers.
   */
  public void loadFile(String filepath, boolean headers){
    this.filepath = filepath;
    this.headers = headers;
  }

  /**
   * Retrieves the file path of the currently loaded CSV file.
   *
   * @return The absolute path to the currently loaded CSV file, or null if no file is loaded.
   */
  public String getFile(){
    return this.filepath;
  }

  /**
   * Indicates whether the currently loaded CSV file contains headers.
   *
   * @return True if the loaded CSV file includes headers; false otherwise.
   */
  public boolean getHeaders(){
    return this.headers;
  }

  /**
   * The entry point of the application. Creates an instance of the Server
   * with a specific data source for handling census data.
   *
   * @param args Command-line arguments passed to the application (currently not used).
   */
  public static void main(String[] args){
    Server server = new Server(new CensusAPIHandler());
  }
}
