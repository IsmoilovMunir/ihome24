<template>
  <div class="min-h-full bg-[#2E2826]">
  <div class="container mx-auto px-4 pt-12 pb-8">
    <h1 class="text-3xl font-bold text-white mb-8">Оформление заказа</h1>

    <div v-if="cartStore.isEmpty" class="text-center py-12">
      <p class="text-xl text-gray-300 mb-4">Ваша корзина пуста</p>
      <router-link
        to="/products"
        class="inline-block bg-primary-600 text-white px-6 py-3 rounded-md hover:bg-primary-700 transition-colors"
      >
        Перейти в каталог
      </router-link>
    </div>

    <form v-else class="grid grid-cols-1 lg:grid-cols-3 gap-6" novalidate @submit.prevent="onFormSubmit">
      <!-- Left column: 3 sections -->
      <div class="lg:col-span-2 space-y-6">
        <div class="flex flex-wrap items-center gap-4 text-sm">
          <span v-for="(s, i) in stepLabels" :key="s.key" class="flex items-center gap-2">
            <span
              :class="[
                'flex h-8 w-8 shrink-0 items-center justify-center rounded-full border-2 font-semibold',
                stepIndex >= i ? 'border-[#F47327] bg-[#F47327]/20 text-white' : 'border-white/20 text-gray-500',
              ]"
            >{{ i + 1 }}</span>
            <span :class="stepIndex >= i ? 'text-white' : 'text-gray-500'">{{ s.label }}</span>
          </span>
        </div>
        <!-- Section 1: Контактные данные (только для гостей; для авторизованных берём из профиля) -->
        <div id="checkout-contacts" v-if="!authStore.isAuthenticated" class="rounded-2xl shadow-md p-6" style="background-color: #3A3331;">
          <h2 class="text-xl font-semibold mb-4 text-white">Контактные данные</h2>
          <div v-if="checkoutStep === 'contacts'" class="space-y-4">
              <div>
                <label class="block text-sm font-medium text-gray-300 mb-1">ФИО *</label>
                <input
                  v-model="form.fullName"
                  type="text"
                  class="w-full px-4 py-2 bg-white/10 border rounded-lg text-white placeholder-gray-400 focus:ring-2 focus:ring-[#F47327] focus:border-transparent"
                  :class="fieldErrors.fullName ? 'border-red-400' : 'border-white/20'"
                  placeholder="Иванов Иван Иванович"
                  @blur="touchField('fullName')"
                />
                <p v-if="fieldErrors.fullName" class="mt-1 text-sm text-red-400">{{ fieldErrors.fullName }}</p>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-300 mb-1">Email *</label>
                <input
                  v-model="form.email"
                  type="email"
                  class="w-full px-4 py-2 bg-white/10 border rounded-lg text-white placeholder-gray-400 focus:ring-2 focus:ring-[#F47327] focus:border-transparent"
                  :class="fieldErrors.email ? 'border-red-400' : 'border-white/20'"
                  placeholder="example@mail.ru"
                  @blur="touchField('email')"
                />
                <p v-if="fieldErrors.email" class="mt-1 text-sm text-red-400">{{ fieldErrors.email }}</p>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-300 mb-1">Телефон *</label>
                <input
                  v-model="form.phone"
                  type="tel"
                  class="w-full px-4 py-2 bg-white/10 border rounded-lg text-white placeholder-gray-400 focus:ring-2 focus:ring-[#F47327] focus:border-transparent"
                  :class="fieldErrors.phone ? 'border-red-400' : 'border-white/20'"
                  placeholder="+7 (999) 123-45-67"
                  @blur="touchField('phone')"
                />
                <p v-if="fieldErrors.phone" class="mt-1 text-sm text-red-400">{{ fieldErrors.phone }}</p>
              </div>
          </div>
          <p v-else class="text-gray-300 text-sm">
            {{ form.fullName }} · {{ form.email }} · {{ formatPhoneDisplay(form.phone) }}
            <button type="button" class="ml-2 text-[#F47327] hover:underline text-sm" @click="checkoutStep = 'contacts'">Изменить</button>
          </p>
        </div>
        <div v-else class="rounded-2xl shadow-md p-6" style="background-color: #3A3331;">
          <h2 class="text-xl font-semibold mb-4 text-white">Контактные данные</h2>
          <p class="text-gray-300 text-sm">
            {{ authStore.user?.fullName }} · {{ authStore.user?.email || '—' }} · {{ formatPhoneDisplay(authStore.user?.phone) }}
          </p>
        </div>

        <!-- Section 2: Способ доставки -->
        <div v-show="showDeliveryStep" id="checkout-delivery-method" data-test="checkout-delivery-method" class="rounded-2xl shadow-md p-6" style="background-color: #3A3331;">
          <h2 class="text-xl font-semibold mb-4 text-white">Способ доставки</h2>
          <div class="flex gap-3 mb-4">
            <button
              type="button"
              data-test="checkout-delivery-method-delivery-tab"
              :class="['flex-1 flex items-center justify-center gap-2 py-3 px-4 rounded-xl border-2 transition-colors', deliveryMethod === 'delivery' ? 'border-[#F47327] bg-[#F47327]/20 text-white' : 'border-white/20 text-gray-400 hover:border-white/40']"
              @click="deliveryMethod = 'delivery'"
            >
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 8h14M5 8a2 2 0 110-4h14a2 2 0 110 4M5 8v10a2 2 0 002 2h10a2 2 0 002-2V8m-9 4h4" />
              </svg>
              <span data-test="checkout-delivery-method-delivery-method-name">Доставка</span>
            </button>
            <button
              type="button"
              data-test="checkout-delivery-method-pickup-tab"
              :class="['flex-1 flex items-center justify-center gap-2 py-3 px-4 rounded-xl border-2 transition-colors', deliveryMethod === 'pickup' ? 'border-[#F47327] bg-[#F47327]/20 text-white' : 'border-white/20 text-gray-400 hover:border-white/40']"
              @click="deliveryMethod = 'pickup'"
            >
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
              <span data-test="checkout-delivery-method-delivery-method-name">Самовывоз</span>
            </button>
          </div>
          <div v-if="deliveryMethod === 'delivery'" data-test="checkout-delivery-method-select-delivery-cta" class="flex items-center justify-between p-4 rounded-xl bg-white/5 border border-white/10">
            <div>
              <span data-test="checkout-delivery-method-select-delivery-cta-subtitle" class="text-gray-300">Куда доставить заказ?</span>
            </div>
            <button type="button" data-test="checkout-cta-button" class="px-4 py-2 rounded-xl bg-[#3A3331] border-2 border-[#F47327] text-white font-semibold hover:bg-[#F47327]/50 transition-colors" @click="openDeliveryMapDialog">
              Указать
            </button>
          </div>
          <div v-if="deliveryMethod === 'delivery'" class="mt-3">
            <label class="block text-sm font-medium text-gray-300 mb-1">Адрес доставки *</label>
            <textarea
              v-model="form.address"
              rows="2"
              class="w-full px-4 py-2 bg-white/10 border border-white/20 rounded-lg text-white placeholder-gray-400 focus:ring-2 focus:ring-[#F47327] focus:border-transparent"
              placeholder="Город, улица, дом, квартира"
            ></textarea>
          </div>
        </div>

        <!-- Section 3: Способ оплаты -->
        <div id="checkout-payment-method" v-show="showPaymentStep" data-test="checkout-payment-method" class="rounded-2xl shadow-md p-6" style="background-color: #3A3331;">
          <h2 class="text-xl font-semibold mb-4 text-white">Способ оплаты</h2>
          <div class="grid grid-cols-2 sm:grid-cols-3 gap-3">
            <button
              v-for="pm in paymentMethods"
              :key="pm.id"
              type="button"
              data-test="checkout-payment-method-card"
              :class="['flex flex-col items-center gap-2 p-4 rounded-xl border-2 transition-colors text-left', paymentMethod === pm.id ? 'border-[#F47327] bg-[#F47327]/20' : 'border-white/20 text-gray-400 hover:border-white/40']"
              @click="selectPayment(pm.id)"
            >
              <span data-test="checkout-payment-method-card-name" class="text-white font-medium text-sm">{{ pm.name }}</span>
              <span v-if="pm.desc" data-test="checkout-payment-method-card-description" class="text-gray-400 text-xs">{{ pm.desc }}</span>
            </button>
          </div>
          <CheckoutCompanyPicker
            v-if="paymentMethod === 'invoice'"
            ref="companyPickerRef"
            v-model="selectedCompany"
            class="mt-4"
          />
          <div class="mt-4">
            <label class="block text-sm font-medium text-gray-300 mb-1">Комментарий к заказу</label>
            <textarea
              v-model="form.comment"
              rows="2"
              class="w-full px-4 py-2 bg-white/10 border border-white/20 rounded-lg text-white placeholder-gray-400 focus:ring-2 focus:ring-[#F47327] focus:border-transparent"
              placeholder="Дополнительная информация"
            ></textarea>
          </div>
        </div>

        <div v-if="error" class="bg-red-500/20 border border-red-400/50 text-red-300 px-4 py-3 rounded-xl">
          {{ error }}
        </div>

        <button
          v-if="showPrimaryButton"
          type="submit"
          :disabled="loading"
          :aria-busy="loading ? 'true' : 'false'"
          class="w-full bg-[#3A3331] text-white py-3 rounded-2xl border-2 border-[#F47327] hover:bg-[#F47327]/50 active:bg-[#F47327]/50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors font-semibold min-h-[48px]"
        >
          <span
            v-if="isSubmittingOrder"
            class="inline-flex items-center justify-center gap-2"
          >
            <svg
              class="h-5 w-5 animate-spin text-white"
              xmlns="http://www.w3.org/2000/svg"
              fill="none"
              viewBox="0 0 24 24"
              aria-hidden="true"
            >
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
            </svg>
            Оформляем заказ…
          </span>
          <span v-else>{{ submitButtonText }}</span>
        </button>
        <p v-if="checkoutStep === 'payment' && paymentConfirmed" class="mt-3 text-xs text-gray-400 leading-relaxed">
          Нажимая на кнопку «Заказать», вы соглашаетесь с условиями
          <NuxtLink to="/support/oferta" class="text-[#F47327] hover:underline">Публичной оферты</NuxtLink>
          и подтверждаете своё
          <button
            type="button"
            class="text-[#F47327] hover:underline inline"
            @click="showPolicyModal = true"
          >
            Согласие на обработку персональных данных
          </button>
        </p>
      </div>

      <!-- Order Summary -->
      <div class="lg:col-span-1">
        <div class="rounded-2xl shadow-md p-6 sticky top-20" style="background-color: #3A3331;">
          <h2 class="text-xl font-semibold mb-4 text-white">Ваш заказ</h2>
          <div class="space-y-3 mb-4">
            <div
              v-for="item in cartStore.items"
              :key="item.product.id"
              class="flex gap-3 items-center"
            >
              <router-link :to="productPath(item.product)" class="flex-shrink-0 w-14 h-14 rounded overflow-hidden">
                <img
                  v-if="getImageUrl(item.product)"
                  :src="getImageUrl(item.product)"
                  :alt="item.product.name"
                  class="w-full h-full object-cover"
                />
                <div
                  v-else
                  class="w-full h-full bg-[#26211E] flex items-center justify-center"
                >
                  <svg class="w-7 h-7 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                  </svg>
                </div>
              </router-link>
              <div class="flex-1 min-w-0 flex flex-col gap-0.5">
                <span class="text-white text-sm font-medium truncate">
                  {{ item.product.name }}
                </span>
                <span class="text-gray-400 text-xs">
                  {{ item.quantity }} шт. · {{ formatPrice(getItemTotal(item)) }}
                </span>
              </div>
            </div>
          </div>
          <div class="border-t border-white/20 pt-4">
            <div class="flex justify-between text-xl font-bold text-white">
              <span>Итого:</span>
              <span>{{ formatPrice(cartStore.totalPrice) }}</span>
            </div>
          </div>
        </div>
      </div>
    </form>
  </div>
  </div>

  <!-- Диалог выбора адреса доставки на карте -->
  <Teleport to="body">
    <div
      v-if="showDeliveryMapDialog"
      class="fixed inset-0 z-[2009] flex items-start justify-center pt-[15vh] pb-8 px-4 bg-black/60 backdrop-blur-sm"
      data-test="checkout-new-delivery-dialog"
      @click.self="closeDeliveryMapDialog"
    >
      <div
        role="dialog"
        aria-modal="true"
        aria-label="Способ получения"
        class="w-full max-w-[1091px] rounded-2xl shadow-2xl overflow-hidden flex flex-col max-h-[85vh]"
        style="background-color: #3A3331;"
        @click.stop
      >
        <div class="flex items-center justify-between p-4 border-b border-white/10">
          <h3 class="text-xl font-semibold text-white" data-test="checkout-new-delivery-dialog-dialog-title">
            Способ получения
          </h3>
          <button
            type="button"
            data-test="ui-modern-dialog-close"
            class="p-2 rounded-lg text-gray-400 hover:text-white hover:bg-white/10 transition-colors"
            aria-label="Закрыть"
            @click="closeDeliveryMapDialog"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        <div class="flex gap-3 p-4 border-b border-white/10">
          <button
            type="button"
            :class="['flex-1 py-2.5 px-4 rounded-xl border-2 transition-colors font-medium', deliveryMapTab === 'delivery' ? 'border-[#F47327] bg-[#F47327]/20 text-white' : 'border-white/20 text-gray-400 hover:border-white/40']"
            data-test="checkout-new-delivery-dialog-shipping-method-selector"
            @click="deliveryMapTab = 'delivery'"
          >
            Доставка
          </button>
          <button
            type="button"
            :class="['flex-1 py-2.5 px-4 rounded-xl border-2 transition-colors font-medium', deliveryMapTab === 'pickup' ? 'border-[#F47327] bg-[#F47327]/20 text-white' : 'border-white/20 text-gray-400 hover:border-white/40']"
            data-test="checkout-new-delivery-dialog-shipping-method-selector"
            @click="deliveryMapTab = 'pickup'"
          >
            Самовывоз
          </button>
        </div>

        <div class="p-4 overflow-auto flex-1" data-test="checkout-delivery-body">
          <div class="flex flex-col lg:flex-row gap-4" data-test="checkout-delivery-body-desktop">
            <!-- Карта: явная высота на мобильном, чтобы карта не схлопывалась (height: 0) -->
            <div class="lg:flex-1 shrink-0 relative h-[280px] sm:h-[320px] lg:h-[500px] rounded-xl overflow-hidden border border-white/10 bg-[#2E2826]" data-test="checkout-delivery-body-desktop-map">
              <div ref="mapContainerRef" class="absolute inset-0 w-full h-full">
                <div v-if="mapLocationLoading" class="absolute inset-0 flex flex-col items-center justify-center gap-3 text-gray-300">
                  <span class="animate-pulse">Определяем ваше местоположение...</span>
                </div>
                <div v-else-if="!mapLoaded && !mapLoadError" class="absolute inset-0 flex items-center justify-center text-gray-400">
                  Загрузка карты...
                </div>
                <div v-if="mapLoadError" class="absolute inset-0 flex items-center justify-center text-amber-400 px-4 text-center">
                  {{ mapLoadError }}
                </div>
              </div>
            </div>

            <!-- Адрес и действия -->
            <div class="lg:w-[340px] flex flex-col gap-4">
              <div data-test="checkout-delivery-body-desktop-address-from-map-input">
                <label class="block text-sm font-medium text-gray-300 mb-1.5">
                  Город, улица, дом
                </label>
                <textarea
                  v-model="mapDialogAddress"
                  rows="3"
                  class="w-full px-4 py-3 bg-white/10 border border-white/20 rounded-xl text-white placeholder-gray-400 focus:ring-2 focus:ring-[#F47327] focus:border-transparent resize-none"
                  placeholder="Введите адрес или выберите на карте"
                />
              </div>
              <p class="text-sm text-gray-400">
                Перетащите метку в нужное место или кликните по карте — адрес обновится автоматически
              </p>
              <p v-if="mapGeocodeLoading" class="text-sm text-[#F47327]">
                Определяем адрес...
              </p>
              <button
                type="button"
                data-test="checkout-delivery-body-desktop-show-address-form-button"
                class="mt-auto w-full py-3 px-4 rounded-xl bg-[#F47327] text-white font-semibold hover:bg-[#F47327]/90 transition-colors"
                @click="confirmDeliveryAddress"
              >
                Продолжить
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Teleport>

  <!-- Оверлей загрузки при отправке заказа -->
  <Teleport to="body">
    <Transition name="success-fade">
      <div
        v-if="isSubmittingOrder"
        class="fixed inset-0 z-[2005] flex flex-col items-center justify-center gap-4 bg-black/60 backdrop-blur-sm px-4"
        role="status"
        aria-live="polite"
        aria-label="Оформление заказа"
      >
        <div class="w-14 h-14 rounded-full border-4 border-white/20 border-t-[#F47327] animate-spin" />
        <p class="text-white text-lg font-semibold text-center">
          Оформляем заказ…
        </p>
        <p class="text-gray-400 text-sm text-center max-w-xs">
          Пожалуйста, подождите
        </p>
      </div>
    </Transition>
  </Teleport>

  <!-- Модальное окно успешного оформления заказа -->
  <Teleport to="body">
    <Transition name="success-fade">
      <div
        v-if="showOrderSuccess"
        class="fixed inset-0 z-[2010] flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm"
        data-test="checkout-order-success-dialog"
      >
        <div
          class="w-full max-w-md rounded-2xl shadow-2xl overflow-hidden text-center"
          style="background-color: #3A3331; border: 1px solid rgba(255,255,255,0.08);"
        >
          <div class="pt-10 pb-6 px-8">
            <div class="w-16 h-16 mx-auto mb-6 rounded-full bg-[#F47327]/20 flex items-center justify-center ring-4 ring-[#F47327]/30">
              <svg class="w-8 h-8 text-[#F47327]" fill="none" stroke="currentColor" viewBox="0 0 24 24" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <path d="M5 13l4 4L19 7" />
              </svg>
            </div>
            <h3 class="text-2xl font-bold text-white mb-2">
              Заказ оформлен
            </h3>
            <p class="text-gray-300 text-sm mb-1">
              Номер заказа
            </p>
            <p class="text-xl font-semibold text-[#F47327] mb-4" data-test="checkout-order-success-number">
              #{{ formatOrderNumber(orderSuccessNumber) }}
            </p>
            <p class="text-gray-400 text-sm mb-6">
              <template v-if="lastPaymentMethod === 'invoice'">
                Реквизиты для оплаты отправлены на email. Назначение платежа: «Оплата заказа №{{ formatOrderNumber(orderSuccessNumber) }}».
              </template>
              <template v-else>
                Подтверждение придёт на указанный email
              </template>
            </p>
            <div
              v-if="lastPaymentMethod === 'invoice' && paymentDetailsForSuccess"
              class="text-left text-xs text-gray-300 bg-white/5 border border-white/10 rounded-xl p-4 mb-6 space-y-1"
            >
              <p class="font-semibold text-white text-sm mb-2">
                Реквизиты для оплаты
              </p>
              <p v-if="paymentDetailsForSuccess.name">
                Получатель: {{ paymentDetailsForSuccess.name }}
              </p>
              <p v-if="paymentDetailsForSuccess.inn">
                ИНН: {{ paymentDetailsForSuccess.inn }}
              </p>
              <p v-if="paymentDetailsForSuccess.kpp">
                КПП: {{ paymentDetailsForSuccess.kpp }}
              </p>
              <p v-if="paymentDetailsForSuccess.bankName">
                Банк: {{ paymentDetailsForSuccess.bankName }}
              </p>
              <p v-if="paymentDetailsForSuccess.bik">
                БИК: {{ paymentDetailsForSuccess.bik }}
              </p>
              <p v-if="paymentDetailsForSuccess.bankAccount">
                Р/с: {{ paymentDetailsForSuccess.bankAccount }}
              </p>
              <p v-if="paymentDetailsForSuccess.correspondentAccount">
                К/с: {{ paymentDetailsForSuccess.correspondentAccount }}
              </p>
            </div>
            <button
              type="button"
              class="w-full py-3.5 px-6 rounded-xl bg-[#F47327] text-white font-semibold hover:bg-[#F47327]/90 active:bg-[#F47327]/80 transition-colors"
              data-test="checkout-order-success-close"
              @click="closeOrderSuccess"
            >
              На главную
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>

  <Teleport to="body">
    <div
      v-if="showPolicyModal"
      class="fixed inset-0 z-[2010] flex items-center justify-center bg-black/60 px-4 py-16 md:py-20"
      @click.self="showPolicyModal = false"
    >
      <div class="max-w-xl w-full rounded-2xl bg-[#2E2826] border border-white/10 p-5 md:p-6 text-sm text-gray-100 shadow-xl">
        <h2 class="text-base md:text-lg font-semibold mb-3 text-center">
          Согласие на обработку персональных данных для iHome24.ru
        </h2>
        <div class="space-y-2.5 max-h-[60vh] overflow-y-auto pr-1 text-[11px] md:text-xs leading-relaxed">
          <p>
            1. Настоящим лицо, подтвердившее своё согласие при регистрации на сайте ihome24.ru (далее — Клиент),
            во исполнение требований Федерального закона «О персональных данных» № 152‑ФЗ от 27.07.2006 г.,
            свободно, своей волей и в своём интересе даёт Обществу, осуществляющему деятельность под брендом
            iHome24 (далее — Оператор), а также его обособленным подразделениям на территории Российской Федерации,
            своё согласие на обработку персональных данных, указанных при регистрации и оформлении заказов.
          </p>
          <p>
            2. Обработка персональных данных Клиента осуществляется любыми способами, предусмотренными законодательством РФ,
            включая, но не ограничиваясь: сбор, запись, систематизацию, накопление, хранение, уточнение (обновление,
            изменение), использование, передачу (в том числе третьим лицам по договорам поручения), обезличивание,
            блокирование и уничтожение персональных данных.
          </p>
          <p>
            3. Цели обработки персональных данных: идентификация Клиента; заключение и исполнение договоров купли‑продажи
            и договоров оказания услуг; доставка заказов; ведение взаиморасчётов; улучшение качества работы сайта и
            сервисов; направление информационных и маркетинговых рассылок при наличии согласия.
          </p>
          <p>
            4. Оператор вправе привлекать на договорной основе третьих лиц для обработки персональных данных при условии
            соблюдения ими требований законодательства РФ об обеспечении конфиденциальности и безопасности персональных данных.
          </p>
          <p>
            5. Согласие действует в течение всего срока использования Клиентом сайта и сервисов iHome24 и может быть
            отозвано Клиентом путём направления письменного заявления в адрес Оператора. В случае отзыва согласия обработка
            и хранение персональных данных осуществляется в объёме и в течение сроков, предусмотренных действующим
            законодательством РФ.
          </p>
        </div>
        <div class="mt-4 flex justify-end">
          <button
            type="button"
            class="px-5 py-2.5 rounded-md bg-[#F47427] text-white text-sm font-semibold hover:bg-[#ff8c42] transition-colors"
            @click="showPolicyModal = false"
          >
            Понятно
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, computed, watch, nextTick, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { onMounted } from 'vue'
import { useCartStore } from '~/stores/cart'
import { useAuthStore } from '~/stores/auth'
import { useSettingsStore } from '~/stores/settings'
import { fileApi, orderApi, checkoutApi, settingsApi } from '~/utils/api'
import { productPath } from '~/utils/productUrl'
import { validateCheckoutContacts } from '~/utils/checkoutValidation'

