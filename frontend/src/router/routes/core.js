import { appname } from "./appname";

const Dashboard = () => import("../../views/Dashboard.vue");
const Login = () => import("../../views/auth/Login.vue");
const Register = () => import("../../views/auth/Register.vue");
const Profile = () => import("../../views/profile/Profile.vue");
const Tenant = () => import("../../views/tenant/gateway/Gateway.vue");
const Help = () => import("../../views/help/Help.vue");
const ForgotPassword = () => import("../../views/auth/ForgotPassword.vue");
const Notifications = () => import("../../views/notification/Notifications.vue");

export default [
  { path: "/", redirect: "/login" },
  { path: "/login", name: "login", component: Login, meta: { hideNav: true } },
  { path: "/register", name: "register", component: Register, meta: { hideNav: true } },
  { path: "/auth/forgot-password", name: "forgot-password", component: ForgotPassword, meta: { hideNav: true } },
  { path: "/tenant-gateway", name: "tenant-gateway", component: Tenant, meta: { requiresAuth: true, hideNav: true } },
  { path: "/profile", name: "profile", component: Profile, meta: { requiresAuth: true, title: "Profile" + appname, skipTenantCheck: true } },
  { path: "/help", name: "help", component: Help, meta: { requiresAuth: true, title: "Help Center" + appname, skipTenantCheck: true } },
  { path: "/notifications", name: "notifications", component: Notifications, meta: { requiresAuth: true, title: "Notification Center" + appname, skipTenantCheck: true } },
  { path: "/dashboard", name: "dashboard", component: Dashboard, meta: { requiresAuth: true, title: "Dashboard" + appname } },
];
