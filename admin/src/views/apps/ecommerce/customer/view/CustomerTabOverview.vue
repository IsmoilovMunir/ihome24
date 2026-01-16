<script setup>
import CustomerOrderTable from './CustomerOrderTable.vue'
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
            icon="tabler-currency-dollar"
            rounded
          />
          <h5 class="text-h5">
            Баланс счета
          </h5>
          <div>
            <h5 class="text-h5 text-primary mb-1">
              $7480
              <span class="text-body-1 d-inline-block">Остаток кредита</span>
            </h5>
            <p class="mb-0">
              Баланс счета для следующей покупки
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
            icon="tabler-gift"
            rounded
          />
          <h5 class="text-h5">
            Loyalty Program
          </h5>
          <div>
            <VChip
              color="success"
              class="mb-2"
              label
              size="small"
            >
              Platinum member
            </VChip>
            <p class="mb-0">
              3000 points to next tier
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
            color="warning"
            icon="tabler-star"
            rounded
          />
          <h5 class="text-h5">
            Список желаний
          </h5>
          <div>
            <h5 class="text-h5 text-warning mb-1">
              15
              <span class="text-body-1 d-inline-block">Товаров в списке желаний</span>
            </h5>
            <p class="mb-0">
              Получать уведомления, когда товары поступят в продажу
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
            color="info"
            icon="tabler-discount"
            rounded
          />
          <h5 class="text-h5">
            Купоны
          </h5>
          <div>
            <h5 class="text-h5 text-info mb-1">
              21
              <span class="text-body-1 d-inline-block">Выигранных купонов</span>
            </h5>
            <p class="mb-0">
              Использовать купон при следующей покупке
            </p>
          </div>
        </VCardText>
      </VCard>
    </VCol>

    <VCol>
      <CustomerOrderTable />
    </VCol>
  </VRow>
</template>
