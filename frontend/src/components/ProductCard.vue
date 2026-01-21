<template>
  <div class="product-card card">
    <div class="product-image">
      <img 
        :src="product.imageUrl || '/placeholder.jpg'" 
        :alt="product.name"
        @error="handleImageError"
      />
    </div>
    <div class="product-info">
      <h3 class="product-name">{{ product.name }}</h3>
      <p class="product-description">{{ stripHtmlTags(product.description) }}</p>
      <div class="product-footer">
        <span class="product-price">{{ formatPrice(product.price) }}</span>
        <button 
          class="btn btn-primary" 
          @click="addToCart"
        >
          В корзину
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useCartStore } from '../store/cart'
import { useRouter } from 'vue-router'

const props = defineProps({
  product: {
    type: Object,
    required: true
  }
})

const cartStore = useCartStore()
const router = useRouter()

const addToCart = () => {
  cartStore.addItem(props.product)
}

const formatPrice = (price) => {
  return new Intl.NumberFormat('ru-RU', {
    style: 'currency',
    currency: 'RUB'
  }).format(price)
}

const handleImageError = (event) => {
  event.target.src = 'https://via.placeholder.com/300x200?text=No+Image'
}

const stripHtmlTags = (html) => {
  if (!html) return ''
  // Удаляем все HTML теги
  const tmp = document.createElement('DIV')
  tmp.innerHTML = html
  return tmp.textContent || tmp.innerText || ''
}
</script>

<style scoped>
.product-card {
  display: flex;
  flex-direction: column;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  cursor: pointer;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.product-image {
  width: 100%;
  height: 200px;
  overflow: hidden;
  border-radius: 4px 4px 0 0;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info {
  padding: 1rem;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.product-name {
  font-size: 1.25rem;
  margin-bottom: 0.5rem;
  color: #333;
}

.product-description {
  color: #666;
  font-size: 0.9rem;
  margin-bottom: 1rem;
  flex: 1;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}

.product-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
}

.product-price {
  font-size: 1.5rem;
  font-weight: bold;
  color: #007bff;
}
</style>
