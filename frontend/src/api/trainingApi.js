// src/api/trainingApi.js
import axios from '@/plugins/axios';

export const trainingApi = {
  // Training Materials
  // READ: Get all training materials
  getAllMaterials(params) {
    return axios.get('/api/training/materials', { params });
  },

  // READ: Get a training material by ID
  getMaterialById(id) {
    return axios.get(`/api/training/materials/${id}`);
  },

  // CREATE: Create a new training material
  createMaterial(data) {
    return axios.post('/api/training/materials', data);
  },

  // UPDATE: Update a training material
  updateMaterial(id, data) {
    return axios.put(`/api/training/materials/${id}`, data);
  },

  // DELETE: Delete a training material
  deleteMaterial(id) {
    return axios.delete(`/api/training/materials/${id}`);
  },

  // Training Categories
  // READ: Get all training categories
  getAllCategories() {
    return axios.get('/api/training/categories');
  },

  // READ: Get a training category by ID
  getCategoryById(id) {
    return axios.get(`/api/training/categories/${id}`);
  },

  // CREATE: Create a new training category
  createCategory(data) {
    return axios.post('/api/training/categories', data);
  },

  // UPDATE: Update a training category
  updateCategory(id, data) {
    return axios.put(`/api/training/categories/${id}`, data);
  },

  // DELETE: Delete a training category
  deleteCategory(id) {
    return axios.delete(`/api/training/categories/${id}`);
  },

  // Agent Training Progress
  // READ: Get training progress for an agent
  getAgentProgress(agentId) {
    return axios.get(`/api/training/progress/agent/${agentId}`);
  },

  // UPDATE: Update training progress
  updateProgress(progressId, data) {
    return axios.put(`/api/training/progress/${progressId}`, data);
  },

  // Agent Onboarding
  // READ: Get all onboarding tasks
  getAllOnboardingTasks() {
    return axios.get('/api/training/onboarding/tasks');
  },

  // READ: Get onboarding progress for an agent
  getAgentOnboardingProgress(agentId) {
    return axios.get(`/api/training/onboarding/progress/agent/${agentId}`);
  },

  // UPDATE: Start an onboarding task
  startOnboardingTask(taskId, agentId) {
    return axios.post(`/api/training/onboarding/tasks/${taskId}/start`, { agentId });
  },

  // Agent Skills
  // READ: Get skills for an agent
  getAgentSkills(agentId) {
    return axios.get(`/api/training/skills/agent/${agentId}`);
  },

  // UPDATE: Update agent skill
  updateAgentSkill(skillId, data) {
    return axios.put(`/api/training/skills/${skillId}`, data);
  },

  // Certifications
  // READ: Get certification levels
  getCertificationLevels() {
    return axios.get('/api/training/certifications/levels')
  },

  getAgentCertifications(agentId) {
    return axios.get(`/api/training/certifications/agent/${agentId}`)
  },

  awardCertification(data) {
    return axios.post('/api/training/certifications/award', data)
  },

  // Training Dashboard
  getTrainingDashboardStats() {
    return axios.get('/api/training/dashboard/stats')
  },

  getAgentsWithTrainingStats() {
    return axios.get('/api/training/dashboard/agents')
  },

  // Material Progress
  getMaterialProgress(materialId) {
    return axios.get(`/api/training/progress/material/${materialId}`)
  }
};
