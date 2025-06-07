import api from './api'


export default {

    
  async obtenerGastos(grupoId: any) {
    const { data } = await api.get(`/gasto/${grupoId}/gastos`)
    return Array.isArray(data)
      ? data
      : (typeof data === 'object' && data !== null && 'gastos' in data
        ? (data as { gastos: any[] }).gastos
        : (typeof data === 'object' && data !== null && 'data' in data ? (data as { data: any[] }).data : []))
  },

  async obtenerParticipantes(gastoId: any) {
    const { data } = await api.get(`/gasto/grupos/${gastoId}/participantes`)
    return data || []
  },

  async marcarSaldado(gastoId: number, participanteId: number, body: { metodoPago: string, notas: string }) {
    const { data } = await api.post(`/gasto/grupos/${gastoId}/participantes/${participanteId}/saldado`, body)
    return data
  },

  async eliminarGasto(gastoId: any) {
    await api.delete(`/gasto/${gastoId}`)
  },

  async obtenerGastoPorId(gastoId: any) {
    const { data } = await api.get(`/gasto/${gastoId}`)
    return data
  },

  async obtenerResumenDeudas(grupoId: any) {
    const { data } = await api.get(`/gasto/grupos/${grupoId}/deudas`)
    return data
  },
}
