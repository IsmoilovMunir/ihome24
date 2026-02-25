<template>
  <div class="personal-index">
    <div class="personal-index__grid">
      <!-- Карточка пользователя -->
      <router-link to="/personal/profile" class="personal-user-panel">
        <div class="personal-user-panel__avatar">
          <img
            v-if="avatarUrl"
            :src="avatarUrl"
            alt="Аватар"
            class="personal-user-panel__avatar-img"
          />
          <span v-else>{{ avatarLetter }}</span>
        </div>
        <div class="personal-user-panel__info">
          <h2 class="personal-user-panel__name">{{ authStore.user?.fullName || 'Пользователь' }}</h2>
          <div v-if="authStore.user?.phone" class="personal-user-panel__phone">
            {{ formatPhone(authStore.user.phone) }}
          </div>
        </div>
        <svg class="personal-user-panel__arrow" width="24" height="24" viewBox="0 0 25 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M9.24622 3.08002L16.7663 10.6C17.5363 11.37 17.5363 12.63 16.7663 13.4L9.24622 20.92" stroke="currentColor" stroke-width="1.5" stroke-miterlimit="10" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </router-link>

      <!-- Карточки быстрого доступа -->
      <router-link to="/personal/orders" class="personal-dashboard-card">
        <svg class="personal-dashboard-card__icon" width="24" height="24" viewBox="0 0 24 24" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
          <path d="M19.59 7.17999H16.47V5.82999C16.47 4.64447 15.9991 3.50751 15.1608 2.66922C14.3225 1.83093 13.1855 1.35999 12 1.35999C10.8145 1.35999 9.67752 1.83093 8.83923 2.66922C8.00095 3.50751 7.53 4.64447 7.53 5.82999V7.17999H4.41C4.30206 7.18253 4.19925 7.22654 4.1229 7.30288C4.04655 7.37923 4.00254 7.48205 4 7.58999V21.15C4.00518 21.5435 4.16382 21.9195 4.44214 22.1978C4.72045 22.4762 5.09644 22.6348 5.49 22.64H18.49C18.887 22.64 19.2681 22.4837 19.5507 22.2048C19.8333 21.9259 19.9947 21.547 20 21.15V7.58999C20 7.48125 19.9568 7.37696 19.8799 7.30007C19.803 7.22318 19.6987 7.17999 19.59 7.17999Z"/>
        </svg>
        <span class="personal-dashboard-card__title">Заказы</span>
      </router-link>

      <router-link to="/personal/favorites" class="personal-dashboard-card">
        <svg class="personal-dashboard-card__icon" width="24" height="24" viewBox="0 0 24 24" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
          <path d="M12.0012 19.6847C11.7573 19.6844 11.5235 19.5873 11.3512 19.4147L4.44121 12.4947C3.98435 12.0386 3.62189 11.497 3.37459 10.9007C3.12729 10.3044 3 9.6652 3 9.01967C3 8.37414 3.12729 7.73495 3.37459 7.13867C3.62189 6.54239 3.98435 6.00072 4.44121 5.54467C4.89641 5.08811 5.43723 4.72587 6.03268 4.47871C6.62813 4.23154 7.2665 4.10431 7.91121 4.10431C8.55592 4.10431 9.1943 4.23154 9.78975 4.47871C10.3852 4.72587 10.926 5.08811 11.3812 5.54467L12.0112 6.16467L12.6212 5.55467C13.0712 5.07396 13.6132 4.68866 14.2151 4.42169C14.817 4.15473 15.4664 4.01155 16.1248 4.00067C16.7831 3.98979 17.437 4.11145 18.0473 4.35839C18.6577 4.60533 19.2122 4.97252 19.6778 5.43811C20.1434 5.90369 20.5106 6.45817 20.7575 7.06855C21.0044 7.67893 21.1261 8.33275 21.1152 8.9911C21.1043 9.64945 20.9612 10.2989 20.6942 10.9008C20.4272 11.5027 20.0419 12.0447 19.5612 12.4947L12.6412 19.4147C12.5581 19.5003 12.4586 19.5683 12.3486 19.6147C12.2387 19.6611 12.1205 19.6849 12.0012 19.6847Z"/>
        </svg>
        <span class="personal-dashboard-card__title">Избранное</span>
      </router-link>

      <router-link to="/personal/profile" class="personal-dashboard-card">
        <svg class="personal-dashboard-card__icon" width="24" height="24" viewBox="0 0 24 24" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
          <path d="M12.5 12a5 5 0 1 0 0-10 5 5 0 0 0 0 10zM21.09 22c0-3.87-3.85-7-8.59-7s-8.59 3.13-8.59 7" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <span class="personal-dashboard-card__title">Личные данные</span>
      </router-link>

      <router-link to="/order-tracking" class="personal-dashboard-card">
        <svg class="personal-dashboard-card__icon" width="24" height="24" viewBox="0 0 24 24" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
          <path d="M12.2366 19.5C16.3787 19.5 19.7366 16.1421 19.7366 12C19.7366 7.85786 16.3787 4.5 12.2366 4.5C8.09443 4.5 4.73657 7.85786 4.73657 12C4.73657 16.1421 8.09443 19.5 12.2366 19.5Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M12.2366 15C13.8935 15 15.2366 13.6569 15.2366 12C15.2366 10.3431 13.8935 9 12.2366 9C10.5797 9 9.23657 10.3431 9.23657 12C9.23657 13.6569 10.5797 15 12.2366 15Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <span class="personal-dashboard-card__title">Где мой заказ?</span>
      </router-link>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useAuthStore } from '../stores/auth'
