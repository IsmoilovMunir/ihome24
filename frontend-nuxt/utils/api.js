/** Совместимость со старыми импортами: делегирует в Nuxt plugin */
export function useLegacyApi() {
  const nuxtApp = useNuxtApp()
  return {
    default: nuxtApp.$api,
    productApi: nuxtApp.$productApi,
    categoryApi: nuxtApp.$categoryApi,
    authApi: nuxtApp.$authApi,
    orderApi: nuxtApp.$orderApi,
    geoApi: nuxtApp.$geoApi,
    settingsApi: nuxtApp.$settingsApi,
    cartApi: nuxtApp.$cartApi,
    fileApi: nuxtApp.$fileApi,
  }
}

// Lazy proxies for store imports (only valid inside actions after app init)
export const productApi = {
  getAll: () => useNuxtApp().$productApi.getAll(),
  getById: (id) => useNuxtApp().$productApi.getById(id),
}
export const categoryApi = {
  getAll: () => useNuxtApp().$categoryApi.getAll(),
  getById: (id) => useNuxtApp().$categoryApi.getById(id),
}
export const authApi = {
  login: (c) => useNuxtApp().$authApi.login(c),
  register: (u) => useNuxtApp().$authApi.register(u),
  logout: () => useNuxtApp().$authApi.logout(),
  getMe: () => useNuxtApp().$authApi.getMe(),
  updateMe: (u) => useNuxtApp().$authApi.updateMe(u),
  sendSmsCode: (p) => useNuxtApp().$authApi.sendSmsCode(p),
  verifySmsCode: (p, c) => useNuxtApp().$authApi.verifySmsCode(p, c),
  completeRegistration: (d) => useNuxtApp().$authApi.completeRegistration(d),
  uploadAvatar: (f) => useNuxtApp().$authApi.uploadAvatar(f),
}
export const orderApi = {
  create: (d) => useNuxtApp().$orderApi.create(d),
  list: (p) => useNuxtApp().$orderApi.list(p),
  listMy: (p) => useNuxtApp().$orderApi.listMy(p),
  getById: (id) => useNuxtApp().$orderApi.getById(id),
}
export const geoApi = {
  getLocation: () => useNuxtApp().$geoApi.getLocation(),
}
export const settingsApi = {
  getPriceTiers: () => useNuxtApp().$settingsApi.getPriceTiers(),
}
export const checkoutApi = {
  saveLead: (data, issueToken) => useNuxtApp().$checkoutApi.saveLead(data, issueToken),
}
export const cartApi = {
  validate: (items) => useNuxtApp().$cartApi.validate(items),
}
export const fileApi = {
  getFileUrl: (p) => useNuxtApp().$fileApi.getFileUrl(p),
  getImageUrlLarge: (p) => useNuxtApp().$fileApi.getImageUrlLarge(p),
  getImageUrlBySize: (p, s) => useNuxtApp().$fileApi.getImageUrlBySize(p, s),
  getImageSrcSet: (p) => useNuxtApp().$fileApi.getImageSrcSet(p),
  getImageUrlOriginal: (p) => useNuxtApp().$fileApi.getImageUrlOriginal(p),
}
export default {
  get: (...args) => useNuxtApp().$api.get(...args),
  post: (...args) => useNuxtApp().$api.post(...args),
  put: (...args) => useNuxtApp().$api.put(...args),
  delete: (...args) => useNuxtApp().$api.delete(...args),
}
