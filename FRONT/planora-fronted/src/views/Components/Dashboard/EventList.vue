<template>
  <div v-if="events.length" class="events-list">
    <div
      v-for="e in events.slice(0, limit)"
      :key="e.id"
      class="event-item"
      @click="$emit('view-event', e)"
    >
      <div class="event-indicator"></div>
      <div class="event-content">
        <h3>{{ e.titulo }}</h3>
        <p>{{ formatDateES(e.fecha) }}</p>
        <p v-if="e.descripcion">{{ truncate(e.descripcion) }}</p>
      </div>
      <ion-icon :icon="chevronForward" />
    </div>
  </div>
  <div v-else class="empty-state">
    <ion-icon :icon="calendarOutline" />
    <ion-text>No hay eventos</ion-text>
  </div>
</template>

<script setup lang="ts">
import { defineProps, defineEmits } from 'vue'
import { chevronForward, calendarOutline } from 'ionicons/icons'
import { IonIcon, IonText } from '@ionic/vue'
import type { EventoDTO } from '@/service/DashboardService'
// Helpers
import { formatDateES } from '@/utils/date'
import { truncate } from '@/utils/string'

const props = defineProps<{ events: EventoDTO[]; limit: number }>()
const emit = defineEmits<{ (e: 'view-event', payload: EventoDTO): void }>()
</script>