import api from '@/service/api'

export interface Nota {
  id: number
  titulo: string
  contenido: string
  grupoId: number
  grupoNombre?: string
  creadaPorId: number
  creadaPorNombre?: string
  fechaCreacion: string
}

export interface NotaCrear {
  titulo: string
  contenido: string
}



function getAuthHeader(token: string) {
  return {
    headers: {
      Authorization: `Bearer ${token}`
    }
  }
}



export class NotasService {
  static async obtenerNotasPorGrupo(grupoId: number, token:string): Promise<Nota[]> {
    const response = await api.get(`/nota/${grupoId}/notas`, getAuthHeader(token))
    return response.data as Nota[]
  }

    static async crearNota(grupoId: number, nota: NotaCrear, token: string): Promise<Nota> {
    const response = await api.post(`/nota/${grupoId}/crear`, nota, getAuthHeader(token))
    return response.data as Nota
  }

  static async actualizarNota(notaId: number, nota: NotaCrear, token: string): Promise<Nota> {
    const response = await api.put(`/nota/${notaId}`, nota,  getAuthHeader(token))
    return response.data as Nota
  }

  static async eliminarNota(notaId: number, token:string): Promise<void> {
    await api.delete(`/nota/${notaId}`,getAuthHeader(token))
  }

  static async obtenerNotaPorId(notaId: number, token:string): Promise<Nota> {
    const response = await api.get(`/nota/${notaId}`, getAuthHeader(token))
    return response.data as Nota
  }

  static async obtenerNotasDelUsuario(token:string): Promise<Nota[]> {
    const response = await api.get(`/nota/usuario`, getAuthHeader(token))
    return response.data as Nota[]
  }
}
 