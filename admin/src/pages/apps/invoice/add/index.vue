<script setup>
import InvoiceEditable from '@/views/apps/invoice/InvoiceEditable.vue'
import InvoiceSendInvoiceDrawer from '@/views/apps/invoice/InvoiceSendInvoiceDrawer.vue'

const invoiceData = ref({
  invoice: {
    id: 5037,
    issuedDate: '',
    service: '',
    total: 0,
    avatar: '',
    invoiceStatus: '',
    dueDate: '',
    balance: 0,
    client: {
      address: '112, Lorem Ipsum, Florida',
      company: 'Greeva Inc',
      companyEmail: 'johndoe@greeva.com',
      contact: '+1 123 3452 12',
      country: 'Россия',
      name: 'John Doe',
    },
  },
  paymentDetails: {
    totalDue: '$12,110.55',
    bankName: 'Сбербанк России',
    country: 'Россия',
    iban: 'ETD95476213',
    swiftCode: 'BR91905',
  },
  purchasedProducts: [{
    title: '',
    cost: 0,
    hours: 0,
    description: '',
  }],
  note: '',
  paymentMethod: '',
  salesperson: '',
  thanksNote: '',
})

const paymentTerms = ref(true)
const clientNotes = ref(false)
const paymentStub = ref(false)
const selectedPaymentMethod = ref('Банковский счёт')

const paymentMethods = [
  'Банковский счёт',
  'PayPal',
  'UPI Transfer',
]

const isSendPaymentSidebarVisible = ref(false)

const addProduct = value => {
  invoiceData.value?.purchasedProducts.push(value)
}

const removeProduct = id => {
  invoiceData.value?.purchasedProducts.splice(id, 1)
}
</script>

<template>
  <VRow>
    <!-- 👉 InvoiceEditable -->
    <VCol
      cols="12"
      md="9"
    >
      <InvoiceEditable
        :data="invoiceData"
        @push="addProduct"
        @remove="removeProduct"
      />
    </VCol>

    <!-- 👉 Right Column: Invoice Action -->
    <VCol
      cols="12"
      md="3"
    >
      <VCard class="mb-8">
        <VCardText>
          <!-- 👉 Send Invoice -->
          <VBtn
            block
            prepend-icon="tabler-send"
            class="mb-4"
            @click="isSendPaymentSidebarVisible = true"
          >
            Отправить счёт
          </VBtn>

          <!-- 👉 Preview -->
          <VBtn
            block
            color="secondary"
            variant="tonal"
            class="mb-4"
            :to="{ name: 'apps-invoice-preview-id', params: { id: '5036' } }"
          >
            Предпросмотр
          </VBtn>

          <!-- 👉 Save -->
          <VBtn
            block
            color="secondary"
            variant="tonal"
          >
            Сохранить
          </VBtn>
        </VCardText>
      </VCard>

      <!-- 👉 Select payment method -->
      <AppSelect
        id="payment-method"
        v-model="selectedPaymentMethod"
        :items="paymentMethods"
        label="Способ оплаты"
        class="mb-6"
      />

      <!-- 👉 Payment Terms -->
      <div class="d-flex align-center justify-space-between">
        <VLabel for="payment-terms">
          Условия оплаты
        </VLabel>
        <div>
          <VSwitch
            id="payment-terms"
            v-model="paymentTerms"
          />
        </div>
      </div>

      <!-- 👉  Client Notes -->
      <div class="d-flex align-center justify-space-between">
        <VLabel for="client-notes">
          Заметки клиента
        </VLabel>
        <div>
          <VSwitch
            id="client-notes"
            v-model="clientNotes"
          />
        </div>
      </div>

      <!-- 👉  Payment Stub -->
      <div class="d-flex align-center justify-space-between">
        <VLabel for="payment-stub">
          Платёжная квитанция
        </VLabel>
        <div>
          <VSwitch
            id="payment-stub"
            v-model="paymentStub"
          />
        </div>
      </div>
    </VCol>
  </VRow>

  <!-- 👉 Send Invoice Sidebar -->
  <InvoiceSendInvoiceDrawer v-model:is-drawer-open="isSendPaymentSidebarVisible" />
</template>
