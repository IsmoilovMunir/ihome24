<!-- ❗Errors in the form are set on line 60 -->
<script setup>
import { VForm } from 'vuetify/components/VForm'
import AuthProvider from '@/views/pages/authentication/AuthProvider.vue'
import { useGenerateImageVariant } from '@core/composable/useGenerateImageVariant'
import authV2LoginIllustrationBorderedDark from '@images/pages/auth-v2-login-illustration-bordered-dark.png'
import authV2LoginIllustrationBorderedLight from '@images/pages/auth-v2-login-illustration-bordered-light.png'
import authV2LoginIllustrationDark from '@images/pages/auth-v2-login-illustration-dark.png'
import authV2LoginIllustrationLight from '@images/pages/auth-v2-login-illustration-light.png'
import authV2MaskDark from '@images/pages/misc-mask-dark.png'
import authV2MaskLight from '@images/pages/misc-mask-light.png'
import { VNodeRenderer } from '@layouts/components/VNodeRenderer'
import { themeConfig } from '@themeConfig'

const authThemeImg = useGenerateImageVariant(authV2LoginIllustrationLight, authV2LoginIllustrationDark, authV2LoginIllustrationBorderedLight, authV2LoginIllustrationBorderedDark, true)
const authThemeMask = useGenerateImageVariant(authV2MaskLight, authV2MaskDark)

definePage({
  meta: {
    layout: 'blank',
    unauthenticatedOnly: true,
  },
})

const isPasswordVisible = ref(false)
const route = useRoute()
const router = useRouter()
const ability = useAbility()

const errors = ref({
  username: undefined,
  email: undefined, // Оставляем для обратной совместимости с бэкендом
  password: undefined,
  authentication: undefined,
})

const refVForm = ref()

const credentials = ref({
  username: '',
  password: '',
})

const rememberMe = ref(false)

const isTwoFactorStep = ref(false)
const twoFactorToken = ref('')
const emailCode = ref('')
const twoFactorError = ref()
const isVerifyingTwoFactor = ref(false)
const isSubmitting = ref(false)

const login = async () => {
  if (isSubmitting.value)
    return

  try {
    isSubmitting.value = true
    errors.value = {
      username: undefined,
      email: undefined,
      password: undefined,
      authentication: undefined,
    }

    const res = await $api('/auth/login', {
      method: 'POST',
      body: {
        username: credentials.value.username,
        password: credentials.value.password,
      },
      onResponseError({ response }) {
        const apiErrors = response?._data?.errors
        errors.value = (apiErrors && typeof apiErrors === 'object') ? apiErrors : {}
        // Если ошибка в email, показываем её в поле username (для обратной совместимости)
        if (apiErrors?.email && !apiErrors?.username) {
          errors.value.username = apiErrors.email
        }
        if (apiErrors?.phone && !apiErrors?.username) {
          errors.value.username = apiErrors.phone
        }
        if (!Object.keys(errors.value).length) {
          errors.value.authentication = [response?._data?.message || 'Ошибка входа. Проверьте логин и пароль.']
        }
      },
    })

    // Если для администратора включена двухфакторная авторизация по email
    if (res?.twoFactorRequired) {
      twoFactorToken.value = res.twoFactorToken
      isTwoFactorStep.value = true
      twoFactorError.value = undefined
      return
    }

    const { accessToken, userData, userAbilityRules } = res
    if (!accessToken || !userData || !Array.isArray(userAbilityRules)) {
      errors.value.authentication = ['Некорректный ответ сервера авторизации']
      return
    }

    useCookie('userAbilityRules').value = userAbilityRules
    ability.update(userAbilityRules)
    useCookie('userData').value = userData
    useCookie('accessToken').value = accessToken

    // Force password change on first login
    if (userData?.passwordChangeRequired) {
      await nextTick(() => {
        router.replace({ name: 'force-change-password' })
      })
      return
    }

    // Redirect to `to` query if exist or redirect to index route

    // ❗ nextTick is required to wait for DOM updates and later redirect
    await nextTick(() => {
      router.replace(route.query.to ? String(route.query.to) : '/')
    })
  } catch (err) {
    console.error(err)
    const hasFieldErrors = errors.value.username || errors.value.email || errors.value.password
    if (!errors.value.authentication && !hasFieldErrors) {
      errors.value.authentication = [err?.data?.message || err?.message || 'Ошибка входа. Попробуйте ещё раз.']
    }
  } finally {
    isSubmitting.value = false
  }
}

