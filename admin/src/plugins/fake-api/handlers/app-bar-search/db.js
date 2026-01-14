export const db = {
  searchItems: [
    {
      title: 'Dashboards',
      category: 'dashboards',
      children: [
        {
          url: { name: 'dashboards-ecommerce' },
          icon: 'tabler-shopping-cart',
          title: 'ECommerce Dashboard',
        },
      ],
    },
    {
      title: 'Apps & Pages',
      category: 'appsPages',
      children: [
        {
          url: { name: 'apps-ecommerce-dashboard' },
          icon: 'tabler-shopping-cart',
          title: 'ECommerce Dashboard',
        },
        {
          url: { name: 'apps-ecommerce-product-list' },
          icon: 'tabler-list',
          title: 'Ecommerce - Product List',
        },
        {
          url: { name: 'apps-ecommerce-product-add' },
          icon: 'tabler-circle-plus',
          title: 'Ecommerce - Add Product',
        },
        {
          url: { name: 'apps-ecommerce-product-category-list' },
          icon: 'tabler-list',
          title: 'Ecommerce - Category List',
        },
        {
          url: { name: 'apps-ecommerce-order-list' },
          icon: 'tabler-list',
          title: 'Ecommerce - Order List',
        },
        {
          url: { name: 'apps-ecommerce-order-details-id', params: { id: '9042' } },
          icon: 'tabler-list-check',
          title: 'Ecommerce - Order Details',
        },
        {
          url: { name: 'apps-ecommerce-customer-list' },
          icon: 'tabler-user',
          title: 'Ecommerce - Customer List',
        },
        {
          url: { name: 'apps-ecommerce-customer-details-id', params: { id: '478426', tab: 'security' } },
          icon: 'tabler-list',
          title: 'Ecommerce - Customer Details',
        },
        {
          url: { name: 'apps-ecommerce-manage-review' },
          icon: 'tabler-quote',
          title: 'Ecommerce - Manage Review',
        },
        {
          url: { name: 'apps-ecommerce-referrals' },
          icon: 'tabler-users-group',
          title: 'Ecommerce - Referrals',
        },
        {
          url: { name: 'apps-ecommerce-settings' },
          icon: 'tabler-settings',
          title: 'Ecommerce - Settings',
        },
        {
          url: { name: 'apps-user-list' },
          icon: 'tabler-users-group',
          title: 'User List',
        },
        {
          url: { name: 'apps-user-view-id', params: { id: 21 } },
          icon: 'tabler-eye',
          title: 'User View',
        },
        {
          url: { name: 'pages-user-profile-tab', params: { tab: 'profile' } },
          icon: 'tabler-user-circle',
          title: 'User Profile - Profile',
        },
        {
          url: { name: 'pages-account-settings-tab', params: { tab: 'account' } },
          icon: 'tabler-user-circle',
          title: 'Account Settings - Account',
        },
        {
          url: { name: 'pages-account-settings-tab', params: { tab: 'security' } },
          icon: 'tabler-lock-open',
          title: 'Account Settings - Security',
        },
        {
          url: { name: 'pages-account-settings-tab', params: { tab: 'billing-plans' } },
          icon: 'tabler-currency-dollar',
          title: 'Account Settings - Billing',
        },
        {
          url: { name: 'pages-account-settings-tab', params: { tab: 'notification' } },
          icon: 'tabler-bell',
          title: 'Account Settings - Notifications',
        },
        {
          url: { name: 'pages-account-settings-tab', params: { tab: 'connection' } },
          icon: 'tabler-link',
          title: 'Account Settings - Connections',
        },
        {
          url: { name: 'pages-pricing' },
          icon: 'tabler-currency-dollar',
          title: 'Pricing',
        },
        {
          url: { name: 'pages-faq' },
          icon: 'tabler-help-circle',
          title: 'FAQ',
        },
        {
          url: { name: 'pages-misc-coming-soon' },
          icon: 'tabler-clock',
          title: 'Coming Soon',
        },
        {
          url: { name: 'pages-misc-under-maintenance' },
          icon: 'tabler-settings',
          title: 'Under Maintenance',
        },
        {
          url: { path: '/pages/misc/page-not-found' },
          icon: 'tabler-alert-circle',
          title: 'Page Not Found - 404',
        },
        {
          url: { path: '/pages/misc/not-authorized' },
          icon: 'tabler-user-x',
          title: 'Not Authorized - 401',
        },
        {
          url: { name: 'pages-authentication-login-v1' },
          icon: 'tabler-login',
          title: 'Login V1',
        },
        {
          url: { name: 'pages-authentication-login-v2' },
          icon: 'tabler-login',
          title: 'Login V2',
        },
        {
          url: { name: 'pages-authentication-register-v1' },
          icon: 'tabler-user-plus',
          title: 'Register V1',
        },
        {
          url: { name: 'pages-authentication-register-v2' },
          icon: 'tabler-user-plus',
          title: 'Register V2',
        },
        {
          icon: 'tabler-mail',
          title: 'Verify Email V1',
          url: { name: 'pages-authentication-verify-email-v1' },
        },
        {
          icon: 'tabler-mail',
          title: 'Verify Email V2',
          url: { name: 'pages-authentication-verify-email-v2' },
        },
        {
          url: { name: 'pages-authentication-forgot-password-v1' },
          icon: 'tabler-lock-exclamation',
          title: 'Forgot Password V1',
        },
        {
          url: { name: 'pages-authentication-forgot-password-v2' },
          icon: 'tabler-lock-exclamation',
          title: 'Forgot Password V2',
        },
        {
          url: { name: 'pages-authentication-reset-password-v1' },
          icon: 'tabler-help-circle',
          title: 'Reset Password V1',
        },
        {
          url: { name: 'pages-authentication-reset-password-v2' },
          icon: 'tabler-help-circle',
          title: 'Reset Password V2',
        },
        {
          icon: 'tabler-devices',
          title: 'Two Steps V1',
          url: { name: 'pages-authentication-two-steps-v1' },
        },
        {
          icon: 'tabler-devices',
          title: 'Two Steps V2',
          url: { name: 'pages-authentication-two-steps-v2' },
        },
        {
          url: { name: 'pages-dialog-examples' },
          icon: 'tabler-square',
          title: 'Dialog Examples',
        },
        {
          url: { name: 'pages-authentication-register-multi-steps' },
          icon: 'tabler-user-plus',
          title: 'Register Multi-Steps',
        },
        {
          url: { name: 'wizard-examples-checkout' },
          icon: 'tabler-shopping-cart',
          title: 'Wizard - Checkout',
        },
        {
          url: { name: 'wizard-examples-create-deal' },
          icon: 'tabler-gift',
          title: 'Wizard - create deal',
        },
        {
          url: { name: 'wizard-examples-property-listing' },
          icon: 'tabler-home',
          title: 'Wizard - Property Listing',
        },
        {
          url: { name: 'apps-roles' },
          icon: 'tabler-shield-checkered',
          title: 'Roles',
        },
        {
          url: { name: 'apps-permissions' },
          icon: 'tabler-shield-checkered',
          title: 'Permissions',
        },
      ],
    },
  ],
}
