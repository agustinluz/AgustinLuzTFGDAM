<template>
  <ion-page>
    <PageHeader
      :title="store.grupo?.nombre || 'Dashboard'" :showMenu="true">
      <template #end>
        <ion-button fill="clear" @click="goToConfig" aria-label="Configuración">
          <ion-icon :icon="settingsOutline" />
        </ion-button>
        <ion-button fill="clear" @click="goToLogout" aria-label="Cerrar sesión">
          <ion-icon :icon="logOutOutline" />
        </ion-button>
      </template>
    </PageHeader>

    <ion-content>
      <!-- 1. Estadísticas -->
      <ion-card class="dashboard-section">
        <ion-card-header>
          <ion-card-title>Estadísticas del Grupo</ion-card-title>
        </ion-card-header>
        <ion-card-content>
          <StatsGrid
            v-if="!store.loading"
            :membersCount="store.participantes.length"
            :upcomingCount="upcomingEventos.length"
          />
          <ion-skeleton-text
            v-else
            animated
            style="width: 70%; height: 2rem; margin: 1rem auto"
          />
        </ion-card-content>
      </ion-card>

      <!-- 2. Calendario y eventos -->
      <ion-card class="dashboard-section">
        <ion-card-header>
          <ion-card-title>Calendario y Eventos</ion-card-title>
        </ion-card-header>
        <ion-card-content class="calendar-grid">
          <CompactCalendar
            v-if="!store.loading"
            :headerDays="headerDays"
            :currentMonthYear="currentMonthYear"
            :calendarDays="calendarDays"
            :eventDates="eventDates"
            :pastEventDates="pastEventDates"
            @prev-month="previousMonth"
            @next-month="nextMonth"
            @select-day="selectDay"
          />
          <div class="events-panel">
            <EventsList
              v-if="!store.loading"
              :events="eventsToShow"
              :limit="4"
              @view-event="openEvent"
            />
            <ion-button
              v-if="!store.loading && daySelected"
              fill="outline"
              expand="block"
              @click="createEventOnDay"
            >
              Crear evento este día
            </ion-button>
          </div>
        </ion-card-content>
      </ion-card>

      <!-- 3. Participantes y acciones -->
      <ion-card class="dashboard-section">
        <ion-card-header>
          <ion-card-title>Participantes y Acciones</ion-card-title>
        </ion-card-header>
        <ion-card-content>
          <div v-if="!store.loading">
            <ParticipantGrid :participants="store.participantes" />
            <QuickActions />  
          </div>
          <div v-else class="loading-placeholder">
            <ion-skeleton-text animated style="width: 100%; height: 4rem" />
          </div>
        </ion-card-content>
      </ion-card>

      <!-- Botón flotante con drawer de acciones -->
      <ion-fab vertical="bottom" horizontal="end">
        <ion-fab-button color="primary" @click="showFabSheet = true">
          <ion-icon :icon="add" />
        </ion-fab-button>
      </ion-fab>
      <ion-action-sheet
        :is-open="showFabSheet"
        header="Crear"
        :buttons="fabButtons"
        @did-dismiss="showFabSheet = false"
      />
      

      <!-- Modales -->
      <EventModal
        v-if="selectedEvent"
        :visible="!!selectedEvent"
        :event="selectedEvent"
        :rolUsuario="store.usuario?.role ?? 'member'"
        :idUsuario="store.usuario?.id ?? ''"
        @close="closeModal"
        @delete-event="handleDelete"
        @edit-event="handleEdit"
      />
      <UserStatsModal
        :abierto="showStatsModal"
        :stats="store.userStats"
        @close="showStatsModal = false"
      />
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
import PageHeader       from '@/components/PageHeader.vue'
import StatsGrid        from '@/views/Components/Dashboard/StatsGrid.vue'
import CompactCalendar  from '@/views/Components/Dashboard/CompactCalendar.vue'
import EventsList       from '@/views/Components/Dashboard/EventList.vue'
import ParticipantGrid  from '@/views/Components/Dashboard/ParticipantGrid.vue'
import QuickActions     from '@/views/Components/Dashboard/QuickAction.vue'
import EventModal       from '@/views/Components/Dashboard/EventModal.vue'
import UserStatsModal   from '@/views/Components/Dashboard/UserStatsModal.vue'

