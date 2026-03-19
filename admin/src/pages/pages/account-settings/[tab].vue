<script setup>
import AccountSettingsAccount from '@/views/pages/account-settings/AccountSettingsAccount.vue'
import AccountSettingsNotification from '@/views/pages/account-settings/AccountSettingsNotification.vue'
import AccountSettingsSecurity from '@/views/pages/account-settings/AccountSettingsSecurity.vue'

const route = useRoute('pages-account-settings-tab')
const router = useRouter()

const allowedTabs = ['account', 'security', 'notification']

const activeTab = computed({
  get: () => {
    const current = route.params.tab
    return allowedTabs.includes(current) ? current : 'account'
  },
  set: () => route.params.tab,
})

// tabs (убраны Оплата и планы, Подключения)
const tabs = [
  {
    title: 'Аккаунт',
    icon: 'tabler-users',
    tab: 'account',
  },
  {
    title: 'Безопасность',
    icon: 'tabler-lock',
    tab: 'security',
  },
  {
    title: 'Уведомления',
    icon: 'tabler-bell',
    tab: 'notification',
  },
]

if (!allowedTabs.includes(route.params.tab)) {
  router.replace({ name: 'pages-account-settings-tab', params: { tab: 'account' } })
}

definePage({ meta: { navActiveLink: 'pages-account-settings-tab' } })
</script>

<template>
  <div>
    <VTabs
      v-model="activeTab"
      class="v-tabs-pill"
    >
      <VTab
        v-for="item in tabs"
        :key="item.icon"
        :value="item.tab"
        :to="{ name: 'pages-account-settings-tab', params: { tab: item.tab } }"
      >
        <VIcon
          size="20"
          start
          :icon="item.icon"
        />
        {{ item.title }}
      </VTab>
    </VTabs>

    <VWindow
      v-model="activeTab"
      class="mt-6 disable-tab-transition"
      :touch="false"
    >
      <!-- Account -->
      <VWindowItem value="account">
        <AccountSettingsAccount />
      </VWindowItem>

      <!-- Security -->
      <VWindowItem value="security">
        <AccountSettingsSecurity />
      </VWindowItem>

      <!-- Notification -->
      <VWindowItem value="notification">
        <AccountSettingsNotification />
      </VWindowItem>
    </VWindow>
  </div>
</template>
