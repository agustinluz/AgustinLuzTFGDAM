<template>
  <ion-page>
    <!-- Cabecera mejorada -->
    <ion-header>
      <ion-toolbar color="primary">
        <ion-title>{{ grupo?.nombre || 'Dashboard' }}</ion-title>
        <ion-buttons slot="end">
          <ion-button fill="clear" @click="goToConfig">
            <ion-icon :icon="settingsOutline" slot="icon-only" />
          </ion-button>
          <ion-button fill="clear" @click="() => router.push('/grupo')">
            <ion-icon :icon="logOutOutline" slot="icon-only" />
          </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>
    
    <!-- Contenido Principal -->
    <ion-content :fullscreen="true">
      <div class="dashboard-container">
        
        <!-- Estadísticas rápidas -->
        <div class="stats-grid">
          <div class="stat-card">
            <ion-icon :icon="people" color="primary" />
            <div class="stat-content">
              <span class="stat-number">{{ participantes.length }}</span>
              <span class="stat-label">Miembros</span>
            </div>
          </div>
          <div class="stat-card">
            <ion-icon :icon="calendar" color="success" />
            <div class="stat-content">
              <span class="stat-number">{{ upcomingEventos.length }}</span>
              <span class="stat-label">Próximos</span>
            </div>
          </div>
        </div>

        <!-- Calendario compacto -->
        <ion-card class="calendar-card">
          <ion-card-header>
            <div class="section-header">
              <div class="section-title">
                <ion-icon :icon="calendar" color="primary" />
                <ion-card-title>{{ currentMonthYear }}</ion-card-title>
              </div>
              <div class="nav-buttons">
                <ion-button size="small" fill="clear" @click="previousMonth">
                  <ion-icon :icon="chevronBack" slot="icon-only" />
                </ion-button>
                <ion-button size="small" fill="clear" @click="nextMonth">
                  <ion-icon :icon="chevronForward" slot="icon-only" />
                </ion-button>
              </div>
            </div>
          </ion-card-header>
          <ion-card-content>
            <div class="calendar-grid">
              <div class="calendar-header-days">
                <div class="day-header" v-for="day in headerDays" :key="day">{{ day }}</div>
              </div>
              <div class="calendar-days">
                <div 
                  v-for="day in calendarDays" 
                  :key="day.date.toISOString()"
                  :class="getDayClass(day)"
                  @click="selectDay(day)"
                >
                  {{ day.day }}
                </div>
              </div>
            </div>
          </ion-card-content>
        </ion-card>
        
        <!-- Eventos próximos -->
        <ion-card class="events-card">
          <ion-card-header>
            <div class="section-header">
              <div class="section-title">
                <ion-icon :icon="timeOutline" color="warning" />
                <ion-card-title>Próximos eventos</ion-card-title>
              </div>
            </div>
          </ion-card-header>
          <ion-card-content>
            <div v-if="upcomingEventos.length" class="events-list">
              <div
                v-for="evento in upcomingEventos.slice(0, 3)" 
                :key="evento.id" 
                class="event-item"
                @click="mostrarEvento(evento.id)"
              >
                <div class="event-indicator"></div>
                <div class="event-content">
                  <h3 class="event-title">{{ evento.titulo }}</h3>
                  <p class="event-date">{{ formatFecha(evento.fecha) }}</p>
                  <p class="event-description" v-if="evento.descripcion">
                    {{ evento.descripcion.substring(0, 80) }}{{ evento.descripcion.length > 80 ? '...' : '' }}
                  </p>
                </div>
                <ion-icon :icon="chevronForward" color="medium" />
              </div>
            </div>
            <div v-else class="empty-state">
              <ion-icon :icon="calendarOutline" color="medium" />
              <ion-text color="medium">No hay eventos próximos</ion-text>
            </div>
          </ion-card-content>
        </ion-card>
        
        <!-- Participantes -->
        <ion-card class="participants-card" v-if="participantes.length">
          <ion-card-header>
            <div class="section-header">
              <div class="section-title">
                <ion-icon :icon="people" color="tertiary" />
                <ion-card-title>Equipo</ion-card-title>
              </div>
              <span class="member-count">{{ participantes.length }} miembros</span>
            </div>
          </ion-card-header>
          <ion-card-content>
            <div class="participants-grid">
              <div 
                class="participant-item" 
                v-for="participante in participantes.slice(0, 6)" 
                :key="participante.id"
              >
                <div
                  class="participant-avatar"
                  :style="{ backgroundColor: getAvatarColor(participante.nombre + ' ' + participante.apellido) }"
                >
                  {{ getInitials(participante.nombre, participante.apellido) }}
                </div>
                <span class="participant-name">
                  {{ participante.nombre }} {{ participante.apellido }}
                </span>
              </div>
              <div v-if="participantes.length > 6" class="more-participants">
                +{{ participantes.length - 6 }} más
              </div>
            </div>
          </ion-card-content>
        </ion-card>
        
        <!-- Accesos Rápidos -->
        <ion-card class="quick-access-card">
          <ion-card-header>
            <div class="section-title">
              <ion-icon :icon="appsOutline" color="secondary" />
              <ion-card-title>Acciones rápidas</ion-card-title>
            </div>
          </ion-card-header>
          <ion-card-content>
            <div class="quick-access-grid">
              <div class="access-item" @click="goTo('gastos')">
                <div class="access-icon">
                  <ion-icon :icon="cash" />
                </div>
                <span>Gastos</span>
              </div>
              <div class="access-item" @click="goTo('eventos')">
                <div class="access-icon">
                  <ion-icon :icon="calendar" />
                </div>
                <span>Eventos</span>
              </div>
              <div class="access-item" @click="goTo('votaciones')">
                <div class="access-icon">
                  <ion-icon :icon="checkboxOutline" />
                </div>
                <span>Votos</span>
              </div>
              <div class="access-item" @click="goTo('galeria')">
                <div class="access-icon">
                  <ion-icon :icon="images" />
                </div>
                <span>Galería</span>
              </div>
              <div class="access-item" @click="goTo('notas')">
                <div class="access-icon">
                  <ion-icon :icon="documentOutline" />
                </div>
                <span>Notas</span>
              </div>
            </div>
          </ion-card-content>
        </ion-card>
        
      </div>
    </ion-content>
    
    <!-- Botón FAB -->
    <ion-fab slot="fixed" vertical="bottom" horizontal="end">
      <ion-fab-button @click="goTo('crear')" color="primary">
        <ion-icon :icon="add" />
      </ion-fab-button>
    </ion-fab>
    
    <!-- Modal para Detalle de Evento -->
    <ion-modal v-model="mostrarModal">
      <ion-header>
        <ion-toolbar color="primary">
          <ion-title>{{ eventoSeleccionado?.titulo || 'Detalle del evento' }}</ion-title>
          <ion-buttons slot="end">
            <ion-button @click="mostrarModal = false">
              <ion-icon :icon="closeOutline" slot="icon-only" />
            </ion-button>
          </ion-buttons>
        </ion-toolbar>
      </ion-header>
      <ion-content class="ion-padding">
        <div v-if="eventoSeleccionado" class="event-detail">
          <h2>{{ eventoSeleccionado.titulo }}</h2>
          <div class="detail-row">
            <ion-icon :icon="calendar" color="primary" />
            <span>{{ formatFecha(eventoSeleccionado.fecha) }}</span>
          </div>
          <div v-if="eventoSeleccionado.descripcion" class="detail-row">
            <ion-icon :icon="documentText" color="primary" />
            <span>{{ eventoSeleccionado.descripcion }}</span>
          </div>
        </div>
        <div v-else class="loading-state">
          <ion-spinner />
          <ion-text color="medium">Cargando...</ion-text>
        </div>
      </ion-content>
    </ion-modal>
  </ion-page>
