package com.chatbot.core.tenant.grpc;

import com.chatbot.core.tenant.core.model.Tenant;
import com.chatbot.core.tenant.core.repository.TenantRepository;
import com.chatbot.core.tenant.grpc.TenantServiceProto.*;
import com.chatbot.core.tenant.grpc.TenantServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TenantServiceGrpcImpl extends TenantServiceGrpc.TenantServiceImplBase {

    @Autowired
    private TenantRepository tenantRepository;

    // Helper method để convert Tenant sang TenantResponse
    private TenantResponse toGrpcTenantResponse(Tenant tenant) {
        return TenantResponse.newBuilder()
                .setId(tenant.getId().toString())
                .setTenantKey(tenant.getTenantKey())
                .setName(tenant.getName())
                .setDescription("") // Tenant không có description field
                .setStatus(tenant.getStatus().name())
                .setVisibility(tenant.getVisibility().name())
                .setCreatedAt(tenant.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toString())
                .setUpdatedAt(tenant.getUpdatedAt().atZone(ZoneId.systemDefault()).toInstant().toString())
                .setExpiresAt(tenant.getExpiresAt() != null ? 
                    tenant.getExpiresAt().atZone(ZoneId.systemDefault()).toInstant().toString() : "")
                .build();
    }

    @Override
    public void validateTenant(ValidateTenantRequest request, StreamObserver<ValidateTenantResponse> responseObserver) {
        try {
            log.info("gRPC: Validating tenant với key: {}", request.getTenantKey());
            
            Tenant tenant = tenantRepository.findByTenantKey(request.getTenantKey())
                    .orElse(null);
            
            boolean valid = tenant != null;
            String status = valid ? tenant.getStatus().name() : "NOT_FOUND";
            String message = valid ? "Tenant hợp lệ" : "Không tìm thấy tenant";
            
            ValidateTenantResponse response = ValidateTenantResponse.newBuilder()
                    .setValid(valid)
                    .setTenantKey(request.getTenantKey())
                    .setStatus(status)
                    .setMessage(message)
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            log.error("Lỗi khi validate tenant qua gRPC", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void checkTenantExists(CheckTenantExistsRequest request, StreamObserver<CheckTenantExistsResponse> responseObserver) {
        try {
            log.info("gRPC: Kiểm tra tenant tồn tại với key: {}", request.getTenantKey());
            
            boolean exists = tenantRepository.findByTenantKey(request.getTenantKey()).isPresent();
            
            CheckTenantExistsResponse response = CheckTenantExistsResponse.newBuilder()
                    .setExists(exists)
                    .setTenantKey(request.getTenantKey())
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            log.error("Lỗi khi kiểm tra tenant tồn tại qua gRPC", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getTenant(GetTenantRequest request, StreamObserver<TenantDetailResponse> responseObserver) {
        try {
            log.info("gRPC: Lấy tenant với key: {}", request.getTenantKey());
            
            Tenant tenant = tenantRepository.findByTenantKey(request.getTenantKey())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy tenant"));
            
            TenantDetailResponse response = TenantDetailResponse.newBuilder()
                    .setId(tenant.getId().toString())
                    .setTenantKey(tenant.getTenantKey())
                    .setName(tenant.getName())
                    .setDescription("") // Tenant không có description field
                    .setStatus(tenant.getStatus().name())
                    .setVisibility(tenant.getVisibility().name())
                    .setCreatedAt(tenant.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toString())
                    .setUpdatedAt(tenant.getUpdatedAt().atZone(ZoneId.systemDefault()).toInstant().toString())
                    .setExpiresAt(tenant.getExpiresAt() != null ? 
                        tenant.getExpiresAt().atZone(ZoneId.systemDefault()).toInstant().toString() : "")
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            log.error("Lỗi khi lấy tenant qua gRPC", e);
            responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void listTenants(ListTenantsRequest request, StreamObserver<ListTenantsResponse> responseObserver) {
        try {
            log.info("gRPC: Liệt kê tenants - trang: {}, kích thước: {}", request.getPage(), request.getSize());
            
            List<Tenant> tenants = tenantRepository.findAll();
            
            List<TenantResponse> tenantResponses = tenants.stream()
                    .map(this::toGrpcTenantResponse)
                    .collect(Collectors.toList());
            
            ListTenantsResponse response = ListTenantsResponse.newBuilder()
                    .addAllTenants(tenantResponses)
                    .setTotalElements(tenants.size())
                    .setTotalPages(1)
                    .setCurrentPage(request.getPage())
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            log.error("Lỗi khi liệt kê tenants qua gRPC", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

    // Các method khác sẽ được implement sau
    @Override
    public void createTenant(CreateTenantRequest request, StreamObserver<TenantResponse> responseObserver) {
        responseObserver.onError(io.grpc.Status.UNIMPLEMENTED
                .withDescription("Method chưa được implement")
                .asRuntimeException());
    }

    @Override
    public void updateTenant(UpdateTenantRequest request, StreamObserver<TenantResponse> responseObserver) {
        responseObserver.onError(io.grpc.Status.UNIMPLEMENTED
                .withDescription("Method chưa được implement")
                .asRuntimeException());
    }

    @Override
    public void deleteTenant(DeleteTenantRequest request, StreamObserver<DeleteTenantResponse> responseObserver) {
        responseObserver.onError(io.grpc.Status.UNIMPLEMENTED
                .withDescription("Method chưa được implement")
                .asRuntimeException());
    }

    @Override
    public void searchTenants(SearchTenantsRequest request, StreamObserver<SearchTenantsResponse> responseObserver) {
        responseObserver.onError(io.grpc.Status.UNIMPLEMENTED
                .withDescription("Method chưa được implement")
                .asRuntimeException());
    }

    @Override
    public void activateTenant(ActivateTenantRequest request, StreamObserver<TenantResponse> responseObserver) {
        responseObserver.onError(io.grpc.Status.UNIMPLEMENTED
                .withDescription("Method chưa được implement")
                .asRuntimeException());
    }

    @Override
    public void suspendTenant(SuspendTenantRequest request, StreamObserver<TenantResponse> responseObserver) {
        responseObserver.onError(io.grpc.Status.UNIMPLEMENTED
                .withDescription("Method chưa được implement")
                .asRuntimeException());
    }

    @Override
    public void getTenantStatus(GetTenantStatusRequest request, StreamObserver<TenantStatusResponse> responseObserver) {
        responseObserver.onError(io.grpc.Status.UNIMPLEMENTED
                .withDescription("Method chưa được implement")
                .asRuntimeException());
    }
}
