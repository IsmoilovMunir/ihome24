<template>
  <div class="space-y-3">
    <p class="text-sm text-gray-300 font-medium">
      Безналичный расчёт
    </p>

    <div
      v-if="enriching"
      class="flex items-center gap-2 py-2 text-sm text-gray-400"
      aria-live="polite"
    >
      <svg class="w-4 h-4 animate-spin text-[#F47327]" fill="none" viewBox="0 0 24 24" aria-hidden="true">
        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
      </svg>
      Загрузка реквизитов…
    </div>

    <template v-if="!selected && !enriching">
      <div
        ref="rootRef"
        class="relative transition-opacity duration-200"
        :class="showManual ? 'opacity-35 pointer-events-none select-none' : ''"
      >
        <label class="sr-only">Поиск организации</label>
        <div class="relative">
          <input
            v-model="query"
            type="text"
            autocomplete="off"
            class="w-full px-4 py-3 pr-10 bg-white/10 border border-white/20 rounded-xl text-white placeholder-gray-400 focus:ring-2 focus:ring-[#F47327] focus:border-transparent"
            placeholder="Название, адрес, ИНН или ОГРН"
            @focus="onFocus"
            @input="onInput"
          >
          <span class="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 pointer-events-none">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
          </span>
        </div>

        <div
          v-if="dropdownOpen && (suggestions.length > 0 || loading || (query.length >= 2 && !loading))"
          class="absolute z-30 left-0 right-0 mt-1 max-h-64 overflow-y-auto rounded-xl border border-white/20 bg-[#2E2826] shadow-xl"
        >
          <div v-if="loading" class="px-4 py-3 text-sm text-gray-400">
            Поиск…
          </div>
          <template v-else-if="suggestions.length === 0 && query.length >= 2">
            <div class="px-4 py-3 text-sm text-gray-400 border-b border-white/10">
              Ничего не найдено по запросу «{{ query.trim() }}»
            </div>
            <button
              type="button"
              class="w-full px-4 py-3 text-left text-sm font-medium text-[#F47327] hover:bg-white/10 transition-colors"
              @mousedown.prevent="openManualForm"
            >
              Заполнить реквизиты вручную
            </button>
          </template>
          <template v-else>
            <button
              v-for="(item, idx) in suggestions"
              :key="`${item.inn}-${idx}`"
              type="button"
              class="w-full text-left px-4 py-3 hover:bg-white/10 border-b border-white/5 last:border-0 transition-colors"
              @mousedown.prevent="selectCompany(item)"
            >
              <div class="text-white text-sm font-medium">
                {{ item.name }}
              </div>
              <div v-if="item.inn" class="text-gray-400 text-xs mt-0.5">
                ИНН {{ item.inn }}
                <span v-if="item.address"> · {{ item.address }}</span>
              </div>
            </button>
          </template>
        </div>
      </div>

      <p
        v-if="!dadataEnabled"
        class="text-xs text-amber-400/90 leading-relaxed transition-opacity duration-200"
        :class="showManual ? 'opacity-35' : ''"
      >
        {{ dadataMessage || 'Поиск по базе ЕГРЮЛ недоступен.' }}
      </p>

      <button
        type="button"
        class="w-full py-2.5 px-4 rounded-xl border border-white/20 text-sm text-gray-300 hover:border-[#F47327]/50 hover:text-white transition-colors duration-200"
        :class="showManual ? 'opacity-35 pointer-events-none' : ''"
        @click="openManualForm"
      >
        {{ dadataEnabled ? 'Заполнить реквизиты вручную' : 'Указать реквизиты организации' }}
      </button>
    </template>

    <Teleport to="body">
      <Transition name="company-form-fade">
        <div
          v-if="showManual"
          class="fixed inset-0 z-[200] flex items-end sm:items-center justify-center"
          role="dialog"
          aria-modal="true"
          aria-labelledby="cashless-form-title"
        >
          <div
            class="absolute inset-0 bg-[#1a1614]/75 backdrop-blur-[2px]"
            aria-hidden="true"
            @click="cancelManual"
          />
          <form
            id="cashless-transfer-form"
            class="relative z-10 w-full sm:max-w-lg max-h-[min(92vh,720px)] overflow-y-auto space-y-3 p-4 sm:p-6 rounded-t-2xl sm:rounded-2xl bg-[#3A3331] border border-white/15 shadow-2xl"
            @submit.prevent="saveManual"
            @click.stop
          >
        <p id="cashless-form-title" class="text-sm font-medium text-white pr-8">
          {{ editingSnapshot ? 'Редактирование реквизитов' : 'Реквизиты организации' }}
        </p>
        <button
          type="button"
          class="absolute top-4 right-4 p-1 text-gray-400 hover:text-white rounded-lg"
          aria-label="Закрыть"
          @click="cancelManual"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>

        <div>
          <label class="block text-sm font-medium text-gray-300 mb-1">
            Наименование <span class="text-[#F47327]">*</span>
          </label>
          <textarea
            ref="formNameRef"
            v-model="manual.name"
            rows="2"
            class="w-full px-4 py-2 bg-white/10 border border-white/20 rounded-lg text-white placeholder-gray-400 focus:ring-2 focus:ring-[#F47327] focus:border-transparent resize-y min-h-[50px]"
            placeholder="ООО «Пример»"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-300 mb-1">
            Адрес <span class="text-[#F47327]">*</span>
          </label>
          <textarea
            v-model="manual.address"
            rows="2"
            class="w-full px-4 py-2 bg-white/10 border border-white/20 rounded-lg text-white placeholder-gray-400 focus:ring-2 focus:ring-[#F47327] focus:border-transparent resize-y min-h-[72px]"
            placeholder="356800, край, р-н, г …"
          />
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-1">
              ОГРН <span class="text-[#F47327]">*</span>
            </label>
            <input
              v-model="manual.ogrn"
              type="text"
              inputmode="numeric"
              maxlength="15"
              class="w-full px-4 py-2 bg-white/10 border border-white/20 rounded-lg text-white font-mono tabular-nums placeholder-gray-400 focus:ring-2 focus:ring-[#F47327] focus:border-transparent"
              placeholder="13 или 15 цифр"
            >
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-1">
              ИНН <span class="text-[#F47327]">*</span>
            </label>
            <input
              v-model="manual.inn"
              type="text"
              inputmode="numeric"
              maxlength="12"
              class="w-full px-4 py-2 bg-white/10 border border-white/20 rounded-lg text-white font-mono tabular-nums placeholder-gray-400 focus:ring-2 focus:ring-[#F47327] focus:border-transparent"
              placeholder="10 или 12 цифр"
            >
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-1">Корр.сч</label>
            <input
              v-model="manual.corrAccount"
              type="text"
              inputmode="numeric"
              maxlength="20"
              class="w-full px-4 py-2 bg-white/10 border border-white/20 rounded-lg text-white font-mono tabular-nums placeholder-gray-400 focus:ring-2 focus:ring-[#F47327] focus:border-transparent"
              placeholder="20 цифр"
            >
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-1">БИК</label>
            <input
              v-model="manual.bik"
              type="text"
              inputmode="numeric"
              maxlength="9"
              class="w-full px-4 py-2 bg-white/10 border border-white/20 rounded-lg text-white font-mono tabular-nums placeholder-gray-400 focus:ring-2 focus:ring-[#F47327] focus:border-transparent"
              placeholder="9 цифр"
            >
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-1">р/с</label>
            <input
              v-model="manual.settlementAccount"
              type="text"
              inputmode="numeric"
              maxlength="20"
              class="w-full px-4 py-2 bg-white/10 border border-white/20 rounded-lg text-white font-mono tabular-nums placeholder-gray-400 focus:ring-2 focus:ring-[#F47327] focus:border-transparent"
              placeholder="20 цифр"
            >
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-1">КПП</label>
            <input
              v-model="manual.kpp"
              type="text"
              inputmode="numeric"
              maxlength="9"
              class="w-full px-4 py-2 bg-white/10 border border-white/20 rounded-lg text-white font-mono tabular-nums placeholder-gray-400 focus:ring-2 focus:ring-[#F47327] focus:border-transparent"
              placeholder="9 цифр"
            >
          </div>
          <div class="sm:col-span-2">
            <label class="block text-sm font-medium text-gray-300 mb-1">
              ОКПО
            </label>
            <input
              v-model="manual.okpo"
              type="text"
              inputmode="numeric"
              maxlength="10"
              class="w-full px-4 py-2 bg-white/10 border border-white/20 rounded-lg text-white font-mono tabular-nums placeholder-gray-400 focus:ring-2 focus:ring-[#F47327] focus:border-transparent"
              placeholder="8 или 10 цифр"
            >
          </div>
        </div>

        <p v-if="manualError" class="text-sm text-red-400">
          {{ manualError }}
        </p>
        <div class="flex flex-wrap gap-2 pt-1">
          <button
            type="submit"
            class="flex-1 min-w-[140px] py-2.5 px-4 rounded-xl bg-[#F47327] text-white text-sm font-semibold hover:bg-[#F47327]/90 transition-colors"
          >
            Сохранить
          </button>
          <button
            type="button"
            class="py-2.5 px-4 rounded-xl border border-white/20 text-sm text-gray-300 hover:text-white transition-colors"
            @click="cancelManual"
          >
            Отмена
          </button>
        </div>
          </form>
        </div>
      </Transition>
    </Teleport>

    <template v-if="selected">
      <div class="p-4 rounded-xl bg-white/5 border border-[#F47327]/40 space-y-2">
        <div class="text-white font-medium text-sm whitespace-pre-wrap">
          {{ selected.name }}
        </div>
        <div v-if="display.address" class="flex gap-2 text-sm">
          <span class="text-gray-400 shrink-0">Адрес:</span>
          <span class="text-gray-200">{{ display.address }}</span>
        </div>
        <div v-if="display.ogrn" class="flex gap-2 text-sm">
          <span class="text-gray-400 shrink-0">ОГРН:</span>
          <span class="text-gray-200 font-mono tabular-nums">{{ display.ogrn }}</span>
        </div>
        <div v-if="display.inn" class="flex gap-2 text-sm">
          <span class="text-gray-400 shrink-0">ИНН:</span>
          <span class="text-gray-200 font-mono tabular-nums">{{ display.inn }}</span>
        </div>
        <div v-if="display.corrAccount" class="flex gap-2 text-sm">
          <span class="text-gray-400 shrink-0">Корр.сч:</span>
          <span class="text-gray-200 font-mono tabular-nums">{{ display.corrAccount }}</span>
        </div>
        <div v-if="display.bik" class="flex gap-2 text-sm">
          <span class="text-gray-400 shrink-0">БИК:</span>
          <span class="text-gray-200 font-mono tabular-nums">{{ display.bik }}</span>
        </div>
        <div v-if="display.settlementAccount" class="flex gap-2 text-sm">
          <span class="text-gray-400 shrink-0">р/с:</span>
          <span class="text-gray-200 font-mono tabular-nums">{{ display.settlementAccount }}</span>
        </div>
        <div v-if="display.kpp" class="flex gap-2 text-sm">
          <span class="text-gray-400 shrink-0">КПП:</span>
          <span class="text-gray-200 font-mono tabular-nums">{{ display.kpp }}</span>
        </div>
        <div v-if="display.okpo" class="flex gap-2 text-sm">
          <span class="text-gray-400 shrink-0">ОКПО:</span>
          <span class="text-gray-200 font-mono tabular-nums">{{ display.okpo }}</span>
        </div>
        <button
          type="button"
          class="text-sm text-[#F47327] hover:underline mt-2"
          @click="startEdit"
        >
          Редактировать информацию
        </button>
      </div>
    </template>

    <p class="text-xs text-gray-400 leading-relaxed">
      После оформления на email придут реквизиты для оплаты. Отгрузка — после поступления средств на расчётный счёт.
    </p>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { companyApi } from '~/utils/api'
