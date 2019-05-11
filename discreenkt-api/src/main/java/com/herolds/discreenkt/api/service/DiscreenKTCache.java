package com.herolds.discreenkt.api.service;

import java.time.Instant;
import java.util.Optional;

import javax.inject.Inject;
import javax.xml.ws.Holder;

import org.ehcache.Cache;
import org.ehcache.PersistentCacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.herolds.discreenkt.api.config.ConfigProvider;
import com.herolds.discreenkt.api.data.Movie;

/**
 * @author bence
 */
public class DiscreenKTCache {

	private final Logger logger = LoggerFactory.getLogger(DiscreenKTCache.class);

	private ConfigProvider configProvider;
	
    private PersistentCacheManager persistentCacheManager;
    
    private Cache<Integer, Movie> cache;
    
    private Cache<Instant, Long> synchronizationCache;

    @Inject
    public DiscreenKTCache(ConfigProvider configProvider) {
    	this.configProvider = configProvider;
    	
        initEhCache();
    }

    private void initEhCache() {
        String cacheLocation = configProvider.getMovieCacheFolder();
        logger.info("Cache location: {}", cacheLocation);

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
