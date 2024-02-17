package edu.brown.cs.student.Server.Mock;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Types;
import com.squareup.moshi.Moshi;
import edu.brown.cs.student.Server.ICensusDataSource;
import edu.brown.cs.student.Server.MockCaching;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;
import java.util.List;
import java.time.*;

//credit to reng1 for moshi adapter and json deserialization. as well as for response map cases.
//Also took the states string and counties string from reng1
public class MockHandler implements Route, ICensusDataSource {
  private String county;
  private String state;
  private String stateCode;
  private String broadbandPercent;
  private Map<String, Object> responseMap;
  private final MockCaching cachingHandler;
  public MockHandler() {
    // Initialize the caching handler with appropriate parameters
    this.cachingHandler = new MockCaching(this, 50, 1);
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
    if (this.county != null && this.state != null && !this.county.equals("null") && !this.state.equals("null")) {
      this.getStateThenCounty();
    }
    else {
      this.responseMap.put("result", "error_bad_request");
      this.responseMap.put("message", "parameters not found");
    }
    return adapter.toJson(this.responseMap);
  }
  private void getStateThenCounty() throws URISyntaxException, IOException, InterruptedException {
    String states = "[[\"NAME\",\"state\"],\n"
        + "  [\"Alabama\",\"01\"],\n"
        + "  [\"Alaska\",\"02\"],\n"
        + "  [\"Arizona\",\"04\"],\n"
        + "  [\"Arkansas\",\"05\"],\n"
        + "  [\"California\",\"06\"],\n"
        + "  [\"Louisiana\",\"22\"],\n"
        + "  [\"Kentucky\",\"21\"],\n"
        + "  [\"Colorado\",\"08\"],\n"
        + "  [\"Connecticut\",\"09\"],\n"
        + "  [\"Delaware\",\"10\"],\n"
        + "  [\"District of Columbia\",\"11\"],\n"
        + "  [\"Florida\",\"12\"],\n"
        + "  [\"Georgia\",\"13\"],\n"
        + "  [\"Hawaii\",\"15\"],\n"
        + "  [\"Idaho\",\"16\"],\n"
        + "  [\"Illinois\",\"17\"],\n"
        + "  [\"Indiana\",\"18\"],\n"
        + "  [\"Iowa\",\"19\"],\n"
        + "  [\"Kansas\",\"20\"],\n"
        + "  [\"Maine\",\"23\"],\n"
        + "  [\"Maryland\",\"24\"],\n"
        + "  [\"Massachusetts\",\"25\"],\n"
        + "  [\"Michigan\",\"26\"],\n"
        + "  [\"Minnesota\",\"27\"],\n"
        + "  [\"Mississippi\",\"28\"],\n"
        + "  [\"Missouri\",\"29\"],\n"
        + "  [\"Montana\",\"30\"],\n"
        + "  [\"Nebraska\",\"31\"],\n"
        + "  [\"Nevada\",\"32\"],\n"
        + "  [\"New Hampshire\",\"33\"],\n"
        + "  [\"New Jersey\",\"34\"],\n"
        + "  [\"New Mexico\",\"35\"],\n"
        + "  [\"New York\",\"36\"],\n"
        + "  [\"North Carolina\",\"37\"],\n"
        + "  [\"North Dakota\",\"38\"],\n"
        + "  [\"Ohio\",\"39\"],\n"
        + "  [\"Oklahoma\",\"40\"],\n"
        + "  [\"Oregon\",\"41\"],\n"
        + "  [\"Pennsylvania\",\"42\"],\n"
        + "  [\"Rhode Island\",\"44\"],\n"
        + "  [\"South Carolina\",\"45\"],\n"
        + "  [\"South Dakota\",\"46\"],\n"
        + "  [\"Tennessee\",\"47\"],\n"
        + "  [\"Texas\",\"48\"],\n"
        + "  [\"Utah\",\"49\"],\n"
        + "  [\"Vermont\",\"50\"],\n"
        + "  [\"Virginia\",\"51\"],\n"
        + "  [\"Washington\",\"53\"],\n"
        + "  [\"West Virginia\",\"54\"],\n"
        + "  [\"Wisconsin\",\"55\"],\n"
        + "  [\"Wyoming\",\"56\"],\n"
        + "  [\"Puerto Rico\",\"72\"]]";

    Moshi moshi = new Moshi.Builder().build();
    Type ls = Types.newParameterizedType(List.class, String.class);
    Type lls = Types.newParameterizedType(List.class, ls);
    JsonAdapter<List<List<String>>> adapter = moshi.adapter(lls);

    List<List<String>> res = adapter.fromJson(states);

    for (List<String> cur : res) {
      if (cur.get(0).equals(this.state)) {
        this.stateCode = cur.get(1);
      }
    }
    if (this.stateCode != null) {
      String counties = "[[\"NAME\",\"S2802_C03_022E\",\"state\",\"county\"],\n"
          + "  [\"Kent County, Rhode Island\",\"84.1\",\"44\",\"003\"],\n"
          + "  [\"Providence County, Rhode Island\",\"85.4\",\"44\",\"007\"],\n"
          + "  [\"Newport County, Rhode Island\",\"90.1\",\"44\",\"005\"],\n"
          + "  [\"Washington County, Rhode Island\",\"92.8\",\"44\",\"009\"]]";

      LocalDateTime time = LocalDateTime.now();
      res = adapter.fromJson(counties);

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
    this.responseMap.put("cache hits", cachingHandler.getStats());
  }
}

