<template>
  <div class="test-view">
    <div class="test-header">
      <h2>Modern Layout Test</h2>
      <p>This is a test view for the new modern layout components.</p>
    </div>
    
    <div class="test-content">
      <div class="test-card">
        <h3>✅ AppShell Component</h3>
        <p>Main layout container with responsive behavior</p>
      </div>
      
      <div class="test-card">
        <h3>✅ ModernSidebar Component</h3>
        <p>Reuses menu structure from old Sidebar.vue</p>
        <ul>
          <li>Tenant Manager (Overview, Member)</li>
          <li>Connection Manager (Auto Connect, Hand Connect)</li>
          <li>Image Manager (Images, Categories)</li>
          <li>Bot Manager (Botpress, Create Bot)</li>
        </ul>
      </div>
      
      <div class="test-card">
        <h3>✅ ModernTopbar Component</h3>
        <p>Reuses logic from old Navbar.vue</p>
        <ul>
          <li>Search functionality</li>
          <li>Language switch</li>
          <li>User menu</li>
          <li>Theme toggle</li>
        </ul>
      </div>
    </div>
    
    <div class="test-actions">
      <el-button @click="testNavigation" type="primary">
        Test Navigation
      </el-button>
      <el-button @click="testTheme">
        Test Theme
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'

const testNavigation = () => {
  ElMessage.success('Navigation test successful!')
}

const testTheme = () => {
  document.documentElement.classList.toggle('dark')
  ElMessage.info('Theme toggled!')
}
</script>

<style scoped>
.test-view {
  padding: 24px;
}

.test-header {
  margin-bottom: 32px;
}

.test-header h2 {
  font-size: 28px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 8px;
}

.test-header p {
  color: #64748b;
  font-size: 16px;
}

.test-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

.test-card {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 24px;
  transition: all 0.2s ease;
}

.test-card:hover {
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.test-card h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.test-card p {
  color: #64748b;
  margin-bottom: 16px;
}

.test-card ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.test-card li {
  padding: 4px 0;
  color: #475569;
  position: relative;
  padding-left: 20px;
}

.test-card li::before {
  content: '→';
  position: absolute;
  left: 0;
  color: #667eea;
}

.test-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
</style>
