<script setup>
import ECommerceAddCategoryDrawer from '@/views/apps/ecommerce/ECommerceAddCategoryDrawer.vue'

// Получаем данные категорий с бэкенда
const {
  data: categoriesData,
  execute: fetchCategories,
} = await useApi(createUrl('/admin/categories'))

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
    image: category.imageUrl || '',
  }))
})

const headers = [
  {
    title: 'Categories',
    key: 'categoryTitle',
  },
  {
    title: 'Total Products',
    key: 'totalProduct',
  },
  {
    title: 'Total Earning',
    key: 'totalEarning',
  },
  {
    title: 'Actions',
    key: 'actions',
    sortable: false,
  },
]

const itemsPerPage = ref(10)
const page = ref(1)
const searchQuery = ref('')
const isAddProductDrawerOpen = ref(false)
const editingCategoryId = ref(null)

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
</script>

<template>
  <div>
    <VCard>
      <VCardText>
        <div class="d-flex justify-sm-space-between flex-wrap gap-y-4 gap-x-6 justify-start">
          <AppTextField
            v-model="searchQuery"
            placeholder="Search Category"
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
              @click="openAddDrawer"
            >
              Add Category
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
              {{ (item.totalEarning).toLocaleString("en-IN", { style: "currency", currency: 'USD' }) }}
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
