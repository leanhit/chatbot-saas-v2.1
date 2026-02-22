# gRPC Interfaces Documentation

## Overview

The chatbot SaaS platform uses gRPC for internal hub-to-hub communication. This provides high-performance, type-safe communication between services with automatic serialization/deserialization and built-in streaming capabilities.

## gRPC Service Architecture

### Service Discovery
```
┌─────────────────────────────────────────────────────────────┐
│                    Service Mesh                             │
├─────────────────────────────────────────────────────────────┤
│  Identity:50051  │  User:50052    │  Tenant:50053          │
│  App:50054       │  Billing:50055 │  Wallet:50056          │
│  Config:50057    │  Message:50058 │  Spokes:50059          │
└─────────────────────────────────────────────────────────────┘
```

### Communication Patterns
- **Synchronous RPC**: Direct request/response
- **Streaming**: Server-side, client-side, and bidirectional streaming
- **Deadlines**: Timeout handling for all calls
- **Cancellation**: Graceful cancellation support

## Proto File Definitions

### Common Types (`common.proto`)
```protobuf
syntax = "proto3";

package com.chatbot.common;

import "google/protobuf/timestamp.proto";
import "google/protobuf/empty.proto";

// Common request/response wrapper
message Request {
  string request_id = 1;
  google.protobuf.Timestamp timestamp = 2;
  map<string, string> metadata = 3;
}

message Response {
  string request_id = 1;
  bool success = 2;
  string error_message = 3;
  google.protobuf.Timestamp timestamp = 4;
  map<string, string> metadata = 5;
}

// Pagination
message PageRequest {
  int32 page = 1;
  int32 size = 2;
  string sort_by = 3;
  string sort_direction = 4;
}

message PageResponse {
  int32 total_elements = 1;
  int32 total_pages = 2;
  int32 current_page = 3;
  int32 page_size = 4;
}

// User and Tenant references
message UserReference {
  string user_id = 1;
  string email = 2;
  string first_name = 3;
  string last_name = 4;
}

message TenantReference {
  string tenant_id = 1;
  string name = 2;
  string status = 3;
}

// Error details
message ErrorDetail {
  string code = 1;
  string message = 2;
  map<string, string> details = 3;
}
```

### Identity Service (`identity-service.proto`)
```protobuf
syntax = "proto3";

package com.chatbot.identity;

import "common.proto";
import "google/protobuf/empty.proto";

service IdentityService {
  // Authentication
  rpc AuthenticateUser(AuthenticateUserRequest) returns (AuthenticateUserResponse);
  rpc ValidateToken(ValidateTokenRequest) returns (ValidateTokenResponse);
  rpc RefreshToken(RefreshTokenRequest) returns (RefreshTokenResponse);
  
  // User management
  rpc GetUserById(GetUserByIdRequest) returns (GetUserByIdResponse);
  rpc GetUserByEmail(GetUserByEmailRequest) returns (GetUserByEmailResponse);
  rpc ValidateUser(ValidateUserRequest) returns (ValidateUserResponse);
  
  // Session management
  rpc CreateUserSession(CreateUserSessionRequest) returns (CreateUserSessionResponse);
  rpc InvalidateUserSession(InvalidateUserSessionRequest) returns (google.protobuf.Empty);
  rpc GetUserSessions(GetUserSessionsRequest) returns (GetUserSessionsResponse);
}

message AuthenticateUserRequest {
  common.Request request = 1;
  string email = 2;
  string password = 3;
  string tenant_id = 4;
}

message AuthenticateUserResponse {
  common.Response response = 1;
  string user_id = 2;
  string email = 3;
  string role = 4;
  string access_token = 5;
  string refresh_token = 6;
  int64 expires_in = 7;
}

message ValidateTokenRequest {
  common.Request request = 1;
  string token = 2;
}

message ValidateTokenResponse {
  common.Response response = 1;
  bool valid = 2;
  common.UserReference user = 3;
  string role = 4;
  int64 expires_at = 5;
}

message RefreshTokenRequest {
  common.Request request = 1;
  string refresh_token = 2;
}

message RefreshTokenResponse {
  common.Response response = 1;
  string access_token = 2;
  string refresh_token = 3;
  int64 expires_in = 4;
}

message GetUserByIdRequest {
  common.Request request = 1;
  string user_id = 2;
}

message GetUserByIdResponse {
  common.Response response = 1;
  common.UserReference user = 2;
  string role = 3;
  string status = 4;
  google.protobuf.Timestamp created_at = 5;
}

message GetUserByEmailRequest {
  common.Request request = 1;
  string email = 2;
}

message GetUserByEmailResponse {
  common.Response response = 1;
  common.UserReference user = 2;
  string role = 3;
  string status = 4;
}

message ValidateUserRequest {
  common.Request request = 1;
  string user_id = 2;
}

message ValidateUserResponse {
  common.Response response = 1;
  bool valid = 2;
  string status = 3;
  common.UserReference user = 4;
}

message CreateUserSessionRequest {
  common.Request request = 1;
  string user_id = 2;
  string token_hash = 3;
  int64 expires_at = 4;
  string ip_address = 5;
  string user_agent = 6;
}

message CreateUserSessionResponse {
  common.Response response = 1;
  string session_id = 2;
}

message InvalidateUserSessionRequest {
  common.Request request = 1;
  string session_id = 2;
}

message GetUserSessionsRequest {
  common.Request request = 1;
  string user_id = 2;
  common.PageRequest page = 3;
}

message GetUserSessionsResponse {
  common.Response response = 1;
  repeated UserSession sessions = 2;
  common.PageResponse page = 3;
}

message UserSession {
  string session_id = 1;
  string user_id = 2;
  int64 expires_at = 3;
  string ip_address = 4;
  string user_agent = 5;
  google.protobuf.Timestamp created_at = 6;
}
```

