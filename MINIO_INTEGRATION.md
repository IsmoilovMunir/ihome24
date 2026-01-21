# Интеграция MinIO S3 для загрузки файлов

## 📋 Описание

Реализована полная интеграция MinIO S3 для загрузки и управления фото и видео товаров через админ-панель.

## ✅ Реализованные функции

### Backend (Spring Boot)

1. **Конфигурация MinIO**
   - Настройка подключения к MinIO через `application.yml`
   - Автоматическое создание bucket при старте приложения

2. **Хранение метаданных**
   - Сущность `FileMetadata` для хранения информации о файлах в БД
   - Связь с товарами через `productId`
   - Отслеживание пользователя через `userId`

3. **Загрузка файлов**
   - Валидация форматов (JPG, JPEG, PNG, WEBP для фото; MP4, WEBM, AVI для видео)
   - Валидация размера (10 МБ для фото, 100 МБ для видео)
   - Автоматическая генерация превью для фото (300x300px)
   - Уникальные имена файлов с временными метками

4. **API Endpoints**
   - `POST /api/admin/files/upload` - загрузка одного файла
   - `POST /api/admin/files/upload/multiple` - загрузка нескольких файлов
   - `DELETE /api/admin/files/{fileId}` - удаление файла
   - `GET /api/admin/files/product/{productId}` - получение всех файлов товара
   - `GET /api/admin/files/product/{productId}/images` - получение только фото
   - `GET /api/admin/files/product/{productId}/videos` - получение только видео
   - `GET /api/files/{filePath}` - публичный доступ к файлам

5. **Безопасность**
   - Только администраторы могут загружать/удалять файлы
   - Публичный доступ только на чтение файлов
   - Логирование всех операций с userId, productId, fileId, timestamp

6. **Логирование**
   - Все загрузки и удаления логируются
   - Ошибки сохраняются в лог-файлы

### Frontend (Vue.js Admin Panel)

1. **Компонент FileUploader.vue**
   - Drag-and-drop загрузка файлов
   - Превью загруженных файлов
   - Прогресс загрузки
   - Удаление файлов
   - Валидация на клиенте

## 🚀 Настройка

### 1. MinIO Server

Запустите MinIO сервер (через Docker):

```bash
docker run -d \
  -p 9000:9000 \
  -p 9001:9001 \
  --name minio \
  -e "MINIO_ROOT_USER=minioadmin" \
  -e "MINIO_ROOT_PASSWORD=minioadmin" \
  minio/minio server /data --console-address ":9001"
```

### 2. Конфигурация Backend

В `application.yml` настройте параметры MinIO:

```yaml
minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket-name: ihome24-media
  max-image-size: 10485760  # 10 МБ
  max-video-size: 104857600  # 100 МБ
  thumbnail-width: 300
  thumbnail-height: 300
```

### 3. Зависимости

Все зависимости уже добавлены в `pom.xml`:
- `io.minio:minio:8.5.7` - MinIO клиент
- `net.coobird:thumbnailator:0.4.20` - Генерация превью

## 📝 Использование

### Backend API

#### Загрузка файла

```bash
curl -X POST http://localhost:8080/api/admin/files/upload \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@image.jpg" \
  -F "productId=1"
```

#### Получение файлов товара

```bash
curl http://localhost:8080/api/admin/files/product/1
```

#### Удаление файла

```bash
curl -X DELETE http://localhost:8080/api/admin/files/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Frontend Component

Использование компонента `FileUploader.vue`:

```vue
<template>
  <FileUploader
    v-model="uploadedFiles"
    :product-id="productId"
    file-type="IMAGE"
    :multiple="true"
  />
</template>

<script setup>
import { ref } from 'vue'
import FileUploader from '@/components/file-upload/FileUploader.vue'

const productId = ref(1)
const uploadedFiles = ref([])
</script>
```

#### Props компонента:

- `modelValue` (Array) - массив загруженных файлов
- `productId` (Number, optional) - ID товара
- `fileType` (String) - тип файлов: 'IMAGE', 'VIDEO', или 'BOTH'
- `multiple` (Boolean) - разрешить множественную загрузку
- `maxSize` (Number, optional) - максимальный размер в байтах

#### Events:

- `@update:modelValue` - обновление списка файлов
- `@uploaded` - файлы успешно загружены
- `@deleted` - файл удален

## 📁 Структура файлов

### Backend

```
backend/src/main/java/com/ihome24/ihome24/
├── config/storage/
│   └── MinIOConfig.java
├── entity/storage/
│   └── FileMetadata.java
├── repository/storage/
│   └── FileMetadataRepository.java
├── service/storage/
│   ├── MinIOService.java
│   └── FileService.java
├── controller/
│   ├── admin/storage/
│   │   └── FileUploadController.java
│   └── publicapi/storage/
│       └── FileController.java
└── dto/response/storage/
    └── FileUploadResponse.java
```

### Frontend

```
admin/src/components/file-upload/
└── FileUploader.vue
```

## 🔒 Безопасность

1. **Авторизация**: Только администраторы могут загружать/удалять файлы
2. **Валидация**: Проверка формата и размера файлов на backend
3. **Логирование**: Все операции логируются с информацией о пользователе
4. **Публичный доступ**: Только чтение файлов через `/api/files/**`

## 📊 Логирование

Все операции логируются в формате:

```
INFO - File uploaded successfully - userId: 1, productId: 1, fileId: 1, fileName: image_20240109120000_abc123.jpg, size: 1024000 bytes, timestamp: 2024-01-09T12:00:00
INFO - File deleted successfully - userId: 1, fileId: 1, fileName: image_20240109120000_abc123.jpg, timestamp: 2024-01-09T12:00:00
```

## 🐛 Обработка ошибок

Все ошибки логируются и возвращаются клиенту с понятными сообщениями:

- Неподдерживаемый формат файла
- Превышение максимального размера
- Ошибки подключения к MinIO
- Ошибки генерации превью

## 📈 Масштабируемость

1. **MinIO**: Поддерживает горизонтальное масштабирование
2. **База данных**: Метаданные хранятся в PostgreSQL/H2
3. **CDN**: Можно настроить CDN для раздачи файлов через presigned URLs

## 🔄 Интеграция с существующим кодом

Компонент можно интегрировать в страницу добавления товара, заменив текстовые поля URL на компонент загрузки файлов.

Пример интеграции в `product/add/index.vue`:

```vue
<template>
  <VCard v-show="activeTab === 'media'">
    <VCardText>
      <!-- Загрузка фото -->
      <FileUploader
        v-model="productImages"
        :product-id="productId"
        file-type="IMAGE"
        :multiple="true"
        @uploaded="handleImagesUploaded"
      />
      
      <!-- Загрузка видео -->
      <FileUploader
        v-model="productVideos"
        :product-id="productId"
        file-type="VIDEO"
        :multiple="false"
        @uploaded="handleVideosUploaded"
      />
    </VCardText>
  </VCard>
</template>

<script setup>
import FileUploader from '@/components/file-upload/FileUploader.vue'

const productImages = ref([])
const productVideos = ref([])

const handleImagesUploaded = (files) => {
  // Обновить mainImage и galleryImages
  if (files.length > 0) {
    mainImage.value = files[0].url
    galleryImages.value = files.map(f => f.url)
  }
}
</script>
```

## 📝 Примечания

1. Для продакшена рекомендуется настроить presigned URLs для прямого доступа к файлам
2. Можно добавить кэширование превью для улучшения производительности
3. Рекомендуется настроить автоматическую очистку неиспользуемых файлов
4. Для больших файлов можно реализовать chunked upload