const router = useRouter()
const cartStore = useCartStore()
const authStore = useAuthStore()
const settingsStore = useSettingsStore()

const YANDEX_MAPS_API_KEY = import.meta.env.VITE_YANDEX_MAPS_API_KEY || ''
const MOSCOW_CENTER = [37.6173, 55.7558]
const DEFAULT_ZOOM = 12

const PRELIMINARY_ORDER_KEY = 'checkoutPreliminaryOrderId'
const preliminaryOrderId = ref(null)

onMounted(async () => {
  await settingsStore.fetchPriceTiers()
  cartStore.validateCart()
  if (import.meta.client) {
    const stored = sessionStorage.getItem(PRELIMINARY_ORDER_KEY)
    if (stored) {
      const id = Number(stored)
      if (!Number.isNaN(id)) preliminaryOrderId.value = id
    }
  }
})

const loading = ref(false)
const error = ref(null)
const deliveryMethod = ref('delivery')
const paymentMethod = ref(null)
const selectedCompany = ref(null)
const companyPickerRef = ref(null)

// Диалог карты доставки
const showDeliveryMapDialog = ref(false)
const deliveryMapTab = ref('delivery')
const mapDialogAddress = ref('')
const mapContainerRef = ref(null)
const mapLoaded = ref(false)
const mapLoadError = ref('')
const DELIVERY_MARKER_SOURCE_ID = 'delivery-marker-source'
let yandexMapInstance = null
let yandexMapListener = null
let yandexMarkerCollection = null
let yandexDeliveryMarker = null
let yandexScriptLoaded = false
const mapGeocodeLoading = ref(false)
const mapLocationLoading = ref(false)

