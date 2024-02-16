package edu.brown.cs.student.Server;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.concurrent.TimeUnit;

public class CachingCensusAPIHandler implements ICensusDataSource {
    private final ICensusDataSource wrappedDataSource;
    private final LoadingCache<String, Object> cache;

    public CachingCensusAPIHandler(ICensusDataSource dataSource, int maxSize, int expireAfterMinutes) {
        this.wrappedDataSource = dataSource;
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireAfterMinutes, TimeUnit.MINUTES)
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
}
