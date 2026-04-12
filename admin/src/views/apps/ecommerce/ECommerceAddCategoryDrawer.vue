<script setup>
import { Image } from '@tiptap/extension-image'
import { Link } from '@tiptap/extension-link'
import { Placeholder } from '@tiptap/extension-placeholder'
import { Underline } from '@tiptap/extension-underline'
import { StarterKit } from '@tiptap/starter-kit'
import {
  EditorContent,
  useEditor,
} from '@tiptap/vue-3'
import { PerfectScrollbar } from 'vue3-perfect-scrollbar'
import { VForm } from 'vuetify/components/VForm'

const props = defineProps({
  isDrawerOpen: {
    type: Boolean,
    required: true,
  },
  categoryId: {
    type: Number,
    default: null,
  },
})

const emit = defineEmits(['update:isDrawerOpen', 'categoryCreated', 'drawerClosed'])

const handleDrawerModelValueUpdate = val => {
  emit('update:isDrawerOpen', val)
  if (!val) {
    // Drawer закрыт - сбрасываем форму
    resetForm()
    emit('drawerClosed')
  }
}

// Функция для загрузки данных категории
const loadCategoryData = async (categoryId) => {
  if (!categoryId) return
  
  try {
    const category = await $api(`admin/categories/${categoryId}`)
    categoryTitle.value = category.name || ''
    categorySlug.value = category.slug || ''
    IMAGE_SLOTS.forEach(slot => {
      const slotState = imageSlotsState[slot.key]
      if (!slotState) return
      clearSlotPreview(slot.key)
      slotState.file = null
      slotState.removeCurrent = false
      slotState.currentUrl = withCacheBuster(category?.[slot.field] || null)
    })
    // Устанавливаем parentId - убеждаемся, что это число или null
    parentCategory.value = category.parentId ? Number(category.parentId) : null
    isParentCategory.value = !category.parentId
    parentStatus.value = category.isActive ? 'Published' : 'Inactive'

    // Тройная плитка каталога — только у категорий верхнего уровня (без parentId)
    mobileTripleTileMode.value = !category.parentId && !!(
      category.mobileImageUrl
      && category.mobileImageUrl2
      && category.mobileImageUrl3
    )

    // Устанавливаем описание в редактор (с задержкой, чтобы editor был готов)
    await nextTick()
    if (editor.value) {
      const description = category.description || ''
      editor.value.commands.setContent(description)
    } else {
      // Если editor еще не готов, пробуем еще раз через небольшую задержку
      setTimeout(() => {
        if (editor.value) {
          const description = category.description || ''
          editor.value.commands.setContent(description)
        }
      }, 100)
    }
  } catch (error) {
    console.error('Ошибка при загрузке категории:', error)
  }
}

// Загружаем данные категории при открытии drawer в режиме редактирования
watch(() => props.isDrawerOpen, async (isOpen) => {
  if (!isOpen) {
    // Drawer закрыт - очищаем форму
    resetForm()
    return
  }
  
  // Drawer открыт - проверяем режим
  if (props.categoryId) {
    // Режим редактирования - загружаем данные категории
    await loadCategoryData(props.categoryId)
  } else {
    // Режим создания - очищаем форму
    resetForm()
  }
})

// Также следим за изменением categoryId
watch(() => props.categoryId, async (categoryId) => {
  if (props.isDrawerOpen && categoryId) {
    // Drawer открыт и categoryId изменился - загружаем данные
    await loadCategoryData(categoryId)
  }
})

const editor = useEditor({
  content: '',
  extensions: [
    StarterKit,
    Image,
    Placeholder.configure({ placeholder: 'Введите описание категории...' }),
    Underline,
    Link.configure({ openOnClick: false }),
  ],
})

const setLink = () => {
  const previousUrl = editor.value?.getAttributes('link').href

  // eslint-disable-next-line no-alert
  const url = window.prompt('Ссылка', previousUrl)

  // cancelled
  if (url === null)
    return

  // empty
  if (url === '') {
    editor.value?.chain().focus().extendMarkRange('link').unsetLink().run()
    
    return
  }

  // update link
  editor.value?.chain().focus().extendMarkRange('link').setLink({ href: url }).run()
}