// Успешное оформление заказа
const showOrderSuccess = ref(false)
const orderSuccessNumber = ref('')
const lastPaymentMethod = ref(null)
const paymentDetailsForSuccess = ref(null)
const formatOrderNumber = (value) => {
  if (value == null) return '—'
  const n = Number(value)
  if (!Number.isFinite(n)) return String(value)
  return n.toString().padStart(3, '0')
}
function closeOrderSuccess() {
  showOrderSuccess.value = false
  router.push('/')
}

function openDeliveryMapDialog() {
  mapDialogAddress.value = form.value.address || ''
  deliveryMapTab.value = 'delivery'
  mapLoaded.value = false
  mapLoadError.value = ''
  mapLocationLoading.value = true
  showDeliveryMapDialog.value = true
  nextTick(() => {
    if (deliveryMapTab.value === 'delivery' && mapContainerRef.value) {
      loadYandexAndInitMap()
    } else {
      mapLocationLoading.value = false
    }
  })
}

function closeDeliveryMapDialog() {
  destroyMap()
  mapLocationLoading.value = false
  showDeliveryMapDialog.value = false
}

function confirmDeliveryAddress() {
  form.value.address = mapDialogAddress.value.trim()
  closeDeliveryMapDialog()
}

function loadScript(src) {
  return new Promise((resolve, reject) => {
    if (document.querySelector(`script[src="${src}"]`)) {
      resolve()
      return
    }
    const script = document.createElement('script')
    script.type = 'text/javascript'
    script.src = src
    script.onload = () => resolve()
    script.onerror = () => reject(new Error('Не удалось загрузить карту'))
    document.head.appendChild(script)
  })
}

