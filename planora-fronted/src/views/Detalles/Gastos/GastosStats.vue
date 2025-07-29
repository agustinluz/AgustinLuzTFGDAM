<template>
  <ion-card class="stats-card">
    <ion-card-content>
      <div class="stats-grid">
        <div class="stat-item">
          <ion-icon :icon="walletOutline" color="primary" />
          <div>
            <h3>{{ total }}</h3>
            <p>Total gastado</p>
          </div>
        </div>
        <div class="stat-item">
          <ion-icon :icon="receiptOutline" color="success" />
          <div>
            <h3>{{ cantidad }}</h3>
            <p>Gastos</p>
          </div>
        </div>
        <div class="stat-item">
          <ion-icon :icon="timeOutline" color="warning" />
          <div>
            <h3>{{ pendientes }}</h3>
            <p>Pendientes</p>
          </div>
        </div>
      </div>
    </ion-card-content>
  </ion-card>
</template>

<script setup>
import { walletOutline, receiptOutline, timeOutline } from 'ionicons/icons';
import { IonCard, IonCardContent, IonIcon } from '@ionic/vue';
defineProps(['total', 'cantidad', 'pendientes']);
</script>

<style scoped>
.stats-card {
  margin-bottom: 1rem;
}
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 1rem;
}
.stat-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}
.stat-item ion-icon {
  font-size: 1.5rem;
}
.stat-item h3 {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 600;
}
.stat-item p {
  margin: 0;
  font-size: 0.85rem;
  color: var(--ion-color-medium);
}
</style>
