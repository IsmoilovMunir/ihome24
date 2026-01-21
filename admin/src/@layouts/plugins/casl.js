import { useAbility } from '@casl/vue'

/**
 * Returns ability result if ACL is configured or else just return true
 * We should allow passing string | undefined to can because for admin ability we omit defining action & subject
 *
 * Useful if you don't know if ACL is configured or not
 * Used in @core files to handle absence of ACL without errors
 *
 * @param {string} action CASL Actions // https://casl.js.org/v4/en/guide/intro#basics
 * @param {string} subject CASL Subject // https://casl.js.org/v4/en/guide/intro#basics
 */
export const can = (action, subject) => {
  const vm = getCurrentInstance()
  if (!vm)
    return false
  const localCan = vm.proxy && '$can' in vm.proxy
    
  return localCan ? vm.proxy?.$can(action, subject) : true
}

/**
 * Check if user can view item based on it's ability
 * Based on item's action and subject & Hide group if all of it's children are hidden
 * @param {object} item navigation object item
 */
export const canViewNavMenuGroup = item => {
  const hasAnyVisibleChild = item.children.some(i => can(i.action, i.subject))

  // If subject and action is defined in item => Return based on children visibility (Hide group if no child is visible)
  // Else check for ability using provided subject and action along with checking if has any visible child
  if (!(item.action && item.subject))
    return hasAnyVisibleChild
  
  return can(item.action, item.subject) && hasAnyVisibleChild
}
export const canNavigate = to => {
  const ability = useAbility()

  // Если маршрут публичный, разрешаем доступ
  if (to.meta?.public) {
    return true
  }

  // Get the most specific route (last one in the matched array)
  const targetRoute = to.matched[to.matched.length - 1]

  // Если у маршрута нет требований к правам доступа, разрешаем доступ
  if (!targetRoute?.meta?.action && !targetRoute?.meta?.subject) {
    // Проверяем, есть ли у родительских маршрутов требования
    const hasAnyPermissionRequirement = to.matched.some(route => route.meta?.action && route.meta?.subject)
    if (!hasAnyPermissionRequirement) {
      return true // Нет требований к правам - разрешаем доступ
    }
  }

  // Проверяем, инициализирован ли ability
  // Если ability не настроен или пуст, разрешаем доступ (fallback для совместимости)
  try {
    // Если у маршрута есть требования к правам, проверяем их
    if (targetRoute?.meta?.action && targetRoute?.meta?.subject) {
      const canAccess = ability.can(targetRoute.meta.action, targetRoute.meta.subject)
      // Если ability не настроен (возвращает false для всех), но у нас есть пользователь - разрешаем
      // Это временная мера до полной настройки CASL
      if (!canAccess && ability.rules && ability.rules.length === 0) {
        return true // Ability не настроен - разрешаем доступ
      }
      return canAccess
    }

    // Проверяем родительские маршруты
    const parentCanAccess = to.matched.some(route => {
      if (route.meta?.action && route.meta?.subject) {
        return ability.can(route.meta.action, route.meta.subject)
      }
      return false
    })
    
    // Если ability не настроен, разрешаем доступ
    if (!parentCanAccess && ability.rules && ability.rules.length === 0) {
      return true
    }
    
    return parentCanAccess
  } catch (error) {
    // В случае ошибки разрешаем доступ (fallback)
    console.warn('Error checking permissions:', error)
    return true
  }
}
