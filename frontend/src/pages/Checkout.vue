<template>
  <div class="checkout-page">
    <div class="container">
      <h1>Оформление заказа</h1>

      <div v-if="cartStore.items.length === 0" class="empty-cart">
        <p>Ваша корзина пуста</p>
        <router-link to="/products" class="btn btn-primary">
          Перейти в каталог
        </router-link>
      </div>

      <div v-else class="checkout-content">
        <form @submit.prevent="submitOrder" class="checkout-form card">
          <h2>Данные покупателя</h2>
          
          <div class="form-group">
            <label for="name">Имя *</label>
            <input 
              type="text" 
              id="name" 
              v-model="formData.name" 
              required
              class="form-input"
            />
          </div>

          <div class="form-group">
            <label for="email">Email *</label>
            <input 
              type="email" 
              id="email" 
              v-model="formData.email" 
              required
              class="form-input"
            />
          </div>

          <div class="form-group">
            <label for="phone">Телефон *</label>
            <input 
              type="tel" 
              id="phone" 
              v-model="formData.phone" 
              required
              class="form-input"
            />
          </div>

          <div class="form-group">
            <label for="address">Адрес доставки *</label>
            <textarea 
              id="address" 
              v-model="formData.address" 
              required
              rows="3"
              class="form-input"
            ></textarea>
          </div>

          <div class="form-group">
            <label for="comment">Комментарий к заказу</label>
            <textarea 
              id="comment" 
              v-model="formData.comment" 
              rows="3"
              class="form-input"
            ></textarea>
          </div>

          <button 
            type="submit" 
            class="btn btn-primary btn-large btn-block"
            :disabled="loading"
          >
            {{ loading ? 'Оформление...' : 'Оформить заказ' }}
          </button>

          <div v-if="error" class="error">
            {{ error }}
          </div>
        </form>

        <div class="order-summary card">
          <h2>Ваш заказ</h2>
          <div class="order-items">
            <div 
              v-for="item in cartStore.items" 
              :key="item.id" 
              class="order-item"
            >
              <span>{{ item.name }} x{{ item.quantity }}</span>
              <span>{{ formatPrice(item.price * item.quantity) }}</span>
            </div>
          </div>
          <div class="order-total">
            <span>Итого:</span>
            <span>{{ formatPrice(cartStore.totalPrice) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '../store/cart'
import { ordersAPI } from '../services/api'

const router = useRouter()
const cartStore = useCartStore()

const formData = ref({
  name: '',
  email: '',
  phone: '',
  address: '',
  comment: ''
})

const loading = ref(false)
const error = ref(null)

const formatPrice = (price) => {
  return new Intl.NumberFormat('ru-RU', {
    style: 'currency',
    currency: 'RUB'
  }).format(price)
}

const submitOrder = async () => {
  loading.value = true
  error.value = null

  try {
    const orderData = {
      customerName: formData.value.name,
      customerEmail: formData.value.email,
      customerPhone: formData.value.phone,
      deliveryAddress: formData.value.address,
      comment: formData.value.comment,
      items: cartStore.items.map(item => ({
        productId: item.id,
        quantity: item.quantity,
        price: item.price
      }))
    }

    const response = await ordersAPI.create(orderData)
    
    // Очищаем корзину после успешного заказа
    cartStore.clearCart()
    
    // Перенаправляем на страницу успеха (можно создать отдельную страницу)
    alert(`Заказ успешно оформлен! Номер заказа: ${response.data.id}`)
    router.push('/')
  } catch (err) {
    error.value = err.response?.data?.message || 'Ошибка при оформлении заказа'
    console.error('Error creating order:', err)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.checkout-page {
  padding: 2rem 0;
}

.checkout-page h1 {
  font-size: 2rem;
  margin-bottom: 2rem;
}

.empty-cart {
  text-align: center;
  padding: 4rem 2rem;
}

.checkout-content {
  display: grid;
  grid-template-columns: 1fr 400px;
  gap: 2rem;
}

.checkout-form h2,
.order-summary h2 {
  font-size: 1.5rem;
  margin-bottom: 1.5rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
}

.form-input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
  font-family: inherit;
}

.form-input:focus {
  outline: none;
  border-color: #007bff;
}

.order-items {
  margin-bottom: 1.5rem;
}

.order-item {
  display: flex;
  justify-content: space-between;
  padding: 0.75rem 0;
  border-bottom: 1px solid #eee;
}

.order-total {
  display: flex;
  justify-content: space-between;
  font-size: 1.25rem;
  font-weight: bold;
  padding-top: 1rem;
  border-top: 2px solid #333;
}

.btn-block {
  width: 100%;
  margin-top: 1rem;
}

@media (max-width: 968px) {
  .checkout-content {
    grid-template-columns: 1fr;
  }
}
</style>
