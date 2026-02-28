<script setup>
const searchQuery = ref('')
const selectedStatus = ref(null)
const selectedRows = ref([])

// Data table options
const itemsPerPage = ref(6)
const page = ref(1)
const sortBy = ref()
const orderBy = ref()

const updateOptions = options => {
  page.value = options.page
  sortBy.value = options.sortBy[0]?.key
  orderBy.value = options.sortBy[0]?.order
}

// 👉 headers
const headers = [
  {
    title: '#',
    key: 'id',
  },
  {
    title: 'Статус',
    key: 'status',
    sortable: false,
  },
  {
    title: 'Всего',
    key: 'total',
  },
  {
    title: 'Дата выдачи',
    key: 'date',
  },
  {
    title: 'Баланс',
    key: 'balance',
  },
  {
    title: 'Действия',
    key: 'actions',
    sortable: false,
  },
]

const {
  data: invoiceData,
  execute: fetchInvoices,
} = await useApi(createUrl('/apps/invoice', {
  query: {
    q: searchQuery,
    status: selectedStatus,
    itemsPerPage,
    page,
    sortBy,
    orderBy,
  },
}))

const invoices = computed(() => invoiceData.value?.invoices ?? [])
const totalInvoices = computed(() => invoiceData.value?.totalInvoices ?? 0)

// 👉 Invoice balance variant resolver
const resolveInvoiceBalanceVariant = (balance, total) => {
  if (balance === total)
    return {
      status: 'Не оплачено',
      chip: { color: 'error' },
    }
  if (balance === 0)
    return {
      status: 'Оплачено',
      chip: { color: 'success' },
    }
  
  return {
    status: balance,
    chip: { variant: 'text' },
  }
}

const resolveInvoiceStatusVariantAndIcon = status => {
  if (status === 'Partial Payment' || status === 'Частичная оплата')
    return {
      variant: 'warning',
      icon: 'tabler-chart-pie-2',
    }
  if (status === 'Paid' || status === 'Оплачено')
    return {
      variant: 'success',
      icon: 'tabler-check',
    }
  if (status === 'Downloaded' || status === 'Скачано')
    return {
      variant: 'info',
      icon: 'tabler-arrow-down',
    }
  if (status === 'Draft' || status === 'Черновик')
    return {
      variant: 'primary',
      icon: 'tabler-folder',
    }
  if (status === 'Sent' || status === 'Отправлено')
    return {
      variant: 'secondary',
      icon: 'tabler-mail',
    }
  if (status === 'Past Due' || status === 'Просрочено')
    return {
      variant: 'error',
      icon: 'tabler-help',
    }
  
  return {
    variant: 'secondary',
    icon: 'tabler-x',
  }
}

const computedMoreList = computed(() => {
  return paramId => [
    {
      title: 'Скачать',
      value: 'download',
      prependIcon: 'tabler-download',
    },
    {
      title: 'Редактировать',
      value: 'edit',
      prependIcon: 'tabler-pencil',
      to: {
        name: 'apps-invoice-edit-id',
        params: { id: paramId },
      },
    },
    {
      title: 'Дублировать',
      value: 'duplicate',
      prependIcon: 'tabler-layers-intersect',
    },
  ]
})

const deleteInvoice = async id => {
  await $api(`/apps/invoice/${ id }`, { method: 'DELETE' })
  fetchInvoices()
}
</script>

