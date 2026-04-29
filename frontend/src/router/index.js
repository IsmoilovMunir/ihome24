import { createRouter, createWebHistory } from 'vue-router'
const Home = () => import('../pages/Home.vue')
const Products = () => import('../pages/Products.vue')
const ProductDetail = () => import('../pages/ProductDetail.vue')
const Cart = () => import('../pages/Cart.vue')
const Checkout = () => import('../pages/Checkout.vue')
const Login = () => import('../pages/Login.vue')
const Register = () => import('../pages/Register.vue')
const Search = () => import('../pages/Search.vue')
const Contacts = () => import('../pages/Contacts.vue')
const Oferta = () => import('../pages/Oferta.vue')
const PersonalLayout = () => import('../layouts/PersonalLayout.vue')
const PersonalIndex = () => import('../pages/PersonalIndex.vue')
const PersonalProfile = () => import('../pages/PersonalProfile.vue')
const PersonalOrders = () => import('../pages/PersonalOrders.vue')
const PersonalPlaceholder = () => import('../pages/PersonalPlaceholder.vue')
const OrderTracking = () => import('../pages/OrderTracking.vue')

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home,
    meta: {
      title: 'iHome24 - Интернет-магазин товаров для дома',
      description: 'iHome24 - интернет-магазин товаров для дома и офиса. Каталог, выгодные цены, доставка.',
      robots: 'index, follow',
    },
  },
  {
    path: '/products',
    name: 'Products',
    component: Products,
    meta: {
      title: 'Каталог товаров - iHome24',
      description: 'Каталог товаров iHome24: товары для дома и офиса с актуальными ценами и наличием.',
      robots: 'index, follow',
    },
  },
  /* Каталог по ЧПУ: /category/slug_родителя/slug_ребёнка/… (тот же компонент, что и /products) */
  {
    path: '/category/:parentSlug/:childSlug/:grandSlug',
    name: 'ProductsCategory3',
    component: Products,
    meta: {
      title: 'Каталог товаров - iHome24',
      description: 'Категории каталога iHome24: подбор товаров для дома и офиса.',
      robots: 'index, follow',
    },
  },
  {
    path: '/category/:parentSlug/:childSlug',
    name: 'ProductsCategory2',
    component: Products,
    meta: {
      title: 'Каталог товаров - iHome24',
      description: 'Категории каталога iHome24: подбор товаров для дома и офиса.',
      robots: 'index, follow',
    },
  },
  {
    path: '/category/:parentSlug',
    name: 'ProductsCategory1',
    component: Products,
    meta: {
      title: 'Каталог товаров - iHome24',
      description: 'Категории каталога iHome24: подбор товаров для дома и офиса.',
      robots: 'index, follow',
    },
  },
  {
    path: '/products/:id/:variantSku?',
    name: 'ProductDetail',
    component: ProductDetail,
    meta: {
      title: 'Карточка товара - iHome24',
      description: 'Подробная информация о товаре, характеристики, наличие и цена в iHome24.',
      robots: 'index, follow',
    },
  },
  {
    path: '/cart',
    name: 'Cart',
    component: Cart,
    meta: {
      title: 'Корзина - iHome24',
      description: 'Корзина покупателя iHome24.',
      robots: 'noindex, nofollow',
    },
  },
  {
    path: '/checkout',
    name: 'Checkout',
    component: Checkout,
    meta: {
      title: 'Оформление заказа - iHome24',
      description: 'Оформление заказа в iHome24.',
      robots: 'noindex, nofollow',
    },
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: {
      title: 'Вход - iHome24',
      description: 'Вход в личный кабинет iHome24.',
      robots: 'noindex, nofollow',
    },
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: {
      title: 'Регистрация - iHome24',
      description: 'Регистрация в iHome24.',
      robots: 'noindex, nofollow',
    },
  },
  {
    path: '/search',
    name: 'Search',
    component: Search,
    meta: {
      title: 'Поиск - iHome24',
      description: 'Поиск по каталогу iHome24.',
      robots: 'noindex, nofollow',
    },
  },
  {
    path: '/support/contacts',
    name: 'Contacts',
    component: Contacts,
    meta: {
      title: 'Контакты - iHome24',
      description: 'Контакты iHome24: способы связи и поддержка.',
      robots: 'index, follow',
    },
  },
  {
    path: '/support/oferta',
    name: 'Oferta',
    component: Oferta,
    meta: {
      title: 'Оферта - iHome24',
      description: 'Публичная оферта интернет-магазина iHome24.',
      robots: 'index, follow',
    },
  },
  // Личный кабинет (с layout и sidebar)
  {
    path: '/personal',
    component: PersonalLayout,
    children: [
      { path: '', name: 'Personal', component: PersonalIndex, meta: { robots: 'noindex, nofollow', title: 'Личный кабинет - iHome24' } },
      { path: 'orders', name: 'PersonalOrders', component: PersonalOrders, meta: { robots: 'noindex, nofollow', title: 'Мои заказы - iHome24' } },
      { path: 'favorites', name: 'PersonalFavorites', component: PersonalPlaceholder, meta: { robots: 'noindex, nofollow', title: 'Избранное - iHome24' } },
      { path: 'profile', name: 'PersonalProfile', component: PersonalProfile, meta: { robots: 'noindex, nofollow', title: 'Профиль - iHome24' } },
    ],
  },
  { path: '/order-tracking', name: 'OrderTracking', component: OrderTracking, meta: { title: 'Отслеживание заказа - iHome24', description: 'Проверка статуса заказа iHome24.', robots: 'index, follow' } },
  { path: '/services', name: 'Services', component: PersonalPlaceholder, meta: { title: 'Сервисы - iHome24', description: 'Сервисы и услуги iHome24.', robots: 'index, follow' } },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) return savedPosition
    if (to.hash) return { el: to.hash, behavior: 'smooth' }
    return { left: 0, top: 0 }
  },
})

