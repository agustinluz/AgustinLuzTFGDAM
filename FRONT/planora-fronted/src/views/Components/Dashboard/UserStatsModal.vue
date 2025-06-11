<template>
  <ion-modal :is-open="abierto" backdrop-dismiss="close">
    <ion-header>
      <ion-toolbar color="primary" class="toolbar">
        <ion-title class="title">Estadísticas de usuarios</ion-title>
        <ion-buttons slot="end">
          <ion-button fill="clear" @click="close" aria-label="Cerrar">
            <ion-icon :icon="closeOutline" />
          </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>
    <ion-content class="content ion-padding">
      <ion-list>
        <ion-item
          v-for="u in stats"
          :key="u.usuarioId"
          lines="none"
          class="item"
          @click="toggle(u.usuarioId)"
        >
          <ion-label class="label">
            <h2 class="name">{{ u.nombreUsuario }}</h2>
            <p class="metrics">
              📝 Creados: <strong>{{ u.eventosCreados }}</strong> | 
              🎟️ Asistidos: <strong>{{ u.eventosAsistidos }}</strong>
            </p>
            <div v-if="expandedId === u.usuarioId" class="extra">
              <p>📋 Notas: <strong>{{ u.notasCreadas }}</strong></p>
              <p>💰 Gastos pagados: <strong>{{ u.gastosPagados }}</strong></p>
              <p>🗳️ Votaciones creadas: <strong>{{ u.votacionesCreadas }}</strong></p>
              <p>✅ Votos emitidos: <strong>{{ u.votosEmitidos }}</strong></p>
              <p>📷 Imágenes subidas: <strong>{{ u.imagenesSubidas }}</strong></p>
            </div>
          </ion-label>
        </ion-item>
      </ion-list>
    </ion-content>
  </ion-modal>
</template>

<script setup lang="ts">
import { defineProps, defineEmits, ref } from 'vue'
import {
  IonModal, IonHeader, IonToolbar, IonTitle,
  IonButtons, IonButton, IonIcon, IonContent,
  IonList, IonItem, IonLabel
} from '@ionic/vue'
import { closeOutline } from 'ionicons/icons'
import type { UsuarioStatsDTO } from '@/service/DashboardService'

// Props
const props = defineProps<{
  abierto: boolean
  stats: UsuarioStatsDTO[]
}>()

// Emits
const emit = defineEmits(['close'] as const)
function close() {
  emit('close')
}
const expandedId = ref<number | null>(null)
function toggle(id: number) {
  expandedId.value = expandedId.value === id ? null : id
}
</script>

<style scoped lang="scss">
.toolbar {
  box-shadow: var(--box-shadow-md);
}
.title {
  font-size: var(--font-size-md);
  font-weight: 600;
}
.content {
  --background: var(--ion-color-background);
}
.item {
  padding: var(--spacing-unit) calc(var(--spacing-unit) * 2);
  transition: background 0.2s;
}
.item:hover {
  background: var(--ion-color-primary-tint);
}
.name {
  margin: 0;
  font-size: var(--font-size-md);
}
.metrics {
  margin: 4px 0 0;
  font-size: var(--font-size-sm);
  color: var(--ion-color-medium);
}
.extra {
  margin-top: 8px;
  font-size: var(--font-size-sm);
  line-height: 1.3;
}
</style>
