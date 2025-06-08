export interface GrupoDTO {
  id: number
  nombre: string
  codigoInvitacion: string
  imagenPerfil: string
  adminId: number
}

export interface EventoDTO {
  id: number
  titulo: string
  descripcion?: string
  ubicacion?: string
  fecha: string // ISO 8601
  grupoId: number
  grupoNombre: string
  creadorId: number
}

export interface UsuarioGrupoDTO {
  usuarioId: number
  grupoId: number
  rol: 'admin' | 'member'
  nombreUsuario: string
  emailUsuario: string
}

// Tipo local de usuario
export interface UsuarioDTO {
  id: string
  role: 'admin' | 'member'
}
/**
 * Estructura para datos de estadística en StatsGrid
 */
export interface StatsData {
  icon: any
  value: number
  label: string
}
