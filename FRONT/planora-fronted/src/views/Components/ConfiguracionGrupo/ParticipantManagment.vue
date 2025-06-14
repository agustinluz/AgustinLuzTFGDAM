<template>
  <ion-card>
    <ion-card-header>
      <ion-card-title>
        <ion-icon :icon="peopleOutline" class="title-icon"></ion-icon>
        Gestión de Participantes
      </ion-card-title>
    </ion-card-header>

    <ion-card-content>
      <!-- Acciones de administrador -->
      <div v-if="isAdmin" class="admin-section">
        <ion-button 
          expand="block" 
          fill="outline" 
          @click="showInviteModal = true"
        >
          <ion-icon :icon="personAddOutline" slot="start"></ion-icon>
          Invitar Participante
        </ion-button>
      </div>

      <!-- Lista de participantes -->
      <div class="participants-list">
        <ion-item-group>
          <ion-item-divider>
            <ion-label>Participantes ({{ displayParticipants.length }})</ion-label>
          </ion-item-divider>

          <ion-item 
            v-for="participant in displayParticipants" 
            :key="participant.id"
            class="participant-item"
          >
            <ion-avatar slot="start">
              <div class="avatar-placeholder">
                {{ getInitial(participant.nombre) }}
              </div>
            </ion-avatar>

            <ion-label>
              <h3>{{ participant.nombre || 'Nombre no disponible' }}</h3>
              <p>{{ participant.email || 'Email no disponible' }}</p>
              <p class="participant-info">
                <ion-chip 
                  :color="participant.esAdmin ? 'primary' : 'medium'"
                  size="small"
                >
                  {{ participant.esAdmin ? 'Administrador' : 'Miembro' }}
                </ion-chip>
                </p>  
            </ion-label>

            <!-- Acciones del participante (solo para admins) -->
            <ion-button 
              v-if="isAdmin && !participant.esAdmin && participant.id !== idUsuarioActual"
              fill="clear" 
              slot="end"
              @click="showParticipantActions(participant)"
            >
              <ion-icon :icon="ellipsisVerticalOutline"></ion-icon>
            </ion-button>
          </ion-item>
        </ion-item-group>
      </div>

      <!-- Modal para invitar participante -->
      <ion-modal :is-open="showInviteModal" @did-dismiss="showInviteModal = false">
        <ion-header>
          <ion-toolbar>
            <ion-title>Invitar Participante</ion-title>
            <ion-buttons slot="end">
              <ion-button @click="showInviteModal = false">
                <ion-icon :icon="closeOutline"></ion-icon>
              </ion-button>
            </ion-buttons>
          </ion-toolbar>
        </ion-header>

        <ion-content>
          <div class="modal-content">
            <ion-card>
              <ion-card-header>
                <ion-card-title>Invitar por Email</ion-card-title>
                <ion-card-subtitle>Busca e invita a un usuario existente</ion-card-subtitle>
              </ion-card-header>

              <ion-card-content>
                <ion-item>
                  <ion-label position="stacked">Email del usuario</ion-label>
                  <ion-input
                    v-model="inviteEmail"
                    type="email"
                    placeholder="usuario@ejemplo.com"
                    @keyup.enter="searchUser"
                  ></ion-input>
                </ion-item>

                <ion-button 
                  expand="block" 
                  @click="searchUser"
                  :disabled="!inviteEmail || searching"
                >
                  <ion-spinner v-if="searching" size="small"></ion-spinner>
                  <ion-icon v-else :icon="searchOutline" slot="start"></ion-icon>
                  {{ searching ? 'Buscando...' : 'Buscar Usuario' }}
                </ion-button>

                <!-- Resultado de búsqueda -->
                <div v-if="searchResult" class="search-result">
                  <ion-item>
                    <ion-avatar slot="start">
                      <div class="avatar-placeholder">
                        {{ getInitial(searchResult.nombre) }}
                      </div>
                    </ion-avatar>
                    <ion-label>
                      <h3>{{ searchResult.nombre || 'Nombre no disponible' }}</h3>
                      <p>{{ searchResult.email || 'Email no disponible' }}</p>
                    </ion-label>
                    <ion-button 
                      fill="clear" 
                      color="primary"
                      @click="inviteUser"
                    >
                      Invitar
                    </ion-button>
                  </ion-item>
                </div>
              </ion-card-content>
            </ion-card>
          </div>
        </ion-content>
      </ion-modal>

      <!-- Action Sheet para acciones del participante -->
      <ion-action-sheet
        :is-open="showActionSheet"
        :buttons="participantActionButtons"
        @did-dismiss="showActionSheet = false"
      ></ion-action-sheet>
    </ion-card-content>
  </ion-card>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import {
  IonCard, IonCardHeader, IonCardTitle, IonCardContent,
  IonButton, IonIcon, IonItem, IonItemGroup, IonItemDivider,
  IonLabel, IonAvatar, IonChip, IonModal, IonHeader, IonToolbar,
  IonTitle, IonButtons, IonContent, IonInput, IonSpinner,
  IonActionSheet, alertController, toastController, IonCardSubtitle
} from '@ionic/vue'
import {
  peopleOutline, personAddOutline, ellipsisVerticalOutline,
  closeOutline, searchOutline, personRemoveOutline,
  swapHorizontalOutline, shieldCheckmarkOutline
} from 'ionicons/icons'
import { groupService } from '@/service/GrupoService'

