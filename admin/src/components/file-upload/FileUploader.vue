<template>
  <div class="file-uploader">
    <!-- Drag and Drop Zone -->
    <VCard
      :class="['upload-zone', { 'drag-over': isDragOver, 'has-files': files.length > 0 }]"
      @drop.prevent="handleDrop"
      @dragover.prevent="isDragOver = true"
      @dragleave.prevent="isDragOver = false"
      @dragenter.prevent="isDragOver = true"
    >
      <VCardText class="text-center pa-8">
        <VIcon
          icon="tabler-cloud-upload"
          size="64"
          color="primary"
          class="mb-4"
        />
        <h6 class="text-h6 mb-2">
          Перетащите файлы сюда или нажмите для выбора
        </h6>
        <p class="text-body-2 text-medium-emphasis mb-4">
          Поддерживаемые форматы: {{ allowedTypesText }}
          <br>
          Максимальный размер: {{ maxSizeText }}
        </p>
        <VBtn
          color="primary"
          prepend-icon="tabler-upload"
          @click="triggerFileInput"
        >
          Выбрать файлы
        </VBtn>
        <input
          ref="fileInput"
          type="file"
          :accept="accept"
          :multiple="multiple"
          class="d-none"
          @change="handleFileSelect"
        >
      </VCardText>
    </VCard>

    <!-- Upload Progress -->
    <VProgressLinear
      v-if="uploading"
      :model-value="uploadProgress"
      color="primary"
      height="8"
      class="mt-4"
    >
      <template #default>
        <strong>{{ Math.round(uploadProgress) }}%</strong>
      </template>
    </VProgressLinear>

    <!-- Uploaded Files Preview with Drag and Drop -->
    <div
      v-if="files.length > 0"
      ref="filesContainer"
      class="mt-4 files-container"
    >
      <VRow class="files-grid">
        <VCol
          v-for="(file, index) in files"
          :key="file.id || `file-${index}`"
          :data-index="index"
          cols="12"
          sm="6"
          md="4"
          lg="3"
          class="file-col draggable-file"
        >
          <VCard class="file-preview-card">
            <!-- Drag Handle and Order -->
            <div class="file-drag-handle">
              <VIcon
                icon="tabler-grip-vertical"
                size="20"
                color="primary"
                class="drag-icon"
              />
              <VChip
                size="small"
                color="primary"
                class="file-order-chip"
              >
                {{ index + 1 }}
              </VChip>
            </div>

          <!-- Image Preview -->
          <div
            v-if="file.fileType === 'IMAGE'"
            class="file-preview-image"
          >
            <VImg
              :src="file.thumbnailUrl || file.url"
              :alt="file.originalName"
              cover
              height="200"
              class="file-preview-img"
            />
            <VOverlay
              :model-value="file.uploading"
              contained
              class="align-center justify-center"
            >
              <VProgressCircular
                v-if="file.uploading"
                indeterminate
                color="white"
              />
            </VOverlay>
          </div>

          <!-- Video Preview -->
          <div
            v-else-if="file.fileType === 'VIDEO'"
            class="file-preview-video"
          >
            <VIcon
              icon="tabler-video"
              size="64"
              color="primary"
              class="mb-2"
            />
            <video
              v-if="file.url"
              :src="file.url"
              class="d-none"
              preload="metadata"
            />
          </div>

          <!-- File Info -->
          <VCardText class="pa-2">
            <div class="text-caption text-truncate">
              {{ file.originalName }}
            </div>
            <div class="text-caption text-medium-emphasis">
              {{ formatFileSize(file.fileSize) }}
            </div>
          </VCardText>

          <!-- Actions -->
          <VCardActions class="pa-2">
            <VSpacer />
            <VBtn
              icon="tabler-eye"
              size="small"
              variant="text"
              :href="file.url"
              target="_blank"
            />
            <VBtn
              icon="tabler-trash"
              size="small"
              variant="text"
              color="error"
              :loading="file.deleting"
              @click="removeFile(file, index)"
            />
            </VCardActions>
          </VCard>
        </VCol>
      </VRow>
    </div>

    <!-- Error Messages -->
    <VAlert
      v-if="error"
      type="error"
      variant="tonal"
      class="mt-4"
      closable
      @click:close="error = null"
    >
      {{ error }}
    </VAlert>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, nextTick } from 'vue'
import { $api } from '@/utils/api'
import { animations } from '@formkit/drag-and-drop'
import { dragAndDrop } from '@formkit/drag-and-drop/vue'

