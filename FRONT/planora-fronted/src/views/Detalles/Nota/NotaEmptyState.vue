<template>
  <div class="empty-state">
    <ion-icon :icon="documentTextOutline" class="empty-icon"></ion-icon>
    <ion-text>
      <h3>No hay notas disponibles</h3>
      <p>¡Sé el primero en crear una nota en este grupo!</p>
    </ion-text>
    <ion-button @click="$emit('crear')" expand="block" class="ion-margin-top">
      Crear primera nota
    </ion-button>
  </div>
</template>

<script setup>
import { IonIcon, IonText, IonButton } from '@ionic/vue'
import { documentTextOutline } from 'ionicons/icons'

defineEmits(['crear'])
</script>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 24px;
  text-align: center;
}
.empty-icon {
  font-size: 64px;
  color: var(--ion-color-medium);
  margin-bottom: 16px;
}
</style>