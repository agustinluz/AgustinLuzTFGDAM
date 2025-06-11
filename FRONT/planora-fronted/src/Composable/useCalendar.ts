// src/composables/useCalendar.ts
import { ref, computed } from 'vue'
import { useDashboardStore } from '@/store/dashboardStore'
import type { EventoDTO } from '@/service/DashboardService'

export type CalendarDay = {
  date: Date
  day: number
  isCurrentMonth: boolean
  isToday: boolean
  isSelected: boolean
}

export function useCalendar() {
  const store = useDashboardStore()
  const currentDate = ref(new Date())
  const selectedDate = ref(new Date())

  const headerDays = ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom']

  const calendarDays = computed<CalendarDay[]>(() => {
    // 1. Calcula primer día visible (incluye prev mes)
    const year = currentDate.value.getFullYear()
    const month = currentDate.value.getMonth()
    const firstOfMonth = new Date(year, month, 1)
    const startDayIdx = (firstOfMonth.getDay() + 6) % 7
    const startDate = new Date(firstOfMonth)
    startDate.setDate(startDate.getDate() - startDayIdx)

    // 2. Construye 6 semanas x 7 días = 42 celdas
    const days: CalendarDay[] = []
    const cursor = new Date(startDate)
    for (let i = 0; i < 42; i++) {
      days.push({
        date: new Date(cursor),
        day: cursor.getDate(),
        isCurrentMonth: cursor.getMonth() === month,
        isToday: cursor.toDateString() === new Date().toDateString(),
        isSelected: cursor.toDateString() ===
          selectedDate.value.toDateString()
      })
      cursor.setDate(cursor.getDate() + 1)
    }
    return days
  })

  const eventDates = computed(() =>
    store.eventos.map(e => new Date(e.fecha).toDateString())
  )

  const pastEventDates = computed(() =>
    store.eventos
      .filter(e => new Date(e.fecha).getTime() < Date.now())
      .map(e => new Date(e.fecha).toDateString())
  )


  const eventsForSelectedDay = computed(() =>
    store.eventos.filter(e =>
      new Date(e.fecha).toDateString() ===
      selectedDate.value.toDateString()
    )
  )

  function previousMonth() {
    const d = new Date(currentDate.value)
    d.setMonth(d.getMonth() - 1, 1)
    currentDate.value = d
  }

  function nextMonth() {
    const d = new Date(currentDate.value)
    d.setMonth(d.getMonth() + 1, 1)
    currentDate.value = d
  }

  function selectDay(day: CalendarDay) {
    selectedDate.value = new Date(day.date)
  }

  return {
    currentDate,
    selectedDate,
    headerDays,
    calendarDays,
    eventDates,
    pastEventDates,
    eventsForSelectedDay,
    previousMonth,
    nextMonth,
    selectDay
  }
}
