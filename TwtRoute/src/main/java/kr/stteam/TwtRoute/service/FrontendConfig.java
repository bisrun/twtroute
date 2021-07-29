package kr.stteam.TwtRoute.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FrontendConfig implements WebMvcConfigurer {

    @Value("${internal.resource.url}")
    private String internalResourceUrl;
    @Value("${external.resource.url}")
    private String externalResourceUrl;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        WebMvcConfigurer.super.addResourceHandlers(registry);
        registry.addResourceHandler(internalResourceUrl)
            .addResourceLocations(externalResourceUrl);
    }
}
