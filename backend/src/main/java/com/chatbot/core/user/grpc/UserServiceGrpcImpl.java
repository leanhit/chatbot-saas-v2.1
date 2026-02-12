package com.chatbot.core.user.grpc;

import com.chatbot.core.user.dto.UserDto;
import com.chatbot.core.user.model.User;
import com.chatbot.core.user.profile.UserProfile;
import com.chatbot.core.user.repository.UserRepository;
import com.chatbot.core.user.repository.UserProfileRepository;
import com.chatbot.core.user.grpc.UserServiceProto.*;
import com.chatbot.core.user.grpc.UserServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * User gRPC Service - Internal Communication
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceGrpcImpl extends UserServiceGrpc.UserServiceImplBase {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    @Override
    public void validateUser(ValidateUserRequest request, StreamObserver<ValidateUserResponse> responseObserver) {
        try {
            log.info("gRPC: Validating user với ID: {}", request.getUserId());
            
            Long userId = Long.parseLong(request.getUserId());
            Optional<User> userOpt = userRepository.findById(userId);
            
            boolean valid = userOpt.map(User::getIsActive).orElse(false);
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
    public void getUserProfile(GetUserProfileRequest request, StreamObserver<GetUserProfileResponse> responseObserver) {
        try {
            log.info("gRPC: Lấy user profile với ID: {}", request.getUserId());
            
            Long userId = Long.parseLong(request.getUserId());
            Optional<UserProfile> profileOpt = userProfileRepository.findByUserId(userId);
            
            GetUserProfileResponse.Builder responseBuilder = GetUserProfileResponse.newBuilder()
                    .setUserId(request.getUserId());
            
            if (profileOpt.isPresent()) {
                UserProfile profile = profileOpt.get();
                responseBuilder
                    .setFullName(profile.getFullName() != null ? profile.getFullName() : "")
                    .setPhoneNumber(profile.getPhoneNumber() != null ? profile.getPhoneNumber() : "")
                    .setAvatar(profile.getAvatar() != null ? profile.getAvatar() : "")
                    .setGender(profile.getGender() != null ? profile.getGender() : "")
                    .setBio(profile.getBio() != null ? profile.getBio() : "")
                    .setJobTitle(profile.getJobTitle() != null ? profile.getJobTitle() : "")
                    .setCompany(profile.getCompany() != null ? profile.getCompany() : "")
                    .setLocation(profile.getLocation() != null ? profile.getLocation() : "");
            } else {
                responseBuilder.setErrorMessage("Không tìm thấy user profile");
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
    public void getUserBasicInfo(GetUserBasicInfoRequest request, StreamObserver<GetUserBasicInfoResponse> responseObserver) {
        try {
            log.info("gRPC: Lấy user basic info với ID: {}", request.getUserId());
            
            Long userId = Long.parseLong(request.getUserId());
            Optional<User> userOpt = userRepository.findById(userId);
            
            GetUserBasicInfoResponse.Builder responseBuilder = GetUserBasicInfoResponse.newBuilder()
                    .setUserId(request.getUserId());
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                responseBuilder
                    .setEmail(user.getEmail())
                    .setRole(user.getSystemRole().name())
                    .setIsActive(user.getIsActive());
            } else {
                responseBuilder.setErrorMessage("Không tìm thấy user");
            }
            
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            log.error("Lỗi khi lấy user basic info qua gRPC", e);
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
            Optional<User> userOpt = userRepository.findById(userId);
            
            boolean isActive = userOpt.map(User::getIsActive).orElse(false);
            
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