### User Service (`user-service.proto`)
```protobuf
syntax = "proto3";

package com.chatbot.user;

import "common.proto";
import "google/protobuf/timestamp.proto";

service UserService {
  // Profile management
  rpc GetUserProfile(GetUserProfileRequest) returns (GetUserProfileResponse);
  rpc UpdateUserProfile(UpdateUserProfileRequest) returns (UpdateUserProfileResponse);
  rpc GetUserPreferences(GetUserPreferencesRequest) returns (GetUserPreferencesResponse);
  rpc UpdateUserPreferences(UpdateUserPreferencesRequest) returns (UpdateUserPreferencesResponse);
  
  // Activity tracking
  rpc LogUserActivity(LogUserActivityRequest) returns (common.Response);
  rpc GetUserActivities(GetUserActivitiesRequest) returns (GetUserActivitiesResponse);
  
  // Analytics
  rpc GetUserAnalytics(GetUserAnalyticsRequest) returns (GetUserAnalyticsResponse);
  rpc GetUserSummary(GetUserSummaryRequest) returns (GetUserSummaryResponse);
  
  // Address management
  rpc GetUserAddresses(GetUserAddressesRequest) returns (GetUserAddressesResponse);
  rpc CreateUserAddress(CreateUserAddressRequest) returns (CreateUserAddressResponse);
  rpc UpdateUserAddress(UpdateUserAddressRequest) returns (UpdateUserAddressResponse);
  rpc DeleteUserAddress(DeleteUserAddressRequest) returns (common.Response);
}

message GetUserProfileRequest {
  common.Request request = 1;
  string user_id = 2;
}

message GetUserProfileResponse {
  common.Response response = 1;
  UserProfile profile = 2;
}

message UserProfile {
  string user_id = 1;
  string first_name = 2;
  string last_name = 3;
  string phone = 4;
  string avatar_url = 5;
  string timezone = 6;
  string language = 7;
  string bio = 8;
  string date_of_birth = 9;
  string gender = 10;
  google.protobuf.Timestamp created_at = 11;
  google.protobuf.Timestamp updated_at = 12;
}

message UpdateUserProfileRequest {
  common.Request request = 1;
  string user_id = 2;
  UserProfile profile = 3;
}

message UpdateUserProfileResponse {
  common.Response response = 1;
  UserProfile profile = 2;
}

message GetUserPreferencesRequest {
  common.Request request = 1;
  string user_id = 2;
}

message GetUserPreferencesResponse {
  common.Response response = 1;
  UserPreferences preferences = 2;
}

message UserPreferences {
  string user_id = 1;
  bool email_notifications = 2;
  bool sms_notifications = 3;
  bool push_notifications = 4;
  string theme = 5;
  string currency = 6;
  string privacy_profile_visibility = 7;
  bool privacy_activity_tracking = 8;
  google.protobuf.Timestamp created_at = 9;
  google.protobuf.Timestamp updated_at = 10;
}

message UpdateUserPreferencesRequest {
  common.Request request = 1;
  string user_id = 2;
  UserPreferences preferences = 3;
}

message UpdateUserPreferencesResponse {
  common.Response response = 1;
  UserPreferences preferences = 2;
}

message LogUserActivityRequest {
  common.Request request = 1;
  string user_id = 2;
  string activity_type = 3;
  string description = 4;
  string ip_address = 5;
  string user_agent = 6;
  map<string, string> metadata = 7;
}

message GetUserActivitiesRequest {
  common.Request request = 1;
  string user_id = 2;
  common.PageRequest page = 3;
  string activity_type = 4;
  google.protobuf.Timestamp from_date = 5;
  google.protobuf.Timestamp to_date = 6;
}

message GetUserActivitiesResponse {
  common.Response response = 1;
  repeated UserActivity activities = 2;
  common.PageResponse page = 3;
}

message UserActivity {
  string id = 1;
  string user_id = 2;
  string activity_type = 3;
  string description = 4;
  string ip_address = 5;
  string user_agent = 6;
  map<string, string> metadata = 7;
  google.protobuf.Timestamp created_at = 8;
}

message GetUserAnalyticsRequest {
  common.Request request = 1;
  string user_id = 2;
  google.protobuf.Timestamp from_date = 3;
  google.protobuf.Timestamp to_date = 4;
  string group_by = 5;
}

message GetUserAnalyticsResponse {
  common.Response response = 1;
  UserAnalytics analytics = 2;
}

message UserAnalytics {
  int32 total_messages = 1;
  int32 total_sessions = 2;
  int32 average_session_duration = 3;
  google.protobuf.Timestamp last_active_at = 4;
  int32 most_active_hour = 5;
  repeated string preferred_channels = 6;
  int32 engagement_score = 7;
}

message GetUserSummaryRequest {
  common.Request request = 1;
  string user_id = 2;
}

message GetUserSummaryResponse {
  common.Response response = 1;
  common.UserReference user = 2;
  UserProfile profile = 3;
  UserPreferences preferences = 4;
  UserAnalytics analytics = 5;
}

message GetUserAddressesRequest {
  common.Request request = 1;
  string user_id = 2;
}

message GetUserAddressesResponse {
  common.Response response = 1;
  repeated UserAddress addresses = 2;
}

message UserAddress {
  string id = 1;
  string user_id = 2;
  string address_type = 3;
  string street_address = 4;
  string city = 5;
  string state_province = 6;
  string postal_code = 7;
  string country = 8;
  bool is_default = 9;
  google.protobuf.Timestamp created_at = 10;
  google.protobuf.Timestamp updated_at = 11;
}

message CreateUserAddressRequest {
  common.Request request = 1;
  string user_id = 2;
  UserAddress address = 3;
}

message CreateUserAddressResponse {
  common.Response response = 1;
  UserAddress address = 2;
}

message UpdateUserAddressRequest {
  common.Request request = 1;
  string user_id = 2;
  string address_id = 3;
  UserAddress address = 4;
}

message UpdateUserAddressResponse {
  common.Response response = 1;
  UserAddress address = 2;
}

message DeleteUserAddressRequest {
  common.Request request = 1;
  string user_id = 2;
  string address_id = 3;
}
```

