package edu.brown.cs.student.Server;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import java.io.File;
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
    String file = request.queryParams("file");
    String headers = request.queryParams("headers");
    String directory = request.queryParams("directory");
    File pDirectory = new File(directory);
    boolean boolHeader = false;
    boolean check = Boolean.parseBoolean(headers);
    if (check) {
      boolHeader = true;
    }
    Moshi moshi = new Moshi.Builder().build();
    Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
    JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);
    Map<String, Object> responseMap = new HashMap<>();
    String absolute = extractFilenames(pDirectory, file);
    if (file != null) {
      if (absolute != null) {
        try {
          Reader fileReader = new FileReader(absolute);
          this.server.loadFile(absolute, boolHeader);
          responseMap.put("result", "success");
          responseMap.put("file", file);

        } catch (FileNotFoundException e) {
          responseMap.put("result", "error_datasource");
          responseMap.put("message", "filepath is invalid");
        }
      } else {
        responseMap.put("result", "error_bad_request");
        responseMap.put("message", "file not found within protected directory");
      }
    }
    else {
      responseMap.put("result", "error_bad_request");
      responseMap.put("message", "file not given");
    }
    return adapter.toJson(responseMap);
  }
  private String extractFilenames(File directory, String target) {
    File[] files = directory.listFiles();
    if (files != null) {
      for (File file : files) {
        if (file.isDirectory()) {
          String result = extractFilenames(file, target);
          if (result != null) {
            return result;
          }
        } else if (file.getName().equalsIgnoreCase(target)) {
          return file.getAbsolutePath();
        }
      }
    }
    return null;
  }
}


