<template>
  <ion-page>
    <ion-header :translucent="true" class="ion-no-border">
      <ion-toolbar color="primary">
        <ion-buttons slot="start">
          <ion-back-button default-href="/dashboard" color="light"></ion-back-button>
        </ion-buttons>
        <ion-title class="header-title">
          <div class="title-container">
            <h1>{{ grupo?.nombre || 'Gastos' }}</h1>
            <p v-if="totalGastos > 0" class="subtitle">{{ participantes.length }} participantes • {{ gastos.length }} gastos</p>
          </div>
        </ion-title>
        <ion-buttons slot="end">
          <ion-button fill="clear" @click="mostrarOpciones">
            <ion-icon :icon="ellipsisVertical" color="light"></ion-icon>
          </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>

    <ion-content :fullscreen="true" class="ion-padding">
      <!-- Loading State -->
      <div v-if="cargando" class="loading-container">
        <ion-spinner name="crescent" color="primary"></ion-spinner>
        <p>Cargando gastos...</p>
      </div>

      <div v-else>
        <!-- Stats Card -->
        <ion-card class="stats-card" v-if="totalGastos > 0">
          <ion-card-content>
            <div class="stats-grid">
              <div class="stat-item">
                <ion-icon :icon="walletOutline" color="primary"></ion-icon>
                <div class="stat-content">
                  <h3>{{ formatMonto(totalGastos) }}</h3>
                  <p>Total gastado</p>
                </div>
              </div>
              <div class="stat-item">
                <ion-icon :icon="peopleOutline" color="success"></ion-icon>
                <div class="stat-content">
                  <h3>{{ participantes.length }}</h3>
                  <p>Participantes</p>
                </div>
              </div>
              <div class="stat-item">
                <ion-icon :icon="receiptOutline" color="warning"></ion-icon>
                <div class="stat-content">
                  <h3>{{ gastos.length }}</h3>
                  <p>Gastos</p>
                </div>
              </div>
            </div>
          </ion-card-content>
        </ion-card>

        <!-- Balance Summary -->
        <ion-card class="balance-card" v-if="participantesConDeudas.length > 0">
          <ion-card-header>
            <div class="card-header">
              <ion-card-title>
                <ion-icon :icon="scaleOutline"></ion-icon>
                Balance General
              </ion-card-title>
              <ion-button 
                fill="clear" 
                size="small" 
                @click="verResumenCompleto"
                color="primary"
              >
                Ver todo
                <ion-icon :icon="chevronForward" slot="end"></ion-icon>
              </ion-button>
            </div>
          </ion-card-header>
          <ion-card-content>
            <div class="balance-list">
              <div 
                v-for="participante in participantesConDeudas.slice(0, 4)" 
                :key="participante.id" 
                class="balance-item"
                :class="{ 'debe': participante.saldo < 0, 'recibe': participante.saldo > 0, 'equilibrado': participante.saldo === 0 }"
              >
                <div class="participante-info">
                  <div class="avatar">
                    <img v-if="participante.foto" :src="participante.foto" :alt="participante.nombre" />
                    <div v-else class="avatar-placeholder" :style="{ backgroundColor: getColorForUser(participante.id) }">
                      {{ getInitials(participante.nombre) }}
                    </div>
                  </div>
                  <div class="info">
                    <h4>{{ participante.nombre }}</h4>
                    <p v-if="participante.saldo < 0" class="status debe">Debe {{ formatMonto(Math.abs(participante.saldo)) }}</p>
                    <p v-else-if="participante.saldo > 0" class="status recibe">Recibe {{ formatMonto(participante.saldo) }}</p>
                    <p v-else class="status equilibrado">Sin deudas</p>
                  </div>
                </div>
                <div class="balance-indicator">
                  <ion-icon 
                    :icon="participante.saldo < 0 ? arrowDown : participante.saldo > 0 ? arrowUp : checkmark"
                    :color="participante.saldo < 0 ? 'danger' : participante.saldo > 0 ? 'success' : 'medium'"
                  ></ion-icon>
                </div>
              </div>
            </div>
          </ion-card-content>
        </ion-card>

        <!-- Filters -->
        <div class="filters-container" v-if="gastos.length > 0">
          <ion-segment v-model="filtroActivo" @ionChange="aplicarFiltro">
            <ion-segment-button value="todos">
              <ion-label>Todos</ion-label>
            </ion-segment-button>
            <ion-segment-button value="recientes">
              <ion-label>Recientes</ion-label>
            </ion-segment-button>
            <ion-segment-button value="mayores">
              <ion-label>Mayores</ion-label>
            </ion-segment-button>
          </ion-segment>
        </div>

        <!-- Gastos List -->
        <ion-card class="gastos-card" v-if="gastosFiltrados.length > 0">
          <ion-card-header>
            <div class="card-header">
              <ion-card-title>
                <ion-icon :icon="listOutline"></ion-icon>
                Gastos {{ filtroActivo === 'todos' ? '' : filtroActivo }}
              </ion-card-title>
              <ion-chip color="primary" outline>
                {{ gastosFiltrados.length }}
              </ion-chip>
            </div>
          </ion-card-header>
          <ion-card-content class="ion-no-padding">
            <div class="gastos-list">
              <div 
                v-for="(gasto, index) in gastosFiltrados" 
                :key="gasto.id" 
                class="gasto-item"
                @click="verDetalles(gasto)"
              >
                <div class="gasto-content">
                  <div class="gasto-left">
                    <div class="gasto-icon">
                      <ion-icon :icon="getIconForGasto(gasto)" :color="getColorForGasto(gasto)"></ion-icon>
                    </div>
                    <div class="gasto-info">
                      <h3>{{ gasto.titulo || 'Gasto sin título' }}</h3>
                      <div class="gasto-meta">
                        <span class="pagador">{{ gasto.pagadoPor?.nombre || 'Desconocido' }}</span>
                        <span class="separator">•</span>
                        <span class="fecha">{{ formatFechaRelativa(gasto.fecha) }}</span>
                        <span v-if="gasto.evento" class="separator">•</span>
                        <ion-chip v-if="gasto.evento" size="small" color="tertiary" outline>
                          {{ gasto.evento.nombre }}
                        </ion-chip>
                      </div>
                      <div class="participantes-preview" v-if="gasto.usuarios && gasto.usuarios.length > 0">
                        <span class="participantes-count">{{ gasto.usuarios.length }} participante{{ gasto.usuarios.length > 1 ? 's' : '' }}</span>
                      </div>
                    </div>
                  </div>
                  <div class="gasto-right">
                    <div class="gasto-monto">
                      <h2>{{ formatMonto(gasto.monto) }}</h2>
                      <p class="por-persona">{{ formatMonto(gasto.monto / (gasto.usuarios?.length || 1)) }}/persona</p>
                    </div>
                    <ion-button 
                      fill="clear" 
                      size="small"
                      color="primary"
                      @click.stop="verDetalles(gasto)"
                    >
                      <ion-icon :icon="chevronForward" slot="icon-only"></ion-icon>
                    </ion-button>
                  </div>
                </div>
                <div v-if="index < gastosFiltrados.length - 1" class="divider"></div>
              </div>
            </div>
          </ion-card-content>
        </ion-card>

        <!-- Empty State -->
        <div v-else-if="!cargando" class="empty-state">
          <ion-icon :icon="receiptOutline" color="medium"></ion-icon>
          <h2>No hay gastos registrados</h2>
          <p>Añade tu primer gasto para comenzar a dividir los costos con tu grupo</p>
          <ion-button expand="block" @click="añadirGasto" class="empty-action-btn">
            <ion-icon :icon="add" slot="start"></ion-icon>
            Añadir primer gasto
          </ion-button>
        </div>
      </div>

      <!-- Gasto Details Modal -->
      <ion-modal :is-open="modalAbierto" @did-dismiss="cerrarModal" class="gasto-modal">
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
          <div class="modal-content">
            <!-- Gasto Header -->
            <ion-card class="gasto-detail-card">
              <ion-card-content>
                <div class="gasto-detail-header">
                  <div class="detail-icon">
                    <ion-icon :icon="getIconForGasto(gastoSeleccionado)" :color="getColorForGasto(gastoSeleccionado)"></ion-icon>
                  </div>
                  <div class="detail-info">
                    <h1>{{ gastoSeleccionado.titulo || 'Gasto' }}</h1>
                    <h2 class="amount">{{ formatMonto(gastoSeleccionado.monto) }}</h2>
                  </div>
                </div>
              </ion-card-content>
            </ion-card>

            <!-- Gasto Info -->
            <ion-card>
              <ion-card-header>
                <ion-card-title>Información</ion-card-title>
              </ion-card-header>
              <ion-card-content>
                <div class="info-grid">
                  <div class="info-item">
                    <ion-icon :icon="person" color="primary"></ion-icon>
                    <div>
                      <h4>Pagado por</h4>
                      <p>{{ gastoSeleccionado.pagadoPor?.nombre || 'Desconocido' }}</p>
                    </div>
                  </div>
                  <div class="info-item">
                    <ion-icon :icon="calendar" color="primary"></ion-icon>
                    <div>
                      <h4>Fecha</h4>
                      <p>{{ formatFechaCompleta(gastoSeleccionado.fecha) }}</p>
                    </div>
                  </div>
                  <div class="info-item" v-if="gastoSeleccionado.evento">
                    <ion-icon :icon="star" color="primary"></ion-icon>
                    <div>
                      <h4>Evento</h4>
                      <p>{{ gastoSeleccionado.evento.nombre }}</p>
                    </div>
                  </div>
                  <div class="info-item">
                    <ion-icon :icon="calculator" color="primary"></ion-icon>
                    <div>
                      <h4>Por persona</h4>
                      <p>{{ formatMonto(gastoSeleccionado.monto / (gastoSeleccionado.usuarios?.length || 1)) }}</p>
                    </div>
                  </div>
                </div>
              </ion-card-content>
            </ion-card>

            <!-- Participantes -->
            <ion-card v-if="gastoSeleccionado.usuarios && gastoSeleccionado.usuarios.length > 0">
              <ion-card-header>
                <ion-card-title>Participantes ({{ gastoSeleccionado.usuarios.length }})</ion-card-title>
              </ion-card-header>
              <ion-card-content>
                <div class="participantes-detail">
                  <div 
                    v-for="usuario in gastoSeleccionado.usuarios" 
                    :key="usuario.id" 
                    class="participante-detail-item"
                    :class="{ 'pagador': usuario.id === gastoSeleccionado.pagadoPor?.id }"
                  >
                    <div class="participante-avatar">
                      <div class="avatar-placeholder" :style="{ backgroundColor: getColorForUser(usuario.id) }">
                        {{ getInitials(usuario.nombre) }}
                      </div>
                    </div>
                    <div class="participante-detail-info">
                      <h4>{{ usuario.nombre }}</h4>
                      <p v-if="usuario.id === gastoSeleccionado.pagadoPor?.id" class="pagador-label">
                        Pagador
                      </p>
                      <p v-else class="debe-label">
                        Debe {{ formatMonto(gastoSeleccionado.monto / gastoSeleccionado.usuarios.length) }}
                      </p>
                    </div>
                    <div class="participante-status">
                      <ion-icon 
                        :icon="usuario.id === gastoSeleccionado.pagadoPor?.id ? checkmarkCircle : time"
                        :color="usuario.id === gastoSeleccionado.pagadoPor?.id ? 'success' : 'warning'"
                      ></ion-icon>
                    </div>
                  </div>
                </div>
              </ion-card-content>
            </ion-card>

            <!-- Actions -->
            <div class="modal-actions">
              <ion-button expand="block" fill="outline" @click="editarGasto">
                <ion-icon :icon="pencil" slot="start"></ion-icon>
                Editar Gasto
              </ion-button>
              <ion-button expand="block" color="danger" fill="clear" @click="confirmarEliminar">
                <ion-icon :icon="trash" slot="start"></ion-icon>
                Eliminar Gasto
              </ion-button>
            </div>
          </div>
        </ion-content>
      </ion-modal>

      <!-- Action Sheet para opciones -->
      <ion-action-sheet
        :is-open="mostrandoOpciones"
        header="Opciones del grupo"
        :buttons="opcionesActionSheet"
        @did-dismiss="mostrandoOpciones = false"
      ></ion-action-sheet>
    </ion-content>

    <!-- FAB -->
    <ion-fab slot="fixed" vertical="bottom" horizontal="end" class="fab-container">
      <ion-fab-button color="primary" @click="añadirGasto" class="main-fab">
        <ion-icon :icon="add"></ion-icon>
      </ion-fab-button>
    </ion-fab>
  </ion-page>
