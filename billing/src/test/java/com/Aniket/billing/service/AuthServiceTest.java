package com.Aniket.billing.service;

import com.Aniket.billing.dto.LoginRequest;
import com.Aniket.billing.dto.SignupRequest;
import com.Aniket.billing.exception.ForbiddenException;
import com.Aniket.billing.exception.ResourceNotFoundException;
import com.Aniket.billing.model.User;
import com.Aniket.billing.repository.UserRepository;
import com.Aniket.billing.security.JWTUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String T1 = "tenant::t1";
    private static final String T2 = "tenant::t2";

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JWTUtil jwtUtil;
    @InjectMocks private AuthService authService;

    private SignupRequest signupReq(String tenantId, String role) {
        SignupRequest r = new SignupRequest();
        r.setTenantId(tenantId);
        r.setUsername("bob");
        r.setPassword("pw");
        r.setRole(role);
        return r;
    }

    private LoginRequest loginReq(String username, String password) {
        LoginRequest r = new LoginRequest();
        r.setUsername(username);
        r.setPassword(password);
        return r;
    }

    private void assertSignupForbidden(SignupRequest req, String callerRole, String callerTenant) {
        assertThrows(ForbiddenException.class,
                () -> authService.signup(req, callerRole, callerTenant));
        // the permission check must happen before ANY database access
        verifyNoInteractions(userRepository);
    }

    // ---------- signup: who is NOT allowed ----------

    @Test
    void signup_noCaller_forbidden() {
        assertSignupForbidden(signupReq(T1, "ADMIN"), null, null);
    }

    @Test
    void signup_nonAdminCaller_forbidden() {
        assertSignupForbidden(signupReq(T1, "STAFF"), "STAFF", T1);
    }

    @Test
    void signup_adminForOtherTenant_forbidden() {
        assertSignupForbidden(signupReq(T2, "ADMIN"), "ADMIN", T1);
    }

    @Test
    void signup_adminCreatingSuperadmin_forbidden() {
        assertSignupForbidden(signupReq(T1, "SUPERADMIN"), "ADMIN", T1);
    }

    @Test
    void signup_adminWithNoTenantInToken_forbidden() {
        assertSignupForbidden(signupReq(T1, "ADMIN"), "ADMIN", null);
    }

    // ---------- signup: who IS allowed ----------

    @Test
    void signup_adminOwnTenant_savesHashedPasswordAndIssuesToken() {
        when(userRepository.findByUsername("bob")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pw")).thenReturn("HASH");
        when(jwtUtil.generateToken("bob", T1, "ADMIN")).thenReturn("tok");

        assertNotNull(authService.signup(signupReq(T1, "ADMIN"), "ADMIN", T1));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertTrue(saved.getId().startsWith("user::"));
        assertEquals(T1, saved.getTenantId());
        assertEquals("bob", saved.getUsername());
        assertEquals("HASH", saved.getPasswordHash());   // never the raw password
        assertEquals("ADMIN", saved.getRole());
    }

    @Test
    void signup_superadmin_canCreateAnyTenantAndRole() {
        when(userRepository.findByUsername("bob")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pw")).thenReturn("HASH");
        when(jwtUtil.generateToken("bob", T2, "SUPERADMIN")).thenReturn("tok");

        assertDoesNotThrow(() -> authService.signup(signupReq(T2, "SUPERADMIN"), "SUPERADMIN", null));

        verify(userRepository).save(any(User.class));
    }

    @Test
    void signup_usernameTaken_throwsAndDoesNotSave() {
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(new User()));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.signup(signupReq(T1, "ADMIN"), "ADMIN", T1));

        assertEquals("Username already taken", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    // ---------- login ----------

    @Test
    void login_success_issuesTokenWithUsersTenantAndRole() {
        User user = User.builder().username("bob").tenantId(T1).role("ADMIN").passwordHash("HASH").build();
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pw", "HASH")).thenReturn(true);
        when(jwtUtil.generateToken("bob", T1, "ADMIN")).thenReturn("tok");

        assertNotNull(authService.login(loginReq("bob", "pw")));

        verify(jwtUtil).generateToken("bob", T1, "ADMIN");
    }

    @Test
    void login_wrongPassword_throwsNotFound_andIssuesNoToken() {
        User user = User.builder().username("bob").tenantId(T1).role("ADMIN").passwordHash("HASH").build();
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("bad", "HASH")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> authService.login(loginReq("bob", "bad")));

        verifyNoInteractions(jwtUtil);
    }

    @Test
    void login_unknownUser_throwsNotFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.login(loginReq("ghost", "pw")));
    }
}