package edu.brown.cs.student.Server.Mock;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import edu.brown.cs.student.Server.ICensusDataSource;
import java.util.concurrent.TimeUnit;

public class MockCaching implements ICensusDataSource {
  private final ICensusDataSource wrappedDataSource;
  private final LoadingCache<String, Object> cache;

  public MockCaching(ICensusDataSource dataSource, int maxSize, int expireAfterMinutes) {
    this.wrappedDataSource = dataSource;
    this.cache = CacheBuilder.newBuilder()
        .maximumSize(maxSize)
        .expireAfterWrite(expireAfterMinutes, TimeUnit.MINUTES)
        .recordStats()
        .build(
            new CacheLoader<>() {
              @Override
              public Object load(String key) throws Exception {
                // Here, split the key into the state and county or any required parameters
                String[] parts = key.split("_");
                return wrappedDataSource.fetchData(parts[0], parts[1]);
              }
            }
        );
  }

  @Override
  public Object fetchData(String state, String county) throws Exception {
    String key = state + "_" + county;
    return cache.getUnchecked(key); // Use Guava's getUnchecked for simplicity
  }
  public String getStats() {
    return "hits=" + cache.stats().hitCount()
        + ", misses=" + cache.stats().missCount()
        + ", cached=" + cache.stats().loadCount()
        + ", evicted=" + cache.stats().evictionCount();
  }
}