const SEO_DEFAULTS = {
  siteUrl: import.meta.env.VITE_SITE_URL || 'https://ihome24.ru',
  title: 'iHome24 - Интернет-магазин',
  description: 'iHome24 - интернет-магазин товаров для дома и офиса.',
  robots: 'index, follow',
}

function upsertMetaTag(attrName, attrValue, content) {
  if (!content) return
  let tag = document.head.querySelector(`meta[${attrName}="${attrValue}"]`)
  if (!tag) {
    tag = document.createElement('meta')
    tag.setAttribute(attrName, attrValue)
    document.head.appendChild(tag)
  }
  tag.setAttribute('content', content)
}

function updateSocialMeta({ title, description, url, image }) {
  upsertMetaTag('property', 'og:type', 'website')
  upsertMetaTag('property', 'og:site_name', 'iHome24')
  upsertMetaTag('property', 'og:title', title)
  upsertMetaTag('property', 'og:description', description)
  upsertMetaTag('property', 'og:url', url)
  if (image) upsertMetaTag('property', 'og:image', image)

  upsertMetaTag('name', 'twitter:card', image ? 'summary_large_image' : 'summary')
  upsertMetaTag('name', 'twitter:title', title)
  upsertMetaTag('name', 'twitter:description', description)
  if (image) upsertMetaTag('name', 'twitter:image', image)
}

function setCanonical(url) {
  let canonical = document.head.querySelector('link[rel="canonical"]')
  if (!canonical) {
    canonical = document.createElement('link')
    canonical.setAttribute('rel', 'canonical')
    document.head.appendChild(canonical)
  }
  canonical.setAttribute('href', url)
}

function absoluteCanonical(to) {
  const base = SEO_DEFAULTS.siteUrl.replace(/\/$/, '')
  const cleanPath = to.path || '/'
  return `${base}${cleanPath}`
}

router.afterEach((to) => {
  const title = to.meta?.title || SEO_DEFAULTS.title
  const description = to.meta?.description || SEO_DEFAULTS.description
  const robots = to.meta?.robots || SEO_DEFAULTS.robots
  const canonical = to.meta?.canonical || absoluteCanonical(to)

  document.title = title
  upsertMetaTag('name', 'description', description)
  upsertMetaTag('name', 'robots', robots)
  setCanonical(canonical)
  updateSocialMeta({ title, description, url: canonical })
})

export default router