// Получаем токен из cookie (используем тот же способ, что и в api.js)
// useCookie auto-imported из unplugin-auto-import
const getAccessToken = () => {
  try {
    // Используем useCookie который auto-imported в проекте (как в api.js)
    return useCookie('accessToken').value || null
  } catch (e) {
    // Fallback на document.cookie если useCookie недоступен
    const value = `; ${document.cookie}`
    const parts = value.split(`; accessToken=`)
    if (parts.length === 2) {
      return decodeURIComponent(parts.pop().split(';').shift() || '')
    }
    return null
  }
}

// Simple fetch wrapper with progress support
const uploadWithProgress = async (url, formData, onProgress) => {
  return new Promise((resolve, reject) => {
    const xhr = new XMLHttpRequest()
    
    xhr.upload.addEventListener('progress', (e) => {
      if (e.lengthComputable && onProgress) {
        onProgress({
          loaded: e.loaded,
          total: e.total
        })
      }
    })
    
    xhr.addEventListener('load', () => {
      if (xhr.status >= 200 && xhr.status < 300) {
        try {
          const response = JSON.parse(xhr.responseText)
          resolve({ data: response })
        } catch (e) {
          resolve({ data: xhr.responseText })
        }
      } else {
        reject(new Error(`HTTP error! status: ${xhr.status}`))
      }
    })
    
    xhr.addEventListener('error', () => {
      reject(new Error('Network error'))
    })
    
    xhr.open('POST', url)
    
    // Получаем токен из cookie
    const accessToken = getAccessToken()
    if (accessToken) {
      xhr.setRequestHeader('Authorization', `Bearer ${accessToken}`)
    }
    
    // Не устанавливаем Content-Type вручную - браузер установит его автоматически с boundary для multipart/form-data
    // Это важно для правильной работы с файлами
    
    xhr.send(formData)
  })
}

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => []
  },
  productId: {
    type: Number,
    default: null
  },
  fileType: {
    type: String,
    default: 'IMAGE', // 'IMAGE' or 'VIDEO'
    validator: (value) => ['IMAGE', 'VIDEO', 'BOTH'].includes(value)
  },
  multiple: {
    type: Boolean,
    default: true
  },
  maxSize: {
    type: Number,
    default: null // in bytes, null = use default from backend
  },
  maxFiles: {
    type: Number,
    default: null // Максимальное количество файлов (null = без ограничений)
  }
})

const emit = defineEmits(['update:modelValue', 'uploaded', 'deleted'])

const fileInput = ref(null)
const filesContainer = ref(null)
const isDragOver = ref(false)
const uploading = ref(false)
const uploadProgress = ref(0)
const error = ref(null)
const files = ref([...props.modelValue])
const fileIndices = ref([]) // Реактивный массив индексов для drag and drop
let dragAndDropInstance = null // Экземпляр drag and drop

const accept = computed(() => {
  if (props.fileType === 'IMAGE') {
    return 'image/png,image/jpeg,image/jpg,image/webp' // PNG приоритетный
  } else if (props.fileType === 'VIDEO') {
    return 'video/mp4,video/webm,video/x-msvideo'
  }
  return 'image/png,image/jpeg,image/jpg,image/webp,video/mp4,video/webm,video/x-msvideo'
})

const allowedTypesText = computed(() => {
  if (props.fileType === 'IMAGE') {
    return 'PNG (рекомендуется), JPG, JPEG, WEBP'
  } else if (props.fileType === 'VIDEO') {
    return 'MP4, WEBM, AVI'
  }
  return 'PNG, JPG, JPEG, WEBP (фото), MP4, WEBM, AVI (видео)'
})

const maxSizeText = computed(() => {
  if (props.maxSize) {
    return formatFileSize(props.maxSize)
  }
  return props.fileType === 'IMAGE' ? '5 МБ' : '100 МБ'
})

const triggerFileInput = () => {
  fileInput.value?.click()
}

