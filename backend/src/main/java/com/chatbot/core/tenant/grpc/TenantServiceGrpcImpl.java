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
import java.util.ArrayList;
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
        try {
            return com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse.newBuilder()
                    .setId(tenant.getId().toString())
                    .setTenantKey(tenant.getTenantKey())
                    .setName(tenant.getName())
                    .setDescription("") // Tenant không có description field
                    .setStatus(tenant.getStatus() != null ? tenant.getStatus().name() : "UNKNOWN")
                    .setVisibility(tenant.getVisibility() != null ? tenant.getVisibility().name() : "UNKNOWN")
                    .setCreatedAt(tenant.getCreatedAt() != null ? 
                        tenant.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toString() : "")
                    .setUpdatedAt(tenant.getUpdatedAt() != null ? 
                        tenant.getUpdatedAt().atZone(ZoneId.systemDefault()).toInstant().toString() : "")
                    .setExpiresAt(tenant.getExpiresAt() != null ? 
                        tenant.getExpiresAt().atZone(ZoneId.systemDefault()).toInstant().toString() : "")
                    .build();
        } catch (Exception e) {
            log.error("Lỗi khi convert tenant sang gRPC response", e);
            // Return empty response on error
            return com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse.newBuilder()
                    .setId("")
                    .setTenantKey("")
                    .setName("")
                    .setDescription("")
                    .setStatus("ERROR")
                    .setVisibility("UNKNOWN")
                    .setCreatedAt("")
                    .setUpdatedAt("")
                    .setExpiresAt("")
                    .build();
        }
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
            
            // Get all tenants with detailed logging
            log.info("gRPC: Bắt đầu lấy tất cả tenants từ repository");
            List<Tenant> allTenants = tenantRepository.findAll();
            log.info("gRPC: Đã lấy được {} tenants từ database", allTenants.size());
            
            // Apply pagination with bounds checking
            int page = request.getPage();
            int size = request.getSize();
            int startIndex = page * size;
            int endIndex = Math.min(startIndex + size, allTenants.size());
            
            log.info("gRPC: Pagination - page: {}, size: {}, startIndex: {}, endIndex: {}", page, size, startIndex, endIndex);
            
            List<Tenant> pagedTenants = new ArrayList<>();
            if (startIndex < allTenants.size() && startIndex >= 0) {
                pagedTenants = allTenants.subList(startIndex, endIndex);
                log.info("gRPC: Đã lấy {} tenants cho trang {}", pagedTenants.size(), page);
            } else {
                log.warn("gRPC: Page {} is out of range, returning empty list", page);
            }
            
            // Convert to gRPC response with individual error handling
            List<com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse> tenantResponses = 
                pagedTenants.stream()
                    .map(tenant -> {
                        try {
                            return toGrpcTenantResponse(tenant);
                        } catch (Exception e) {
                            log.error("gRPC: Lỗi khi convert tenant ID: {}", tenant.getId(), e);
                            return null;
                        }
                    })
                    .filter(response -> response != null)
                    .collect(Collectors.toList());
            
            log.info("gRPC: Đã convert {} tenants thành gRPC response", tenantResponses.size());
            
            // Calculate pagination info
            int totalPages = (int) Math.ceil((double) allTenants.size() / size);
            log.info("gRPC: Pagination info - totalElements: {}, totalPages: {}", allTenants.size(), totalPages);
            
            ListTenantsResponse response = ListTenantsResponse.newBuilder()
                    .addAllTenants(tenantResponses)
                    .setTotalElements(allTenants.size())
                    .setTotalPages(totalPages)
                    .setCurrentPage(page)
                    .build();
            
            log.info("gRPC: Gửi response với {} tenants", tenantResponses.size());
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            log.error("gRPC: Lỗi khi liệt kê tenants qua gRPC", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Lỗi khi liệt kê tenants: " + e.getMessage())
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
            
            // Get all tenants
            List<Tenant> allTenants = tenantRepository.findAll();
            
            // Filter by query
            String query = request.getQuery() != null ? request.getQuery().toLowerCase().trim() : "";
            List<Tenant> filteredTenants = allTenants.stream()
                    .filter(tenant -> {
                        if (query.isEmpty()) {
                            return true; // Return all if query is empty
                        }
                        return (tenant.getName() != null && tenant.getName().toLowerCase().contains(query)) ||
                               (tenant.getTenantKey() != null && tenant.getTenantKey().toLowerCase().contains(query));
                    })
                    .collect(Collectors.toList());
            
            // Apply pagination
            int page = request.getPage();
            int size = request.getSize();
            int startIndex = page * size;
            int endIndex = Math.min(startIndex + size, filteredTenants.size());
            
            List<Tenant> pagedTenants = new ArrayList<>();
            if (startIndex < filteredTenants.size()) {
                pagedTenants = filteredTenants.subList(startIndex, endIndex);
            }
            
            // Convert to gRPC response
            List<com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse> tenantResponses = 
                pagedTenants.stream()
                    .map(this::toGrpcTenantResponse)
                    .collect(Collectors.toList());
            
            // Calculate pagination info
            int totalPages = (int) Math.ceil((double) filteredTenants.size() / size);
            
            SearchTenantsResponse response = SearchTenantsResponse.newBuilder()
                    .addAllTenants(tenantResponses)
                    .setTotalElements(filteredTenants.size())
                    .setTotalPages(totalPages)
                    .setCurrentPage(page)
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
