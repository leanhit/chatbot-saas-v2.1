<template>
  <div class="image-cropper-overlay" v-if="isVisible" @click.self="handleCancel">
    <div class="image-cropper-modal">
      <div class="image-cropper-header">
        <h3>{{ title }}</h3>
        <button @click="handleCancel" class="btn-close">
          <Icon icon="mdi:close" class="h-5 w-5" />
        </button>
      </div>
      
      <div class="image-cropper-content">
        <div class="cropper-container" ref="cropperContainer">
          <div class="cropper-wrapper">
            <img
              ref="imageElement"
              :src="imageUrl"
              @load="onImageLoad"
              @error="onImageError"
              class="cropper-image"
              :style="{ transform: `scale(${zoom}) translate(${translateX}px, ${translateY}px)` }"
            />
            
            <!-- Crop Square Overlay -->
            <div
              class="crop-square"
              :style="cropSquareStyle"
              @mousedown="startDrag"
            >
              <!-- Corner Handles -->
              <div class="handle handle-tl" @mousedown.stop="startResize('tl')"></div>
              <div class="handle handle-tr" @mousedown.stop="startResize('tr')"></div>
              <div class="handle handle-bl" @mousedown.stop="startResize('bl')"></div>
              <div class="handle handle-br" @mousedown.stop="startResize('br')"></div>
              
              <!-- Edge Handles -->
              <div class="handle handle-t" @mousedown.stop="startResize('t')"></div>
              <div class="handle handle-r" @mousedown.stop="startResize('r')"></div>
              <div class="handle handle-b" @mousedown.stop="startResize('b')"></div>
              <div class="handle handle-l" @mousedown.stop="startResize('l')"></div>
              
              <!-- Center Grid -->
              <div class="grid-lines">
                <div class="grid-line horizontal"></div>
                <div class="grid-line horizontal"></div>
                <div class="grid-line vertical"></div>
                <div class="grid-line vertical"></div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- Controls -->
        <div class="cropper-controls">
          <div class="zoom-controls">
            <button @click="zoomOut" class="btn-zoom" :disabled="zoom <= 0.5">
              <Icon icon="mdi:magnify-minus" class="h-4 w-4" />
            </button>
            <span class="zoom-label">{{ Math.round(zoom * 100) }}%</span>
            <button @click="zoomIn" class="btn-zoom" :disabled="zoom >= 3">
              <Icon icon="mdi:magnify-plus" class="h-4 w-4" />
            </button>
          </div>
          
          <div class="action-buttons">
            <button @click="handleCancel" class="btn-cancel">
              Cancel
            </button>
            <button @click="handleCrop" class="btn-crop" :disabled="!imageLoaded">
              Crop & Upload
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { Icon } from '@iconify/vue'

