<script setup>
const customerNotifications = ref([
  {
    type: 'Новая регистрация клиента',
    email: true,
    app: true,
  },
  {
    type: 'Сброс пароля аккаунта клиента',
    email: true,
    app: true,
  },
  {
    type: 'Приглашение в аккаунт клиента',
    email: false,
    app: false,
  },
])

const shippingNotifications = ref([
  {
    type: 'Забрано',
    email: true,
    app: true,
  },
  {
    type: 'Обновление доставки',
    email: true,
    app: false,
  },
  {
    type: 'Доставлено',
    email: false,
    app: true,
  },
])

const ordersNotification = ref([
  {
    type: 'Покупка заказа',
    email: true,
    app: true,
  },
  {
    type: 'Заказ отменен',
    email: true,
    app: false,
  },
  {
    type: 'Запрос на возврат заказа',
    email: false,
    app: true,
  },
  {
    type: 'Подтверждение заказа',
    email: true,
    app: false,
  },
  {
    type: 'Ошибка платежа',
    email: true,
    app: false,
  },
])
</script>

<template>
  <VCard class="mb-4">
    <VCardText>
      <h5 class="text-h5 mb-2">
        Клиенты
      </h5>
      <VTable class="text-no-wrap mb-6 border rounded">
        <thead>
          <tr>
            <th scope="col">
              ТИП
            </th>
            <th scope="col">
              EMAIL
            </th>
            <th scope="col">
              ПРИЛОЖЕНИЕ
            </th>
          </tr>
        </thead>

        <tbody>
          <tr
            v-for="notification in customerNotifications"
            :key="notification.type"
          >
            <td
              width="400px"
              class="text-high-emphasis"
            >
              {{ notification.type }}
            </td>
            <td>
              <VCheckbox v-model="notification.email" />
            </td>

            <td>
              <VCheckbox v-model="notification.app" />
            </td>
          </tr>
        </tbody>
      </VTable>

      <h5 class="text-h5 mb-2">
        Заказы
      </h5>
      <VTable class="text-no-wrap mb-6 border rounded">
        <thead>
          <tr>
            <th scope="col">
              ТИП
            </th>
            <th scope="col">
              EMAIL
            </th>
            <th scope="col">
              ПРИЛОЖЕНИЕ
            </th>
          </tr>
        </thead>

        <tbody>
          <tr
            v-for="notification in ordersNotification"
            :key="notification.type"
          >
            <td
              width="400px"
              class="text-high-emphasis"
            >
              {{ notification.type }}
            </td>
            <td>
              <VCheckbox v-model="notification.email" />
            </td>

            <td>
              <VCheckbox v-model="notification.app" />
            </td>
          </tr>
        </tbody>
      </VTable>

      <h5 class="text-h5 mb-2">
        Доставка
      </h5>
      <VTable class="text-no-wrap mb-6 border rounded">
        <thead>
          <tr>
            <th scope="col">
              ТИП
            </th>
            <th scope="col">
              EMAIL
            </th>
            <th scope="col">
              ПРИЛОЖЕНИЕ
            </th>
          </tr>
        </thead>

        <tbody>
          <tr
            v-for="notification in shippingNotifications"
            :key="notification.type"
          >
            <td
              width="400px"
              class="text-high-emphasis"
            >
              {{ notification.type }}
            </td>
            <td>
              <VCheckbox v-model="notification.email" />
            </td>

            <td>
              <VCheckbox v-model="notification.app" />
            </td>
          </tr>
        </tbody>
      </VTable>
    </VCardText>
  </VCard>

  <div class="d-flex justify-end gap-x-4">
    <VBtn
      variant="tonal"
      color="secondary"
    >
      Отменить
    </VBtn>
    <VBtn>Сохранить изменения</VBtn>
  </div>
</template>
