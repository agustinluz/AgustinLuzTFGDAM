<template>
  <ion-header :translucent="true" class="ion-no-border">
    <ion-toolbar color="primary">
      <ion-buttons slot="start">
        <ion-back-button :default-href="`/dashboard/${grupoId}`" color="light" />
      </ion-buttons>
      <ion-title>
        <div class="title-container">
          <h1>{{ grupo?.nombre || 'Gastos' }}</h1>
          <p v-if="cantidad > 0">{{ cantidad }} gastos • {{ total }}</p>
        </div>
      </ion-title>
      <ion-buttons slot="end">
        <ion-button fill="clear" @click="$emit('abrirOpciones')">
          <ion-icon :icon="ellipsisVertical" color="light" />
        </ion-button>
      </ion-buttons>
    </ion-toolbar>
  </ion-header>
</template>

<script setup>
import { IonHeader, IonToolbar, IonButtons, IonBackButton, IonTitle, IonButton, IonIcon } from '@ionic/vue';
import { defineProps } from 'vue';
import { ellipsisVertical } from 'ionicons/icons';
defineProps(['grupo', 'cantidad', 'total','grupoId']);
defineEmits([ 'abrirOpciones']);
</script>

<style scoped>
.title-container h1 {
  margin: 0;
  font-size: 1.2rem;
  font-weight: 600;
}

.title-container p {
  margin: 0;
  font-size: 0.9rem;
  opacity: 0.8;
}
</style>
