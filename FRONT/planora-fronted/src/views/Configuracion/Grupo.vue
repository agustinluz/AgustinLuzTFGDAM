<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-buttons slot="start">
          <ion-back-button default-href="/grupos"></ion-back-button>
        </ion-buttons>
        <ion-title>Editar Grupo</ion-title>
        <ion-buttons slot="end">
          <ion-button @click="guardarCambios" :disabled="guardando">
            <ion-icon :icon="checkmark"></ion-icon>
          </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>

    <ion-content>
      <ion-loading :is-open="cargando" message="Cargando..."></ion-loading>

      <!-- Información del Grupo -->
      <ion-card>
        <ion-card-header>
          <ion-card-title>Información del Grupo</ion-card-title>
        </ion-card-header>
        <ion-card-content>
          <!-- Avatar del Grupo -->
          <div class="grupo-avatar-container">
            <ion-avatar class="grupo-avatar" @click="seleccionarImagen">
              <img 
                :src="grupoData.imagenPerfil || '/assets/default-group.png'" 
                :alt="grupoData.nombre"
              />
              <div class="avatar-overlay">
                <ion-icon :icon="camera"></ion-icon>
              </div>
            </ion-avatar>
          </div>

          <!-- Nombre del Grupo -->
          <ion-item>
            <ion-label position="stacked">Nombre del Grupo</ion-label>
            <ion-input 
              v-model="grupoData.nombre"
              placeholder="Ingresa el nombre del grupo"
              :class="{ 'ion-invalid': errors.nombre }"
            ></ion-input>
            <ion-note v-if="errors.nombre" slot="error">{{ errors.nombre }}</ion-note>
          </ion-item>

          <!-- Código de Invitación -->
          <ion-item>
            <ion-label position="stacked">Código de Invitación</ion-label>
            <ion-input 
              :value="grupoData.codigoInvitacion" 
              readonly
            ></ion-input>
            <ion-button 
              slot="end" 
              fill="clear" 
              @click="copiarCodigo"
            >
              <ion-icon :icon="copy"></ion-icon>
            </ion-button>
          </ion-item>
        </ion-card-content>
      </ion-card>

      <!-- Gestión de Participantes -->
      <ion-card>
        <ion-card-header>
          <ion-card-title>
            Participantes ({{ participantes.length }})
          </ion-card-title>
          <ion-card-subtitle>
            Gestiona los miembros del grupo
          </ion-card-subtitle>
        </ion-card-header>
        <ion-card-content>
          <!-- Botón Agregar Participante -->
          <ion-button 
            expand="block" 
            fill="outline" 
            @click="mostrarAgregarParticipante"
          >
            <ion-icon :icon="personAdd" slot="start"></ion-icon>
            Agregar Participante
          </ion-button>

          <!-- Lista de Participantes -->
          <ion-list v-if="participantes.length > 0">
            <ion-item 
              v-for="participante in participantes" 
              :key="participante.id"
            >
              <ion-avatar slot="start">
                <img 
                  :src="participante.imagenPerfil || '/assets/default-avatar.png'" 
                  :alt="participante.nombre"
                />
              </ion-avatar>
              
              <ion-label>
                <h2>{{ participante.nombre }}</h2>
                <p>{{ participante.email }}</p>
                <ion-badge 
                  :color="participante.rol === 'admin' ? 'primary' : 'medium'"
                >
                  {{ participante.rol === 'admin' ? 'Administrador' : 'Miembro' }}
                </ion-badge>
              </ion-label>

              <!-- Acciones para cada participante -->
              <ion-button 
                v-if="participante.id !== currentUserId"
                slot="end" 
                fill="clear" 
                @click="mostrarOpcionesParticipante(participante)"
              >
                <ion-icon :icon="ellipsisVertical"></ion-icon>
              </ion-button>
            </ion-item>
          </ion-list>

          <ion-item v-else>
            <ion-label>
              <p>No hay participantes en este grupo</p>
            </ion-label>
          </ion-item>
        </ion-card-content>
      </ion-card>

      <!-- Zona de Peligro -->
      <ion-card color="danger">
        <ion-card-header>
          <ion-card-title>Zona de Peligro</ion-card-title>
        </ion-card-header>
        <ion-card-content>
          <ion-button 
            expand="block" 
            color="danger" 
            fill="outline"
            @click="confirmarEliminarGrupo"
          >
            <ion-icon :icon="trash" slot="start"></ion-icon>
            Eliminar Grupo
          </ion-button>
        </ion-card-content>
      </ion-card>
    </ion-content>

    <!-- Modal Agregar Participante -->
    <ion-modal :is-open="modalAgregarAbierto" @did-dismiss="cerrarModalAgregar">
      <ion-header>
        <ion-toolbar>
          <ion-title>Agregar Participante</ion-title>
          <ion-buttons slot="end">
            <ion-button @click="cerrarModalAgregar">
              <ion-icon :icon="close"></ion-icon>
            </ion-button>
          </ion-buttons>
        </ion-toolbar>
      </ion-header>
      <ion-content>
        <ion-item>
          <ion-label position="stacked">Nombre</ion-label>
          <ion-input 
            v-model="nuevoParticipante.nombre"
            placeholder="Nombre del participante"
          ></ion-input>
        </ion-item>
        <ion-item>
          <ion-label position="stacked">Email</ion-label>
          <ion-input 
            v-model="nuevoParticipante.email"
            type="email"
            placeholder="email@ejemplo.com"
          ></ion-input>
        </ion-item>
        <ion-item>
          <ion-label position="stacked">Contraseña</ion-label>
          <ion-input 
            v-model="nuevoParticipante.password"
            type="password"
            placeholder="Contraseña"
          ></ion-input>
        </ion-item>
        <div class="ion-padding">
          <ion-button 
            expand="block" 
            @click="agregarParticipante"
            :disabled="!nuevoParticipante.nombre || !nuevoParticipante.email"
          >
            Agregar
          </ion-button>
        </div>
      </ion-content>
    </ion-modal>

    <!-- Action Sheet Opciones Participante -->
    <ion-action-sheet
      :is-open="actionSheetAbierto"
      :buttons="opcionesParticipante"
      @did-dismiss="cerrarActionSheet"
    ></ion-action-sheet>

    <!-- Input de archivo oculto para seleccionar imagen -->
    <input
      ref="fileInput"
      type="file"
      accept="image/*"
      style="display: none"
      @change="manejarSeleccionImagen"
    />
  </ion-page>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  IonPage, IonHeader, IonToolbar, IonTitle, IonContent, IonButtons, IonButton,
  IonBackButton, IonCard, IonCardHeader, IonCardTitle, IonCardSubtitle, IonCardContent,
  IonItem, IonLabel, IonInput, IonNote, IonAvatar, IonList, IonBadge, IonIcon,
  IonModal, IonActionSheet, IonLoading, alertController, toastController
} from '@ionic/vue'
import {
  checkmark, camera, copy, personAdd, ellipsisVertical, close, trash,
  personRemove, shield, person
} from 'ionicons/icons'

