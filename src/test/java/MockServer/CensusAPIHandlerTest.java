package MockServer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.Server.CensusAPIHandler;
import edu.brown.cs.student.Server.Mock.MockHandler;
import edu.brown.cs.student.Server.MockCaching;
import edu.brown.cs.student.Server.Server;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
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
 * Testing class for CensusAPIHandler
 */
public class CensusAPIHandlerTest {

  @BeforeAll
  public static void setup_before_everything() {
    Spark.port(0);
    Logger.getLogger("").setLevel(Level.WARNING);
  }
  final Server server = new Server(new CensusAPIHandler());

  /**
   * Sets up Spark
   */
  @BeforeEach
  public void setup() {
    Spark.get("/broadband", new CensusAPIHandler());
    Spark.get("/mockbroadband", new MockHandler());
    Spark.init();
    Spark.awaitInitialization();
  }

  /**
   * Shuts down Spark
   */
  @AfterEach
  public void teardown() {
    Spark.unmap("/broadband");
    Spark.unmap("/mockbroadband");
    Spark.awaitStop();
  }

  /**
   * Tests for successful retrieval of percentages of broadband
   * @throws IOException
   */
  @Test
  public void testSuccess() throws IOException {
    HttpURLConnection clientConnection = query("broadband?county=Providence&state=Rhode+Island");
    assertEquals(200, clientConnection.getResponseCode());
    Map<String, Object> res = moshiHelper(clientConnection);
    Object time = res.get("time");
    assertEquals("{result=success, county=Providence, Percentage of broadband access in Providence=77.4, time="+ time + ", state=Rhode Island}", res.toString());

    clientConnection = query("broadband?state=Washington&county=King");
    assertEquals(200, clientConnection.getResponseCode());
    res = moshiHelper(clientConnection);
    time = res.get("time");
    assertEquals("{result=success, county=King, time=" + time + ", state=Washington, Percentage of broadband access in King=90.0}", res.toString());

    clientConnection.disconnect();
  }

  /**
   * Tests for missing parameter
   * @throws IOException
   */
  @Test
  public void testMissing() throws IOException {
    HttpURLConnection clientConnection = query("broadband?county=Providence");
    assertEquals(200, clientConnection.getResponseCode());
    Map<String, Object> res = moshiHelper(clientConnection);
    assertEquals("{result=error_bad_request, message=parameters not found}", res.toString());

    clientConnection = query("broadband?state=Washington");
    assertEquals(200, clientConnection.getResponseCode());
    res = moshiHelper(clientConnection);
    assertEquals("{result=error_bad_request, message=parameters not found}", res.toString());

    clientConnection = query("broadband?");
    assertEquals(200, clientConnection.getResponseCode());
    res = moshiHelper(clientConnection);
    assertEquals("{result=error_bad_request, message=parameters not found}", res.toString());
    clientConnection.disconnect();
  }

  /**
   * Tests for parameters when not found
   * @throws IOException
   */
  @Test
  public void testNotFound() throws IOException {
    HttpURLConnection clientConnection = query("broadband?county=King&state=W");
    assertEquals(200, clientConnection.getResponseCode());
    Map<String, Object> res = moshiHelper(clientConnection);
    assertEquals("{result=error_bad_request, county=King, state=W, message=state not found}", res.toString());

    clientConnection = query("broadband?county=Kin&state=Washington");
    assertEquals(200, clientConnection.getResponseCode());
    res = moshiHelper(clientConnection);
    assertEquals("{result=error_bad_request, county=Kin, state=Washington, message=county not found}", res.toString());

    clientConnection = query("broadband?county=Kin&state=Washingto");
    assertEquals(200, clientConnection.getResponseCode());
    res = moshiHelper(clientConnection);
    assertEquals("{result=error_bad_request, county=Kin, state=Washingto, message=state not found}", res.toString());
    clientConnection.disconnect();
  }

  /**
   * Tests bad query
   * @throws IOException
   */
  @Test
  public void testBadQuery() throws IOException {
    HttpURLConnection clientConnection = query("hahahahaha");
    assertEquals(404, clientConnection.getResponseCode());
    clientConnection.disconnect();
  }

