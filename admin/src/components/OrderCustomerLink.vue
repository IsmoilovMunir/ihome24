<script setup>
const props = defineProps({
  item: {
    type: Object,
    required: true,
  },
})

const router = useRouter()
const resolving = ref(false)

const customerRoute = computed(() => {
  const id = props.item?.customerId
  if (id == null || id === '') return null
  return {
    name: 'apps-ecommerce-customer-details-id',
    params: { id: String(id) },
  }
})

async function openCustomerProfile() {
  if (customerRoute.value) {
    await router.push(customerRoute.value)
    return
  }

  const params = new URLSearchParams()
  if (props.item?.email) params.set('email', props.item.email.trim())
  if (props.item?.phone) params.set('phone', props.item.phone)

  if (!params.toString()) return

  resolving.value = true
  try {
    const data = await $api(`/apps/ecommerce/customers/resolve?${params}`)
    const customerId = data?.customerId
    if (customerId != null) {
      await router.push({
        name: 'apps-ecommerce-customer-details-id',
        params: { id: String(customerId) },
      })
      return
    }
    console.warn('Клиент не найден для', props.item?.email, props.item?.phone)
  } catch (e) {
    console.error('Клиент не найден:', e)
  } finally {
    resolving.value = false
  }
}
</script>

<template>
  <RouterLink
    v-if="customerRoute"
    :to="customerRoute"
    class="text-link font-weight-medium d-inline-block"
    style="line-height: 1.375rem;"
  >
    {{ item.customer }}
  </RouterLink>
  <a
    v-else
    href="#"
    class="text-link font-weight-medium d-inline-block"
    style="line-height: 1.375rem;"
    :class="{ 'opacity-60 pointer-events-none': resolving }"
    @click.prevent="openCustomerProfile"
  >
    <span v-if="resolving">Загрузка…</span>
    <span v-else>{{ item.customer }}</span>
  </a>
</template>
