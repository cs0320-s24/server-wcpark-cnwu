package Server;

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
import java.util.Set;
import spark.Request;
import spark.Response;
import spark.Route;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.time.*;

/**
 * This class is used to illustrate how to build and send a GET request then prints the response.
 *
 * Check out the rest of the gearup for an exercise on how to parse the response and deserialize
 * it into an object.
 */
public class CensusAPIHandler implements Route {
  private String county;
  private String state;
  private String stateCode;
  private String broadbandPercent;
  private Map<String, Object> responseMap;
  @Override
  public Object handle(Request request, Response response) throws Exception {
    this.county = request.queryParams("county");
    this.state = request.queryParams("state");
    this.stateCode = null;
    this.broadbandPercent = null;
    this.responseMap = new HashMap<>();

    Moshi moshi = new Moshi.Builder().build();
    Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
    JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);

    if (this.county != null && this.state != null) {
      this.getStateThenCounty();
    }
    return adapter.toJson(this.responseMap);
  }
  private void getStateThenCounty() throws URISyntaxException, IOException, InterruptedException {
    HttpRequest buildApiRequest = HttpRequest.newBuilder()
        .uri(new URI("https://api.census.gov/data/2010/dec/sf1?get=NAME&for=state:*"))
        .GET()
        .build();

    // Send that API request then store the response in this variable. Note the generic type.
    HttpResponse<String> sentApiResponse = HttpClient.newBuilder()
        .build().
        send(buildApiRequest, HttpResponse.BodyHandlers.ofString());

    Moshi moshi = new Moshi.Builder().build();
    Type ls = Types.newParameterizedType(List.class, String.class);
    Type lls = Types.newParameterizedType(List.class, ls);
    JsonAdapter<List<List<String>>> adapter = moshi.adapter(lls);

    List<List<String>> res = adapter.fromJson(sentApiResponse.body());

    for (List<String> cur : res) {
      if (cur.get(1).equals(this.state)) {
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

      // Send that API request then store the response in this variable. Note the generic type.
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
    }
  }
}

