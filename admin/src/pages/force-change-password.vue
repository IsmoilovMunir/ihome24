<script setup>
import { VForm } from 'vuetify/components/VForm'

definePage({
  meta: {
    layout: 'blank',
  },
})

const router = useRouter()
const errors = ref({
  currentPassword: undefined,
  newPassword: undefined,
  confirmPassword: undefined,
  authentication: undefined,
  error: undefined,
})

const refVForm = ref()
const isNewPasswordVisible = ref(false)
const isConfirmPasswordVisible = ref(false)
const isSubmitting = ref(false)

const form = ref({
  newPassword: '',
  confirmPassword: '',
})

const minLength6Validator = value => String(value || '').length >= 6 || 'Пароль должен быть не короче 6 символов'

const submit = async () => {
  try {
    if (isSubmitting.value)
      return

    errors.value = {}
    isSubmitting.value = true

    await $api('/auth/change-password', {
      method: 'POST',
      body: {
        newPassword: form.value.newPassword,
        confirmPassword: form.value.confirmPassword,
      },
      onResponseError({ response }) {
        const data = response?._data

        // Backend may return either { errors: { field: [...] } } or { message: "..." }
        const apiErrors = data?.errors && typeof data.errors === 'object' ? data.errors : {}
        errors.value = apiErrors

        if (!Object.keys(apiErrors).length) {
          errors.value = {
            error: data?.message || data?.error || 'Не удалось изменить пароль. Проверьте введённые данные.',
          }
        }
      },
    })

    // After changing password, force re-login with the new password
    useCookie('userData').value = null
    useCookie('accessToken').value = null
    useCookie('userAbilityRules').value = null
    if (typeof window !== 'undefined')
      window.localStorage.removeItem('adminLastActivity')

    await nextTick(() => {
      router.replace({ name: 'login', query: { reason: 'password_changed' } })
    })
  } catch (e) {
    if (!errors.value || !Object.keys(errors.value).length) {
      errors.value = {
        error: 'Не удалось изменить пароль. Попробуйте ещё раз.',
      }
    }
  } finally {
    isSubmitting.value = false
  }
}

const onSubmit = () => {
  refVForm.value?.validate().then(({ valid }) => {
    if (valid)
      submit()
  })
}
</script>

<template>
  <div
    class="d-flex align-center justify-center pa-6"
    style="min-height: 100vh;"
  >
    <VCard
      :max-width="520"
      class="pa-6 w-100"
    >
      <VCardText>
        <h4 class="text-h4 mb-2">
          Смена пароля
        </h4>
        <p class="text-body-1 mb-6">
          Это ваш первый вход. Пожалуйста, установите новый пароль.
        </p>

        <VAlert
          v-if="errors.error"
          type="error"
          variant="tonal"
          class="mb-4"
        >
          {{ Array.isArray(errors.error) ? errors.error[0] : errors.error }}
        </VAlert>
        <VAlert
          v-else-if="errors.authentication"
          type="error"
          variant="tonal"
          class="mb-4"
        >
          {{ Array.isArray(errors.authentication) ? errors.authentication[0] : errors.authentication }}
        </VAlert>

        <VForm
          ref="refVForm"
          @submit.prevent="onSubmit"
        >
          <VRow>
            <VCol cols="12">
              <AppTextField
                v-model="form.newPassword"
                :rules="[requiredValidator, minLength6Validator]"
                label="Новый пароль"
                placeholder="············"
                :type="isNewPasswordVisible ? 'text' : 'password'"
                :append-inner-icon="isNewPasswordVisible ? 'tabler-eye-off' : 'tabler-eye'"
                :error-messages="errors.newPassword"
                @click:append-inner="isNewPasswordVisible = !isNewPasswordVisible"
              />
            </VCol>

            <VCol cols="12">
              <AppTextField
                v-model="form.confirmPassword"
                :rules="[requiredValidator, value => confirmedValidator(value, form.newPassword)]"
                label="Подтвердить пароль"
                placeholder="············"
                :type="isConfirmPasswordVisible ? 'text' : 'password'"
                :append-inner-icon="isConfirmPasswordVisible ? 'tabler-eye-off' : 'tabler-eye'"
                :error-messages="errors.confirmPassword"
                @click:append-inner="isConfirmPasswordVisible = !isConfirmPasswordVisible"
              />
            </VCol>

            <VCol cols="12">
              <VBtn
                type="submit"
                block
                :loading="isSubmitting"
                :disabled="isSubmitting"
              >
                Сохранить пароль
              </VBtn>
            </VCol>
          </VRow>
        </VForm>
      </VCardText>
    </VCard>
  </div>
</template>

