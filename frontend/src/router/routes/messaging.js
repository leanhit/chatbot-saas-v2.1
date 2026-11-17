import { appname } from "./appname";

const Messages = () => import("../../views/messages/chat/Chat.vue");
const CustomerData = () => import("../../views/customers/CustomerData.vue");

export default [
  { path: "/messages", name: "messages", component: Messages, meta: { requiresAuth: true, title: "Messages" + appname } },
  { path: "/customers", name: "customers", component: CustomerData, meta: { requiresAuth: true, title: "Customer Data" + appname } },
];
