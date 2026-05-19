const EMAIL_REGEX = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/

export function normalizePhoneDigits(phone) {
  if (!phone) return ''
  const digits = String(phone).replace(/\D/g, '')
  if (digits.length === 11 && digits.startsWith('8')) return '7' + digits.slice(1)
  if (digits.length === 10) return '7' + digits
  if (digits.length === 11 && digits.startsWith('7')) return digits
  return digits
}

export function isValidRussianMobile(phone) {
  const digits = normalizePhoneDigits(phone)
  return digits.length === 11 && digits.startsWith('7') && digits[1] === '9'
}

export function isValidEmail(email) {
  const v = String(email || '').trim()
  return v.length > 0 && EMAIL_REGEX.test(v)
}

export function isValidFullName(name) {
  const v = String(name || '').trim()
  if (v.length < 2) return false
  const parts = v.split(/\s+/).filter(Boolean)
  if (parts.length >= 2) return parts.every((p) => p.length >= 2)
  // одно слово (например «Мария») — допустимо от 2 символов
  return parts.length === 1 && parts[0].length >= 2
}

export function validateCheckoutContacts({ fullName, email, phone }) {
  const errors = {}
  if (!isValidFullName(fullName)) {
    errors.fullName = 'Укажите ФИО (имя и фамилия или одно имя от 2 символов)'
  }
  if (!isValidEmail(email)) {
    errors.email = 'Введите корректный email, например name@mail.ru'
  }
  if (!isValidRussianMobile(phone)) {
    errors.phone = 'Введите номер РФ в формате +7 9XX XXX-XX-XX'
  }
  return errors
}
