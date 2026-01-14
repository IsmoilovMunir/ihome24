<script setup>
import ECommerceAddCategoryDrawer from '@/views/apps/ecommerce/ECommerceAddCategoryDrawer.vue'
import { $api } from '@/utils/api'

const categoryData = ref([])
const isLoading = ref(false)

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

// Загрузка категорий из API
const loadCategories = async () => {
  try {
    isLoading.value = true
    const response = await $api('/admin/categories', {
      method: 'GET',
    })
    
    // Преобразуем данные из API в формат для таблицы
    categoryData.value = response.map(cat => ({
      id: cat.id,
      categoryTitle: cat.name,
      description: cat.description || '',
      totalProduct: 0, // Можно добавить подсчет товаров позже
      totalEarning: 0, // Можно добавить подсчет доходов позже
      image: cat.imageUrl || '/src/assets/images/placeholder.png',
      isActive: cat.isActive,
    }))
  } catch (error) {
    console.error('Ошибка при загрузке категорий:', error)
    alert('Ошибка при загрузке категорий: ' + (error.data?.message || error.message || 'Неизвестная ошибка'))
  } finally {
    isLoading.value = false
  }
}

// Обработчик добавления новой категории
const handleCategoryAdded = () => {
  loadCategories() // Перезагружаем список категорий
  editingCategoryId.value = null // Сбрасываем ID редактируемой категории
}

// Функция редактирования категории
const editCategory = (item) => {
  editingCategoryId.value = item.id
  isAddProductDrawerOpen.value = true
}

// Загружаем категории при монтировании компонента
onMounted(() => {
  loadCategories()
})
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
              v-model="itemsPerPage"
              :items="[5, 10, 15]"
              style="max-inline-size: 100px; min-inline-size: 100px;"
            />
            <VBtn
              prepend-icon="tabler-plus"
              @click="isAddProductDrawerOpen = !isAddProductDrawerOpen"
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
          :loading="isLoading"
          item-value="id"
          :search="searchQuery"
          show-select
          class="text-no-wrap"
        >
          <template #item.actions="{ item }">
            <IconBtn @click="editCategory(item)">
              <VIcon
                icon="tabler-edit"
                size="22"
              />
            </IconBtn>
            <IconBtn>
              <VIcon
                icon="tabler-dots-vertical"
                size="22"
              />
            </IconBtn>
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
              {{ (item.totalEarning).toLocaleString("ru-RU", { style: "currency", currency: 'RUB' }) }}
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
      @category-added="handleCategoryAdded"
      @update:is-drawer-open="(val) => { 
        isAddProductDrawerOpen = val
        // Сбрасываем ID редактируемой категории только после закрытия drawer
        if (!val) {
          editingCategoryId = null
        }
      }"
    />
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
