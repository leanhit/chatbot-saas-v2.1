package com.chatbot.core.identity.service;

import com.chatbot.core.user.model.User;
import com.chatbot.core.user.repository.AuthRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TwoFactorServiceTest {

    @Mock private AuthRepository authRepository;

    @InjectMocks
    private TwoFactorService service;

    private User user(String email) {
        User u = new User();
        u.setEmail(email);
        u.setTotpEnabled(false);
        return u;
    }

    @Test
    void generateSecretStoresSecretAndDisables2fa() {
        User u = user("a@test.com");
        when(authRepository.findByEmail("a@test.com")).thenReturn(Optional.of(u));
        when(authRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        ReflectionTestUtils.setField(service, "appName", "TestApp");

        TwoFactorService.TwoFactorSetup setup = service.generateSecret("a@test.com");

        assertNotNull(setup.secret());
        assertTrue(setup.secret().length() >= 16);
        assertTrue(setup.otpAuthUri().startsWith("otpauth://totp/TestApp:a@test.com"));
        assertFalse(u.getTotpEnabled());
        assertEquals(setup.secret(), u.getTotpSecret());
    }

    @Test
    void generateSecretRejectsUnknownUser() {
        when(authRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.generateSecret("ghost@test.com"));
    }

    @Test
    void enableFailsWhenNoSecret() {
        User u = user("a@test.com");
        u.setTotpSecret(null);
        when(authRepository.findByEmail("a@test.com")).thenReturn(Optional.of(u));

        assertThrows(IllegalStateException.class, () -> service.enable("a@test.com", "123456"));
    }

    @Test
    void enableReturnsFalseForInvalidCode() {
        User u = user("a@test.com");
        u.setTotpSecret("JBSWY3DPEHPK3PXP");
        when(authRepository.findByEmail("a@test.com")).thenReturn(Optional.of(u));

        assertFalse(service.enable("a@test.com", "000000"));
        assertFalse(u.getTotpEnabled());
        verify(authRepository, never()).save(any());
    }

    @Test
    void disableReturnsFalseWhenNotEnabled() {
        User u = user("a@test.com");
        u.setTotpSecret("JBSWY3DPEHPK3PXP");
        u.setTotpEnabled(false);
        when(authRepository.findByEmail("a@test.com")).thenReturn(Optional.of(u));

        assertFalse(service.disable("a@test.com", "123456"));
    }

    @Test
    void verifyCodeNullSafe() {
        assertFalse(service.verifyCode(null, "123456"));
        assertFalse(service.verifyCode("secret", null));
        assertFalse(service.verifyCode("secret", "  "));
        assertFalse(service.verifyCode("not-a-valid-secret!!!", "123456"));
    }

    @Test
    void isEnabledRequiresBothFlagAndSecret() {
        User u = user("a@test.com");
        assertFalse(service.isEnabled(u));
        u.setTotpEnabled(true);
        assertFalse(service.isEnabled(u)); // no secret
        u.setTotpSecret("JBSWY3DPEHPK3PXP");
        assertTrue(service.isEnabled(u));
    }
}
