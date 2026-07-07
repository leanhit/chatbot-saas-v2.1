package com.chatbot.core.tenant.grpc;

import lombok.RequiredArgsConstructor;
import com.chatbot.core.tenant.exception.TenantNotFoundException;
import com.chatbot.core.tenant.model.Tenant;
import com.chatbot.core.tenant.repository.TenantRepository;
import com.chatbot.core.tenant.service.TenantService;
import com.chatbot.core.tenant.grpc.TenantServiceProto.*;
import com.chatbot.core.tenant.dto.CreateTenantRequest;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

/**
 * gRPC service implementation cho Tenant operations.
 *
 * SECURITY: All gRPC endpoints are protected by GrpcAuthInterceptor which validates
 * JWT tokens from metadata before processing any request.
 */
@Service
@Slf4j
public @RequiredArgsConstructor
class TenantServiceGrpcImpl extends TenantServiceGrpc.TenantServiceImplBase {


    private final TenantRepository tenantRepository;


    private final TenantService tenantService;


    private final com.chatbot.core.tenant.profile.repository.TenantProfileRepository tenantProfileRepository;

    // =========================================================================
    // HELPERS
    // =========================================================================

    private TenantServiceProto.TenantResponse toGrpcTenantResponse(Tenant tenant) {
        try {
            String description = tenantProfileRepository.findById(tenant.getId())
                    .map(p -> p.getDescription() != null ? p.getDescription() : "")
                    .orElse("");

            return TenantServiceProto.TenantResponse.newBuilder()
                    .setId(tenant.getId().toString())
                    .setTenantKey(tenant.getTenantKey())
                    .setName(tenant.getName())
                    .setDescription(description)
                    .setStatus(tenant.getStatus() != null ? tenant.getStatus().name() : "UNKNOWN")
                    .setVisibility(tenant.getVisibility() != null ? tenant.getVisibility().name() : "UNKNOWN")
                    .setCreatedAt(tenant.getCreatedAt() != null
                            ? tenant.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toString() : "")
                    .setUpdatedAt(tenant.getUpdatedAt() != null
                            ? tenant.getUpdatedAt().atZone(ZoneId.systemDefault()).toInstant().toString() : "")
                    .setExpiresAt(tenant.getExpiresAt() != null
                            ? tenant.getExpiresAt().atZone(ZoneId.systemDefault()).toInstant().toString() : "")
                    .build();
        } catch (Exception e) {
            log.error("[gRPC] Error converting tenant {} to response: {}", tenant.getId(), e.getMessage());
            return TenantServiceProto.TenantResponse.newBuilder()
                    .setId(tenant.getId() != null ? tenant.getId().toString() : "")
                    .setTenantKey(tenant.getTenantKey() != null ? tenant.getTenantKey() : "")
                    .setName(tenant.getName() != null ? tenant.getName() : "")
                    .setDescription("").setStatus("ERROR").setVisibility("UNKNOWN")
                    .setCreatedAt("").setUpdatedAt("").setExpiresAt("")
                    .build();
        }
    }

    // =========================================================================
    // VALIDATE / CHECK
    // =========================================================================