const handleFileSelect = (event) => {
  const selectedFiles = Array.from(event.target.files)
  uploadFiles(selectedFiles)
  // Reset input
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

const handleDrop = (event) => {
  isDragOver.value = false
  const droppedFiles = Array.from(event.dataTransfer.files)
  uploadFiles(droppedFiles)
}

const uploadFiles = async (fileList) => {
  error.value = null

  // Проверяем ограничение на количество файлов
  if (props.maxFiles && files.value.length + fileList.length > props.maxFiles) {
    error.value = `Максимальное количество файлов: ${props.maxFiles}. Уже загружено: ${files.value.length}`
    return
  }

  // Ограничиваем количество файлов для загрузки
  const filesToUpload = props.maxFiles 
    ? fileList.slice(0, props.maxFiles - files.value.length)
    : fileList

  if (filesToUpload.length < fileList.length) {
    error.value = `Можно загрузить только ${props.maxFiles} файлов. Будут загружены первые ${filesToUpload.length} файла.`
  }

  // Validate files
  for (const file of filesToUpload) {
    if (!validateFile(file)) {
      return
    }
  }

  uploading.value = true
  uploadProgress.value = 0

  try {
    const formData = new FormData()
    
    if (props.multiple) {
      fileList.forEach(file => {
        formData.append('files', file)
      })
    } else {
      formData.append('file', fileList[0])
    }

    if (props.productId) {
      formData.append('productId', props.productId.toString())
    }

    const endpoint = props.multiple ? '/admin/files/upload/multiple' : '/admin/files/upload'
    // Используем относительный путь, который будет проксироваться через Vite на backend
    const url = `/api${endpoint}`
    
    const response = await uploadWithProgress(url, formData, (progressEvent) => {
      if (progressEvent.total) {
        uploadProgress.value = (progressEvent.loaded / progressEvent.total) * 100
      }
    })
    
    const uploadedFiles = Array.isArray(response.data) ? response.data : [response.data]
    
    // Проверяем ограничение перед добавлением
    const availableSlots = props.maxFiles ? props.maxFiles - files.value.length : uploadedFiles.length
    const filesToAdd = uploadedFiles.slice(0, availableSlots)
    
    // Add uploaded files to the list
    filesToAdd.forEach((file, addIndex) => {
      files.value.push({
        ...file,
        uploading: false,
        sortOrder: files.value.length + addIndex // Устанавливаем порядок сортировки
      })
    })
    
    // Обновляем индексы для drag and drop
    fileIndices.value = files.value.map((_, i) => i)
    
    // Переинициализируем drag and drop после добавления файлов
    nextTick(() => {
      if (files.value.length >= 2) {
        initDragAndDrop()
      }
    })
    
    if (uploadedFiles.length > availableSlots) {
      error.value = `Загружено только ${availableSlots} из ${uploadedFiles.length} файлов (максимум ${props.maxFiles})`
    }

    emit('update:modelValue', files.value)
    emit('uploaded', uploadedFiles)
    
    uploadProgress.value = 100
  } catch (err) {
    error.value = err.data?.message || err.message || 'Ошибка при загрузке файлов'
    console.error('Upload error:', err)
  } finally {
    uploading.value = false
    setTimeout(() => {
      uploadProgress.value = 0
    }, 1000)
  }
}

const removeFile = async (file, index) => {
  // URL файлы или файлы без ID просто удаляем из списка локально
  if (!file.id || file.isUrl || String(file.id).startsWith('url-')) {
    files.value.splice(index, 1)
    // Обновляем индексы после удаления
    fileIndices.value = files.value.map((_, i) => i)
    emit('update:modelValue', files.value)
    emit('deleted', file)
    return
  }

  try {
    file.deleting = true
    // Используем относительный путь через прокси
    await $api(`/admin/files/${file.id}`, {
      method: 'DELETE'
    })

    files.value.splice(index, 1)
    // Обновляем индексы после удаления
    fileIndices.value = files.value.map((_, i) => i)
    emit('update:modelValue', files.value)
    emit('deleted', file)
  } catch (err) {
    error.value = err.data?.message || err.message || 'Ошибка при удалении файла'
    console.error('Delete error:', err)
  } finally {
    file.deleting = false
  }
}

const validateFile = (file) => {
  // Check file type
  const allowedImageTypes = ['image/png', 'image/jpeg', 'image/jpg', 'image/webp'] // PNG приоритетный
  const allowedVideoTypes = ['video/mp4', 'video/webm', 'video/x-msvideo']
  
  const isImage = allowedImageTypes.includes(file.type)
  const isVideo = allowedVideoTypes.includes(file.type)

  if (props.fileType === 'IMAGE' && !isImage) {
    error.value = 'Разрешены только изображения (PNG рекомендуется, JPG, JPEG, WEBP)'
    return false
  }

  if (props.fileType === 'VIDEO' && !isVideo) {
    error.value = 'Разрешены только видео (MP4, WEBM, AVI)'
    return false
  }

  if (props.fileType === 'BOTH' && !isImage && !isVideo) {
    error.value = 'Разрешены только изображения и видео'
    return false
  }

  // Check file size (по умолчанию 5 МБ для изображений)
  const maxSize = props.maxSize || (props.fileType === 'IMAGE' ? 5 * 1024 * 1024 : 100 * 1024 * 1024)
  if (file.size > maxSize) {
    error.value = `Размер файла превышает максимально допустимый: ${formatFileSize(maxSize)}`
    return false
  }

  return true
}

const formatFileSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

// Watch for external changes to modelValue
watch(() => props.modelValue, (newValue) => {
  files.value = [...newValue]
  // Обновляем sortOrder при изменении
  files.value.forEach((file, index) => {
    if (file.sortOrder === undefined || file.sortOrder === null) {
      file.sortOrder = index
    }
  })
  // Обновляем индексы для drag and drop
  fileIndices.value = files.value.map((_, i) => i)
  nextTick(() => {
    if (files.value.length >= 2) {
      initDragAndDrop()
    }
  })
}, { deep: true })

// Инициализация drag and drop для сортировки файлов
const initDragAndDrop = async () => {
  await nextTick()
  if (!filesContainer.value || files.value.length < 2) {
    dragAndDropInstance = null
    return
  }

  const container = filesContainer.value
  if (!container) return

  // Обновляем индексы
  fileIndices.value = files.value.map((_, i) => i)

  try {
    // Проверяем, что dragAndDrop и animations доступны
    if (!dragAndDrop || !animations) {
      console.warn('dragAndDrop or animations not available')
      return
    }
    
    dragAndDropInstance = dragAndDrop({
      parent: container,
      values: fileIndices,
      draggable: (el) => {
        return el.classList.contains('draggable-file')
      },
      plugins: [animations()],
      handleEnd: () => {
        // После завершения перетаскивания обновляем порядок файлов
        const reorderedFiles = fileIndices.value.map(index => files.value[index])
        files.value = reorderedFiles
        // Обновляем sortOrder
        files.value.forEach((file, index) => {
          file.sortOrder = index
        })
        emit('update:modelValue', files.value)
        // Отправляем обновление сортировки на сервер
        updateSortOrderOnServer()
      }
    })
  } catch (e) {
    console.warn('Failed to initialize drag and drop:', e)
    // Не блокируем работу компонента, если drag and drop не работает
  }
}

// Обновление сортировки на сервере
const updateSortOrderOnServer = async () => {
  if (!props.productId || files.value.length === 0) return
  
  try {
    const fileIds = files.value
      .filter(f => f.id) // Только файлы с ID (уже загруженные)
      .map(f => f.id)
    
    if (fileIds.length > 0) {
      await $api('/admin/files/sort', {
        method: 'PUT',
        body: fileIds
      })
    }
  } catch (error) {
    console.warn('Failed to update sort order on server:', error)
  }
}

onMounted(() => {
  if (files.value.length >= 2) {
    initDragAndDrop()
  }
})

// Переинициализируем при изменении количества файлов
watch(() => files.value.length, (newLength, oldLength) => {
  if (newLength >= 2 && oldLength < 2) {
    nextTick(() => {
      initDragAndDrop()
    })
  } else if (newLength < 2) {
    dragAndDropInstance = null
  }
})
</script>

<style scoped>
.file-uploader {
  width: 100%;
}

.upload-zone {
  border: 2px dashed rgba(var(--v-theme-on-surface), 0.12);
  transition: all 0.3s ease;
  cursor: pointer;
}

.upload-zone.drag-over {
  border-color: rgb(var(--v-theme-primary));
  background-color: rgba(var(--v-theme-primary), 0.05);
}

.upload-zone.has-files {
  border-style: solid;
  border-width: 1px;
}

.files-container {
  position: relative;
}

.file-col {
  cursor: move;
  transition: transform 0.2s;
}

.file-col:hover {
  transform: translateY(-2px);
}

.file-col[data-dragging] {
  opacity: 0.5;
  transform: scale(0.95);
}

.file-preview-card {
  position: relative;
  overflow: hidden;
  height: 100%;
}

.file-drag-handle {
  position: absolute;
  top: 8px;
  left: 8px;
  z-index: 10;
  display: flex;
  align-items: center;
  gap: 4px;
  background: rgba(255, 255, 255, 0.9);
  padding: 4px 8px;
  border-radius: 4px;
  cursor: grab;
}

.file-drag-handle:active {
  cursor: grabbing;
}

.file-order-chip {
  min-width: 24px;
  height: 24px;
  font-weight: bold;
}

.file-preview-image {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
}

.file-preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.file-preview-video {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  background-color: rgba(var(--v-theme-surface-variant), 0.5);
}
</style>
