<template>
  <div class="grupos-section">
    <div class="section-header">
      <h2>Todos mis grupos</h2>
      <p>{{ grupos.length }} grupo{{ grupos.length !== 1 ? 's' : '' }}</p>
    </div>
    <ion-grid>
      <ion-row>
        <ion-col v-for="g in grupos" :key="g.id" size="12" size-md="6" size-lg="4">
          <GroupCard
            :grupo="g"
            :is-active="g.id === activoId"
            :is-admin="isAdmin(g)"
            @click="$emit('select', g.id)"
          />
        </ion-col>
      </ion-row>
    </ion-grid>
  </div>
</template>

<script setup lang="ts">
import { IonGrid, IonRow, IonCol } from '@ionic/vue'
import GroupCard from './GroupCard.vue'

defineProps<{ grupos: any[], activoId: number | null, isAdmin: (g:any)=>boolean }>()
</script>

<style scoped>
.grupos-section {
  margin-top: 8px;
}
.section-header {
  margin-bottom: 20px;
}
  .section-header h2 {
    font-size: var(--font-size-lg);
    font-weight: 600;
    margin: 0 0 4px 0;
    color: var(--ion-color-dark);
  }
  .section-header p {
    margin: 0;
    color: var(--ion-color-medium);
    font-size: var(--font-size-sm);
  }
</style>