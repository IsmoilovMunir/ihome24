<script setup>
import avatar1 from '@images/avatars/avatar-1.png'
import { $api } from '@/utils/api'

const refInputEl = ref()
const isConfirmDialogOpen = ref(false)
const isLoading = ref(false)
const isSaving = ref(false)
const accountDataLocal = ref({
  avatarImg: avatar1,
  firstName: '',
  lastName: '',
  email: '',
  org: '',
  phone: '',
  address: '',
  state: '',
  zip: '',
  country: 'Россия',
  language: 'Русский',
  timezone: '(GMT+03:00) Москва',
  currency: 'RUB',
})
const originalAccountData = ref(null)
const isAccountDeactivated = ref(false)
const validateAccountDeactivation = [v => !!v || 'Пожалуйста, подтвердите деактивацию аккаунта']

// Загрузка данных пользователя с бэкенда
const loadUserData = async () => {
  try {
    isLoading.value = true
    const data = await $api('/auth/me')
    
    if (data) {
      // Разделяем fullName на firstName и lastName
      const nameParts = (data.fullName || '').split(' ')
      const firstName = nameParts[0] || ''
      const lastName = nameParts.slice(1).join(' ') || ''
      
      accountDataLocal.value = {
        avatarImg: data.avatar || avatar1,
        firstName: firstName,
        lastName: lastName,
        email: data.email || '',
        org: data.company || '',
        phone: data.contact || '',
        address: '',
        state: '',
        zip: '',
        country: data.country || 'Россия',
        language: 'Русский',
        timezone: '(GMT+03:00) Москва',
        currency: 'RUB',
      }
      
      originalAccountData.value = structuredClone(accountDataLocal.value)
    }
  } catch (error) {
    console.error('Ошибка при загрузке данных пользователя:', error)
  } finally {
    isLoading.value = false
  }
}

const resetForm = () => {
  if (originalAccountData.value) {
    accountDataLocal.value = structuredClone(originalAccountData.value)
  }
}

// Сохранение данных на бэкенд
const saveAccountData = async () => {
  try {
    isSaving.value = true
    
    // Формируем fullName из firstName и lastName
    const fullName = [accountDataLocal.value.firstName, accountDataLocal.value.lastName]
      .filter(Boolean)
      .join(' ')
    
    const updateData = {
      fullName: fullName || accountDataLocal.value.firstName || accountDataLocal.value.lastName,
      email: accountDataLocal.value.email,
      company: accountDataLocal.value.org,
      country: accountDataLocal.value.country,
      contact: accountDataLocal.value.phone,
      avatar: accountDataLocal.value.avatarImg !== avatar1 ? accountDataLocal.value.avatarImg : null,
    }
    
    await $api('/auth/me', {
      method: 'PUT',
      body: updateData,
    })
    
    // Обновляем originalAccountData после успешного сохранения
    originalAccountData.value = structuredClone(accountDataLocal.value)
    
    // Показываем уведомление об успехе
    alert('Данные успешно сохранены!')
  } catch (error) {
    console.error('Ошибка при сохранении данных:', error)
    alert('Ошибка при сохранении данных: ' + (error.data?.message || error.message || 'Неизвестная ошибка'))
  } finally {
    isSaving.value = false
  }
}

onMounted(() => {
  loadUserData()
})

const changeAvatar = async file => {
  const fileReader = new FileReader()
  const { files } = file.target
  if (files && files.length) {
    const selectedFile = files[0]
    
    // Проверка размера файла (800KB)
    if (selectedFile.size > 800 * 1024) {
      alert('Размер файла превышает 800КБ')
      return
    }
    
    fileReader.readAsDataURL(selectedFile)
    fileReader.onload = async () => {
      if (typeof fileReader.result === 'string') {
        accountDataLocal.value.avatarImg = fileReader.result
        
        // Автоматически сохраняем аватар на бэкенд
        try {
          await $api('/auth/me', {
            method: 'PUT',
            body: {
              avatar: fileReader.result,
            },
          })
        } catch (error) {
          console.error('Ошибка при сохранении аватара:', error)
        }
      }
    }
  }
}

