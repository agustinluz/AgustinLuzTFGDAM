<template>
  <ion-modal v-model:visible="visible" v-if="event">
    <ion-header>
      <ion-toolbar color="primary">
        <ion-title>{{ event.titulo }}</ion-title>
        <ion-buttons slot="end">
          <ion-button @click="close()">
            <ion-icon :icon="closeOutline" />
          </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding">
      <h2>{{ event.titulo }}</h2>
      <div class="detail-row">
        <ion-icon :icon="calendar" />
        <span>{{ formatFecha(event.fecha) }}</span>
      </div>
      <div v-if="event.descripcion" class="detail-row">
        <ion-icon :icon="documentText" />
        <span>{{ event.descripcion }}</span>
      </div>

      <div v-if="canEdit" class="ion-padding">
        <ion-button expand="block" @click="handleEdit()">Editar</ion-button>
        <ion-button expand="block" color="danger" @click="handleDelete()">Eliminar</ion-button>
      </div>
    </ion-content>
  </ion-modal>
</template>

<script lang="ts">
import { defineComponent, computed, PropType } from 'vue'
import type { EventoDTO } from '@/service/DashboardService'
import { calendar, documentText, closeOutline } from 'ionicons/icons'

export default defineComponent({
  name: 'EventModal',
  props: {
    event: { type: Object as PropType<EventoDTO | null>, default: null },
    visible: { type: Boolean, required: true },
    userRole: { type: String as PropType<'admin' | 'member'>, required: true },
    userId: { type: [String, Number] as PropType<string | number>, required: true }
  },
  emits: ['close', 'delete-event', 'edit-event'],
  setup(props, { emit }) {
    const canEdit = computed(() => {
      if (!props.event) return false
      return props.userRole === 'admin' ||
             props.event.creadorId === Number(props.userId)
    })

    const close = () => emit('close')
    const handleDelete = () => {
      if (!props.event) return
      emit('delete-event', props.event.id)
      close()
    }
    const handleEdit = () => {
      if (!props.event) return
      emit('edit-event', props.event)
    }
    const formatFecha = (iso: string) =>
      new Date(iso).toLocaleDateString('es-ES', {
        day: '2-digit', month: 'long', year: 'numeric'
      })

    return { calendar, documentText, closeOutline,
             canEdit, close, handleDelete, handleEdit, formatFecha }
  }
})
</script>
