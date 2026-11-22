package sayyeed.dev.atlantbackend.utils;

import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimiter {

    private final int maxRequests;
    private final long windowMillis;
    private final ConcurrentHashMap<String, Deque<Long>> requestTimestamps = new ConcurrentHashMap<>();

    public RateLimiter() {
        this(5, 60_000L);
    }

    RateLimiter(int maxRequests, long windowMillis) {
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;
    }

    public synchronized boolean allow(String key) {
        long now = System.currentTimeMillis();

        // Get or create deque for this key
        Deque<Long> timestamps = requestTimestamps.computeIfAbsent(key, k -> new ArrayDeque<>());

        // Prune old timestamps outside the window
        while (!timestamps.isEmpty() && timestamps.peek() < now - windowMillis) {
            timestamps.poll();
        }

        // Check if we can allow this request
        if (timestamps.size() < maxRequests) {
            timestamps.add(now);
            return true;
        }

        return false;
    }
}
