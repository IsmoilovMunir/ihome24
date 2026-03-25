<script setup>
const normalizeUser = raw => {
  const u = raw ?? {}

  // Keep existing shape, but ensure fields used in v-model exist.
  const phone = u.phone ?? u.phoneNumber ?? u.mobile ?? u.contact ?? ''
  const contact = u.contact ?? ''

  return {
    id: 0,
    fullName: '',
    company: '',
    role: '',
    username: '',
    country: '',
    email: '',
    currentPlan: '',
    status: '',
    avatar: '',
    taskDone: null,
    projectDone: null,
    taxId: '',
    language: '',
    ...u,
    phone: phone == null ? '' : String(phone),
    contact: contact == null ? '' : String(contact),
  }
}

const props = defineProps({
  userData: {
    type: Object,
    required: false,
    default: () => ({
      id: 0,
      fullName: '',
      company: '',
      role: '',
      username: '',
      country: '',
      phone: '',
      contact: '',
      email: '',
      currentPlan: '',
      status: '',
      avatar: '',
      taskDone: null,
      projectDone: null,
      taxId: '',
      language: '',
    }),
  },
  isDialogVisible: {
    type: Boolean,
    required: true,
  },
})

const emit = defineEmits([
  'submit',
  'update:isDialogVisible',
])

const userData = ref(normalizeUser(structuredClone(toRaw(props.userData))))
const isUseAsBillingAddress = ref(false)
const firstName = ref('')
const lastName = ref('')

const syncNamesFromFullName = u => {
  const fullName = (u?.fullName || '').trim()
  if (!fullName) {
    firstName.value = ''
    lastName.value = ''
    return
  }

  const parts = fullName.split(/\s+/)
  firstName.value = parts[0] || ''
  // Join the rest as last name, so "Ivan Ivan Ivanovich" works.
  lastName.value = parts.slice(1).join(' ')
}

watch(
  () => props.userData,
  newVal => {
    userData.value = normalizeUser(structuredClone(toRaw(newVal)))
    syncNamesFromFullName(userData.value)
  },
  { deep: true },
)

syncNamesFromFullName(userData.value)

const onFormSubmit = () => {
  // Rebuild fullName from name parts (the old split() v-model doesn't mutate fullName).
  userData.value.fullName = [firstName.value, lastName.value].filter(Boolean).join(' ')
  emit('update:isDialogVisible', false)
  emit('submit', userData.value)
}

const onFormReset = () => {
  userData.value = normalizeUser(structuredClone(toRaw(props.userData)))
  syncNamesFromFullName(userData.value)
  emit('update:isDialogVisible', false)
}

const dialogModelValueUpdate = val => {
  emit('update:isDialogVisible', val)
}
</script>

<template>
  <VDialog
    :width="$vuetify.display.smAndDown ? 'auto' : 900"
    :model-value="props.isDialogVisible"
    @update:model-value="dialogModelValueUpdate"
  >
    <!-- Dialog close btn -->
    <DialogCloseBtn @click="dialogModelValueUpdate(false)" />

    <VCard class="pa-sm-10 pa-2">
      <VCardText>
        <!-- 👉 Title -->
        <h4 class="text-h4 text-center mb-2">
          Редактировать информацию о пользователе
        </h4>
        <p class="text-body-1 text-center mb-6">
          Обновление данных пользователя будет проверено на соответствие требованиям конфиденциальности.
        </p>

        <!-- 👉 Form -->
        <VForm
          class="mt-6"
          @submit.prevent="onFormSubmit"
        >
          <VRow>
            <!-- 👉 First Name -->
            <VCol
              cols="12"
              md="6"
            >
              <AppTextField
                v-model="firstName"
                label="Имя"
                placeholder="Иван"
              />
            </VCol>

            <!-- 👉 Last Name -->
            <VCol
              cols="12"
              md="6"
            >
              <AppTextField
                v-model="lastName"
                label="Фамилия"
                placeholder="Иванов"
              />
            </VCol>

            <!-- 👉 Username -->
            <VCol cols="12">
              <AppTextField
                v-model="userData.username"
                label="Имя пользователя"
                placeholder="ivan.ivanov.007"
              />
            </VCol>

            <!-- 👉 Billing Email -->
            <VCol
              cols="12"
              md="6"
            >
              <AppTextField
                v-model="userData.email"
                label="Электронная почта"
                placeholder="example@email.com"
              />
            </VCol>

            <!-- 👉 Status -->
            <VCol
              cols="12"
              md="6"
            >
              <AppSelect
                v-model="userData.status"
                label="Статус"
                placeholder="Активен"
                :items="[
                  { title: 'Активен', value: 'Active' },
                  { title: 'Неактивен', value: 'Inactive' },
                  { title: 'Ожидает', value: 'Pending' },
                ]"
              />
            </VCol>

            <!-- 👉 Tax Id -->
            <VCol
              cols="12"
              md="6"
            >
              <AppTextField
                v-model="userData.taxId"
                label="ИНН (налоговый номер)"
                placeholder="123456789"
              />
            </VCol>

            <!-- 👉 Contact -->
            <VCol
              cols="12"
              md="6"
            >
              <AppTextField
                v-model="userData.phone"
                label="Номер телефона (регистрация)"
                placeholder="+7 987 654-32-10"
              />
            </VCol>

            <!-- 👉 Language -->
            <VCol
              cols="12"
              md="6"
            >
              <AppSelect
                v-model="userData.language"
                closable-chips
                chips
                multiple
                label="Язык"
                placeholder="Английский"
                :items="[
                  { title: 'Английский', value: 'English' },
                  { title: 'Испанский', value: 'Spanish' },
                  { title: 'Французский', value: 'French' },
                ]"
              />
            </VCol>

            <!-- 👉 Country -->
            <VCol
              cols="12"
              md="6"
            >
              <AppSelect
                v-model="userData.country"
                label="Страна"
                placeholder="США"
                :items="[
                  { title: 'США', value: 'United States' },
                  { title: 'Великобритания', value: 'United Kingdom' },
                  { title: 'Франция', value: 'France' },
                ]"
              />
            </VCol>

            <!-- 👉 Switch -->
            <VCol cols="12">
              <VSwitch
                v-model="isUseAsBillingAddress"
                density="compact"
                label="Использовать как платёжный адрес?"
              />
            </VCol>

            <!-- 👉 Submit and Cancel -->
            <VCol
              cols="12"
              class="d-flex flex-wrap justify-center gap-4"
            >
              <VBtn type="submit">
                Сохранить
              </VBtn>

              <VBtn
                color="secondary"
                variant="tonal"
                @click="onFormReset"
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
