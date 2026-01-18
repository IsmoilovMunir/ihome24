<template>
  <VCard
    title="Профиль"
    class="mb-6"
  >
    <VCardText>
      <VRow>
        <VCol
          cols="12"
          md="6"
        >
          <AppTextField
            label="Название магазина"
            placeholder="Мой магазин"
          />
        </VCol>
        <VCol
          cols="12"
          md="6"
        >
          <AppTextField
            label="Телефон"
            placeholder="+7 (999) 123-45-67"
          />
        </VCol>
        <VCol
          cols="12"
          md="6"
        >
          <AppTextField
            label="Email для связи магазина"
            placeholder="info@magazin.ru"
          />
        </VCol>
        <VCol
          cols="12"
          md="6"
        >
          <AppTextField
            label="Email отправителя"
            placeholder="info@magazin.ru"
          />
        </VCol>
        <VCol>
          <VAlert
            color="warning"
            variant="tonal"
            icon="tabler-bell"
          >
            <VAlertTitle class="mb-0">
              Подтвердите, что у вас есть доступ к email отправителя в настройках.
            </VAlertTitle>
          </VAlert>
        </VCol>
      </VRow>
    </VCardText>
  </VCard>

  <VCard
    title="Платежная информация"
    class="mb-6"
  >
    <VCardText>
      <VRow>
        <VCol
          cols="12"
          md="6"
        >
          <AppTextField
            label="Юридическое название компании"
            placeholder="ООО «Моя Компания»"
          />
        </VCol>

        <VCol
          cols="12"
          md="6"
        >
          <AppSelect
            label="Страна/Регион"
            :items="['Россия', 'Казахстан', 'Беларусь']"
            placeholder="Россия"
          />
        </VCol>

        <VCol
          cols="12"
          md="6"
        >
          <AppTextField
            placeholder="г. Москва, ул. Примерная, д. 1"
            label="Адрес"
          />
        </VCol>

        <VCol
          cols="12"
          md="6"
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
            placeholder="101000"
          />
        </VCol>
      </VRow>
    </VCardText>
  </VCard>

  <VCard class="mb-6">
    <template #title>
      <div class="v-card-title text-wrap">
        Часовой пояс и единицы измерения
      </div>
    </template>

    <template #subtitle>
      <div class="text-wrap">
        Используется для расчета цен товаров, веса доставки и времени заказов.
      </div>
    </template>
    <VCardText>
      <VRow>
        <VCol cols="12">
          <AppSelect
            label="Часовой пояс"
            :items="['(UTC+03:00) Москва', '(UTC+05:00) Екатеринбург', '(UTC+07:00) Красноярск', '(UTC+10:00) Владивосток']"
            placeholder="(UTC+03:00) Москва"
          />
        </VCol>

        <VCol
          cols="12"
          md="6"
        >
          <AppSelect
            label="Система единиц"
            :items="['Метрическая система', 'Имперская', 'Международная система']"
            placeholder="Метрическая система"
          />
        </VCol>

        <VCol
          cols="12"
          md="6"
        >
          <AppSelect
            label="Единица веса по умолчанию"
            placeholder="Килограмм"
            :items="['Килограмм', 'Фунт', 'Грамм']"
          />
        </VCol>
      </VRow>
    </VCardText>
  </VCard>

  <VCard
    title="Валюта магазина"
    subtitle="Валюта, в которой продаются ваши товары."
    class="mb-6"
  >
    <VCardText>
      <AppSelect
        label="Валюта магазина"
        :items="['RUB', 'USD', 'EUR', 'KZT']"
        placeholder="RUB"
      />
    </VCardText>
  </VCard>

  <VCard
    title="Формат ID заказа"
    subtitle="Отображается на странице заказов, страницах клиентов и уведомлениях о заказах для идентификации заказов."
    class="mb-6"
  >
    <VCardText>
      <VRow>
        <VCol
          cols="12"
          md="6"
        >
          <AppTextField
            label="Префикс"
            prefix="#"
          />
        </VCol>

        <VCol
          cols="12"
          md="6"
        >
          <AppTextField
            label="Суффикс"
            suffix="₽"
          />
        </VCol>
      </VRow>

      <div class="mt-2">
        Ваш ID заказа будет отображаться как #1001, #1002, #1003 ...
      </div>
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
