<template>
  <div class="ws-page">
    <div class="ws-page__bg" aria-hidden="true">
      <span class="ws-page__orb ws-page__orb--1" />
      <span class="ws-page__orb ws-page__orb--2" />
      <span class="ws-page__orb ws-page__orb--3" />
    </div>

    <div class="ws-page__inner container mx-auto px-4">
      <!-- Hero -->
      <header class="ws-hero ws-reveal">
        <NuxtLink to="/" class="ws-back">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
            <path d="M19 12H5M12 19l-7-7 7-7" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
          На главную
        </NuxtLink>

        <span class="ws-badge ws-badge--pulse">Оптовым клиентам</span>
        <h1 class="ws-h1">Товары для дома оптом — iHome24</h1>
        <p class="ws-lead">
          Оптово-розничный поставщик для ИП, ООО и самозанятых. Договор, УПД, доставка по России.
        </p>

        <div class="ws-hero-stats">
          <div v-for="stat in heroStats" :key="stat.label" class="ws-stat">
            <span class="ws-stat__icon" aria-hidden="true"><WholesaleIcon :name="stat.icon" /></span>
            <span class="ws-stat__val">{{ stat.value }}</span>
            <span class="ws-stat__label">{{ stat.label }}</span>
          </div>
        </div>

        <div class="ws-actions">
          <a href="#wholesale-contact" class="ws-btn ws-btn--primary">
            <WholesaleIcon name="phone" />
            Оставить заявку
          </a>
          <NuxtLink to="/products" class="ws-btn ws-btn--secondary">
            <WholesaleIcon name="assortment" />
            Каталог
          </NuxtLink>
        </div>
      </header>

      <div class="ws-content">
        <!-- Цены -->
        <section class="ws-block ws-reveal">
          <div class="ws-block__head">
            <span class="ws-block__icon"><WholesaleIcon name="prices" /></span>
            <h2 class="ws-h2">Оптовые цены и условия</h2>
          </div>
          <p class="ws-text">Прогрессивные скидки от розничной цены:</p>

          <div class="ws-tiers">
            <div
              v-for="(row, i) in discountRows"
              :key="row.sum"
              class="ws-tier"
              :class="{ 'ws-tier--vip': i === discountRows.length - 1 }"
              :style="{ animationDelay: `${i * 0.08}s` }"
            >
              <span class="ws-tier__discount">{{ row.discount }}</span>
              <span class="ws-tier__sum">{{ row.sum }}</span>
              <span class="ws-tier__status">{{ row.status }}</span>
            </div>
          </div>

          <p class="ws-text ws-text--sm">
            Мин. первый заказ — 10&nbsp;000&nbsp;₽. VIP-условия от 150&nbsp;000&nbsp;₽/мес.
          </p>

          <div class="ws-calc">
            <div class="ws-calc__head">
              <span class="ws-calc__icon"><WholesaleIcon name="calc" /></span>
              <h3 class="ws-h3">Калькулятор скидки</h3>
            </div>
            <label class="ws-calc__label">
              <span>Сумма заказа, ₽</span>
              <input
                v-model.number="calcAmount"
                type="number"
                min="0"
                step="1000"
                class="ws-input"
                placeholder="10000"
              />
            </label>
            <Transition name="ws-pop">
              <div v-if="calcResult" class="ws-calc__result">
                <span class="ws-calc__badge">{{ calcResult.discount }}</span>
                <span class="ws-calc__status">{{ calcResult.status }}</span>
              </div>
            </Transition>
          </div>

          <div class="ws-cta">
            <p class="ws-cta__text">Узнали свою скидку? Менеджер закрепит условия в договоре.</p>
            <button type="button" class="ws-btn ws-btn--primary ws-btn--sm" @click="scrollToForm('price')">
              Получить условия
            </button>
          </div>
        </section>

        <!-- Шаги -->
        <section class="ws-block ws-reveal">
          <div class="ws-block__head">
            <span class="ws-block__icon"><WholesaleIcon name="steps" /></span>
            <h2 class="ws-h2">Как стать клиентом</h2>
          </div>
          <ol class="ws-steps">
            <li v-for="(step, i) in steps" :key="i" class="ws-step">
              <span class="ws-step__num">{{ i + 1 }}</span>
              <span class="ws-step__text">{{ step }}</span>
            </li>
          </ol>
          <button type="button" class="ws-cta-link" @click="scrollToForm('start')">
            Начать подключение →
          </button>
        </section>

        <!-- Аудитория -->
        <section class="ws-block ws-reveal">
          <div class="ws-block__head">
            <span class="ws-block__icon"><WholesaleIcon name="shop" /></span>
            <h2 class="ws-h2">Кому подходит</h2>
          </div>
          <p class="ws-text"><strong>Товары для дома оптом</strong> — для бизнеса любого масштаба:</p>
          <div class="ws-grid">
            <article v-for="item in audience" :key="item.title" class="ws-card">
              <span class="ws-card__icon"><WholesaleIcon :name="item.icon" /></span>
              <h3 class="ws-h3">{{ item.title }}</h3>
              <p class="ws-text ws-text--sm">{{ item.text }}</p>
            </article>
          </div>
          <div class="ws-cta">
            <p class="ws-cta__text">Подберём ассортимент под ваш формат бизнеса.</p>
            <button type="button" class="ws-btn ws-btn--primary ws-btn--sm" @click="scrollToForm('partner')">
              Обсудить поставки
            </button>
          </div>
        </section>
        <section class="ws-block ws-reveal">
          <div class="ws-block__head">
            <span class="ws-block__icon"><WholesaleIcon name="docs" /></span>
            <h2 class="ws-h2">Документы и оплата</h2>
          </div>
          <div class="ws-chips">
            <span v-for="doc in documents" :key="doc" class="ws-chip">
              <WholesaleIcon name="file" />
              {{ doc }}
            </span>
          </div>
          <p class="ws-text ws-text--sm">
            Оплата: р/с, корп. карта, наличные. ЭДО: Диадок, СБИС, Контур. НДС 20%, работаем с УСН.
            Отсрочка 14–30 дней — от 3 мес. сотрудничества.
          </p>
        </section>

        <!-- Доставка -->
        <section class="ws-block ws-reveal">
          <div class="ws-block__head">
            <span class="ws-block__icon"><WholesaleIcon name="delivery" /></span>
            <h2 class="ws-h2">Доставка</h2>
          </div>
          <ul class="ws-list">
            <li v-for="d in delivery" :key="d">
              <span class="ws-list__icon"><WholesaleIcon name="truck" /></span>
              {{ d }}
            </li>
          </ul>
          <p class="ws-text ws-text--sm">Обработка 1–2 дня. Сборные грузы — ежедневно.</p>
        </section>

        <!-- Ассортимент -->
        <section class="ws-block ws-reveal">
          <div class="ws-block__head">
            <span class="ws-block__icon"><WholesaleIcon name="assortment" /></span>
            <h2 class="ws-h2">Хиты оптовых закупок</h2>
          </div>
          <p class="ws-text ws-text--sm">
            Топ-категории из нашего каталога — то, что чаще всего берут магазины, селлеры и HoReCa:
          </p>
          <div class="ws-cats">
            <article
              v-for="(cat, i) in topCategories"
              :key="cat.title"
              class="ws-cat"
              :class="{ 'ws-cat--hit': cat.hit }"
              :style="{ animationDelay: `${i * 0.05}s` }"
            >
              <span v-if="cat.hit" class="ws-cat__hit">Хит</span>
              <span class="ws-cat__icon"><WholesaleIcon :name="cat.icon" /></span>
              <h3 class="ws-cat__title">{{ cat.title }}</h3>
              <p class="ws-cat__hint">{{ cat.hint }}</p>
            </article>
          </div>
          <p class="ws-text ws-text--sm ws-cats-foot">
            И ещё 70+ направлений: кофемолки, фритюрницы, Wi‑Fi роутеры, автомобильная акустика и др.
            <NuxtLink to="/products" class="ws-cats-link">Весь каталог →</NuxtLink>
          </p>
          <div class="ws-cta">
            <p class="ws-cta__text">Нужен оптовый прайс с актуальными остатками?</p>
            <button type="button" class="ws-btn ws-btn--primary ws-btn--sm" @click="scrollToForm('pricelist')">
              Запросить прайс
            </button>
          </div>
        </section>

        <!-- Преимущества -->
        <section class="ws-block ws-reveal">
          <div class="ws-block__head">
            <span class="ws-block__icon"><WholesaleIcon name="star" /></span>
            <h2 class="ws-h2">Почему iHome24</h2>
          </div>
          <div class="ws-benefits">
            <div v-for="b in benefits" :key="b.title" class="ws-benefit">
              <span class="ws-benefit__icon"><WholesaleIcon :name="b.icon" /></span>
              <div>
                <h3 class="ws-h3">{{ b.title }}</h3>
                <p class="ws-text ws-text--sm">{{ b.text }}</p>
              </div>
            </div>
          </div>
        </section>

        <!-- Конверсия перед FAQ -->
        <div class="ws-cta-banner ws-reveal">
          <div class="ws-cta-banner__body">
            <p class="ws-cta-banner__title">Готовы к первому оптовому заказу?</p>
            <p class="ws-cta-banner__sub">Перезвоним за 15 минут · мин. заказ 10 000 ₽</p>
          </div>
          <div class="ws-cta-banner__actions">
            <button type="button" class="ws-btn ws-btn--primary ws-btn--sm" @click="scrollToForm()">
              Оставить заявку
            </button>
            <a :href="`tel:${phoneClean}`" class="ws-btn ws-btn--secondary ws-btn--sm">
              Позвонить
            </a>
          </div>
        </div>

        <!-- FAQ -->
        <section class="ws-block ws-reveal">
          <div class="ws-block__head">
            <span class="ws-block__icon"><WholesaleIcon name="faq" /></span>
            <h2 class="ws-h2">Вопросы и ответы</h2>
          </div>
          <div class="ws-faq">
            <details v-for="item in faq" :key="item.q" class="ws-faq__item">
              <summary class="ws-faq__q">
                {{ item.q }}
                <svg class="ws-faq__chevron" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                  <path d="M6 9l6 6 6-6" stroke-linecap="round" stroke-linejoin="round" />
                </svg>
              </summary>
              <p class="ws-faq__a">{{ item.a }}</p>
            </details>
          </div>
        </section>

        <!-- Контакты -->
        <section id="wholesale-contact" class="ws-block ws-block--contact ws-reveal">
          <div class="ws-block__head">
            <span class="ws-block__icon"><WholesaleIcon name="phone" /></span>
            <h2 class="ws-h2">Связаться с менеджером</h2>
          </div>
          <p class="ws-text ws-text--sm">Перезвоним за 15 минут в рабочее время:</p>

          <div class="ws-contacts">
            <a :href="`tel:${phoneClean}`" class="ws-contact">
              <span class="ws-contact__icon"><WholesaleIcon name="phone" /></span>
              <span class="ws-contact__label">Телефон</span>
              <span class="ws-contact__val">+7 (980) 941-66-66</span>
            </a>
            <a href="mailto:opt@ihome24.ru" class="ws-contact">
              <span class="ws-contact__icon"><WholesaleIcon name="mail" /></span>
              <span class="ws-contact__label">Email</span>
              <span class="ws-contact__val">opt@ihome24.ru</span>
            </a>
            <div class="ws-contact ws-contact--static">
              <span class="ws-contact__icon"><WholesaleIcon name="clock" /></span>
              <span class="ws-contact__label">Ежедневно</span>
              <span class="ws-contact__val ws-contact__val--hours">8:00–20:00</span>
            </div>
          </div>

          <form class="ws-form" @submit.prevent="submitForm">
            <div class="ws-form__row">
              <input v-model="form.name" type="text" required placeholder="Имя / компания" class="ws-input" />
              <input v-model="form.phone" type="tel" required placeholder="Телефон" class="ws-input" />
            </div>
            <input v-model="form.inn" type="text" placeholder="ИНН (необязательно)" class="ws-input" />
            <textarea v-model="form.message" rows="2" placeholder="Комментарий" class="ws-input ws-input--area" />
            <input
              v-model="form.website"
              type="text"
              tabindex="-1"
              autocomplete="off"
              aria-hidden="true"
              class="ws-form__honeypot"
            />
            <button
              type="submit"
              class="ws-btn ws-btn--primary ws-form__submit"
              :disabled="formSubmitting"
            >
              Отправить заявку
            </button>
            <Transition name="ws-pop">
              <p v-if="formError" class="ws-form__err">{{ formError }}</p>
            </Transition>
          </form>
        </section>

        <footer class="ws-footer ws-reveal">
          iHome24 — оптовый поставщик товаров для дома по всей России.
        </footer>
      </div>
    </div>

    <Transition name="ws-sticky">
      <div v-if="showStickyCta" class="ws-sticky" role="region" aria-label="Быстрая связь">
        <a :href="`tel:${phoneClean}`" class="ws-sticky__call">
          <WholesaleIcon name="phone" />
          <span>Позвонить</span>
        </a>
        <button type="button" class="ws-sticky__form" @click="scrollToForm()">
          Оставить заявку
        </button>
      </div>
    </Transition>

    <Teleport to="body">
      <Transition name="ws-overlay">
        <div
          v-if="formSubmitting || formSent"
          class="ws-overlay"
          role="dialog"
          aria-modal="true"
          :aria-label="formSent ? 'Заявка принята' : 'Отправка заявки'"
          @click.self="formSent && closeSuccessOverlay()"
        >
          <div class="ws-overlay__card">
            <template v-if="formSubmitting && !formSent">
              <div class="ws-overlay__spinner" aria-hidden="true" />
              <p class="ws-overlay__title">Отправляем заявку</p>
              <p class="ws-overlay__text">Подождите несколько секунд…</p>
            </template>
            <template v-else-if="formSent">
              <div class="ws-overlay__success-icon" aria-hidden="true">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                  <circle cx="12" cy="12" r="10" />
                  <path d="M8 12l3 3 5-6" stroke-linecap="round" stroke-linejoin="round" />
                </svg>
              </div>
              <p class="ws-overlay__title">Заявка принята!</p>
              <p class="ws-overlay__text">Менеджер свяжется с вами в ближайшее время.</p>
              <button type="button" class="ws-btn ws-btn--primary ws-overlay__btn" @click="closeSuccessOverlay">
                Хорошо
              </button>
            </template>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'