<template>
  <VCard
    v-if="invoices"
    id="invoice-list"
  >
    <VCardText>
      <div class="d-flex justify-space-between flex-wrap gap-4">
        <div class="d-flex gap-4 align-center">
          <div class="d-flex align-center gap-x-2">
            <div>
              Показать
            </div>
            <AppSelect
              :model-value="itemsPerPage"
              :items="[
                { value: 6, title: '6' },
                { value: 10, title: '10' },
                { value: 25, title: '25' },
                { value: 50, title: '50' },
                { value: 100, title: '100' },
                { value: -1, title: 'Все' },
              ]"
              @update:model-value="itemsPerPage = parseInt($event, 10)"
            />
          </div>
          <!-- 👉 Create invoice -->
          <VBtn
            prepend-icon="tabler-plus"
            :to="{ name: 'apps-invoice-add' }"
          >
            Создать счет
          </VBtn>
        </div>
        <div class="d-flex align-center flex-wrap gap-4">
          <!-- 👉 Search  -->
          <div class="invoice-list-filter">
            <AppTextField
              v-model="searchQuery"
              placeholder="Поиск счета"
            />
          </div>
          <!-- 👉 Select status -->
          <div class="invoice-list-filter">
            <AppSelect
              v-model="selectedStatus"
              placeholder="Выберите статус"
              clearable
              clear-icon="tabler-x"
              single-line
              :items="['Скачано', 'Черновик', 'Отправлено', 'Оплачено', 'Частичная оплата', 'Просрочено']"
            />
          </div>
        </div>
      </div>
    </VCardText>

    <VDivider />

    <!-- SECTION Datatable -->
    <VDataTableServer
      v-model="selectedRows"
      v-model:items-per-page="itemsPerPage"
      v-model:page="page"
      show-select
      :items-length="totalInvoices"
      :headers="headers"
      :items="invoices"
      item-value="id"
      class="text-no-wrap"
      @update:options="updateOptions"
    >
      <!-- id -->
      <template #item.id="{ item }">
        <RouterLink :to="{ name: 'apps-invoice-preview-id', params: { id: item.id } }">
          #{{ item.id }}
        </RouterLink>
      </template>

      <!-- status -->
      <template #item.status="{ item }">
        <VTooltip>
          <template #activator="{ props }">
            <VAvatar
              :size="28"
              v-bind="props"
              :color="resolveInvoiceStatusVariantAndIcon(item.invoiceStatus).variant"
              variant="tonal"
            >
              <VIcon
                :size="16"
                :icon="resolveInvoiceStatusVariantAndIcon(item.invoiceStatus).icon"
              />
            </VAvatar>
          </template>
          <p class="mb-0">
            {{ item.invoiceStatus }}
          </p>
          <p class="mb-0">
            Баланс: {{ item.balance }}
          </p>
          <p class="mb-0">
            Срок оплаты: {{ item.dueDate }}
          </p>
        </VTooltip>
      </template>

      <!-- Total -->
      <template #item.total="{ item }">
        ₽{{ item.total }}
      </template>

      <!-- Date -->
      <template #item.date="{ item }">
        {{ item.issuedDate }}
      </template>

      <!-- Balance -->
      <template #item.balance="{ item }">
        <VChip
          v-if="typeof ((resolveInvoiceBalanceVariant(item.balance, item.total)).status) === 'string'"
          :color="resolveInvoiceBalanceVariant(item.balance, item.total).chip.color"
          label
          size="x-small"
        >
          {{ (resolveInvoiceBalanceVariant(item.balance, item.total)).status }}
        </VChip>

        <template v-else>
          <span class="text-base text-high-emphasis">
            {{ Number((resolveInvoiceBalanceVariant(item.balance, item.total)).status) > 0 ? `₽${(resolveInvoiceBalanceVariant(item.balance, item.total)).status}` : `-₽${Math.abs(Number((resolveInvoiceBalanceVariant(item.balance, item.total)).status))}` }}
          </span>
        </template>
      </template>

      <!-- Actions -->
      <template #item.actions="{ item }">
        <IconBtn @click="deleteInvoice(item.id)">
          <VIcon icon="tabler-trash" />
        </IconBtn>

        <IconBtn :to="{ name: 'apps-invoice-preview-id', params: { id: item.id } }">
          <VIcon icon="tabler-eye" />
        </IconBtn>

        <MoreBtn
          :menu-list="computedMoreList(item.id)"
          item-props
          color="undefined"
          size="small"
        />
      </template>

      <!-- pagination -->
      <template #bottom>
        <TablePagination
          v-model:page="page"
          :items-per-page="itemsPerPage"
          :total-items="totalInvoices"
        />
      </template>
    </VDataTableServer>
    <!-- !SECTION -->
  </VCard>
</template>

<style lang="scss">
#invoice-list {
  .invoice-list-actions {
    inline-size: 8rem;
  }

  .invoice-list-filter {
    inline-size: 12rem;
  }
}
</style>
