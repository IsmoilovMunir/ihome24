<script setup>
import { ref, onMounted } from 'vue'
import { $api } from '@/utils/api'

const rate = ref('')
const percentAdjustment = ref('0')
const loading = ref(false)
const saving = ref(false)
const error = ref(null)

async function load() {
  loading.value = true
  error.value = null
  try {
    const data = await $api('/admin/company-settings/currency')
    if (data) {
      rate.value = data.currencyRate != null ? String(data.currencyRate) : ''
      percentAdjustment.value = data.currencyPercentAdjustment != null ? String(data.currencyPercentAdjustment) : '0'
    }
  } catch (e) {
    error.value = e?.data?.message || e?.message || 'Не удалось загрузить настройки'
  } finally {
    loading.value = false
  }
}

async function save() {
  const rateNum = parseFloat(rate.value)
  const percentNum = parseFloat(percentAdjustment.value)
  if (Number.isNaN(rateNum) || rateNum <= 0) {
    error.value = 'Укажите корректный курс валюты (число больше 0)'
    return
  }
  if (Number.isNaN(percentNum) || percentNum < -100 || percentNum > 100) {
    error.value = 'Процент коррекции должен быть от -100 до 100'
    return
  }
  saving.value = true
  error.value = null
  try {
    await $api('/admin/company-settings/currency', {
      method: 'PUT',
      body: {
        currencyRate: rateNum,
        currencyPercentAdjustment: percentNum,
      },
    })
    // Можно показать уведомление об успехе
  } catch (e) {
    error.value = e?.data?.message || e?.message || 'Не удалось сохранить настройки'
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<template>
  <VCard>
    <VCardTitle class="text-h6 d-flex align-center gap-2">
      <i class="bi bi-currency-dollar text-primary" style="font-size: 20px;"></i>
      <span>Курс валюты</span>
    </VCardTitle>
    <VCardText>
      <p class="mb-4 text-body-2 text-medium-emphasis">
        Укажите текущий курс валюты (например, 1 $ = 80 ₽ — введите 80) и при необходимости процент коррекции.
        При росте доллара укажите положительный % (например, +2), при снижении — отрицательный (например, -1.5).
        Значение 0 — без изменений. Процент применяется ко всем товарам.
      </p>

      <VAlert
        v-if="error"
        type="error"
        density="comfortable"
        class="mb-4"
      >
        {{ error }}
      </VAlert>

      <VRow>
        <VCol
          cols="12"
          md="6"
        >
          <VTextField
            v-model="rate"
            label="Курс валюты"
            placeholder="Например, 80"
            type="number"
            min="0"
            step="0.01"
            :loading="loading"
            :disabled="loading"
          />
        </VCol>
        <VCol
          cols="12"
          md="6"
        >
          <VTextField
            v-model="percentAdjustment"
            label="Изменение в %"
            placeholder="0 — без изменений, +2 или -1.5"
            type="number"
            step="0.01"
            :loading="loading"
            :disabled="loading"
          />
        </VCol>
      </VRow>

      <div class="mt-4 d-flex gap-2">
        <VBtn
          color="primary"
          :loading="saving"
          :disabled="loading"
          @click="save"
        >
          Сохранить
        </VBtn>
        <VBtn
          variant="tonal"
          color="secondary"
          :disabled="loading"
          @click="load"
        >
          Обновить
        </VBtn>
      </div>
    </VCardText>
  </VCard>
</template>
