<script setup>
definePage({
  meta: {
    action: 'manage',
    subject: 'Ecommerce',
    navActiveLink: 'apps-ecommerce-wholesale-lead-list',
  },
})

const searchQuery = ref('')
const statusFilter = ref(null)
const itemsPerPage = ref(10)
const page = ref(1)
const sortBy = ref('createdAt')
const orderBy = ref('desc')
const loading = ref(false)
const actionLoadingId = ref(null)
const snackbar = ref({ show: false, text: '', color: 'error' })

const widgetData = ref([
  { title: 'Новые', value: 0, icon: 'tabler-inbox', key: 'new', color: 'info' },
  { title: 'В работе', value: 0, icon: 'tabler-user-check', key: 'inProgress', color: 'warning' },
  { title: 'Завершены', value: 0, icon: 'tabler-circle-check', key: 'done', color: 'success' },
  { title: 'Всего', value: 0, icon: 'tabler-file-text', key: 'total', color: 'primary' },
])

const headers = [
  { title: 'ID', key: 'id' },
  { title: 'Клиент', key: 'name' },
  { title: 'Телефон', key: 'phone' },
  { title: 'Дата', key: 'createdAt' },
  { title: 'Статус', key: 'status' },
  { title: 'Менеджер', key: 'managerName', sortable: false },
  { title: 'Комментарий', key: 'message', sortable: false },
  { title: 'Действия', key: 'actions', sortable: false },
]

const statusItems = [
  { title: 'Все', value: null },
  { title: 'Новые', value: 'NEW' },
  { title: 'В работе', value: 'IN_PROGRESS' },
  { title: 'Завершены', value: 'DONE' },
]

const leads = ref([])
const totalLeads = ref(0)

const resolveLeadChip = status => {
  if (status === 'IN_PROGRESS') return { text: 'В работе', color: 'warning' }
  if (status === 'DONE') return { text: 'Завершена', color: 'success' }
  return { text: 'Новая', color: 'info' }
}

