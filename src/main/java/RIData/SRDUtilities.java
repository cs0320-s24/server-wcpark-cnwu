package RIData;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.io.IOException;

public class SRDUtilities {
  public static StateRequestData deserializeData(String jsonData) {
    try {
      // Initializes Moshi
      Moshi moshi = new Moshi.Builder().build();

      JsonAdapter<StateRequestData> adapter = moshi.adapter(StateRequestData.class);

      StateRequestData data = adapter.fromJson(jsonData);

      return data;
    }
    catch (IOException e) {
      e.printStackTrace();
      return new StateRequestData();
    }
  }
}
