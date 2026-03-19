<script setup>
const isNewPasswordVisible = ref(false)
const isConfirmPasswordVisible = ref(false)
const smsVerificationNumber = ref('+1(968) 819-2547')
const isTwoFactorDialogOpen = ref(false)

// Recent devices Headers
const recentDeviceHeader = [
  {
    title: 'БРАУЗЕР',
    key: 'browser',
  },
  {
    title: 'УСТРОЙСТВО',
    key: 'device',
  },
  {
    title: 'МЕСТОПОЛОЖЕНИЕ',
    key: 'location',
  },
  {
    title: 'ПОСЛЕДНЯЯ АКТИВНОСТЬ',
    key: 'activity',
  },
]

const recentDevices = [
  {
    browser: 'Chrome (Windows)',
    logo: 'tabler-brand-windows',
    color: 'info',
    device: 'HP Specter 360',
    location: 'Швейцария',
    activity: '10 июля 2021 20:07',
  },
  {
    browser: 'Chrome (iPhone)',
    logo: 'tabler-device-mobile',
    color: 'error',
    device: 'iPhone 12x',
    location: 'Австралия',
    activity: '13 июля 2021 10:10',
  },
  {
    browser: 'Chrome (Android)',
    logo: 'tabler-brand-android',
    color: 'success',
    device: 'OnePlus 9 Pro',
    location: 'Дубай',
    activity: '4 июля 2021 15:15',
  },
  {
    browser: 'Chrome (macOS)',
    logo: 'tabler-brand-apple',
    color: 'secondary',
    device: 'Apple iMac',
    location: 'Индия',
    activity: '20 июля 2021 21:01',
  },
  {
    browser: 'Chrome (Windows)',
    logo: 'tabler-brand-windows',
    color: 'info',
    device: 'HP Specter 360',
    location: 'Швейцария',
    activity: '10 июля 2021 20:07',
  },
  {
    browser: 'Chrome (Android)',
    logo: 'tabler-brand-android',
    color: 'success',
    device: 'OnePlus 9 Pro',
    location: 'Дубай',
    activity: '4 июля 2021 15:15',
  },
]
</script>

<template>
  <VRow>
    <VCol cols="12">
      <!-- 👉 Change password -->
      <VCard title="Изменить пароль">
        <VCardText>
          <VAlert
            variant="tonal"
            color="warning"
            title="Убедитесь, что выполнены следующие требования"
            text="Минимум 8 символов, заглавные буквы и символы"
            class="mb-4"
            closable
          />

          <VForm @submit.prevent="() => {}">
            <VRow>
              <VCol
                cols="12"
                md="6"
              >
                <AppTextField
                  label="Новый пароль"
                  placeholder="············"
                  :type="isNewPasswordVisible ? 'text' : 'password'"
                  :append-inner-icon="isNewPasswordVisible ? 'tabler-eye-off' : 'tabler-eye'"
                  @click:append-inner="isNewPasswordVisible = !isNewPasswordVisible"
                />
              </VCol>
              <VCol
                cols="12"
                md="6"
              >
                <AppTextField
                  label="Подтвердить пароль"
                  autocomplete="confirm-password"
                  placeholder="············"
                  :type="isConfirmPasswordVisible ? 'text' : 'password'"
                  :append-inner-icon="isConfirmPasswordVisible ? 'tabler-eye-off' : 'tabler-eye'"
                  @click:append-inner="isConfirmPasswordVisible = !isConfirmPasswordVisible"
                />
              </VCol>

              <VCol cols="12">
                <VBtn type="submit">
                  Изменить пароль
                </VBtn>
              </VCol>
            </VRow>
          </VForm>
        </VCardText>
      </VCard>
    </VCol>

    <VCol cols="12">
      <!-- 👉 Two step verification -->
      <VCard
        title="Двухфакторная аутентификация"
        subtitle="Обеспечьте безопасность вашего аккаунта с помощью шага аутентификации."
      >
        <VCardText>
          <div class="text-h6 mb-1">
            SMS
          </div>
          <AppTextField placeholder="+1(968) 819-2547">
            <template #append>
              <IconBtn>
                <VIcon
                  icon="tabler-edit"
                  size="22"
                />
              </IconBtn>
              <IconBtn>
                <VIcon
                  icon="tabler-user-plus"
                  size="22"
                />
              </IconBtn>
            </template>
          </AppTextField>

          <p class="mb-0 mt-4">
            Двухфакторная аутентификация добавляет дополнительный уровень безопасности к вашему аккаунту, требуя больше, чем просто пароль для входа. <a
              href="javascript:void(0)"
              class="text-decoration-none"
            >Узнать больше</a>.
          </p>
        </VCardText>
      </VCard>
    </VCol>

    <VCol cols="12">
      <!-- 👉 Recent devices -->
      <VCard title="Недавние устройства">
        <VDivider />
        <VDataTable
          :items="recentDevices"
          :headers="recentDeviceHeader"
          hide-default-footer
          class="text-no-wrap"
        >
          <template #item.browser="{ item }">
            <div class="d-flex align-center gap-x-4">
              <VIcon
                :icon="item.logo"
                :color="item.color"
                size="22"
              />
              <div class="text-body-1 text-high-emphasis">
                {{ item.browser }}
              </div>
            </div>
          </template>
          <!-- TODO Refactor this after vuetify provides proper solution for removing default footer -->
          <template #bottom />
        </VDataTable>
      </VCard>
    </VCol>
  </VRow>

  <!-- 👉 Enable One Time Password Dialog -->
  <TwoFactorAuthDialog
    v-model:is-dialog-visible="isTwoFactorDialogOpen"
    :sms-code="smsVerificationNumber"
  />
</template>
