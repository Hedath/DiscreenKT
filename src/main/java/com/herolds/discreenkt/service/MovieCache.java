package com.herolds.discreenkt.service;

import com.herolds.discreenkt.config.ConfigProvider;
import com.herolds.discreenkt.data.Movie;
import org.ehcache.Cache;
import org.ehcache.PersistentCacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;

/**
 * @author bence
 */
public class MovieCache {

    private static MovieCache instance;

    private PersistentCacheManager persistentCacheManager;
    private Cache<Integer, Movie> cache;

    public static MovieCache getInstance() {
        if (instance == null) {
            instance = new MovieCache();
        }
        return instance;
    }

    private MovieCache() {
        initEhCache();
    }

    private void initEhCache() {
        String cacheLocation = ConfigProvider.getInstance().getCacheLocation();

        persistentCacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .with(CacheManagerBuilder.persistence(cacheLocation))
                .withCache("movieCache",
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(Integer.class, Movie.class,
                                ResourcePoolsBuilder.newResourcePoolsBuilder()
                                        .heap(100, EntryUnit.ENTRIES)
                                        .offheap(30, MemoryUnit.MB)
                                        .disk(50, MemoryUnit.MB, true)
                        )
                ).build(true);

        cache = persistentCacheManager.getCache("movieCache", Integer.class, Movie.class);
    }

    public boolean containsMovie(Movie movie) {
        return cache.containsKey(movie.getKTid());
    }

    public void putMovie(Movie movie) {
        cache.put(movie.getKTid(), movie);
    }

    public void close() {
        persistentCacheManager.close();
    }
}
