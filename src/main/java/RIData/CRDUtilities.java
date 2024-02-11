package RIData;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.io.IOException;

public class CRDUtilities {
    public static CountyRequestData deserializeData(String jsonData) {
      try {
        // Initializes Moshi
        Moshi moshi = new Moshi.Builder().build();

        JsonAdapter<CountyRequestData> adapter = moshi.adapter(CountyRequestData.class);

        CountyRequestData data = adapter.fromJson(jsonData);

        return data;
      }
      catch (IOException e) {
        e.printStackTrace();
        return new CountyRequestData();
      }
    }
  }
