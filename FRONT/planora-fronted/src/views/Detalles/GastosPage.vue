<template>
  <ion-page>
    <ion-header>
      <ion-toolbar color="primary">
        <ion-buttons slot="start">
          <ion-back-button default-href="/dashboard"></ion-back-button>
        </ion-buttons>
        <ion-title>{{ grupo?.nombre || 'Gastos' }}</ion-title>
        <ion-buttons slot="end">
          <ion-button fill="clear">
            <ion-icon :icon="person" slot="icon-only"></ion-icon>
          </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding">
      <!-- Resumen de deudas totales -->
      <ion-card class="deudas-card">
        <ion-card-header>
          <div class="deudas-header">
            <ion-card-title>Deudas Totales</ion-card-title>
            <ion-icon :icon="chevronForward" color="medium"></ion-icon>
          </div>
        </ion-card-header>
        <ion-card-content>
          <div class="participantes-grid">
            <div v-for="participante in participantesConDeudas" :key="participante.id" class="participante-item">
              <div class="participante-avatar">
                <img v-if="participante.foto" :src="participante.foto" :alt="participante.nombre" />
                <div v-else class="avatar-placeholder">
                  {{ participante.nombre.charAt(0).toUpperCase() }}
                </div>
              </div>
              <div class="participante-info">
                <p class="participante-nombre">{{ participante.nombre }}</p>
                <p class="participante-monto" :class="{ 'deuda': participante.saldo < 0, 'credito': participante.saldo > 0 }">
                  {{ formatMonto(participante.saldo) }}
                </p>
              </div>
            </div>
          </div>
        </ion-card-content>
      </ion-card>

      <!-- Lista de gastos -->
      <ion-card class="gastos-card">
        <ion-card-header>
          <div class="gastos-header">
            <ion-card-title>Gastos</ion-card-title>
            <ion-icon :icon="chevronForward" color="medium"></ion-icon>
          </div>
        </ion-card-header>
        <ion-card-content>
          <div v-if="gastos.length" class="gastos-list">
            <div v-for="gasto in gastos" :key="gasto.id" class="gasto-item" @click="verDetalles(gasto)">
              <div class="gasto-info">
                <div class="gasto-header-info">
                  <span class="pagador">{{ gasto.pagadoPor?.nombre || 'Desconocido' }}</span>
                  <span class="cantidad">{{ formatMonto(gasto.monto) }}</span>
                </div>
                <div class="gasto-detalles">
                  <span class="fecha">{{ formatFecha(gasto.fecha) }}</span>
                </div>
              </div>
              <ion-button 
                fill="solid" 
                size="small" 
                color="primary"
                class="detalles-btn"
                @click.stop="verDetalles(gasto)"
              >
                DETALLES
              </ion-button>
            </div>
          </div>
          <div v-else class="no-gastos">
            <ion-text color="medium">No hay gastos registrados</ion-text>
          </div>
        </ion-card-content>
      </ion-card>

      <!-- Modal para detalles del gasto -->
      <ion-modal :is-open="modalAbierto" @did-dismiss="cerrarModal">
        <ion-header>
          <ion-toolbar>
            <ion-title>Detalles del Gasto</ion-title>
            <ion-buttons slot="end">
              <ion-button @click="cerrarModal">
                <ion-icon :icon="close"></ion-icon>
              </ion-button>
            </ion-buttons>
          </ion-toolbar>
        </ion-header>
        <ion-content class="ion-padding" v-if="gastoSeleccionado">
          <ion-card>
            <ion-card-header>
              <ion-card-title>{{ gastoSeleccionado.titulo || 'Gasto' }}</ion-card-title>
            </ion-card-header>
            <ion-card-content>
              <ion-list>
                <ion-item>
                  <ion-label>
                    <h3>Monto</h3>
                    <p>{{ formatMonto(gastoSeleccionado.monto) }}</p>
                  </ion-label>
                </ion-item>
                <ion-item>
                  <ion-label>
                    <h3>Pagado por</h3>
                    <p>{{ gastoSeleccionado.pagadoPor?.nombre || 'Desconocido' }}</p>
                  </ion-label>
                </ion-item>
                <ion-item>
                  <ion-label>
                    <h3>Fecha</h3>
                    <p>{{ formatFechaCompleta(gastoSeleccionado.fecha) }}</p>
                  </ion-label>
                </ion-item>
                <ion-item v-if="gastoSeleccionado.descripcion">
                  <ion-label>
                    <h3>Descripción</h3>
                    <p>{{ gastoSeleccionado.descripcion }}</p>
                  </ion-label>
                </ion-item>
              </ion-list>
            </ion-card-content>
          </ion-card>
        </ion-content>
      </ion-modal>
    </ion-content>

    <!-- FAB para añadir gasto -->
    <ion-fab slot="fixed" vertical="bottom" horizontal="end">
      <ion-fab-button @click="añadirGasto" color="primary">
        <ion-icon :icon="add"></ion-icon>
      </ion-fab-button>
    </ion-fab>
  </ion-page>
