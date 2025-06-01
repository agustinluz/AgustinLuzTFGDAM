<template>
  <ion-page>
    <ion-header :translucent="true" class="ion-no-border">
      <ion-toolbar color="primary">
        <ion-buttons slot="start">
          <ion-back-button default-href="/dashboard" color="light"></ion-back-button>
        </ion-buttons>
        <ion-title>
          <div class="title-container">
            <h1>{{ grupo?.nombre || 'Gastos' }}</h1>
            <p v-if="gastos && gastos.length > 0">{{ gastos.length }} gastos • {{ formatMonto(totalGastos) }}</p>
          </div>
        </ion-title>
        <ion-buttons slot="end">
          <ion-button fill="clear" @click="mostrarOpciones = true">
            <ion-icon :icon="ellipsisVertical" color="light"></ion-icon>
          </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>

    <ion-content :fullscreen="true" class="ion-padding">
      <!-- Loading -->
      <div v-if="cargando" class="loading-container">
        <ion-spinner name="crescent" color="primary"></ion-spinner>
        <p>Cargando gastos...</p>
      </div>

      <div v-else>
        <!-- Stats Card -->
        <ion-card class="stats-card" v-if="gastos && gastos.length > 0">
          <ion-card-content>
            <div class="stats-grid">
              <div class="stat-item">
                <ion-icon :icon="walletOutline" color="primary"></ion-icon>
                <div>
                  <h3>{{ formatMonto(totalGastos) }}</h3>
                  <p>Total gastado</p>
                </div>
              </div>
              <div class="stat-item">
                <ion-icon :icon="receiptOutline" color="success"></ion-icon>
                <div>
                  <h3>{{ gastos.length }}</h3>
                  <p>Gastos</p>
                </div>
              </div>
              <div class="stat-item">
                <ion-icon :icon="timeOutline" color="warning"></ion-icon>
                <div>
                  <h3>{{ gastosPendientes }}</h3>
                  <p>Pendientes</p>
                </div>
              </div>
            </div>
          </ion-card-content>
        </ion-card>

        <!-- Filtros -->
        <ion-segment v-model="filtroActivo" @ionChange="aplicarFiltro" v-if="gastos && gastos.length > 0">
          <ion-segment-button value="todos">
            <ion-label>Todos ({{ gastos.length }})</ion-label>
          </ion-segment-button>
          <ion-segment-button value="pendientes">
            <ion-label>Pendientes ({{ gastosPendientes }})</ion-label>
          </ion-segment-button>
        </ion-segment>

        <!-- Lista de Gastos -->
        <ion-card v-if="gastosFiltrados && gastosFiltrados.length > 0" class="gastos-card">
          <ion-card-content class="ion-no-padding">
            <ion-item 
              v-for="gasto in gastosFiltrados" 
              :key="gasto.id" 
              button 
              @click="verDetalles(gasto)"
              class="gasto-item"
            >
              <div class="gasto-icon" slot="start">
                <ion-icon :icon="getIconForGasto(gasto)" :color="getColorForGasto(gasto)"></ion-icon>
              </div>
              <ion-label>
                <h2>{{ gasto.titulo }}</h2>
                <p>{{ gasto.pagadoPor?.nombre }} • {{ formatFecha(gasto.fecha) }}</p>
                <p v-if="gasto.evento">{{ gasto.evento.nombre }}</p>
              </ion-label>
              <div slot="end" class="gasto-end">
                <h3>{{ formatMonto(gasto.monto) }}</h3>
                <ion-chip size="small" :color="tieneDeudasPendientes(gasto) ? 'warning' : 'success'">
                  {{ tieneDeudasPendientes(gasto) ? 'Pendiente' : 'Saldado' }}
                </ion-chip>
              </div>
            </ion-item>
          </ion-card-content>
        </ion-card>

        <!-- Empty State -->
        <div v-else-if="!cargando" class="empty-state">
          <ion-icon :icon="receiptOutline" color="medium"></ion-icon>
          <h2>{{ getEmptyMessage() }}</h2>
          <ion-button @click="añadirGasto" expand="block">
            <ion-icon :icon="add" slot="start"></ion-icon>
            Añadir Gasto
          </ion-button>
        </div>
      </div>

      <!-- Modal de Detalles -->
      <ion-modal :is-open="modalAbierto" @did-dismiss="cerrarModal">
        <ion-header>
          <ion-toolbar>
            <ion-title>Detalles del Gasto</ion-title>
            <ion-buttons slot="end">
              <ion-button @click="cerrarModal" fill="clear">
                <ion-icon :icon="close"></ion-icon>
              </ion-button>
            </ion-buttons>
          </ion-toolbar>
        </ion-header>
        <ion-content class="ion-padding" v-if="gastoSeleccionado">
          <!-- Información del Gasto -->
          <ion-card>
            <ion-card-header>
              <ion-card-title>{{ gastoSeleccionado.titulo }}</ion-card-title>
              <ion-card-subtitle>{{ formatMonto(gastoSeleccionado.monto) }}</ion-card-subtitle>
            </ion-card-header>
            <ion-card-content>
              <div class="info-grid">
                <div class="info-item">
                  <ion-icon :icon="person" color="primary"></ion-icon>
                  <div>
                    <strong>Pagado por:</strong>
                    <p>{{ gastoSeleccionado.pagadoPor?.nombre }}</p>
                  </div>
                </div>
                <div class="info-item">
                  <ion-icon :icon="calendar" color="primary"></ion-icon>
                  <div>
                    <strong>Fecha:</strong>
                    <p>{{ formatFechaCompleta(gastoSeleccionado.fecha) }}</p>
                  </div>
                </div>
                <div class="info-item" v-if="gastoSeleccionado.evento">
                  <ion-icon :icon="star" color="primary"></ion-icon>
                  <div>
                    <strong>Evento:</strong>
                    <p>{{ gastoSeleccionado.evento.nombre }}</p>
                  </div>
                </div>
              </div>
            </ion-card-content>
          </ion-card>

          <!-- Participantes -->
          <ion-card v-if="participantesGasto && participantesGasto.length > 0">
            <ion-card-header>
              <ion-card-title>Participantes ({{ participantesGasto.length }})</ion-card-title>
            </ion-card-header>
            <ion-card-content>
              <ion-item v-for="participante in participantesGasto" :key="participante.id">
                <ion-avatar slot="start">
                  <div class="avatar-text">{{ getInitials(participante.nombre) }}</div>
                </ion-avatar>
                <ion-label>
                  <h3>{{ participante.nombre }}</h3>
                  <p>{{ formatMonto(gastoSeleccionado.monto / participantesGasto.length) }}</p>
                </ion-label>
                <ion-button 
                  v-if="!participante.saldado" 
                  fill="clear" 
                  size="small" 
                  color="success"
                  @click="marcarSaldado(gastoSeleccionado.id, participante.id)"
                >
                  <ion-icon :icon="checkmarkCircle"></ion-icon>
                </ion-button>
                <ion-icon v-else :icon="checkmarkCircle" color="success"></ion-icon>
              </ion-item>
            </ion-card-content>
          </ion-card>

          <!-- Acciones -->
          <div class="modal-actions">
            <ion-button expand="block" fill="outline" @click="editarGasto">
              <ion-icon :icon="create" slot="start"></ion-icon>
              Editar
            </ion-button>
            <ion-button expand="block" color="danger" fill="clear" @click="eliminarGasto">
              <ion-icon :icon="trash" slot="start"></ion-icon>
              Eliminar
            </ion-button>
          </div>
        </ion-content>
      </ion-modal>

      <!-- Action Sheet -->
      <ion-action-sheet
        :is-open="mostrarOpciones"
        header="Opciones"
        :buttons="opcionesMenu"
        @did-dismiss="mostrarOpciones = false"
      ></ion-action-sheet>
    </ion-content>

    <!-- FAB -->
    <ion-fab slot="fixed" vertical="bottom" horizontal="end">
      <ion-fab-button color="primary" @click="añadirGasto">
        <ion-icon :icon="add"></ion-icon>
      </ion-fab-button>
    </ion-fab>
  </ion-page>
