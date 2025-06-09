<template>
  <div class="events-list">
    <ion-list v-if="events.length">
      <ion-item
        button
        lines="full"
        v-for="e in events.slice(0, limit)"
        :key="e.id"
        @click="$emit('view-event', e)"
      >
        <ion-badge slot="start">{{ formatDateBadge(e.fecha) }}</ion-badge>
        <ion-label>
          <h2>{{ e.titulo }}</h2>
          <p v-if="e.descripcion">{{ truncate(e.descripcion) }}</p>
        </ion-label>
        <ion-icon :icon="chevronForward" slot="end" />
      </ion-item>
    </ion-list>

    <div v-else class="empty-state">
      <ion-icon :icon="calendarOutline" />
      <ion-text>No hay eventos</ion-text>
    </div>
  </div>
</template>

<script setup lang="ts">
import { defineProps, defineEmits } from 'vue'
import { chevronForward, calendarOutline } from 'ionicons/icons'
import { formatDateES } from '@/utils/date'
import { truncate } from '@/utils/string'
import type { EventoDTO } from '@/service/DashboardService'

const props = defineProps<{ events: EventoDTO[]; limit: number }>()
const emit = defineEmits<{ (e: 'view-event', payload: EventoDTO): void }>()

// Muestra día y mes abreviado en badge
const formatDateBadge = (iso: string) => {
  const d = new Date(iso)
  return d.toLocaleDateString('es-ES', { day: '2-digit', month: 'short' })
}
</script>

<style scoped lang="scss">
.events-list {
  ion-list {
    border-radius: var(--border-radius);
    box-shadow: var(--box-shadow-sm);
    background: var(--ion-color-surface);
  }
  ion-item {
    --padding-start: var(--spacing-unit);
    --inner-padding-end: var(--spacing-unit);
    ion-badge {
      font-size: var(--font-size-sm);
      background: var(--ion-color-primary-shade);
      color: #fff;
    }
    ion-icon {
      color: var(--ion-color-medium);
    }
  }
  .empty-state {
    text-align: center;
    padding: calc(var(--spacing-unit) * 2);
    color: var(--ion-color-medium);
    ion-icon {
      font-size: var(--font-size-xl);
      margin-bottom: var(--spacing-unit);
    }
  }
}
</style>
