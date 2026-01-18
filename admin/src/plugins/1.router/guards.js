import { canNavigate } from '@layouts/plugins/casl'

export const setupGuards = router => {
  // 👉 router.beforeEach
  // Docs: https://router.vuejs.org/guide/advanced/navigation-guards.html#global-before-guards
  router.beforeEach(to => {
    /*
         * If it's a public route, continue navigation. This kind of pages are allowed to visited by login & non-login users. Basically, without any restrictions.
         * Examples of public routes are, 404, under maintenance, etc.
         */
    if (to.meta.public)
      return

    /**
         * Check if user is logged in by checking if token & user data exists in local storage
         * Feel free to update this logic to suit your needs
         */
    const isLoggedIn = !!(useCookie('userData').value && useCookie('accessToken').value)

    /*
          If user is logged in and is trying to access login like page, redirect to home
          else allow visiting the page
          (WARN: Don't allow executing further by return statement because next code will check for permissions)
         */
    if (to.meta.unauthenticatedOnly) {
      if (isLoggedIn)
        return '/'
      else
        return undefined
    }
    // Проверяем права доступа только если пользователь залогинен
    if (isLoggedIn && to.matched.length) {
      const canAccess = canNavigate(to)
      if (!canAccess) {
        // Если пользователь залогинен, но нет прав - показываем страницу not-authorized
        // Но только если маршрут действительно требует прав доступа
        const hasPermissionRequirement = to.matched.some(route => route.meta?.action && route.meta?.subject)
        if (hasPermissionRequirement) {
          return { name: 'not-authorized' }
        }
        // Если нет требований к правам, но canNavigate вернул false - разрешаем доступ
        // Это может произойти если ability не настроен
      }
    } else if (!isLoggedIn && !to.meta?.public && to.matched.length) {
      // Если пользователь не залогинен и маршрут не публичный - редирект на логин
      return {
        name: 'login',
        query: {
          ...to.query,
          to: to.fullPath !== '/' ? to.path : undefined,
        },
      }
    }
    
    // Продолжаем навигацию
    return undefined
  })
}
