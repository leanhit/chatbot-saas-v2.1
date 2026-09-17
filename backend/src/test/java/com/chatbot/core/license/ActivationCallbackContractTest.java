package com.chatbot.core.license;

import com.chatbot.core.user.model.User;
import com.chatbot.core.identity.security.CustomUserDetails;
import com.chatbot.core.identity.service.JwtService;
import com.chatbot.core.license.controller.ActivationController;
import com.chatbot.core.license.controller.LicenseController;
import com.chatbot.core.license.dto.LicenseResponse;
import com.chatbot.core.license.service.LicenseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ActivationCallbackContractTest {

    @Mock
    private LicenseService licenseService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private LicenseController licenseController;

    private ActivationController activationController;

    @BeforeEach
    void setUp() {
        activationController = new ActivationController();
    }

    @Test
    @DisplayName("Local Activation Entrypoint: /activate 302 redirects to /api/license/activate preserving deviceId and state")
    void testActivationEntrypointRedirect() {
        String deviceId = "sta_hw_test_99999";
        String state = "nonce_random_abc123";

        ResponseEntity<Void> response = activationController.activatePage(deviceId, state, null);

        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertEquals("/api/license/activate?deviceId=sta_hw_test_99999&state=nonce_random_abc123", 
                response.getHeaders().getFirst("Location"));
    }

    @Test
    @DisplayName("Unauthenticated User: /api/license/activate 302 redirects to Cloud Login with encoded target redirect")
    void testUnauthenticatedActivationRedirect() {
        String deviceId = "sta_hw_test_99999";
        String state = "nonce_random_abc123";

        ResponseEntity<Void> response = licenseController.activateLicenseBrowser(deviceId, state, null);

        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        String location = response.getHeaders().getFirst("Location");
        assertNotNull(location);
        assertTrue(location.startsWith("/login?redirect="));
        assertTrue(location.contains("sta_hw_test_99999"));
        assertTrue(location.contains("nonce_random_abc123"));
    }

    @Test
    @DisplayName("Authenticated User Callback Contract: HTTP 302 Redirect to http://localhost:1717/callback?token=<LICENSE_JWT>&state=<NONCE>")
    void testAuthenticatedActivationCallbackContract() {
        String deviceId = "sta_hw_test_99999";
        String state = "nonce_random_abc123";

        User mockUser = new User();
        mockUser.setId(100L);
        mockUser.setEmail("testuser@startai.vn");
        CustomUserDetails userDetails = new CustomUserDetails(mockUser);

        LicenseResponse mockLicense = LicenseResponse.builder()
                .id(1L)
                .sub("100")
                .email("testuser@startai.vn")
                .exp(1800000000L)
                .features(List.of("facebook", "zalo"))
                .modules(List.of("reengage", "ai-reply"))
                .limits(Map.of("bots", 2))
                .build();

        when(licenseService.hasActiveLicense(100L)).thenReturn(true);
        when(licenseService.getLicenseForUser(100L)).thenReturn(mockLicense);
        when(jwtService.generateLicenseToken(
                eq("testuser@startai.vn"),
                eq(100L),
                eq(deviceId),
                anyLong(),
                anyList(),
                anyList(),
                anyMap()
        )).thenReturn("eyJhbGciOiJSUzI1NiJ9.MOCK_PAYLOAD.MOCK_SIGNATURE");

        ResponseEntity<Void> response = licenseController.activateLicenseBrowser(deviceId, state, userDetails);

        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        String location = response.getHeaders().getFirst("Location");
        assertNotNull(location);

        // Verify exact callback contract format
        assertTrue(location.startsWith("http://localhost:1717/callback?"));
        
        var uriComponents = UriComponentsBuilder.fromUriString(location).build();
        String returnedToken = uriComponents.getQueryParams().getFirst("token");
        String returnedState = uriComponents.getQueryParams().getFirst("state");

        assertEquals("eyJhbGciOiJSUzI1NiJ9.MOCK_PAYLOAD.MOCK_SIGNATURE", returnedToken);
        assertEquals(state, returnedState, "State/nonce MUST be preserved without modification!");
    }
}
