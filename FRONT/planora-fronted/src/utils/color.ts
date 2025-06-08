// src/utils/color.ts

const colors = [
  '#FF6B6B', '#4ECDC4', '#45B7D1',
  '#96CEB4', '#FECA57', '#FF9FF3',
  '#54A0FF'
];

/**
 * Devuelve un color basado en el nombre.
 * Si `fullName` es falsy, devuelve el primer color por defecto.
 */
export function getAvatarColor(fullName?: string): string {
  if (!fullName) return colors[0];
  let hash = 0;
  for (let i = 0; i < fullName.length; i++) {
    // calcula un hash sencillo
    hash = fullName.charCodeAt(i) + ((hash << 5) - hash);
  }
  return colors[Math.abs(hash) % colors.length];
}
