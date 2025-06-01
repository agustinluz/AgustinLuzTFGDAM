<template>
  <ion-page>
    <ion-header :translucent="true" class="ion-no-border">
      <ion-toolbar color="primary">
        <ion-buttons slot="start">
          <ion-back-button :default-href="`/grupos/${grupoId}`" color="light"></ion-back-button>
        </ion-buttons>
        <ion-title class="header-title">
          <div class="title-container">
            <h1>Nuevo Gasto</h1>
            <p v-if="grupo?.nombre" class="subtitle">{{ grupo.nombre }}</p>
          </div>
        </ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content :fullscreen="true" class="ion-padding">
      <!-- Loading State -->
      <div v-if="cargando" class="loading-container">
        <ion-spinner name="crescent" color="primary"></ion-spinner>
        <p>Cargando datos...</p>
      </div>

      <div v-else class="form-container">
        <!-- Información básica -->
        <ion-card class="modern-card">
          <ion-card-header>
            <div class="card-header">
              <div class="header-left">
                <div class="header-icon">
                  <ion-icon :icon="receiptOutline"></ion-icon>
                </div>
                <ion-card-title>Información del Gasto</ion-card-title>
              </div>
            </div>
          </ion-card-header>
          <ion-card-content>
            <div class="form-section">
              <ion-item class="modern-item">
                <ion-input
                  v-model="formulario.titulo"
                  label="Título del gasto"
                  placeholder="Ej: Cena en restaurante"
                  fill="outline"
                  :class="{ 'ion-invalid': errores.titulo }"
                  @ionBlur="validarCampo('titulo')"
                ></ion-input>
              </ion-item>
              <ion-note v-if="errores.titulo" color="danger" class="error-note">
                {{ errores.titulo }}
              </ion-note>

              <ion-item class="modern-item ion-margin-top">
                <ion-input
                  v-model="formulario.monto"
                  label="Monto total"
                  placeholder="0.00"
                  type="number"
                  step="0.01"
                  fill="outline"
                  :class="{ 'ion-invalid': errores.monto }"
                  @ionBlur="validarCampo('monto')"
                >
                  <div slot="end" class="currency-symbol">€</div>
                </ion-input>
              </ion-item>
              <ion-note v-if="errores.monto" color="danger" class="error-note">
                {{ errores.monto }}
              </ion-note>
            </div>
          </ion-card-content>
        </ion-card>

        <!-- Pagador y participantes -->
        <ion-card class="modern-card">
          <ion-card-header>
            <div class="card-header">
              <div class="header-left">
                <div class="header-icon">
                  <ion-icon :icon="peopleOutline"></ion-icon>
                </div>
                <ion-card-title>Participantes</ion-card-title>
              </div>
            </div>
          </ion-card-header>
          <ion-card-content>
            <!-- Pagador -->
            <div class="form-section">
              <ion-item class="modern-item">
                <ion-select
                  v-model="formulario.pagadoPorId"
                  label="Pagado por"
                  placeholder="Seleccionar quien pagó"
                  interface="popover"
                  fill="outline"
                  :class="{ 'ion-invalid': errores.pagadoPorId }"
                  @ionChange="validarCampo('pagadoPorId')"
                >
                  <ion-select-option v-for="usuario in usuarios" :key="usuario.id" :value="usuario.id">
                    <div class="user-option">
                      <div class="user-avatar" :style="{ backgroundColor: getColorForUser(usuario.id) }">
                        {{ getInitials(usuario.nombre) }}
                      </div>
                      <span>{{ usuario.nombre }}</span>
                    </div>
                  </ion-select-option>
                </ion-select>
              </ion-item>
              <ion-note v-if="errores.pagadoPorId" color="danger" class="error-note">
                {{ errores.pagadoPorId }}
              </ion-note>

              <!-- Lista de participantes -->
              <div class="participants-section ion-margin-top">
                <div class="section-label">
                  <ion-icon :icon="checkboxOutline"></ion-icon>
                  <span>Participantes ({{ participantesSeleccionados.length }})</span>
                </div>
                
                <div class="participants-grid">
                  <div 
                    v-for="usuario in usuarios" 
                    :key="usuario.id"
                    class="participant-card"
                    :class="{ 'selected': formulario.participantesIds.includes(usuario.id) }"
                    @click="toggleParticipante(usuario.id)"
                  >
                    <div class="participant-avatar" :style="{ backgroundColor: getColorForUser(usuario.id) }">
                      {{ getInitials(usuario.nombre) }}
                    </div>
                    <div class="participant-info">
                      <h4>{{ usuario.nombre }}</h4>
                      <p>{{ usuario.email }}</p>
                    </div>
                    <div class="participant-checkbox">
                      <ion-checkbox
                        :checked="formulario.participantesIds.includes(usuario.id)"
                        @ionChange="() => toggleParticipante(usuario.id)"
                      ></ion-checkbox>
                    </div>
                  </div>
                </div>
                
                <ion-note v-if="errores.participantes" color="danger" class="error-note">
                  {{ errores.participantes }}
                </ion-note>
              </div>
            </div>
          </ion-card-content>
        </ion-card>

        <!-- División del gasto -->
        <ion-card class="modern-card">
          <ion-card-header>
            <div class="card-header">
              <div class="header-left">
                <div class="header-icon">
                  <ion-icon :icon="calculatorOutline"></ion-icon>
                </div>
                <ion-card-title>División del Gasto</ion-card-title>
              </div>
            </div>
          </ion-card-header>
          <ion-card-content>
            <div class="division-options">
              <ion-radio-group v-model="formulario.partesIguales" @ionChange="onDivisionChange">
                <ion-item class="division-option">
                  <ion-radio slot="start" :value="true"></ion-radio>
                  <ion-label>
                    <h3>Partes iguales</h3>
                    <p>Dividir el monto entre todos los participantes</p>
                    <div v-if="formulario.partesIguales && participantesSeleccionados.length > 0" class="division-preview">
                      {{ formatMonto(formulario.monto / participantesSeleccionados.length) }} por persona
                    </div>
                  </ion-label>
                </ion-item>

                <ion-item class="division-option">
                  <ion-radio slot="start" :value="false"></ion-radio>
                  <ion-label>
                    <h3>Cantidades personalizadas</h3>
                    <p>Especificar monto individual para cada participante</p>
                  </ion-label>
                </ion-item>
              </ion-radio-group>
            </div>

            <!-- Cantidades personalizadas -->
            <div v-if="!formulario.partesIguales && participantesSeleccionados.length > 0" class="custom-amounts">
              <div class="amounts-header">
                <h4>Cantidades por participante</h4>
                <div class="total-indicator" :class="{ 'valid': totalPersonalizado === parseFloat(formulario.monto) }">
                  Total: {{ formatMonto(totalPersonalizado) }} / {{ formatMonto(formulario.monto) }}
                </div>
              </div>
              
              <div class="amounts-list">
                <div 
                  v-for="usuario in participantesSeleccionados" 
                  :key="usuario.id"
                  class="amount-item"
                >
                  <div class="user-info">
                    <div class="user-avatar" :style="{ backgroundColor: getColorForUser(usuario.id) }">
                      {{ getInitials(usuario.nombre) }}
                    </div>
                    <span>{{ usuario.nombre }}</span>
                  </div>
                  <ion-input
                    v-model="formulario.cantidadesPersonalizadas[usuario.id]"
                    type="number"
                    step="0.01"
                    placeholder="0.00"
                    fill="outline"
                    @ionInput="calcularTotalPersonalizado"
                  >
                    <div slot="end" class="currency-symbol">€</div>
                  </ion-input>
                </div>
              </div>
              
              <ion-note v-if="errores.cantidades" color="danger" class="error-note">
                {{ errores.cantidades }}
              </ion-note>
            </div>
          </ion-card-content>
        </ion-card>

        <!-- Evento (opcional) -->
        <ion-card class="modern-card" v-if="eventos.length > 0">
          <ion-card-header>
            <div class="card-header">
              <div class="header-left">
                <div class="header-icon">
                  <ion-icon :icon="starOutline"></ion-icon>
                </div>
                <ion-card-title>Evento (Opcional)</ion-card-title>
              </div>
            </div>
          </ion-card-header>
          <ion-card-content>
            <ion-item class="modern-item">
              <ion-select
                v-model="formulario.eventoId"
                label="Asociar a evento"
                placeholder="Sin evento específico"
                interface="popover"
                fill="outline"
              >
                <ion-select-option :value="null">Sin evento</ion-select-option>
                <ion-select-option v-for="evento in eventos" :key="evento.id" :value="evento.id">
                  {{ evento.nombre }}
                </ion-select-option>
              </ion-select>
            </ion-item>
          </ion-card-content>
        </ion-card>

        <!-- Resumen -->
        <ion-card class="modern-card summary-card" v-if="participantesSeleccionados.length > 0">
          <ion-card-header>
            <div class="card-header">
              <div class="header-left">
                <div class="header-icon">
                  <ion-icon :icon="eyeOutline"></ion-icon>
                </div>
                <ion-card-title>Resumen</ion-card-title>
              </div>
            </div>
          </ion-card-header>
          <ion-card-content>
            <div class="summary-content">
              <div class="summary-item">
                <span class="label">Monto total:</span>
                <span class="value">{{ formatMonto(formulario.monto) }}</span>
              </div>
              <div class="summary-item">
                <span class="label">Participantes:</span>
                <span class="value">{{ participantesSeleccionados.length }}</span>
              </div>
              <div class="summary-item">
                <span class="label">Pagado por:</span>
                <span class="value">{{ pagadorSeleccionado?.nombre || 'No seleccionado' }}</span>
              </div>
              <div class="summary-item">
                <span class="label">División:</span>
                <span class="value">{{ formulario.partesIguales ? 'Partes iguales' : 'Personalizada' }}</span>
              </div>
            </div>
          </ion-card-content>
        </ion-card>

        <!-- Botones de acción -->
        <div class="action-buttons">
          <ion-button 
            expand="block" 
            size="large"
            @click="crearGasto"
            :disabled="!formularioValido || enviando"
            class="primary-button"
          >
            <ion-spinner v-if="enviando" name="crescent" slot="start"></ion-spinner>
            <ion-icon v-else :icon="saveOutline" slot="start"></ion-icon>
            {{ enviando ? 'Guardando...' : 'Crear Gasto' }}
          </ion-button>
          
          <ion-button 
            expand="block" 
            fill="clear" 
            color="medium"
            @click="cancelar"
            :disabled="enviando"
            class="secondary-button"
          >
            Cancelar
          </ion-button>
        </div>
      </div>

      <!-- Toast para mensajes -->
      <ion-toast
        :is-open="mostrarToast"
        :message="mensajeToast"
        :duration="3000"
        :color="tipoToast"
        @did-dismiss="mostrarToast = false"
      ></ion-toast>
    </ion-content>
  </ion-page>