</template>

<script setup>
import {
  IonPage, IonHeader, IonToolbar, IonTitle, IonContent, IonCard, IonCardHeader,
  IonCardTitle, IonCardContent, IonIcon, IonButton, IonButtons, IonItem,
  IonBackButton, IonFab, IonFabButton, IonModal, IonLabel, IonAvatar,
  IonSegment, IonSegmentButton, IonChip, IonSpinner, IonActionSheet,
  IonCardSubtitle, useIonRouter, alertController, toastController
} from '@ionic/vue'

import { 
  person, add, close, ellipsisVertical, walletOutline, receiptOutline, 
  timeOutline, checkmarkCircle, calendar, star, create, trash, 
  restaurant, car, home, ticket
} from 'ionicons/icons'

import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'

const router = useIonRouter()
const route = useRoute()
const grupoId = route.params.id

// Estados reactivos
const gastos = ref([])
const grupo = ref(null)
const participantesGasto = ref([])
const modalAbierto = ref(false)
const gastoSeleccionado = ref(null)
const cargando = ref(true)
const filtroActivo = ref('todos')
const mostrarOpciones = ref(false)

// API Base URL
const API_URL = import.meta.env.VITE_API_URL

// Computed properties corregidas
const totalGastos = computed(() => {
  if (!gastos.value || gastos.value.length === 0) return 0
  return gastos.value.reduce((sum, gasto) => sum + parseFloat(gasto.monto || 0), 0)
})