  /**
   * Tests caching features
   * @throws Exception
   */
  @Test
  public void testMockCaching() throws Exception {
    MockCaching caching = new MockCaching(new MockHandler(), 2, 1);
    caching.fetchData("Rhode_Island", "Providence");
    String stats = caching.getStats();
    assertEquals("hits=0, misses=1, cached=1, evicted=0", stats);
    caching.fetchData("Rhode_Island", "Providence");
    stats = caching.getStats();
    assertEquals("hits=1, misses=1, cached=1, evicted=0", stats);
    caching.fetchData("Washington", "King");
    stats = caching.getStats();
    assertEquals("hits=1, misses=2, cached=2, evicted=0", stats);
    caching.fetchData("Washington", "Providence");
    stats = caching.getStats();
    assertEquals("hits=1, misses=3, cached=3, evicted=1", stats);
  }

  /**
   * Tests for successful retrieval of percentages of broadband
   * @throws IOException
   */
  @Test
  public void testMockSuccess() throws IOException {
    HttpURLConnection clientConnection = query("mockbroadband?county=Providence&state=Rhode+Island");
    assertEquals(200, clientConnection.getResponseCode());
    Map<String, Object> res = moshiHelper(clientConnection);
    Object time = res.get("time");
    assertEquals("{result=success, county=Providence, Percentage of broadband access in Providence=85.4, time="+ time + ", state=Rhode Island}", res.toString());

    clientConnection.disconnect();
  }

  /**
   * Tests for missing parameter
   * @throws IOException
   */
  @Test
  public void testMockMissing() throws IOException {
    HttpURLConnection clientConnection = query("mockbroadband?county=Providence");
    assertEquals(200, clientConnection.getResponseCode());
    Map<String, Object> res = moshiHelper(clientConnection);
    assertEquals("{result=error_bad_request, message=parameters not found}", res.toString());

    clientConnection = query("broadband?");
    assertEquals(200, clientConnection.getResponseCode());
    res = moshiHelper(clientConnection);
    assertEquals("{result=error_bad_request, message=parameters not found}", res.toString());
    clientConnection.disconnect();
  }

  /**
   * Tests for parameters when not found
   * @throws IOException
   */
  @Test
  public void testMockNotFound() throws IOException {
    HttpURLConnection clientConnection = query("mockbroadband?county=King&state=W");
    assertEquals(200, clientConnection.getResponseCode());
    Map<String, Object> res = moshiHelper(clientConnection);
    assertEquals("{result=error_bad_request, county=King, state=W, message=state not found}", res.toString());

    clientConnection = query("broadband?county=Kin&state=Washington");
    assertEquals(200, clientConnection.getResponseCode());
    res = moshiHelper(clientConnection);
    assertEquals("{result=error_bad_request, county=Kin, state=Washington, message=county not found}", res.toString());

    clientConnection = query("broadband?county=Kin&state=Washingto");
    assertEquals(200, clientConnection.getResponseCode());
    res = moshiHelper(clientConnection);
    assertEquals("{result=error_bad_request, county=Kin, state=Washingto, message=state not found}", res.toString());
    clientConnection.disconnect();

    clientConnection = query("broadband?county=Provi&state=Rhode+Island");
    assertEquals(200, clientConnection.getResponseCode());
    res = moshiHelper(clientConnection);
    assertEquals("{result=error_bad_request, county=Provi, state=Rhode Island, message=county not found}", res.toString());
  }

  /**
   * Tests bad query
   * @throws IOException
   */
  @Test
  public void testMockBadQuery() throws IOException {
    HttpURLConnection clientConnection = query("hahahahaha");
    assertEquals(404, clientConnection.getResponseCode());
    clientConnection.disconnect();
  }
  /**
   * Helper method to make a specified API call
   * @param apiCall
   * @return connection to the URL
   * @throws IOException
   */
  public HttpURLConnection query(String apiCall) throws IOException {
    URL requestURL = new URL("http://localhost:"+Spark.port()+"/"+apiCall);
    HttpURLConnection clientConnection = (HttpURLConnection) requestURL.openConnection();
    clientConnection.connect();
    return clientConnection;
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
}