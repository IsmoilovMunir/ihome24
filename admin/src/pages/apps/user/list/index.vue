<script setup>
import AddNewUserDrawer from '@/views/apps/user/list/AddNewUserDrawer.vue'

// Enforce ADMIN-only access on this route via CASL guard.
definePage({
  meta: {
    action: 'manage',
    subject: 'AdminUsers',
    navActiveLink: 'apps-user-list',
  },
})

const searchQuery = ref('')
const selectedRole = ref()
const selectedPlan = ref()
const selectedStatus = ref()

// Data table options
const itemsPerPage = ref(10)
const page = ref(1)
const sortBy = ref()
const orderBy = ref()
const selectedRows = ref([])
const currentUser = useCookie('userData')
const isAdminUser = computed(() => (currentUser.value?.role || '').toLowerCase() === 'admin')

// Roles list (for inline role editing)
const { data: rolesData } = await useApi('/apps/roles')
const roleItems = computed(() => {
  const list = rolesData.value || []
  return list
    .filter(r => r?.name && r.name !== 'users')
    .map(r => ({ title: r.displayName || r.name, value: r.name }))
})

const updateOptions = options => {
  sortBy.value = options.sortBy[0]?.key
  orderBy.value = options.sortBy[0]?.order
}

// Headers
const headers = [
  {
    title: 'Пользователь',
    key: 'user',
  },
  {
    title: 'Роль',
    key: 'role',
  },
  {
    title: 'План',
    key: 'plan',
  },
  {
    title: 'Оплата',
    key: 'billing',
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

const {
  data: usersData,
  execute: fetchUsers,
} = await useApi(createUrl('/apps/users', {
  query: {
    q: searchQuery,
    status: selectedStatus,
    plan: selectedPlan,
    role: selectedRole,
    itemsPerPage,
    page,
    sortBy,
    orderBy,
  },
}))

const users = computed(() => usersData.value?.users ?? [])
const totalUsers = computed(() => Number(usersData.value?.totalUsers ?? 0))

// 👉 search filters
const roles = [
  {
    title: 'Администратор',
    value: 'admin',
  },
  {
    title: 'Менеджер',
    value: 'manager',
  },
]

const plans = [
  {
    title: 'Базовый',
    value: 'basic',
  },
  {
    title: 'Компания',
    value: 'company',
  },
  {
    title: 'Предприятие',
    value: 'enterprise',
  },
  {
    title: 'Команда',
    value: 'team',
  },
]

const status = [
  {
    title: 'Ожидает',
    value: 'pending',
  },
  {
    title: 'Активен',
    value: 'active',
  },
  {
    title: 'Неактивен',
    value: 'inactive',
  },
]

const resolveUserRoleVariant = role => {
  const roleLowerCase = role.toLowerCase()
  if (roleLowerCase === 'admin')
    return {
      color: 'primary',
      icon: 'tabler-crown',
    }
  if (roleLowerCase === 'manager')
    return {
      color: 'info',
      icon: 'tabler-briefcase',
    }
  
  return {
    color: 'primary',
    icon: 'tabler-user',
  }
}

const resolveUserStatusVariant = stat => {
  const statLowerCase = stat.toLowerCase()
  if (statLowerCase === 'pending')
    return 'warning'
  if (statLowerCase === 'active')
    return 'success'
  if (statLowerCase === 'inactive')
    return 'secondary'
  
  return 'primary'
}

const isAddNewUserDrawerVisible = ref(false)

// API origin для файлов и аватаров (без суффикса /api)
const resolveApiOrigin = () => {
  const configured = (import.meta.env.VITE_API_BASE_URL || '').trim()
  const fallback = 'http://localhost:8080'
  const rawBase = configured || fallback

  if (typeof window === 'undefined') {
    return rawBase.replace(/\/api\/?$/, '')
  }

  const host = window.location.hostname
  const isCurrentHostLocal = ['localhost', '127.0.0.1', '::1'].includes(host)
  const isConfiguredLocalhost = /^https?:\/\/(localhost|127\.0\.0\.1|\[::1\])(?::\d+)?(\/.*)?$/i.test(rawBase)

  if (!isCurrentHostLocal && isConfiguredLocalhost) {
    return window.location.origin
  }

  return rawBase.replace(/\/api\/?$/, '')
}

const API_BASE_URL = resolveApiOrigin()

const getAvatarUrl = avatar => {
  if (!avatar)
    return null

  // Локальный аватар (fallback без MinIO)
  if (avatar.startsWith('local:')) {
    const userId = avatar.substring('local:'.length)
    if (!userId)
      return null
    let url = `${API_BASE_URL}/api/avatars/${userId}`
    const sep = url.includes('?') ? '&' : '?'
    return `${url}${sep}t=${Date.now()}`
  }

  // Уже полный URL
  if (avatar.startsWith('http://') || avatar.startsWith('https://')) {
    let url = avatar
    const sep = url.includes('?') ? '&' : '?'
    return `${url}${sep}t=${Date.now()}`
  }

  // Пути от MinIO через /api/files
  let filePath = avatar
  if (filePath.startsWith('/api/files/'))
    filePath = filePath.substring('/api/files/'.length)
  if (filePath.startsWith('api/files/'))
    filePath = filePath.substring('api/files/'.length)
  if (filePath.startsWith('/'))
    filePath = filePath.substring(1)

  let url = `${API_BASE_URL}/api/files/${filePath}`
  const sep = url.includes('?') ? '&' : '?'
  return `${url}${sep}t=${Date.now()}`
}

const isTempPasswordDialogVisible = ref(false)
const createdTempPassword = ref('')
const createdUsername = ref('')
const createUserMessage = ref('')
const createUserEmailSent = ref(false)
const createUserError = ref('')

const addNewUser = async payload => {
  try {
    createUserError.value = ''
    const res = await $api('/apps/users/admin-create', {
      method: 'POST',
      body: payload,
    })

    createdTempPassword.value = res?.temporaryPassword || ''
    createdUsername.value = res?.user?.username || payload?.username || ''
    createUserMessage.value = res?.message || 'Пользователь создан.'
    createUserEmailSent.value = Boolean(res?.emailSent)
    isTempPasswordDialogVisible.value = true
    isAddNewUserDrawerVisible.value = false

    // Refetch User
    fetchUsers()
  } catch (error) {
    const data = error?.data || {}
    const errors = data?.errors || {}
    if (typeof errors === 'object' && Object.keys(errors).length) {
      createUserError.value = Object.values(errors)[0] || 'Ошибка создания пользователя'
    } else {
      createUserError.value = data?.message || error?.message || 'Ошибка создания пользователя'
    }
  }
}

const deleteUser = async id => {
  await $api(`/apps/users/${ id }`, { method: 'DELETE' })

  // Delete from selectedRows
  const index = selectedRows.value.findIndex(row => row === id)
  if (index !== -1)
    selectedRows.value.splice(index, 1)

  // Refetch User
  fetchUsers()
}

const changeUserRole = async (userId, newRoleName) => {
  try {
    if (!userId || !newRoleName)
      return

    await $api(`/apps/users/${userId}/role`, {
      method: 'PATCH',
      body: { roleName: newRoleName },
    })

    // Refetch list to reflect updated roles.
    fetchUsers()
  } catch (error) {
    console.error('Error changing user role:', error)
    const message = error?.data?.message || error?.data?.errors
      ? Object.values(error.data.errors).flat().join('\n')
      : (error?.data?.message || error?.message || 'Не удалось изменить роль')
    alert(message)
  }
}

const widgetData = ref([
  {
    title: 'Сессии',
    value: '21,459',
    change: 29,
    desc: 'Всего пользователей',
    icon: 'tabler-users',
    iconColor: 'primary',
  },
  {
    title: 'Платные пользователи',
    value: '4,567',
    change: 18,
    desc: 'Аналитика за прошлую неделю',
    icon: 'tabler-user-plus',
    iconColor: 'error',
  },
  {
    title: 'Активные пользователи',
    value: '19,860',
    change: -14,
    desc: 'Аналитика за прошлую неделю',
    icon: 'tabler-user-check',
    iconColor: 'success',
  },
  {
    title: 'Ожидающие пользователи',
    value: '237',
    change: 42,
    desc: 'Аналитика за прошлую неделю',
    icon: 'tabler-user-search',
    iconColor: 'warning',
  },
])
</script>

<template>
  <section>
    <!-- 👉 Widgets -->
    <div class="d-flex mb-6">
      <VRow>
        <template
          v-for="(data, id) in widgetData"
          :key="id"
        >
          <VCol
            cols="12"
            md="3"
            sm="6"
          >
            <VCard>
              <VCardText>
                <div class="d-flex justify-space-between">
                  <div class="d-flex flex-column gap-y-1">
                    <div class="text-body-1 text-high-emphasis">
                      {{ data.title }}
                    </div>
                    <div class="d-flex gap-x-2 align-center">
                      <h4 class="text-h4">
                        {{ data.value }}
                      </h4>
                      <div
                        class="text-base"
                        :class="data.change > 0 ? 'text-success' : 'text-error'"
                      >
                        ({{ prefixWithPlus(data.change) }}%)
                      </div>
                    </div>
                    <div class="text-sm">
                      {{ data.desc }}
                    </div>
                  </div>
                  <VAvatar
                    :color="data.iconColor"
                    variant="tonal"
                    rounded
                    size="42"
                  >
                    <VIcon
                      :icon="data.icon"
                      size="26"
                    />
                  </VAvatar>
                </div>
              </VCardText>
            </VCard>
          </VCol>
        </template>
      </VRow>
    </div>

    <VCard class="mb-6">
      <VCardItem class="pb-4">
        <VCardTitle>Фильтры</VCardTitle>
      </VCardItem>

      <VCardText>
        <VRow>
          <!-- 👉 Select Role -->
          <VCol
            cols="12"
            sm="4"
          >
            <AppSelect
              v-model="selectedRole"
              placeholder="Выберите роль"
              :items="roles"
              clearable
              clear-icon="tabler-x"
            />
          </VCol>
          <!-- 👉 Select Plan -->
          <VCol
            cols="12"
            sm="4"
          >
            <AppSelect
              v-model="selectedPlan"
              placeholder="Выберите план"
              :items="plans"
              clearable
              clear-icon="tabler-x"
            />
          </VCol>
          <!-- 👉 Select Status -->
          <VCol
            cols="12"
            sm="4"
          >
            <AppSelect
              v-model="selectedStatus"
              placeholder="Выберите статус"
              :items="status"
              clearable
              clear-icon="tabler-x"
            />
          </VCol>
        </VRow>
      </VCardText>

      <VDivider />

      <VCardText class="d-flex flex-wrap gap-4">
        <div class="me-3 d-flex gap-3">
          <AppSelect
            :model-value="itemsPerPage"
            :items="[
              { value: 10, title: '10' },
              { value: 25, title: '25' },
              { value: 50, title: '50' },
              { value: 100, title: '100' },
              { value: -1, title: 'Все' },
            ]"
            style="inline-size: 6.25rem;"
            @update:model-value="itemsPerPage = parseInt($event, 10)"
          />
        </div>
        <VSpacer />

        <div class="app-user-search-filter d-flex align-center flex-wrap gap-4">
          <!-- 👉 Search  -->
          <div style="inline-size: 15.625rem;">
            <AppTextField
              v-model="searchQuery"
              placeholder="Поиск пользователя"
            />
          </div>

          <!-- 👉 Export button -->
          <VBtn
            variant="tonal"
            color="secondary"
            prepend-icon="tabler-upload"
          >
            Экспорт
          </VBtn>

          <!-- 👉 Add user button -->
          <VBtn
            v-if="isAdminUser"
            prepend-icon="tabler-plus"
            @click="isAddNewUserDrawerVisible = true"
          >
            Добавить пользователя
          </VBtn>
        </div>
      </VCardText>

      <VDivider />

      <!-- SECTION datatable -->
      <VDataTableServer
        v-model:items-per-page="itemsPerPage"
        v-model:model-value="selectedRows"
        v-model:page="page"
        :items="users"
        item-value="id"
        :items-length="totalUsers"
        :headers="headers"
        class="text-no-wrap"
        show-select
        @update:options="updateOptions"
      >
        <!-- User -->
        <template #item.user="{ item }">
          <div class="d-flex align-center gap-x-4">
            <VAvatar
              size="34"
              :variant="!item.avatar ? 'tonal' : undefined"
              :color="!item.avatar ? resolveUserRoleVariant(item.role).color : undefined"
            >
              <VImg
                v-if="item.avatar"
                :src="getAvatarUrl(item.avatar)"
              />
              <span v-else>{{ avatarText(item.fullName) }}</span>
            </VAvatar>
            <div class="d-flex flex-column">
              <h6 class="text-base">
                <RouterLink
                  :to="{ name: 'apps-user-view-id', params: { id: item.id } }"
                  class="font-weight-medium text-link"
                >
                  {{ item.fullName }}
                </RouterLink>
              </h6>
              <div class="text-sm">
                {{ item.email }}
              </div>
            </div>
          </div>
        </template>

        <!-- 👉 Role -->
        <template #item.role="{ item }">
          <div class="d-flex align-center gap-x-2">
            <VIcon
              :size="22"
              :icon="resolveUserRoleVariant(item.role).icon"
              :color="resolveUserRoleVariant(item.role).color"
            />

            <AppSelect
              :model-value="item.role"
              :items="roleItems"
              item-title="title"
              item-value="value"
              density="compact"
              style="inline-size: 12rem;"
              hide-details
              variant="underlined"
              @update:model-value="val => changeUserRole(item.id, typeof val === 'string' ? val : val?.value)"
            />
          </div>
        </template>

        <!-- Plan -->
        <template #item.plan="{ item }">
          <div class="text-body-1 text-high-emphasis text-capitalize">
            {{ item.currentPlan }}
          </div>
        </template>

        <!-- Status -->
        <template #item.status="{ item }">
          <VChip
            :color="resolveUserStatusVariant(item.status)"
            size="small"
            label
            class="text-capitalize"
          >
            {{ item.status }}
          </VChip>
        </template>

        <!-- Actions -->
        <template #item.actions="{ item }">
          <IconBtn @click="deleteUser(item.id)">
            <VIcon icon="tabler-trash" />
          </IconBtn>

          <IconBtn :to="{ name: 'apps-user-view-id', params: { id: item.id } }">
            <VIcon icon="tabler-eye" />
          </IconBtn>

          <VBtn
            icon
            variant="text"
            color="medium-emphasis"
          >
            <VIcon icon="tabler-dots-vertical" />
            <VMenu activator="parent">
              <VList>
                <VListItem :to="{ name: 'apps-user-view-id', params: { id: item.id } }">
                  <template #prepend>
                    <VIcon icon="tabler-eye" />
                  </template>

                  <VListItemTitle>Просмотр</VListItemTitle>
                </VListItem>

                <VListItem
                  :to="{ name: 'apps-user-view-id', params: { id: item.id }, query: { tab: 'account' } }"
                >
                  <template #prepend>
                    <VIcon icon="tabler-pencil" />
                  </template>
                  <VListItemTitle>Редактировать</VListItemTitle>
                </VListItem>

                <VListItem @click="deleteUser(item.id)">
                  <template #prepend>
                    <VIcon icon="tabler-trash" />
                  </template>
                  <VListItemTitle>Удалить</VListItemTitle>
                </VListItem>
              </VList>
            </VMenu>
          </VBtn>
        </template>

        <!-- pagination -->
        <template #bottom>
          <TablePagination
            v-model:page="page"
            :items-per-page="itemsPerPage"
            :total-items="totalUsers"
          />
        </template>
      </VDataTableServer>
      <!-- SECTION -->
    </VCard>
    <!-- 👉 Add New User -->
    <AddNewUserDrawer
      v-model:is-drawer-open="isAddNewUserDrawerVisible"
      :server-error="createUserError"
      @submit="addNewUser"
    />

    <VDialog
      :model-value="isTempPasswordDialogVisible"
      :width="$vuetify.display.smAndDown ? 'auto' : 520"
      @update:model-value="isTempPasswordDialogVisible = false"
    >
      <DialogCloseBtn @click="isTempPasswordDialogVisible = false" />
      <VCard class="pa-sm-8 pa-4">
        <VCardText>
          <h4 class="text-h4 text-center mb-2">
            Пользователь создан
          </h4>
          <p class="text-body-1 text-center mb-6">
            {{ createUserMessage }}
          </p>

          <VAlert
            :type="createUserEmailSent ? 'success' : 'warning'"
            variant="tonal"
            class="mb-4"
          >
            {{ createUserEmailSent ? 'Письмо с доступом отправлено на email пользователя.' : 'Письмо не отправлено. Передайте временный пароль вручную.' }}
          </VAlert>

          <AppTextField
            v-model="createdUsername"
            readonly
            class="mb-4"
            label="Логин"
          />

          <AppTextField
            v-model="createdTempPassword"
            readonly
            label="Временный пароль"
          />
        </VCardText>
      </VCard>
    </VDialog>
  </section>
</template>
