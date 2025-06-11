<template>
  <ion-card>
    <ion-card-header>
      <ion-card-title>
        <ion-icon :icon="settingsOutline" class="title-icon"></ion-icon>
        Acciones del Grupo
      </ion-card-title>
    </ion-card-header>

    <ion-card-content>
      <!-- Acciones para administradores -->
      <div v-if="isAdmin" class="admin-actions">
        <ion-item-group>
          <ion-item-divider>
            <ion-label>Acciones de Administrador</ion-label>
          </ion-item-divider>

          <!-- Transferir administración -->
          <ion-item 
            button 
            @click="showTransferAdminModal = true"
            class="action-item"
          >
            <ion-icon :icon="personAddOutline" slot="start" color="primary"></ion-icon>
            <ion-label>
              <h3>Transferir Administración</h3>
              <p>Asignar otro usuario como administrador</p>
            </ion-label>
            <ion-icon :icon="chevronForwardOutline" slot="end" color="medium"></ion-icon>
          </ion-item>
        </ion-item-group>
      </div>

      <!-- Acciones para todos los usuarios -->
      <ion-item-group>
        <ion-item-divider v-if="isAdmin">
          <ion-label>Acciones Generales</ion-label>
        </ion-item-divider>

        <!-- Salir del grupo -->
        <ion-item 
          button 
          @click="confirmLeaveGroup"
          class="action-item warning"
        >
          <ion-icon :icon="exitOutline" slot="start" color="warning"></ion-icon>
          <ion-label>
            <h3>{{ isAdmin ? 'Salir del Grupo' : 'Abandonar Grupo' }}</h3>
            <p v-if="isAdmin">Debes transferir la administración primero</p>
            <p v-else>Salir de este grupo</p>
          </ion-label>
          <ion-icon :icon="chevronForwardOutline" slot="end" color="medium"></ion-icon>
        </ion-item>
      </ion-item-group>

      <!-- Modal para transferir administración -->
      <ion-modal :is-open="showTransferAdminModal" @did-dismiss="showTransferAdminModal = false">
        <ion-header>
          <ion-toolbar>
            <ion-title>Transferir Administración</ion-title>
            <ion-buttons slot="end">
              <ion-button @click="showTransferAdminModal = false">
                <ion-icon :icon="closeOutline"></ion-icon>
              </ion-button>
            </ion-buttons>
          </ion-toolbar>
        </ion-header>

        <ion-content>
          <div class="modal-content">
            <ion-card>
              <ion-card-header>
                <ion-card-subtitle>Seleccionar Nuevo Administrador</ion-card-subtitle>
                <ion-card-title>¿A quién quieres transferir la administración?</ion-card-title>
              </ion-card-header>

              <ion-card-content>
                <ion-list>
                  <ion-radio-group v-model="selectedNewAdmin">
                    <ion-item 
                      v-for="participant in eligibleParticipants" 
                      :key="participant.id"
                    >
                      <ion-avatar slot="start">
                        <div class="avatar-placeholder">
                          {{ participant.nombre.charAt(0).toUpperCase() }}
                        </div>
                      </ion-avatar>
                      <ion-label>
                        <h3>{{ participant.nombre }}</h3>
                        <p>{{ participant.email }}</p>
                        <p class="join-date">
                          Miembro desde: {{ formatDate(participant.fechaUnion) }}
                        </p>
                      </ion-label>
                      <ion-radio 
                        slot="end" 
                        :value="participant.id"
                      ></ion-radio>
                    </ion-item>
                  </ion-radio-group>
                </ion-list>

                <div class="warning-message">
                  <ion-icon :icon="warningOutline" color="warning"></ion-icon>
                  <p>
                    <strong>Advertencia:</strong> Esta acción no se puede deshacer. 
                    Una vez transferida la administración, no podrás recuperarla a menos 
                    que el nuevo administrador te la transfiera de vuelta.
                  </p>
                </div>

                <div class="modal-actions">
                  <ion-button 
                    fill="clear" 
                    @click="showTransferAdminModal = false"
                  >
                    Cancelar
                  </ion-button>
                  <ion-button 
                    color="primary"
                    :disabled="!selectedNewAdmin"
                    @click="executeTransferAdmin"
                  >
                    Transferir Administración
                  </ion-button>
                </div>
              </ion-card-content>
            </ion-card>
          </div>
        </ion-content>
      </ion-modal>
    </ion-card-content>
  </ion-card>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import {
  IonCard, IonCardHeader, IonCardTitle, IonCardContent,
  IonItem, IonItemGroup, IonItemDivider, IonLabel,
  IonIcon, IonButton, IonButtons, IonModal, IonHeader,
  IonToolbar, IonTitle, IonContent, IonList,
  IonRadioGroup, IonRadio, IonAvatar,
  alertController, toastController,IonCardSubtitle
} from '@ionic/vue'
import {
  settingsOutline, personAddOutline, exitOutline,
  chevronForwardOutline, closeOutline, warningOutline
} from 'ionicons/icons'

