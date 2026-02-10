<script setup>
import InvoiceProductEdit from './InvoiceProductEdit.vue'
import { VNodeRenderer } from '@layouts/components/VNodeRenderer'
import { themeConfig } from '@themeConfig'

const props = defineProps({
  data: {
    type: null,
    required: true,
  },
})

const emit = defineEmits([
  'push',
  'remove',
])

const invoice = ref(props.data.invoice)
const salesperson = ref(props.data.salesperson)
const thanksNote = ref(props.data.thanksNote)
const note = ref(props.data.note)

// 👉 Clients
const clients = ref([])

// 👉 fetchClients
const fetchClients = async () => {
  const { data, error } = await useApi('/apps/invoice/clients')
  if (error.value)
    console.log(error.value)
  else
    clients.value = data.value
}

fetchClients()

// 👉 Add item function
const addItem = () => {
  emit('push', {
    title: 'Дизайн приложения',
    cost: 24,
    hours: 1,
    description: 'Дизайн UI kit и страниц приложения.',
  })
}

const removeProduct = id => {
  emit('remove', id)
}
</script>

<template>
  <VCard class="pa-6 pa-sm-12">
    <!-- SECTION Header -->
    <div class="d-flex flex-wrap justify-space-between flex-column rounded bg-var-theme-background flex-sm-row gap-6 pa-6 mb-6">
      <!-- 👉 Left Content -->
      <div>
        <div class="d-flex align-center app-logo mb-6">
          <!-- 👉 Logo -->
          <VNodeRenderer :nodes="themeConfig.app.logo" />

          <!-- 👉 Title -->
          <h6 class="app-logo-title">
            {{ themeConfig.app.title }}
          </h6>
        </div>

        <!-- 👉 Address -->
        <p class="text-high-emphasis mb-0">
          Office 149, 450 South Brand Brooklyn
        </p>
        <p class="text-high-emphasis mb-0">
          Москва, Россия
        </p>
        <p class="text-high-emphasis mb-0">
          +1 (123) 456 7891, +44 (876) 543 2198
        </p>
      </div>

      <!-- 👉 Right Content -->
      <div class="d-flex flex-column gap-2">
        <!-- 👉 Invoice Id -->
        <div class="d-flex align-start align-sm-center gap-x-4 font-weight-medium text-lg flex-column flex-sm-row">
          <span
            class="text-high-emphasis text-sm-end"
            style="inline-size: 5.625rem ;"
          >Счёт:</span>
          <span>
            <AppTextField
              id="invoice-id"
              v-model="invoice.id"
              disabled
              prefix="#"
              style="inline-size: 9.5rem;"
            />
          </span>
        </div>

        <!-- 👉 Issue Date -->
        <div class="d-flex gap-x-4 align-start align-sm-center flex-column flex-sm-row">
          <span
            class="text-high-emphasis text-sm-end"
            style="inline-size: 5.625rem;"
          >Дата выдачи:</span>

          <span style="inline-size: 9.5rem;">
            <AppDateTimePicker
              id="issued-date"
              v-model="invoice.issuedDate"
              placeholder="YYYY-MM-DD"
              :config="{ position: 'auto right' }"
            />
          </span>
        </div>

        <!-- 👉 Due Date -->
        <div class="d-flex gap-x-4 align-start align-sm-center flex-column flex-sm-row">
          <span
            class="text-high-emphasis text-sm-end"
            style="inline-size: 5.625rem;"
          >Срок оплаты:</span>
          <span style="min-inline-size: 9.5rem;">
            <AppDateTimePicker
              id="due-date"
              v-model="invoice.dueDate"
              placeholder="YYYY-MM-DD"
              :config="{ position: 'auto right' }"
            />
          </span>
        </div>
      </div>
    </div>
    <!-- !SECTION -->

    <VRow>
      <VCol class="text-no-wrap">
        <h6 class="text-h6 mb-4">
          Платёж получателю:
        </h6>

        <VSelect
          id="client-name"
          v-model="invoice.client"
          :items="clients"
          item-title="name"
          item-value="name"
          placeholder="Выберите клиента"
          return-object
          class="mb-4"
          style="inline-size: 11.875rem;"
        />
        <p class="mb-0">
          {{ invoice.client.name }}
        </p>
        <p class="mb-0">
          {{ invoice.client.company }}
        </p>
        <p
          v-if="invoice.client.address"
          class="mb-0"
        >
          {{ invoice.client.address }}, {{ invoice.client.country }}
        </p>
        <p class="mb-0">
          {{ invoice.client.contact }}
        </p>
        <p class="mb-0">
          {{ invoice.client.companyEmail }}
        </p>
      </VCol>

      <VCol class="text-no-wrap">
        <h6 class="text-h6 mb-4">
          Реквизиты оплаты:
        </h6>

        <table>
          <tbody>
            <tr>
              <td class="pe-4">
                К оплате:
              </td>
              <td>{{ props.data.paymentDetails.totalDue }}</td>
            </tr>
            <tr>
              <td class="pe-4">
                Банк:
              </td>
              <td>{{ props.data.paymentDetails.bankName }}</td>
            </tr>
            <tr>
              <td class="pe-4">
                Страна:
              </td>
              <td>{{ props.data.paymentDetails.country }}</td>
            </tr>
            <tr>
              <td class="pe-4">
                IBAN:
              </td>
              <td>
                <p class="text-wrap me-4">
                  {{ props.data.paymentDetails.iban }}
                </p>
              </td>
            </tr>
            <tr>
              <td class="pe-4">
                SWIFT-код:
              </td>
              <td>{{ props.data.paymentDetails.swiftCode }}</td>
            </tr>
          </tbody>
        </table>
      </VCol>
    </VRow>

    <VDivider class="my-6 border-dashed" />
    <!-- 👉 Add purchased products -->
    <div class="add-products-form">
      <div
        v-for="(product, index) in props.data.purchasedProducts"
        :key="product.title"
        class="mb-4"
      >
        <InvoiceProductEdit
          :id="index"
          :data="product"
          @remove-product="removeProduct"
        />
      </div>

      <VBtn
        size="small"
        prepend-icon="tabler-plus"
        @click="addItem"
      >
        Добавить позицию
      </VBtn>
    </div>

    <VDivider class="my-6 border-dashed" />

    <!-- 👉 Total Amount -->
    <div class="d-flex justify-space-between flex-wrap flex-column flex-sm-row">
      <div class="mb-6 mb-sm-0">
        <div class="d-flex align-center mb-4">
          <h6 class="text-h6 me-2">
            Менеджер:
          </h6>
          <AppTextField
            id="salesperson"
            v-model="salesperson"
            style="inline-size: 8rem;"
            placeholder="Имя менеджера"
          />
        </div>

        <AppTextField
          id="thanks-note"
          v-model="thanksNote"
          placeholder="Благодарим за покупку"
        />
      </div>

      <div>
        <table class="w-100">
          <tbody>
            <tr>
              <td class="pe-16">
                Подитог:
              </td>
              <td :class="$vuetify.locale.isRtl ? 'text-start' : 'text-end'">
                <h6 class="text-h6">
                  $1800
                </h6>
              </td>
            </tr>
            <tr>
              <td class="pe-16">
                Скидка:
              </td>
              <td :class="$vuetify.locale.isRtl ? 'text-start' : 'text-end'">
                <h6 class="text-h6">
                  $28
                </h6>
              </td>
            </tr>
            <tr>
              <td class="pe-16">
                НДС:
              </td>
              <td :class="$vuetify.locale.isRtl ? 'text-start' : 'text-end'">
                <h6 class="text-h6">
                  21%
                </h6>
              </td>
            </tr>
          </tbody>
        </table>

        <VDivider class="mt-4 mb-3" />

        <table class="w-100">
          <tbody>
            <tr>
              <td class="pe-16">
                Итого:
              </td>
              <td :class="$vuetify.locale.isRtl ? 'text-start' : 'text-end'">
                <h6 class="text-h6">
                  $1690
                </h6>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <VDivider class="my-6 border-dashed" />

    <div>
      <h6 class="text-h6 mb-2">
        Примечание:
      </h6>
      <VTextarea
        id="note"
        v-model="note"
        placeholder="Введите примечание..."
        :rows="2"
      />
    </div>
  </VCard>
</template>
