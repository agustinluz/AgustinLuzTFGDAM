import { ref, computed } from 'vue'
import { NotasService, type Nota, type NotaCrear } from '@/service/NotaService'

export function useNotas(grupoId: number) {
  const notas = ref<Nota[]>([])
  const cargando = ref(false)
  const guardando = ref(false)
  const busqueda = ref('')

  const notasFiltradas = computed(() => {
    const termino = busqueda.value.trim().toLowerCase()
    if (!termino) return notas.value
    return notas.value.filter(nota =>
      nota.titulo.toLowerCase().includes(termino) ||
      nota.contenido.toLowerCase().includes(termino)
    )
  })


  const cargarNotas = async () => {
    try {
      cargando.value = true
      const token = localStorage.getItem('token')
    if (!token) throw new Error('Token no encontrado')

      notas.value = await NotasService.obtenerNotasPorGrupo(grupoId,token)
    } catch (error) {
      console.error('Error al cargar notas:', error)
      throw error
    } finally {
      cargando.value = false
    }
  }


  const actualizarNota = async (notaId: number, nota: NotaCrear) => {
    try {
      guardando.value = true
      const token = localStorage.getItem('token')
    if (!token) throw new Error('Token no encontrado')

      await NotasService.actualizarNota(notaId, nota,token)
      await cargarNotas()
    } catch (error) {
      console.error('Error al actualizar nota:', error)
      throw error
    } finally {
      guardando.value = false
    }
  }

  const eliminarNota = async (notaId: number) => {
  try {
    const token = localStorage.getItem('token')
    if (!token) throw new Error('Token no encontrado')

    await NotasService.eliminarNota(notaId, token)
    await cargarNotas()
  } catch (error) {
    console.error('Error al eliminar nota:', error)
    throw error
  }
}


  return {
    notas,
    notasFiltradas,
    busqueda,
    cargando,
    guardando,
    cargarNotas,
    actualizarNota,
    eliminarNota
  }
}
