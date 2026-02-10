<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { $api } from '@/utils/api'
import FileUploader from '@/components/file-upload/FileUploader.vue'

const router = useRouter()
const route = useRoute()

// Состояние загрузки
const isFetching = ref(false)
const activeTab = ref('basic')
const productId = ref(null)
const isEditMode = ref(false)

// ========== PRODUCT BASIC INFO ==========
const productSku = ref('')
const productBrand = ref('')
const productTitle = ref('')
const categoryId = ref(null)
const parentCategoryId = ref(null)

// ========== PRODUCT DESCRIPTION ==========
const descriptionShort = ref('')
const descriptionFull = ref('')
const benefits = ref([''])

const addBenefit = () => benefits.value.push('')
const removeBenefit = (index) => {
  if (benefits.value.length > 1) {
    benefits.value.splice(index, 1)
  }
}

// ========== CHARACTERISTICS ==========
const characteristics = ref([
  { key: '', name: '', value: '', filterable: true }
])

const addCharacteristic = () => {
  characteristics.value.push({ key: '', name: '', value: '', filterable: true })
}

const removeCharacteristic = (index) => {
  if (characteristics.value.length > 1) {
    characteristics.value.splice(index, 1)
  }
}

// ========== MEDIA ==========
const uploadedImages = ref([])
const uploadedGalleryImages = ref([])
const uploadedVideos = ref([])
const mainImage = ref('')
const galleryImages = ref([])
const videoUrl = ref('')

const handleImagesUploaded = (files) => {
  uploadedImages.value = files
  if (files.length > 0 && files[0].url) {
    mainImage.value = files[0].url
  }
  // Если есть изображения в галерее, первое может стать основным
  if (uploadedGalleryImages.value.length > 0 && !mainImage.value) {
    mainImage.value = uploadedGalleryImages.value[0].url
  }
}

// Функция для нормализации URL для сравнения
const normalizeUrl = (url) => {
  if (!url) return ''
  // Убираем протокол и домен для сравнения
  return url.replace(/^https?:\/\/[^/]+/, '').replace(/^\/api/, '')
}

const handleImageDeleted = (file) => {
  uploadedImages.value = uploadedImages.value.filter(f => f.id !== file.id)
  
  // Удаляем из galleryImages если это URL файл
  if (file.isUrl) {
    galleryImages.value = galleryImages.value.filter(url => {
      const normalizedUrl = normalizeUrl(url)
      const normalizedFileUrl = normalizeUrl(file.url)
      return normalizedUrl !== normalizedFileUrl
    })
  }
  
  if (uploadedImages.value.length > 0) {
    mainImage.value = uploadedImages.value[0].url
  } else if (uploadedGalleryImages.value.length > 0) {
    // Берем первое из галереи как основное
    mainImage.value = uploadedGalleryImages.value[0].url
    // Перемещаем его в основное
    uploadedImages.value = [uploadedGalleryImages.value[0]]
    uploadedGalleryImages.value = uploadedGalleryImages.value.slice(1)
  } else {
    mainImage.value = ''
  }
}

const handleGalleryImagesUploaded = () => {
  // Не перезаписываем uploadedGalleryImages — FileUploader уже обновил v-model полным списком (старые + новые файлы).
  // Перезапись только новыми файлами приводила к потере ранее добавленных при загрузке по одному.
  uploadedGalleryImages.value.forEach((file, index) => {
    file.sortOrder = index
  })
  // Если нет основного изображения, берем первое из галереи
  if (!mainImage.value && uploadedGalleryImages.value.length > 0 && uploadedGalleryImages.value[0].url) {
    mainImage.value = uploadedGalleryImages.value[0].url
    uploadedImages.value = [uploadedGalleryImages.value[0]]
    uploadedGalleryImages.value = uploadedGalleryImages.value.slice(1)
  }
}

const handleGalleryImageDeleted = (file) => {
  uploadedGalleryImages.value = uploadedGalleryImages.value.filter(f => f.id !== file.id)
  
  // Удаляем из galleryImages если это URL файл
  if (file.isUrl) {
    const normalizedFileUrl = normalizeUrl(file.url)
    galleryImages.value = galleryImages.value.filter(url => {
      const normalizedUrl = normalizeUrl(url)
      return normalizedUrl !== normalizedFileUrl
    })
  }
  
  // Если удалили главное изображение, обновляем mainImage
  const normalizedMainImage = normalizeUrl(mainImage.value)
  const normalizedFileUrl = normalizeUrl(file.url)
  
  if (normalizedMainImage === normalizedFileUrl) {
    if (uploadedGalleryImages.value.length > 0) {
      mainImage.value = uploadedGalleryImages.value[0].url
    } else if (uploadedImages.value.length > 0) {
      mainImage.value = uploadedImages.value[0].url
    } else {
      mainImage.value = ''
    }
  }
}

const handleVideoUploaded = (files) => {
  uploadedVideos.value = files
  if (files.length > 0 && files[0].url) {
    videoUrl.value = files[0].url
  }
}

const handleVideoDeleted = (file) => {
  uploadedVideos.value = uploadedVideos.value.filter(f => f.id !== file.id)
  if (uploadedVideos.value.length === 0) {
    videoUrl.value = ''
  }
}

// Функция для преобразования URL в объекты файлов для отображения
const createFileObjectFromUrl = (url, index = 0) => {
  if (!url) return null
  
  // Извлекаем имя файла из URL
  const urlParts = url.split('/')
  const fileName = urlParts[urlParts.length - 1] || `image_${index + 1}.png`
  
  // Формируем полный URL для отображения
  let fullUrl = url
  if (!url.startsWith('http')) {
    // Если URL начинается с /api, используем его как есть (будет проксироваться)
    // Иначе добавляем базовый URL
    if (url.startsWith('/api')) {
      fullUrl = url
    } else {
      fullUrl = `/api${url.startsWith('/') ? url : '/' + url}`
    }
  }
  
  return {
    id: `url-${index}-${Date.now()}`, // Временный ID для URL файлов
    url: fullUrl,
    thumbnailUrl: fullUrl,
    originalName: fileName,
    fileType: 'IMAGE',
    fileSize: 0, // Размер неизвестен для URL
    sortOrder: index,
    isUrl: true // Флаг что это URL, а не загруженный файл
  }
}

const addGalleryImage = () => galleryImages.value.push('')
const removeGalleryImage = (index) => {
  galleryImages.value.splice(index, 1)
}

// Функция для генерации контрольной суммы EAN-13
const generateEAN13Checksum = (code) => {
  let sum = 0
  for (let i = 0; i < 12; i++) {
    const digit = parseInt(code[i])
    sum += (i % 2 === 0) ? digit : digit * 3
  }
  const remainder = sum % 10
  return remainder === 0 ? 0 : 10 - remainder
}