import { fileApi } from '../services/api'

const authStore = useAuthStore()

const avatarUrl = computed(() => {
  const path = authStore.user?.avatar
  if (!path) return null
  const base = fileApi.getFileUrl(path)
  if (!base) return null
  const sep = base.includes('?') ? '&' : '?'
  return `${base}${sep}t=${Date.now()}`
})

const formatPhone = (phone) => {
  if (!phone) return ''
  const digits = phone.replace(/\D/g, '')
  if (digits.length >= 10) {
    const match = digits.slice(-10).match(/^(\d{3})(\d{3})(\d{2})(\d{2})$/)
    return match ? `+7-${match[1]}-${match[2]}-${match[3]}-${match[4]}` : phone
  }
  return phone
}

const avatarLetter = computed(() => {
  const name = authStore.user?.fullName || 'П'
  return name.charAt(0).toUpperCase()
})
</script>

<style scoped>
.personal-index {
  font-family: "helvetica", sans-serif;
}

.personal-index__grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 16px;
}

@media (min-width: 600px) {
  .personal-index__grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (min-width: 900px) {
  .personal-index__grid {
    grid-template-columns: 1fr repeat(2, 1fr);
    gap: 20px;
  }
}

.personal-user-panel {
  grid-column: 1 / -1;
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  background: #2E2826;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  text-decoration: none;
  color: inherit;
  transition: background 0.2s, border-color 0.2s;
}

.personal-user-panel:hover {
  background: #352f2d;
  border-color: rgba(244, 116, 39, 0.3);
}

.personal-user-panel__avatar {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  background: rgba(244, 116, 39, 0.2);
  color: #F47427;
  font-size: 24px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: hidden;
}

.personal-user-panel__avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.personal-user-panel__info {
  flex: 1;
  min-width: 0;
}

.personal-user-panel__name {
  margin: 0 0 4px;
  font-size: 20px;
  font-weight: 600;
  color: #fff;
}

.personal-user-panel__phone {
  font-size: 14px;
  color: #9d9390;
}

.personal-user-panel__arrow {
  flex-shrink: 0;
  color: #9d9390;
  transition: color 0.2s, transform 0.2s;
}

.personal-user-panel:hover .personal-user-panel__arrow {
  color: #F47427;
  transform: translateX(4px);
}

.personal-dashboard-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  background: #2E2826;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  text-decoration: none;
  color: inherit;
  transition: background 0.2s, border-color 0.2s;
}

.personal-dashboard-card:hover {
  background: #352f2d;
  border-color: rgba(244, 116, 39, 0.3);
}

.personal-dashboard-card__icon {
  flex-shrink: 0;
  color: #F47427;
}

.personal-dashboard-card__title {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
}
</style>
