package edu.brown.cs.student.Server;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Types;
import java.lang.reflect.Type;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import com.squareup.moshi.Moshi;
import okio.Buffer;
import org.junit.jupiter.api.BeforeAll;
import spark.Spark;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test class for LoadHandler
 */
public class LoadHandlerTest {
  //credit to reng1 helped set up testing and cases.

  /**
   * Sets up Spark
   */
  @BeforeAll
  public static void setup_before_everything() {
    Spark.port(0);
    Logger.getLogger("").setLevel(Level.WARNING);
  }

  final Server server = new Server(new CensusAPIHandler());


  /**
   * Makes new Handlers for each test
   */
  @BeforeEach
  public void setup() {
    Spark.get("/loadcsv", new LoadHandler(server));
    Spark.init();
    Spark.awaitInitialization();
  }

  /**
   * Shuts down Spark
   */
  @AfterEach
  public void teardown() {
    Spark.unmap("/loadcsv");
    Spark.awaitStop();
  }

  /**
   * Tests loading when no file or directory is specified
   * @throws IOException
   */
  @Test
  public void testNoFile() throws IOException {
    HttpURLConnection clientConnection = query("loadcsv?directory=C:\\Users\\Willi\\OneDrive\\Desktop\\cs320\\server-wcpark-cnwu\\data");
    assertEquals(200, clientConnection.getResponseCode());
    Map<String, Object> res = moshiHelper(clientConnection);
    assertEquals(Map.of("result", "error_bad_request", "message", "file not given"), res);
    clientConnection.disconnect();
  }

  /**
   * Tests successfully loading a file
   * @throws IOException
   */
  @Test
  public void testSuccessLoad() throws IOException {
    HttpURLConnection clientConnection = query("loadcsv?file=stardata.csv&directory=C:\\Users\\Willi\\OneDrive\\Desktop\\cs320\\server-wcpark-cnwu\\data");
    assertEquals(200, clientConnection.getResponseCode());
    Map<String, Object> res = moshiHelper(clientConnection);
    assertEquals(Map.of("result", "success", "file", "stardata.csv"), res);
    clientConnection.disconnect();
  }

  /**
   * Tests loading an invalid file
   * @throws IOException
   */
  @Test
  public void testBadFile() throws IOException {
    //Tests for unknown file
    HttpURLConnection clientConnection = query(
        "loadcsv?file=unprotecte.csv&directory=C:\\Users\\Willi\\OneDrive\\Desktop\\cs320\\server-wcpark-cnwu\\data");
    assertEquals(200, clientConnection.getResponseCode());
    Map<String, Object> res = moshiHelper(clientConnection);
    assertEquals(Map.of("result", "error_bad_request", "message",
        "file not found within protected directory"), res);

    //Tests for file outside protected directory
    clientConnection = query(
        "loadcsv?file=unprotected.csv&directory=C:\\Users\\Willi\\OneDrive\\Desktop\\cs320\\server-wcpark-cnwu\\data");
    assertEquals(200, clientConnection.getResponseCode());
    res = moshiHelper(clientConnection);
    assertEquals(Map.of("result", "error_bad_request", "message",
        "file not found within protected directory"), res);

    clientConnection.disconnect();
  }

  /**
   * Tests bad API call
   * @throws IOException
   */
  @Test
  public void testAPICall() throws IOException {
    HttpURLConnection clientConnection = query("loadcsv1");
    assertEquals(404, clientConnection.getResponseCode());
    clientConnection.disconnect();
  }

  /**
   * Helper method to retrieve mock data from Json format
   * @param clientConnection
   * @return Deserialized data from connection
   * @throws IOException
   */
  public Map<String,Object> moshiHelper(HttpURLConnection clientConnection) throws IOException {
    Moshi moshi = new Moshi.Builder().build();
    Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
    JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);
    return adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
  }

  /**
   * Helper method to make a specified API call
   * @param apiCall
   * @return connection to the URL
   * @throws IOException
   */
  public HttpURLConnection query(String apiCall) throws IOException {
    URL requestURL = new URL("http://localhost:"+Spark.port()+"/"+apiCall);
    HttpURLConnection clientConnection = (HttpURLConnection)requestURL.openConnection();
    clientConnection.connect();
    return clientConnection;
  }
}