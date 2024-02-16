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
public class ViewHandler implements Route {
  private final Server server;
  public ViewHandler(Server server) {
    this.server = server;
  }
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
