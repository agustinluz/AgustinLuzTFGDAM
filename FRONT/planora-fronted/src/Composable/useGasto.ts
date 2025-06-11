// src/Composable/useGastos.ts
import { ref, computed, onMounted } from 'vue';
import GastoService from '@/service/GastosService';

// Interfaces locales para tipado
interface Usuario { id: number; nombre: string; }
interface DeudaGasto { deudorId: number; deudorNombre: string; monto: number; saldado: boolean; }
interface GastoResumen { id: number; titulo: string; monto: number; eventoId: number | null; deudas: DeudaGasto[]; partesIguales: boolean; usuarios: Usuario[]; }
interface Evento { id: number; titulo: string; }
interface GastoDetalle extends GastoResumen { fechaCreacion: string; evento?: Evento; pagadoPor: Usuario; }

enum Filtro { TODOS = 'todos', PENDIENTES = 'pendientes', SALDADOS = 'saldados' }

export function useGastos(grupoId: number) {
  const gastos = ref<GastoResumen[]>([]);
  const gastoSeleccionado = ref<GastoDetalle | null>(null);
  const cargando = ref(false);
  const filtro = ref<Filtro>(Filtro.TODOS);

  const totalGastos = computed(() =>
    gastos.value.reduce((sum, g) => sum + Number(g.monto || 0), 0)
  );

  const gastosPendientes = computed(() =>
    gastos.value.filter(g => g.deudas.some(d => !d.saldado)).length
  );

  const gastosSaldados = computed(() =>
    gastos.value.filter(g => g.deudas.every(d => d.saldado)).length
  );

  const gastosFiltrados = computed(() =>
    filtro.value === Filtro.PENDIENTES
      ? gastos.value.filter(g => g.deudas.some(d => !d.saldado))
      : filtro.value === Filtro.SALDADOS
        ? gastos.value.filter(g => g.deudas.every(d => d.saldado))
        : gastos.value
  );

  const cargarGastos = async () => {
    cargando.value = true;
    try {
      const data = await GastoService.obtenerGastos(grupoId);
      gastos.value = data;
    } catch (e) {
      console.error('Error cargando gastos', e);
      gastos.value = [];
    } finally {
      cargando.value = false;
    }
  };

  const actualizarGastoEnLista = async (gastoId: number) => {
    try {
      const detalleActualizado = await GastoService.obtenerGastoPorId(gastoId);
      const index = gastos.value.findIndex(g => g.id === gastoId);
      if (index !== -1) {
        gastos.value[index] = {
          ...detalleActualizado,
          deudas: detalleActualizado.deudas,
          partesIguales: detalleActualizado.partesIguales,
          usuarios: detalleActualizado.usuarios
        };
      }
    } catch (e) {
      console.error('Error actualizando gasto individual', e);
    }
  };

  const seleccionarGasto = async (resumen: { id: number }) => {
    try {
      const detalle = await GastoService.obtenerGastoPorId(resumen.id);
      gastoSeleccionado.value = detalle;
    } catch (e) {
      console.error('Error obteniendo detalle de gasto', e);
      gastoSeleccionado.value = null;
    }
  };

  const marcarSaldado = async (gastoId: number, participanteId: number, metodo = 'efectivo', notas = '') => {
    try {
      await GastoService.marcarSaldado(gastoId, participanteId, { metodoPago: metodo, notas });
      await actualizarGastoEnLista(gastoId);
    } catch (e) {
      console.error('Error marcando saldado', e);
    }
  };

  const eliminarGasto = async (id: number) => {
    try {
      await GastoService.eliminarGasto(id);
      gastoSeleccionado.value = null;
      gastos.value = gastos.value.filter(g => g.id !== id);
    } catch (e) {
      console.error('Error eliminando gasto', e);
    }
  };

  onMounted(cargarGastos);

  return {
    gastos,
    gastoSeleccionado,
    cargando,
    filtro,
    totalGastos,
    gastosPendientes,
    gastosSaldados,
    gastosFiltrados,
    cargarGastos,
    seleccionarGasto,
    marcarSaldado,
    eliminarGasto
  };
}
