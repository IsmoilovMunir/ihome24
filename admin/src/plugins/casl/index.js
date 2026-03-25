import { createMongoAbility } from '@casl/ability'
import { abilitiesPlugin } from '@casl/vue'
import { watch } from 'vue'

export default function (app) {
  const userAbilityRules = useCookie('userAbilityRules')

  const normalizeRules = raw => {
    let rules = raw ?? []
    if (rules && !Array.isArray(rules))
      rules = [rules]
    // deny-by-default: empty => no abilities
    return Array.isArray(rules) ? rules : []
  }

  const initialAbility = createMongoAbility(normalizeRules(userAbilityRules.value))

  app.use(abilitiesPlugin, initialAbility, {
    useGlobalProperties: true,
  })

  // Keep ability in sync with cookie changes (e.g. after role update + /auth/me poll)
  watch(
    () => userAbilityRules.value,
    newVal => {
      try {
        initialAbility.update(normalizeRules(newVal))
      } catch (e) {
        // Avoid breaking navigation if CASL update fails for some reason
      }
    },
    { deep: true },
  )
}
