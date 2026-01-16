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
    currentImageUrl.value = category.imageUrl || null
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
    Placeholder.configure({ placeholder: 'Enter a category description...' }),
    Underline,
    Link.configure({ openOnClick: false }),
  ],
})

const setLink = () => {
  const previousUrl = editor.value?.getAttributes('link').href

  // eslint-disable-next-line no-alert
  const url = window.prompt('URL', previousUrl)

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
  const url = window.prompt('URL')
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

const resetForm = () => {
  refVForm.value?.reset()
  editor.value?.commands.clearContent()
  categoryTitle.value = null
  categorySlug.value = null
  categoryImg.value = null
  currentImageUrl.value = null
  parentCategory.value = null
  parentStatus.value = null
}

const closeForm = () => {
  resetForm()
  emit('update:isDrawerOpen', false)
}

const handleSubmit = async () => {
  const { valid } = await refVForm.value?.validate()
  if (!valid) return

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
    
    // Определяем imageUrl: если выбрано новое изображение, используем его URL (пока null, нужно загрузить на сервер)
    // Если новое изображение не выбрано, используем текущий URL при редактировании
    let imageUrl = null
    if (categoryImg.value && categoryImg.value.length > 0) {
      // TODO: Реализовать загрузку изображения на сервер и получить URL
      // Пока оставляем null, но в будущем здесь будет URL загруженного изображения
      imageUrl = null
    } else if (currentImageUrl.value) {
      // При редактировании, если новое изображение не выбрано, используем текущий URL
      imageUrl = currentImageUrl.value
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
    if (props.categoryId) {
      // Режим редактирования
      console.log('Обновление категории ID:', props.categoryId)
      console.log('Данные для отправки:', JSON.stringify(categoryData, null, 2))
      const response = await $api(`admin/categories/${props.categoryId}`, {
        method: 'PUT',
        body: categoryData,
      })
      console.log('Категория обновлена, ответ:', response)
    } else {
      // Режим создания
      console.log('Создание категории:', JSON.stringify(categoryData, null, 2))
      const response = await $api('admin/categories', {
        method: 'POST',
        body: categoryData,
      })
      console.log('Категория создана:', response)
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
      :title="props.categoryId ? 'Edit Category' : 'Add Category'"
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
                  label="Title"
                  :rules="[requiredValidator]"
                  placeholder="Fashion"
                />
              </VCol>

              <VCol cols="12">
                <AppTextField
                  v-model="categorySlug"
                  label="Slug"
                  :rules="[requiredValidator]"
                  placeholder="Trends fashion"
                />
              </VCol>

              <VCol cols="12">
                <VLabel>
                  <span class="text-sm text-high-emphasis mb-1">Attachment</span>
                </VLabel>
                
                <!-- Отображаем текущее изображение при редактировании -->
                <div
                  v-if="currentImageUrl && !categoryImg"
                  class="mb-4"
                >
                  <div class="text-body-2 text-medium-emphasis mb-2">
                    Current Image:
                  </div>
                  <VAvatar
                    size="120"
                    rounded
                    variant="tonal"
                  >
                    <img
                      :src="currentImageUrl"
                      :alt="categoryTitle || 'Category image'"
                      style="width: 100%; height: 100%; object-fit: cover;"
                    >
                  </VAvatar>
                </div>
                
                <VFileInput
                  v-model="categoryImg"
                  prepend-icon=""
                  :rules="props.categoryId ? [] : [requiredValidator]"
                  clearable
                  :label="currentImageUrl ? 'Change Image (optional)' : 'Upload Image'"
                >
                  <template #append>
                    <VBtn variant="tonal">
                      Choose
                    </VBtn>
                  </template>
                </VFileInput>
                
                <!-- Показываем превью нового изображения, если выбрано -->
                <div
                  v-if="categoryImg && categoryImg.length > 0"
                  class="mt-4"
                >
                  <div class="text-body-2 text-medium-emphasis mb-2">
                    New Image Preview:
                  </div>
                  <VAvatar
                    size="120"
                    rounded
                    variant="tonal"
                  >
                    <img
                      :src="URL.createObjectURL(categoryImg[0])"
                      alt="Preview"
                      style="width: 100%; height: 100%; object-fit: cover;"
                    >
                  </VAvatar>
                </div>
              </VCol>

              <VCol cols="12">
                <AppSelect
                  v-model="parentCategory"
                  label="Parent Category"
                  placeholder="Select Parent Category (optional)"
                  :items="parentCategoryOptions"
                  clearable
                />
              </VCol>

              <VCol cols="12">
                <p class="text-body-2 text-high-emphasis mb-1">
                  Description
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
                  placeholder="Select Category Status"
                  label="Select Category Status"
                  :items="['Published', 'Inactive', 'Scheduled']"
                />
              </VCol>

              <VCol cols="12">
                <div class="d-flex justify-start">
                  <VBtn
                    type="submit"
                    color="primary"
                    class="me-4"
                  >
                    {{ props.categoryId ? 'Update' : 'Add' }}
                  </VBtn>
                  <VBtn
                    color="error"
                    variant="tonal"
                    @click="closeForm"
                  >
                    Discard
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
