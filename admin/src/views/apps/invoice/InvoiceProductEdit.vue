<!-- eslint-disable vue/no-mutating-props -->
<script setup>
const props = defineProps({
  id: {
    type: Number,
    required: true,
  },
  data: {
    type: Object,
    required: true,
    default: () => ({
      title: 'Дизайн приложения',
      cost: 24,
      hours: 1,
      description: 'Дизайн UI kit и страниц приложения.',
    }),
  },
})

const emit = defineEmits([
  'removeProduct',
  'totalAmount',
])

const itemsOptions = [
  {
    title: 'Дизайн приложения',
    cost: 24,
    hours: 1,
    description: 'Дизайн UI kit и страниц приложения.',
  },
  {
    title: 'Кастомизация',
    cost: 26,
    hours: 1,
    description: 'Доработка и исправление ошибок.',
  },
  {
    title: 'Шаблон',
    cost: 28,
    hours: 1,
    description: 'Админ-шаблон.',
  },
  {
    title: 'Разработка приложения',
    cost: 32,
    hours: 1,
    description: 'Создание нативного приложения.',
  },
]

const selectedItem = ref('Кастомизация')
const localProductData = ref(structuredClone(toRaw(props.data)))

watch(selectedItem, () => {
  const item = itemsOptions.filter(obj => {
    return obj.title === selectedItem.value
  })

  localProductData.value = item[0]
})

const removeProduct = () => {
  emit('removeProduct', props.id)
}

const totalPrice = computed(() => Number(localProductData.value.cost) * Number(localProductData.value.hours))

watch(totalPrice, () => {
  emit('totalAmount', totalPrice.value)
}, { immediate: true })
</script>

<template>
  <!-- eslint-disable vue/no-mutating-props -->
  <div class="add-products-header mb-2 d-none d-md-flex mb-4">
    <VRow class="me-10">
      <VCol
        cols="12"
        md="6"
      >
        <h6 class="text-h6">
          Товар
        </h6>
      </VCol>
      <VCol
        cols="12"
        md="2"
      >
        <h6 class="text-h6 ps-2">
          Стоимость
        </h6>
      </VCol>
      <VCol
        cols="12"
        md="2"
      >
        <h6 class="text-h6 ps-2">
          Часы
        </h6>
      </VCol>
      <VCol
        cols="12"
        md="2"
      >
        <h6 class="text-h6">
          Цена
        </h6>
      </VCol>
    </VRow>
  </div>

  <VCard
    flat
    border
    class="d-flex flex-sm-row flex-column-reverse"
  >
    <!-- 👉 Left Form -->
    <div class="pa-6 flex-grow-1">
      <VRow>
        <VCol
          cols="12"
          md="6"
        >
          <AppSelect
            id="item"
            v-model="selectedItem"
            :items="itemsOptions"
            item-title="title"
            item-value="title"
            placeholder="Выберите товар"
            class="mb-6"
          />

          <AppTextarea
            id="item-description"
            v-model="localProductData.description"
            rows="2"
            placeholder="Описание товара"
            persistent-placeholder
          />
        </VCol>
        <VCol
          cols="12"
          md="2"
          sm="4"
        >
          <AppTextField
            id="item-cost"
            v-model="localProductData.cost"
            type="number"
            placeholder="Стоимость"
            class="mb-6"
          />

          <div class="text-high-emphasis text-no-wrap mt-4">
            <p class="mb-1">
              Скидка
            </p>
            <span>0%</span>
            <span class="mx-2">
              0%
              <VTooltip activator="parent">НДС 1</VTooltip>
            </span>
            <span>
              0%
              <VTooltip activator="parent">НДС 2</VTooltip>
            </span>
          </div>
        </VCol>
        <VCol
          cols="12"
          md="2"
          sm="4"
        >
          <AppTextField
            id="item-hours"
            v-model="localProductData.hours"
            type="number"
            placeholder="5"
          />
        </VCol>
        <VCol
          cols="12"
          md="2"
          sm="4"
        >
          <p class="my-2">
            <span class="d-inline d-md-none">Цена: </span>
            <span class="text-high-emphasis">${{ totalPrice }}</span>
          </p>
        </VCol>
      </VRow>
    </div>

    <!-- 👉 Item Actions -->
    <div
      class="d-flex flex-column align-end item-actions"
      :class="$vuetify.display.smAndUp ? 'border-s' : 'border-b' "
    >
      <IconBtn
        size="36"
        @click="removeProduct"
      >
        <VIcon
          :size="24"
          icon="tabler-x"
        />
      </IconBtn>
    </div>
  </VCard>
</template>
