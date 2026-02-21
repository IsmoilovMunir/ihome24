<template>
  <nav class="mobile-bottom-menu">
    <router-link to="/" class="mobile-menu-item">
      <svg
        class="mobile-menu-icon"
        fill="none"
        stroke="currentColor"
        viewBox="0 0 24 24"
      >
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"
        />
      </svg>
      <span class="mobile-menu-label">Главная</span>
    </router-link>

    <router-link to="/products" class="mobile-menu-item">
      <svg
        class="mobile-menu-icon"
        fill="none"
        stroke="currentColor"
        viewBox="0 0 24 24"
      >
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z"
        />
      </svg>
      <span class="mobile-menu-label">Каталог</span>
    </router-link>

    <router-link to="/cart" class="mobile-menu-item">
      <div class="mobile-menu-icon-wrapper">
        <svg
          class="mobile-menu-icon"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z"
          />
        </svg>
        <span
          v-if="cartStore.totalItems > 0"
          class="mobile-menu-badge"
        >
          {{ cartStore.totalItems }}
        </span>
      </div>
      <span class="mobile-menu-label">Корзина</span>
    </router-link>

    <button
      type="button"
      class="mobile-menu-item mobile-menu-item--button"
      @click.stop="personalMenuStore.openMenu"
    >
      <svg
        class="mobile-menu-icon"
        fill="none"
        stroke="currentColor"
        viewBox="0 0 24 24"
      >
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
        />
      </svg>
      <span class="mobile-menu-label">Кабинет</span>
    </button>
  </nav>
</template>

<script setup>
import { useCartStore } from '../stores/cart'
import { usePersonalMenuStore } from '../stores/personalMenu'

const cartStore = useCartStore()
const personalMenuStore = usePersonalMenuStore()
</script>

<style scoped>
.mobile-bottom-menu {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: none;
  justify-content: space-around;
  align-items: center;
  background: rgba(46, 40, 38, 0.4);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  padding: 8px 0;
  z-index: 10000;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.3);
}

@media screen and (max-width: 768px) {
  .mobile-bottom-menu {
    display: flex;
  }
}

.mobile-menu-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  padding: 8px 4px;
  color: #9d9390;
  text-decoration: none;
  transition: color 0.3s;
  position: relative;
}

.mobile-menu-item:hover,
.mobile-menu-item.router-link-active {
  color: #fff;
}

.mobile-menu-item--button {
  background: none;
  border: none;
  cursor: pointer;
  font: inherit;
}

.mobile-menu-icon-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.mobile-menu-icon {
  width: 24px;
  height: 24px;
  margin-bottom: 4px;
}

.mobile-menu-badge {
  position: absolute;
  top: -8px;
  right: -8px;
  background: #F47427;
  color: #fff;
  font-size: 10px;
  font-weight: bold;
  border-radius: 50%;
  width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.mobile-menu-label {
  font-size: 11px;
  font-family: "helvetica", sans-serif;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  font-weight: 500;
}
</style>
