<template>
  <ion-page>
    <ion-header>
      <ion-toolbar color="primary">
        <ion-title>{{ grupo?.nombre || 'Dashboard' }}</ion-title>
        <ion-buttons slot="end">
          <ion-button fill="clear" @click="() => router.push('/grupo')">
            <ion-icon :icon="logOutOutline" slot="icon-only"></ion-icon>
          </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>

    <ion-content :fullscreen="true" class="ion-padding">
      <!-- Calendario -->
      <ion-card class="calendar-card">
        <ion-card-header>
          <div class="calendar-header">
            <ion-button fill="clear" @click="previousMonth">
              <ion-icon :icon="chevronBack" slot="icon-only"></ion-icon>
            </ion-button>
            <ion-card-title class="month-title">{{ currentMonthYear }}</ion-card-title>
            <ion-button fill="clear" @click="nextMonth">
              <ion-icon :icon="chevronForward" slot="icon-only"></ion-icon>
            </ion-button>
          </div>
        </ion-card-header>
        <ion-card-content>
          <div class="calendar-grid">
            <div class="calendar-header-days">
              <div class="day-header">DOM</div>
              <div class="day-header">LUN</div>
              <div class="day-header">MAR</div>
              <div class="day-header">MIÉ</div>
              <div class="day-header">JUE</div>
              <div class="day-header">VIE</div>
              <div class="day-header">SÁB</div>
            </div>
            <div class="calendar-days">
              <div 
                v-for="day in calendarDays" 
                :key="day.date"
                :class="getDayClass(day)"
                @click="selectDay(day)"
              >
                {{ day.day }}
              </div>
            </div>
          </div>
        </ion-card-content>
      </ion-card>

      <!-- Sección de Eventos -->
      <ion-card class="events-card">
        <ion-card-header>
          <div class="events-header">
            <div class="event-icon">
              <ion-icon :icon="calendar" color="primary"></ion-icon>
            </div>
            <ion-card-title>Eventos</ion-card-title>
            <ion-button fill="clear" size="small">
              <ion-icon :icon="ellipsisVertical" slot="icon-only"></ion-icon>
            </ion-button>
          </div>
        </ion-card-header>
        <ion-card-content>
          <div v-if="eventos.length" class="events-list">
            <div v-for="evento in eventos.slice(0, 3)" :key="evento.id" class="event-item">
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

      <!-- Accesos rápidos (más compactos) -->
      <ion-card class="quick-access-card">
        <ion-card-content>
          <div class="quick-access-grid">
            <div class="quick-access-item" @click="goTo('gastos')">
              <ion-icon :icon="cash" color="primary"></ion-icon>
            </div>
            <div class="quick-access-item" @click="goTo('eventos')">
              <ion-icon :icon="calendar" color="primary"></ion-icon>
            </div>
            <div class="quick-access-item" @click="goTo('votaciones')">
              <ion-icon :icon="checkboxOutline" color="primary"></ion-icon>
            </div>
            <div class="quick-access-item" @click="goTo('galeria')">
              <ion-icon :icon="images" color="primary"></ion-icon>
            </div>
            <div class="quick-access-item" @click="goTo('notas')">
              <ion-icon :icon="documentText" color="primary"></ion-icon>
            </div>
          </div>
        </ion-card-content>
      </ion-card>
    </ion-content>

    <!-- FAB -->
    <ion-fab slot="fixed" vertical="bottom" horizontal="end">
      <ion-fab-button @click="goTo('crear')" color="primary">
        <ion-icon :icon="add"></ion-icon>
      </ion-fab-button>
    </ion-fab>
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

const router = useIonRouter()
const grupo = ref(null)
const eventos = ref([])
const currentDate = ref(new Date())
const selectedDate = ref(new Date())

const usuario = JSON.parse(localStorage.getItem('usuario'))
const grupoId = localStorage.getItem('grupoActivoId')

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
  const lastDay = new Date(year, month + 1, 0)
  const startDate = new Date(firstDay)
  startDate.setDate(startDate.getDate() - firstDay.getDay())
  
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

const previousMonth = () => {
  const newDate = new Date(currentDate.value)
  newDate.setMonth(newDate.getMonth() - 1)
  currentDate.value = newDate
}

const nextMonth = () => {
  const newDate = new Date(currentDate.value)
  newDate.setMonth(newDate.getMonth() + 1)
  currentDate.value = newDate
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

onMounted(async () => {
  grupo.value = { nombre: 'Cargando...' }
  await fetchEventos()
  await fetchGrupo()
})
</script>

<style scoped>
.calendar-card {
  margin-bottom: 16px;
}

.calendar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0;
}

.month-title {
  flex: 1;
  text-align: center;
  font-size: 1.2rem;
  color: #e74c3c;
}

.calendar-grid {
  width: 100%;
}

.calendar-header-days {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 8px;
  margin-bottom: 8px;
}

.day-header {
  text-align: center;
  font-size: 0.8rem;
  font-weight: 500;
  color: #666;
  padding: 4px;
}

.calendar-days {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
}

.calendar-day {
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.9rem;
  cursor: pointer;
  border-radius: 50%;
  transition: all 0.2s ease;
}

.calendar-day:hover {
  background-color: #f0f0f0;
}

.calendar-day.other-month {
  color: #ccc;
}

.calendar-day.today {
  background-color: #e74c3c;
  color: white;
  font-weight: bold;
}

.calendar-day.selected {
  background-color: #3498db;
  color: white;
}

.events-card {
  margin-bottom: 16px;
}

.events-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.event-icon {
  width: 40px;
  height: 40px;
  background-color: #f8f9fa;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.events-header ion-card-title {
  flex: 1;
  margin: 0;
}

.events-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.event-item {
  border-left: 3px solid #3498db;
  padding-left: 12px;
}

.event-title {
  margin: 0 0 4px 0;
  font-size: 1.1rem;
  font-weight: 600;
  color: #2c3e50;
}

.event-subtitle {
  margin: 0 0 8px 0;
  font-size: 0.9rem;
  color: #7f8c8d;
}

.event-description {
  margin: 0;
  font-size: 0.9rem;
  color: #95a5a6;
  line-height: 1.4;
}

.no-events {
  text-align: center;
  padding: 20px 0;
}

.quick-access-card {
  margin-bottom: 80px;
}

.quick-access-grid {
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 8px 0;
}

.quick-access-item {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f8f9fa;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.quick-access-item:hover {
  background-color: #e9ecef;
  transform: translateY(-2px);
}

.quick-access-item ion-icon {
  font-size: 24px;
}
</style>