/** Обратное геокодирование: координаты → адрес (Yandex Geocoder API) */
async function reverseGeocode(lon, lat) {
  const url = `https://geocode-maps.yandex.ru/1.x/?apikey=${encodeURIComponent(YANDEX_MAPS_API_KEY)}&geocode=${lon},${lat}&format=json`
  const res = await fetch(url)
  if (!res.ok) throw new Error('Не удалось определить адрес')
  const data = await res.json()
  const member = data?.response?.GeoObjectCollection?.featureMember?.[0]
  const text = member?.GeoObject?.metaDataProperty?.GeocoderMetaData?.text
  return text || null
}

/** Иконка метки места (оранжевая, хорошо видимая) */
const LOCATION_PIN_COLOR = '#F47427'
function createLocationPinElement() {
  const el = document.createElement('div')
  el.style.cssText = 'width: 40px; height: 50px; transform: translate(-50%, -100%); cursor: grab; line-height: 0;'
  el.title = 'Перетащите в нужное место'
  el.innerHTML = `<svg width="40" height="50" viewBox="0 0 40 50" fill="none" xmlns="http://www.w3.org/2000/svg">
    <path d="M20 0C8.954 0 0 8.954 0 20c0 15 20 30 20 30s20-15 20-30C40 8.954 31.046 0 20 0z" fill="${LOCATION_PIN_COLOR}"/>
    <circle cx="20" cy="20" r="8" fill="white" stroke="${LOCATION_PIN_COLOR}" stroke-width="2"/>
  </svg>`
  return el
}

