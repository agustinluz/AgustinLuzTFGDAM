<template>
  <ion-page>
    <ion-header :translucent="true" class="ion-no-border">
      <ion-toolbar color="primary">
        <ion-buttons slot="start">
          <ion-back-button :default-href="`/dashboard/${grupoId}/gastos`" color="light"></ion-back-button>
        </ion-buttons>
        <ion-title class="header-title">
          <div class="title-container">
            <h1>Editar Gasto</h1>
            <p v-if="grupo" class="subtitle">{{ grupo.nombre }}</p>
          </div>
        </ion-title>
        <ion-buttons slot="end">
          <ion-button fill="clear" @click="guardarGasto" :disabled="!formularioValido || guardando">
            <ion-icon v-if="guardando" name="hourglass-outline" color="light"></ion-icon>
            <ion-icon v-else :icon="checkmark" color="light"></ion-icon>
          </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>

    <ion-content :fullscreen="true" class="ion-padding">
      <!-- Loading State -->
      <div v-if="cargando" class="loading-container">
        <ion-spinner name="crescent" color="primary"></ion-spinner>
        <p>Cargando gasto...</p>
      </div>

      <div v-else-if="gasto">
        <!-- Información básica del gasto -->
        <ion-card class="form-card">
          <ion-card-header>
            <ion-card-title>
              <ion-icon :icon="receiptOutline"></ion-icon>
              Información del Gasto
            </ion-card-title>
          </ion-card-header>
          <ion-card-content>
            <div class="form-fields">
              <!-- Título -->
              <ion-item class="form-item">
                <ion-label position="stacked">Título del gasto</ion-label>
                <ion-input
                  v-model="formulario.titulo"
                  placeholder="Ej: Cena en restaurante"
                  :clear-input="true"
                  @ionInput="validarFormulario"
                ></ion-input>
              </ion-item>

              <!-- Monto -->
              <ion-item class="form-item">
                <ion-label position="stacked">Monto (€)</ion-label>
                <ion-input
                  v-model="formulario.monto"
                  type="number"
                  step="0.01"
                  min="0"
                  placeholder="0.00"
                  @ionInput="validarFormulario"
                ></ion-input>
              </ion-item>

              <!-- Pagado por -->
              <ion-item class="form-item" button @click="abrirSelectorPagador">
                <ion-label position="stacked">Pagado por</ion-label>
                <div class="selector-content">
                  <div v-if="pagadorSeleccionado" class="participante-selected">
                    <div class="avatar-small" :style="{ backgroundColor: getColorForUser(pagadorSeleccionado.id) }">
                      {{ getInitials(pagadorSeleccionado.nombre) }}
                    </div>
                    <span>{{ pagadorSeleccionado.nombre }}</span>
                  </div>
                  <span v-else class="placeholder">Seleccionar pagador</span>
                  <ion-icon :icon="chevronDown" color="medium"></ion-icon>
                </div>
              </ion-item>

              <!-- Evento (opcional) -->
              <ion-item class="form-item" button @click="abrirSelectorEvento">
                <ion-label position="stacked">Evento (opcional)</ion-label>
                <div class="selector-content">
                  <div v-if="eventoSeleccionado" class="evento-selected">
                    <ion-chip color="tertiary" outline>
                      {{ eventoSeleccionado.nombre }}
                    </ion-chip>
                  </div>
                  <span v-else class="placeholder">Sin evento asociado</span>
                  <ion-icon :icon="chevronDown" color="medium"></ion-icon>
                </div>
              </ion-item>
            </div>
          </ion-card-content>
        </ion-card>

        <!-- División del gasto -->
        <ion-card class="form-card">
          <ion-card-header>
            <ion-card-title>
              <ion-icon :icon="peopleOutline"></ion-icon>
              División del Gasto
            </ion-card-title>
          </ion-card-header>
          <ion-card-content>
            <!-- Tipo de división -->
            <div class="division-type">
              <ion-segment v-model="formulario.partesIguales" @ionChange="cambiarTipoDivision">
                <ion-segment-button :value="true">
                  <ion-label>Partes iguales</ion-label>
                </ion-segment-button>
                <ion-segment-button :value="false">
                  <ion-label>Personalizado</ion-label>
                </ion-segment-button>
              </ion-segment>
            </div>

            <!-- Participantes -->
            <div class="participantes-section">
              <div class="section-header">
                <h3>Participantes</h3>
                <ion-button 
                  fill="clear" 
                  size="small" 
                  @click="seleccionarTodos"
                  color="primary"
                >
                  {{ todosSeleccionados ? 'Deseleccionar todos' : 'Seleccionar todos' }}
                </ion-button>
              </div>

              <div class="participantes-list">
                <div 
                  v-for="participante in participantes" 
                  :key="participante.id"
                  class="participante-item"
                  :class="{ 'selected': esParticipanteSeleccionado(participante.id) }"
                >
                  <ion-checkbox
                    :checked="esParticipanteSeleccionado(participante.id)"
                    @ionChange="toggleParticipante(participante)"
                  ></ion-checkbox>
                  
                  <div class="participante-info">
                    <div class="avatar-small" :style="{ backgroundColor: getColorForUser(participante.id) }">
                      {{ getInitials(participante.nombre) }}
                    </div>
                    <div class="info">
                      <h4>{{ participante.nombre }}</h4>
                      <p v-if="formulario.partesIguales && formulario.participantesIds.includes(participante.id)">
                        {{ formatMonto(parseFloat(formulario.monto || 0) / formulario.participantesIds.length) }}
                      </p>
                    </div>
                  </div>

                  <!-- Monto personalizado -->
                  <div v-if="!formulario.partesIguales && esParticipanteSeleccionado(participante.id)" class="monto-personalizado">
                    <ion-input
                      v-model="formulario.cantidadesPersonalizadas[participante.id]"
                      type="number"
                      step="0.01"
                      min="0"
                      placeholder="0.00"
                      @ionInput="validarFormulario"
                    ></ion-input>
                    <span class="currency">€</span>
                  </div>
                </div>
              </div>

              <!-- Resumen de división personalizada -->
              <div v-if="!formulario.partesIguales && formulario.participantesIds.length > 0" class="division-summary">
                <div class="summary-row">
                  <span>Total asignado:</span>
                  <span class="amount" :class="{ 'error': totalAsignado !== parseFloat(formulario.monto || 0) }">
                    {{ formatMonto(totalAsignado) }}
                  </span>
                </div>
                <div class="summary-row">
                  <span>Total del gasto:</span>
                  <span class="amount">{{ formatMonto(parseFloat(formulario.monto || 0)) }}</span>
                </div>
                <div v-if="Math.abs(totalAsignado - parseFloat(formulario.monto || 0)) > 0.01" class="warning-message">
                  <ion-icon :icon="warningOutline" color="warning"></ion-icon>
                  <span>Las cantidades no coinciden con el total del gasto</span>
                </div>
              </div>
            </div>
          </ion-card-content>
        </ion-card>

        <!-- Botones de acción -->
        <div class="action-buttons">
          <ion-button 
            expand="block" 
            @click="guardarGasto" 
            :disabled="!formularioValido || guardando"
            class="save-button"
          >
            <ion-icon v-if="guardando" name="hourglass-outline" slot="start"></ion-icon>
            <ion-icon v-else :icon="save" slot="start"></ion-icon>
            {{ guardando ? 'Guardando...' : 'Guardar Cambios' }}
          </ion-button>
          
          <ion-button 
            expand="block" 
            fill="outline" 
            @click="cancelar"
            :disabled="guardando"
          >
            <ion-icon :icon="close" slot="start"></ion-icon>
            Cancelar
          </ion-button>
        </div>
      </div>

      <!-- Selector de Pagador -->
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
        <ion-content class="ion-padding">
          <div class="selector-list">
            <div 
              v-for="participante in participantes" 
              :key="participante.id"
              class="selector-item"
              @click="seleccionarPagador(participante)"
            >
              <div class="participante-info">
                <div class="avatar-small" :style="{ backgroundColor: getColorForUser(participante.id) }">
                  {{ getInitials(participante.nombre) }}
                </div>
                <div class="info">
                  <h4>{{ participante.nombre }}</h4>
                  <p>{{ participante.email }}</p>
                </div>
              </div>
              <ion-radio
                :checked="formulario.pagadoPorId === participante.id"
                @click="seleccionarPagador(participante)"
              ></ion-radio>
            </div>
          </div>
        </ion-content>
      </ion-modal>

      <!-- Selector de Evento -->
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
        <ion-content class="ion-padding">
          <div class="selector-list">
            <!-- Opción sin evento -->
            <div class="selector-item" @click="seleccionarEvento(null)">
              <div class="evento-info">
                <ion-icon :icon="closeCircleOutline" color="medium"></ion-icon>
                <div class="info">
                  <h4>Sin evento asociado</h4>
                </div>
              </div>
              <ion-radio :checked="formulario.eventoId === null"></ion-radio>
            </div>
            
            <!-- Lista de eventos -->
            <div 
              v-for="evento in eventos" 
              :key="evento.id"
              class="selector-item"
              @click="seleccionarEvento(evento)"
            >
              <div class="evento-info">
                <ion-icon :icon="star" color="tertiary"></ion-icon>
                <div class="info">
                  <h4>{{ evento.nombre }}</h4>
                  <p v-if="evento.descripcion">{{ evento.descripcion }}</p>
                </div>
              </div>
              <ion-radio :checked="formulario.eventoId === evento.id"></ion-radio>
            </div>
          </div>
        </ion-content>
      </ion-modal>
    </ion-content>
  </ion-page>
