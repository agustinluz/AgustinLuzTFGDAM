<template>
  <ion-page>
    <!-- Cabecera -->
    <ion-header>
      <ion-toolbar color="primary">
        <ion-title>{{ grupo?.nombre || 'Dashboard' }}</ion-title>
        <ion-buttons slot="end">
          <ion-button fill="clear" @click="() => router.push('/grupo')">
            <ion-icon :icon="logOutOutline" slot="icon-only" />
          </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>
    
    <!-- Contenido Principal -->
    <ion-content :fullscreen="true" class="ion-padding">
      
      <!-- Calendario (estilo anterior, iniciando en lunes) -->
      <ion-card class="calendar-card">
        <ion-card-header>
          <div class="calendar-header">
            <ion-button fill="clear" @click="previousMonth">
              <ion-icon :icon="chevronBack" slot="icon-only" />
            </ion-button>
            <ion-card-title class="month-title">{{ currentMonthYear }}</ion-card-title>
            <ion-button fill="clear" @click="nextMonth">
              <ion-icon :icon="chevronForward" slot="icon-only" />
            </ion-button>
          </div>
        </ion-card-header>
        <ion-card-content>
          <div class="calendar-grid">
            <div class="calendar-header-days">
              <!-- Encabezado actualizado para iniciar en lunes -->
              <div class="day-header" v-for="(day, idx) in headerDays" :key="idx">{{ day }}</div>
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
      
      <!-- Eventos (sólo próximos 7 días) -->
      <ion-card class="events-card">
        <ion-card-header>
          <div class="events-header">
            <ion-icon :icon="calendar" color="primary" />
            <ion-card-title>Eventos próximos</ion-card-title>
            <ion-button fill="clear" size="small">
              <ion-icon :icon="ellipsisVertical" slot="icon-only" />
            </ion-button>
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
              <div class="event-content">
                <h3 class="event-title">{{ evento.titulo }}</h3>
                <p class="event-subtitle">{{ formatFecha(evento.fecha) }}</p>
                <p class="event-description" v-if="evento.descripcion">
                  {{ evento.descripcion }}
                </p>
              </div>
            </div>
          </div>
          <div v-else class="no-events">
            <ion-text color="medium">No hay eventos próximos</ion-text>
          </div>
        </ion-card-content>
      </ion-card>
      
      <!-- Participantes -->
      <ion-card class="participants-card" v-if="participantes.length">
        <ion-card-header>
          <div class="participants-header">
            <ion-icon name="people" color="primary" />
            <ion-card-title>Participantes</ion-card-title>
          </div>
        </ion-card-header>
        <ion-card-content>
          <div class="participants-list">
            <div 
              class="participant-container" 
              v-for="participante in participantes" 
              :key="participante.id"
            >
              <div
                class="participant-avatar"
                :style="{ backgroundColor: getAvatarColor(participante.nombre + ' ' + participante.apellido) }"
              >
                {{ getInitials(participante.nombre, participante.apellido) }}
              </div>
              <div class="participant-name">
                {{ participante.nombre }} {{ participante.apellido }}
              </div>
            </div>
          </div>
        </ion-card-content>
      </ion-card>
      
      <!-- Accesos Rápidos -->
      <ion-card class="quick-access-card">
        <ion-card-content>
          <div class="quick-access-grid">
            <div class="quick-access-item" @click="goTo('gastos')">
              <ion-icon :icon="cash" color="primary" />
            </div>
            <div class="quick-access-item" @click="goTo('eventos')">
              <ion-icon :icon="calendar" color="primary" />
            </div>
            <div class="quick-access-item" @click="goTo('votaciones')">
              <ion-icon :icon="checkboxOutline" color="primary" />
            </div>
            <div class="quick-access-item" @click="goTo('galeria')">
              <ion-icon :icon="images" color="primary" />
            </div>
            <div class="quick-access-item" @click="goTo('notas')">
              <ion-icon :icon="documentText" color="primary" />
            </div>
          </div>
        </ion-card-content>
      </ion-card>
      
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
              <ion-icon name="close" slot="icon-only" />
            </ion-button>
          </ion-buttons>
        </ion-toolbar>
      </ion-header>
      <ion-content class="ion-padding">
        <div v-if="eventoSeleccionado">
          <h2>{{ eventoSeleccionado.titulo }}</h2>
          <p><strong>Fecha:</strong> {{ formatFecha(eventoSeleccionado.fecha) }}</p>
          <p v-if="eventoSeleccionado.descripcion">
            <strong>Descripción:</strong> {{ eventoSeleccionado.descripcion }}
          </p>
        </div>
        <div v-else>
          <ion-text color="medium">Cargando...</ion-text>
        </div>
      </ion-content>
    </ion-modal>
  </ion-page>
</template>

