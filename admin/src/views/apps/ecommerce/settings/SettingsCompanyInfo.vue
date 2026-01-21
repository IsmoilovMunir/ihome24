<script setup>
import { ref, onMounted } from 'vue'
import { $api } from '@/utils/api'

const isSubmitting = ref(false)
const isLoading = ref(false)

// Данные формы
const companyName = ref('')
const companyInn = ref('')
const companyOgrn = ref('')
const companyKpp = ref('')
const companyCountry = ref('Russia')
const legalAddress = ref('')
const actualAddress = ref('')
const phone = ref('')
const email = ref('')
const website = ref('')
const directorName = ref('')
const accountantName = ref('')
const bankName = ref('')
const bankAccount = ref('')
const correspondentAccount = ref('')
const bik = ref('')

const countries = [
  { title: 'Россия', value: 'Russia' },
  { title: 'Казахстан', value: 'Kazakhstan' },
  { title: 'Беларусь', value: 'Belarus' },
  { title: 'Украина', value: 'Ukraine' }
]

// Загрузка текущих настроек компании
const loadCompanySettings = async () => {
  try {
    isLoading.value = true
    const response = await $api('/admin/company-settings', {
      method: 'GET',
    })
    
    if (response) {
      companyName.value = response.name || ''
      companyInn.value = response.inn || ''
      companyOgrn.value = response.ogrn || ''
      companyKpp.value = response.kpp || ''
      companyCountry.value = response.country || 'Russia'
      legalAddress.value = response.legalAddress || ''
      actualAddress.value = response.actualAddress || ''
      phone.value = response.phone || ''
      email.value = response.email || ''
      website.value = response.website || ''
      directorName.value = response.directorName || ''
      accountantName.value = response.accountantName || ''
      bankName.value = response.bankName || ''
      bankAccount.value = response.bankAccount || ''
      correspondentAccount.value = response.correspondentAccount || ''
      bik.value = response.bik || ''
    }
  } catch (error) {
    console.error('Ошибка при загрузке настроек компании:', error)
    // Если настроек нет, это нормально - пользователь может их создать
  } finally {
    isLoading.value = false
  }
}

// Сохранение настроек компании
const saveCompanySettings = async () => {
  if (!companyName.value || !companyCountry.value) {
    alert('Пожалуйста, заполните обязательные поля: Название компании и Страна')
    return
  }

  try {
    isSubmitting.value = true

    const settingsData = {
      name: companyName.value,
      inn: companyInn.value || null,
      ogrn: companyOgrn.value || null,
      kpp: companyKpp.value || null,
      country: companyCountry.value,
      legalAddress: legalAddress.value || null,
      actualAddress: actualAddress.value || null,
      phone: phone.value || null,
      email: email.value || null,
      website: website.value || null,
      directorName: directorName.value || null,
      accountantName: accountantName.value || null,
      bankName: bankName.value || null,
      bankAccount: bankAccount.value || null,
      correspondentAccount: correspondentAccount.value || null,
      bik: bik.value || null,
    }

    await $api('/admin/company-settings', {
      method: 'POST',
      body: settingsData,
    })

    alert('Настройки компании успешно сохранены!')
  } catch (error) {
    console.error('Ошибка при сохранении настроек компании:', error)
    alert('Ошибка при сохранении настроек: ' + (error.data?.message || error.message || 'Неизвестная ошибка'))
  } finally {
    isSubmitting.value = false
  }
}

onMounted(() => {
  loadCompanySettings()
})
</script>