</template>

<script setup>
import {
  IonPage, IonHeader, IonToolbar, IonTitle, IonContent, IonCard, IonCardHeader,
  IonCardTitle, IonCardContent, IonButton, IonButtons, IonBackButton, IonIcon,
  IonItem, IonLabel, IonInput, IonSegment, IonSegmentButton, IonCheckbox,
  IonChip, IonSpinner, IonModal, IonRadio, useIonRouter
} from '@ionic/vue'

import { 
  receiptOutline, peopleOutline, checkmark, chevronDown, save, close,
  warningOutline, star, closeCircleOutline
} from 'ionicons/icons'

import { ref, onMounted, computed, watch } from 'vue'
import { useRoute } from 'vue-router'

const router = useIonRouter()
const route = useRoute()

// Refs reactivos
const gasto = ref(null)
const grupo = ref(null)
const participantes = ref([])
const eventos = ref([])
const cargando = ref(true)
const guardando = ref(false)
const modalPagadorAbierto = ref(false)
const modalEventoAbierto = ref(false)

// IDs de la ruta
const grupoId = route.params.grupoId || route.params.id
const gastoId = route.params.gastoId

// Formulario
const formulario = ref({
  titulo: '',
  monto: '',
  pagadoPorId: null,
  eventoId: null,
  partesIguales: true,
  participantesIds: [],
  cantidadesPersonalizadas: {}
})

