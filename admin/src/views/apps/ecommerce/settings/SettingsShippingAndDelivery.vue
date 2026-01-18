<script setup>
import avatar1 from '@images/avatars/avatar-1.png'
import usflag from '@images/icons/countries/us.png'

const domesticTableData = [
  {
    rate: 'Вес',
    condition: '5Кг-10Кг',
    price: '900₽',
  },
  {
    rate: 'НДС',
    condition: '12%',
    price: '2500₽',
  },
  {
    rate: 'Пошлина',
    condition: '-',
    price: '-',
  },
]

const InternationalTableData = [
  {
    rate: 'Вес',
    condition: '5Кг-10Кг',
    price: '900₽',
  },
  {
    rate: 'НДС',
    condition: '12%',
    price: '2500₽',
  },
  {
    rate: 'Пошлина',
    condition: 'Россия',
    price: '4900₽',
  },
]
</script>

<template>
  <VCard class="mb-6">
    <VCardItem
      title="Зона доставки"
      subtitle="Выберите, куда вы доставляете и сколько взимаете за доставку при оформлении заказа."
    >
      <template #append>
        <VBtn variant="text">
          Создать зону
        </VBtn>
      </template>
    </VCardItem>

    <VCardText>
      <div class="mb-6">
        <div class="d-flex flex-wrap align-center mb-4">
          <VAvatar
            :image="avatar1"
            size="34"
            class="me-2"
          />

          <div>
            <h6 class="text-h6">
              Внутренняя доставка
            </h6>
            <div class="text-body-2">
              Россия
            </div>
          </div>

          <VSpacer />

          <div>
            <IconBtn color="secondary">
              <VIcon icon="tabler-pencil" />
            </IconBtn>
            <IconBtn color="secondary">
              <VIcon icon="tabler-trash" />
            </IconBtn>
          </div>
        </div>

        <VTable class="mb-4 border rounded">
          <thead>
            <tr>
              <th>НАЗВАНИЕ ТАРИФА</th>
              <th>УСЛОВИЕ</th>
              <th>ЦЕНА</th>
              <th>ДЕЙСТВИЯ</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="(data, index) in domesticTableData"
              :key="index"
            >
              <td>{{ data.rate }}</td>
              <td>{{ data.condition }}</td>
              <td>{{ data.price }}</td>
              <td>
                <IconBtn>
                  <VIcon icon="tabler-dots-vertical" />
                </IconBtn>
              </td>
            </tr>
          </tbody>
        </VTable>

        <VBtn variant="tonal">
          Добавить тариф
        </VBtn>
      </div>

      <div>
        <div class="d-flex flex-wrap align-center mb-4">
          <VAvatar
            :image="usflag"
            size="30"
            class="me-2"
          />

          <div>
            <h6 class="text-h6">
              Международная доставка
            </h6>
            <div class="text-body-2">
              Россия
            </div>
          </div>

          <VSpacer />
          <div>
            <IconBtn color="secondary">
              <VIcon icon="tabler-pencil" />
            </IconBtn>
            <IconBtn color="secondary">
              <VIcon icon="tabler-trash" />
            </IconBtn>
          </div>
        </div>

        <VTable class="mb-4 border rounded">
          <thead>
            <tr>
              <th>НАЗВАНИЕ ТАРИФА</th>
              <th>УСЛОВИЕ</th>
              <th>ЦЕНА</th>
              <th>ДЕЙСТВИЯ</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="(data, index) in InternationalTableData"
              :key="index"
            >
              <td>{{ data.rate }}</td>
              <td>{{ data.condition }}</td>
              <td>{{ data.price }}</td>
              <td>
                <IconBtn>
                  <VIcon icon="tabler-dots-vertical" />
                </IconBtn>
              </td>
            </tr>
          </tbody>
        </VTable>

        <VBtn variant="tonal">
          Добавить тариф
        </VBtn>
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