const phoneClean = '+79809416666'
const showStickyCta = ref(false)

const formContextHints = {
  price: 'Интересуют оптовые условия и скидка по калькулятору.',
  start: 'Хочу стать оптовым клиентом iHome24.',
  partner: 'Интересуют оптовые поставки для моего бизнеса.',
  pricelist: 'Прошу прислать актуальный оптовый прайс с остатками.',
}

const heroStats = [
  { icon: 'tag', value: 'до 15%', label: 'скидка' },
  { icon: 'box', value: '800+', label: 'товаров' },
  { icon: 'truck', value: '1–2 дня', label: 'отгрузка' },
]

const discountRows = [
  { sum: 'до 9 999 ₽', discount: '0%', status: 'Розница' },
  { sum: '10–29 999 ₽', discount: '5%', status: 'Партнёр' },
  { sum: '30–99 999 ₽', discount: '10%', status: 'Бизнес' },
  { sum: 'от 100 000 ₽', discount: '15%', status: 'VIP' },
]

const steps = [
  'Оставьте заявку или позвоните менеджеру.',
  'Укажите ИНН — подготовим договор.',
  'Подпишите договор (ЭДО: Диадок, СБИС, Контур).',
  'Получите доступ к оптовому кабинету.',
  'Первый заказ — отгрузка за 1–2 дня.',
]

