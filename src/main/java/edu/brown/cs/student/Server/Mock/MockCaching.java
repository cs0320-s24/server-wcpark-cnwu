package edu.brown.cs.student.Server.Mock;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import edu.brown.cs.student.Server.ICensusDataSource;
import java.util.concurrent.TimeUnit;

/**
 * CachingCensusAPIHandler is a decorator for ICensusDataSource that adds caching functionality
 * to census data fetching operations. It utilizes Guava's LoadingCache to cache the responses
 * for frequently requested state and county combinations to improve performance by reducing
 * redundant API calls. Cached entries expire after a configurable amount of time and have a
 * maximum size limit to manage memory usage efficiently.
 */
public class MockCaching implements ICensusDataSource {
  private final ICensusDataSource wrappedDataSource;
  private final LoadingCache<String, Object> cache;

  /**
   * Constructs a CachingCensusAPIHandler wrapping around a given ICensusDataSource.
   *
   * @param dataSource The data source to be wrapped and cached.
   * @param maxSize The maximum number of entries the cache can hold.
   * @param expireAfterMinutes The duration after which a cache entry should expire, in minutes.
   */

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

  /**
   * Fetches data for a given state and county, utilizing the cache to serve repeated requests efficiently.
   *
   * @param state The state for which data is requested.
   * @param county The county within the state for which data is requested.
   * @return The fetched data, either from the cache or directly from the wrapped data source.
   * @throws Exception if there is an issue fetching the data.
   */
  @Override
  public Object fetchData(String state, String county) throws Exception {
    String key = state + "_" + county;
    return cache.getUnchecked(key); // Use Guava's getUnchecked for simplicity
  }

  /**
   * Obtains the desired stats from the cache
   * @return cache stats in a String
   */
  public String getStats() {
    return "hits=" + cache.stats().hitCount()
        + ", misses=" + cache.stats().missCount()
        + ", cached=" + cache.stats().loadCount()
        + ", evicted=" + cache.stats().evictionCount();
  }
}
