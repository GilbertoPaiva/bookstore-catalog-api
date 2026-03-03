package com.gilbertopaiva.bookstore_catalog_api.book;

import com.gilbertopaiva.bookstore_catalog_api.config.CacheConfig;
import com.github.benmanes.caffeine.cache.Cache;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@Tag(name = "Books", description = "CRUD de livros com paginação, filtros e cache")
@RestController
@RequestMapping("/books/cache")
@RequiredArgsConstructor
public class CacheStatsController {

    private final CacheManager cacheManager;

    @Operation(summary = "Estatísticas do cache", description = "Retorna hits, misses, hit rate e evictions dos caches `books` e `book`.")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> stats() {
        Map<String, Object> result = new LinkedHashMap<>();

        for (String name : new String[]{CacheConfig.CACHE_BOOKS, CacheConfig.CACHE_BOOK}) {
            org.springframework.cache.Cache springCache = cacheManager.getCache(name);
            if (springCache instanceof CaffeineCache caffeineCache) {
                Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();
                com.github.benmanes.caffeine.cache.stats.CacheStats stats = nativeCache.stats();

                Map<String, Object> cacheInfo = new LinkedHashMap<>();
                cacheInfo.put("size",        nativeCache.estimatedSize());
                cacheInfo.put("hitCount",    stats.hitCount());
                cacheInfo.put("missCount",   stats.missCount());
                cacheInfo.put("hitRate",     String.format("%.2f%%", stats.hitRate() * 100));
                cacheInfo.put("evictionCount", stats.evictionCount());
                cacheInfo.put("loadCount",   stats.loadCount());
                result.put(name, cacheInfo);
            }
        }

        return ResponseEntity.ok(result);
    }
}

