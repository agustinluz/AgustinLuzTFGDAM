<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-title>Notas del grupo</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding">
      <ion-list v-if="notas.length">
        <ion-item v-for="nota in notas" :key="nota.id">
          <ion-label>
            <h2>{{ nota.titulo }}</h2>
            <p>{{ nota.contenido }}</p>
          </ion-label>
        </ion-item>
      </ion-list>
      <ion-text v-else>No hay notas disponibles.</ion-text>
    </ion-content>
  </ion-page>
</template>

<script setup>
import { IonPage, IonHeader, IonToolbar, IonTitle, IonContent, IonList, IonItem, IonLabel, IonText } from '@ionic/vue'
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'

const notas = ref([])
const grupoId = useRoute().params.id

onMounted(async () => {
  const res = await fetch(`${import.meta.env.VITE_API_URL}/grupos/${grupoId}/notas`)
  if (res.ok) notas.value = await res.json()
})
</script>
