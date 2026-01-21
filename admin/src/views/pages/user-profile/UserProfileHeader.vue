<script setup>
import UserProfileHeaderBg from '@images/pages/user-profile-header-bg.png'

const profileHeaderData = ref()

const fetchProfileHeader = async () => {
  try {
    const data = await $api('/auth/me')
    if (data) {
      // Форматируем дату создания
      let joiningDateText = 'Присоединился недавно'
      if (data.createdAt) {
        try {
          const date = new Date(data.createdAt)
          const monthNames = ['января', 'февраля', 'марта', 'апреля', 'мая', 'июня', 
                             'июля', 'августа', 'сентября', 'октября', 'ноября', 'декабря']
          const month = monthNames[date.getMonth()]
          const year = date.getFullYear()
          joiningDateText = `Присоединился ${month} ${year}`
        } catch (e) {
          console.error('Ошибка форматирования даты:', e)
        }
      }
      
      // Преобразуем данные из бэкенда в формат, ожидаемый фронтендом
      profileHeaderData.value = {
        fullName: data.fullName || data.username || 'Пользователь',
        location: data.country || 'Россия',
        joiningDate: joiningDateText,
        designation: data.role || 'Пользователь',
        profileImg: data.avatar || null,
        coverImg: UserProfileHeaderBg, // Дефолтное изображение
      }
    }
  } catch (error) {
    console.error('Ошибка при загрузке профиля:', error)
  }
}

onMounted(() => {
  fetchProfileHeader()
})
</script>

<template>
  <VCard v-if="profileHeaderData">
    <VImg
      :src="profileHeaderData.coverImg"
      min-height="125"
      max-height="250"
      cover
    />

    <VCardText class="d-flex align-bottom flex-sm-row flex-column justify-center gap-x-6">
      <div class="d-flex h-0">
        <VAvatar
          rounded
          size="130"
          :image="profileHeaderData.profileImg"
          class="user-profile-avatar mx-auto"
        />
      </div>

      <div class="user-profile-info w-100 mt-16 pt-6 pt-sm-0 mt-sm-0">
        <h4 class="text-h4 text-center text-sm-start font-weight-medium mb-2">
          {{ profileHeaderData?.fullName }}
        </h4>

        <div class="d-flex align-center justify-center justify-sm-space-between flex-wrap gap-5">
          <div class="d-flex flex-wrap justify-center justify-sm-start flex-grow-1 gap-6">
            <span class="d-flex gap-x-2 align-center">
              <VIcon
                size="24"
                icon="tabler-palette"
              />
              <div class="text-body-1 font-weight-medium">
                {{ profileHeaderData?.designation }}
              </div>
            </span>

            <span class="d-flex gap-x-2 align-center">
              <VIcon
                size="24"
                icon="tabler-map-pin"
              />
              <div class="text-body-1 font-weight-medium">
                {{ profileHeaderData?.location }}
              </div>
            </span>

            <span class="d-flex gap-x-2 align-center">
              <VIcon
                size="24"
                icon="tabler-calendar"
              />
              <div class="text-body-1 font-weight-medium">
                {{ profileHeaderData?.joiningDate }}
              </div>
            </span>
          </div>

          <VBtn prepend-icon="tabler-user-check">
            Подключен
          </VBtn>
        </div>
      </div>
    </VCardText>
  </VCard>
</template>

<style lang="scss">
.user-profile-avatar {
  border: 5px solid rgb(var(--v-theme-surface));
  background-color: rgb(var(--v-theme-surface)) !important;
  inset-block-start: -3rem;

  .v-img__img {
    border-radius: 0.125rem;
  }
}
</style>
