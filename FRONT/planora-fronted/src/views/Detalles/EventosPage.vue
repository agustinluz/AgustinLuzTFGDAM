<template>
  <ion-page>
    <ion-header>
      <ion-toolbar color="primary">
        <ion-buttons slot="start">
          <ion-back-button :default-href="`/dashboard/${grupoId}`"></ion-back-button>
        </ion-buttons>
        <ion-title>📅 Eventos del Grupo</ion-title>
        <ion-buttons slot="end">
          <ion-button @click="abrirModalCrear">
            <ion-icon name="add-outline"></ion-icon>
          </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding">
      <ion-refresher slot="fixed" @ion-refresh="manejarRefresh">
        <ion-refresher-content 
          pulling-text="Desliza para actualizar" 
          refreshing-spinner="circles" />
      </ion-refresher>

      <!-- Filtros -->
      <EventoFiltros
        v-model:filtro-texto="filtroTexto"
        v-model:filtro-fecha="filtroFecha"
        @filtrar="filtrarEventos"
      />

      <ion-spinner v-if="loading" name="crescent" class="spinner-center" />

      <!-- Lista de eventos -->
      <ion-list v-if="!loading && eventosFiltrados.length" lines="none">
        <EventoCard
          v-for="evento in eventosFiltrados"
          :key="evento.id"
          :evento="evento"
          @editar="editarEvento"
          @eliminar="confirmarEliminar"
        />
      </ion-list>

      <!-- Estado vacío -->
      <div v-if="!loading && !eventosFiltrados.length" class="empty-state">
        <ion-icon name="calendar-outline" class="empty-icon"></ion-icon>
        <h3>{{ eventos.length === 0 ? 'No hay eventos' : 'No se encontraron eventos' }}</h3>
        <p>{{ eventos.length === 0 ? 'Este grupo aún no tiene eventos registrados.' : 'Intenta cambiar los filtros de búsqueda.' }}</p>
        <ion-button v-if="eventos.length === 0" @click="abrirModalCrear" fill="outline">
          <ion-icon name="add-outline" slot="start"></ion-icon>
          Crear primer evento
        </ion-button>
      </div>

      <!-- Modal Crear/Editar -->
      <EventoModal
        :is-open="modalAbierto"
        :evento-editando="eventoEditando"
        :guardando="guardando"
        @cerrar="cerrarModal"
        @guardar="guardarEvento"
      />

      <!-- Toast messages -->
      <ion-toast
        :is-open="toast.mostrar"
        :message="toast.mensaje"
        :duration="2500"
        :color="toast.color"
        @did-dismiss="toast.mostrar = false"
      />

      <!-- Alert de confirmación -->
      <ion-alert
        :is-open="alertEliminar.mostrar"
        header="Confirmar eliminación"
        :message="`¿Estás seguro de que quieres eliminar el evento '${alertEliminar.evento?.titulo}'?`"
        :buttons="[
          { text: 'Cancelar', role: 'cancel' },
          { text: 'Eliminar', handler: () => eliminarEventoConfirmado() }
        ]"
        @did-dismiss="alertEliminar.mostrar = false"
      />
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import {
  IonPage,
  IonHeader,
  IonToolbar,
  IonTitle,
  IonContent,
  IonButtons,
  IonBackButton,
  IonButton,
  IonIcon,
  IonSpinner,
  IonRefresher,
  IonRefresherContent,
  IonList,
  IonToast,
  IonAlert
} from '@ionic/vue'

// Componentes
import EventoFiltros from './Evento/EventoFiltro.vue'
import EventoCard from './Evento/EventoCard.vue'
import EventoModal from './Evento/EventoModal.vue'

// Composables y servicios
import { useEventos } from './Evento/Composable/UseEventos'
import type { Evento, EventoCrear } from '@/service/EventoService'

const route = useRoute()
const grupoId = parseInt(route.params.id as string)
const token = localStorage.getItem('token') || ''
const {
  eventos,
  eventosFiltrados,
  loading,
  guardando,
  filtroTexto,
  filtroFecha,
  cargarEventos,
  crearEvento,
  actualizarEvento,
  eliminarEvento
} = useEventos(grupoId, token)


// Estado local del componente
const modalAbierto = ref(false)
const eventoEditando = ref<Evento | null>(null)

const toast = ref({
  mostrar: false,
  mensaje: '',
  color: 'success'
})

const alertEliminar = ref({
  mostrar: false,
  evento: null as Evento | null
})

// Métodos del modal
const abrirModalCrear = () => {
  eventoEditando.value = null
  modalAbierto.value = true
}

const editarEvento = (evento: Evento) => {
  eventoEditando.value = evento
  modalAbierto.value = true
}

const cerrarModal = () => {
  modalAbierto.value = false
  eventoEditando.value = null
}

// Guardar evento (crear o actualizar)
const guardarEvento = async (formData: any) => {
  try {
    const eventoData: EventoCrear = {
      titulo: formData.titulo,
      descripcion: formData.descripcion,
      ubicacion: formData.ubicacion,
      fecha: new Date(formData.fecha)
    }

    if (eventoEditando.value) {
      await actualizarEvento(eventoEditando.value.id, eventoData)
      mostrarToast('Evento actualizado correctamente')
    } else {
      await crearEvento(eventoData)
      mostrarToast('Evento creado correctamente')
    }
    
    cerrarModal()
  } catch (error) {
    mostrarToast('Error al guardar el evento', 'danger')
  }
}

// Eliminar evento
const confirmarEliminar = (evento: Evento) => {
  alertEliminar.value = {
    mostrar: true,
    evento: evento
  }
}

const eliminarEventoConfirmado = async () => {
  try {
    if (alertEliminar.value.evento) {
      await eliminarEvento(alertEliminar.value.evento.id)
      mostrarToast('Evento eliminado correctamente')
    }
  } catch (error) {
    mostrarToast('Error al eliminar el evento', 'danger')
  }
  
  alertEliminar.value.mostrar = false
}

// Utilidades
const mostrarToast = (mensaje: string, color: string = 'success') => {
  toast.value = {
    mostrar: true,
    mensaje,
    color
  }
}

const manejarRefresh = async (ev: any) => {
  try {
    await cargarEventos()
  } catch (error) {
    mostrarToast('Error al cargar los eventos', 'danger')
  } finally {
    ev.target.complete()
  }
}

const filtrarEventos = () => {
  // Los eventos se filtran automáticamente a través del computed
  // en el composable useEventos
}

// Inicialización
onMounted(async () => {
  try {
    await cargarEventos()
  } catch (error) {
    mostrarToast('Error al cargar los eventos', 'danger')
  }
})
</script>

<style scoped>
.spinner-center {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 2rem;
}

.empty-state {
  text-align: center;
  padding: 3rem 1rem;
  color: var(--ion-color-medium);
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}
</style>