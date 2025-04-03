package com.msy.projects.flightsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
public class CacheService {

    private final CacheManager cacheManager;

    @Autowired
    public CacheService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * Clear all route caches when transportation data changes
     */
    public void clearRouteCache() {
        cacheManager.getCache("routeCache").clear();
    }
    
    /**
     * Clear specific route cache entries
     * 
     * @param originCode Origin location code
     * @param destinationCode Destination location code
     */
    public void clearSpecificRouteCache(String originCode, String destinationCode) {
        // Since we can't easily clear by pattern in Spring Cache abstraction,
        // we'll clear the entire cache in this implementation
        clearRouteCache();
    }
}