</template>

<script setup>
import {
  IonPage, IonHeader, IonToolbar, IonTitle, IonContent, IonCard, IonCardHeader,
  IonCardTitle, IonCardContent, IonText, IonIcon, IonButton, IonButtons,
  IonBackButton, IonFab, IonFabButton, IonModal, IonList, IonItem, IonLabel,
  IonSegment, IonSegmentButton, IonChip, IonSpinner, IonActionSheet,
  useIonRouter
} from '@ionic/vue'

import { 
  person, chevronForward, add, close, ellipsisVertical, walletOutline,
  peopleOutline, receiptOutline, scaleOutline, listOutline, arrowDown,
  arrowUp, checkmark, calendar, star, calculator, checkmarkCircle, time,
  pencil, trash
} from 'ionicons/icons'

import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'

const router = useIonRouter()
const route = useRoute()
const gastos = ref([])
const grupo = ref(null)
const participantes = ref([])
const modalAbierto = ref(false)
const gastoSeleccionado = ref(null)
const cargando = ref(true)
const filtroActivo = ref('todos')
const mostrandoOpciones = ref(false)
const grupoId = route.params.id

// Computed properties
const totalGastos = computed(() => {
  return gastos.value.reduce((total, gasto) => total + parseFloat(gasto.monto || 0), 0)
})

