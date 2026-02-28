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
    currentImageUrl.value = withCacheBuster(category.imageUrl || null)
    // Устанавливаем parentId - убеждаемся, что это число или null
    parentCategory.value = category.parentId ? Number(category.parentId) : null
    parentStatus.value = category.isActive ? 'Published' : 'Inactive'
    console.log('Загружена категория:', category)
    console.log('Parent ID загружен:', parentCategory.value)
    
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
const categoryImg = ref()
const currentImageUrl = ref(null) // URL текущего изображения при редактировании
const parentCategory = ref()
const parentStatus = ref()
const categoryFileInput = ref(null)
const previewImageUrl = ref(null)
const removeCurrentImage = ref(false)

const removeCategoryImage = async () => {
  categoryImg.value = null
  removeCurrentImage.value = true
  currentImageUrl.value = null
  if (previewImageUrl.value) {
    URL.revokeObjectURL(previewImageUrl.value)
    previewImageUrl.value = null
  }

  if (props.categoryId) {
    try {
      const response = await $api(`admin/categories/${props.categoryId}/image`, {
        method: 'DELETE',
      })
      currentImageUrl.value = withCacheBuster(response?.imageUrl || null)
      removeCurrentImage.value = false
    } catch (error) {
      console.error('Ошибка при удалении изображения:', error)
    }
  }
}

const selectedCategoryFile = computed(() => {
  if (!categoryImg.value) {
    return null
  }
  if (Array.isArray(categoryImg.value)) {
    return categoryImg.value[0] || null
  }
  return categoryImg.value
})

watch(selectedCategoryFile, file => {
  if (previewImageUrl.value) {
    URL.revokeObjectURL(previewImageUrl.value)
    previewImageUrl.value = null
  }
  if (file) {
    previewImageUrl.value = URL.createObjectURL(file)
  }
})

const triggerCategoryFileSelect = () => {
  categoryFileInput.value?.click()
}

const handleCategoryFileChange = event => {
  const file = event.target?.files?.[0] || null
  categoryImg.value = file
  if (event.target) {
    event.target.value = ''
  }
}

const resetForm = () => {
  refVForm.value?.reset()
  editor.value?.commands.clearContent()
  categoryTitle.value = null
  categorySlug.value = null
  categoryImg.value = null
  currentImageUrl.value = null
  parentCategory.value = null
  parentStatus.value = null
  removeCurrentImage.value = false
  if (previewImageUrl.value) {
    URL.revokeObjectURL(previewImageUrl.value)
    previewImageUrl.value = null
  }
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

const uploadCategoryImage = async (categoryId) => {
  if (!selectedCategoryFile.value) {
    return null
  }

  const formData = new FormData()
  formData.append('file', selectedCategoryFile.value)

  const accessToken = getAccessToken()
  const response = await fetch(`/api/admin/categories/${categoryId}/image`, {
    method: 'POST',
    headers: accessToken ? { Authorization: `Bearer ${accessToken}` } : undefined,
    body: formData,
  })

  if (!response.ok) {
    const errorText = await response.text()
    throw new Error(errorText || 'Ошибка при загрузке изображения')
  }

  const responseJson = await response.json()

  return responseJson?.imageUrl || null
}

const handleSubmit = async () => {
  const { valid } = await refVForm.value?.validate()
  if (!valid) return
  if (!props.categoryId && !selectedCategoryFile.value) {
    alert('Загрузите изображение категории')
    return
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
    
    // Определяем imageUrl: если новое изображение не выбрано, используем текущий URL
    let imageUrl = null
    if (!removeCurrentImage.value && currentImageUrl.value) {
      // При редактировании, если новое изображение не выбрано, используем текущий URL
      imageUrl = stripCacheBuster(currentImageUrl.value)
    }
    
    // Определяем parentId - преобразуем в число, если значение есть
    let parentIdValue = null
    if (parentCategory.value !== null && parentCategory.value !== undefined && parentCategory.value !== '') {
      const numValue = Number(parentCategory.value)
      // Проверяем, что это валидное число и больше 0
      if (!isNaN(numValue) && numValue > 0) {
        parentIdValue = numValue
      }
    }
    
    // Подготовка данных для отправки
    const categoryData = {
      name: categoryTitle.value,
      slug: formattedSlug,
      description: description,
      imageUrl: imageUrl,
      isActive: isActive,
      sortOrder: 0,
      parentId: parentIdValue,
    }
    
    console.log('Parent Category value:', parentCategory.value)
    console.log('Parent ID to send:', parentIdValue)

    // Отправка на бэкенд
    let savedCategoryId = props.categoryId
    if (props.categoryId) {
      // Режим редактирования
      console.log('Обновление категории ID:', props.categoryId)
      console.log('Данные для отправки:', JSON.stringify(categoryData, null, 2))
      const response = await $api(`admin/categories/${props.categoryId}`, {
        method: 'PUT',
        body: categoryData,
      })
      console.log('Категория обновлена, ответ:', response)
      savedCategoryId = response?.id || props.categoryId
    } else {
      // Режим создания
      console.log('Создание категории:', JSON.stringify(categoryData, null, 2))
      const response = await $api('admin/categories', {
        method: 'POST',
        body: categoryData,
      })
      console.log('Категория создана:', response)
      savedCategoryId = response?.id
    }

    if (savedCategoryId && selectedCategoryFile.value) {
      const uploadedImageUrl = await uploadCategoryImage(savedCategoryId)
      if (uploadedImageUrl) {
        currentImageUrl.value = withCacheBuster(uploadedImageUrl)
        removeCurrentImage.value = false
      }
      categoryImg.value = null
    }

    if (removeCurrentImage.value) {
      currentImageUrl.value = null
      removeCurrentImage.value = false
    }

    // Успешно создано/обновлено - обновляем список категорий и закрываем drawer
    await fetchCategories()
    emit('categoryCreated')
    closeForm()
  } catch (error) {
    console.error('Ошибка при создании категории:', error)
    // Можно добавить уведомление об ошибке
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
                <VLabel>
                  <span class="text-sm text-high-emphasis mb-1">Вложение</span>
                </VLabel>

                <div class="d-flex align-center gap-4">
                  <VAvatar
                    size="120"
                    rounded
                    variant="tonal"
                  >
                    <img
                      v-if="previewImageUrl"
                      :src="previewImageUrl"
                      :alt="categoryTitle || 'Изображение категории'"
                      style="width: 100%; height: 100%; object-fit: cover;"
                    >
                    <img
                      v-else-if="currentImageUrl && !removeCurrentImage"
                      :src="currentImageUrl"
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
                      ref="categoryFileInput"
                      type="file"
                      class="d-none"
                      accept="image/png,image/jpeg,image/jpg,image/webp"
                      @change="handleCategoryFileChange"
                    >
                    <VBtn
                      variant="tonal"
                      @click="triggerCategoryFileSelect"
                    >
                      {{ currentImageUrl || selectedCategoryFile ? 'Изменить' : 'Загрузить' }}
                    </VBtn>
                    <VBtn
                      v-if="currentImageUrl || selectedCategoryFile"
                      variant="tonal"
                      color="error"
                      @click="removeCategoryImage"
                    >
                      Удалить фото
                    </VBtn>
                    <div class="text-body-2 text-medium-emphasis">
                      Одно фото на категорию
                    </div>
                  </div>
                </div>
              </VCol>

              <VCol cols="12">
                <AppSelect
                  v-model="parentCategory"
                  label="Родительская категория"
                  placeholder="Выберите родительскую категорию (необязательно)"
                  :items="parentCategoryOptions"
                  clearable
                />
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
