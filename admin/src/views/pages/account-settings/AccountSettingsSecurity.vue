<script setup>
import laptopGirl from '@images/illustrations/laptop-girl.png'
import { $api } from '@/utils/api'

const isCurrentPasswordVisible = ref(false)
const isNewPasswordVisible = ref(false)
const isConfirmPasswordVisible = ref(false)
const currentPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')

const passwordRequirements = [
  'Минимум 8 символов — чем больше, тем лучше',
  'Хотя бы одна строчная буква',
  'Хотя бы одна цифра, символ или пробел',
]

const serverKeys = [
  {
    name: 'Server Key 1',
    key: '23eaf7f0-f4f7-495e-8b86-fad3261282ac',
    createdOn: '28 Apr 2021, 18:20 GTM+4:10',
    permission: 'Full Access',
  },
  {
    name: 'Server Key 2',
    key: 'bb98e571-a2e2-4de8-90a9-2e231b5e99',
    createdOn: '12 Feb 2021, 10:30 GTM+2:30',
    permission: 'Read Only',
  },
  {
    name: 'Server Key 3',
    key: '2e915e59-3105-47f2-8838-6e46bf83b711',
    createdOn: '28 Dec 2020, 12:21 GTM+4:10',
    permission: 'Full Access',
  },
]

const recentDevicesHeaders = [
  {
    title: 'Браузер',
    key: 'browser',
  },
  {
    title: 'Устройство',
    key: 'device',
  },
  {
    title: 'Местоположение',
    key: 'location',
  },
  {
    title: 'Недавняя активность',
    key: 'recentActivity',
  },
]

const recentDevices = ref([])
const isLoadingDevices = ref(false)

const isOneTimePasswordDialogVisible = ref(false)

const loadRecentDevices = async () => {
  try {
    isLoadingDevices.value = true
    const res = await $api('/auth/login-devices')
    recentDevices.value = (res || []).map(item => ({
      browser: item.browser || 'Неизвестный браузер',
      device: item.device || 'Устройство',
      location: item.location || 'Неизвестно',
      recentActivity: item.recentActivity || '—',
      deviceIcon: item.deviceIcon || {
        icon: 'tabler-device-laptop',
        color: 'info',
      },
    }))
  } catch (err) {
    console.error('Не удалось загрузить недавние устройства', err)
  } finally {
    isLoadingDevices.value = false
  }
}

onMounted(() => {
  loadRecentDevices()
})
</script>

