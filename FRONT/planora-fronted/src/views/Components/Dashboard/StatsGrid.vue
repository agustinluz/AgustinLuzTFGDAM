<template>
  <ion-grid class="stats-grid">
    <ion-row>
      <ion-col size="6" size-md="4" v-for="stat in stats" :key="stat.label">
        <ion-card class="stat-card">
          <ion-card-content class="stat-content">
            <ion-icon :icon="stat.icon" class="stat-icon" />
            <div>
              <h2 class="stat-number">{{ stat.value }}</h2>
              <p class="stat-label">{{ stat.label }}</p>
            </div>
          </ion-card-content>
        </ion-card>
      </ion-col>
    </ion-row>
  </ion-grid>
</template>

<script setup lang="ts">
import { defineProps, computed } from 'vue'
import { people, calendar } from 'ionicons/icons'
import { IonGrid, IonRow, IonCol, IonCard, IonCardContent, IonIcon } from '@ionic/vue'
import type { StatsData } from '@/types'

// Props
const props = defineProps<{ membersCount: number; upcomingCount: number }>()

// Reactive stats array recomputed when props change
const stats = computed<StatsData[]>(() => [
  { icon: people, value: props.membersCount, label: 'Miembros' },
  { icon: calendar, value: props.upcomingCount, label: 'Próximos' }
])
</script>

<style scoped lang="scss">
.stats-grid {
  margin: 1rem 0;
  ion-card.stat-card {
    @include card-base;
    .stat-content {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      .stat-icon {
        font-size: 2.25rem;
        color: var(--ion-color-primary);
      }
      .stat-number {
        font-size: 1.75rem;
        margin: 0;
      }
      .stat-label {
        font-size: 1rem;
        color: var(--ion-color-medium);
      }
    }
  }
}
</style>