const audience = [
  { icon: 'shop', title: 'Магазины', text: 'Прямые поставки, резерв под объём, прайс с остатками.' },
  { icon: 'hotel', title: 'Отели и хостелы', text: 'Текстиль, посуда, гигиена для номерного фонда.' },
  { icon: 'cafe', title: 'Кафе и рестораны', text: 'Утварь, одноразовая посуда, моющие средства.' },
  { icon: 'office', title: 'Офисы', text: 'Хозтовары, канцелярия, снабжение по графику.' },
  { icon: 'reseller', title: 'Перепродавцы и ИП', text: 'Опт для магазинов, маркетплейсов, досок объявлений.' },
  { icon: 'marketplace', title: 'Селлеры WB, Ozon', text: 'Артикулы, фото, штрихкоды, Честный Знак, FBO/FBS.' },
]

const documents = [
  'Договор поставки',
  'Счёт на оплату',
  'УПД',
  'ТОРГ-12',
  'Сертификаты',
]

const delivery = [
  'СДЭК — ПВЗ или курьер',
  'ПЭК — крупногабарит',
  'Деловые линии — паллеты',
  'Самовывоз — склад Москва',
]

const topCategories = [
  {
    icon: 'appliance',
    title: 'Бытовая техника',
    hint: 'Стабильный спрос круглый год',
    hit: true,
  },
  {
    icon: 'kettle',
    title: 'Электрические чайники',
    hint: 'Топ на маркетплейсах',
    hit: true,
  },
  {
    icon: 'kitchenware',
    title: 'Сковороды и кастрюли',
    hint: 'Посуда для дома и HoReCa',
    hit: true,
  },
  {
    icon: 'cctv',
    title: 'Видеонаблюдение',
    hint: 'Wi‑Fi и уличные камеры',
    hit: true,
  },
  {
    icon: 'charge',
    title: 'Power Bank и зарядки',
    hint: 'Быстрый оборот, импульсные покупки',
    hit: false,
  },
  {
    icon: 'headphones',
    title: 'Наушники и гарнитуры',
    hint: 'Популярны у селлеров',
    hit: false,
  },
  {
    icon: 'auto',
    title: 'Автотовары',
    hint: 'Аксессуары, акустика, компрессоры',
    hit: false,
  },
  {
    icon: 'microwave',
    title: 'Микроволновые печи',
    hint: 'Востребованы в рознице',
    hit: false,
  },
  {
    icon: 'blender',
    title: 'Блендеры и миксеры',
    hint: 'Погружные и стационарные',
    hit: false,
  },
  {
    icon: 'humidifier',
    title: 'Увлажнители воздуха',
    hint: 'Сезонный bestseller',
    hit: false,
  },
  {
    icon: 'vacuum',
    title: 'Техника для уборки',
    hint: 'Пылесосы и поломойки',
    hit: false,
  },
  {
    icon: 'beauty',
    title: 'Красота и здоровье',
    hint: 'Стайлеры, массажёры, триммеры',
    hit: false,
  },
]

