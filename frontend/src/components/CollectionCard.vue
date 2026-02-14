<template>
  <router-link
    :to="`/products?category=${collection.id}`"
    class="collection-card protect-image relative block w-full h-[460px] overflow-hidden text-white rounded-xl cursor-pointer transition-all duration-300 ease-in-out group"
    style="background-color: #26211E;"
    @contextmenu.prevent
  >
    <!-- Image (под текстом) -->
    <img
      v-if="imageUrl"
      :src="imageUrl"
      :alt="collection.name"
      class="absolute top-1/2 left-1/2 z-0 w-[90%] h-[90%] object-contain transition-transform duration-300 group-hover:scale-110 -translate-x-1/2 -translate-y-1/2"
      draggable="false"
    />
    
    <!-- Фон под текстом и сам текст поверх фото; оранжевый фон — при наведении -->
    <div class="collection-card-content absolute bottom-0 left-0 right-0 z-10 w-full p-7 pr-16 text-white">
      <h3 class="collection-title text-xl font-semibold mb-2 overflow-hidden text-ellipsis whitespace-nowrap text-white">
        Коллекции {{ collection.name }}
      </h3>
      <p v-if="collection.description" class="collection-description text-sm text-white line-clamp-2">
        {{ collection.description }}
      </p>
      
      <!-- Arrow icon -->
      <div class="absolute top-7 right-7 w-4 h-3">
        <svg width="15" height="13" viewBox="0 0 15 13" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path fill-rule="evenodd" fill="#FFF" d="M9.5 12l-1-1 4.2-4H0V6h12.7l-4-4 .8-1L15 6.5 9.5 12z"/>
        </svg>
      </div>
    </div>
  </router-link>
</template>

<script setup>
import { computed } from 'vue'
import { fileApi } from '../services/api'

const props = defineProps({
  collection: {
    type: Object,
    required: true,
  },
})

const imageUrl = computed(() => {
  if (props.collection.imageUrl) {
    return fileApi.getFileUrl(props.collection.imageUrl)
  }
  return null
})
</script>

<style scoped>
.collection-title,
.collection-description {
  font-family: "bork", sans-serif;
}

.collection-card {
  will-change: box-shadow;
}

/* По умолчанию — полупрозрачный тёмный фон, волнистый верх */
.collection-card-content {
  padding-top: 2rem;
  background: linear-gradient(to top, rgba(38, 33, 30, 0.9) 0%, rgba(38, 33, 30, 0.5) 50%, transparent 100%);
  clip-path: polygon(0 12%, 8% 6%, 16% 14%, 24% 4%, 32% 12%, 40% 6%, 50% 10%, 60% 5%, 68% 12%, 76% 7%, 84% 11%, 92% 6%, 100% 10%, 100% 100%, 0 100%);
  transition: background 0.35s ease;
}

/* При наведении — оранжевый #F37021 и живая волна */
.collection-card:hover .collection-card-content {
  background: linear-gradient(to top, #F37021 0%, rgba(243, 112, 33, 0.98) 70%, transparent 100%);
  animation: wave-flow 3s ease-in-out infinite;
}

@keyframes wave-flow {
  0%, 100% {
    clip-path: polygon(0 14%, 10% 6%, 20% 16%, 30% 4%, 40% 12%, 50% 8%, 60% 14%, 70% 6%, 80% 12%, 90% 4%, 100% 14%, 100% 100%, 0 100%);
  }
  50% {
    clip-path: polygon(0 8%, 10% 14%, 20% 6%, 30% 14%, 40% 8%, 50% 14%, 60% 6%, 70% 12%, 80% 6%, 90% 14%, 100% 8%, 100% 100%, 0 100%);
  }
}

@media (max-width: 767px) {
  .collection-card {
    height: calc(100vw - 109px);
    max-width: 100%;
  }
}
</style>
