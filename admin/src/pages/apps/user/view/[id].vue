<script setup>
import UserBioPanel from '@/views/apps/user/view/UserBioPanel.vue'
import UserTabAccount from '@/views/apps/user/view/UserTabAccount.vue'
import UserTabBillingsPlans from '@/views/apps/user/view/UserTabBillingsPlans.vue'
import UserTabConnections from '@/views/apps/user/view/UserTabConnections.vue'
import UserTabNotifications from '@/views/apps/user/view/UserTabNotifications.vue'
import UserTabSecurity from '@/views/apps/user/view/UserTabSecurity.vue'

definePage({
  meta: {
    action: 'manage',
    subject: 'AdminUsers',
  },
})

const route = useRoute('apps-user-view-id')
// Список пользователей: «Редактировать» открывает сразу вкладку «Аккаунт» (индекс 0)
const userTab = ref(route.query.tab === 'account' ? 0 : null)

const tabs = [
  {
    icon: 'tabler-users',
    title: 'Аккаунт',
  },
  {
    icon: 'tabler-lock',
    title: 'Безопасность',
  },
  {
    icon: 'tabler-bookmark',
    title: 'Оплата и план',
  },
  {
    icon: 'tabler-bell',
    title: 'Уведомления',
  },
  {
    icon: 'tabler-link',
    title: 'Подключения',
  },
]

const { data: userData } = await useApi(`/apps/users/${ route.params.id }`)
</script>

<template>
  <VRow v-if="userData">
    <VCol
      cols="12"
      md="5"
      lg="4"
    >
      <UserBioPanel :user-data="userData" />
    </VCol>

    <VCol
      cols="12"
      md="7"
      lg="8"
    >
      <VTabs
        v-model="userTab"
        class="v-tabs-pill"
      >
        <VTab
          v-for="tab in tabs"
          :key="tab.icon"
        >
          <VIcon
            :size="18"
            :icon="tab.icon"
            class="me-1"
          />
          <span>{{ tab.title }}</span>
        </VTab>
      </VTabs>

      <VWindow
        v-model="userTab"
        class="mt-6 disable-tab-transition"
        :touch="false"
      >
        <VWindowItem>
          <UserTabAccount :user-data="userData" />
        </VWindowItem>

        <VWindowItem>
          <UserTabSecurity />
        </VWindowItem>

        <VWindowItem>
          <UserTabBillingsPlans :user-data="userData" />
        </VWindowItem>

        <VWindowItem>
          <UserTabNotifications />
        </VWindowItem>

        <VWindowItem>
          <UserTabConnections />
        </VWindowItem>
      </VWindow>
    </VCol>
  </VRow>
  <div v-else>
    <VAlert
      type="error"
      variant="tonal"
    >
      Пользователь с ID {{ route.params.id }} не найден!
    </VAlert>
  </div>
</template>
