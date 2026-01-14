<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { $api } from '@/utils/api'

const router = useRouter()
const route = useRoute()
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
const mainImage = ref('')
const galleryImages = ref([''])
const videoUrl = ref('')
const addGalleryImage = () => galleryImages.value.push('')
const removeGalleryImage = (index) => {
  if (galleryImages.value.length > 1) {
    galleryImages.value.splice(index, 1)
  }
}

// ========== VARIANTS ==========
const variants = ref([
  {
    variantId: '',
    sku: '',
    attributes: { color: '', size: '' },
    price: { base: '', sale: '', currency: 'RUB', vat: 20 },
    stock: { quantity: 0 },
    barcodes: { skuBarcode: '', ean13: '' },
    logistics: {
      weightKg: '',
      dimensionsCm: { length: '', width: '', height: '' },
      delivery: { methods: [], deliveryDays: '' }
    }
  }
])

const addVariant = () => {
  variants.value.push({
    variantId: '',
    sku: '',
    attributes: { color: '', size: '' },
    price: { base: '', sale: '', currency: 'RUB', vat: 20 },
    stock: { quantity: 0 },
    barcodes: { skuBarcode: '', ean13: '' },
    logistics: {
      weightKg: '',
      dimensionsCm: { length: '', width: '', height: '' },
      delivery: { methods: [], deliveryDays: '' }
    }
  })
}

const removeVariant = (index) => {
  if (variants.value.length > 1) {
    variants.value.splice(index, 1)
  }
}

const deliveryMethods = ['COURIER', 'PICKUP', 'POST', 'EXPRESS']

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
  }
}

// Загрузка данных продукта для редактирования
const loadProductData = async () => {
  if (!productId.value) {
    console.warn('Не указан ID продукта для загрузки')
    return
  }
  
  try {
    isFetching.value = true
    console.log('Загрузка данных продукта с ID:', productId.value)
    
    const response = await $api(`/admin/products/${productId.value}`, {
      method: 'GET',
    })
    
    console.log('Данные продукта загружены:', response)
    
    // Заполняем форму данными продукта
    productSku.value = response.sku || ''
    productBrand.value = response.brand || ''
    productTitle.value = response.name || ''
    categoryId.value = response.category?.id || null
    parentCategoryId.value = response.category?.parentId || null
    
    if (response.description) {
      descriptionShort.value = response.description.short || ''
      descriptionFull.value = response.description.full || ''
      benefits.value = response.description.benefits || ['']
    }
    
    if (response.characteristics && response.characteristics.length > 0) {
      characteristics.value = response.characteristics.map(c => ({
        key: c.key || '',
        name: c.name || '',
        value: c.value || '',
        filterable: c.filterable !== undefined ? c.filterable : true
      }))
    }
    
    if (response.media) {
      mainImage.value = response.media.mainImage || ''
      galleryImages.value = response.media.gallery && response.media.gallery.length > 0 
        ? response.media.gallery 
        : ['']
      videoUrl.value = response.media.video || ''
    }
    
    if (response.variants && response.variants.length > 0) {
      variants.value = response.variants.map(v => ({
        variantId: v.variantId || '',
        sku: v.sku || '',
        attributes: v.attributes || { color: '', size: '' },
        price: {
          base: v.price?.base?.toString() || '',
          sale: v.price?.sale?.toString() || '',
          currency: v.price?.currency || 'RUB',
          vat: v.price?.vat || 20
        },
        stock: {
          quantity: v.stock?.quantity || 0
        },
        barcodes: {
          skuBarcode: v.barcodes?.skuBarcode || '',
          ean13: v.barcodes?.ean13 || ''
        },
        logistics: {
          weightKg: v.logistics?.weightKg?.toString() || '',
          dimensionsCm: {
            length: v.logistics?.dimensionsCm?.length?.toString() || '',
            width: v.logistics?.dimensionsCm?.width?.toString() || '',
            height: v.logistics?.dimensionsCm?.height?.toString() || ''
          },
          delivery: {
            methods: v.logistics?.delivery?.methods || [],
            deliveryDays: v.logistics?.delivery?.deliveryDays || ''
          }
        }
      }))
    }
    
    if (response.returns) {
      returnsAllowed.value = response.returns.allowed !== undefined ? response.returns.allowed : true
      returnsDays.value = response.returns.days || 14
      returnsConditions.value = response.returns.conditions || ''
    }
    
    if (response.seo) {
      seoSlug.value = response.seo.slug || ''
      seoMetaTitle.value = response.seo.metaTitle || ''
      seoMetaDescription.value = response.seo.metaDescription || ''
    }
    
    status.value = response.status || 'draft'
    
    console.log('Форма заполнена данными продукта')
  } catch (error) {
    console.error('Ошибка при загрузке данных продукта:', error)
    alert('Ошибка при загрузке данных продукта: ' + (error.data?.message || error.message || 'Неизвестная ошибка'))
    // При ошибке загрузки переключаемся в режим добавления
    isEditMode.value = false
    productId.value = null
  } finally {
    isFetching.value = false
  }
}

