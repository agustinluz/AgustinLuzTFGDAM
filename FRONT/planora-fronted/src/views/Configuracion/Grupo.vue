<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-buttons slot="start">
          <ion-back-button default-href="/grupo"></ion-back-button>
        </ion-buttons>
        <ion-title>Editar Grupo</ion-title>
        <ion-buttons slot="end">
          <ion-button @click="guardarCambios" :disabled="guardando || !formularioValido">
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
            <ion-label position="stacked">Nombre del Grupo *</ion-label>
            <ion-input 
              v-model="grupoData.nombre"
              placeholder="Ingresa el nombre del grupo"
              :class="{ 'ion-invalid': errors.nombre }"
              @ionInput="validarNombre"
              clear-input
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
              size="small"
            >
              <ion-icon :icon="copy"></ion-icon>
            </ion-button>
            <ion-button 
              slot="end" 
              fill="clear" 
              @click="generarNuevoCodigo"
              size="small"
              :disabled="!esAdmin"
            >
              <ion-icon :icon="refresh"></ion-icon>
            </ion-button>
          </ion-item>
        </ion-card-content>
      </ion-card>

      <!-- Estadísticas del Grupo -->
      <ion-card v-if="estadisticas">
        <ion-card-header>
          <ion-card-title>Estadísticas</ion-card-title>
        </ion-card-header>
        <ion-card-content>
          <div class="estadisticas-grid">
            <div class="estadistica-item">
              <h3>{{ estadisticas.totalParticipantes }}</h3>
              <p>Participantes</p>
            </div>
            <div class="estadistica-item">
              <h3>{{ estadisticas.totalEventos }}</h3>
              <p>Eventos</p>
            </div>
            <div class="estadistica-item">
              <h3>{{ estadisticas.totalGastos }}</h3>
              <p>Gastos</p>
            </div>
            <div class="estadistica-item">
              <h3>{{ estadisticas.totalNotas }}</h3>
              <p>Notas</p>
            </div>
          </div>
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
          <!-- Botones de Acción -->
          <div class="botones-participantes">
            <ion-button 
              expand="block" 
              fill="outline" 
              @click="mostrarAgregarParticipante"
              :disabled="!esAdmin"
            >
              <ion-icon :icon="personAdd" slot="start"></ion-icon>
              Agregar Participante
            </ion-button>
            
            <ion-button 
              expand="block" 
              fill="outline" 
              @click="mostrarInvitarExistente"
              :disabled="!esAdmin"
            >
              <ion-icon :icon="mail" slot="start"></ion-icon>
              Invitar Usuario Existente
            </ion-button>
          </div>

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
                v-if="participante.id !== currentUserId && esAdmin"
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

      <!-- Transferir Administración -->
      <ion-card v-if="esAdmin && participantes.length > 1">
        <ion-card-header>
          <ion-card-title>Transferir Administración</ion-card-title>
          <ion-card-subtitle>
            Transfiere el control del grupo a otro miembro
          </ion-card-subtitle>
        </ion-card-header>
        <ion-card-content>
          <ion-button 
            expand="block" 
            fill="outline"
            color="warning"
            @click="mostrarTransferirAdmin"
          >
            <ion-icon :icon="repeat" slot="start"></ion-icon>
            Transferir Administración
          </ion-button>
        </ion-card-content>
      </ion-card>

      <!-- Zona de Peligro -->
      <ion-card color="danger" v-if="esAdmin">
        <ion-card-header>
          <ion-card-title>Zona de Peligro</ion-card-title>
          <ion-card-subtitle>
            Acciones irreversibles
          </ion-card-subtitle>
        </ion-card-header>
        <ion-card-content>
          <ion-button 
            expand="block" 
            color="danger" 
            fill="outline"
            @click="confirmarEliminarGrupo"
          >
            <ion-icon :icon="trash" slot="start"></ion-icon>
            Eliminar Grupo Permanentemente
          </ion-button>
        </ion-card-content>
      </ion-card>

      <!-- Salir del Grupo -->
      <ion-card v-if="!esAdmin">
        <ion-card-content>
          <ion-button 
            expand="block" 
            color="medium" 
            fill="outline"
            @click="confirmarSalirGrupo"
          >
            <ion-icon :icon="exit" slot="start"></ion-icon>
            Salir del Grupo
          </ion-button>
        </ion-card-content>
      </ion-card>
    </ion-content>

    <!-- Modal Agregar Participante -->
    <ion-modal :is-open="modalAgregarAbierto" @did-dismiss="cerrarModalAgregar">
      <ion-header>
        <ion-toolbar>
          <ion-title>Agregar Nuevo Participante</ion-title>
          <ion-buttons slot="end">
            <ion-button @click="cerrarModalAgregar">
              <ion-icon :icon="close"></ion-icon>
            </ion-button>
          </ion-buttons>
        </ion-toolbar>
      </ion-header>
      <ion-content>
        <div class="ion-padding">
          <ion-item>
            <ion-label position="stacked">Nombre *</ion-label>
            <ion-input 
              v-model="nuevoParticipante.nombre"
              placeholder="Nombre completo"
              :class="{ 'ion-invalid': errorsNuevo.nombre }"
            ></ion-input>
            <ion-note v-if="errorsNuevo.nombre" slot="error">{{ errorsNuevo.nombre }}</ion-note>
          </ion-item>
          
          <ion-item>
            <ion-label position="stacked">Email *</ion-label>
            <ion-input 
              v-model="nuevoParticipante.email"
              type="email"
              placeholder="email@ejemplo.com"
              :class="{ 'ion-invalid': errorsNuevo.email }"
            ></ion-input>
            <ion-note v-if="errorsNuevo.email" slot="error">{{ errorsNuevo.email }}</ion-note>
          </ion-item>
          
          <ion-item>
            <ion-label position="stacked">Contraseña *</ion-label>
            <ion-input 
              v-model="nuevoParticipante.password"
              type="password"
              placeholder="Contraseña temporal"
              :class="{ 'ion-invalid': errorsNuevo.password }"
            ></ion-input>
            <ion-note v-if="errorsNuevo.password" slot="error">{{ errorsNuevo.password }}</ion-note>
          </ion-item>
          
          <ion-button 
            expand="block" 
            @click="agregarParticipante"
            :disabled="procesandoNuevo || !formularioNuevoValido"
            class="ion-margin-top"
          >
            <ion-spinner v-if="procesandoNuevo" name="crescent"></ion-spinner>
            <span v-else>Crear y Agregar Usuario</span>
          </ion-button>
        </div>
      </ion-content>
    </ion-modal>

    <!-- Modal Invitar Usuario Existente -->
    <ion-modal :is-open="modalInvitarAbierto" @did-dismiss="cerrarModalInvitar">
      <ion-header>
        <ion-toolbar>
          <ion-title>Invitar Usuario Existente</ion-title>
          <ion-buttons slot="end">
            <ion-button @click="cerrarModalInvitar">
              <ion-icon :icon="close"></ion-icon>
            </ion-button>
          </ion-buttons>
        </ion-toolbar>
      </ion-header>
      <ion-content>
        <div class="ion-padding">
          <ion-item>
            <ion-label position="stacked">Email del Usuario</ion-label>
            <ion-input 
              v-model="emailInvitacion"
              type="email"
              placeholder="email@ejemplo.com"
              @ionInput="buscarUsuarioPorEmail"
            ></ion-input>
          </ion-item>
          
          <div v-if="usuarioEncontrado" class="usuario-encontrado">
            <ion-item>
              <ion-avatar slot="start">
                <img :src="usuarioEncontrado.imagenPerfil || '/assets/default-avatar.png'" />
              </ion-avatar>
              <ion-label>
                <h2>{{ usuarioEncontrado.nombre }}</h2>
                <p>{{ usuarioEncontrado.email }}</p>
              </ion-label>
            </ion-item>
          </div>
          
          <ion-button 
            expand="block" 
            @click="invitarUsuarioExistente"
            :disabled="procesandoInvitar || !usuarioEncontrado"
            class="ion-margin-top"
          >
            <ion-spinner v-if="procesandoInvitar" name="crescent"></ion-spinner>
            <span v-else>Enviar Invitación</span>
          </ion-button>
        </div>
      </ion-content>
    </ion-modal>

    <!-- Modal Transferir Administración -->
    <ion-modal :is-open="modalTransferirAbierto" @did-dismiss="cerrarModalTransferir">
      <ion-header>
        <ion-toolbar>
          <ion-title>Transferir Administración</ion-title>
          <ion-buttons slot="end">
            <ion-button @click="cerrarModalTransferir">
              <ion-icon :icon="close"></ion-icon>
            </ion-button>
          </ion-buttons>
        </ion-toolbar>
      </ion-header>
      <ion-content>
        <div class="ion-padding">
          <ion-text>
            <p>Selecciona el nuevo administrador del grupo. Esta acción no se puede deshacer.</p>
          </ion-text>
          
          <ion-radio-group v-model="nuevoAdminId">
            <ion-item 
              v-for="participante in participantesSinAdmin" 
              :key="participante.id"
            >
              <ion-avatar slot="start">
                <img :src="participante.imagenPerfil || '/assets/default-avatar.png'" />
              </ion-avatar>
              <ion-label>
                <h2>{{ participante.nombre }}</h2>
                <p>{{ participante.email }}</p>
              </ion-label>
              <ion-radio slot="end" :value="participante.id"></ion-radio>
            </ion-item>
          </ion-radio-group>
          
          <ion-button 
            expand="block" 
            @click="transferirAdministracion"
            :disabled="procesandoTransferir || !nuevoAdminId"
            color="warning"
            class="ion-margin-top"
          >
            <ion-spinner v-if="procesandoTransferir" name="crescent"></ion-spinner>
            <span v-else>Transferir Administración</span>
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

    <!-- Input de archivo oculto -->
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
  IonModal, IonActionSheet, IonLoading, IonSpinner, IonText, IonRadioGroup, IonRadio,
  alertController, toastController
} from '@ionic/vue'
import {
  checkmark, camera, copy, personAdd, ellipsisVertical, close, trash,
  personRemove, shield, person, refresh, mail, repeat, exit
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

interface Estadisticas {
  totalParticipantes: number
  totalEventos: number
  totalGastos: number
  totalNotas: number
  totalAdmins: number
  totalMiembros: number
}

// Composables
const route = useRoute()
const router = useRouter()

// Estado reactivo
const cargando = ref(false)
const guardando = ref(false)
const procesandoNuevo = ref(false)
const procesandoInvitar = ref(false)
const procesandoTransferir = ref(false)

const grupoId = ref<number>(Number(route.params.id))
const currentUserId = ref<number>(1) // Obtener del contexto de autenticación

const grupoData = ref<Grupo>({
  id: 0,
  nombre: '',
  codigoInvitacion: '',
  imagenPerfil: '',
  adminId: 0
})

const participantes = ref<Participante[]>([])
const estadisticas = ref<Estadisticas | null>(null)
const participanteSeleccionado = ref<Participante | null>(null)

// Modales
const modalAgregarAbierto = ref(false)
const modalInvitarAbierto = ref(false)
const modalTransferirAbierto = ref(false)
const actionSheetAbierto = ref(false)

// Formularios
const nuevoParticipante = ref<NuevoParticipante>({
  nombre: '',
  email: '',
  password: ''
})

const emailInvitacion = ref('')
const usuarioEncontrado = ref<any>(null)
const nuevoAdminId = ref<number | null>(null)

// Validaciones
const errors = ref<{[key: string]: string}>({})
const errorsNuevo = ref<{[key: string]: string}>({})

// Referencias
const fileInput = ref<HTMLInputElement>()

// Computed Properties
const esAdmin = computed(() => {
  return grupoData.value.adminId === currentUserId.value
})

const formularioValido = computed(() => {
  return grupoData.value.nombre.trim().length > 0 && Object.keys(errors.value).length === 0
})

const formularioNuevoValido = computed(() => {
  return nuevoParticipante.value.nombre.trim().length > 0 &&
         nuevoParticipante.value.email.trim().length > 0 &&
         nuevoParticipante.value.password.trim().length > 0 &&
         Object.keys(errorsNuevo.value).length === 0
})

const participantesSinAdmin = computed(() => {
  return participantes.value.filter(p => p.id !== currentUserId.value)
})

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

// Métodos de carga de datos
const cargarDatosGrupo = async () => {
  cargando.value = true
  try {
    const response = await fetch(`/api/grupos/${grupoId.value}`, {
      headers: {
        'usuarioId': currentUserId.value.toString(),
        'Content-Type': 'application/json',
        'authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    if (response.ok) {
      const data = await response.json()
      grupoData.value = {
        id: data.id,
        nombre: data.nombre,
        codigoInvitacion: data.codigoInvitacion,
        imagenPerfil: data.imagenPerfil,
        adminId: data.admin?.id || 0
      }
    } else {
      throw new Error('Error al cargar el grupo')
    }
  } catch (error) {
    console.error('Error cargando datos del grupo:', error)
    mostrarToast('Error al cargar los datos del grupo', 'danger')
  } finally {
    cargando.value = false
  }
}

const cargarParticipantes = async () => {
  try {
    const response = await fetch(`/api/grupos/${grupoId.value}/participantes-con-roles`, {
      headers: {
        'usuarioId': currentUserId.value.toString()
      }
    })
    
    if (response.ok) {
      const data = await response.json()
      participantes.value = data.map((item: any) => ({
        id: item.usuario.id,
        nombre: item.usuario.nombre,
        email: item.usuario.email,
        imagenPerfil: item.usuario.imagenPerfil,
        rol: item.rol
      }))
    }
  } catch (error) {
    console.error('Error cargando participantes:', error)
  }
}

const cargarEstadisticas = async () => {
  try {
    const response = await fetch(`/api/grupos/${grupoId.value}/estadisticas`, {
      headers: {
        'usuarioId': currentUserId.value.toString()
      }
    })
    
    if (response.ok) {
      estadisticas.value = await response.json()
    }
  } catch (error) {
    console.error('Error cargando estadísticas:', error)
  }
}

// Validaciones
const validarNombre = () => {
  errors.value = {}
  if (!grupoData.value.nombre.trim()) {
    errors.value.nombre = 'El nombre es requerido'
  } else if (grupoData.value.nombre.trim().length < 3) {
    errors.value.nombre = 'El nombre debe tener al menos 3 caracteres'
  }
}

const validarNuevoParticipante = () => {
  errorsNuevo.value = {}
  
  if (!nuevoParticipante.value.nombre.trim()) {
    errorsNuevo.value.nombre = 'El nombre es requerido'
  }
  
  if (!nuevoParticipante.value.email.trim()) {
    errorsNuevo.value.email = 'El email es requerido'
  } else if (!/\S+@\S+\.\S+/.test(nuevoParticipante.value.email)) {
    errorsNuevo.value.email = 'El email no es válido'
  }
  
  if (!nuevoParticipante.value.password.trim()) {
    errorsNuevo.value.password = 'La contraseña es requerida'
  } else if (nuevoParticipante.value.password.length < 6) {
    errorsNuevo.value.password = 'La contraseña debe tener al menos 6 caracteres'
  }
}

// Métodos principales
const guardarCambios = async () => {
  if (!formularioValido.value) return
  
  guardando.value = true
  try {
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

const agregarParticipante = async () => {
  validarNuevoParticipante()
  if (!formularioNuevoValido.value) return
  
  procesandoNuevo.value = true
  try {
    const response = await fetch(`/api/grupos/${grupoId.value}/usuarios`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(nuevoParticipante.value)
    })
    
    if (response.ok) {
      mostrarToast('Participante agregado correctamente', 'success')
      cerrarModalAgregar()
      await cargarParticipantes()
      await cargarEstadisticas()
    } else {
      throw new Error('Error al agregar participante')
    }
  } catch (error) {
    console.error('Error agregando participante:', error)
    mostrarToast('Error al agregar el participante', 'danger')
  } finally {
    procesandoNuevo.value = false
  }
}

const buscarUsuarioPorEmail = async () => {
  if (!emailInvitacion.value || emailInvitacion.value.length < 3) {
    usuarioEncontrado.value = null
    return
  }
  
  try {
    const response = await fetch(`/api/grupos/buscar-usuario?email=${emailInvitacion.value}`, {
      headers: {
        'usuarioId': currentUserId.value.toString()
      }
    })
    
    if (response.ok) {
      usuarioEncontrado.value = await response.json()
    } else {
      usuarioEncontrado.value = null
    }
  } catch (error) {
    usuarioEncontrado.value = null
  }
}

const invitarUsuarioExistente = async () => {
  if (!usuarioEncontrado.value) return
  
  procesandoInvitar.value = true
  try {
    const response = await fetch(`/api/grupos/${grupoId.value}/invitar?emailUsuario=${usuarioEncontrado.value.email}`, {
      method: 'POST',
      headers: {
        'adminId': currentUserId.value.toString()
      }
    })
    
    if (response.ok) {
      mostrarToast('Usuario invitado correctamente', 'success')
      cerrarModalInvitar()
      await cargarParticipantes()
      await cargarEstadisticas()
    } else if (response.status === 409) {
      mostrarToast('El usuario ya es miembro del grupo', 'warning')
    } else {
      throw new Error('Error al invitar usuario')
    }
  } catch (error) {
    console.error('Error invitando usuario:', error)
    mostrarToast('Error al enviar la invitación', 'danger')
  } finally {
    procesandoInvitar.value = false
  }
}

const cambiarRolParticipante = async (nuevoRol: 'admin' | 'miembro') => {
  if (!participanteSeleccionado.value) return
  
  try {
    const response = await fetch(`/api/grupos/${grupoId.value}/usuarios/${participanteSeleccionado.value.id}/rol?nuevoRol=${nuevoRol}`, {
      method: 'PUT',
      headers: {
        'adminId': currentUserId.value.toString()
      }
    })
    
    if (response.ok) {
      mostrarToast(`Rol actualizado a ${nuevoRol}`, 'success')
      await cargarParticipantes()
    } else {
      throw new Error('Error al cambiar el rol')
    }
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
            const response = await fetch(`/api/grupos/${grupoId.value}/usuarios/${participanteSeleccionado.value!.id}`, {
              method: 'DELETE',
              headers: {
                'adminId': currentUserId.value.toString()
              }
            })
            
            if (response.ok) {
              mostrarToast('Participante eliminado del grupo', 'success')
              await cargarParticipantes()
              await cargarEstadisticas()
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

const transferirAdministracion = async () => {
  if (!nuevoAdminId.value) return
  
  const alert = await alertController.create({
    header: 'Confirmar transferencia',
    message: '¿Estás seguro de transferir la administración? Esta acción no se puede deshacer.',
    buttons: [
      'Cancelar',
      {
        text: 'Transferir',
        role: 'destructive',
        handler: async () => {
          procesandoTransferir.value = true
          try {
            const response = await fetch(`/api/grupos/${grupoId.value}/transferir-admin`, {
              method: 'PUT',
              headers: {
                'Content-Type': 'application/json',
                'adminId': currentUserId.value.toString()
              },
              body: JSON.stringify({ nuevoAdminId: nuevoAdminId.value })
            })
            
            if (response.ok) {
              mostrarToast('Administración transferida correctamente', 'success')
              cerrarModalTransferir()
              await cargarDatosGrupo()
              await cargarParticipantes()
            } else {
              throw new Error('Error al transferir administración')
            }
          } catch (error) {
            console.error('Error transfiriendo administración:', error)
            mostrarToast('Error al transferir la administración', 'danger')
          } finally {
            procesandoTransferir.value = false
          }
        }
      }
    ]
  })
  
  await alert.present()
}

const confirmarEliminarGrupo = async () => {
  const alert = await alertController.create({
    header: 'Eliminar Grupo',
    message: 'Esta acción eliminará permanentemente el grupo y todos sus datos. ¿Estás seguro?',
    inputs: [
      {
        name: 'confirmacion',
        type: 'text',
        placeholder: 'Escribe "ELIMINAR" para confirmar'
      }
    ],
    buttons: [
      'Cancelar',
      {
        text: 'Eliminar',
        role: 'destructive',
        handler: async (data) => {
          if (data.confirmacion === 'ELIMINAR') {
            await eliminarGrupo()
          } else {
            mostrarToast('Confirmación incorrecta', 'warning')
          }
        }
      }
    ]
  })
  
  await alert.present()
}

const eliminarGrupo = async () => {
  try {
    const response = await fetch(`/api/grupos/${grupoId.value}`, {
      method: 'DELETE',
      headers: {
        'adminId': currentUserId.value.toString()
      }
    })
    
    if (response.ok) {
      mostrarToast('Grupo eliminado correctamente', 'success')
      router.replace('/tabs/grupos')
    } else {
      throw new Error('Error al eliminar el grupo')
    }
  } catch (error) {
    console.error('Error eliminando grupo:', error)
    mostrarToast('Error al eliminar el grupo', 'danger')
  }
}

const confirmarSalirGrupo = async () => {
  const alert = await alertController.create({
    header: 'Salir del Grupo',
    message: '¿Estás seguro de que quieres salir de este grupo?',
    buttons: [
      'Cancelar',
      {
        text: 'Salir',
        role: 'destructive',
        handler: async () => {
          await salirDelGrupo()
        }
      }
    ]
  })
  
  await alert.present()
}

const salirDelGrupo = async () => {
  try {
    const response = await fetch(`/api/grupos/${grupoId.value}/usuarios/${currentUserId.value}`, {
      method: 'DELETE',
      headers: {
        'usuarioId': currentUserId.value.toString()
      }
    })
    
    if (response.ok) {
      mostrarToast('Has salido del grupo', 'success')
      router.replace('/tabs/grupos')
    } else {
      throw new Error('Error al salir del grupo')
    }
  } catch (error) {
    console.error('Error saliendo del grupo:', error)
    mostrarToast('Error al salir del grupo', 'danger')
  }
}

// Métodos de utilidad
const copiarCodigo = async () => {
  try {
    await navigator.clipboard.writeText(grupoData.value.codigoInvitacion)
    mostrarToast('Código copiado al portapapeles', 'success')
  } catch (error) {
    console.error('Error copiando código:', error)
    mostrarToast('Error al copiar el código', 'danger')
  }
}

const generarNuevoCodigo = async () => {
  const alert = await alertController.create({
    header: 'Generar Nuevo Código',
    message: '¿Estás seguro? El código anterior dejará de funcionar.',
    buttons: [
      'Cancelar',
      {
        text: 'Generar',
        handler: async () => {
          try {
            const response = await fetch(`/api/grupos/${grupoId.value}/nuevo-codigo`, {
              method: 'PUT',
              headers: {
                'adminId': currentUserId.value.toString()
              }
            })
            
            if (response.ok) {
              const data = await response.json()
              grupoData.value.codigoInvitacion = data.nuevoCodigo
              mostrarToast('Nuevo código generado', 'success')
            } else {
              throw new Error('Error al generar nuevo código')
            }
          } catch (error) {
            console.error('Error generando código:', error)
            mostrarToast('Error al generar el código', 'danger')
          }
        }
      }
    ]
  })
  
  await alert.present()
}

const seleccionarImagen = () => {
  if (!esAdmin.value) return
  fileInput.value?.click()
}

const manejarSeleccionImagen = async (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  
  if (!file) return
  
  // Validar tipo de archivo
  if (!file.type.startsWith('image/')) {
    mostrarToast('Por favor selecciona una imagen válida', 'warning')
    return
  }
  
  // Validar tamaño (max 5MB)
  if (file.size > 5 * 1024 * 1024) {
    mostrarToast('La imagen no debe superar los 5MB', 'warning')
    return
  }
  
  try {
    const formData = new FormData()
    formData.append('imagen', file)
    
    const response = await fetch(`/api/grupos/${grupoId.value}/imagen`, {
      method: 'PUT',
      headers: {
        'adminId': currentUserId.value.toString()
      },
      body: formData
    })
    
    if (response.ok) {
      const data = await response.json()
      grupoData.value.imagenPerfil = data.imagenPerfil
      mostrarToast('Imagen actualizada correctamente', 'success')
    } else {
      throw new Error('Error al subir la imagen')
    }
  } catch (error) {
    console.error('Error subiendo imagen:', error)
    mostrarToast('Error al actualizar la imagen', 'danger')
  }
  
  // Limpiar el input
  target.value = ''
}

// Métodos de modal
const mostrarAgregarParticipante = () => {
  limpiarFormularioNuevo()
  modalAgregarAbierto.value = true
}

const cerrarModalAgregar = () => {
  modalAgregarAbierto.value = false
  limpiarFormularioNuevo()
}

const mostrarInvitarExistente = () => {
  emailInvitacion.value = ''
  usuarioEncontrado.value = null
  modalInvitarAbierto.value = true
}

const cerrarModalInvitar = () => {
  modalInvitarAbierto.value = false
  emailInvitacion.value = ''
  usuarioEncontrado.value = null
}

const mostrarTransferirAdmin = () => {
  nuevoAdminId.value = null
  modalTransferirAbierto.value = true
}

const cerrarModalTransferir = () => {
  modalTransferirAbierto.value = false
  nuevoAdminId.value = null
}

const mostrarOpcionesParticipante = (participante: Participante) => {
  participanteSeleccionado.value = participante
  actionSheetAbierto.value = true
}

const cerrarActionSheet = () => {
  actionSheetAbierto.value = false
  participanteSeleccionado.value = null
}

const limpiarFormularioNuevo = () => {
  nuevoParticipante.value = {
    nombre: '',
    email: '',
    password: ''
  }
  errorsNuevo.value = {}
}

// Utilidades
const mostrarToast = async (message: string, color: 'success' | 'warning' | 'danger' = 'success') => {
  const toast = await toastController.create({
    message,
    duration: 3000,
    color,
    position: 'bottom'
  })
  await toast.present()
}

// Lifecycle
onMounted(async () => {
  await Promise.all([
    cargarDatosGrupo(),
    cargarParticipantes(),
    cargarEstadisticas()
  ])
})
</script>

<style scoped>
.grupo-avatar-container {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

.grupo-avatar {
  width: 120px;
  height: 120px;
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

.estadisticas-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-top: 16px;
}

.estadistica-item {
  text-align: center;
  padding: 16px;
  background: var(--ion-color-light);
  border-radius: 8px;
}

.estadistica-item h3 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: bold;
  color: var(--ion-color-primary);
}

.estadistica-item p {
  margin: 0;
  font-size: 14px;
  color: var(--ion-color-medium);
}

.botones-participantes {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 20px;
}

.usuario-encontrado {
  margin: 16px 0;
  border: 1px solid var(--ion-color-light);
  border-radius: 8px;
}

@media (min-width: 768px) {
  .estadisticas-grid {
    grid-template-columns: repeat(4, 1fr);
  }
  
  .botones-participantes {
    flex-direction: row;
  }
}
</style>