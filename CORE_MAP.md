# Core Map - SaaS Platform v0.1

## Purpose of Core Map

This document defines the frozen architecture boundary of the SaaS Platform v0.1. It serves as the authoritative reference for what exists in the core infrastructure versus what is intentionally deferred to future phases.

**Core ≠ Feature.** Core provides the foundation; features are built upon it.

---

## Architecture Overview

### Hub & Spoke Model

The platform follows a Hub & Spoke architecture where:

- **Hubs** provide foundational services (Identity, Tenant, App, Billing, Wallet, Config)
- **Spokes** are SaaS applications that consume hub services (Chatbot AI, ERP, CRM)
- **Clear boundaries** prevent cross-contamination between hubs
- **Multi-tenancy** is built into the foundation

### Design Principles

- **Separation of Concerns:** Each hub has a single, well-defined responsibility
- **Read-Only Policies:** Critical hubs (Billing, Config) maintain data integrity
- **Ledger-Based Operations:** Financial operations use immutable ledgers
- **Centralized Configuration:** All configuration flows through Config Hub

---

## Core Hubs

### Identity Hub ✅

**Responsibility:** User authentication and JWT management

**What Exists:**
- User entity with email, status, locale metadata
- Credential management with password hashing
- JWT token generation and validation
- User-tenant relationship mapping
- Thread-local security context

**Boundary:** Identity only handles authentication. No tenant management, no business logic.

### Tenant Hub ✅

**Responsibility:** Multi-tenant workspace management

**What Exists:**
- Tenant entity with UUID-based identification
- Workspace configuration (name, status, default locale)
- Member management with role-based access
- Invitation system for member onboarding
- Multi-tenancy infrastructure (filters, context)

**Boundary:** Tenant management only. No authentication, no app-specific logic.

### App Hub ✅

**Responsibility:** Application enable/disable and access control

**What Exists:**
- App registry (CHATBOT, ERP, CRM)
- Tenant-app relationship management
- Access control guard framework
- Pricing resolution infrastructure
- Exception-based authorization flow

**Boundary:** App management and access control only. No business logic implementation.

### Billing Hub ✅

**Responsibility:** Plan and entitlement management (READ-ONLY)

**What Exists:**
- Plan registry (FREE, PRO, ENTERPRISE)
- Tenant plan assignment
- App entitlement validation
- Plan-based feature access control

**Boundary:** READ-ONLY policy enforcement. No payment processing, no money handling.

### Wallet Service ✅

**Responsibility:** Ledger-based balance management

**What Exists:**
- Tenant wallet with balance tracking
- Immutable transaction ledger
- TOPUP and PURCHASE operations
- Balance validation and enforcement

**Boundary:** Ledger operations only. No payment processing, no external financial integration.

### Config Hub ✅

**Responsibility:** Centralized configuration management (READ-ONLY)

**What Exists:**
- Multi-scope configuration (SYSTEM, TENANT, APP, TENANT_APP)
- Hierarchical configuration fallback
- READ-ONLY configuration API
- Default system configurations

**Boundary:** Configuration storage and retrieval only. No business logic, no feature flags.

---

## App Layer

### Current State ⚠️

**Infrastructure Exists:**
- App enable/disable framework
- Access control guards
- Pricing resolution system
- Configuration integration points

**Business Logic Deferred:**
- Chatbot AI implementation
- ERP system integration
- CRM functionality
- Application-specific business rules

### Architecture Intent

The app layer is designed as a clean separation where:
- Core hubs provide foundational services
- Apps consume hub services through well-defined APIs
- Each app is an independent spoke
- No app logic contaminates core hubs

---

## Explicit Non-Goals v0.1

### Payment Processing
- Billing Hub remains READ-ONLY
- No payment gateway integration
- No subscription management
- No invoicing or receipt generation

### Advanced Features
- No user profile management
- No advanced tenant customization
- No workflow automation
- No analytics or reporting

### External Integrations
- No third-party service integrations
- No webhook systems
- No API gateway features
- No external authentication providers

### Feature Management
- No feature flag engines
- No A/B testing framework
- No dynamic configuration changes
- No runtime behavior modification

---

## Architecture Freeze Statement

### v0.1 Boundary

The core architecture defined in this document is **FROZEN**. This means:

- **No structural changes** to hub boundaries
- **No new hubs** without architectural review
- **No cross-hub dependencies** without explicit approval
- **No business logic** in core hubs

### Evolution Path

Future development will:
- **Build upon** existing core infrastructure
- **Respect** established hub boundaries
- **Maintain** separation of concerns
- **Follow** the Hub & Spoke pattern

### Compliance Requirements

All future development must:
- Reference this Core Map for architectural guidance
- Maintain hub boundary integrity
- Use Config Hub for configuration needs
- Follow established patterns for new app spokes

---

## Conclusion

The v0.1 Core Map establishes a solid foundation for a scalable SaaS platform. The Hub & Spoke architecture ensures clean separation of concerns while providing the necessary infrastructure for multi-tenant applications.

**Core is the foundation. Features are the building blocks.** This distinction ensures architectural stability while enabling rapid application development.

*This document represents the frozen state of v0.1 architecture. Any deviations require formal architectural review.*
