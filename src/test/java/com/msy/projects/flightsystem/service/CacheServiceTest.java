package com.msy.projects.flightsystem.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CacheServiceTest {

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache routeCache;

    @InjectMocks
    private CacheService cacheService;

    @Test
    void clearRouteCache_ShouldClearCache() {
        // Arrange
        when(cacheManager.getCache("routeCache")).thenReturn(routeCache);

        // Act
        cacheService.clearRouteCache();

        // Assert
        verify(cacheManager).getCache("routeCache");
        verify(routeCache).clear();
    }

    @Test
    void clearSpecificRouteCache_ShouldClearEntireCache() {
        // Arrange
        when(cacheManager.getCache("routeCache")).thenReturn(routeCache);

        // Act
        cacheService.clearSpecificRouteCache("IST", "LHR");

        // Assert
        verify(cacheManager).getCache("routeCache");
        verify(routeCache).clear();
    }
}