const verifyEmailCode = async () => {
  if (!twoFactorToken.value)
    return

  try {
    if (isVerifyingTwoFactor.value)
      return

    isVerifyingTwoFactor.value = true
    twoFactorError.value = undefined

    const res = await $api('/auth/verify-email-code', {
      method: 'POST',
      body: {
        twoFactorToken: twoFactorToken.value,
        code: emailCode.value,
      },
      onResponseError({ response }) {
        const apiErrors = response._data?.errors
        twoFactorError.value = (apiErrors?.code && apiErrors.code[0]) || (apiErrors?.twoFactorToken && apiErrors.twoFactorToken[0]) || 'Неверный код'
      },
    })

    const { accessToken, userData, userAbilityRules } = res

    useCookie('userAbilityRules').value = userAbilityRules
    ability.update(userAbilityRules)
    useCookie('userData').value = userData
    useCookie('accessToken').value = accessToken

    await nextTick(() => {
      router.replace(route.query.to ? String(route.query.to) : '/')
    })
  } catch (err) {
    console.error(err)
  } finally {
    isVerifyingTwoFactor.value = false
  }
}

const onSubmit = () => {
  refVForm.value?.validate().then(({ valid: isValid }) => {
    if (isValid)
      login()
  })
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
          style="padding-inline: 6.25rem;"
        >
          <VImg
            max-width="613"
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
      class="auth-card-v2 d-flex align-center justify-center"
    >
      <VCard
        flat
        :max-width="500"
        class="mt-12 mt-sm-0 pa-4"
      >
        <VCardText>
          <h4 class="text-h4 mb-1">
            Добро пожаловать в <span class="text-capitalize"> {{ themeConfig.app.title }} </span>! 👋🏻
          </h4>
          <p class="mb-0">
            Пожалуйста, войдите в свой аккаунт и начните приключение
          </p>
        </VCardText>
        <VCardText>
          <VForm
            ref="refVForm"
            @submit.prevent="onSubmit"
          >
            <VRow>
              <template v-if="!isTwoFactorStep">
                <!-- username -->
                <VCol cols="12">
                  <AppTextField
                    v-model="credentials.username"
                    label="Имя пользователя"
                    placeholder="Введите имя пользователя"
                    type="text"
                    autofocus
                    :rules="[requiredValidator]"
                    :error-messages="errors.username || errors.email"
                  />
                </VCol>

                <!-- password -->
                <VCol cols="12">
                  <VAlert
                    v-if="errors.authentication"
                    type="error"
                    variant="tonal"
                    class="mb-4"
                  >
                    {{ Array.isArray(errors.authentication) ? errors.authentication[0] : errors.authentication }}
                  </VAlert>

                  <AppTextField
                    v-model="credentials.password"
                    label="Пароль"
                    placeholder="············"
                    :rules="[requiredValidator]"
                    :type="isPasswordVisible ? 'text' : 'password'"
                    autocomplete="password"
                    :error-messages="errors.password"
                    :append-inner-icon="isPasswordVisible ? 'tabler-eye-off' : 'tabler-eye'"
                    @click:append-inner="isPasswordVisible = !isPasswordVisible"
                  />

                  <div class="d-flex align-center flex-wrap justify-space-between my-6">
                    <VCheckbox
                      v-model="rememberMe"
                      label="Запомнить меня"
                    />
                    <RouterLink
                      class="text-primary ms-2 mb-1"
                      :to="{ name: 'forgot-password' }"
                    >
                      Забыли пароль?
                    </RouterLink>
                  </div>

                  <VBtn
                    block
                    type="submit"
                    :loading="isSubmitting"
                    :disabled="isSubmitting"
                  >
                    Войти
                  </VBtn>
                </VCol>
              </template>

              <template v-else>
                <VCol cols="12">
                  <p class="mb-2">
                    На ваш email отправлен код подтверждения входа. Введите его ниже, чтобы завершить авторизацию.
                  </p>
                  <AppTextField
                    v-model="emailCode"
                    label="Код из письма"
                    placeholder="Введите 6-значный код"
                    type="text"
                    maxlength="6"
                    :error-messages="twoFactorError"
                  />
                </VCol>
                <VCol cols="12">
                  <VBtn
                    block
                    type="button"
                    :loading="isVerifyingTwoFactor"
                    :disabled="isVerifyingTwoFactor"
                    @click="verifyEmailCode"
                  >
                    Подтвердить код
                  </VBtn>
                  <VBtn
                    block
                    variant="tonal"
                    color="secondary"
                    class="mt-2"
                    type="button"
                    @click="isTwoFactorStep = false"
                  >
                    Назад к вводу логина и пароля
                  </VBtn>
                </VCol>
              </template>

              <!-- блок соц-сетей и альтернативных провайдеров авторизации скрыт в админ-панели -->
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
