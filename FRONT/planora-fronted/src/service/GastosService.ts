import api from './api';

interface Evento { id: number; titulo: string; }
interface Usuario { id: number; nombre: string; }
interface DeudaGasto { deudorId: number; deudorNombre: string; monto: number; saldado: boolean; }
interface GastoResumen { id: number; titulo: string; monto: number; eventoId: number | null; deudas: DeudaGasto[]; partesIguales: boolean; usuarios: Usuario[]; }
interface GastoDetalle extends GastoResumen { fechaCreacion: string; evento?: Evento; pagadoPor: Usuario; }

export default {
  async obtenerGastos(grupoId: number): Promise<GastoResumen[]> {
    const response = await api.get(`/gasto/${grupoId}/gastos`);
    return response.data as GastoResumen[];
  },

  async obtenerEventos(grupoId: number): Promise<Evento[]> {
    const response = await api.get(`/eventos/${grupoId}/eventos`);
    return response.data as Evento[];
  },

  async obtenerUsuarios(grupoId: number): Promise<Usuario[]> {
    const response = await api.get(`/grupos/${grupoId}/usuarios`);
    return response.data as Usuario[];
  },

  async obtenerGastoPorId(gastoId: number): Promise<GastoDetalle> {
    const response = await api.get(`/gasto/${gastoId}`);
    const data = response.data as GastoDetalle;
    return {
      ...data,
      fecha: data.fechaCreacion,
      evento: data.evento ? { ...data.evento, nombre: data.evento.titulo } : undefined
    } as any;
  },

  async marcarSaldado(
  gastoId: number,
  participanteId: number,
  body: { metodoPago: string; notas: string }
): Promise<any> {
  // ojo al orden de segmentos: '/gasto/grupos/.../participantes/.../saldado'
  const { data } = await api.post(
    `/gasto/grupos/${gastoId}/participantes/${participanteId}/saldado`,
    body
  );
  return data;
},
  async eliminarGasto(gastoId: number): Promise<void> {
    await api.delete(`/gasto/${gastoId}`);
  },

  async crearGasto(grupoId: number, payload: any): Promise<GastoDetalle> {
    const response = await api.post(`/gasto/${grupoId}/crear`, payload);
    return response.data as GastoDetalle;
  },

  async actualizarGasto(gastoId: number, payload: any): Promise<GastoDetalle> {
    const response = await api.put(`/gasto/${gastoId}`, payload);
    return response.data as GastoDetalle;
  }
};