</template>

<script setup>
import {
  IonPage, IonHeader, IonToolbar, IonTitle, IonContent, IonCard, IonCardHeader,
  IonCardTitle, IonCardContent, IonText, IonFab, IonFabButton, IonIcon,
  IonButton, IonButtons, IonModal, IonSpinner, useIonRouter
} from '@ionic/vue'
import { 
  add, chevronBack, chevronForward, calendar, cash, checkboxOutline, images,
  logOutOutline, settingsOutline, people, timeOutline, calendarOutline,
  appsOutline, documentText, closeOutline,
  documentOutline
} from 'ionicons/icons'
import { ref, onMounted, computed } from 'vue'

const router = useIonRouter()
const grupo = ref(null)
const eventos = ref([])
const participantes = ref([])
const eventoSeleccionado = ref(null)
const mostrarModal = ref(false)
const currentDate = ref(new Date())
const selectedDate = ref(new Date())

const usuario = JSON.parse(localStorage.getItem('usuario'))
const grupoId = localStorage.getItem('grupoActivoId')

const headerDays = ['L', 'M', 'X', 'J', 'V', 'S', 'D']

const currentMonthYear = computed(() => {
  return currentDate.value.toLocaleDateString('es-ES', {
    month: 'long',
    year: 'numeric'
  }).replace(/^\w/, c => c.toUpperCase())
})

