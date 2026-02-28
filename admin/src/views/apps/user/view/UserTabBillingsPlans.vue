<script setup>
import americanExpress from '@images/icons/payments/american-express.png'
import mastercard from '@images/icons/payments/mastercard.png'
import visa from '@images/icons/payments/visa.png'

const isUpgradePlanDialogVisible = ref(false)
const currentCardDetails = ref()
const isCardEditDialogVisible = ref(false)
const isCardAddDialogVisible = ref(false)
const isEditAddressDialogVisible = ref(false)

const openEditCardDialog = cardDetails => {
  currentCardDetails.value = cardDetails
  isCardEditDialogVisible.value = true
}

const creditCards = [
  {
    name: 'Tom McBride',
    number: '4851234567899865',
    expiry: '12/24',
    isPrimary: true,
    isExpired: false,
    type: 'mastercard',
    cvv: '123',
    image: mastercard,
  },
  {
    name: 'Mildred Wagner',
    number: '5531234567895678',
    expiry: '02/24',
    isPrimary: false,
    isExpired: false,
    type: 'visa',
    cvv: '456',
    image: visa,
  },
  {
    name: 'Lester Jennings',
    number: '5531234567890002',
    expiry: '08/20',
    isPrimary: false,
    isExpired: true,
    type: 'visa',
    cvv: '456',
    image: americanExpress,
  },
]

const currentAddress = {
  companyName: 'Pixinvent',
  billingEmail: 'gertrude@gmail.com',
  taxID: 'TAX-875623',
  vatNumber: 'SDF754K77',
  address: '100 Water Plant Avenue, Building 1303 Wake Island',
  contact: '+1(609) 933-44-22',
  country: 'Россия',
  state: 'Queensland',
  zipCode: 403114,
}

const currentBillingAddress = {
  firstName: 'Shamus',
  lastName: 'Tuttle',
  selectedCountry: 'Россия',
  addressLine1: '45 Rocker Terrace',
  addressLine2: 'Latheronwheel',
  landmark: 'KW5 8NW, London',
  contact: '+1 (609) 972-22-22',
  country: 'Россия',
  city: 'London',
  state: 'London',
  zipCode: 110001,
}
</script>