const participantesConDeudas = computed(() => {
  const saldos = {}
  
  participantes.value.forEach(p => {
    saldos[p.id] = { ...p, saldo: 0 }
  })
  
  gastos.value.forEach(gasto => {
    const pagadorId = gasto.pagadoPor?.id
    const participantesGasto = gasto.usuarios || participantes.value
    const montoPorPersona = parseFloat(gasto.monto) / participantesGasto.length
    
    if (pagadorId && saldos[pagadorId]) {
      saldos[pagadorId].saldo += parseFloat(gasto.monto) - montoPorPersona
    }
    
    participantesGasto.forEach(p => {
      if (p.id !== pagadorId && saldos[p.id]) {
        saldos[p.id].saldo -= montoPorPersona
      }
    })
  })
  
  return Object.values(saldos).sort((a, b) => Math.abs(b.saldo) - Math.abs(a.saldo))
})

const gastosFiltrados = computed(() => {
  let filtrados = [...gastos.value]
  
  switch(filtroActivo.value) {
    case 'recientes':
      filtrados.sort((a, b) => new Date(b.fecha) - new Date(a.fecha))
      filtrados = filtrados.slice(0, 10)
      break
    case 'mayores':
      filtrados.sort((a, b) => parseFloat(b.monto) - parseFloat(a.monto))
      break
  }
  
  return filtrados
})

