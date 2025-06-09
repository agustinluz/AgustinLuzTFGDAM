<template>
  <ion-card class="participants-card">
    <ion-card-header class="header">
      <ion-icon :icon="people" class="header-icon" />
      <ion-card-title>Equipo ({{ participants.length }})</ion-card-title>
    </ion-card-header>
    <ion-card-content>
      <ion-grid v-if="participants.length" class="grid">
        <ion-row>
          <ion-col
            v-for="p in participants.slice(0,6)"
            :key="p.usuarioId"
            size="4" size-md="3"
            class="col"
          >
            <ion-avatar class="avatar" :title="p.nombreUsuario">
              <span class="initials">{{ getInitials(p.nombreUsuario) }}</span>
            </ion-avatar>
            <p class="name">{{ p.nombreUsuario }}</p>
          </ion-col>
          <ion-col
            v-if="participants.length > 6"
            size="4" size-md="3"
            class="more-col"
          >
            <div class="more">+{{ participants.length - 6 }}</div>
          </ion-col>
        </ion-row>
      </ion-grid>
      <div v-else class="empty">
        <p>No hay miembros aún.</p>
      </div>
    </ion-card-content>
  </ion-card>
</template>

<script setup lang="ts">
import { defineProps } from 'vue'
import { people } from 'ionicons/icons'
import {
  IonCard, IonCardHeader, IonCardTitle, IonCardContent,
  IonGrid, IonRow, IonCol, IonAvatar, IonIcon
} from '@ionic/vue'
import type { UsuarioGrupoDTO } from '@/service/DashboardService'
import { getInitials } from '@/utils/string'
import { getAvatarColor } from '@/utils/color'

const props = defineProps<{ participants: UsuarioGrupoDTO[] }>()
const participants = props.participants
</script>

<style scoped lang="scss">
.participants-card {
  background: var(--ion-color-surface);
  border-radius: var(--border-radius);
  box-shadow: var(--box-shadow-sm);
  margin-bottom: calc(var(--spacing-unit) * 2);

  .header {
    display: flex;
    align-items: center;
    gap: var(--spacing-unit);
    padding: var(--spacing-unit);
  }
  .header-icon {
    font-size: var(--font-size-lg);
    color: var(--ion-color-tertiary);
  }

  .grid {
    padding: 0 var(--spacing-unit) var(--spacing-unit);
    .col {
      text-align: center;
      margin-bottom: var(--spacing-unit);
    }
    .avatar {
      --size: 56px;
      background: var(--ion-color-primary-tint);
      box-shadow: var(--box-shadow-sm);
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 50%;
    }
    .initials {
      font-weight: 600;
      color: #fff;
    }
    .name {
      margin: 4px 0 0;
      font-size: var(--font-size-sm);
      color: #333;
    }
    .more-col {
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .more {
      width: 56px; height: 56px;
      background: var(--ion-color-medium);
      color: #fff;
      border-radius: 50%;
      display: flex; align-items: center; justify-content: center;
      font-weight: 600;
    }
  }

  .empty {
    text-align: center;
    padding: var(--spacing-unit);
    color: var(--ion-color-medium);
    font-size: var(--font-size-sm);
  }
}
</style>
