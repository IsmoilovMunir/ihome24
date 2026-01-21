<script setup>
import product21 from '@images/ecommerce-images/product-21.png'
import product22 from '@images/ecommerce-images/product-22.png'
import product23 from '@images/ecommerce-images/product-23.png'
import product24 from '@images/ecommerce-images/product-24.png'

const orderData = ref()
const route = useRoute('apps-ecommerce-order-details-id')
const { data } = await useApi(`/apps/ecommerce/orders/${ route.params.id }`)
if (data.value)
  orderData.value = data.value
const isConfirmDialogVisible = ref(false)
const isUserInfoEditDialogVisible = ref(false)
const isEditAddressDialogVisible = ref(false)

const headers = [
  {
    title: 'Товар',
    key: 'productName',
  },
  {
    title: 'Цена',
    key: 'price',
  },
  {
    title: 'Количество',
    key: 'quantity',
  },
  {
    title: 'Всего',
    key: 'total',
  },
]

const resolvePaymentStatus = payment => {
  if (payment === 1)
    return {
      text: 'Оплачено',
      color: 'success',
    }
  if (payment === 2)
    return {
      text: 'Ожидает',
      color: 'warning',
    }
  if (payment === 3)
    return {
      text: 'Отменено',
      color: 'secondary',
    }
  if (payment === 4)
    return {
      text: 'Ошибка',
      color: 'error',
    }
}

const resolveStatus = status => {
  if (status === 'Delivered')
    return {
      text: 'Delivered',
      color: 'success',
    }
  if (status === 'Out for Delivery')
    return {
      text: 'Out for Delivery',
      color: 'primary',
    }
  if (status === 'Ready to Pickup')
    return {
      text: 'Ready to Pickup',
      color: 'info',
    }
  if (status === 'Dispatched')
    return {
      text: 'Dispatched',
      color: 'warning',
    }
}

const userData = {
  id: null,
  fullName: orderData.value ? orderData.value.customer : '',
  company: 'Pixinvent',
  role: 'Web developer',
  username: 'T1940',
  country: 'Россия',
  contact: '+1 (609) 972-22-22',
  email: orderData.value?.email,
  status: 'Active',
  taxId: 'Tax-8894',
  language: 'English',
  currentPlan: '',
  avatar: '',
  taskDone: null,
  projectDone: null,
}

