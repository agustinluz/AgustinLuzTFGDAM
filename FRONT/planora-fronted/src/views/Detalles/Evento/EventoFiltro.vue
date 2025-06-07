<template>
  <div class="filtros-container">
    <ion-searchbar 
      :model-value="filtroTexto" 
      placeholder="Buscar eventos..." 
      @ion-input="actualizarFiltroTexto"
      show-clear-button="focus">
    </ion-searchbar>
    
    <ion-segment :model-value="filtroFecha" @ion-change="actualizarFiltroFecha">
      <ion-segment-button value="todos">
        <ion-label>Todos</ion-label>
      </ion-segment-button>
      <ion-segment-button value="proximos">
        <ion-label>Próximos</ion-label>
      </ion-segment-button>
      <ion-segment-button value="pasados">
        <ion-label>Pasados</ion-label>
      </ion-segment-button>
    </ion-segment>
  </div>
</template>

<script setup lang="ts">
import {
  IonSearchbar,
  IonSegment,
  IonSegmentButton,
  IonLabel
} from '@ionic/vue'

interface Props {
  filtroTexto: string
  filtroFecha: string
}

interface Emits {
  (e: 'update:filtroTexto', value: string): void
  (e: 'update:filtroFecha', value: string): void
  (e: 'filtrar'): void
}

defineProps<Props>()
const emit = defineEmits<Emits>()

const actualizarFiltroTexto = (event: CustomEvent) => {
  emit('update:filtroTexto', event.detail.value)
  emit('filtrar')
}

const actualizarFiltroFecha = (event: CustomEvent) => {
  emit('update:filtroFecha', event.detail.value)
  emit('filtrar')
}
</script>

<style scoped>
.filtros-container {
  margin-bottom: 1rem;
}

.filtros-container ion-segment {
  margin-top: 0.5rem;
}
</style>