const calendarDays = computed(() => {
  const year = currentDate.value.getFullYear()
  const month = currentDate.value.getMonth()
  const firstDay = new Date(year, month, 1)
  const dayIndex = firstDay.getDay() === 0 ? 6 : firstDay.getDay() - 1
  const startDate = new Date(firstDay)
  startDate.setDate(startDate.getDate() - dayIndex)
  
  const days = []
  const currentDay = new Date(startDate)
  for (let i = 0; i < 42; i++) {
    days.push({
      date: new Date(currentDay),
      day: currentDay.getDate(),
      isCurrentMonth: currentDay.getMonth() === month,
      isToday: currentDay.toDateString() === new Date().toDateString(),
      isSelected: currentDay.toDateString() === selectedDate.value.toDateString()
    })
    currentDay.setDate(currentDay.getDate() + 1)
  }
  return days
})

const upcomingEventos = computed(() => {
  const now = new Date()
  return eventos.value.filter(evento => {
    const eventDate = new Date(evento.fecha)
    const diffDays = (eventDate - now) / (1000 * 60 * 60 * 24)
    return diffDays >= 0 && diffDays < 7
  }).sort((a, b) => new Date(a.fecha) - new Date(b.fecha))
})

const fetchEventos = async () => {
  try {
    const res = await fetch(`${import.meta.env.VITE_API_URL}/eventos/${grupoId}/eventos`)
    eventos.value = await res.json()
  } catch (err) {
    console.error('Error cargando eventos:', err)
  }
}

const fetchGrupo = async () => {
  try {
    const res = await fetch(`${import.meta.env.VITE_API_URL}/grupos/${grupoId}`)
    if (res.ok) {
      grupo.value = await res.json()
    } else {
      grupo.value = { nombre: 'Mi Grupo' }
    }
  } catch (err) {
    console.error('Error al cargar el grupo:', err)
    grupo.value = { nombre: 'Mi Grupo' }
  }
}

const fetchParticipantes = async () => {
  try {
    const res = await fetch(`${import.meta.env.VITE_API_URL}/grupos/${grupoId}/participantes`, {
      headers: { 'usuarioId': usuario?.id || '' }
    })
    if (res.ok) {
      participantes.value = await res.json()
    } else {
      participantes.value = []
    }
  } catch (err) {
    console.error('Error al cargar participantes:', err)
    participantes.value = []
  }
}

const fetchDetalleEvento = async (eventoId) => {
  try {
    const res = await fetch(`${import.meta.env.VITE_API_URL}/eventos/${eventoId}`)
    if (res.ok) {
      eventoSeleccionado.value = await res.json()
    } else {
      eventoSeleccionado.value = null
    }
  } catch (err) {
    console.error('Error al cargar detalle del evento:', err)
    eventoSeleccionado.value = null
  }
}

const goToConfig = () => {
  router.push(`/grupo/${grupoId}/configuracion`)
}

const previousMonth = () => {
  currentDate.value.setMonth(currentDate.value.getMonth() - 1)
  currentDate.value = new Date(currentDate.value)
}

const nextMonth = () => {
  currentDate.value.setMonth(currentDate.value.getMonth() + 1)
  currentDate.value = new Date(currentDate.value)
}

const selectDay = (day) => {
  selectedDate.value = new Date(day.date)
}

const getDayClass = (day) => {
  let classes = ['calendar-day']
  if (!day.isCurrentMonth) classes.push('other-month')
  if (day.isToday) classes.push('today')
  if (day.isSelected) classes.push('selected')
  return classes.join(' ')
}

const goTo = (ruta) => {
  router.push(`/dashboard/${grupoId}/${ruta}`)
}

