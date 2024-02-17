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

public class SearchHandler implements Route {
  private final Server server;
  public SearchHandler(Server server) {
    this.server = server;
  }
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
            responseMap.put("headers", headers);
            if(colID != null) {
              responseMap.put("colID", colID);
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
