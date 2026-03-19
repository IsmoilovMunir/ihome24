<script setup>
import AddEditRoleDialog from '@/components/dialogs/AddEditRoleDialog.vue'

const { data: rolesData, execute: fetchRoles } = await useApi('/apps/roles')
const roles = computed(() => rolesData.value || [])

const isRolePermissionsDialogVisible = ref(false)
const activeRole = ref(null)

const isUpsertRoleDialogVisible = ref(false)
const roleToEdit = ref(null)

const openPermissions = role => {
  activeRole.value = role
  isRolePermissionsDialogVisible.value = true
}

const openCreate = () => {
  roleToEdit.value = null
  isUpsertRoleDialogVisible.value = true
}

const openEdit = role => {
  roleToEdit.value = role
  isUpsertRoleDialogVisible.value = true
}
</script>

<template>
  <VRow>
    <VCol
      cols="12"
      class="d-flex justify-end"
    >
      <VBtn @click="openCreate">
        Добавить роль
      </VBtn>
    </VCol>

    <!-- 👉 Roles -->
    <VCol
      v-for="item in roles"
      :key="item.id ?? item.name"
      cols="12"
      sm="6"
      lg="4"
    >
      <VCard>
        <VCardText class="d-flex align-center pb-4">
          <div class="text-body-1">
            Всего {{ item.userCount ?? 0 }} пользователей
          </div>

          <VSpacer />
        </VCardText>

        <VCardText>
          <div class="d-flex justify-space-between align-center">
            <div>
              <h5 class="text-h5">
                {{ item.displayName || item.name }}
              </h5>
              <div class="d-flex align-center">
                <a
                  href="javascript:void(0)"
                  @click="openPermissions(item)"
                >
                  Просмотреть права
                </a>
                <span class="mx-2 text-disabled">|</span>
                <a
                  href="javascript:void(0)"
                  @click="openEdit(item)"
                >
                  Редактировать
                </a>
              </div>
            </div>
            <IconBtn>
              <VIcon
                icon="tabler-copy"
                class="text-high-emphasis"
              />
            </IconBtn>
          </div>
        </VCardText>
      </VCard>
    </VCol>
  </VRow>

  <VDialog
    :model-value="isRolePermissionsDialogVisible"
    :width="$vuetify.display.smAndDown ? 'auto' : 700"
    @update:model-value="isRolePermissionsDialogVisible = false"
  >
    <DialogCloseBtn @click="isRolePermissionsDialogVisible = false" />
    <VCard class="pa-sm-8 pa-4">
      <VCardText>
        <h4 class="text-h4 text-center mb-2">
          Права роли
        </h4>
        <p class="text-body-1 text-center mb-6">
          {{ activeRole?.displayName || activeRole?.name || '—' }}
        </p>

        <VList class="card-list">
          <VListItem
            v-for="p in (activeRole?.permissions || [])"
            :key="p"
          >
            <VListItemTitle class="text-body-1">
              {{ p }}
            </VListItemTitle>
          </VListItem>
          <VListItem v-if="!(activeRole?.permissions || []).length">
            <VListItemTitle class="text-body-1 text-disabled">
              Нет назначенных прав
            </VListItemTitle>
          </VListItem>
        </VList>
      </VCardText>
    </VCard>
  </VDialog>

  <AddEditRoleDialog
    v-model:is-dialog-visible="isUpsertRoleDialogVisible"
    :role="roleToEdit"
    @saved="fetchRoles()"
  />
</template>
