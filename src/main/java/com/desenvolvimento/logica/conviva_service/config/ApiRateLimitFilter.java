package com.desenvolvimento.logica.conviva_service.config;

import com.desenvolvimento.logica.conviva_security.holder.TenantContextHolder;
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
import java.util.UUID;

@Component
public class ApiRateLimitFilter extends OncePerRequestFilter {

    @Value("${security.rate-limit.api.capacity}")
    private Integer capacity;

    @Value("${security.rate-limit.api.duration-minutes}")
    private Integer durationMinutes;

    private final Cache<String, Bucket> cache =
            Caffeine.newBuilder()
                    .expireAfterAccess(Duration.ofHours(1))
                    .maximumSize(10_000)
                    .build();

    private final ObjectMapper objectMapper;

    public ApiRateLimitFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String uri = request.getRequestURI();

        if (isPublicEndpoint(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        UUID userId = TenantContextHolder.getUserId();

        if (userId == null) {
            filterChain.doFilter(request, response);
            return;
        }

        Bucket bucket = cache.get(
                userId.toString(),
                key -> createBucket()
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

            response.setHeader("Retry-After", "60");

            ApiResponse<Void> apiResponse = new ApiResponse<>(
                    false,
                    "Limite de requisições excedido."
            );

            objectMapper.writeValue(
                    response.getWriter(),
                    apiResponse
            );

            return;
        }

        filterChain.doFilter(request, response);

    }

    private Bucket createBucket() {
        return Bucket.builder()
                .addLimit(
                        Bandwidth.classic(
                                capacity,
                                Refill.greedy(
                                        capacity,
                                        Duration.ofMinutes(durationMinutes)
                                )
                        )
                )
                .build();
    }

    private boolean isPublicEndpoint(String uri) {
        return uri.contains("/auth/login")
                || uri.contains("/auth/refresh")
                || uri.contains("/swagger-ui")
                || uri.contains("/v3/api-docs")
                || uri.contains("/actuator/health");
    }
}
