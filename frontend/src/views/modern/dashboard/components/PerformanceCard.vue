<template>
  <div class="performance-card">
    <div class="card-header">
      <h3 class="card-title">{{ title }}</h3>
      <div class="time-filter">
        <el-button-group>
          <el-button 
            v-for="period in timePeriods" 
            :key="period.value"
            :type="selectedPeriod === period.value ? 'primary' : 'default'"
            size="small"
            @click="selectedPeriod = period.value"
          >
            {{ period.label }}
          </el-button>
        </el-button-group>
      </div>
    </div>
    
    <div class="chart-container">
      <div class="chart-placeholder">
        <div class="chart-bars">
          <div 
            v-for="(dataset, datasetIndex) in chartData.datasets" 
            :key="datasetIndex"
            class="chart-dataset"
          >
            <div class="dataset-label">
              <div 
                class="dataset-color" 
                :style="{ background: dataset.color }"
              ></div>
              <span>{{ dataset.label }}</span>
            </div>
            <div class="bars">
              <div 
                v-for="(value, index) in dataset.data" 
                :key="index"
                class="bar"
                :style="{ 
                  height: `${(value / maxValue) * 100}%`,
                  background: dataset.color 
                }"
              ></div>
            </div>
          </div>
        </div>
        
        <div class="chart-labels">
          <div 
            v-for="label in chartData.labels" 
            :key="label"
            class="chart-label"
          >
            {{ label }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

interface Dataset {
  label: string
  data: number[]
  color: string
}

interface ChartData {
  labels: string[]
  datasets: Dataset[]
}

interface Props {
  title: string
  chartData: ChartData
}

const props = defineProps<Props>()

const selectedPeriod = ref('7d')

const timePeriods = ref([
  { label: '7D', value: '7d' },
  { label: '1M', value: '1m' },
  { label: '3M', value: '3m' }
])

const maxValue = computed(() => {
  const allValues = props.chartData.datasets.flatMap(dataset => dataset.data)
  return Math.max(...allValues, 100) // Minimum value of 100 for better visualization
})
</script>

<style scoped>
.performance-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  height: 100%;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f1f5f9;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.chart-container {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 200px;
}

.chart-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.chart-bars {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.chart-dataset {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.dataset-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  font-weight: 500;
  color: #64748b;
}

.dataset-color {
  width: 12px;
  height: 12px;
  border-radius: 2px;
}

.bars {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  height: 60px;
}

.bar {
  flex: 1;
  min-height: 4px;
  border-radius: 2px 2px 0 0;
  transition: height 0.3s ease;
  position: relative;
}

.bar:hover {
  opacity: 0.8;
}

.chart-labels {
  display: flex;
  justify-content: space-between;
  padding: 0 4px;
}

.chart-label {
  font-size: 11px;
  color: #94a3b8;
  text-align: center;
  flex: 1;
}

/* Responsive */
@media (max-width: 768px) {
  .performance-card {
    padding: 20px;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .bars {
    height: 40px;
    gap: 4px;
  }
  
  .chart-label {
    font-size: 10px;
  }
}
</style>
