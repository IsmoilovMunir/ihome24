<script setup>
import About from './About.vue'
import ActivityTimeline from './ActivityTimeline.vue'
import Connection from './Connection.vue'
import ProjectList from './ProjectList.vue'
import Teams from './Teams.vue'

const router = useRoute('pages-user-profile-tab')
const profileTabData = ref()

const fetchAboutData = async () => {
  if (router.params.tab === 'profile') {
    try {
      // Получаем данные текущего пользователя с бэкенда
      const userData = await $api('/auth/me')
      
      if (userData) {
        // Преобразуем данные из бэкенда в формат, ожидаемый фронтендом
        profileTabData.value = {
          about: [
            { property: 'Полное имя', value: userData.fullName || userData.username || 'Пользователь', icon: 'tabler-user' },
            { property: 'Статус', value: userData.status === 'ACTIVE' ? 'активен' : userData.status?.toLowerCase() || 'активен', icon: 'tabler-check' },
            { property: 'Роль', value: userData.role || 'Пользователь', icon: 'tabler-crown' },
            { property: 'Страна', value: userData.country || 'Россия', icon: 'tabler-flag' },
            { property: 'Язык', value: 'Русский', icon: 'tabler-language' },
          ],
          contacts: [
            { property: 'Контакт', value: userData.contact || 'Не указан', icon: 'tabler-phone-call' },
            { property: 'Email', value: userData.email || 'Не указан', icon: 'tabler-mail' },
          ],
          teams: [
            { property: 'Компания', value: userData.company || 'Не указана', icon: 'tabler-building' },
            { property: 'План', value: userData.currentPlan || 'Базовый', icon: 'tabler-crown' },
          ],
          overview: [
            { property: 'Задач выполнено', value: '0', icon: 'tabler-check' },
            { property: 'Связи', value: '0', icon: 'tabler-users' },
            { property: 'Проектов завершено', value: '0', icon: 'tabler-layout-grid' },
          ],
          connections: [
            // Можно добавить реальные связи позже
          ],
          teamsTech: [
            // Можно добавить реальные команды позже
          ],
        }
      }
    } catch (error) {
      console.error('Ошибка при загрузке данных профиля:', error)
    }
  }
}

watch(router, fetchAboutData, { immediate: true })
</script>

<template>
  <VRow v-if="profileTabData">
    <VCol
      md="4"
      cols="12"
    >
      <About :data="profileTabData" />
    </VCol>

    <VCol
      cols="12"
      md="8"
    >
      <VRow>
        <VCol cols="12">
          <ActivityTimeline />
        </VCol>

        <VCol
          cols="12"
          md="6"
        >
          <Connection :connections-data="profileTabData.connections" />
        </VCol>

        <VCol
          cols="12"
          md="6"
        >
          <Teams :teams-data="profileTabData.teamsTech" />
        </VCol>

        <VCol cols="12">
          <ProjectList />
        </VCol>
      </VRow>
    </VCol>
  </VRow>
</template>