const benefits = [
  { icon: 'box', title: '800+ в наличии', text: 'Отгрузка день в день, без ожидания под заказ.' },
  { icon: 'tag', title: 'Честные цены', text: 'Без скрытых комиссий, скидка в договоре.' },
  { icon: 'manager', title: 'Личный менеджер', text: 'Один контакт на заказы и документы.' },
  { icon: 'truck', title: 'Гибкая логистика', text: 'Любая ТК, консолидация для регионов.' },
  { icon: 'file', title: 'ЭДО в день отгрузки', text: 'Закрывающие документы с товаром или онлайн.' },
]

const faq = [
  { q: 'Можно ли заказать пробную партию?', a: 'Да, минимальный первый заказ — 10 000 ₽.' },
  { q: 'Работаете с самозанятыми?', a: 'Да, достаточно ИНН и паспорта. Оптовые цены с первого заказа.' },
  { q: 'Есть отсрочка?', a: '14–30 дней после 3 месяцев сотрудничества.' },
  { q: 'Сертификаты на товары?', a: 'Декларации и сертификаты ЕАС по запросу.' },
  { q: 'Как узнать остатки?', a: 'Все товары на сайте — в наличии. Если позиция есть в каталоге, её можно заказать.' },
]

const calcAmount = ref(10000)
const calcResult = computed(() => {
  const n = Number(calcAmount.value)
  if (!Number.isFinite(n) || n < 0) return null
  if (n < 10000) return { discount: '0%', status: 'Розница' }
  if (n < 30000) return { discount: '5%', status: 'Партнёр' }
  if (n < 100000) return { discount: '10%', status: 'Бизнес' }
  return { discount: '15%', status: 'VIP' }
})

const form = ref({ name: '', phone: '', inn: '', message: '', website: '' })
const formSent = ref(false)
const formSubmitting = ref(false)
const formError = ref('')

function closeSuccessOverlay() {
  formSent.value = false
}

watch([formSubmitting, formSent], ([submitting, sent]) => {
  if (!import.meta.client) return
  document.body.style.overflow = submitting || sent ? 'hidden' : ''
})

async function submitForm() {
  if (formSubmitting.value) return
  formError.value = ''
  formSent.value = false
  formSubmitting.value = true

  try {
    const { wholesaleApi } = useLegacyApi()
    const { data } = await wholesaleApi.submitLead({
      name: form.value.name.trim(),
      phone: form.value.phone.trim(),
      inn: form.value.inn.trim() || undefined,
      message: form.value.message.trim() || undefined,
      website: form.value.website.trim() || undefined,
    })

    if (!data?.success) {
      throw new Error(data?.message || 'submit failed')
    }

    formSent.value = true
    form.value = { name: '', phone: '', inn: '', message: '', website: '' }
  } catch (err) {
    formError.value = err?.response?.data?.message
      || 'Не удалось отправить заявку. Позвоните +7 (980) 941-66-66 или напишите на opt@ihome24.ru'
  } finally {
    formSubmitting.value = false
  }
}

function scrollToForm(context = '') {
  if (context && formContextHints[context] && !form.value.message) {
    form.value.message = formContextHints[context]
  }
  const el = document.getElementById('wholesale-contact')
  if (!el) return
  el.scrollIntoView({ behavior: 'smooth', block: 'start' })
  window.setTimeout(() => {
    el.querySelector('input, textarea')?.focus()
  }, 400)
}

let onScrollSticky = null

