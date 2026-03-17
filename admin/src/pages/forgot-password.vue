<script setup>
import { useGenerateImageVariant } from '@core/composable/useGenerateImageVariant'
import { VNodeRenderer } from '@layouts/components/VNodeRenderer'
import { themeConfig } from '@themeConfig'
import authV2ForgotPasswordIllustrationDark from '@images/pages/auth-v2-forgot-password-illustration-dark.png'
import authV2ForgotPasswordIllustrationLight from '@images/pages/auth-v2-forgot-password-illustration-light.png'
import authV2MaskDark from '@images/pages/misc-mask-dark.png'
import authV2MaskLight from '@images/pages/misc-mask-light.png'

const email = ref('')
const loading = ref(false)
const success = ref(false)
const errorMessage = ref('')
const authThemeImg = useGenerateImageVariant(authV2ForgotPasswordIllustrationLight, authV2ForgotPasswordIllustrationDark)
const authThemeMask = useGenerateImageVariant(authV2MaskLight, authV2MaskDark)

definePage({
  meta: {
    layout: 'blank',
    unauthenticatedOnly: true,
  },
})

const onSubmit = async () => {
  if (!email.value) {
    errorMessage.value = 'Укажите email или имя пользователя'
    return
  }
  loading.value = true
  errorMessage.value = ''
  success.value = false

  try {
    const res = await $api('/auth/forgot-password', {
      method: 'POST',
      body: {
        email: email.value,
      },
      onResponseError({ response }) {
        const errors = response._data?.errors
        errorMessage.value = (errors?.email && errors.email[0]) || 'Не удалось отправить письмо для сброса пароля'
      },
    })
    if (res?.success)
      success.value = true
  } catch (err) {
    console.error(err)
    if (!errorMessage.value)
      errorMessage.value = 'Не удалось отправить письмо для сброса пароля'
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
    class="auth-wrapper bg-surface"
    no-gutters
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
            max-width="468"
            :src="authThemeImg"
            class="auth-illustration mt-16 mb-2"
          />
        </div>

        <img
          class="auth-footer-mask"
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
      class="d-flex align-center justify-center"
    >
      <VCard
        flat
        :max-width="500"
        class="mt-12 mt-sm-0 pa-4"
      >
        <VCardText>
          <h4 class="text-h4 mb-1">
            Забыли пароль? 🔒
          </h4>
          <p class="mb-0">
            Введите ваш email и мы отправим вам инструкции для сброса пароля
          </p>
        </VCardText>

        <VCardText>
          <VForm
            @submit.prevent="onSubmit"
          >
            <VRow>
              <VCol cols="12">
                <AppTextField
                  v-model="email"
                  autofocus
                  label="Email или имя пользователя"
                  type="text"
                  placeholder="admin@example.com"
                  :error-messages="errorMessage"
                />
              </VCol>

              <VCol cols="12">
                <VAlert
                  v-if="success"
                  type="success"
                  variant="tonal"
                  class="mb-4"
                >
                  Мы отправили письмо с инструкциями по сбросу пароля.
                </VAlert>
              </VCol>

              <VCol cols="12">
                <VBtn
                  block
                  type="submit"
                  :loading="loading"
                >
                  Отправить ссылку для сброса
                </VBtn>
              </VCol>

              <!-- back to login -->
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
@use "@core/scss/template/pages/page-auth.scss";
</style>