const addImage = () => {

  // eslint-disable-next-line no-alert
  const url = window.prompt('Ссылка')
  if (url)
    editor.value?.chain().focus().setImage({ src: url }).run()
}

// Загружаем список категорий для выбора родительской категории
const {
  data: categoriesData,
  execute: fetchCategories,
} = await useApi(createUrl('/admin/categories'))

// Преобразуем категории в формат для селекта
const parentCategoryOptions = computed(() => {
  if (!categoriesData.value || !Array.isArray(categoriesData.value)) {
    return []
  }
  // Исключаем текущую редактируемую категорию из списка родительских (чтобы нельзя было выбрать саму себя)
  return categoriesData.value
    .filter(category => category.id !== props.categoryId)
    .map(category => ({
      title: category.name,
      value: category.id,
    }))
})

const refVForm = ref()
const categoryTitle = ref()
const categorySlug = ref()
const parentCategory = ref()
const isParentCategory = ref(false)
const parentStatus = ref()
const IMAGE_SLOTS = [
  { key: 'main', label: 'Фото категории', field: 'imageUrl', slotApi: 'MAIN', rootOnly: false },
  { key: 'banner', label: 'Баннер категории (родительская)', field: 'bannerImageUrl', slotApi: 'BANNER', rootOnly: true },
  { key: 'menu', label: 'Фото для меню', field: 'menuImageUrl', slotApi: 'MENU', rootOnly: false },
  { key: 'collection', label: 'Фото для коллекции', field: 'collectionImageUrl', slotApi: 'COLLECTION', rootOnly: false },
  { key: 'mobile', label: 'Фото для мобильной плитки', field: 'mobileImageUrl', slotApi: 'MOBILE', rootOnly: false },
  { key: 'mobile2', label: 'Мобильная плитка — фото 2 из 3', field: 'mobileImageUrl2', slotApi: 'MOBILE2', rootOnly: false },
  { key: 'mobile3', label: 'Мобильная плитка — фото 3 из 3', field: 'mobileImageUrl3', slotApi: 'MOBILE3', rootOnly: false },
]

/** Один файл для каталога, меню и блока коллекций на сайте */
const UNIFIED_CATALOG_SLOT_KEYS = ['main', 'menu', 'collection']
const UNIFIED_CATALOG_SLOT_KEY_SET = new Set(UNIFIED_CATALOG_SLOT_KEYS)

const imageSlotsState = reactive(
  Object.fromEntries(
    IMAGE_SLOTS.map(slot => [slot.key, {
      file: null,
      currentUrl: null,
      previewUrl: null,
      removeCurrent: false,
      inputRef: null,
    }]),
  ),
)

const visibleImageSlots = computed(() => {
  return IMAGE_SLOTS.filter(slot => !slot.rootOnly || isParentCategory.value)
})

const separateImageSlots = computed(() => {
  return visibleImageSlots.value.filter(slot => !UNIFIED_CATALOG_SLOT_KEY_SET.has(slot.key))
})

/** Режим плитки: ровно 3 фото (все обязательны при сохранении) */
const mobileTripleTileMode = ref(false)

const separateBannerSlots = computed(() => separateImageSlots.value.filter(s => s.key === 'banner'))

const separateNonBannerSlots = computed(() => separateImageSlots.value.filter(s => s.key !== 'banner'))

const separateNonBannerFiltered = computed(() => {
  return separateNonBannerSlots.value.filter(slot => {
    if (slot.key === 'mobile2' || slot.key === 'mobile3')
      return mobileTripleTileMode.value
    return true
  })
})

const getMobileSlotLabel = slot => {
  if (!mobileTripleTileMode.value && slot.key === 'mobile')
    return 'Фото для мобильной плитки'
  if (slot.key === 'mobile')
    return 'Мобильная плитка — фото 1 из 3'
  return slot.label
}

