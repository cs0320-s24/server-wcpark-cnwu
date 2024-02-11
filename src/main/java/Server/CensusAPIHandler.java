package Server;

import RIData.CRDUtilities;
import RIData.CountyRequestData;
import RIData.SRDUtilities;
import RIData.StateRequestData;
import java.io.IOException;
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

public class CensusAPIHandler implements Route {
  private Map<String, String> stateMap;
  public CensusAPIHandler(){
    this.stateMap = new HashMap<>();
    stateMap.put("Alabama", "01");
    stateMap.put("Alaska", "02");
    stateMap.put("Arizona", "04");
    stateMap.put("Arkansas", "05");
    stateMap.put("California", "06");
    stateMap.put("Louisiana", "22");
    stateMap.put("Kentucky", "21");
    stateMap.put("Colorado", "08");
    stateMap.put("Connecticut", "09");
    stateMap.put("Delaware", "10");
    stateMap.put("District of Columbia", "11");
    stateMap.put("Florida", "12");
    stateMap.put("Georgia", "13");
    stateMap.put("Hawaii", "15");
    stateMap.put("Idaho", "16");
    stateMap.put("Illinois", "17");
    stateMap.put("Indiana", "18");
    stateMap.put("Iowa", "19");
    stateMap.put("Kansas", "20");
    stateMap.put("Maine", "23");
    stateMap.put("Maryland", "24");
    stateMap.put("Massachusetts", "25");
    stateMap.put("Michigan", "26");
    stateMap.put("Minnesota", "27");
    stateMap.put("Mississippi", "28");
    stateMap.put("Missouri", "29");
    stateMap.put("Montana", "30");
    stateMap.put("Nebraska", "31");
    stateMap.put("Nevada", "32");
    stateMap.put("New Hampshire", "33");
    stateMap.put("New Jersey", "34");
    stateMap.put("New Mexico", "35");
    stateMap.put("New York", "36");
    stateMap.put("North Carolina", "37");
    stateMap.put("North Dakota", "38");
    stateMap.put("Ohio", "39");
    stateMap.put("Oklahoma", "40");
    stateMap.put("Oregon", "41");
    stateMap.put("Pennsylvania", "42");
    stateMap.put("Rhode Island", "44");
    stateMap.put("South Carolina", "45");
    stateMap.put("South Dakota", "46");
    stateMap.put("Tennessee", "47");
    stateMap.put("Texas", "48");
    stateMap.put("Utah", "49");
    stateMap.put("Vermont", "50");
    stateMap.put("Virginia", "51");
    stateMap.put("Washington", "53");
    stateMap.put("West Virginia", "54");
    stateMap.put("Wisconsin", "55");
    stateMap.put("Wyoming", "56");
    stateMap.put("Puerto Rico", "72");
  }
  @Override
  public Object handle(Request request, Response response) {
    // If you are interested in how parameters are received, try commenting out and
    // printing these lines! Notice that requesting a specific parameter requires that parameter
    // to be fulfilled.
    // If you specify a queryParam, you can access it by appending ?parameterName=name to the
    // endpoint
    // ex. http://localhost:3232/activity?participants=num
    Set<String> params = request.queryParams();
    //     System.out.println(params);
    String year = request.queryParams("year");
    String county = request.queryParams("county");
    String state = request.queryParams("state");

    //     System.out.println(participants);

    // Creates a hashmap to store the results of the request
    Map<String, Object> responseMap = new HashMap<>();

    try {
      // Sends a request to the API and receives JSON back
      String stateJson = this.sendStateRequest(year, state);
      // Deserializes JSON into an Activity
      StateRequestData stateData = SRDUtilities.deserializeData(stateJson);
      String countyJson = this.sendCountyRequest(year, stateData.getCounty(), state);
      CountyRequestData countyData = CRDUtilities.deserializeData(countyJson);
      return responseMap;
    } catch (Exception e) {
      e.printStackTrace();
      // This is a relatively unhelpful exception message. An important part of this sprint will be
      // in learning to debug correctly by creating your own informative error messages where Spark
      // falls short.
      responseMap.put("result", "Exception");
    }
    return responseMap;
  }

  private String sendCountyRequest(String year, String county, String state)
      throws URISyntaxException, IOException, InterruptedException {
    // Build a request to this BoredAPI. Try out this link in your browser, what do you see?
    // TODO 1: Looking at the documentation, how can we add to the URI to query based
    // on participant number?
    HttpRequest ACSApiRequest =
        HttpRequest.newBuilder()
            .uri(new URI("https://api.census.gov/data/" + year +
                "/acs/acs1/subject/variables?get=NAME,S2802_C03_022E&for=county:" + county +
                "&in=state:" + state))
            .GET()
            .build();

    // Send that API request then store the response in this variable. Note the generic type.
    HttpResponse<String> sentACIApiResponse =
        HttpClient.newBuilder()
            .build()
            .send(ACSApiRequest, HttpResponse.BodyHandlers.ofString());

    // What's the difference between these two lines? Why do we return the body? What is useful from
    // the raw response (hint: how can we use the status of response)?
    System.out.println(sentACIApiResponse);
    System.out.println(sentACIApiResponse.body());

    return sentACIApiResponse.body();
  }

  private String sendStateRequest(String year, String state)
      throws URISyntaxException, IOException, InterruptedException {
    // Build a request to this BoredAPI. Try out this link in your browser, what do you see?
    // TODO 1: Looking at the documentation, how can we add to the URI to query based
    // on participant number?
    HttpRequest ACSApiRequest =
        HttpRequest.newBuilder()
            .uri(new URI("https://api.census.gov/data/" +year+"/dec/sf1?get=NAME&for=county:*&in=state:" + this.stateMap.get(state)))
            .GET()
            .build();

    // Send that API request then store the response in this variable. Note the generic type.
    HttpResponse<String> sentACIApiResponse =
        HttpClient.newBuilder()
            .build()
            .send(ACSApiRequest, HttpResponse.BodyHandlers.ofString());

    // What's the difference between these two lines? Why do we return the body? What is useful from
    // the raw response (hint: how can we use the status of response)?
    System.out.println(sentACIApiResponse);
    System.out.println(sentACIApiResponse.body());

    return sentACIApiResponse.body();
  }
}
