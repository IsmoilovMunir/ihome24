export default [
  {
    title: 'Приложения',
    icon: { icon: 'tabler-layout-grid-add' },
    children: [
      {
        title: 'Электронная коммерция',
        icon: { icon: 'tabler-shopping-cart-plus' },
        action: 'read',
        subject: 'Ecommerce',
        children: [
          {
            title: 'Панель управления',
            to: 'apps-ecommerce-dashboard',
            action: 'read',
            subject: 'Ecommerce',
          },
          {
            title: 'Товары',
            action: 'read',
            subject: 'EcommerceProduct',
            children: [
              { title: 'Список', to: 'apps-ecommerce-product-list', action: 'read', subject: 'EcommerceProduct' },
              { title: 'Добавить', to: 'apps-ecommerce-product-add', action: 'manage', subject: 'EcommerceProduct' },
              { title: 'Категории', to: 'apps-ecommerce-product-category-list', action: 'read', subject: 'EcommerceProduct' },
            ],
          },
          {
            title: 'Заказы',
            action: 'manage',
            subject: 'Ecommerce',
            children: [
              { title: 'Список', to: 'apps-ecommerce-order-list', action: 'manage', subject: 'Ecommerce' },
              { title: 'Завершённые', to: 'apps-ecommerce-order-completed', action: 'manage', subject: 'Ecommerce' },
              // Детали должны открываться из списка конкретного заказа
            ],
          },
          {
            title: 'Клиенты',
            action: 'manage',
            subject: 'Ecommerce',
            children: [
              { title: 'Список', to: 'apps-ecommerce-customer-list', action: 'manage', subject: 'Ecommerce' },
              // Детали должны открываться из списка конкретного клиента
            ],
          },
          {
            title: 'Управление отзывами',
            to: 'apps-ecommerce-manage-review',
            action: 'manage',
            subject: 'Ecommerce',
          },
          {
            title: 'Рефералы',
            to: 'apps-ecommerce-referrals',
            action: 'manage',
            subject: 'Ecommerce',
          },
          {
            title: 'Настройки',
            to: 'apps-ecommerce-settings',
            action: 'manage',
            subject: 'Ecommerce',
          },
        ],
      },
      {
        title: 'Пользователи',
        icon: { icon: 'tabler-users' },
        action: 'manage',
        subject: 'AdminUsers',
        children: [
          { title: 'Список', to: 'apps-user-list', action: 'manage', subject: 'AdminUsers' },
          { title: 'Просмотр', to: { name: 'apps-user-view-id', params: { id: 21 } }, action: 'manage', subject: 'AdminUsers' },
        ],
      },
      {
        title: 'Роли и разрешения',
        icon: { icon: 'tabler-settings' },
        action: 'manage',
        subject: 'AdminRoles',
        children: [
          { title: 'Роли', to: 'apps-roles', action: 'manage', subject: 'AdminRoles' },
          { title: 'Разрешения', to: 'apps-permissions', action: 'manage', subject: 'AdminRoles' },
        ],
      },
    ],
  },
]
