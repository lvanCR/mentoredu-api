package com.mentoredu.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Rate limiting para /api/v1/** — protege contra fuerza bruta y abuso.
 * Auth endpoints: 20 req/min por IP.
 * Resto de la API: 60 req/min por IP.
 * Limpieza de buckets inactivos cada hora (AtomicLong evita cleanup duplicado bajo concurrencia).
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int      AUTH_CAPACITY       = 20;
    private static final int      API_CAPACITY        = 60;
    private static final Duration REFILL_PERIOD       = Duration.ofMinutes(1);
    private static final long     CLEANUP_INTERVAL_MS = Duration.ofHours(1).toMillis();

    private final ConcurrentHashMap<String, Bucket> authBuckets = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Bucket> apiBuckets  = new ConcurrentHashMap<>();
    private final AtomicLong lastCleanupAt = new AtomicLong(System.currentTimeMillis());

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/v1/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        evictIfNeeded();
        String ip     = resolveClientIp(request);
        boolean isAuth = request.getRequestURI().startsWith("/api/v1/auth/");

        Bucket bucket = isAuth
            ? authBuckets.computeIfAbsent(ip, k -> newBucket(AUTH_CAPACITY))
            : apiBuckets.computeIfAbsent(ip,  k -> newBucket(API_CAPACITY));

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("Retry-After", "60");
            response.getWriter().write(
                "{\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"Demasiadas solicitudes. Intenta de nuevo en un minuto.\"}");
        }
    }

    private void evictIfNeeded() {
        long now  = System.currentTimeMillis();
        long last = lastCleanupAt.get();
        if (now - last > CLEANUP_INTERVAL_MS && lastCleanupAt.compareAndSet(last, now)) {
            authBuckets.entrySet().removeIf(e -> e.getValue().getAvailableTokens() == AUTH_CAPACITY);
            apiBuckets.entrySet().removeIf(e  -> e.getValue().getAvailableTokens() == API_CAPACITY);
        }
    }

    private Bucket newBucket(int capacity) {
        return Bucket.builder()
            .addLimit(Bandwidth.classic(capacity, Refill.greedy(capacity, REFILL_PERIOD)))
            .build();
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            // Usar la última IP de la cadena: la que agrega el proxy de confianza (Render).
            String[] parts = forwarded.split(",");
            return parts[parts.length - 1].trim();
        }
        return request.getRemoteAddr();
    }
}
