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

public class CensusAPIHandler implements Route, ICensusDataSource {
  private String county;
  private String state;
  private String stateCode;
  private String broadbandPercent;
  private Map<String, Object> responseMap;
  private final CachingCensusAPIHandler cachingHandler;
  public CensusAPIHandler() {
    // Initialize the caching handler with appropriate parameters
    this.cachingHandler = new CachingCensusAPIHandler(this, 50, 1);
  }
  @Override
  public Object handle(Request request, Response response) throws Exception {
    String state = request.queryParams("state");
    String county = request.queryParams("county");
    return cachingHandler.fetchData(state, county);
  }
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

    if (this.county != null && this.state != null) {
      this.getStateThenCounty();
    }
    else {
      this.responseMap.put("result", "error_bad_request");
      this.responseMap.put("message", "parameters not found");
    }
    return adapter.toJson(this.responseMap);
  }
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

