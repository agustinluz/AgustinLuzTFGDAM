import { ref, computed } from 'vue'
import { EventosService, type Evento, type EventoCrear } from '@/service/EventoService'

export function useEventos(grupoId: number, token: string) {
  const eventos = ref<Evento[]>([])
  const loading = ref(false)
  const guardando = ref(false)
  const filtroTexto = ref('')
  const filtroFecha = ref('todos')

  // Computed: Eventos filtrados
  const eventosFiltrados = computed(() => {
    let filtrados = [...eventos.value]

    // Filtro por texto
    if (filtroTexto.value.trim()) {
      const texto = filtroTexto.value.toLowerCase()
      filtrados = filtrados.filter(evento =>
        evento.titulo.toLowerCase().includes(texto) ||
        evento.descripcion.toLowerCase().includes(texto) ||
        (evento.ubicacion && evento.ubicacion.toLowerCase().includes(texto))
      )
    }

    // Filtro por fecha
    const ahora = new Date()
    if (filtroFecha.value === 'proximos') {
      filtrados = filtrados.filter(evento => new Date(evento.fecha) > ahora)
    } else if (filtroFecha.value === 'pasados') {
      filtrados = filtrados.filter(evento => new Date(evento.fecha) <= ahora)
    }

    // Orden por fecha ascendente
    filtrados.sort((a, b) => new Date(a.fecha).getTime() - new Date(b.fecha).getTime())

    return filtrados
  })

  // Cargar eventos del grupo
  const cargarEventos = async () => {
    try {
      loading.value = true
      eventos.value = await EventosService.obtenerEventosGrupo(grupoId)
    } catch (error) {
      console.error('Error cargando eventos:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // Crear evento
  const crearEvento = async (eventoData: EventoCrear) => {
    try {
      guardando.value = true
      await EventosService.crearEvento(grupoId, eventoData, token)
      await cargarEventos()
    } catch (error) {
      console.error('Error creando evento:', error)
      throw error
    } finally {
      guardando.value = false
    }
  }

  // Actualizar evento
  const actualizarEvento = async (eventoId: number, eventoData: EventoCrear) => {
    try {
      guardando.value = true
      await EventosService.actualizarEvento(eventoId, eventoData, token)
      await cargarEventos()
    } catch (error) {
      console.error('Error actualizando evento:', error)
      throw error
    } finally {
      guardando.value = false
    }
  }

  // Eliminar evento
  const eliminarEvento = async (eventoId: number) => {
    try {
      await EventosService.eliminarEvento(eventoId, token)
      await cargarEventos()
    } catch (error) {
      console.error('Error eliminando evento:', error)
      throw error
    }
  }

  return {
    // Estado
    eventos,
    eventosFiltrados,
    loading,
    guardando,
    filtroTexto,
    filtroFecha,

    // Métodos
    cargarEventos,
    crearEvento,
    actualizarEvento,
    eliminarEvento
  }
}
