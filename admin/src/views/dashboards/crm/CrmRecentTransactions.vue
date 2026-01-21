<script setup>
import aeIcon from '@images/icons/payments/ae-icon.png'
import mastercardIcon from '@images/icons/payments/mastercard-icon.png'
import visaIcon from '@images/icons/payments/visa-icon.png'

const lastTransitions = [
  {
    cardImg: visaIcon,
    lastDigit: '*4230',
    cardType: 'Кредитная',
    sentDate: '17 Mar 2026',
    status: 'Подтверждено',
    trend: '+$1,678',
  },
  {
    cardImg: mastercardIcon,
    lastDigit: '*5578',
    cardType: 'Кредитная',
    sentDate: '12 Feb 2026',
    status: 'Отклонено',
    trend: '-$839',
  },
  {
    cardImg: aeIcon,
    lastDigit: '*4567',
    cardType: 'Кредитная',
    sentDate: '28 Feb 2026',
    status: 'Подтверждено',
    trend: '+$435',
  },
  {
    cardImg: visaIcon,
    lastDigit: '*5699',
    cardType: 'Кредитная',
    sentDate: '8 Jan 2026',
    status: 'Ожидает',
    trend: '+$2,345',
  },
  {
    cardImg: visaIcon,
    lastDigit: '*5699',
    cardType: 'Кредитная',
    sentDate: '8 Jan 2026',
    status: 'Отклонено',
    trend: '-$234',
  },
]

const resolveStatus = {
  'Подтверждено': 'success',
  'Отклонено': 'error',
  'Ожидает': 'secondary',
}

const moreList = [
  {
    title: 'Обновить',
    value: 'refresh',
  },
  {
    title: 'Скачать',
    value: 'Download',
  },
  {
    title: 'Показать все',
    value: 'View All',
  },
]

const getPaddingStyle = index => index ? 'padding-block-end: 1.25rem;' : 'padding-block: 1.25rem;'
</script>

<template>
  <VCard title="Последние транзакции">
    <template #append>
      <div class="me-n2">
        <MoreBtn
          size="small"
          :menu-list="moreList"
        />
      </div>
    </template>

    <VDivider />
    <VTable class="text-no-wrap transaction-table">
      <thead>
        <tr>
          <th>КАРТА</th>
          <th>ДАТА</th>
          <th>СТАТУС</th>
          <th>
            ТРЕНД
          </th>
        </tr>
      </thead>

      <tbody>
        <tr
          v-for="(transition, index) in lastTransitions"
          :key="index"
        >
          <td
            :style="getPaddingStyle(index)"
            style="padding-inline-end: 1.5rem;"
          >
            <div class="d-flex align-center">
              <div class="me-4">
                <VImg
                  :src="transition.cardImg"
                  width="50"
                />
              </div>
              <div>
                <p class="text-base mb-0 text-high-emphasis">
                  {{ transition.lastDigit }}
                </p>
                <p class="text-sm mb-0">
                  {{ transition.cardType }}
                </p>
              </div>
            </div>
          </td>
          <td
            :style="getPaddingStyle(index)"
            style="padding-inline-end: 1.5rem;"
          >
            <p class="text-high-emphasis text-base mb-0">
              Отправлено
            </p>
            <div class="text-sm">
              {{ transition.sentDate }}
            </div>
          </td>
          <td
            :style="getPaddingStyle(index)"
            style="padding-inline-end: 1.5rem;"
          >
            <VChip
              label
              :color="resolveStatus[transition.status] || 'secondary'"
              size="small"
            >
              {{ transition.status }}
            </VChip>
          </td>
          <td
            :style="getPaddingStyle(index)"
            style="padding-inline-end: 1.5rem;"
            align="right"
          >
            <div class="text-high-emphasis text-base">
              {{ transition.trend }}
            </div>
          </td>
        </tr>
      </tbody>
    </VTable>
  </VCard>
</template>

<style lang="scss">
.transaction-table {
  &.v-table .v-table__wrapper > table > tbody > tr:not(:last-child) > td,
  &.v-table .v-table__wrapper > table > tbody > tr:not(:last-child) > th {
    border-block-end: none !important;
  }
}
</style>
