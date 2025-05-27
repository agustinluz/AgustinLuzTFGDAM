<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-title>Galería</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding">
      <ion-grid>
        <ion-row>
          <ion-col size="6" v-for="img in imagenes" :key="img.id">
            <ion-img :src="img.url" />
          </ion-col>
        </ion-row>
      </ion-grid>
      <ion-text v-if="imagenes.length === 0">No hay imágenes subidas.</ion-text>
    </ion-content>
  </ion-page>
</template>

<script setup>
import { IonPage, IonHeader, IonToolbar, IonTitle, IonContent, IonGrid, IonRow, IonCol, IonImg, IonText } from '@ionic/vue'
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'

const imagenes = ref([])
const grupoId = useRoute().params.id

onMounted(async () => {
  const res = await fetch(`${import.meta.env.VITE_API_URL}/imagenes/grupo/${grupoId}`)
  if (res.ok) imagenes.value = await res.json()
})
</script>