// Функция для генерации EAN-13 штрихкода
const generateEAN13 = () => {
  // Код страны для России: 460-469
  const countryCode = '460'
  // Генерируем случайный 9-значный код (4 цифры производителя + 5 цифр товара)
  const randomCode = Math.floor(100000000 + Math.random() * 900000000).toString()
  const baseCode = countryCode + randomCode.substring(0, 9)
  // Вычисляем контрольную сумму
  const checksum = generateEAN13Checksum(baseCode)
  return baseCode + checksum
}

// Функция для генерации SKU штрихкода на основе SKU варианта
const generateSKUBarcode = (sku) => {
  if (!sku || sku.trim() === '') {
    // Если SKU нет, генерируем случайный 13-значный код
    return generateEAN13()
  }
  // Преобразуем SKU в числовой формат и дополняем до 13 цифр
  const skuNumeric = sku.replace(/\D/g, '') // Убираем все нецифровые символы
  if (skuNumeric.length === 0) {
    return generateEAN13()
  }
  
  // Если SKU уже 13 цифр, используем его
  if (skuNumeric.length === 13) {
    return skuNumeric
  }
  
  // Дополняем SKU до 12 цифр и вычисляем контрольную сумму
  const paddedSku = skuNumeric.padStart(12, '0').substring(0, 12)
  const checksum = generateEAN13Checksum(paddedSku)
  return paddedSku + checksum
}

// Функция для генерации ID варианта
const generateVariantId = (index) => {
  return `var_${String(index + 1).padStart(3, '0')}`
}

// Функция для генерации SKU варианта на основе основного SKU и атрибутов
const generateVariantSku = (baseSku, attributes, variantIndex) => {
  if (!baseSku || baseSku.trim() === '') {
    // Если основного SKU нет, генерируем на основе индекса
    return `VARIANT-${String(variantIndex + 1).padStart(3, '0')}`
  }
  
  const parts = [baseSku.trim()]
  
  // Добавляем цвет, если он указан
  if (attributes?.color && attributes.color.trim()) {
    const color = attributes.color.trim().toUpperCase().substring(0, 3)
    parts.push(color)
  }
  
  // Добавляем размер, если он указан
  if (attributes?.size && attributes.size.trim()) {
    const size = attributes.size.trim().toUpperCase()
    parts.push(size)
  }
  
  // Если есть атрибуты, объединяем через дефис
  if (parts.length > 1) {
    return parts.join('-')
  }
  
  // Если атрибутов нет, добавляем индекс варианта
  return `${baseSku.trim()}-${String(variantIndex + 1).padStart(3, '0')}`
}

// ========== VARIANTS ==========
const variants = ref([
  {
    variantId: '',
    sku: '',
    attributes: { color: '', size: '' },
    price: { base: '', sale: '', currency: 'RUB', vat: 20 },
    stock: { quantity: 0 },
    barcodes: { 
      skuBarcode: '', 
      ean13: '' // Будет сгенерирован автоматически
    },
    logistics: {
      weightKg: '',
      dimensionsCm: { length: '', width: '', height: '' },
      delivery: { methods: [], deliveryDays: '' }
    }
  }
])

// Инициализируем EAN-13, ID и SKU для первого варианта после определения функций
if (variants.value.length > 0) {
  const firstVariant = variants.value[0]
  if (!firstVariant.barcodes.ean13 || firstVariant.barcodes.ean13 === '') {
    firstVariant.barcodes.ean13 = generateEAN13()
  }
  if (!firstVariant.variantId || firstVariant.variantId.trim() === '') {
    firstVariant.variantId = generateVariantId(0)
  }
  if (!firstVariant.sku || firstVariant.sku.trim() === '') {
    firstVariant.sku = generateVariantSku(productSku.value, firstVariant.attributes, 0)
  }
}

const addVariant = () => {
  const variantIndex = variants.value.length
  const newVariant = {
    variantId: generateVariantId(variantIndex), // Автоматически генерируем ID варианта
    sku: generateVariantSku(productSku.value, { color: '', size: '' }, variantIndex), // Автоматически генерируем SKU варианта
    attributes: { color: '', size: '' },
    price: { base: '', sale: '', currency: 'RUB', vat: 20 },
    stock: { quantity: 0 },
    barcodes: { 
      skuBarcode: '', 
      ean13: generateEAN13() // Автоматически генерируем EAN-13
    },
    logistics: {
      weightKg: '',
      dimensionsCm: { length: '', width: '', height: '' },
      delivery: { methods: [], deliveryDays: '' }
    }
  }
  variants.value.push(newVariant)
  
  // Генерируем SKU штрихкод для нового варианта
  if (newVariant.sku && newVariant.sku.trim()) {
    newVariant.barcodes.skuBarcode = generateSKUBarcode(newVariant.sku)
  }
}

const removeVariant = (index) => {
  if (variants.value.length > 1) {
    variants.value.splice(index, 1)
  }
}

const deliveryMethods = ['COURIER', 'PICKUP', 'POST', 'EXPRESS']

const toggleDeliveryMethod = (variant, method) => {
  // Убеждаемся, что methods существует и является массивом
  if (!variant.logistics.delivery.methods) {
    variant.logistics.delivery.methods = []
  }
  if (!Array.isArray(variant.logistics.delivery.methods)) {
    variant.logistics.delivery.methods = []
  }
  
  const idx = variant.logistics.delivery.methods.indexOf(method)
  if (idx > -1) {
    variant.logistics.delivery.methods.splice(idx, 1)
  } else {
    variant.logistics.delivery.methods.push(method)
  }
  
  // Логирование для отладки
  console.log('Методы доставки после изменения:', variant.logistics.delivery.methods)
}

// ========== RETURNS ==========
const returnsAllowed = ref(true)
const returnsDays = ref(14)
const returnsConditions = ref('Товар без следов использования')

// ========== SEO ==========
const seoSlug = ref('')
const seoMetaTitle = ref('')
const seoMetaDescription = ref('')

// ========== STATUS ==========
const status = ref('draft')

// ========== DROPDOWN DATA ==========
const categories = ref([])
const currencies = [
  { title: 'RUB', value: 'RUB' },
  { title: 'USD', value: 'USD' },
  { title: 'EUR', value: 'EUR' }
]

// ========== LOAD DATA ==========
const loadCategories = async () => {
  try {
    const response = await $api('/admin/categories', { method: 'GET' })
    categories.value = response.map(cat => ({
      title: cat.name,
      value: cat.id,
      parentId: cat.parentId
    }))
  } catch (error) {
    console.error('Ошибка при загрузке категорий:', error)
    categories.value = []
  }
}

