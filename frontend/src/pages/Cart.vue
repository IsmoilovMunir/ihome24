<template>
  <div class="cart-page">
    <div class="container">
      <h1>Корзина</h1>

      <div v-if="cartStore.items.length === 0" class="empty-cart">
        <p>Ваша корзина пуста</p>
        <router-link to="/products" class="btn btn-primary">
          Перейти в каталог
        </router-link>
      </div>

      <div v-else class="cart-content">
        <div class="cart-items">
          <div 
            v-for="item in cartStore.items" 
            :key="item.id" 
            class="cart-item card"
          >
            <div class="cart-item-image">
              <img 
                :src="item.imageUrl || '/placeholder.jpg'" 
                :alt="item.name"
                @error="handleImageError"
              />
            </div>
            <div class="cart-item-info">
              <h3>{{ item.name }}</h3>
              <p class="cart-item-price">{{ formatPrice(item.price) }}</p>
            </div>
            <div class="cart-item-quantity">
              <button 
                class="btn btn-secondary" 
                @click="decreaseQuantity(item.id)"
              >
                -
              </button>
              <span class="quantity">{{ item.quantity }}</span>
              <button 
                class="btn btn-secondary" 
                @click="increaseQuantity(item.id)"
              >
                +
              </button>
            </div>
            <div class="cart-item-total">
              <strong>{{ formatPrice(item.price * item.quantity) }}</strong>
            </div>
            <button 
              class="btn btn-danger" 
              @click="removeItem(item.id)"
            >
              Удалить
            </button>
          </div>
        </div>

        <div class="cart-summary card">
          <h2>Итого</h2>
          <div class="summary-row">
            <span>Товаров:</span>
            <span>{{ cartStore.totalItems }}</span>
          </div>
          <div class="summary-row total">
            <span>Сумма:</span>
            <span>{{ formatPrice(cartStore.totalPrice) }}</span>
          </div>
          <router-link to="/checkout" class="btn btn-primary btn-large btn-block">
            Оформить заказ
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useCartStore } from '../store/cart'

const cartStore = useCartStore()

const formatPrice = (price) => {
  return new Intl.NumberFormat('ru-RU', {
    style: 'currency',
    currency: 'RUB'
  }).format(price)
}

const increaseQuantity = (productId) => {
  const item = cartStore.items.find(item => item.id === productId)
  if (item) {
    cartStore.updateQuantity(productId, item.quantity + 1)
  }
}

const decreaseQuantity = (productId) => {
  const item = cartStore.items.find(item => item.id === productId)
  if (item && item.quantity > 1) {
    cartStore.updateQuantity(productId, item.quantity - 1)
  }
}

const removeItem = (productId) => {
  if (confirm('Удалить товар из корзины?')) {
    cartStore.removeItem(productId)
  }
}

const handleImageError = (event) => {
  event.target.src = 'https://via.placeholder.com/100x100?text=No+Image'
}
</script>

<style scoped>
.cart-page {
  padding: 2rem 0;
}

.cart-page h1 {
  font-size: 2rem;
  margin-bottom: 2rem;
}

.empty-cart {
  text-align: center;
  padding: 4rem 2rem;
}

.empty-cart p {
  font-size: 1.25rem;
  color: #666;
  margin-bottom: 2rem;
}

.cart-content {
  display: grid;
  grid-template-columns: 1fr 350px;
  gap: 2rem;
}

.cart-items {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.cart-item {
  display: grid;
  grid-template-columns: 100px 1fr auto auto auto;
  gap: 1rem;
  align-items: center;
  padding: 1rem;
}

.cart-item-image {
  width: 100px;
  height: 100px;
  overflow: hidden;
  border-radius: 4px;
}

.cart-item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cart-item-info h3 {
  font-size: 1.1rem;
  margin-bottom: 0.5rem;
}

.cart-item-price {
  color: #666;
}

.cart-item-quantity {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.cart-item-quantity button {
  width: 32px;
  height: 32px;
  padding: 0;
}

.quantity {
  min-width: 40px;
  text-align: center;
}

.cart-item-total {
  font-size: 1.25rem;
}

.cart-summary {
  position: sticky;
  top: 100px;
  height: fit-content;
}

.cart-summary h2 {
  font-size: 1.5rem;
  margin-bottom: 1.5rem;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  padding: 0.75rem 0;
  border-bottom: 1px solid #eee;
}

.summary-row.total {
  font-size: 1.25rem;
  font-weight: bold;
  border-bottom: none;
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 2px solid #333;
}

.btn-block {
  width: 100%;
  margin-top: 1.5rem;
}

@media (max-width: 968px) {
  .cart-content {
    grid-template-columns: 1fr;
  }

  .cart-item {
    grid-template-columns: 1fr;
    text-align: center;
  }
}
</style>
