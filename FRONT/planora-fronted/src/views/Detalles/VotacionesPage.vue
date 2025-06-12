<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-buttons slot="start">
          <ion-back-button :default-href="`/dashboard/${grupoId}`" color="light"></ion-back-button>
        </ion-buttons>
        <ion-title>Votaciones del Grupo</ion-title>
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
        <ion-text>Cargando votaciones...</ion-text>
      </div>

      <!-- Content -->
      <div v-else>
        <!-- Search Bar -->
        <ion-searchbar 
          v-model="busqueda"
          placeholder="Buscar votaciones..."
          @ionInput="filtrarVotaciones"
          class="ion-margin-bottom">
        </ion-searchbar>

        <!-- Votaciones List -->
        <ion-list v-if="votacionesFiltradas.length" class="ion-margin">
          <ion-card v-for="votacion in votacionesFiltradas" :key="votacion.id" class="votacion-card">
            <ion-card-header>
              <div class="votacion-header">
                <ion-card-title>{{ votacion.titulo }}</ion-card-title>
                <div class="votacion-actions">
                  <ion-badge :color="getEstadoColor(votacion.estado)" class="estado-badge">
                    {{ votacion.estado }}
                  </ion-badge>
                  <ion-button 
                    @click="editarVotacion(votacion)" 
                    fill="clear" 
                    size="small"
                    v-if="esPropietario(votacion) && votacion.estado === 'ACTIVA'">
                    <ion-icon :icon="createOutline"></ion-icon>
                  </ion-button>
                  <ion-button 
                    @click="cerrarVotacion(votacion)" 
                    fill="clear" 
                    size="small" 
                    color="warning"
                    v-if="esPropietario(votacion) && votacion.estado === 'ACTIVA'">
                    <ion-icon :icon="lockClosedOutline"></ion-icon>
                  </ion-button>
                  <ion-button 
                    @click="confirmarEliminar(votacion)" 
                    fill="clear" 
                    size="small" 
                    color="danger"
                    v-if="esPropietario(votacion)">
                    <ion-icon :icon="trashOutline"></ion-icon>
                  </ion-button>
                </div>
              </div>
              <ion-card-subtitle>
                Por: {{ votacion.nombreCreador || 'Usuario' }} • 
                {{ formatearFecha(votacion.fechaCreacion) }}
                <span v-if="votacion.fechaCierre">
                  • Cierra: {{ formatearFecha(votacion.fechaCierre) }}
                </span>
              </ion-card-subtitle>
            </ion-card-header>
            
            <ion-card-content>
              <p class="votacion-descripcion" v-if="votacion.descripcion">
                {{ votacion.descripcion }}
              </p>
              
              <!-- Opciones de votación -->
              <div class="opciones-container" v-if="votacion.opciones">
                <h4>Opciones:</h4>
                <ion-chip 
                  v-for="opcion in votacion.opciones" 
                  :key="opcion" 
                  outline>
                  {{ opcion }}
                </ion-chip>
              </div>

              <!-- Estado de votación del usuario -->
              <div class="estado-voto" v-if="votacion.estado === 'ACTIVA'">
                <ion-button 
                  @click="abrirModalVotar(votacion)" 
                  fill="outline" 
                  size="small"
                  v-if="!votacion.yaVote">
                  <ion-icon :icon="checkboxOutline" slot="start"></ion-icon>
                  Votar
                </ion-button>
                <ion-chip color="success" v-else>
                  <ion-icon :icon="checkmarkCircleOutline" slot="start"></ion-icon>
                  Ya votaste
                </ion-chip>
              </div>

              <!-- Botones de acción -->
              <div class="acciones-votacion">
                <ion-button 
                  @click="verResultados(votacion)" 
                  fill="clear" 
                  size="small">
                  <ion-icon :icon="barChartOutline" slot="start"></ion-icon>
                  Ver Resultados
                </ion-button>
              </div>
            </ion-card-content>
          </ion-card>
        </ion-list>

        <!-- Empty State -->
        <div v-else-if="!cargando" class="empty-state">
          <ion-icon :icon="barChartOutline" class="empty-icon"></ion-icon>
          <ion-text>
            <h3>No hay votaciones disponibles</h3>
            <p>¡Sé el primero en crear una votación en este grupo!</p>
          </ion-text>
          <ion-button @click="mostrarModalCrear = true" expand="block" class="ion-margin-top">
            Crear primera votación
          </ion-button>
        </div>
      </div>

      <!-- Modal Crear/Editar Votación -->
      <ion-modal :is-open="mostrarModalCrear || mostrarModalEditar" @did-dismiss="cerrarModales">
        <ion-header>
          <ion-toolbar>
            <ion-title>{{ votacionEditando ? 'Editar Votación' : 'Nueva Votación' }}</ion-title>
            <ion-buttons slot="end">
              <ion-button @click="cerrarModales" fill="clear">
                <ion-icon :icon="closeOutline"></ion-icon>
              </ion-button>
            </ion-buttons>
          </ion-toolbar>
        </ion-header>
        
        <ion-content class="ion-padding">
          <form @submit.prevent="guardarVotacion">
            <ion-item>
              <ion-label position="stacked">Pregunta/Título *</ion-label>
              <ion-input 
                v-model="formularioVotacion.pregunta" 
                placeholder="¿Cuál es tu pregunta?"
                required>
              </ion-input>
            </ion-item>
            
            <ion-item class="ion-margin-top">
              <ion-label position="stacked">Descripción</ion-label>
              <ion-textarea 
                v-model="formularioVotacion.descripcion" 
                placeholder="Descripción opcional de la votación..."
                rows="3">
              </ion-textarea>
            </ion-item>

            <!-- Opciones -->
            <div class="opciones-form ion-margin-top">
              <ion-label>
                <h3>Opciones de votación *</h3>
              </ion-label>
              
              <div v-for="(opcion, index) in formularioVotacion.opciones" :key="index" class="opcion-item">
                <ion-item>
                  <ion-input 
                    v-model="formularioVotacion.opciones[index]" 
                    :placeholder="`Opción ${index + 1}`"
                    required>
                  </ion-input>
                  <ion-button 
                    @click="eliminarOpcion(index)" 
                    fill="clear" 
                    slot="end"
                    v-if="formularioVotacion.opciones.length > 2">
                    <ion-icon :icon="removeCircleOutline" color="danger"></ion-icon>
                  </ion-button>
                </ion-item>
              </div>
              
              <ion-button 
                @click="agregarOpcion" 
                fill="outline" 
                expand="block" 
                class="ion-margin-top">
                <ion-icon :icon="addCircleOutline" slot="start"></ion-icon>
                Agregar opción
              </ion-button>
            </div>

            <!-- Fecha límite -->
            <ion-item class="ion-margin-top">
              <ion-label position="stacked">Fecha límite (opcional)</ion-label>
              <ion-datetime 
                v-model="formularioVotacion.fechaLimite"
                presentation="date-time"
                :min="new Date().toISOString()">
              </ion-datetime>
            </ion-item>
            
            <div class="ion-margin-top">
              <ion-button 
                type="submit" 
                expand="block" 
                :disabled="!puedeGuardar || guardandoVotacion">
                <ion-spinner v-if="guardandoVotacion" slot="start"></ion-spinner>
                {{ guardandoVotacion ? 'Guardando...' : (votacionEditando ? 'Actualizar' : 'Crear Votación') }}
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

      <!-- Modal Votar -->
      <ion-modal :is-open="mostrarModalVotar" @did-dismiss="mostrarModalVotar = false">
        <ion-header>
          <ion-toolbar>
            <ion-title>Votar</ion-title>
            <ion-buttons slot="end">
              <ion-button @click="mostrarModalVotar = false" fill="clear">
                <ion-icon :icon="closeOutline"></ion-icon>
              </ion-button>
            </ion-buttons>
          </ion-toolbar>
        </ion-header>
        
        <ion-content class="ion-padding">
          <div v-if="votacionParaVotar">
            <h2>{{ votacionParaVotar.titulo }}</h2>
            <p v-if="votacionParaVotar.descripcion">{{ votacionParaVotar.descripcion }}</p>
            
            <ion-radio-group v-model="opcionSeleccionada">
              <ion-item v-for="opcion in votacionParaVotar.opciones" :key="opcion">
                <ion-label>{{ opcion }}</ion-label>
                <ion-radio slot="start" :value="opcion"></ion-radio>
              </ion-item>
            </ion-radio-group>
            
            <div class="ion-margin-top">
              <ion-button 
                @click="confirmarVoto" 
                expand="block" 
                :disabled="!opcionSeleccionada || enviandoVoto">
                <ion-spinner v-if="enviandoVoto" slot="start"></ion-spinner>
                {{ enviandoVoto ? 'Enviando voto...' : 'Confirmar voto' }}
              </ion-button>
              
              <ion-button 
                @click="mostrarModalVotar = false" 
                expand="block" 
                fill="outline" 
                class="ion-margin-top">
                Cancelar
              </ion-button>
            </div>
          </div>
        </ion-content>
      </ion-modal>

      <!-- Modal Resultados -->
      <ion-modal :is-open="mostrarModalResultados" @did-dismiss="mostrarModalResultados = false">
        <ion-header>
          <ion-toolbar>
            <ion-title>Resultados</ion-title>
            <ion-buttons slot="end">
              <ion-button @click="mostrarModalResultados = false" fill="clear">
                <ion-icon :icon="closeOutline"></ion-icon>
              </ion-button>
            </ion-buttons>
          </ion-toolbar>
        </ion-header>
        
        <ion-content class="ion-padding">
          <div v-if="resultadosVotacion">
            <h2>{{ resultadosVotacion.titulo }}</h2>
            <p><strong>Total de votos:</strong> {{ resultadosVotacion.totalVotos }}</p>
            <p><strong>Estado:</strong> {{ resultadosVotacion.estado }}</p>
            
            <div class="resultados-container ion-margin-top">
              <div v-for="(cantidad, opcion) in resultadosVotacion.resultados" :key="opcion" class="resultado-item">
                <div class="resultado-header">
                  <span class="opcion-nombre">{{ opcion }}</span>
                  <span class="votos-cantidad">{{ cantidad }} votos</span>
                </div>
                <ion-progress-bar 
                  :value="calcularPorcentaje(cantidad, resultadosVotacion.totalVotos)"
                  class="ion-margin-top">
                </ion-progress-bar>
                <span class="porcentaje">
                  {{ calcularPorcentaje(cantidad, resultadosVotacion.totalVotos) * 100 }}%
                </span>
              </div>
            </div>
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
  IonInput, IonTextarea, IonDatetime, IonRadioGroup, IonRadio, IonChip, IonBadge,
  IonProgressBar, alertController, toastController
} from '@ionic/vue'
import { 
  addOutline, createOutline, trashOutline, closeOutline, checkboxOutline,
  checkmarkCircleOutline, barChartOutline, lockClosedOutline, addCircleOutline,
  removeCircleOutline
} from 'ionicons/icons'
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'