const loadProductData = async () => {
  if (!productId.value) return
  
  try {
    isFetching.value = true
    isLoadingData.value = true // Устанавливаем флаг загрузки данных
    const response = await $api(`/admin/products/${productId.value}`, { method: 'GET' })
    
    console.log('Product response:', response) // Для отладки
    
    // Основная информация
    productSku.value = response.sku || response.product?.sku || ''
    productBrand.value = response.brand || response.product?.brand || ''
    productTitle.value = response.name || response.product?.title || ''
    categoryId.value = response.category?.id || response.product?.category?.id || null
    parentCategoryId.value = response.category?.parentId || response.product?.category?.parentId || null
    
    // Описание
    if (response.description || response.product?.description) {
      const desc = response.product?.description || {}
      descriptionFull.value = desc.full || response.description || ''
      descriptionShort.value = desc.shortDescription || (response.description ? response.description.substring(0, 200) : '')
      // Benefits теперь возвращаются напрямую в response
      benefits.value = response.benefits && response.benefits.length > 0 
        ? response.benefits 
        : (desc.benefits && desc.benefits.length > 0 ? desc.benefits : [''])
    } else {
      descriptionShort.value = ''
      descriptionFull.value = ''
      // Проверяем, есть ли benefits в ответе напрямую
      benefits.value = response.benefits && response.benefits.length > 0 ? response.benefits : ['']
    }
    
    if (response.characteristics && response.characteristics.length > 0) {
      characteristics.value = response.characteristics.map(c => ({
        key: c.key || '',
        name: c.name || '',
        value: c.value || '',
        filterable: c.filterable !== undefined ? c.filterable : true
      }))
    } else {
      characteristics.value = [{ key: '', name: '', value: '', filterable: true }]
    }
    
    // Обработка изображений
    uploadedImages.value = []
    uploadedGalleryImages.value = []
    galleryImages.value = []
    
    if (response.images && response.images.length > 0) {
      // Используем product_images как единственный источник — он хранит правильный порядок (main, gallery)
      const sortedImages = [...response.images].sort((a, b) => {
        const aPrimary = a.isPrimary ? 0 : 1
        const bPrimary = b.isPrimary ? 0 : 1
        if (aPrimary !== bPrimary) return aPrimary - bPrimary
        return (a.sortOrder || 0) - (b.sortOrder || 0)
      })
      const imageUrls = sortedImages.map(img => img.imageUrl).filter(url => url)
      
      if (imageUrls.length > 0) {
        mainImage.value = imageUrls[0]
        
        // Преобразуем URL в объекты файлов для отображения
        const fileObjects = imageUrls.map((url, index) => createFileObjectFromUrl(url, index))
        
        // Первое изображение - основное
        if (fileObjects[0]) {
          uploadedImages.value = [fileObjects[0]]
        }
        
        // Остальные - в галерею
        if (fileObjects.length > 1) {
          uploadedGalleryImages.value = fileObjects.slice(1)
        }
      }
    } else if (response.imageUrl) {
      mainImage.value = response.imageUrl
      const fileObject = createFileObjectFromUrl(response.imageUrl, 0)
      if (fileObject) {
        uploadedImages.value = [fileObject]
      }
    } else {
      mainImage.value = ''
    }
    
    // Видео
    videoUrl.value = response.videoUrl || response.product?.media?.video || response.media?.video || ''
    
    try {
      const videosResponse = await $api(`/admin/files/product/${productId.value}/videos`)
      if (videosResponse && videosResponse.length > 0) {
        uploadedVideos.value = videosResponse.slice(0, 1)
        if (uploadedVideos.value[0]?.url) {
          videoUrl.value = uploadedVideos.value[0].url
        }
      }
    } catch (error) {
      console.warn('Не удалось загрузить видео из MinIO:', error)
    }
    
    // Варианты
    if (response.variants && response.variants.length > 0) {
      console.log('Загрузка вариантов из ответа:', response.variants)
      variants.value = response.variants.map(v => {
        // Обрабатываем атрибуты: если пустой объект или null, используем дефолтные значения
        let attributes = v.attributes || {}
        if (!attributes.color && !attributes.size && Object.keys(attributes).length === 0) {
          attributes = { color: '', size: '' }
        } else {
          // Убеждаемся, что есть поля color и size
          attributes = {
            color: attributes.color || '',
            size: attributes.size || '',
            ...attributes // Сохраняем другие атрибуты, если есть
          }
        }
        
        return {
        variantId: v.variantId || v.id?.toString() || '',
        sku: v.sku || '',
        attributes: attributes,
        price: {
          base: v.price?.base ? v.price.base.toString() : (v.price ? v.price.toString() : ''),
          sale: v.price?.sale ? v.price.sale.toString() : '',
          currency: v.price?.currency || 'RUB',
          vat: v.price?.vat || 20
        },
        stock: {
          quantity: v.stock?.quantity || v.stockQuantity || 0
        },
        barcodes: {
          skuBarcode: v.barcodes?.skuBarcode || (v.sku ? generateSKUBarcode(v.sku) : ''),
          ean13: v.barcodes?.ean13 || generateEAN13()
        },
        logistics: {
          weightKg: (() => {
            const val = v.logistics?.weightKg
            if (val === null || val === undefined) return ''
            return val.toString()
          })(),
          dimensionsCm: {
            length: (() => {
              const val = v.logistics?.dimensionsCm?.length
              if (val === null || val === undefined) return ''
              return val.toString()
            })(),
            width: (() => {
              const val = v.logistics?.dimensionsCm?.width
              if (val === null || val === undefined) return ''
              return val.toString()
            })(),
            height: (() => {
              const val = v.logistics?.dimensionsCm?.height
              if (val === null || val === undefined) return ''
              return val.toString()
            })()
          },
          delivery: {
            methods: (() => {
              const methods = v.logistics?.delivery?.methods
              console.log('Загрузка методов доставки для варианта:', v.sku, methods)
              if (!methods) return []
              if (!Array.isArray(methods)) {
                console.warn('Методы доставки не являются массивом:', methods)
                return []
              }
              // Фильтруем пустые значения и возвращаем массив
              const filtered = methods.filter(m => m && m.trim())
              console.log('Отфильтрованные методы доставки:', filtered)
              return filtered
            })(),
            deliveryDays: v.logistics?.delivery?.deliveryDays || ''
          }
        }
        }
      })
      console.log('Загруженные варианты с методами доставки:', variants.value.map(v => ({
        sku: v.sku,
        deliveryMethods: v.logistics.delivery.methods
      })))
    } else {
      // Если вариантов нет, создаем один на основе основных данных продукта
      const baseSku = response.sku || productSku.value || ''
      const attributes = { color: '', size: '' }
      const variantSku = baseSku ? generateVariantSku(baseSku, attributes, 0) : generateVariantSku('', attributes, 0)
      variants.value = [{
        variantId: generateVariantId(0),
        sku: variantSku,
        attributes: attributes,
        price: {
          base: response.price ? response.price.toString() : '',
          sale: response.oldPrice ? response.oldPrice.toString() : '',
          currency: 'RUB',
          vat: 20
        },
        stock: {
          quantity: response.stockQuantity || 0
        },
        barcodes: {
          skuBarcode: variantSku ? generateSKUBarcode(variantSku) : '',
          ean13: generateEAN13()
        },
        logistics: {
          weightKg: '',
          dimensionsCm: { length: '', width: '', height: '' },
          delivery: { methods: [], deliveryDays: '' }
        }
      }]
    }
    
    // После загрузки данных генерируем SKU и ID для вариантов, если их нет
    variants.value.forEach((variant, index) => {
      // Генерируем ID варианта, если его нет
      if (!variant.variantId || variant.variantId.trim() === '') {
        variant.variantId = generateVariantId(index)
      }
      // Генерируем SKU варианта, если его нет
      if (!variant.sku || variant.sku.trim() === '') {
        variant.sku = generateVariantSku(productSku.value || response.sku || '', variant.attributes, index)
      }
      // Генерируем EAN-13, если его нет
      if (!variant.barcodes.ean13 || variant.barcodes.ean13.trim() === '') {
        variant.barcodes.ean13 = generateEAN13()
      }
      // Генерируем SKU штрихкод, если есть SKU, но нет штрихкода
      if (variant.sku && variant.sku.trim() && (!variant.barcodes.skuBarcode || variant.barcodes.skuBarcode.trim() === '')) {
        variant.barcodes.skuBarcode = generateSKUBarcode(variant.sku)
      }
    })
    
    // Returns
    if (response.returns) {
      returnsAllowed.value = response.returns.allowed !== undefined ? response.returns.allowed : true
      returnsDays.value = response.returns.days || 14
      returnsConditions.value = response.returns.conditions || ''
    } else {
      returnsAllowed.value = true
      returnsDays.value = 14
      returnsConditions.value = ''
    }
    
    // SEO
    if (response.seo) {
      seoSlug.value = response.seo.slug || ''
      seoMetaTitle.value = response.seo.metaTitle || ''
      seoMetaDescription.value = response.seo.metaDescription || ''
    } else {
      seoSlug.value = ''
      seoMetaTitle.value = ''
      seoMetaDescription.value = ''
    }
    
    status.value = response.status || (response.isActive ? 'published' : 'draft')
    
    // После загрузки данных генерируем SKU, ID и штрихкоды для вариантов, если их нет
    variants.value.forEach((variant, index) => {
      // Генерируем ID варианта, если его нет
      if (!variant.variantId || variant.variantId.trim() === '') {
        variant.variantId = generateVariantId(index)
      }
      // Генерируем SKU варианта, если его нет
      if (!variant.sku || variant.sku.trim() === '') {
        variant.sku = generateVariantSku(productSku.value || response.sku || '', variant.attributes, index)
      }
      // Генерируем EAN-13, если его нет
      if (!variant.barcodes.ean13 || variant.barcodes.ean13.trim() === '') {
        variant.barcodes.ean13 = generateEAN13()
      }
      // Генерируем SKU штрихкод, если есть SKU, но нет штрихкода
      if (variant.sku && variant.sku.trim() && (!variant.barcodes.skuBarcode || variant.barcodes.skuBarcode.trim() === '')) {
        variant.barcodes.skuBarcode = generateSKUBarcode(variant.sku)
      }
    })
  } catch (error) {
    console.error('Ошибка при загрузке данных продукта:', error)
    alert('Ошибка при загрузке данных продукта: ' + (error.data?.message || error.message || 'Неизвестная ошибка'))
    isEditMode.value = false
    productId.value = null
  } finally {
    isFetching.value = false
    isLoadingData.value = false // Снимаем флаг загрузки данных
  }
}

