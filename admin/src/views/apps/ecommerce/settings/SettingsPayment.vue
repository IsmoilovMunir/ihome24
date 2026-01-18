<script setup>
import { ref } from 'vue'
import paypal from '@images/cards/paypal-primary.png'

const isAddPaymentMethodsDialogVisible = ref(false)
const isPaymentProvidersDialogVisible = ref(false)
</script>

<template>
  <div>
    <!-- 👉 Payment Providers  -->
    <VCard
      class="mb-6"
      title="Провайдеры платежей"
    >
      <VCardText>
        <div class="text-body-1 mb-5">
          Провайдеры, которые позволяют принимать платежи по ставке, установленной третьей стороной. Дополнительная комиссия будет применяться к новым заказам после выбора тарифа.
        </div>
        <VBtn
          variant="tonal"
          @click="isPaymentProvidersDialogVisible = !isPaymentProvidersDialogVisible"
        >
          Выбрать провайдера
        </VBtn>
      </VCardText>
    </VCard>

    <!-- 👉 Supported Payment Methods -->
    <VCard
      title="Поддерживаемые способы оплаты"
      subtitle="Способы оплаты, доступные с одним из одобренных платежных провайдеров ihome."
      class="mb-6"
    >
      <VCardText>
        <h6 class="text-h6 mb-5">
          По умолчанию
        </h6>
        <div class="my-class mb-5">
          <div class="d-flex justify-space-between align-center mb-6">
            <div class="rounded paypal-logo">
              <img
                :src="paypal"
                alt="Pixinvent"
                style="padding-block: 6px;padding-inline: 18px;"
              >
            </div>

            <VBtn variant="text">
              Активировать PayPal
            </VBtn>
          </div>
          <VDivider />
          <div class="d-flex justify-space-between flex-wrap mt-6 gap-4">
            <div>
              <div
                class="text-body-2 mb-2"
                style="min-inline-size: 220px;"
              >
                Провайдер
              </div>
              <h6 class="text-h6">
                PayPal
              </h6>
            </div>

            <div>
              <div
                class="text-body-2 mb-2"
                style="min-inline-size: 220px;"
              >
                Статус
              </div>
              <VChip
                color="warning"
                size="small"
                label
              >
                Неактивен
              </VChip>
            </div>

            <div>
              <div
                class="text-body-2 mb-2"
                style="min-inline-size: 220px;"
              >
                Комиссия за транзакцию
              </div>
              <h6 class="text-h6">
                2.99%
              </h6>
            </div>
          </div>
        </div>
        <VBtn
          variant="tonal"
          @click="isAddPaymentMethodsDialogVisible = !isAddPaymentMethodsDialogVisible"
        >
          Добавить способ оплаты
        </VBtn>
      </VCardText>
    </VCard>

    <!-- 👉 Manual Payment Methods -->
    <VCard
      title="Ручные способы оплаты"
      class="mb-6"
    >
      <VCardText>
        <p>Платежи, которые осуществляются вне вашего интернет-магазина. Когда клиент выбирает ручной способ оплаты, например, наложенный платеж, вам нужно будет подтвердить его заказ перед выполнением.</p>

        <VBtn
          variant="tonal"
          :append-icon="$vuetify.display.smAndUp ? 'tabler-chevron-down' : ''"
        >
          Добавить ручной способ оплаты

          <VMenu activator="parent">
            <VList>
              <VListItem
                v-for="(item, index) in ['Создать пользовательский способ оплаты', 'Банковский депозит', 'Денежный перевод', 'Наложенный платеж (НП)']"
                :key="index"
                :value="index"
              >
                <VListItemTitle>{{ item }}</VListItemTitle>
              </VListItem>
            </VList>
          </VMenu>
        </VBtn>
      </VCardText>
    </VCard>

    <div class="d-flex justify-end gap-x-4">
      <VBtn
        color="secondary"
        variant="tonal"
      >
        Отменить
      </VBtn>
      <VBtn color="primary">
        Сохранить изменения
      </VBtn>
    </div>
  </div>

  <AddPaymentMethodDialog v-model:is-dialog-visible="isAddPaymentMethodsDialogVisible" />
  <PaymentProvidersDialog v-model:is-dialog-visible="isPaymentProvidersDialogVisible" />
</template>

<style lang="scss" scoped>
.paypal-logo {
  background-color: #fff;
  block-size: 37px;
  box-shadow: 0 2px 4px 0 rgba(165, 163, 174, 30%);
  inline-size: 58px;
}
</style>
