<template>
  <footer
    class="text-white mt-auto border-t border-white/20 md:pb-0"
    :class="[isProductDetail ? 'pb-40' : 'pb-20', isCart || isCheckout ? 'bg-[#2E2826]' : 'bg-[#3A3331]']"
  >
    <div class="container mx-auto px-4 py-8">
      <div class="footer-panel p-6 md:p-8">
        <div>
          <h2 class="text-lg font-semibold text-white md:text-xl">
            Контакты
          </h2>
          <div class="mt-4 flex flex-col gap-3 text-sm leading-relaxed text-gray-300 md:text-base">
            <address class="not-italic">
              <a
                :href="yandexMapUrl"
                target="_blank"
                rel="noopener noreferrer"
                class="text-inherit transition-colors hover:text-[#F47327]"
              >
                Москва, 19&nbsp;км МКАД, ТЦ «Южные ворота», вход&nbsp;Ф, линия&nbsp;22/66
              </a>
            </address>
            <a
              href="tel:+79809416666"
              class="inline-flex w-fit text-base font-medium text-white transition-colors hover:text-[#F47327] md:text-lg"
            >
              +7 (980) 941-66-66
            </a>
            <div class="footer-social mt-4 flex flex-wrap items-center gap-4">
              <a
                :href="socialLinks.instagram"
                class="footer-social__link text-white transition-colors hover:text-[#F47327]"
                target="_blank"
                rel="noopener noreferrer"
                aria-label="iHome24 в Instagram"
              >
                <svg class="footer-social__icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
                  <rect x="2" y="2" width="20" height="20" rx="5" stroke="currentColor" stroke-width="1.6" />
                  <circle cx="12" cy="12" r="4" stroke="currentColor" stroke-width="1.6" />
                  <circle cx="17.5" cy="6.5" r="1.2" fill="currentColor" />
                </svg>
              </a>
              <a
                :href="socialLinks.telegram"
                class="footer-social__link text-white transition-colors hover:text-[#F47327]"
                target="_blank"
                rel="noopener noreferrer"
                aria-label="iHome24 в Telegram"
              >
                <svg class="footer-social__icon" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
                  <path fill="currentColor" d="M12 0C5.373 0 0 5.373 0 12s5.373 12 12 12 12-5.373 12-12S18.627 0 12 0zm5.562 8.161l-1.84 8.672c-.14.625-.5.777-.99.483l-2.74-2.02-1.32 1.27c-.13.13-.24.24-.49.24l.19-2.7 5.06-4.57c.22-.19-.05-.3-.34-.11l-6.25 3.95-2.7-.84c-.59-.18-.6-.59.12-.88l10.56-4.07c.49-.18.92.11.75.89z" />
                </svg>
              </a>
              <span
                class="footer-social__vk"
                title="ВКонтакте — скоро"
                aria-label="ВКонтакте, ссылка появится позже"
              >
                <svg class="footer-social__icon" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
                  <path
                    fill="currentColor"
                    d="M15.684 0H8.316C1.592 0 0 1.592 0 8.316v7.368C0 22.408 1.592 24 8.316 24h7.368C22.408 24 24 22.408 24 15.684V8.316C24 1.592 22.408 0 15.684 0zm3.692 17.123h-1.744c-.66 0-.864-.525-2.05-1.727-1.033-1-1.49-1.135-1.744-1.135-.356 0-.458.102-.458.593v1.575c0 .424-.135.678-1.253.678-1.846 0-3.896-1.118-5.335-3.202C4.624 10.857 4.03 8.57 4.03 8.096c0-.254.102-.491.593-.491h1.744c.44 0 .61.203.78.678.863 2.491 2.303 4.675 2.896 4.675.22 0 .322-.102.322-.66V9.721c-.068-1.186-.695-1.287-.695-1.71 0-.203.17-.407.44-.407h2.744c.373 0 .508.203.508.644v3.168c0 .372.17.508.271.508.22 0 .407-.136.814-.542 1.254-1.406 2.151-3.574 2.151-3.574.17-.287.44-.474.846-.474h1.744c.524 0 .644.27.524.644-.254 1.186-2.71 4.675-2.71 4.675-.322.407-.44.576 0 .984.305.305 1.287 1.253 1.778 2.02.508.779.88 1.44.88 1.965v.203c0 .424-.221.644-.644.644z"
                  />
                </svg>
              </span>
            </div>
          </div>
        </div>

        <div
          class="mt-8 flex flex-col items-center gap-4 border-t border-white/10 pt-8 sm:flex-row sm:justify-center sm:gap-8"
        >
          <router-link to="/support/contacts" class="text-sm text-gray-400 transition-colors hover:text-white">
            Контакты
          </router-link>
          <router-link to="/support/oferta" class="text-sm text-gray-400 transition-colors hover:text-white">
            Договор оферты
          </router-link>
        </div>

        <div class="mt-8 border-t border-white/10 pt-8 text-center text-sm text-gray-400">
          <p>&copy; 2025 iHome24. Все права защищены.</p>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const yandexMapUrl = 'https://yandex.ru/maps/-/CPfc4L3-'

const socialLinks = {
  instagram: 'https://www.instagram.com/ihome.24',
  telegram: 'https://t.me/ihome24',
}

const route = useRoute()
const isProductDetail = computed(() => route.path.startsWith('/products/') && route.params.id)
const isCart = computed(() => route.path === '/cart')
const isCheckout = computed(() => route.path === '/checkout')
</script>

<style scoped>
.footer-social__icon {
  display: block;
  width: 26px;
  height: 26px;
}

.footer-social__vk {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  opacity: 0.45;
  cursor: default;
  user-select: none;
}
</style>
