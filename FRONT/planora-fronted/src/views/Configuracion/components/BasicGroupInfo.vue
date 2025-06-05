<template>
  <ion-card>
    <ion-card-header>
      <ion-card-title>Información del Grupo</ion-card-title>
    </ion-card-header>
    
    <ion-card-content>
      <!-- Avatar del grupo -->
      <div class="group-avatar-section">
        <ion-avatar class="group-avatar">
          <img 
            :src="grupo.imagenPerfil || '/assets/default-group.png'" 
            :alt="grupo.nombre"
          />
        </ion-avatar>
        <ion-button 
          v-if="isAdmin" 
          fill="clear" 
          size="small"
          @click="changeGroupImage"
        >
          <ion-icon :icon="cameraOutline"></ion-icon>
          Cambiar
        </ion-button>
      </div>

      <!-- Nombre del grupo -->
      <ion-item>
        <ion-label position="stacked">Nombre del Grupo</ion-label>
        <ion-input
          v-model="editData.nombre"
          :readonly="!isAdmin || !isEditing"
          placeholder="Nombre del grupo"
        ></ion-input>
      </ion-item>

      <!-- Código de invitación -->
      <ion-item>
        <ion-label position="stacked">Código de Invitación</ion-label>
        <ion-input :value="grupo.codigoInvitacion" readonly></ion-input>
        <ion-button 
          v-if="isAdmin"
          fill="clear" 
          slot="end"
          @click="copyInviteCode"
        >
          <ion-icon :icon="copyOutline"></ion-icon>
        </ion-button>
      </ion-item>

      <!-- Botones de acción -->
      <div class="action-buttons" v-if="isAdmin">
        <ion-button 
          v-if="!isEditing"
          expand="block" 
          fill="outline"
          @click="startEditing"
        >
          <ion-icon :icon="createOutline" slot="start"></ion-icon>
          Editar Información
        </ion-button>

        <div v-else class="edit-buttons">
          <ion-button 
            expand="block" 
            color="primary"
            @click="saveChanges"
            :disabled="!hasChanges"
          >
            <ion-icon :icon="saveOutline" slot="start"></ion-icon>
            Guardar
          </ion-button>
          
          <ion-button 
            expand="block" 
            fill="outline"
            @click="cancelEditing"
          >
            <ion-icon :icon="closeOutline" slot="start"></ion-icon>
            Cancelar
          </ion-button>
        </div>

        <ion-button 
          expand="block" 
          color="medium"
          @click="generateNewCode"
        >
          <ion-icon :icon="refreshOutline" slot="start"></ion-icon>
          Generar Nuevo Código
        </ion-button>
      </div>
    </ion-card-content>
  </ion-card>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { 
  IonCard, IonCardHeader, IonCardTitle, IonCardContent,
  IonItem, IonLabel, IonInput, IonButton, IonIcon, IonAvatar,
  actionSheetController, toastController
} from '@ionic/vue'
import { 
  cameraOutline, copyOutline, createOutline, saveOutline, 
  closeOutline, refreshOutline 
} from 'ionicons/icons'

interface Props {
  grupo: any
  isAdmin: boolean
}

const props = defineProps<Props>()
const emit = defineEmits(['update-group', 'generate-code'])

const isEditing = ref(false)
const editData = ref({
  nombre: '',
  imagenPerfil: ''
})

// Define resetEditData function BEFORE using it in the watcher
const resetEditData = () => {
  editData.value = {
    nombre: props.grupo.nombre || '',
    imagenPerfil: props.grupo.imagenPerfil || ''
  }
}

const hasChanges = computed(() => {
  return editData.value.nombre !== props.grupo.nombre ||
         editData.value.imagenPerfil !== props.grupo.imagenPerfil
})

// Now the watcher can safely call resetEditData
watch(() => props.grupo, (newGrupo) => {
  resetEditData()
}, { immediate: true })

const startEditing = () => {
  isEditing.value = true
  resetEditData()
}

const cancelEditing = () => {
  isEditing.value = false
  resetEditData()
}

const saveChanges = () => {
  if (!hasChanges.value) return
  
  emit('update-group', {
    nombre: editData.value.nombre,
    imagenPerfil: editData.value.imagenPerfil
  })
  
  isEditing.value = false
}

const changeGroupImage = async () => {
  const actionSheet = await actionSheetController.create({
    header: 'Cambiar imagen del grupo',
    buttons: [
      {
        text: 'Cámara',
        icon: cameraOutline,
        handler: () => {
          // TODO: Implementar captura desde cámara
          console.log('Camera selected')
        }
      },
      {
        text: 'Galería',
        icon: 'image-outline',
        handler: () => {
          // TODO: Implementar selección desde galería
          console.log('Gallery selected')
        }
      },
      {
        text: 'Cancelar',
        role: 'cancel'
      }
    ]
  })
  
  await actionSheet.present()
}

const copyInviteCode = async () => {
  try {
    await navigator.clipboard.writeText(props.grupo.codigoInvitacion)
    
    const toast = await toastController.create({
      message: 'Código copiado al portapapeles',
      duration: 2000,
      color: 'success',
      position: 'top'
    })
    await toast.present()
  } catch (error) {
    const toast = await toastController.create({
      message: 'Error al copiar código',
      duration: 2000,
      color: 'danger',
      position: 'top'
    })
    await toast.present()
  }
}

const generateNewCode = () => {
  emit('generate-code')
}
</script>

<style scoped>
.group-avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 20px;
}

.group-avatar {
  width: 80px;
  height: 80px;
  margin-bottom: 8px;
}

.action-buttons {
  margin-top: 16px;
}

.edit-buttons {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.edit-buttons ion-button {
  margin: 0;
}
</style>  