const onMobileTripleModeToggle = isTriple => {
  if (!isTriple) {
    ['mobile2', 'mobile3'].forEach(key => {
      const st = imageSlotsState[key]
      if (!st) return
      clearSlotPreview(key)
      st.file = null
      st.removeCurrent = true
      st.currentUrl = null
    })
  }
}

const unifiedCatalogPreviewSrc = computed(() => {
  for (const key of UNIFIED_CATALOG_SLOT_KEYS) {
    const s = imageSlotsState[key]
    if (s?.previewUrl)
      return s.previewUrl
  }
  for (const key of UNIFIED_CATALOG_SLOT_KEYS) {
    const s = imageSlotsState[key]
    if (s?.currentUrl && !s.removeCurrent)
      return s.currentUrl
  }
  return null
})

const unifiedCatalogInputRef = ref(null)

const triggerUnifiedCatalogFileSelect = () => {
  unifiedCatalogInputRef.value?.click()
}

const handleParentCategoryToggle = value => {
  if (value) {
    parentCategory.value = null
  }
  else if (mobileTripleTileMode.value) {
    mobileTripleTileMode.value = false
    onMobileTripleModeToggle(false)
  }
}

const setImageSlotInputRef = (slotKey, el) => {
  if (imageSlotsState[slotKey]) {
    imageSlotsState[slotKey].inputRef = el
  }
}

const getSelectedSlotFile = slotKey => {
  const value = imageSlotsState[slotKey]?.file
  if (!value) return null
  if (Array.isArray(value)) return value[0] || null
  return value
}

const clearSlotPreview = slotKey => {
  const slotState = imageSlotsState[slotKey]
  if (!slotState) return
  if (slotState.previewUrl) {
    URL.revokeObjectURL(slotState.previewUrl)
    slotState.previewUrl = null
  }
}

const triggerCategoryFileSelect = slotKey => {
  imageSlotsState[slotKey]?.inputRef?.click()
}

const handleCategoryFileChange = (slotKey, event) => {
  const slotState = imageSlotsState[slotKey]
  if (!slotState) return
  const file = event.target?.files?.[0] || null
  slotState.file = file
  slotState.removeCurrent = false
  clearSlotPreview(slotKey)
  if (file) {
    slotState.previewUrl = URL.createObjectURL(file)
  }
  if (event.target) {
    event.target.value = ''
  }
}

const handleUnifiedCatalogFileChange = event => {
  const file = event.target?.files?.[0] || null
  UNIFIED_CATALOG_SLOT_KEYS.forEach(key => {
    const slotState = imageSlotsState[key]
    if (!slotState) return
    clearSlotPreview(key)
    slotState.removeCurrent = false
    slotState.file = null
  })
  imageSlotsState.main.file = file
  if (file)
    imageSlotsState.main.previewUrl = URL.createObjectURL(file)
  if (event.target)
    event.target.value = ''
}

const removeUnifiedCatalogImages = async () => {
  const unifiedSlotConfigs = IMAGE_SLOTS.filter(s => UNIFIED_CATALOG_SLOT_KEY_SET.has(s.key))
  UNIFIED_CATALOG_SLOT_KEYS.forEach(key => {
    const slotState = imageSlotsState[key]
    if (!slotState) return
    clearSlotPreview(key)
    slotState.file = null
    slotState.removeCurrent = true
    slotState.currentUrl = null
  })
  if (props.categoryId) {
    let lastResponse = null
    for (const slot of unifiedSlotConfigs) {
      try {
        lastResponse = await $api(`admin/categories/${props.categoryId}/image?slot=${slot.slotApi}`, {
          method: 'DELETE',
        })
      }
      catch (error) {
        console.error('Ошибка при удалении изображения:', error)
      }
    }
    if (lastResponse) {
      unifiedSlotConfigs.forEach(slot => {
        const slotState = imageSlotsState[slot.key]
        if (!slotState) return
        slotState.currentUrl = withCacheBuster(lastResponse?.[slot.field] || null)
        slotState.removeCurrent = false
      })
    }
  }
}

