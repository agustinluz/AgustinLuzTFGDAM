<template>
  <ion-page>
    <ion-header>
      <ion-toolbar color="primary">
        <ion-buttons slot="start">
          <ion-back-button :default-href="`/dashboard/${grupoId}/gastos`"></ion-back-button>
        </ion-buttons>
        <ion-title>Detalle Gasto</ion-title>
        <ion-buttons slot="end">
          <ion-button fill="clear">
            <ion-icon :icon="person" slot="icon-only"></ion-icon>
          </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding">
      <div v-if="gasto">
        <!-- Información del Gasto -->
        <ion-card class="gasto-info-card">
          <ion-card-header>
            <ion-card-title>{{ gasto.titulo || 'Gasto' }}</ion-card-title>
            <ion-card-subtitle v-if="gasto.descripcion">{{ gasto.descripcion }}</ion-card-subtitle>
          </ion-card-header>
          <ion-card-content>
            <div class="monto-total">
              <span class="monto-label">Total</span>
              <span class="monto-value">${{ formatMonto(gasto.monto) }}</span>
            </div>
            <div class="fecha-info">
              <ion-icon :icon="calendar" color="medium"></ion-icon>
              <span>{{ formatFechaCompleta(gasto.fecha) }}</span>
            </div>
          </ion-card-content>
        </ion-card>

        <!-- Realizador del Pago -->
        <ion-card class="pagador-card">
          <ion-card-header>
            <ion-card-title>Realizador del Pago</ion-card-title>
          </ion-card-header>
          <ion-card-content>
            <div class="pagador-info">
              <div class="participante-avatar">
                <img v-if="gasto.pagadoPor?.foto" :src="gasto.pagadoPor.foto" :alt="gasto.pagadoPor.nombre" />
                <div v-else class="avatar-placeholder">
                  {{ getInitials(gasto.pagadoPor?.nombre || 'U') }}
                </div>
              </div>
              <div class="participante-detalles">
                <p class="participante-nombre">{{ gasto.pagadoPor?.nombre || 'Desconocido' }}</p>
                <p class="participante-estado">Pago realizado</p>
              </div>
            </div>
          </ion-card-content>
        </ion-card>

        <!-- Participantes -->
        <ion-card class="participantes-card">
          <ion-card-header>
            <ion-card-title>Participantes</ion-card-title>
          </ion-card-header>
          <ion-card-content>
            <div class="participantes-list">
              <div 
                v-for="participante in participantesConDeudas" 
                :key="participante.id" 
                class="participante-item"
              >
                <div class="participante-info">
                  <div class="participante-avatar">
                    <img v-if="participante.foto" :src="participante.foto" :alt="participante.nombre" />
                    <div v-else class="avatar-placeholder">
                      {{ getInitials(participante.nombre) }}
                    </div>
                  </div>
                  <div class="participante-detalles">
                    <p class="participante-nombre">{{ participante.nombre }}</p>
                    <p class="participante-deuda">
                      Debe: ${{ formatMonto(participante.deuda) }}
                    </p>
                  </div>
                </div>
                <div class="participante-acciones">
                  <ion-chip 
                    v-if="participante.saldado" 
                    color="success" 
                    class="saldado-chip"
                  >
                    <ion-icon :icon="checkmark"></ion-icon>
                    <ion-label>Saldado</ion-label>
                  </ion-chip>
                  <ion-checkbox 
                    v-model="participante.saldado"
                    @ionChange="toggleSaldado(participante)"
                    class="saldado-checkbox"
                  ></ion-checkbox>
                </div>
              </div>
            </div>
          </ion-card-content>
        </ion-card>

        <!-- Estado del Gasto -->
        <div class="estado-gasto">
          <ion-button 
            expand="block" 
            :color="gastoCompletamenteSaldado ? 'success' : 'medium'"
            :disabled="!gastoCompletamenteSaldado"
            class="estado-button"
          >
            <ion-icon 
              :icon="gastoCompletamenteSaldado ? checkmarkCircle : timeOutline" 
              slot="start"
            ></ion-icon>
            {{ gastoCompletamenteSaldado ? 'Gasto Saldado' : 'Pendiente de Saldar' }}
          </ion-button>
        </div>
      </div>

      <!-- Loading state -->
      <div v-else class="loading-container">
        <ion-spinner name="crescent"></ion-spinner>
        <p>Cargando detalles...</p>
      </div>
    </ion-content>

    <!-- Bottom Navigation -->
    <ion-tab-bar slot="bottom" class="bottom-navigation">
      <ion-tab-button 
        @click="navigateTo('/dashboard')"
        :class="{ active: currentTab === 'gastos' }"
      >
        <ion-icon :icon="card"></ion-icon>
      </ion-tab-button>
      
      <ion-tab-button 
        @click="navigateTo('/calendar')"
        :class="{ active: currentTab === 'calendar' }"
      >
        <ion-icon :icon="calendar"></ion-icon>
      </ion-tab-button>
      
      <ion-tab-button 
        @click="navigateTo('/detalles')"
        :class="{ active: currentTab === 'detalles' }"
      >
        <ion-icon :icon="checkmarkCircle"></ion-icon>
        <div class="active-indicator"></div>
      </ion-tab-button>
      
      <ion-tab-button 
        @click="navigateTo('/photos')"
        :class="{ active: currentTab === 'photos' }"
      >
        <ion-icon :icon="image"></ion-icon>
      </ion-tab-button>
      
      <ion-tab-button 
        @click="navigateTo('/chat')"
        :class="{ active: currentTab === 'chat' }"
      >
        <ion-icon :icon="chatbubble"></ion-icon>
      </ion-tab-button>
    </ion-tab-bar>
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
  IonCardSubtitle,
  IonCardContent,
  IonIcon,
  IonButton,
  IonButtons,
  IonBackButton,
  IonCheckbox,
  IonChip,
  IonLabel,
  IonSpinner,
  IonTabBar,
  IonTabButton,
  useIonRouter
} from '@ionic/vue'
import { 
  person, 
  calendar,
  checkmark,
  checkmarkCircle,
  timeOutline,
  card,
  image,
  chatbubble
} from 'ionicons/icons'
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'

