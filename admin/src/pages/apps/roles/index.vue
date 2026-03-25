<script setup>
import RoleCards from '@/views/apps/roles/RoleCards.vue'
import UserList from '@/views/apps/roles/UserList.vue'

definePage({
  meta: {
    action: 'manage',
    subject: 'AdminRoles',
    navActiveLink: 'apps-roles',
  },
})
</script>

<template>
  <VRow>
    <VCol cols="12">
      <h4 class="text-h4 mb-1">
        Список ролей
      </h4>
      <p class="text-body-1 mb-0">
        Роль предоставляет доступ к предопределённым меню и функциям, чтобы в зависимости от назначенной роли администратор мог получить доступ к тому, что ему нужно
      </p>
    </VCol>

    <!-- 👉 Roles Cards -->
    <VCol cols="12">
      <RoleCards />
    </VCol>

    <VCol cols="12">
      <h4 class="text-h4 mb-1 mt-6">
        Всего пользователей с их ролями
      </h4>
      <p class="text-body-1 mb-0">
        Найдите все учётные записи администраторов вашей компании и их связанные роли.
      </p>
    </VCol>

    <VCol cols="12">
      <!-- 👉 User List  -->
      <UserList />
    </VCol>
  </VRow>
</template>