function addMarkerAt(ymaps3, lng, lat, onDragEndCallback) {
  removeDeliveryMarker()
  if (!yandexMarkerCollection) return
  const { YMapMarker } = ymaps3
  if (typeof YMapMarker !== 'function') return
  const markerEl = createLocationPinElement()
  const markerProps = {
    id: 'delivery-point',
    source: DELIVERY_MARKER_SOURCE_ID,
    coordinates: [lng, lat],
    draggable: true,
    mapFollowsOnDrag: false,
  }
  if (typeof onDragEndCallback === 'function') {
    markerProps.onDragEnd = onDragEndCallback
  }
  const marker = new YMapMarker(markerProps, markerEl)
  yandexMarkerCollection.addChild(marker)
  yandexDeliveryMarker = marker
}

async function loadYandexAndInitMap() {
  if (!mapContainerRef.value) return
  if (!YANDEX_MAPS_API_KEY) {
    mapLoadError.value = 'Добавьте VITE_YANDEX_MAPS_API_KEY в .env для отображения карты'
    mapLocationLoading.value = false
    return
  }
  try {
    if (!yandexScriptLoaded) {
      await loadScript(`https://api-maps.yandex.ru/v3/?apikey=${YANDEX_MAPS_API_KEY}&lang=ru_RU`)
      yandexScriptLoaded = true
    }

    const userPosition = await new Promise((resolve) => {
      if (!navigator.geolocation) {
        resolve(null)
        return
      }
      navigator.geolocation.getCurrentPosition(
        (pos) => resolve({ lng: pos.coords.longitude, lat: pos.coords.latitude }),
        () => resolve(null),
        { enableHighAccuracy: true, timeout: 10000, maximumAge: 60000 }
      )
    })

    mapLocationLoading.value = false

    await new Promise(r => requestAnimationFrame(r))
    await nextTick()

    await window.ymaps3.ready
    const ymaps3 = window.ymaps3
    const { YMap, YMapDefaultSchemeLayer, YMapFeatureDataSource, YMapLayer, YMapCollection, YMapListener, YMapMarker } = ymaps3
    destroyMap()

    const center = userPosition ? [userPosition.lng, userPosition.lat] : MOSCOW_CENTER
    const zoom = userPosition ? 15 : DEFAULT_ZOOM

    const markerCollection = new YMapCollection()
    yandexMarkerCollection = markerCollection

    const map = new YMap(mapContainerRef.value, {
      location: { center, zoom },
    })
    map.addChild(new YMapDefaultSchemeLayer())
    map.addChild(new YMapFeatureDataSource({ id: DELIVERY_MARKER_SOURCE_ID }))
    map.addChild(new YMapLayer({ source: DELIVERY_MARKER_SOURCE_ID, type: 'markers', zIndex: 2020 }))
    map.addChild(markerCollection)

    const updateAddressFromCoords = async (lng, lat) => {
      mapGeocodeLoading.value = true
      try {
        const address = await reverseGeocode(lng, lat)
        if (address) mapDialogAddress.value = address
      } catch (err) {
        console.error('Geocode error:', err)
      } finally {
        mapGeocodeLoading.value = false
      }
    }

    const onMarkerDragEnd = (coordinates) => {
      const lng = Array.isArray(coordinates) ? coordinates[0] : coordinates?.lng ?? coordinates?.[0]
      const lat = Array.isArray(coordinates) ? coordinates[1] : coordinates?.lat ?? coordinates?.[1]
      if (lng != null && lat != null) updateAddressFromCoords(lng, lat)
    }

    const onMapClick = async (object, event) => {
      const coords = event?.coordinates
      if (!coords || coords.length < 2) return
      const [lng, lat] = coords
      await updateAddressFromCoords(lng, lat)
      addMarkerAt(ymaps3, lng, lat, onMarkerDragEnd)
    }

    const listener = new YMapListener({ layer: 'any', onClick: onMapClick })
    map.addChild(listener)
    yandexMapListener = listener

    if (userPosition) {
      addMarkerAt(ymaps3, userPosition.lng, userPosition.lat, onMarkerDragEnd)
      mapGeocodeLoading.value = true
      try {
        const address = await reverseGeocode(userPosition.lng, userPosition.lat)
        if (address) mapDialogAddress.value = address
      } catch (_) {}
      finally {
        mapGeocodeLoading.value = false
      }
    }

    yandexMapInstance = map
    mapLoaded.value = true
  } catch (e) {
    console.error('Yandex Map init error:', e)
    mapLoadError.value = e?.message || 'Не удалось загрузить карту. Проверьте API ключ.'
    mapLocationLoading.value = false
  }
}

