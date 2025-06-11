<template>
  <ion-card button class="grupo-card" :class="{ 'active-group': isActive }" @click="$emit('click')">
    <ion-card-content>
      <div class="card-header">
        <div class="grupo-avatar">
          <img v-if="grupo.imagenPerfil" :src="grupo.imagenPerfil" :alt="grupo.nombre" />
          <ion-icon v-else :icon="people" color="primary" />
        </div>
        <div class="grupo-info">
          <ion-card-title class="grupo-nombre">{{ grupo.nombre }}</ion-card-title>
          <div class="codigo-container">
            <ion-icon :icon="key" size="small" />
            <span>{{ grupo.codigoInvitacion }}</span>
          </div>
        </div>
        <ion-chip v-if="isAdmin" color="success" class="admin-chip">
          <ion-icon :icon="shield" size="small" />
          <ion-label>Admin</ion-label>
        </ion-chip>
      </div>
      <div class="grupo-stats">
        <div class="stat-item">
          <ion-icon :icon="people" size="small" />
          <span>{{ grupo.participantesCount ?? 0 }}</span>
        </div>
        <div class="stat-item">
          <ion-icon :icon="calendar" size="small" />
          <span>{{ grupo.eventosCount ?? 0 }}</span>
        </div>
        <div class="stat-item">
          <ion-icon :icon="camera" size="small" />
          <span>{{ grupo.fotosCount ?? 0 }}</span>
        </div>
      </div>
    </ion-card-content>
  </ion-card>
</template>

<script setup lang="ts">
import { IonCard, IonCardContent, IonCardTitle, IonIcon, IonChip, IonLabel } from '@ionic/vue'
import { people, key, calendar, camera, shield } from 'ionicons/icons'

defineProps<{ grupo: any, isActive: boolean, isAdmin: boolean }>()
</script>

<style scoped>
.grupo-card {
  margin: 8px 0;
  border-radius: 20px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
  background: white;
  border: 2px solid transparent;
}
.grupo-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
}
.grupo-card.active-group {
  border-color: var(--ion-color-primary);
  background: linear-gradient(135deg, #fff 0%, var(--ion-color-primary-tint) 100%);
}
.card-header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 16px;
  position: relative;
}
.grupo-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: var(--ion-color-primary-tint);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  flex-shrink: 0;
}
.grupo-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.grupo-info {
  flex: 1;
}
.grupo-nombre {
  margin: 0 0 6px 0;
  font-size: 1.1em;
  font-weight: 600;
}
.codigo-container {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--ion-color-medium);
  font-size: 0.85em;
}
.admin-chip {
  position: absolute;
  top: 0;
  right: 0;
  height: 24px;
  font-size: 0.75em;
}
.grupo-stats {
  display: flex;
  justify-content: space-between;
  padding: 12px 16px;
  background: rgba(var(--ion-color-primary-rgb), 0.05);
  border-radius: 12px;
  font-size: 0.85em;
}
.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--ion-color-medium);
}
</style>