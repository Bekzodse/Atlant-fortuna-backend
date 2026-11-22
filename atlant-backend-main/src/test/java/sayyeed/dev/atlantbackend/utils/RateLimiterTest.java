package sayyeed.dev.atlantbackend.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimiterTest {

    @Test
    void allowsUpToMaxRequestsThenRejects() {
        RateLimiter limiter = new RateLimiter(2, 100_000L);

        assertTrue(limiter.allow("1.2.3.4"));
        assertTrue(limiter.allow("1.2.3.4"));
        assertFalse(limiter.allow("1.2.3.4"));
    }

    @Test
    void resetsAfterWindowPasses() throws InterruptedException {
        RateLimiter limiter = new RateLimiter(1, 100L);

        assertTrue(limiter.allow("5.6.7.8"));
        assertFalse(limiter.allow("5.6.7.8"));

        Thread.sleep(150);

        assertTrue(limiter.allow("5.6.7.8"));
    }

    @Test
    void tracksDifferentKeysIndependently() {
        RateLimiter limiter = new RateLimiter(1, 100_000L);

        assertTrue(limiter.allow("1.1.1.1"));
        assertTrue(limiter.allow("2.2.2.2"));
    }
}