// Props
interface Props {
  isAdmin: boolean
  grupoId: number
  participants?: any[]
}

const props = withDefaults(defineProps<Props>(), {
  participants: () => []
})

// Emits
const emit = defineEmits<{
  transferAdmin: [newAdminId: number]
  leaveGroup: []
}>()

// State
const showTransferAdminModal = ref(false)
const selectedNewAdmin = ref<number | null>(null)

// Computed
const normalizedParticipants = computed(() =>
  props.participants.map(p => ({
    id: p.id ?? p.usuarioId,
    nombre: p.nombre ?? p.nombreUsuario,
    email: p.email ?? p.emailUsuario,
    esAdmin: p.esAdmin ?? (p.rol ? p.rol.toLowerCase() === 'admin' : false),
    fechaUnion: p.fechaUnion
  }))
)

const eligibleParticipants = computed(() =>
  normalizedParticipants.value.filter(p => !p.esAdmin)
)

const hasAnotherAdmin = computed(() =>
  normalizedParticipants.value.filter(p => p.esAdmin).length > 1
)
// Methods
// Methods
const confirmLeaveGroup = async () => {
  let message = '¿Estás seguro de que quieres salir del grupo?'
  let buttons

  if (props.isAdmin && !hasAnotherAdmin.value) {
    message = 'Como único administrador, debes transferir la administración antes de poder salir del grupo.'
    buttons = [{ text: 'Entendido', role: 'cancel' }]
  } else {
    buttons = [
      { text: 'Cancelar', role: 'cancel' },
      {
        text: 'Salir',
        role: 'destructive',
        handler: () => {
          emit('leaveGroup')
        }
      }
    ]
  }

  const alert = await alertController.create({
    header: 'Salir del Grupo',
    message,
    buttons
  })

  await alert.present()
}


const executeTransferAdmin = async () => {
  if (!selectedNewAdmin.value) {
    showToast('Por favor selecciona un usuario', 'warning')
    return
  }

  const selectedParticipant = normalizedParticipants.value.find(
    p => p.id === selectedNewAdmin.value
  )

  const alert = await alertController.create({
    header: 'Confirmar Transferencia',
    message: `¿Estás seguro de transferir la administración a ${selectedParticipant?.nombre}? Esta acción no se puede deshacer.`,
    buttons: [
      { text: 'Cancelar', role: 'cancel' },
      {
        text: 'Transferir',
        role: 'destructive',
        handler: () => {
          emit('transferAdmin', selectedNewAdmin.value!)
          showTransferAdminModal.value = false
          selectedNewAdmin.value = null
        }
      }
    ]
  })

  await alert.present()
}

const formatDate = (dateString: string) => {
  if (!dateString) return 'Fecha no disponible'
  
  try {
    const date = new Date(dateString)
    return date.toLocaleDateString('es-ES', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    })
  } catch {
    return 'Fecha no válida'
  }
}

const showToast = async (message: string, color: string) => {
  const toast = await toastController.create({
    message,
    duration: 2000,
    color,
    position: 'top'
  })
  await toast.present()
}
</script>

<style scoped>
.title-icon {
  margin-right: 8px;
  vertical-align: middle;
}

.admin-actions {
  margin-bottom: 16px;
}

.action-item {
  --padding-start: 16px;
  --padding-end: 16px;
  margin-bottom: 8px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.action-item:hover {
  --background: var(--ion-color-light);
}

.action-item.danger:hover {
  --background: rgba(var(--ion-color-danger-rgb), 0.1);
}

.action-item.warning:hover {
  --background: rgba(var(--ion-color-warning-rgb), 0.1);
}

.modal-content {
  padding: 16px;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--ion-color-primary);
  color: white;
  font-weight: bold;
  font-size: 18px;
}

.join-date {
  font-size: 12px;
  color: var(--ion-color-medium);
  margin-top: 4px;
}

.warning-message {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  background: rgba(var(--ion-color-warning-rgb), 0.1);
  padding: 16px;
  border-radius: 8px;
  margin: 16px 0;
  border-left: 4px solid var(--ion-color-warning);
}

.warning-message ion-icon {
  margin-top: 2px;
  flex-shrink: 0;
}

.warning-message p {
  margin: 0;
  font-size: 14px;
  line-height: 1.4;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid var(--ion-color-light);
}

ion-item-divider {
  --background: var(--ion-color-light);
  --color: var(--ion-color-dark);
  font-weight: 600;
}

ion-radio-group ion-item {
  --padding-start: 0;
  --padding-end: 0;
  margin-bottom: 8px;
}

@media (max-width: 768px) {
  .modal-actions {
    flex-direction: column;
  }
  
  .modal-actions ion-button {
    width: 100%;
  }
}
</style>