// ========== AUTO GENERATE SLUG ==========
const generateSlug = () => {
  if (productTitle.value && !seoSlug.value) {
    seoSlug.value = productTitle.value
      .toLowerCase()
      .replace(/[^a-zа-я0-9\s-]/g, '')
      .replace(/\s+/g, '-')
      .replace(/-+/g, '-')
      .trim()
  }
}

// ========== SUBMIT ==========
const buildProductRequest = () => {
  // Собираем все URL изображений из загруженных файлов и URL полей
  const uploadedUrls = [
    ...uploadedImages.value.map(f => f.url).filter(url => url),
    ...uploadedGalleryImages.value.map(f => f.url).filter(url => url)
  ]
  
  const manualUrls = galleryImages.value.filter(img => img && img.trim())
  
  // Объединяем все URL, убираем дубликаты
  const allGalleryImages = [...new Set([...uploadedUrls, ...manualUrls])]
  
  // Определяем основное изображение
  const mainImageUrl = mainImage.value || 
    (uploadedImages.value.length > 0 ? uploadedImages.value[0].url : null) ||
    (uploadedGalleryImages.value.length > 0 ? uploadedGalleryImages.value[0].url : null) ||
    (allGalleryImages.length > 0 ? allGalleryImages[0] : null)

  // Определяем категорию: если выбрана категория - используем её, если только родительская - используем родительскую
  let categoryIdToSend = categoryId.value || null
  let parentIdToSend = null
  
  if (!categoryIdToSend && parentCategoryId.value) {
    // Если выбрана только родительская категория, используем её как категорию товара
    categoryIdToSend = parentCategoryId.value
    parentIdToSend = null
  } else if (categoryIdToSend) {
    // Если выбрана категория, parentId не нужен (он уже есть в самой категории)
    parentIdToSend = null
  }
  
  const requestData = {
    product: {
      sku: productSku.value?.trim() || null,
      brand: productBrand.value?.trim() || null,
      title: productTitle.value,
      category: {
        id: categoryIdToSend,
        parentId: parentIdToSend
      },
      description: {
        shortDescription: descriptionShort.value || null,
        full: descriptionFull.value || null,
        benefits: benefits.value.filter(b => b.trim())
      },
      characteristics: characteristics.value
        .filter(c => c.key && c.name && c.value)
        .map(c => ({
          key: c.key,
          name: c.name,
          value: c.value,
          filterable: c.filterable
        })),
      media: {
        mainImage: mainImageUrl,
        gallery: allGalleryImages.filter(url => url !== mainImageUrl), // Убираем основное из галереи
        video: videoUrl.value || null
      }
    },
    variants: variants.value.map(v => ({
      variantId: v.variantId || null,
      sku: v.sku || null,
      attributes: v.attributes || {},
      price: {
        base: (v.price?.base && v.price.base.toString().trim()) ? parseFloat(v.price.base) : 0,
        sale: (v.price?.sale && v.price.sale.toString().trim()) ? parseFloat(v.price.sale) : null,
        currency: v.price?.currency || 'RUB',
        vat: v.price?.vat || 20
      },
      stock: {
        quantity: (v.stock?.quantity && v.stock.quantity.toString().trim()) ? parseInt(v.stock.quantity) : 0
      },
      barcodes: {
        skuBarcode: (v.barcodes?.skuBarcode && v.barcodes.skuBarcode.trim()) ? v.barcodes.skuBarcode : null,
        ean13: (v.barcodes?.ean13 && v.barcodes.ean13.trim()) ? v.barcodes.ean13 : null
      },
      logistics: {
        weightKg: (() => {
          const val = v.logistics?.weightKg
          if (!val) return null
          const str = val.toString().trim()
          if (!str || str === '0' || str === '0.0') return null
          const num = parseFloat(str)
          return isNaN(num) ? null : num
        })(),
        dimensionsCm: {
          length: (() => {
            const val = v.logistics?.dimensionsCm?.length
            if (!val) return null
            const str = val.toString().trim()
            if (!str || str === '0' || str === '0.0') return null
            const num = parseFloat(str)
            return isNaN(num) ? null : num
          })(),
          width: (() => {
            const val = v.logistics?.dimensionsCm?.width
            if (!val) return null
            const str = val.toString().trim()
            if (!str || str === '0' || str === '0.0') return null
            const num = parseFloat(str)
            return isNaN(num) ? null : num
          })(),
          height: (() => {
            const val = v.logistics?.dimensionsCm?.height
            if (!val) return null
            const str = val.toString().trim()
            if (!str || str === '0' || str === '0.0') return null
            const num = parseFloat(str)
            return isNaN(num) ? null : num
          })()
        },
        delivery: {
          methods: (() => {
            const methods = v.logistics?.delivery?.methods
            if (!methods || !Array.isArray(methods)) return []
            // Фильтруем пустые значения и возвращаем массив
            return methods.filter(m => m && m.trim())
          })(),
          deliveryDays: (v.logistics?.delivery?.deliveryDays && v.logistics.delivery.deliveryDays.trim()) 
            ? v.logistics.delivery.deliveryDays.trim() 
            : null
        }
      }
    })),
    returns: {
      allowed: returnsAllowed.value,
      days: returnsDays.value || 14,
      conditions: returnsConditions.value || null
    },
    seo: {
      slug: seoSlug.value || null,
      metaTitle: seoMetaTitle.value || null,
      metaDescription: seoMetaDescription.value || null
    },
    status: status.value
  }
  
  console.log('Product request data:', JSON.stringify(requestData, null, 2)) // Для отладки
  console.log('Логистика вариантов:', requestData.variants.map(v => ({
    sku: v.sku,
    logistics: v.logistics
  })))
  console.log('Методы доставки вариантов:', requestData.variants.map(v => ({
    sku: v.sku,
    deliveryMethods: v.logistics?.delivery?.methods
  })))
  return requestData
}

