import { canNavigate } from '@layouts/plugins/casl'

// If backend/proxy hangs, router navigation must not hang forever.
let lastMeCheckAt = 0
let meCheckInFlight = null
const ME_CHECK_TTL_MS = 30_000
const ME_CHECK_TIMEOUT_MS = 7_000

export const setupGuards = router => {
  // 👉 router.beforeEach
  // Docs: https://router.vuejs.org/guide/advanced/navigation-guards.html#global-before-guards
  router.beforeEach(async to => {
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
    const userDataCookie = useCookie('userData')
    const accessTokenCookie = useCookie('accessToken')
    const isLoggedIn = !!(userDataCookie.value && accessTokenCookie.value)

    // If there is an access token, but backend is down / token expired,
    // we should fail fast to login instead of keeping a "half-logged-in" UI.
    // Validate existing access token even on unauthenticated-only pages (e.g. login),
    // otherwise we may redirect into the app with a stale/invalid token.
    if (accessTokenCookie.value && !to.meta?.public && to.matched.length) {
      try {
        const now = Date.now()

        if (now - lastMeCheckAt > ME_CHECK_TTL_MS) {
          if (!meCheckInFlight) {
            meCheckInFlight = $api('/auth/me', { timeout: ME_CHECK_TIMEOUT_MS })
              .finally(() => {
                meCheckInFlight = null
              })
          }

          await meCheckInFlight
          lastMeCheckAt = now
        }
      } catch (error) {
        useCookie('userData').value = null
        useCookie('accessToken').value = null
        useCookie('userAbilityRules').value = null
        if (typeof window !== 'undefined')
          window.localStorage.removeItem('adminLastActivity')

        return {
          name: 'login',
          query: {
            ...to.query,
            reason: 'unauthorized',
            to: to.fullPath !== '/' ? to.path : undefined,
          },
        }
      }
    }

    // 👉 Таймаут неактивности 1 час для админ-панели
    if (isLoggedIn && typeof window !== 'undefined') {
      const MAX_IDLE_MS = 60 * 60 * 1000 // 1 час
      const lastActivity = window.localStorage.getItem('adminLastActivity')
      const now = Date.now()

      if (lastActivity && !Number.isNaN(Number(lastActivity)) && now - Number(lastActivity) > MAX_IDLE_MS) {
        // Сессия просрочена: очищаем данные и отправляем на логин
        useCookie('userData').value = null
        useCookie('accessToken').value = null
        window.localStorage.removeItem('adminLastActivity')

        return {
          name: 'login',
          query: {
            ...to.query,
            reason: 'session_expired',
            to: to.fullPath !== '/' ? to.path : undefined,
          },
        }
      }

      // Обновляем время последней активности
      window.localStorage.setItem('adminLastActivity', String(now))
    }

    // Если профиль пользователя не заполнен, принудительно отправляем в настройки аккаунта
    if (isLoggedIn && to.matched.length) {
      const userData = userDataCookie.value || {}
      const profileIncomplete = !userData.fullName || !userData.email

      const goingToAccountSettings = to.name === 'pages-account-settings-tab'
      const goingToForceChangePassword = to.name === 'force-change-password'

      // If password change required, force redirect
      if (userData.passwordChangeRequired && !goingToForceChangePassword) {
        return { name: 'force-change-password' }
      }

        // Role-based restriction for editor: only products section
        // Normalize role to be case-insensitive.
        const userRole = typeof userData.role === 'string' ? userData.role.toLowerCase().trim() : ''
        if (userRole === 'editor' && !to.meta.public && !to.meta.unauthenticatedOnly) {
        const allowed = new Set([
          'apps-ecommerce-product-list',
          'apps-ecommerce-product-add',
          'apps-ecommerce-product-category-list',
          // allow common routes
          'force-change-password',
          'pages-account-settings-tab',
          'not-authorized',
        ])

        if (!allowed.has(String(to.name))) {
          return { name: 'apps-ecommerce-product-list' }
        }
      }

      if (profileIncomplete && !goingToAccountSettings && !goingToForceChangePassword && !to.meta.public && !to.meta.unauthenticatedOnly) {
        return {
          name: 'pages-account-settings-tab',
          params: { tab: 'account' },
          query: { ...to.query, completeProfile: '1' },
        }
      }
    }

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
      try {
        const canAccess = canNavigate(to)
        if (!canAccess) {
          // Если пользователь залогинен, но нет прав - показываем страницу not-authorized
          // Но только если маршрут действительно требует прав доступа
          const hasPermissionRequirement = to.matched.some(route => route.meta?.action && route.meta?.subject)
          if (hasPermissionRequirement) {
            return { name: 'not-authorized' }
          }
          // Если нет требований к правам, но canNavigate вернул false - разрешаем доступ
          // Это может произойти если ability не настроена
        }
      } catch (error) {
        // Если Ability еще не инициализирована, не ломаем навигацию.
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
