<template>
  <ion-modal :is-open="visible" @did-dismiss="$emit('cerrar')">
    <ion-header>
      <ion-toolbar>
        <ion-title>{{ nota?.titulo || 'Nota' }}</ion-title>
        <ion-buttons slot="end">
          <ion-button fill="clear" @click="$emit('cerrar')">
            <ion-icon :icon="closeOutline"></ion-icon>
          </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding">
      <ion-text color="medium">
        <p><strong>Autor:</strong> {{ nota?.creadaPorNombre || 'Usuario' }}</p>
        <p><strong>Fecha:</strong> {{ formatearFecha(nota?.fechaCreacion) }}</p>
      </ion-text>
      <ion-text class="nota-contenido">
        <p>{{ nota?.contenido }}</p>
      </ion-text>
    </ion-content>
  </ion-modal>
</template>

<script setup>
import {
  IonModal, IonHeader, IonToolbar, IonTitle, IonButtons,
  IonButton, IonIcon, IonContent, IonText
} from '@ionic/vue'
import { closeOutline } from 'ionicons/icons'

const props = defineProps({
  visible: Boolean,
  nota: Object
})

defineEmits(['cerrar'])

const formatearFecha = (fecha) => {
  if (!fecha) return 'Sin fecha'
  return new Date(fecha).toLocaleString('es-ES', {
    year: 'numeric', month: 'short', day: 'numeric',
    hour: '2-digit', minute: '2-digit'
  })
}
</script>

<style scoped>
.nota-contenido {
  margin-top: 1rem;
  white-space: pre-wrap;
  font-size: 1rem;
  line-height: 1.6;
}
</style>
