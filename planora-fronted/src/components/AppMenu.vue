<template>
  <ion-menu content-id="main-content">
    <ion-header>
      <ion-toolbar color="primary">
        <ion-title>Menú</ion-title>
      </ion-toolbar>
    </ion-header>
    <ion-content>
      <ion-list>
        <ion-item router-link="/grupo" @click="closeMenu">Grupos</ion-item>
        <ion-item v-if="grupoId" :router-link="`/dashboard/${grupoId}`" @click="closeMenu">Dashboard</ion-item>
        <ion-item v-if="grupoId" :router-link="`/dashboard/${grupoId}/eventos`" @click="closeMenu">Eventos</ion-item>
        <ion-item v-if="grupoId" :router-link="`/dashboard/${grupoId}/gastos`" @click="closeMenu">Gastos</ion-item>
        <ion-item v-if="grupoId" :router-link="`/dashboard/${grupoId}/votaciones`" @click="closeMenu">Votaciones</ion-item>
        <ion-item v-if="grupoId" :router-link="`/dashboard/${grupoId}/notas`" @click="closeMenu">Notas</ion-item>
        <ion-item v-if="grupoId" :router-link="`/dashboard/${grupoId}/galeria`" @click="closeMenu">Galería</ion-item>
        <ion-item v-if="grupoId" :router-link="`/grupo/${grupoId}/resumen`" @click="closeMenu">Resumen</ion-item>
        <ion-item v-if="grupoId" :router-link="`/grupo/${grupoId}/configuracion`" @click="closeMenu">Configuración</ion-item>
        <ion-item router-link="/perfil" @click="closeMenu">Perfil</ion-item>
        <ion-item router-link="/invitaciones" @click="closeMenu">Invitaciones</ion-item>
      </ion-list>
    </ion-content>
  </ion-menu>
</template>

<script setup lang="ts">import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { menuController, IonMenu, IonHeader, IonToolbar, IonTitle, IonContent, IonList, IonItem } from '@ionic/vue'

const route = useRoute()
const grupoId = ref(localStorage.getItem('grupoActivoId') || '')

watch(() => route.fullPath, () => {
  grupoId.value = localStorage.getItem('grupoActivoId') || ''
})

const closeMenu = () => menuController.close()
</script>