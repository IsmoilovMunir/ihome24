<script setup>
defineProps({
  /** При true — clearError перед переходом на главную (error.vue) */
  fromErrorBoundary: { type: Boolean, default: false },
})

const quickLinks = [
  {
    to: '/products',
    label: 'Каталог',
    shortLabel: 'Каталог',
    desc: '800+ товаров',
    icon: 'catalog',
  },
  {
    to: '/optovym-klientam',
    label: 'Оптовым клиентам',
    shortLabel: 'Оптом',
    desc: 'Скидки до 20%',
    icon: 'wholesale',
  },
  {
    to: '/support/contacts',
    label: 'Контакты',
    shortLabel: 'Контакты',
    desc: 'Поможем найти',
    icon: 'contacts',
  },
]

function goHome(event) {
  if (import.meta.client && typeof clearError === 'function') {
    event?.preventDefault?.()
    clearError({ redirect: '/' })
  }
}
</script>

<template>
  <div class="not-found">
    <div class="not-found__bg" aria-hidden="true">
      <span class="not-found__orb not-found__orb--1" />
      <span class="not-found__orb not-found__orb--2" />
      <span class="not-found__orb not-found__orb--3" />
    </div>

    <div class="not-found__inner">
      <div class="not-found__card">
        <div class="not-found__brand">
          <NuxtLink
            to="/"
            class="not-found__logo"
            aria-label="iHome24 — на главную"
            @click="fromErrorBoundary ? goHome($event) : undefined"
          >
            <img
              src="/photos/logo.svg"
              alt="iHome24"
              class="not-found__logo-img"
              width="140"
              height="47"
            />
          </NuxtLink>
        </div>

        <p class="not-found__code" aria-hidden="true">404</p>

        <span class="not-found__badge">Страница не найдена</span>

        <h1 class="not-found__title">
          Здесь ничего нет
        </h1>
        <p class="not-found__text">
          Возможно, ссылка устарела или страница была удалена.
          Перейдите в каталог или на главную — там точно найдёте нужное.
        </p>

        <div class="not-found__actions">
          <NuxtLink
            to="/"
            class="not-found__btn not-found__btn--primary"
            @click="fromErrorBoundary ? goHome($event) : undefined"
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
              <path d="M3 9.5L12 3l9 6.5V20a1 1 0 01-1 1h-5v-7H9v7H4a1 1 0 01-1-1V9.5z" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
            На главную
          </NuxtLink>
          <NuxtLink to="/products" class="not-found__btn not-found__btn--secondary">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
              <path d="M4 6h16M4 12h16M4 18h10" stroke-linecap="round" />
            </svg>
            Каталог
          </NuxtLink>
        </div>
      </div>

      <nav class="not-found__links" aria-label="Полезные разделы">
        <NuxtLink
          v-for="link in quickLinks"
          :key="link.to"
          :to="link.to"
          class="not-found__link-card"
        >
          <span class="not-found__link-icon" aria-hidden="true">
            <!-- Каталог -->
            <svg v-if="link.icon === 'catalog'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75">
              <rect x="3" y="4" width="18" height="16" rx="2.5" />
              <path d="M7 9h10M7 13h10M7 17h6" stroke-linecap="round" />
            </svg>
            <!-- Опт -->
            <svg v-else-if="link.icon === 'wholesale'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75">
              <path d="M3 9l9-5 9 5v8l-9 5-9-5V9z" stroke-linejoin="round" />
              <path d="M12 14v5M7.5 9.5L12 12l4.5-2.5" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
            <!-- Контакты -->
            <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75">
              <path d="M4.5 6h15a1.5 1.5 0 011.5 1.5v9A1.5 1.5 0 0119.5 18h-15A1.5 1.5 0 013 16.5v-9A1.5 1.5 0 014.5 6z" stroke-linejoin="round" />
              <path d="M6 9l6 4 6-4" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
          </span>
          <span class="not-found__link-body">
            <span class="not-found__link-label not-found__link-label--full">{{ link.label }}</span>
            <span class="not-found__link-label not-found__link-label--short">{{ link.shortLabel }}</span>
            <span class="not-found__link-desc">{{ link.desc }}</span>
          </span>
        </NuxtLink>
      </nav>

      <p class="not-found__help">
        Нужна помощь?
        <a href="tel:+79809416666" class="not-found__phone">+7 (980) 941-66-66</a>
      </p>
    </div>
  </div>
</template>

<style scoped>
.not-found {
  position: relative;
  min-height: 100vh;
  min-height: 100dvh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px 16px 48px;
  background: #3a3331;
  overflow: hidden;
}

.not-found__bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.not-found__orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.45;
}

.not-found__orb--1 {
  width: 420px;
  height: 420px;
  top: -120px;
  right: -80px;
  background: rgba(244, 116, 39, 0.35);
}

.not-found__orb--2 {
  width: 320px;
  height: 320px;
  bottom: -60px;
  left: -100px;
  background: rgba(244, 116, 39, 0.2);
}

.not-found__orb--3 {
  width: 200px;
  height: 200px;
  top: 40%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(255, 255, 255, 0.04);
  opacity: 1;
}

