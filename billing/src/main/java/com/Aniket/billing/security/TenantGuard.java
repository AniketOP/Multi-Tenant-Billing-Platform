package com.Aniket.billing.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

@Component
public class TenantGuard implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception
    {

        if(!(handler instanceof HandlerMethod)){
            return true;
        }

        String role = (String) request.getAttribute("role");
        if("SUPERADMIN".equals(role)){
            return true;
        }

        @SuppressWarnings("unchecked")
        Map<String,String> pathVars =
                (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        String urlTenantId = pathVars != null ? pathVars.get("tenantId") : null;

        String tokenTenantId = (String) request.getAttribute("tenantId");

        if(urlTenantId == null || tokenTenantId == null || !urlTenantId.equals(tokenTenantId)){
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Tenant mismatch");
            return false;
        }

        return true;


    }

}