export default {
  name: 'ImageCropper',
  components: {
    Icon
  },
  props: {
    isVisible: {
      type: Boolean,
      default: false
    },
    title: {
      type: String,
      default: 'Crop Image'
    },
    imageUrl: {
      type: String,
      required: true
    },
    outputSize: {
      type: Number,
      default: 800 // Default 800x800 pixels
    }
  },
  emits: ['cancel', 'crop'],
  setup(props, { emit }) {
    // Refs
    const imageElement = ref(null)
    const cropperContainer = ref(null)
    
    // State
    const imageLoaded = ref(false)
    const imageError = ref(false)
    const zoom = ref(1)
    const translateX = ref(0)
    const translateY = ref(0)
    
    // Crop square properties
    const cropSize = ref(200)
    const cropX = ref(0)
    const cropY = ref(0)
    
    // Drag and resize state
    const isDragging = ref(false)
    const isResizing = ref(false)
    const resizeHandle = ref(null)
    const dragStartX = ref(0)
    const dragStartY = ref(0)
    const cropStartX = ref(0)
    const cropStartY = ref(0)
    const cropStartSize = ref(0)
    
    // Computed
    const cropSquareStyle = computed(() => ({
      left: `${cropX.value}px`,
      top: `${cropY.value}px`,
      width: `${cropSize.value}px`,
      height: `${cropSize.value}px`
    }))
    
    // Methods
    const onImageLoad = () => {
      imageLoaded.value = true
      imageError.value = false
      
      // Initialize crop square in center
      if (imageElement.value && cropperContainer.value) {
        const containerRect = cropperContainer.value.getBoundingClientRect()
        const imageRect = imageElement.value.getBoundingClientRect()
        
        // Calculate initial position
        const containerWidth = containerRect.width
        const containerHeight = containerRect.height
        
        // Set crop square in center with max size
        const maxSize = Math.min(containerWidth, containerHeight) * 0.8
        cropSize.value = Math.min(maxSize, 300)
        
        cropX.value = (containerWidth - cropSize.value) / 2
        cropY.value = (containerHeight - cropSize.value) / 2
        
        // Reset zoom and position
        zoom.value = 1
        translateX.value = 0
        translateY.value = 0
      }
    }
    
    const onImageError = () => {
      imageError.value = true
      imageLoaded.value = false
    }
    
    const zoomIn = () => {
      if (zoom.value < 3) {
        zoom.value = Math.min(zoom.value + 0.1, 3)
      }
    }
    
    const zoomOut = () => {
      if (zoom.value > 0.5) {
        zoom.value = Math.max(zoom.value - 0.1, 0.5)
      }
    }
    
    const startDrag = (e) => {
      if (isResizing.value) return
      
      isDragging.value = true
      dragStartX.value = e.clientX
      dragStartY.value = e.clientY
      cropStartX.value = cropX.value
      cropStartY.value = cropY.value
      
      document.addEventListener('mousemove', handleDrag)
      document.addEventListener('mouseup', stopDrag)
    }
    
    const handleDrag = (e) => {
      if (!isDragging.value) return
      
      const deltaX = e.clientX - dragStartX.value
      const deltaY = e.clientY - dragStartY.value
      
      let newX = cropStartX.value + deltaX
      let newY = cropStartY.value + deltaY
      
      // Constrain to container bounds
      if (cropperContainer.value) {
        const containerRect = cropperContainer.value.getBoundingClientRect()
        newX = Math.max(0, Math.min(newX, containerRect.width - cropSize.value))
        newY = Math.max(0, Math.min(newY, containerRect.height - cropSize.value))
      }
      
      cropX.value = newX
      cropY.value = newY
    }
    
    const stopDrag = () => {
      isDragging.value = false
      document.removeEventListener('mousemove', handleDrag)
      document.removeEventListener('mouseup', stopDrag)
    }
    
    const startResize = (handle) => {
      isResizing.value = true
      resizeHandle.value = handle
      dragStartX.value = event.clientX
      dragStartY.value = event.clientY
      cropStartX.value = cropX.value
      cropStartY.value = cropY.value
      cropStartSize.value = cropSize.value
      
      document.addEventListener('mousemove', handleResize)
      document.addEventListener('mouseup', stopResize)
    }
    
    const handleResize = (e) => {
      if (!isResizing.value) return
      
      const deltaX = e.clientX - dragStartX.value
      const deltaY = e.clientY - dragStartY.value
      
      let newSize = cropStartSize.value
      let newX = cropStartX.value
      let newY = cropStartY.value
      
      switch (resizeHandle.value) {
        case 'br':
          newSize = Math.max(50, cropStartSize.value + deltaX + deltaY)
          break
        case 'tr':
          newSize = Math.max(50, cropStartSize.value + deltaX - deltaY)
          newX = cropStartX.value + deltaX
          break
        case 'bl':
          newSize = Math.max(50, cropStartSize.value - deltaX + deltaY)
          newY = cropStartY.value + deltaY
          break
        case 'tl':
          newSize = Math.max(50, cropStartSize.value - deltaX - deltaY)
          newX = cropStartX.value + deltaX
          newY = cropStartY.value + deltaY
          break
        case 't':
          newSize = Math.max(50, cropStartSize.value - deltaY)
          newY = cropStartY.value + deltaY
          break
        case 'b':
          newSize = Math.max(50, cropStartSize.value + deltaY)
          break
        case 'l':
          newSize = Math.max(50, cropStartSize.value - deltaX)
          newX = cropStartX.value + deltaX
          break
        case 'r':
          newSize = Math.max(50, cropStartSize.value + deltaX)
          break
      }
      
      // Constrain to container bounds
      if (cropperContainer.value) {
        const containerRect = cropperContainer.value.getBoundingClientRect()
        
        newSize = Math.min(newSize, containerRect.width, containerRect.height)
        newX = Math.max(0, Math.min(newX, containerRect.width - newSize))
        newY = Math.max(0, Math.min(newY, containerRect.height - newSize))
      }
      
      cropSize.value = newSize
      cropX.value = newX
      cropY.value = newY
    }
    
    const stopResize = () => {
      isResizing.value = false
      resizeHandle.value = null
      document.removeEventListener('mousemove', handleResize)
      document.removeEventListener('mouseup', stopResize)
    }
    
    const handleCrop = () => {
      if (!imageLoaded.value || !imageElement.value) return
      
      try {
        // Create canvas for cropping
        const canvas = document.createElement('canvas')
        const ctx = canvas.getContext('2d')
        
        // Set canvas size to output size
        canvas.width = props.outputSize
        canvas.height = props.outputSize
        
        // Calculate source coordinates considering zoom and pan
        const imageRect = imageElement.value.getBoundingClientRect()
        const containerRect = cropperContainer.value.getBoundingClientRect()
        
        const scaleX = imageElement.value.naturalWidth / imageRect.width
        const scaleY = imageElement.value.naturalHeight / imageRect.height
        
        // Calculate crop area in original image coordinates
        const cropAreaX = (cropX.value - translateX.value) / zoom.value
        const cropAreaY = (cropY.value - translateY.value) / zoom.value
        const cropAreaSize = cropSize.value / zoom.value
        
        // Convert to original image pixel coordinates
        const sourceX = cropAreaX * scaleX
        const sourceY = cropAreaY * scaleY
        const sourceWidth = cropAreaSize * scaleX
        const sourceHeight = cropAreaSize * scaleY
        
        // Ensure source coordinates are within image bounds
        const clampedSourceX = Math.max(0, Math.min(sourceX, imageElement.value.naturalWidth - sourceWidth))
        const clampedSourceY = Math.max(0, Math.min(sourceY, imageElement.value.naturalHeight - sourceHeight))
        const clampedSourceWidth = Math.min(sourceWidth, imageElement.value.naturalWidth - clampedSourceX)
        const clampedSourceHeight = Math.min(sourceHeight, imageElement.value.naturalHeight - clampedSourceY)
        
        console.log('🎯 [ImageCropper] Crop coordinates:', {
          sourceX: clampedSourceX,
          sourceY: clampedSourceY,
          sourceWidth: clampedSourceWidth,
          sourceHeight: clampedSourceHeight,
          imageWidth: imageElement.value.naturalWidth,
          imageHeight: imageElement.value.naturalHeight
        })
        
        // Clear canvas before drawing
        ctx.clearRect(0, 0, canvas.width, canvas.height)
        
        // Draw cropped image to canvas with proper centering
        ctx.drawImage(
          imageElement.value,
          clampedSourceX,
          clampedSourceY,
          clampedSourceWidth,
          clampedSourceHeight,
          0,
          0,
          props.outputSize,
          props.outputSize
        )
        
        // Convert to blob
        canvas.toBlob((blob) => {
          if (blob) {
            // Create file from blob
            const fileName = `cropped_${Date.now()}.png`
            const file = new File([blob], fileName, { type: 'image/png' })
            
            // Emit crop event with file
            emit('crop', file)
          }
        }, 'image/png', 0.9)
        
      } catch (error) {
        console.error('Error cropping image:', error)
      }
    }
    
    const handleCancel = () => {
      emit('cancel')
    }
    
    // Cleanup
    onUnmounted(() => {
      document.removeEventListener('mousemove', handleDrag)
      document.removeEventListener('mouseup', stopDrag)
      document.removeEventListener('mousemove', handleResize)
      document.removeEventListener('mouseup', stopResize)
    })
    
    return {
      // Refs
      imageElement,
      cropperContainer,
      
      // State
      imageLoaded,
      imageError,
      zoom,
      translateX,
      translateY,
      cropSquareStyle,
      
      // Methods
      onImageLoad,
      onImageError,
      zoomIn,
      zoomOut,
      startDrag,
      startResize,
      handleCrop,
      handleCancel
    }
  }
}
</script>