    @Override
    public void validateTenant(ValidateTenantRequest request, StreamObserver<ValidateTenantResponse> responseObserver) {
        try {
            log.debug("[gRPC] validateTenant: key={}", request.getTenantKey());
            Tenant tenant = tenantRepository.findByTenantKey(request.getTenantKey()).orElse(null);
            boolean valid = tenant != null;

            responseObserver.onNext(ValidateTenantResponse.newBuilder()
                    .setValid(valid)
                    .setTenantKey(request.getTenantKey())
                    .setStatus(valid ? tenant.getStatus().name() : "NOT_FOUND")
                    .setMessage(valid ? "Tenant hợp lệ" : "Không tìm thấy tenant")
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("[gRPC] validateTenant error", e);
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void checkTenantExists(CheckTenantExistsRequest request, StreamObserver<CheckTenantExistsResponse> responseObserver) {
        try {
            boolean exists = tenantRepository.findByTenantKey(request.getTenantKey()).isPresent();
            responseObserver.onNext(CheckTenantExistsResponse.newBuilder()
                    .setExists(exists).setTenantKey(request.getTenantKey()).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("[gRPC] checkTenantExists error", e);
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getTenantStatus(GetTenantStatusRequest request, StreamObserver<TenantStatusResponse> responseObserver) {
        try {
            Tenant tenant = tenantRepository.findByTenantKey(request.getTenantKey())
                    .orElseThrow(() -> new TenantNotFoundException("Không tìm thấy tenant: " + request.getTenantKey()));
            responseObserver.onNext(TenantStatusResponse.newBuilder()
                    .setTenantKey(request.getTenantKey())
                    .setStatus(tenant.getStatus().name())
                    .setMessage("Tenant status retrieved successfully")
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("[gRPC] getTenantStatus error", e);
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getTenant(GetTenantRequest request, StreamObserver<TenantDetailResponse> responseObserver) {
        try {
            Tenant tenant = tenantRepository.findByTenantKey(request.getTenantKey())
                    .orElseThrow(() -> new TenantNotFoundException("Không tìm thấy tenant: " + request.getTenantKey()));

            String description = tenantProfileRepository.findById(tenant.getId())
                    .map(p -> p.getDescription() != null ? p.getDescription() : "")
                    .orElse("");

            responseObserver.onNext(TenantDetailResponse.newBuilder()
                    .setId(tenant.getId().toString())
                    .setTenantKey(tenant.getTenantKey())
                    .setName(tenant.getName())
                    .setDescription(description)
                    .setStatus(tenant.getStatus().name())
                    .setVisibility(tenant.getVisibility().name())
                    .setCreatedAt(tenant.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toString())
                    .setUpdatedAt(tenant.getUpdatedAt().atZone(ZoneId.systemDefault()).toInstant().toString())
                    .setExpiresAt(tenant.getExpiresAt() != null
                            ? tenant.getExpiresAt().atZone(ZoneId.systemDefault()).toInstant().toString() : "")
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("[gRPC] getTenant error", e);
            responseObserver.onError(io.grpc.Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    // =========================================================================
    // LIST / SEARCH — dùng DB-level pagination thay vì loadAll() vào memory
    // =========================================================================

    @Override
    public void listTenants(ListTenantsRequest request, StreamObserver<ListTenantsResponse> responseObserver) {
        try {
            int page = Math.max(request.getPage(), 0);
            int size = request.getSize() > 0 ? request.getSize() : 20;

            // Dùng Spring Data pagination — không load toàn bộ bảng vào heap
            Page<Tenant> tenantPage = tenantRepository.findAll(PageRequest.of(page, size));

            List<TenantServiceProto.TenantResponse> tenantResponses = tenantPage.getContent().stream()
                    .map(this::toGrpcTenantResponse)
                    .collect(Collectors.toList());

            responseObserver.onNext(ListTenantsResponse.newBuilder()
                    .addAllTenants(tenantResponses)
                    .setTotalElements((int) tenantPage.getTotalElements())
                    .setTotalPages(tenantPage.getTotalPages())
                    .setCurrentPage(page)
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("[gRPC] listTenants error", e);
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription("Lỗi khi liệt kê tenants: " + e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void searchTenants(SearchTenantsRequest request, StreamObserver<SearchTenantsResponse> responseObserver) {
        try {
            int page = Math.max(request.getPage(), 0);
            int size = request.getSize() > 0 ? request.getSize() : 20;
            String keyword = request.getQuery() != null ? request.getQuery().trim() : "";

            // Dùng DB-level search query thay vì in-memory filter
            Page<Tenant> tenantPage = tenantRepository.search(keyword, PageRequest.of(page, size));

            List<TenantServiceProto.TenantResponse> tenantResponses = tenantPage.getContent().stream()
                    .map(this::toGrpcTenantResponse)
                    .collect(Collectors.toList());

            responseObserver.onNext(SearchTenantsResponse.newBuilder()
                    .addAllTenants(tenantResponses)
                    .setTotalElements((int) tenantPage.getTotalElements())
                    .setTotalPages(tenantPage.getTotalPages())
                    .setCurrentPage(page)
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("[gRPC] searchTenants error", e);
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    // =========================================================================
    // MUTATIONS — Protected by GrpcAuthInterceptor
    // =========================================================================

    @Override
    public void createTenant(TenantServiceProto.CreateTenantRequest request,
                             StreamObserver<TenantServiceProto.TenantResponse> responseObserver) {
        try {
            CreateTenantRequest dtoRequest = new CreateTenantRequest();
            dtoRequest.setName(request.getName());
            dtoRequest.setVisibility(com.chatbot.core.tenant.model.TenantVisibility.valueOf(request.getVisibility()));

            com.chatbot.core.tenant.dto.TenantResponse response = tenantService.createTenant(dtoRequest);

            responseObserver.onNext(TenantServiceProto.TenantResponse.newBuilder()
                    .setId(response.getId().toString())
                    .setTenantKey(response.getTenantKey())
                    .setName(response.getName())
                    .setDescription("")
                    .setStatus(response.getStatus().toString())
                    .setVisibility(response.getVisibility().toString())
                    .setCreatedAt(response.getCreatedAt().toString())
                    .setUpdatedAt(response.getCreatedAt().toString())
                    .setExpiresAt(response.getExpiresAt() != null ? response.getExpiresAt().toString() : "")
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("[gRPC] createTenant error", e);
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void updateTenant(UpdateTenantRequest request,
                             StreamObserver<TenantServiceProto.TenantResponse> responseObserver) {
        try {
            Tenant tenant = tenantRepository.findByTenantKey(request.getTenantKey())
                    .orElseThrow(() -> new TenantNotFoundException("Không tìm thấy tenant: " + request.getTenantKey()));

            if (request.getName() != null && !request.getName().isEmpty()) {
                tenant.setName(request.getName());
            }
            if (request.getDescription() != null && !request.getDescription().isEmpty()) {
                tenantProfileRepository.findById(tenant.getId()).ifPresent(profile -> {
                    profile.setDescription(request.getDescription());
                    tenantProfileRepository.save(profile);
                });
            }
            if (request.getVisibility() != null && !request.getVisibility().isEmpty()) {
                tenant.setVisibility(com.chatbot.core.tenant.model.TenantVisibility.valueOf(request.getVisibility()));
            }
            tenant.setUpdatedAt(LocalDateTime.now());

            responseObserver.onNext(toGrpcTenantResponse(tenantRepository.save(tenant)));
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("[gRPC] updateTenant error", e);
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void deleteTenant(DeleteTenantRequest request, StreamObserver<DeleteTenantResponse> responseObserver) {
        try {
            Tenant tenant = tenantRepository.findByTenantKey(request.getTenantKey())
                    .orElseThrow(() -> new TenantNotFoundException("Không tìm thấy tenant: " + request.getTenantKey()));

            // Soft-delete: set DELETED thay vì INACTIVE
            tenant.setStatus(com.chatbot.core.tenant.model.TenantStatus.DELETED);
            tenant.setUpdatedAt(LocalDateTime.now());
            tenantRepository.save(tenant);

            responseObserver.onNext(DeleteTenantResponse.newBuilder()
                    .setSuccess(true).setMessage("Tenant đã được xóa thành công").build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("[gRPC] deleteTenant error", e);
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void activateTenant(ActivateTenantRequest request,
                               StreamObserver<TenantServiceProto.TenantResponse> responseObserver) {
        try {
            Tenant tenant = tenantRepository.findByTenantKey(request.getTenantKey())
                    .orElseThrow(() -> new TenantNotFoundException("Không tìm thấy tenant: " + request.getTenantKey()));
            tenant.setStatus(com.chatbot.core.tenant.model.TenantStatus.ACTIVE);
            tenant.setUpdatedAt(LocalDateTime.now());
            responseObserver.onNext(toGrpcTenantResponse(tenantRepository.save(tenant)));
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("[gRPC] activateTenant error", e);
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void suspendTenant(SuspendTenantRequest request,
                              StreamObserver<TenantServiceProto.TenantResponse> responseObserver) {
        try {
            Tenant tenant = tenantRepository.findByTenantKey(request.getTenantKey())
                    .orElseThrow(() -> new TenantNotFoundException("Không tìm thấy tenant: " + request.getTenantKey()));
            tenant.setStatus(com.chatbot.core.tenant.model.TenantStatus.SUSPENDED);
            tenant.setUpdatedAt(LocalDateTime.now());
            responseObserver.onNext(toGrpcTenantResponse(tenantRepository.save(tenant)));
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("[gRPC] suspendTenant error", e);
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }
}
