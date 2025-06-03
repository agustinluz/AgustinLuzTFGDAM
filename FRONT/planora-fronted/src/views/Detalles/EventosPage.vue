<template>
  <ion-page>
    <ion-header>
      <ion-toolbar color="primary">
        <ion-title>📅 Eventos del Grupo</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding">
      <ion-refresher slot="fixed" @ionRefresh="cargarEventos">
        <ion-refresher-content pulling-text="Desliza para actualizar" refreshing-spinner="circles" />
      </ion-refresher>

      <ion-spinner v-if="loading" name="crescent" class="spinner-center" />

      <ion-list v-if="!loading && eventos.length" lines="none">
        <ion-card v-for="evento in eventos" :key="evento.id" class="evento-card">
          <ion-card-header class="evento-header">
            <div class="evento-header-left">
              <ion-icon name="calendar-outline" class="evento-icon" />
              <div>
                <ion-card-title>{{ evento.titulo }}</ion-card-title>
                <ion-card-subtitle class="grupo-nombre">
                  <ion-icon name="people-outline" class="icon-sub" />
                  {{ evento.grupoNombre }}
                </ion-card-subtitle>
              </div>
            </div>
            <div class="evento-fecha">
              <ion-icon name="time-outline" class="icon-sub" />
              {{ formatFecha(evento.fecha) }}
            </div>
          </ion-card-header>

          <ion-card-content class="evento-content">
            <p class="descripcion">{{ evento.descripcion }}</p>
            <p v-if="evento.ubicacion" class="ubicacion">
              <ion-icon name="location-outline" class="icon-sub" />
              {{ evento.ubicacion }}
            </p>
          </ion-card-content>
        </ion-card>
      </ion-list>

      <ion-text color="medium" v-if="!loading && !eventos.length">
        <p class="ion-text-center">Este grupo aún no tiene eventos registrados.</p>
      </ion-text>

      <ion-toast
        :is-open="error"
        message="Ocurrió un error al cargar los eventos."
        duration="2500"
        color="danger"
        @didDismiss="error = false"
      />
    </ion-content>
  </ion-page>
</template>

<script setup>
import {
  IonPage, IonHeader, IonToolbar, IonTitle, IonContent,
  IonCard, IonCardHeader, IonCardTitle, IonCardSubtitle, IonCardContent,
  IonList, IonIcon, IonText, IonSpinner, IonRefresher, IonRefresherContent, IonToast
} from '@ionic/vue'
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'

const eventos = ref([])
const loading = ref(true)
const error = ref(false)
const route = useRoute()
const grupoId = route.params.id

const formatFecha = (fecha) => {
  const opciones = {
    weekday: 'long', day: 'numeric', month: 'long',
    year: 'numeric', hour: '2-digit', minute: '2-digit'
  }
  return new Date(fecha).toLocaleDateString('es-ES', opciones)
}

const cargarEventos = async (ev = null) => {
  try {
    loading.value = true
    const res = await fetch(`${import.meta.env.VITE_API_URL}/eventos/${grupoId}/eventos`)
    if (!res.ok) throw new Error()
    const data = await res.json()
    eventos.value = data.map(e => ({
      id: e.id,
      titulo: e.titulo,
      descripcion: e.descripcion,
      ubicacion: e.ubicacion,
      fecha: e.fecha,
      grupoId: e.grupoId,
      grupoNombre: e.grupoNombre
    }))
  } catch (e) {
    error.value = true
  } finally {
    loading.value = false
    if (ev) ev.target.complete()
  }
}

onMounted(() => {
  cargarEventos()
})
</script>

<style scoped>
.spinner-center {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 2rem;
}

.evento-card {
  margin-bottom: 1.2rem;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.07);
  transition: transform 0.2s ease;
}

.evento-card:hover {
  transform: scale(1.01);
}

.evento-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 0;
}

.evento-header-left {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.evento-icon {
  font-size: 2rem;
  color: var(--ion-color-primary);
}

.icon-sub {
  margin-right: 0.3rem;
  color: var(--ion-color-medium);
  font-size: 1.1rem;
}

.evento-fecha {
  font-size: 0.95rem;
  color: var(--ion-color-dark);
  display: flex;
  align-items: center;
  gap: 0.3rem;
}

.evento-content .descripcion {
  margin-bottom: 0.5rem;
  font-size: 1rem;
  line-height: 1.4;
}

.ubicacion {
  color: var(--ion-color-primary);
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 0.4rem;
}
</style>