const opcionesActionSheet = computed(() => [
  
  {
    text: 'Configuración',
    icon: 'settings-outline',
    handler: () => irConfiguracion()
  },
  {
    text: 'Cancelar',
    role: 'cancel'
  }
])

// Methods
const fetchDatos = async () => {
  cargando.value = true
  try {
    const [resGrupo, resGastos, resParticipantes] = await Promise.all([
      fetch(`${import.meta.env.VITE_API_URL}/grupos/${grupoId}`),
      fetch(`${import.meta.env.VITE_API_URL}/gasto/${grupoId}/gastos`),
      fetch(`${import.meta.env.VITE_API_URL}/grupos/${grupoId}/participantes`)
    ])
    
    if (resGrupo.ok) grupo.value = await resGrupo.json()
    if (resGastos.ok) gastos.value = await resGastos.json()
    if (resParticipantes.ok) participantes.value = await resParticipantes.json()
    
  } catch (error) {
    console.error('Error cargando datos:', error)
    // Datos de ejemplo para desarrollo
    grupo.value = { nombre: 'Viaje a Barcelona' }
    participantes.value = [
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
        usuarios: participantes.value,
        evento: { id: 1, nombre: 'Alojamiento' }
      },
      {
        id: 2,
        titulo: 'Cena restaurante',
        monto: 85.50,
        fecha: new Date(Date.now() - 86400000).toISOString(),
        pagadoPor: { id: 2, nombre: 'Antonio López' },
        usuarios: participantes.value.slice(0, 3)
      },
      {
        id: 3,
        titulo: 'Taxi aeropuerto',
        monto: 35.20,
        fecha: new Date(Date.now() - 172800000).toISOString(),
        pagadoPor: { id: 3, nombre: 'Gertrudis Martín' },
        usuarios: participantes.value
      }
    ]
  } finally {
    cargando.value = false
  }
}

