package edu.brown.cs.student.Server;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Types;
import com.squareup.moshi.Moshi;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;
import java.util.List;
import java.time.*;

//credit to reng1 for moshi adapter and json deserialization. as well as for response map cases

/**
 * Handles requests to fetch broadband access data for specific states and counties
 * from the U.S. Census Bureau's API and implements caching to improve performance.
 * Implements both Spark's Route for HTTP request handling and ICensusDataSource for direct data fetching.
 */
public class CensusAPIHandler implements Route, ICensusDataSource {
  private String county;
  private String state;
  private String stateCode;
  private String broadbandPercent;
  private Map<String, Object> responseMap;
  private final CachingCensusAPIHandler cachingHandler;

  /**
   * Initializes a new CensusAPIHandler instance with a caching layer.
   */
  public CensusAPIHandler() {
    // Initialize the caching handler with appropriate parameters
    this.cachingHandler = new CachingCensusAPIHandler(this, 50, 1);
  }

  /**
   * Handles incoming HTTP requests by extracting state and county parameters
   * and fetching the broadband access data, either from cache or by making an API call.
   *
   * @param request The Spark request object containing query parameters.
   * @param response The Spark response object for setting response properties.
   * @return A JSON string representing the broadband access data or an error message.
   * @throws Exception if there's an issue processing the request.
   */
  @Override
  public Object handle(Request request, Response response) throws Exception {
    String state = request.queryParams("state");
    String county = request.queryParams("county");
    return cachingHandler.fetchData(state, county);
  }
  /**
   * Directly fetches broadband access data for a given state and county. This method is called
   * by the caching handler upon cache misses and is also usable for direct data fetching
   * without going through Spark's request-response cycle.
   *
   * @param state The state for which broadband data is requested.
   * @param county The county within the state for which broadband data is requested.
   * @return A JSON string representing the broadband access data or an error message.
   * @throws Exception if there's an issue fetching the data from the API.
   */
  @Override
  public Object fetchData(String state, String county) throws Exception {
    this.state = state;
    this.county = county;
    this.stateCode = null;
    this.broadbandPercent = null;
    this.responseMap = new HashMap<>();

    Moshi moshi = new Moshi.Builder().build();
    Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
    JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);

    if (this.county != null && this.state != null && !this.county.equals("null") && !this.state.equals("null")) {
      this.getStateThenCounty();
    }
    else {
      this.responseMap.put("result", "error_bad_request");
      this.responseMap.put("message", "parameters not found");
    }
    return adapter.toJson(this.responseMap);
  }

  /**
   * Fetches broadband access data from the U.S. Census API based on state and county codes.
   * This method constructs the request URL, sends the request, and processes the API response.
   *
   * @return The API response as a JSON string.
   * @throws URISyntaxException if the constructed URI is invalid.
   * @throws IOException if an I/O error occurs when sending the request.
   * @throws InterruptedException if the request is interrupted.
   */
  private void getStateThenCounty() throws URISyntaxException, IOException, InterruptedException {
    HttpRequest buildApiRequest = HttpRequest.newBuilder()
        .uri(new URI("https://api.census.gov/data/2010/dec/sf1?get=NAME&for=state:*"))
        .GET()
        .build();
    HttpResponse<String> sentApiResponse = HttpClient.newBuilder()
        .build().
        send(buildApiRequest, HttpResponse.BodyHandlers.ofString());

    Moshi moshi = new Moshi.Builder().build();
    Type ls = Types.newParameterizedType(List.class, String.class);
    Type lls = Types.newParameterizedType(List.class, ls);
    JsonAdapter<List<List<String>>> adapter = moshi.adapter(lls);

    List<List<String>> res = adapter.fromJson(sentApiResponse.body());

    for (List<String> cur : res) {
      if (cur.get(0).equals(this.state)) {
        this.stateCode = cur.get(1);
      }
    }
    if (this.stateCode != null) {
      buildApiRequest = HttpRequest.newBuilder()
          .uri(new URI(
              "https://api.census.gov/data/2019/acs/acs1/subject/variables?get=NAME,S2802_C03_022E&for=county:*&in=state:"
                  + this.stateCode))
          .GET()
          .build();
      sentApiResponse = HttpClient.newBuilder()
          .build().
          send(buildApiRequest, HttpResponse.BodyHandlers.ofString());

      LocalDateTime time = LocalDateTime.now();
      res = adapter.fromJson(sentApiResponse.body());

      for (List<String> cur : res) {
        if (cur.get(0).equals(this.county + " County, " + this.state)) {
          this.broadbandPercent = cur.get(1);
        }
      }
      if (this.broadbandPercent != null) {
        this.responseMap.put("result", "success");
        this.responseMap.put("time", time.toString());
        this.responseMap.put("Percentage of broadband access in " + this.county, this.broadbandPercent);

      } else {
        this.responseMap.put("result", "error_bad_request");
        this.responseMap.put("message", "county not found");
      }
    }
    else {
      this.responseMap.put("result", "error_bad_request");
      this.responseMap.put("message", "state not found");
    }
    this.responseMap.put("state", this.state);
    this.responseMap.put("county", this.county);
  }
}