<style scoped>
.image-cropper-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.image-cropper-modal {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 800px;
  max-height: 90vh;
  overflow: hidden;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
}

.image-cropper-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e5e7eb;
}

.image-cropper-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #111827;
}

.btn-close {
  background: none;
  border: none;
  padding: 8px;
  border-radius: 6px;
  cursor: pointer;
  color: #6b7280;
  transition: all 0.2s;
}

.btn-close:hover {
  background: #f3f4f6;
  color: #111827;
}

.image-cropper-content {
  padding: 20px;
}

.cropper-container {
  position: relative;
  width: 100%;
  height: 400px;
  background: #f9fafb;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 20px;
}

.cropper-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cropper-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  user-select: none;
  cursor: move;
}

.crop-square {
  position: absolute;
  border: 2px solid #3b82f6;
  background: rgba(59, 130, 246, 0.1);
  cursor: move;
  user-select: none;
}

.handle {
  position: absolute;
  background: #3b82f6;
  border: 2px solid white;
  border-radius: 50%;
}

.handle-tl { top: -4px; left: -4px; width: 8px; height: 8px; cursor: nw-resize; }
.handle-tr { top: -4px; right: -4px; width: 8px; height: 8px; cursor: ne-resize; }
.handle-bl { bottom: -4px; left: -4px; width: 8px; height: 8px; cursor: sw-resize; }
.handle-br { bottom: -4px; right: -4px; width: 8px; height: 8px; cursor: se-resize; }
.handle-t { top: -4px; left: 50%; transform: translateX(-50%); width: 8px; height: 8px; cursor: n-resize; }
.handle-b { bottom: -4px; left: 50%; transform: translateX(-50%); width: 8px; height: 8px; cursor: s-resize; }
.handle-l { top: 50%; left: -4px; transform: translateY(-50%); width: 8px; height: 8px; cursor: w-resize; }
.handle-r { top: 50%; right: -4px; transform: translateY(-50%); width: 8px; height: 8px; cursor: e-resize; }