const router = useIonRouter()
const route = useRoute()
const gasto = ref(null)
const participantes = ref([])
const grupoId = route.params.grupoId || route.params.id
const gastoId = route.params.gastoId || route.params.id
const currentTab = ref('detalles')

// Computed para participantes con información de deudas
const participantesConDeudas = computed(() => {
  if (!gasto.value || !participantes.value.length) return []
  
  const montoPorPersona = gasto.value.monto / participantes.value.length
  
  return participantes.value.map(participante => ({
    ...participante,
    deuda: montoPorPersona,
    saldado: participante.saldado || false
  }))
})

// Computed para verificar si el gasto está completamente saldado
const gastoCompletamenteSaldado = computed(() => {
  return participantesConDeudas.value.every(p => p.saldado)
})

const fetchGasto = async () => {
  try {
    // Cargar detalles del gasto
    const resGasto = await fetch(`${import.meta.env.VITE_API_URL}/gastos/${gastoId}`)
    if (resGasto.ok) {
      gasto.value = await resGasto.json()
    }
    
    // Cargar participantes del grupo
    const resParticipantes = await fetch(`${import.meta.env.VITE_API_URL}/grupos/${grupoId}/participantes`)
    if (resParticipantes.ok) {
      participantes.value = await resParticipantes.json()
    }
  } catch (error) {
    console.error('Error cargando gasto:', error)
    // Datos de ejemplo para desarrollo
    gasto.value = {
      id: 1,
      titulo: 'Cena en restaurante',
      descripcion: 'Cena grupal en el restaurante italiano',
      monto: 120.50,
      fecha: new Date().toISOString(),
      pagadoPor: { id: 1, nombre: 'Juan Carlos', foto: null }
    }
    
    participantes.value = [
      { id: 1, nombre: 'Juan Carlos', foto: null, saldado: true },
      { id: 2, nombre: 'María González', foto: null, saldado: false },
      { id: 3, nombre: 'Pedro Martínez', foto: null, saldado: false }
    ]
  }
}

