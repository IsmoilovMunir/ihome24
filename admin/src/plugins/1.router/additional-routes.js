const emailRouteComponent = () => import('@/pages/apps/email/index.vue')

// 👉 Redirects
export const redirects = [
  // ℹ️ We are redirecting to different pages based on role.
  // NOTE: Role is just for UI purposes. ACL is based on abilities.
  {
    path: '/',
    name: 'index',
    redirect: to => {
      // Redirect by role after login.
      const userData = useCookie('userData')
      const userRole = (userData.value?.role || '').toLowerCase()
      if (userRole === 'admin')
        return { name: 'dashboards-ecommerce' }
      if (userRole === 'manager')
        return { name: 'apps-ecommerce-order-list' }
      if (userRole === 'editor')
        return { name: 'apps-ecommerce-product-list' }
      if (userRole === 'client')
        return { name: 'access-control' }
      
      return { name: 'login', query: to.query }
    },
  },
  {
    path: '/pages/user-profile',
    name: 'pages-user-profile',
    redirect: () => ({ name: 'pages-user-profile-tab', params: { tab: 'profile' } }),
  },
  {
    path: '/pages/account-settings',
    name: 'pages-account-settings',
    redirect: () => ({ name: 'pages-account-settings-tab', params: { tab: 'account' } }),
  },
]
export const routes = [
  // Email filter
  {
    path: '/apps/email/filter/:filter',
    name: 'apps-email-filter',
    component: emailRouteComponent,
    meta: {
      navActiveLink: 'apps-email',
      layoutWrapperClasses: 'layout-content-height-fixed',
    },
  },

  // Email label
  {
    path: '/apps/email/label/:label',
    name: 'apps-email-label',
    component: emailRouteComponent,
    meta: {
      // contentClass: 'email-application',
      navActiveLink: 'apps-email',
      layoutWrapperClasses: 'layout-content-height-fixed',
    },
  },
  {
    path: '/apps/ecommerce/dashboard',
    name: 'apps-ecommerce-dashboard',
    component: () => import('@/pages/dashboards/ecommerce.vue'),
  },
  {
    path: '/apps/ecommerce/product/edit/:id',
    name: 'apps-ecommerce-product-edit',
    component: () => import('@/pages/apps/ecommerce/product/add/index.vue'),
    meta: {
      navActiveLink: 'apps-ecommerce-product',
    },
  },
]