const resetForm = () => {
  refVForm.value?.reset()
  editor.value?.commands.clearContent()
  categoryTitle.value = null
  categorySlug.value = null
  parentCategory.value = null
  isParentCategory.value = false
  parentStatus.value = null
  mobileTripleTileMode.value = false
  IMAGE_SLOTS.forEach(slot => {
    const slotState = imageSlotsState[slot.key]
    if (!slotState) return
    clearSlotPreview(slot.key)
    slotState.file = null
    slotState.currentUrl = null
    slotState.removeCurrent = false
    slotState.inputRef = null
  })
}

const closeForm = () => {
  resetForm()
  emit('update:isDrawerOpen', false)
}

const getAccessToken = () => {
  try {
    return useCookie('accessToken').value || null
  } catch (e) {
    const value = `; ${document.cookie}`
    const parts = value.split(`; accessToken=`)
    if (parts.length === 2) {
      return decodeURIComponent(parts.pop().split(';').shift() || '')
    }
    return null
  }
}

const withCacheBuster = url => {
  if (!url) return url
  const separator = url.includes('?') ? '&' : '?'
  return `${url}${separator}t=${Date.now()}`
}

const stripCacheBuster = url => {
  if (!url) return url
  return url.replace(/([?&])t=\d+/, '').replace(/[?&]$/, '')
}

const uploadCategoryImage = async (categoryId, slotConfig, fileOverride = null) => {
  const selectedFile = fileOverride ?? getSelectedSlotFile(slotConfig.key)
  if (!selectedFile) {
    return null
  }

  const formData = new FormData()
  formData.append('file', selectedFile)

  const accessToken = getAccessToken()
  const response = await fetch(`/api/admin/categories/${categoryId}/image?slot=${slotConfig.slotApi}`, {
    method: 'POST',
    headers: accessToken ? { Authorization: `Bearer ${accessToken}` } : undefined,
    body: formData,
  })

  if (!response.ok) {
    const errorText = await response.text()
    throw new Error(errorText || 'Ошибка при загрузке изображения')
  }

  const responseJson = await response.json()

  return responseJson?.[slotConfig.field] || null
}

const removeCategoryImage = async slotConfig => {
  const slotState = imageSlotsState[slotConfig.key]
  if (!slotState) return
  slotState.file = null
  slotState.removeCurrent = true
  slotState.currentUrl = null
  clearSlotPreview(slotConfig.key)

  if (props.categoryId) {
    try {
      const response = await $api(`admin/categories/${props.categoryId}/image?slot=${slotConfig.slotApi}`, {
        method: 'DELETE',
      })
      slotState.currentUrl = withCacheBuster(response?.[slotConfig.field] || null)
      slotState.removeCurrent = false
    } catch (error) {
      console.error('Ошибка при удалении изображения:', error)
    }
  }
}