const publishProduct = async () => {
  if (!productTitle.value || !variants.value[0]?.price?.base) {
    alert('Пожалуйста, заполните обязательные поля: Название товара и базовая цена варианта')
    return
  }

  try {
    isFetching.value = true
    const productData = buildProductRequest()
    productData.status = 'published'

    let response
    if (isEditMode.value && productId.value) {
      response = await $api(`/admin/products/${productId.value}`, {
        method: 'PUT',
        body: productData
      })
    } else {
      response = await $api('/admin/products', {
        method: 'POST',
        body: productData
      })
    }
    
    router.push('/apps/ecommerce/product/list')
  } catch (error) {
    console.error('Ошибка при создании товара:', error)
    alert('Ошибка при создании товара: ' + (error.data?.message || error.message || 'Неизвестная ошибка'))
  } finally {
    isFetching.value = false
  }
}

const saveDraft = async () => {
  if (!productTitle.value) {
    alert('Пожалуйста, заполните название товара')
    return
  }

  try {
    isFetching.value = true
    const productData = buildProductRequest()
    productData.status = 'draft'

    let response
    if (isEditMode.value && productId.value) {
      response = await $api(`/admin/products/${productId.value}`, {
        method: 'PUT',
        body: productData
      })
      alert('Черновик обновлен успешно!')
    } else {
      response = await $api('/admin/products', {
        method: 'POST',
        body: productData
      })
      alert('Черновик сохранен успешно!')
    }
  } catch (error) {
    console.error('Ошибка при сохранении черновика:', error)
    alert('Ошибка при сохранении черновика: ' + (error.data?.message || error.message || 'Неизвестная ошибка'))
  } finally {
    isFetching.value = false
  }
}

const cancel = () => {
  router.push('/apps/ecommerce/product/list')
}

// Флаг для предотвращения генерации штрихкодов при загрузке данных
const isLoadingData = ref(false)

// Автоматическая генерация SKU варианта при изменении основного SKU товара
watch(() => productSku.value, (newSku, oldSku) => {
  if (isLoadingData.value || !oldSku) return // Пропускаем при загрузке данных и первую инициализацию
  
  variants.value.forEach((variant, index) => {
    // Генерируем SKU варианта только если он пустой или был сгенерирован автоматически
    if (!variant.sku || variant.sku.trim() === '' || variant.sku.startsWith('VARIANT-')) {
      variant.sku = generateVariantSku(newSku, variant.attributes, index)
      // Генерируем SKU штрихкод
      if (variant.sku && variant.sku.trim()) {
        variant.barcodes.skuBarcode = generateSKUBarcode(variant.sku)
      }
    }
  })
})

// Автоматическая генерация SKU варианта при изменении атрибутов (цвет, размер)
watch(() => variants.value.map(v => ({ color: v.attributes?.color || '', size: v.attributes?.size || '' })), (newAttrs, oldAttrs) => {
  if (isLoadingData.value || !oldAttrs) return // Пропускаем при загрузке данных и первую инициализацию
  
  newAttrs.forEach((attrs, index) => {
    const variant = variants.value[index]
    if (variant) {
      const oldAttrs = oldAttrs[index]
      // Если изменились атрибуты, генерируем новый SKU
      if (oldAttrs && (oldAttrs.color !== attrs.color || oldAttrs.size !== attrs.size)) {
        variant.sku = generateVariantSku(productSku.value, variant.attributes, index)
        // Генерируем SKU штрихкод
        if (variant.sku && variant.sku.trim()) {
          variant.barcodes.skuBarcode = generateSKUBarcode(variant.sku)
        }
      }
    }
  })
}, { deep: true })

