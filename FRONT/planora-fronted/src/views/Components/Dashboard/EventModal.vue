<template>
  <ion-modal :is-open="visible">
    <ion-header>
      <ion-toolbar color="primary">
        <ion-buttons slot="start">
          <ion-button fill="clear" @click="close()">
            <ion-icon :icon="closeOutline" />
          </ion-button>
        </ion-buttons>
        <ion-title>{{ event?.titulo }}</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding modal-content">
      <h2 class="modal-title">{{ event?.titulo }}</h2>
      <div class="detail-row">
        <ion-icon :icon="calendar" />
        <span>{{ formatFecha(event?.fecha) }}</span>
      </div>
      <div v-if="event?.descripcion" class="detail-row">
        <ion-icon :icon="documentText" />
        <span>{{ event.descripcion }}</span>
      </div>
      <ion-button
        v-if="isUpcoming"
        expand="block"
        color="light"
        @click="goToAsistir"
      >
        Confirmar asistencia
      </ion-button>
      <div v-if="canEdit" class="actions">
        <ion-button expand="block" @click="handleEdit()">Editar evento</ion-button>
        <ion-button expand="block" color="danger" @click="handleDelete()">Eliminar evento</ion-button>
      </div>
    </ion-content>
  </ion-modal>
</template>

<script lang="ts">
import { defineComponent, computed, PropType } from 'vue'
import { calendar, documentText, closeOutline } from 'ionicons/icons'
import {
  IonModal, IonHeader, IonToolbar, IonButtons, IonButton,
  IonTitle, IonContent, IonIcon
} from '@ionic/vue'
import type { EventoDTO } from '@/service/DashboardService'
import { useRouter } from 'vue-router'

export default defineComponent({
  name: 'EventModal',
  props: {
    event: { type: Object as PropType<EventoDTO | null>, default: null },
    visible: { type: Boolean, required: true },
    rolUsuario: { type: String as PropType<'admin'|'member'>, required: true },
    idUsuario: { type: [String,Number] as PropType<string|number>, required: true }
  },
  emits: ['close','delete-event','edit-event'],
  setup(props, { emit }) {
    const router = useRouter()
    const canEdit = computed(() => {
      if (!props.event) return false
       return props.rolUsuario === 'admin' || props.event.creadorId === Number(props.idUsuario)
    })
     const isUpcoming = computed(() => {
      if (!props.event) return false
      return new Date(props.event.fecha).getTime() >= Date.now()
    })
    const close = () => emit('close')
    const handleDelete = () => {
      if (props.event) {
        emit('delete-event', props.event.id)
        close()
      }
    }
    const handleEdit = () => {
      if (props.event) emit('edit-event', props.event)
    }
  const goToAsistir = () => {
      if (props.event) {
        router.push({ name: 'asistir-evento', params: { eventoId: props.event.id, grupoId: props.event.grupoId } })
        close()
      }
    }
    const formatFecha = (iso?: string) =>
      iso
        ? new Date(iso).toLocaleDateString('es-ES', {
            day: '2-digit', month: 'long', year: 'numeric'
          })
        : ''

    return { calendar, documentText, closeOutline, canEdit, isUpcoming, close, handleDelete, handleEdit, goToAsistir, formatFecha }
  }
})
</script>

<style scoped lang="scss">
.modal-content {
  h2.modal-title {
    font-size: var(--font-size-lg);
    margin-bottom: var(--spacing-unit);
  }
  .detail-row {
    display: flex;
    align-items: center;
    gap: var(--spacing-unit);
    margin-bottom: var(--spacing-unit);
    font-size: var(--font-size-md);
    ion-icon { font-size: var(--font-size-md); }
  }
  .actions {
    margin-top: calc(var(--spacing-unit) * 2);
    ion-button {
      margin-bottom: var(--spacing-unit);
      box-shadow: var(--box-shadow-sm);
    }
  }
}
</style>
