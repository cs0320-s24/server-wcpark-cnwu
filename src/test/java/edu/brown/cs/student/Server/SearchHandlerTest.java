
package MockServer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.Server.CensusAPIHandler;
import edu.brown.cs.student.Server.LoadHandler;
import edu.brown.cs.student.Server.SearchHandler;
import edu.brown.cs.student.Server.Server;
import edu.brown.cs.student.Server.ViewHandler;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import okio.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Spark;

/**
 * Test class for SearchHandler
 */
public class SearchHandlerTest {
  Server server = new Server(new CensusAPIHandler());

  /**
   * Sets up Spark
   */
  @BeforeAll
  public static void setup_before_everything() {
    Spark.port(0);
    Logger.getLogger("").setLevel(Level.WARNING);
  }

  @BeforeEach
  public void setup() {
    Spark.get("/loadcsv", new LoadHandler(server));
    Spark.get("/viewcsv", new ViewHandler(server));
    Spark.get("/searchcsv", new SearchHandler(server));
    Spark.init();
    Spark.awaitInitialization();
  }

  /**
   * Shuts down Spark
   */
  @AfterEach
  public void teardown() {
    Spark.unmap("/loadcsv");
    Spark.unmap("/viewcsv");
    Spark.unmap("/searchcsv");
    Spark.awaitStop();
  }

  /**
   * Tests search with ColID
   * @throws IOException
   */
  @Test
  public void testSearchColQuery() throws IOException {
    HttpURLConnection clientConnection = query("loadcsv?file=stardata.csv&directory=C:\\Users\\Willi\\OneDrive\\Desktop\\cs320\\server-wcpark-cnwu\\data");
    assertEquals(200, clientConnection.getResponseCode());
    Map<String, Object> res = moshiHelper(clientConnection);
    assertEquals(Map.of("result", "success", "file", "stardata.csv"),  res);

    clientConnection = query("searchcsv?query=Sol&headers=false&colID=1");
    assertEquals(200, clientConnection.getResponseCode());
    res = moshiHelper(clientConnection);
    assertEquals("{result=success, headers=false, query=Sol, rows=[2.0], colID=1}",  res.toString());
    clientConnection.disconnect();
  }

  /**
   * Tests simple searching of csv col index specified
   * @throws IOException
   */
  @Test
  public void testSearchColIndex() throws IOException {
    HttpURLConnection clientConnection = query("loadcsv?file=stardata.csv&directory=C:\\Users\\Willi\\OneDrive\\Desktop\\cs320\\server-wcpark-cnwu\\data");
    assertEquals(200, clientConnection.getResponseCode());
    Map<String, Object> res = moshiHelper(clientConnection);
    assertEquals(Map.of("result", "success", "file", "stardata.csv"),  res);

    clientConnection = query("searchcsv?query=Sol&headers=false&colID=1");
    assertEquals(200, clientConnection.getResponseCode());
    res = moshiHelper(clientConnection);
    assertEquals("{result=success, headers=false, query=Sol, rows=[2.0], colID=1}",  res.toString());

    clientConnection = query("searchcsv?query=Sol&headers=false&colID=-10");
    assertEquals(200, clientConnection.getResponseCode());
    res = moshiHelper(clientConnection);
    assertEquals("{result=error_bad_request, message=no data in specified column/index}",  res.toString());
    clientConnection.disconnect();
  }

  /**
   * Tests simple searching of csv no col specified
   * @throws IOException
   */
  @Test
  public void testSearchNoCol() throws IOException {
    HttpURLConnection clientConnection = query("loadcsv?file=stardata.csv&directory=C:\\Users\\Willi\\OneDrive\\Desktop\\cs320\\server-wcpark-cnwu\\data");
    assertEquals(200, clientConnection.getResponseCode());
    Map<String, Object> res = moshiHelper(clientConnection);
    assertEquals(Map.of("result", "success", "file", "stardata.csv"),  res);

    clientConnection = query("searchcsv?query=Sol&headers=false");
    assertEquals(200, clientConnection.getResponseCode());
    res = moshiHelper(clientConnection);
    assertEquals("{result=success, headers=false, query=Sol, rows=[2.0]}",  res.toString());

    clientConnection.disconnect();
  }

