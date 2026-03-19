<script setup>

const props = defineProps({
  customerData: {
    type: null,
    required: true,
  },
})

const isUserInfoEditDialogVisible = ref(false)

const customerData = {
  id: props.customerData.id,
  fullName: props.customerData.customer,
  firstName: props.customerData.customer.split(' ')[0],
  lastName: props.customerData.customer.split(' ')[1],
  company: '',
  role: '',
  username: props.customerData.customer,
  country: props.customerData.country,
  contact: props.customerData.contact,
  email: props.customerData.email,
  currentPlan: '',
  status: props.customerData.status,
  avatar: '',
  taskDone: null,
  projectDone: null,
  taxId: 'Tax-8894',
  language: 'English',
}
</script>

<template>
  <VRow>
    <!-- SECTION Customer Details -->
    <VCol cols="12">
      <VCard v-if="props.customerData">
        <VCardText class="text-center pt-12">
          <!-- 👉 Avatar -->
          <VAvatar
            rounded
            :size="120"
            :color="!props.customerData.customer ? 'primary' : undefined"
            :variant="!props.customerData.avatar ? 'tonal' : undefined"
          >
            <VImg
              v-if="props.customerData.avatar"
              :src="props.customerData.avatar"
            />
            <span
              v-else
              class="text-5xl font-weight-medium"
            >
              {{ avatarText(props.customerData.customer) }}
            </span>
          </VAvatar>

          <!-- 👉 Customer fullName -->
          <h5 class="text-h5 mt-4">
            {{ props.customerData.customer }}
          </h5>
          <div class="text-body-1">
            ID клиента #{{ props.customerData.customerId }}
          </div>

          <div class="d-flex justify-space-evenly gap-x-5 mt-6">
            <div class="d-flex align-center">
              <VAvatar
                variant="tonal"
                color="primary"
                rounded
                class="me-4"
              >
                <VIcon icon="tabler-shopping-cart" />
              </VAvatar>
              <div class="d-flex flex-column align-start">
                <h5 class="text-h5">
                  {{ props.customerData.order }}
                </h5>
                <div class="text-body-1">
                  Заказов
                </div>
              </div>
            </div>
            <div class="d-flex align-center">
              <VAvatar
                variant="tonal"
                color="primary"
                rounded
                class="me-3"
              >
                <VIcon icon="tabler-currency-rubel" />
              </VAvatar>
              <div class="d-flex flex-column align-start">
                <h5 class="text-h5">
                  {{ Number(props.customerData.totalSpent || 0).toLocaleString('ru-RU') }} ₽
                </h5>
                <div class="text-body-1">
                  Потрачено
                </div>
              </div>
            </div>
          </div>
        </VCardText>

        <!-- 👉 Customer Details -->
        <VCardText>
          <h5 class="text-h5">
            Детали
          </h5>

          <VDivider class="my-4" />

          <VList class="card-list mt-2">
            <VListItem>
              <h6 class="text-h6">
                Имя пользователя:
                <span class="text-body-1 d-inline-block">
                  {{ props.customerData.customer }}
                </span>
              </h6>
            </VListItem>

            <VListItem>
              <h6 class="text-h6">
                Email для оплаты:
                <span class="text-body-1 d-inline-block">
                  {{ props.customerData.email }}
                </span>
              </h6>
            </VListItem>

            <VListItem>
              <div class="d-flex gap-x-2 align-center">
                <h6 class="text-h6">
                  Статус:
                </h6>
                <VChip
                  label
                  color="success"
                  size="small"
                >
                  {{ props.customerData.status }}
                </VChip>
              </div>
            </VListItem>

            <VListItem>
              <h6 class="text-h6">
                Телефон:
                <span class="text-body-1 d-inline-block">
                  {{ props.customerData.phone || props.customerData.contact || 'Не указан' }}
                </span>
              </h6>
            </VListItem>

            <VListItem>
              <h6 class="text-h6">
                Страна:
                <span class="text-body-1 d-inline-block">
                  {{ props.customerData.country }}
                </span>
              </h6>
            </VListItem>
          </VList>
        </VCardText>

        <VCardText class="text-center">
          <VBtn
            block
            @click="isUserInfoEditDialogVisible = !isUserInfoEditDialogVisible"
          >
            Редактировать
          </VBtn>
        </VCardText>
      </VCard>
    </VCol>
    <!-- !SECTION -->
  </VRow>
  <UserInfoEditDialog
    v-model:is-dialog-visible="isUserInfoEditDialogVisible"
    :user-data="customerData"
  />
</template>

<style lang="scss" scoped>
.card-list {
  --v-card-list-gap: 0.5rem;
}

</style>