// Props
interface Props {
  participants: any[]
  isAdmin: boolean
  grupoId: number
  idUsuarioActual?: number
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  participantUpdated: []
}>()

// State
const showInviteModal = ref(false)
const showActionSheet = ref(false)
const searching = ref(false)
const inviteEmail = ref('')
const searchResult = ref<any>(null)
const selectedParticipant = ref<any>(null)


// Computed
const participantActionButtons = computed(() => [
  {
    text: 'Promover a Administrador',
    icon: shieldCheckmarkOutline,
    handler: () => promoteToAdmin()
  },
  {
    text: 'Cambiar Rol',
    icon: swapHorizontalOutline,
    handler: () => changeRole()
  },
  {
    text: 'Remover del Grupo',
    role: 'destructive',
    icon: personRemoveOutline,
    handler: () => removeParticipant()
  },
  {
    text: 'Cancelar',
    role: 'cancel'
  }
])
// Normalizar participantes para usar propiedades consistentes
const displayParticipants = computed(() =>
  props.participants.map((p: any) => ({
    id: p.id ?? p.usuarioId,
    nombre: p.nombre ?? p.nombreUsuario,
    email: p.email ?? p.emailUsuario,
    esAdmin: p.esAdmin ?? (p.rol ? p.rol.toLowerCase() === 'admin' : false),
    fechaUnion: p.fechaUnion
  }))
)

// Methods
const getInitial = (nombre: string | null | undefined): string => {
  if (!nombre || typeof nombre !== 'string') {
    return '?'
  }
  return nombre.charAt(0).toUpperCase()
}

const showParticipantActions = (participant: any) => {
  selectedParticipant.value = participant
  showActionSheet.value = true
}

const searchUser = async () => {
  if (!inviteEmail.value.trim()) {
    showToast('Por favor ingresa un email', 'warning')
    return
  }

  searching.value = true
  searchResult.value = null

  try {
    const response = await groupService.searchUserByEmail(inviteEmail.value.trim())
    if (response.data) {
      searchResult.value = response.data
    } else {
      showToast('Usuario no encontrado', 'warning')
    }
  } catch (error) {
    console.error('Error buscando usuario:', error)
    showToast('Error al buscar usuario', 'danger')
  } finally {
    searching.value = false
  }
}

