<script setup>
import { useGenerateImageVariant } from '@core/composable/useGenerateImageVariant'
import { VNodeRenderer } from '@layouts/components/VNodeRenderer'
import { themeConfig } from '@themeConfig'
import authV2ResetPasswordIllustrationDark from '@images/pages/auth-v2-reset-password-illustration-dark.png'
import authV2ResetPasswordIllustrationLight from '@images/pages/auth-v2-reset-password-illustration-light.png'
import authV2MaskDark from '@images/pages/misc-mask-dark.png'
import authV2MaskLight from '@images/pages/misc-mask-light.png'

const route = useRoute()
const router = useRouter()

const form = ref({
  newPassword: '',
  confirmPassword: '',
})

const loading = ref(false)
const errorMessage = ref('')
const success = ref(false)

const authThemeImg = useGenerateImageVariant(authV2ResetPasswordIllustrationLight, authV2ResetPasswordIllustrationDark)
const authThemeMask = useGenerateImageVariant(authV2MaskLight, authV2MaskDark)
const isPasswordVisible = ref(false)
const isConfirmPasswordVisible = ref(false)

definePage({
  meta: {
    layout: 'blank',
    public: true,
  },
})

const onSubmit = async () => {
  const token = route.query.token ? String(route.query.token) : ''
  if (!token) {
    errorMessage.value = 'Неверная или истекшая ссылка для сброса пароля'
    return
  }

  if (!form.value.newPassword || !form.value.confirmPassword) {
    errorMessage.value = 'Введите и подтвердите новый пароль'
    return
  }

  loading.value = true
  errorMessage.value = ''

  try {
    const res = await $api('/auth/reset-password', {
      method: 'POST',
      body: {
        token,
        newPassword: form.value.newPassword,
        confirmPassword: form.value.confirmPassword,
      },
      onResponseError({ response }) {
        const errors = response._data?.errors
        errorMessage.value =
          (errors?.newPassword && errors.newPassword[0]) ||
          (errors?.confirmPassword && errors.confirmPassword[0]) ||
          (errors?.token && errors.token[0]) ||
          'Не удалось изменить пароль'
      },
    })

    if (res?.success) {
      success.value = true
      // через небольшую паузу отправляем на логин
      setTimeout(() => {
        router.replace({ name: 'login' })
      }, 2000)
    }
  } catch (err) {
    console.error(err)
    if (!errorMessage.value)
      errorMessage.value = 'Не удалось изменить пароль'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <RouterLink to="/">
    <div class="auth-logo d-flex align-center gap-x-3">
      <VNodeRenderer :nodes="themeConfig.app.logo" />
      <h1 class="auth-title">
        {{ themeConfig.app.title }}
      </h1>
    </div>
  </RouterLink>

  <VRow
    no-gutters
    class="auth-wrapper bg-surface"
  >
    <VCol
      md="8"
      class="d-none d-md-flex"
    >
      <div class="position-relative bg-background w-100 me-0">
        <div
          class="d-flex align-center justify-center w-100 h-100"
          style="padding-inline: 150px;"
        >
          <VImg
            max-width="451"
            :src="authThemeImg"
            class="auth-illustration mt-16 mb-2"
          />
        </div>

        <img
          class="auth-footer-mask flip-in-rtl"
          :src="authThemeMask"
          alt="auth-footer-mask"
          height="280"
          width="100"
        >
      </div>
    </VCol>

    <VCol
      cols="12"
      md="4"
      class="auth-card-v2 d-flex align-center justify-center"
    >
      <VCard
        flat
        :max-width="500"
        class="mt-12 mt-sm-0 pa-6"
      >
        <VCardText>
          <h4 class="text-h4 mb-1">
            Сброс пароля 🔒
          </h4>
          <p class="mb-0">
            Установите новый пароль для входа в админ-панель
          </p>
        </VCardText>

        <VCardText>
          <VAlert
            v-if="success"
            type="success"
            variant="tonal"
            class="mb-4"
          >
            Пароль успешно изменён. Сейчас вы будете перенаправлены на страницу входа.
          </VAlert>

          <VForm @submit.prevent="onSubmit">
            <VRow>
              <VCol cols="12">
                <AppTextField
                  v-model="form.newPassword"
                  autofocus
                  label="Новый пароль"
                  placeholder="············"
                  :type="isPasswordVisible ? 'text' : 'password'"
                  autocomplete="password"
                  :append-inner-icon="isPasswordVisible ? 'tabler-eye-off' : 'tabler-eye'"
                  :error-messages="errorMessage"
                  @click:append-inner="isPasswordVisible = !isPasswordVisible"
                />
              </VCol>

              <VCol cols="12">
                <AppTextField
                  v-model="form.confirmPassword"
                  label="Подтверждение пароля"
                  autocomplete="confirm-password"
                  placeholder="············"
                  :type="isConfirmPasswordVisible ? 'text' : 'password'"
                  :append-inner-icon="isConfirmPasswordVisible ? 'tabler-eye-off' : 'tabler-eye'"
                  @click:append-inner="isConfirmPasswordVisible = !isConfirmPasswordVisible"
                />
              </VCol>

              <VCol cols="12">
                <VBtn
                  block
                  type="submit"
                  :loading="loading"
                >
                  Установить новый пароль
                </VBtn>
              </VCol>

              <VCol cols="12">
                <RouterLink
                  class="d-flex align-center justify-center"
                  :to="{ name: 'login' }"
                >
                  <VIcon
                    icon="tabler-chevron-left"
                    size="20"
                    class="me-1 flip-in-rtl"
                  />
                  <span>Вернуться к входу</span>
                </RouterLink>
              </VCol>
            </VRow>
          </VForm>
        </VCardText>
      </VCard>
    </VCol>
  </VRow>
</template>

<style lang="scss">
@use "@core/scss/template/pages/page-auth";
</style>

