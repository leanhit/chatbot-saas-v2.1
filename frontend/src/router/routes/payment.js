import { appname } from "./appname";

const PaymentDeposit = () => import("../../views/payment/Deposit.vue");
const PaymentHistory = () => import("../../views/payment/History.vue");

export default [
  { path: "/payment/deposit", name: "payment-deposit", component: PaymentDeposit, meta: { requiresAuth: true, requiresOwner: true, title: "Payment Deposit" + appname } },
  { path: "/payment/history", name: "payment-history", component: PaymentHistory, meta: { requiresAuth: true, requiresOwner: true, title: "Payment History" + appname } },
];