// Watch на изменение route.query.id для загрузки данных при редактировании
watch(() => route.query.id, async (newId) => {
  if (newId) {
    const id = parseInt(newId)
    if (id && id !== productId.value) {
      productId.value = id
      isEditMode.value = true
      await loadProductData()
    }
  } else {
    // Если ID нет, значит это режим добавления
    productId.value = null
    isEditMode.value = false
  }
}, { immediate: true })

onMounted(async () => {
  await loadCategories()
  
  // Проверяем, есть ли ID продукта в query параметрах
  if (route.query.id) {
    productId.value = parseInt(route.query.id)
    isEditMode.value = true
    await loadProductData()
  }
})

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
const publishProduct = async () => {
  if (!productTitle.value || !variants.value[0]?.price?.base) {
    alert('Пожалуйста, заполните обязательные поля: Название товара и базовая цена варианта')
    return
  }

  try {
    isFetching.value = true

    const productData = {
      product: {
        sku: productSku.value || null,
        brand: productBrand.value || null,
        title: productTitle.value,
        category: {
          id: categoryId.value || null,
          parentId: parentCategoryId.value || null
        },
        description: {
          short: descriptionShort.value || null,
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
          mainImage: mainImage.value || null,
          gallery: galleryImages.value.filter(img => img.trim()),
          video: videoUrl.value || null
        }
      },
      variants: variants.value.map(v => ({
        variantId: v.variantId || null,
        sku: v.sku || null,
        attributes: v.attributes,
        price: {
          base: parseFloat(v.price.base) || 0,
          sale: v.price.sale ? parseFloat(v.price.sale) : null,
          currency: v.price.currency || 'RUB',
          vat: v.price.vat || 20
        },
        stock: {
          quantity: parseInt(v.stock.quantity) || 0
        },
        barcodes: {
          skuBarcode: v.barcodes.skuBarcode || null,
          ean13: v.barcodes.ean13 || null
        },
        logistics: {
          weightKg: v.logistics.weightKg ? parseFloat(v.logistics.weightKg) : null,
          dimensionsCm: {
            length: v.logistics.dimensionsCm.length ? parseFloat(v.logistics.dimensionsCm.length) : null,
            width: v.logistics.dimensionsCm.width ? parseFloat(v.logistics.dimensionsCm.width) : null,
            height: v.logistics.dimensionsCm.height ? parseFloat(v.logistics.dimensionsCm.height) : null
          },
          delivery: {
            methods: v.logistics.delivery.methods || [],
            deliveryDays: v.logistics.delivery.deliveryDays || null
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
      status: 'published'
    }

    let response
    if (isEditMode.value && productId.value) {
      // Редактирование существующего продукта
      response = await $api(`/admin/products/${productId.value}`, {
        method: 'PUT',
        body: productData
      })
      console.log('Товар обновлен успешно:', response)
    } else {
      // Создание нового продукта
      response = await $api('/admin/products', {
        method: 'POST',
        body: productData
      })
      console.log('Товар создан успешно:', response)
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

    const productData = {
      product: {
        sku: productSku.value || null,
        brand: productBrand.value || null,
        title: productTitle.value,
        category: {
          id: categoryId.value || null,
          parentId: parentCategoryId.value || null
        },
        description: {
          short: descriptionShort.value || null,
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
          mainImage: mainImage.value || null,
          gallery: galleryImages.value.filter(img => img.trim()),
          video: videoUrl.value || null
        }
      },
      variants: variants.value.map(v => ({
        variantId: v.variantId || null,
        sku: v.sku || null,
        attributes: v.attributes,
        price: {
          base: parseFloat(v.price.base) || 0,
          sale: v.price.sale ? parseFloat(v.price.sale) : null,
          currency: v.price.currency || 'RUB',
          vat: v.price.vat || 20
        },
        stock: {
          quantity: parseInt(v.stock.quantity) || 0
        },
        barcodes: {
          skuBarcode: v.barcodes.skuBarcode || null,
          ean13: v.barcodes.ean13 || null
        },
        logistics: {
          weightKg: v.logistics.weightKg ? parseFloat(v.logistics.weightKg) : null,
          dimensionsCm: {
            length: v.logistics.dimensionsCm.length ? parseFloat(v.logistics.dimensionsCm.length) : null,
            width: v.logistics.dimensionsCm.width ? parseFloat(v.logistics.dimensionsCm.width) : null,
            height: v.logistics.dimensionsCm.height ? parseFloat(v.logistics.dimensionsCm.height) : null
          },
          delivery: {
            methods: v.logistics.delivery.methods || [],
            deliveryDays: v.logistics.delivery.deliveryDays || null
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
      status: 'draft'
    }

    let response
    if (isEditMode.value && productId.value) {
      // Редактирование существующего продукта
      response = await $api(`/admin/products/${productId.value}`, {
        method: 'PUT',
        body: productData
      })
      console.log('Черновик обновлен успешно:', response)
      alert('Черновик обновлен успешно!')
    } else {
      // Создание нового продукта
      response = await $api('/admin/products', {
        method: 'POST',
        body: productData
      })
      console.log('Черновик сохранен успешно:', response)
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

// Метод для переключения метода доставки
const toggleDeliveryMethod = (variant, method) => {
  if (!variant.logistics.delivery.methods) {
    variant.logistics.delivery.methods = []
  }
  const idx = variant.logistics.delivery.methods.indexOf(method)
  if (idx > -1) {
    variant.logistics.delivery.methods.splice(idx, 1)
  } else {
    variant.logistics.delivery.methods.push(method)
  }
}
</script>

<template>
  <div>
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
        <VTab value="logistics">
          Логистика
        </VTab>
        <VTab value="seo">
          SEO
        </VTab>
      </VTabs>
    </VCard>

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
              <!-- Product Basic Info -->
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
                <ProductDescriptionEditor
                  v-model="descriptionFull"
                  placeholder="Подробное описание товара"
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

                <!-- Attributes -->
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

                <!-- Price -->
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

                <!-- Stock -->
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

                <!-- Barcodes -->
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

                <!-- Logistics -->
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
                <AppTextField
                  v-model="mainImage"
                  label="Основное изображение (URL)"
                  placeholder="https://cdn.site.ru/products/100023/main.jpg"
                />
              </VCol>
              <VCol cols="12">
                <VDivider class="my-4" />
                <div class="d-flex justify-space-between align-center mb-4">
                  <h6 class="text-h6 mb-0">
                    Галерея изображений
                  </h6>
                  <VBtn
                    size="small"
                    prepend-icon="tabler-plus"
                    @click="addGalleryImage"
                  >
                    Добавить изображение
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
                    v-if="galleryImages.length > 1"
                    icon="tabler-x"
                    variant="text"
                    size="small"
                    @click="removeGalleryImage(index)"
                  />
                </div>
              </VCol>
              <VCol cols="12">
                <VDivider class="my-4" />
                <AppTextField
                  v-model="videoUrl"
                  label="Видео (URL)"
                  placeholder="https://youtube.com/watch?v=..."
                />
              </VCol>
            </VRow>
          </VCardText>
        </VCard>

        <!-- Logistics Tab -->
        <VCard
          v-show="activeTab === 'logistics'"
          title="Возвраты и логистика"
          class="mb-6"
        >
          <VCardText>
            <VRow>
              <VCol cols="12">
                <VCheckbox
                  v-model="returnsAllowed"
                  label="Возврат разрешен"
                />
              </VCol>
              <VCol
                cols="12"
                md="6"
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
