<script setup>
import { ref, onMounted } from 'vue'
import { $api } from '@/utils/api'

const tiers = ref([])
const loading = ref(false)
const saving = ref(false)
const error = ref(null)

async function load() {
  loading.value = true
  error.value = null
  try {
    const data = await $api('/admin/company-settings/price-tiers')
    if (data?.tiers?.length) {
      tiers.value = data.tiers.map(t => ({
        minQty: t.minQty ?? 1,
        maxQty: t.maxQty ?? null,
        discountPercent: t.discountPercent != null ? Number(t.discountPercent) : 0,
        label: t.label || '',
      }))
    } else {
      tiers.value = [
        { minQty: 1, maxQty: 10, discountPercent: 0, label: 'Розничная' },
        { minQty: 11, maxQty: 100, discountPercent: 10, label: 'Мелкий опт' },
        { minQty: 101, maxQty: null, discountPercent: 15, label: 'Крупный опт' },
      ]
    }
  } catch (e) {
    error.value = e?.data?.message || e?.message || 'Не удалось загрузить настройки'
  } finally {
    loading.value = false
  }
}

function addTier() {
  const maxMin = Math.max(0, ...tiers.value.map(t => t.minQty || 0))
  tiers.value.push({
    minQty: maxMin + 1,
    maxQty: null,
    discountPercent: 0,
    label: '',
  })
}

function removeTier(index) {
  if (tiers.value.length <= 1) return
  tiers.value.splice(index, 1)
}

async function save() {
  const normalized = tiers.value.map(t => ({
    minQty: Number(t.minQty) || 0,
    maxQty: t.maxQty !== '' && t.maxQty !== null ? Number(t.maxQty) : null,
    discountPercent: Number(t.discountPercent) || 0,
    label: String(t.label || '').trim() || 'Уровень',
  }))
  const invalid = normalized.find(t => t.minQty < 0 || (t.maxQty != null && t.maxQty < t.minQty))
  if (invalid) {
    error.value = 'Проверьте диапазоны: мин. количество ≥ 0, макс. ≥ мин. (или пусто для «без границы»)'
    return
  }
  saving.value = true
  error.value = null
  try {
    await $api('/admin/company-settings/price-tiers', {
      method: 'PUT',
      body: { tiers: normalized },
    })
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
      <i
        class="tabler-chart-line text-primary"
        style="font-size: 20px;"
      />
      <span>Ценовые уровни (объёмные скидки)</span>
    </VCardTitle>
    <VCardText>
      <p class="mb-4 text-body-2 text-medium-emphasis">
        Настройте уровни цен в зависимости от количества: розничная цена, мелкий опт и крупный опт.
        Укажите диапазон количества (от–до) и процент скидки от базовой цены. На фронте при выборе количества
        автоматически будет применяться нужный уровень и пересчитываться сумма.
      </p>

      <VAlert
        v-if="error"
        type="error"
        density="comfortable"
        class="mb-4"
      >
        {{ error }}
      </VAlert>

      <div class="mb-4">
        <div
          v-for="(tier, index) in tiers"
          :key="index"
          class="d-flex flex-wrap align-center gap-3 mb-3 pa-3 rounded border"
        >
          <VTextField
            v-model.number="tier.minQty"
            label="От кол-ва"
            type="number"
            min="0"
            style="max-width: 120px;"
            density="compact"
          />
          <span class="text-medium-emphasis">–</span>
          <VTextField
            v-model="tier.maxQty"
            label="До кол-ва"
            type="number"
            min="0"
            placeholder="∞"
            style="max-width: 120px;"
            density="compact"
          />
          <VTextField
            v-model.number="tier.discountPercent"
            label="Скидка %"
            type="number"
            min="0"
            max="100"
            step="0.5"
            style="max-width: 100px;"
            density="compact"
          />
          <VTextField
            v-model="tier.label"
            label="Название"
            placeholder="Розничная / Мелкий опт / Крупный опт"
            style="min-width: 160px; flex: 1;"
            density="compact"
          />
          <VBtn
            icon
            variant="text"
            color="error"
            size="small"
            :disabled="tiers.length <= 1"
            @click="removeTier(index)"
          >
            <i class="tabler-trash" />
          </VBtn>
        </div>
        <VBtn
          variant="tonal"
          color="secondary"
          size="small"
          class="mb-2"
          @click="addTier"
        >
          <i class="tabler-plus me-1" />
          Добавить уровень
        </VBtn>
      </div>

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