</template>

<script setup>
import {
  IonPage,
  IonHeader,
  IonToolbar,
  IonTitle,
  IonContent,
  IonCard,
  IonCardHeader,
  IonCardTitle,
  IonCardContent,
  IonText,
  IonIcon,
  IonButton,
  IonButtons,
  IonBackButton,
  IonFab,
  IonFabButton,
  IonModal,
  IonList,
  IonItem,
  IonLabel,
  useIonRouter
} from '@ionic/vue'
import { 
  person, 
  chevronForward, 
  add, 
  close
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
const grupoId = route.params.id

// Computed para calcular saldos de participantes
const participantesConDeudas = computed(() => {
  const saldos = {}
  
  // Inicializar saldos
  participantes.value.forEach(p => {
    saldos[p.id] = { ...p, saldo: 0 }
  })
  
  // Calcular saldos basado en gastos
  gastos.value.forEach(gasto => {
    const pagadorId = gasto.pagadoPor?.id
    const montoPorPersona = gasto.monto / participantes.value.length
    
    // El pagador debe recibir dinero
    if (pagadorId && saldos[pagadorId]) {
      saldos[pagadorId].saldo += gasto.monto - montoPorPersona
    }
    
    // Todos los demás deben dinero
    participantes.value.forEach(p => {
      if (p.id !== pagadorId && saldos[p.id]) {
        saldos[p.id].saldo -= montoPorPersona
      }
    })
  })
  
  return Object.values(saldos).slice(0, 3) // Mostrar solo los primeros 3
})

const fetchDatos = async () => {
  try {
    // Cargar grupo
    const resGrupo = await fetch(`${import.meta.env.VITE_API_URL}/grupos/${grupoId}`)
    if (resGrupo.ok) {
      grupo.value = await resGrupo.json()
    }
    
    // Cargar gastos
    const resGastos = await fetch(`${import.meta.env.VITE_API_URL}/gasto/${grupoId}/gastos`)
    if (resGastos.ok) {
      gastos.value = await resGastos.json()
    }
    
    // Cargar participantes
    const resParticipantes = await fetch(`${import.meta.env.VITE_API_URL}/grupos/${grupoId}/participantes`)
    if (resParticipantes.ok) {
      participantes.value = await resParticipantes.json()
    }
  } catch (error) {
    console.error('Error cargando datos:', error)
    // Datos de ejemplo para mostrar el diseño
    grupo.value = { nombre: 'Mi Grupo' }
    participantes.value = [
      { id: 1, nombre: 'Carmen', foto: null },
      { id: 2, nombre: 'Antonio', foto: null },
      { id: 3, nombre: 'Gertr', foto: null }
    ]
    gastos.value = [
      {
        id: 1,
        titulo: 'Cena restaurante',
        monto: 85.50,
        fecha: new Date().toISOString(),
        pagadoPor: { id: 1, nombre: 'Carmen' },
        descripcion: 'Cena en el restaurante italiano'
      },
      {
        id: 2,
        titulo: 'Gasolina',
        monto: 45.20,
        fecha: new Date(Date.now() - 86400000).toISOString(),
        pagadoPor: { id: 2, nombre: 'Antonio' },
        descripcion: 'Combustible para el viaje'
      }
    ]
  }
}

const formatMonto = (monto) => {
  const formatted = Math.abs(monto).toFixed(2)
  return `${monto < 0 ? '-' : ''}$${formatted}`
}

const formatFecha = (fecha) => {
  return new Date(fecha).toLocaleDateString('es-ES', {
    day: '2-digit',
    month: '2-digit'
  })
}

const formatFechaCompleta = (fecha) => {
  return new Date(fecha).toLocaleDateString('es-ES', {
    day: '2-digit',
    month: 'long',
    year: 'numeric'
  })
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
  router.push(`/dashboard/${grupoId}/gastos/nuevo`)
}

onMounted(() => {
  fetchDatos()
})
</script>