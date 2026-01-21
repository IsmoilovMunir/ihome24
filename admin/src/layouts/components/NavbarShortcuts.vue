<script setup>
const shortcuts = [
  {
    icon: 'tabler-user',
    title: 'Пользователи',
    subtitle: 'Управление пользователями',
    to: { name: 'apps-user-list' },
  },
  {
    icon: 'tabler-users',
    title: 'Управление ролями',
    subtitle: 'Разрешения',
    to: { name: 'apps-roles' },
  },
  {
    icon: 'tabler-device-desktop-analytics',
    title: 'Панель управления',
    subtitle: 'Панель электронной коммерции',
    to: { name: 'dashboards-ecommerce' },
  },
  {
    icon: 'tabler-settings',
    title: 'Настройки',
    subtitle: 'Настройки аккаунта',
    to: {
      name: 'pages-account-settings-tab',
      params: { tab: 'account' },
    },
  },
]
</script>

<template>
  <Shortcuts :shortcuts="shortcuts" />
</template>
