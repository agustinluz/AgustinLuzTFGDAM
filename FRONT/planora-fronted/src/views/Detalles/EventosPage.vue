<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-title>Eventos del grupo</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding">
      <ion-list v-if="eventos.length">
        <ion-item v-for="evento in eventos" :key="evento.id">
          <ion-label>
            <h2>{{ evento.titulo }}</h2>
            <p>{{ evento.descripcion }}</p>
            <p>{{ formatFecha(evento.fecha) }}</p>
          </ion-label>
        </ion-item>
      </ion-list>
      <ion-text v-else>No hay eventos disponibles.</ion-text>
    </ion-content>
  </ion-page>
</template>

<script setup>
import { IonPage, IonHeader, IonToolbar, IonTitle, IonContent, IonList, IonItem, IonLabel, IonText } from '@ionic/vue'
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'

const eventos = ref([])
const route = useRoute()
const grupoId = route.params.id

const formatFecha = (fecha) => new Date(fecha).toLocaleString('es-ES')

onMounted(async () => {
  const res = await fetch(`${import.meta.env.VITE_API_URL}/grupos/${grupoId}/eventos`)
  if (res.ok) eventos.value = await res.json()
})
</script>
