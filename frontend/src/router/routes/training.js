import { appname } from "./appname";

const TrainingLibrary = () => import("../../views/training/TrainingLibrary.vue");
const TrainingDetail = () => import("../../views/training/TrainingDetail.vue");
const AgentTrainingDashboard = () => import("../../views/training/AgentTrainingDashboard.vue");
const OnboardingWizard = () => import("../../views/training/OnboardingWizard.vue");
const SkillMatrix = () => import("../../views/training/SkillMatrix.vue");
const CertificationOverview = () => import("../../views/training/CertificationOverview.vue");

export default [
  { path: "/training/library", name: "training-library", component: TrainingLibrary, meta: { requiresAuth: true, title: "Training Library" + appname } },
  { path: "/training/detail/:id", name: "training-detail", component: TrainingDetail, props: true, meta: { requiresAuth: true, title: "Training Detail" + appname } },
  { path: "/training/dashboard", name: "training-dashboard", component: AgentTrainingDashboard, meta: { requiresAuth: true, title: "Agent Training Dashboard" + appname } },
  { path: "/training/onboarding", name: "onboarding-wizard", component: OnboardingWizard, meta: { requiresAuth: true, title: "Onboarding Wizard" + appname } },
  { path: "/training/skills", name: "skill-matrix", component: SkillMatrix, meta: { requiresAuth: true, title: "Skill Matrix" + appname } },
  { path: "/training/certifications", name: "certification-overview", component: CertificationOverview, meta: { requiresAuth: true, title: "Certification Overview" + appname } },
];
