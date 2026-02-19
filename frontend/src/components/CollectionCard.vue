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
      class="absolute inset-0 w-full h-full object-cover transition-transform duration-300 group-hover:scale-110 z-0"
    />
    
    <!-- Lightning/flash effect -->
    <div class="collection-lightning" />
    
    <!-- Overlay with wave (rises from bottom on hover) -->
    <div class="collection-overlay overflow-hidden">
      <svg class="collection-wave-svg" viewBox="0 0 200 110" preserveAspectRatio="none">
        <path d="M0,45 Q25,25 50,45 T100,45 T150,45 T200,45 L200,110 L0,110 Z" fill="#E0651D"/>
      </svg>
    </div>
    
    <!-- Content -->
    <div class="collection-content">
      <div class="collection-text">
        <h3 class="collection-title text-sm md:text-xl font-semibold mb-2 overflow-hidden text-ellipsis whitespace-nowrap text-white">
          Коллекции {{ collection.name }}
        </h3>
        <p v-if="collection.description" class="collection-description text-xs md:text-sm text-white line-clamp-2">
          {{ collection.description }}
        </p>
      </div>
      <!-- Arrow icon -->
      <div class="collection-arrow">
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
.collection-content {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  width: 100%;
  padding: 1.75rem 4rem 1.75rem 1.75rem;
  z-index: 20;
  transform: translateY(0.5rem);
}

.collection-text {
  position: relative;
}

.collection-arrow {
  position: absolute;
  top: 1.75rem;
  right: 1.75rem;
  width: 1rem;
  height: 0.75rem;
}

.collection-title,
.collection-description {
  font-family: "bork", sans-serif;
}

/* Lightning flash effect */
.collection-lightning {
  position: absolute;
  inset: 0;
  z-index: 15;
  pointer-events: none;
  background: linear-gradient(
    105deg,
    transparent 0%,
    transparent 40%,
    rgba(255, 255, 255, 0.15) 45%,
    rgba(255, 255, 255, 0.4) 50%,
    rgba(255, 255, 255, 0.15) 55%,
    transparent 60%,
    transparent 100%
  );
  background-size: 200% 100%;
  background-position: 200% 0;
  opacity: 0;
  transition: opacity 0.15s ease-out;
}

.collection-card:hover .collection-lightning {
  opacity: 1;
  animation: lightning-flash 0.6s ease-out forwards;
}

@keyframes lightning-flash {
  0% {
    background-position: -100% 0;
  }
  100% {
    background-position: 200% 0;
  }
}

/* Wave overlay - smoothly rises from bottom, no fading */
.collection-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 110px;
  z-index: 10;
  transform: translateY(100%);
  transition: transform 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.collection-card:hover .collection-overlay {
  transform: translateY(0);
}

.collection-wave-svg {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

@media (max-width: 767px) {
  .collection-card {
    height: calc(100vw - 109px);
    max-width: 100%;
  }

  .collection-content {
    padding: 1rem 1rem 1rem 1rem;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 0.5rem;
  }

  .collection-text {
    flex: 1;
    min-width: 0;
  }

  .collection-arrow {
    position: static;
    flex-shrink: 0;
    width: 0.75rem;
    height: 0.65rem;
  }

  .collection-arrow svg {
    width: 12px;
    height: 10px;
  }
}
</style>