</template>

<script setup>
import {
  IonPage, IonHeader, IonToolbar, IonTitle, IonContent, IonCard, IonCardHeader,
  IonCardTitle, IonCardContent, IonIcon, IonButton, IonButtons, IonBackButton,
  IonInput, IonSelect, IonSelectOption, IonLabel, IonItem, IonNote, IonCheckbox,
  IonRadioGroup, IonRadio, IonSpinner, IonToast, useIonRouter
} from '@ionic/vue'

import { 
  receiptOutline, peopleOutline, calculatorOutline, starOutline, eyeOutline,
  saveOutline, checkboxOutline
} from 'ionicons/icons'

import { ref, onMounted, computed, watch, reactive } from 'vue'
import { useRoute } from 'vue-router'

const router = useIonRouter()
const route = useRoute()
const grupoId = route.params.id

// Estados principales
const cargando = ref(true)
const enviando = ref(false)
const usuarios = ref([])
const eventos = ref([])
const grupo = ref(null)

// Estados del toast
const mostrarToast = ref(false)
const mensajeToast = ref('')
const tipoToast = ref('success')

// Formulario reactivo
const formulario = reactive({
  titulo: '',
  monto: '',
  pagadoPorId: null,
  participantesIds: [],
  partesIguales: true,
  eventoId: null,
  cantidadesPersonalizadas: {}
})

