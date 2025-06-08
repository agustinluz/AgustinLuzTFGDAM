// src/utils/date.ts

/**
 * Formatea una fecha ISO a "DD de MMMM de YYYY" en español.
 */
export function formatDateES(iso: string): string {
  return new Date(iso).toLocaleDateString('es-ES', {
    day: '2-digit',
    month: 'long',
    year: 'numeric'
  })
}