<template>
  <ion-page>
    <PageHeader
      :title="store.grupo?.nombre || 'Dashboard'"
      showBack
      backHref="/grupo"
    >
      <template #end>
        <ion-button fill="clear" @click="goToConfig" aria-label="Configuración">
          <ion-icon :icon="settingsOutline" />
        </ion-button>
        <ion-button fill="clear" @click="goToLogout" aria-label="Cerrar sesión">
          <ion-icon :icon="logOutOutline" />
        </ion-button>
      </template>
    </PageHeader>

    <ion-content class="dashboard">
      <!-- 1. Estadísticas -->
      <section class="section stats">
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
      </section>

      <!-- 2. Calendario + Próximos eventos -->
      <section class="section calendar-events">
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
            fill="clear"
            class="view-all"
            @click="createEventOnDay"
          >
            Crear evento
          </ion-button>
          <ion-skeleton-text
            v-else
            animated
            style="width: 90%; height: 1.5rem; margin: 0.5rem auto"
          />
        </div>
      </section>

      <!-- 3. Participantes + Acciones rápidas -->
      <section class="section participants-actions">
        <div v-if="!store.loading">
          <ParticipantGrid :participants="store.participantes" />
          <QuickActions />
        </div>
        <div v-else class="loading-placeholder">
          <ion-skeleton-text animated style="width: 100%; height: 4rem" />
        </div>
      </section>

      <!-- 4. Botón flotante de añadir evento -->
      <ion-fab vertical="bottom" horizontal="end">
        <ion-fab-button color="primary" @click="createEvent">
          <ion-icon :icon="add" />
        </ion-fab-button>
      </ion-fab>

      <!-- 5. Modal de detalles de evento -->
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

      <!-- 6. Modal de estadísticas de usuarios -->
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

import {
  IonPage,
  IonContent,
  IonButton,
  IonIcon,
  IonSkeletonText,
  IonFab,
  IonFabButton
} from '@ionic/vue'
import { add, settingsOutline, logOutOutline, chevronBack } from 'ionicons/icons'


const store = useDashboardStore()
const {
  currentDate, selectedDate,headerDays, calendarDays,
  eventDates, eventsForSelectedDay, pastEventDates,
  previousMonth, nextMonth, selectDay
} = useCalendar()

const router = useRouter()
const grupoId = localStorage.getItem('grupoActivoId')!

// Estado local
const selectedEvent   = ref<EventoDTO | null>(null)
const showStatsModal  = ref(false)

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
const selectedDayHasEvents = computed(() => eventsForSelectedDay.value.length > 0)
const eventsToShow = computed(() =>
  daySelected.value
    ? eventsForSelectedDay.value
    : upcomingEventos.value
)

onMounted(() => {
  const usuarioGuardado = JSON.parse(localStorage.getItem('usuario')!)
  store.usuario = usuarioGuardado
  store.fetchAll(grupoId, usuarioGuardado.id)
})
// Handlers de eventos
async function openEvent(e: EventoDTO) {
  selectedEvent.value = await store.getEventoDetalle(String(e.id))
}
function closeModal() { selectedEvent.value = null }
function handleDelete(id: number) {
  store.deleteEvento(String(id))
  closeModal()
}
function handleEdit(e: EventoDTO) {
  router.push({ path: `/dashboard/${grupoId}/eventos`, query: { editar: e.id } })
}

// Navegación
function goToConfig()    { router.push({ name: 'configuracionGrupo', params: { grupoId } }) }
function goToGroupList() { router.push('/grupo') }
function goToLogout() {
  localStorage.clear()
  router.push('/login')
}
function createEvent()  {
  router.push(`/dashboard/${grupoId}/crear`)
}

function createEventOnDay(){
  const date = selectedDate.value.toISOString()
  router.push({ path: `/dashboard/${grupoId}/crear/evento`, query: { fecha: date } })
}


</script>

<style scoped lang="scss">
.dashboard {
  background: var(--ion-color-background);

  .section {
    padding: calc(var(--spacing-unit) * 2);
    & + .section { margin-top: calc(var(--spacing-unit) * 2); }
  }

  .stats {
    display: flex;
    justify-content: center;
  }

  .calendar-events {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: calc(var(--spacing-unit) * 2);
    @media (max-width: 768px) { grid-template-columns: 1fr; }

    .events-panel {
      display: flex;
      flex-direction: column;
      .view-all {
        align-self: flex-end;
        margin-top: var(--spacing-unit);
        font-size: var(--font-size-sm);
      }
    }
  }

  .participants-actions {
    display: grid;
    grid-template-columns: 2fr 1fr;
    gap: calc(var(--spacing-unit) * 2);
    @media (max-width: 768px) { grid-template-columns: 1fr; }
  }

  .loading-placeholder {
    display: flex;
    flex-direction: column;
    gap: var(--spacing-unit);
  }

  ion-fab-button {
    box-shadow: var(--box-shadow-md);
  }
}
</style>
