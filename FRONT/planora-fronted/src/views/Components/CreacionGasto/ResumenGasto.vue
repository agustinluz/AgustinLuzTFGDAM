// ResumenGasto.vue
<template>
  <ion-card class="modern-card summary-card">
    <ion-card-header>
      <div class="card-header">
        <ion-icon :icon="eyeOutline" class="header-icon" />
        <ion-card-title>Resumen</ion-card-title>
      </div>
    </ion-card-header>
    <ion-card-content>
      <div class="summary-item"><strong>Total:</strong> {{ formatMonto(formulario.monto) }}</div>
      <div class="summary-item"><strong>Participantes:</strong> {{ participantes.length }}</div>
      <div class="summary-item"><strong>Pagado por:</strong> {{ pagador?.nombre || '-' }}</div>
    </ion-card-content>
  </ion-card>
</template>
<script setup>
import { IonCard, IonCardHeader, IonCardTitle, IonCardContent, IonIcon } from '@ionic/vue';
import { eyeOutline } from 'ionicons/icons';
const props = defineProps({ formulario: Object, participantes: Array, pagador: Object });
const formatMonto = m => new Intl.NumberFormat('es-ES',{style:'currency',currency:'EUR'}).format(parseFloat(m)||0);
</script>
