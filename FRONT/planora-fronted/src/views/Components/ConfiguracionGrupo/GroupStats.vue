<template>
  <ion-card>
    <ion-card-header>
      <ion-card-title>
        Estadísticas
        <ion-button 
          fill="clear" 
          size="small" 
          @click="$emit('refresh')"
        >
          <ion-icon :icon="refreshOutline"></ion-icon>
        </ion-button>
      </ion-card-title>
    </ion-card-header>
    
    <ion-card-content>
      <div class="stats-grid">
        <!-- Participantes -->
        <div class="stat-item">
          <ion-icon :icon="peopleOutline" class="stat-icon"></ion-icon>
          <div class="stat-content">
            <div class="stat-number">{{ stats.totalParticipantes || 0 }}</div>
            <div class="stat-label">Participantes</div>
          </div>
        </div>

        <!-- Administradores -->
        <div class="stat-item">
          <ion-icon :icon="shieldCheckmarkOutline" class="stat-icon"></ion-icon>
          <div class="stat-content">
            <div class="stat-number">{{ stats.totalAdmins || 0 }}</div>
            <div class="stat-label">Administradores</div>
          </div>
        </div>

        <!-- Eventos -->
        <div class="stat-item">
          <ion-icon :icon="calendarOutline" class="stat-icon"></ion-icon>
          <div class="stat-content">
            <div class="stat-number">{{ stats.totalEventos || 0 }}</div>
            <div class="stat-label">Eventos</div>
          </div>
        </div>

        <!-- Gastos -->
        <div class="stat-item">
          <ion-icon :icon="cardOutline" class="stat-icon"></ion-icon>
          <div class="stat-content">
            <div class="stat-number">{{ stats.totalGastos || 0 }}</div>
            <div class="stat-label">Gastos</div>
          </div>
        </div>

        <!-- Notas -->
        <div class="stat-item">
          <ion-icon :icon="documentTextOutline" class="stat-icon"></ion-icon>
          <div class="stat-content">
            <div class="stat-number">{{ stats.totalNotas || 0 }}</div>
            <div class="stat-label">Notas</div>
          </div>
        </div>

        <div class="stat-item">
          <ion-icon :icon="checkboxOutline" class="stat-icon"></ion-icon>
          <div class="stat-content">
            <div class="stat-number">{{ stats.totalVotaciones || 0 }}</div>
            <div class="stat-label">Votaciones</div>
          </div>
        </div>

        <!-- Miembros regulares -->
        <div class="stat-item">
          <ion-icon :icon="personOutline" class="stat-icon"></ion-icon>
          <div class="stat-content">
            <div class="stat-number">{{ stats.totalMiembros || 0 }}</div>
            <div class="stat-label">Miembros</div>
          </div>
        </div>
      </div>


      <!-- Información adicional -->
      <div class="additional-info" v-if="stats.grupoId">
        <ion-item lines="none">
          <ion-label>
            <h3>{{ stats.nombreGrupo }}</h3>
            <p>ID del grupo: {{ stats.grupoId }}</p>
          </ion-label>
        </ion-item>
      </div>
    </ion-card-content>
  </ion-card>
</template>

<script setup lang="ts">
import { 
  IonCard, IonCardHeader, IonCardTitle, IonCardContent,
  IonButton, IonIcon, IonItem, IonLabel
} from '@ionic/vue'

interface Props {
  stats: any
}

defineProps<Props>()
defineEmits(['refresh'])
</script>

<style scoped>
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  padding: 12px;
  background: var(--ion-color-light);
  border-radius: 8px;
  gap: 12px;
}

.stat-icon {
  font-size: 24px;
  color: var(--ion-color-primary);
  min-width: 24px;
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 20px;
  font-weight: bold;
  color: var(--ion-color-dark);
  line-height: 1;
}

.stat-label {
  font-size: 12px;
  color: var(--ion-color-medium);
  margin-top: 2px;
}

.additional-info {
  border-top: 1px solid var(--ion-color-light);
  padding-top: 16px;
  margin-top: 16px;
}
</style>