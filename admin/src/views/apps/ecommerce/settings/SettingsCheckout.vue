<script setup>
const contactMethod = ref('Phone number')
const fullName = ref('Only require last name')
const companyName = ref('Don\'t include')
const addressLine = ref('Optional')
const shippingAddress = ref('Optional')
</script>

<template>
  <VCard
    title="Способ связи с клиентом"
    subtitle="Выберите способ связи, который клиенты используют при оформлении заказа."
    class="mb-6"
  >
    <VCardText>
      <VRadioGroup
        v-model="contactMethod"
        class="mb-4"
      >
        <VRadio
          label="Номер телефона"
          value="Phone number"
        />
        <VRadio
          label="Email"
          value="Email"
        />
      </VRadioGroup>

      <VAlert
        type="info"
        variant="tonal"
      >
        <VAlertTitle class="mb-0">
          Для отправки SMS-уведомлений необходимо установить SMS-приложение.
        </VAlertTitle>
      </VAlert>
    </VCardText>
  </VCard>

  <VCard
    title="Информация о клиенте"
    class="mb-6"
  >
    <VCardText class="customer-info-card">
      <VRadioGroup
        v-model="fullName"
        label="Полное имя"
        class="mb-4"
      >
        <VRadio
          value="Only require last name"
          label="Только фамилия обязательна"
        />
        <VRadio
          value="Require first and last name"
          label="Имя и фамилия обязательны"
        />
      </VRadioGroup>

      <VRadioGroup
        v-model="companyName"
        label="Название компании"
        class="mb-4"
      >
        <VRadio
          value="Don't include"
          label="Не включать"
        />
        <VRadio
          value="Optional"
          label="Необязательно"
        />
        <VRadio
          value="Required"
          label="Обязательно"
        />
      </VRadioGroup>

      <VRadioGroup
        v-model="addressLine"
        label="Адрес строка 2 (квартира, офис и т.д.)"
        class="mb-4"
      >
        <VRadio
          value="Don't include"
          label="Не включать"
        />
        <VRadio
          value="Optional"
          label="Необязательно"
        />
        <VRadio
          value="Required"
          label="Обязательно"
        />
      </VRadioGroup>

      <VRadioGroup
        v-model="shippingAddress"
        label="Номер телефона адреса доставки"
        class="mb-4"
      >
        <VRadio
          value="Don't include"
          label="Не включать"
        />
        <VRadio
          value="Optional"
          label="Необязательно"
        />
        <VRadio
          value="Required"
          label="Обязательно"
        />
      </VRadioGroup>
    </VCardText>
  </VCard>

  <div class="d-flex justify-end gap-x-4">
    <VBtn
      variant="tonal"
      color="secondary"
    >
      Отменить
    </VBtn>
    <VBtn>Сохранить изменения</VBtn>
  </div>
</template>

<style lang="scss" scoped>
.customer-info-card {
  .v-radio-group {
    .v-input__control {
      .v-label {
        margin-inline-start: 0;
      }
    }
  }
}
</style>
