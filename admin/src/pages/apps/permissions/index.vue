<script setup>
const headers = [
  {
    title: 'Название',
    key: 'name',
  },
  {
    title: 'Назначено',
    key: 'assignedTo',
    sortable: false,
  },
  {
    title: 'Дата создания',
    key: 'createdDate',
    sortable: false,
  },
  {
    title: 'Действия',
    key: 'actions',
    sortable: false,
  },
]

const search = ref('')

// Data table options
const itemsPerPage = ref(10)
const page = ref(1)
const sortBy = ref()
const orderBy = ref()

const updateOptions = options => {
  sortBy.value = options.sortBy[0]?.key
  orderBy.value = options.sortBy[0]?.order
}

const isPermissionDialogVisible = ref(false)
const isAddPermissionDialogVisible = ref(false)
const permissionName = ref('')

const colors = {
  'support': {
    color: 'info',
    text: 'Поддержка',
  },
  'users': {
    color: 'success',
    text: 'Пользователи',
  },
  'manager': {
    color: 'warning',
    text: 'Менеджер',
  },
  'administrator': {
    color: 'primary',
    text: 'Администратор',
  },
  'restricted-user': {
    color: 'error',
    text: 'Ограниченный пользователь',
  },
}

const { data: permissionsData } = await useApi(createUrl('/apps/permissions', {
  query: {
    q: search,
    itemsPerPage,
    page,
    sortBy,
    orderBy,
  },
}))

const permissions = computed(() => permissionsData.value.permissions)
const totalPermissions = computed(() => permissionsData.value.totalPermissions)

const editPermission = name => {
  isPermissionDialogVisible.value = true
  permissionName.value = name
}
</script>

<template>
  <VRow>
    <VCol cols="12">
      <h4 class="text-h4 mb-1">
        Список разрешений
      </h4>
      <p class="text-body-1 mb-6">
        Разрешения определяют, какие действия может выполнять пользователь в системе. Управляйте разрешениями для различных ролей.
      </p>
    </VCol>
    <VCol cols="12">
      <VCard>
        <VCardText class="d-flex align-center justify-space-between flex-wrap gap-4">
          <div class="d-flex gap-2 align-center">
            <p class="text-body-1 mb-0">
              Показать
            </p>
            <AppSelect
              :model-value="itemsPerPage"
              :items="[
                { value: 5, title: '5' },
                { value: 25, title: '25' },
                { value: 50, title: '50' },
                { value: 100, title: '100' },
                { value: -1, title: 'Все' },
              ]"
              style="inline-size: 5.5rem;"
              @update:model-value="itemsPerPage = parseInt($event, 10)"
            />
          </div>

          <div class="d-flex align-center gap-4 flex-wrap">
            <AppTextField
              v-model="search"
              placeholder="Поиск разрешения"
              style="inline-size: 15.625rem;"
            />
            <VBtn
              density="default"
              prepend-icon="tabler-plus"
              @click="isAddPermissionDialogVisible = true"
            >
              Добавить разрешение
            </VBtn>
          </div>
        </VCardText>

        <VDivider />

        <VDataTableServer
          v-model:items-per-page="itemsPerPage"
          v-model:page="page"
          :items-length="totalPermissions"
          :items-per-page-options="[
            { value: 5, title: '5' },
            { value: 10, title: '10' },
            { value: -1, title: '$vuetify.dataFooter.itemsPerPageAll' },
          ]"
          :headers="headers"
          :items="permissions"
          item-value="name"
          class="text-no-wrap"
          @update:options="updateOptions"
        >
          <!-- Name -->
          <template #item.name="{ item }">
            <div class="text-high-emphasis text-body-1">
              {{ item.name }}
            </div>
          </template>

          <!-- Assigned To -->
          <template #item.assignedTo="{ item }">
            <div class="d-flex gap-4">
              <VChip
                v-for="text in item.assignedTo"
                :key="text"
                label
                size="small"
                :color="colors[text].color"
                class="font-weight-medium"
              >
                {{ colors[text].text }}
              </VChip>
            </div>
          </template>

          <template #bottom>
            <TablePagination
              v-model:page="page"
              :items-per-page="itemsPerPage"
              :total-items="totalPermissions"
            />
          </template>

          <!-- Actions -->
          <template #item.actions="{ item }">
            <VBtn
              icon
              size="small"
              color="medium-emphasis"
              variant="text"
              @click="editPermission(item.name)"
            >
              <VIcon
                size="22"
                icon="tabler-edit"
              />
            </VBtn>
            <IconBtn>
              <VIcon
                icon="tabler-dots-vertical"
                size="22"
              />
            </IconBtn>
          </template>
        </VDataTableServer>
      </VCard>

      <AddEditPermissionDialog
        v-model:is-dialog-visible="isPermissionDialogVisible"
        v-model:permission-name="permissionName"
      />
      <AddEditPermissionDialog v-model:is-dialog-visible="isAddPermissionDialogVisible" />
    </VCol>
  </VRow>
</template>