  /**
   * Tests searching for value within headers when headers=true
   * @throws IOException
   */
  @Test
  public void testHeadersTrue() throws IOException {
    HttpURLConnection clientConnection = query("loadcsv?file=stardata.csv&directory=C:\\Users\\Willi\\OneDrive\\Desktop\\cs320\\server-wcpark-cnwu\\data&headers=true");
    assertEquals(200, clientConnection.getResponseCode());
    Map<String, Object> res = moshiHelper(clientConnection);
    assertEquals(Map.of("result", "success", "file", "stardata.csv"),  res);

    clientConnection = query("searchcsv?query=Sol&colID=ProperName");
    assertEquals(200, clientConnection.getResponseCode());
    res = moshiHelper(clientConnection);
    assertEquals("{result=success, headers=true, query=Sol, rows=[2.0], colID=ProperName}",  res.toString());

    clientConnection.disconnect();
  }

  /**
   * Tests searching with query that has no matches
   * @throws IOException
   */
  @Test
  public void testSearchBadQuery() throws IOException {
    HttpURLConnection clientConnection = query("loadcsv?file=stardata.csv&directory=C:\\Users\\Willi\\OneDrive\\Desktop\\cs320\\server-wcpark-cnwu\\data");
    assertEquals(200, clientConnection.getResponseCode());
    Map<String, Object> res = moshiHelper(clientConnection);
    assertEquals(Map.of("result", "success", "file", "stardata.csv"),  res);

    clientConnection = query("searchcsv?query=soll");
    assertEquals(200, clientConnection.getResponseCode());
    res = moshiHelper(clientConnection);
    assertEquals("{result=error_bad_request, message=no data in specified column/index}",  res.toString());

    clientConnection.disconnect();
  }

  /**
   * tests searching before file is loaded
   * @throws IOException
   */
  @Test
  public void testFileNotLoaded() throws IOException {
    HttpURLConnection clientConnection = query("searchcsv?query=Sol");
    assertEquals(200, clientConnection.getResponseCode());
    Map<String, Object> res = moshiHelper(clientConnection);
    assertEquals(Map.of("result", "error_datasource", "message", "filepath is not loaded"),  res);

    clientConnection.disconnect();
  }

  /**
   * tests searching after load is called on a file that doesn't exist
   * @throws IOException
   */
  @Test
  public void testSearchFileNotFound() throws IOException {
    HttpURLConnection clientConnection = query("loadcsv?file=stardat.csv&directory=C:\\Users\\Willi\\OneDrive\\Desktop\\cs320\\server-wcpark-cnwu\\data");
    assertEquals(200, clientConnection.getResponseCode());

    clientConnection = query("searchcsv?query=Sol");
    assertEquals(200, clientConnection.getResponseCode());

    Map<String, Object> res = moshiHelper(clientConnection);
    assertEquals(Map.of("result", "error_datasource", "message", "filepath is not loaded"),  res);

    clientConnection.disconnect();
  }

  /**
   * tests searching when there is no query specified
   * @throws IOException
   */
  @Test
  public void testNoQuery() throws IOException {
    HttpURLConnection clientConnection = query("loadcsv?file=stardata.csv&directory=C:\\Users\\Willi\\OneDrive\\Desktop\\cs320\\server-wcpark-cnwu\\data");
    assertEquals(200, clientConnection.getResponseCode());
    Map<String, Object> res = moshiHelper(clientConnection);
    assertEquals(Map.of("result", "success", "file", "stardata.csv"),  res);

    clientConnection = query("searchcsv?colID=1");
    assertEquals(200, clientConnection.getResponseCode());
    res = moshiHelper(clientConnection);
    assertEquals("{result=error_bad_request, message=missing query}".toString(),  res.toString());

    clientConnection.disconnect();
  }

  /**
   * Helper method to retrieve mock data from Json format
   * @param clientConnection
   * @return
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
   * @param apiCall Deserialized data from connection
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