import {
  normalizeCompanyParty,
  partyToManual,
  manualToParty,
  validateCompanyParty,
} from '~/utils/companyFormat'

const props = defineProps({
  modelValue: {
    type: Object,
    default: null,
  },
})

const emit = defineEmits(['update:modelValue'])

const rootRef = ref(null)
const query = ref('')
const suggestions = ref([])
const loading = ref(false)
const dropdownOpen = ref(false)
const dadataEnabled = ref(true)
const dadataMessage = ref('')
const showManual = ref(false)
const enriching = ref(false)
const manualError = ref('')
const formNameRef = ref(null)
let debounceTimer = null

const editingSnapshot = ref(null)

function emptyManual() {
  return {
    name: '',
    address: '',
    ogrn: '',
    inn: '',
    corrAccount: '',
    bik: '',
    settlementAccount: '',
    kpp: '',
    okpo: '',
  }
}

const manual = ref(emptyManual())

const selected = ref(props.modelValue)

const display = computed(() => normalizeCompanyParty(selected.value) || {})

watch(() => props.modelValue, (v) => {
  selected.value = v
})

watch(selected, (v) => {
  emit('update:modelValue', v)
})

function prefillManualFromQuery() {
  const q = query.value?.trim() || ''
  if (!q) return
  const digits = q.replace(/\D/g, '')
  if (digits.length === 10 || digits.length === 12) {
    manual.value.inn = digits
  } else if (digits.length === 13 || digits.length === 15) {
    manual.value.ogrn = digits
  } else if (!manual.value.name) {
    manual.value.name = q
  }
}