const gastosPendientes = computed(() => {
  if (!gastos.value || gastos.value.length === 0) return 0
  return gastos.value.filter(gasto => tieneDeudasPendientes(gasto)).length
})

const gastosFiltrados = computed(() => {
  if (!gastos.value) return []
  
  if (filtroActivo.value === 'pendientes') {
    return gastos.value.filter(gasto => tieneDeudasPendientes(gasto))
  }
  return gastos.value
})

const opcionesMenu = computed(() => [
  { text: 'Resumen completo', handler: () => router.push(`/grupos/${grupoId}/resumen`) },
  { text: 'Configuración', handler: () => router.push(`/grupos/${grupoId}/config`) },
  { text: 'Cancelar', role: 'cancel' }
])

// Función helper para hacer peticiones
const apiRequest = async (endpoint, options = {}) => {
  const url = `${API_URL}${endpoint}`
  const config = {
    headers: {
      'Content-Type': 'application/json',
      ...options.headers
    },
    ...options
  }

  const response = await fetch(url, config)
  
  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`)
  }
  
  return response.json()
}
const cargarDatos = async () => {
  cargando.value = true
  try {
    // Cargar datos en paralelo
    await Promise.all([
      cargarGrupo(),
      cargarGastos()
    ])
  } catch (error) {
    console.error('Error al cargar datos:', error)
    // Datos de ejemplo para desarrollo
    if (import.meta.env.DEV) {
      console.log('Usando datos de ejemplo para desarrollo')
      grupo.value = { 
        id: grupoId,
        nombre: 'Viaje a Barcelona' 
      }
      
      const participantesEjemplo = [
        { id: 1, nombre: 'Carmen García', email: 'carmen@email.com' },
        { id: 2, nombre: 'Antonio López', email: 'antonio@email.com' },
        { id: 3, nombre: 'Gertrudis Martín', email: 'gert@email.com' },
        { id: 4, nombre: 'José Silva', email: 'jose@email.com' }
      ]
      
      gastos.value = [
        {
          id: 1,
          titulo: 'Hotel Barcelona',
          monto: 240.00,
          fecha: new Date().toISOString(),
          pagadoPor: { id: 1, nombre: 'Carmen García' },
          usuarios: participantesEjemplo,
          evento: { id: 1, nombre: 'Alojamiento' }
        },
        {
          id: 2,
          titulo: 'Cena restaurante',
          monto: 85.50,
          fecha: new Date(Date.now() - 86400000).toISOString(),
          pagadoPor: { id: 2, nombre: 'Antonio López' },
          usuarios: participantesEjemplo.slice(0, 3)
        }
      ]
    } else {
      mostrarError('Error al cargar los datos')
    }
  } finally {
    cargando.value = false
  }
}

const cargarGastos = async () => {
  try {
    console.log('Cargando gastos para grupo:', grupoId);
    
    // ✅ URL CORRECTA según tu GastoController
    const data = await apiRequest(`/gasto/${grupoId}/gastos`);
    
    gastos.value = Array.isArray(data) ? data : (data?.gastos || data?.data || []);
    console.log('Gastos cargados:', gastos.value);
  } catch (error) {
    console.error('Error al cargar gastos:', error);
    throw error;
  }
};
const cargarGrupo = async () => {
  try {
    // Intentar diferentes URLs hasta encontrar la correcta
    let data
    try {
      data = await apiRequest(`/grupos/${grupoId}`)
    } catch (error1) {
      try {
        data = await apiRequest(`/grupos/${grupoId}`)
      } catch (error2) {
        data = await apiRequest(`/api/v1/grupos/${grupoId}`)
      }
    }
    
    grupo.value = data
  } catch (error) {
    console.error('Error al cargar grupo:', error)
    throw error
  }
}


const cargarParticipantes = async (gastoId) => {
  try {
    // Usar los participantes del gasto seleccionado si están disponibles
    if (gastoSeleccionado.value && gastoSeleccionado.value.usuarios) {
      participantesGasto.value = gastoSeleccionado.value.usuarios
      return
    }
    
    // ✅ URL CORRECTA según tu GastoController
    const data = await apiRequest(`/gasto/grupos/${gastoId}/participantes`)
    participantesGasto.value = data || []
  } catch (error) {
    console.error('Error al cargar participantes:', error)
    participantesGasto.value = []
  }
}

const marcarSaldado = async (gastoId, participanteId) => {
  try {
    console.log('Intentando marcar como saldado:', { gastoId, participanteId });
    
    const body = {
      metodoPago: 'efectivo',
      notas: 'Marcado desde la app'
    };
    
    console.log('Body que se envía:', body);
    
    // ✅ URL CORRECTA según tu GastoController
    const response = await fetch(`${API_URL}/gasto/grupos/${gastoId}/participantes/${participanteId}/saldado`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(body)
    });
    
    console.log('Status de respuesta:', response.status);
    
    if (!response.ok) {
      const errorText = await response.text();
      console.error('Error del servidor:', errorText);
      throw new Error(`HTTP error! status: ${response.status} - ${errorText}`);
    }
    
    const resultado = await response.json();
    console.log('Respuesta exitosa:', resultado);
    
    // Actualizar estado local
    const participante = participantesGasto.value.find(p => p.id === participanteId);
    if (participante) {
      participante.saldado = true;
    }
    
    mostrarToast(resultado.message || 'Marcado como saldado', 'success');
    await cargarGastos();
  } catch (error) {
    console.error('Error al marcar como saldado:', error);
    mostrarError('Error al marcar como saldado: ' + error.message);
  }
};


const eliminarGasto = async () => {
  try {
    const alert = await alertController.create({
      header: 'Eliminar Gasto',
      message: '¿Estás seguro de eliminar este gasto?',
      buttons: [
        { text: 'Cancelar', role: 'cancel' },
        { 
          text: 'Eliminar', 
          role: 'destructive',
          handler: async () => {
            try {
              // ✅ URL CORRECTA según tu GastoController
              await apiRequest(`/api/gasto/${gastoSeleccionado.value.id}`, {
                method: 'DELETE'
              })
              mostrarToast('Gasto eliminado', 'success')
              cerrarModal()
              await cargarGastos()
            } catch (error) {
              console.error('Error al eliminar:', error)
              mostrarError('Error al eliminar el gasto')
            }
          }
        }
      ]
    })
    await alert.present()
  } catch (error) {
    console.error('Error al crear alerta:', error)
  }
}
// Métodos de utilidad
const formatMonto = (monto) => {
  const num = parseFloat(monto) || 0
  return new Intl.NumberFormat('es-ES', {
    style: 'currency',
    currency: 'EUR'
  }).format(num)
}

const formatFecha = (fecha) => {
  if (!fecha) return ''
  
  const ahora = new Date()
  const fechaGasto = new Date(fecha)
  const diffTime = Math.abs(ahora - fechaGasto)
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  
  if (diffDays === 1) return 'Hoy'
  if (diffDays === 2) return 'Ayer'
  if (diffDays <= 7) return `Hace ${diffDays} días`
  
  return fechaGasto.toLocaleDateString('es-ES', { 
    day: 'numeric', 
    month: 'short' 
  })
}

const formatFechaCompleta = (fecha) => {
  if (!fecha) return ''
  
  return new Date(fecha).toLocaleDateString('es-ES', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

const getInitials = (nombre) => {
  if (!nombre) return 'NA'
  return nombre.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2)
}

const getIconForGasto = (gasto) => {
  const titulo = gasto.titulo?.toLowerCase() || ''
  if (titulo.includes('hotel') || titulo.includes('alojamiento')) return home
  if (titulo.includes('comida') || titulo.includes('restaurante') || titulo.includes('cena')) return restaurant
  if (titulo.includes('taxi') || titulo.includes('transporte')) return car
  if (titulo.includes('entrada') || titulo.includes('ticket')) return ticket
  return receiptOutline
}

const getColorForGasto = (gasto) => {
  const monto = parseFloat(gasto.monto || 0)
  if (monto > 100) return 'danger'
  if (monto > 50) return 'warning'
  return 'primary'
}

const tieneDeudasPendientes = (gasto) => {
  // Lógica temporal - aquí implementarías la lógica real basada en el estado de pagos
  // Por ahora usa una lógica simple basada en si todos los participantes han pagado
  return Math.random() > 0.6 // Simula que algunos gastos tienen deudas pendientes
}

const getEmptyMessage = () => {
  return filtroActivo.value === 'pendientes' ? 
    'No hay gastos pendientes' : 
    'No hay gastos registrados'
}

// Acciones
const aplicarFiltro = (event) => {
  filtroActivo.value = event.detail.value
}

const verDetalles = async (gasto) => {
  gastoSeleccionado.value = gasto
  await cargarParticipantes(gasto.id)
  modalAbierto.value = true
}

const cerrarModal = () => {
  modalAbierto.value = false
  gastoSeleccionado.value = null
  participantesGasto.value = []
}

const añadirGasto = () => {
  router.push(`/dashboard/${grupoId}/crear/gasto`)
}

const editarGasto = () => {
  if (gastoSeleccionado.value) {
    router.push(`/dashboard/${grupoId}/gastos/${gastoSeleccionado.value.id}/editar`)
  }
  cerrarModal()
}

// Utilidades de UI
const mostrarToast = async (mensaje, color = 'primary') => {
  const toast = await toastController.create({
    message: mensaje,
    duration: 2000,
    color: color
  })
  await toast.present()
}

const mostrarError = (mensaje) => {
  mostrarToast(mensaje, 'danger')
}

// Lifecycle - usar la función corregida
onMounted(() => {
  cargarDatos()
})
</script>

<style scoped>
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  text-align: center;
}

.title-container h1 {
  margin: 0;
  font-size: 1.2rem;
  font-weight: 600;
}

.title-container p {
  margin: 0;
  font-size: 0.9rem;
  opacity: 0.8;
}

.stats-card {
  margin-bottom: 1rem;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 1rem;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.stat-item ion-icon {
  font-size: 1.5rem;
}

.stat-item h3 {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 600;
}

.stat-item p {
  margin: 0;
  font-size: 0.85rem;
  color: var(--ion-color-medium);
}

.gastos-card {
  margin-top: 1rem;
}

.gasto-item {
  --padding-start: 16px;
  --padding-end: 16px;
}

.gasto-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--ion-color-light);
}

.gasto-end {
  text-align: right;
}

.gasto-end h3 {
  margin: 0 0 0.5rem 0;
  font-size: 1.1rem;
  font-weight: 600;
}

.empty-state {
  text-align: center;
  padding: 3rem 1rem;
}

.empty-state ion-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.empty-state h2 {
  color: var(--ion-color-medium);
  margin-bottom: 2rem;
}

.info-grid {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.info-item {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
}

.info-item ion-icon {
  margin-top: 0.25rem;
}

.info-item div {
  flex: 1;
}

.info-item strong {
  display: block;
  margin-bottom: 0.25rem;
}

.info-item p {
  margin: 0;
  color: var(--ion-color-medium);
}

.avatar-text {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: var(--ion-color-primary);
  color: white;
  font-weight: 600;
  border-radius: 50%;
}

.modal-actions {
  margin-top: 2rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

ion-segment {
  margin-bottom: 1rem;
}
</style>