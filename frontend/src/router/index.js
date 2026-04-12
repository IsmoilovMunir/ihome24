import { createRouter, createWebHistory } from 'vue-router'
import Home from '../pages/Home.vue'
import Products from '../pages/Products.vue'
import ProductDetail from '../pages/ProductDetail.vue'
import Cart from '../pages/Cart.vue'
import Checkout from '../pages/Checkout.vue'
import Login from '../pages/Login.vue'
import Register from '../pages/Register.vue'
import Search from '../pages/Search.vue'
import Contacts from '../pages/Contacts.vue'
import Oferta from '../pages/Oferta.vue'
import PersonalLayout from '../layouts/PersonalLayout.vue'
import PersonalIndex from '../pages/PersonalIndex.vue'
import PersonalProfile from '../pages/PersonalProfile.vue'
import PersonalOrders from '../pages/PersonalOrders.vue'
import PersonalPlaceholder from '../pages/PersonalPlaceholder.vue'
import OrderTracking from '../pages/OrderTracking.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home,
  },
  {
    path: '/products',
    name: 'Products',
    component: Products,
  },
  /* Каталог по ЧПУ: /category/slug_родителя/slug_ребёнка/… (тот же компонент, что и /products) */
  {
    path: '/category/:parentSlug/:childSlug/:grandSlug',
    name: 'ProductsCategory3',
    component: Products,
  },
  {
    path: '/category/:parentSlug/:childSlug',
    name: 'ProductsCategory2',
    component: Products,
  },
  {
    path: '/category/:parentSlug',
    name: 'ProductsCategory1',
    component: Products,
  },
  {
    path: '/products/:id/:variantSku?',
    name: 'ProductDetail',
    component: ProductDetail,
  },
  {
    path: '/cart',
    name: 'Cart',
    component: Cart,
  },
  {
    path: '/checkout',
    name: 'Checkout',
    component: Checkout,
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
  },
  {
    path: '/search',
    name: 'Search',
    component: Search,
  },
  {
    path: '/support/contacts',
    name: 'Contacts',
    component: Contacts,
  },
  {
    path: '/support/oferta',
    name: 'Oferta',
    component: Oferta,
  },
  // Личный кабинет (с layout и sidebar)
  {
    path: '/personal',
    component: PersonalLayout,
    children: [
      { path: '', name: 'Personal', component: PersonalIndex },
      { path: 'orders', name: 'PersonalOrders', component: PersonalOrders },
      { path: 'favorites', name: 'PersonalFavorites', component: PersonalPlaceholder },
      { path: 'profile', name: 'PersonalProfile', component: PersonalProfile },
    ],
  },
  { path: '/order-tracking', name: 'OrderTracking', component: OrderTracking },
  { path: '/services', name: 'Services', component: PersonalPlaceholder },
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

export default router
