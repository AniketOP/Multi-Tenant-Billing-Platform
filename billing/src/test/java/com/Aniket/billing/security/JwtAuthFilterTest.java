package com.Aniket.billing.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JwtAuthFilterTest {

    private static final String SECRET = "test-secret-key-that-is-at-least-32-bytes-long!!";
    private static final String OTHER_SECRET = "a-completely-different-secret-key-32-bytes-min!!";

    private final JWTUtil jwtUtil = new JWTUtil(SECRET);
    private final JwtAuthFilter filter = new JwtAuthFilter(jwtUtil);

    private MockHttpServletResponse response;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        response = new MockHttpServletResponse();
        chain = mock(FilterChain.class);
    }

    private MockHttpServletRequest request(String method, String path) {
        MockHttpServletRequest req = new MockHttpServletRequest(method, path);
        req.setRequestURI(path);
        return req;
    }

    private MockHttpServletRequest withBearer(String method, String path, String token) {
        MockHttpServletRequest req = request(method, path);
        req.addHeader("Authorization", "Bearer " + token);
        return req;
    }

    private void assertRejected401() throws Exception {
        assertEquals(401, response.getStatus());
        verify(chain, never()).doFilter(any(), any());
    }

    // ---------- pass-through paths ----------

    @Test
    void options_passesThroughWithoutToken() throws Exception {
        MockHttpServletRequest req = request("OPTIONS", "/tenants/tenant::t1/units");

        filter.doFilter(req, response, chain);

        verify(chain).doFilter(req, response);
    }

    @Test
    void publicPaths_onlyLoginAndActuator_passThroughWithoutToken() throws Exception {
        for (String path : new String[]{"/auth/login", "/actuator/health"}) {
            FilterChain c = mock(FilterChain.class);

            filter.doFilter(request("GET", path), new MockHttpServletResponse(), c);

            verify(c).doFilter(any(), any());
        }
    }

    @Test
    void tenantsAndSignup_requireToken() throws Exception {
        for (String[] mp : new String[][]{
                {"GET", "/tenants"}, {"POST", "/tenants"}, {"GET", "/tenants/tenant::t1"},
                {"PATCH", "/tenants/tenant::t1"}, {"DELETE", "/tenants/tenant::t1"},
                {"POST", "/auth/signup"}}) {
            FilterChain c = mock(FilterChain.class);
            MockHttpServletResponse res = new MockHttpServletResponse();

            filter.doFilter(request(mp[0], mp[1]), res, c);

            assertEquals(401, res.getStatus(), mp[0] + " " + mp[1]);
            verify(c, never()).doFilter(any(), any());
        }
    }

    @Test
    void tenantSubResources_areNotPublic() throws Exception {
        filter.doFilter(request("GET", "/tenants/tenant::t1/units"), response, chain);

        assertRejected401();
    }

    // ---------- rejected requests ----------

    @Test
    void missingAuthHeader_returns401() throws Exception {
        filter.doFilter(request("GET", "/tenants/tenant::t1/units"), response, chain);

        assertRejected401();
    }

    @Test
    void nonBearerHeader_returns401() throws Exception {
        MockHttpServletRequest req = request("GET", "/tenants/tenant::t1/units");
        req.addHeader("Authorization", "Basic abc123");

        filter.doFilter(req, response, chain);

        assertRejected401();
    }

    @Test
    void garbageToken_returns401() throws Exception {
        filter.doFilter(withBearer("GET", "/tenants/tenant::t1/units", "garbage"), response, chain);

        assertRejected401();
    }

    @Test
    void tokenSignedWithWrongSecret_returns401() throws Exception {
        String forged = new JWTUtil(OTHER_SECRET).generateToken("u", "tenant::t1", "ADMIN");

        filter.doFilter(withBearer("GET", "/tenants/tenant::t1/units", forged), response, chain);

        assertRejected401();
    }

    @Test
    void expiredToken_returns401() throws Exception {
        String expired = Jwts.builder()
                .subject("u")
                .claim("tenantId", "tenant::t1")
                .claim("role", "ADMIN")
                .issuedAt(new Date(System.currentTimeMillis() - 2 * 3600_000L))
                .expiration(new Date(System.currentTimeMillis() - 3600_000L))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes()))
                .compact();

        filter.doFilter(withBearer("GET", "/tenants/tenant::t1/units", expired), response, chain);

        assertRejected401();
    }

    @Test
    void payloadSwappedToAnotherTenant_returns401() throws Exception {
        String[] a = jwtUtil.generateToken("u", "tenant::t1", "ADMIN").split("\\.");
        String[] b = jwtUtil.generateToken("u", "tenant::t2", "ADMIN").split("\\.");
        String tampered = a[0] + "." + b[1] + "." + a[2];

        filter.doFilter(withBearer("GET", "/tenants/tenant::t1/units", tampered), response, chain);

        assertRejected401();
    }

    // ---------- happy path ----------

    @Test
    void validToken_setsRequestAttributes_andContinuesChain() throws Exception {
        String token = jwtUtil.generateToken("rahul_admin", "tenant::t1", "ADMIN");
        MockHttpServletRequest req = withBearer("GET", "/tenants/tenant::t1/units", token);

        filter.doFilter(req, response, chain);

        assertEquals("rahul_admin", req.getAttribute("username"));
        assertEquals("tenant::t1", req.getAttribute("tenantId"));
        assertEquals("ADMIN", req.getAttribute("role"));
        assertEquals(200, response.getStatus());
        verify(chain).doFilter(req, response);
    }
}