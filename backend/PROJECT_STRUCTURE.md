# 🏗️ Hub & Spoke Multi-SaaS Project Structure

## 📁 Complete Folder Structure

```
chatbot-saas-v2.1/backend/
├── 📄 ChatbotApplication.java
├── 📄 build.gradle
├── 📄 application.yml
├── 📄 docker-compose.yml
├── 📄 Dockerfile
│
├── 📁 docs/                           # 📚 Documentation
│   ├── api/                           # API documentation
│   │   ├── identity-hub.md
│   │   ├── tenant-hub.md
│   │   ├── app-hub.md
│   │   ├── billing-hub.md
│   │   ├── wallet-hub.md
│   │   ├── config-hub.md
│   │   └── message-hub.md
│   ├── architecture/                  # Architecture docs
│   │   ├── hub-spoke-design.md
│   │   ├── database-schema.md
│   │   ├── grpc-interfaces.md
│   │   └── saga-patterns.md
│   └── deployment/                    # Deployment guides
│       ├── kubernetes/
│       ├── database-migration/
│       └── monitoring/
│
├── 📁 scripts/                        # 🛠️ Utility scripts
│   ├── database/                      # Database scripts
│   │   ├── migrations/                # Flyway/Liquibase migrations
│   │   │   ├── identity-hub/
│   │   │   ├── tenant-hub/
│   │   │   ├── app-hub/
│   │   │   ├── billing-hub/
│   │   │   ├── wallet-hub/
│   │   │   ├── config-hub/
│   │   │   └── message-hub/
│   │   ├── setup-databases.sh         # Create separate databases
│   │   └── seed-data.sql              # Initial seed data
│   ├── kubernetes/                    # K8s deployment scripts
│   │   ├── namespaces/
│   │   ├── services/
│   │   ├── deployments/
│   │   └── ingress/
│   └── monitoring/                    # Monitoring setup
│       ├── prometheus/
│       ├── grafana/
│       └── jaeger/
│
├── 📁 src/main/java/com/chatbot/
│   ├── 📄 ChatbotApplication.java     # Main application entry
│   │
│   ├── 📁 core/                      # 🔒 Core Hubs - v0.1 LOCKED
│   │   ├── 📁 identity/              # 👤 Identity Hub
│   │   │   ├── 📁 controller/
│   │   │   │   ├── 📄 AuthController.java
│   │   │   │   ├── 📄 UserController.java
│   │   │   │   └── 📄 HealthController.java
│   │   │   ├── 📁 service/
│   │   │   │   ├── 📄 AuthService.java
│   │   │   │   ├── 📄 UserService.java
│   │   │   │   └── 📄 JwtService.java
│   │   │   ├── 📁 repository/
│   │   │   │   └── 📄 AuthRepository.java
│   │   │   ├── 📁 model/
│   │   │   │   ├── 📄 Auth.java
│   │   │   │   ├── 📄 SystemRole.java
│   │   │   │   └── 📄 UserStatus.java
│   │   │   ├── 📁 dto/
│   │   │   │   ├── 📄 LoginRequest.java
│   │   │   │   ├── 📄 RegisterRequest.java
│   │   │   │   ├── 📄 AuthResponse.java
│   │   │   │   ├── 📄 UserDto.java
│   │   │   │   └── 📄 ChangePasswordRequest.java
│   │   │   ├── 📁 security/
│   │   │   │   ├── 📄 JwtFilter.java
│   │   │   │   ├── 📄 CustomUserDetails.java
│   │   │   │   └── 📄 IdentitySecurityConfig.java
│   │   │   ├── 📁 grpc/
│   │   │   │   ├── 📄 IdentityServiceGrpcImpl.java
│   │   │   │   └── 📄 IdentityGrpcClient.java
│   │   │   └── 📁 config/
│   │   │       ├── 📄 IdentityDatabaseConfig.java
│   │   │       └── 📄 IdentityGrpcConfig.java
│   │   │
│   │   ├── 📁 tenant/                # 🏢 Tenant Hub
│   │   │   ├── 📁 core/
│   │   │   │   ├── 📁 controller/
│   │   │   │   │   ├── 📄 TenantController.java
│   │   │   │   │   └── 📄 TenantHealthController.java
│   │   │   │   ├── 📁 service/
│   │   │   │   │   ├── 📄 TenantService.java
│   │   │   │   │   └── 📄 TenantValidationService.java
│   │   │   │   ├── 📁 repository/
│   │   │   │   │   └── 📄 TenantRepository.java
│   │   │   │   ├── 📁 model/
│   │   │   │   │   ├── 📄 Tenant.java
│   │   │   │   │   ├── 📄 TenantStatus.java
│   │   │   │   │   └── 📄 TenantVisibility.java
│   │   │   │   ├── 📁 dto/
│   │   │   │   │   ├── 📄 CreateTenantRequest.java
│   │   │   │   │   ├── 📄 TenantResponse.java
│   │   │   │   │   ├── 📄 TenantDetailResponse.java
│   │   │   │   │   └── 📄 TenantSearchRequest.java
│   │   │   │   └── 📁 grpc/
│   │   │   │       ├── 📄 TenantServiceGrpcImpl.java
│   │   │   │       └── 📄 TenantGrpcClient.java
│   │   │   ├── 📁 membership/
│   │   │   │   ├── 📁 controller/
│   │   │   │   ├── 📁 service/
│   │   │   │   ├── 📁 repository/
│   │   │   │   ├── 📁 model/
│   │   │   │   │   ├── 📄 TenantMember.java
│   │   │   │   │   ├── 📄 TenantRole.java
│   │   │   │   │   └── 📄 MembershipStatus.java
│   │   │   │   └── 📁 dto/
│   │   │   ├── 📁 profile/
│   │   │   │   ├── 📁 controller/
│   │   │   │   ├── 📁 service/
│   │   │   │   ├── 📁 repository/
│   │   │   │   ├── 📁 model/
│   │   │   │   │   └── 📄 TenantProfile.java
│   │   │   │   └── 📁 dto/
│   │   │   └── 📁 professional/
│   │   │       ├── 📁 controller/
│   │   │       ├── 📁 service/
│   │   │       ├── 📁 repository/
│   │   │       ├── 📁 model/
│   │   │       │   └── 📄 TenantProfessional.java
│   │   │       └── 📁 dto/
│   │   │
│   │   ├── 📁 app/                   # 🚀 App Hub
│   │   │   ├── 📁 registry/
│   │   │   │   ├── 📁 controller/
│   │   │   │   │   ├── 📄 AppRegistryController.java
│   │   │   │   │   └── 📄 AppController.java
│   │   │   │   ├── 📁 service/
│   │   │   │   │   ├── 📄 AppRegistryService.java
│   │   │   │   │   └── 📄 AppService.java
│   │   │   │   ├── 📁 repository/
│   │   │   │   │   └── 📄 AppRegistryRepository.java
│   │   │   │   ├── 📁 model/
│   │   │   │   │   ├── 📄 AppRegistry.java
│   │   │   │   │   ├── 📄 AppType.java
│   │   │   │   │   ├── 📄 AppStatus.java
│   │   │   │   │   └── 📄 AppConfiguration.java
│   │   │   │   ├── 📁 dto/
│   │   │   │   │   ├── 📄 RegisterAppRequest.java
│   │   │   │   │   ├── 📄 AppResponse.java
│   │   │   │   │   └── 📄 AppConfigurationDto.java
│   │   │   │   └── 📁 grpc/
│   │   │   │       ├── 📄 AppServiceGrpcImpl.java
│   │   │   │       └── 📄 AppGrpcClient.java
│   │   │   ├── 📁 subscription/
│   │   │   │   ├── 📁 controller/
│   │   │   │   ├── 📁 service/
│   │   │   │   │   ├── 📄 AppSubscriptionService.java
│   │   │   │   │   └── 📄 SubscriptionValidationService.java
│   │   │   │   ├── 📁 repository/
│   │   │   │   │   └── 📄 AppSubscriptionRepository.java
│   │   │   │   ├── 📁 model/
│   │   │   │   │   ├── 📄 AppSubscription.java
│   │   │   │   │   ├── 📄 SubscriptionStatus.java
│   │   │   │   │   └── 📄 SubscriptionPlan.java
│   │   │   │   └── 📁 dto/
│   │   │   │       ├── 📄 SubscribeAppRequest.java
│   │   │   │       └── 📄 SubscriptionResponse.java
│   │   │   └── 📁 guard/
│   │   │       ├── 📁 service/
│   │   │       │   ├── 📄 AppGuardService.java
│   │   │       │   └── 📄 AccessControlService.java
│   │   │       ├── 📁 model/
│   │   │       │   ├── 📄 AppGuard.java
│   │   │       │   ├── 📄 GuardRule.java
│   │   │       │   └── 📄 GuardType.java
│   │   │       └── 📁 dto/
│   │   │
│   │   ├── 📁 billing/               # 💰 Billing Hub (READ-ONLY)
│   │   │   ├── 📁 account/
│   │   │   │   ├── 📁 controller/
│   │   │   │   ├── 📁 service/
│   │   │   │   ├── 📁 repository/
│   │   │   │   ├── 📁 model/
│   │   │   │   │   ├── 📄 BillingAccount.java
│   │   │   │   │   └── 📄 BillingType.java
│   │   │   │   └── 📁 dto/
│   │   │   ├── 📁 subscription/
│   │   │   │   ├── 📁 controller/
│   │   │   │   ├── 📁 service/
│   │   │   │   ├── 📁 repository/
│   │   │   │   ├── 📁 model/
│   │   │   │   │   └── 📄 BillingSubscription.java
│   │   │   │   └── 📁 dto/
│   │   │   └── 📁 entitlement/
│   │   │       ├── 📁 controller/
│   │   │       ├── 📁 service/
│   │   │       │   ├── 📄 EntitlementService.java
│   │   │       │   └── 📄 EntitlementValidationService.java
│   │   │       ├── 📁 repository/
│   │   │       ├── 📁 model/
│   │   │       │   ├── 📄 Entitlement.java
│   │   │       │   ├── 📄 Feature.java
│   │   │       │   └── 📄 UsageLimit.java
│   │   │       └── 📁 dto/
│   │   │
│   │   ├── 📁 wallet/                # 💳 Wallet Hub
│   │   │   ├── 📁 wallet/
│   │   │   │   ├── 📁 controller/
│   │   │   │   ├── 📁 service/
│   │   │   │   │   ├── 📄 WalletService.java
│   │   │   │   │   └── 📄 BalanceService.java
│   │   │   │   ├── 📁 repository/
│   │   │   │   │   └── 📄 WalletRepository.java
│   │   │   │   ├── 📁 model/
│   │   │   │   │   ├── 📄 Wallet.java
│   │   │   │   │   ├── 📄 WalletStatus.java
│   │   │   │   │   └── 📄 Currency.java
│   │   │   │   └── 📁 dto/
│   │   │   │       ├── 📄 CreateWalletRequest.java
│   │   │   │       ├── 📄 WalletResponse.java
│   │   │   │       └── 📄 BalanceDto.java
│   │   │   ├── 📁 transaction/
│   │   │   │   ├── 📁 controller/
│   │   │   │   ├── 📁 service/
│   │   │   │   │   ├── 📄 TransactionService.java
│   │   │   │   │   └── 📄 TopupPurchaseService.java
│   │   │   │   ├── 📁 repository/
│   │   │   │   │   └── 📄 TransactionRepository.java
│   │   │   │   ├── 📁 model/
│   │   │   │   │   ├── 📄 Transaction.java
│   │   │   │   │   ├── 📄 TransactionType.java
│   │   │   │   │   └── 📄 TransactionStatus.java
│   │   │   │   └── 📁 dto/
│   │   │   │       ├── 📄 TopupRequest.java
│   │   │   │       ├── 📄 PurchaseRequest.java
│   │   │   │       └── 📄 TransactionResponse.java
│   │   │   └── 📁 ledger/
│   │   │       ├── 📁 service/
│   │   │       │   ├── 📄 LedgerService.java
│   │   │       │   └── 📄 DoubleEntryService.java
│   │   │       ├── 📁 repository/
│   │   │       │   └── 📄 LedgerRepository.java
│   │   │       ├── 📁 model/
│   │   │       │   ├── 📄 Ledger.java
│   │   │       │   ├── 📄 LedgerEntry.java
│   │   │       │   └── 📄 AccountType.java
│   │   │       └── 📁 dto/
│   │   │
│   │   ├── 📁 config/                # ⚙️ Config Hub
│   │   │   ├── 📁 runtime/
│   │   │   │   ├── 📁 controller/
│   │   │   │   ├── 📁 service/
│   │   │   │   │   ├── 📄 RuntimeConfigService.java
│   │   │   │   │   └── 📄 ConfigCacheService.java
│   │   │   │   ├── 📁 repository/
│   │   │   │   │   └── 📄 RuntimeConfigRepository.java
│   │   │   │   ├── 📁 model/
│   │   │   │   │   ├── 📄 RuntimeConfig.java
│   │   │   │   │   ├── 📄 ConfigType.java
│   │   │   │   │   └── 📄 ConfigScope.java
│   │   │   │   └── 📁 dto/
│   │   │   │       ├── 📄 ConfigRequest.java
│   │   │   │       └── 📄 ConfigResponse.java
│   │   │   └── 📁 environment/
│   │   │       ├── 📁 service/
│   │   │       │   └── 📄 EnvironmentConfigService.java
│   │   │       ├── 📁 repository/
│   │   │       ├── 📁 model/
│   │   │       │   ├── 📄 EnvironmentConfig.java
│   │   │       │   └── 📄 Environment.java
│   │   │       └── 📁 dto/
│   │   │
│   │   └── 📁 message/               # 📨 Message Hub
│   │       ├── 📁 router/
│   │       │   ├── 📁 service/
│   │       │   │   ├── 📄 MessageRouterService.java
│   │       │   │   ├── 📄 RoutingDecisionService.java
│   │       │   │   └── 📄 SingleDecisionPointService.java
│   │       │   ├── 📁 model/
│   │       │   │   ├── 📄 Route.java
│   │       │   │   ├── 📄 RoutingRule.java
│   │       │   │   └── 📄 Destination.java
│   │       │   └── 📁 dto/
│   │       ├── 📁 decision/
│   │       │   ├── 📁 service/
│   │       │   │   ├── 📄 DecisionEngine.java
│   │       │   │   ├── 📄 HubSelectionService.java
│   │       │   │   └── 📄 MessageProcessor.java
│   │       │   ├── 📁 model/
│   │       │   │   ├── 📄 Decision.java
│   │       │   │   ├── 📄 DecisionType.java
│   │       │   │   └── 📄 ProcessingContext.java
│   │       │   └── 📁 dto/
│   │       ├── 📁 processor/
│   │       │   ├── 📁 service/
│   │       │   │   ├── 📄 MessageProcessorService.java
│   │       │   │   ├── 📄 MessageTransformer.java
│   │       │   │   └── 📄 MessageValidator.java
│   │       │   ├── 📁 model/
│   │       │   │   ├── 📄 ProcessingStep.java
│   │       │   │   └── 📄 ProcessorConfig.java
│   │       │   └── 📁 dto/
│   │       ├── 📁 store/
│   │       │   ├── 📁 controller/
│   │       │   ├── 📁 service/
│   │       │   ├── 📁 repository/
│   │       │   ├── 📁 model/
│   │       │   │   ├── 📄 Message.java
│   │       │   │   ├── 📄 Conversation.java
│   │       │   │   └── 📄 MessageType.java
│   │       │   └── 📁 dto/
│   │       └── 📁 grpc/
│   │           ├── 📄 MessageServiceGrpcImpl.java
│   │           └── 📄 MessageGrpcClient.java
│   │
│   ├── 📁 spokes/                  # 🔗 External Integrations
│   │   ├── 📁 facebook/            # Facebook integration
│   │   │   ├── 📁 webhook/
│   │   │   │   ├── 📁 controller/
│   │   │   │   │   └── 📄 FacebookWebhookController.java
│   │   │   │   ├── 📁 service/
│   │   │   │   │   ├── 📄 FacebookWebhookService.java
│   │   │   │   │   └── 📄 WebhookEventProcessor.java
│   │   │   │   ├── 📁 model/
│   │   │   │   │   ├── 📄 WebhookEvent.java
│   │   │   │   │   └── 📄 WebhookEventType.java
│   │   │   │   └── 📁 dto/
│   │   │   ├── 📁 connection/
│   │   │   │   ├── 📁 controller/
│   │   │   │   ├── 📁 service/
│   │   │   │   ├── 📁 repository/
│   │   │   │   ├── 📁 model/
│   │   │   │   │   ├── 📄 FacebookConnection.java
│   │   │   │   │   └── 📄 ConnectionStatus.java
│   │   │   │   └── 📁 dto/
│   │   │   ├── 📁 user/
│   │   │   │   ├── 📁 controller/
│   │   │   │   ├── 📁 service/
│   │   │   │   ├── 📁 repository/
│   │   │   │   ├── 📁 model/
│   │   │   │   │   └── 📄 FacebookUser.java
│   │   │   │   └── 📁 dto/
│   │   │   └── 📁 autoconnect/
│   │   │       ├── 📁 controller/
│   │   │       ├── 📁 service/
│   │   │       ├── 📁 model/
│   │   │       └── 📁 dto/
│   │   ├── 📁 botpress/            # Botpress integration
│   │   │   ├── 📁 api/
│   │   │   │   ├── 📁 service/
│   │   │   │   │   ├── 📄 BotpressApiService.java
│   │   │   │   │   └── 📄 BotpressClientService.java
│   │   │   │   ├── 📁 model/
│   │   │   │   │   ├── 📄 BotpressMessage.java
│   │   │   │   │   ├── 📄 BotpressResponse.java
│   │   │   │   │   └── 📄 BotpressBot.java
│   │   │   │   └── 📁 dto/
│   │   │   ├── 📁 auth/
│   │   │   │   ├── 📁 service/
│   │   │   │   │   └── 📄 BotpressAuthService.java
│   │   │   │   └── 📁 dto/
│   │   │   └── 📁 mapping/
│   │   │       ├── 📁 service/
│   │   │       │   └── 📄 UserIdMappingService.java
│   │   │       ├── 📁 repository/
│   │   │       ├── 📁 model/
│   │   │       │   └── 📄 UserIdMapping.java
│   │   │       └── 📁 dto/
│   │   ├── 📁 odoo/                # Odoo integration
│   │   │   ├── 📁 client/
│   │   │   │   ├── 📁 service/
│   │   │   │   │   └── 📄 OdooApiClient.java
│   │   │   │   └── 📁 dto/
│   │   │   └── 📁 service/
│   │   │       ├── 📁 CustomerDataService.java
│   │   │       ├── 📁 FbCapturedPhoneService.java
│   │   │       └── 📁 FbCustomerStagingCrudService.java
│   │   └── 📁 minio/               # MinIO integration
│   │       ├── 📁 storage/
│   │       │   ├── 📁 service/
│   │       │   │   └── 📄 MinioStorageService.java
│   │       │   └── 📁 dto/
│   │       └── 📁 service/
│   │           ├── 📁 FileMetadataService.java
│   │           ├── 📁 CategoryService.java
│   │           └── 📁 ImageProcessingService.java
│   │
│   └── 📁 shared/                 # 🔧 Shared Components
│       ├── 📁 security/            # Security components
│       │   ├── 📄 JwtFilter.java
│       │   ├── 📄 JwtUtils.java
│       │   ├── 📄 SecurityUtils.java
│       │   ├── 📄 RateLimitingFilter.java
│       │   └── 📄 HubSecurityInterceptor.java
│       ├── 📁 exceptions/          # Global exception handling
│       │   ├── 📄 GlobalExceptionHandler.java
│       │   ├── 📄 HubException.java
│       │   ├── 📄 ValidationException.java
│       │   ├── 📄 ResourceNotFoundException.java
│       │   └── 📄 UnauthorizedException.java
│       ├── 📁 dto/                # Common DTOs
│       │   ├── 📄 ApiResponse.java
│       │   ├── 📄 ErrorResponse.java
│       │   ├── 📄 PageResponse.java
│       │   ├── 📄 TenantContext.java
│       │   └── 📄 UserContext.java
│       ├── 📁 utils/               # Utility classes
│       │   ├── 📄 DateUtils.java
│       │   ├── 📄 StringUtils.java
│       │   ├── 📄 ValidationUtils.java
│       │   ├── 📄 JsonUtils.java
│       │   └── 📄 EncryptionUtils.java
│       ├── 📁 infrastructure/       # Infrastructure components
│       │   ├── 📄 BaseRepository.java
│       │   ├── 📄 BaseService.java
│       │   ├── 📄 BaseTenantEntity.java
│       │   ├── 📄 AuditEntity.java
│       │   └── 📄 BaseEntity.java
│       ├── 📁 constants/           # Constants and enums
│       │   ├── 📄 ApiConstants.java
│       │   ├── 📄 ErrorConstants.java
│       │   ├── 📄 CacheConstants.java
│       │   └── 📄 SystemConstants.java
│       ├── 📁 saga/               # Saga framework
│       │   ├── 📄 SagaManager.java
│       │   ├── 📄 SagaInstance.java
│       │   ├── 📄 SagaStep.java
│       │   ├── 📄 SagaDefinition.java
│       │   ├── 📄 CompensatingTransaction.java
│       │   └── 📄 SagaOrchestration.java
│       └── 📁 messaging/          # Message queue components
│           ├── 📄 RabbitMQConfig.java
│           ├── 📄 MessagePublisher.java
│           ├── 📄 MessageConsumer.java
│           ├── 📄 Event.java
│           ├── 📄 EventHandler.java
│           └── 📄 EventTypes.java
│
├── 📁 src/main/resources/
│   ├── 📄 application.yml          # Main configuration
│   ├── 📁 application-identity.yml  # Identity hub config
│   ├── 📁 application-tenant.yml   # Tenant hub config
│   ├── 📁 application-app.yml      # App hub config
│   ├── 📁 application-billing.yml  # Billing hub config
│   ├── 📁 application-wallet.yml   # Wallet hub config
│   ├── 📁 application-config.yml   # Config hub config
│   ├── 📁 application-message.yml  # Message hub config
│   ├── 📁 proto/                  # gRPC proto files
│   │   ├── 📄 identity-service.proto
│   │   ├── 📄 tenant-service.proto
│   │   ├── 📄 app-service.proto
│   │   ├── 📄 billing-service.proto
│   │   ├── 📄 wallet-service.proto
│   │   ├── 📄 config-service.proto
│   │   └── 📄 message-service.proto
│   └── 📁 static/                  # Static resources
│
├── 📁 src/test/java/com/chatbot/
│   ├── 📁 core/                   # Hub tests
│   │   ├── 📁 identity/
│   │   ├── 📁 tenant/
│   │   ├── 📁 app/
│   │   ├── 📁 billing/
│   │   ├── 📁 wallet/
│   │   ├── 📁 config/
│   │   └── 📁 message/
│   ├── 📁 spokes/                 # Spoke tests
│   │   ├── 📁 facebook/
│   │   ├── 📁 botpress/
│   │   ├── 📁 odoo/
│   │   └── 📁 minio/
│   ├── 📁 shared/                 # Shared tests
│   │   ├── 📁 security/
│   │   ├── 📁 utils/
│   │   └── 📁 saga/
│   └── 📁 integration/            # Integration tests
│       ├── 📁 grpc/
│       ├── 📁 database/
│       ├── 📁 messaging/
│       └── 📁 end-to-end/
│
└── 📁 .github/                   # GitHub workflows
    └── 📁 workflows/
        ├── 📄 ci.yml
        ├── 📄 cd.yml
        └── 📄 security-scan.yml
```