<template>
  <VRow>
    <!-- 👉 Current Plan -->
    <VCol cols="12">
      <VCard title="Текущий план">
        <VCardText>
          <VRow>
            <VCol
              cols="12"
              md="6"
              order-md="1"
              order="2"
            >
              <h6 class="text-h6 mb-1">
                Ваш текущий план - Базовый
              </h6>
              <p>
                Простой старт для всех
              </p>

              <h6 class="text-h6 mb-1">
                Активен до 09 дек 2026
              </h6>
              <p>
                Мы отправим вам уведомление при истечении подписки
              </p>

              <h6 class="text-h6 mb-1">
                <span class="d-inline-block me-2">₽99 в месяц</span>
                <VChip
                  color="primary"
                  size="small"
                  label
                >
                  Популярный
                </VChip>
              </h6>
              <p class="mb-0">
                Стандартный план для малого и среднего бизнеса
              </p>
            </VCol>

            <VCol
              cols="12"
              md="6"
              order-md="2"
              order="1"
            >
              <!-- 👉 Alert -->
              <VAlert
                color="warning"
                variant="tonal"
              >
                <VAlertTitle class="mb-1">
                  Требуется ваше внимание!
                </VAlertTitle>
                <div class="text-base">
                  Ваш план требует обновления
                </div>
              </VAlert>

              <!-- 👉 Progress -->
              <div class="d-flex justify-space-between font-weight-bold mt-4 mb-2">
                <h6 class="text-h6">
                  Дни
                </h6>
                <h6 class="text-h6">
                  26 из 30 дней
                </h6>
              </div>

              <VProgressLinear
                rounded
                color="primary"
                :height="10"
                :model-value="75"
              />
              <p class="text-sm mt-1">
                Ваш план требует обновления
              </p>
            </VCol>

            <VCol
              cols="12"
              order="3"
            >
              <div class="d-flex flex-wrap gap-4">
                <VBtn @click="isUpgradePlanDialogVisible = true">
                  Обновить план
                </VBtn>

                <VBtn
                  color="error"
                  variant="tonal"
                >
                  Отменить подписку
                </VBtn>
              </div>
            </VCol>
          </VRow>
        </VCardText>
      </VCard>
    </VCol>

    <!-- 👉 Payment Methods -->
    <VCol cols="12">
      <VCard title="Способы оплаты">
        <template #append>
          <VBtn
            prepend-icon="tabler-plus"
            size="small"
            @click="isCardAddDialogVisible = !isCardAddDialogVisible"
          >
            Добавить карту
          </VBtn>
        </template>

        <VCardText class="d-flex flex-column gap-y-4">
          <VCard
            v-for="card in creditCards"
            :key="card.name"
            border
            flat
          >
            <VCardText class="d-flex flex-sm-row flex-column gap-6 justify-space-between">
              <div class="text-no-wrap">
                <img
                  :src="card.image"
                  :height="25"
                >
                <div class="my-2 d-flex gap-x-2 align-center">
                  <h6 class="text-h6">
                    {{ card.name }}
                  </h6>
                  <VChip
                    v-if="card.isPrimary || card.isExpired"
                    label
                    :color="card.isPrimary ? 'primary' : card.isExpired ? 'error' : 'secondary'"
                    size="small"
                  >
                    {{ card.isPrimary ? 'Основная' : card.isExpired ? 'Истекла' : '' }}
                  </VChip>
                </div>
                <div class="text-body-1">
                  **** **** **** {{ card.number.substring(card.number.length - 4) }}
                </div>
              </div>

              <div class="d-flex flex-column text-sm-end gap-y-4">
                <div class="order-sm-0 order-1">
                  <VBtn
                    variant="tonal"
                    size="small"
                    class="me-4"
                    @click="openEditCardDialog(card)"
                  >
                    Редактировать
                  </VBtn>
                  <VBtn
                    color="error"
                    variant="tonal"
                    size="small"
                  >
                    Удалить
                  </VBtn>
                </div>

                <div class="order-sm-1 order-0 text-sm">
                  Карта истекает {{ card.expiry }}
                </div>
              </div>
            </VCardText>
          </VCard>
        </VCardText>
      </VCard>
    </VCol>

    <VCol cols="12">
      <!-- 👉 Billing Address -->
      <VCard title="Адрес для выставления счетов">
        <template #append>
          <VBtn
            size="small"
            prepend-icon="tabler-plus"
            @click="isEditAddressDialogVisible = !isEditAddressDialogVisible"
          >
            Редактировать адрес
          </VBtn>
        </template>

        <VCardText>
          <VRow>
            <VCol
              cols="12"
              lg="6"
            >
              <VTable class="billing-address-table">
                <tr>
                  <td>
                    <h6 class="text-h6 text-no-wrap mb-2">
                      Название компании:
                    </h6>
                  </td>
                  <td>
                    <p class="text-body-1 mb-2">
                      {{ currentAddress.companyName }}
                    </p>
                  </td>
                </tr>
                <tr>
                  <td>
                    <h6 class="text-h6 text-no-wrap mb-2">
                      Email для оплаты:
                    </h6>
                  </td>
                  <td>
                    <p class="text-body-1 mb-2">
                      {{ currentAddress.billingEmail }}
                    </p>
                  </td>
                </tr>
                <tr>
                  <td>
                    <h6 class="text-h6 text-no-wrap mb-2">
                      Налоговый ID:
                    </h6>
                  </td>
                  <td>
                    <p class="text-body-1 mb-2">
                      {{ currentAddress.taxID }}
                    </p>
                  </td>
                </tr>
                <tr>
                  <td>
                    <h6 class="text-h6 text-no-wrap mb-2">
                      НДС номер:
                    </h6>
                  </td>
                  <td>
                    <p class="text-body-1 mb-2">
                      {{ currentAddress.vatNumber }}
                    </p>
                  </td>
                </tr>
                <tr>
                  <td class="d-flex align-baseline">
                    <h6 class="text-h6 text-no-wrap">
                      Адрес для выставления счетов:
                    </h6>
                  </td>
                  <td>
                    <p class="text-body-1 mb-2">
                      {{ currentAddress.address }}
                    </p>
                  </td>
                </tr>
              </VTable>
            </VCol>

            <VCol
              cols="12"
              lg="6"
            >
              <VTable class="billing-address-table">
                <tr>
                  <td>
                    <h6 class="text-h6 text-no-wrap mb-2">
                      Контакт:
                    </h6>
                  </td>
                  <td>
                    <p class="text-body-1 mb-2">
                      {{ currentAddress.contact }}
                    </p>
                  </td>
                </tr>
                <tr>
                  <td>
                    <h6 class="text-h6 text-no-wrap mb-2">
                      Страна:
                    </h6>
                  </td>
                  <td>
                    <p class="text-body-1 mb-2">
                      {{ currentAddress.country }}
                    </p>
                  </td>
                </tr>
                <tr>
                  <td>
                    <h6 class="text-h6 text-no-wrap mb-2">
                      Регион:
                    </h6>
                  </td>
                  <td>
                    <p class="text-body-1 mb-2">
                      {{ currentAddress.state }}
                    </p>
                  </td>
                </tr>
                <tr>
                  <td>
                    <h6 class="text-h6 text-no-wrap mb-2">
                      Почтовый индекс:
                    </h6>
                  </td>
                  <td>
                    <p class="text-body-1 mb-2">
                      {{ currentAddress.zipCode }}
                    </p>
                  </td>
                </tr>
              </VTable>
            </VCol>
          </VRow>
        </VCardText>
      </VCard>
    </VCol>
  </VRow>

  <!-- 👉 Edit Card Dialog -->
  <CardAddEditDialog
    v-model:is-dialog-visible="isCardEditDialogVisible"
    :card-details="currentCardDetails"
  />

  <!-- 👉 Add Card Dialog -->
  <CardAddEditDialog v-model:is-dialog-visible="isCardAddDialogVisible" />

  <!-- 👉 Edit Address dialog -->
  <AddEditAddressDialog
    v-model:is-dialog-visible="isEditAddressDialogVisible"
    :billing-address="currentBillingAddress"
  />

  <!-- 👉 Upgrade plan dialog -->
  <UserUpgradePlanDialog v-model:is-dialog-visible="isUpgradePlanDialogVisible" />
</template>

<style lang="scss">
.billing-address-table {
  tr {
    td:first-child {
      inline-size: 148px;
    }
  }
}
</style>
