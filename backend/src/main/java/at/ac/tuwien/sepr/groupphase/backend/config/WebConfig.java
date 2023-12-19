package at.ac.tuwien.sepr.groupphase.backend.config;

import at.ac.tuwien.sepr.groupphase.backend.auth.AuthenticationInterceptor;
import at.ac.tuwien.sepr.groupphase.backend.service.AuthenticationService;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// this configuration effectively disables CORS; this is helpful during development but a bad idea in production
@Profile("!prod")
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthenticationService authService;

    public WebConfig(AuthenticationService service) {
        this.authService = service;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry
            .addMapping("/**")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthenticationInterceptor(authService));
        WebMvcConfigurer.super.addInterceptors(registry);
    }
}
