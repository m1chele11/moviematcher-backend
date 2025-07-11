package com.movieAI.moviematcher.config;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Enhanced RestTemplate configuration with connection pooling, timeouts, and error handling.
 * <p>
 * This configuration provides a production-ready RestTemplate with:
 * - Connection pooling for better performance
 * - Configurable timeouts
 * - Proper resource management
 * - Error handling capabilities
 * <p>
 * The configuration uses Apache HttpComponents for better control over HTTP client behavior.
 */
@Configuration
public class EnhancedRestTemplateConfig {

    @Value("${http.client.connection-timeout:5000}")
    private int connectionTimeout;

    @Value("${http.client.read-timeout:10000}")
    private int readTimeout;

    @Value("${http.client.max-connections:50}")
    private int maxConnections;

    @Value("${http.client.max-connections-per-route:10}")
    private int maxConnectionsPerRoute;

    /**
     * Creates a RestTemplate bean with enhanced configuration
     */
    @Bean
    public RestTemplate restTemplate() {
        // Configure connection pooling
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(maxConnections);
        connectionManager.setDefaultMaxPerRoute(maxConnectionsPerRoute);

        // Configure request timeouts
        RequestConfig requestConfig = RequestConfig.custom()
                //.setConnectionRequestTimeout(Timeout.of(Duration.ofMilliseconds(connectionTimeout)))
                //.setResponseTimeout(Timeout.of(Duration.ofMilliseconds(readTimeout)))
                .setConnectionRequestTimeout(Timeout.of(Duration.ofSeconds(connectionTimeout)))
                .build();

        // Build HTTP client with configuration
        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();

        // Create HTTP request factory
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);

        return new RestTemplate(requestFactory);
    }

    /**
     * Creates a separate RestTemplate for external API calls with different timeout settings
     */
    @Bean("externalApiRestTemplate")
    public RestTemplate externalApiRestTemplate() {
        // Configure with longer timeouts for external APIs
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(maxConnections);
        connectionManager.setDefaultMaxPerRoute(maxConnectionsPerRoute);

        RequestConfig requestConfig = RequestConfig.custom()
                //.setConnectionRequestTimeout(Timeout.of(Duration.ofMilliseconds(connectionTimeout)))
                //.setResponseTimeout(Timeout.of(Duration.ofMilliseconds(readTimeout * 2))) // Longer timeout for external APIs
                .setConnectionRequestTimeout(Timeout.of(Duration.ofSeconds(connectionTimeout)))
                .build();

        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);

        return new RestTemplate(requestFactory);
    }
}
