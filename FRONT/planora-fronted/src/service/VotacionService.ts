import api from './api'

export const votacionService = {
  listarPorGrupo(grupoId: string, token: string) {
    return api.get(`/grupos/${grupoId}/votaciones`, {
      headers: { Authorization: `Bearer ${token}` }
    })
  },
  obtenerMiVoto(id: number, token: string) {
    return api.get(`/votaciones/${id}/mi-voto`, {
      headers: { Authorization: `Bearer ${token}` }
    })
  },
  guardar(grupoId: string, data: any, token: string) {
    return api.post(`/grupos/${grupoId}/votaciones`, data, {
      headers: { Authorization: `Bearer ${token}` }
    })
  },
  actualizar(id: number, data: any, token: string) {
    return api.put(`/votaciones/${id}`, data, {
      headers: { Authorization: `Bearer ${token}` }
    })
  },
  votar(id: number, opcion: string, token: string) {
    return api.post(`/votaciones/${id}/votar`, { opcion }, {
      headers: { Authorization: `Bearer ${token}` }
    })
  },
  resultados(id: number) {
    return api.get(`/votaciones/${id}/resultados`)
  },
  cerrar(id: number, token: string) {
    return api.put(`/votaciones/${id}/cerrar`, {}, {
      headers: { Authorization: `Bearer ${token}` }
    })
  },
  eliminar(id: number, token: string) {
    return api.delete(`/votaciones/${id}`, {
      headers: { Authorization: `Bearer ${token}` }
    })
  }
}