## 🎯 **Key Design Principles**

### **🔒 Core Hubs (v0.1 LOCKED)**
- **Identity Hub**: Authentication, JWT (NO tenant, NO role)
- **Tenant Hub**: Workspace, membership management
- **App Hub**: Enable/Disable apps, Guard functionality
- **Billing Hub**: Entitlement (READ-ONLY)
- **Wallet Hub**: Ledger TOPUP/PURCHASE operations
- **Config Hub**: Runtime configuration management
- **Message Hub**: Single Decision Point for routing

### **🔗 Spokes (External Integrations)**
- **Facebook**: Webhooks, connections, auto-connect
- **Botpress**: API integration, authentication, user mapping
- **Odoo**: ERP integration, customer data
- **MinIO**: File storage, image processing

### **🔧 Shared Components**
- **Security**: JWT, CORS, rate limiting, hub security
- **Exceptions**: Global error handling
- **DTOs**: Common data transfer objects
- **Utils**: Utility classes and helpers
- **Infrastructure**: Base classes, entities
- **Saga**: Distributed transaction management
- **Messaging**: Message queue components

## 📦 **Package Naming Convention**

```java
// Core Hubs
com.chatbot.core.identity.*
com.chatbot.core.tenant.*
com.chatbot.core.app.*
com.chatbot.core.billing.*
com.chatbot.core.wallet.*
com.chatbot.core.config.*
com.chatbot.core.message.*

// Spokes
com.chatbot.spokes.facebook.*
com.chatbot.spokes.botpress.*
com.chatbot.spokes.odoo.*
com.chatbot.spokes.minio.*

// Shared
com.chatbot.shared.security.*
com.chatbot.shared.exceptions.*
com.chatbot.shared.dto.*
com.chatbot.shared.utils.*
com.chatbot.shared.infrastructure.*
com.chatbot.shared.saga.*
com.chatbot.shared.messaging.*
```