<script setup>
import {
  IonPage,
  IonHeader,
  IonToolbar,
  IonTitle,
  IonContent,
  IonCard,
  IonCardHeader,
  IonCardTitle,
  IonCardContent,
  IonText,
  IonFab,
  IonFabButton,
  IonIcon,
  IonButton,
  IonButtons,
  IonModal,
  useIonRouter
} from '@ionic/vue'
import { 
  add,
  chevronBack,
  chevronForward,
  calendar,
  ellipsisVertical,
  cash,
  checkboxOutline,
  images,
  documentText,
  logOutOutline 
} from 'ionicons/icons'
import { ref, onMounted, computed } from 'vue'

// Router y estados
const router = useIonRouter()
const grupo = ref(null)
const eventos = ref([])
const participantes = ref([])
const eventoSeleccionado = ref(null)
const mostrarModal = ref(false)
const currentDate = ref(new Date())
const selectedDate = ref(new Date())

// Datos del usuario y grupo
const usuario = JSON.parse(localStorage.getItem('usuario'))
const grupoId = localStorage.getItem('grupoActivoId')

// --- Calendario ---
// Encabezado de días: iniciamos en lunes
const headerDays = ['LUN', 'MAR', 'MIÉ', 'JUE', 'VIE', 'SÁB', 'DOM']

// Título del mes (ej. "Agosto 2025")
const currentMonthYear = computed(() => {
  return currentDate.value.toLocaleDateString('es-ES', {
    month: 'long',
    year: 'numeric'
  }).replace(/^\w/, c => c.toUpperCase())
})

// Genera los días para el calendario iniciando en lunes
const calendarDays = computed(() => {
  const year = currentDate.value.getFullYear()
  const month = currentDate.value.getMonth()
  const firstDay = new Date(year, month, 1)
  // Si el primer día es domingo (0), lo tratamos como 6; de lo contrario, restamos 1.
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

// --- Filtrado de Eventos (solo próximos 7 días) ---
const upcomingEventos = computed(() => {
  const now = new Date()
  return eventos.value.filter(evento => {
    const eventDate = new Date(evento.fecha)
    // Diferencia en días
    const diffDays = (eventDate - now) / (1000 * 60 * 60 * 24)
    return diffDays >= 0 && diffDays < 7
  }).sort((a, b) => new Date(a.fecha) - new Date(b.fecha))
})

// --- Llamadas a la API ---
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

// --- Navegación y Selección ---
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
  let classes = []
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

// --- Funciones para Avatares de Participantes ---
const getInitials = (nombre, apellido) => {
  const first = nombre ? nombre.charAt(0).toUpperCase() : ''
  const last = apellido ? apellido.charAt(0).toUpperCase() : ''
  return first + last
}

const getAvatarColor = (texto) => {
  let hash = 0
  for (let i = 0; i < texto.length; i++) {
    hash = texto.charCodeAt(i) + ((hash << 5) - hash)
  }
  let color = '#'
  for (let i = 0; i < 3; i++) {
    const value = (hash >> (i * 8)) & 0xff
    color += ('00' + value.toString(16)).substr(-2)
  }
  return color
}

onMounted(async () => {
  grupo.value = { nombre: 'Cargando...' }
  await Promise.all([fetchGrupo(), fetchEventos(), fetchParticipantes()])
})
</script>

<style scoped>
/* Estilos para el calendario (estilo anterior) */
.calendar-card {
  margin-bottom: 16px;
}
.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.calendar-grid {
  display: flex;
  flex-direction: column;
}
.calendar-header-days {
  display: flex;
  justify-content: space-between;
  font-weight: bold;
  border-bottom: 1px solid #ccc;
  margin-bottom: 8px;
}
.day-header {
  flex: 1;
  text-align: center;
  padding: 4px;
}
.calendar-days {
  display: flex;
  flex-wrap: wrap;
}
.calendar-days > div {
  width: calc(100% / 7);
  text-align: center;
  padding: 10px 0;
  cursor: pointer;
  min-height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.other-month {
  color: #ccc;
}
.today {
  border: 2px solid var(--ion-color-primary);
  border-radius: 50%;
}
.selected {
  background-color: var(--ion-color-primary);
  color: #fff;
  border-radius: 50%;
}

/* Estilos para Eventos */
.events-card,
.quick-access-card {
  margin-bottom: 16px;
}
.events-header {
  display: flex;
  align-items: center;
  gap: 8px;
}
.event-item {
  padding: 8px 0;
  border-bottom: 1px solid #eee;
  cursor: pointer;
}
.event-content .event-title {
  font-size: 1rem;
  font-weight: bold;
}
.event-content .event-subtitle,
.event-content .event-description {
  font-size: 0.85rem;
  color: #555;
}

/* Estilos para Participantes */
.participants-header {
  display: flex;
  align-items: center;
  gap: 8px;
}
.participants-list {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
.participant-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 60px;
}

.participant-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  font-weight: bold;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  text-transform: uppercase;
}

.participant-name {
  margin-top: 4px;
  font-size: 0.75rem;
  color: #333;
  text-align: center;
}

/* Accesos Rápidos */
.quick-access-grid {
  display: flex;
  justify-content: space-around;
  align-items: center;
}
.quick-access-item {
  padding: 12px;
  cursor: pointer;
}
</style>