onMounted(() => {
  if (import.meta.server) return
  const els = document.querySelectorAll('.ws-reveal')

  if (els.length) {
    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
      els.forEach(el => el.classList.add('is-visible'))
    } else {
      const obs = new IntersectionObserver(
        (entries) => {
          entries.forEach((e) => {
            if (e.isIntersecting) {
              e.target.classList.add('is-visible')
              obs.unobserve(e.target)
            }
          })
        },
        { threshold: 0.06, rootMargin: '0px 0px -40px 0px' },
      )
      els.forEach(el => obs.observe(el))
    }
  }

  const hero = document.querySelector('.ws-hero')
  const contact = document.getElementById('wholesale-contact')

  onScrollSticky = () => {
    const heroBottom = hero?.getBoundingClientRect().bottom ?? 0
    const contactTop = contact?.getBoundingClientRect().top ?? 9999
    showStickyCta.value = heroBottom < 0 && contactTop > window.innerHeight * 0.4
  }

  window.addEventListener('scroll', onScrollSticky, { passive: true })
  onScrollSticky()
})

onUnmounted(() => {
  if (onScrollSticky) {
    window.removeEventListener('scroll', onScrollSticky)
  }
  if (import.meta.client) {
    document.body.style.overflow = ''
  }
})
</script>

<style scoped>
.ws-page {
  position: relative;
  overflow: hidden;
  padding: 16px 0 88px;
  color: rgba(255, 255, 255, 0.92);
  font-family: helvetica, sans-serif;
}

@media (min-width: 769px) {
  .ws-page {
    padding-bottom: 32px;
  }
}

@media (min-width: 768px) {
  .ws-page {
    padding: 24px 0 48px;
  }
}

.ws-page__bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 0;
}

.ws-page__orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  animation: ws-orb-float 12s ease-in-out infinite;
}

.ws-page__orb--1 {
  width: 380px;
  height: 380px;
  top: -100px;
  right: -60px;
  background: rgba(244, 116, 39, 0.32);
}

.ws-page__orb--2 {
  width: 300px;
  height: 300px;
  bottom: 10%;
  left: -80px;
  background: rgba(244, 116, 39, 0.18);
  animation-delay: -4s;
}

.ws-page__orb--3 {
  width: 180px;
  height: 180px;
  top: 45%;
  left: 55%;
  background: rgba(255, 255, 255, 0.04);
  animation-delay: -8s;
}

@keyframes ws-orb-float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(-12px, 16px) scale(1.05); }
}

.ws-page__inner {
  position: relative;
  z-index: 1;
  max-width: 720px;
}

/* Reveal */
.ws-reveal {
  opacity: 0;
  transform: translateY(18px);
  transition: opacity 0.55s ease, transform 0.55s ease;
}

.ws-reveal.is-visible {
  opacity: 1;
  transform: translateY(0);
}

/* Hero */
.ws-hero {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  margin-bottom: 28px;
}

.ws-back {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  align-self: flex-start;
  color: rgba(255, 255, 255, 0.55);
  font-size: 13px;
  margin-bottom: 16px;
  transition: color 0.2s;
}

.ws-back svg {
  width: 16px;
  height: 16px;
}

.ws-back:hover {
  color: #f47427;
}

.ws-badge {
  display: inline-block;
  padding: 5px 12px;
  border-radius: 999px;
  background: rgba(244, 116, 39, 0.15);
  color: #f47427;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  margin-bottom: 12px;
}

.ws-badge--pulse {
  animation: ws-badge-pulse 3s ease-in-out infinite;
}

@keyframes ws-badge-pulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(244, 116, 39, 0); }
  50% { box-shadow: 0 0 0 6px rgba(244, 116, 39, 0.12); }
}

.ws-h1 {
  font-size: clamp(1.4rem, 5vw, 2rem);
  font-weight: 700;
  color: #fff;
  line-height: 1.2;
  margin: 0 0 12px;
  font-family: bork, sans-serif;
}

.ws-lead {
  font-size: 14px;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.72);
  margin: 0 auto 20px;
  max-width: 480px;
}

.ws-hero-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  margin-bottom: 20px;
}

.ws-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: 10px 6px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.08);
  transition: transform 0.2s, border-color 0.2s;
}

.ws-stat:hover {
  transform: translateY(-2px);
  border-color: rgba(244, 116, 39, 0.3);
}

.ws-stat__icon {
  display: flex;
  color: #f47427;
  width: 20px;
  height: 20px;
}

.ws-stat__icon :deep(svg) {
  width: 100%;
  height: 100%;
}

.ws-stat__val {
  font-size: 15px;
  font-weight: 700;
  color: #fff;
  line-height: 1.2;
}

.ws-stat__label {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.5);
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.ws-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.ws-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  min-height: 44px;
  padding: 10px 12px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 600;
  text-decoration: none;
  border: none;
  cursor: pointer;
  transition: opacity 0.2s, transform 0.15s, box-shadow 0.2s;
  white-space: nowrap;
}

.ws-btn :deep(svg) {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}

.ws-btn:hover {
  transform: translateY(-1px);
}

.ws-btn--primary {
  background: #f47427;
  color: #fff;
  box-shadow: 0 4px 16px rgba(244, 116, 39, 0.3);
}

.ws-btn--secondary {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.15);
}

.ws-btn--sm {
  min-height: 40px;
  padding: 8px 14px;
  font-size: 12px;
}

@media (min-width: 480px) {
  .ws-actions {
    display: flex;
    justify-content: center;
    gap: 10px;
  }

  .ws-btn {
    width: auto;
    padding: 12px 22px;
    border-radius: 999px;
    font-size: 14px;
  }
}

