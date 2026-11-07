// src/trainingApi.js
import axios from '@/plugins/axios';

export const trainingApi = {
  // Training Materials
  // READ: Get all training materials
  getAllMaterials(params) {
    return axios.get('/training/materials', { params });
  },

  // READ: Get a training material by ID
  getMaterialById(id) {
    return axios.get(`/training/materials/${id}`);
  },

  // CREATE: Create a new training material
  createMaterial(data) {
    return axios.post('/training/materials', data);
  },

  // UPDATE: Update a training material
  updateMaterial(id, data) {
    return axios.put(`/training/materials/${id}`, data);
  },

  // DELETE: Delete a training material
  deleteMaterial(id) {
    return axios.delete(`/training/materials/${id}`);
  },

  // Training Categories
  // READ: Get all training categories
  getAllCategories() {
    return axios.get('/training/categories');
  },

  // READ: Get a training category by ID
  getCategoryById(id) {
    return axios.get(`/training/categories/${id}`);
  },

  // CREATE: Create a new training category
  createCategory(data) {
    return axios.post('/training/categories', data);
  },

  // UPDATE: Update a training category
  updateCategory(id, data) {
    return axios.put(`/training/categories/${id}`, data);
  },

  // DELETE: Delete a training category
  deleteCategory(id) {
    return axios.delete(`/training/categories/${id}`);
  },

  // Agent Training Progress
  // READ: Get training progress for an agent
  getAgentProgress(agentId) {
    return axios.get(`/training/progress/agent/${agentId}`);
  },

  // UPDATE: Update training progress
  updateProgress(progressId, data) {
    return axios.put(`/training/progress/${progressId}`, data);
  },

  // Agent Onboarding
  // READ: Get all onboarding tasks
  getAllOnboardingTasks() {
    return axios.get('/training/onboarding/tasks');
  },

  // READ: Get onboarding progress for an agent
  getAgentOnboardingProgress(agentId) {
    return axios.get(`/training/onboarding/progress/agent/${agentId}`);
  },

  // UPDATE: Start an onboarding task
  startOnboardingTask(taskId, agentId) {
    return axios.post(`/training/onboarding/tasks/${taskId}/start`, { agentId });
  },

  // UPDATE: Complete an onboarding task
  completeOnboardingTask(agentId, taskId, notes = '') {
    return axios.post('/onboarding/progress/complete', null, {
      params: { agentId, taskId, notes }
    });
  },

  // Agent Skills
  // READ: Get skills for an agent
  getAgentSkills(agentId) {
    return axios.get(`/training/skills/agent/${agentId}`);
  },

  // UPDATE: Update agent skill
  updateAgentSkill(skillId, data) {
    return axios.put(`/training/skills/${skillId}`, data);
  },

  // Certifications
  // READ: Get certification levels
  getCertificationLevels() {
    return axios.get('/training/certifications/levels')
  },

  getAgentCertifications(agentId) {
    return axios.get(`/training/certifications/agent/${agentId}`)
  },

  awardCertification(data) {
    return axios.post('/training/certifications/award', data)
  },

  // Training Dashboard
  getTrainingDashboardStats() {
    return axios.get('/training/dashboard/stats')
  },

  getAgentsWithTrainingStats() {
    return axios.get('/training/dashboard/agents')
  },

  // Material Progress
  getMaterialProgress(materialId) {
    return axios.get(`/training/progress/material/${materialId}`)
  }
};
