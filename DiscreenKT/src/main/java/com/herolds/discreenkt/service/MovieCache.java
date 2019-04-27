package com.herolds.discreenkt.service;

import java.time.Instant;
import java.util.Optional;

import javax.xml.ws.Holder;

import org.ehcache.Cache;
import org.ehcache.PersistentCacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;

import com.herolds.discreenkt.config.ConfigProvider;
import com.herolds.discreenkt.data.Movie;

/**
 * @author bence
 */
public class MovieCache {

    private static MovieCache instance;

    private PersistentCacheManager persistentCacheManager;
    private Cache<Integer, Movie> cache;
    
    private Cache<Instant, Long> synchronizationCache;

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
        String cacheLocation = ConfigProvider.getInstance().getMovieCacheFolder();

        persistentCacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .with(CacheManagerBuilder.persistence(cacheLocation))
                .withCache("movieCache",
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(Integer.class, Movie.class,
                                ResourcePoolsBuilder.newResourcePoolsBuilder()
                                        .heap(100, EntryUnit.ENTRIES)
                                        .offheap(30, MemoryUnit.MB)
                                        .disk(50, MemoryUnit.MB, true)
                        )
                )
                .withCache("synchronizationCache",
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(Instant.class, Long.class,
                                ResourcePoolsBuilder.newResourcePoolsBuilder()
                                        .heap(100, EntryUnit.ENTRIES)
                                        .offheap(30, MemoryUnit.MB)
                                        .disk(50, MemoryUnit.MB, true)
                        )
                )
                .build(true);

        cache = persistentCacheManager.getCache("movieCache", Integer.class, Movie.class);
        synchronizationCache = persistentCacheManager.getCache("synchronizationCache", Instant.class, Long.class);
        
        System.out.println("movieCache");
        cache.forEach(entry -> System.out.println("Key: " + entry.getKey() + " Value: " + entry.getValue()));
        System.out.println("synchronizationCache");
        synchronizationCache.forEach(entry -> System.out.println("Key: " + entry.getKey() + " Value: " + entry.getValue()));
    }
    
    public void putSynchronization(Long successCount) {
        synchronizationCache.put(Instant.now(), successCount);
    }
    
    public Optional<Instant> getLastSynchronization() {
    	Holder<Instant> lastSynchronization = new Holder<>();
    	synchronizationCache.forEach(synchronizationEntry -> {
    		if (lastSynchronization.value == null) {
    			lastSynchronization.value = synchronizationEntry.getKey();
    		}
    		
    		if (lastSynchronization.value.isBefore(synchronizationEntry.getKey())) {
    			lastSynchronization.value = synchronizationEntry.getKey();
    		}
    	});
    	
    	return Optional.ofNullable(lastSynchronization.value); 
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