// Errores de validación
const errores = reactive({
  titulo: '',
  monto: '',
  pagadoPorId: '',
  participantes: '',
  cantidades: ''
})

const totalPersonalizado = ref(0)

// Computed properties
const participantesSeleccionados = computed(() => {
  return usuarios.value.filter(u => formulario.participantesIds.includes(u.id))
})

const pagadorSeleccionado = computed(() => {
  return usuarios.value.find(u => u.id === formulario.pagadoPorId)
})

const formularioValido = computed(() => {
  return formulario.titulo && 
         formulario.monto && 
         formulario.pagadoPorId && 
         formulario.participantesIds.length > 0 &&
         (formulario.partesIguales || totalPersonalizado.value === parseFloat(formulario.monto))
})

// Watchers
watch(() => formulario.participantesIds, (nuevos) => {
  if (!formulario.partesIguales) {
    // Inicializar cantidades personalizadas para nuevos participantes
    nuevos.forEach(id => {
      if (!formulario.cantidadesPersonalizadas[id]) {
        formulario.cantidadesPersonalizadas[id] = ''
      }
    })
    
    // Remover cantidades de participantes no seleccionados
    Object.keys(formulario.cantidadesPersonalizadas).forEach(id => {
      if (!nuevos.includes(parseInt(id))) {
        delete formulario.cantidadesPersonalizadas[id]
      }
    })
    
    calcularTotalPersonalizado()
  }
})

