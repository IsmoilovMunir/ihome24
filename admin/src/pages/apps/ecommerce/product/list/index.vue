<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { $api } from '@/utils/api'

const widgetData = ref([
  {
    title: 'Продажи в магазине',
    value: '$5,345',
    icon: 'tabler-smart-home',
    desc: '5k заказов',
    change: 5.7,
  },
  {
    title: 'Продажи на сайте',
    value: '$674,347',
    icon: 'tabler-device-laptop',
    desc: '21k заказов',
    change: 12.4,
  },
  {
    title: 'Скидка',
    value: '$14,235',
    icon: 'tabler-gift',
    desc: '6k заказов',
  },
  {
    title: 'Партнёрская программа',
    value: '$8,345',
    icon: 'tabler-wallet',
    desc: '150 заказов',
    change: -3.5,
  },
])

const headers = [
  {
    title: 'Товар',
    key: 'product',
  },
  {
    title: 'Категория',
    key: 'category',
  },
  {
    title: 'Склад',
    key: 'stock',
    sortable: false,
  },
  {
    title: 'SKU',
    key: 'sku',
  },
  {
    title: 'Цена',
    key: 'price',
  },
  {
    title: 'Кол-во',
    key: 'qty',
  },
  {
    title: 'Статус',
    key: 'status',
  },
  {
    title: 'Действия',
    key: 'actions',
    sortable: false,
  },
]

const selectedStatus = ref()
const selectedCategory = ref()
const selectedStock = ref()
const searchQuery = ref('')
const selectedRows = ref([])

const status = ref([
  {
    title: 'Запланировано',
    value: 'Scheduled',
  },
  {
    title: 'Опубликовано',
    value: 'Published',
  },
  {
    title: 'Неактивно',
    value: 'Inactive',
  },
])

const categories = ref([])
const isLoading = ref(false)

const stockStatus = ref([
  {
    title: 'В наличии',
    value: true,
  },
  {
    title: 'Нет в наличии',
    value: false,
  },
])

// Data table options
const itemsPerPage = ref(10)
const page = ref(1)
const sortBy = ref()
const orderBy = ref()

const updateOptions = options => {
  sortBy.value = options.sortBy[0]?.key
  orderBy.value = options.sortBy[0]?.order
}

const resolveCategory = category => {
  // Универсальная функция для определения цвета и иконки категории
  if (!category) return { color: 'default', icon: 'tabler-category' }
  
  const categoryName = category.name || category
  const lowerName = categoryName.toLowerCase()
  
  if (lowerName.includes('accessories') || lowerName.includes('аксессуар'))
    return { color: 'error', icon: 'tabler-device-watch' }
  if (lowerName.includes('home') || lowerName.includes('дом'))
    return { color: 'info', icon: 'tabler-home' }
  if (lowerName.includes('electronic') || lowerName.includes('электро'))
    return { color: 'primary', icon: 'tabler-device-imac' }
  if (lowerName.includes('shoe') || lowerName.includes('обувь'))
    return { color: 'success', icon: 'tabler-shoe' }
  if (lowerName.includes('office') || lowerName.includes('офис'))
    return { color: 'warning', icon: 'tabler-briefcase' }
  if (lowerName.includes('game') || lowerName.includes('игр'))
    return { color: 'primary', icon: 'tabler-device-gamepad-2' }
  
  return { color: 'default', icon: 'tabler-category' }
}

const resolveStatus = statusMsg => {
  if (!statusMsg) return { text: 'Неизвестно', color: 'default' }
  
  const status = statusMsg.toLowerCase()
  if (status === 'scheduled' || status === 'запланировано')
    return { text: 'Запланировано', color: 'warning' }
  if (status === 'published' || status === 'опубликовано')
    return { text: 'Опубликовано', color: 'success' }
  if (status === 'inactive' || status === 'неактивно' || status === 'draft' || status === 'черновик')
    return { text: 'Неактивно', color: 'error' }
  
  return { text: statusMsg, color: 'default' }
}

const products = ref([])
const totalProduct = ref(0)

