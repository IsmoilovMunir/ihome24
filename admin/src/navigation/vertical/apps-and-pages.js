export default [
  { heading: 'Приложения и страницы' },
  {
    title: 'Электронная коммерция',
    icon: { icon: 'tabler-shopping-cart' },
    children: [
      {
        title: 'Панель управления',
        to: 'apps-ecommerce-dashboard',
      },
      {
        title: 'Товары',
        children: [
          { title: 'Список', to: 'apps-ecommerce-product-list' },
          { title: 'Добавить', to: 'apps-ecommerce-product-add' },
          { title: 'Категории', to: 'apps-ecommerce-product-category-list' },
        ],
      },
      {
        title: 'Заказы',
        children: [
          { title: 'Список', to: 'apps-ecommerce-order-list' },
          { title: 'Детали', to: { name: 'apps-ecommerce-order-details-id', params: { id: '1' } } },
        ],
      },
      {
        title: 'Клиенты',
        children: [
          { title: 'Список', to: 'apps-ecommerce-customer-list' },
          { title: 'Детали', to: { name: 'apps-ecommerce-customer-details-id', params: { id: 478426 } } },
        ],
      },
      {
        title: 'Управление отзывами',
        to: 'apps-ecommerce-manage-review',
      },
      {
        title: 'Рефералы',
        to: 'apps-ecommerce-referrals',
      },
      {
        title: 'Настройки',
        to: 'apps-ecommerce-settings',
      },
    ],
  },
  // Временно скрыто
  // {
  //   title: 'Академия',
  //   icon: { icon: 'tabler-school' },
  //   children: [
  //     { title: 'Панель управления', to: 'apps-academy-dashboard' },
  //     { title: 'Мои курсы', to: 'apps-academy-my-course' },
  //     { title: 'Детали курса', to: 'apps-academy-course-details' },
  //   ],
  // },
  // {
  //   title: 'Логистика',
  //   icon: { icon: 'tabler-truck' },
  //   children: [
  //     { title: 'Панель управления', to: 'apps-logistics-dashboard' },
  //     { title: 'Автопарк', to: 'apps-logistics-fleet' },
  //   ],
  // },
  // {
  //   title: 'Email',
  //   icon: { icon: 'tabler-mail' },
  //   to: 'apps-email',
  // },
  // {
  //   title: 'Chat',
  //   icon: { icon: 'tabler-message-circle-2' },
  //   to: 'apps-chat',
  // },
  // {
  //   title: 'Calendar',
  //   icon: { icon: 'tabler-calendar' },
  //   to: 'apps-calendar',
  // },
  // {
  //   title: 'Kanban',
  //   icon: { icon: 'tabler-layout-kanban' },
  //   to: 'apps-kanban',
  // },
  // {
  //   title: 'Invoice',
  //   icon: { icon: 'tabler-file-invoice' },
  //   children: [
  //     { title: 'List', to: 'apps-invoice-list' },
  //     { title: 'Preview', to: { name: 'apps-invoice-preview-id', params: { id: '5036' } } },
  //     { title: 'Edit', to: { name: 'apps-invoice-edit-id', params: { id: '5036' } } },
  //     { title: 'Add', to: 'apps-invoice-add' },
  //   ],
  // },
  {
    title: 'Пользователи',
    icon: { icon: 'tabler-user' },
    children: [
      { title: 'Список', to: 'apps-user-list' },
      { title: 'Просмотр', to: { name: 'apps-user-view-id', params: { id: 21 } } },
    ],
  },
  {
    title: 'Роли и разрешения',
    icon: { icon: 'tabler-lock' },
    children: [
      { title: 'Роли', to: 'apps-roles' },
      { title: 'Разрешения', to: 'apps-permissions' },
    ],
  },
  {
    title: 'Страницы',
    icon: { icon: 'tabler-file' },
    children: [
      { title: 'Профиль пользователя', to: { name: 'pages-user-profile-tab', params: { tab: 'profile' } } },
      { title: 'Настройки аккаунта', to: { name: 'pages-account-settings-tab', params: { tab: 'account' } } },
      { title: 'Цены', to: 'pages-pricing' },
      { title: 'FAQ', to: 'pages-faq' },
      {
        title: 'Разное',
        children: [
          { title: 'Скоро', to: 'pages-misc-coming-soon', target: '_blank' },
          { title: 'На обслуживании', to: 'pages-misc-under-maintenance', target: '_blank' },
          { title: 'Страница не найдена - 404', to: { path: '/pages/misc/not-found' }, target: '_blank' },
          { title: 'Не авторизован - 401', to: { path: '/pages/misc/not-authorized' }, target: '_blank' },
        ],
      },
    ],
  },
  {
    title: 'Аутентификация',
    icon: { icon: 'tabler-shield-lock' },
    children: [
      {
        title: 'Вход',
        children: [
          { title: 'Вход v1', to: 'pages-authentication-login-v1', target: '_blank' },
          { title: 'Вход v2', to: 'pages-authentication-login-v2', target: '_blank' },
        ],
      },
      {
        title: 'Регистрация',
        children: [
          { title: 'Регистрация v1', to: 'pages-authentication-register-v1', target: '_blank' },
          { title: 'Регистрация v2', to: 'pages-authentication-register-v2', target: '_blank' },
          { title: 'Многошаговая регистрация', to: 'pages-authentication-register-multi-steps', target: '_blank' },
        ],
      },
      {
        title: 'Подтверждение email',
        children: [
          { title: 'Подтверждение email v1', to: 'pages-authentication-verify-email-v1', target: '_blank' },
          { title: 'Подтверждение email v2', to: 'pages-authentication-verify-email-v2', target: '_blank' },
        ],
      },
      {
        title: 'Забыли пароль',
        children: [
          { title: 'Забыли пароль v1', to: 'pages-authentication-forgot-password-v1', target: '_blank' },
          { title: 'Забыли пароль v2', to: 'pages-authentication-forgot-password-v2', target: '_blank' },
        ],
      },
      {
        title: 'Сброс пароля',
        children: [
          { title: 'Сброс пароля v1', to: 'pages-authentication-reset-password-v1', target: '_blank' },
          { title: 'Сброс пароля v2', to: 'pages-authentication-reset-password-v2', target: '_blank' },
        ],
      },
      {
        title: 'Два шага',
        children: [
          { title: 'Два шага v1', to: 'pages-authentication-two-steps-v1', target: '_blank' },
          { title: 'Два шага v2', to: 'pages-authentication-two-steps-v2', target: '_blank' },
        ],
      },
    ],
  },
]
