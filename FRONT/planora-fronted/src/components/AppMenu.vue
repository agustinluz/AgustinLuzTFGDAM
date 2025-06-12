<template>
  <ion-menu content-id="main-content">
    <ion-header>
      <ion-toolbar color="primary">
        <ion-title>Menú</ion-title>
      </ion-toolbar>
    </ion-header>
    <ion-content>
      <ion-list>
        <ion-item router-link="/grupo">Grupos</ion-item>
        <ion-item v-if="grupoId" :router-link="`/dashboard/${grupoId}`">Dashboard</ion-item>
        <ion-item v-if="grupoId" :router-link="`/dashboard/${grupoId}/eventos`">Eventos</ion-item>
        <ion-item v-if="grupoId" :router-link="`/dashboard/${grupoId}/gastos`">Gastos</ion-item>
        <ion-item v-if="grupoId" :router-link="`/dashboard/${grupoId}/votaciones`">Votaciones</ion-item>
        <ion-item v-if="grupoId" :router-link="`/dashboard/${grupoId}/notas`">Notas</ion-item>
        <ion-item v-if="grupoId" :router-link="`/dashboard/${grupoId}/galeria`">Galería</ion-item>
        <ion-item v-if="grupoId" :router-link="`/grupo/${grupoId}/resumen`">Resumen</ion-item>
        <ion-item v-if="grupoId" :router-link="`/grupo/${grupoId}/configuracion`">Configuración</ion-item>
        <ion-item router-link="/perfil">Perfil</ion-item>
        <ion-item router-link="/invitaciones">Invitaciones</ion-item>
      </ion-list>
    </ion-content>
  </ion-menu>
</template>

<script setup lang="ts">
import { IonMenu, IonHeader, IonToolbar, IonTitle, IonContent, IonList, IonItem } from '@ionic/vue'
const grupoId = localStorage.getItem('grupoActivoId') || ''
</script>