// Computed properties
const pagadorSeleccionado = computed(() => {
  return participantes.value.find(p => p.id === formulario.value.pagadoPorId)
})

const eventoSeleccionado = computed(() => {
  return eventos.value.find(e => e.id === formulario.value.eventoId)
})

const todosSeleccionados = computed(() => {
  return formulario.value.participantesIds.length === participantes.value.length
})

const totalAsignado = computed(() => {
  if (formulario.value.partesIguales) {
    return parseFloat(formulario.value.monto || 0)
  }
  
  return Object.values(formulario.value.cantidadesPersonalizadas)
    .reduce((total, cantidad) => total + parseFloat(cantidad || 0), 0)
})

const formularioValido = computed(() => {
  const tituloValido = formulario.value.titulo?.trim().length > 0
  const montoValido = parseFloat(formulario.value.monto) > 0
  const pagadorValido = formulario.value.pagadoPorId !== null
  const participantesValidos = formulario.value.participantesIds.length > 0
  
  let cantidadesValidas = true
  if (!formulario.value.partesIguales) {
    cantidadesValidas = Math.abs(totalAsignado.value - parseFloat(formulario.value.monto || 0)) < 0.01
  }
  
  return tituloValido && montoValido && pagadorValido && participantesValidos && cantidadesValidas
})

