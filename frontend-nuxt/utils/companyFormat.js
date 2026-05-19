/** Нормализация реквизитов организации для отображения и отправки в заказ. */

export function digitsOnly(value) {
  if (value == null || value === '') return ''
  return String(value).replace(/\D/g, '')
}

/** ИНН: 10 или 12 цифр */
export function formatInn(value) {
  return digitsOnly(value)
}

/** КПП: 9 цифр */
export function formatKpp(value) {
  const d = digitsOnly(value)
  if (!d) return ''
  if (d.length >= 9) return d.slice(0, 9)
  return d.padStart(9, '0')
}

/** ОГРН: 13 (ИП) или 15 (юрлицо) */
export function formatOgrn(value) {
  const d = digitsOnly(value)
  if (!d) return ''
  if (d.length <= 13) return d.padStart(13, '0')
  return d.padStart(15, '0').slice(-15)
}

/** ОКПО: 8 или 10 цифр, с ведущими нулями */
export function formatOkpo(value) {
  const d = digitsOnly(value)
  if (!d) return ''
  if (d.length <= 8) return d.padStart(8, '0')
  return d.padStart(10, '0').slice(-10)
}

/** БИК: 9 цифр */
export function formatBik(value) {
  const d = digitsOnly(value)
  if (!d) return ''
  if (d.length >= 9) return d.slice(0, 9)
  return d.padStart(9, '0')
}

/** Расчётный / корр. счёт: 20 цифр */
export function formatBankAccount(value) {
  const d = digitsOnly(value)
  if (!d) return ''
  if (d.length >= 20) return d.slice(0, 20)
  return d.padStart(20, '0')
}

/** Адрес: индекс, регион, … через запятую */
export function formatCompanyAddress(value) {
  if (!value) return ''
  return String(value)
    .trim()
    .replace(/\s+/g, ' ')
    .replace(/\s*,\s*/g, ', ')
}

export function normalizeCompanyParty(party) {
  if (!party) return null
  return {
    ...party,
    name: party.name?.trim() || '',
    inn: formatInn(party.inn),
    kpp: party.kpp ? formatKpp(party.kpp) : null,
    ogrn: party.ogrn ? formatOgrn(party.ogrn) : null,
    okpo: party.okpo ? formatOkpo(party.okpo) : null,
    address: party.address ? formatCompanyAddress(party.address) : null,
    corrAccount: party.corrAccount ? formatBankAccount(party.corrAccount) : null,
    bik: party.bik ? formatBik(party.bik) : null,
    settlementAccount: party.settlementAccount ? formatBankAccount(party.settlementAccount) : null,
  }
}

/** Сообщение об ошибке валидации или null */
export function validateCompanyParty(party) {
  const v = normalizeCompanyParty(party)
  if (!v?.name) return 'Укажите наименование организации'
  if (!v?.address) return 'Укажите адрес организации'
  const ogrn = digitsOnly(v.ogrn)
  if (ogrn.length !== 13 && ogrn.length !== 15) return 'ОГРН должен содержать 13 или 15 цифр'
  if (!v.inn || (v.inn.length !== 10 && v.inn.length !== 12)) {
    return 'ИНН должен содержать 10 или 12 цифр'
  }
  const okpo = digitsOnly(v.okpo)
  if (okpo && okpo.length !== 8 && okpo.length !== 10) {
    return 'ОКПО должен содержать 8 или 10 цифр'
  }
  const bik = digitsOnly(v.bik)
  if (bik && bik.length !== 9) return 'БИК должен содержать 9 цифр'
  const corr = digitsOnly(v.corrAccount)
  if (corr && corr.length !== 20) return 'Корр. счёт должен содержать 20 цифр'
  const rs = digitsOnly(v.settlementAccount)
  if (rs && rs.length !== 20) return 'Расчётный счёт должен содержать 20 цифр'
  return null
}

export function partyToManual(party) {
  const c = normalizeCompanyParty(party) || {}
  return {
    name: c.name || '',
    address: c.address || '',
    ogrn: c.ogrn || '',
    inn: c.inn || '',
    corrAccount: c.corrAccount || '',
    bik: c.bik || '',
    settlementAccount: c.settlementAccount || '',
    kpp: c.kpp || '',
    okpo: c.okpo || '',
  }
}

export function manualToParty(manual) {
  return normalizeCompanyParty({
    name: manual.name?.trim(),
    address: manual.address?.trim(),
    ogrn: manual.ogrn,
    inn: manual.inn,
    corrAccount: manual.corrAccount || null,
    bik: manual.bik || null,
    settlementAccount: manual.settlementAccount || null,
    kpp: manual.kpp || null,
    okpo: manual.okpo || null,
    type: null,
  })
}
