<script setup>
import CustomerOrderTable from './CustomerOrderTable.vue'

const props = defineProps({
  customerData: {
    type: Object,
    required: true,
  },
})

const totalSpent = computed(() => {
  const v = props.customerData?.totalSpent
  if (v == null || v === '') return 0
  return Number(v)
})

const ordersCount = computed(() => {
  const v = props.customerData?.order
  if (v == null || v === '') return 0
  return Number(v)
})
</script>

<template>
  <VRow>
    <VCol
      cols="12"
      md="6"
    >
      <VCard>
        <VCardText class="d-flex gap-y-2 flex-column">
          <VAvatar
            variant="tonal"
            color="primary"
            icon="tabler-currency-rubel"
            rounded
          />
          <h5 class="text-h5">
            Всего потрачено
          </h5>
          <div>
            <h5 class="text-h5 text-primary mb-1">
              {{ totalSpent.toLocaleString('ru-RU') }} ₽
            </h5>
            <p class="mb-0">
              Сумма по всем заказам клиента
            </p>
          </div>
        </VCardText>
      </VCard>
    </VCol>

    <VCol
      cols="12"
      md="6"
    >
      <VCard>
        <VCardText class="d-flex gap-y-2 flex-column">
          <VAvatar
            variant="tonal"
            color="success"
            icon="tabler-shopping-cart"
            rounded
          />
          <h5 class="text-h5">
            Заказы
          </h5>
          <div>
            <h5 class="text-h5 text-success mb-1">
              {{ ordersCount.toLocaleString('ru-RU') }}
              <span class="text-body-1 d-inline-block">всего заказов</span>
            </h5>
          </div>
        </VCardText>
      </VCard>
    </VCol>

    <VCol>
      <CustomerOrderTable
        :customer-email="props.customerData?.email"
        :customer-phone="props.customerData?.phone"
      />
    </VCol>
  </VRow>
</template>
