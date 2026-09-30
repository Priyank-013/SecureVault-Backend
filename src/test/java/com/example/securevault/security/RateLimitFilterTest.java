package com.example.securevault.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.PrintWriter;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

public class RateLimitFilterTest {

    StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);

    HttpServletRequest request = mock(HttpServletRequest.class);

    HttpServletResponse response = mock(HttpServletResponse.class);

    FilterChain filterChain = mock(FilterChain.class);

    ValueOperations<String, String> valueOperations =
            mock(ValueOperations.class);

    PrintWriter writer = mock(PrintWriter.class);

    @Test
    void testRateLimit() throws Exception {

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        when(valueOperations.increment("rate_limit:192.168.1.10"))
                .thenReturn(101L);

        when(request.getRequestURI()).thenReturn("/api/secrets");

        when(request.getRemoteAddr()).thenReturn("192.168.1.10");

        when(response.getWriter()).thenReturn(writer);

        RateLimitFilter filter = new RateLimitFilter(redisTemplate);

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(429);

        verify(filterChain, never()).doFilter(request, response);
    }
}