const inviteUser = async () => {
  if (!searchResult.value) return

  try {
    await groupService.inviteExistingUser(props.grupoId, searchResult.value.email)
    showToast(`Usuario ${searchResult.value.nombre} invitado correctamente`, 'success')
    resetInviteModal()
    emit('participantUpdated')
  } catch (error) {
    console.error('Error invitando usuario:', error)
    showToast('Error al invitar usuario', 'danger')
  }
}
const promoteToAdmin = async () => {
  if (!selectedParticipant.value) return

  const alert = await alertController.create({
    header: 'Promover a Administrador',
    message: `¿Estás seguro de promover a ${selectedParticipant.value.nombre} como administrador?`,
    buttons: [
      { text: 'Cancelar', role: 'cancel' },
      {
        text: 'Promover',
        handler: async () => {
          try {
            await groupService.changeUserRole(
              props.grupoId,
              selectedParticipant.value.id,
              'admin'
            )
            showToast(`${selectedParticipant.value.nombre} promovido a administrador`, 'success')
            emit('participantUpdated')
          } catch (error) {
            console.error('Error promoviendo usuario:', error)
            showToast('Error al promover usuario', 'danger')
          }
        }
      }
    ]
  })

  await alert.present()
}

const changeRole = async () => {
  if (!selectedParticipant.value) return

  const alert = await alertController.create({
    header: 'Cambiar Rol',
    message: `Cambiar rol de ${selectedParticipant.value.nombre}`,
    inputs: [
      {
        name: 'role',
        type: 'radio',
        label: 'Miembro',
        value: 'miembro',
        checked: !selectedParticipant.value.esAdmin
      },
      {
        name: 'role',
        type: 'radio',
        label: 'Administrador',
        value: 'admin',
        checked: selectedParticipant.value.esAdmin
      }
    ],
    buttons: [
      { text: 'Cancelar', role: 'cancel' },
      {
        text: 'Cambiar',
        handler: async (data) => {
          try {
            await groupService.changeUserRole(
              props.grupoId,
              selectedParticipant.value.id,
              data
            )
            showToast('Rol actualizado correctamente', 'success')
            emit('participantUpdated')
          } catch (error) {
            console.error('Error cambiando rol:', error)
            showToast('Error al cambiar rol', 'danger')
          }
        }
      }
    ]
  })

  await alert.present()
}

const removeParticipant = async () => {
  if (!selectedParticipant.value) return

  const alert = await alertController.create({
    header: 'Remover Participante',
    message: `¿Estás seguro de remover a ${selectedParticipant.value.nombre} del grupo?`,
    buttons: [
      { text: 'Cancelar', role: 'cancel' },
      {
        text: 'Remover',
        role: 'destructive',
        handler: async () => {
          try {
            await groupService.removeParticipant(
              props.grupoId,
              selectedParticipant.value.id
            )
            showToast(`${selectedParticipant.value.nombre} removido del grupo`, 'success')
            emit('participantUpdated')
          } catch (error) {
            console.error('Error removiendo participante:', error)
            showToast('Error al remover participante', 'danger')
          }
        }
      }
    ]
  })

  await alert.present()
}

const resetInviteModal = () => {
  showInviteModal.value = false
  inviteEmail.value = ''
  searchResult.value = null
}

const showToast = async (message: string, color: string) => {
  const toast = await toastController.create({
    message,
    duration: 2000,
    color,
    position: 'bottom'
  })
  await toast.present()
}
</script>

<style scoped>
.title-icon {
  margin-right: 8px;
  vertical-align: middle;
}

.admin-section {
  margin-bottom: 20px;
}

.participants-list {
  margin-top: 16px;
}

.participant-item {
  --padding-start: 16px;
  --padding-end: 16px;
  margin-bottom: 8px;
  border-radius: 8px;
  border: 1px solid var(--ion-color-light);
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
  font-size: 16px;
}

.participant-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 4px;
}

.modal-content {
  padding: 16px;
}

.search-result {
  margin: 16px 0;
  border: 1px solid var(--ion-color-light);
  border-radius: 8px;
}



ion-item-divider {
  --background: var(--ion-color-light);
  --color: var(--ion-color-dark);
  font-weight: 600;
  margin: 16px 0 8px 0;
}

@media (max-width: 768px) {
  .participant-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
}
</style>