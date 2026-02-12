package com.chatbot.core.identity.grpc.identity;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.58.0)",
    comments = "Source: src/main/resources/proto/identity-service.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class IdentityServiceGrpc {

  private IdentityServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "com.chatbot.core.identity.grpc.identity.IdentityService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.ValidateTokenRequest,
      com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.ValidateTokenResponse> getValidateTokenMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ValidateToken",
      requestType = com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.ValidateTokenRequest.class,
      responseType = com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.ValidateTokenResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.ValidateTokenRequest,
      com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.ValidateTokenResponse> getValidateTokenMethod() {
    io.grpc.MethodDescriptor<com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.ValidateTokenRequest, com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.ValidateTokenResponse> getValidateTokenMethod;
    if ((getValidateTokenMethod = IdentityServiceGrpc.getValidateTokenMethod) == null) {
      synchronized (IdentityServiceGrpc.class) {
        if ((getValidateTokenMethod = IdentityServiceGrpc.getValidateTokenMethod) == null) {
          IdentityServiceGrpc.getValidateTokenMethod = getValidateTokenMethod =
              io.grpc.MethodDescriptor.<com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.ValidateTokenRequest, com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.ValidateTokenResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ValidateToken"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.ValidateTokenRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.ValidateTokenResponse.getDefaultInstance()))
              .setSchemaDescriptor(new IdentityServiceMethodDescriptorSupplier("ValidateToken"))
              .build();
        }
      }
    }
    return getValidateTokenMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.GetUserRequest,
      com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.GetUserResponse> getGetUserProfileMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetUserProfile",
      requestType = com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.GetUserRequest.class,
      responseType = com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.GetUserResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.GetUserRequest,
      com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.GetUserResponse> getGetUserProfileMethod() {
    io.grpc.MethodDescriptor<com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.GetUserRequest, com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.GetUserResponse> getGetUserProfileMethod;
    if ((getGetUserProfileMethod = IdentityServiceGrpc.getGetUserProfileMethod) == null) {
      synchronized (IdentityServiceGrpc.class) {
        if ((getGetUserProfileMethod = IdentityServiceGrpc.getGetUserProfileMethod) == null) {
          IdentityServiceGrpc.getGetUserProfileMethod = getGetUserProfileMethod =
              io.grpc.MethodDescriptor.<com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.GetUserRequest, com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.GetUserResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetUserProfile"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.GetUserRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.GetUserResponse.getDefaultInstance()))
              .setSchemaDescriptor(new IdentityServiceMethodDescriptorSupplier("GetUserProfile"))
              .build();
        }
      }
    }
    return getGetUserProfileMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.ValidateUserRequest,
      com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.ValidateUserResponse> getValidateUserMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ValidateUser",
      requestType = com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.ValidateUserRequest.class,
      responseType = com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.ValidateUserResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.ValidateUserRequest,
      com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.ValidateUserResponse> getValidateUserMethod() {
    io.grpc.MethodDescriptor<com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.ValidateUserRequest, com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.ValidateUserResponse> getValidateUserMethod;
    if ((getValidateUserMethod = IdentityServiceGrpc.getValidateUserMethod) == null) {
      synchronized (IdentityServiceGrpc.class) {
        if ((getValidateUserMethod = IdentityServiceGrpc.getValidateUserMethod) == null) {
          IdentityServiceGrpc.getValidateUserMethod = getValidateUserMethod =
              io.grpc.MethodDescriptor.<com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.ValidateUserRequest, com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.ValidateUserResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ValidateUser"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.ValidateUserRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.ValidateUserResponse.getDefaultInstance()))
              .setSchemaDescriptor(new IdentityServiceMethodDescriptorSupplier("ValidateUser"))
              .build();
        }
      }
    }
    return getValidateUserMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.GetUserRoleRequest,
      com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.GetUserRoleResponse> getGetUserRoleMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetUserRole",
      requestType = com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.GetUserRoleRequest.class,
      responseType = com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.GetUserRoleResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.GetUserRoleRequest,
      com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.GetUserRoleResponse> getGetUserRoleMethod() {
    io.grpc.MethodDescriptor<com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.GetUserRoleRequest, com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.GetUserRoleResponse> getGetUserRoleMethod;
    if ((getGetUserRoleMethod = IdentityServiceGrpc.getGetUserRoleMethod) == null) {
      synchronized (IdentityServiceGrpc.class) {
        if ((getGetUserRoleMethod = IdentityServiceGrpc.getGetUserRoleMethod) == null) {
          IdentityServiceGrpc.getGetUserRoleMethod = getGetUserRoleMethod =
              io.grpc.MethodDescriptor.<com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.GetUserRoleRequest, com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.GetUserRoleResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetUserRole"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.GetUserRoleRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.GetUserRoleResponse.getDefaultInstance()))
              .setSchemaDescriptor(new IdentityServiceMethodDescriptorSupplier("GetUserRole"))
              .build();
        }
      }
    }
    return getGetUserRoleMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.IsUserActiveRequest,
      com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.IsUserActiveResponse> getIsUserActiveMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "IsUserActive",
      requestType = com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.IsUserActiveRequest.class,
      responseType = com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.IsUserActiveResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.IsUserActiveRequest,
      com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.IsUserActiveResponse> getIsUserActiveMethod() {
    io.grpc.MethodDescriptor<com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.IsUserActiveRequest, com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.IsUserActiveResponse> getIsUserActiveMethod;
    if ((getIsUserActiveMethod = IdentityServiceGrpc.getIsUserActiveMethod) == null) {
      synchronized (IdentityServiceGrpc.class) {
        if ((getIsUserActiveMethod = IdentityServiceGrpc.getIsUserActiveMethod) == null) {
          IdentityServiceGrpc.getIsUserActiveMethod = getIsUserActiveMethod =
              io.grpc.MethodDescriptor.<com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.IsUserActiveRequest, com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.IsUserActiveResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "IsUserActive"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.IsUserActiveRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.IsUserActiveResponse.getDefaultInstance()))
              .setSchemaDescriptor(new IdentityServiceMethodDescriptorSupplier("IsUserActive"))
              .build();
        }
      }
    }
    return getIsUserActiveMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static IdentityServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<IdentityServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<IdentityServiceStub>() {
        @java.lang.Override
        public IdentityServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new IdentityServiceStub(channel, callOptions);
        }
      };
    return IdentityServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static IdentityServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<IdentityServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<IdentityServiceBlockingStub>() {
        @java.lang.Override
        public IdentityServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new IdentityServiceBlockingStub(channel, callOptions);
        }
      };
    return IdentityServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static IdentityServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<IdentityServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<IdentityServiceFutureStub>() {
        @java.lang.Override
        public IdentityServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new IdentityServiceFutureStub(channel, callOptions);
        }
      };
    return IdentityServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     * <pre>
     * Validate JWT token for inter-hub communication
     * </pre>
     */
    default void validateToken(com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.ValidateTokenRequest request,
        io.grpc.stub.StreamObserver<com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.ValidateTokenResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getValidateTokenMethod(), responseObserver);
    }

    /**
     * <pre>
     * Get user profile information
     * </pre>
     */
    default void getUserProfile(com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.GetUserRequest request,
        io.grpc.stub.StreamObserver<com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.GetUserResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetUserProfileMethod(), responseObserver);
    }

    /**
     * <pre>
     * Validate user existence and status
     * </pre>
     */
    default void validateUser(com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.ValidateUserRequest request,
        io.grpc.stub.StreamObserver<com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.ValidateUserResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getValidateUserMethod(), responseObserver);
    }

    /**
     * <pre>
     * Get user role for authorization
     * </pre>
     */
    default void getUserRole(com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.GetUserRoleRequest request,
        io.grpc.stub.StreamObserver<com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.GetUserRoleResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetUserRoleMethod(), responseObserver);
    }

    /**
     * <pre>
     * Check if user is active
     * </pre>
     */
    default void isUserActive(com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.IsUserActiveRequest request,
        io.grpc.stub.StreamObserver<com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.IsUserActiveResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getIsUserActiveMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service IdentityService.
   */
  public static abstract class IdentityServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return IdentityServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service IdentityService.
   */
  public static final class IdentityServiceStub
      extends io.grpc.stub.AbstractAsyncStub<IdentityServiceStub> {
    private IdentityServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected IdentityServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new IdentityServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * Validate JWT token for inter-hub communication
     * </pre>
     */
    public void validateToken(com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.ValidateTokenRequest request,
        io.grpc.stub.StreamObserver<com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.ValidateTokenResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getValidateTokenMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Get user profile information
     * </pre>
     */
    public void getUserProfile(com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.GetUserRequest request,
        io.grpc.stub.StreamObserver<com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.GetUserResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetUserProfileMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Validate user existence and status
     * </pre>
     */
    public void validateUser(com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.ValidateUserRequest request,
        io.grpc.stub.StreamObserver<com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.ValidateUserResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getValidateUserMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Get user role for authorization
     * </pre>
     */
    public void getUserRole(com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.GetUserRoleRequest request,
        io.grpc.stub.StreamObserver<com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.GetUserRoleResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetUserRoleMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Check if user is active
     * </pre>
     */
    public void isUserActive(com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.IsUserActiveRequest request,
        io.grpc.stub.StreamObserver<com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.IsUserActiveResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getIsUserActiveMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service IdentityService.
   */
  public static final class IdentityServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<IdentityServiceBlockingStub> {
    private IdentityServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected IdentityServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new IdentityServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Validate JWT token for inter-hub communication
     * </pre>
     */
    public com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.ValidateTokenResponse validateToken(com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.ValidateTokenRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getValidateTokenMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Get user profile information
     * </pre>
     */
    public com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.GetUserResponse getUserProfile(com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.GetUserRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetUserProfileMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Validate user existence and status
     * </pre>
     */
    public com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.ValidateUserResponse validateUser(com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.ValidateUserRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getValidateUserMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Get user role for authorization
     * </pre>
     */
    public com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.GetUserRoleResponse getUserRole(com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.GetUserRoleRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetUserRoleMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Check if user is active
     * </pre>
     */
    public com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.IsUserActiveResponse isUserActive(com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.IsUserActiveRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getIsUserActiveMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service IdentityService.
   */
  public static final class IdentityServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<IdentityServiceFutureStub> {
    private IdentityServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected IdentityServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new IdentityServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Validate JWT token for inter-hub communication
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.ValidateTokenResponse> validateToken(
        com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.ValidateTokenRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getValidateTokenMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Get user profile information
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.GetUserResponse> getUserProfile(
        com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.GetUserRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetUserProfileMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Validate user existence and status
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.ValidateUserResponse> validateUser(
        com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.ValidateUserRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getValidateUserMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Get user role for authorization
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.GetUserRoleResponse> getUserRole(
        com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.GetUserRoleRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetUserRoleMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Check if user is active
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.IsUserActiveResponse> isUserActive(
        com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.IsUserActiveRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getIsUserActiveMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_VALIDATE_TOKEN = 0;
  private static final int METHODID_GET_USER_PROFILE = 1;
  private static final int METHODID_VALIDATE_USER = 2;
  private static final int METHODID_GET_USER_ROLE = 3;
  private static final int METHODID_IS_USER_ACTIVE = 4;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_VALIDATE_TOKEN:
          serviceImpl.validateToken((com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.ValidateTokenRequest) request,
              (io.grpc.stub.StreamObserver<com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.ValidateTokenResponse>) responseObserver);
          break;
        case METHODID_GET_USER_PROFILE:
          serviceImpl.getUserProfile((com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.GetUserRequest) request,
              (io.grpc.stub.StreamObserver<com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.GetUserResponse>) responseObserver);
          break;
        case METHODID_VALIDATE_USER:
          serviceImpl.validateUser((com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.ValidateUserRequest) request,
              (io.grpc.stub.StreamObserver<com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.ValidateUserResponse>) responseObserver);
          break;
        case METHODID_GET_USER_ROLE:
          serviceImpl.getUserRole((com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.GetUserRoleRequest) request,
              (io.grpc.stub.StreamObserver<com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.GetUserRoleResponse>) responseObserver);
          break;
        case METHODID_IS_USER_ACTIVE:
          serviceImpl.isUserActive((com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.IsUserActiveRequest) request,
              (io.grpc.stub.StreamObserver<com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.IsUserActiveResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getValidateTokenMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.ValidateTokenRequest,
              com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.ValidateTokenResponse>(
                service, METHODID_VALIDATE_TOKEN)))
        .addMethod(
          getGetUserProfileMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.GetUserRequest,
              com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.GetUserResponse>(
                service, METHODID_GET_USER_PROFILE)))
        .addMethod(
          getValidateUserMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.ValidateUserRequest,
              com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.ValidateUserResponse>(
                service, METHODID_VALIDATE_USER)))
        .addMethod(
          getGetUserRoleMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.GetUserRoleRequest,
              com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.GetUserRoleResponse>(
                service, METHODID_GET_USER_ROLE)))
        .addMethod(
          getIsUserActiveMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.IsUserActiveRequest,
              com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.IsUserActiveResponse>(
                service, METHODID_IS_USER_ACTIVE)))
        .build();
  }

  private static abstract class IdentityServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    IdentityServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.chatbot.core.identity.grpc.identity.IdentityServiceOuterClass.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("IdentityService");
    }
  }

  private static final class IdentityServiceFileDescriptorSupplier
      extends IdentityServiceBaseDescriptorSupplier {
    IdentityServiceFileDescriptorSupplier() {}
  }

  private static final class IdentityServiceMethodDescriptorSupplier
      extends IdentityServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    IdentityServiceMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (IdentityServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new IdentityServiceFileDescriptorSupplier())
              .addMethod(getValidateTokenMethod())
              .addMethod(getGetUserProfileMethod())
              .addMethod(getValidateUserMethod())
              .addMethod(getGetUserRoleMethod())
              .addMethod(getIsUserActiveMethod())
              .build();
        }
      }
    }
    return result;
  }
}
