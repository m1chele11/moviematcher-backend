package com.movieAI.moviematcher.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;



/**
 * Configuration class to define a RestTemplate bean.
 * <p>
 * This class provides a RestTemplate bean that can be used for making HTTP requests
 * to external services. The RestTemplate is a synchronous client to perform HTTP requests,
 * exposing a simple, template method API over underlying HTTP client libraries such as the JDK
 * HttpURLConnection, Apache HttpComponents, and others.
 * <p>
 * Usage:
 * <pre>
 *     &#64;Autowired
 *     private RestTemplate restTemplate;
 *
 *     public void someMethod() {
 *         String response = restTemplate.getForObject("http://example.com/api/resource", String.class);
 *         // Process the response
 *     }
 * </pre>
 * <p>
 * Note: In a production environment, consider configuring additional settings for the RestTemplate,
 * such as connection timeouts, error handlers, and message converters, to suit your application's needs.
 */


@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
