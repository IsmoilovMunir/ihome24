<template>
  <div class="min-h-[60vh] flex items-center justify-center py-12">
    <div class="w-full max-w-md">
      <div class="bg-white rounded-lg shadow-md p-8">
        <h1 class="text-3xl font-bold text-gray-900 mb-6 text-center">Регистрация</h1>
        <p class="text-gray-600 mb-6 text-center">
          Регистрация выполняется при первом входе по номеру телефона. Введите номер, получите код в SMS, введите код — и при необходимости укажите ФИО и email.
        </p>
        <router-link
          to="/login"
          class="block w-full bg-primary-600 text-white py-3 rounded-md hover:bg-primary-700 text-center font-semibold"
        >
          Перейти к входу
        </router-link>
        <div class="mt-6 text-center">
          <p class="text-gray-600">
            Уже есть аккаунт?
            <router-link to="/login" class="text-primary-600 hover:text-primary-700 font-semibold">
              Войти
            </router-link>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
</script>
