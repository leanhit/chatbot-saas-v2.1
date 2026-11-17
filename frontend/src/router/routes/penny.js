import { appname } from "./appname";

const BotManagement = () => import("../../views/penny/bots/BotManagement.vue");
const Connections = () => import("../../views/penny/connections/Connections.vue");
const Rules = () => import("../../views/penny/rules/Rules.vue");
const KnowledgeBaseList = () => import("../../views/penny/knowledge-base/KnowledgeBaseList.vue");
const PennyMetrics = () => import("../../views/penny/metrics/PennyMetrics.vue");
const PennyMonitoringDashboard = () => import("../../views/penny/metrics/PennyMonitoringDashboard.vue");
const PennyAnalyticsDashboard = () => import("../../views/penny/metrics/PennyAnalyticsDashboard.vue");
const EscalationTickets = () => import("../../views/penny/escalation/EscalationTickets.vue");
const PennyBotConfig = () => import("../../views/penny/config/PennyBotConfig.vue");

export default [
  { path: "/penny-bots", name: "penny-bots", component: BotManagement, meta: { requiresAuth: true, title: "Penny Bot Management" + appname } },
  { path: "/penny-connections", name: "penny-connections", component: Connections, meta: { requiresAuth: true, title: "Penny Connections" + appname } },
  { path: "/penny-rules", name: "penny-rules", component: Rules, meta: { requiresAuth: true, title: "Penny Rules" + appname } },
  { path: "/penny-knowledge-base", name: "penny-knowledge-base", component: KnowledgeBaseList, meta: { requiresAuth: true, title: "Knowledge Base" + appname } },
  { path: "/penny/bots/:botId/knowledge-base", name: "penny-knowledge-base-legacy", component: KnowledgeBaseList, props: true, meta: { requiresAuth: true, title: "Knowledge Base" + appname } },
  { path: "/penny/metrics", name: "penny-metrics", component: PennyMetrics, meta: { requiresAuth: true, title: "Penny Metrics" + appname } },
  { path: "/penny/monitoring", name: "penny-monitoring", component: PennyMonitoringDashboard, meta: { requiresAuth: true, title: "Penny Monitoring Dashboard" + appname } },
  { path: "/penny/analytics", name: "penny-analytics", component: PennyAnalyticsDashboard, meta: { requiresAuth: true, title: "Penny Analytics Dashboard" + appname } },
  { path: "/penny-escalation", name: "penny-escalation", component: EscalationTickets, meta: { requiresAuth: true, title: "Escalation Tickets" + appname } },
  { path: "/penny/bots/:botId/escalation", name: "penny-escalation-legacy", component: EscalationTickets, props: true, meta: { requiresAuth: true, title: "Escalation Tickets" + appname } },
  { path: "/penny-bot-config", name: "penny-bot-config", component: PennyBotConfig, meta: { requiresAuth: true, title: "Bot Configuration" + appname } },
  { path: "/penny/bots/:botId/config", name: "penny-bot-config-legacy", component: PennyBotConfig, props: true, meta: { requiresAuth: true, title: "Bot Configuration" + appname } },
];