const getInitials = (nombre) => {
  if (!nombre) return 'U'
  return nombre.split(' ').map(n => n[0]).join('').toUpperCase()
}

const formatMonto = (monto) => {
  return Number(monto).toFixed(2)
}

const formatFechaCompleta = (fecha) => {
  return new Date(fecha).toLocaleDateString('es-ES', {
    day: '2-digit',
    month: 'long',
    year: 'numeric'
  })
}

const toggleSaldado = async (participante) => {
  try {
    // Aquí harías la llamada a la API para actualizar el estado
    const response = await fetch(`${import.meta.env.VITE_API_URL}/gastos/${gastoId}/participantes/${participante.id}/saldado`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ saldado: participante.saldado })
    })
    
    if (!response.ok) {
      // Revertir el cambio si falla
      participante.saldado = !participante.saldado
      console.error('Error actualizando estado de saldado')
    }
  } catch (error) {
    // Revertir el cambio si falla
    participante.saldado = !participante.saldado
    console.error('Error:', error)
  }
}

const navigateTo = (path) => {
  router.push(path)
}

onMounted(() => {
  fetchGasto()
})
</script>

<style scoped>
.gasto-info-card {
  margin-bottom: 16px;
}

.monto-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 12px;
}

.monto-label {
  font-size: 0.9rem;
  color: #6c757d;
  font-weight: 500;
}

.monto-value {
  font-size: 1.8rem;
  font-weight: bold;
  color: #2c3e50;
}

.fecha-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #6c757d;
  font-size: 0.9rem;
}

.pagador-card, .participantes-card {
  margin-bottom: 16px;
}

.pagador-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.participante-avatar {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  overflow: hidden;
  background-color: #e9ecef;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.participante-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  font-size: 18px;
  font-weight: bold;
  color: #6c757d;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.participante-detalles {
  flex: 1;
}

.participante-nombre {
  font-size: 1rem;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 4px 0;
}

.participante-estado {
  font-size: 0.85rem;
  color: #6c757d;
  margin: 0;
}

.participante-deuda {
  font-size: 0.85rem;
  color: #6c757d;
  margin: 0;
}

.participantes-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.participante-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f1f3f4;
}

.participante-item:last-child {
  border-bottom: none;
}

.participante-info {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
}

.participante-acciones {
  display: flex;
  align-items: center;
  gap: 12px;
}

.saldado-chip {
  font-size: 0.75rem;
}

.saldado-checkbox {
  --size: 24px;
}

.estado-gasto {
  margin: 24px 0;
}

.estado-button {
  --border-radius: 12px;
  --padding-top: 16px;
  --padding-bottom: 16px;
  font-weight: 600;
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
  color: #6c757d;
  margin: 0;
}

/* Bottom Navigation Styles */
.bottom-navigation {
  border-top: 1px solid #e0e0e0;
  background-color: white;
}

.bottom-navigation ion-tab-button {
  --color: #9e9e9e;
  --color-selected: #2c3e50;
  position: relative;
}

.bottom-navigation ion-tab-button.active {
  --color: #2c3e50;
}

.bottom-navigation ion-tab-button.active .active-indicator {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 32px;
  height: 3px;
  background-color: #2c3e50;
  border-radius: 2px 2px 0 0;
}

/* Responsive */
@media (max-width: 480px) {
  .monto-total {
    padding: 12px;
  }
  
  .monto-value {
    font-size: 1.5rem;
  }
  
  .participante-avatar {
    width: 45px;
    height: 45px;
  }
  
  .participante-info {
    gap: 12px;
  }
}
</style>