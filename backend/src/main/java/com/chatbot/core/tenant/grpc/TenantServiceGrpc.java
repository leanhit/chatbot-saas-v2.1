package com.chatbot.core.tenant.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * Tenant Service Definition
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.58.0)",
    comments = "Source: tenant-service.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class TenantServiceGrpc {

  private TenantServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "tenant.TenantService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.chatbot.core.tenant.grpc.TenantServiceProto.CreateTenantRequest,
      com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse> getCreateTenantMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CreateTenant",
      requestType = com.chatbot.core.tenant.grpc.TenantServiceProto.CreateTenantRequest.class,
      responseType = com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.chatbot.core.tenant.grpc.TenantServiceProto.CreateTenantRequest,
      com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse> getCreateTenantMethod() {
    io.grpc.MethodDescriptor<com.chatbot.core.tenant.grpc.TenantServiceProto.CreateTenantRequest, com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse> getCreateTenantMethod;
    if ((getCreateTenantMethod = TenantServiceGrpc.getCreateTenantMethod) == null) {
      synchronized (TenantServiceGrpc.class) {
        if ((getCreateTenantMethod = TenantServiceGrpc.getCreateTenantMethod) == null) {
          TenantServiceGrpc.getCreateTenantMethod = getCreateTenantMethod =
              io.grpc.MethodDescriptor.<com.chatbot.core.tenant.grpc.TenantServiceProto.CreateTenantRequest, com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "CreateTenant"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.chatbot.core.tenant.grpc.TenantServiceProto.CreateTenantRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse.getDefaultInstance()))
              .setSchemaDescriptor(new TenantServiceMethodDescriptorSupplier("CreateTenant"))
              .build();
        }
      }
    }
    return getCreateTenantMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.chatbot.core.tenant.grpc.TenantServiceProto.GetTenantRequest,
      com.chatbot.core.tenant.grpc.TenantServiceProto.TenantDetailResponse> getGetTenantMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetTenant",
      requestType = com.chatbot.core.tenant.grpc.TenantServiceProto.GetTenantRequest.class,
      responseType = com.chatbot.core.tenant.grpc.TenantServiceProto.TenantDetailResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.chatbot.core.tenant.grpc.TenantServiceProto.GetTenantRequest,
      com.chatbot.core.tenant.grpc.TenantServiceProto.TenantDetailResponse> getGetTenantMethod() {
    io.grpc.MethodDescriptor<com.chatbot.core.tenant.grpc.TenantServiceProto.GetTenantRequest, com.chatbot.core.tenant.grpc.TenantServiceProto.TenantDetailResponse> getGetTenantMethod;
    if ((getGetTenantMethod = TenantServiceGrpc.getGetTenantMethod) == null) {
      synchronized (TenantServiceGrpc.class) {
        if ((getGetTenantMethod = TenantServiceGrpc.getGetTenantMethod) == null) {
          TenantServiceGrpc.getGetTenantMethod = getGetTenantMethod =
              io.grpc.MethodDescriptor.<com.chatbot.core.tenant.grpc.TenantServiceProto.GetTenantRequest, com.chatbot.core.tenant.grpc.TenantServiceProto.TenantDetailResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetTenant"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.chatbot.core.tenant.grpc.TenantServiceProto.GetTenantRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.chatbot.core.tenant.grpc.TenantServiceProto.TenantDetailResponse.getDefaultInstance()))
              .setSchemaDescriptor(new TenantServiceMethodDescriptorSupplier("GetTenant"))
              .build();
        }
      }
    }
    return getGetTenantMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.chatbot.core.tenant.grpc.TenantServiceProto.UpdateTenantRequest,
      com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse> getUpdateTenantMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "UpdateTenant",
      requestType = com.chatbot.core.tenant.grpc.TenantServiceProto.UpdateTenantRequest.class,
      responseType = com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.chatbot.core.tenant.grpc.TenantServiceProto.UpdateTenantRequest,
      com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse> getUpdateTenantMethod() {
    io.grpc.MethodDescriptor<com.chatbot.core.tenant.grpc.TenantServiceProto.UpdateTenantRequest, com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse> getUpdateTenantMethod;
    if ((getUpdateTenantMethod = TenantServiceGrpc.getUpdateTenantMethod) == null) {
      synchronized (TenantServiceGrpc.class) {
        if ((getUpdateTenantMethod = TenantServiceGrpc.getUpdateTenantMethod) == null) {
          TenantServiceGrpc.getUpdateTenantMethod = getUpdateTenantMethod =
              io.grpc.MethodDescriptor.<com.chatbot.core.tenant.grpc.TenantServiceProto.UpdateTenantRequest, com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "UpdateTenant"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.chatbot.core.tenant.grpc.TenantServiceProto.UpdateTenantRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse.getDefaultInstance()))
              .setSchemaDescriptor(new TenantServiceMethodDescriptorSupplier("UpdateTenant"))
              .build();
        }
      }
    }
    return getUpdateTenantMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.chatbot.core.tenant.grpc.TenantServiceProto.DeleteTenantRequest,
      com.chatbot.core.tenant.grpc.TenantServiceProto.DeleteTenantResponse> getDeleteTenantMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DeleteTenant",
      requestType = com.chatbot.core.tenant.grpc.TenantServiceProto.DeleteTenantRequest.class,
      responseType = com.chatbot.core.tenant.grpc.TenantServiceProto.DeleteTenantResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.chatbot.core.tenant.grpc.TenantServiceProto.DeleteTenantRequest,
      com.chatbot.core.tenant.grpc.TenantServiceProto.DeleteTenantResponse> getDeleteTenantMethod() {
    io.grpc.MethodDescriptor<com.chatbot.core.tenant.grpc.TenantServiceProto.DeleteTenantRequest, com.chatbot.core.tenant.grpc.TenantServiceProto.DeleteTenantResponse> getDeleteTenantMethod;
    if ((getDeleteTenantMethod = TenantServiceGrpc.getDeleteTenantMethod) == null) {
      synchronized (TenantServiceGrpc.class) {
        if ((getDeleteTenantMethod = TenantServiceGrpc.getDeleteTenantMethod) == null) {
          TenantServiceGrpc.getDeleteTenantMethod = getDeleteTenantMethod =
              io.grpc.MethodDescriptor.<com.chatbot.core.tenant.grpc.TenantServiceProto.DeleteTenantRequest, com.chatbot.core.tenant.grpc.TenantServiceProto.DeleteTenantResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "DeleteTenant"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.chatbot.core.tenant.grpc.TenantServiceProto.DeleteTenantRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.chatbot.core.tenant.grpc.TenantServiceProto.DeleteTenantResponse.getDefaultInstance()))
              .setSchemaDescriptor(new TenantServiceMethodDescriptorSupplier("DeleteTenant"))
              .build();
        }
      }
    }
    return getDeleteTenantMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.chatbot.core.tenant.grpc.TenantServiceProto.ListTenantsRequest,
      com.chatbot.core.tenant.grpc.TenantServiceProto.ListTenantsResponse> getListTenantsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ListTenants",
      requestType = com.chatbot.core.tenant.grpc.TenantServiceProto.ListTenantsRequest.class,
      responseType = com.chatbot.core.tenant.grpc.TenantServiceProto.ListTenantsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.chatbot.core.tenant.grpc.TenantServiceProto.ListTenantsRequest,
      com.chatbot.core.tenant.grpc.TenantServiceProto.ListTenantsResponse> getListTenantsMethod() {
    io.grpc.MethodDescriptor<com.chatbot.core.tenant.grpc.TenantServiceProto.ListTenantsRequest, com.chatbot.core.tenant.grpc.TenantServiceProto.ListTenantsResponse> getListTenantsMethod;
    if ((getListTenantsMethod = TenantServiceGrpc.getListTenantsMethod) == null) {
      synchronized (TenantServiceGrpc.class) {
        if ((getListTenantsMethod = TenantServiceGrpc.getListTenantsMethod) == null) {
          TenantServiceGrpc.getListTenantsMethod = getListTenantsMethod =
              io.grpc.MethodDescriptor.<com.chatbot.core.tenant.grpc.TenantServiceProto.ListTenantsRequest, com.chatbot.core.tenant.grpc.TenantServiceProto.ListTenantsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ListTenants"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.chatbot.core.tenant.grpc.TenantServiceProto.ListTenantsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.chatbot.core.tenant.grpc.TenantServiceProto.ListTenantsResponse.getDefaultInstance()))
              .setSchemaDescriptor(new TenantServiceMethodDescriptorSupplier("ListTenants"))
              .build();
        }
      }
    }
    return getListTenantsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.chatbot.core.tenant.grpc.TenantServiceProto.SearchTenantsRequest,
      com.chatbot.core.tenant.grpc.TenantServiceProto.SearchTenantsResponse> getSearchTenantsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SearchTenants",
      requestType = com.chatbot.core.tenant.grpc.TenantServiceProto.SearchTenantsRequest.class,
      responseType = com.chatbot.core.tenant.grpc.TenantServiceProto.SearchTenantsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.chatbot.core.tenant.grpc.TenantServiceProto.SearchTenantsRequest,
      com.chatbot.core.tenant.grpc.TenantServiceProto.SearchTenantsResponse> getSearchTenantsMethod() {
    io.grpc.MethodDescriptor<com.chatbot.core.tenant.grpc.TenantServiceProto.SearchTenantsRequest, com.chatbot.core.tenant.grpc.TenantServiceProto.SearchTenantsResponse> getSearchTenantsMethod;
    if ((getSearchTenantsMethod = TenantServiceGrpc.getSearchTenantsMethod) == null) {
      synchronized (TenantServiceGrpc.class) {
        if ((getSearchTenantsMethod = TenantServiceGrpc.getSearchTenantsMethod) == null) {
          TenantServiceGrpc.getSearchTenantsMethod = getSearchTenantsMethod =
              io.grpc.MethodDescriptor.<com.chatbot.core.tenant.grpc.TenantServiceProto.SearchTenantsRequest, com.chatbot.core.tenant.grpc.TenantServiceProto.SearchTenantsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SearchTenants"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.chatbot.core.tenant.grpc.TenantServiceProto.SearchTenantsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.chatbot.core.tenant.grpc.TenantServiceProto.SearchTenantsResponse.getDefaultInstance()))
              .setSchemaDescriptor(new TenantServiceMethodDescriptorSupplier("SearchTenants"))
              .build();
        }
      }
    }
    return getSearchTenantsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.chatbot.core.tenant.grpc.TenantServiceProto.ValidateTenantRequest,
      com.chatbot.core.tenant.grpc.TenantServiceProto.ValidateTenantResponse> getValidateTenantMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ValidateTenant",
      requestType = com.chatbot.core.tenant.grpc.TenantServiceProto.ValidateTenantRequest.class,
      responseType = com.chatbot.core.tenant.grpc.TenantServiceProto.ValidateTenantResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.chatbot.core.tenant.grpc.TenantServiceProto.ValidateTenantRequest,
      com.chatbot.core.tenant.grpc.TenantServiceProto.ValidateTenantResponse> getValidateTenantMethod() {
    io.grpc.MethodDescriptor<com.chatbot.core.tenant.grpc.TenantServiceProto.ValidateTenantRequest, com.chatbot.core.tenant.grpc.TenantServiceProto.ValidateTenantResponse> getValidateTenantMethod;
    if ((getValidateTenantMethod = TenantServiceGrpc.getValidateTenantMethod) == null) {
      synchronized (TenantServiceGrpc.class) {
        if ((getValidateTenantMethod = TenantServiceGrpc.getValidateTenantMethod) == null) {
          TenantServiceGrpc.getValidateTenantMethod = getValidateTenantMethod =
              io.grpc.MethodDescriptor.<com.chatbot.core.tenant.grpc.TenantServiceProto.ValidateTenantRequest, com.chatbot.core.tenant.grpc.TenantServiceProto.ValidateTenantResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ValidateTenant"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.chatbot.core.tenant.grpc.TenantServiceProto.ValidateTenantRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.chatbot.core.tenant.grpc.TenantServiceProto.ValidateTenantResponse.getDefaultInstance()))
              .setSchemaDescriptor(new TenantServiceMethodDescriptorSupplier("ValidateTenant"))
              .build();
        }
      }
    }
    return getValidateTenantMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.chatbot.core.tenant.grpc.TenantServiceProto.CheckTenantExistsRequest,
      com.chatbot.core.tenant.grpc.TenantServiceProto.CheckTenantExistsResponse> getCheckTenantExistsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CheckTenantExists",
      requestType = com.chatbot.core.tenant.grpc.TenantServiceProto.CheckTenantExistsRequest.class,
      responseType = com.chatbot.core.tenant.grpc.TenantServiceProto.CheckTenantExistsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.chatbot.core.tenant.grpc.TenantServiceProto.CheckTenantExistsRequest,
      com.chatbot.core.tenant.grpc.TenantServiceProto.CheckTenantExistsResponse> getCheckTenantExistsMethod() {
    io.grpc.MethodDescriptor<com.chatbot.core.tenant.grpc.TenantServiceProto.CheckTenantExistsRequest, com.chatbot.core.tenant.grpc.TenantServiceProto.CheckTenantExistsResponse> getCheckTenantExistsMethod;
    if ((getCheckTenantExistsMethod = TenantServiceGrpc.getCheckTenantExistsMethod) == null) {
      synchronized (TenantServiceGrpc.class) {
        if ((getCheckTenantExistsMethod = TenantServiceGrpc.getCheckTenantExistsMethod) == null) {
          TenantServiceGrpc.getCheckTenantExistsMethod = getCheckTenantExistsMethod =
              io.grpc.MethodDescriptor.<com.chatbot.core.tenant.grpc.TenantServiceProto.CheckTenantExistsRequest, com.chatbot.core.tenant.grpc.TenantServiceProto.CheckTenantExistsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "CheckTenantExists"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.chatbot.core.tenant.grpc.TenantServiceProto.CheckTenantExistsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.chatbot.core.tenant.grpc.TenantServiceProto.CheckTenantExistsResponse.getDefaultInstance()))
              .setSchemaDescriptor(new TenantServiceMethodDescriptorSupplier("CheckTenantExists"))
              .build();
        }
      }
    }
    return getCheckTenantExistsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.chatbot.core.tenant.grpc.TenantServiceProto.ActivateTenantRequest,
      com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse> getActivateTenantMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ActivateTenant",
      requestType = com.chatbot.core.tenant.grpc.TenantServiceProto.ActivateTenantRequest.class,
      responseType = com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.chatbot.core.tenant.grpc.TenantServiceProto.ActivateTenantRequest,
      com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse> getActivateTenantMethod() {
    io.grpc.MethodDescriptor<com.chatbot.core.tenant.grpc.TenantServiceProto.ActivateTenantRequest, com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse> getActivateTenantMethod;
    if ((getActivateTenantMethod = TenantServiceGrpc.getActivateTenantMethod) == null) {
      synchronized (TenantServiceGrpc.class) {
        if ((getActivateTenantMethod = TenantServiceGrpc.getActivateTenantMethod) == null) {
          TenantServiceGrpc.getActivateTenantMethod = getActivateTenantMethod =
              io.grpc.MethodDescriptor.<com.chatbot.core.tenant.grpc.TenantServiceProto.ActivateTenantRequest, com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ActivateTenant"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.chatbot.core.tenant.grpc.TenantServiceProto.ActivateTenantRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse.getDefaultInstance()))
              .setSchemaDescriptor(new TenantServiceMethodDescriptorSupplier("ActivateTenant"))
              .build();
        }
      }
    }
    return getActivateTenantMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.chatbot.core.tenant.grpc.TenantServiceProto.SuspendTenantRequest,
      com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse> getSuspendTenantMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SuspendTenant",
      requestType = com.chatbot.core.tenant.grpc.TenantServiceProto.SuspendTenantRequest.class,
      responseType = com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.chatbot.core.tenant.grpc.TenantServiceProto.SuspendTenantRequest,
      com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse> getSuspendTenantMethod() {
    io.grpc.MethodDescriptor<com.chatbot.core.tenant.grpc.TenantServiceProto.SuspendTenantRequest, com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse> getSuspendTenantMethod;
    if ((getSuspendTenantMethod = TenantServiceGrpc.getSuspendTenantMethod) == null) {
      synchronized (TenantServiceGrpc.class) {
        if ((getSuspendTenantMethod = TenantServiceGrpc.getSuspendTenantMethod) == null) {
          TenantServiceGrpc.getSuspendTenantMethod = getSuspendTenantMethod =
              io.grpc.MethodDescriptor.<com.chatbot.core.tenant.grpc.TenantServiceProto.SuspendTenantRequest, com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SuspendTenant"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.chatbot.core.tenant.grpc.TenantServiceProto.SuspendTenantRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse.getDefaultInstance()))
              .setSchemaDescriptor(new TenantServiceMethodDescriptorSupplier("SuspendTenant"))
              .build();
        }
      }
    }
    return getSuspendTenantMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.chatbot.core.tenant.grpc.TenantServiceProto.GetTenantStatusRequest,
      com.chatbot.core.tenant.grpc.TenantServiceProto.TenantStatusResponse> getGetTenantStatusMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetTenantStatus",
      requestType = com.chatbot.core.tenant.grpc.TenantServiceProto.GetTenantStatusRequest.class,
      responseType = com.chatbot.core.tenant.grpc.TenantServiceProto.TenantStatusResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.chatbot.core.tenant.grpc.TenantServiceProto.GetTenantStatusRequest,
      com.chatbot.core.tenant.grpc.TenantServiceProto.TenantStatusResponse> getGetTenantStatusMethod() {
    io.grpc.MethodDescriptor<com.chatbot.core.tenant.grpc.TenantServiceProto.GetTenantStatusRequest, com.chatbot.core.tenant.grpc.TenantServiceProto.TenantStatusResponse> getGetTenantStatusMethod;
    if ((getGetTenantStatusMethod = TenantServiceGrpc.getGetTenantStatusMethod) == null) {
      synchronized (TenantServiceGrpc.class) {
        if ((getGetTenantStatusMethod = TenantServiceGrpc.getGetTenantStatusMethod) == null) {
          TenantServiceGrpc.getGetTenantStatusMethod = getGetTenantStatusMethod =
              io.grpc.MethodDescriptor.<com.chatbot.core.tenant.grpc.TenantServiceProto.GetTenantStatusRequest, com.chatbot.core.tenant.grpc.TenantServiceProto.TenantStatusResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetTenantStatus"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.chatbot.core.tenant.grpc.TenantServiceProto.GetTenantStatusRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.chatbot.core.tenant.grpc.TenantServiceProto.TenantStatusResponse.getDefaultInstance()))
              .setSchemaDescriptor(new TenantServiceMethodDescriptorSupplier("GetTenantStatus"))
              .build();
        }
      }
    }
    return getGetTenantStatusMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static TenantServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<TenantServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<TenantServiceStub>() {
        @java.lang.Override
        public TenantServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new TenantServiceStub(channel, callOptions);
        }
      };
    return TenantServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static TenantServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<TenantServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<TenantServiceBlockingStub>() {
        @java.lang.Override
        public TenantServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new TenantServiceBlockingStub(channel, callOptions);
        }
      };
    return TenantServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static TenantServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<TenantServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<TenantServiceFutureStub>() {
        @java.lang.Override
        public TenantServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new TenantServiceFutureStub(channel, callOptions);
        }
      };
    return TenantServiceFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * Tenant Service Definition
   * </pre>
   */
  public interface AsyncService {

    /**
     * <pre>
     * Tenant Management
     * </pre>
     */
    default void createTenant(com.chatbot.core.tenant.grpc.TenantServiceProto.CreateTenantRequest request,
        io.grpc.stub.StreamObserver<com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getCreateTenantMethod(), responseObserver);
    }

    /**
     */
    default void getTenant(com.chatbot.core.tenant.grpc.TenantServiceProto.GetTenantRequest request,
        io.grpc.stub.StreamObserver<com.chatbot.core.tenant.grpc.TenantServiceProto.TenantDetailResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetTenantMethod(), responseObserver);
    }

    /**
     */
    default void updateTenant(com.chatbot.core.tenant.grpc.TenantServiceProto.UpdateTenantRequest request,
        io.grpc.stub.StreamObserver<com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getUpdateTenantMethod(), responseObserver);
    }

    /**
     */
    default void deleteTenant(com.chatbot.core.tenant.grpc.TenantServiceProto.DeleteTenantRequest request,
        io.grpc.stub.StreamObserver<com.chatbot.core.tenant.grpc.TenantServiceProto.DeleteTenantResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getDeleteTenantMethod(), responseObserver);
    }

    /**
     */
    default void listTenants(com.chatbot.core.tenant.grpc.TenantServiceProto.ListTenantsRequest request,
        io.grpc.stub.StreamObserver<com.chatbot.core.tenant.grpc.TenantServiceProto.ListTenantsResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getListTenantsMethod(), responseObserver);
    }

    /**
     */
    default void searchTenants(com.chatbot.core.tenant.grpc.TenantServiceProto.SearchTenantsRequest request,
        io.grpc.stub.StreamObserver<com.chatbot.core.tenant.grpc.TenantServiceProto.SearchTenantsResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSearchTenantsMethod(), responseObserver);
    }

    /**
     * <pre>
     * Tenant Validation
     * </pre>
     */
    default void validateTenant(com.chatbot.core.tenant.grpc.TenantServiceProto.ValidateTenantRequest request,
        io.grpc.stub.StreamObserver<com.chatbot.core.tenant.grpc.TenantServiceProto.ValidateTenantResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getValidateTenantMethod(), responseObserver);
    }

    /**
     */
    default void checkTenantExists(com.chatbot.core.tenant.grpc.TenantServiceProto.CheckTenantExistsRequest request,
        io.grpc.stub.StreamObserver<com.chatbot.core.tenant.grpc.TenantServiceProto.CheckTenantExistsResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getCheckTenantExistsMethod(), responseObserver);
    }

    /**
     * <pre>
     * Tenant Status Management
     * </pre>
     */
    default void activateTenant(com.chatbot.core.tenant.grpc.TenantServiceProto.ActivateTenantRequest request,
        io.grpc.stub.StreamObserver<com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getActivateTenantMethod(), responseObserver);
    }

    /**
     */
    default void suspendTenant(com.chatbot.core.tenant.grpc.TenantServiceProto.SuspendTenantRequest request,
        io.grpc.stub.StreamObserver<com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSuspendTenantMethod(), responseObserver);
    }

    /**
     */
    default void getTenantStatus(com.chatbot.core.tenant.grpc.TenantServiceProto.GetTenantStatusRequest request,
        io.grpc.stub.StreamObserver<com.chatbot.core.tenant.grpc.TenantServiceProto.TenantStatusResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetTenantStatusMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service TenantService.
   * <pre>
   * Tenant Service Definition
   * </pre>
   */
  public static abstract class TenantServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return TenantServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service TenantService.
   * <pre>
   * Tenant Service Definition
   * </pre>
   */
  public static final class TenantServiceStub
      extends io.grpc.stub.AbstractAsyncStub<TenantServiceStub> {
    private TenantServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected TenantServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new TenantServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * Tenant Management
     * </pre>
     */
    public void createTenant(com.chatbot.core.tenant.grpc.TenantServiceProto.CreateTenantRequest request,
        io.grpc.stub.StreamObserver<com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getCreateTenantMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getTenant(com.chatbot.core.tenant.grpc.TenantServiceProto.GetTenantRequest request,
        io.grpc.stub.StreamObserver<com.chatbot.core.tenant.grpc.TenantServiceProto.TenantDetailResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetTenantMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void updateTenant(com.chatbot.core.tenant.grpc.TenantServiceProto.UpdateTenantRequest request,
        io.grpc.stub.StreamObserver<com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getUpdateTenantMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void deleteTenant(com.chatbot.core.tenant.grpc.TenantServiceProto.DeleteTenantRequest request,
        io.grpc.stub.StreamObserver<com.chatbot.core.tenant.grpc.TenantServiceProto.DeleteTenantResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getDeleteTenantMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void listTenants(com.chatbot.core.tenant.grpc.TenantServiceProto.ListTenantsRequest request,
        io.grpc.stub.StreamObserver<com.chatbot.core.tenant.grpc.TenantServiceProto.ListTenantsResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getListTenantsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void searchTenants(com.chatbot.core.tenant.grpc.TenantServiceProto.SearchTenantsRequest request,
        io.grpc.stub.StreamObserver<com.chatbot.core.tenant.grpc.TenantServiceProto.SearchTenantsResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSearchTenantsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Tenant Validation
     * </pre>
     */
    public void validateTenant(com.chatbot.core.tenant.grpc.TenantServiceProto.ValidateTenantRequest request,
        io.grpc.stub.StreamObserver<com.chatbot.core.tenant.grpc.TenantServiceProto.ValidateTenantResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getValidateTenantMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void checkTenantExists(com.chatbot.core.tenant.grpc.TenantServiceProto.CheckTenantExistsRequest request,
        io.grpc.stub.StreamObserver<com.chatbot.core.tenant.grpc.TenantServiceProto.CheckTenantExistsResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getCheckTenantExistsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Tenant Status Management
     * </pre>
     */
    public void activateTenant(com.chatbot.core.tenant.grpc.TenantServiceProto.ActivateTenantRequest request,
        io.grpc.stub.StreamObserver<com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getActivateTenantMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void suspendTenant(com.chatbot.core.tenant.grpc.TenantServiceProto.SuspendTenantRequest request,
        io.grpc.stub.StreamObserver<com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSuspendTenantMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getTenantStatus(com.chatbot.core.tenant.grpc.TenantServiceProto.GetTenantStatusRequest request,
        io.grpc.stub.StreamObserver<com.chatbot.core.tenant.grpc.TenantServiceProto.TenantStatusResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetTenantStatusMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service TenantService.
   * <pre>
   * Tenant Service Definition
   * </pre>
   */
  public static final class TenantServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<TenantServiceBlockingStub> {
    private TenantServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected TenantServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new TenantServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Tenant Management
     * </pre>
     */
    public com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse createTenant(com.chatbot.core.tenant.grpc.TenantServiceProto.CreateTenantRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getCreateTenantMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.chatbot.core.tenant.grpc.TenantServiceProto.TenantDetailResponse getTenant(com.chatbot.core.tenant.grpc.TenantServiceProto.GetTenantRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetTenantMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse updateTenant(com.chatbot.core.tenant.grpc.TenantServiceProto.UpdateTenantRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getUpdateTenantMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.chatbot.core.tenant.grpc.TenantServiceProto.DeleteTenantResponse deleteTenant(com.chatbot.core.tenant.grpc.TenantServiceProto.DeleteTenantRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getDeleteTenantMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.chatbot.core.tenant.grpc.TenantServiceProto.ListTenantsResponse listTenants(com.chatbot.core.tenant.grpc.TenantServiceProto.ListTenantsRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getListTenantsMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.chatbot.core.tenant.grpc.TenantServiceProto.SearchTenantsResponse searchTenants(com.chatbot.core.tenant.grpc.TenantServiceProto.SearchTenantsRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSearchTenantsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Tenant Validation
     * </pre>
     */
    public com.chatbot.core.tenant.grpc.TenantServiceProto.ValidateTenantResponse validateTenant(com.chatbot.core.tenant.grpc.TenantServiceProto.ValidateTenantRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getValidateTenantMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.chatbot.core.tenant.grpc.TenantServiceProto.CheckTenantExistsResponse checkTenantExists(com.chatbot.core.tenant.grpc.TenantServiceProto.CheckTenantExistsRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getCheckTenantExistsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Tenant Status Management
     * </pre>
     */
    public com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse activateTenant(com.chatbot.core.tenant.grpc.TenantServiceProto.ActivateTenantRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getActivateTenantMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse suspendTenant(com.chatbot.core.tenant.grpc.TenantServiceProto.SuspendTenantRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSuspendTenantMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.chatbot.core.tenant.grpc.TenantServiceProto.TenantStatusResponse getTenantStatus(com.chatbot.core.tenant.grpc.TenantServiceProto.GetTenantStatusRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetTenantStatusMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service TenantService.
   * <pre>
   * Tenant Service Definition
   * </pre>
   */
  public static final class TenantServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<TenantServiceFutureStub> {
    private TenantServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected TenantServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new TenantServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Tenant Management
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse> createTenant(
        com.chatbot.core.tenant.grpc.TenantServiceProto.CreateTenantRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getCreateTenantMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.chatbot.core.tenant.grpc.TenantServiceProto.TenantDetailResponse> getTenant(
        com.chatbot.core.tenant.grpc.TenantServiceProto.GetTenantRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetTenantMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse> updateTenant(
        com.chatbot.core.tenant.grpc.TenantServiceProto.UpdateTenantRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getUpdateTenantMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.chatbot.core.tenant.grpc.TenantServiceProto.DeleteTenantResponse> deleteTenant(
        com.chatbot.core.tenant.grpc.TenantServiceProto.DeleteTenantRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getDeleteTenantMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.chatbot.core.tenant.grpc.TenantServiceProto.ListTenantsResponse> listTenants(
        com.chatbot.core.tenant.grpc.TenantServiceProto.ListTenantsRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getListTenantsMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.chatbot.core.tenant.grpc.TenantServiceProto.SearchTenantsResponse> searchTenants(
        com.chatbot.core.tenant.grpc.TenantServiceProto.SearchTenantsRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSearchTenantsMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Tenant Validation
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.chatbot.core.tenant.grpc.TenantServiceProto.ValidateTenantResponse> validateTenant(
        com.chatbot.core.tenant.grpc.TenantServiceProto.ValidateTenantRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getValidateTenantMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.chatbot.core.tenant.grpc.TenantServiceProto.CheckTenantExistsResponse> checkTenantExists(
        com.chatbot.core.tenant.grpc.TenantServiceProto.CheckTenantExistsRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getCheckTenantExistsMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Tenant Status Management
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse> activateTenant(
        com.chatbot.core.tenant.grpc.TenantServiceProto.ActivateTenantRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getActivateTenantMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse> suspendTenant(
        com.chatbot.core.tenant.grpc.TenantServiceProto.SuspendTenantRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSuspendTenantMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.chatbot.core.tenant.grpc.TenantServiceProto.TenantStatusResponse> getTenantStatus(
        com.chatbot.core.tenant.grpc.TenantServiceProto.GetTenantStatusRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetTenantStatusMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_CREATE_TENANT = 0;
  private static final int METHODID_GET_TENANT = 1;
  private static final int METHODID_UPDATE_TENANT = 2;
  private static final int METHODID_DELETE_TENANT = 3;
  private static final int METHODID_LIST_TENANTS = 4;
  private static final int METHODID_SEARCH_TENANTS = 5;
  private static final int METHODID_VALIDATE_TENANT = 6;
  private static final int METHODID_CHECK_TENANT_EXISTS = 7;
  private static final int METHODID_ACTIVATE_TENANT = 8;
  private static final int METHODID_SUSPEND_TENANT = 9;
  private static final int METHODID_GET_TENANT_STATUS = 10;

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
        case METHODID_CREATE_TENANT:
          serviceImpl.createTenant((com.chatbot.core.tenant.grpc.TenantServiceProto.CreateTenantRequest) request,
              (io.grpc.stub.StreamObserver<com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse>) responseObserver);
          break;
        case METHODID_GET_TENANT:
          serviceImpl.getTenant((com.chatbot.core.tenant.grpc.TenantServiceProto.GetTenantRequest) request,
              (io.grpc.stub.StreamObserver<com.chatbot.core.tenant.grpc.TenantServiceProto.TenantDetailResponse>) responseObserver);
          break;
        case METHODID_UPDATE_TENANT:
          serviceImpl.updateTenant((com.chatbot.core.tenant.grpc.TenantServiceProto.UpdateTenantRequest) request,
              (io.grpc.stub.StreamObserver<com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse>) responseObserver);
          break;
        case METHODID_DELETE_TENANT:
          serviceImpl.deleteTenant((com.chatbot.core.tenant.grpc.TenantServiceProto.DeleteTenantRequest) request,
              (io.grpc.stub.StreamObserver<com.chatbot.core.tenant.grpc.TenantServiceProto.DeleteTenantResponse>) responseObserver);
          break;
        case METHODID_LIST_TENANTS:
          serviceImpl.listTenants((com.chatbot.core.tenant.grpc.TenantServiceProto.ListTenantsRequest) request,
              (io.grpc.stub.StreamObserver<com.chatbot.core.tenant.grpc.TenantServiceProto.ListTenantsResponse>) responseObserver);
          break;
        case METHODID_SEARCH_TENANTS:
          serviceImpl.searchTenants((com.chatbot.core.tenant.grpc.TenantServiceProto.SearchTenantsRequest) request,
              (io.grpc.stub.StreamObserver<com.chatbot.core.tenant.grpc.TenantServiceProto.SearchTenantsResponse>) responseObserver);
          break;
        case METHODID_VALIDATE_TENANT:
          serviceImpl.validateTenant((com.chatbot.core.tenant.grpc.TenantServiceProto.ValidateTenantRequest) request,
              (io.grpc.stub.StreamObserver<com.chatbot.core.tenant.grpc.TenantServiceProto.ValidateTenantResponse>) responseObserver);
          break;
        case METHODID_CHECK_TENANT_EXISTS:
          serviceImpl.checkTenantExists((com.chatbot.core.tenant.grpc.TenantServiceProto.CheckTenantExistsRequest) request,
              (io.grpc.stub.StreamObserver<com.chatbot.core.tenant.grpc.TenantServiceProto.CheckTenantExistsResponse>) responseObserver);
          break;
        case METHODID_ACTIVATE_TENANT:
          serviceImpl.activateTenant((com.chatbot.core.tenant.grpc.TenantServiceProto.ActivateTenantRequest) request,
              (io.grpc.stub.StreamObserver<com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse>) responseObserver);
          break;
        case METHODID_SUSPEND_TENANT:
          serviceImpl.suspendTenant((com.chatbot.core.tenant.grpc.TenantServiceProto.SuspendTenantRequest) request,
              (io.grpc.stub.StreamObserver<com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse>) responseObserver);
          break;
        case METHODID_GET_TENANT_STATUS:
          serviceImpl.getTenantStatus((com.chatbot.core.tenant.grpc.TenantServiceProto.GetTenantStatusRequest) request,
              (io.grpc.stub.StreamObserver<com.chatbot.core.tenant.grpc.TenantServiceProto.TenantStatusResponse>) responseObserver);
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
          getCreateTenantMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.chatbot.core.tenant.grpc.TenantServiceProto.CreateTenantRequest,
              com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse>(
                service, METHODID_CREATE_TENANT)))
        .addMethod(
          getGetTenantMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.chatbot.core.tenant.grpc.TenantServiceProto.GetTenantRequest,
              com.chatbot.core.tenant.grpc.TenantServiceProto.TenantDetailResponse>(
                service, METHODID_GET_TENANT)))
        .addMethod(
          getUpdateTenantMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.chatbot.core.tenant.grpc.TenantServiceProto.UpdateTenantRequest,
              com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse>(
                service, METHODID_UPDATE_TENANT)))
        .addMethod(
          getDeleteTenantMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.chatbot.core.tenant.grpc.TenantServiceProto.DeleteTenantRequest,
              com.chatbot.core.tenant.grpc.TenantServiceProto.DeleteTenantResponse>(
                service, METHODID_DELETE_TENANT)))
        .addMethod(
          getListTenantsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.chatbot.core.tenant.grpc.TenantServiceProto.ListTenantsRequest,
              com.chatbot.core.tenant.grpc.TenantServiceProto.ListTenantsResponse>(
                service, METHODID_LIST_TENANTS)))
        .addMethod(
          getSearchTenantsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.chatbot.core.tenant.grpc.TenantServiceProto.SearchTenantsRequest,
              com.chatbot.core.tenant.grpc.TenantServiceProto.SearchTenantsResponse>(
                service, METHODID_SEARCH_TENANTS)))
        .addMethod(
          getValidateTenantMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.chatbot.core.tenant.grpc.TenantServiceProto.ValidateTenantRequest,
              com.chatbot.core.tenant.grpc.TenantServiceProto.ValidateTenantResponse>(
                service, METHODID_VALIDATE_TENANT)))
        .addMethod(
          getCheckTenantExistsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.chatbot.core.tenant.grpc.TenantServiceProto.CheckTenantExistsRequest,
              com.chatbot.core.tenant.grpc.TenantServiceProto.CheckTenantExistsResponse>(
                service, METHODID_CHECK_TENANT_EXISTS)))
        .addMethod(
          getActivateTenantMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.chatbot.core.tenant.grpc.TenantServiceProto.ActivateTenantRequest,
              com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse>(
                service, METHODID_ACTIVATE_TENANT)))
        .addMethod(
          getSuspendTenantMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.chatbot.core.tenant.grpc.TenantServiceProto.SuspendTenantRequest,
              com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse>(
                service, METHODID_SUSPEND_TENANT)))
        .addMethod(
          getGetTenantStatusMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.chatbot.core.tenant.grpc.TenantServiceProto.GetTenantStatusRequest,
              com.chatbot.core.tenant.grpc.TenantServiceProto.TenantStatusResponse>(
                service, METHODID_GET_TENANT_STATUS)))
        .build();
  }

  private static abstract class TenantServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    TenantServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.chatbot.core.tenant.grpc.TenantServiceProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("TenantService");
    }
  }

  private static final class TenantServiceFileDescriptorSupplier
      extends TenantServiceBaseDescriptorSupplier {
    TenantServiceFileDescriptorSupplier() {}
  }

  private static final class TenantServiceMethodDescriptorSupplier
      extends TenantServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    TenantServiceMethodDescriptorSupplier(java.lang.String methodName) {
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
      synchronized (TenantServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new TenantServiceFileDescriptorSupplier())
              .addMethod(getCreateTenantMethod())
              .addMethod(getGetTenantMethod())
              .addMethod(getUpdateTenantMethod())
              .addMethod(getDeleteTenantMethod())
              .addMethod(getListTenantsMethod())
              .addMethod(getSearchTenantsMethod())
              .addMethod(getValidateTenantMethod())
              .addMethod(getCheckTenantExistsMethod())
              .addMethod(getActivateTenantMethod())
              .addMethod(getSuspendTenantMethod())
              .addMethod(getGetTenantStatusMethod())
              .build();
        }
      }
    }
    return result;
  }
}