/* Blocks */
.ws-content {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.ws-block {
  padding: 16px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(8px);
}

@media (min-width: 640px) {
  .ws-block {
    padding: 20px 22px;
  }
}

.ws-block--contact {
  border-color: rgba(244, 116, 39, 0.25);
  background: rgba(244, 116, 39, 0.06);
}

.ws-block__head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.ws-block__icon {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: rgba(244, 116, 39, 0.14);
  color: #f47427;
}

.ws-block__icon :deep(svg) {
  width: 18px;
  height: 18px;
}

.ws-h2 {
  margin: 0;
  font-size: 1.05rem;
  font-weight: 700;
  color: #fff;
  line-height: 1.3;
}

.ws-h3 {
  margin: 0 0 4px;
  font-size: 13px;
  font-weight: 600;
  color: #fff;
}

.ws-text {
  margin: 0 0 12px;
  font-size: 14px;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.78);
}

.ws-text--sm {
  font-size: 13px;
  margin-bottom: 10px;
}

/* Tiers */
.ws-tiers {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
  margin-bottom: 12px;
}

@media (min-width: 640px) {
  .ws-tiers {
    grid-template-columns: repeat(4, 1fr);
  }
}

.ws-tier {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: 12px 8px;
  border-radius: 12px;
  background: rgba(0, 0, 0, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.06);
  text-align: center;
  animation: ws-tier-in 0.4s ease both;
  transition: transform 0.2s, border-color 0.2s;
}

.ws-tier:hover {
  transform: scale(1.02);
  border-color: rgba(244, 116, 39, 0.35);
}

.ws-tier--vip {
  border-color: rgba(244, 116, 39, 0.35);
  background: rgba(244, 116, 39, 0.1);
}

@keyframes ws-tier-in {
  from { opacity: 0; transform: scale(0.95); }
  to { opacity: 1; transform: scale(1); }
}

.ws-tier__discount {
  font-size: 1.25rem;
  font-weight: 800;
  color: #f47427;
  line-height: 1;
}

.ws-tier__sum {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.55);
  line-height: 1.3;
}

.ws-tier__status {
  font-size: 11px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.85);
}

/* Calc */
.ws-calc {
  margin-top: 4px;
  padding: 14px;
  border-radius: 12px;
  background: rgba(0, 0, 0, 0.12);
  border: 1px solid rgba(244, 116, 39, 0.2);
}

.ws-calc__head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.ws-calc__icon {
  display: flex;
  color: #f47427;
  width: 18px;
  height: 18px;
}

.ws-calc__icon :deep(svg) {
  width: 100%;
  height: 100%;
}

.ws-calc__label {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
}

.ws-calc__result {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  margin-top: 10px;
}

.ws-calc__badge {
  padding: 6px 12px;
  border-radius: 8px;
  background: #f47427;
  color: #fff;
  font-weight: 700;
  font-size: 14px;
}

.ws-calc__status {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.85);
}

.ws-pop-enter-active {
  animation: ws-pop 0.35s ease;
}

@keyframes ws-pop {
  from { opacity: 0; transform: scale(0.92); }
  to { opacity: 1; transform: scale(1); }
}

/* Steps */
.ws-steps {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.ws-step {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  font-size: 13px;
  line-height: 1.5;
  color: rgba(255, 255, 255, 0.85);
}

.ws-step__num {
  flex-shrink: 0;
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: #f47427;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 12px;
}

/* Grid cards */
.ws-grid {
  display: grid;
  gap: 8px;
}

@media (min-width: 640px) {
  .ws-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }
}

.ws-card {
  padding: 12px;
  border-radius: 12px;
  background: rgba(0, 0, 0, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.06);
  transition: border-color 0.2s, transform 0.2s;
}

.ws-card:hover {
  border-color: rgba(244, 116, 39, 0.3);
  transform: translateY(-2px);
}

.ws-card__icon {
  display: flex;
  width: 28px;
  height: 28px;
  margin-bottom: 8px;
  padding: 5px;
  border-radius: 8px;
  background: rgba(244, 116, 39, 0.12);
  color: #f47427;
}

.ws-card__icon :deep(svg) {
  width: 100%;
  height: 100%;
}

.ws-card .ws-text {
  margin-bottom: 0;
}

/* Chips */
.ws-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 12px;
}

.ws-chip {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 6px 10px;
  border-radius: 8px;
  font-size: 11px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.85);
}

.ws-chip :deep(svg) {
  width: 12px;
  height: 12px;
  color: #f47427;
  flex-shrink: 0;
}