function removeDeliveryMarker() {
  if (yandexDeliveryMarker && yandexMarkerCollection) {
    try {
      yandexMarkerCollection.removeChild(yandexDeliveryMarker)
    } catch (_) {}
    yandexDeliveryMarker = null
  }
}

function destroyMap() {
  removeDeliveryMarker()
  yandexMapListener = null
  yandexMarkerCollection = null
  if (yandexMapInstance && mapContainerRef.value) {
    try {
      if (yandexMapInstance.destroy) yandexMapInstance.destroy()
    } catch (_) {}
    yandexMapInstance = null
  }
  mapLoaded.value = false
}

watch(deliveryMapTab, (tab) => {
  if (tab === 'delivery' && showDeliveryMapDialog.value && mapContainerRef.value && !mapLoaded.value && !mapLoadError.value) {
    nextTick(loadYandexAndInitMap)
  }
})

onUnmounted(() => {
  destroyMap()
})

const paymentMethods = [
  { id: 'cash', name: 'При получении', desc: 'наличными или картой' },
  { id: 'invoice', name: 'По расчётному счёту', desc: 'для юр. лиц' },
]

function normalizePhoneForDisplay(phone) {
  if (!phone) return ''
  const digits = phone.replace(/\D/g, '')
  if (digits.length === 11 && digits.startsWith('7')) return `+7 (${digits.slice(1, 4)}) ${digits.slice(4, 7)}-${digits.slice(7, 9)}-${digits.slice(9)}`
  return phone
}

const form = ref({
  fullName: authStore.user?.fullName || '',
  email: authStore.user?.email || '',
  phone: authStore.user?.phone ? normalizePhoneForDisplay(authStore.user.phone) : '',
  address: '',
  pickupAddress: '',
  comment: '',
})

