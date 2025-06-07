<template>
  <form @submit.prevent="emitCambioYGuardar">

    <ion-item>
      <ion-input
        v-model="formData.titulo"
        label="Título de la nota"
        label-placement="stacked"
        placeholder="Ej. Reunión semanal"
        fill="outline"
        :maxlength="100"
        counter
        required
      />
    </ion-item>

    <ion-item class="textarea-item">
      <ion-textarea
        v-model="formData.contenido"
        label="Contenido"
        label-placement="stacked"
        placeholder="Escribe el contenido..."
        fill="outline"
        rows="8"
        auto-grow
        :maxlength="2000"
        counter
        required
      />
    </ion-item>

    <ion-button
      expand="block"
      class="ion-margin-top"
      type="submit"
      :disabled="!puedeGuardar || cargando"
      
    >
      <ion-spinner v-if="cargando" slot="start"></ion-spinner>
      {{ modo === 'editar' ? 'Actualizar nota' : 'Crear nota' }}
      
    </ion-button>
  </form>
</template>

<script setup>
import { IonItem, IonInput, IonTextarea, IonButton, IonSpinner } from '@ionic/vue'
import { computed, watch, reactive } from 'vue'

const props = defineProps({
  titulo: String,
  contenido: String,
  cargando: Boolean,
  modo: String // 'crear' | 'editar'
})

const emit = defineEmits(['guardar', 'update:titulo', 'update:contenido'])

const formData = reactive({
  titulo: props.titulo || '',
  contenido: props.contenido || ''
})

// Sincroniza con el padre
watch(() => props.titulo, (val) => {
  formData.titulo = val
})
watch(() => props.contenido, (val) => {
  formData.contenido = val
})
const puedeGuardar = computed(() => {
  return formData.titulo.trim().length > 0 && formData.contenido.trim().length > 0
})


const emitCambioYGuardar = () => {
  emit('update:titulo', formData.titulo)
  emit('update:contenido', formData.contenido)
  emit('guardar')
}

</script>
