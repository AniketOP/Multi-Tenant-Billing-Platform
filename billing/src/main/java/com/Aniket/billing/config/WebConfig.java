package com.Aniket.billing.config;


import com.Aniket.billing.security.TenantGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final TenantGuard tenantGuard;

    @Override
    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(tenantGuard)
                .addPathPatterns("/tenants/{tenants}/**")
                .excludePathPatterns("/tenants", "/tenants/*");
    }



}
