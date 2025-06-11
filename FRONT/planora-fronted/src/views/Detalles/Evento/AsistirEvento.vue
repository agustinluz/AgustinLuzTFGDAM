<template>
  <ion-page>
    <ion-header>
      <ion-toolbar color="primary">
        <ion-buttons slot="start">
          <ion-back-button :default-href="`/dashboard/${grupoId}/eventos`" />
        </ion-buttons>
        <ion-title>Asistencia</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding">
      <div v-if="!evento" class="loading">
        <ion-spinner name="crescent" />
      </div>
      <div v-else class="content">
        <h2>{{ evento.titulo }}</h2>
        <p>{{ formatFecha(evento.fecha) }}</p>
        <p v-if="evento.descripcion">{{ evento.descripcion }}</p>
         <ion-button fill="clear" size="small" @click="modalOpen = true">
          🎉 {{ asistentesConfirmados.length }}
        </ion-button>
        <ion-button
          v-if="!asistenciaUsuario || !asistenciaUsuario.asistio"
          expand="block"
          color="primary"
          @click="marcar(true)"
          :disabled="enviando"
        >
          Confirmar asistencia
        </ion-button>
        <ion-button
          v-else
          expand="block"
          color="warning"
          @click="marcar(false)"
          :disabled="enviando"
        >
          Cambiar asistencia
        </ion-button>
        
      </div>
      <ion-toast :is-open="mostrarToast" :message="mensajeToast" :color="toastColor" @did-dismiss="mostrarToast=false" />
       <ion-modal :is-open="modalOpen" @will-dismiss="modalOpen=false">
        <ion-header>
          <ion-toolbar>
            <ion-title>Asistentes</ion-title>
          </ion-toolbar>
        </ion-header>
        <ion-content class="ion-padding">
          <ion-segment v-model="segment">
            <ion-segment-button value="asistentes">
              <ion-label>Asistentes</ion-label>
            </ion-segment-button>
            <ion-segment-button value="noasistentes">
              <ion-label>No asistentes</ion-label>
            </ion-segment-button>
          </ion-segment>
          <ion-list v-if="segment==='asistentes'">
            <ion-item v-for="a in asistentesConfirmados" :key="a.usuarioId">
              <ion-avatar slot="start">
                <img :src="avatarUrl(a.usuarioId)" />
              </ion-avatar>
              <ion-label>{{ a.nombre }}</ion-label>
            </ion-item>
          </ion-list>
          <ion-list v-else>
            <ion-item v-for="a in asistentesRechazados" :key="a.usuarioId">
              <ion-avatar slot="start">
                <img :src="avatarUrl(a.usuarioId)" />
              </ion-avatar>
              <ion-label>{{ a.nombre }}</ion-label>
            </ion-item>
          </ion-list>
        </ion-content>
      </ion-modal>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useIonRouter, IonPage, IonHeader, IonToolbar, IonTitle, IonContent, IonButtons, 
  IonBackButton, IonSpinner, IonButton, 
  IonToast, IonModal, IonSegment, IonSegmentButton, 
  IonLabel, IonList, IonItem, IonAvatar } from '@ionic/vue'
import { useRoute } from 'vue-router'
import { EventosService } from '@/service/EventoService'

const route = useRoute()
const router = useIonRouter()
const grupoId = route.params.grupoId || route.params.id || ''
const eventoId = Number(route.params.eventoId || route.params.id)
const token = localStorage.getItem('token') || ''

const evento = ref<any|null>(null)
const enviando = ref(false)
const mostrarToast = ref(false)
const mensajeToast = ref('')
const toastColor = ref('success')
const asistentes = ref<any[]>([])
const modalOpen = ref(false)
const segment = ref('asistentes')
const usuario = JSON.parse(localStorage.getItem('usuario') || '{}')

const asistentesConfirmados = computed(() => asistentes.value.filter(a => a.asistio))
const asistentesRechazados = computed(() => asistentes.value.filter(a => !a.asistio))
const asistenciaUsuario = computed(() => asistentes.value.find(a => a.usuarioId === usuario.id))

const formatFecha = (iso: string) =>
  new Date(iso).toLocaleString('es-ES', {
    day: '2-digit',
    month: 'long',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })

async function cargarEvento() {
  try {
    evento.value = await EventosService.obtenerEventoPorId(eventoId)
    asistentes.value = await EventosService.obtenerAsistentes(eventoId)
  } catch (e) {
    mensajeToast.value = 'Error al cargar evento'
    toastColor.value = 'danger'
    mostrarToast.value = true
  }
}

async function marcar(asistio: boolean) {
  if (enviando.value) return
  enviando.value = true
  try {
    await EventosService.marcarAsistencia(eventoId, asistio, token)
    mensajeToast.value = 'Asistencia registrada'
    toastColor.value = 'success'
    mostrarToast.value = true
    await cargarEvento()
  } catch (e) {
    mensajeToast.value = 'Error al registrar'
    toastColor.value = 'danger'
    mostrarToast.value = true
  } finally {
    enviando.value = false
  }
}

onMounted(cargarEvento)

function avatarUrl(id: number) {
  return `https://i.pravatar.cc/64?u=${id}`
}
</script>

<style scoped>
.loading { display: flex; justify-content: center; margin-top: 2rem; }
.content { display: flex; flex-direction: column; gap: 1rem; }
</style>