// Interfaces
interface Grupo {
  id: number
  nombre: string
  codigoInvitacion: string
  imagenPerfil: string
  adminId: number
}

interface Participante {
  id: number
  nombre: string
  email: string
  imagenPerfil?: string
  rol: 'admin' | 'miembro'
}

interface NuevoParticipante {
  nombre: string
  email: string
  password: string
}

// Composables
const route = useRoute()
const router = useRouter()

// Estado reactivo
const cargando = ref(false)
const guardando = ref(false)
const grupoId = ref<number>(Number(route.params.id))
const currentUserId = ref<number>(1) // Este valor vendría del contexto de autenticación

const grupoData = ref<Grupo>({
  id: 0,
  nombre: '',
  codigoInvitacion: '',
  imagenPerfil: '',
  adminId: 0
})

const participantes = ref<Participante[]>([])
const participanteSeleccionado = ref<Participante | null>(null)

// Modales y Action Sheets
const modalAgregarAbierto = ref(false)
const actionSheetAbierto = ref(false)

const nuevoParticipante = ref<NuevoParticipante>({
  nombre: '',
  email: '',
  password: ''
})

// Validaciones
const errors = ref<{[key: string]: string}>({})

// Referencias
const fileInput = ref<HTMLInputElement>()

// Computed
const opcionesParticipante = computed(() => {
  if (!participanteSeleccionado.value) return []
  
  const opciones = []
  
  if (participanteSeleccionado.value.rol === 'miembro') {
    opciones.push({
      text: 'Hacer Administrador',
      icon: shield,
      handler: () => cambiarRolParticipante('admin')
    })
  } else if (participanteSeleccionado.value.rol === 'admin') {
    opciones.push({
      text: 'Quitar Administrador',
      icon: person,
      handler: () => cambiarRolParticipante('miembro')
    })
  }
  
  opciones.push({
    text: 'Eliminar del Grupo',
    icon: personRemove,
    role: 'destructive',
    handler: () => eliminarParticipante()
  })
  
  opciones.push({
    text: 'Cancelar',
    role: 'cancel'
  })
  
  return opciones
})

