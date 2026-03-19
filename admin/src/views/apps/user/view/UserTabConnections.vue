<script setup>
// OAuth / соцсети для карточки пользователя в админке не используются — без демо-списков.
</script>

<template>
  <VCard title="Подключения">
    <VCardText>
      <VAlert
        type="info"
        variant="tonal"
        title="Не используется"
        text="Демо-подключения Google, Slack, соцсетей и т.п. удалены. При необходимости интеграции их можно добавить с реальным API."
      />
    </VCardText>
  </VCard>
</template>
