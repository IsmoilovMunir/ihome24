<script setup>
import rocketImg from '@images/eCommerce/rocket.png'

const props = defineProps({
  customerData: {
    type: null,
    required: true,
  },
})

const isUserInfoEditDialogVisible = ref(false)
const isUpgradePlanDialogVisible = ref(false)

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
                <VIcon icon="tabler-currency-dollar" />
              </VAvatar>
              <div class="d-flex flex-column align-start">
                <h5 class="text-h5">
                  ${{ props.customerData.totalSpent }}
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
            Details
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
                Контакт:
                <span class="text-body-1 d-inline-block">
                  {{ props.customerData.contact }}
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
            Edit Details
          </VBtn>
        </VCardText>
      </VCard>
    </VCol>
    <!-- !SECTION -->

    <!-- SECTION  Upgrade to Premium -->
    <VCol cols="12">
      <VCard
        flat
        class="current-plan"
      >
        <VCardText>
          <div class="d-flex align-center">
            <div>
              <h5 class="text-h5 text-white mb-4">
                Обновить до премиум
              </h5>
              <p class="mb-6 text-wrap">
                Обновите клиента до премиум-членства для доступа к профессиональным функциям.
              </p>
            </div>
            <div>
              <VImg
                :src="rocketImg"
                height="108"
                width="108"
              />
            </div>
          </div>
          <VBtn
            color="#fff"
            class="text-primary"
            block
            @click="isUpgradePlanDialogVisible = !isUpgradePlanDialogVisible"
          >
            Обновить до премиум
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
  <UserUpgradePlanDialog v-model:is-dialog-visible="isUpgradePlanDialogVisible" />
</template>

<style lang="scss" scoped>
.card-list {
  --v-card-list-gap: 0.5rem;
}

.current-plan {
  background: linear-gradient(45deg, rgb(var(--v-theme-primary)) 0%, #9e95f5 100%);
  color: #fff;
}
</style>