const checkoutStep = ref(authStore.isAuthenticated ? 'delivery' : 'contacts')
const fieldErrors = ref({})
const paymentConfirmed = ref(false)
const showPolicyModal = ref(false)

const stepLabels = computed(() => {
  if (authStore.isAuthenticated) {
    return [
      { key: 'delivery', label: 'Доставка' },
      { key: 'payment', label: 'Оплата' },
    ]
  }
  return [
    { key: 'contacts', label: 'Контакты' },
    { key: 'delivery', label: 'Доставка' },
    { key: 'payment', label: 'Оплата' },
  ]
})

const stepIndex = computed(() => {
  const idx = stepLabels.value.findIndex((s) => s.key === checkoutStep.value)
  return idx >= 0 ? idx : 0
})

const showDeliveryStep = computed(
  () => authStore.isAuthenticated || checkoutStep.value === 'delivery' || checkoutStep.value === 'payment',
)
const showPaymentStep = computed(() => checkoutStep.value === 'payment')

const isSubmittingOrder = computed(() => loading.value && checkoutStep.value === 'payment')

const showPrimaryButton = computed(() => {
  if (checkoutStep.value === 'payment') return paymentConfirmed.value
  return true
})

const submitButtonText = computed(() => {
  if (loading.value) {
    if (checkoutStep.value === 'contacts') return 'Сохранение...'
    if (checkoutStep.value === 'delivery') return 'Далее...'
    return 'Оформление...'
  }
  if (checkoutStep.value === 'contacts') return 'Оформить'
  if (checkoutStep.value === 'delivery') return 'Далее'
  return 'Заказать'
})

function touchField(field) {
  const errors = validateCheckoutContacts(form.value)
  if (errors[field]) fieldErrors.value = { ...fieldErrors.value, [field]: errors[field] }
  else {
    const next = { ...fieldErrors.value }
    delete next[field]
    fieldErrors.value = next
  }
}

function scrollToCheckoutSection(id) {
  if (!import.meta.client) return
  nextTick(() => {
    document.getElementById(id)?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  })
}

function validateContactsFields() {
  const errors = validateCheckoutContacts(form.value)
  fieldErrors.value = errors
  const ok = Object.keys(errors).length === 0
  if (!ok) {
    error.value = 'Проверьте контактные данные — поля с ошибками отмечены ниже'
    scrollToCheckoutSection('checkout-contacts')
  }
  return ok
}

function buildLeadCartItems() {
  return cartStore.items
    .filter(item => item.product?.id != null)
    .map(item => ({
      productId: item.product.id,
      quantity: item.quantity,
    }))
}

function rememberPreliminaryOrderId(id) {
  if (id == null) return
  preliminaryOrderId.value = id
  if (import.meta.client) {
    sessionStorage.setItem(PRELIMINARY_ORDER_KEY, String(id))
  }
}

async function saveCheckoutLead(step, issueToken = false) {
  if (authStore.isAuthenticated) return null
  const items = buildLeadCartItems()
  const payload = {
    fullName: form.value.fullName.trim(),
    email: form.value.email.trim(),
    phone: form.value.phone,
    step,
    ...(items.length ? { items } : {}),
  }
  const res = await checkoutApi.saveLead(payload, issueToken)
  if (res.data?.preliminaryOrderId) {
    rememberPreliminaryOrderId(res.data.preliminaryOrderId)
  }
  if (issueToken && res.data?.accessToken) {
    authStore.setAuth(res.data.accessToken, res.data.userData)
  }
  return res.data
}

function validateDeliveryStep() {
  if (deliveryMethod.value === 'delivery' && !form.value.address?.trim()) {
    error.value = 'Укажите адрес доставки'
    return false
  }
  error.value = null
  return true
}

function selectPayment(id) {
  paymentMethod.value = id
  paymentConfirmed.value = true
  if (id === 'invoice' && !paymentDetailsForSuccess.value) {
    settingsApi.getPaymentDetails()
      .then(res => { paymentDetailsForSuccess.value = res?.data ?? res })
      .catch(() => {})
  }
}

function validateInvoiceCompany() {
  if (paymentMethod.value !== 'invoice') return true
  const msg = companyPickerRef.value?.validate?.()
  if (msg) {
    error.value = msg
    return false
  }
  return true
}

watch(checkoutStep, (step) => {
  if (step === 'payment') {
    paymentConfirmed.value = false
    paymentMethod.value = null
    selectedCompany.value = null
  }
})

watch(deliveryMethod, (method) => {
  if (checkoutStep.value !== 'delivery') return
  if (method === 'pickup') {
    checkoutStep.value = 'payment'
    if (!authStore.isAuthenticated) {
      saveCheckoutLead('delivery').catch(() => {})
    }
  }
})

function formatPhoneDisplay(phone) {
  if (!phone) return '—'
  return normalizePhoneForDisplay(phone)
}

function normalizePhoneForApi(phone) {
  if (!phone) return ''
  const digits = String(phone).replace(/\D/g, '')
  return digits.length === 11 && digits.startsWith('7') ? digits : digits.length === 10 ? '7' + digits : digits
}

watch(() => authStore.user, (user) => {
  if (user) {
    form.value.fullName = user.fullName || form.value.fullName
    form.value.email = user.email || form.value.email
    form.value.phone = user.phone ? normalizePhoneForDisplay(user.phone) : form.value.phone
  }
}, { deep: true })

const getImageUrl = (product) => {
  if (product.imageUrl) {
    return fileApi.getFileUrl(product.imageUrl)
  }
  if (product.images && product.images.length > 0) {
    const img = product.images[0]
    const imgUrl = img.imageUrl || img.url || (typeof img === 'string' ? img : null)
    if (imgUrl) {
      return fileApi.getFileUrl(imgUrl)
    }
  }
  return null
}

