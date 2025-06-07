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

        <ion-list v-if="notasFiltradas.length">
          <NotaCard
            v-for="nota in notasFiltradas"
            :key="nota.id"
            :nota="nota"
            :esProietario="esProietario(nota)"
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
  IonList, IonText, IonSpinner, IonSearchbar, IonIcon, IonAlert
} from '@ionic/vue'
import { addOutline } from 'ionicons/icons'
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useNotas } from './Nota/Composable/UseNotas'
import NotaCard from './Nota/NotaCard.vue'
import NotaDetalle from './Nota/NotaDetalle.vue'
import NotaEmptyState from './Nota/NotaEmptyState.vue'

const route = useRoute()
const router = useRouter()
const grupoId = Number(route.params.id)

const usuarioActual = ref(null)
const notaSeleccionada = ref(null)
const mostrarModalVer = ref(false)
const mostrarAlertaEliminar = ref(false)
const notaPendienteEliminar = ref(null)

const {
  notasFiltradas,
  busqueda,
  cargando,
  cargarNotas,
  eliminarNota
} = useNotas(grupoId)

onMounted(() => {
  cargarNotas()
  const userData = localStorage.getItem('usuario')
  if (userData) usuarioActual.value = JSON.parse(userData)
})

const esProietario = (nota) => {
  return usuarioActual.value && nota.creadaPorId === usuarioActual.value.id
}

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