// Автоматическая генерация штрихкодов при изменении SKU (только если данные не загружаются)
watch(() => variants.value.map(v => v.sku), (newSkus, oldSkus) => {
  if (isLoadingData.value || !oldSkus) return // Пропускаем при загрузке данных и первую инициализацию
  
  newSkus.forEach((sku, index) => {
    const variant = variants.value[index]
    if (variant && sku && sku.trim()) {
      // Генерируем SKU штрихкод только если он пустой или был изменен SKU
      const oldSku = oldSkus[index]
      if ((!variant.barcodes.skuBarcode || variant.barcodes.skuBarcode.trim() === '') || 
          (oldSku !== sku && sku.trim())) {
        variant.barcodes.skuBarcode = generateSKUBarcode(sku)
      }
    }
  })
}, { deep: true })

// Автоматическая генерация EAN-13 для новых вариантов
watch(() => variants.value.length, (newLength, oldLength) => {
  if (isLoadingData.value) return // Пропускаем при загрузке данных
  
  // Если добавлен новый вариант, генерируем EAN-13 для него
  if (newLength > oldLength && oldLength !== undefined) {
    const newVariant = variants.value[newLength - 1]
    if (newVariant && (!newVariant.barcodes.ean13 || newVariant.barcodes.ean13.trim() === '')) {
      newVariant.barcodes.ean13 = generateEAN13()
    }
    // Если есть SKU, генерируем и SKU штрихкод
    if (newVariant.sku && newVariant.sku.trim() && 
        (!newVariant.barcodes.skuBarcode || newVariant.barcodes.skuBarcode.trim() === '')) {
      newVariant.barcodes.skuBarcode = generateSKUBarcode(newVariant.sku)
    }
  }
})

onMounted(() => {
  console.log('Product Add page mounted')
  // Загружаем категории без await, чтобы не блокировать рендеринг
  loadCategories().catch(err => {
    console.error('Ошибка при загрузке категорий:', err)
  })
  
  // Проверяем ID продукта
  const id = route.params.id || route.query.id
  if (id) {
    productId.value = parseInt(id)
    isEditMode.value = true
    // Загружаем данные продукта без await
    loadProductData().catch(err => {
      console.error('Ошибка при загрузке данных продукта:', err)
    })
  } else {
    isEditMode.value = false
    productId.value = null
  }
})

definePage({ meta: { navActiveLink: 'apps-ecommerce-product' } })
</script>

