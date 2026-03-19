<script setup>
import { PerfectScrollbar } from 'vue3-perfect-scrollbar'

const props = defineProps({
  isDrawerOpen: {
    type: Boolean,
    required: true,
  },
  serverError: {
    type: String,
    default: '',
  },
})

const emit = defineEmits([
  'update:isDrawerOpen',
  'submit',
])

const isFormValid = ref(false)
const refForm = ref()
const fullName = ref('')
const userName = ref('')
const email = ref('')
const roleName = ref()
const temporaryPassword = ref('')
const { data: rolesData } = await useApi('/apps/roles')
const roleItems = computed(() => {
  const roles = rolesData.value || []
  return roles
    .filter(r => r?.name && r.name !== 'users')
    .map(r => ({ title: r.displayName || r.name, value: r.name }))
})

const generateTempPassword = () => {
  const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789'
  let out = ''
  for (let i = 0; i < 10; i++)
    out += chars[Math.floor(Math.random() * chars.length)]
  temporaryPassword.value = out
}

// 👉 drawer close
const closeNavigationDrawer = () => {
  emit('update:isDrawerOpen', false)
  nextTick(() => {
    refForm.value?.reset()
    refForm.value?.resetValidation()
  })
}

const onSubmit = () => {
  refForm.value?.validate().then(({ valid }) => {
    if (valid) {
      emit('submit', {
        fullName: fullName.value,
        username: userName.value || null,
        email: email.value,
        roleName: roleName.value,
        temporaryPassword: temporaryPassword.value || null,
      })
    }
  })
}

const handleDrawerModelValueUpdate = val => {
  emit('update:isDrawerOpen', val)
}
</script>

<template>
  <VNavigationDrawer
    data-allow-mismatch
    temporary
    :width="400"
    location="end"
    class="scrollable-content"
    :model-value="props.isDrawerOpen"
    @update:model-value="handleDrawerModelValueUpdate"
  >
    <!-- 👉 Title -->
    <AppDrawerHeaderSection
      title="Добавить нового пользователя"
      @cancel="closeNavigationDrawer"
    />

    <VDivider />

    <PerfectScrollbar :options="{ wheelPropagation: false }">
      <VCard flat>
        <VCardText>
          <VAlert
            v-if="props.serverError"
            type="error"
            variant="tonal"
            class="mb-4"
          >
            {{ props.serverError }}
          </VAlert>

          <!-- 👉 Form -->
          <VForm
            ref="refForm"
            v-model="isFormValid"
            @submit.prevent="onSubmit"
          >
            <VRow>
              <!-- 👉 Full name -->
              <VCol cols="12">
                <AppTextField
                  v-model="fullName"
                  :rules="[requiredValidator]"
                  label="Полное имя"
                  placeholder="Иван Иванов"
                />
              </VCol>

              <!-- 👉 Username -->
              <VCol cols="12">
                <AppTextField
                  v-model="userName"
                  label="Имя пользователя (опционально)"
                  placeholder="Если пусто — сгенерируется из email"
                />
              </VCol>

              <!-- 👉 Email -->
              <VCol cols="12">
                <AppTextField
                  v-model="email"
                  :rules="[requiredValidator, emailValidator]"
                  label="Email"
                  placeholder="ivanov@email.com"
                />
              </VCol>

              <!-- 👉 Role -->
              <VCol cols="12">
                <AppSelect
                  v-model="roleName"
                  label="Выберите роль"
                  placeholder="Выберите роль"
                  :rules="[requiredValidator]"
                  :items="roleItems"
                />
              </VCol>

              <!-- 👉 Temporary password -->
              <VCol cols="12">
                <AppTextField
                  v-model="temporaryPassword"
                  label="Временный пароль (опционально)"
                  placeholder="Если пусто — сгенерируется автоматически"
                >
                  <template #append-inner>
                    <IconBtn @click="generateTempPassword">
                      <VIcon icon="tabler-refresh" />
                    </IconBtn>
                  </template>
                </AppTextField>
              </VCol>

              <!-- 👉 Submit and Cancel -->
              <VCol cols="12">
                <VBtn
                  type="submit"
                  class="me-3"
                >
                  Создать пользователя
                </VBtn>
                <VBtn
                  type="reset"
                  variant="tonal"
                  color="error"
                  @click="closeNavigationDrawer"
                >
                  Отменить
                </VBtn>
              </VCol>
            </VRow>
          </VForm>
        </VCardText>
      </VCard>
    </PerfectScrollbar>
  </VNavigationDrawer>
</template>
