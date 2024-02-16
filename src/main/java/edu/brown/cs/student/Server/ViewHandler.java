package edu.brown.cs.student.Server;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.CreatorFromRowTypes.CSVString;
import edu.brown.cs.student.CreatorFromRowTypes.CreatorFromRow;
import edu.brown.cs.student.Parser.CSVParser;
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
 * Handles requests to view the contents of the currently loaded CSV file.
 * This handler allows clients to retrieve the data from the loaded CSV,
 * formatted as JSON, including information on whether the file contains headers.
 */
public class ViewHandler implements Route {
  private final Server server;

  /**
   * Constructs a ViewHandler with a reference to the server instance.
   * This reference is used to access the currently loaded CSV file and its configuration.
   *
   * @param server The server instance to which this handler belongs.
   */
  public ViewHandler(Server server) {
    this.server = server;
  }

  /**
   * Processes incoming requests to view the data of the currently loaded CSV file.
   * Returns the data as JSON, including a success or error message based on the operation result.
   *
   * @param request  The Spark request object, containing parameters for the request.
   * @param response The Spark response object, used to modify the HTTP response.
   * @return A JSON string representing the CSV data or an error message if the file is not found or not loaded.
   * @throws Exception if there's an issue processing the request or reading the file.
   */
  @Override
  public Object handle(Request request, Response response) throws Exception {
    Moshi moshi = new Moshi.Builder().build();
    Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
    JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);
    Map<String, Object> responseMap = new HashMap<>();
    boolean headers = this.server.getHeaders();
    if (this.server.getFile() != null) {
      try {
        Reader reader = new FileReader(this.server.getFile());
        CSVParser<CreatorFromRow<CSVString>> parser = new CSVParser<CreatorFromRow<CSVString>>(headers, new CSVString(), reader);
        List<CreatorFromRow<CSVString>> parsed = parser.getParsed();

        responseMap.put("result", "success");
        responseMap.put("data", parsed);
      } catch (FileNotFoundException e) {
        responseMap.put("result", "error_datasource");
        responseMap.put("message", "filepath not found");
      }
    } else {
      responseMap.put("result", "error_datasource");
      responseMap.put("message", "file not loaded");
    }
    return adapter.toJson(responseMap);
  }
}
