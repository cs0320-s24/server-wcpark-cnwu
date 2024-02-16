package edu.brown.cs.student.Server;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.HashMap;
import spark.Request;
import spark.Response;
import spark.Route;

import java.lang.reflect.Type;
import java.util.Map;
public class LoadHandler implements Route {
  private final Server server;
  public LoadHandler(Server server) {
    this.server = server;
  }
  @Override
  public Object handle(Request request, Response response) {
    String fileName = request.queryParams("filepath");
    String headers = request.queryParams("headers");
    boolean boolHeader = true;
    boolean checked = false;
    if (headers.equals("true")) {
      checked = true;
    }
    else if (headers.equals("false")) {
      boolHeader = false;
      checked = true;
    }
    if(!checked) {
      throw new IllegalArgumentException("Did not input valid value for headers (ex. True/False)");
    }

    Moshi moshi = new Moshi.Builder().build();
    Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
    JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);
    Map<String, Object> responseMap = new HashMap<>();
    //Credit to reng1 for protected directory idea
    if (fileName != null) {
      try {
        Reader file = new FileReader(fileName);
        this.server.loadFile(fileName, boolHeader);
        responseMap.put("result", "success");
        responseMap.put("filepath", fileName);

      } catch (FileNotFoundException e) {
        this.server.loadFile(null, false);
        responseMap.put("result", "error_datasource");
        responseMap.put("message", "filepath is invalid");
      }
    } else {
      responseMap.put("result", "error_bad_request");
      responseMap.put("message", "filepath not given or file not from ./data folder");
    }
    return adapter.toJson(responseMap);
  }
}


