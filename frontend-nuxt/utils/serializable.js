/** Клон в обычные JSON-объекты — нужен для Pinia SSR payload (избегает hasOwnProperty ошибок). */
export function toPlainSerializable(value) {
  if (value === undefined || value === null) return value
  try {
    return JSON.parse(JSON.stringify(value))
  } catch {
    return value
  }
}