const formatMonto = (monto) => {
  const num = parseFloat(monto) || 0
  return new Intl.NumberFormat('es-ES', {
    style: 'currency',
    currency: 'EUR'
  }).format(num)
}

const formatFechaRelativa = (fecha) => {
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
  return new Date(fecha).toLocaleDateString('es-ES', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

const getInitials = (nombre) => {
  return nombre.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2)
}

const getColorForUser = (userId) => {
  const colors = ['#3880ff', '#10dc60', '#ffce00', '#f04141', '#7044ff', '#10d4aa']
  return colors[userId % colors.length]
}

const getIconForGasto = (gasto) => {
  const titulo = gasto.titulo?.toLowerCase() || ''
  if (titulo.includes('hotel') || titulo.includes('alojamiento')) return 'bed-outline'
  if (titulo.includes('cena') || titulo.includes('comida') || titulo.includes('restaurante')) return 'restaurant-outline'
  if (titulo.includes('taxi') || titulo.includes('transporte') || titulo.includes('gasolina')) return 'car-outline'
  if (titulo.includes('compra') || titulo.includes('super')) return 'bag-outline'
  return receiptOutline
}

const getColorForGasto = (gasto) => {
  const monto = parseFloat(gasto.monto)
  if (monto > 100) return 'danger'
  if (monto > 50) return 'warning'
  return 'primary'
}

const verDetalles = (gasto) => {
  gastoSeleccionado.value = gasto
  modalAbierto.value = true
}

const cerrarModal = () => {
  modalAbierto.value = false
  gastoSeleccionado.value = null
}

const añadirGasto = () => {
  router.push(`/dashboard/${grupoId}/crear/gasto`)
}

const editarGasto = () => {
  if (gastoSeleccionado.value) {
    router.push(`/dashboard/${grupoId}/gastos/${gastoSeleccionado.value.id}/editar`)
  }
}

const confirmarEliminar = () => {
  // Implementar confirmación de eliminación
  console.log('Eliminar gasto:', gastoSeleccionado.value?.id)
}

const aplicarFiltro = (event) => {
  filtroActivo.value = event.detail.value
}

const mostrarOpciones = () => {
  mostrandoOpciones.value = true
}

const verResumenCompleto = () => {
  router.push(`/dashboard/${grupoId}/resumen`)
}

const irConfiguracion = () => {
  router.push(`/dashboard/${grupoId}/configuracion`)
}

onMounted(() => {
  fetchDatos()
})
</script>

<style scoped>
/* Header Styles */
.header-title {
  text-align: left;
}

.title-container h1 {
  margin: 0;
  font-size: 1.2rem;
  font-weight: 600;
}

.title-container .subtitle {
  margin: 0;
  font-size: 0.8rem;
  opacity: 0.8;
  font-weight: 400;
}

/* Loading */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  gap: 1rem;
}

/* Stats Card */
.stats-card {
  margin-bottom: 1rem;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.stat-item ion-icon {
  font-size: 1.5rem;
}

.stat-content h3 {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--ion-text-color);
}

.stat-content p {
  margin: 0;
  font-size: 0.8rem;
  color: var(--ion-color-medium);
}

/* Balance Card */
.balance-card {
  margin-bottom: 1rem;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: between;
  align-items: center;
}

.card-header ion-card-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1rem;
  font-weight: 600;
}

.balance-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.balance-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.balance-item.debe {
  background-color: rgba(244, 67, 54, 0.1);
}

.balance-item.recibe {
  background-color: rgba(76, 175, 80, 0.1);
}

.balance-item.equilibrado {
  background-color: rgba(158, 158, 158, 0.1);
}

.participante-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 0.9rem;
}

.info h4 {
  margin: 0;
  font-size: 0.9rem;
  font-weight: 600;
}
</style>