// Métodos de utilidad
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

const mostrarMensaje = (mensaje, tipo = 'success') => {
  mensajeToast.value = mensaje
  tipoToast.value = tipo
  mostrarToast.value = true
}

// Métodos de validación
const validarCampo = (campo) => {
  errores[campo] = ''
  
  switch(campo) {
    case 'titulo':
      if (!formulario.titulo.trim()) {
        errores.titulo = 'El título es obligatorio'
      }
      break
      
    case 'monto':
      const monto = parseFloat(formulario.monto)
      if (!monto || monto <= 0) {
        errores.monto = 'El monto debe ser mayor a 0'
      }
      break
      
    case 'pagadoPorId':
      if (!formulario.pagadoPorId) {
        errores.pagadoPorId = 'Selecciona quien pagó el gasto'
      }
      break
  }
}

const validarFormulario = () => {
  let valido = true
  
  // Validar campos básicos
  validarCampo('titulo')
  validarCampo('monto')
  validarCampo('pagadoPorId')
  
  // Validar participantes
  if (formulario.participantesIds.length === 0) {
    errores.participantes = 'Selecciona al menos un participante'
    valido = false
  }
  
  // Validar cantidades personalizadas
  if (!formulario.partesIguales) {
    const total = totalPersonalizado.value
    const montoTotal = parseFloat(formulario.monto)
    
    if (Math.abs(total - montoTotal) > 0.01) {
      errores.cantidades = 'La suma de las cantidades debe igualar el monto total'
      valido = false
    }
  }
  
  return valido && Object.values(errores).every(error => !error)
}

// Métodos de interacción
const toggleParticipante = (usuarioId) => {
  const index = formulario.participantesIds.indexOf(usuarioId)
  if (index > -1) {
    formulario.participantesIds.splice(index, 1)
  } else {
    formulario.participantesIds.push(usuarioId)
  }
}

const onDivisionChange = () => {
  if (formulario.partesIguales) {
    formulario.cantidadesPersonalizadas = {}
  } else {
    // Inicializar cantidades personalizadas
    participantesSeleccionados.value.forEach(usuario => {
      formulario.cantidadesPersonalizadas[usuario.id] = ''
    })
  }
}

const calcularTotalPersonalizado = () => {
  totalPersonalizado.value = Object.values(formulario.cantidadesPersonalizadas)
    .reduce((total, monto) => total + (parseFloat(monto) || 0), 0)
}

// Métodos principales
const cargarDatos = async () => {
  cargando.value = true
  try {
    await Promise.all([
      cargarGrupo(),
      cargarUsuarios(),
      cargarEventos()
    ])
    
    // Establecer usuario actual como pagador por defecto
    const usuarioActual = JSON.parse(localStorage.getItem('usuario') || '{}')
    if (usuarioActual.id) {
      formulario.pagadoPorId = usuarioActual.id
      formulario.participantesIds = [usuarioActual.id]
    }
    
  } catch (error) {
    console.error('Error al cargar datos:', error)
    mostrarMensaje('Error al cargar los datos', 'danger')
  } finally {
    cargando.value = false
  }
}

const cargarGrupo = async () => {
  try {
    const res = await fetch(`${import.meta.env.VITE_API_URL}/grupos/${grupoId}`)
    if (!res.ok) throw new Error('Error al cargar grupo')
    grupo.value = await res.json()
  } catch (error) {
    throw new Error('Error al cargar información del grupo')
  }
}

const cargarUsuarios = async () => {
  try {
    const res = await fetch(`${import.meta.env.VITE_API_URL}/grupos/${grupoId}/usuarios`)
    if (!res.ok) throw new Error('Error al cargar usuarios')
    usuarios.value = await res.json()
  } catch (error) {
    throw new Error('Error al cargar usuarios del grupo')
  }
}

