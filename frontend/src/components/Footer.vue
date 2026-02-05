<template>
  <footer class="bg-[#3A3331] text-white mt-auto border-t border-white/20">
    <div class="container mx-auto px-4 py-8">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
        <div>
          <h3 class="text-xl font-bold mb-4">iHome24</h3>
          <p class="text-gray-400">
            Интернет-магазин качественных товаров для дома и офиса
          </p>
        </div>
        <div>
          <h4 class="text-lg font-semibold mb-4">Навигация</h4>
          <ul class="space-y-2">
            <li>
              <router-link to="/" class="text-gray-400 hover:text-white transition-colors">
                Главная
              </router-link>
            </li>
            <li>
              <router-link to="/products" class="text-gray-400 hover:text-white transition-colors">
                Каталог
              </router-link>
            </li>
          </ul>
        </div>
        <div>
          <h4 class="text-lg font-semibold mb-4">Контакты</h4>
          <p class="text-gray-400">Email: info@ihome24.ru</p>
        </div>
      </div>
      <div class="border-t border-white/10 mt-8 pt-8 text-center text-gray-400">
        <p>&copy; 2025 iHome24. Все права защищены.</p>
      </div>
    </div>
  </footer>
</template>
