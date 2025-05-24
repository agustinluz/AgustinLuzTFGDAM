<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-title>Votaciones del grupo</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding">
      <ion-list v-if="votaciones.length">
        <ion-item v-for="v in votaciones" :key="v.id">
          <ion-label>
            <h2>{{ v.titulo }}</h2>
            <p>{{ v.descripcion }}</p>
          </ion-label>
        </ion-item>
      </ion-list>
      <ion-text v-else>No hay votaciones activas.</ion-text>
    </ion-content>
  </ion-page>
</template>

<script setup>
import { IonPage, IonHeader, IonToolbar, IonTitle, IonContent, IonList, IonItem, IonLabel, IonText } from '@ionic/vue'
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'

const votaciones = ref([])
const grupoId = useRoute().params.id

onMounted(async () => {
  const res = await fetch(`${import.meta.env.VITE_API_URL}/grupos/${grupoId}/votaciones`)
  if (res.ok) votaciones.value = await res.json()
})
</script>
