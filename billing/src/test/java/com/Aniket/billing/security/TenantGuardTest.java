package com.Aniket.billing.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class TenantGuardTest {

    private static final String T1 = "tenant::t1";
    private static final String T2 = "tenant::t2";

    private final TenantGuard guard = new TenantGuard();
    private final Object handler = mock(HandlerMethod.class);

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    private void urlTenant(String tenantId) {
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Map.of("tenantId", tenantId));
    }

    @Test
    void nonHandlerMethod_isAllowed() throws Exception {
        assertTrue(guard.preHandle(request, response, new Object()));
    }

    @Test
    void superadmin_isAllowedEvenOnAnotherTenantsUrl() throws Exception {
        request.setAttribute("role", "SUPERADMIN");
        request.setAttribute("tenantId", T1);
        urlTenant(T2);

        assertTrue(guard.preHandle(request, response, handler));
    }

    @Test
    void matchingTenant_isAllowed() throws Exception {
        request.setAttribute("role", "ADMIN");
        request.setAttribute("tenantId", T1);
        urlTenant(T1);

        assertTrue(guard.preHandle(request, response, handler));
        assertEquals(200, response.getStatus());
    }

    @Test
    void mismatchedTenant_returns403() throws Exception {
        request.setAttribute("role", "ADMIN");
        request.setAttribute("tenantId", T1);
        urlTenant(T2);

        assertFalse(guard.preHandle(request, response, handler));
        assertEquals(403, response.getStatus());
    }

    @Test
    void noTenantPathVariable_returns403() throws Exception {
        request.setAttribute("role", "ADMIN");
        request.setAttribute("tenantId", T1);
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Map.of("id", "x"));

        assertFalse(guard.preHandle(request, response, handler));
        assertEquals(403, response.getStatus());
    }

    @Test
    void nullPathVariablesMap_returns403() throws Exception {
        request.setAttribute("role", "ADMIN");
        request.setAttribute("tenantId", T1);

        assertFalse(guard.preHandle(request, response, handler));
        assertEquals(403, response.getStatus());
    }

    @Test
    void noTenantInToken_returns403() throws Exception {
        request.setAttribute("role", "ADMIN");
        urlTenant(T1);

        assertFalse(guard.preHandle(request, response, handler));
        assertEquals(403, response.getStatus());
    }

    @Test
    void noRole_stillEnforcesTenantMatch() throws Exception {
        request.setAttribute("tenantId", T1);
        urlTenant(T2);

        assertFalse(guard.preHandle(request, response, handler));
        assertEquals(403, response.getStatus());
    }

    @Test
    void getOwnTenantById_isAllowed() throws Exception {
        request.setMethod("GET");
        request.setAttribute("role", "ADMIN");
        request.setAttribute("tenantId", T1);
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Map.of("id", T1));

        assertTrue(guard.preHandle(request, response, handler));
    }

    @Test
    void getOtherTenantById_returns403() throws Exception {
        request.setMethod("GET");
        request.setAttribute("role", "ADMIN");
        request.setAttribute("tenantId", T1);
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Map.of("id", T2));

        assertFalse(guard.preHandle(request, response, handler));
        assertEquals(403, response.getStatus());
    }

    @Test
    void patchOrDeleteOwnTenantById_returns403_forNonSuperadmin() throws Exception {
        for (String method : new String[]{"PATCH", "DELETE"}) {
            MockHttpServletRequest req = new MockHttpServletRequest();
            MockHttpServletResponse res = new MockHttpServletResponse();
            req.setMethod(method);
            req.setAttribute("role", "ADMIN");
            req.setAttribute("tenantId", T1);
            req.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Map.of("id", T1));

            assertFalse(guard.preHandle(req, res, handler), method);
            assertEquals(403, res.getStatus());
        }
    }

    @Test
    void listAllTenants_returns403_forNonSuperadmin_butAllowedForSuperadmin() throws Exception {
        request.setMethod("GET");
        request.setAttribute("role", "ADMIN");
        request.setAttribute("tenantId", T1);
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Map.of());

        assertFalse(guard.preHandle(request, response, handler));
        assertEquals(403, response.getStatus());

        MockHttpServletRequest su = new MockHttpServletRequest();
        su.setMethod("GET");
        su.setAttribute("role", "SUPERADMIN");
        assertTrue(guard.preHandle(su, new MockHttpServletResponse(), handler));
    }
}