// Métodos
const cargarDatosGrupo = async () => {
  cargando.value = true
  try {
    // Simular llamada a la API
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // Datos simulados - aquí harías la llamada real a tu API
    grupoData.value = {
      id: grupoId.value,
      nombre: 'Grupo de Ejemplo',
      codigoInvitacion: 'ABC12345',
      imagenPerfil: '',
      adminId: currentUserId.value
    }
    
    participantes.value = [
      {
        id: 1,
        nombre: 'Admin Usuario',
        email: 'admin@ejemplo.com',
        rol: 'admin'
      },
      {
        id: 2,
        nombre: 'Juan Pérez',
        email: 'juan@ejemplo.com',
        rol: 'miembro'
      },
      {
        id: 3,
        nombre: 'María García',
        email: 'maria@ejemplo.com',
        rol: 'miembro'
      }
    ]
  } catch (error) {
    console.error('Error cargando datos del grupo:', error)
    mostrarToast('Error al cargar los datos del grupo', 'danger')
  } finally {
    cargando.value = false
  }
}

const validarFormulario = (): boolean => {
  errors.value = {}
  
  if (!grupoData.value.nombre.trim()) {
    errors.value.nombre = 'El nombre es requerido'
  }
  
  return Object.keys(errors.value).length === 0
}

