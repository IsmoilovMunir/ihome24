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
import { $api } from '@/utils/api'

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

const emit = defineEmits(['update:isDrawerOpen', 'category-added'])

const handleDrawerModelValueUpdate = val => {
  emit('update:isDrawerOpen', val)
  // Если drawer закрывается и это не режим редактирования, сбрасываем форму
  if (!val && !props.categoryId) {
    resetForm()
  }
}

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

const refVForm = ref()
const categoryTitle = ref()
const categorySlug = ref()
const categoryImgUrl = ref()
const parentCategory = ref()
const parentStatus = ref('Published') // Значение остается 'Published' для API, но отображается как 'Опубликовано'
const parentCategories = ref([])
const isSubmitting = ref(false)

// Загрузка категорий для выбора родительской категории
const loadCategories = async () => {
  try {
    const response = await $api('/admin/categories', {
      method: 'GET',
    })
    parentCategories.value = response.map(cat => ({
      title: cat.name,
      value: cat.id,
    }))
  } catch (error) {
    console.error('Ошибка при загрузке категорий:', error)
  }
}

// Загрузка данных категории для редактирования
const loadCategoryData = async () => {
  if (!props.categoryId) return
  
  try {
    const response = await $api(`/admin/categories/${props.categoryId}`, {
      method: 'GET',
    })
    
    categoryTitle.value = response.name || ''
    categorySlug.value = response.slug || ''
    categoryImgUrl.value = response.imageUrl || null
    parentCategory.value = response.parentId || null
    parentStatus.value = response.isActive ? 'Published' : 'Inactive'
    
    if (response.description && editor.value) {
      editor.value.commands.setContent(response.description)
    } else if (editor.value) {
      editor.value.commands.clearContent()
    }
  } catch (error) {
    console.error('Ошибка при загрузке данных категории:', error)
    alert('Ошибка при загрузке данных категории: ' + (error.data?.message || error.message || 'Неизвестная ошибка'))
  }
}

// Загрузка категорий при открытии drawer
watch(() => props.isDrawerOpen, async (isOpen) => {
  if (isOpen) {
    await loadCategories()
    // Если есть categoryId, загружаем данные для редактирования
    if (props.categoryId) {
      await loadCategoryData()
    } else {
      // Только при добавлении новой категории сбрасываем форму
      resetForm()
    }
  }
})

// Отдельный watch на categoryId для случаев, когда ID меняется при открытом drawer
watch(() => props.categoryId, async (newId) => {
  if (props.isDrawerOpen && newId) {
    await loadCategoryData()
  }
}, { immediate: false })

// Функция для создания или обновления категории
const createCategory = async () => {
  if (!refVForm.value?.validate()) {
    return
  }

  if (!categoryTitle.value || !categorySlug.value) {
    alert('Пожалуйста, заполните обязательные поля: Название и URL-адрес')
    return
  }

  try {
    isSubmitting.value = true

    const categoryData = {
      name: categoryTitle.value,
      slug: categorySlug.value.toLowerCase().replace(/\s+/g, '-').replace(/[^a-z0-9-]/g, ''),
      description: editor.value?.getHTML() || null,
      imageUrl: categoryImgUrl.value || null,
      parentId: parentCategory.value || null,
      isActive: parentStatus.value === 'Published',
      sortOrder: 0,
    }

    let response
    if (props.categoryId) {
      // Редактирование существующей категории
      response = await $api(`/admin/categories/${props.categoryId}`, {
        method: 'PUT',
        body: categoryData,
      })
      console.log('Категория обновлена успешно:', response)
      alert('Категория успешно обновлена!')
    } else {
      // Создание новой категории
      response = await $api('/admin/categories', {
        method: 'POST',
        body: categoryData,
      })
      console.log('Категория создана успешно:', response)
      alert('Категория успешно создана!')
    }
    
    // Уведомление родительского компонента
    emit('category-added', response)
    
    // Закрываем drawer и сбрасываем форму только после успешного сохранения
    emit('update:isDrawerOpen', false)
    
    // Сбрасываем форму только если это было добавление, а не редактирование
    if (!props.categoryId) {
      resetForm()
    }
  } catch (error) {
    console.error('Ошибка при сохранении категории:', error)
    alert('Ошибка при сохранении категории: ' + (error.data?.message || error.message || 'Неизвестная ошибка'))
  } finally {
    isSubmitting.value = false
  }
}

const resetForm = () => {
  // Сбрасываем форму только если это не режим редактирования
  if (!props.categoryId) {
    categoryTitle.value = null
    categorySlug.value = null
    categoryImgUrl.value = null
    parentCategory.value = null
    parentStatus.value = 'Published' // Значение для API
    if (editor.value) {
      editor.value.commands.clearContent()
    }
    refVForm.value?.reset()
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
            @submit.prevent="createCategory"
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
                  placeholder="moda-trendy"
                />
              </VCol>

              <VCol cols="12">
                <AppTextField
                  v-model="categoryImgUrl"
                  label="URL изображения"
                  placeholder="https://example.com/image.jpg"
                  hint="Введите URL изображения (загрузка файла будет добавлена позже)"
                  persistent-hint
                />
              </VCol>

              <VCol cols="12">
                <AppSelect
                  v-model="parentCategory"
                  label="Родительская категория"
                  placeholder="Выберите родительскую категорию (необязательно)"
                  :items="parentCategories"
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
                    :loading="isSubmitting"
                    :disabled="isSubmitting"
                  >
                    {{ props.categoryId ? 'Сохранить' : 'Добавить' }}
                  </VBtn>
                  <VBtn
                    color="error"
                    variant="tonal"
                    @click="resetForm"
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