const formatDate = value => {
  if (!value) return '—'
  return new Date(value).toLocaleString('ru-RU', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

const fetchStats = async () => {
  try {
    const data = await $api('/apps/ecommerce/wholesale-leads/stats')
    widgetData.value = widgetData.value.map(item => ({
      ...item,
      value: data?.[item.key] ?? 0,
    }))
  } catch (e) {
    console.error('Ошибка загрузки статистики заявок:', e)
  }
}

const fetchLeads = async () => {
  loading.value = true
  try {
    const params = new URLSearchParams({
      page: String(page.value),
      itemsPerPage: String(itemsPerPage.value),
      sortBy: sortBy.value || 'createdAt',
      orderBy: orderBy.value || 'desc',
    })
    if (searchQuery.value?.trim()) params.set('q', searchQuery.value.trim())
    if (statusFilter.value) params.set('status', statusFilter.value)

    const data = await $api(`/apps/ecommerce/wholesale-leads?${params}`)
    leads.value = data?.leads ?? []
    totalLeads.value = data?.total ?? 0
  } catch (e) {
    console.error('Ошибка загрузки заявок:', e)
    leads.value = []
    totalLeads.value = 0
  } finally {
    loading.value = false
  }
}

const refreshAll = async () => {
  await Promise.all([fetchStats(), fetchLeads()])
}

const updateOptions = options => {
  sortBy.value = options.sortBy[0]?.key ?? 'createdAt'
  orderBy.value = options.sortBy[0]?.order ?? 'desc'
  fetchLeads()
}

const runAction = async (id, action) => {
  actionLoadingId.value = id
  try {
    await $api(`/apps/ecommerce/wholesale-leads/${id}/${action}`, { method: 'POST' })
    await refreshAll()
  } catch (e) {
    snackbar.value = {
      show: true,
      color: 'error',
      text: e?.response?._data?.message || e?.data?.message || 'Не удалось выполнить действие',
    }
  } finally {
    actionLoadingId.value = null
  }
}

watch([searchQuery, statusFilter, itemsPerPage], () => {
  page.value = 1
  fetchLeads()
})

watch(page, () => {
  fetchLeads()
})

onMounted(() => {
  refreshAll()
})
</script>

<template>
  <div>
    <VCard class="mb-6">
      <VCardText>
        <VRow>
          <VCol
            v-for="(data, id) in widgetData"
            :key="id"
            cols="12"
            sm="6"
            md="3"
          >
            <div class="d-flex justify-space-between align-center">
              <div>
                <h4 class="text-h4">
                  {{ data.value }}
                </h4>
                <div class="text-body-1">
                  {{ data.title }}
                </div>
              </div>
              <VAvatar
                variant="tonal"
                :color="data.color"
                rounded
                size="42"
              >
                <VIcon
                  :icon="data.icon"
                  size="26"
                />
              </VAvatar>
            </div>
          </VCol>
        </VRow>
      </VCardText>
    </VCard>

    <VCard>
      <VCardText>
        <div class="d-flex justify-space-between flex-wrap gap-y-4">
          <div class="d-flex flex-wrap gap-4 align-center">
            <AppTextField
              v-model="searchQuery"
              style="max-inline-size: 280px; min-inline-size: 220px;"
              placeholder="Поиск: имя, телефон, ID"
            />
            <AppSelect
              v-model="statusFilter"
              :items="statusItems"
              item-title="title"
              item-value="value"
              style="min-inline-size: 180px;"
              placeholder="Статус"
            />
          </div>
          <AppSelect
            :model-value="itemsPerPage"
            :items="[5, 10, 20, 50].map(n => ({ value: n, title: String(n) }))"
            item-value="value"
            item-title="title"
            style="min-inline-size: 90px;"
            @update:model-value="itemsPerPage = Number($event)"
          />
        </div>
      </VCardText>

      <VDivider />

      <VDataTableServer
        v-model:page="page"
        v-model:items-per-page="itemsPerPage"
        :headers="headers"
        :items="leads"
        :items-length="totalLeads"
        :loading="loading"
        item-value="id"
        class="text-no-wrap"
        @update:options="updateOptions"
      >
        <template #item.name="{ item }">
          <div>
            <div class="font-weight-medium">
              {{ item.name }}
            </div>
            <div
              v-if="item.inn"
              class="text-caption text-medium-emphasis"
            >
              ИНН: {{ item.inn }}
            </div>
          </div>
        </template>

        <template #item.phone="{ item }">
          <a
            :href="`tel:${item.phone}`"
            class="text-primary"
          >{{ item.phone }}</a>
        </template>

        <template #item.createdAt="{ item }">
          {{ formatDate(item.createdAt) }}
        </template>

        <template #item.status="{ item }">
          <VChip
            :color="resolveLeadChip(item.status).color"
            size="small"
            label
          >
            {{ item.statusLabel || resolveLeadChip(item.status).text }}
          </VChip>
        </template>

        <template #item.managerName="{ item }">
          <span v-if="item.managerName">{{ item.managerName }}</span>
          <span
            v-else
            class="text-medium-emphasis"
          >—</span>
        </template>

        <template #item.message="{ item }">
          <span class="text-truncate d-inline-block" style="max-width: 220px;">
            {{ item.message || '—' }}
          </span>
        </template>

        <template #item.actions="{ item }">
          <div class="d-flex gap-1">
            <VBtn
              v-if="item.status === 'NEW'"
              size="small"
              color="primary"
              variant="tonal"
              :loading="actionLoadingId === item.id"
              @click="runAction(item.id, 'take')"
            >
              Взять
            </VBtn>
            <VBtn
              v-if="item.status === 'IN_PROGRESS'"
              size="small"
              color="success"
              variant="tonal"
              :loading="actionLoadingId === item.id"
              @click="runAction(item.id, 'complete')"
            >
              Завершить
            </VBtn>
            <VBtn
              v-if="item.status === 'IN_PROGRESS'"
              size="small"
              color="secondary"
              variant="text"
              :loading="actionLoadingId === item.id"
              @click="runAction(item.id, 'release')"
            >
              Снять
            </VBtn>
          </div>
        </template>
      </VDataTableServer>
    </VCard>

    <VSnackbar
      v-model="snackbar.show"
      :color="snackbar.color"
      :timeout="4000"
    >
      {{ snackbar.text }}
    </VSnackbar>
  </div>
</template>
