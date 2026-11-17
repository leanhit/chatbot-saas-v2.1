import { appname } from "./appname";

const QueueView = () => import("../../views/routing/QueueView.vue");
const SupervisorDashboard = () => import("../../views/routing/SupervisorDashboard.vue");
const RoutingSimulation = () => import("../../views/routing/RoutingSimulation.vue");
const EscalationHistory = () => import("../../views/routing/EscalationHistory.vue");
const TransferHistory = () => import("../../views/routing/TransferHistory.vue");
const AgentAssignment = () => import("../../views/routing/AgentAssignment.vue");

export default [
  { path: "/routing/queue", name: "routing-queue", component: QueueView, meta: { requiresAuth: true, title: "Queue View" + appname } },
  { path: "/routing/supervisor", name: "routing-supervisor", component: SupervisorDashboard, meta: { requiresAuth: true, title: "Supervisor Dashboard" + appname } },
  { path: "/routing/simulation", name: "routing-simulation", component: RoutingSimulation, meta: { requiresAuth: true, title: "Routing Simulation" + appname } },
  { path: "/routing/escalation", name: "routing-escalation", component: EscalationHistory, meta: { requiresAuth: true, title: "Escalation Rules" + appname } },
  { path: "/routing/transfers", name: "routing-transfers", component: TransferHistory, meta: { requiresAuth: true, title: "Transfer History" + appname } },
  { path: "/routing/assignment", name: "routing-assignment", component: AgentAssignment, meta: { requiresAuth: true, title: "Agent Assignment" + appname } },
];
