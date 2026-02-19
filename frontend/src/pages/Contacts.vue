<template>
  <div class="min-h-[60vh] py-12">
    <div class="container mx-auto px-4 relative">
      <router-link to="/" class="contacts-home-btn contacts-home-btn-top">
        <svg class="contacts-home-btn-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M19 12H5M12 19l-7-7 7-7"/>
        </svg>
        На главную
      </router-link>

      <h1 class="text-3xl md:text-4xl font-bold text-white mb-8 text-center uppercase tracking-wide" style="font-family: 'bork', sans-serif;">
        Контакты
      </h1>

      <div class="max-w-2xl mx-auto">
        <div class="contacts-card">
          <!-- Реквизиты -->
          <div class="contacts-section">
            <h2 class="contacts-section-title">Реквизиты</h2>
            <p class="contacts-entity">ИП ХАЛИДИ ФАРЕС АБДУЛ ХАЛИЛ</p>
            <div class="contacts-row">
              <span class="contacts-label">ОГРНИП:</span>
              <span class="contacts-value">321774600061352</span>
            </div>
            <div class="contacts-row">
              <span class="contacts-label">ИНН:</span>
              <span class="contacts-value">772433267562</span>
            </div>
          </div>

          <!-- Адреса -->
          <div class="contacts-section">
            <h2 class="contacts-section-title">Адреса</h2>
            <div class="contacts-row contacts-row-block">
              <span class="contacts-label">Юридический адрес:</span>
              <span class="contacts-value">Москва, МКАД, 19-й километр, вл20с1, 115612</span>
            </div>
            <div class="contacts-row contacts-row-block">
              <span class="contacts-label">Фактический адрес:</span>
              <span class="contacts-value">Москва, МКАД, 19-й километр, вл20с1, 115612</span>
            </div>
          </div>

          <!-- Контакты -->
          <div class="contacts-section">
            <h2 class="contacts-section-title">Связь</h2>
            <div class="contacts-row contacts-row-highlight">
              <span class="contacts-label">Время работы:</span>
              <span class="contacts-value">Каждый день с 8:00 до 22:00</span>
            </div>
            <div class="contacts-row">
              <a :href="`tel:${phoneClean}`" class="contacts-link">
                <svg class="contacts-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
                </svg>
                <span class="contacts-value">+7 926 777 4738</span>
              </a>
            </div>
            <div class="contacts-row">
              <a href="mailto:info@ihome24.ru" class="contacts-link">
                <svg class="contacts-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                </svg>
                <span class="contacts-value">info@ihome24.ru</span>
              </a>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
const phoneClean = '+79267774738'
</script>

<style scoped>
.contacts-card {
  background: rgba(46, 40, 38, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
}

.contacts-section {
  padding-bottom: 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.contacts-section:last-of-type {
  border-bottom: none;
  padding-bottom: 0;
}

.contacts-section:not(:first-child) {
  padding-top: 24px;
}

.contacts-section-title {
  font-size: 14px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: #F47427;
  margin-bottom: 12px;
  font-family: "helvetica", sans-serif;
}

.contacts-entity {
  font-size: 16px;
  font-weight: 500;
  color: #fff;
  margin-bottom: 12px;
  line-height: 1.4;
}

.contacts-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.contacts-row:last-child {
  margin-bottom: 0;
}

.contacts-row-block {
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
}

.contacts-row-highlight {
  margin-bottom: 16px;
}

.contacts-label {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.65);
  min-width: 140px;
}

.contacts-value {
  font-size: 15px;
  color: #fff;
}

.contacts-link {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #fff;
  text-decoration: none;
  transition: color 0.2s;
}

.contacts-link:hover {
  color: #F47427;
}

.contacts-icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

@media (max-width: 640px) {
  .contacts-card {
    padding: 24px 20px;
  }

  .contacts-row-block .contacts-value {
    font-size: 14px;
  }
}

.contacts-home-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 14px 32px;
  font-size: 0.875rem;
  font-weight: 600;
  color: #fff;
  background: transparent;
  border-radius: 8px;
  text-decoration: none;
  transition: transform 0.2s, color 0.2s;
  font-family: "helvetica", sans-serif;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.contacts-home-btn-arrow {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
}

.contacts-home-btn:hover {
  transform: translateY(-2px);
  color: #F47427;
}

.contacts-home-btn-top {
  position: absolute;
  top: 0;
  left: 1rem;
}

@media (max-width: 640px) {
  .contacts-home-btn-top {
    left: 1rem;
    padding: 10px 20px;
    font-size: 0.75rem;
  }
}
</style>
