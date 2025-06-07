<template>
  <ion-modal :is-open="isOpen" @will-dismiss="$emit('cerrar')">
    <ion-header>
      <ion-toolbar>
        <ion-title>{{ eventoEditando ? 'Editar Evento' : 'Nuevo Evento' }}</ion-title>
        <ion-buttons slot="end">
          <ion-button @click="$emit('cerrar')">Cancelar</ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>
    <ion-content class="ion-padding">
      <form @submit.prevent="guardarEvento">
        <ion-item>
          <ion-label position="stacked">Título *</ion-label>
          <ion-input 
            v-model="formData.titulo" 
            placeholder="Nombre del evento"
            required>
          </ion-input>
        </ion-item>

        <ion-item>
          <ion-label position="stacked">Descripción</ion-label>
          <ion-textarea 
            v-model="formData.descripcion" 
            placeholder="Describe el evento..."
            :rows="3">
          </ion-textarea>
        </ion-item>

        <ion-item>
          <ion-label position="stacked">Ubicación</ion-label>
          <ion-input 
            v-model="formData.ubicacion" 
            placeholder="¿Dónde será el evento?">
          </ion-input>
        </ion-item>

        <ion-item>
          <ion-label position="stacked">Fecha y Hora *</ion-label>
          <ion-datetime 
            v-model="formData.fecha"
            display-format="DD/MM/YYYY HH:mm"
            picker-format="DD/MM/YYYY HH:mm"
            :min="new Date().toISOString()"
            placeholder="Selecciona fecha y hora">
          </ion-datetime>
        </ion-item>

        <div class="form-buttons">
          <ion-button 
            expand="block" 
            type="submit" 
            :disabled="!formData.titulo || !formData.fecha || guardando">
            <ion-spinner v-if="guardando" name="crescent"></ion-spinner>
            <span v-else>{{ eventoEditando ? 'Actualizar' : 'Crear' }} Evento</span>
          </ion-button>
        </div>
      </form>
    </ion-content>
  </ion-modal>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import {
  IonModal,
  IonHeader,
  IonToolbar,
  IonTitle,
  IonContent,
  IonButtons,
  IonButton,
  IonItem,
  IonLabel,
  IonInput,
  IonTextarea,
  IonDatetime,
  IonSpinner
} from '@ionic/vue'
import type { Evento } from '@/service/EventoService'

interface Props {
  isOpen: boolean
  eventoEditando?: Evento | null
  guardando: boolean
}

interface Emits {
  (e: 'cerrar'): void
  (e: 'guardar', formData: FormData): void
}

interface FormData {
  titulo: string
  descripcion: string
  ubicacion: string
  fecha: string
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const formData = ref<FormData>({
  titulo: '',
  descripcion: '',
  ubicacion: '',
  fecha: ''
})

// Observar cambios en eventoEditando para llenar el formulario
watch(() => props.eventoEditando, (evento) => {
  if (evento) {
    formData.value = {
      titulo: evento.titulo,
      descripcion: evento.descripcion,
      ubicacion: evento.ubicacion || '',
      fecha: new Date(evento.fecha).toISOString()
    }
  } else {
    // Resetear formulario para nuevo evento
    formData.value = {
      titulo: '',
      descripcion: '',
      ubicacion: '',
      fecha: ''
    }
  }
}, { immediate: true })

const guardarEvento = () => {
  emit('guardar', formData.value)
}
</script>

<style scoped>
.form-buttons {
  margin-top: 2rem;
}
</style>