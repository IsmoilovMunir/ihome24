export default [
  {
    title: 'Страницы',
    icon: { icon: 'tabler-file' },
    children: [
      {
        title: 'Профиль пользователя',
        icon: { icon: 'tabler-user-circle' },
        to: { name: 'pages-user-profile-tab', params: { tab: 'profile' } },
      },
      {
        title: 'Настройки аккаунта',
        icon: { icon: 'tabler-settings' },
        to: { name: 'pages-account-settings-tab', params: { tab: 'account' } },
      },
      { title: 'FAQ', icon: { icon: 'tabler-help' }, to: 'pages-faq' },
      { title: 'Цены', icon: { icon: 'tabler-diamond' }, to: 'pages-pricing' },
      {
        title: 'Разное',
        icon: { icon: 'tabler-cube' },
        children: [
          { title: 'Скоро', to: 'pages-misc-coming-soon' },
          { title: 'На обслуживании', to: 'pages-misc-under-maintenance', target: '_blank' },
          { title: 'Страница не найдена - 404', to: { path: '/pages/misc/not-found' }, target: '_blank' },
          { title: 'Не авторизован - 401', to: { path: '/pages/misc/not-authorized' }, target: '_blank' },
        ],
      },
      {
        title: 'Аутентификация',
        icon: { icon: 'tabler-lock' },
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
    ],
  },
]
