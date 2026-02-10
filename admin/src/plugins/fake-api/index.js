import { setupWorker } from 'msw/browser'

// Handlers
import { handlerAppBarSearch } from '@db/app-bar-search/index'
import { handlerAppsAcademy } from '@db/apps/academy/index'
import { handlerAppsCalendar } from '@db/apps/calendar/index'
import { handlerAppsChat } from '@db/apps/chat/index'
import { handlerAppsEcommerce } from '@db/apps/ecommerce/index'
import { handlerAppsEmail } from '@db/apps/email/index'
import { handlerAppsInvoice } from '@db/apps/invoice/index'
import { handlerAppsKanban } from '@db/apps/kanban/index'
import { handlerAppLogistics } from '@db/apps/logistics/index'
// Исключаем handlers для пользователей, ролей, прав и аутентификации - они работают через реальный бэкенд
// import { handlerAppsPermission } from '@db/apps/permission/index'
// import { handlerAppsUsers } from '@db/apps/users/index'
// import { handlerAuth } from '@db/auth/index'
import { handlerDashboard } from '@db/dashboard/index'
import { handlerPagesDatatable } from '@db/pages/datatable/index'
import { handlerPagesFaq } from '@db/pages/faq/index'
import { handlerPagesHelpCenter } from '@db/pages/help-center/index'
import { handlerPagesProfile } from '@db/pages/profile/index'

// Исключаем handlers для пользователей, ролей, прав и аутентификации из MSW
// Эти endpoints теперь работают через реальный бэкенд
const worker = setupWorker(...handlerAppsEcommerce, ...handlerAppsAcademy, ...handlerAppsInvoice, /* ...handlerAppsUsers, */ ...handlerAppsEmail, ...handlerAppsCalendar, ...handlerAppsChat, /* ...handlerAppsPermission, */ ...handlerPagesHelpCenter, ...handlerPagesProfile, ...handlerPagesFaq, ...handlerPagesDatatable, ...handlerAppBarSearch, ...handlerAppLogistics, /* ...handlerAuth, */ ...handlerAppsKanban, ...handlerDashboard)
export default function () {
  // MSW по умолчанию отключён (баг "Cannot read properties of undefined (reading 'url')").
  // Включить: VITE_ENABLE_MSW=true — для демо-данных (dashboard, email, kanban).
  if (import.meta.env.VITE_ENABLE_MSW !== 'true') {
    if (typeof navigator !== 'undefined' && navigator.serviceWorker) {
      navigator.serviceWorker.getRegistrations().then(regs => {
        regs.forEach(reg => {
          if (reg.active?.scriptURL?.includes('mockServiceWorker'))
            reg.unregister()
        })
      })
    }
    return
  }

  // Only load fake API in development mode to improve performance
  if (import.meta.env.DEV) {
    const workerUrl = `${import.meta.env.BASE_URL ?? '/'}mockServiceWorker.js`

    worker.start({
      serviceWorker: {
        url: workerUrl,
      },
      // Пропускаем необработанные запросы к реальному бэкенду
      onUnhandledRequest: 'bypass',
    })
  }
}
