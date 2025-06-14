<template>
  <ion-page>
    <ion-header :translucent="true" class="ion-no-border">
      <ion-toolbar color="primary">
        <ion-buttons slot="start">
          <ion-back-button :default-href="`/dashboard/${grupoId}/gastos`" color="light"></ion-back-button>
        </ion-buttons>
        <ion-title>Editar Gasto</ion-title>
        <ion-buttons slot="end">
          <ion-button @click="guardarGasto" :disabled="!formularioValido || guardando" fill="clear">
            <ion-icon v-if="!guardando" :icon="checkmark" color="light"></ion-icon>
            <ion-spinner v-else name="crescent" color="light"></ion-spinner>
          </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>

    <ion-content :fullscreen="true" class="ion-padding">
      <!-- Loading inicial -->
      <div v-if="cargandoInicial" class="loading-container">
        <ion-spinner name="crescent" color="primary"></ion-spinner>
        <p>Cargando datos del gasto...</p>
      </div>

      <div v-else>
        <!-- Información básica -->
        <ion-card>
          <ion-card-header>
            <ion-card-title>Información del Gasto</ion-card-title>
          </ion-card-header>
          <ion-card-content>
            <ion-item class="form-item">
              <ion-label position="stacked">Título del gasto *</ion-label>
              <ion-input
                v-model="formulario.titulo"
                placeholder="Ej: Cena en restaurante"
                :class="{ 'ion-invalid': errores.titulo }"
              ></ion-input>
              <ion-note v-if="errores.titulo" slot="error">{{ errores.titulo }}</ion-note>
            </ion-item>

            <ion-item class="form-item">
              <ion-label position="stacked">Monto *</ion-label>
              <ion-input
                v-model="formulario.monto"
                type="number"
                step="0.01"
                placeholder="0.00"
                :class="{ 'ion-invalid': errores.monto }"
              >
                <div slot="end" class="currency-symbol">€</div>
              </ion-input>
              <ion-note v-if="errores.monto" slot="error">{{ errores.monto }}</ion-note>
            </ion-item>

            <ion-item class="form-item" button @click="abrirSelectorPagador">
              <ion-label position="stacked">Pagado por *</ion-label>
              <ion-input
                :value="pagadorSeleccionado?.nombre || 'Seleccionar persona'"
                readonly
                :class="{ 'ion-invalid': errores.pagadoPorId }"
              ></ion-input>
              <ion-icon :icon="chevronDown" slot="end" color="medium"></ion-icon>
              <ion-note v-if="errores.pagadoPorId" slot="error">{{ errores.pagadoPorId }}</ion-note>
            </ion-item>
          </ion-card-content>
        </ion-card>

        <!-- Participantes -->
        <ion-card>
          <ion-card-header>
            <ion-card-title>Participantes</ion-card-title>
            <ion-card-subtitle>Selecciona quién participa en este gasto</ion-card-subtitle>
          </ion-card-header>
          <ion-card-content>
            <div class="participantes-grid">
              <div 
                v-for="miembro in miembrosGrupo" 
                :key="miembro.id"
                class="participante-item"
                :class="{ 'selected': esParticipante(miembro.id) }"
                @click="toggleParticipante(miembro.id)"
              >
                <ion-avatar class="participante-avatar">
                  <div class="avatar-text">{{ getInitials(miembro.nombre) }}</div>
                </ion-avatar>
                <div class="participante-info">
                  <h4>{{ miembro.nombre }}</h4>
                </div>
                <ion-checkbox 
                  :checked="esParticipante(miembro.id)"
                  @ion-change="toggleParticipante(miembro.id)"
                ></ion-checkbox>
              </div>
            </div>
            <ion-note v-if="errores.participantesIds" color="danger" class="error-note">
              {{ errores.participantesIds }}
            </ion-note>
          </ion-card-content>
        </ion-card>

        <!-- División del gasto -->
        <ion-card>
          <ion-card-header>
            <ion-card-title>División del Gasto</ion-card-title>
          </ion-card-header>
          <ion-card-content>
            <ion-radio-group v-model="formulario.tipoReparto">
              <ion-item>
                <ion-radio slot="start" value="iguales"></ion-radio>
                <ion-label>
                  <h3>Partes iguales</h3>
                  <p>Dividir el monto equitativamente entre todos</p>
                </ion-label>
              </ion-item>
              <ion-item>
                <ion-radio slot="start" value="personalizadas"></ion-radio>
                <ion-label>
                  <h3>Cantidades personalizadas</h3>
                  <p>Asignar montos específicos a cada persona</p>
                </ion-label>
              </ion-item>
            </ion-radio-group>

            <!-- Cantidades personalizadas -->
            <div v-if="formulario.tipoReparto === 'personalizadas'" class="cantidades-personalizadas">
              <ion-item v-for="participante in participantesSeleccionados" :key="participante.id">
                <ion-avatar slot="start">
                  <div class="avatar-text">{{ getInitials(participante.nombre) }}</div>
                </ion-avatar>
                <ion-label>{{ participante.nombre }}</ion-label>
                <ion-input
                  v-model="formulario.cantidadesPersonalizadas[participante.id]"
                  type="number"
                  step="0.01"
                  placeholder="0.00"
                  slot="end"
                  style="max-width: 100px;"
                >
                  <div slot="end" class="currency-symbol">€</div>
                </ion-input>
              </ion-item>
              
              <div class="total-personalizado">
                <strong>Total asignado: {{ formatMonto(totalPersonalizado) }}</strong>
                <p v-if="totalPersonalizado !== parseFloat(formulario.monto)" class="error-text">
                  ⚠️ El total debe coincidir con el monto del gasto
                </p>
              </div>
            </div>
          </ion-card-content>
        </ion-card>

        <!-- Evento (opcional) -->
        <ion-card v-if="eventos && eventos.length > 0">
          <ion-card-header>
            <ion-card-title>Evento (Opcional)</ion-card-title>
          </ion-card-header>
          <ion-card-content>
            <ion-item button @click="abrirSelectorEvento">
              <ion-label>
                {{ eventoSeleccionado?.nombre || 'Sin evento asociado' }}
              </ion-label>
              <ion-icon :icon="chevronDown" slot="end" color="medium"></ion-icon>
            </ion-item>
          </ion-card-content>
        </ion-card>

        <!-- Resumen -->
        <ion-card class="resumen-card">
          <ion-card-header>
            <ion-card-title>Resumen</ion-card-title>
          </ion-card-header>
          <ion-card-content>
            <div class="resumen-item">
              <span>Total del gasto:</span>
              <strong>{{ formatMonto(formulario.monto) }}</strong>
            </div>
            <div class="resumen-item">
              <span>Participantes:</span>
              <strong>{{ participantesSeleccionados.length }}</strong>
            </div>
            <div class="resumen-item" v-if="formulario.tipoReparto === 'iguales'">
              <span>Por persona:</span>
              <strong>{{ formatMonto(montoPorPersona) }}</strong>
            </div>
          </ion-card-content>
        </ion-card>
      </div>
    </ion-content>

    <!-- Modal Selector de Pagador -->
    <ion-modal :is-open="modalPagadorAbierto" @did-dismiss="modalPagadorAbierto = false">
      <ion-header>
        <ion-toolbar>
          <ion-title>Seleccionar Pagador</ion-title>
          <ion-buttons slot="end">
            <ion-button @click="modalPagadorAbierto = false" fill="clear">
              <ion-icon :icon="close"></ion-icon>
            </ion-button>
          </ion-buttons>
        </ion-toolbar>
      </ion-header>
      <ion-content>
        <ion-list>
          <ion-item 
            v-for="miembro in miembrosGrupo" 
            :key="miembro.id"
            button
            @click="seleccionarPagador(miembro)"
            :class="{ 'selected-item': formulario.pagadoPorId === miembro.id }"
          >
            <ion-avatar slot="start">
              <div class="avatar-text">{{ getInitials(miembro.nombre) }}</div>
            </ion-avatar>
            <ion-label>
              <h3>{{ miembro.nombre }}</h3>
              <p>{{ miembro.email }}</p>
            </ion-label>
            <ion-icon 
              v-if="formulario.pagadoPorId === miembro.id" 
              :icon="checkmark" 
              color="primary" 
              slot="end"
            ></ion-icon>
          </ion-item>
        </ion-list>
      </ion-content>
    </ion-modal>

    <!-- Modal Selector de Evento -->
    <ion-modal :is-open="modalEventoAbierto" @did-dismiss="modalEventoAbierto = false">
      <ion-header>
        <ion-toolbar>
          <ion-title>Seleccionar Evento</ion-title>
          <ion-buttons slot="end">
            <ion-button @click="modalEventoAbierto = false" fill="clear">
              <ion-icon :icon="close"></ion-icon>
            </ion-button>
          </ion-buttons>
        </ion-toolbar>
      </ion-header>
      <ion-content>
        <ion-list>
          <ion-item 
            button
            @click="seleccionarEvento(null)"
            :class="{ 'selected-item': !formulario.eventoId }"
          >
            <ion-icon :icon="removeCircleOutline" slot="start" color="medium"></ion-icon>
            <ion-label>Sin evento asociado</ion-label>
            <ion-icon 
              v-if="!formulario.eventoId" 
              :icon="checkmark" 
              color="primary" 
              slot="end"
            ></ion-icon>
          </ion-item>
          <ion-item 
            v-for="evento in eventos" 
            :key="evento.id"
            button
            @click="seleccionarEvento(evento)"
            :class="{ 'selected-item': formulario.eventoId === evento.id }"
          >
            <ion-icon :icon="star" slot="start" color="warning"></ion-icon>
            <ion-label>
              <h3>{{ evento.nombre }}</h3>
              <p>{{ evento.descripcion }}</p>
            </ion-label>
            <ion-icon 
              v-if="formulario.eventoId === evento.id" 
              :icon="checkmark" 
              color="primary" 
              slot="end"
            ></ion-icon>
          </ion-item>
        </ion-list>
      </ion-content>
    </ion-modal>
  </ion-page>
