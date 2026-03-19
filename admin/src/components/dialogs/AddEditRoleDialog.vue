<script setup>
import { VForm } from 'vuetify/components/VForm'

const props = defineProps({
  role: {
    type: Object,
    required: false,
    default: null,
  },
  isDialogVisible: {
    type: Boolean,
    required: true,
  },
})

const emit = defineEmits([
  'update:isDialogVisible',
  'saved',
])

const refForm = ref()
const isFormValid = ref(false)

const name = ref('')
const displayName = ref('')
const selectedPermissions = ref([])

const { data: permissionsData } = await useApi(createUrl('/apps/permissions', {
  query: {
    q: '',
    page: 1,
    itemsPerPage: 1000,
  },
}))

const permissionItems = computed(() => (permissionsData.value?.permissions || []).map(p => ({
  title: p.name,
  value: p.name,
})))

watch(
  () => props.role,
  r => {
    name.value = r?.name || ''
    displayName.value = r?.displayName || ''
    selectedPermissions.value = Array.isArray(r?.permissions) ? [...r.permissions] : []
  },
  { immediate: true },
)

const close = () => {
  emit('update:isDialogVisible', false)
}

const onSubmit = () => {
  refForm.value?.validate().then(async ({ valid }) => {
    if (!valid) return

    const payload = {
      name: name.value,
      displayName: displayName.value,
      permissions: selectedPermissions.value,
    }

    if (props.role?.id) {
      await $api(`/apps/roles/${props.role.id}`, { method: 'PUT', body: payload })
    } else {
      await $api('/apps/roles', { method: 'POST', body: payload })
    }

    emit('saved')
    close()
  })
}
</script>

<template>
  <VDialog
    :width="$vuetify.display.smAndDown ? 'auto' : 700"
    :model-value="props.isDialogVisible"
    @update:model-value="close"
  >
    <DialogCloseBtn @click="close" />

    <VCard class="pa-sm-10 pa-2">
      <VCardText>
        <h4 class="text-h4 text-center mb-2">
          {{ props.role?.id ? 'Редактировать роль' : 'Добавить роль' }}
        </h4>
        <p class="text-body-1 text-center mb-6">
          Настройте роль и назначьте права
        </p>

        <VForm
          ref="refForm"
          v-model="isFormValid"
          @submit.prevent="onSubmit"
        >
          <VRow>
            <VCol cols="12">
              <AppTextField
                v-model="name"
                :rules="[requiredValidator]"
                label="Системное имя (уникально)"
                placeholder="например: admin, manager"
              />
            </VCol>

            <VCol cols="12">
              <AppTextField
                v-model="displayName"
                label="Отображаемое имя"
                placeholder="например: Администратор"
              />
            </VCol>

            <VCol cols="12">
              <AppSelect
                v-model="selectedPermissions"
                multiple
                chips
                closable-chips
                label="Права"
                placeholder="Выберите права"
                :items="permissionItems"
              />
            </VCol>

            <VCol
              cols="12"
              class="d-flex align-center justify-center gap-4"
            >
              <VBtn type="submit">
                Сохранить
              </VBtn>
              <VBtn
                color="secondary"
                variant="tonal"
                @click="close"
              >
                Отмена
              </VBtn>
            </VCol>
          </VRow>
        </VForm>
      </VCardText>
    </VCard>
  </VDialog>
</template>