.not-found__inner {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 560px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.not-found__card {
  width: 100%;
  text-align: center;
  padding: 0;
}

.not-found__brand {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

.not-found__logo {
  display: inline-flex;
  line-height: 0;
  opacity: 0.95;
  transition: opacity 0.2s;
}

.not-found__logo:hover {
  opacity: 1;
}

.not-found__logo-img {
  display: block;
  height: 36px;
  width: auto;
  max-width: min(52vw, 160px);
}

@media (min-width: 640px) {
  .not-found__logo-img {
    height: 42px;
    max-width: 180px;
  }
}

.not-found__code {
  margin: 0 0 8px;
  font-size: clamp(4.5rem, 18vw, 6.5rem);
  font-weight: 800;
  line-height: 1;
  letter-spacing: -0.04em;
  background: linear-gradient(135deg, #fff 0%, rgba(255, 255, 255, 0.45) 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  font-family: 'bork', sans-serif;
  user-select: none;
}

.not-found__badge {
  display: inline-block;
  margin-bottom: 12px;
  padding: 6px 14px;
  border-radius: 999px;
  background: rgba(244, 116, 39, 0.15);
  color: #f47427;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.not-found__title {
  margin: 0 0 12px;
  font-size: clamp(1.35rem, 4vw, 1.75rem);
  font-weight: 700;
  color: #fff;
  line-height: 1.25;
  font-family: 'bork', sans-serif;
}

.not-found__text {
  margin: 0 0 20px;
  font-size: 14px;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.72);
  max-width: 400px;
  margin-left: auto;
  margin-right: auto;
}

@media (min-width: 480px) {
  .not-found__text {
    margin-bottom: 28px;
    font-size: 15px;
    line-height: 1.65;
  }
}

.not-found__actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  width: 100%;
}

.not-found__btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  width: 100%;
  min-height: 44px;
  padding: 11px 10px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 600;
  text-decoration: none;
  transition: opacity 0.2s, transform 0.15s, box-shadow 0.2s;
  border: none;
  cursor: pointer;
  white-space: nowrap;
}

.not-found__btn svg {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}

@media (min-width: 480px) {
  .not-found__actions {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    gap: 12px;
    width: auto;
  }

  .not-found__btn {
    width: auto;
    min-height: auto;
    padding: 14px 24px;
    border-radius: 999px;
    font-size: 15px;
    gap: 8px;
  }

  .not-found__btn svg {
    width: 18px;
    height: 18px;
  }
}

.not-found__btn:hover {
  opacity: 0.92;
  transform: translateY(-1px);
}

.not-found__btn--primary {
  background: #f47427;
  color: #fff;
  box-shadow: 0 4px 20px rgba(244, 116, 39, 0.35);
}

.not-found__btn--primary:hover {
  box-shadow: 0 6px 24px rgba(244, 116, 39, 0.45);
}

.not-found__btn--secondary {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.18);
}

.not-found__links {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
  width: 100%;
  margin-top: 16px;
}

.not-found__link-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 10px 6px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.08);
  text-decoration: none;
  text-align: center;
  transition: border-color 0.2s, background 0.2s, transform 0.15s;
  min-width: 0;
}

.not-found__link-card:hover {
  border-color: rgba(244, 116, 39, 0.4);
  background: rgba(244, 116, 39, 0.1);
  transform: translateY(-1px);
}

.not-found__link-icon {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: rgba(244, 116, 39, 0.14);
  color: #f47427;
  transition: background 0.2s, color 0.2s;
}

.not-found__link-icon svg {
  width: 18px;
  height: 18px;
  display: block;
}

.not-found__link-card:hover .not-found__link-icon {
  background: rgba(244, 116, 39, 0.28);
  color: #ff8a4c;
}

.not-found__link-body {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
  width: 100%;
}

.not-found__link-label {
  font-size: 11px;
  font-weight: 600;
  color: #fff;
  line-height: 1.25;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.not-found__link-label--full {
  display: none;
}

.not-found__link-label--short {
  display: block;
}

.not-found__link-desc {
  display: none;
  font-size: 11px;
  line-height: 1.3;
  color: rgba(255, 255, 255, 0.5);
}

@media (min-width: 480px) {
  .not-found__links {
    gap: 10px;
    margin-top: 20px;
  }

  .not-found__link-card {
    flex-direction: row;
    align-items: center;
    gap: 10px;
    padding: 10px 12px;
    text-align: left;
  }

  .not-found__link-icon {
    width: 38px;
    height: 38px;
  }

  .not-found__link-icon svg {
    width: 19px;
    height: 19px;
  }

  .not-found__link-label {
    font-size: 13px;
  }

  .not-found__link-label--full {
    display: block;
  }

  .not-found__link-label--short {
    display: none;
  }

  .not-found__link-desc {
    display: block;
  }
}

@media (min-width: 640px) {
  .not-found__links {
    grid-template-columns: repeat(3, 1fr);
    gap: 12px;
  }

  .not-found__link-card {
    flex-direction: column;
    align-items: center;
    padding: 14px 10px;
    text-align: center;
  }

  .not-found__link-icon {
    width: 42px;
    height: 42px;
    border-radius: 12px;
  }

  .not-found__link-icon svg {
    width: 20px;
    height: 20px;
  }

  .not-found__link-label {
    font-size: 14px;
  }

  .not-found__link-desc {
    font-size: 12px;
  }
}

.not-found__help {
  margin: 16px 0 0;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.5);
}

.not-found__phone {
  color: #f47427;
  font-weight: 600;
  text-decoration: none;
  margin-left: 4px;
  transition: color 0.2s;
}

.not-found__phone:hover {
  color: #ff8a4c;
}
</style>