### Tenant Service (`tenant-service.proto`)
```protobuf
syntax = "proto3";

package com.chatbot.tenant;

import "common.proto";
import "google/protobuf/timestamp.proto";
import "google/protobuf/empty.proto";

service TenantService {
  // Tenant management
  rpc GetTenantById(GetTenantByIdRequest) returns (GetTenantByIdResponse);
  rpc CreateTenant(CreateTenantRequest) returns (CreateTenantResponse);
  rpc UpdateTenant(UpdateTenantRequest) returns (UpdateTenantResponse);
  rpc DeleteTenant(DeleteTenantRequest) returns (common.Response);
  
  // Membership management
  rpc GetTenantMembers(GetTenantMembersRequest) returns (GetTenantMembersResponse);
  rpc AddTenantMember(AddTenantMemberRequest) returns (AddTenantMemberResponse);
  rpc UpdateTenantMember(UpdateTenantMemberRequest) returns (UpdateTenantMemberResponse);
  rpc RemoveTenantMember(RemoveTenantMemberRequest) returns (common.Response);
  
  // Validation
  rpc ValidateTenantAccess(ValidateTenantAccessRequest) returns (ValidateTenantAccessResponse);
  rpc GetUserTenants(GetUserTenantsRequest) returns (GetUserTenantsResponse);
}

message GetTenantByIdRequest {
  common.Request request = 1;
  string tenant_id = 2;
}

message GetTenantByIdResponse {
  common.Response response = 1;
  Tenant tenant = 2;
}

message Tenant {
  string id = 1;
  string name = 2;
  string description = 3;
  string status = 4;
  string visibility = 5;
  string owner_id = 6;
  string industry = 7;
  string company_size = 8;
  string website = 9;
  string logo_url = 10;
  int32 member_count = 11;
  google.protobuf.Timestamp created_at = 12;
  google.protobuf.Timestamp updated_at = 13;
}

message CreateTenantRequest {
  common.Request request = 1;
  string name = 2;
  string description = 3;
  string visibility = 4;
  string industry = 5;
  string company_size = 6;
  string owner_id = 7;
}

message CreateTenantResponse {
  common.Response response = 1;
  Tenant tenant = 2;
}

message UpdateTenantRequest {
  common.Request request = 1;
  string tenant_id = 2;
  Tenant tenant = 3;
}

message UpdateTenantResponse {
  common.Response response = 1;
  Tenant tenant = 2;
}

message DeleteTenantRequest {
  common.Request request = 1;
  string tenant_id = 2;
}

message GetTenantMembersRequest {
  common.Request request = 1;
  string tenant_id = 2;
  common.PageRequest page = 3;
}

message GetTenantMembersResponse {
  common.Response response = 1;
  repeated TenantMember members = 2;
  common.PageResponse page = 3;
}

message TenantMember {
  string id = 1;
  string tenant_id = 2;
  string user_id = 3;
  string role = 4;
  string status = 5;
  common.UserReference user = 6;
  google.protobuf.Timestamp joined_at = 7;
  google.protobuf.Timestamp updated_at = 8;
}

message AddTenantMemberRequest {
  common.Request request = 1;
  string tenant_id = 2;
  string user_id = 3;
  string role = 4;
}

message AddTenantMemberResponse {
  common.Response response = 1;
  TenantMember member = 2;
}

message UpdateTenantMemberRequest {
  common.Request request = 1;
  string tenant_id = 2;
  string member_id = 3;
  string role = 4;
}

message UpdateTenantMemberResponse {
  common.Response response = 1;
  TenantMember member = 2;
}

message RemoveTenantMemberRequest {
  common.Request request = 1;
  string tenant_id = 2;
  string member_id = 3;
}

message ValidateTenantAccessRequest {
  common.Request request = 1;
  string tenant_id = 2;
  string user_id = 3;
  string required_role = 4;
}

message ValidateTenantAccessResponse {
  common.Response response = 1;
  bool valid = 2;
  string role = 3;
  common.TenantReference tenant = 4;
}

message GetUserTenantsRequest {
  common.Request request = 1;
  string user_id = 2;
  common.PageRequest page = 3;
}

message GetUserTenantsResponse {
  common.Response response = 1;
  repeated Tenant tenants = 2;
  common.PageResponse page = 3;
}
```

