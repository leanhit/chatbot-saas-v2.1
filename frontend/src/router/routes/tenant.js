import { appname } from "./appname";

const TenantOverview = () => import("../../views/tenant/overview/TenantOverview.vue");
const TenantMember = () => import("../../views/tenant/member/TenantMember.vue");
const TenantSettings = () => import("../../views/tenant/settings/TenantSettings.vue");
const TenantWebhooks = () => import("../../views/tenant/settings/TenantWebhooks.vue");
const TenantApiKeys = () => import("../../views/tenant/settings/TenantApiKeys.vue");
const TwoFactorSetup = () => import("../../views/settings/TwoFactorSetup.vue");
const GdprCenter = () => import("../../views/settings/GdprCenter.vue");
const AgentManagement = () => import("../../views/tenant/agent/AgentManagement.vue");
const AuditLog = () => import("../../views/tenant/AuditLog.vue");

export default [
  { path: "/tenant/overview", name: "tenant-overview", component: TenantOverview, meta: { requiresAuth: true, title: "Tenant Overview" + appname } },
  { path: "/tenant/members", name: "tenant-members", component: TenantMember, meta: { requiresAuth: true, title: "Tenant Members" + appname } },
  { path: "/tenant/settings", name: "tenant-settings", component: TenantSettings, meta: { requiresAuth: true, title: "Tenant Settings" + appname } },
  { path: "/tenant/settings/webhooks", name: "tenant-webhooks", component: TenantWebhooks, meta: { requiresAuth: true, title: "Outbound Webhooks" + appname } },
  { path: "/tenant/settings/api-keys", name: "tenant-api-keys", component: TenantApiKeys, meta: { requiresAuth: true, title: "API Keys" + appname } },
  { path: "/settings/2fa", name: "two-factor-setup", component: TwoFactorSetup, meta: { requiresAuth: true, title: "Two-Factor Authentication" + appname } },
  { path: "/settings/gdpr", name: "gdpr-center", component: GdprCenter, meta: { requiresAuth: true, title: "GDPR & Data Privacy" + appname } },
  { path: "/tenant/agents", name: "tenant-agents", component: AgentManagement, meta: { requiresAuth: true, title: "Agent Management" + appname } },
  { path: "/tenant/audit-log", name: "audit-log", component: AuditLog, meta: { requiresAuth: true, title: "Audit Log" + appname } },
];
