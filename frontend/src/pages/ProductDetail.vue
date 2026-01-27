<template>
  <div class="product-detail-page">
    <div class="container">
      <div v-if="loading" class="loading">
        Загрузка товара...
      </div>

      <div v-if="error" class="error">
        {{ error }}
      </div>

      <div v-if="!loading && !error && product" class="product-detail">
        <div class="product-detail-content">
          <div class="product-image-section">
            <div class="product-image-large" v-if="currentImageUrl">
              <img 
                :src="getSizedImageUrl(currentImageUrl, 'large')" 
                :alt="product.name"
                loading="lazy"
                @error="handleImageError"
                @load="() => console.log('Main image loaded:', currentImageUrl)"
              />
            </div>
            <div v-else-if="product && (product.gallery?.length > 0 || product.imageUrl)" class="product-image-large" style="display: flex; align-items: center; justify-content: center; background: #f5f5f5;">
              <p style="color: #999;">Загрузка изображения...</p>
            </div>
            <div v-else class="product-image-placeholder">
              <p>Изображение не найдено</p>
            </div>
            <!-- Галерея изображений (если есть) -->
            <div v-if="product && product.gallery && product.gallery.length > 0" class="product-gallery">
              <img 
                v-for="(img, index) in product.gallery" 
                :key="`gallery-${index}-${img}`"
                :src="getSizedImageUrl(img, 'small')" 
                :alt="`${product.name} - изображение ${index + 1}`"
                class="gallery-thumb"
                :class="{ active: index === currentImageIndex }"
                loading="lazy"
                @click="selectImage(index)"
                @error="(e) => { console.error('Gallery image error:', img, e); e.target.style.opacity = '0.3' }"
                @load="() => console.log('Gallery image loaded:', img)"
              />
            </div>
          </div>
          <div class="product-detail-info">
            <!-- Категория -->
            <div v-if="product.category" class="product-category">
              <span class="category-badge">{{ product.category }}</span>
            </div>
            
            <h1>{{ product.name }}</h1>
            
            <!-- SKU -->
            <div v-if="product.sku" class="product-sku">
              <span class="label">Артикул:</span>
              <span class="value">{{ product.sku }}</span>
            </div>
            
            <!-- Описание -->
            <div v-if="product.description" class="product-description-section">
              <h3>Описание</h3>
              <div class="product-description-full" v-html="product.description"></div>
            </div>
            
            <!-- Цена -->
            <div class="product-price-section">
              <div v-if="product.oldPrice && product.oldPrice > product.price" class="price-old">
                {{ formatPrice(product.oldPrice) }}
              </div>
              <div class="product-price-large">
                {{ formatPrice(product.price) }}
              </div>
            </div>
            
            <!-- Наличие на складе -->
            <div class="product-stock">
              <span v-if="product.stockQuantity > 0" class="stock-available">
                В наличии: {{ product.stockQuantity }} шт.
              </span>
              <span v-else class="stock-unavailable">
                Нет в наличии
              </span>
            </div>
            
            <!-- Характеристики (если есть) -->
            <div v-if="product.characteristics && product.characteristics.length > 0" class="product-characteristics">
              <button 
                class="btn btn-link characteristics-btn" 
                @click="showCharacteristicsDrawer = true"
              >
                Характеристики
              </button>
            </div>
            
            <!-- Drawer для характеристик -->
            <div 
              v-if="showCharacteristicsDrawer" 
              class="characteristics-drawer-overlay"
              @click="showCharacteristicsDrawer = false"
            >
              <div 
                class="characteristics-drawer"
                @click.stop
              >
                <div class="drawer-header">
                  <h2>Характеристики</h2>
                  <button 
                    class="drawer-close-btn"
                    @click="showCharacteristicsDrawer = false"
                  >
                    ×
                  </button>
                </div>
                <div class="drawer-content">
                  <table class="characteristics-table">
                    <tr v-for="(char, index) in product.characteristics" :key="index">
                      <td class="char-name">{{ char.name }}</td>
                      <td class="char-value">{{ char.value }}</td>
                    </tr>
                  </table>
                </div>
              </div>
            </div>
            
            <!-- Действия -->
            <div class="product-actions">
              <div class="quantity-selector">
                <button 
                  class="btn btn-secondary" 
                  @click="decreaseQuantity"
                  :disabled="quantity <= 1"
                >
                  -
                </button>
                <span class="quantity">{{ quantity }}</span>
                <button 
                  class="btn btn-secondary" 
                  @click="increaseQuantity"
                  :disabled="quantity >= product.stockQuantity"
                >
                  +
                </button>
              </div>
              <button 
                class="btn btn-primary btn-large" 
                @click="addToCart"
                :disabled="product.stockQuantity === 0"
              >
                {{ product.stockQuantity > 0 ? 'Добавить в корзину' : 'Нет в наличии' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useProductsStore } from '../store/products'
import { useCartStore } from '../store/cart'

const route = useRoute()
const productsStore = useProductsStore()
const cartStore = useCartStore()

const { product, loading, error } = storeToRefs(productsStore)
const quantity = ref(1)
const currentImageIndex = ref(0)
const showCharacteristicsDrawer = ref(false)

const formatPrice = (price) => {
  return new Intl.NumberFormat('ru-RU', {
    style: 'currency',
    currency: 'RUB'
  }).format(price)
}

const increaseQuantity = () => {
  if (product.value && quantity.value < product.value.stockQuantity) {
    quantity.value++
  }
}

const decreaseQuantity = () => {
  if (quantity.value > 1) {
    quantity.value--
  }
}

const addToCart = () => {
  if (product.value && product.value.stockQuantity > 0) {
    const maxQuantity = Math.min(quantity.value, product.value.stockQuantity)
    for (let i = 0; i < maxQuantity; i++) {
      cartStore.addItem(product.value)
    }
    quantity.value = 1
    alert(`Товар добавлен в корзину! (${maxQuantity} шт.)`)
  }
}

const handleImageError = (event) => {
  console.error('Ошибка загрузки изображения:', event.target.src)
  // Скрываем изображение при ошибке или используем прозрачный пиксель
  event.target.style.display = 'none'
  // Или можно использовать data URL для прозрачного изображения
  // event.target.src = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="600" height="400"%3E%3Crect width="600" height="400" fill="%23f0f0f0"/%3E%3Ctext x="50%25" y="50%25" text-anchor="middle" dy=".3em" fill="%23999"%3EИзображение не найдено%3C/text%3E%3C/svg%3E'
}

const getSizedImageUrl = (url, size) => {
  if (!url) return ''
  return url.replace(/\/(small|medium|large|original)\//, `/${size}/`)
}

const selectImage = (index) => {
  currentImageIndex.value = index
}

const currentImageUrl = computed(() => {
  const prod = product.value
  if (!prod) {
    return ''
  }
  
  // Проверяем галерею
  if (prod.gallery && Array.isArray(prod.gallery) && prod.gallery.length > 0) {
    const index = Math.max(0, Math.min(currentImageIndex.value, prod.gallery.length - 1))
    const url = prod.gallery[index]
    if (url && typeof url === 'string' && url.trim()) {
      return url
    }
  }
  
  // Используем imageUrl как fallback
  if (prod.imageUrl && typeof prod.imageUrl === 'string' && prod.imageUrl.trim()) {
    return prod.imageUrl
  }
  
  return ''
})

onMounted(async () => {
  try {
    await productsStore.fetchProductById(route.params.id)
    // Сбрасываем индекс при загрузке нового товара
    currentImageIndex.value = 0
    await nextTick()
    console.log('Product loaded:', product.value)
    console.log('Gallery:', product.value?.gallery)
    console.log('Current image URL:', currentImageUrl.value)
  } catch (err) {
    console.error('Error loading product:', err)
  }
})
</script>

<style scoped>
.product-detail-page {
  padding: 2rem 0;
}

.product-detail {
  background: white;
  border-radius: 8px;
  padding: 2rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.product-detail-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 3rem;
}

.product-image-section {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.product-image-large {
  width: 100%;
  height: 500px;
  overflow: hidden;
  border-radius: 8px;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.product-image-large img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  background: white;
}

.product-image-placeholder {
  width: 100%;
  height: 500px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  border-radius: 8px;
  color: #999;
}

.product-gallery {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.gallery-thumb {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
  cursor: pointer;
  border: 2px solid transparent;
  transition: border-color 0.3s;
}

.gallery-thumb:hover {
  border-color: #007bff;
}

.gallery-thumb.active {
  border-color: #007bff;
  border-width: 3px;
}

.product-category {
  margin-bottom: 1rem;
}

.category-badge {
  display: inline-block;
  padding: 0.25rem 0.75rem;
  background: #f0f0f0;
  border-radius: 4px;
  font-size: 0.9rem;
  color: #666;
}

.product-sku {
  margin-bottom: 1rem;
  color: #666;
  font-size: 0.9rem;
}

.product-sku .label {
  font-weight: bold;
  margin-right: 0.5rem;
}

.product-description-section {
  margin-bottom: 2rem;
}

.product-description-section h3 {
  font-size: 1.25rem;
  margin-bottom: 0.5rem;
  color: #333;
}

.product-detail-info h1 {
  font-size: 2rem;
  margin-bottom: 1rem;
}

.product-description-full {
  color: #666;
  font-size: 1.1rem;
  line-height: 1.5;
  margin-bottom: 2rem;
}

.product-description-full :deep(p) {
  margin-bottom: 0;
  margin-top: 0;
  padding: 0;
}

.product-description-full :deep(p + p) {
  margin-top: 0.5rem;
}

.product-price-section {
  margin-bottom: 2rem;
}

.price-old {
  font-size: 1.5rem;
  color: #999;
  text-decoration: line-through;
  margin-bottom: 0.5rem;
}

.product-price-large {
  font-size: 2.5rem;
  font-weight: bold;
  color: #007bff;
}

.product-stock {
  margin-bottom: 2rem;
  font-size: 1rem;
}

.stock-available {
  color: #28a745;
  font-weight: bold;
}

.stock-unavailable {
  color: #dc3545;
  font-weight: bold;
}

.product-characteristics {
  margin-bottom: 2rem;
}

.characteristics-btn {
  background: none;
  border: none;
  color: #007bff;
  text-decoration: underline;
  cursor: pointer;
  font-size: 1rem;
  padding: 0;
}

.characteristics-btn:hover {
  color: #0056b3;
}

.characteristics-drawer-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  justify-content: flex-end;
  animation: fadeIn 0.3s ease;
}

.characteristics-drawer {
  width: 500px;
  max-width: 90vw;
  height: 100%;
  background: white;
  box-shadow: -2px 0 8px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  animation: slideInRight 0.3s ease;
}

.drawer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #eee;
}

.drawer-header h2 {
  margin: 0;
  font-size: 1.5rem;
  color: #333;
}

.drawer-close-btn {
  background: none;
  border: none;
  font-size: 2rem;
  color: #666;
  cursor: pointer;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.drawer-close-btn:hover {
  background-color: #f5f5f5;
}

.drawer-content {
  flex: 1;
  overflow-y: auto;
  padding: 1.5rem;
}

.characteristics-table {
  width: 100%;
  border-collapse: collapse;
}

.characteristics-table tr {
  border-bottom: 1px solid #eee;
}

.characteristics-table td {
  padding: 0.75rem 0;
}

.char-name {
  font-weight: bold;
  color: #666;
  width: 40%;
}

.char-value {
  color: #333;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideInRight {
  from {
    transform: translateX(100%);
  }
  to {
    transform: translateX(0);
  }
}

@media (max-width: 768px) {
  .characteristics-drawer {
    width: 100%;
    max-width: 100vw;
  }
}

.product-actions {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.quantity-selector {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.quantity-selector button {
  width: 40px;
  height: 40px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.quantity {
  font-size: 1.25rem;
  font-weight: bold;
  min-width: 40px;
  text-align: center;
}

.btn-large {
  padding: 1rem 2rem;
  font-size: 1.1rem;
}

@media (max-width: 768px) {
  .product-detail-content {
    grid-template-columns: 1fr;
  }
}
</style>