const formatFecha = (fecha) => {
  return new Date(fecha).toLocaleDateString('es-ES', {
    day: '2-digit',
    month: 'long',
    year: 'numeric'
  })
}

const mostrarEvento = async (eventoId) => {
  eventoSeleccionado.value = null
  mostrarModal.value = true
  await fetchDetalleEvento(eventoId)
}

const getInitials = (nombre, apellido) => {
  const first = nombre ? nombre.charAt(0).toUpperCase() : ''
  const last = apellido ? apellido.charAt(0).toUpperCase() : ''
  return first + last
}

const getAvatarColor = (texto) => {
  const colors = ['#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4', '#FECA57', '#FF9FF3', '#54A0FF']
  let hash = 0
  for (let i = 0; i < texto.length; i++) {
    hash = texto.charCodeAt(i) + ((hash << 5) - hash)
  }
  return colors[Math.abs(hash) % colors.length]
}

onMounted(async () => {
  grupo.value = { nombre: 'Cargando...' }
  await Promise.all([fetchGrupo(), fetchEventos(), fetchParticipantes()])
})
</script>

<style scoped>
.dashboard-container {
  padding: 16px;
  max-width: 1200px;
  margin: 0 auto;
}

/* Estadísticas */
.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 20px;
}

.stat-card {
  background: var(--ion-color-light);
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.stat-content {
  display: flex;
  flex-direction: column;
}

.stat-number {
  font-size: 1.5rem;
  font-weight: bold;
  color: var(--ion-color-dark);
}

.stat-label {
  font-size: 0.875rem;
  color: var(--ion-color-medium);
}

/* Headers de sección */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.nav-buttons {
  display: flex;
  gap: 4px;
}

.member-count {
  font-size: 0.875rem;
  color: var(--ion-color-medium);
  background: var(--ion-color-light);
  padding: 4px 8px;
  border-radius: 12px;
}

/* Calendario compacto */
.calendar-card {
  margin-bottom: 20px;
}

.calendar-grid {
  display: flex;
  flex-direction: column;
}

.calendar-header-days {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 2px;
  margin-bottom: 8px;
}

.day-header {
  text-align: center;
  padding: 8px 4px;
  font-weight: 600;
  font-size: 0.75rem;
  color: var(--ion-color-medium);
}

.calendar-days {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 2px;
}

.calendar-day {
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 500;
  transition: all 0.2s ease;
}

.calendar-day:hover {
  background-color: var(--ion-color-light);
}

.other-month {
  color: var(--ion-color-light-shade);
}

.today {
  background-color: var(--ion-color-primary);
  color: white;
}

.selected {
  background-color: var(--ion-color-secondary);
  color: white;
}

/* Eventos */
.events-card {
  margin-bottom: 20px;
}

.events-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.event-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: var(--ion-color-light);
  border-radius: 12px;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.event-item:hover {
  transform: translateY(-2px);
}

.event-indicator {
  width: 4px;
  height: 40px;
  background: var(--ion-color-warning);
  border-radius: 2px;
}

.event-content {
  flex: 1;
}

.event-title {
  font-size: 1rem;
  font-weight: 600;
  margin: 0 0 4px 0;
  color: var(--ion-color-dark);
}

.event-date {
  font-size: 0.875rem;
  color: var(--ion-color-medium);
  margin: 0 0 4px 0;
}

.event-description {
  font-size: 0.875rem;
  color: var(--ion-color-medium-shade);
  margin: 0;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 20px;
}

/* Participantes */
.participants-card {
  margin-bottom: 20px;
}

.participants-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 16px;
}

.participant-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.participant-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 1rem;
}

.participant-name {
  font-size: 0.875rem;
  text-align: center;
  color: var(--ion-color-dark);
}

.more-participants {
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--ion-color-medium);
  font-size: 0.875rem;
  background: var(--ion-color-light);
  border-radius: 12px;
  padding: 16px;
}

/* Accesos rápidos */
.quick-access-card {
  margin-bottom: 20px;
}

.quick-access-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.access-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 8px;
  cursor: pointer;
  border-radius: 12px;
  transition: background-color 0.2s ease;
}

.access-item:hover {
  background-color: var(--ion-color-light);
}

.access-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: var(--ion-color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.access-item span {
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--ion-color-dark);
}

/* Modal */
.event-detail {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 40px;
}

/* Responsive */
@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .quick-access-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .participants-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>