function openManualForm() {
  editingSnapshot.value = null
  dropdownOpen.value = false
  manualError.value = ''
  prefillManualFromQuery()
  showManual.value = true
  nextTick(() => formNameRef.value?.focus())
}

watch(showManual, (open) => {
  if (!import.meta.client) return
  document.body.style.overflow = open ? 'hidden' : ''
})

function startEdit() {
  const c = normalizeCompanyParty(selected.value)
  if (!c) return
  editingSnapshot.value = { ...c }
  manual.value = partyToManual(c)
  selected.value = null
  query.value = c.name || ''
  showManual.value = true
  dropdownOpen.value = false
  manualError.value = ''
  emit('update:modelValue', null)
  nextTick(() => formNameRef.value?.focus())
}

function cancelManual() {
  showManual.value = false
  manualError.value = ''
  if (editingSnapshot.value) {
    selected.value = editingSnapshot.value
    query.value = selected.value.name || ''
    emit('update:modelValue', editingSnapshot.value)
    editingSnapshot.value = null
    manual.value = emptyManual()
    return
  }
  manual.value = emptyManual()
  if (!selected.value) {
    emit('update:modelValue', null)
  }
}

function applyParty(party) {
  const normalized = normalizeCompanyParty(party)
  selected.value = normalized
  query.value = normalized?.name || ''
  showManual.value = false
  editingSnapshot.value = null
  manual.value = emptyManual()
  dropdownOpen.value = false
  suggestions.value = []
  manualError.value = ''
  emit('update:modelValue', normalized)
}

