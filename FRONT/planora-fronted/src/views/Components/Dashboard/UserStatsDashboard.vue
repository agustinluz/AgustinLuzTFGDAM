<template>
  <ion-modal v-model:visible="visible">
    <ion-header>
      <ion-toolbar color="primary">
        <ion-title>Estadísticas de usuarios</ion-title>
        <ion-buttons slot="end">
          <ion-button @click="close">
            <ion-icon :icon="closeOutline" />
          </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>
    <ion-content class="ion-padding">
      <ion-list>
        <ion-item v-for="u in stats" :key="u.usuarioId">
          <ion-label>
            <h2>{{ u.nombreUsuario }}</h2>
            <p>Eventos creados: {{ u.eventosCreados }} | Asistencias: {{ u.eventosAsistidos }}</p>
          </ion-label>
        </ion-item>
      </ion-list>
    </ion-content>
  </ion-modal>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IonModal, IonHeader, IonToolbar, IonTitle, IonButtons, IonButton, IonIcon, IonContent, IonList, IonItem, IonLabel } from '@ionic/vue'
import { closeOutline } from 'ionicons/icons'
import type { UsuarioStatsDTO } from '@/service/DashboardService'

export default defineComponent({
  name: 'UserStatsModal',
  props: {
    visible: { type: Boolean, required: true },
    stats: { type: Array as PropType<UsuarioStatsDTO[]>, default: () => [] }
  },
  emits: ['close'],
  setup(_, { emit }) {
    const close = () => emit('close')
    return { close, closeOutline }
  }
})
</script>