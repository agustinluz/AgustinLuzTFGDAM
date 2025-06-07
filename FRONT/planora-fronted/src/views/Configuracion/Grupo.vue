  <template>
    <ion-page>
      <ion-header>
        <ion-toolbar>
          <ion-buttons slot="start">
            <ion-back-button default-href="/grupos"></ion-back-button>
          </ion-buttons>
          <ion-title>Configuración del Grupo</ion-title>
        </ion-toolbar>
      </ion-header>

      <ion-content>
        <div v-if="loading" class="loading-container">
          <ion-spinner></ion-spinner>
          <p>Cargando información del grupo...</p>
        </div>

        <div v-else class="settings-container">
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
    IonButtons, IonBackButton, IonSpinner, alertController, toastController 
  } from '@ionic/vue'

  import { useAuthStore } from '@/service/auth'
  import { groupService } from '@/service/GrupoService'
  import BasicGroupInfo from '@/views/Components/ConfiguracionGrupo/BasicGroupInfo.vue'
  import GroupStats from '@/views/Components/ConfiguracionGrupo/GroupStats.vue'
  import ParticipantManagement from '@/views/Components/ConfiguracionGrupo/ParticipantManagment.vue'
  import GroupActions from '@/views/Components/ConfiguracionGrupo/AccionesGrupo.vue'
  const route = useRoute()
  const router = useRouter()
  const authStore = useAuthStore()

  // Mejorar la obtención del grupoId con validación
  const grupoId = ref<number>(0)
  const grupo = ref<{ adminId?: number; codigoInvitacion?: string; [key: string]: any }>({ codigoInvitacion: '' })
  const groupStats = ref<any>({})
  const participants = ref<any[]>([])
  const loading = ref(true)

  const isAdmin = computed(() => {
    return grupo.value.adminId === authStore.currentUser?.id
  })

  onMounted(() => {
    initializeComponent()
  })

  const initializeComponent = async () => {
    // Obtener grupoId de múltiples fuentes
    let id = 0
    
    // 1. Primero intentar desde route params
    if (route.params.id) {
      id = Number(route.params.id)
    }
    
    // 2. Si no hay params o es NaN, intentar desde localStorage
    if (isNaN(id) || id === 0) {
      const grupoActivoId = localStorage.getItem('grupoActivoId')
      if (grupoActivoId) {
        id = Number(grupoActivoId)
      }
    }
    
    // 3. Si aún no tenemos un ID válido, intentar desde el usuario
    if (isNaN(id) || id === 0) {
      const usuarioData = localStorage.getItem('usuario')
      if (usuarioData) {
        const usuario = JSON.parse(usuarioData)
        if (usuario.grupoIds && usuario.grupoIds.length > 0) {
          id = usuario.grupoIds[0] // Tomar el primer grupo disponible
        }
      }
    }
    
    // Validar que tenemos un ID válido
    if (isNaN(id) || id === 0) {
      console.error('No se pudo obtener un ID de grupo válido')
      showToast('Error: No se pudo identificar el grupo', 'danger')
      router.push('/grupos')
      return
    }
    
    grupoId.value = id
    console.log('Grupo ID obtenido:', grupoId.value)
    
    // Cargar los datos del grupo
    await loadGroupData()
  }

  const loadGroupData = async () => {
    try {
      loading.value = true
      console.log('Cargando datos para grupo ID:', grupoId.value)
      
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
    try {
      console.log('Cargando info del grupo:', grupoId.value)
      const response = await groupService.getGroupById(grupoId.value)
      
      if (!response || !response.data) {
        throw new Error('Grupo no encontrado')
      }
      
      grupo.value = response.data
      console.log('Grupo cargado:', grupo.value)
    } catch (error) {
      console.error('Error al cargar grupo:', error)
      showToast('Grupo no encontrado', 'warning')
      router.push('/grupos')
    }
  }

  const loadGroupStats = async () => {
    try {
      console.log('Cargando estadísticas del grupo:', grupoId.value)
      const response = await groupService.getGroupStats(grupoId.value)
      
      if (response && response.data) {
        groupStats.value = response.data
        console.log('Estadísticas cargadas:', groupStats.value)
      } else {
        console.warn('No se encontraron estadísticas')
        groupStats.value = {}
      }
    } catch (error) {
      console.error('Error al cargar estadísticas:', error)
      groupStats.value = {}
    }
  }

  const loadParticipants = async () => {
    try {
      console.log('Cargando participantes del grupo:', grupoId.value)
      const response = await groupService.getParticipantsWithRoles(grupoId.value)
      
      if (response && response.data && Array.isArray(response.data)) {
        participants.value = response.data
        console.log('Participantes cargados:', participants.value)
      } else {
        console.warn('No hay participantes en este grupo')
        participants.value = []
      }
    } catch (error) {
      console.error('Error al cargar participantes:', error)
      participants.value = []
    }
  }

  const handleUpdateGroup = async (updateData: any) => {
    try {
      await groupService.updateGroup(grupoId.value, updateData)
      await loadGroupInfo()
      showToast('Grupo actualizado correctamente', 'success')
    } catch (error) {
      console.error('Error al actualizar grupo:', error)
      showToast('Error al actualizar el grupo', 'danger')
    }
  }

  const handleGenerateCode = async () => {
    try {
      const response = await groupService.generateInviteCode(grupoId.value)
      if (response && response.data) {
        grupo.value.codigoInvitacion = response.data.codigoInvitacion
        showToast('Nuevo código generado', 'success')
      }
    } catch (error) {
      console.error('Error al generar código:', error)
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
              router.push('/grupo')
            } catch (error) {
              console.error('Error al transferir administración:', error)
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
              router.push('/grupos')
            } catch (error) {
              console.error('Error al salir del grupo:', error)
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
              router.push('/grupos')
            } catch (error) {
              console.error('Error al eliminar grupo:', error)
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

  .loading-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 200px;
    gap: 16px;
  }

  .loading-container p {
    color: var(--ion-color-medium);
    margin: 0;
  }
  </style>