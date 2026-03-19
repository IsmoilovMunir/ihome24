<script setup>
const route = useRoute('apps-user-view-id')

const isNewPasswordVisible = ref(false)
const isConfirmPasswordVisible = ref(false)
const newPassword = ref('')
const confirmPassword = ref('')
const isLoading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

const changePassword = async () => {
  if (!newPassword.value || !confirmPassword.value) {
    errorMessage.value = 'Пожалуйста, заполните все поля'
    return
  }

  if (newPassword.value.length < 6) {
    errorMessage.value = 'Пароль должен содержать не менее 6 символов'
    return
  }

  if (newPassword.value !== confirmPassword.value) {
    errorMessage.value = 'Пароли не совпадают'
    return
  }

  isLoading.value = true
  errorMessage.value = ''
  successMessage.value = ''

  try {
    const userId = route.params.id

    await $api(`/apps/users/${userId}/password`, {
      method: 'PATCH',
      body: {
        password: newPassword.value,
      },
    })

    successMessage.value = 'Пароль успешно изменён'
    newPassword.value = ''
    confirmPassword.value = ''

    setTimeout(() => {
      successMessage.value = ''
    }, 3000)
  } catch (error) {
    console.error('Error changing password:', error)
    errorMessage.value = error.data?.message || error.message || 'Ошибка при изменении пароля'
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <VRow>
    <VCol cols="12">
      <VCard title="Изменить пароль">
        <VCardText>
          <VAlert
            variant="tonal"
            color="info"
            class="mb-4"
            title="Требования"
            text="Не менее 6 символов (как на сервере). Рекомендуется использовать буквы, цифры и символы."
          />

          <VAlert
            v-if="errorMessage"
            type="error"
            variant="tonal"
            closable
            class="mb-4"
            @click:close="errorMessage = ''"
          >
            {{ errorMessage }}
          </VAlert>

          <VAlert
            v-if="successMessage"
            type="success"
            variant="tonal"
            closable
            class="mb-4"
            @click:close="successMessage = ''"
          >
            {{ successMessage }}
          </VAlert>

          <VForm @submit.prevent="changePassword">
            <VRow>
              <VCol
                cols="12"
                md="6"
              >
                <AppTextField
                  v-model="newPassword"
                  label="Новый пароль"
                  placeholder="············"
                  :type="isNewPasswordVisible ? 'text' : 'password'"
                  :append-inner-icon="isNewPasswordVisible ? 'tabler-eye-off' : 'tabler-eye'"
                  :disabled="isLoading"
                  @click:append-inner="isNewPasswordVisible = !isNewPasswordVisible"
                />
              </VCol>
              <VCol
                cols="12"
                md="6"
              >
                <AppTextField
                  v-model="confirmPassword"
                  label="Подтвердите пароль"
                  autocomplete="confirm-password"
                  placeholder="············"
                  :type="isConfirmPasswordVisible ? 'text' : 'password'"
                  :append-inner-icon="isConfirmPasswordVisible ? 'tabler-eye-off' : 'tabler-eye'"
                  :disabled="isLoading"
                  @click:append-inner="isConfirmPasswordVisible = !isConfirmPasswordVisible"
                />
              </VCol>

              <VCol cols="12">
                <VBtn
                  type="submit"
                  :loading="isLoading"
                >
                  Изменить пароль
                </VBtn>
              </VCol>
            </VRow>
          </VForm>
        </VCardText>
      </VCard>
    </VCol>
  </VRow>
</template>
