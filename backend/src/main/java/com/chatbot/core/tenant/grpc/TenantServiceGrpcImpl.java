package com.chatbot.core.tenant.grpc;

import com.chatbot.core.tenant.model.Tenant;
import com.chatbot.core.tenant.repository.TenantRepository;
import com.chatbot.core.tenant.service.TenantService;
import com.chatbot.core.tenant.grpc.TenantServiceProto.*;
import com.chatbot.core.tenant.grpc.TenantServiceGrpc;
import com.chatbot.core.tenant.dto.CreateTenantRequest;
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
    
    @Autowired
    private TenantService tenantService;

    // Helper method để convert Tenant sang TenantResponse
    private com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse toGrpcTenantResponse(Tenant tenant) {
        return com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse.newBuilder()
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
    public void createTenant(com.chatbot.core.tenant.grpc.TenantServiceProto.CreateTenantRequest request, StreamObserver<com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse> responseObserver) {
        try {
            log.info("gRPC: Creating tenant with key: {}, name: {}", request.getTenantKey(), request.getName());
            
            // Convert gRPC request to DTO
            CreateTenantRequest dtoRequest = new CreateTenantRequest();
            dtoRequest.setName(request.getName());
            dtoRequest.setVisibility(com.chatbot.core.tenant.model.TenantVisibility.valueOf(request.getVisibility()));
            
            // Call existing service
            com.chatbot.core.tenant.dto.TenantResponse response = tenantService.createTenant(dtoRequest);
            
            // Convert DTO to gRPC response
            com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse grpcResponse = 
                com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse.newBuilder()
                    .setId(response.getId().toString())
                    .setTenantKey(response.getTenantKey())
                    .setName(response.getName())
                    .setDescription("") // Empty description
                    .setStatus(response.getStatus().toString())
                    .setVisibility(response.getVisibility().toString())
                    .setCreatedAt(response.getCreatedAt().toString())
                    .setUpdatedAt(response.getCreatedAt().toString()) // Use createdAt as updatedAt
                    .setExpiresAt(response.getExpiresAt() != null ? response.getExpiresAt().toString() : "")
                    .build();
            
            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            log.error("Lỗi khi create tenant qua gRPC", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void updateTenant(UpdateTenantRequest request, StreamObserver<com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse> responseObserver) {
        try {
            log.info("gRPC: Updating tenant with key: {}, name: {}", request.getTenantKey(), request.getName());
            
            // Find tenant by tenantKey first
            Tenant tenant = tenantRepository.findByTenantKey(request.getTenantKey())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy tenant với key: " + request.getTenantKey()));
            
            // Update tenant entity
            if (request.getName() != null && !request.getName().isEmpty()) {
                tenant.setName(request.getName());
            }
            if (request.getDescription() != null && !request.getDescription().isEmpty()) {
                // Note: Tenant entity doesn't have description field, you might need to add it
            }
            if (request.getVisibility() != null && !request.getVisibility().isEmpty()) {
                tenant.setVisibility(com.chatbot.core.tenant.model.TenantVisibility.valueOf(request.getVisibility()));
            }
            
            tenant.setUpdatedAt(LocalDateTime.now());
            
            // Save the updated tenant
            Tenant updatedTenant = tenantRepository.save(tenant);
            
            // Convert to gRPC response
            com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse grpcResponse = 
                toGrpcTenantResponse(updatedTenant);
            
            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            log.error("Lỗi khi update tenant qua gRPC", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void deleteTenant(DeleteTenantRequest request, StreamObserver<DeleteTenantResponse> responseObserver) {
        try {
            log.info("gRPC: Deleting tenant with key: {}", request.getTenantKey());
            
            // Find tenant by tenantKey
            Tenant tenant = tenantRepository.findByTenantKey(request.getTenantKey())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy tenant với key: " + request.getTenantKey()));
            
            // Soft delete by changing status to INACTIVE or DELETED
            tenant.setStatus(com.chatbot.core.tenant.model.TenantStatus.INACTIVE);
            tenant.setUpdatedAt(LocalDateTime.now());
            
            // Save the change
            tenantRepository.save(tenant);
            
            // Create response
            DeleteTenantResponse response = DeleteTenantResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Tenant đã được xóa thành công")
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            log.error("Lỗi khi delete tenant qua gRPC", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void searchTenants(SearchTenantsRequest request, StreamObserver<SearchTenantsResponse> responseObserver) {
        try {
            log.info("gRPC: Searching tenants with query: {}, page: {}, size: {}", 
                    request.getQuery(), request.getPage(), request.getSize());
            
            // Get all tenants and filter by query (simple implementation)
            List<Tenant> allTenants = tenantRepository.findAll();
            
            List<Tenant> filteredTenants = allTenants.stream()
                    .filter(tenant -> {
                        String query = request.getQuery().toLowerCase();
                        return tenant.getName().toLowerCase().contains(query) ||
                               tenant.getTenantKey().toLowerCase().contains(query);
                    })
                    .skip(request.getPage() * request.getSize())
                    .limit(request.getSize())
                    .collect(Collectors.toList());
            
            // Convert to gRPC response
            List<com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse> tenantResponses = 
                filteredTenants.stream()
                    .map(this::toGrpcTenantResponse)
                    .collect(Collectors.toList());
            
            SearchTenantsResponse response = SearchTenantsResponse.newBuilder()
                    .addAllTenants(tenantResponses)
                    .setTotalElements(filteredTenants.size())
                    .setTotalPages((int) Math.ceil((double) allTenants.size() / request.getSize()))
                    .setCurrentPage(request.getPage())
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            log.error("Lỗi khi search tenants qua gRPC", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void activateTenant(ActivateTenantRequest request, StreamObserver<com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse> responseObserver) {
        try {
            log.info("gRPC: Activating tenant with key: {}", request.getTenantKey());
            
            Tenant tenant = tenantRepository.findByTenantKey(request.getTenantKey())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy tenant với key: " + request.getTenantKey()));
            
            tenant.setStatus(com.chatbot.core.tenant.model.TenantStatus.ACTIVE);
            tenant.setUpdatedAt(LocalDateTime.now());
            
            Tenant updatedTenant = tenantRepository.save(tenant);
            
            com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse grpcResponse = 
                toGrpcTenantResponse(updatedTenant);
            
            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            log.error("Lỗi khi activate tenant qua gRPC", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void suspendTenant(SuspendTenantRequest request, StreamObserver<com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse> responseObserver) {
        try {
            log.info("gRPC: Suspending tenant with key: {}", request.getTenantKey());
            
            Tenant tenant = tenantRepository.findByTenantKey(request.getTenantKey())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy tenant với key: " + request.getTenantKey()));
            
            tenant.setStatus(com.chatbot.core.tenant.model.TenantStatus.SUSPENDED);
            tenant.setUpdatedAt(LocalDateTime.now());
            
            Tenant updatedTenant = tenantRepository.save(tenant);
            
            com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse grpcResponse = 
                toGrpcTenantResponse(updatedTenant);
            
            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            log.error("Lỗi khi suspend tenant qua gRPC", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getTenantStatus(GetTenantStatusRequest request, StreamObserver<TenantStatusResponse> responseObserver) {
        try {
            log.info("gRPC: Getting status for tenant with key: {}", request.getTenantKey());
            
            Tenant tenant = tenantRepository.findByTenantKey(request.getTenantKey())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy tenant với key: " + request.getTenantKey()));
            
            TenantStatusResponse response = TenantStatusResponse.newBuilder()
                    .setTenantKey(request.getTenantKey())
                    .setStatus(tenant.getStatus().name())
                    .setMessage("Tenant status retrieved successfully")
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            log.error("Lỗi khi get tenant status qua gRPC", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }
}