<template>
  <VRow>
    <!-- SECTION: Change Password -->
    <VCol cols="12">
      <VCard title="Смена пароля">
        <VForm>
          <VCardText class="pt-0">
            <!-- 👉 Current Password -->
            <VRow>
              <VCol
                cols="12"
                md="6"
              >
                <!-- 👉 current password -->
                <AppTextField
                  v-model="currentPassword"
                  :type="isCurrentPasswordVisible ? 'text' : 'password'"
                  :append-inner-icon="isCurrentPasswordVisible ? 'tabler-eye-off' : 'tabler-eye'"
                  label="Текущий пароль"
                  autocomplete="on"
                  placeholder="············"
                  @click:append-inner="isCurrentPasswordVisible = !isCurrentPasswordVisible"
                />
              </VCol>
            </VRow>

            <!-- 👉 New Password -->
            <VRow>
              <VCol
                cols="12"
                md="6"
              >
                <!-- 👉 new password -->
                <AppTextField
                  v-model="newPassword"
                  :type="isNewPasswordVisible ? 'text' : 'password'"
                  :append-inner-icon="isNewPasswordVisible ? 'tabler-eye-off' : 'tabler-eye'"
                  label="Новый пароль"
                  autocomplete="on"
                  placeholder="············"
                  @click:append-inner="isNewPasswordVisible = !isNewPasswordVisible"
                />
              </VCol>

              <VCol
                cols="12"
                md="6"
              >
                <!-- 👉 confirm password -->
                <AppTextField
                  v-model="confirmPassword"
                  :type="isConfirmPasswordVisible ? 'text' : 'password'"
                  :append-inner-icon="isConfirmPasswordVisible ? 'tabler-eye-off' : 'tabler-eye'"
                  label="Подтверждение нового пароля"
                  autocomplete="on"
                  placeholder="············"
                  @click:append-inner="isConfirmPasswordVisible = !isConfirmPasswordVisible"
                />
              </VCol>
            </VRow>
          </VCardText>

          <!-- 👉 Password Requirements -->
          <VCardText>
            <h6 class="text-h6 text-medium-emphasis mb-4">
              Требования к паролю:
            </h6>

            <VList class="card-list">
              <VListItem
                v-for="item in passwordRequirements"
                :key="item"
                :title="item"
                class="text-medium-emphasis"
              >
                <template #prepend>
                  <VIcon
                    size="10"
                    icon="tabler-circle-filled"
                  />
                </template>
              </VListItem>
            </VList>
          </VCardText>

          <!-- 👉 Action Buttons -->
          <VCardText class="d-flex flex-wrap gap-4">
            <VBtn>Сохранить изменения</VBtn>

            <VBtn
              type="reset"
              color="secondary"
              variant="tonal"
            >
              Сбросить
            </VBtn>
          </VCardText>
        </VForm>
      </VCard>
    </VCol>
    <!-- !SECTION -->

    <!-- SECTION Two-steps verification -->
    <VCol cols="12">
      <VCard title="Двухфакторная аутентификация">
        <VCardText>
          <h5 class="text-h5 text-medium-emphasis mb-4">
            Двухфакторная аутентификация пока не включена.
          </h5>
          <p class="mb-6">
            Двухфакторная аутентификация добавляет дополнительный уровень безопасности,
            требуя при входе не только пароль, но и одноразовый код.
          </p>

          <VBtn @click="isOneTimePasswordDialogVisible = true">
            Включить двухфакторную аутентификацию
          </VBtn>
        </VCardText>
      </VCard>
    </VCol>
    <!-- !SECTION -->

    <VCol cols="12">
      <!-- SECTION: Create an API key -->
      <VCard title="Создать API‑ключ">
        <VRow no-gutters>
          <!-- 👉 Choose API Key -->
          <VCol
            cols="12"
            md="5"
            order-md="0"
            order="1"
          >
            <VCardText class="pt-1">
              <VForm @submit.prevent="() => { }">
                <VRow>
                  <!-- 👉 Choose API Key -->
                  <VCol cols="12">
                    <AppSelect
                      label="Выберите тип API‑ключа"
                      placeholder="Выберите тип API‑ключа"
                      :items="['Полный доступ', 'Изменение', 'Чтение и выполнение', 'Список содержимого папки', 'Только чтение', 'Чтение и запись']"
                    />
                  </VCol>

                  <!-- 👉 Name the API Key -->
                  <VCol cols="12">
                    <AppTextField
                      label="Название API‑ключа"
                      placeholder="Введите название API‑ключа"
                    />
                  </VCol>

                  <!-- 👉 Create Key Button -->
                  <VCol cols="12">
                    <VBtn
                      type="submit"
                      block
                    >
                      Создать ключ
                    </VBtn>
                  </VCol>
                </VRow>
              </VForm>
            </VCardText>
          </VCol>

          <!-- 👉 Lady image -->
          <VCol
            cols="12"
            md="7"
            order="0"
            order-md="1"
            class="d-flex flex-column justify-center align-center"
          >
            <VImg
              :src="laptopGirl"
              :width="$vuetify.display.smAndDown ? '150' : '200'"
              :style="$vuetify.display.smAndDown ? 'margin-block-end: 24px' : 'position: absolute; bottom: 0;'"
            />
          </VCol>
        </VRow>
      </VCard>
      <!-- !SECTION -->
    </VCol>

    <VCol cols="12">
      <!-- SECTION: API Keys List -->
      <VCard>
        <VCardItem class="pb-4">
          <VCardTitle>Список API‑ключей и доступ</VCardTitle>
        </VCardItem>
        <VCardText>
          API‑ключ — это зашифрованная строка, которая идентифицирует приложение.
          Ключи используются для доступа к данным и привязки запросов к вашему проекту
          для квот и учёта.
        </VCardText>

        <!-- 👉 Server Status -->
        <VCardText class="d-flex flex-column gap-y-6">
          <VCard
            v-for="serverKey in serverKeys"
            :key="serverKey.key"
            flat
            class="pa-4"
            color="rgba(var(--v-theme-on-surface),var(--v-hover-opacity))"
          >
            <div class="d-flex flex-column gap-y-2">
              <div class="d-flex align-center flex-wrap">
                <h5 class="text-h5 me-3">
                  {{ serverKey.name }}
                </h5>
                <VChip
                  label
                  color="primary"
                  size="small"
                >
                  {{ serverKey.permission }}
                </VChip>
              </div>
              <div class="d-flex align-center text-base font-weight-medium">
                <h6 class="text-h6 text-medium-emphasis me-3">
                  {{ serverKey.key }}
                </h6>
                <div class="cursor-pointer">
                  <VIcon
                    icon="tabler-copy"
                    size="20"
                  />
                </div>
              </div>
              <div class="text-disabled">
                Создан {{ serverKey.createdOn }}
              </div>
            </div>
          </VCard>
        </VCardText>
      </VCard>
      <!-- !SECTION -->
    </VCol>

    <!-- SECTION Recent Devices -->
    <VCol cols="12">
      <!-- 👉 Table -->
      <VCard title="Недавние устройства">
        <VDivider />

        <VDataTable
          :headers="recentDevicesHeaders"
          :items="recentDevices"
          hide-default-footer
          class="text-no-wrap"
        >
          <template #item.browser="{ item }">
            <div class="d-flex">
              <VIcon
                start
                size="22"
                :icon="item.deviceIcon.icon"
                :color="item.deviceIcon.color"
              />
              <div class="text-high-emphasis text-body-1 font-weight-medium">
                {{ item.browser }}
              </div>
            </div>
          </template>
          <template #item.device="{ item }">
            {{ item.device }}
          </template>
          <template #item.location="{ item }">
            {{ item.location }}
          </template>
          <template #item.recentActivity="{ item }">
            {{ item.recentActivity }}
          </template>
          <!-- TODO Refactor this after vuetify provides proper solution for removing default footer -->
          <template #bottom />
        </VDataTable>
      </VCard>
    </VCol>
    <!-- !SECTION -->
  </VRow>

  <!-- SECTION Enable One time password -->
  <TwoFactorAuthDialog v-model:is-dialog-visible="isOneTimePasswordDialogVisible" />
  <!-- !SECTION -->
</template>

<style lang="scss" scoped>
.card-list {
  --v-card-list-gap: 16px;
}

.server-close-btn {
  inset-inline-end: 0.5rem;
}
</style>