// Загрузка категорий для фильтра
const loadCategories = async () => {
  try {
    const response = await $api('/admin/categories', { method: 'GET' })
    categories.value = response.map(cat => ({
      title: cat.name,
      value: cat.id,
    }))
  } catch (error) {
    console.error('Ошибка при загрузке категорий:', error)
  }
}

// Загрузка товаров
const fetchProducts = async () => {
  try {
    isLoading.value = true
    const response = await $api('/admin/products', { method: 'GET' })
    products.value = response.map(product => ({
      id: product.id,
      productName: product.name,
      productBrand: product.brand || '',
      category: product.category ? product.category.name : '',
      categoryObj: product.category,
      stock: product.isActive || false,
      sku: product.sku || '',
      price: product.price ? `₽${product.price}` : '₽0',
      qty: product.stockQuantity || 0,
      status: product.status || 'draft',
      image: product.imageUrl || null,
    }))
    totalProduct.value = products.value.length
  } catch (error) {
    console.error('Ошибка при загрузке товаров:', error)
    products.value = []
    totalProduct.value = 0
  } finally {
    isLoading.value = false
  }
}

const deleteProduct = async id => {
  try {
    await $api(`/admin/products/${id}`, { method: 'DELETE' })
    
    // Delete from selectedRows
    const index = selectedRows.value.findIndex(row => row === id)
    if (index !== -1)
      selectedRows.value.splice(index, 1)

    // Refetch products
    await fetchProducts()
  } catch (error) {
    console.error('Ошибка при удалении товара:', error)
    alert('Ошибка при удалении товара: ' + (error.data?.message || error.message || 'Неизвестная ошибка'))
  }
}

// Загружаем данные при монтировании
onMounted(() => {
  loadCategories()
  fetchProducts()
})

// Реактивная загрузка при изменении фильтров
watch([searchQuery, selectedStatus, selectedCategory, selectedStock], () => {
  fetchProducts()
})
</script>

