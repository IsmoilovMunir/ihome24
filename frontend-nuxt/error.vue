<script setup>
const error = useError()

const statusCode = computed(() => error.value?.statusCode || 500)

const title = computed(() => {
  if (statusCode.value >= 500) return 'Ошибка сервера'
  return 'Ошибка'
})

const description = computed(() => {
  if (statusCode.value >= 500) {
    return 'Что-то пошло не так на нашей стороне. Попробуйте обновить страницу или вернитесь позже.'
  }
  return error.value?.message || 'Что-то пошло не так.'
})

useSeoMeta({
  title: computed(() => `${statusCode.value} — ${title.value} | iHome24`),
  description,
  robots: 'noindex, nofollow',
})

function retry() {
  clearError({ redirect: '/' })
  if (import.meta.client) {
    window.location.reload()
  }
}
</script>

<template>
  <PagesNotFound v-if="statusCode === 404" from-error-boundary />

  <div v-else class="server-error">
    <div class="server-error__bg" aria-hidden="true">
      <span class="server-error__orb server-error__orb--1" />
      <span class="server-error__orb server-error__orb--2" />
    </div>

    <div class="server-error__inner">
      <div class="server-error__card">
        <div class="server-error__brand">
          <NuxtLink to="/" class="server-error__logo" aria-label="iHome24 — на главную">
            <img
              src="/photos/logo.svg"
              alt="iHome24"
              class="server-error__logo-img"
              width="140"
              height="47"
            />
          </NuxtLink>
        </div>

        <p class="server-error__code" aria-hidden="true">{{ statusCode }}</p>
        <span class="server-error__badge">{{ statusCode >= 500 ? 'Серверная ошибка' : 'Ошибка' }}</span>
        <h1 class="server-error__title">{{ title }}</h1>
        <p class="server-error__text">{{ description }}</p>

        <div class="server-error__actions">
          <NuxtLink
            to="/"
            class="server-error__btn server-error__btn--primary"
            @click.prevent="clearError({ redirect: '/' })"
          >
            На главную
          </NuxtLink>
          <button
            v-if="statusCode >= 500"
            type="button"
            class="server-error__btn server-error__btn--secondary"
            @click="retry"
          >
            Обновить страницу
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.server-error {
  position: relative;
  min-height: 100vh;
  min-height: 100dvh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px 16px;
  background: #3a3331;
  overflow: hidden;
}

.server-error__bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.server-error__orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
}

.server-error__orb--1 {
  width: 360px;
  height: 360px;
  top: -80px;
  left: -60px;
  background: rgba(244, 116, 39, 0.25);
}

.server-error__orb--2 {
  width: 280px;
  height: 280px;
  bottom: -40px;
  right: -60px;
  background: rgba(255, 255, 255, 0.05);
}

.server-error__inner {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 480px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.server-error__logo {
  display: inline-flex;
  line-height: 0;
}

.server-error__logo-img {
  display: block;
  height: 36px;
  width: auto;
  max-width: min(52vw, 160px);
}

.server-error__brand {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

.server-error__card {
  width: 100%;
  text-align: center;
  padding: 40px 28px;
  border-radius: 16px;
  background: rgba(46, 40, 38, 0.92);
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.35);
}

.server-error__code {
  margin: 0 0 8px;
  font-size: clamp(3.5rem, 14vw, 5rem);
  font-weight: 800;
  line-height: 1;
  color: rgba(255, 255, 255, 0.2);
  font-family: 'bork', sans-serif;
}

.server-error__badge {
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

.server-error__title {
  margin: 0 0 12px;
  font-size: 1.5rem;
  font-weight: 700;
  color: #fff;
  font-family: 'bork', sans-serif;
}

.server-error__text {
  margin: 0 0 28px;
  font-size: 15px;
  line-height: 1.65;
  color: rgba(255, 255, 255, 0.72);
}

.server-error__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  justify-content: center;
}

.server-error__btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 14px 28px;
  border-radius: 999px;
  font-size: 15px;
  font-weight: 600;
  text-decoration: none;
  transition: opacity 0.2s, transform 0.15s;
  cursor: pointer;
  border: none;
}

.server-error__btn:hover {
  opacity: 0.92;
  transform: translateY(-1px);
}

.server-error__btn--primary {
  background: #f47427;
  color: #fff;
}

.server-error__btn--secondary {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.18);
}
</style>
