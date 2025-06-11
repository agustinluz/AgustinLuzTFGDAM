<template>
  <ion-grid class="stats-grid">
    <ion-row>
      <ion-col v-for="stat in stats" :key="stat.label" size="6" size-md="4">
        <ion-card class="stat-card">
          <ion-card-content class="content">
            <ion-icon :icon="stat.icon" class="icon" />
            <div class="text">
              <h3 class="number">{{ stat.value }}</h3>
              <p class="label">{{ stat.label }}</p>
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
const props = defineProps<{ membersCount: number; upcomingCount: number }>()
const stats = computed(() => [
  { icon: people,   value: props.membersCount, label: 'Miembros' },
  { icon: calendar, value: props.upcomingCount, label: 'Próximos' }
])
</script>

<style scoped lang="scss">
.stat-card {
  background: var(--ion-color-surface);
  border-radius: var(--border-radius);
  box-shadow: var(--box-shadow-sm);
  transition: transform 0.2s ease;
  &:hover { transform: translateY(-4px); }

  .content {
    display: flex;
    align-items: center;
    gap: var(--spacing-unit);
  }
  .icon { font-size: var(--font-size-xl); color: var(--ion-color-primary); }
  .number { margin: 0; font-size: var(--font-size-lg); }
  .label  { margin: 0; font-size: var(--font-size-sm); color: #666; }
}
</style>
