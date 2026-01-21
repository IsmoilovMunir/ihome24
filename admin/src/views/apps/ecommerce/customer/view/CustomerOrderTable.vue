<script setup>
const searchQuery = ref('')

// Data table options
const itemsPerPage = ref(10)
const page = ref(1)
const sortBy = ref()
const orderBy = ref()

const updateOptions = options => {
  sortBy.value = options.sortBy[0]?.key
  orderBy.value = options.sortBy[0]?.order
}

const headers = [
  {
    title: 'Заказ',
    key: 'order',
  },
  {
    title: 'Дата',
    key: 'date',
  },
  {
    title: 'Статус',
    key: 'status',
  },
  {
    title: 'Потрачено',
    key: 'spent',
  },
  {
    title: 'Действия',
    key: 'actions',
    sortable: false,
  },
]

const resolveStatus = status => {
  if (status === 'Delivered' || status === 'Доставлено')
    return { color: 'success' }
  if (status === 'Out for Delivery' || status === 'В доставке')
    return { color: 'primary' }
  if (status === 'Ready to Pickup' || status === 'Готово к получению')
    return { color: 'info' }
  if (status === 'Dispatched' || status === 'Отправлено')
    return { color: 'warning' }
}

const {
  data: ordersData,
  execute: fetchOrders,
} = await useApi(createUrl('/apps/ecommerce/orders', {
  query: {
    q: searchQuery,
    page,
    itemsPerPage,
    sortBy,
    orderBy,
  },
}))

const orders = computed(() => ordersData.value?.orders || [])
const totalOrder = computed(() => ordersData.value?.total || 0)

const deleteOrder = async id => {
  await $api(`/apps/ecommerce/orders/${ id }`, { method: 'DELETE' })
  fetchOrders()
}
</script>

<template>
  <VCard>
    <VCardText>
      <div class="d-flex justify-space-between flex-wrap align-center gap-4">
        <h5 class="text-h5">
          Orders placed
        </h5>
        <div>
          <AppTextField
            v-model="searchQuery"
            placeholder="Search Order"
            style=" max-inline-size: 200px; min-inline-size: 200px;"
          />
        </div>
      </div>
    </VCardText>

    <VDivider />
    <VDataTableServer
      v-model:items-per-page="itemsPerPage"
      v-model:page="page"
      :headers="headers"
      :items="orders"
      item-value="id"
      :items-length="totalOrder"
      class="text-no-wrap"
      @update:options="updateOptions"
    >
      <!-- Order ID -->
      <template #item.order="{ item }">
        <RouterLink :to="{ name: 'apps-ecommerce-order-details-id', params: { id: item.order } }">
          #{{ item.order }}
        </RouterLink>
      </template>

      <!-- Date -->
      <template #item.date="{ item }">
        {{ new Date(item.date).toDateString() }}
      </template>

      <!-- Status -->
      <template #item.status="{ item }">
        <VChip
          label
          :color="resolveStatus(item.status)?.color"
          size="small"
        >
          {{ item.status }}
        </VChip>
      </template>

      <!-- Spent -->
      <template #item.spent="{ item }">
        ${{ item.spent }}
      </template>

      <!-- Actions -->
      <template #item.actions="{ item }">
        <IconBtn>
          <VIcon icon="tabler-dots-vertical" />
          <VMenu activator="parent">
            <VList>
              <VListItem
                value="view"
                :to="{ name: 'apps-ecommerce-order-details-id', params: { id: item.order } }"
              >
                Просмотр
              </VListItem>
              <VListItem
                value="delete"
                @click="deleteOrder(item.id)"
              >
                Удалить
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
          :total-items="totalOrder"
        />
      </template>
    </VDataTableServer>
  </VCard>
</template>
