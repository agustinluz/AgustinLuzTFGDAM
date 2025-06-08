// src/utils/string.ts

/**
 * Trunca un texto a `maxLength` caracteres (default 80) añadiendo “…” si hace falta.
 */
export function truncate(text = '', maxLength = 80): string {
  if (text.length <= maxLength) return text
  return text.substring(0, maxLength) + '…'
}

/**
 * Devuelve las iniciales (dos letras) de un nombre completo.
 * Si `fullName` es falsy, devuelve cadena vacía.
 */
export function getInitials(fullName?: string): string {
  if (!fullName) return ''
  const parts = fullName.trim().split(/\s+/)
  // Usa la primera letra de las dos primeras palabras
  const initials = parts
    .map(w => w.charAt(0).toUpperCase())
    .filter(Boolean)
    .slice(0, 2)
    .join('')
  return initials
}
