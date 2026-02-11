package com.chatbot.modules.tenant.infra;

import com.chatbot.modules.tenant.core.model.Tenant;
import com.chatbot.modules.tenant.core.repository.TenantRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

// @Component - Temporarily disabled
// @RequiredArgsConstructor
// @Slf4j
// public class TenantResolverFilter extends OncePerRequestFilter {
//
//    private final TenantRepository tenantRepository;
//
//    @Override
//    protected void doFilterInternal(
//            HttpServletRequest request,
//            HttpServletResponse response,
//            FilterChain filterChain
//    ) throws ServletException, IOException {
//
//        // Implementation commented out for now
//    }
// }
