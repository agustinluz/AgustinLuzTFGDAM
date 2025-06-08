// DTOs y servicio de Dashboard
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
  fecha: string         // ISO date
  grupoId: number
  grupoNombre: string
  creadorId: number     // ¡asegúrate de que el backend lo rellene!
}

export interface UsuarioGrupoDTO {
  usuarioId: number
  grupoId: number
  rol: 'admin' | 'member'
  nombreUsuario: string
  emailUsuario: string
}

const API = import.meta.env.VITE_API_URL // p.e. "http://localhost:8080/api"
export const dashboardService = {
  async getGrupo(grupoId: string): Promise<GrupoDTO> {
    const res = await fetch(`${API}/grupos/${grupoId}`)
    if (!res.ok) throw new Error('Error al cargar el grupo')
    return res.json()
  },

  async getEventos(grupoId: string): Promise<EventoDTO[]> {
    const res = await fetch(`${API}/eventos/${grupoId}/eventos`)
    if (!res.ok) throw new Error('Error al cargar eventos')
    return res.json()
  },

  async getParticipantes(grupoId: string, usuarioId: string): Promise<UsuarioGrupoDTO[]> {
    const res = await fetch(`${API}/grupos/${grupoId}/usuarios`, {
      headers: { 'usuarioId': usuarioId }
    })
    if (!res.ok) throw new Error('Error al cargar participantes')
    return res.json()
  },

  async getEventoDetalle(eventoId: string): Promise<EventoDTO> {
    const res = await fetch(`${API}/eventos/${eventoId}`)
    if (!res.ok) throw new Error('Error al cargar detalle de evento')
    return res.json()
  },
  // En DashboardService
async deleteEvento(eventoId: string): Promise<void> {
  const res = await fetch(`${API}/eventos/${eventoId}`, { method: 'DELETE' })
  if (!res.ok) throw new Error('Error al eliminar evento')
}

}