## 🗄️ **Database Structure**

```
chatbot_identity_db    # Identity Hub
├── users
├── user_profiles
└── user_sessions

chatbot_tenant_db     # Tenant Hub
├── tenants
├── tenant_members
├── tenant_profiles
└── tenant_professionals

chatbot_app_db        # App Hub
├── app_registry
├── app_subscriptions
└── app_guards

chatbot_billing_db    # Billing Hub (READ-ONLY)
├── billing_accounts
├── billing_subscriptions
└── entitlements

chatbot_wallet_db     # Wallet Hub
├── wallets
├── transactions
└── ledger

chatbot_config_db     # Config Hub
├── runtime_configs
└── environment_configs

chatbot_message_db    # Message Hub
├── messages
├── conversations
└── routing_rules

chatbot_spokes_db     # Spokes
├── facebook_connections
├── facebook_users
├── botpress_mappings
├── odoo_customers
└── file_metadata
```

## 🚀 **Deployment Structure**

```
# Kubernetes Namespaces
chatbot-system/
├── identity-hub/
├── tenant-hub/
├── app-hub/
├── billing-hub/
├── wallet-hub/
├── config-hub/
├── message-hub/
├── facebook-spoke/
├── botpress-spoke/
├── odoo-spoke/
├── minio-spoke/
├── shared-services/
└── monitoring/
```

This structure provides:
- ✅ Clear separation of concerns
- ✅ Independent scaling capability
- ✅ Database isolation per hub
- ✅ gRPC communication between hubs
- ✅ Comprehensive testing strategy
- ✅ Production-ready deployment structure
