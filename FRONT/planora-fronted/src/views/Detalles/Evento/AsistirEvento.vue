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
        <ion-button expand="block" color="primary" @click="marcar(true)" :disabled="enviando">
          Confirmar asistencia
        </ion-button>
        <ion-button expand="block" fill="outline" @click="marcar(false)" :disabled="enviando">
          No asistir
        </ion-button>
      </div>
      <ion-toast :is-open="mostrarToast" :message="mensajeToast" :color="toastColor" @did-dismiss="mostrarToast=false" />
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useIonRouter, IonPage, IonHeader, IonToolbar, IonTitle, IonContent, IonButtons, IonBackButton, IonSpinner, IonButton, IonToast } from '@ionic/vue'
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
    setTimeout(() => router.back(), 1500)
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
</style>