### Message Service (`message-service.proto`)
```protobuf
syntax = "proto3";

package com.chatbot.message;

import "common.proto";
import "google/protobuf/timestamp.proto";

service MessageService {
  // Message routing
  rpc RouteMessage(RouteMessageRequest) returns (RouteMessageResponse);
  rpc GetRoutingRules(GetRoutingRulesRequest) returns (GetRoutingRulesResponse);
  
  // Message processing
  rpc ProcessMessage(ProcessMessageRequest) returns (ProcessMessageResponse);
  rpc GetDecisionHistory(GetDecisionHistoryRequest) returns (GetDecisionHistoryResponse);
  
  // Message store
  rpc StoreMessage(StoreMessageRequest) returns (common.Response);
  rpc GetConversation(GetConversationRequest) returns (GetConversationResponse);
  rpc GetUserConversations(GetUserConversationsRequest) returns (GetUserConversationsResponse);
  rpc SendMessage(SendMessageRequest) returns (SendMessageResponse);
}

message RouteMessageRequest {
  common.Request request = 1;
  Message message = 2;
  RoutingContext routing_context = 3;
}

message RouteMessageResponse {
  common.Response response = 1;
  RoutingDecision routing_decision = 2;
  string status = 3;
}

message Message {
  string id = 1;
  string content = 2;
  string message_type = 3;
  MessageParticipant sender = 4;
  MessageParticipant recipient = 5;
  string channel = 6;
  google.protobuf.Timestamp timestamp = 7;
  map<string, string> metadata = 8;
}

message MessageParticipant {
  string id = 1;
  string type = 2; // USER, BOT, AGENT
  string name = 3;
}

message RoutingContext {
  string tenant_id = 1;
  string priority = 2;
  int32 timeout = 3;
  map<string, string> context = 4;
}

message RoutingDecision {
  Destination destination = 1;
  double confidence = 2;
  string reason = 3;
  int32 processing_time = 4;
}

message Destination {
  string type = 1; // BOTPRESS, HUMAN_AGENT, EXTERNAL_API
  string bot_id = 2;
  string endpoint = 3;
  map<string, string> parameters = 4;
}

message GetRoutingRulesRequest {
  common.Request request = 1;
  string tenant_id = 2;
  string status = 3;
}

message GetRoutingRulesResponse {
  common.Response response = 1;
  repeated RoutingRule rules = 2;
}

message RoutingRule {
  string id = 1;
  string tenant_id = 2;
  string name = 3;
  int32 priority = 4;
  repeated RuleCondition conditions = 5;
  repeated RuleAction actions = 6;
  string status = 7;
  google.protobuf.Timestamp created_at = 8;
  google.protobuf.Timestamp updated_at = 9;
}

message RuleCondition {
  string field = 1;
  string operator = 2;
  string value = 3;
  bool case_sensitive = 4;
}

message RuleAction {
  string type = 1;
  map<string, string> parameters = 2;
}

message ProcessMessageRequest {
  common.Request request = 1;
  Message message = 2;
  ProcessingOptions processing_options = 3;
}

message ProcessingOptions {
  bool run_intent_detection = 1;
  bool run_sentiment_analysis = 2;
  bool run_entity_extraction = 3;
  string language = 4;
}

message ProcessMessageResponse {
  common.Response response = 1;
  ProcessingResult processing_result = 2;
  repeated NextAction next_actions = 3;
}

message ProcessingResult {
  Intent intent = 2;
  Sentiment sentiment = 3;
  repeated Entity entities = 4;
  string language = 5;
  int32 processing_time = 6;
}

message Intent {
  string name = 1;
  double confidence = 2;
}

message Sentiment {
  double score = 1;
  string label = 2;
}

message Entity {
  string type = 1;
  string value = 2;
  double confidence = 3;
}

message NextAction {
  string type = 1;
  map<string, string> parameters = 2;
}

message GetDecisionHistoryRequest {
  common.Request request = 1;
  string message_id = 2;
}

message GetDecisionHistoryResponse {
  common.Response response = 1;
  repeated Decision decisions = 2;
}

message Decision {
  string type = 1;
  map<string, string> result = 2;
  google.protobuf.Timestamp timestamp = 3;
}

message StoreMessageRequest {
  common.Request request = 1;
  Message message = 2;
  string conversation_id = 3;
}

message GetConversationRequest {
  common.Request request = 1;
  string conversation_id = 2;
  common.PageRequest page = 3;
  google.protobuf.Timestamp from_date = 4;
  google.protobuf.Timestamp to_date = 5;
}

message GetConversationResponse {
  common.Response response = 1;
  repeated Message messages = 2;
  common.PageResponse page = 3;
}

message GetUserConversationsRequest {
  common.Request request = 1;
  string user_id = 2;
  common.PageRequest page = 3;
  string status = 4;
  string channel = 5;
}

message GetUserConversationsResponse {
  common.Response response = 1;
  repeated Conversation conversations = 2;
  common.PageResponse page = 3;
}

message Conversation {
  string id = 1;
  MessageParticipant participant = 2;
  string channel = 3;
  string status = 4;
  Message last_message = 5;
  int32 message_count = 6;
  google.protobuf.Timestamp created_at = 7;
  google.protobuf.Timestamp updated_at = 8;
}

message SendMessageRequest {
  common.Request request = 1;
  string conversation_id = 2;
  string content = 3;
  string message_type = 4;
  map<string, string> metadata = 5;
}

message SendMessageResponse {
  common.Response response = 1;
  Message message = 2;
}
```

