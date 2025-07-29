<template>
  <div class="form-section">
    <h3 class="section-title">
      <ion-icon name="information-circle-outline"></ion-icon>
      Información del evento
    </h3>
    <ion-item>
      <ion-input
        label="Título del evento"
        label-placement="stacked"
        fill="outline"
        v-model="modelTitulo"
        placeholder="Ej: Cena de cumpleaños"
        :maxlength="100"
        counter
        required
      />
    </ion-item>
    <ion-item class="textarea-item">
      <ion-textarea
        label="Descripción"
        label-placement="stacked"
        fill="outline"
        v-model="modelDescripcion"
        placeholder="Describe los detalles del evento..."
        :rows="4"
        auto-grow
        :maxlength="500"
        counter
      />
    </ion-item>
  </div>
</template>

<script setup lang="ts">
import { IonItem, IonInput, IonTextarea, IonIcon } from '@ionic/vue'
import { computed } from 'vue'

const props = defineProps<{ titulo: string; descripcion: string }>()
const emit = defineEmits<{
  (e: 'update:titulo', value: string): void
  (e: 'update:descripcion', value: string): void
}>()

const modelTitulo = computed({
  get: () => props.titulo,
  set: v => emit('update:titulo', v)
})

const modelDescripcion = computed({
  get: () => props.descripcion,
  set: v => emit('update:descripcion', v)
})
</script>

<style scoped>
</style>