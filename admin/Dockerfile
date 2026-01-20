# Dockerfile для админ-панели (Vue.js)
FROM node:20-alpine

WORKDIR /app

# Аргументы сборки для переменных окружения
ARG VITE_API_BASE_URL=http://localhost:8080/api
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL

# Копируем package files
COPY package*.json ./

# Устанавливаем зависимости (отключаем postinstall, т.к. файлы еще не скопированы)
RUN npm ci --ignore-scripts

# Копируем остальные файлы
COPY . .

# Запускаем postinstall скрипты после копирования всех файлов
RUN npm run build:icons && npm run msw:init

# Открываем порт
EXPOSE 5173

# Запускаем dev сервер
CMD ["npm", "run", "dev", "--", "--host", "0.0.0.0"]
