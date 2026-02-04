<script setup>
const widgetData = ref([
  {
    title: 'Продажи в магазине',
    value: '₽5,345',
    icon: 'tabler-smart-home',
    desc: '5к заказов',
    change: 5.7,
  },
  {
    title: 'Продажи на сайте',
    value: '₽674,347',
    icon: 'tabler-device-laptop',
    desc: '21к заказов',
    change: 12.4,
  },
  {
    title: 'Скидки',
    value: '₽14,235',
    icon: 'tabler-gift',
    desc: '6к заказов',
  },
  {
    title: 'Партнерская программа',
    value: '₽8,345',
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
    title: 'Наличие',
    key: 'stock',
    sortable: false,
  },
  {
    title: 'Артикул',
    key: 'sku',
  },
  {
    title: 'Цена',
    key: 'price',
  },
  {
    title: 'Количество',
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

const categories = ref([
  {
    title: 'Аксессуары',
    value: 'Accessories',
  },
  {
    title: 'Декор для дома',
    value: 'Home Decor',
  },
  {
    title: 'Электроника',
    value: 'Electronics',
  },
  {
    title: 'Обувь',
    value: 'Shoes',
  },
  {
    title: 'Офис',
    value: 'Office',
  },
  {
    title: 'Игры',
    value: 'Games',
  },
])

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
  if (category === 'Accessories')
    return {
      color: 'error',
      icon: 'tabler-device-watch',
    }
  if (category === 'Home Decor')
    return {
      color: 'info',
      icon: 'tabler-home',
    }
  if (category === 'Electronics')
    return {
      color: 'primary',
      icon: 'tabler-device-imac',
    }
  if (category === 'Shoes')
    return {
      color: 'success',
      icon: 'tabler-shoe',
    }
  if (category === 'Office')
    return {
      color: 'warning',
      icon: 'tabler-briefcase',
    }
  if (category === 'Games')
    return {
      color: 'primary',
      icon: 'tabler-device-gamepad-2',
    }
}

const truncateWords = (text, maxWords = 10) => {
  if (!text || typeof text !== 'string') return ''
  const words = text.trim().split(/\s+/)
  if (words.length <= maxWords) return text
  return words.slice(0, maxWords).join(' ') + '...'
}

const resolveStatus = statusMsg => {
  if (statusMsg === 'Scheduled')
    return {
      text: 'Запланировано',
      color: 'warning',
    }
  if (statusMsg === 'Published')
    return {
      text: 'Опубликовано',
      color: 'success',
    }
  if (statusMsg === 'Inactive')
    return {
      text: 'Неактивно',
      color: 'error',
    }
}

const {
  data: productsData,
  execute: fetchProducts,
} = await useApi(createUrl('/admin/products', {
  query: {
    q: searchQuery,
    stock: selectedStock,
    category: selectedCategory,
    status: selectedStatus,
    page,
    itemsPerPage,
    sortBy,
    orderBy,
  },
}))

// Преобразуем данные от бэкенда в формат, ожидаемый фронтендом
const products = computed(() => {
  if (!productsData.value || !Array.isArray(productsData.value)) {
    return []
  }
  
  return productsData.value.map(product => {
    // Форматируем цену (BigDecimal -> строка с 2 знаками после запятой)
    let priceFormatted = '₽0'
    if (product.price) {
      const priceValue = typeof product.price === 'number' 
        ? product.price 
        : parseFloat(product.price)
      priceFormatted = `₽${priceValue.toFixed(2)}`
    }
    
    return {
      id: product.id,
      productName: product.name || '',
      productBrand: product.description || '',
      category: product.category?.name || 'Uncategorized',
      stock: (product.stockQuantity || 0) > 0,
      sku: product.sku || '',
      price: priceFormatted,
      qty: product.stockQuantity || 0,
      status: product.isActive ? 'Published' : 'Inactive',
      image: product.imageUrl || '',
    }
  })
})

const totalProduct = computed(() => {
  if (!productsData.value || !Array.isArray(productsData.value)) {
    return 0
  }
  return productsData.value.length
})

const deleteProduct = async id => {
  try {
    await $api(`admin/products/${ id }`, { method: 'DELETE' })

    // Delete from selectedRows
    const index = selectedRows.value.findIndex(row => row === id)
    if (index !== -1)
      selectedRows.value.splice(index, 1)

    // Refetch products
    fetchProducts()
  } catch (error) {
    console.error('Ошибка при удалении товара:', error)
    // Можно добавить уведомление об ошибке
  }
}
</script>

<template>
  <div>
    <!-- 👉 widgets -->
    <VCard class="mb-6">
      <VCardText class="px-3">
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
              placeholder="Наличие"
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

          <!-- 👉 Add Product button -->
          <VBtn
            color="primary"
            prepend-icon="tabler-plus"
            @click="$router.push({ name: 'apps-ecommerce-product-add' })"
          >
            Добавить товар
          </VBtn>

        </div>
      </div>

      <VDivider class="mt-4" />

      <!-- 👉 Datatable  -->
      <VDataTableServer
        v-model:items-per-page="itemsPerPage"
        v-model:model-value="selectedRows"
        v-model:page="page"
        :headers="headers"
        show-select
        :items="products"
        :items-length="totalProduct"
        class="text-no-wrap"
        @update:options="updateOptions"
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
            <div class="d-flex flex-column min-w-0" style="max-width: 280px;">
              <span class="text-body-1 font-weight-medium text-high-emphasis" :title="item.productName">{{ truncateWords(item.productName, 3) }}</span>
              <span class="text-body-2 text-truncate" :title="item.productBrand">{{ truncateWords(item.productBrand, 3) }}</span>
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
          <IconBtn @click="$router.push(`/apps/ecommerce/product/edit/${item.id}`)">
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
      </VDataTableServer>
    </VCard>
  </div>
</template>
