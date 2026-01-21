<script setup>
import { PerfectScrollbar } from 'vue3-perfect-scrollbar'

const props = defineProps({
  isDrawerOpen: {
    type: Boolean,
    required: true,
  },
})

const emit = defineEmits([
  'update:isDrawerOpen',
  'userData',
])

const isFormValid = ref(false)
const refForm = ref()
const fullName = ref('')
const userName = ref('')
const email = ref('')
const company = ref('')
const country = ref()
const contact = ref('')
const role = ref()
const plan = ref()
const status = ref()

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
      emit('userData', {
        id: 0,
        fullName: fullName.value,
        company: company.value,
        role: role.value,
        country: country.value,
        contact: contact.value,
        email: email.value,
        currentPlan: plan.value,
        status: status.value,
        avatar: '',
        billing: 'Автоматическое списание',
      })
      emit('update:isDrawerOpen', false)
      nextTick(() => {
        refForm.value?.reset()
        refForm.value?.resetValidation()
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
                  :rules="[requiredValidator]"
                  label="Имя пользователя"
                  placeholder="ivanov"
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

              <!-- 👉 company -->
              <VCol cols="12">
                <AppTextField
                  v-model="company"
                  :rules="[requiredValidator]"
                  label="Компания"
                  placeholder="Название компании"
                />
              </VCol>

              <!-- 👉 Country -->
              <VCol cols="12">
                <AppSelect
                  v-model="country"
                  label="Выберите страну"
                  placeholder="Выберите страну"
                  :rules="[requiredValidator]"
                  :items="[
                    { title: 'Россия', value: 'Россия' },
                    { title: 'Казахстан', value: 'Казахстан' },
                    { title: 'Беларусь', value: 'Беларусь' },
                    { title: 'Украина', value: 'Украина' }
                  ]"
                />
              </VCol>

              <!-- 👉 Contact -->
              <VCol cols="12">
                <AppTextField
                  v-model="contact"
                  type="number"
                  :rules="[requiredValidator]"
                  label="Контакт"
                  placeholder="+7-999-123-45-67"
                />
              </VCol>

              <!-- 👉 Role -->
              <VCol cols="12">
                <AppSelect
                  v-model="role"
                  label="Выберите роль"
                  placeholder="Выберите роль"
                  :rules="[requiredValidator]"
                  :items="[
                    { title: 'Администратор', value: 'admin' },
                    { title: 'Автор', value: 'author' },
                    { title: 'Редактор', value: 'editor' },
                    { title: 'Сопровождающий', value: 'maintainer' },
                    { title: 'Подписчик', value: 'subscriber' }
                  ]"
                />
              </VCol>

              <!-- 👉 Plan -->
              <VCol cols="12">
                <AppSelect
                  v-model="plan"
                  label="Выберите план"
                  placeholder="Выберите план"
                  :rules="[requiredValidator]"
                  :items="[
                    { title: 'Базовый', value: 'basic' },
                    { title: 'Компания', value: 'company' },
                    { title: 'Предприятие', value: 'enterprise' },
                    { title: 'Команда', value: 'team' }
                  ]"
                />
              </VCol>

              <!-- 👉 Status -->
              <VCol cols="12">
                <AppSelect
                  v-model="status"
                  label="Выберите статус"
                  placeholder="Выберите статус"
                  :rules="[requiredValidator]"
                  :items="[{ title: 'Активен', value: 'active' }, { title: 'Неактивен', value: 'inactive' }, { title: 'Ожидает', value: 'pending' }]"
                />
              </VCol>

              <!-- 👉 Submit and Cancel -->
              <VCol cols="12">
                <VBtn
                  type="submit"
                  class="me-3"
                >
                  Отправить
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