// reset avatar image
const resetAvatar = async () => {
  const defaultAvatar = avatar1
  accountDataLocal.value.avatarImg = defaultAvatar
  
  // Сбрасываем аватар на бэкенде
  try {
    await $api('/auth/me', {
      method: 'PUT',
      body: {
        avatar: null,
      },
    })
  } catch (error) {
    console.error('Ошибка при сбросе аватара:', error)
  }
}

const timezones = [
  '(GMT+03:00) Москва',
  '(GMT+05:00) Екатеринбург',
  '(GMT+07:00) Красноярск',
  '(GMT+10:00) Владивосток',
  '(GMT+02:00) Калининград',
  '(GMT+04:00) Самара',
  '(GMT+06:00) Омск',
  '(GMT+08:00) Иркутск',
  '(GMT+09:00) Якутск',
  '(GMT+11:00) Магадан',
  '(GMT+12:00) Камчатка',
]

const currencies = [
  'RUB',
  'USD',
  'EUR',
  'KZT',
  'BYN',
  'UAH',
]
</script>

<template>
  <VRow>
    <VCol cols="12">
      <VCard>
        <VCardText class="d-flex">
          <!-- 👉 Avatar -->
          <VAvatar
            rounded
            size="100"
            class="me-6"
            :image="accountDataLocal.avatarImg"
          />

          <!-- 👉 Upload Photo -->
          <form class="d-flex flex-column justify-center gap-4">
            <div class="d-flex flex-wrap gap-4">
              <VBtn
                color="primary"
                size="small"
                @click="refInputEl?.click()"
              >
                <VIcon
                  icon="tabler-cloud-upload"
                  class="d-sm-none"
                />
                <span class="d-none d-sm-block">Загрузить новое фото</span>
              </VBtn>

              <input
                ref="refInputEl"
                type="file"
                name="file"
                accept=".jpeg,.png,.jpg,GIF"
                hidden
                @input="changeAvatar"
              >

              <VBtn
                type="reset"
                size="small"
                color="secondary"
                variant="tonal"
                @click="resetAvatar"
              >
                <span class="d-none d-sm-block">Сбросить</span>
                <VIcon
                  icon="tabler-refresh"
                  class="d-sm-none"
                />
              </VBtn>
            </div>

            <p class="text-body-1 mb-0">
              Разрешены JPG, GIF или PNG. Максимальный размер 800КБ
            </p>
          </form>
        </VCardText>

        <VCardText class="pt-2">
          <!-- 👉 Form -->
          <VForm class="mt-3">
            <VRow>
              <!-- 👉 First Name -->
              <VCol
                md="6"
                cols="12"
              >
                <AppTextField
                  v-model="accountDataLocal.firstName"
                  placeholder="Иван"
                  label="Имя"
                />
              </VCol>

              <!-- 👉 Last Name -->
              <VCol
                md="6"
                cols="12"
              >
                <AppTextField
                  v-model="accountDataLocal.lastName"
                  placeholder="Иванов"
                  label="Фамилия"
                />
              </VCol>

              <!-- 👉 Email -->
              <VCol
                cols="12"
                md="6"
              >
                <AppTextField
                  v-model="accountDataLocal.email"
                  label="E-mail"
                  placeholder="ivanov@example.com"
                  type="email"
                />
              </VCol>

              <!-- 👉 Organization -->
              <VCol
                cols="12"
                md="6"
              >
                <AppTextField
                  v-model="accountDataLocal.org"
                  label="Организация"
                  placeholder="Название компании"
                />
              </VCol>

              <!-- 👉 Phone -->
              <VCol
                cols="12"
                md="6"
              >
                <AppTextField
                  v-model="accountDataLocal.phone"
                  label="Номер телефона"
                  placeholder="+7 (999) 123-45-67"
                />
              </VCol>

              <!-- 👉 Address -->
              <VCol
                cols="12"
                md="6"
              >
                <AppTextField
                  v-model="accountDataLocal.address"
                  label="Адрес"
                  placeholder="г. Москва, ул. Примерная, д. 1"
                />
              </VCol>

              <!-- 👉 State -->
              <VCol
                cols="12"
                md="6"
              >
                <AppTextField
                  v-model="accountDataLocal.state"
                  label="Область/Регион"
                  placeholder="Московская область"
                />
              </VCol>

              <!-- 👉 Zip Code -->
              <VCol
                cols="12"
                md="6"
              >
                <AppTextField
                  v-model="accountDataLocal.zip"
                  label="Почтовый индекс"
                  placeholder="101000"
                />
              </VCol>

              <!-- 👉 Country -->
              <VCol
                cols="12"
                md="6"
              >
                <AppSelect
                  v-model="accountDataLocal.country"
                  label="Страна"
                  :items="['Россия', 'Казахстан', 'Беларусь', 'Украина']"
                  placeholder="Выберите страну"
                />
              </VCol>

              <!-- 👉 Language -->
              <VCol
                cols="12"
                md="6"
              >
                <AppSelect
                  v-model="accountDataLocal.language"
                  label="Язык"
                  placeholder="Выберите язык"
                  :items="['Русский', 'English', 'Қазақша']"
                />
              </VCol>

              <!-- 👉 Timezone -->
              <VCol
                cols="12"
                md="6"
              >
                <AppSelect
                  v-model="accountDataLocal.timezone"
                  label="Часовой пояс"
                  placeholder="Выберите часовой пояс"
                  :items="timezones"
                  :menu-props="{ maxHeight: 200 }"
                />
              </VCol>

              <!-- 👉 Currency -->
              <VCol
                cols="12"
                md="6"
              >
                <AppSelect
                  v-model="accountDataLocal.currency"
                  label="Валюта"
                  placeholder="Выберите валюту"
                  :items="currencies"
                  :menu-props="{ maxHeight: 200 }"
                />
              </VCol>

              <!-- 👉 Form Actions -->
              <VCol
                cols="12"
                class="d-flex flex-wrap gap-4"
              >
                <VBtn
                  :loading="isSaving"
                  :disabled="isSaving"
                  @click="saveAccountData"
                >
                  Сохранить изменения
                </VBtn>

                <VBtn
                  color="secondary"
                  variant="tonal"
                  type="reset"
                  @click.prevent="resetForm"
                >
                  Отменить
                </VBtn>
              </VCol>
            </VRow>
          </VForm>
        </VCardText>
      </VCard>
    </VCol>

    <VCol cols="12">
      <!-- 👉 Delete Account -->
      <VCard title="Удаление аккаунта">
        <VCardText>
          <!-- 👉 Checkbox and Button  -->
          <div>
            <VCheckbox
              v-model="isAccountDeactivated"
              :rules="validateAccountDeactivation"
              label="Я подтверждаю деактивацию моего аккаунта"
            />
          </div>

          <VBtn
            :disabled="!isAccountDeactivated"
            color="error"
            class="mt-6"
            @click="isConfirmDialogOpen = true"
          >
            Деактивировать аккаунт
          </VBtn>
        </VCardText>
      </VCard>
    </VCol>
  </VRow>

  <!-- Confirm Dialog -->
  <ConfirmDialog
    v-model:is-dialog-visible="isConfirmDialogOpen"
    confirmation-question="Вы уверены, что хотите деактивировать свой аккаунт?"
    confirm-title="Деактивировано!"
    confirm-msg="Ваш аккаунт был успешно деактивирован."
    cancel-title="Отменено"
    cancel-msg="Деактивация аккаунта отменена!"
  />
</template>
