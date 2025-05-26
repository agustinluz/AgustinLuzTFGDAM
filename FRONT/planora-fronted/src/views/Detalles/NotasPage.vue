<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-buttons slot="start">
          <ion-back-button default-href="/grupo"></ion-back-button>
        </ion-buttons>
        <ion-title>Notas del Grupo</ion-title>
        <ion-buttons slot="end">
          <ion-button @click="mostrarModalCrear = true" fill="clear">
            <ion-icon :icon="addOutline"></ion-icon>
          </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>

    <ion-content>
      <!-- Loading State -->
      <div v-if="cargando" class="loading-container">
        <ion-spinner></ion-spinner>
        <ion-text>Cargando notas...</ion-text>
      </div>

      <!-- Content -->
      <div v-else>
        <!-- Search Bar -->
        <ion-searchbar 
          v-model="busqueda"
          placeholder="Buscar notas..."
          @ionInput="filtrarNotas"
          class="ion-margin-bottom">
        </ion-searchbar>

        <!-- Notas List -->
        <ion-list v-if="notasFiltradas.length" class="ion-margin">
          <ion-card v-for="nota in notasFiltradas" :key="nota.id" class="nota-card">
            <ion-card-header>
              <div class="nota-header">
                <ion-card-title>{{ nota.titulo }}</ion-card-title>
                <div class="nota-actions">
                  <ion-button 
                    @click="editarNota(nota)" 
                    fill="clear" 
                    size="small"
                    v-if="esProietario(nota)">
                    <ion-icon :icon="createOutline"></ion-icon>
                  </ion-button>
                  <ion-button 
                    @click="confirmarEliminar(nota)" 
                    fill="clear" 
                    size="small" 
                    color="danger"
                    v-if="esProietario(nota)">
                    <ion-icon :icon="trashOutline"></ion-icon>
                  </ion-button>
                </div>
              </div>
              <ion-card-subtitle>
                Por: {{ nota.nombreUsuario || 'Usuario' }} • 
                {{ formatearFecha(nota.fechaCreacion) }}
              </ion-card-subtitle>
            </ion-card-header>
            
            <ion-card-content>
              <p class="nota-contenido">{{ nota.contenido }}</p>
              <ion-button 
                @click="verNotaCompleta(nota)" 
                fill="clear" 
                size="small"
                v-if="nota.contenido && nota.contenido.length > 150">
                Ver más
              </ion-button>
            </ion-card-content>
          </ion-card>
        </ion-list>

        <!-- Empty State -->
        <div v-else-if="!cargando" class="empty-state">
          <ion-icon :icon="documentTextOutline" class="empty-icon"></ion-icon>
          <ion-text>
            <h3>No hay notas disponibles</h3>
            <p>¡Sé el primero en crear una nota en este grupo!</p>
          </ion-text>
          <ion-button @click="mostrarModalCrear = true" expand="block" class="ion-margin-top">
            Crear primera nota
          </ion-button>
        </div>
      </div>

      <!-- Modal Crear/Editar Nota -->
      <ion-modal :is-open="mostrarModalCrear || mostrarModalEditar" @did-dismiss="cerrarModales">
        <ion-header>
          <ion-toolbar>
            <ion-title>{{ notaEditando ? 'Editar Nota' : 'Nueva Nota' }}</ion-title>
            <ion-buttons slot="end">
              <ion-button @click="cerrarModales" fill="clear">
                <ion-icon :icon="closeOutline"></ion-icon>
              </ion-button>
            </ion-buttons>
          </ion-toolbar>
        </ion-header>
        
        <ion-content class="ion-padding">
          <form @submit.prevent="guardarNota">
            <ion-item>
              <ion-label position="stacked">Título *</ion-label>
              <ion-input 
                v-model="formularioNota.titulo" 
                placeholder="Ingresa el título de la nota"
                required>
              </ion-input>
            </ion-item>
            
            <ion-item class="ion-margin-top">
              <ion-label position="stacked">Contenido *</ion-label>
              <ion-textarea 
                v-model="formularioNota.contenido" 
                placeholder="Escribe el contenido de la nota..."
                rows="8"
                required>
              </ion-textarea>
            </ion-item>
            
            <div class="ion-margin-top">
              <ion-button 
                type="submit" 
                expand="block" 
                :disabled="!formularioNota.titulo || !formularioNota.contenido || guardandoNota">
                <ion-spinner v-if="guardandoNota" slot="start"></ion-spinner>
                {{ guardandoNota ? 'Guardando...' : (notaEditando ? 'Actualizar' : 'Crear Nota') }}
              </ion-button>
              
              <ion-button 
                @click="cerrarModales" 
                expand="block" 
                fill="outline" 
                class="ion-margin-top">
                Cancelar
              </ion-button>
            </div>
          </form>
        </ion-content>
      </ion-modal>

      <!-- Modal Ver Nota Completa -->
      <ion-modal :is-open="mostrarModalVer" @did-dismiss="mostrarModalVer = false">
        <ion-header>
          <ion-toolbar>
            <ion-title>{{ notaSeleccionada?.titulo }}</ion-title>
            <ion-buttons slot="end">
              <ion-button @click="mostrarModalVer = false" fill="clear">
                <ion-icon :icon="closeOutline"></ion-icon>
              </ion-button>
            </ion-buttons>
          </ion-toolbar>
        </ion-header>
        
        <ion-content class="ion-padding">
          <div v-if="notaSeleccionada">
            <ion-text class="nota-meta">
              <p><strong>Autor:</strong> {{ notaSeleccionada.creadaPorNombre || 'Usuario' }}</p>
              <p><strong>Fecha:</strong> {{ formatearFecha(notaSeleccionada.fechaCreacion) }}</p>
            </ion-text>
            <ion-text class="nota-contenido-completo">
              <p>{{ notaSeleccionada.contenido }}</p>
            </ion-text>
          </div>
        </ion-content>
      </ion-modal>
    </ion-content>
  </ion-page>
