import api from './api'

export const usuarioService = {
  getById(id: number) {
    return api.get(`/usuarios/${id}`)
  },
  update(id: number, data: any) {
    return api.put(`/usuarios/${id}`, data)
  }
}