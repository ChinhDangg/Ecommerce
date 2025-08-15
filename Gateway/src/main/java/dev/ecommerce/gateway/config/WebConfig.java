package dev.ecommerce.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${url.front-end}")
    private String feUrl;

    // probably need to enable or used by Security config or WebMvcConfigurer to apply?
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // match all gateway routes
                .allowedOrigins(feUrl) // frontend origin
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Authorization", "Cache-Control", "Content-Type")
                .allowCredentials(true);
    }

}
