<template>
  <router-link
    :to="`/products?category=${collection.id}`"
    class="collection-card relative block w-full h-[460px] overflow-hidden text-white rounded-xl cursor-pointer transition-all duration-300 ease-in-out group"
    style="background-color: #26211E;"
  >
    <!-- Image -->
    <img
      v-if="imageUrl"
      :src="imageUrl"
      :alt="collection.name"
      class="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 w-[90%] h-[90%] object-contain transition-transform duration-300 group-hover:scale-110"
    />
    
    <!-- Content -->
    <div class="absolute bottom-0 w-full p-7 pr-16 z-20 transform translate-y-2">
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

.collection-card:hover {
  box-shadow: inset 0 -70px 0 0 #F37021;
}

@media (max-width: 767px) {
  .collection-card {
    height: calc(100vw - 109px);
    max-width: 100%;
  }
  
  .collection-card:hover {
    box-shadow: inset 0 -70px 0 0 #F37021;
  }
}
</style>
