package edu.brown.cs.student.Server;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.CreatorFromRowTypes.CSVString;
import edu.brown.cs.student.CreatorFromRowTypes.CreatorFromRow;
import edu.brown.cs.student.Parser.CSVParser;
import edu.brown.cs.student.UserFacing.CSVSearcher;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import spark.Request;
import spark.Response;
import spark.Route;
import java.io.FileReader;
import java.io.Reader;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Handles search requests on the currently loaded CSV file.
 * This handler allows clients to specify a search query and an optional column identifier
 * to filter search results. The response includes the row numbers where the query matches.
 */

public class SearchHandler implements Route {
  private final Server server;

  /**
   * Constructs a SearchHandler with a reference to the server instance.
   * This reference is used to access server-wide properties like the currently loaded CSV file.
   *
   * @param server The server instance to which this handler belongs.
   */
  public SearchHandler(Server server) {
    this.server = server;
  }

  /**
   * Processes incoming search requests, executing the search on the currently loaded CSV file.
   * The search can be performed across all columns or within a specified column based on the query parameters.
   *
   * @param request  The Spark request object, containing query parameters for the search.
   * @param response The Spark response object, used to modify the HTTP response.
   * @return A JSON string representing the search results or an error message.
   * @throws Exception if there's an issue processing the request.
   */
  @Override
  public Object handle(Request request, Response response) throws Exception {

    String query = request.queryParams("query");
    String colID = request.queryParams("colID");

    Moshi moshi = new Moshi.Builder().build();
    Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
    JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);
    Map<String, Object> responseMap = new HashMap<>();

    boolean headers = this.server.getHeaders();

    if (query != null) {
      if (this.server.getFile() != null) {
        try {
          Reader reader = new FileReader(this.server.getFile());
          CSVParser<CreatorFromRow<CSVString>> parser = new CSVParser<CreatorFromRow<CSVString>>(headers, new CSVString(), reader);
          CSVSearcher<String, String, CSVString> searcher =
              new CSVSearcher<String, String, CSVString>(parser);
          List<Integer> rows = searcher.search(query, colID, parser.getHeaders());
          if(rows.isEmpty()) {
            responseMap.put("result", "error_bad_request");
            responseMap.put("message", "no data in specified column/index");
          }
          else {
            responseMap.put("result", "success");
            responseMap.put("query", query);
            responseMap.put("rows", rows);
            if(colID != null) {
              responseMap.put("columnId", colID);
            }
          }

        } catch (FileNotFoundException e) {
          responseMap.put("result", "error_datasource");
          responseMap.put("message", "file not found");
        }
      } else {
        responseMap.put("result", "error_datasource");
        responseMap.put("message", "filepath is not loaded");
      }

    } else {
      responseMap.put("result", "error_bad_request");
      responseMap.put("message", "missing query");
    }
    return adapter.toJson(responseMap);
  }
}