## Client Configuration

### Java Client Setup
```java
// Identity Service Client
ManagedChannel channel = ManagedChannelBuilder
    .forAddress("identity-service", 50051)
    .usePlaintext()
    .build();

IdentityServiceGrpc.IdentityServiceBlockingStub identityClient = 
    IdentityServiceGrpc.newBlockingStub(channel)
        .withDeadlineAfter(5, TimeUnit.SECONDS);

// User Service Client
ManagedChannel userChannel = ManagedChannelBuilder
    .forAddress("user-service", 50052)
    .usePlaintext()
    .build();

UserServiceGrpc.UserServiceBlockingStub userClient = 
    UserServiceGrpc.newBlockingStub(userChannel)
        .withDeadlineAfter(5, TimeUnit.SECONDS);
```

### Interceptor Configuration
```java
// Authentication Interceptor
ClientInterceptor authInterceptor = new ClientInterceptor() {
    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
        MethodDescriptor<ReqT, RespT> method, 
        CallOptions callOptions, 
        Channel next) {
        
        return next.newCall(method, callOptions.withHeaders(
            Metadata.Builder()
                .put(Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER), 
                     "Bearer " + token)
                .build()
        ));
    }
};

// Apply interceptor
IdentityServiceGrpc.IdentityServiceBlockingStub client = 
    IdentityServiceGrpc.newBlockingStub(channel)
        .withInterceptors(authInterceptor);
```

