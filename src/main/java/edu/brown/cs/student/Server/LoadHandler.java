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

/**
 * Handles requests to load a CSV file into the server's context.
 * This handler allows clients to specify a file (within a protected directory)
 * and whether the file has headers. It then updates the server's state
 * to reflect the newly loaded file.
 */
public class LoadHandler implements Route {
  private final Server server;

  /**
   * Constructs a LoadHandler with a reference to the main server instance.
   * This reference allows the handler to update the server's state.
   *
   * @param server The server instance to which this handler belongs.
   */
  public LoadHandler(Server server) {
    this.server = server;
  }

  /**
   * Processes incoming requests to load a CSV file. Extracts parameters from
   * the request to determine the file's path and whether it includes headers.
   * Updates the server's state to reflect the loaded file if successful.
   *
   * @param request  The Spark request object, containing query parameters.
   * @param response The Spark response object, used to modify the HTTP response.
   * @return A JSON string indicating the result of the load operation.
   */
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
    if(headers == null) {
      throw new IllegalArgumentException("Did not input valid value for headers (ex. True/False)");
    }

    Moshi moshi = new Moshi.Builder().build();
    Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
    JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);
    Map<String, Object> responseMap = new HashMap<>();
    String absolute = extractFilenames(pDirectory, file);
    if (file != null &&  absolute != null) {
      try {
        Reader fileReader = new FileReader(absolute);
        this.server.loadFile(absolute, boolHeader);
        responseMap.put("result", "success");
        responseMap.put("filepath", file);

      } catch (FileNotFoundException e) {
        this.server.loadFile(null, false);
        responseMap.put("result", "error_datasource");
        responseMap.put("message", "filepath is invalid");
      }
    } else {
      responseMap.put("result", "error_bad_request");
      responseMap.put("message", "file not found within protected directory");
    }
    return adapter.toJson(responseMap);
  }

  /**
   * Recursively searches a directory for a file with the specified name,
   * returning the absolute path if found. Searches subdirectories as well.
   *
   * @param directory The directory to search.
   * @param target    The filename to search for.
   * @return The absolute path of the file if found, otherwise null.
   */
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


