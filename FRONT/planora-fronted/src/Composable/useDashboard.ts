import { ref, computed, onMounted } from 'vue'
import {
  dashboardService,
  type GrupoDTO,
  type EventoDTO,
  type UsuarioGrupoDTO
} from '@/service/DashboardService'

type CalendarDay = {
  date: Date
  day: number
  isCurrentMonth: boolean
  isToday: boolean
  isSelected: boolean
}

export function useDashboard() {
  // Usuario desde localStorage
  const usuario = JSON.parse(localStorage.getItem('usuario') || '{}') as {
    id: string
    role: 'admin' | 'member'
  }
  const grupoId = localStorage.getItem('grupoActivoId') || ''

  // Estado
  const loading = ref<boolean>(false)
  const grupo = ref<GrupoDTO | null>(null)
  const eventos = ref<EventoDTO[]>([])
  const participantes = ref<UsuarioGrupoDTO[]>([])
  const currentDate = ref<Date>(new Date())
  const selectedDate = ref<Date>(new Date())
  const selectedEvent = ref<EventoDTO | null>(null)

  const headerDays = ['L', 'M', 'X', 'J', 'V', 'S', 'D']

  const currentMonthYear = computed<string>(() =>
    currentDate.value
      .toLocaleDateString('es-ES', { month: 'long', year: 'numeric' })
      .replace(/^\w/, (c) => c.toUpperCase())
  )

  const calendarDays = computed<CalendarDay[]>(() => {
    const year = currentDate.value.getFullYear()
    const month = currentDate.value.getMonth()
    const firstOfMonth = new Date(year, month, 1)
    const dayIndex = firstOfMonth.getDay() === 0 ? 6 : firstOfMonth.getDay() - 1
    const start = new Date(firstOfMonth)
    start.setDate(start.getDate() - dayIndex)

    const days: CalendarDay[] = []
    const cursor = new Date(start)
    for (let i = 0; i < 42; i++) {
      days.push({
        date: new Date(cursor),
        day: cursor.getDate(),
        isCurrentMonth: cursor.getMonth() === month,
        isToday: cursor.toDateString() === new Date().toDateString(),
        isSelected:
          cursor.toDateString() === selectedDate.value.toDateString()
      })
      cursor.setDate(cursor.getDate() + 1)
    }
    return days
  })

  const eventDates = computed<string[]>(() =>
    eventos.value.map((e) => new Date(e.fecha).toDateString())
  )

  const upcomingEventos = computed<EventoDTO[]>(() => {
    const now = Date.now()
    return eventos.value
      .filter((e) => {
        const diff = new Date(e.fecha).getTime() - now
        return diff >= 0 && diff < 7 * 24 * 60 * 60 * 1000
      })
      .sort(
        (a, b) =>
          new Date(a.fecha).getTime() - new Date(b.fecha).getTime()
      )
  })

  const eventsForSelectedDay = computed<EventoDTO[]>(() =>
    eventos.value.filter(
      (e) =>
        new Date(e.fecha).toDateString() ===
        selectedDate.value.toDateString()
    )
  )

  const fetchAll = async (): Promise<void> => {
    loading.value = true
    try {
      const [g, evs, parts] = await Promise.all([
        dashboardService.getGrupo(grupoId),
        dashboardService.getEventos(grupoId),
        dashboardService.getParticipantes(grupoId, usuario.id)
      ])
      grupo.value = g
      eventos.value = evs
      participantes.value = parts
    } catch (err) {
      console.error('Error en fetchAll:', err)
    } finally {
      loading.value = false
    }
  }

  const previousMonth = (): void => {
    currentDate.value.setMonth(currentDate.value.getMonth() - 1)
    currentDate.value = new Date(currentDate.value)
  }
  const nextMonth = (): void => {
    currentDate.value.setMonth(currentDate.value.getMonth() + 1)
    currentDate.value = new Date(currentDate.value)
  }

  const selectDay = (day: CalendarDay): void => {
    selectedDate.value = new Date(day.date)
    selectedEvent.value = null
  }

  onMounted(fetchAll)

  return {
    usuario,
    grupo,
    eventos,
    participantes,
    loading,
    currentDate,
    selectedDate,
    selectedEvent,
    headerDays,
    currentMonthYear,
    calendarDays,
    eventDates,
    upcomingEventos,
    eventsForSelectedDay,
    fetchAll,
    previousMonth,
    nextMonth,
    selectDay
  }
}
