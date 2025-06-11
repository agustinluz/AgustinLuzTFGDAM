import api from './api'

export interface Evento {
  id: number
  titulo: string
  descripcion: string
  ubicacion?: string
  fecha: string // ISO string desde backend
  grupoId: number
  grupoNombre: string
}

export interface EventoCrear {
  titulo: string
  descripcion: string
  ubicacion?: string
  fecha: Date
}

export interface EventoActualizar extends EventoCrear {
  id: number
}

function getAuthHeader(token: string) {
  return {
    headers: {
      Authorization: `Bearer ${token}`
    }
  }
}

export class EventosService {
  static async obtenerEventosGrupo(grupoId: number): Promise<Evento[]> {
    try {
      const response = await api.get(`/eventos/${grupoId}/eventos`)
      return (response.data as Evento[])
    } catch (error) {
      console.error('Error al obtener eventos:', error)
      throw new Error('Error al cargar los eventos')
    }
  }

  static async crearEvento(grupoId: number, evento: EventoCrear, token: string): Promise<Evento> {
    try {
      const payload = {
        ...evento,
        fecha: evento.fecha.toISOString()
      }
      const response = await api.post(`/eventos/${grupoId}/crear`, payload, getAuthHeader(token))
      return response.data as Evento
    } catch (error) {
      console.error('Error al crear evento:', error)
      throw new Error('Error al crear el evento')
    }
  }

  static async actualizarEvento(eventoId: number, evento: EventoCrear, token: string): Promise<Evento> {
    try {
      const payload = {
        ...evento,
        fecha: evento.fecha.toISOString()
      }
      const response = await api.put(`/eventos/${eventoId}`, payload, getAuthHeader(token))
      return response.data as Evento
    } catch (error) {
      console.error('Error al actualizar evento:', error)
      throw new Error('Error al actualizar el evento')
    }
  }

  static async eliminarEvento(eventoId: number, token: string): Promise<void> {
    try {
      await api.delete(`/eventos/${eventoId}`, getAuthHeader(token))
    } catch (error) {
      console.error('Error al eliminar evento:', error)
      throw new Error('Error al eliminar el evento')
    }
  }

  static async obtenerEventoPorId(eventoId: number): Promise<Evento> {
    try {
      const response = await api.get(`/eventos/${eventoId}`)
      return response.data as Evento
    } catch (error) {
      console.error('Error al obtener evento:', error)
      throw new Error('Error al obtener el evento')
    }
  }
   static async marcarAsistencia(eventoId: number, asistio: boolean, token: string): Promise<void> {
    try {
      await api.post(`/eventos/${eventoId}/asistencia?asistio=${asistio}`, {}, getAuthHeader(token))
    } catch (error) {
      console.error('Error al registrar asistencia:', error)
      throw new Error('Error al registrar asistencia')
    }
  }

  static async obtenerEstadisticas(eventoId: number): Promise<any> {
    try {
      const response = await api.get(`/eventos/${eventoId}/asistencia/estadisticas`)
      return response.data
    } catch (error) {
      console.error('Error al obtener estadisticas:', error)
      throw new Error('Error al obtener estadisticas')
    }
  }

  static async obtenerAsistentes(eventoId: number): Promise<any[]> {
    try {
      const response = await api.get(`/eventos/${eventoId}/asistentes`)
      return response.data as any[]
    } catch (error) {
      console.error('Error al obtener asistentes:', error)
      throw new Error('Error al obtener asistentes')
    }
  }
}