const cargarEventos = async () => {
  try {
    const res = await fetch(`${import.meta.env.VITE_API_URL}/grupos/${grupoId}/eventos`)
    if (res.ok) {
      eventos.value = await res.json()
    }
  } catch (error) {
    console.warn('No se pudieron cargar los eventos:', error)
    eventos.value = []
  }
}

const crearGasto = async () => {
  if (!validarFormulario()) {
    mostrarMensaje('Por favor, corrige los errores en el formulario', 'danger')
    return
  }
  
  enviando.value = true
  
  try {
    const payload = {
      titulo: formulario.titulo.trim(),
      monto: parseFloat(formulario.monto),
      pagadoPorId: formulario.pagadoPorId,
      grupoId: parseInt(grupoId),
      eventoId: formulario.eventoId,
      partesIguales: formulario.partesIguales,
      participantesIds: formulario.participantesIds,
      cantidadesPersonalizadas: formulario.partesIguales ? null : formulario.cantidadesPersonalizadas
    }
    
    const res = await fetch(`${import.meta.env.VITE_API_URL}/gasto/${grupoId}/crear`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })

    if (!res.ok) {
      const errorText = await res.text()
      throw new Error(errorText || 'Error al crear el gasto')
    }
    
    const gastoCreado = await res.json()
    mostrarMensaje('Gasto creado correctamente', 'success')
    
    // Navegar de vuelta al grupo después de un breve delay
    setTimeout(() => {
      router.push(`/dashborard/${grupoId}`)
    }, 1500)
    
  } catch (error) {
    console.error('Error al crear gasto:', error)
    mostrarMensaje(error.message || 'Error al crear el gasto', 'danger')
  } finally {
    enviando.value = false
  }
}

const cancelar = () => {
  router.push(`/dashborard/${grupoId}`)
}

// Lifecycle
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
  min-height: 200px;
  gap: 16px;
}

.form-container {
  max-width: 600px;
  margin: 0 auto;
}

.modern-card {
  margin-bottom: 16px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: var(--ion-color-primary-tint);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--ion-color-primary);
}

.modern-item {
  --inner-padding-start: 0;
  --inner-padding-end: 0;
  margin-bottom: 16px;
}

.currency-symbol {
  color: var(--ion-color-medium);
  font-weight: 500;
}

.error-note {
  display: block;
  margin-top: 4px;
  margin-left: 16px;
  font-size: 12px;
}

.user-option {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar, .participant-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 12px;
}

.participants-section {
  background: var(--ion-color-light);
  border-radius: 8px;
  padding: 16px;
}

.section-label {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  font-weight: 600;
  color: var(--ion-color-dark);
}

.participants-grid {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.participant-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: white;
  border-radius: 8px;
  border: 2px solid transparent;
  cursor: pointer;
  transition: all 0.2s ease;
}

.participant-card:hover {
  background: var(--ion-color-primary-tint);
}

.participant-card.selected {
  border-color: var(--ion-color-primary);
  background: var(--ion-color-primary-tint);
}

.participant-info {
  flex: 1;
}

.participant-info h4 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
}

.participant-info p {
  margin: 2px 0 0 0;
  font-size: 12px;
  color: var(--ion-color-medium);
}

.division-options {
  margin-bottom: 16px;
}

.division-option {
  margin-bottom: 16px;
}

.division-preview {
  color: var(--ion-color-primary);
  font-weight: 600;
  margin-top: 4px;
}

.custom-amounts {
  background: var(--ion-color-light);
  border-radius: 8px;
  padding: 16px;
  margin-top: 16px;
}

.amounts-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.total-indicator {
  font-size: 14px;
  font-weight: 600;
  color: var(--ion-color-danger);
}

.total-indicator.valid {
  color: var(--ion-color-success);
}

.amounts-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.amount-item {
  display: flex;
  align-items: center;
  gap: 12px;
  background: white;
  padding: 8px 12px;
  border-radius: 8px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  font-size: 14px;
  font-weight: 500;
}

.amount-item ion-input {
  max-width: 120px;
}

.summary-card {
  background: var(--ion-color-primary-tint);
}

.summary-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.summary-item .label {
  color: var(--ion-color-medium);
  font-size: 14px;
}

.summary-item .value {
  font-weight: 600;
  color: var(--ion-color-dark);
}

.action-buttons {
  margin-top: 24px;
  margin-bottom: 32px;
}

.primary-button {
  --border-radius: 12px;
  margin-bottom: 12px;
  height: 48px;
}

.secondary-button {
  --border-radius: 12px;
  height: 44px;
}

.title-container h1 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
}

</style>