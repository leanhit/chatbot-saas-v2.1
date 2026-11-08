package com.chatbot.core.tenant.service;

import com.chatbot.core.identity.grpc.IdentityGrpcClient;
import com.chatbot.core.identity.grpc.IdentityServiceOuterClass.GetUserResponse;
import com.chatbot.core.identity.grpc.IdentityServiceOuterClass.GetUserRoleResponse;
import com.chatbot.core.identity.grpc.IdentityServiceOuterClass.IsUserActiveResponse;
import com.chatbot.core.identity.grpc.IdentityServiceOuterClass.ValidateTokenResponse;
import com.chatbot.core.identity.grpc.IdentityServiceOuterClass.ValidateUserResponse;
import com.chatbot.core.tenant.exception.GrpcIntegrationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdentityGrpcServiceTest {

    @Mock
    private IdentityGrpcClient identityGrpcClient;

    @InjectMocks
    private IdentityGrpcService service;

    // ---------- validateToken ----------

    @Test
    void validateTokenReturnsValidFlag() {
        when(identityGrpcClient.validateToken("tok"))
            .thenReturn(ValidateTokenResponse.newBuilder()
                .setValid(true).setUserId("u1").setEmail("a@b.c").build());
        assertTrue(service.validateToken("tok"));
    }

    @Test
    void validateTokenReturnsFalseForInvalid() {
        when(identityGrpcClient.validateToken("bad"))
            .thenReturn(ValidateTokenResponse.newBuilder().setValid(false).build());
        assertFalse(service.validateToken("bad"));
    }

    @Test
    void validateTokenThrowsOnNullResponse() {
        when(identityGrpcClient.validateToken("tok")).thenReturn(null);
        assertThrows(RuntimeException.class, () -> service.validateToken("tok"));
    }

    @Test
    void validateTokenWrapsGrpcErrors() {
        when(identityGrpcClient.validateToken("tok")).thenThrow(new RuntimeException("grpc down"));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.validateToken("tok"));
        assertTrue(ex.getMessage().contains("Failed to validate token"));
    }

    // ---------- validateUser ----------

    @Test
    void validateUserReturnsValidFlag() {
        when(identityGrpcClient.validateUser("u1"))
            .thenReturn(ValidateUserResponse.newBuilder().setValid(true).setIsActive(true).build());
        assertTrue(service.validateUser("u1"));
    }

    @Test
    void validateUserThrowsOnNullResponse() {
        when(identityGrpcClient.validateUser("u1")).thenReturn(null);
        assertThrows(RuntimeException.class, () -> service.validateUser("u1"));
    }

    // ---------- getUserRole ----------

    @Test
    void getUserRoleReturnsRole() {
        when(identityGrpcClient.getUserRole("u1"))
            .thenReturn(GetUserRoleResponse.newBuilder().setRole("ADMIN").build());
        assertEquals("ADMIN", service.getUserRole("u1"));
    }

    @Test
    void getUserRoleThrowsOnEmptyRole() {
        when(identityGrpcClient.getUserRole("u1"))
            .thenReturn(GetUserRoleResponse.newBuilder().setRole("").build());
        assertThrows(RuntimeException.class, () -> service.getUserRole("u1"));
    }

    // ---------- isUserActive ----------

    @Test
    void isUserActiveReturnsFlag() {
        when(identityGrpcClient.isUserActive("u1"))
            .thenReturn(IsUserActiveResponse.newBuilder().setIsActive(true).build());
        assertTrue(service.isUserActive("u1"));
    }

    @Test
    void isUserActiveThrowsOnNullResponse() {
        when(identityGrpcClient.isUserActive("u1")).thenReturn(null);
        assertThrows(RuntimeException.class, () -> service.isUserActive("u1"));
    }

    // ---------- getUserProfile ----------

    @Test
    void getUserProfileReturnsResponse() {
        GetUserResponse resp = GetUserResponse.newBuilder()
            .setEmail("a@b.c").setRole("USER").setIsActive(true).build();
        when(identityGrpcClient.getUserProfile("u1")).thenReturn(resp);
        assertEquals("a@b.c", service.getUserProfile("u1").getEmail());
    }

    @Test
    void getUserProfileThrowsGrpcIntegrationExceptionOnNull() {
        when(identityGrpcClient.getUserProfile("u1")).thenReturn(null);
        assertThrows(GrpcIntegrationException.class, () -> service.getUserProfile("u1"));
    }

    @Test
    void getUserProfileWrapsErrors() {
        when(identityGrpcClient.getUserProfile("u1")).thenThrow(new RuntimeException("down"));
        assertThrows(GrpcIntegrationException.class, () -> service.getUserProfile("u1"));
    }
}