const currentBillingAddress = {
  fullName: orderData.value?.customer,
  firstName: orderData.value?.customer.split(' ')[0],
  lastName: orderData.value?.customer.split(' ')[1],
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

const orderDetail = [
  {
    productName: 'OnePlus 7 Pro',
    productImage: product21,
    subtitle: 'Storage: 128gb',
    price: 799,
    quantity: 1,
    total: 799,
  },
  {
    productName: 'Face Cream',
    productImage: product22,
    subtitle: 'Gender: Women',
    price: 89,
    quantity: 1,
    total: 89,
  },
  {
    productName: 'Wooden Chair',
    productImage: product23,
    subtitle: 'Material: Woodem',
    price: 289,
    quantity: 2,
    total: 578,
  },
  {
    productName: 'Nike Jorden',
    productImage: product24,
    subtitle: 'Size: 8UK',
    price: 299,
    quantity: 2,
    total: 598,
  },
]
</script>

<template>
  <div v-if="orderData">
    <div class="d-flex justify-space-between align-center flex-wrap gap-y-4 mb-6">
      <div>
        <div class="d-flex gap-2 align-center mb-2 flex-wrap">
          <h5 class="text-h5">
            Заказ #{{ route.params.id }}
          </h5>
          <div class="d-flex gap-x-2">
            <VChip
              v-if="orderData?.payment"
              variant="tonal"
              :color="resolvePaymentStatus(orderData.payment)?.color"
              label
              size="small"
            >
              {{ resolvePaymentStatus(orderData.payment)?.text }}
            </VChip>
            <VChip
              v-if="orderData?.status"
              v-bind="resolveStatus(orderData?.status)"
              label
              size="small"
            />
          </div>
        </div>
        <div class="text-body-1">
          Aug 17, 2020, 5:48 (ET)
        </div>
      </div>

      <VBtn
        variant="tonal"
        color="error"
        @click="isConfirmDialogVisible = !isConfirmDialogVisible"
      >
        Удалить заказ
      </VBtn>
    </div>

    <VRow>
      <VCol
        cols="12"
        md="8"
      >
        <!-- 👉 Order Details -->
        <VCard class="mb-6">
          <VCardItem>
            <template #title>
              <h5 class="text-h5">
                Order Details
              </h5>
            </template>
            <template #append>
              <div class="text-base font-weight-medium text-primary cursor-pointer">
                Edit
              </div>
            </template>
          </VCardItem>

          <VDivider />
          <VDataTable
            :headers="headers"
            :items="orderDetail"
            item-value="productName"
            show-select
            class="text-no-wrap"
          >
            <template #item.productName="{ item }">
              <div class="d-flex gap-x-3 align-center">
                <VAvatar
                  size="34"
                  :image="item.productImage"
                  :rounded="0"
                />

                <div class="d-flex flex-column align-start">
                  <h6 class="text-h6">
                    {{ item.productName }}
                  </h6>

                  <span class="text-body-2">
                    {{ item.subtitle }}
                  </span>
                </div>
              </div>
            </template>

            <template #item.price="{ item }">
              <div class="text-body-1">
                ${{ item.price }}
              </div>
            </template>

            <template #item.total="{ item }">
              <div class="text-body-1">
                ${{ item.total }}
              </div>
            </template>

            <template #item.quantity="{ item }">
              <div class="text-body-1">
                {{ item.quantity }}
              </div>
            </template>

            <template #bottom />
          </VDataTable>
          <VDivider />

          <VCardText>
            <div class="d-flex align-end flex-column">
              <table class="text-high-emphasis">
                <tbody>
                  <tr>
                    <td width="200px">
                      Подытог:
                    </td>
                    <td class="font-weight-medium">
                      $2,093
                    </td>
                  </tr>
                  <tr>
                    <td>Доставка: </td>
                    <td class="font-weight-medium">
                      $2
                    </td>
                  </tr>
                  <tr>
                    <td>Налог: </td>
                    <td class="font-weight-medium">
                      $28
                    </td>
                  </tr>
                  <tr>
                    <td class="text-high-emphasis font-weight-medium">
                      Итого:
                    </td>
                    <td class="font-weight-medium">
                      $2,113
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </VCardText>
        </VCard>

        <!-- 👉 Shipping Activity -->
        <VCard title="Shipping Activity">
          <VCardText>
            <VTimeline
              truncate-line="both"
              line-inset="9"
              align="start"
              side="end"
              line-color="primary"
              density="compact"
            >
              <VTimelineItem
                dot-color="primary"
                size="x-small"
              >
                <div class="d-flex justify-space-between align-center">
                  <div class="app-timeline-title">
                    Заказ размещен (ID заказа: #32543)
                  </div>
                  <div class="app-timeline-meta">
                    Вторник 10:20
                  </div>
                </div>
                <p class="app-timeline-text mb-0 mt-3">
                  Ваш заказ успешно размещен
                </p>
              </VTimelineItem>

              <VTimelineItem
                dot-color="primary"
                size="x-small"
              >
                <div class="d-flex justify-space-between align-center">
                  <span class="app-timeline-title">Pick-up</span>
                  <span class="app-timeline-meta">Wednesday 11:29 AM</span>
                </div>
                <p class="app-timeline-text mb-0 mt-3">
                  Pick-up scheduled with courier
                </p>
              </VTimelineItem>

              <VTimelineItem
                dot-color="primary"
                size="x-small"
              >
                <div class="d-flex justify-space-between align-center">
                  <span class="app-timeline-title">Отправлено</span>
                  <span class="app-timeline-meta">Четверг 8:15</span>
                </div>
                <p class="app-timeline-text mb-0 mt-3">
                  Товар забран курьером.
                </p>
              </VTimelineItem>

              <VTimelineItem
                dot-color="primary"
                size="x-small"
              >
                <div class="d-flex justify-space-between align-center">
                  <span class="app-timeline-title">Package arrived</span>
                  <span class="app-timeline-meta">Saturday 15:20 AM</span>
                </div>
                <p class="app-timeline-text mb-0 mt-3">
                  Package arrived at an Amazon facility, NY
                </p>
              </VTimelineItem>

              <VTimelineItem
                dot-color="primary"
                size="x-small"
              >
                <div class="d-flex justify-space-between align-center">
                  <span class="app-timeline-title">Отправлено на доставку</span>
                  <span class="app-timeline-meta">Сегодня 14:12</span>
                </div>
                <p class="app-timeline-text mb-0 mt-3">
                  Посылка покинула склад Amazon, NY
                </p>
              </VTimelineItem>

              <VTimelineItem
                dot-color="secondary"
                size="x-small"
              >
                <div class="d-flex justify-space-between align-center">
                  <span class="app-timeline-title">Delivery</span>
                </div>
                <p class="app-timeline-text mb-4 mt-3">
                  Package will be delivered by tomorrow
                </p>
              </VTimelineItem>
            </VTimeline>
          </VCardText>
        </VCard>
      </VCol>

      <VCol
        cols="12"
        md="4"
      >
        <!-- 👉 Customer Details  -->
        <VCard class="mb-6">
          <VCardText class="d-flex flex-column gap-y-6">
            <h5 class="text-h5">
              Данные клиента
            </h5>

            <div class="d-flex align-center">
              <VAvatar
                v-if="orderData"
                :variant="!orderData?.avatar.length ? 'tonal' : undefined"
                :rounded="1"
                class="me-3"
              >
                <VImg
                  v-if="orderData?.avatar"
                  :src="orderData?.avatar"
                />

                <span
                  v-else
                  class="font-weight-medium"
                >{{ avatarText(orderData?.customer) }}</span>
              </VAvatar>
              <div>
                <h6 class="text-h6">
                  {{ orderData?.customer }}
                </h6>
                <div class="text-body-1">
                  ID клиента: #{{ orderData?.order }}
                </div>
              </div>
            </div>

            <div class="d-flex gap-x-3 align-center">
              <VAvatar
                variant="tonal"
                color="success"
              >
                <VIcon icon="tabler-shopping-cart" />
              </VAvatar>
              <h6 class="text-h6">
                12 Orders
              </h6>
            </div>

            <div class="d-flex flex-column gap-y-1">
              <div class="d-flex justify-space-between align-center">
                <h6 class="text-h6">
                  Контактная информация
                </h6>
                <div
                  class="text-base text-primary cursor-pointer font-weight-medium"
                  @click="isUserInfoEditDialogVisible = !isUserInfoEditDialogVisible"
                >
                  Редактировать
                </div>
              </div>
              <span>Email: {{ orderData?.email }}</span>
              <span>Телефон: +1 (609) 972-22-22</span>
            </div>
          </VCardText>
        </VCard>

        <!-- 👉 Shipping Address -->
        <VCard class="mb-6">
          <VCardItem>
            <VCardTitle>Адрес доставки</VCardTitle>
            <template #append>
              <div class="d-flex align-center justify-space-between">
                <div
                  class="text-base font-weight-medium text-primary cursor-pointer"
                  @click="isEditAddressDialogVisible = !isEditAddressDialogVisible"
                >
                  Редактировать
                </div>
              </div>
            </template>
          </VCardItem>

          <VCardText>
            <div class="text-body-1">
              Москва, ул. Тверская, д. 1 <br> Россия
            </div>
          </VCardText>
        </VCard>

        <!-- 👉 Billing Address -->
        <VCard>
          <VCardText>
            <div class="d-flex align-center justify-space-between mb-2">
              <h5 class="text-h5">
                Billing Address
              </h5>
              <div
                class="text-base font-weight-medium text-primary cursor-pointer"
                @click="isEditAddressDialogVisible = !isEditAddressDialogVisible"
              >
                Edit
              </div>
            </div>
            <div>
              Москва, ул. Тверская, д. 1 <br> Россия
            </div>

            <div class="mt-6">
              <h5 class="text-h5 mb-1">
                Mastercard
              </h5>
              <div class="text-body-1">
                Card Number: ******{{ orderData?.methodNumber }}
              </div>
            </div>
          </VCardText>
        </VCard>
      </VCol>
    </VRow>

    <ConfirmDialog
      v-model:is-dialog-visible="isConfirmDialogVisible"
      confirmation-question="Вы уверены, что хотите отменить заказ?"
      cancel-msg="Заказ отменен!!"
      cancel-title="Отменено"
      confirm-msg="Ваш заказ успешно отменен."
      confirm-title="Отменено!"
    />

    <UserInfoEditDialog
      v-model:is-dialog-visible="isUserInfoEditDialogVisible"
      :user-data="userData"
    />

    <AddEditAddressDialog
      v-model:is-dialog-visible="isEditAddressDialogVisible"
      :billing-address="currentBillingAddress"
    />
  </div>
  <section v-else>
    <VAlert
      type="error"
      variant="tonal"
    >
      Заказ с ID #{{ route.params.id }} недоступен или не найден!
    </VAlert>
  </section>
</template>