// Estado reactivo
const votaciones = ref([])
const votacionesFiltradas = ref([])
const busqueda = ref('')
const cargando = ref(true)
const guardandoVotacion = ref(false)
const enviandoVoto = ref(false)

// Modales
const mostrarModalCrear = ref(false)
const mostrarModalEditar = ref(false)
const mostrarModalVotar = ref(false)
const mostrarModalResultados = ref(false)

// Formulario y edición
const formularioVotacion = ref({
  pregunta: '',
  descripcion: '',
  opciones: ['', ''],
  fechaLimite: null
})
const votacionEditando = ref(null)
const votacionParaVotar = ref(null)
const opcionSeleccionada = ref('')
const resultadosVotacion = ref(null)

// Datos de ruta
const route = useRoute()
const grupoId = route.params.id

// Usuario actual
const usuarioActual = ref(null)

// Computed
const puedeGuardar = computed(() => {
  return formularioVotacion.value.pregunta && 
         formularioVotacion.value.opciones.length >= 2 &&
         formularioVotacion.value.opciones.every(opcion => opcion.trim())
})

onMounted(() => {
  cargarUsuarioActual()
  cargarVotaciones()
})

const cargarVotaciones = async () => {
  try {
    cargando.value = true
    const token = localStorage.getItem('token')
    
    const response = await fetch(`${import.meta.env.VITE_API_URL}/grupos/${grupoId}/votaciones`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    
    if (response.ok) {
      votaciones.value = await response.json()
      
      // Verificar el estado de voto para cada votación
      for (const votacion of votaciones.value) {
        await verificarEstadoVoto(votacion)
      }
      
      votacionesFiltradas.value = [...votaciones.value]
    } else {
      mostrarToast('Error al cargar las votaciones', 'danger')
    }
  } catch (error) {
    console.error('Error:', error)
    mostrarToast('Error de conexión', 'danger')
  } finally {
    cargando.value = false
  }
}

const verificarEstadoVoto = async (votacion) => {
  try {
    const token = localStorage.getItem('token')
    const response = await fetch(`${import.meta.env.VITE_API_URL}/votaciones/${votacion.id}/mi-voto`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    
    if (response.ok) {
      const voto = await response.json()
      votacion.yaVote = true
      votacion.miVoto = voto.opcion
    } else if (response.status === 404) {
      // No ha votado, esto es normal
      votacion.yaVote = false
    } else {
      console.error('Error verificando voto:', response.status)
      votacion.yaVote = false
    }
  } catch (error) {
    console.error('Error de conexión verificando voto:', error)
    votacion.yaVote = false
  }
}

const cargarUsuarioActual = () => {
  const usuarioData = localStorage.getItem('usuario')
  if (usuarioData) {
    usuarioActual.value = JSON.parse(usuarioData)
  }
}

const filtrarVotaciones = () => {
  const termino = busqueda.value.toLowerCase()
  votacionesFiltradas.value = votaciones.value.filter(votacion => 
    votacion.titulo.toLowerCase().includes(termino) || 
    (votacion.descripcion && votacion.descripcion.toLowerCase().includes(termino))
  )
}

const esPropietario = (votacion) => {
  return usuarioActual.value && votacion.creadoPorId === usuarioActual.value.id
}

const getEstadoColor = (estado) => {
  switch (estado) {
    case 'ACTIVA': return 'success'
    case 'CERRADA': return 'medium'
    case 'PENDIENTE': return 'warning'
    default: return 'medium'
  }
}

const editarVotacion = (votacion) => {
  votacionEditando.value = votacion
  formularioVotacion.value = {
    pregunta: votacion.titulo,
    descripcion: votacion.descripcion || '',
    opciones: [...votacion.opciones],
    fechaLimite: votacion.fechaCierre
  }
  mostrarModalEditar.value = true
}

const agregarOpcion = () => {
  formularioVotacion.value.opciones.push('')
}

const eliminarOpcion = (index) => {
  if (formularioVotacion.value.opciones.length > 2) {
    formularioVotacion.value.opciones.splice(index, 1)
  }
}

const guardarVotacion = async () => {
  try {
    guardandoVotacion.value = true
    const token = localStorage.getItem('token')
    
    const url = votacionEditando.value 
      ? `${import.meta.env.VITE_API_URL}/votaciones/${votacionEditando.value.id}`
      : `${import.meta.env.VITE_API_URL}/grupos/${grupoId}/votaciones`
    
    const method = votacionEditando.value ? 'PUT' : 'POST'
    
    const body = {
      pregunta: formularioVotacion.value.pregunta,
      descripcion: formularioVotacion.value.descripcion,
      opciones: formularioVotacion.value.opciones.filter(opcion => opcion.trim()),
      fechaLimite: formularioVotacion.value.fechaLimite
    }
    
    const response = await fetch(url, {
      method,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(body)
    })
    
    if (response.ok) {
      await cargarVotaciones()
      cerrarModales()
      mostrarToast(
        votacionEditando.value ? 'Votación actualizada exitosamente' : 'Votación creada exitosamente',
        'success'
      )
    } else {
      const errorData = await response.json()
      mostrarToast('Error al guardar la votación: ' + (errorData.error || 'Error desconocido'), 'danger')
    }
  } catch (error) {
    console.error('Error:', error)
    mostrarToast('Error de conexión', 'danger')
  } finally {
    guardandoVotacion.value = false
  }
}

const abrirModalVotar = (votacion) => {
  if (votacion.yaVote) {
    mostrarToast('Ya has votado en esta votación', 'warning')
    return
  }
  
  votacionParaVotar.value = votacion
  opcionSeleccionada.value = ''
  mostrarModalVotar.value = true
}

const confirmarVoto = async () => {
  try {
    enviandoVoto.value = true
    const token = localStorage.getItem('token')
    
    const response = await fetch(`${import.meta.env.VITE_API_URL}/votaciones/${votacionParaVotar.value.id}/votar`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({
        opcion: opcionSeleccionada.value
      })
    })
    
    if (response.ok) {
      mostrarModalVotar.value = false
      await cargarVotaciones()
      mostrarToast('Voto registrado exitosamente', 'success')
    } else {
      const errorData = await response.json()
      mostrarToast('Error al votar: ' + (errorData.error || 'Error desconocido'), 'danger')
    }
  } catch (error) {
    console.error('Error:', error)
    mostrarToast('Error de conexión', 'danger')
  } finally {
    enviandoVoto.value = false
  }
}

const verResultados = async (votacion) => {
  try {
    const response = await fetch(`${import.meta.env.VITE_API_URL}/votaciones/${votacion.id}/resultados`)
    
    if (response.ok) {
      resultadosVotacion.value = await response.json()
      mostrarModalResultados.value = true
    } else {
      mostrarToast('Error al cargar los resultados', 'danger')
    }
  } catch (error) {
    console.error('Error:', error)
    mostrarToast('Error de conexión', 'danger')
  }
}

const cerrarVotacion = async (votacion) => {
  const alert = await alertController.create({
    header: 'Cerrar votación',
    message: `¿Estás seguro de que quieres cerrar la votación "${votacion.titulo}"? Esta acción no se puede deshacer.`,
    buttons: [
      {
        text: 'Cancelar',
        role: 'cancel'
      },
      {
        text: 'Cerrar',
        role: 'destructive',
        handler: async () => {
          try {
            const token = localStorage.getItem('token')
            const response = await fetch(`${import.meta.env.VITE_API_URL}/votaciones/${votacion.id}/cerrar`, {
              method: 'PUT',
              headers: {
                'Authorization': `Bearer ${token}`
              }
            })
            
            if (response.ok) {
              await cargarVotaciones()
              mostrarToast('Votación cerrada exitosamente', 'success')
            } else {
              mostrarToast('Error al cerrar la votación', 'danger')
            }
          } catch (error) {
            console.error('Error:', error)
            mostrarToast('Error de conexión', 'danger')
          }
        }
      }
    ]
  })
  
  await alert.present()
}

const confirmarEliminar = async (votacion) => {
  const alert = await alertController.create({
    header: 'Confirmar eliminación',
    message: `¿Estás seguro de que quieres eliminar la votación "${votacion.titulo}"?`,
    buttons: [
      {
        text: 'Cancelar',
        role: 'cancel'
      },
      {
        text: 'Eliminar',
        role: 'destructive',
        handler: () => eliminarVotacion(votacion)
      }
    ]
  })
  
  await alert.present()
}

const eliminarVotacion = async (votacion) => {
  try {
    const token = localStorage.getItem('token')
    
    const response = await fetch(`${import.meta.env.VITE_API_URL}/votaciones/${votacion.id}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    
    if (response.ok) {
      await cargarVotaciones()
      mostrarToast('Votación eliminada exitosamente', 'success')
    } else {
      mostrarToast('Error al eliminar la votación', 'danger')
    }
  } catch (error) {
    console.error('Error:', error)
    mostrarToast('Error de conexión', 'danger')
  }
}

const cerrarModales = () => {
  mostrarModalCrear.value = false
  mostrarModalEditar.value = false
  votacionEditando.value = null
  formularioVotacion.value = {
    pregunta: '',
    descripcion: '',
    opciones: ['', ''],
    fechaLimite: null
  }
}

const calcularPorcentaje = (cantidad, total) => {
  return total > 0 ? cantidad / total : 0
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

.votacion-card {
  margin-bottom: 16px;
}

.votacion-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.votacion-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.estado-badge {
  margin-right: 8px;
}

.votacion-descripcion {
  margin: 12px 0;
  color: var(--ion-color-medium);
}

.opciones-container {
  margin: 16px 0;
}

.opciones-container h4 {
  margin: 0 0 8px 0;
  font-size: 14px;
  font-weight: 600;
  color: var(--ion-color-medium);
}

.estado-voto {
  margin: 16px 0;
}

.acciones-votacion {
  margin-top: 16px;
  display: flex;
  gap: 8px;
  justify-content: flex-start;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.empty-icon {
  font-size: 80px;
  color: var(--ion-color-medium);
  margin-bottom: 20px;
}

.opciones-form {
  border: 1px solid var(--ion-color-light);
  border-radius: 8px;
  padding: 16px;
}

.opcion-item {
  margin-bottom: 8px;
}

.resultados-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.resultado-item {
  padding: 16px;
  border: 1px solid var(--ion-color-light);
  border-radius: 8px;
}

.resultado-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.opcion-nombre {
  font-weight: 600;
}

.votos-cantidad {
  color: var(--ion-color-medium);
  font-size: 14px;
}

.porcentaje {
  font-size: 12px;
  color: var(--ion-color-medium);
  margin-top: 4px;
  display: block;
}
</style>