<template>
  <ion-header>
    <ion-toolbar class="toolbar" color="primary">
      <ion-buttons slot="start">
        <ion-button fill="clear" @click="$emit('back')" aria-label="Volver">
          <ion-icon :icon="homeOutline" />
        </ion-button>
      </ion-buttons>
      <ion-title class="title">{{ title }}</ion-title>
      <ion-buttons slot="end">
        <ion-button fill="clear" @click="$emit('config')" aria-label="Configuración">
          <ion-icon :icon="settingsOutline" />
        </ion-button>
        <ion-button fill="clear" @click="$emit('logout')" aria-label="Cerrar sesión">
          <ion-icon :icon="logOutOutline" />
        </ion-button>
      </ion-buttons>
    </ion-toolbar>
  </ion-header>
</template>

<script setup lang="ts">
import { defineProps } from 'vue'
import {
  IonHeader, IonToolbar, IonTitle,
  IonButtons, IonButton, IonIcon
} from '@ionic/vue'
import {
  homeOutline,
  settingsOutline,
  logOutOutline
} from 'ionicons/icons'

const props = defineProps<{ title: string }>()
</script>

<style scoped lang="scss">
.toolbar {
  box-shadow: var(--box-shadow-md);
  padding: 0 var(--spacing-unit);
}
.title {
  font-size: var(--font-size-lg);
  font-weight: 600;
  text-align: center;
  flex: 1;
}
ion-buttons {
  --padding-end: var(--spacing-unit);
}
</style>
