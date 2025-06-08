<template>
  <ion-card class="participants-card">
    <ion-card-header>
      <div class="section-header">
        <div class="section-title">
          <ion-icon :icon="people" color="tertiary" />
          <ion-card-title>Equipo</ion-card-title>
        </div>
        <span class="member-count">
          {{ participants.length }} miembros
        </span>
      </div>
    </ion-card-header>
    <ion-card-content>
      <div v-if="participants.length" class="participants-grid">
        <div
          class="participant-item"
          v-for="p in participants.slice(0, 6)"
          :key="p.usuarioId"
        >
          <div
            class="participant-avatar"
            :style="{ backgroundColor: getAvatarColor(p.nombreUsuario) }"
          >
            {{ getInitials(p.nombreUsuario) }}
          </div>
          <span class="participant-name">
            {{ p.nombreUsuario }}
          </span>
        </div>
        <div v-if="participants.length > 6" class="more-participants">
          +{{ participants.length - 6 }} más
        </div>
      </div>
      <div v-else class="empty-state">
        <p>No hay miembros</p>
      </div>
    </ion-card-content>
  </ion-card>
</template>

<script setup lang="ts">
import { defineProps } from 'vue'
import { people } from 'ionicons/icons'
import { IonCard, IonCardHeader, IonCardTitle, IonCardContent, IonIcon } from '@ionic/vue'
import type { UsuarioGrupoDTO } from '@/service/DashboardService'
// Helpers
import { getInitials } from '@/utils/string'
import { getAvatarColor } from '@/utils/color'

const props = defineProps<{ participants: UsuarioGrupoDTO[] }>()
const participants = props.participants
</script>