const handleSubmit = async () => {
  const { valid } = await refVForm.value?.validate()
  if (!valid) return
  if (!props.categoryId && !getSelectedSlotFile('main')) {
    alert('Загрузите изображение категории')
    return
  }

  if (mobileTripleTileMode.value && isParentCategory.value) {
    for (const key of ['mobile', 'mobile2', 'mobile3']) {
      const st = imageSlotsState[key]
      if (!st) continue
      const hasImage = (st.currentUrl && !st.removeCurrent) || getSelectedSlotFile(key)
      if (!hasImage) {
        // eslint-disable-next-line no-alert
        alert('В режиме «три фото в плитке» нужно загрузить ровно три изображения: фото 1, 2 и 3. Сейчас не хватает одного или нескольких.')
        return
      }
    }
  }
  else {
    const st = imageSlotsState.mobile
    const hasMobile = st && ((st.currentUrl && !st.removeCurrent) || getSelectedSlotFile('mobile'))
    if (!hasMobile) {
      // eslint-disable-next-line no-alert
      alert('Загрузите обязательное фото для мобильной плитки.')
      return
    }
  }

  try {
    // Получаем описание из редактора (используем текст, так как HTML может быть слишком сложным)
    const description = editor.value?.getText() || ''
    
    // Определяем статус активности
    const isActive = parentStatus.value === 'Published'
    
    // Преобразуем slug в правильный формат (только строчные буквы, цифры и дефисы)
    const formattedSlug = categorySlug.value
      ?.toLowerCase()
      .replace(/[^a-z0-9-]/g, '-')
      .replace(/-+/g, '-')
      .replace(/^-|-$/g, '') || ''
    
    // Определяем parentId - преобразуем в число, если значение есть
    let parentIdValue = null
    if (!isParentCategory.value && parentCategory.value !== null && parentCategory.value !== undefined && parentCategory.value !== '') {
      const numValue = Number(parentCategory.value)
      // Проверяем, что это валидное число и больше 0
      if (!isNaN(numValue) && numValue > 0) {
        parentIdValue = numValue
      }
    }

    const isRootCategory = isParentCategory.value
    
    // Подготовка данных для отправки
    const categoryData = {
      name: categoryTitle.value,
      slug: formattedSlug,
      description: description,
      isActive: isActive,
      sortOrder: 0,
      parentId: parentIdValue,
    }

    IMAGE_SLOTS.forEach(slot => {
      if (slot.rootOnly && !isRootCategory) {
        categoryData[slot.field] = null
        return
      }
      const slotState = imageSlotsState[slot.key]
      categoryData[slot.field] = (!slotState.removeCurrent && slotState.currentUrl)
        ? stripCacheBuster(slotState.currentUrl)
        : null
    })

    if (!mobileTripleTileMode.value || !isRootCategory) {
      categoryData.mobileImageUrl2 = null
      categoryData.mobileImageUrl3 = null
    }

    // Отправка на бэкенд
    let savedCategoryId = props.categoryId
    if (props.categoryId) {
      // Режим редактирования
      const response = await $api(`admin/categories/${props.categoryId}`, {
        method: 'PUT',
        body: categoryData,
      })
      savedCategoryId = response?.id || props.categoryId
    } else {
      // Режим создания
      const response = await $api('admin/categories', {
        method: 'POST',
        body: categoryData,
      })
      savedCategoryId = response?.id
    }

    if (savedCategoryId) {
      const unifiedCatalogUploadFile = getSelectedSlotFile('main')
      for (const slot of IMAGE_SLOTS) {
        if (slot.rootOnly && !isRootCategory) continue
        if (!isRootCategory && (slot.key === 'mobile2' || slot.key === 'mobile3')) continue
        const slotState = imageSlotsState[slot.key]
        const fileToUpload = UNIFIED_CATALOG_SLOT_KEY_SET.has(slot.key)
          ? unifiedCatalogUploadFile
          : getSelectedSlotFile(slot.key)
        if (!fileToUpload) continue
        try {
          const uploadedImageUrl = await uploadCategoryImage(savedCategoryId, slot, fileToUpload)
          if (uploadedImageUrl) {
            slotState.currentUrl = withCacheBuster(uploadedImageUrl)
            slotState.removeCurrent = false
          }
          slotState.file = null
          clearSlotPreview(slot.key)
        } catch (uploadErr) {
          console.error('Ошибка загрузки изображения категории:', uploadErr)
          const msg = uploadErr?.message || uploadErr?.toString?.() || 'Не удалось загрузить файл'
          // eslint-disable-next-line no-alert
          alert(`Категория сохранена, но изображение (${slot.label}) не загрузилось.\n\n${msg}\n\nПроверьте размер (до 20 МБ) и формат (JPG, PNG, WEBP).`)
          await fetchCategories()
          emit('categoryCreated')
          return
        }
      }
    }

    // Успешно создано/обновлено - обновляем список категорий и закрываем drawer
    await fetchCategories()
    emit('categoryCreated')
    closeForm()
  } catch (error) {
    console.error('Ошибка при создании категории:', error)
    const msg = error?.data?.message || error?.message || error?.toString?.() || 'Ошибка сохранения'
    // eslint-disable-next-line no-alert
    alert(msg)
  }
}
</script>