</template>

<script setup>
import {
  IonPage, IonHeader, IonToolbar, IonTitle, IonContent, IonButtons, IonButton, IonBackButton,
  IonList, IonCard, IonCardHeader, IonCardTitle, IonCardSubtitle, IonCardContent,
  IonIcon, IonText, IonSpinner, IonSearchbar, IonModal, IonItem, IonLabel, 
  IonInput, IonTextarea, alertController, toastController
} from '@ionic/vue'
import { 
  addOutline, createOutline, trashOutline, documentTextOutline, closeOutline 
} from 'ionicons/icons'
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'

// Estado reactivo
const notas = ref([])
const notasFiltradas = ref([])
const busqueda = ref('')
const cargando = ref(true)
const guardandoNota = ref(false)

// Modales
const mostrarModalCrear = ref(false)
const mostrarModalEditar = ref(false)
const mostrarModalVer = ref(false)

// Formulario y edición
const formularioNota = ref({
  titulo: '',
  contenido: ''
})
const notaEditando = ref(null)
const notaSeleccionada = ref(null)

// Datos de ruta
const route = useRoute()
const grupoId = route.params.id

// Usuario actual
const usuarioActual = ref(null)

onMounted(() => {
  cargarUsuarioActual()
  cargarNotas()
})

const cargarNotas = async () => {
  try {
    cargando.value = true
    const response = await fetch(`${import.meta.env.VITE_API_URL}/nota/${grupoId}/notas`)
    
    if (response.ok) {
      notas.value = await response.json()
      notasFiltradas.value = [...notas.value]
    } else {
      mostrarToast('Error al cargar las notas', 'danger')
    }
  } catch (error) {
    console.error('Error:', error)
    mostrarToast('Error de conexión', 'danger')
  } finally {
    cargando.value = false
  }
}

const cargarUsuarioActual = () => {
  // Obtener usuario del localStorage (guardado en login)
  const usuarioData = localStorage.getItem('usuario')
  if (usuarioData) {
    usuarioActual.value = JSON.parse(usuarioData)
  }
}

