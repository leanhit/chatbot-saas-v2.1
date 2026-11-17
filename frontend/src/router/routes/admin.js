import { appname } from "./appname";

const BankAccountManagement = () => import("../../views/admin/BankAccountManagement.vue");
const PackageManagement = () => import("../../views/admin/PackageManagement.vue");
const DiscountManagement = () => import("../../views/admin/DiscountManagement.vue");
const PaymentAnalytics = () => import("../../views/admin/PaymentAnalytics.vue");
const WebhookManagement = () => import("../../views/admin/WebhookManagement.vue");
const UserManagement = () => import("../../views/admin/UserManagement.vue");
const SkillsManagement = () => import("../../views/admin/SkillsManagement.vue");
const RoutingRules = () => import("../../views/admin/RoutingRules.vue");
const SLAMonitoring = () => import("../../views/admin/SLAMonitoring.vue");
const EscalationTiers = () => import("../../views/admin/EscalationTiers.vue");

export default [
  { path: "/admin/bank-account", name: "admin-bank-account", component: BankAccountManagement, meta: { requiresAuth: true, requiresSystemAdmin: true, title: "Bank Account Management" + appname } },
  { path: "/admin/packages", name: "admin-packages", component: PackageManagement, meta: { requiresAuth: true, requiresSystemAdmin: true, title: "Package Management" + appname } },
  { path: "/admin/discounts", name: "admin-discounts", component: DiscountManagement, meta: { requiresAuth: true, requiresSystemAdmin: true, title: "Discount Management" + appname } },
  { path: "/admin/analytics", name: "admin-analytics", component: PaymentAnalytics, meta: { requiresAuth: true, requiresSystemAdmin: true, title: "Payment Analytics" + appname } },
  { path: "/admin/webhooks", name: "admin-webhooks", component: WebhookManagement, meta: { requiresAuth: true, requiresSystemAdmin: true, title: "Webhook Management" + appname } },
  { path: "/admin/users", name: "admin-users", component: UserManagement, meta: { requiresAuth: true, requiresSystemAdmin: true, title: "User Management" + appname } },
  { path: "/admin/skills", name: "admin-skills", component: SkillsManagement, meta: { requiresAuth: true, requiresSystemAdmin: true, title: "Skills Management" + appname } },
  { path: "/admin/routing-rules", name: "admin-routing-rules", component: RoutingRules, meta: { requiresAuth: true, requiresSystemAdmin: true, title: "Routing Rules" + appname } },
  { path: "/admin/sla-monitoring", name: "admin-sla-monitoring", component: SLAMonitoring, meta: { requiresAuth: true, requiresSystemAdmin: true, title: "SLA Monitoring" + appname } },
  { path: "/admin/escalation-tiers", name: "admin-escalation-tiers", component: EscalationTiers, meta: { requiresAuth: true, requiresSystemAdmin: true, title: "Escalation Tiers" + appname } },
];