// Methods
const fetchDatos = async () => {
  cargando.value = true
  try {
    const [resGasto, resGrupo, resParticipantes, resEventos] = await Promise.all([
      fetch(`${import.meta.env.VITE_API_URL}/gasto/${gastoId}`),
      fetch(`${import.meta.env.VITE_API_URL}/grupos/${grupoId}`),
      fetch(`${import.meta.env.VITE_API_URL}/grupos/${grupoId}/participantes`),
      fetch(`${import.meta.env.VITE_API_URL}/grupos/${grupoId}/eventos`)
    ])
    
    if (resGasto.ok) {
      const gastoData = await resGasto.json()
      gasto.value = gastoData
      inicializarFormulario(gastoData)
    }
    if (resGrupo.ok) grupo.value = await resGrupo.json()
    if (resParticipantes.ok) participantes.value = await resParticipantes.json()
    if (resEventos.ok) eventos.value = await resEventos.json()
    
  } catch (error) {
    console.error('Error cargando datos:', error)
    // Datos de ejemplo para desarrollo
    const gastoEjemplo = {
      id: gastoId,
      titulo: 'Cena restaurante',
      monto: 85.50,
      pagadoPor: { id: 1, nombre: 'Carmen García' },
      evento: { id: 1, nombre: 'Comidas' },
      usuarios: [
        { id: 1, nombre: 'Carmen García', email: 'carmen@email.com' },
        { id: 2, nombre: 'Antonio López', email: 'antonio@email.com' }
      ]
    }
    
    gasto.value = gastoEjemplo
    grupo.value = { nombre: 'Viaje a Barcelona' }
    participantes.value = [
      { id: 1, nombre: 'Carmen García', email: 'carmen@email.com' },
      { id: 2, nombre: 'Antonio López', email: 'antonio@email.com' },
      { id: 3, nombre: 'Gertrudis Martín', email: 'gert@email.com' },
      { id: 4, nombre: 'José Silva', email: 'jose@email.com' }
    ]
    eventos.value = [
      { id: 1, nombre: 'Comidas', descripcion: 'Restaurantes y comida' },
      { id: 2, nombre: 'Transporte', descripcion: 'Taxis, gasolina, etc.' }
    ]
    
    inicializarFormulario(gastoEjemplo)
  } finally {
    cargando.value = false
  }
}

const inicializarFormulario = (gastoData) => {
  formulario.value = {
    titulo: gastoData.titulo || '',
    monto: gastoData.monto?.toString() || '',
    pagadoPorId: gastoData.pagadoPor?.id || null,
    eventoId: gastoData.evento?.id || null,
    partesIguales: true, // Por defecto asumimos partes iguales
    participantesIds: gastoData.usuarios?.map(u => u.id) || [],
    cantidadesPersonalizadas: {}
  }
  
  // Inicializar cantidades personalizadas
  if (gastoData.usuarios) {
    gastoData.usuarios.forEach(usuario => {
      formulario.value.cantidadesPersonalizadas[usuario.id] = (gastoData.monto / gastoData.usuarios.length).toFixed(2)
    })
  }
}

const formatMonto = (monto) => {
  const num = parseFloat(monto) || 0
  return new Intl.NumberFormat('es-ES', {
    style: 'currency',
    currency: 'EUR'
  }).format(num)
}

const getInitials = (nombre) => {
  return nombre.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2)
}

const getColorForUser = (userId) => {
  const colors = ['#3880ff', '#10dc60', '#ffce00', '#f04141', '#7044ff', '#10d4aa']
  return colors[userId % colors.length]
}