<template>
  <div>
    <!-- 👉 widgets -->
    <VCard class="mb-6">
      <VCardText class="px-3">
        <VRow>
          <template
            v-for="(data, id) in widgetData"
            :key="id"
          >
            <VCol
              cols="12"
              sm="6"
              md="3"
              class="px-6"
            >
              <div
                class="d-flex justify-space-between"
                :class="$vuetify.display.xs
                  ? id !== widgetData.length - 1 ? 'border-b pb-4' : ''
                  : $vuetify.display.sm
                    ? id < (widgetData.length / 2) ? 'border-b pb-4' : ''
                    : ''"
              >
                <div class="d-flex flex-column gap-y-1">
                  <div class="text-body-1 text-capitalize">
                    {{ data.title }}
                  </div>

                  <h4 class="text-h4">
                    {{ data.value }}
                  </h4>

                  <div class="d-flex align-center gap-x-2">
                    <div class="text-no-wrap">
                      {{ data.desc }}
                    </div>

                    <VChip
                      v-if="data.change"
                      label
                      :color="data.change > 0 ? 'success' : 'error'"
                      size="small"
                    >
                      {{ prefixWithPlus(data.change) }}%
                    </VChip>
                  </div>
                </div>

                <VAvatar
                  variant="tonal"
                  rounded
                  size="44"
                >
                  <VIcon
                    :icon="data.icon"
                    size="28"
                    class="text-high-emphasis"
                  />
                </VAvatar>
              </div>
            </VCol>
            <VDivider
              v-if="$vuetify.display.mdAndUp ? id !== widgetData.length - 1
                : $vuetify.display.smAndUp ? id % 2 === 0
                  : false"
              vertical
              inset
              length="92"
            />
          </template>
        </VRow>
      </VCardText>
    </VCard>

    <!-- 👉 products -->
    <VCard
      title="Фильтры"
      class="mb-6"
    >
      <VCardText>
        <VRow>
          <!-- 👉 Select Status -->
          <VCol
            cols="12"
            sm="4"
          >
            <AppSelect
              v-model="selectedStatus"
              placeholder="Статус"
              :items="status"
              clearable
              clear-icon="tabler-x"
            />
          </VCol>

          <!-- 👉 Select Category -->
          <VCol
            cols="12"
            sm="4"
          >
            <AppSelect
              v-model="selectedCategory"
              placeholder="Категория"
              :items="categories"
              clearable
              clear-icon="tabler-x"
            />
          </VCol>

          <!-- 👉 Select Stock Status -->
          <VCol
            cols="12"
            sm="4"
          >
            <AppSelect
              v-model="selectedStock"
              placeholder="Склад"
              :items="stockStatus"
              clearable
              clear-icon="tabler-x"
            />
          </VCol>
        </VRow>
      </VCardText>

      <VDivider />

      <div class="d-flex flex-wrap gap-4 ma-6">
        <div class="d-flex align-center">
          <!-- 👉 Search  -->
          <AppTextField
            v-model="searchQuery"
            placeholder="Поиск товара"
            style="inline-size: 200px;"
            class="me-3"
          />
        </div>

        <VSpacer />
        <div class="d-flex gap-4 flex-wrap align-center">
          <AppSelect
            v-model="itemsPerPage"
            :items="[5, 10, 20, 25, 50]"
          />
          <!-- 👉 Export button -->
          <VBtn
            variant="tonal"
            color="secondary"
            prepend-icon="tabler-upload"
          >
            Экспорт
          </VBtn>

          <VBtn
            color="primary"
            prepend-icon="tabler-plus"
            @click="$router.push('/apps/ecommerce/product/add')"
          >
            Добавить товар
          </VBtn>
        </div>
      </div>

      <VDivider class="mt-4" />

      <!-- 👉 Datatable  -->
      <VDataTable
        v-model:items-per-page="itemsPerPage"
        v-model:model-value="selectedRows"
        v-model:page="page"
        :headers="headers"
        show-select
        :items="products"
        :items-length="totalProduct"
        class="text-no-wrap"
        :loading="isLoading"
      >
        <!-- product  -->
        <template #item.product="{ item }">
          <div class="d-flex align-center gap-x-4">
            <VAvatar
              v-if="item.image"
              size="38"
              variant="tonal"
              rounded
              :image="item.image"
            />
            <div class="d-flex flex-column">
              <span class="text-body-1 font-weight-medium text-high-emphasis">{{ item.productName }}</span>
              <span class="text-body-2">{{ item.productBrand }}</span>
            </div>
          </div>
        </template>

        <!-- category -->
        <template #item.category="{ item }">
          <VAvatar
            size="30"
            variant="tonal"
            :color="resolveCategory(item.category)?.color"
            class="me-4"
          >
            <VIcon
              :icon="resolveCategory(item.category)?.icon"
              size="18"
            />
          </VAvatar>
          <span class="text-body-1 text-high-emphasis">{{ item.category }}</span>
        </template>

        <!-- stock -->
        <template #item.stock="{ item }">
          <VSwitch :model-value="item.stock" />
        </template>

        <!-- status -->
        <template #item.status="{ item }">
          <VChip
            v-bind="resolveStatus(item.status)"
            density="default"
            label
            size="small"
          />
        </template>

        <!-- Actions -->
        <template #item.actions="{ item }">
          <IconBtn @click="$router.push(`/apps/ecommerce/product/add?id=${item.id}`)">
            <VIcon icon="tabler-edit" />
          </IconBtn>

          <IconBtn>
            <VIcon icon="tabler-dots-vertical" />
            <VMenu activator="parent">
              <VList>
                <VListItem
                  value="download"
                  prepend-icon="tabler-download"
                >
                  Скачать
                </VListItem>

                <VListItem
                  value="delete"
                  prepend-icon="tabler-trash"
                  @click="deleteProduct(item.id)"
                >
                  Удалить
                </VListItem>

                <VListItem
                  value="duplicate"
                  prepend-icon="tabler-copy"
                >
                  Дублировать
                </VListItem>
              </VList>
            </VMenu>
          </IconBtn>
        </template>

        <!-- pagination -->
        <template #bottom>
          <TablePagination
            v-model:page="page"
            :items-per-page="itemsPerPage"
            :total-items="totalProduct"
          />
        </template>
      </VDataTable>
    </VCard>
  </div>
</template>
