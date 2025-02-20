package faang.school.urlshortenerservice.util;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class HashCache {

    private final int cacheCapacity;
    private final int threshold;
    private final int batchSize;
    private final int initialMinSize;
    private final int initialFillingSize;
    private final HashGenerator hashGenerator;
    private final ExecutorService fillUpCacheExecutorService;
    private final AtomicBoolean isFillingUp = new AtomicBoolean(false);
    private final Queue<String> cache;

    public HashCache(
            @Value("${app.hash-cache.size:1000}") int cacheCapacity,
            @Value("${app.hash-cache.threshold:200}") int threshold,
            @Value("${app.hashes-generation.batch-size:800}") int batchSize,
            @Value("${app.hashes-generation.initial-min-size:500}") int initialMinSize,
            @Value("${app.hashes-generation.initial-filling-size:250}") int initialFillingSize,
            HashGenerator hashGenerator,
            ExecutorService fillUpCacheExecutorService
    ) {
        this.cacheCapacity = cacheCapacity;
        this.threshold = threshold;
        this.batchSize = batchSize;
        this.initialMinSize = initialMinSize;
        this.initialFillingSize = initialFillingSize;
        this.hashGenerator = hashGenerator;
        this.fillUpCacheExecutorService = fillUpCacheExecutorService;
        this.cache = new ConcurrentLinkedDeque<>();
    }

    @PostConstruct
    public void init() {
        int hashesCount = hashGenerator.getHashesCount();
        if (hashesCount >= initialMinSize) {
            fillCache(initialFillingSize);
        } else {
            hashGenerator.generateBatchOfHashes(cacheCapacity + batchSize);
            fillCache(cacheCapacity);
        }
    }

    public String getHash() {
        fillUpCacheIfNeeded();
        return cache.poll();
    }

    private void fillUpCacheIfNeeded() {
        if (cache.size() < threshold && isFillingUp.compareAndSet(false, true)) {
            fillUpCacheExecutorService.execute(() -> {
                try {
                    fillUpCache(batchSize);
                } finally {
                    isFillingUp.set(false);
                }
            });
        }
    }

    private void fillUpCache(int batchSize) {
        List<String> hashesFromDb = hashGenerator.getHashes(batchSize);
        cache.addAll(hashesFromDb);
        hashGenerator.generateBatchOfHashesAsync(cacheCapacity);
    }

    private void fillCache(int size) {
        List<String> hashesFromDb = hashGenerator.getHashes(size);
        cache.addAll(hashesFromDb);
    }
}