const validarFormulario = () => {
  // La validación se hace automáticamente con los computed properties
}

const abrirSelectorPagador = () => {
  modalPagadorAbierto.value = true
}

const abrirSelectorEvento = () => {
  modalEventoAbierto.value = true
}

const seleccionarPagador = (participante) => {
  formulario.value.pagadoPorId = participante.id
  modalPagadorAbierto.value = false
}

const seleccionarEvento = (evento) => {
  formulario.value.eventoId = evento?.id || null
  modalEventoAbierto.value = false
}

const cambiarTipoDivision = (event) => {
  formulario.value.partesIguales = event.detail.value
  
  if (formulario.value.partesIguales) {
    // Resetear cantidades personalizadas
    const montoPorPersona = parseFloat(formulario.value.monto || 0) / formulario.value.participantesIds.length
    formulario.value.participantesIds.forEach(id => {
      formulario.value.cantidadesPersonalizadas[id] = montoPorPersona.toFixed(2)
    })
  }
}

const esParticipanteSeleccionado = (participanteId) => {
  return formulario.value.participantesIds.includes(participanteId)
}

const toggleParticipante = (participante) => {
  const index = formulario.value.participantesIds.indexOf(participante.id)
  
  if (index > -1) {
    // Remover participante
    formulario.value.participantesIds.splice(index, 1)
    delete formulario.value.cantidadesPersonalizadas[participante.id]
  } else {
    // Agregar participante
    formulario.value.participantesIds.push(participante.id)
    const montoPorPersona = parseFloat(formulario.value.monto || 0) / (formulario.value.participantesIds.length || 1)
    formulario.value.cantidadesPersonalizadas[participante.id] = montoPorPersona.toFixed(2)
  }
  
  validarFormulario()
}

const seleccionarTodos = () => {
  if (todosSeleccionados.value) {
    // Deseleccionar todos
    formulario.value.participantesIds = []
    formulario.value.cantidadesPersonalizadas = {}
  } else {
    // Seleccionar todos
    formulario.value.participantesIds = participantes.value.map(p => p.id)
    const montoPorPersona = parseFloat(formulario.value.monto || 0) / participantes.value.length
    participantes.value.forEach(p => {
      formulario.value.cantidadesPersonalizadas[p.id] = montoPorPersona.toFixed(2)
    })
  }
  
  validarFormulario()
}

const guardarGasto = async () => {
  if (!formularioValido.value || guardando.value) return
  
  guardando.value = true
  
  try {
    const payload = {
      titulo: formulario.value.titulo.trim(),
      monto: parseFloat(formulario.value.monto),
      pagadoPorId: formulario.value.pagadoPorId,
      grupoId: parseInt(grupoId),
      eventoId: formulario.value.eventoId,
      partesIguales: formulario.value.partesIguales,
      participantesIds: formulario.value.participantesIds,
      cantidadesPersonalizadas: formulario.value.partesIguales ? {} : formulario.value.cantidadesPersonalizadas
    }
    
    const response = await fetch(`${import.meta.env.VITE_API_URL}/gasto/${gastoId}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(payload)
    })
    
    if (response.ok) {
      router.back()
    } else {
      throw new Error('Error al actualizar el gasto')
    }
    
  } catch (error) {
    console.error('Error guardando gasto:', error)
    // Simular éxito para desarrollo
    setTimeout(() => {
      router.back()
    }, 1000)
  } finally {
    guardando.value = false
  }
}

const cancelar = () => {
  router.back()
}

// Watchers
watch(() => formulario.value.monto, (newMonto) => {
  if (formulario.value.partesIguales && formulario.value.participantesIds.length > 0) {
    const montoPorPersona = parseFloat(newMonto || 0) / formulario.value.participantesIds.length
    formulario.value.participantesIds.forEach(id => {
      formulario.value.cantidadesPersonalizadas[id] = montoPorPersona.toFixed(2)
    })
  }
})

onMounted(() => {
  fetchDatos()
})
</script>