<template>
  <VCard>
    <VCardItem>
      <VCardTitle>Информация о компании</VCardTitle>
      <VCardSubtitle>
        Общая информация о компании, которая будет применяться ко всем товарам
      </VCardSubtitle>
    </VCardItem>

    <VDivider />

    <VCardText>
      <VForm @submit.prevent="saveCompanySettings">
        <VRow>
          <!-- Основная информация -->
          <VCol cols="12">
            <h6 class="text-h6 mb-4">
              Основная информация
            </h6>
          </VCol>

          <VCol
            cols="12"
            md="6"
          >
            <AppTextField
              v-model="companyName"
              label="Название компании"
              placeholder="ООО «Моя Компания»"
              required
            />
          </VCol>

          <VCol
            cols="12"
            md="6"
          >
            <AppSelect
              v-model="companyCountry"
              label="Страна"
              :items="countries"
              required
            />
          </VCol>

          <VCol
            cols="12"
            md="4"
          >
            <AppTextField
              v-model="companyInn"
              label="ИНН"
              placeholder="7701234567"
            />
          </VCol>

          <VCol
            cols="12"
            md="4"
          >
            <AppTextField
              v-model="companyOgrn"
              label="ОГРН"
              placeholder="1234567890123"
            />
          </VCol>

          <VCol
            cols="12"
            md="4"
          >
            <AppTextField
              v-model="companyKpp"
              label="КПП"
              placeholder="123456789"
            />
          </VCol>

          <!-- Адреса -->
          <VCol cols="12">
            <VDivider class="my-4" />
            <h6 class="text-h6 mb-4">
              Адреса
            </h6>
          </VCol>

          <VCol cols="12">
            <AppTextField
              v-model="legalAddress"
              label="Юридический адрес"
              placeholder="г. Москва, ул. Примерная, д. 1"
              rows="2"
            />
          </VCol>

          <VCol cols="12">
            <AppTextField
              v-model="actualAddress"
              label="Фактический адрес"
              placeholder="г. Москва, ул. Примерная, д. 1"
              rows="2"
            />
          </VCol>

          <!-- Контакты -->
          <VCol cols="12">
            <VDivider class="my-4" />
            <h6 class="text-h6 mb-4">
              Контактная информация
            </h6>
          </VCol>

          <VCol
            cols="12"
            md="6"
          >
            <AppTextField
              v-model="phone"
              label="Телефон"
              placeholder="+7 (999) 123-45-67"
            />
          </VCol>

          <VCol
            cols="12"
            md="6"
          >
            <AppTextField
              v-model="email"
              label="Email"
              placeholder="info@company.ru"
              type="email"
            />
          </VCol>

          <VCol cols="12">
            <AppTextField
              v-model="website"
              label="Веб-сайт"
              placeholder="https://www.company.ru"
            />
          </VCol>

          <!-- Руководство -->
          <VCol cols="12">
            <VDivider class="my-4" />
            <h6 class="text-h6 mb-4">
              Руководство
            </h6>
          </VCol>

          <VCol
            cols="12"
            md="6"
          >
            <AppTextField
              v-model="directorName"
              label="ФИО директора"
              placeholder="Иванов Иван Иванович"
            />
          </VCol>

          <VCol
            cols="12"
            md="6"
          >
            <AppTextField
              v-model="accountantName"
              label="ФИО главного бухгалтера"
              placeholder="Петрова Петра Петровна"
            />
          </VCol>

          <!-- Банковские реквизиты -->
          <VCol cols="12">
            <VDivider class="my-4" />
            <h6 class="text-h6 mb-4">
              Банковские реквизиты
            </h6>
          </VCol>

          <VCol cols="12">
            <AppTextField
              v-model="bankName"
              label="Название банка"
              placeholder="ПАО «Сбербанк»"
            />
          </VCol>

          <VCol
            cols="12"
            md="6"
          >
            <AppTextField
              v-model="bankAccount"
              label="Расчетный счет"
              placeholder="40702810123456789012"
            />
          </VCol>

          <VCol
            cols="12"
            md="6"
          >
            <AppTextField
              v-model="correspondentAccount"
              label="Корреспондентский счет"
              placeholder="30101810100000000593"
            />
          </VCol>

          <VCol cols="12">
            <AppTextField
              v-model="bik"
              label="БИК"
              placeholder="044525593"
            />
          </VCol>

          <!-- Кнопки -->
          <VCol cols="12">
            <VDivider class="my-4" />
            <div class="d-flex justify-end gap-4">
              <VBtn
                type="submit"
                :loading="isSubmitting"
                :disabled="isSubmitting"
              >
                Сохранить настройки
              </VBtn>
            </div>
          </VCol>
        </VRow>
      </VForm>
    </VCardText>
  </VCard>
</template>