<template>
  <VNavigationDrawer
    :model-value="props.isDrawerOpen"
    temporary
    location="end"
    width="370"
    border="none"
    class="category-navigation-drawer scrollable-content"
    @update:model-value="handleDrawerModelValueUpdate"
  >
    <!-- 👉 Header -->
    <AppDrawerHeaderSection
      :title="props.categoryId ? 'Редактировать категорию' : 'Добавить категорию'"
      @cancel="$emit('update:isDrawerOpen', false)"
    />

    <VDivider />

    <PerfectScrollbar :options="{ wheelPropagation: false }">
      <VCard flat>
        <VCardText>
          <VForm
            ref="refVForm"
            @submit.prevent="handleSubmit"
          >
            <VRow>
              <VCol cols="12">
                <AppTextField
                  v-model="categoryTitle"
                  label="Название"
                  :rules="[requiredValidator]"
                  placeholder="Мода"
                />
              </VCol>

              <VCol cols="12">
                <AppTextField
                  v-model="categorySlug"
                  label="URL-адрес"
                  :rules="[requiredValidator]"
                  placeholder="moda"
                />
              </VCol>

              <VCol cols="12">
                <VSwitch
                  v-model="isParentCategory"
                  color="primary"
                  label="Это родительская категория"
                  inset
                  @update:model-value="handleParentCategoryToggle"
                />
              </VCol>

              <VCol
                v-if="!isParentCategory"
                cols="12"
              >
                <AppSelect
                  v-model="parentCategory"
                  label="Родительская категория"
                  placeholder="Выберите родительскую категорию"
                  :items="parentCategoryOptions"
                  clearable
                />
              </VCol>

              <VCol cols="12">
                <VLabel>
                  <span class="text-sm text-high-emphasis mb-1">Фото категории (каталог, меню, коллекции)</span>
                </VLabel>

                <div class="d-flex align-center gap-4">
                  <VAvatar
                    size="120"
                    rounded
                    variant="tonal"
                  >
                    <img
                      v-if="unifiedCatalogPreviewSrc"
                      :src="unifiedCatalogPreviewSrc"
                      :alt="categoryTitle || 'Изображение категории'"
                      style="width: 100%; height: 100%; object-fit: cover;"
                    >
                    <span
                      v-else
                      class="text-body-2 text-medium-emphasis"
                    >
                      Нет фото
                    </span>
                  </VAvatar>

                  <div class="d-flex flex-column gap-2">
                    <input
                      ref="unifiedCatalogInputRef"
                      type="file"
                      class="d-none"
                      accept="image/png,image/jpeg,image/jpg,image/webp"
                      @change="handleUnifiedCatalogFileChange"
                    >
                    <VBtn
                      variant="tonal"
                      @click="triggerUnifiedCatalogFileSelect"
                    >
                      {{ unifiedCatalogPreviewSrc || getSelectedSlotFile('main') ? 'Изменить' : 'Загрузить' }}
                    </VBtn>
                    <VBtn
                      v-if="unifiedCatalogPreviewSrc || getSelectedSlotFile('main')"
                      variant="tonal"
                      color="error"
                      @click="removeUnifiedCatalogImages"
                    >
                      Удалить фото
                    </VBtn>
                    <div class="text-body-2 text-medium-emphasis">
                      Одно изображение для карточки категории, пункта меню и блока коллекций на главной
                    </div>
                  </div>
                </div>
              </VCol>

              <VCol
                v-for="slot in separateBannerSlots"
                :key="slot.key"
                cols="12"
              >
                <VLabel>
                  <span class="text-sm text-high-emphasis mb-1">{{ slot.label }}</span>
                </VLabel>

                <div class="d-flex align-center gap-4">
                  <VAvatar
                    size="120"
                    rounded
                    variant="tonal"
                  >
                    <img
                      v-if="imageSlotsState[slot.key].previewUrl"
                      :src="imageSlotsState[slot.key].previewUrl"
                      :alt="categoryTitle || 'Изображение категории'"
                      style="width: 100%; height: 100%; object-fit: cover;"
                    >
                    <img
                      v-else-if="imageSlotsState[slot.key].currentUrl && !imageSlotsState[slot.key].removeCurrent"
                      :src="imageSlotsState[slot.key].currentUrl"
                      :alt="categoryTitle || 'Изображение категории'"
                      style="width: 100%; height: 100%; object-fit: cover;"
                    >
                    <span
                      v-else
                      class="text-body-2 text-medium-emphasis"
                    >
                      Нет фото
                    </span>
                  </VAvatar>

                  <div class="d-flex flex-column gap-2">
                    <input
                      :ref="el => setImageSlotInputRef(slot.key, el)"
                      type="file"
                      class="d-none"
                      accept="image/png,image/jpeg,image/jpg,image/webp"
                      @change="event => handleCategoryFileChange(slot.key, event)"
                    >
                    <VBtn
                      variant="tonal"
                      @click="triggerCategoryFileSelect(slot.key)"
                    >
                      {{ imageSlotsState[slot.key].currentUrl || getSelectedSlotFile(slot.key) ? 'Изменить' : 'Загрузить' }}
                    </VBtn>
                    <VBtn
                      v-if="imageSlotsState[slot.key].currentUrl || getSelectedSlotFile(slot.key)"
                      variant="tonal"
                      color="error"
                      @click="removeCategoryImage(slot)"
                    >
                      Удалить фото
                    </VBtn>
                    <div class="text-body-2 text-medium-emphasis">
                      Только для родительской категории
                    </div>
                  </div>
                </div>
              </VCol>

              <VCol
                v-if="isParentCategory"
                cols="12"
              >
                <VSwitch
                  v-model="mobileTripleTileMode"
                  color="primary"
                  inset
                  hide-details
                  label="Плитка каталога: три фото в ряд"
                  @update:model-value="onMobileTripleModeToggle"
                />
                <div class="text-body-2 text-medium-emphasis mt-1">
                  Доступно только для родительской категории. Включите и загрузите <strong>ровно три</strong> фото (слоты 1–3). Иначе оставьте выключенным и используйте одно фото для мобильной плитки.
                </div>
              </VCol>

              <VCol
                v-for="slot in separateNonBannerFiltered"
                :key="slot.key"
                cols="12"
              >
                <VLabel>
                  <span class="text-sm text-high-emphasis mb-1">{{ ['mobile','mobile2','mobile3'].includes(slot.key) ? getMobileSlotLabel(slot) : slot.label }}</span>
                </VLabel>

                <div class="d-flex align-center gap-4">
                  <VAvatar
                    size="120"
                    rounded
                    variant="tonal"
                  >
                    <img
                      v-if="imageSlotsState[slot.key].previewUrl"
                      :src="imageSlotsState[slot.key].previewUrl"
                      :alt="categoryTitle || 'Изображение категории'"
                      style="width: 100%; height: 100%; object-fit: cover;"
                    >
                    <img
                      v-else-if="imageSlotsState[slot.key].currentUrl && !imageSlotsState[slot.key].removeCurrent"
                      :src="imageSlotsState[slot.key].currentUrl"
                      :alt="categoryTitle || 'Изображение категории'"
                      style="width: 100%; height: 100%; object-fit: cover;"
                    >
                    <span
                      v-else
                      class="text-body-2 text-medium-emphasis"
                    >
                      Нет фото
                    </span>
                  </VAvatar>

                  <div class="d-flex flex-column gap-2">
                    <input
                      :ref="el => setImageSlotInputRef(slot.key, el)"
                      type="file"
                      class="d-none"
                      accept="image/png,image/jpeg,image/jpg,image/webp"
                      @change="event => handleCategoryFileChange(slot.key, event)"
                    >
                    <VBtn
                      variant="tonal"
                      @click="triggerCategoryFileSelect(slot.key)"
                    >
                      {{ imageSlotsState[slot.key].currentUrl || getSelectedSlotFile(slot.key) ? 'Изменить' : 'Загрузить' }}
                    </VBtn>
                    <VBtn
                      v-if="imageSlotsState[slot.key].currentUrl || getSelectedSlotFile(slot.key)"
                      variant="tonal"
                      color="error"
                      @click="removeCategoryImage(slot)"
                    >
                      Удалить фото
                    </VBtn>
                    <div class="text-body-2 text-medium-emphasis">
                      {{
                        mobileTripleTileMode && isParentCategory && ['mobile','mobile2','mobile3'].includes(slot.key)
                          ? 'Обязательно все три фото в этом режиме.'
                          : (slot.key === 'mobile'
                            ? 'Обязательно: одно фото для мобильной плитки (каталог на телефоне).'
                            : 'Отдельное фото для этого блока')
                      }}
                    </div>
                  </div>
                </div>
              </VCol>

              <VCol cols="12">
                <p class="text-body-2 text-high-emphasis mb-1">
                  Описание
                </p>
                <div class="border rounded px-3 py-1">
                  <EditorContent :editor="editor" />
                  <div
                    v-if="editor"
                    class="d-flex justify-end flex-wrap gap-x-2"
                  >
                    <VIcon
                      icon="tabler-bold"
                      :color="editor.isActive('bold') ? 'primary' : ''"
                      size="20"
                      @click="editor.chain().focus().toggleBold().run()"
                    />

                    <VIcon
                      :color="editor.isActive('underline') ? 'primary' : ''"
                      icon="tabler-underline"
                      size="20"
                      @click="editor.commands.toggleUnderline()"
                    />

                    <VIcon
                      :color="editor.isActive('italic') ? 'primary' : ''"
                      icon="tabler-italic"
                      size="20"
                      @click="editor.chain().focus().toggleItalic().run()"
                    />

                    <VIcon
                      :color="editor.isActive('bulletList') ? 'primary' : ''"
                      icon="tabler-list"
                      size="20"
                      @click="editor.chain().focus().toggleBulletList().run()"
                    />

                    <VIcon
                      :color="editor.isActive('orderedList') ? 'primary' : ''"
                      icon="tabler-list-numbers"
                      size="20"
                      @click="editor.chain().focus().toggleOrderedList().run()"
                    />

                    <VIcon
                      icon="tabler-link"
                      size="20"
                      @click="setLink"
                    />

                    <VIcon
                      icon="tabler-photo"
                      size="20"
                      @click="addImage"
                    />
                  </div>
                </div>
              </VCol>

              <VCol cols="12">
                <AppSelect
                  v-model="parentStatus"
                  :rules="[requiredValidator]"
                  placeholder="Выберите статус категории"
                  label="Статус категории"
                  :items="[
                    { title: 'Опубликовано', value: 'Published' },
                    { title: 'Неактивно', value: 'Inactive' },
                    { title: 'Запланировано', value: 'Scheduled' }
                  ]"
                />
              </VCol>

              <VCol cols="12">
                <div class="d-flex justify-start">
                  <VBtn
                    type="submit"
                    color="primary"
                    class="me-4"
                  >
                    {{ props.categoryId ? 'Обновить' : 'Добавить' }}
                  </VBtn>
                  <VBtn
                    color="error"
                    variant="tonal"
                    @click="closeForm"
                  >
                    Отменить
                  </VBtn>
                </div>
              </VCol>
            </VRow>
          </VForm>
        </VCardText>
      </VCard>
    </PerfectScrollbar>
  </VNavigationDrawer>
</template>

<style lang="scss">
.category-navigation-drawer {
  .ProseMirror {
    min-block-size: 9vh !important;

    p {
      margin-block-end: 0;
    }

    p.is-editor-empty:first-child::before {
      block-size: 0;
      color: #adb5bd;
      content: attr(data-placeholder);
      float: inline-start;
      pointer-events: none;
    }

    &-focused {
      outline: none;
    }

    ul,
    ol {
      padding-inline: 1.125rem;
    }
  }

  .is-active {
    border-color: rgba(var(--v-theme-primary), var(--v-border-opacity)) !important;
    background-color: rgba(var(--v-theme-primary), var(--v-activated-opacity));
    color: rgb(var(--v-theme-primary));
  }
}
</style>
