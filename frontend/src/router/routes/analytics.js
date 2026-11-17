import { appname } from "./appname";

const FunnelAnalysis = () => import("../../views/analytics/FunnelAnalysis.vue");
const CustomReports = () => import("../../views/analytics/CustomReports.vue");
const CustomerJourney = () => import("../../views/analytics/CustomerJourney.vue");

export default [
  { path: "/analytics/funnel", name: "analytics-funnel", component: FunnelAnalysis, meta: { requiresAuth: true, title: "Funnel Analysis" + appname } },
  { path: "/analytics/reports", name: "analytics-reports", component: CustomReports, meta: { requiresAuth: true, title: "Custom Reports" + appname } },
  { path: "/analytics/journey", name: "analytics-journey", component: CustomerJourney, meta: { requiresAuth: true, title: "Customer Journey" + appname } },
];
