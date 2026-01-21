export const db = {
  searchItems: [
    {
      title: 'Панели управления',
      category: 'dashboards',
      children: [
        {
          url: { name: 'dashboards-ecommerce' },
          icon: 'tabler-shopping-cart',
          title: 'Панель управления электронной коммерцией',
        },
      ],
    },
    {
      title: 'Приложения и страницы',
      category: 'appsPages',
      children: [
        {
          url: { name: 'apps-ecommerce-dashboard' },
          icon: 'tabler-shopping-cart',
          title: 'Панель управления электронной коммерцией',
        },
        {
          url: { name: 'apps-ecommerce-product-list' },
          icon: 'tabler-list',
          title: 'Электронная коммерция - Список товаров',
        },
        {
          url: { name: 'apps-ecommerce-product-add' },
          icon: 'tabler-circle-plus',
          title: 'Электронная коммерция - Добавить товар',
        },
        {
          url: { name: 'apps-ecommerce-product-category-list' },
          icon: 'tabler-list',
          title: 'Электронная коммерция - Список категорий',
        },
        {
          url: { name: 'apps-ecommerce-order-list' },
          icon: 'tabler-list',
          title: 'Электронная коммерция - Список заказов',
        },
        {
          url: { name: 'apps-ecommerce-order-details-id', params: { id: '9042' } },
          icon: 'tabler-list-check',
          title: 'Электронная коммерция - Детали заказа',
        },
        {
          url: { name: 'apps-ecommerce-customer-list' },
          icon: 'tabler-user',
          title: 'Электронная коммерция - Список клиентов',
        },
        {
          url: { name: 'apps-ecommerce-customer-details-id', params: { id: '478426', tab: 'security' } },
          icon: 'tabler-list',
          title: 'Электронная коммерция - Детали клиента',
        },
        {
          url: { name: 'apps-ecommerce-manage-review' },
          icon: 'tabler-quote',
          title: 'Электронная коммерция - Управление отзывами',
        },
        {
          url: { name: 'apps-ecommerce-referrals' },
          icon: 'tabler-users-group',
          title: 'Электронная коммерция - Рефералы',
        },
        {
          url: { name: 'apps-ecommerce-settings' },
          icon: 'tabler-settings',
          title: 'Электронная коммерция - Настройки',
        },
        {
          url: { name: 'apps-user-list' },
          icon: 'tabler-users-group',
          title: 'Список пользователей',
        },
        {
          url: { name: 'apps-user-view-id', params: { id: 21 } },
          icon: 'tabler-eye',
          title: 'Просмотр пользователя',
        },
        {
          url: { name: 'pages-user-profile-tab', params: { tab: 'profile' } },
          icon: 'tabler-user-circle',
          title: 'Профиль пользователя - Профиль',
        },
        {
          url: { name: 'pages-account-settings-tab', params: { tab: 'account' } },
          icon: 'tabler-user-circle',
          title: 'Настройки аккаунта - Аккаунт',
        },
        {
          url: { name: 'pages-account-settings-tab', params: { tab: 'security' } },
          icon: 'tabler-lock-open',
          title: 'Настройки аккаунта - Безопасность',
        },
        {
          url: { name: 'pages-account-settings-tab', params: { tab: 'billing-plans' } },
          icon: 'tabler-currency-dollar',
          title: 'Настройки аккаунта - Биллинг',
        },
        {
          url: { name: 'pages-account-settings-tab', params: { tab: 'notification' } },
          icon: 'tabler-bell',
          title: 'Настройки аккаунта - Уведомления',
        },
        {
          url: { name: 'pages-account-settings-tab', params: { tab: 'connection' } },
          icon: 'tabler-link',
          title: 'Настройки аккаунта - Подключения',
        },
        {
          url: { name: 'pages-pricing' },
          icon: 'tabler-currency-dollar',
          title: 'Цены',
        },
        {
          url: { name: 'pages-faq' },
          icon: 'tabler-help-circle',
          title: 'FAQ',
        },
        {
          url: { name: 'pages-misc-coming-soon' },
          icon: 'tabler-clock',
          title: 'Скоро',
        },
        {
          url: { name: 'pages-misc-under-maintenance' },
          icon: 'tabler-settings',
          title: 'На обслуживании',
        },
        {
          url: { path: '/pages/misc/page-not-found' },
          icon: 'tabler-alert-circle',
          title: 'Страница не найдена - 404',
        },
        {
          url: { path: '/pages/misc/not-authorized' },
          icon: 'tabler-user-x',
          title: 'Не авторизован - 401',
        },
        {
          url: { name: 'pages-authentication-login-v1' },
          icon: 'tabler-login',
          title: 'Вход V1',
        },
        {
          url: { name: 'pages-authentication-login-v2' },
          icon: 'tabler-login',
          title: 'Вход V2',
        },
        {
          url: { name: 'pages-authentication-register-v1' },
          icon: 'tabler-user-plus',
          title: 'Регистрация V1',
        },
        {
          url: { name: 'pages-authentication-register-v2' },
          icon: 'tabler-user-plus',
          title: 'Регистрация V2',
        },
        {
          icon: 'tabler-mail',
          title: 'Подтверждение email V1',
          url: { name: 'pages-authentication-verify-email-v1' },
        },
        {
          icon: 'tabler-mail',
          title: 'Подтверждение email V2',
          url: { name: 'pages-authentication-verify-email-v2' },
        },
        {
          url: { name: 'pages-authentication-forgot-password-v1' },
          icon: 'tabler-lock-exclamation',
          title: 'Забыли пароль V1',
        },
        {
          url: { name: 'pages-authentication-forgot-password-v2' },
          icon: 'tabler-lock-exclamation',
          title: 'Забыли пароль V2',
        },
        {
          url: { name: 'pages-authentication-reset-password-v1' },
          icon: 'tabler-help-circle',
          title: 'Сброс пароля V1',
        },
        {
          url: { name: 'pages-authentication-reset-password-v2' },
          icon: 'tabler-help-circle',
          title: 'Сброс пароля V2',
        },
        {
          icon: 'tabler-devices',
          title: 'Два шага V1',
          url: { name: 'pages-authentication-two-steps-v1' },
        },
        {
          icon: 'tabler-devices',
          title: 'Два шага V2',
          url: { name: 'pages-authentication-two-steps-v2' },
        },
        {
          url: { name: 'pages-authentication-register-multi-steps' },
          icon: 'tabler-user-plus',
          title: 'Многошаговая регистрация',
        },
        {
          url: { name: 'apps-roles' },
          icon: 'tabler-shield-checkered',
          title: 'Роли',
        },
        {
          url: { name: 'apps-permissions' },
          icon: 'tabler-shield-checkered',
          title: 'Разрешения',
        },
      ],
    },
  ],
}