function openManualWithParty(party) {
  manual.value = partyToManual(party)
  selected.value = null
  query.value = manual.value.name || ''
  showManual.value = true
  dropdownOpen.value = false
  suggestions.value = []
  emit('update:modelValue', null)
  nextTick(() => formNameRef.value?.focus())
}

async function enrichPartyFromDadata(party) {
  if (!dadataEnabled.value || !party?.inn) {
    return party
  }
  try {
    const res = await companyApi.findByInn(party.inn)
    const full = res?.data ?? res
    if (full && (full.inn || full.name)) {
      return normalizeCompanyParty({ ...party, ...full })
    }
  } catch {
    /* фоновая догрузка необязательна */
  }
  return party
}

function saveManual() {
  manualError.value = ''
  const party = manualToParty(manual.value)
  const err = validateCompanyParty(party)
  if (err) {
    manualError.value = err
    return
  }
  applyParty(party)
}

function onFocus() {
  dropdownOpen.value = true
  if (query.value.length >= 2) {
    fetchSuggestions(query.value)
  }
}

function onInput() {
  dropdownOpen.value = true
  if (debounceTimer) clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => {
    fetchSuggestions(query.value)
  }, 300)
}

async function fetchSuggestions(q) {
  const trimmed = q?.trim() || ''
  if (trimmed.length < 2) {
    suggestions.value = []
    return
  }
  loading.value = true
  try {
    const res = await companyApi.suggest(trimmed)
    const data = res?.data ?? res
    dadataEnabled.value = data?.enabled !== false
    dadataMessage.value = data?.message || ''
    suggestions.value = data?.suggestions ?? []
  } catch {
    suggestions.value = []
  } finally {
    loading.value = false
  }
}