/* List */
.ws-list {
  list-style: none;
  padding: 0;
  margin: 0 0 10px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.ws-list li {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 13px;
  line-height: 1.45;
  color: rgba(255, 255, 255, 0.85);
}

.ws-list__icon {
  flex-shrink: 0;
  display: flex;
  width: 22px;
  height: 22px;
  padding: 3px;
  border-radius: 6px;
  background: rgba(244, 116, 39, 0.12);
  color: #f47427;
}

.ws-list__icon :deep(svg) {
  width: 100%;
  height: 100%;
}

/* Top categories */
.ws-cats {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
  margin-bottom: 12px;
}

@media (min-width: 640px) {
  .ws-cats {
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 10px;
  }
}

.ws-cat {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
  padding: 12px 10px;
  border-radius: 12px;
  background: rgba(0, 0, 0, 0.14);
  border: 1px solid rgba(255, 255, 255, 0.07);
  transition: border-color 0.2s, transform 0.2s, background 0.2s;
  animation: ws-tier-in 0.45s ease both;
  min-width: 0;
}

.ws-cat:hover {
  border-color: rgba(244, 116, 39, 0.35);
  background: rgba(244, 116, 39, 0.08);
  transform: translateY(-2px);
}

.ws-cat--hit {
  border-color: rgba(244, 116, 39, 0.22);
}

.ws-cat__hit {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 9px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  background: #f47427;
  color: #fff;
  line-height: 1.3;
}

.ws-cat__icon {
  display: flex;
  width: 32px;
  height: 32px;
  padding: 6px;
  border-radius: 10px;
  background: rgba(244, 116, 39, 0.14);
  color: #f47427;
  flex-shrink: 0;
}

.ws-cat__icon :deep(svg) {
  width: 100%;
  height: 100%;
}

.ws-cat__title {
  margin: 0;
  font-size: 12px;
  font-weight: 600;
  color: #fff;
  line-height: 1.3;
  padding-right: 28px;
}

@media (min-width: 480px) {
  .ws-cat__title {
    font-size: 13px;
    padding-right: 0;
  }
}

.ws-cat__hint {
  margin: 0;
  font-size: 10px;
  line-height: 1.35;
  color: rgba(255, 255, 255, 0.5);
  display: none;
}

@media (min-width: 480px) {
  .ws-cat__hint {
    display: block;
    font-size: 11px;
  }
}

.ws-cats-foot {
  margin-bottom: 0;
}

.ws-cats-link {
  display: inline-block;
  margin-left: 4px;
  color: #f47427;
  font-weight: 600;
  text-decoration: none;
  transition: color 0.2s;
}

.ws-cats-link:hover {
  color: #ff8a4c;
}

/* Benefits */
.ws-benefits {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.ws-benefit {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  padding: 10px;
  border-radius: 10px;
  background: rgba(0, 0, 0, 0.1);
  transition: transform 0.2s;
}

.ws-benefit:hover {
  transform: translateX(4px);
}

.ws-benefit__icon {
  flex-shrink: 0;
  display: flex;
  width: 32px;
  height: 32px;
  padding: 6px;
  border-radius: 10px;
  background: rgba(244, 116, 39, 0.14);
  color: #f47427;
}

.ws-benefit__icon :deep(svg) {
  width: 100%;
  height: 100%;
}

.ws-benefit .ws-text {
  margin-bottom: 0;
}

/* FAQ */
.ws-faq {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.ws-faq__item {
  border-radius: 10px;
  background: rgba(0, 0, 0, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.07);
  overflow: hidden;
  transition: border-color 0.2s;
}

.ws-faq__item[open] {
  border-color: rgba(244, 116, 39, 0.3);
}

.ws-faq__q {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 12px 14px;
  cursor: pointer;
  font-weight: 600;
  font-size: 13px;
  color: #fff;
  list-style: none;
}

.ws-faq__q::-webkit-details-marker {
  display: none;
}

.ws-faq__chevron {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
  color: #f47427;
  transition: transform 0.25s ease;
}

.ws-faq__item[open] .ws-faq__chevron {
  transform: rotate(180deg);
}

.ws-faq__a {
  padding: 0 14px 12px;
  margin: 0;
  font-size: 13px;
  line-height: 1.55;
  color: rgba(255, 255, 255, 0.72);
}

/* Contacts */
.ws-contacts {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  margin-bottom: 16px;
}

.ws-contact {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 10px 6px;
  border-radius: 12px;
  background: rgba(0, 0, 0, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.08);
  text-decoration: none;
  color: inherit;
  text-align: center;
  transition: border-color 0.2s, transform 0.2s;
}

.ws-contact:hover {
  border-color: #f47427;
  transform: translateY(-2px);
}

.ws-contact--static {
  cursor: default;
}

.ws-contact--static .ws-contact__val--hours {
  white-space: nowrap;
  word-break: normal;
}

.ws-contact__icon {
  display: flex;
  width: 28px;
  height: 28px;
  padding: 5px;
  border-radius: 8px;
  background: rgba(244, 116, 39, 0.15);
  color: #f47427;
}

.ws-contact__icon :deep(svg) {
  width: 100%;
  height: 100%;
}

.ws-contact__label {
  font-size: 9px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: rgba(255, 255, 255, 0.45);
}

.ws-contact__val {
  font-size: 11px;
  font-weight: 600;
  color: #fff;
  line-height: 1.25;
  word-break: break-word;
}

@media (min-width: 480px) {
  .ws-contact__val {
    font-size: 13px;
  }
}

/* Form */
.ws-form {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.ws-form__row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.ws-input {
  width: 100%;
  padding: 11px 14px;
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  background: rgba(255, 255, 255, 0.06);
  color: #fff;
  font-size: 14px;
  font-family: inherit;
  transition: border-color 0.2s;
}

.ws-input::placeholder {
  color: rgba(255, 255, 255, 0.35);
}

.ws-input:focus {
  outline: none;
  border-color: #f47427;
}

.ws-input--area {
  resize: vertical;
  min-height: 64px;
}

.ws-form__submit {
  width: 100%;
  margin-top: 4px;
}

@media (min-width: 480px) {
  .ws-form__submit {
    width: auto;
    align-self: flex-start;
  }
}

.ws-form__err {
  margin: 0;
  font-size: 13px;
  line-height: 1.45;
  color: #ff9b7a;
}

.ws-form__honeypot {
  position: absolute;
  left: -9999px;
  width: 1px;
  height: 1px;
  opacity: 0;
  pointer-events: none;
}

.ws-form__submit:disabled {
  opacity: 0.7;
  cursor: wait;
}

/* Fullscreen overlay — loading & success */
.ws-overlay {
  position: fixed;
  inset: 0;
  z-index: 10001;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: rgba(46, 40, 38, 0.82);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.ws-overlay__card {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: min(340px, 100%);
  padding: 32px 24px 28px;
  border-radius: 20px;
  text-align: center;
  background: linear-gradient(160deg, #3a3331 0%, #2e2826 100%);
  border: 1px solid rgba(255, 255, 255, 0.12);
  box-shadow: 0 24px 64px rgba(0, 0, 0, 0.45), 0 0 0 1px rgba(244, 116, 39, 0.08);
}

.ws-overlay__spinner {
  width: 52px;
  height: 52px;
  margin-bottom: 20px;
  border: 4px solid rgba(244, 116, 39, 0.2);
  border-top: 4px solid #f47427;
  border-radius: 50%;
  animation: ws-overlay-spin 0.75s linear infinite;
  will-change: transform;
}

@keyframes ws-overlay-spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.ws-overlay__success-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  margin-bottom: 16px;
  border-radius: 50%;
  background: rgba(125, 216, 125, 0.15);
  color: #7dd87d;
  animation: ws-overlay-check 0.5s cubic-bezier(0.34, 1.4, 0.64, 1);
}

.ws-overlay__success-icon svg {
  width: 32px;
  height: 32px;
}

.ws-overlay__success-icon svg circle {
  opacity: 0.35;
}

@keyframes ws-overlay-check {
  0% { opacity: 0; transform: scale(0.4); }
  100% { opacity: 1; transform: scale(1); }
}

.ws-overlay__title {
  margin: 0 0 8px;
  font-size: clamp(1.1rem, 4vw, 1.35rem);
  font-weight: 700;
  color: #fff;
  font-family: bork, sans-serif;
}

.ws-overlay__text {
  margin: 0;
  font-size: 14px;
  line-height: 1.5;
  color: rgba(255, 255, 255, 0.72);
  max-width: 260px;
}

.ws-overlay__btn {
  margin-top: 20px;
  min-width: 140px;
  border-radius: 999px;
}

.ws-overlay-enter-active,
.ws-overlay-leave-active {
  transition: opacity 0.28s ease;
}

.ws-overlay-enter-active .ws-overlay__card,
.ws-overlay-leave-active .ws-overlay__card {
  transition: transform 0.28s ease, opacity 0.28s ease;
}

.ws-overlay-enter-from,
.ws-overlay-leave-to {
  opacity: 0;
}

.ws-overlay-enter-from .ws-overlay__card,
.ws-overlay-leave-to .ws-overlay__card {
  opacity: 0;
  transform: scale(0.92) translateY(12px);
}

.ws-footer {
  text-align: center;
  padding: 16px 0 0;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.45);
}

/* Contextual CTAs — не в каждом блоке, только после «горячих» секций */
.ws-cta {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 10px;
  margin-top: 16px;
  padding: 14px;
  border-radius: 12px;
  background: rgba(244, 116, 39, 0.08);
  border: 1px solid rgba(244, 116, 39, 0.2);
}

@media (min-width: 480px) {
  .ws-cta {
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
  }
}

.ws-cta__text {
  margin: 0;
  font-size: 13px;
  line-height: 1.45;
  color: rgba(255, 255, 255, 0.78);
}

.ws-cta-link {
  display: inline-block;
  margin-top: 12px;
  padding: 0;
  border: none;
  background: none;
  color: #f47427;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  text-align: left;
  transition: opacity 0.2s;
}

.ws-cta-link:hover {
  opacity: 0.85;
}

.ws-cta-banner {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin: 8px 0 20px;
  padding: 16px;
  border-radius: 14px;
  background: linear-gradient(135deg, rgba(244, 116, 39, 0.18), rgba(244, 116, 39, 0.06));
  border: 1px solid rgba(244, 116, 39, 0.35);
}

@media (min-width: 480px) {
  .ws-cta-banner {
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
  }
}

.ws-cta-banner__title {
  margin: 0 0 4px;
  font-size: 15px;
  font-weight: 700;
  color: #fff;
}

.ws-cta-banner__sub {
  margin: 0;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
}

.ws-cta-banner__actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  flex-shrink: 0;
}

@media (min-width: 480px) {
  .ws-cta-banner__actions {
    width: auto;
  }
}

/* Липкая панель — одна на всю страницу */
.ws-sticky {
  position: fixed;
  left: 12px;
  right: 12px;
  bottom: 72px;
  z-index: 9998;
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 8px;
  padding: 8px;
  border-radius: 14px;
  background: rgba(46, 40, 38, 0.92);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.12);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.35);
}

@media (min-width: 769px) {
  .ws-sticky {
    left: 50%;
    right: auto;
    bottom: 20px;
    width: min(420px, calc(100% - 32px));
    transform: translateX(-50%);
  }
}

.ws-sticky__call {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  min-height: 44px;
  padding: 0 14px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  text-decoration: none;
  white-space: nowrap;
  transition: background 0.2s;
}

.ws-sticky__call :deep(svg) {
  width: 16px;
  height: 16px;
  color: #f47427;
}

.ws-sticky__call:hover {
  background: rgba(255, 255, 255, 0.14);
}

.ws-sticky__form {
  min-height: 44px;
  padding: 0 14px;
  border: none;
  border-radius: 10px;
  background: #f47427;
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.2s, transform 0.15s;
}

.ws-sticky__form:hover {
  opacity: 0.92;
  transform: translateY(-1px);
}

.ws-sticky-enter-active,
.ws-sticky-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.ws-sticky-enter-from,
.ws-sticky-leave-to {
  opacity: 0;
  transform: translateY(12px);
}

@media (min-width: 769px) {
  .ws-sticky-enter-from,
  .ws-sticky-leave-to {
    transform: translateX(-50%) translateY(12px);
  }

  .ws-sticky-enter-active,
  .ws-sticky-leave-active {
    transition: opacity 0.25s ease, transform 0.25s ease;
  }
}

@media (prefers-reduced-motion: reduce) {
  .ws-page__orb,
  .ws-badge--pulse,
  .ws-tier,
  .ws-reveal {
    animation: none !important;
    transition: none !important;
  }

  .ws-reveal {
    opacity: 1;
    transform: none;
  }
}
</style>
