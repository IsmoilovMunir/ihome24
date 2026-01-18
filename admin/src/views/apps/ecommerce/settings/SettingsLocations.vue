<script setup>
const isFullfilOnline = ref(true)
</script>

<template>
  <div>
    <VCard
      title="Название местоположения"
      class="mb-6"
    >
      <VCardText>
        <AppTextField
          label="Название местоположения"
          placeholder="Склад Москва"
        />
        <div class="my-4">
          <VCheckbox
            v-model="isFullfilOnline"
            label="Выполнять онлайн-заказы из этого местоположения"
          />
        </div>
        <VAlert
          type="info"
          variant="tonal"
        >
          <VAlertTitle class="mb-0">
            Это ваше местоположение по умолчанию. Чтобы изменить, выполняете ли вы онлайн-заказы из этого местоположения, сначала выберите другое местоположение по умолчанию.
          </VAlertTitle>
        </VAlert>
      </VCardText>
    </VCard>

    <VCard title="Адрес">
      <VCardText>
        <VRow>
          <VCol cols="12">
            <AppSelect
              label="Страна/Регион"
              placeholder="Выберите страну"
              :items="['Россия', 'Казахстан', 'Беларусь']"
              model-value="Россия"
            />
          </VCol>

          <VCol
            cols="12"
            md="4"
          >
            <AppTextField
              label="Адрес"
              placeholder="г. Москва, ул. Примерная, д. 1"
            />
          </VCol>

          <VCol
            cols="12"
            md="4"
          >
            <AppTextField
              label="Квартира, офис и т.д."
              placeholder="Офис 101"
            />
          </VCol>

          <VCol
            cols="12"
            md="4"
          >
            <AppTextField
              label="Телефон"
              placeholder="+7 (999) 123-45-67"
            />
          </VCol>

          <VCol
            cols="12"
            md="4"
          >
            <AppTextField
              label="Город"
              placeholder="Москва"
            />
          </VCol>

          <VCol
            cols="12"
            md="4"
          >
            <AppTextField
              label="Область/Регион"
              placeholder="Московская область"
            />
          </VCol>

          <VCol
            cols="12"
            md="4"
          >
            <AppTextField
              label="Почтовый индекс"
              type="number"
              placeholder="101000"
            />
          </VCol>
        </VRow>
      </VCardText>
    </VCard>

    <div class="d-flex justify-end gap-x-4 mt-6">
      <VBtn
        variant="tonal"
        color="secondary"
      >
        Отменить
      </VBtn>
      <VBtn>Сохранить изменения</VBtn>
    </div>
  </div>
</template>
