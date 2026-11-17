import { appname } from "./appname";

const CustomerProfiles = () => import("../../views/customer/CustomerProfiles.vue");
const CustomerProfileDetail = () => import("../../views/customer/CustomerProfileDetail.vue");
const FeedbackSurvey = () => import("../../views/customer/FeedbackSurvey.vue");
const CustomerAnalytics = () => import("../../views/customer/CustomerAnalytics.vue");
const VIPQueue = () => import("../../views/customer/VIPQueue.vue");
const SegmentOverview = () => import("../../views/customer/SegmentOverview.vue");

export default [
  { path: "/customer/profiles", name: "customer-profiles", component: CustomerProfiles, meta: { requiresAuth: true, title: "Customer Profiles" + appname } },
  { path: "/customer/profiles/:id", name: "customer-profile-detail", component: CustomerProfileDetail, props: true, meta: { requiresAuth: true, title: "Customer Detail" + appname } },
  { path: "/customer/feedback", name: "customer-feedback", component: FeedbackSurvey, meta: { requiresAuth: true, title: "Feedback Survey" + appname } },
  { path: "/customer/analytics", name: "customer-analytics", component: CustomerAnalytics, meta: { requiresAuth: true, title: "Customer Analytics" + appname } },
  { path: "/customer/vip-queue", name: "customer-vip-queue", component: VIPQueue, meta: { requiresAuth: true, title: "VIP Queue" + appname } },
  { path: "/customer/segments", name: "customer-segments", component: SegmentOverview, meta: { requiresAuth: true, title: "Customer Segments" + appname } },
];
