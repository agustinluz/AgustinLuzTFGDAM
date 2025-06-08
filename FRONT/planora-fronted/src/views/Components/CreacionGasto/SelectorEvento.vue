// SelectorEvento.vue
<template>
  <ion-card class="modern-card">
    <ion-card-header>
      <div class="card-header">
        <ion-icon :icon="starOutline" class="header-icon" />
        <ion-card-title>Evento (Opcional)</ion-card-title>
      </div>
    </ion-card-header>
    <ion-card-content>
      <ion-item>
        <ion-select v-model="formulario.eventoId" placeholder="Sin evento" interface="popover">
          <ion-select-option :value="null">Sin evento</ion-select-option>
          <ion-select-option v-for="e in eventos" :key="e.id" :value="e.id">{{ e.titulo }}</ion-select-option>
        </ion-select>
      </ion-item>
    </ion-card-content>
  </ion-card>
</template>
<script setup>
import { IonCard, IonCardHeader, IonCardTitle, IonCardContent, IonItem, IonSelect, IonSelectOption, IonIcon } from '@ionic/vue';
import { starOutline } from 'ionicons/icons';
const props = defineProps({ formulario: Object, eventos: Array });
</script>