</template>

<script setup>
import {
  IonPage, IonHeader, IonToolbar, IonTitle, IonContent, IonCard, IonCardHeader,
  IonCardTitle, IonCardContent, IonItem, IonLabel, IonInput, IonButton, IonButtons,
  IonBackButton, IonIcon, IonNote, IonCheckbox, IonRadioGroup, IonRadio,
  IonModal, IonList, IonAvatar, IonSpinner, IonCardSubtitle,
  useIonRouter, alertController, toastController
} from '@ionic/vue'

import { 
  checkmark, chevronDown, close, star, removeCircleOutline
} from 'ionicons/icons'

import { ref, onMounted, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import api from '@/service/api'

const router = useIonRouter()
const route = useRoute()
const grupoId = route.params.grupoId
const gastoId = route.params.gastoId

// Estados reactivos
const cargandoInicial = ref(true)
const guardando = ref(false)
const miembrosGrupo = ref([])
const eventos = ref([])
const modalPagadorAbierto = ref(false)
const modalEventoAbierto = ref(false)

// Formulario
const formulario = ref({
  titulo: '',
  monto: '',
  pagadoPorId: null,
  participantesIds: [],
  tipoReparto: 'iguales',
  cantidadesPersonalizadas: {},
  eventoId: null
})

const errores = ref({})

// API Base URL
const API_URL = import.meta.env.VITE_API_URL

// Computed properties
const pagadorSeleccionado = computed(() => {
  return miembrosGrupo.value.find(m => m.id === formulario.value.pagadoPorId)
})

const eventoSeleccionado = computed(() => {
  return eventos.value.find(e => e.id === formulario.value.eventoId)
})

const participantesSeleccionados = computed(() => {
  return miembrosGrupo.value.filter(m => 
    formulario.value.participantesIds.includes(m.id)
  )
})

const montoPorPersona = computed(() => {
  const monto = parseFloat(formulario.value.monto) || 0
  const participantes = formulario.value.participantesIds.length || 1
  return monto / participantes
})

const totalPersonalizado = computed(() => {
  return Object.values(formulario.value.cantidadesPersonalizadas)
    .reduce((sum, val) => sum + (parseFloat(val) || 0), 0)
})

const formularioValido = computed(() => {
  return formulario.value.titulo && 
         formulario.value.monto && 
         formulario.value.pagadoPorId && 
         formulario.value.participantesIds.length > 0 &&
         (formulario.value.tipoReparto === 'iguales' || 
          totalPersonalizado.value === parseFloat(formulario.value.monto))
})

const apiRequest = async (endpoint, options = {}) => {
  const url = `${API_URL}${endpoint}`
  const config = {
    headers: {
      'Content-Type': 'application/json',
      ...options.headers
    },
    ...options
  }

  const response = await api({ url, ...config })
  return response.data
}
// Métodos de carga de datos
const cargarDatosIniciales = async () => {
  cargandoInicial.value = true
  try {
    await Promise.all([
      cargarGasto(),
      cargarMiembrosGrupo(),
      cargarEventos()
    ])
  } catch (error) {
    console.error('Error al cargar datos:', error)
    mostrarError('Error al cargar los datos del gasto')
  } finally {
    cargandoInicial.value = false
  }
}

const cargarGasto = async () => {
  try {
    const gasto = await apiRequest(`/gasto/${gastoId}`)
    
    // Llenar el formulario con los datos del gasto
    formulario.value = {
      titulo: gasto.titulo,
      monto: gasto.monto.toString(),
      pagadoPorId: gasto.pagadoPor.id,
      participantesIds: gasto.usuarios.map(u => u.id),
      tipoReparto: gasto.partesIguales ? 'iguales' : 'personalizadas',
      cantidadesPersonalizadas: gasto.cantidadesPersonalizadas || {},
      eventoId: gasto.evento?.id || null
    }
  } catch (error) {
    console.error('Error al cargar gasto:', error)
    throw error
  }
}

const cargarMiembrosGrupo = async () => {
  try {
    // Intentar diferentes URLs para obtener miembros del grupo
    let miembros
    try {
      miembros = await apiRequest(`/grupos/${grupoId}/usuarios`)
    } catch (error1) {
      console.warn('Error al cargar miembros del grupo:', error1)
      
    }
    
    miembrosGrupo.value = miembros
  } catch (error) {
    console.error('Error al cargar miembros:', error)
    throw error
  }
}

const cargarEventos = async () => {
  try {
    let eventosData
    try {
      eventosData = await apiRequest(`/eventos/${grupoId}/eventos`)
    } catch (error1) {
      try {
        eventosData = await apiRequest(`/eventos/grupo/${grupoId}`)
      } catch (error2) {
        // Datos de ejemplo para desarrollo
        eventosData = [
          { id: 1, nombre: 'Alojamiento', descripcion: 'Gastos de hotel y hospedaje' },
          { id: 2, nombre: 'Comidas', descripcion: 'Restaurantes y comida' },
          { id: 3, nombre: 'Transporte', descripcion: 'Taxis, metro, etc.' }
        ]
      }
    }
    
    eventos.value = eventosData || []
  } catch (error) {
    console.error('Error al cargar eventos:', error)
    eventos.value = []
  }
}

// Métodos de validación
const validarFormulario = () => {
  errores.value = {}
  
  if (!formulario.value.titulo.trim()) {
    errores.value.titulo = 'El título es obligatorio'
  }
  
  if (!formulario.value.monto || parseFloat(formulario.value.monto) <= 0) {
    errores.value.monto = 'El monto debe ser mayor a 0'
  }
  
  if (!formulario.value.pagadoPorId) {
    errores.value.pagadoPorId = 'Selecciona quién pagó'
  }
  
  if (formulario.value.participantesIds.length === 0) {
    errores.value.participantesIds = 'Selecciona al menos un participante'
  }
  
  return Object.keys(errores.value).length === 0
}

// Métodos de interacción
const esParticipante = (usuarioId) => {
  return formulario.value.participantesIds.includes(usuarioId)
}

const toggleParticipante = (usuarioId) => {
  const index = formulario.value.participantesIds.indexOf(usuarioId)
  if (index > -1) {
    formulario.value.participantesIds.splice(index, 1)
    delete formulario.value.cantidadesPersonalizadas[usuarioId]
  } else {
    formulario.value.participantesIds.push(usuarioId)
    if (formulario.value.tipoReparto === 'personalizadas') {
      formulario.value.cantidadesPersonalizadas[usuarioId] = ''
    }
  }
}

const abrirSelectorPagador = () => {
  modalPagadorAbierto.value = true
}

const seleccionarPagador = (miembro) => {
  formulario.value.pagadoPorId = miembro.id
  modalPagadorAbierto.value = false
}

const abrirSelectorEvento = () => {
  modalEventoAbierto.value = true
}

const seleccionarEvento = (evento) => {
  formulario.value.eventoId = evento?.id || null
  modalEventoAbierto.value = false
}

const guardarGasto = async () => {
  if (!validarFormulario()) {
    mostrarError('Por favor corrige los errores en el formulario')
    return
  }
  
  guardando.value = true
  
  try {
    const gastoData = {
      titulo: formulario.value.titulo.trim(),
      monto: parseFloat(formulario.value.monto),
      pagadoPorId: formulario.value.pagadoPorId,
      participantesIds: formulario.value.participantesIds,
      partesIguales: formulario.value.tipoReparto === 'iguales',
      cantidadesPersonalizadas: formulario.value.tipoReparto === 'personalizadas' ? 
        formulario.value.cantidadesPersonalizadas : null,
      eventoId: formulario.value.eventoId
    }
    
    await apiRequest(`/gasto/${gastoId}`, {
      method: 'PUT',
      body: JSON.stringify(gastoData)
    })
    
    mostrarToast('Gasto actualizado correctamente', 'success')
    router.push(`/dashboard/${grupoId}/gastos`)
    
  } catch (error) {
    console.error('Error al actualizar gasto:', error)
    mostrarError('Error al actualizar el gasto')
  } finally {
    guardando.value = false
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

const getInitials = (nombre) => {
  if (!nombre) return 'NA'
  return nombre.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2)
}

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

// Watchers
watch(() => formulario.value.tipoReparto, (nuevoTipo) => {
  if (nuevoTipo === 'personalizadas') {
    // Inicializar cantidades personalizadas
    formulario.value.participantesIds.forEach(id => {
      if (!formulario.value.cantidadesPersonalizadas[id]) {
        formulario.value.cantidadesPersonalizadas[id] = ''
      }
    })
  }
})

// Lifecycle
onMounted(() => {
  cargarDatosIniciales()
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

.form-item {
  margin-bottom: 1rem;
}

.currency-symbol {
  color: var(--ion-color-medium);
  margin-left: 0.5rem;
}

.participantes-grid {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.participante-item {
  display: flex;
  align-items: center;
  padding: 0.75rem;
  border: 2px solid var(--ion-color-light);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.participante-item:hover {
  border-color: var(--ion-color-primary-tint);
}

.participante-item.selected {
  border-color: var(--ion-color-primary);
  background: var(--ion-color-primary-tint);
}

.participante-avatar {
  width: 40px;
  height: 40px;
  margin-right: 0.75rem;
}

.participante-info {
  flex: 1;
}

.participante-info h4 {
  margin: 0;
  font-size: 0.9rem;
  font-weight: 500;
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

.cantidades-personalizadas {
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid var(--ion-color-light);
}

.total-personalizado {
  margin-top: 1rem;
  padding: 0.75rem;
  background: var(--ion-color-light);
  border-radius: 8px;
  text-align: center;
}

.error-text {
  color: var(--ion-color-danger);
  margin: 0.25rem 0 0 0;
}

.error-note {
  display: block;
  margin-top: 0.5rem;
}

.resumen-card {
  margin-top: 1rem;
}

.resumen-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem 0;
  border-bottom: 1px solid var(--ion-color-light);
}

.resumen-item:last-child {
  border-bottom: none;
}

.selected-item {
  --background: var(--ion-color-primary-tint);
}

ion-item.ion-invalid {
  --border-color: var(--ion-color-danger);
}
</style>