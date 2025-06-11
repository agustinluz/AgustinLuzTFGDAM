<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-buttons slot="start">
          <ion-back-button default-href="/grupo" />
        </ion-buttons>
        <ion-title>Notas del Grupo</ion-title>
        <ion-buttons slot="end">
          <ion-button @click="irACrearNota" fill="clear">
            <ion-icon :icon="addOutline" slot="icon-only" />
          </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>

    <ion-content>
      <div v-if="cargando" class="loading-container">
        <ion-spinner />
        <ion-text>Cargando notas...</ion-text>
      </div>

      <div v-else>
        <ion-searchbar v-model="busqueda" placeholder="Buscar notas..." class="ion-margin-bottom" />
        <ion-item class="ion-margin-bottom">
          <ion-label position="stacked">Filtrar por evento</ion-label>
          <ion-select v-model="eventoId" interface="popover" placeholder="Todos">
            <ion-select-option :value="null">Todos los eventos</ion-select-option>
            <ion-select-option v-for="ev in eventos" :key="ev.id" :value="ev.id">
              {{ ev.titulo }}
            </ion-select-option>
          </ion-select>
        </ion-item>

        <ion-list v-if="notasFiltradas.length">
          <NotaCard
            v-for="nota in notasFiltradas"
            :key="nota.id"
            :nota="nota"
            @editar="editarNota"
            @eliminar="confirmarEliminar"
            @ver="verNotaCompleta"
          />
        </ion-list>

        <NotaEmptyState v-else @crear="irACrearNota" />
      </div>

      <!-- Modal Detalle Nota -->
      <NotaDetalle
        :visible="mostrarModalVer"
        :nota="notaSeleccionada"
        @cerrar="cerrarDetalle"
      />

      <!-- Alerta de eliminación -->
      <ion-alert
        :is-open="mostrarAlertaEliminar"
        header="Eliminar nota"
        :message="`¿Seguro que quieres eliminar la nota <strong>${notaPendienteEliminar?.titulo}</strong>?`"
        :buttons="[
          {
            text: 'Cancelar',
            role: 'cancel',
            handler: () => {
              mostrarAlertaEliminar.value = false
            }
          },
          {
            text: 'Eliminar',
            role: 'destructive',
            handler: eliminarNotaSeleccionada
          }
        ]"
      />
    </ion-content>
  </ion-page>
</template>

<script setup>
import {
  IonPage, IonHeader, IonToolbar, IonTitle, IonContent, IonButtons, IonButton, IonBackButton,
  IonList, IonText, IonSpinner, IonSearchbar, IonIcon, IonAlert, IonSelect, IonSelectOption, IonItem, IonLabel
} from '@ionic/vue'
import { addOutline } from 'ionicons/icons'
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useNotas } from './Nota/Composable/UseNotas'
import { EventosService } from '@/service/EventoService'
import NotaCard from './Nota/NotaCard.vue'
import NotaDetalle from './Nota/NotaDetalle.vue'
import NotaEmptyState from './Nota/NotaEmptyState.vue'

const route = useRoute()
const router = useRouter()
const grupoId = Number(route.params.id)

const eventoId = ref<null>(null)
const eventos = ref([])

const usuarioActual = ref(null)
const notaSeleccionada = ref(null)
const mostrarModalVer = ref(false)
const mostrarAlertaEliminar = ref(false)
const notaPendienteEliminar = ref(null)

watch(eventoId, async () => {
  await cargarNotas()
})

const {
  notasFiltradas,
  busqueda,
  cargando,
  cargarNotas,
  eliminarNota
} = useNotas(grupoId, eventoId)

onMounted(async () => {
  await cargarNotas()
  try {
    eventos.value = await EventosService.obtenerEventosGrupo(grupoId)
  } catch (e) {
    eventos.value = []
  }
  const datosUsuario  = localStorage.getItem('usuario')
  if (datosUsuario ) usuarioActual.value = JSON.parse(datosUsuario )
})


const irACrearNota = () => {
  router.push(`/dashboard/${grupoId}/notas/crear`)
}

const editarNota = (nota) => {
  router.push(`/dashboard/${grupoId}/notas/editar/${nota.id}`)
}

const verNotaCompleta = (nota) => {
  notaSeleccionada.value = nota
  mostrarModalVer.value = true
}

const cerrarDetalle = () => {
  mostrarModalVer.value = false
  notaSeleccionada.value = null
}

const confirmarEliminar = (nota) => {
  notaPendienteEliminar.value = nota
  mostrarAlertaEliminar.value = true
}

const eliminarNotaSeleccionada = async () => {
  try {
    const token = localStorage.getItem('token')
    if (!token) throw new Error('Token no encontrado')
    await eliminarNota(notaPendienteEliminar.value.id, token)
  } catch (error) {
    console.error('Error al eliminar:', error)
  } finally {
    mostrarAlertaEliminar.value = false
    notaPendienteEliminar.value = null
    await cargarNotas() // recargar tras eliminar
  }
}
</script>

<style scoped>
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  gap: 16px;
}
</style>
