<template>
  <div class="form-section">
    <h3 class="section-title">
      <ion-icon name="calendar-outline" />
      Fecha y hora
    </h3>
    <ion-item button @click="openCalendar">
      <ion-label>
        <h3>Fecha del evento</h3>
        <p v-if="modelFecha">{{ formatFecha(modelFecha) }}</p>
        <p v-else class="placeholder-text">Selecciona fecha y hora</p>
      </ion-label>
      <ion-icon name="chevron-forward-outline" slot="end" />
    </ion-item>
    <ion-modal :is-open="showCalendar" @did-dismiss="closeCalendar">
      <ion-header>
        <ion-toolbar>
          <ion-title>Seleccionar fecha</ion-title>
          <ion-buttons slot="end">
            <ion-button @click="closeCalendar">Cerrar</ion-button>
          </ion-buttons>
        </ion-toolbar>
      </ion-header>
      <ion-content class="calendar-modal">
        <ion-datetime
          v-model="modelFecha"
          presentation="date-time"
          :min="minDate"
          locale="es-ES"
        />
        <div class="calendar-actions">
          <ion-button expand="block" @click="confirmDate" :disabled="!modelFecha">
            Confirmar fecha
          </ion-button>
        </div>
      </ion-content>
    </ion-modal>
  </div>
</template>

<script setup lang="ts">
import {
  IonItem,
  IonLabel,
  IonIcon,
  IonModal,
  IonHeader,
  IonToolbar,
  IonTitle,
  IonButtons,
  IonButton,
  IonContent,
  IonDatetime
} from '@ionic/vue'
import { ref, computed } from 'vue'

const props = defineProps<{ fecha: string }>()
const emit = defineEmits<{ (e: 'update:fecha', value: string): void }>()

const showCalendar = ref(false)
const minDate = new Date().toISOString()
const modelFecha = computed({
  get: () => props.fecha,
  set: v => emit('update:fecha', v)
})

const openCalendar = () => (showCalendar.value = true)
const closeCalendar = () => (showCalendar.value = false)
const confirmDate = () => closeCalendar()

const formatFecha = (dateStr: string) => {
  const fecha = new Date(dateStr)
  return fecha.toLocaleString('es-ES', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>

<style scoped>
.calendar-modal { padding: 1rem; }
.calendar-actions { padding: 1rem; margin-top: 1rem; }
</style>