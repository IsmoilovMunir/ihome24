<script setup>
import ECommerceAddCustomerDrawer from '@/views/apps/ecommerce/ECommerceAddCustomerDrawer.vue'
import CustomerBioPanel from '@/views/apps/ecommerce/customer/view/CustomerBioPanel.vue'
import CustomerTabNotification from '@/views/apps/ecommerce/customer/view/CustomerTabNotification.vue'
import CustomerTabOverview from '@/views/apps/ecommerce/customer/view/CustomerTabOverview.vue'

const route = useRoute('apps-ecommerce-customer-details-id')
const customerData = ref()
const userTab = ref(null)
const isLoading = ref(true)

const tabs = [
  {
    title: 'Обзор',
    icon: 'tabler-user',
  },
  {
    title: 'Уведомления',
    icon: 'tabler-bell',
  },
]

try {
  const { data } = await useApi(`/apps/ecommerce/customers/${ route.params.id }`)
  if (data.value)
    customerData.value = data.value
} finally {
  isLoading.value = false
}
const isAddCustomerDrawerOpen = ref(false)
</script>

<template>
  <div>
    <!-- 👉 Header  -->
    <div class="d-flex justify-space-between align-center flex-wrap gap-y-4 mb-6">
      <div>
        <h4 class="text-h4 mb-1">
          ID клиента #{{ route.params.id }}
        </h4>
        <div class="text-body-1">
          Aug 17, 2020, 5:48 (ET)
        </div>
      </div>
      <div class="d-flex gap-4">
        <VBtn
          variant="tonal"
          color="error"
        >
          Удалить клиента
        </VBtn>
      </div>
    </div>
    <!-- 👉 Customer Profile  -->
    <VRow v-if="isLoading">
      <VCol cols="12">
        <VProgressLinear indeterminate />
      </VCol>
    </VRow>
    <VRow v-else-if="customerData">
      <VCol
        v-if="customerData"
        cols="12"
        md="5"
        lg="4"
      >
        <CustomerBioPanel :customer-data="customerData" />
      </VCol>
      <VCol
        cols="12"
        md="7"
        lg="8"
      >
        <VTabs
          v-model="userTab"
          class="v-tabs-pill mb-3 disable-tab-transition"
        >
          <VTab
            v-for="tab in tabs"
            :key="tab.title"
          >
            <VIcon
              size="20"
              start
              :icon="tab.icon"
            />
            {{ tab.title }}
          </VTab>
        </VTabs>
        <VWindow
          v-model="userTab"
          class="disable-tab-transition"
          :touch="false"
        >
          <VWindowItem>
            <CustomerTabOverview :customer-data="customerData" />
          </VWindowItem>
          <VWindowItem>
            <CustomerTabNotification />
          </VWindowItem>
        </VWindow>
      </VCol>
    </VRow>
    <div v-else>
      <VAlert
        type="error"
        variant="tonal"
      >
        Клиент с ID {{ route.params.id }} не найден!
      </VAlert>
      <div class="mt-4">
        <VBtn :to="{ name: 'apps-ecommerce-customer-list' }">
          Вернуться к списку клиентов
        </VBtn>
      </div>
    </div>
    <ECommerceAddCustomerDrawer v-model:is-drawer-open="isAddCustomerDrawerOpen" />
  </div>
</template>
