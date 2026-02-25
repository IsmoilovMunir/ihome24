<template>
  <div class="personal page-content">
    <div class="personal__container">
      <!-- Боковое меню -->
      <aside class="personal__sidebar">
        <nav class="personal-nav">
          <div class="personal-menu-desktop">
            <div class="personal-menu-desktop__list">
              <router-link
                to="/personal"
                class="personal-menu-desktop__item"
                :class="{ 'personal-menu-desktop__item--active': route.path === '/personal' }"
              >
                <svg class="personal-menu-desktop__icon" width="24" height="24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="m9.268 3.603-4.94 3.85c-.825.642-1.495 2.008-1.495 3.044v6.792c0 2.127 1.733 3.869 3.86 3.869h10.614a3.871 3.871 0 0 0 3.86-3.86v-6.673c0-1.11-.743-2.53-1.65-3.162l-5.665-3.97c-1.284-.898-3.346-.852-4.584.11zM12 17.49v-2.75" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                Личный кабинет
              </router-link>
              <router-link
                to="/personal/orders"
                class="personal-menu-desktop__item"
                :class="{ 'personal-menu-desktop__item--active': route.path === '/personal/orders' }"
              >
                <svg class="personal-menu-desktop__icon" width="24" height="24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M9.5 22h6c4.02 0 4.74-1.61 4.95-3.57l.75-6C21.47 9.99 20.77 8 16.5 8h-8c-4.27 0-4.97 1.99-4.7 4.43l.75 6C4.76 20.39 5.48 22 9.5 22zM8 7.67V6.7c0-2.25 1.81-4.46 4.06-4.67A4.5 4.5 0 0 1 17 6.51v1.38" stroke="currentColor" stroke-width="1.5" stroke-miterlimit="10" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                Заказы
              </router-link>
              <router-link
                to="/personal/favorites"
                class="personal-menu-desktop__item"
                :class="{ 'personal-menu-desktop__item--active': route.path === '/personal/favorites' }"
              >
                <svg class="personal-menu-desktop__icon" width="24" height="24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M12.62 20.81C12.28 20.93 11.72 20.93 11.38 20.81C8.48 19.82 2 15.69 2 8.68998C2 5.59998 4.49 3.09998 7.56 3.09998C9.38 3.09998 10.99 3.97998 12 5.33998C13.01 3.97998 14.63 3.09998 16.44 3.09998C19.51 3.09998 22 5.59998 22 8.68998C22 15.69 15.52 19.82 12.62 20.81Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                Избранное
              </router-link>
              <router-link
                to="/personal/profile"
                class="personal-menu-desktop__item"
                :class="{ 'personal-menu-desktop__item--active': route.path === '/personal/profile' }"
              >
                <svg class="personal-menu-desktop__icon" width="24" height="24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M12.5 12a5 5 0 1 0 0-10 5 5 0 0 0 0 10zM21.09 22c0-3.87-3.85-7-8.59-7s-8.59 3.13-8.59 7" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                Личные данные
              </router-link>
              <router-link
                to="/order-tracking"
                class="personal-menu-desktop__item"
                :class="{ 'personal-menu-desktop__item--active': route.path === '/order-tracking' }"
              >
                <svg class="personal-menu-desktop__icon" width="24" height="24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M12.2366 19.5C16.3787 19.5 19.7366 16.1421 19.7366 12C19.7366 7.85786 16.3787 4.5 12.2366 4.5C8.09443 4.5 4.73657 7.85786 4.73657 12C4.73657 16.1421 8.09443 19.5 12.2366 19.5Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M12.2366 15C13.8935 15 15.2366 13.6569 15.2366 12C15.2366 10.3431 13.8935 9 12.2366 9C10.5797 9 9.23657 10.3431 9.23657 12C9.23657 13.6569 10.5797 15 12.2366 15Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M12.2366 4V2" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M4.23657 12H2.23657" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M12.2366 20V22" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M20.2366 12H22.2366" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                Где мой заказ?
              </router-link>
            </div>
          </div>
        </nav>
      </aside>

      <!-- Контент -->
      <main class="personal__content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router'

const route = useRoute()
</script>

<style scoped>
.personal {
  min-height: calc(100vh - 140px);
  padding: 24px 0 48px;
  font-family: "helvetica", sans-serif;
}

.personal__container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 20px;
  display: grid;
  grid-template-columns: 260px 1fr;
  gap: 32px;
}

@media (max-width: 900px) {
  .personal__container {
    grid-template-columns: 1fr;
  }

  .personal__sidebar {
    display: none;
  }
}

.personal__sidebar {
  position: sticky;
  top: 24px;
  height: fit-content;
}

.personal-nav {
  background: #2E2826;
  border-radius: 12px;
  padding: 12px;
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.personal-menu-desktop__list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.personal-menu-desktop__item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  border-radius: 8px;
  transition: background 0.2s, color 0.2s;
}

.personal-menu-desktop__item:hover {
  background: rgba(255, 255, 255, 0.06);
  color: #fff;
}

.personal-menu-desktop__item--active {
  background: rgba(244, 116, 39, 0.15);
  color: #F47427;
}

.personal-menu-desktop__icon {
  flex-shrink: 0;
  color: inherit;
}

.personal__content {
  min-width: 0;
}
</style>
