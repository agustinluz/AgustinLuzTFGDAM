<template>
  <ion-segment v-model="modelo" @ionChange="$emit('filtrar', $event.detail.value)">
    <ion-segment-button value="todos">
      <ion-label>Todos ({{ total }})</ion-label>
    </ion-segment-button>
    <ion-segment-button value="pendientes">
      <ion-label>Pendientes ({{ pendientes }})</ion-label>
    </ion-segment-button>
    <ion-segment-button value="saldados">
      <ion-label>Saldados ({{ saldados }})</ion-label>
    </ion-segment-button>
  </ion-segment>
</template>

<script setup>
import { IonSegment, IonSegmentButton, IonLabel } from '@ionic/vue';
import { defineProps, defineEmits } from 'vue';
import { IonIcon } from '@ionic/vue';
import { ref, watch } from 'vue';
const props = defineProps(['total', 'pendientes', 'valorInicial', 'saldados', 'valorInicial']);
const modelo = ref(props.valorInicial || 'todos');
watch(() => props.valorInicial, v => (modelo.value = v));
defineEmits(['filtrar']);
</script>

<style scoped>
ion-segment {
  margin-bottom: 1rem;
}
</style>