const guardarCambios = async () => {
  if (!validarFormulario()) return
  
  guardando.value = true
  try {
    // Aquí harías la llamada a tu API PUT /api/grupos/{id}
    const response = await fetch(`/api/grupos/${grupoId.value}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'usuarioId': currentUserId.value.toString()
      },
      body: JSON.stringify({
        nombre: grupoData.value.nombre,
        imagenPerfil: grupoData.value.imagenPerfil
      })
    })
    
    if (response.ok) {
      mostrarToast('Grupo actualizado correctamente', 'success')
      router.back()
    } else {
      throw new Error('Error al actualizar el grupo')
    }
  } catch (error) {
    console.error('Error guardando cambios:', error)
    mostrarToast('Error al guardar los cambios', 'danger')
  } finally {
    guardando.value = false
  }
}

const seleccionarImagen = () => {
  fileInput.value?.click()
}

const manejarSeleccionImagen = (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  
  if (file) {
    const reader = new FileReader()
    reader.onload = (e) => {
      grupoData.value.imagenPerfil = e.target?.result as string
    }
    reader.readAsDataURL(file)
  }
}

const copiarCodigo = async () => {
  try {
    await navigator.clipboard.writeText(grupoData.value.codigoInvitacion)
    mostrarToast('Código copiado al portapapeles', 'success')
  } catch (error) {
    mostrarToast('Error al copiar el código', 'danger')
  }
}

const mostrarAgregarParticipante = () => {
  modalAgregarAbierto.value = true
}

const cerrarModalAgregar = () => {
  modalAgregarAbierto.value = false
  nuevoParticipante.value = { nombre: '', email: '', password: '' }
}

const agregarParticipante = async () => {
  try {
    // Llamada a tu API POST /api/grupos/{id}/usuarios
    const response = await fetch(`/api/grupos/${grupoId.value}/usuarios`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(nuevoParticipante.value)
    })
    
    if (response.ok) {
      const nuevoUsuario = await response.json()
      participantes.value.push({
        ...nuevoUsuario,
        rol: 'miembro'
      })
      mostrarToast('Participante agregado correctamente', 'success')
      cerrarModalAgregar()
    } else {
      throw new Error('Error al agregar participante')
    }
  } catch (error) {
    console.error('Error agregando participante:', error)
    mostrarToast('Error al agregar el participante', 'danger')
  }
}

const mostrarOpcionesParticipante = (participante: Participante) => {
  participanteSeleccionado.value = participante
  actionSheetAbierto.value = true
}

const cerrarActionSheet = () => {
  actionSheetAbierto.value = false
  participanteSeleccionado.value = null
}

const cambiarRolParticipante = async (nuevoRol: 'admin' | 'miembro') => {
  if (!participanteSeleccionado.value) return
  
  try {
    // Aquí implementarías la lógica para cambiar el rol
    // Esto requeriría un nuevo endpoint en tu backend
    participanteSeleccionado.value.rol = nuevoRol
    mostrarToast(`Rol actualizado a ${nuevoRol}`, 'success')
  } catch (error) {
    console.error('Error cambiando rol:', error)
    mostrarToast('Error al cambiar el rol', 'danger')
  }
  
  cerrarActionSheet()
}

const eliminarParticipante = async () => {
  if (!participanteSeleccionado.value) return
  
  const alert = await alertController.create({
    header: 'Confirmar eliminación',
    message: `¿Estás seguro de eliminar a ${participanteSeleccionado.value.nombre} del grupo?`,
    buttons: [
      'Cancelar',
      {
        text: 'Eliminar',
        role: 'destructive',
        handler: async () => {
          try {
            // Llamada a tu API DELETE /api/grupos/{grupoId}/usuarios/{usuarioId}
            const response = await fetch(
              `/api/grupos/${grupoId.value}/usuarios/${participanteSeleccionado.value!.id}`,
              {
                method: 'DELETE',
                headers: {
                  'adminId': currentUserId.value.toString()
                }
              }
            )
            
            if (response.ok) {
              participantes.value = participantes.value.filter(
                p => p.id !== participanteSeleccionado.value!.id
              )
              mostrarToast('Participante eliminado del grupo', 'success')
            } else {
              throw new Error('Error al eliminar participante')
            }
          } catch (error) {
            console.error('Error eliminando participante:', error)
            mostrarToast('Error al eliminar el participante', 'danger')
          }
        }
      }
    ]
  })
  
  await alert.present()
  cerrarActionSheet()
}

const confirmarEliminarGrupo = async () => {
  const alert = await alertController.create({
    header: 'Eliminar Grupo',
    message: 'Esta acción no se puede deshacer. ¿Estás seguro de eliminar este grupo?',
    buttons: [
      'Cancelar',
      {
        text: 'Eliminar',
        role: 'destructive',
        handler: () => {
          // Implementar eliminación del grupo
          mostrarToast('Funcionalidad no implementada', 'warning')
        }
      }
    ]
  })
  
  await alert.present()
}

const mostrarToast = async (mensaje: string, color: string = 'primary') => {
  const toast = await toastController.create({
    message: mensaje,
    duration: 3000,
    color: color,
    position: 'bottom'
  })
  await toast.present()
}

// Lifecycle
onMounted(() => {
  cargarDatosGrupo()
})
</script>

<style scoped>
.grupo-avatar-container {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

.grupo-avatar {
  width: 100px;
  height: 100px;
  position: relative;
  cursor: pointer;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.grupo-avatar:hover .avatar-overlay {
  opacity: 1;
}

.avatar-overlay ion-icon {
  color: white;
  font-size: 24px;
}

ion-badge {
  font-size: 0.7em;
  margin-top: 4px;
}

ion-card[color="danger"] {
  --background: rgba(var(--ion-color-danger-rgb), 0.1);
  --border-color: var(--ion-color-danger);
  border: 1px solid var(--border-color);
}
</style>