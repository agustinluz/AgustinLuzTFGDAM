<!-- src/views/CrearElementoPage.vue -->
<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-title>Crear nuevo</ion-title>
      </ion-toolbar>
    </ion-header>
    <ion-content class="ion-padding">
      <ion-grid>
        <ion-row>
          <ion-col size="6">
            <ion-card button @click="goTo('evento')">
              <ion-card-content class="ion-text-center">Evento</ion-card-content>
            </ion-card>
          </ion-col>
          <ion-col size="6">
            <ion-card button @click="goTo('gasto')">
              <ion-card-content class="ion-text-center">Gasto</ion-card-content>
            </ion-card>
          </ion-col>
        </ion-row>
        <ion-row>
          <ion-col size="6">
            <ion-card button @click="goTo('votacion')">
              <ion-card-content class="ion-text-center">Votación</ion-card-content>
            </ion-card>
          </ion-col>
          <ion-col size="6">
            <ion-card button @click="goTo('nota')">
              <ion-card-content class="ion-text-center">Nota</ion-card-content>
            </ion-card>
          </ion-col>
        </ion-row>
      </ion-grid>
    </ion-content>
  </ion-page>
</template>

<script setup>
import {
  IonPage, IonHeader, IonToolbar, IonTitle, IonContent, IonGrid, IonRow, IonCol, IonCard, IonCardContent
} from '@ionic/vue'
import { useIonRouter } from '@ionic/vue'

const router = useIonRouter()

const goTo = (tipo) => {
  router.push(`/dashboard/${grupoId}/crear/${tipo}`)
}
</script>
