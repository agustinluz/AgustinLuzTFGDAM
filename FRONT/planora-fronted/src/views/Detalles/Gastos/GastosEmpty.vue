<template>
  <div class="empty-state">
    <ion-icon :icon="icono" color="medium" />
    <h2>{{ mensaje }}</h2>
    <ion-button @click="$emit('crear')" expand="block">
      <ion-icon :icon="add" slot="start" />
      Añadir Gasto
    </ion-button>
  </div>
</template>

<script setup>
import { IonIcon, IonButton } from '@ionic/vue'
import { defineProps, defineEmits } from 'vue'
import { add, receiptOutline } from 'ionicons/icons'

defineProps({
  mensaje: String,
  icono: { type: String, default: receiptOutline }
})
defineEmits(['crear'])
</script>

<style scoped>
.empty-state {
  text-align: center;
  padding: 3rem 1rem;
}
.empty-state ion-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}
.empty-state h2 {
  color: var(--ion-color-medium);
  margin-bottom: 2rem;
}
</style>