.grid-lines {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.grid-line {
  position: absolute;
  background: rgba(255, 255, 255, 0.5);
}

.grid-line.horizontal:nth-child(1) { top: 33.33%; left: 0; right: 0; height: 1px; }
.grid-line.horizontal:nth-child(2) { top: 66.66%; left: 0; right: 0; height: 1px; }
.grid-line.vertical:nth-child(1) { left: 33.33%; top: 0; bottom: 0; width: 1px; }
.grid-line.vertical:nth-child(2) { left: 66.66%; top: 0; bottom: 0; width: 1px; }

.cropper-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
}

.zoom-controls {
  display: flex;
  align-items: center;
  gap: 10px;
}

.btn-zoom {
  background: #f3f4f6;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  padding: 6px 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-zoom:hover:not(:disabled) {
  background: #e5e7eb;
}

.btn-zoom:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.zoom-label {
  min-width: 50px;
  text-align: center;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.action-buttons {
  display: flex;
  gap: 10px;
}

.btn-cancel {
  background: white;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 500;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-cancel:hover {
  background: #f9fafb;
  color: #374151;
}

.btn-crop {
  background: #3b82f6;
  border: 1px solid #3b82f6;
  border-radius: 6px;
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 500;
  color: white;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-crop:hover:not(:disabled) {
  background: #2563eb;
}

.btn-crop:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Dark mode support */
@media (prefers-color-scheme: dark) {
  .image-cropper-modal {
    background: #1f2937;
  }
  
  .image-cropper-header {
    border-bottom-color: #374151;
  }
  
  .image-cropper-header h3 {
    color: #f9fafb;
  }
  
  .btn-close:hover {
    background: #374151;
    color: #f9fafb;
  }
  
  .cropper-container {
    background: #111827;
  }
  
  .btn-cancel {
    background: #374151;
    border-color: #4b5563;
    color: #d1d5db;
  }
  
  .btn-cancel:hover {
    background: #4b5563;
    color: #f9fafb;
  }
}
</style>
