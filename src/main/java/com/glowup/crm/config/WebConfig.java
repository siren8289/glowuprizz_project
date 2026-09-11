package com.glowup.crm.config;

import com.glowup.crm.security.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final TokenService tokenService;
    private final String corsOrigin;

    public WebConfig(TokenService tokenService, @Value("${app.cors-origin}") String corsOrigin) {
        this.tokenService = tokenService;
        this.corsOrigin = corsOrigin;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedOrigins(corsOrigin.split(",")).allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AdminInterceptor(tokenService)).addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login");
    }

    private record AdminInterceptor(TokenService tokenService) implements HandlerInterceptor {
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            String header = request.getHeader("Authorization");
            Long adminId = header != null && header.startsWith("Bearer ")
                    ? tokenService.verify(header.substring(7)) : null;
            if (adminId == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }
            request.setAttribute("adminId", adminId);
            return true;
        }
    }
}
