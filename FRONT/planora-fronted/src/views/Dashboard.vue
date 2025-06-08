<template>
  <ion-page>
    <AppHeader :title="store.grupo?.nombre || 'Dashboard'" @back="goToGroupList" @config="goToConfig" @logout="goToLogout" />
    <ion-content fullscreen>
      <ion-grid class="dashboard-grid">
        <!-- Estadísticas -->
        <ion-row>
          <ion-col size="12" size-md="6" size-xl="4">
            <template v-if="store.loading">
              <ion-card>
                <ion-card-content>
                  <ion-skeleton-text animated style="width: 60%; height: 2rem; margin-bottom: .5rem" />
                  <ion-skeleton-text animated style="width: 40%; height: 1.5rem" />
                </ion-card-content>
              </ion-card>
            </template>
            <template v-else>
              <StatsGrid :membersCount="store.participantes.length" :upcomingCount="upcomingEventos.length" @show-stats="loadUserStats" />
            </template>
          </ion-col>
        </ion-row>

        <!-- Calendario compacto + Lista de eventos -->
        <ion-row>
          <ion-col size="12" size-lg="6">
            <transition name="fade-up" mode="out-in">
              <template v-if="!store.loading" key="calendar">
                <CompactCalendar :headerDays="headerDays" :currentMonthYear="currentMonthYear"
                  :calendarDays="calendarDays" :eventDates="eventDates" @prev-month="previousMonth"
                  @next-month="nextMonth" @select-day="selectDay" />
              </template>
            </transition>
          </ion-col>
          <ion-col size="12" size-lg="6">
            <transition-group name="list-stagger" tag="div">
              <template v-if="!store.loading" key="events">
                <EventsList :events="eventsForSelectedDay.length
                  ? eventsForSelectedDay
                  : upcomingEventos" :limit="3" @view-event="openEvent" />
              </template>
            </transition-group>
          </ion-col>
        </ion-row>

        <!-- Participantes + Acciones rápidas -->
        <ion-row>
          <ion-col size="12" size-md="6">
            <template v-if="store.loading">
              <ion-skeleton-text animated style="width: 100%; height: 4rem" />
            </template>
            <template v-else>
              <ParticipantGrid :participants="store.participantes" />
            </template>
          </ion-col>
          <ion-col size="12" size-md="6">
            <template v-if="store.loading">
              <ion-skeleton-text animated style="width: 100%; height: 4rem" />
            </template>
            <template v-else>
              <QuickActions />
            </template>
          </ion-col>
        </ion-row>
      </ion-grid>

      <!-- Modal de evento -->
      <EventModal v-if="selectedEvent" :event="selectedEvent" :visible="!!selectedEvent"
        :userRole="store.usuario?.role ?? 'member'" :userId="store.usuario?.id ?? ''" @close="closeModal"
        @delete-event="handleDelete" @edit-event="handleEdit" />

      <!-- Botón Agregar -->
      <ion-fab slot="fixed" vertical="bottom" horizontal="end">
        <ion-fab-button color="primary" @click="createEvent">
          <ion-icon :icon="add" />
        </ion-fab-button>
      </ion-fab>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useDashboardStore } from '@/store/dashboardStore'
import { useCalendar } from '@/Composable/useCalendar'
import type { EventoDTO } from '@/service/DashboardService'

// Componentes
import AppHeader from '@/views/Components/Dashboard/AppHeader.vue'
import StatsGrid from '@/views/Components/Dashboard/StatsGrid.vue'
import CompactCalendar from '@/views/Components/Dashboard/CompactCalendar.vue'
import EventsList from '@/views/Components/Dashboard/EventList.vue'
import ParticipantGrid from '@/views/Components/Dashboard/ParticipantGrid.vue'
import QuickActions from '@/views/Components/Dashboard/QuickAction.vue'
import EventModal from '@/views/Components/Dashboard/EventModal.vue'

// Ionic
import {
  IonPage, IonContent, IonGrid, IonRow, IonCol,
  IonFab, IonFabButton, IonIcon, IonCardContent, IonCard
} from '@ionic/vue'
import { IonSkeletonText } from '@ionic/vue'
import { add } from 'ionicons/icons'

// Store & Composable
const store = useDashboardStore()
const {
  currentDate,
  selectedDate,
  headerDays,
  calendarDays,
  eventDates,
  eventsForSelectedDay,
  previousMonth,
  nextMonth,
  selectDay
} = useCalendar()

// Router
const router = useRouter()
const grupoId = localStorage.getItem('grupoActivoId')!

// Modal state
const selectedEvent = ref<EventoDTO | null>(null)

// Formatted month-year
const currentMonthYear = computed(() =>
  currentDate.value.toLocaleString('default', { month: 'long', year: 'numeric' })
)

// Upcoming this week
const upcomingEventos = computed(() =>
  store.eventos.filter(e => {
    const delta = new Date(e.fecha).getTime() - Date.now()
    return delta >= 0 && delta < 7 * 24 * 60 * 60 * 1000
  })
)

// Initial data load
onMounted(() => {
  const user = JSON.parse(localStorage.getItem('usuario')!)
  store.usuario = user
  store.fetchAll(grupoId, user.id)
})

// Handlers
async function openEvent(e: EventoDTO) {
  selectedEvent.value = await store.getEventoDetalle(String(e.id))
}

function closeModal() {
  selectedEvent.value = null
}

function handleDelete(id: number) {
  store.deleteEvento(String(id))
  closeModal()
}

function handleEdit(e: EventoDTO) {
  router.push({ name: 'event-edit', params: { id: e.id } })
}

// Navegación
function goToConfig() {
  router.push({ name: 'configuracionGrupo', params: { grupoId } })
}
function goToGroupList() {
  router.push('/grupo')
}
function goToLogout() {
  router.push({ name: 'group-list' })
}
function goToSection(route: string) {
  router.push({ name: route, params: { grupoId } })
}
function createEvent() {
  router.push({ name: 'event-create', params: { grupoId } })
}
</script>

<style scoped lang="scss">
.dashboard-grid {
  --ion-grid-padding: 1.5rem;
  --ion-grid-column-padding: 1rem;
  --ion-grid-row-padding: 1rem;
}

.fade-up-enter-active {
  transition: all 0.3s ease;
}

.fade-up-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.list-stagger-enter-active {
  transition: all 0.25s ease;
}

.list-stagger-enter-from {
  opacity: 0;
  transform: translateY(5px);
}

.list-stagger-enter-to {
  opacity: 1;
  transform: translateY(0);
}

.list-stagger-enter-active>* {
  transition-delay: calc(var(--enter-index) * 50ms);
}
</style>