const formatPrice = (price) => {
  if (!price) return '0 ₽'
  return new Intl.NumberFormat('ru-RU', {
    style: 'currency',
    currency: 'RUB',
    minimumFractionDigits: 0,
  }).format(price)
}

function getItemTotal(item) {
  const unitPrice = settingsStore.unitPriceForQuantity(item.product?.price ?? 0, item.quantity ?? 0)
  return Math.round(unitPrice * (item.quantity ?? 0) * 100) / 100
}

async function onFormSubmit() {
  error.value = null
  if (checkoutStep.value === 'contacts') {
    if (!validateContactsFields()) return
    loading.value = true
    let saveWarning = null
    try {
      await saveCheckoutLead('contacts')
    } catch (err) {
      const errData = err.response?.data?.errors
      if (errData) {
        const mapped = {}
        for (const [k, v] of Object.entries(errData)) {
          mapped[k] = Array.isArray(v) ? v[0] : v
        }
        fieldErrors.value = { ...fieldErrors.value, ...mapped }
        const first = Object.values(mapped)[0]
        if (Object.keys(mapped).length) {
          error.value = first || 'Проверьте контактные данные'
          scrollToCheckoutSection('checkout-contacts')
          return
        }
      }
      saveWarning = err.response?.data?.message || 'Не удалось сохранить контакты на сервере'
      console.warn('Checkout lead save failed:', err)
    } finally {
      loading.value = false
    }
    checkoutStep.value = 'delivery'
    error.value = saveWarning
    scrollToCheckoutSection('checkout-delivery-method')
    return
  }

  if (checkoutStep.value === 'delivery') {
    if (!validateDeliveryStep()) return
    loading.value = true
    try {
      if (!authStore.isAuthenticated) {
        try {
          await saveCheckoutLead('delivery')
        } catch (e) {
          console.warn('Checkout delivery lead save failed:', e)
        }
      }
      checkoutStep.value = 'payment'
      scrollToCheckoutSection('checkout-payment-method')
    } catch (_) {
      error.value = 'Не удалось перейти к оплате'
    } finally {
      loading.value = false
    }
    return
  }

  if (checkoutStep.value === 'payment') {
    await submitOrder()
    return
  }
}

const submitOrder = async () => {
  if (!paymentConfirmed.value) {
    error.value = 'Выберите способ оплаты'
    scrollToCheckoutSection('checkout-payment-method')
    return
  }
  if (!validateInvoiceCompany()) {
    scrollToCheckoutSection('checkout-payment-method')
    return
  }
  if (!validateDeliveryStep()) return

  loading.value = true
  error.value = null
  await nextTick()

  try {
    if (!authStore.isAuthenticated) {
      try {
        await saveCheckoutLead('payment', true)
      } catch (leadErr) {
        console.warn('Checkout payment lead save failed:', leadErr)
      }
    }

    const user = authStore.user
    const fullName = user?.fullName ?? form.value.fullName?.trim()
    const email = user?.email ?? form.value.email?.trim()
    const phone = user?.phone ? normalizePhoneForApi(user.phone) : normalizePhoneForApi(form.value.phone)
    const address = deliveryMethod.value === 'delivery' ? form.value.address : form.value.pickupAddress
    const orderData = {
      fullName,
      email,
      phone: phone || form.value.phone?.trim(),
      address: address?.trim() || '',
      deliveryMethod: deliveryMethod.value,
      pickupAddress: form.value.pickupAddress?.trim() || '',
      paymentMethod: paymentMethod.value,
      comment: form.value.comment.trim(),
      ...(paymentMethod.value === 'invoice' && selectedCompany.value
        ? {
            companyName: selectedCompany.value.name?.trim(),
            companyInn: selectedCompany.value.inn,
            companyKpp: selectedCompany.value.kpp || undefined,
            companyAddress: selectedCompany.value.address || undefined,
            companyOgrn: selectedCompany.value.ogrn || undefined,
            companyOkpo: selectedCompany.value.okpo || undefined,
            companyCorrAccount: selectedCompany.value.corrAccount || undefined,
            companyBik: selectedCompany.value.bik || undefined,
            companySettlementAccount: selectedCompany.value.settlementAccount || undefined,
          }
        : {}),
      items: cartStore.items.map((item) => ({
        productId: item.product.id,
        quantity: item.quantity,
      })),
      ...(preliminaryOrderId.value ? { preliminaryOrderId: preliminaryOrderId.value } : {}),
    }
    const response = await orderApi.create(orderData)
    const payload = response?.data ?? response
    orderSuccessNumber.value = payload?.order ?? payload?.orderNumber ?? '—'
    lastPaymentMethod.value = paymentMethod.value
    if (paymentMethod.value === 'invoice' && !paymentDetailsForSuccess.value) {
      try {
        const res = await settingsApi.getPaymentDetails()
        paymentDetailsForSuccess.value = res?.data ?? res
      } catch (_) {}
    }
    preliminaryOrderId.value = null
    if (import.meta.client) sessionStorage.removeItem(PRELIMINARY_ORDER_KEY)
    cartStore.clearCart()
    loading.value = false
    showOrderSuccess.value = true
  } catch (err) {
    scrollToCheckoutSection('checkout-payment-method')
    const errData = err.response?.data?.errors
    if (errData) {
      const first = Object.values(errData).flat()[0]
      error.value = first || 'Ошибка при оформлении заказа'
    } else {
      error.value = err.response?.data?.message || 'Ошибка при оформлении заказа. Попробуйте еще раз.'
    }
    console.error('Order error:', err)
  } finally {
    if (!showOrderSuccess.value) loading.value = false
  }
}

</script>

<style scoped>
.success-fade-enter-active,
.success-fade-leave-active {
  transition: opacity 0.25s ease;
}
.success-fade-enter-from,
.success-fade-leave-to {
  opacity: 0;
}
</style>