<template>
  <div>
    <!-- Debug info -->
    <div v-if="false" class="pa-2 mb-2 bg-info text-white">
      DEBUG: Страница загружена. Route: {{ route.path }}, Name: {{ route.name }}
    </div>
    
    <!-- Header -->
    <div class="d-flex flex-wrap justify-start justify-sm-space-between gap-y-4 gap-x-6 mb-6">
      <div class="d-flex flex-column justify-center">
        <h4 class="text-h4 font-weight-medium">
          {{ isEditMode ? 'Редактировать товар' : 'Добавить новый товар' }}
        </h4>
        <div class="text-body-1">
          {{ isEditMode ? 'Редактирование товара с полной информацией' : 'Создание товара с полной информацией' }}
        </div>
      </div>

      <div class="d-flex gap-4 align-center flex-wrap">
        <VBtn
          variant="tonal"
          color="secondary"
          @click="cancel"
        >
          Отменить
        </VBtn>
        <VBtn
          variant="tonal"
          color="primary"
          :loading="isFetching"
          @click="saveDraft"
        >
          Сохранить черновик
        </VBtn>
        <VBtn
          :loading="isFetching"
          @click="publishProduct"
        >
          Опубликовать товар
        </VBtn>
      </div>
    </div>

    <!-- Tabs Navigation -->
    <VCard class="mb-6">
      <VTabs
        v-model="activeTab"
        class="px-4"
      >
        <VTab value="basic">
          Основная информация
        </VTab>
        <VTab value="description">
          Описание
        </VTab>
        <VTab value="variants">
          Варианты
        </VTab>
        <VTab value="media">
          Медиа
        </VTab>
        <VTab value="seo">
          SEO
        </VTab>
      </VTabs>
    </VCard>

    <!-- Content -->
    <VRow>
      <VCol cols="12">
        <!-- Basic Information Tab -->
        <VCard
          v-show="activeTab === 'basic'"
          title="Основная информация"
          class="mb-6"
        >
          <VCardText>
            <VRow>
              <VCol cols="12">
                <h6 class="text-h6 mb-4">
                  Информация о товаре
                </h6>
              </VCol>
              <VCol cols="12">
                <AppTextField
                  v-model="productTitle"
                  label="Название товара"
                  placeholder="Футболка мужская хлопковая oversize"
                  required
                  @blur="generateSlug"
                />
              </VCol>
              <VCol
                cols="12"
                md="6"
              >
                <AppTextField
                  v-model="productSku"
                  label="SKU"
                  placeholder="TSHIRT-001"
                />
              </VCol>
              <VCol
                cols="12"
                md="6"
              >
                <AppTextField
                  v-model="productBrand"
                  label="Бренд"
                  placeholder="MyBrand"
                />
              </VCol>
              <VCol
                cols="12"
                md="6"
              >
                <AppSelect
                  v-model="categoryId"
                  label="Категория"
                  placeholder="Выберите категорию"
                  :items="categories"
                />
              </VCol>
              <VCol
                cols="12"
                md="6"
              >
                <AppSelect
                  v-model="parentCategoryId"
                  label="Родительская категория"
                  placeholder="Выберите родительскую категорию"
                  :items="categories.filter(c => c.value !== categoryId)"
                />
              </VCol>
            </VRow>
          </VCardText>
        </VCard>

        <!-- Description Tab -->
        <VCard
          v-show="activeTab === 'description'"
          title="Описание товара"
          class="mb-6"
        >
          <VCardText>
            <VRow>
              <VCol cols="12">
                <AppTextField
                  v-model="descriptionShort"
                  label="Краткое описание"
                  placeholder="Мужская футболка из 100% хлопка."
                  rows="2"
                />
              </VCol>
              <VCol cols="12">
                <span class="mb-1 d-block">Полное описание</span>
                <AppTextarea
                  v-model="descriptionFull"
                  placeholder="Подробное описание товара"
                  rows="8"
                  class="border rounded"
                />
              </VCol>
              <VCol cols="12">
                <VDivider class="my-4" />
                <div class="d-flex justify-space-between align-center mb-4">
                  <h6 class="text-h6 mb-0">
                    Преимущества товара
                  </h6>
                  <VBtn
                    size="small"
                    prepend-icon="tabler-plus"
                    @click="addBenefit"
                  >
                    Добавить
                  </VBtn>
                </div>
                <div
                  v-for="(benefit, index) in benefits"
                  :key="index"
                  class="d-flex gap-2 mb-2"
                >
                  <AppTextField
                    v-model="benefits[index]"
                    :placeholder="`Преимущество ${index + 1}`"
                    class="flex-grow-1"
                  />
                  <VBtn
                    v-if="benefits.length > 1"
                    icon="tabler-x"
                    variant="text"
                    size="small"
                    @click="removeBenefit(index)"
                  />
                </div>
              </VCol>
              <VCol cols="12">
                <VDivider class="my-4" />
                <div class="d-flex justify-space-between align-center mb-4">
                  <h6 class="text-h6 mb-0">
                    Характеристики
                  </h6>
                  <VBtn
                    size="small"
                    prepend-icon="tabler-plus"
                    @click="addCharacteristic"
                  >
                    Добавить
                  </VBtn>
                </div>
                <div
                  v-for="(char, index) in characteristics"
                  :key="index"
                  class="mb-4 pa-4 border rounded"
                >
                  <VRow>
                    <VCol
                      cols="12"
                      md="3"
                    >
                      <AppTextField
                        v-model="char.key"
                        label="Ключ"
                        placeholder="material"
                      />
                    </VCol>
                    <VCol
                      cols="12"
                      md="3"
                    >
                      <AppTextField
                        v-model="char.name"
                        label="Название"
                        placeholder="Материал"
                      />
                    </VCol>
                    <VCol
                      cols="12"
                      md="4"
                    >
                      <AppTextField
                        v-model="char.value"
                        label="Значение"
                        placeholder="Хлопок"
                      />
                    </VCol>
                    <VCol
                      cols="12"
                      md="1"
                      class="d-flex align-center"
                    >
                      <VCheckbox
                        v-model="char.filterable"
                        label="Фильтруемый"
                      />
                    </VCol>
                    <VCol
                      cols="12"
                      class="d-flex justify-end"
                    >
                      <VBtn
                        v-if="characteristics.length > 1"
                        icon="tabler-x"
                        variant="text"
                        size="small"
                        @click="removeCharacteristic(index)"
                      >
                        Удалить
                      </VBtn>
                    </VCol>
                  </VRow>
                </div>
              </VCol>
            </VRow>
          </VCardText>
        </VCard>

        <!-- Variants Tab -->
        <VCard
          v-show="activeTab === 'variants'"
          title="Варианты товара"
          class="mb-6"
        >
          <VCardText>
            <div
              v-for="(variant, index) in variants"
              :key="index"
              class="mb-6 pa-4 border rounded"
            >
              <div class="d-flex justify-space-between align-center mb-4">
                <h6 class="text-h6 mb-0">
                  Вариант {{ index + 1 }}
                </h6>
                <VBtn
                  v-if="variants.length > 1"
                  icon="tabler-x"
                  variant="text"
                  size="small"
                  @click="removeVariant(index)"
                >
                  Удалить вариант
                </VBtn>
              </div>

              <VRow>
                <VCol
                  cols="12"
                  md="6"
                >
                  <AppTextField
                    v-model="variant.sku"
                    label="SKU варианта"
                    placeholder="TSHIRT-001-BLK-M"
                    @blur="() => {
                      if (variant.sku && variant.sku.trim() && (!variant.barcodes.skuBarcode || variant.barcodes.skuBarcode.trim() === '')) {
                        variant.barcodes.skuBarcode = generateSKUBarcode(variant.sku)
                      }
                    }"
                  />
                </VCol>
                <VCol
                  cols="12"
                  md="6"
                >
                  <AppTextField
                    v-model="variant.variantId"
                    label="ID варианта"
                    placeholder="var_001"
                  />
                </VCol>

                <VCol cols="12">
                  <h6 class="text-subtitle-1 mb-2">
                    Атрибуты варианта
                  </h6>
                </VCol>
                <VCol
                  cols="12"
                  md="6"
                >
                  <AppTextField
                    v-model="variant.attributes.color"
                    label="Цвет"
                    placeholder="Черный"
                  />
                </VCol>
                <VCol
                  cols="12"
                  md="6"
                >
                  <AppTextField
                    v-model="variant.attributes.size"
                    label="Размер"
                    placeholder="M"
                  />
                </VCol>

                <VCol cols="12">
                  <VDivider class="my-4" />
                  <h6 class="text-subtitle-1 mb-2">
                    Цена
                  </h6>
                </VCol>
                <VCol
                  cols="12"
                  md="3"
                >
                  <AppTextField
                    v-model="variant.price.base"
                    label="Базовая цена"
                    placeholder="1990"
                    type="number"
                    required
                  />
                </VCol>
                <VCol
                  cols="12"
                  md="3"
                >
                  <AppTextField
                    v-model="variant.price.sale"
                    label="Цена со скидкой"
                    placeholder="1490"
                    type="number"
                  />
                </VCol>
                <VCol
                  cols="12"
                  md="3"
                >
                  <AppSelect
                    v-model="variant.price.currency"
                    label="Валюта"
                    :items="currencies"
                  />
                </VCol>
                <VCol
                  cols="12"
                  md="3"
                >
                  <AppTextField
                    v-model="variant.price.vat"
                    label="НДС (%)"
                    placeholder="20"
                    type="number"
                  />
                </VCol>

                <VCol cols="12">
                  <VDivider class="my-4" />
                  <h6 class="text-subtitle-1 mb-2">
                    Склад
                  </h6>
                </VCol>
                <VCol
                  cols="12"
                  md="6"
                >
                  <AppTextField
                    v-model="variant.stock.quantity"
                    label="Количество на складе"
                    placeholder="24"
                    type="number"
                  />
                </VCol>

                <VCol cols="12">
                  <VDivider class="my-4" />
                  <h6 class="text-subtitle-1 mb-2">
                    Штрихкоды
                  </h6>
                </VCol>
                <VCol
                  cols="12"
                  md="6"
                >
                  <AppTextField
                    v-model="variant.barcodes.skuBarcode"
                    label="SKU штрихкод"
                    placeholder="4601234567890"
                  />
                </VCol>
                <VCol
                  cols="12"
                  md="6"
                >
                  <AppTextField
                    v-model="variant.barcodes.ean13"
                    label="EAN-13"
                    placeholder="4601234567890"
                  />
                </VCol>

                <VCol cols="12">
                  <VDivider class="my-4" />
                  <h6 class="text-subtitle-1 mb-2">
                    Логистика
                  </h6>
                </VCol>
                <VCol
                  cols="12"
                  md="4"
                >
                  <AppTextField
                    v-model="variant.logistics.weightKg"
                    label="Вес (кг)"
                    placeholder="0.35"
                    type="number"
                    step="0.01"
                  />
                </VCol>
                <VCol
                  cols="12"
                  md="2"
                >
                  <AppTextField
                    v-model="variant.logistics.dimensionsCm.length"
                    label="Длина (см)"
                    placeholder="30"
                    type="number"
                  />
                </VCol>
                <VCol
                  cols="12"
                  md="2"
                >
                  <AppTextField
                    v-model="variant.logistics.dimensionsCm.width"
                    label="Ширина (см)"
                    placeholder="25"
                    type="number"
                  />
                </VCol>
                <VCol
                  cols="12"
                  md="2"
                >
                  <AppTextField
                    v-model="variant.logistics.dimensionsCm.height"
                    label="Высота (см)"
                    placeholder="3"
                    type="number"
                  />
                </VCol>
                <VCol
                  cols="12"
                  md="2"
                >
                  <AppTextField
                    v-model="variant.logistics.delivery.deliveryDays"
                    label="Дни доставки"
                    placeholder="2-5"
                  />
                </VCol>
                <VCol cols="12">
                  <VLabel class="mb-2">
                    Методы доставки
                  </VLabel>
                  <div class="d-flex flex-wrap gap-2">
                    <VChip
                      v-for="method in deliveryMethods"
                      :key="method"
                      :color="variant.logistics.delivery.methods && variant.logistics.delivery.methods.includes(method) ? 'primary' : 'default'"
                      variant="outlined"
                      clickable
                      @click="toggleDeliveryMethod(variant, method)"
                    >
                      {{ method }}
                    </VChip>
                  </div>
                </VCol>
              </VRow>
            </div>

            <VBtn
              prepend-icon="tabler-plus"
              @click="addVariant"
            >
              Добавить вариант
            </VBtn>
          </VCardText>
        </VCard>

        <!-- Media Tab -->
        <VCard
          v-show="activeTab === 'media'"
          title="Медиа файлы"
          class="mb-6"
        >
          <VCardText>
            <VRow>
              <VCol cols="12">
                <h6 class="text-h6 mb-4">
                  Основное изображение
                </h6>
                <FileUploader
                  v-model="uploadedImages"
                  :product-id="productId"
                  file-type="IMAGE"
                  :multiple="false"
                :max-size="10485760"
                  @uploaded="handleImagesUploaded"
                  @deleted="handleImageDeleted"
                />
                <p class="text-caption text-medium-emphasis mt-2">
                  Первое загруженное изображение станет основным
                </p>
              </VCol>

              <VCol cols="12">
                <VDivider class="my-4" />
                <h6 class="text-h6 mb-4">
                  Галерея изображений
                </h6>
                
                <FileUploader
                  v-model="uploadedGalleryImages"
                  :product-id="productId"
                  file-type="IMAGE"
                  :multiple="true"
                  :max-size="10485760"
                  :max-files="4"
                  @uploaded="handleGalleryImagesUploaded"
                  @deleted="handleGalleryImageDeleted"
                />
                <p class="text-caption text-medium-emphasis mt-2">
                  Максимум 4 фото. Перетащите фото для изменения порядка - первое фото станет главным
                </p>
              </VCol>

              <VCol cols="12">
                <VDivider class="my-4" />
                <VExpansionPanels variant="accordion">
                  <VExpansionPanel>
                    <VExpansionPanelTitle>
                      <h6 class="text-h6 mb-0">
                        Указать URL вручную (опционально)
                      </h6>
                    </VExpansionPanelTitle>
                    <VExpansionPanelText>
                      <VRow>
                        <VCol cols="12">
                          <AppTextField
                            v-model="mainImage"
                            label="Основное изображение (URL)"
                            placeholder="Будет установлено автоматически при загрузке или укажите URL вручную"
                            hint="Можно указать URL вручную или загрузить файл выше"
                            persistent-hint
                          />
                        </VCol>
                        <VCol cols="12">
                          <div class="d-flex justify-space-between align-center mb-4">
                            <h6 class="text-h6 mb-0">
                              URL галереи изображений
                            </h6>
                            <VBtn
                              size="small"
                              prepend-icon="tabler-plus"
                              @click="addGalleryImage"
                            >
                              Добавить URL
                            </VBtn>
                          </div>
                          <div
                            v-for="(img, index) in galleryImages"
                            :key="index"
                            class="d-flex gap-2 mb-2"
                          >
                            <AppTextField
                              v-model="galleryImages[index]"
                              :placeholder="`URL изображения ${index + 1}`"
                              class="flex-grow-1"
                            />
                            <VBtn
                              v-if="galleryImages.length > 0"
                              icon="tabler-x"
                              variant="text"
                              size="small"
                              @click="removeGalleryImage(index)"
                            />
                          </div>
                        </VCol>
                      </VRow>
                    </VExpansionPanelText>
                  </VExpansionPanel>
                </VExpansionPanels>
              </VCol>

              <VCol cols="12">
                <VDivider class="my-4" />
                <h6 class="text-h6 mb-4">
                  Видео товара (опционально)
                </h6>
                <FileUploader
                  v-model="uploadedVideos"
                  :product-id="productId"
                  file-type="VIDEO"
                  :multiple="false"
                  :max-size="104857600"
                  @uploaded="handleVideoUploaded"
                  @deleted="handleVideoDeleted"
                />
                <AppTextField
                  v-model="videoUrl"
                  label="Видео (URL)"
                  placeholder="https://cdn.example.com/product.mp4"
                  class="mt-4"
                />
              </VCol>
            </VRow>
          </VCardText>
        </VCard>

        <!-- SEO Tab -->
        <VCard
          v-show="activeTab === 'seo'"
          title="SEO настройки"
          class="mb-6"
        >
          <VCardText>
            <VRow>
              <VCol cols="12">
                <AppTextField
                  v-model="seoSlug"
                  label="URL slug"
                  placeholder="futbolka-muzhskaya-hlopkovaya-oversize"
                  hint="Автоматически генерируется из названия товара"
                />
              </VCol>
              <VCol cols="12">
                <AppTextField
                  v-model="seoMetaTitle"
                  label="Meta Title"
                  placeholder="Футболка мужская oversize купить"
                />
              </VCol>
              <VCol cols="12">
                <AppTextField
                  v-model="seoMetaDescription"
                  label="Meta Description"
                  placeholder="Хлопковая мужская футболка с доставкой по России"
                  rows="3"
                />
              </VCol>
              <VCol cols="12">
                <VDivider class="my-4" />
                <h6 class="text-h6 mb-4">
                  Возвраты
                </h6>
                <VCheckbox
                  v-model="returnsAllowed"
                  label="Возврат разрешен"
                />
                <VCol
                  cols="12"
                  md="6"
                  class="mt-4"
                >
                  <AppTextField
                    v-model="returnsDays"
                    label="Срок возврата (дней)"
                    placeholder="14"
                    type="number"
                  />
                </VCol>
                <VCol cols="12">
                  <AppTextField
                    v-model="returnsConditions"
                    label="Условия возврата"
                    placeholder="Товар без следов использования"
                    rows="3"
                  />
                </VCol>
              </VCol>
            </VRow>
          </VCardText>
        </VCard>
      </VCol>
    </VRow>
  </div>
</template>

<style lang="scss" scoped>
.border {
  border: 1px solid rgba(var(--v-theme-on-surface), 0.12);
  border-radius: 6px;
}
</style>