## Error Handling

### Status Codes
- `OK (0)`: Success
- `CANCELLED (1)`: Operation cancelled
- `UNKNOWN (2)`: Unknown error
- `INVALID_ARGUMENT (3)`: Invalid arguments
- `DEADLINE_EXCEEDED (4)`: Deadline exceeded
- `NOT_FOUND (5)`: Entity not found
- `ALREADY_EXISTS (6)`: Entity already exists
- `PERMISSION_DENIED (7)`: Permission denied
- `UNAUTHENTICATED (16)`: Unauthenticated
- `RESOURCE_EXHAUSTED (8)`: Resource exhausted
- `FAILED_PRECONDITION (9)`: Failed precondition
- `ABORTED (10)`: Operation aborted
- `OUT_OF_RANGE (11)`: Out of range
- `UNIMPLEMENTED (12)`: Unimplemented
- `INTERNAL (13)`: Internal error
- `UNAVAILABLE (14)`: Service unavailable
- `DATA_LOSS (15)`: Data loss

### Error Response Format
```java
try {
    AuthenticateUserResponse response = identityClient.authenticateUser(request);
    // Handle success
} catch (StatusRuntimeException e) {
    Status status = e.getStatus();
    switch (status.getCode()) {
        case NOT_FOUND:
            // Handle user not found
            break;
        case UNAUTHENTICATED:
            // Handle authentication failure
            break;
        case DEADLINE_EXCEEDED:
            // Handle timeout
            break;
        default:
            // Handle other errors
            break;
    }
}
```

## Performance Considerations

### Connection Pooling
```java
// Shared channel for multiple stubs
ManagedChannel sharedChannel = ManagedChannelBuilder
    .forAddress("service-host", port)
    .usePlaintext()
    .maxInboundMessageSize(4 * 1024 * 1024) // 4MB
    .keepAliveTime(30, TimeUnit.SECONDS)
    .build();

// Reuse channel for multiple service calls
Service1Grpc.Service1BlockingStub client1 = Service1Grpc.newBlockingStub(sharedChannel);
Service2Grpc.Service2BlockingStub client2 = Service2Grpc.newBlockingStub(sharedChannel);
```

### Streaming
```java
// Server-side streaming
Iterator<GetUserActivitiesResponse> responses = 
    userClient.getUserActivities(request).iterator();

while (responses.hasNext()) {
    GetUserActivitiesResponse response = responses.next();
    // Process each response
}

// Client-side streaming
StreamObserver<LogUserActivityRequest> requestObserver = 
    userClient.logUserActivities(new StreamObserver<LogUserActivityResponse>() {
        @Override
        public void onNext(LogUserActivityResponse value) {
            // Handle response
        }
        
        @Override
        public void onError(Throwable t) {
            // Handle error
        }
        
        @Override
        public void onCompleted() {
            // Handle completion
        }
    });

// Send multiple requests
requestObserver.onNext(request1);
requestObserver.onNext(request2);
requestObserver.onCompleted();
```

This gRPC interface design provides type-safe, high-performance communication between hubs with proper error handling, streaming support, and performance optimizations.