// Ionic
import {
  IonPage,
  IonContent,
  IonButton,
  IonIcon,
  IonSkeletonText,
  IonFab,
  IonFabButton,
  IonActionSheet,
  IonCard,
  IonCardHeader,
  IonCardTitle,
  IonCardContent
} from '@ionic/vue'

import { add, settingsOutline, logOutOutline,  calendarOutline, cash } from 'ionicons/icons'

const store = useDashboardStore()
const {
  currentDate, selectedDate, headerDays, calendarDays,
  eventDates, eventsForSelectedDay, pastEventDates,
  previousMonth, nextMonth, selectDay
} = useCalendar()

const router = useRouter()
const grupoId = localStorage.getItem('grupoActivoId')!
const selectedEvent = ref<EventoDTO | null>(null)
const showStatsModal = ref(false)
const showFabSheet = ref(false)

const currentMonthYear = computed(() =>
  currentDate.value.toLocaleString('default', { month: 'long', year: 'numeric' })
)

const upcomingEventos = computed(() =>
  store.eventos.filter(e => {
    const dt = new Date(e.fecha).getTime() - Date.now()
    return dt >= 0 && dt < 7 * 24 * 60 * 60 * 1000
  })
)
const daySelected = computed(() => !!selectedDate.value)
const eventsToShow = computed(() =>
  daySelected.value ? eventsForSelectedDay.value : upcomingEventos.value
)

onMounted(() => {
  const usuarioGuardado = JSON.parse(localStorage.getItem('usuario')!)
  store.usuario = usuarioGuardado
  store.fetchAll(grupoId, usuarioGuardado.id)
})

// Acciones
function openEvent(e: EventoDTO) {
  store.getEventoDetalle(String(e.id)).then(ev => selectedEvent.value = ev)
}
function closeModal() { selectedEvent.value = null }
function handleDelete(id: number) {
  store.deleteEvento(String(id))
  closeModal()
}
function handleEdit(e: EventoDTO) {
  router.push({ path: `/dashboard/${grupoId}/eventos`, query: { editar: e.id } })
}
function goToConfig() {
  router.push({ name: 'configuracionGrupo', params: { grupoId } })
}
function goToLogout() {
  localStorage.clear()
  router.push('/login')
}
function createEvent() {
  router.push(`/dashboard/${grupoId}/crear/evento`)
}
function createGasto() {
  router.push(`/dashboard/${grupoId}/crear/gasto`)
}
function createEventOnDay() {
  const date = selectedDate.value.toISOString()
  router.push({ path: `/dashboard/${grupoId}/crear/evento`, query: { fecha: date } })
}
const fabButtons = [
  {
    text: 'Crear Evento',
    icon: calendarOutline,
    handler: () => createEvent()
  },
  {
    text: 'Crear Gasto',
    icon: cash,
    handler: () => createGasto()
  },
  {
    text: 'Cancelar',
    role: 'cancel'
  }
]
</script>

<style scoped lang="scss">
.dashboard {
  background: var(--ion-color-background);

  .dashboard-section {
    margin-bottom: 1.5rem;
  }

  .calendar-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1.5rem;

    @media (max-width: 768px) {
      grid-template-columns: 1fr;
    }

    .events-panel {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
    }
  }

  .loading-placeholder {
    display: flex;
    flex-direction: column;
    gap: 1rem;
  }

  .fab-label {
    font-size: 0.75rem;
    margin-left: 0.25rem;
  }

  ion-fab-button {
    box-shadow: var(--box-shadow-md);
  }
}
</style>
