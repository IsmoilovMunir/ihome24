<script setup>
defineProps({
  name: { type: String, required: true },
})
</script>

<template>
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" aria-hidden="true">
    <!-- prices -->
    <template v-if="name === 'prices'">
      <path d="M12 3v18M7 8h8a3 3 0 010 6H9a3 3 0 000 6h10" stroke-linecap="round" />
    </template>
    <!-- steps -->
    <template v-else-if="name === 'steps'">
      <path d="M4 7h16M4 12h10M4 17h6" stroke-linecap="round" />
      <circle cx="18" cy="12" r="3" />
    </template>
    <!-- audience -->
    <template v-else-if="name === 'shop'">
      <path d="M4 9h16l-1.5 10H5.5L4 9z" stroke-linejoin="round" />
      <path d="M9 9V6a3 3 0 016 0v3" stroke-linecap="round" />
    </template>
    <template v-else-if="name === 'hotel'">
      <path d="M4 20V8l8-4 8 4v12" stroke-linejoin="round" />
      <path d="M9 20v-6h6v6" stroke-linecap="round" />
    </template>
    <template v-else-if="name === 'cafe'">
      <path d="M6 8h12v8a2 2 0 01-2 2H8a2 2 0 01-2-2V8z" />
      <path d="M18 10h1a2 2 0 012 2v2a2 2 0 01-2 2h-1" stroke-linecap="round" />
    </template>
    <template v-else-if="name === 'office'">
      <rect x="4" y="6" width="16" height="14" rx="2" />
      <path d="M9 10h2M13 10h2M9 14h6" stroke-linecap="round" />
    </template>
    <template v-else-if="name === 'reseller'">
      <path d="M7 7h10v10H7z" stroke-linejoin="round" />
      <path d="M4 12h3M17 12h3M12 4v3M12 17v3" stroke-linecap="round" />
    </template>
    <template v-else-if="name === 'marketplace'">
      <rect x="3" y="4" width="18" height="16" rx="2.5" />
      <path d="M7 9h4M7 13h10M7 17h6" stroke-linecap="round" />
    </template>
    <!-- docs / delivery / benefits -->
    <template v-else-if="name === 'docs'">
      <path d="M8 4h8l4 4v12a1 1 0 01-1 1H5a1 1 0 01-1-1V5a1 1 0 011-1h3z" stroke-linejoin="round" />
      <path d="M16 4v4h4M9 13h6M9 17h4" stroke-linecap="round" />
    </template>
    <template v-else-if="name === 'delivery'">
      <path d="M3 7h11v8H3z" stroke-linejoin="round" />
      <path d="M14 10h4l3 3v2h-7v-5z" stroke-linejoin="round" />
      <circle cx="7" cy="17" r="2" /><circle cx="17" cy="17" r="2" />
    </template>
    <template v-else-if="name === 'assortment'">
      <path d="M4 7h16M4 12h16M4 17h10" stroke-linecap="round" />
      <rect x="3" y="4" width="18" height="16" rx="2" />
    </template>
    <template v-else-if="name === 'star'">
      <path d="M12 3l2.4 5.5L20 9.5l-4.5 4 1.2 6L12 17l-4.7 2.5 1.2-6L4 9.5l5.6-1L12 3z" stroke-linejoin="round" />
    </template>
    <template v-else-if="name === 'box'">
      <path d="M12 3l8 4.5v9L12 21l-8-4.5v-9L12 3z" stroke-linejoin="round" />
      <path d="M12 12l8-4.5M12 12v9M12 12L4 7.5" />
    </template>
    <template v-else-if="name === 'tag'">
      <path d="M20 12l-8 8-8-8V4h8l8 8z" stroke-linejoin="round" />
      <circle cx="9" cy="9" r="1.5" fill="currentColor" stroke="none" />
    </template>
    <template v-else-if="name === 'manager'">
      <circle cx="12" cy="8" r="3.5" />
      <path d="M5 20c0-3.5 3-6 7-6s7 2.5 7 6" stroke-linecap="round" />
    </template>
    <template v-else-if="name === 'truck'">
      <path d="M3 7h11v8H3z" stroke-linejoin="round" />
      <path d="M14 10h4l3 3v2h-7v-5z" stroke-linejoin="round" />
      <circle cx="7" cy="17" r="2" /><circle cx="17" cy="17" r="2" />
    </template>
    <template v-else-if="name === 'file'">
      <path d="M8 4h6l4 4v12H8V4z" stroke-linejoin="round" />
      <path d="M14 4v4h4" stroke-linecap="round" />
    </template>
    <template v-else-if="name === 'faq'">
      <circle cx="12" cy="12" r="9" />
      <path d="M9.5 9a2.5 2.5 0 014.8 1c0 1.5-2.3 1.5-2.3 3" stroke-linecap="round" />
      <circle cx="12" cy="17" r="0.5" fill="currentColor" stroke="none" />
    </template>
    <template v-else-if="name === 'phone'">
      <path d="M6.5 4h3l1.5 4-2 1.5a11 11 0 005 5L17 12.5 21 14v3a2 2 0 01-2 2A16 16 0 014 6.5a2 2 0 012-2.5z" stroke-linejoin="round" />
    </template>
    <template v-else-if="name === 'mail'">
      <rect x="3" y="5" width="18" height="14" rx="2" />
      <path d="M3 7l9 6 9-6" stroke-linecap="round" stroke-linejoin="round" />
    </template>
    <template v-else-if="name === 'clock'">
      <circle cx="12" cy="12" r="9" />
      <path d="M12 7v5l3 2" stroke-linecap="round" />
    </template>
    <template v-else-if="name === 'calc'">
      <rect x="5" y="3" width="14" height="18" rx="2" />
      <path d="M8 8h8M8 12h3M13 12h3M8 16h3M13 16h3" stroke-linecap="round" />
    </template>
    <template v-else-if="name === 'appliance'">
      <rect x="5" y="3" width="14" height="18" rx="2" />
      <circle cx="12" cy="14" r="3" />
      <path d="M9 3v3M15 3v3" stroke-linecap="round" />
    </template>
    <template v-else-if="name === 'kettle'">
      <path d="M5 10h11a3 3 0 010 6H7a2 2 0 01-2-2v-2a2 2 0 012-2z" stroke-linejoin="round" />
      <path d="M16 12h2a2 2 0 012 2v1a2 2 0 01-2 2h-2" stroke-linecap="round" />
      <path d="M8 6V4M12 6V4" stroke-linecap="round" />
    </template>
    <template v-else-if="name === 'kitchenware'">
      <ellipse cx="12" cy="14" rx="8" ry="3" />
      <path d="M4 14V9a8 3 0 0116 0v5" stroke-linejoin="round" />
      <path d="M12 6V3" stroke-linecap="round" />
    </template>
    <template v-else-if="name === 'cctv'">
      <path d="M4 8h10v8H4z" stroke-linejoin="round" />
      <path d="M14 11l6-3v8l-6-3v-2z" stroke-linejoin="round" />
      <circle cx="9" cy="12" r="2" />
    </template>
    <template v-else-if="name === 'charge'">
      <rect x="7" y="3" width="10" height="18" rx="2" />
      <path d="M11 7h2M11 11h2M11 15h2" stroke-linecap="round" />
      <path d="M4 10h3M17 10h3" stroke-linecap="round" />
    </template>
    <template v-else-if="name === 'headphones'">
      <path d="M4 12v4a2 2 0 002 2h1V10H5a1 1 0 00-1 1zM20 12v4a2 2 0 01-2 2h-1V10h2a1 1 0 011 1z" stroke-linejoin="round" />
      <path d="M4 12a8 8 0 0116 0" stroke-linecap="round" />
    </template>
    <template v-else-if="name === 'auto'">
      <path d="M5 16l1.5-5h11L19 16" stroke-linejoin="round" />
      <rect x="3" y="16" width="18" height="4" rx="1" />
      <circle cx="7.5" cy="18" r="1" /><circle cx="16.5" cy="18" r="1" />
    </template>
    <template v-else-if="name === 'microwave'">
      <rect x="4" y="5" width="16" height="14" rx="2" />
      <rect x="7" y="8" width="7" height="8" rx="1" />
      <circle cx="17" cy="10" r="0.75" fill="currentColor" stroke="none" />
      <circle cx="17" cy="14" r="0.75" fill="currentColor" stroke="none" />
    </template>
    <template v-else-if="name === 'blender'">
      <path d="M9 4h6v4H9z" stroke-linejoin="round" />
      <path d="M8 8h8v10a2 2 0 01-2 2h-4a2 2 0 01-2-2V8z" stroke-linejoin="round" />
      <path d="M10 14h4" stroke-linecap="round" />
    </template>
    <template v-else-if="name === 'humidifier'">
      <path d="M8 18h8" stroke-linecap="round" />
      <path d="M9 18V9a3 3 0 016 0v9" stroke-linejoin="round" />
      <path d="M12 5V3M10 6s1 2 2 2 2-2 2-2" stroke-linecap="round" />
    </template>
    <template v-else-if="name === 'vacuum'">
      <circle cx="12" cy="13" r="6" />
      <path d="M12 7V4M8 4h8" stroke-linecap="round" />
      <path d="M15 13h3" stroke-linecap="round" />
    </template>
    <template v-else-if="name === 'beauty'">
      <circle cx="12" cy="8" r="3" />
      <path d="M7 20c0-3 2.5-5 5-5s5 2 5 5" stroke-linecap="round" />
      <path d="M5 12h2M17 12h2" stroke-linecap="round" />
    </template>
    <template v-else-if="name === 'light'">
      <path d="M9 18h6M10 18v2h4v-2" stroke-linecap="round" />
      <path d="M8 10a4 4 0 018 0c0 2-1 3-2 4H10c-1-1-2-2-2-4z" stroke-linejoin="round" />
    </template>
    <template v-else>
      <circle cx="12" cy="12" r="9" />
    </template>
  </svg>
</template>
