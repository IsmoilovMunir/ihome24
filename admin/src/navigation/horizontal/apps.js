export default [
  {
    title: 'Приложения',
    icon: { icon: 'tabler-layout-grid-add' },
    children: [
      {
        title: 'Электронная коммерция',
        icon: { icon: 'tabler-shopping-cart-plus' },
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
              { title: 'Завершённые', to: 'apps-ecommerce-order-completed' },
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
      {
        title: 'Пользователи',
        icon: { icon: 'tabler-users' },
        children: [
          { title: 'Список', to: 'apps-user-list' },
          { title: 'Просмотр', to: { name: 'apps-user-view-id', params: { id: 21 } } },
        ],
      },
      {
        title: 'Роли и разрешения',
        icon: { icon: 'tabler-settings' },
        children: [
          { title: 'Роли', to: 'apps-roles' },
          { title: 'Разрешения', to: 'apps-permissions' },
        ],
      },
    ],
  },
]