const filtrarNotas = () => {
  const termino = busqueda.value.toLowerCase()
  notasFiltradas.value = notas.value.filter(nota => 
    nota.titulo.toLowerCase().includes(termino) || 
    nota.contenido.toLowerCase().includes(termino)
  )
}

const esProietario = (nota) => {
  return usuarioActual.value && nota.creadaPorId === usuarioActual.value.id
}

const editarNota = (nota) => {
  notaEditando.value = nota
  formularioNota.value = {
    titulo: nota.titulo,
    contenido: nota.contenido
  }
  mostrarModalEditar.value = true
}

const verNotaCompleta = (nota) => {
  notaSeleccionada.value = nota
  mostrarModalVer.value = true
}

const guardarNota = async () => {
  try {
    guardandoNota.value = true
    const token = localStorage.getItem('token') // Ajusta según tu implementación
    
    const url = notaEditando.value 
      ? `${import.meta.env.VITE_API_URL}/nota/${notaEditando.value.id}`
      : `${import.meta.env.VITE_API_URL}/nota/${grupoId}/crear`
    
    const method = notaEditando.value ? 'PUT' : 'POST'
    
    const response = await fetch(url, {
      method,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(formularioNota.value)
    })
    
    if (response.ok) {
      await cargarNotas()
      cerrarModales()
      mostrarToast(
        notaEditando.value ? 'Nota actualizada exitosamente' : 'Nota creada exitosamente',
        'success'
      )
    } else {
      const errorText = await response.text()
      mostrarToast('Error al guardar la nota: ' + errorText, 'danger')
    }
  } catch (error) {
    console.error('Error:', error)
    mostrarToast('Error de conexión', 'danger')
  } finally {
    guardandoNota.value = false
  }
}

const confirmarEliminar = async (nota) => {
  const alert = await alertController.create({
    header: 'Confirmar eliminación',
    message: `¿Estás seguro de que quieres eliminar la nota "${nota.titulo}"?`,
    buttons: [
      {
        text: 'Cancelar',
        role: 'cancel'
      },
      {
        text: 'Eliminar',
        role: 'destructive',
        handler: () => eliminarNota(nota)
      }
    ]
  })
  
  await alert.present()
}

const eliminarNota = async (nota) => {
  try {
    const token = localStorage.getItem('token')
    
    const response = await fetch(`${import.meta.env.VITE_API_URL}/nota/${nota.id}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    
    if (response.ok) {
      await cargarNotas()
      mostrarToast('Nota eliminada exitosamente', 'success')
    } else {
      mostrarToast('Error al eliminar la nota', 'danger')
    }
  } catch (error) {
    console.error('Error:', error)
    mostrarToast('Error de conexión', 'danger')
  }
}

const cerrarModales = () => {
  mostrarModalCrear.value = false
  mostrarModalEditar.value = false
  notaEditando.value = null
  formularioNota.value = {
    titulo: '',
    contenido: ''
  }
}

const formatearFecha = (fecha) => {
  if (!fecha) return 'Fecha no disponible'
  return new Date(fecha).toLocaleDateString('es-ES', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const mostrarToast = async (mensaje, color = 'primary') => {
  const toast = await toastController.create({
    message: mensaje,
    duration: 3000,
    color,
    position: 'bottom'
  })
  await toast.present()
}
</script>

<style scoped>
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  gap: 16px;
}

.nota-card {
  margin-bottom: 16px;
}

.nota-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.nota-actions {
  display: flex;
  gap: 4px;
}

.nota-contenido {
  white-space: pre-wrap;
  line-height: 1.5;
  max-height: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.nota-contenido-completo {
  white-space: pre-wrap;
  line-height: 1.6;
}

.nota-meta {
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--ion-color-light);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 24px;
  text-align: center;
}

.empty-icon {
  font-size: 64px;
  color: var(--ion-color-medium);
  margin-bottom: 16px;
}

.empty-state h3 {
  color: var(--ion-color-dark);
  margin-bottom: 8px;
}

.empty-state p {
  color: var(--ion-color-medium);
  margin-bottom: 0;
}
</style>