<script setup>
const siteUrl = useSiteUrl()

const PAGE_TITLE = 'Товары для дома оптом — iHome24 | Поставщик для ИП и юрлиц'
const PAGE_DESCRIPTION =
  'Купите товары для дома оптом на ihome24.ru. Работаем с ИП, ООО и самозанятыми. Скидки от 5% до 20%, договор, УПД, доставка по всей России. Личный менеджер, отгрузка от 1 дня.'

useSeoMeta({
  title: PAGE_TITLE,
  description: PAGE_DESCRIPTION,
  ogTitle: PAGE_TITLE,
  ogDescription: PAGE_DESCRIPTION,
  ogType: 'website',
  robots: 'index, follow',
})

useHead({
  link: [{ rel: 'canonical', href: `${siteUrl}/optovym-klientam`, key: 'canonical' }],
  script: [
    {
      type: 'application/ld+json',
      key: 'jsonld-wholesale',
      innerHTML: JSON.stringify({
        '@context': 'https://schema.org',
        '@type': 'LocalBusiness',
        name: 'iHome24 — оптовый поставщик товаров для дома',
        url: `${siteUrl}/optovym-klientam`,
        telephone: '+7-980-941-66-66',
        email: 'opt@ihome24.ru',
        address: {
          '@type': 'PostalAddress',
          addressLocality: 'Москва',
          streetAddress: 'МКАД, 19-й километр, вл20с1',
          postalCode: '115612',
          addressCountry: 'RU',
        },
        openingHoursSpecification: {
          '@type': 'OpeningHoursSpecification',
          dayOfWeek: [
            'Monday',
            'Tuesday',
            'Wednesday',
            'Thursday',
            'Friday',
            'Saturday',
            'Sunday',
          ],
          opens: '08:00',
          closes: '20:00',
        },
        areaServed: 'RU',
        description: PAGE_DESCRIPTION,
      }),
    },
    {
      type: 'application/ld+json',
      key: 'jsonld-wholesale-faq',
      innerHTML: JSON.stringify({
        '@context': 'https://schema.org',
        '@type': 'FAQPage',
        mainEntity: [
          {
            '@type': 'Question',
            name: 'Можно ли заказать небольшую пробную партию?',
            acceptedAnswer: {
              '@type': 'Answer',
              text: 'Да. Минимальный первый заказ — 10 000 ₽. Это позволяет протестировать товар и качество работы перед крупной закупкой.',
            },
          },
          {
            '@type': 'Question',
            name: 'Работаете ли вы с самозанятыми?',
            acceptedAnswer: {
              '@type': 'Answer',
              text: 'Да. Самозанятым достаточно ИНН и паспорта. Оформляем чек через приложение «Мой налог». Оптовые цены действуют с первого заказа.',
            },
          },
          {
            '@type': 'Question',
            name: 'Есть ли отсрочка платежа?',
            acceptedAnswer: {
              '@type': 'Answer',
              text: 'Отсрочка от 14 до 30 дней доступна постоянным клиентам после трёх месяцев сотрудничества.',
            },
          },
        ],
      }),
    },
  ],
})
</script>

<template>
  <PagesWholesaleClients />
</template>
