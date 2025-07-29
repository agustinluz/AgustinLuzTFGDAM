
// BotonesAccion.vue
<template>
  <div class="action-buttons">
    <ion-button :disabled="!valido || enviando" expand="block" size="large" class="primary-button" @click="crear">
      <ion-spinner v-if="enviando" slot="start" name="crescent" />
      Crear Gasto
    </ion-button>
    <ion-button expand="block" fill="clear" color="medium" @click="cancelar">
      Cancelar
    </ion-button>
  </div>
</template>
<script setup>
import { IonButton, IonSpinner } from '@ionic/vue';
const props = defineProps({ valido: Boolean, enviando: Boolean });
const emit = defineEmits(['crear','cancelar']);
const crear = () => emit('crear');
const cancelar = () => emit('cancelar');
</script>
