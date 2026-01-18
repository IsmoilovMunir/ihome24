import { createMongoAbility } from '@casl/ability'
import { abilitiesPlugin } from '@casl/vue'

export default function (app) {
  const userAbilityRules = useCookie('userAbilityRules')
  
  // Обеспечиваем правильный формат правил
  let rules = userAbilityRules.value ?? []
  
  // Если rules это объект, преобразуем в массив
  if (rules && !Array.isArray(rules)) {
    rules = [rules]
  }
  
  // Если rules пустой массив или undefined, создаем правила по умолчанию для совместимости
  if (!rules || rules.length === 0) {
    // Правила по умолчанию - полный доступ (для случаев, когда ability rules еще не загружены)
    rules = [{ action: 'manage', subject: 'all' }]
  }
  
  const initialAbility = createMongoAbility(rules)

  app.use(abilitiesPlugin, initialAbility, {
    useGlobalProperties: true,
  })
}
