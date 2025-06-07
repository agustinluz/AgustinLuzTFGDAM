import { ref, computed, Ref } from 'vue';
import GastoService from '@/service/GastosService';

interface DeudaGasto {
  id: number;
  deudorId: number;
  deudorNombre: string;
  acreedorId: number;
  acreedorNombre: string;
  gastoId: number;
  titulo: string;
  monto: number;
  saldado: boolean;
}

interface Gasto {
  id: number;
  titulo: string;
  monto: number;
  deudas: DeudaGasto[];
  pendiente?: boolean;
}

export function useGastos(grupoId: number) {
  const gastos: Ref<Gasto[]> = ref([]);
  const gastoSeleccionado: Ref<Gasto | null> = ref(null);
  const cargando = ref(true);
  const filtro = ref<'todos' | 'pendientes'>('todos');

  const totalGastos = computed(() =>
    gastos.value.reduce((sum, g) => sum + parseFloat(String(g.monto || 0)), 0)
  );

  const gastosPendientes = computed(() =>
    gastos.value.filter(g => g.pendiente).length
  );

  const gastosFiltrados = computed(() =>
    filtro.value === 'pendientes'
      ? gastos.value.filter(g => g.pendiente)
      : gastos.value
  );

  const cargarGastos = async () => {
    cargando.value = true;
    try {
      const data: Gasto[] = await GastoService.obtenerGastos(grupoId);
      gastos.value = data.map(g => {
        const tienePendientes = Array.isArray(g.deudas) && g.deudas.some((d: DeudaGasto) => !d.saldado);
        return {
          ...g,
          pendiente: tienePendientes
        };
      });
    } catch (e) {
      console.error('Error al cargar gastos', e);
    } finally {
      cargando.value = false;
    }
  };

  const seleccionarGasto = (gasto: Gasto) => {
    gastoSeleccionado.value = gasto;
  };

  const marcarSaldado = async (
  gastoId: number,
  participanteId: number,
  metodoPago: string = 'efectivo',
  notas: string = ''
) => {
  if (!participanteId || typeof participanteId !== 'number') {
    console.warn('❌ Participante ID inválido:', participanteId)
    return
  }
  try {
    await GastoService.marcarSaldado(gastoId, participanteId, { metodoPago, notas })
    await cargarGastos()
  } catch (e) {
    console.error('Error al marcar saldado', e)
  }
}


  const obtenerGastoPorId = async (gastoId: number) => {
  try {
    return await GastoService.obtenerGastoPorId(gastoId)
  } catch (e) {
    console.error('Error al obtener gasto por ID', e)
    return null
  }
}

const eliminarGasto = async (gastoId: number) => {
  try {
    await GastoService.eliminarGasto(gastoId)
    gastoSeleccionado.value = null
    await cargarGastos()
  } catch (e) {
    console.error('Error al eliminar gasto', e)
  }
}


  return {
    gastos,
    gastoSeleccionado,
    cargando,
    filtro,
    totalGastos,
    gastosPendientes,
    gastosFiltrados,
    cargarGastos,
    seleccionarGasto,
    marcarSaldado,
    eliminarGasto,
    obtenerGastoPorId
  };
}