async function selectCompany(item) {
  editingSnapshot.value = null
  dropdownOpen.value = false
  suggestions.value = []
  manualError.value = ''

  let party = normalizeCompanyParty({ ...item })
  enriching.value = true
  try {
    party = await enrichPartyFromDadata(party)
  } finally {
    enriching.value = false
  }

  if (!validateCompanyParty(party)) {
    openManualWithParty(party)
    return
  }
  applyParty(party)
}

function onClickOutside(e) {
  if (rootRef.value && !rootRef.value.contains(e.target)) {
    dropdownOpen.value = false
  }
}

async function checkDadataStatus() {
  try {
    const res = await companyApi.suggest('аа')
    const data = res?.data ?? res
    dadataEnabled.value = data?.enabled !== false
    dadataMessage.value = data?.message || ''
  } catch {
    dadataEnabled.value = false
    dadataMessage.value = 'Не удалось связаться с сервером. Заполните реквизиты вручную.'
  }
}

onMounted(() => {
  document.addEventListener('click', onClickOutside)
  checkDadataStatus()
})

onUnmounted(() => {
  document.removeEventListener('click', onClickOutside)
  if (debounceTimer) clearTimeout(debounceTimer)
  if (import.meta.client) {
    document.body.style.overflow = ''
  }
})

defineExpose({
  validate() {
    if (enriching.value) {
      return 'Подождите, загружаются реквизиты организации'
    }
    if (selected.value) {
      return validateCompanyParty(selected.value)
    }
    if (showManual.value) {
      return 'Нажмите «Сохранить» после заполнения реквизитов организации'
    }
    return 'Укажите организацию для оплаты по счёту'
  },
})
</script>

<style scoped>
.company-form-fade-enter-active,
.company-form-fade-leave-active {
  transition: opacity 0.2s ease;
}
.company-form-fade-enter-active form,
.company-form-fade-leave-active form {
  transition: transform 0.25s ease, opacity 0.2s ease;
}
.company-form-fade-enter-from,
.company-form-fade-leave-to {
  opacity: 0;
}
.company-form-fade-enter-from form,
.company-form-fade-leave-to form {
  transform: translateY(1rem);
  opacity: 0;
}
@media (min-width: 640px) {
  .company-form-fade-enter-from form,
  .company-form-fade-leave-to form {
    transform: translateY(0.5rem) scale(0.98);
  }
}
</style>
