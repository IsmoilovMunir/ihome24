<template>
  <div class="personal-placeholder">
    <h1>{{ title }}</h1>
    <p>Раздел в разработке</p>
    <router-link to="/" class="personal-placeholder__link">Вернуться на главную</router-link>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const titles = {
  personal: 'Личный кабинет',
  orders: 'Заказы',
  purchases: 'Товары',
  bonuses: 'Бонусы',
  privileges: 'Ваши привилегии',
  favorites: 'Избранное',
  compare: 'Сравнение',
  profile: 'Личные данные',
  present: 'Подарочные карты',
  services: 'Сервисы',
  'order-tracking': 'Где мой заказ?',
}

const title = computed(() => {
  const path = route.path.replace(/^\/personal\/?/, '').replace(/^\//, '') || 'personal'
  return titles[path] || 'Личный кабинет'
})
</script>

<style scoped>
.personal-placeholder {
  min-height: 60vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  text-align: center;
}

.personal-placeholder h1 {
  font-size: 24px;
  font-weight: 600;
  color: #2E2826;
  margin: 0 0 12px;
}

.personal-placeholder p {
  color: #6b7280;
  margin: 0 0 24px;
}

.personal-placeholder__link {
  color: #F47427;
  font-weight: 500;
  text-decoration: none;
}

.personal-placeholder__link:hover {
  text-decoration: underline;
}
</style>
