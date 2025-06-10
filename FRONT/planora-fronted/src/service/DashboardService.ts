// dashboardService.ts
import api from './api'

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
  fecha: string
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
  fotoPerfil?: string
}

export interface UsuarioStatsDTO {
  usuarioId: number
  nombreUsuario: string
  rol: string
  eventosCreados: number
  eventosAsistidos: number
}

export const dashboardService = {
  async getGrupo(grupoId: string): Promise<GrupoDTO> {
    try {
      const res = await api.get<GrupoDTO>(`/grupos/${grupoId}`)
      return res.data
    } catch (err) {
      throw new Error('Error al cargar el grupo')
    }
  },

  async getEventos(grupoId: string): Promise<EventoDTO[]> {
    try {
      const res = await api.get<EventoDTO[]>(`/eventos/${grupoId}/eventos`)
      return res.data
    } catch (err) {
      throw new Error('Error al cargar eventos')
    }
  },

  async getParticipantes(grupoId: string, usuarioId: string): Promise<UsuarioGrupoDTO[]> {
    try {
      const res = await api.get<UsuarioGrupoDTO[]>(`/grupos/${grupoId}/usuarios`, {
        headers: { usuarioId }
      })
      return res.data
    } catch (err) {
      throw new Error('Error al cargar participantes')
    }
  },

  async getEventoDetalle(eventoId: string): Promise<EventoDTO> {
    try {
      const res = await api.get<EventoDTO>(`/eventos/${eventoId}`)
      return res.data
    } catch (err) {
      throw new Error('Error al cargar detalle de evento')
    }
  },

  async getUsuarioStats(grupoId: string, usuarioId: string): Promise<UsuarioStatsDTO[]> {
    try {
      const res = await api.get<UsuarioStatsDTO[]>(`/grupos/${grupoId}/estadisticas/usuarios`, {
        headers: { usuarioId }
      })
      return res.data
    } catch (err) {
      throw new Error('Error al cargar estadísticas')
    }
  },

  async deleteEvento(eventoId: string): Promise<void> {
    try {
      await api.delete(`/eventos/${eventoId}`)
    } catch (err) {
      throw new Error('Error al eliminar evento')
    }
  }
}
