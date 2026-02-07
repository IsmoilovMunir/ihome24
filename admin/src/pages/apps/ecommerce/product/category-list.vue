<script setup>
import ECommerceAddCategoryDrawer from '@/views/apps/ecommerce/ECommerceAddCategoryDrawer.vue'

// Получаем данные категорий с бэкенда
const {
  data: categoriesData,
  execute: fetchCategories,
} = await useApi(createUrl('/admin/categories'))

const withCacheBuster = url => {
  if (!url) return url
  const separator = url.includes('?') ? '&' : '?'
  return `${url}${separator}t=${Date.now()}`
}

// Преобразуем данные от бэкенда в формат, ожидаемый фронтендом
const categoryData = computed(() => {
  if (!categoriesData.value || !Array.isArray(categoriesData.value)) {
    return []
  }
  
  return categoriesData.value.map(category => ({
    id: category.id,
    categoryTitle: category.name || '',
    description: category.description || '',
    totalProduct: 0, // TODO: Добавить подсчет продуктов по категории
    totalEarning: 0, // TODO: Добавить подсчет доходов по категории
    image: withCacheBuster(category.imageUrl || ''),
  }))
})

const headers = [
  {
    title: 'Категории',
    key: 'categoryTitle',
  },
  {
    title: 'Всего товаров',
    key: 'totalProduct',
  },
  {
    title: 'Общий доход',
    key: 'totalEarning',
  },
  {
    title: 'Действия',
    key: 'actions',
    sortable: false,
  },
]

const itemsPerPage = ref(10)
const page = ref(1)
const searchQuery = ref('')
const isAddProductDrawerOpen = ref(false)
const editingCategoryId = ref(null)
const isDetailsDialogOpen = ref(false)
const selectedCategory = ref(null)
const isDeletingCategory = ref(false)

const openEditDrawer = (categoryId) => {
  editingCategoryId.value = categoryId
  isAddProductDrawerOpen.value = true
}

const closeDrawer = () => {
  editingCategoryId.value = null
}

const openAddDrawer = () => {
  editingCategoryId.value = null
  isAddProductDrawerOpen.value = true
}

const openDetailsDialog = category => {
  selectedCategory.value = category
  isDetailsDialogOpen.value = true
}

const deleteCategory = async categoryId => {
  if (!categoryId || isDeletingCategory.value) return
  const confirmed = window.confirm('Удалить категорию?')
  if (!confirmed) return

  try {
    isDeletingCategory.value = true
    await $api(`admin/categories/${categoryId}`, { method: 'DELETE' })
    await fetchCategories()
  } catch (error) {
    console.error('Ошибка при удалении категории:', error)
    alert('Не удалось удалить категорию')
  } finally {
    isDeletingCategory.value = false
  }
}
</script>

<template>
  <div>
    <VCard>
      <VCardText>
        <div class="d-flex justify-sm-space-between flex-wrap gap-y-4 gap-x-6 justify-start">
          <AppTextField
            v-model="searchQuery"
            placeholder="Поиск категории"
            style="max-inline-size: 280px; min-inline-size: 280px;"
          />

          <div class="d-flex align-center flex-wrap gap-4">
            <AppSelect
              :model-value="itemsPerPage"
              :items="[5, 10, 15].map(n => ({ value: n, title: String(n) }))"
              item-value="value"
              item-title="title"
              @update:model-value="itemsPerPage = Number($event)"
              style="max-inline-size: 100px; min-inline-size: 100px;"
            />
            <VBtn
              prepend-icon="tabler-plus"
              @click="openAddDrawer"
            >
              Добавить категорию
            </VBtn>
          </div>
        </div>
      </VCardText>

      <VDivider />

      <div class="category-table">
        <VDataTable
          v-model:items-per-page="itemsPerPage"
          v-model:page="page"
          :headers="headers"
          :items="categoryData"
          item-value="categoryTitle"
          :search="searchQuery"
          show-select
          class="text-no-wrap"
        >
          <template #item.actions="{ item }">
            <IconBtn @click="openEditDrawer(item.id)">
              <VIcon
                icon="tabler-edit"
                size="22"
              />
            </IconBtn>
            <VMenu>
              <template #activator="{ props }">
                <IconBtn v-bind="props">
                  <VIcon
                    icon="tabler-dots-vertical"
                    size="22"
                  />
                </IconBtn>
              </template>
              <VList>
                <VListItem @click="openDetailsDialog(item)">
                  <VListItemTitle>Детали</VListItemTitle>
                </VListItem>
                <VListItem @click="deleteCategory(item.id)">
                  <VListItemTitle>Удалить</VListItemTitle>
                </VListItem>
              </VList>
            </VMenu>
          </template>
          <template #item.categoryTitle="{ item }">
            <div class="d-flex gap-x-3 align-center">
              <VAvatar
                variant="tonal"
                rounded
                size="38"
              >
                <img
                  :src="item.image"
                  :alt="item.categoryTitle"
                  width="38"
                  height="38"
                >
              </VAvatar>
              <div>
                <h6 class="text-h6">
                  {{ item.categoryTitle }}
                </h6>
                <div class="text-body-2">
                  {{ item.description }}
                </div>
              </div>
            </div>
          </template>
          <template #item.totalEarning="{ item }">
            <div class="text-body-1 text-end pe-4">
              ₽{{ (item.totalEarning || 0).toLocaleString("ru-RU", { minimumFractionDigits: 2, maximumFractionDigits: 2 }) }}
            </div>
          </template>
          <template #item.totalProduct="{ item }">
            <div class="text-end pe-4">
              {{ (item.totalProduct).toLocaleString() }}
            </div>
          </template>

          <template #bottom>
            <TablePagination
              v-model:page="page"
              :items-per-page="itemsPerPage"
              :total-items="categoryData.length"
            />
          </template>
        </VDataTable>
      </div>
    </VCard>

    <ECommerceAddCategoryDrawer 
      v-model:is-drawer-open="isAddProductDrawerOpen"
      :category-id="editingCategoryId"
      @category-created="fetchCategories"
      @drawer-closed="closeDrawer"
    />

    <VDialog v-model="isDetailsDialogOpen" max-width="520">
      <VCard>
        <VCardTitle>Детали категории</VCardTitle>
        <VCardText v-if="selectedCategory">
          <div class="d-flex flex-column gap-2">
            <div><strong>ID:</strong> {{ selectedCategory.id }}</div>
            <div><strong>Название:</strong> {{ selectedCategory.categoryTitle }}</div>
            <div><strong>Описание:</strong> {{ selectedCategory.description || '—' }}</div>
            <div v-if="selectedCategory.image">
              <strong>Фото:</strong>
              <div class="mt-2">
                <img
                  :src="selectedCategory.image"
                  alt="Фото категории"
                  style="max-width: 100%; border-radius: 6px;"
                >
              </div>
            </div>
          </div>
        </VCardText>
        <VCardActions>
          <VSpacer />
          <VBtn variant="tonal" @click="isDetailsDialogOpen = false">Закрыть</VBtn>
        </VCardActions>
      </VCard>
    </VDialog>
  </div>
</template>

<style lang="scss">
.category-table {
  .v-table {
    th:nth-child(3),
    th:nth-child(4) {
      .v-data-table-header__content {
        justify-content: end;
      }
    }
  }
}
</style>
