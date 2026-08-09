package com.desenvolvimento.logica.conviva_service.config;

import com.desenvolvimento.logica.conviva_service.common.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

    @Value("${security.rate-limit.login.capacity}")
    private Integer capacityLogin;

    @Value("${security.rate-limit.login.duration-minutes}")
    private Integer durationMinutesLogin;

    @Value("${security.rate-limit.refresh.capacity}")
    private Integer capacityRefresh;

    @Value("${security.rate-limit.refresh.duration-minutes}")
    private Integer durationMinutesRefresh;

    @Value("${security.rate-limit.default.capacity}")
    private Integer capacityDefault;

    @Value("${security.rate-limit.default.duration-minutes}")
    private Integer durationMinutesDefault;

    private final ObjectMapper objectMapper;

    public AuthRateLimitFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String uri = request.getRequestURI();

        if (!isProtectedEndpoint(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = getClientIp(request);

        Bucket bucket = cache.get(
                ip + ":" + uri,
                key -> createBucket(uri)
        );

        long availableTokens = bucket.getAvailableTokens();

        response.setHeader(
                "X-Rate-Limit-Remaining",
                String.valueOf(availableTokens)
        );

        if (!bucket.tryConsume(1)) {

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            response.setHeader(
                    "Retry-After",
                    "60"
            );

            ApiResponse<Void> apiResponse = new ApiResponse<>(
                    false,
                    "Muitas requisições. Tente novamente em instantes.",
                    null
            );

            objectMapper.writeValue(
                    response.getWriter(),
                    apiResponse
            );

            return;
        }

        filterChain.doFilter(request, response);

    }

    private boolean isProtectedEndpoint(String uri) {
        return uri.contains("/auth/login")
                || uri.contains("/auth/refresh")
                || uri.contains("/auth/logout")
                || uri.contains("/auth/change-password");
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp;
        }
        return request.getRemoteAddr();
    }

    private Bucket createBucket(String uri) {
        if (uri.contains("/auth/login")) {
            return Bucket.builder()
                    .addLimit(
                            Bandwidth.classic(
                                    capacityLogin,
                                    Refill.greedy(
                                            capacityLogin,
                                            Duration.ofMinutes(durationMinutesLogin)
                                    )
                            )
                    )
                    .build();
        }

        if (uri.contains("/auth/refresh")) {
            return Bucket.builder()
                    .addLimit(
                            Bandwidth.classic(
                                    capacityRefresh,
                                    Refill.greedy(
                                            capacityRefresh,
                                            Duration.ofMinutes(durationMinutesRefresh)
                                    )
                            )
                    )
                    .build();
        }
        return Bucket.builder()
                .addLimit(
                        Bandwidth.classic(
                                capacityDefault,
                                Refill.greedy(
                                        capacityDefault,
                                        Duration.ofMinutes(durationMinutesDefault)
                                )
                        )
                )
                .build();
    }

    private final Cache<String, Bucket> cache =
            Caffeine.newBuilder()
                    .expireAfterAccess(Duration.ofHours(1))
                    .maximumSize(10_000)
                    .build();
}
