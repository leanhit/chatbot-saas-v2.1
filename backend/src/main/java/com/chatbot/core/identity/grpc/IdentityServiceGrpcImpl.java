package com.chatbot.core.identity.grpc;

import com.chatbot.core.identity.grpc.IdentityServiceOuterClass.*;
import com.chatbot.core.identity.grpc.IdentityServiceGrpc;
import com.chatbot.core.identity.model.Auth;
import com.chatbot.core.identity.repository.AuthRepository;
import com.chatbot.core.identity.service.JwtService;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class IdentityServiceGrpcImpl extends IdentityServiceGrpc.IdentityServiceImplBase {
    
    @Autowired
    private AuthRepository authRepository;
    
    @Autowired
    private JwtService jwtService;

    @Override
    public void validateToken(ValidateTokenRequest request, StreamObserver<ValidateTokenResponse> responseObserver) {
        try {
            log.info("gRPC: Validating token cho user");
            
            String token = request.getToken();
            String email = jwtService.extractUsername(token);
            Optional<Auth> userOpt = authRepository.findByEmail(email);
            
            boolean valid = userOpt.isPresent() && jwtService.isTokenValid(token, userOpt.get());
            
            ValidateTokenResponse.Builder responseBuilder = ValidateTokenResponse.newBuilder()
                    .setValid(valid);
            
            if (valid && userOpt.isPresent()) {
                Auth user = userOpt.get();
                responseBuilder
                    .setUserId(user.getId().toString())
                    .setEmail(user.getEmail())
                    .setRole(user.getSystemRole().name());
            } else {
                responseBuilder.setErrorMessage("Token không hợp lệ hoặc đã hết hạn");
            }
            
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            log.error("Lỗi khi validate token qua gRPC", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getUserProfile(GetUserRequest request, StreamObserver<GetUserResponse> responseObserver) {
        try {
            log.info("gRPC: Lấy user profile với ID: {}", request.getUserId());
            
            Long userId = Long.parseLong(request.getUserId());
            Optional<Auth> userOpt = authRepository.findById(userId);
            
            GetUserResponse.Builder responseBuilder = GetUserResponse.newBuilder()
                    .setUserId(request.getUserId());
            
            if (userOpt.isPresent()) {
                Auth user = userOpt.get();
                responseBuilder
                    .setEmail(user.getEmail())
                    .setRole(user.getSystemRole().name())
                    .setIsActive(user.getIsActive())
                    .setCreatedAt("") // Auth không có createdAt
                    .setUpdatedAt(""); // Auth không có updatedAt
            } else {
                responseBuilder.setErrorMessage("Không tìm thấy user");
            }
            
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            log.error("Lỗi khi lấy user profile qua gRPC", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void validateUser(ValidateUserRequest request, StreamObserver<ValidateUserResponse> responseObserver) {
        try {
            log.info("gRPC: Validating user với ID: {}", request.getUserId());
            
            Long userId = Long.parseLong(request.getUserId());
            Optional<Auth> userOpt = authRepository.findById(userId);
            
            boolean valid = userOpt.map(Auth::getIsActive).orElse(false);
            String message = valid ? "User hợp lệ" : "User không tồn tại hoặc không active";
            
            ValidateUserResponse response = ValidateUserResponse.newBuilder()
                    .setValid(valid)
                    .setUserId(request.getUserId())
                    .setIsActive(valid)
                    .setMessage(message)
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            log.error("Lỗi khi validate user qua gRPC", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getUserRole(GetUserRoleRequest request, StreamObserver<GetUserRoleResponse> responseObserver) {
        try {
            log.info("gRPC: Lấy user role với ID: {}", request.getUserId());
            
            Long userId = Long.parseLong(request.getUserId());
            Optional<Auth> userOpt = authRepository.findById(userId);
            
            GetUserRoleResponse.Builder responseBuilder = GetUserRoleResponse.newBuilder()
                    .setUserId(request.getUserId());
            
            if (userOpt.isPresent()) {
                Auth user = userOpt.get();
                responseBuilder.setRole(user.getSystemRole().name());
            } else {
                responseBuilder.setErrorMessage("Không tìm thấy user");
            }
            
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            log.error("Lỗi khi lấy user role qua gRPC", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void isUserActive(IsUserActiveRequest request, StreamObserver<IsUserActiveResponse> responseObserver) {
        try {
            log.info("gRPC: Kiểm tra user active với ID: {}", request.getUserId());
            
            Long userId = Long.parseLong(request.getUserId());
            Optional<Auth> userOpt = authRepository.findById(userId);
            
            boolean isActive = userOpt.map(Auth::getIsActive).orElse(false);
            
            IsUserActiveResponse.Builder responseBuilder = IsUserActiveResponse.newBuilder()
                    .setUserId(request.getUserId())
                    .setIsActive(isActive);
            
            if (!userOpt.isPresent()) {
                responseBuilder.setErrorMessage("Không tìm thấy user");
            }
            
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            log.error("Lỗi khi kiểm tra user active qua gRPC", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }
}
