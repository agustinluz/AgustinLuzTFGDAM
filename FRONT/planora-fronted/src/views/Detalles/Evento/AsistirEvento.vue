<template>
  <ion-page>
    <ion-header>
      <ion-toolbar color="primary">
        <ion-buttons slot="start">
          <ion-button fill="clear" color="light" @click="irDashboard">
            <ion-icon name="arrow-back" />
          </ion-button>
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
         <ion-button fill="clear" size="small">
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
         <ion-segment v-model="segment" class="list-segment">
          <ion-segment-button value="asistentes">
            <ion-label>Asistentes</ion-label>
          </ion-segment-button>
          <ion-segment-button value="noasistentes">
            <ion-label>No asistentes</ion-label>
          </ion-segment-button>
        </ion-segment>
        <ion-list v-if="segment==='asistentes'">
          <ion-item v-for="a in asistentesConfirmados" :key="a.usuarioId">
            <ion-avatar slot="start" class="avatar" :title="a.nombre">
              <img v-if="a.fotoPerfil" :src="a.fotoPerfil" />
              <span v-else class="initials">{{ getInitials(a.nombre) }}</span>
            </ion-avatar>
            <ion-label>{{ a.nombre }}</ion-label>
          </ion-item>
        </ion-list>
        <ion-list v-else>
          <ion-item v-for="a in asistentesRechazados" :key="a.usuarioId">
            <ion-avatar slot="start" class="avatar" :title="a.nombre">
              <img v-if="a.fotoPerfil" :src="a.fotoPerfil" />
              <span v-else class="initials">{{ getInitials(a.nombre) }}</span>
            </ion-avatar>
            <ion-label>{{ a.nombre }}</ion-label>
          </ion-item>
        </ion-list>
        
        
      </div>
      <ion-toast
        :is-open="mostrarToast"
        :message="mensajeToast"
        :color="toastColor"
        :duration="2000"
        position="bottom"
        @did-dismiss="mostrarToast=false"
      />
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useIonRouter, IonPage, IonHeader, IonToolbar, IonTitle, IonContent, IonButtons, 
  IonSpinner, IonButton, IonIcon, 
  IonToast, IonSegment, IonSegmentButton, 
  IonLabel, IonList, IonItem, IonAvatar } from '@ionic/vue'
import { useRoute } from 'vue-router'
import { EventosService } from '@/service/EventoService'
import { getInitials } from '@/utils/string'

const route = useRoute()
const router = useIonRouter()
const grupoId = ref(route.params.grupoId || route.params.id || '')
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

  const irDashboard = () => {
  router.push(`/dashboard/${grupoId.value}/eventos`)
}

async function cargarEvento() {
  try {
    evento.value = await EventosService.obtenerEventoPorId(eventoId)
    if (!grupoId.value && evento.value) {
      grupoId.value = evento.value.grupoId
    }
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


</script>

<style scoped>
.loading { display: flex; justify-content: center; margin-top: 2rem; }
.content { display: flex; flex-direction: column; gap: 1rem; }
.list-segment { margin-top: 1rem; }
.volver-btn { margin-top: 1.5rem; }
.avatar {
  --size: 48px;
  background: var(--ion-color-primary-tint);
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  overflow: hidden;
}
.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.initials {
  font-weight: 600;
  color: #fff;
}
</style>