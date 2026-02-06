<template>
  <div class="backend-test">
    <h2>Backend Connection Test</h2>
    <p>Status: {{ status }}</p>
    <p v-if="message">Message: {{ message }}</p>
    <button @click="testConnection">Test Connection</button>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import api from '@/services/api';

const status = ref<string>('Testing...');
const message = ref<string>('');

const testConnection = async () => {
  try {
    console.log('Testing backend connection...');
    
    // Test with POST to auth/login endpoint with dummy credentials
    const response = await api.post('/auth/login', {
      email: "test@test.com",
      password: "invalid"
    });
    
    console.log('Backend response:', response);
    status.value = 'Backend connected';
    message.value = JSON.stringify(response.data);
  } catch (error: any) {
    console.error('Backend connection test:', error);
    
    // Check if we reached the backend successfully
    if (error.response) {
      // Backend responded - connection is working
      const statusCode = error.response.status;
      console.log('Backend HTTP status:', statusCode);
      
      if (statusCode === 401 || statusCode === 400 || statusCode === 405) {
        status.value = 'Backend connected';
        message.value = `Backend reachable (HTTP ${statusCode})`;
      } else {
        status.value = 'Backend connected';
        message.value = `Backend reachable (HTTP ${statusCode})`;
      }
    } else if (error.request) {
      // Network error - no response received
      status.value = 'Connection failed';
      message.value = 'Network error - backend not reachable';
    } else {
      // Other error
      status.value = 'Connection failed';
      message.value = error.message || 'Unknown error';
    }
  }
};

onMounted(() => {
  testConnection();
});
</script>

<style scoped>
.backend-test {
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 8px;
  margin: 20px;
}

button {
  padding: 10px 20px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

button:hover {
  background-color: #0056b3;
}
</style>
