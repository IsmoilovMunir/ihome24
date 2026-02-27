<template>
  <div class="personal-profile-page">
    <div class="personal-profile-page__stack">
      <!-- Блок учётных данных -->
      <div class="personal-credentials">
        <label class="personal-user-avatar personal-credentials__avatar">
          <input
            type="file"
            accept=".png,.jpg,.jpeg"
            class="personal-user-avatar__input"
            ref="avatarInputRef"
            @change="onAvatarChange"
          />
          <div class="personal-user-avatar__inner">
            <img
              v-if="avatarPreview"
              :src="avatarPreview"
              alt="Аватар"
              class="personal-user-avatar__img"
            />
            <span v-else class="personal-user-avatar__letter">{{ avatarLetter }}</span>
            <div class="personal-user-avatar__overlay">
              <!-- Камера, когда фото ещё нет -->
              <svg
                v-if="!avatarPreview"
                viewBox="0 0 24 24"
                fill="none"
                width="22"
                height="22"
              >
                <path
                  d="M9 5.5L9.8 4.2C10.1 3.7 10.5 3.5 11.1 3.5H12.9C13.5 3.5 13.9 3.7 14.2 4.2L15 5.5H17C18.9 5.5 20.5 7.1 20.5 9V15C20.5 16.9 18.9 18.5 17 18.5H7C5.1 18.5 3.5 16.9 3.5 15V9C3.5 7.1 5.1 5.5 7 5.5H9Z"
                  stroke="currentColor"
                  stroke-width="1.5"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
                <circle
                  cx="12"
                  cy="12"
                  r="3.25"
                  stroke="currentColor"
                  stroke-width="1.5"
                />
              </svg>
              <!-- Карандаш, когда фото уже есть -->
              <svg
                v-else
                viewBox="0 0 24 24"
                fill="none"
                width="20"
                height="20"
              >
                <path
                  d="M5 19H9L18.5 9.5C19.3284 8.67157 19.3284 7.32843 18.5 6.5C17.6716 5.67157 16.3284 5.67157 15.5 6.5L6 16V19Z"
                  stroke="currentColor"
                  stroke-width="1.5"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
                <path
                  d="M13.5 8L16 10.5"
                  stroke="currentColor"
                  stroke-width="1.5"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
            </div>
          </div>
        </label>
        <div class="personal-credentials__name">{{ authStore.user?.fullName || 'Пользователь' }}</div>
        <div class="personal-credentials__contacts">
          <span v-if="authStore.user?.phone" class="personal-credentials__phone">{{ formatPhone(authStore.user.phone) }}</span>
          <span v-if="authStore.user?.phone" class="personal-credentials__sep">/</span>
          <button
            v-if="!authStore.user?.email"
            type="button"
            class="personal-credentials__add-email"
            @click="openEditModal"
          >
            Добавить E-mail
          </button>
          <span v-else class="personal-credentials__email">{{ authStore.user.email }}</span>
        </div>
        <button class="personal-credentials__button" @click="openEditModal">
          Редактировать
        </button>
      </div>

      <!-- Адреса доставки -->
      <div class="personal-profile-box">
        <div class="personal-profile-box__header">
          <h3 class="personal-profile-box__title">Адреса доставки</h3>
        </div>
        <div class="personal-addresses">
          <p v-if="!addresses.length" class="personal-addresses__empty">Нет сохранённых адресов</p>
          <div
            v-for="addr in addresses"
            :key="addr.id"
            class="personal-address"
            :class="{ 'personal-address--default': addr.isDefault }"
          >
            <div class="personal-address__row">
              <span class="personal-address__label">{{ addr.label }}</span>
              <div class="personal-address__actions">
                <button
                  type="button"
                  class="personal-address__control"
                  :class="{ 'personal-address__control--active': addr.isDefault }"
                  :title="addr.isDefault ? 'Адрес по умолчанию' : 'Сделать основным'"
                  @click="setDefaultAddress(addr.id)"
                >
                  <svg width="14" height="14" viewBox="0 0 14 14" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M9.18029 5.07259L13.2139 5.65879L10.2952 8.50392L10.107 8.6874L10.1514 8.94648L10.8404 12.9633L7.23301 11.0669L7.00036 10.9446L6.76771 11.0669L3.16013 12.9632L3.84924 8.94625L3.89369 8.68715L3.70544 8.50366L0.786968 5.65898L4.82016 5.07284L5.08028 5.03503L5.19661 4.79933L7.00034 1.14483L8.80383 4.79907L8.92016 5.03478L9.18029 5.07259Z" stroke="currentColor" fill="none"/>
                  </svg>
                </button>
                <button
                  type="button"
                  class="personal-address__control"
                  title="Удалить"
                  @click="deleteAddress(addr.id)"
                >
                  <svg width="24" height="24" viewBox="0 0 25 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M21.5 5.97998C18.17 5.64998 14.82 5.47998 11.48 5.47998C9.5 5.47998 7.52 5.57998 5.54 5.77998L3.5 5.97998" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M9 4.97L9.22 3.66C9.38 2.71 9.5 2 11.19 2H13.81C15.5 2 15.63 2.75 15.78 3.67L16 4.97" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M19.5 6L18.7 19.21C18.59 20.78 18.5 22 15.71 22H9.29002C6.50002 22 6.41002 20.78 6.30002 19.21L5.5 6.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </button>
              </div>
            </div>
            <div class="personal-address__full">{{ addr.fullAddress }}</div>
          </div>
        </div>
        <button class="personal-profile-box__add" @click="showAddressModal = true">
          Добавить адрес
        </button>
      </div>

      <!-- Юридическое лицо -->
      <div class="personal-profile-box personal-profile-box--collapsed">
        <div class="personal-profile-box__header personal-profile-box__header--row">
          <h3 class="personal-profile-box__title">Юридическое лицо</h3>
          <button class="personal-profile-box__add">Добавить адрес</button>
        </div>
      </div>

      <!-- Рассылки -->
      <div class="personal-profile-box personal-profile-box--collapse">
        <button
          type="button"
          class="personal-profile-box__header personal-profile-box__header--clickable"
          @click="subscriptionsOpen = !subscriptionsOpen"
        >
          <h3 class="personal-profile-box__title">Рассылки</h3>
          <svg
            width="24"
            height="24"
            viewBox="0 0 21 21"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
            class="personal-profile-box__arrow"
            :class="{ 'personal-profile-box__arrow--open': subscriptionsOpen }"
          >
            <path d="M16.1665 9.3457L11.8998 13.6124C11.2582 14.2541 10.2082 14.2541 9.56649 13.6124L5.2998 9.3457" stroke="currentColor" stroke-width="1.4" stroke-miterlimit="10" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
        <div v-show="subscriptionsOpen" class="personal-profile-box__content">
          <p class="personal-profile-box__text">Настройки рассылок будут доступны позже.</p>
        </div>
      </div>

      <!-- Удалить аккаунт -->
      <div class="personal-delete-account">
        <button type="button" class="personal-delete-account__button" @click="confirmDeleteAccount">
          Удалить аккаунт
        </button>
      </div>
    </div>

    <!-- Модальное окно редактирования -->
    <div v-if="showEditModal" class="personal-modal-overlay" @click.self="showEditModal = false">
      <div class="personal-modal">
        <h3 class="personal-modal__title">Редактировать данные</h3>
        <form class="personal-modal__form" @submit.prevent="saveProfile">
          <div class="personal-modal__field">
            <label class="personal-modal__label">ФИО</label>
            <input v-model="editForm.fullName" type="text" class="personal-modal__input" placeholder="Введите ФИО" />
          </div>
          <div class="personal-modal__field">
            <label class="personal-modal__label">E-mail</label>
            <input v-model="editForm.email" type="email" class="personal-modal__input" placeholder="example@mail.com" />
          </div>
          <div class="personal-modal__actions">
            <button type="button" class="personal-modal__btn personal-modal__btn--secondary" @click="showEditModal = false">
              Отмена
            </button>
            <button type="submit" class="personal-modal__btn personal-modal__btn--primary">
              Сохранить
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Модальное окно добавления адреса -->
    <div v-if="showAddressModal" class="personal-modal-overlay" @click.self="showAddressModal = false">
      <div class="personal-modal">
        <h3 class="personal-modal__title">{{ editingAddress ? 'Редактировать адрес' : 'Добавить адрес' }}</h3>
        <form class="personal-modal__form" @submit.prevent="saveAddress">
          <div class="personal-modal__field">
            <label class="personal-modal__label">Название (например, б-р Самаркандский)</label>
            <input v-model="addressForm.label" type="text" class="personal-modal__input" placeholder="Название адреса" required />
          </div>
          <div class="personal-modal__field">
            <label class="personal-modal__label">Полный адрес</label>
            <textarea v-model="addressForm.fullAddress" class="personal-modal__input personal-modal__textarea" placeholder="г Москва, ул. Примерная, д. 1, кв. 1" rows="3" required></textarea>
          </div>
          <div class="personal-modal__actions">
            <button type="button" class="personal-modal__btn personal-modal__btn--secondary" @click="cancelAddressEdit">
              Отмена
            </button>
            <button type="submit" class="personal-modal__btn personal-modal__btn--primary">
              {{ editingAddress ? 'Сохранить' : 'Добавить' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useAuthStore } from '../stores/auth'
import { authApi } from '../services/api'
import { fileApi } from '../services/api'

const authStore = useAuthStore()

const ADDRESSES_KEY = 'ihome24_user_addresses'

const formatPhone = (phone) => {
  if (!phone) return ''
  const digits = phone.replace(/\D/g, '')
  if (digits.length >= 10) {
    const match = digits.slice(-10).match(/^(\d{3})(\d{3})(\d{2})(\d{2})$/)
    return match ? `+7-${match[1]}-${match[2]}-${match[3]}-${match[4]}` : phone
  }
  return phone
}

const avatarLetter = computed(() => {
  const name = authStore.user?.fullName || 'П'
  return name.charAt(0).toUpperCase()
})

const avatarPreview = ref(null)
const avatarInputRef = ref(null)

function withCacheBuster(url) {
  if (!url) return url
  const sep = url.includes('?') ? '&' : '?'
  return `${url}${sep}t=${Date.now()}`
}

const showEditModal = ref(false)
const showAddressModal = ref(false)
const subscriptionsOpen = ref(false)
const editingAddress = ref(null)

const editForm = ref({
  fullName: '',
  email: '',
})

const addressForm = ref({
  label: '',
  fullAddress: '',
})

const addresses = ref([])

function loadAddresses() {
  try {
    const raw = localStorage.getItem(ADDRESSES_KEY)
    addresses.value = raw ? JSON.parse(raw) : []
  } catch {
    addresses.value = []
  }
}

function saveAddresses() {
  localStorage.setItem(ADDRESSES_KEY, JSON.stringify(addresses.value))
}

function setDefaultAddress(id) {
  addresses.value = addresses.value.map(a => ({
    ...a,
    isDefault: a.id === id,
  }))
  saveAddresses()
}

function deleteAddress(id) {
  addresses.value = addresses.value.filter(a => a.id !== id)
  if (addresses.value.length && !addresses.value.some(a => a.isDefault)) {
    addresses.value[0].isDefault = true
  }
  saveAddresses()
}

function saveAddress() {
  if (editingAddress.value) {
    const idx = addresses.value.findIndex(a => a.id === editingAddress.value.id)
    if (idx >= 0) {
      addresses.value[idx] = {
        ...addresses.value[idx],
        label: addressForm.value.label,
        fullAddress: addressForm.value.fullAddress,
      }
    }
  } else {
    const newAddr = {
      id: Date.now().toString(),
      label: addressForm.value.label,
      fullAddress: addressForm.value.fullAddress,
      isDefault: addresses.value.length === 0,
    }
    addresses.value.push(newAddr)
  }
  saveAddresses()
  cancelAddressEdit()
  showAddressModal.value = false
}

function cancelAddressEdit() {
  editingAddress.value = null
  addressForm.value = { label: '', fullAddress: '' }
  showAddressModal.value = false
}

async function onAvatarChange(e) {
  const file = e.target?.files?.[0]
  if (!file) return
  if (!/^image\/(png|jpeg|jpg)$/.test(file.type)) {
    alert('Выберите изображение в формате PNG, JPG или JPEG')
    return
  }
  try {
    const res = await authApi.uploadAvatar(file)
    authStore.setAuth(authStore.accessToken, { ...authStore.user, ...res.data })
    const base = fileApi.getFileUrl(res.data.avatar) || res.data.avatar
    avatarPreview.value = withCacheBuster(base)
  } catch (err) {
    console.error('Ошибка загрузки аватара:', err, err.response?.data)
    const msg = err.response?.data?.error
      || err.response?.data?.message
      || (err.response?.data?.errors ? Object.values(err.response.data.errors).flat().join('\n') : null)
      || 'Не удалось загрузить аватар'
    alert(msg)
  }
  e.target.value = ''
}

function openEditModal() {
  editForm.value = {
    fullName: authStore.user?.fullName || '',
    email: authStore.user?.email || '',
  }
  showEditModal.value = true
}

async function saveProfile() {
  try {
    const res = await authApi.updateMe(editForm.value)
    authStore.setAuth(authStore.accessToken, { ...authStore.user, ...res.data })
    showEditModal.value = false
  } catch (err) {
    console.error('Ошибка сохранения:', err)
    alert(err.response?.data?.errors ? Object.values(err.response.data.errors).flat().join('\n') : 'Ошибка сохранения')
  }
}

function confirmDeleteAccount() {
  if (confirm('Вы уверены, что хотите удалить аккаунт? Это действие необратимо.')) {
    authStore.logout()
    window.location.href = '/'
  }
}

watch(
  () => authStore.user,
  (user) => {
    if (user?.avatar && !avatarPreview.value) {
      const base = fileApi.getFileUrl(user.avatar) || user.avatar
      avatarPreview.value = withCacheBuster(base)
    }
  },
  { immediate: true }
)

onMounted(() => {
  loadAddresses()
})
</script>

<style scoped>
.personal-profile-page {
  font-family: "helvetica", sans-serif;
}

.personal-profile-page__stack {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* Credentials block */
.personal-credentials {
  background: #2E2826;
  border-radius: 12px;
  padding: 24px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 16px 24px;
}

.personal-user-avatar {
  cursor: pointer;
  display: block;
  flex-shrink: 0;
}

.personal-user-avatar__input {
  position: absolute;
  width: 0;
  height: 0;
  opacity: 0;
  overflow: hidden;
}

.personal-user-avatar__inner {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  background: rgba(244, 116, 39, 0.2);
  color: #F47427;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.personal-user-avatar__img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.personal-user-avatar__letter {
  font-size: 24px;
  font-weight: 600;
}

.personal-user-avatar__overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
}

.personal-user-avatar:hover .personal-user-avatar__overlay {
  opacity: 1;
}

.personal-credentials__name {
  font-size: 18px;
  font-weight: 600;
  color: #fff;
  flex: 1;
  min-width: 120px;
}

.personal-credentials__contacts {
  font-size: 14px;
  color: #9d9390;
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: wrap;
}

.personal-credentials__add-email {
  background: none;
  border: none;
  color: #F47427;
  cursor: pointer;
  padding: 0;
  font-size: inherit;
  text-decoration: underline;
}

.personal-credentials__add-email:hover {
  color: #ff8c42;
}

.personal-credentials__email {
  color: #9d9390;
}

.personal-credentials__button {
  padding: 10px 20px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s, border-color 0.2s;
}

.personal-credentials__button:hover {
  background: rgba(244, 116, 39, 0.2);
  border-color: rgba(244, 116, 39, 0.5);
}

/* Profile boxes */
.personal-profile-box {
  background: #2E2826;
  border-radius: 12px;
  padding: 24px;
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.personal-profile-box__header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.personal-profile-box__header--row {
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}

.personal-profile-box__header--clickable {
  width: 100%;
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
  margin: 0;
  text-align: left;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.personal-profile-box__title {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
  color: #fff;
}

.personal-profile-box__arrow {
  flex-shrink: 0;
  color: #9d9390;
  transition: transform 0.2s;
}

.personal-profile-box__arrow--open {
  transform: rotate(-90deg);
}

.personal-profile-box__add {
  padding: 10px 20px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s, border-color 0.2s;
}

.personal-profile-box__add:hover {
  background: rgba(244, 116, 39, 0.2);
  border-color: rgba(244, 116, 39, 0.5);
}

.personal-profile-box__content {
  padding-top: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.personal-profile-box__text {
  margin: 0;
  font-size: 14px;
  color: #9d9390;
}

/* Addresses */
.personal-addresses {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.personal-addresses__empty {
  margin: 0;
  font-size: 14px;
  color: #9d9390;
}

.personal-address {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  padding: 16px;
}

.personal-address--default {
  border-color: rgba(244, 116, 39, 0.3);
}

.personal-address__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}

.personal-address__label {
  font-size: 14px;
  font-weight: 600;
  color: #fff;
}

.personal-address__actions {
  display: flex;
  gap: 4px;
}

.personal-address__control {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: none;
  border: none;
  color: #9d9390;
  cursor: pointer;
  border-radius: 8px;
  transition: color 0.2s, background 0.2s;
}

.personal-address__control:hover {
  color: #F47427;
  background: rgba(244, 116, 39, 0.1);
}

.personal-address__control--active {
  color: #F47427;
}

.personal-address__full {
  font-size: 13px;
  color: #9d9390;
  line-height: 1.4;
}

/* Delete account */
.personal-delete-account {
  padding-top: 8px;
}

.personal-delete-account__button {
  padding: 10px 20px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  color: #9d9390;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s, color 0.2s, border-color 0.2s;
}

.personal-delete-account__button:hover {
  color: #e74c3c;
  border-color: rgba(231, 76, 60, 0.5);
  background: rgba(231, 76, 60, 0.1);
}

/* Modal */
.personal-modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.personal-modal {
  background: #2E2826;
  border-radius: 12px;
  padding: 24px;
  max-width: 440px;
  width: 100%;
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.personal-modal__title {
  margin: 0 0 20px;
  font-size: 18px;
  font-weight: 600;
  color: #fff;
}

.personal-modal__form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.personal-modal__field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.personal-modal__label {
  font-size: 13px;
  font-weight: 500;
  color: #9d9390;
}

.personal-modal__input {
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 8px;
  color: #fff;
  font-size: 14px;
  font-family: inherit;
}

.personal-modal__input::placeholder {
  color: #6b615e;
}

.personal-modal__textarea {
  resize: vertical;
  min-height: 80px;
}

.personal-modal__actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  padding-top: 8px;
}

.personal-modal__btn {
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  font-family: inherit;
  transition: background 0.2s, border-color 0.2s;
}

.personal-modal__btn--secondary {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #fff;
}

.personal-modal__btn--secondary:hover {
  background: rgba(255, 255, 255, 0.15);
}

.personal-modal__btn--primary {
  background: #F47427;
  border: none;
  color: #fff;
}

.personal-modal__btn--primary:hover {
  background: #ff8c42;
}
</style>
