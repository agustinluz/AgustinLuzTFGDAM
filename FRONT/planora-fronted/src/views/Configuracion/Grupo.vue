<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-buttons slot="start">
          <ion-back-button default-href="/"></ion-back-button>
        </ion-buttons>
        <ion-title>Configuración del Grupo</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content>
      <div class="settings-container">
        <!-- Información básica del grupo -->
        <BasicGroupInfo 
          :grupo="grupo"
          :is-admin="isAdmin"
          @update-group="handleUpdateGroup"
          @generate-code="handleGenerateCode"
        />

        <!-- Estadísticas del grupo -->
        <GroupStats 
          :stats="groupStats"
          @refresh="loadGroupStats"
        />

        <!-- Gestión de participantes -->
        <ParticipantManagement 
          :grupo-id="grupoId"
          :is-admin="isAdmin"
          :participants="participants"
          :stats="groupStats"
          @refresh-participants="loadParticipants"
        />

        <!-- Acciones del grupo -->
        <GroupActions 
          :is-admin="isAdmin"
          :grupo-id="grupoId"
          @transfer-admin="handleTransferAdmin"
          @leave-group="handleLeaveGroup"
          @delete-group="handleDeleteGroup"
        />
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { 
  IonPage, IonHeader, IonToolbar, IonTitle, IonContent, 
  IonButtons, IonBackButton, alertController, toastController 
} from '@ionic/vue'

import { useAuthStore } from '@/service/auth'
import { groupService } from '@/service/GrupoService'
import BasicGroupInfo from './components/BasicGroupInfo.vue'
import GroupStats from './components/GroupStats.vue'
import ParticipantManagement from './components/ParticipantManagment.vue'
import GroupActions from './components/AccionesGrupo.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const grupoId = ref<number>(Number(route.params.id))
const grupo = ref<any>({})
const groupStats = ref<any>({})
const participants = ref<any[]>([])
const loading = ref(false)

const isAdmin = computed(() => {
  return grupo.value.adminId === authStore.currentUser?.id
})

onMounted(() => {
  loadGroupData()
})

const loadGroupData = async () => {
  try {
    loading.value = true
    await Promise.all([
      loadGroupInfo(),
      loadGroupStats(),
      loadParticipants()
    ])
  } catch (error) {
    console.error('Error loading group data:', error)
    showToast('Error al cargar la información del grupo', 'danger')
  } finally {
    loading.value = false
  }
}

const loadGroupInfo = async () => {
  const response = await groupService.getGroupById(grupoId.value) as { data: any } 
  if (!response.data) {
    showToast('Grupo no encontrado', 'warning')
    router.push('/groups')
    return
  }
  grupo.value = response.data
}

const loadGroupStats = async () => {
  const response = await groupService.getGroupStats(grupoId.value) as { data: any }
  if (!response.data) {
    showToast('No se encontraron estadísticas para este grupo', 'warning')
    return
  }
  groupStats.value = response.data
}

const loadParticipants = async () => {
  const response = await groupService.getParticipantsWithRoles(grupoId.value) as { data: any[] }
  if (!response.data || response.data.length === 0) {
    showToast('No hay participantes en este grupo', 'warning')
    participants.value = []
    return
  }
  participants.value = response.data
}

const handleUpdateGroup = async (updateData: any) => {
  try {
    await groupService.updateGroup(grupoId.value, updateData)
    await loadGroupInfo()
    showToast('Grupo actualizado correctamente', 'success')
  } catch (error) {
    showToast('Error al actualizar el grupo', 'danger')
  }
}

const handleGenerateCode = async () => {
  try {
    const response = await groupService.generateInviteCode(grupoId.value) as { data: any }
    grupo.value.codigoInvitacion = response.data.codigoInvitacion
    showToast('Nuevo código generado', 'success')
  } catch (error) {
    showToast('Error al generar código', 'danger')
  }
}

const handleTransferAdmin = async (newAdminId: number) => {
  const alert = await alertController.create({
    header: 'Transferir Administración',
    message: '¿Estás seguro de transferir la administración? Esta acción no se puede deshacer.',
    buttons: [
      { text: 'Cancelar', role: 'cancel' },
      {
        text: 'Transferir',
        handler: async () => {
          try {
            await groupService.transferAdmin(grupoId.value, newAdminId)
            showToast('Administración transferida', 'success')
            router.push('/groups')
          } catch (error) {
            showToast('Error al transferir administración', 'danger')
          }
        }
      }
    ]
  })
  await alert.present()
}

const handleLeaveGroup = async () => {
  const alert = await alertController.create({
    header: 'Salir del Grupo',
    message: '¿Estás seguro de que quieres salir del grupo?',
    buttons: [
      { text: 'Cancelar', role: 'cancel' },
      {
        text: 'Salir',
        handler: async () => {
          try {
            await groupService.leaveGroup(grupoId.value)
            showToast('Has salido del grupo', 'success')
            router.push('/groups')
          } catch (error) {
            showToast('Error al salir del grupo', 'danger')
          }
        }
      }
    ]
  })
  await alert.present()
}

const handleDeleteGroup = async () => {
  const alert = await alertController.create({
    header: 'Eliminar Grupo',
    message: '¿Estás seguro de eliminar el grupo? Esta acción no se puede deshacer.',
    buttons: [
      { text: 'Cancelar', role: 'cancel' },
      {
        text: 'Eliminar',
        handler: async () => {
          try {
            await groupService.deleteGroup(grupoId.value)
            showToast('Grupo eliminado', 'success')
            router.push('/groups')
          } catch (error) {
            showToast('Error al eliminar grupo', 'danger')
          }
        }
      }
    ]
  })
  await alert.present()
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
.settings-container {
  padding: 16px;
  max-width: 600px;
  margin: 0 auto;
}
</style>