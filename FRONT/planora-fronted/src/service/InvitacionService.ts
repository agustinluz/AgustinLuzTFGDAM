import api from './api'

export interface Invitacion {
  id: number
  grupoId: number
  grupoNombre: string
  usuarioInvitadoId: number
  usuarioInvitadoNombre: string
  estado: string
  fecha: string
}

export const invitacionService = {
  async listar(usuarioId: number) {
    const res = await api.get(`/invitaciones/usuario/${usuarioId}`)
    return res.data as Invitacion[]
  },
  async aceptar(id: number) {
    await api.post(`/invitaciones/${id}/aceptar`)
  },
  async rechazar(id: number) {
    await api.post(`/invitaciones/${id}/rechazar`)
  },
  async obtenerGrupoPorCodigo(codigo: string) {
    const res = await api.get(`/auth/invitacion/${codigo}`)
    return res.data
  }
}