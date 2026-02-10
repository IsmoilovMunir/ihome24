<script setup>
import * as XLSX from 'xlsx'

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

const categories = ref([])

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
  const map = {
    'Accessories': { color: 'error', icon: 'tabler-device-watch' },
    'Home Decor': { color: 'info', icon: 'tabler-home' },
    'Electronics': { color: 'primary', icon: 'tabler-device-imac' },
    'Shoes': { color: 'success', icon: 'tabler-shoe' },
    'Office': { color: 'warning', icon: 'tabler-briefcase' },
    'Games': { color: 'primary', icon: 'tabler-device-gamepad-2' },
  }
  return map[category] ?? { color: 'secondary', icon: 'tabler-tag' }
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
    categoryId: selectedCategory,
    status: selectedStatus,
    page,
    itemsPerPage,
    sortBy,
    orderBy,
  },
}))

onMounted(async () => {
  try {
    const data = await $api('/admin/categories')
    categories.value = (data ?? []).map(c => ({ title: c.name, value: c.id }))
  } catch {
    categories.value = []
  }
})

watch([searchQuery, selectedCategory, selectedStock, selectedStatus], () => {
  page.value = 1
})

const debouncedFetch = useDebounceFn(() => fetchProducts(), 300)

watch([searchQuery, selectedCategory, selectedStock, selectedStatus, page, itemsPerPage, sortBy, orderBy], () => {
  debouncedFetch()
}, { immediate: true })

// Преобразуем данные от бэкенда в формат, ожидаемый фронтендом
const products = computed(() => {
  const list = productsData.value?.products ?? productsData.value
  if (!list || !Array.isArray(list)) {
    return []
  }
  
  return list.map(product => {
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
  const data = productsData.value
  if (data?.total != null) return Number(data.total)
  if (Array.isArray(data)) return data.length
  return 0
})

const deleteProduct = async id => {
  try {
    await $api(`admin/products/${ id }`, { method: 'DELETE' })

    // Delete from selectedRows
    const index = selectedRows.value.findIndex(row => (typeof row === 'object' ? row?.id : row) === id)
    if (index !== -1)
      selectedRows.value.splice(index, 1)

    // Refetch products
    fetchProducts()
  } catch (error) {
    console.error('Ошибка при удалении товара:', error)
  }
}

const productToRow = p => ({
  'ID': p.id,
  'Название': p.name ?? p.productName ?? '',
  'Описание': p.description ?? p.productBrand ?? '',
  'Бренд': p.brand ?? '',
  'Категория': (p.category?.name ?? p.category ?? '') || 'Uncategorized',
  'Артикул': p.sku ?? '',
  'Цена': p.price != null ? (typeof p.price === 'number' ? p.price : parseFloat(String(p.price).replace(/[^\d.-]/g, '') || 0)) : '',
  'Старая цена': p.oldPrice != null ? parseFloat(p.oldPrice) : '',
  'Количество': p.stockQuantity ?? p.qty ?? 0,
  'В наличии': (p.stockQuantity ?? p.qty ?? 0) > 0 ? 'Да' : 'Нет',
  'Статус': (p.isActive === true || p.status === 'Published') ? 'Опубликовано' : ((p.isActive === false || p.status === 'Inactive') ? 'Неактивно' : (p.status ?? '')),
  'URL изображения': p.imageUrl ?? p.image ?? '',
  'Дата создания': p.createdAt ? new Date(p.createdAt).toLocaleString('ru-RU') : '',
})

const exportToExcel = async () => {
  try {
    let rows = []
    if (selectedRows.value?.length > 0) {
      const items = selectedRows.value.map(r => (typeof r === 'object' ? r : products.value.find(p => p.id === r))).filter(Boolean)
      rows = items.map(item => {
        const raw = productsData.value?.products?.find(p => p.id === item.id)
        return productToRow(raw ? { ...raw, ...item } : item)
      })
    } else {
      const params = new URLSearchParams()
      if (searchQuery.value) params.set('q', searchQuery.value)
      if (selectedCategory.value) params.set('categoryId', selectedCategory.value)
      if (selectedStock.value != null) params.set('stock', selectedStock.value)
      if (selectedStatus.value) params.set('status', selectedStatus.value)
      params.set('itemsPerPage', '100')
      let list = []
      for (let pageNum = 1; ; pageNum++) {
        params.set('page', String(pageNum))
        const data = await $api(`/admin/products?${params}`)
        const chunk = data?.products ?? data ?? []
        list = list.concat(chunk)
        if (chunk.length < 100)
          break
      }
      rows = list.map(p => productToRow(p))
    }
    if (rows.length === 0) {
      // eslint-disable-next-line no-alert
      alert('Нет данных для экспорта. Выберите товары или убедитесь, что список не пуст.')
      return
    }
    const ws = XLSX.utils.json_to_sheet(rows)
    const wb = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(wb, ws, 'Товары')
    XLSX.writeFile(wb, `товары_${new Date().toISOString().slice(0, 10)}.xlsx`)
  } catch (error) {
    console.error('Ошибка экспорта:', error)
    // eslint-disable-next-line no-alert
    alert('Ошибка при экспорте. Проверьте консоль.')
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
              item-value="value"
              item-title="title"
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
              item-value="value"
              item-title="title"
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
              item-value="value"
              item-title="title"
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
            :model-value="itemsPerPage"
            :items="[5, 10, 20, 25, 50].map(n => ({ value: n, title: String(n) }))"
            item-value="value"
            item-title="title"
            @update:model-value="itemsPerPage = Number($event)"
          />
          <!-- 👉 Export button -->
          <VBtn
            variant="tonal"
            color="secondary"
            prepend-icon="tabler-file-export"
            @click="exportToExcel"
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
