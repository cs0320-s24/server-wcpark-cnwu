package RIData;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.io.IOException;

public class CensusAPIUtilities {
  public static CensusData deserializeData(String jsonData) {
    try {
      Moshi moshi = new Moshi.Builder().build();
      JsonAdapter<CensusData> adapter = moshi.adapter(CensusData.class);
      CensusData data = adapter.fromJson(jsonData);
      return data;
    }
    catch (IOException e) {
      e.printStackTrace();
      return new CensusData();
    }
  }
}
