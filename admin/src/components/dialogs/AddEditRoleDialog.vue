<script setup>
import { VForm } from 'vuetify/components/VForm'

const props = defineProps({
  rolePermissions: {
    type: Object,
    required: false,
    default: () => ({
      name: '',
      permissions: [],
    }),
  },
  isDialogVisible: {
    type: Boolean,
    required: true,
  },
})

const emit = defineEmits([
  'update:isDialogVisible',
  'update:rolePermissions',
])


// 👉 Permission List
const permissions = ref([
  {
    name: 'Управление пользователями',
    read: false,
    write: false,
    create: false,
  },
  {
    name: 'Управление контентом',
    read: false,
    write: false,
    create: false,
  },
  {
    name: 'Управление спорами',
    read: false,
    write: false,
    create: false,
  },
  {
    name: 'Управление базой данных',
    read: false,
    write: false,
    create: false,
  },
  {
    name: 'Финансовое управление',
    read: false,
    write: false,
    create: false,
  },
  {
    name: 'Отчетность',
    read: false,
    write: false,
    create: false,
  },
  {
    name: 'Управление API',
    read: false,
    write: false,
    create: false,
  },
  {
    name: 'Управление репозиторием',
    read: false,
    write: false,
    create: false,
  },
  {
    name: 'Заработная плата',
    read: false,
    write: false,
    create: false,
  },
])

const isSelectAll = ref(false)
const role = ref('')
const refPermissionForm = ref()

const checkedCount = computed(() => {
  let counter = 0
  permissions.value.forEach(permission => {
    Object.entries(permission).forEach(([key, value]) => {
      if (key !== 'name' && value)
        counter++
    })
  })
  
  return counter
})

const isIndeterminate = computed(() => checkedCount.value > 0 && checkedCount.value < permissions.value.length * 3)

// select all
watch(isSelectAll, val => {
  permissions.value = permissions.value.map(permission => ({
    ...permission,
    read: val,
    write: val,
    create: val,
  }))
})

// if Indeterminate is false, then set isSelectAll to false
watch(isIndeterminate, () => {
  if (!isIndeterminate.value)
    isSelectAll.value = false
})

// if all permissions are checked, then set isSelectAll to true
watch(permissions, () => {
  if (checkedCount.value === permissions.value.length * 3)
    isSelectAll.value = true
}, { deep: true })

// if rolePermissions is not empty, then set permissions
watch(() => props, () => {
  if (props.rolePermissions && props.rolePermissions.permissions.length) {
    role.value = props.rolePermissions.name
    permissions.value = permissions.value.map(permission => {
      const rolePermission = props.rolePermissions?.permissions.find(item => item.name === permission.name)
      if (rolePermission) {
        return {
          ...permission,
          ...rolePermission,
        }
      }
      
      return permission
    })
  }
})

const onSubmit = () => {
  const rolePermissions = {
    name: role.value,
    permissions: permissions.value,
  }

  emit('update:rolePermissions', rolePermissions)
  emit('update:isDialogVisible', false)
  isSelectAll.value = false
  refPermissionForm.value?.reset()
}

const onReset = () => {
  emit('update:isDialogVisible', false)
  isSelectAll.value = false
  refPermissionForm.value?.reset()
}
</script>

<template>
  <VDialog
    :width="$vuetify.display.smAndDown ? 'auto' : 900"
    :model-value="props.isDialogVisible"
    @update:model-value="onReset"
  >
    <!-- 👉 Dialog close btn -->
    <DialogCloseBtn @click="onReset" />

    <VCard class="pa-sm-10 pa-2">
      <VCardText>
        <!-- 👉 Title -->
        <h4 class="text-h4 text-center mb-2">
          {{ props.rolePermissions.name ? 'Редактировать' : 'Добавить новую' }} роль
        </h4>
        <p class="text-body-1 text-center mb-6">
          Установить разрешения роли
        </p>

        <!-- 👉 Form -->
        <VForm ref="refPermissionForm">
          <!-- 👉 Role name -->
          <AppTextField
            v-model="role"
            label="Название роли"
            placeholder="Введите название роли"
          />

          <h5 class="text-h5 my-6">
            Разрешения роли
          </h5>

          <!-- 👉 Role Permissions -->

          <VTable class="permission-table text-no-wrap mb-6">
            <!-- 👉 Admin  -->
            <tr>
              <td>
                <h6 class="text-h6">
                  Доступ администратора
                </h6>
              </td>
              <td colspan="3">
                <div class="d-flex justify-end">
                  <VCheckbox
                    v-model="isSelectAll"
                    v-model:indeterminate="isIndeterminate"
                    label="Выбрать все"
                  />
                </div>
              </td>
            </tr>

            <!-- 👉 Other permission loop -->
            <template
              v-for="permission in permissions"
              :key="permission.name"
            >
              <tr>
                <td>
                  <h6 class="text-h6">
                    {{ permission.name }}
                  </h6>
                </td>
                <td>
                  <div class="d-flex justify-end">
                    <VCheckbox
                      v-model="permission.read"
                      label="Чтение"
                    />
                  </div>
                </td>
                <td>
                  <div class="d-flex justify-end">
                    <VCheckbox
                      v-model="permission.write"
                      label="Запись"
                    />
                  </div>
                </td>
                <td>
                  <div class="d-flex justify-end">
                    <VCheckbox
                      v-model="permission.create"
                      label="Создание"
                    />
                  </div>
                </td>
              </tr>
            </template>
          </VTable>

          <!-- 👉 Actions button -->
          <div class="d-flex align-center justify-center gap-4">
            <VBtn @click="onSubmit">
              Отправить
            </VBtn>

            <VBtn
              color="secondary"
              variant="tonal"
              @click="onReset"
            >
              Отмена
            </VBtn>
          </div>
        </VForm>
      </VCardText>
    </VCard>
  </VDialog>
</template>

<style lang="scss">
.permission-table {
  td {
    border-block-end: 1px solid rgba(var(--v-border-color), var(--v-border-opacity));
    padding-block: 0.5rem;

    .v-checkbox {
      min-inline-size: 4.75rem;
    }

    &:not(:first-child) {
      padding-inline: 0.5rem;
    }

    .v-label {
      white-space: nowrap;
    }
  }
}
</style>
