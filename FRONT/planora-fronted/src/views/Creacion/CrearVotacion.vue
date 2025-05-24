<!-- src/views/CrearVotacionPage.vue -->
<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-buttons slot="start">
          <ion-back-button></ion-back-button>
        </ion-buttons>
        <ion-title>Nueva votación</ion-title>
        <ion-buttons slot="end">
          <ion-button 
            @click="crearVotacion" 
            :disabled="!puedeGuardar || isLoading"
            fill="clear"
          >
            <ion-icon name="checkmark" slot="icon-only"></ion-icon>
          </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>

    <ion-content>
      <form @submit.prevent="crearVotacion" class="votacion-form">
        <!-- Información básica -->
        <div class="form-section">
          <h3 class="section-title">
            <ion-icon name="help-circle-outline"></ion-icon>
            Pregunta de la votación
          </h3>
          
          <ion-item class="textarea-item">
            <ion-textarea
              label="Pregunta"
              label-placement="stacked"
              fill="outline"
              v-model="pregunta"
              placeholder="Ej: ¿Dónde prefieres que celebremos la cena?"
              rows="3"
              auto-grow
              :maxlength="300"
              counter
              required
            ></ion-textarea>
          </ion-item>
        </div>

        <!-- Opciones de votación -->
        <div class="form-section">
          <h3 class="section-title">
            <ion-icon name="list-outline"></ion-icon>
            Opciones de votación
            <span class="opciones-count">({{ opciones.length }})</span>
          </h3>
          
          <div class="opciones-container">
            <div 
              v-for="(opcion, index) in opciones" 
              :key="`opcion-${index}`"
              class="opcion-item"
            >
              <ion-item>
                <ion-input
                  :label="`Opción ${index + 1}`"
                  label-placement="stacked"
                  fill="outline"
                  v-model="opciones[index]"
                  :placeholder="`Opción ${index + 1}`"
                  :maxlength="100"
                  required
                ></ion-input>
                <ion-button 
                  v-if="opciones.length > 2"
                  fill="clear" 
                  color="danger" 
                  slot="end"
                  @click="eliminarOpcion(index)"
                >
                  <ion-icon name="trash-outline" slot="icon-only"></ion-icon>
                </ion-button>
              </ion-item>
            </div>
          </div>

          <div class="opciones-actions">
            <ion-button 
              fill="outline" 
              @click="agregarOpcion"
              :disabled="opciones.length >= 10"
            >
              <ion-icon name="add-outline" slot="start"></ion-icon>
              Agregar opción
            </ion-button>
            <p class="opciones-help" v-if="opciones.length < 2">
              <ion-icon name="information-circle-outline"></ion-icon>
              Necesitas al menos 2 opciones para crear la votación
            </p>
          </div>
        </div>

        <!-- Fecha límite (opcional) -->
        <div class="form-section">
          <h3 class="section-title">
            <ion-icon name="time-outline"></ion-icon>
            Fecha límite (opcional)
          </h3>
          
          <ion-item>
            <ion-checkbox 
              v-model="tieneFechaLimite"
              slot="start"
            ></ion-checkbox>
            <ion-label>
              <h3>Establecer fecha límite</h3>
              <p>La votación se cerrará automáticamente</p>
            </ion-label>
          </ion-item>

          <ion-item 
            v-if="tieneFechaLimite" 
            button 
            @click="abrirCalendario"
            class="fecha-limite-item"
          >
            <ion-label>
              <h3>Fecha límite</h3>
              <p v-if="fechaLimite">{{ formatearFecha(fechaLimite) }}</p>
              <p v-else class="placeholder-text">Selecciona fecha y hora</p>
            </ion-label>
            <ion-icon name="chevron-forward-outline" slot="end"></ion-icon>
          </ion-item>

          <!-- Modal de calendario -->
          <ion-modal :is-open="mostrarCalendario" @did-dismiss="cerrarCalendario">
            <ion-header>
              <ion-toolbar>
                <ion-title>Fecha límite</ion-title>
                <ion-buttons slot="end">
                  <ion-button @click="cerrarCalendario">Cerrar</ion-button>
                </ion-buttons>
              </ion-toolbar>
            </ion-header>
            <ion-content class="calendar-modal">
              <ion-datetime
                v-model="fechaLimite"
                presentation="date-time"
                :min="fechaMinima"
                locale="es-ES"
                @ion-change="onFechaChange"
              ></ion-datetime>
              <div class="calendar-actions">
                <ion-button expand="block" @click="confirmarFecha" :disabled="!fechaLimite">
                  Confirmar fecha límite
                </ion-button>
              </div>
            </ion-content>
          </ion-modal>
        </div>

        <!-- Vista previa -->
        <div class="form-section" v-if="pregunta.trim() && opcionesValidas.length >= 2">
          <h3 class="section-title">
            <ion-icon name="eye-outline"></ion-icon>
            Vista previa
          </h3>
          
          <div class="preview-card">
            <div class="preview-question">
              <h4>{{ pregunta }}</h4>
            </div>
            <div class="preview-options">
              <div 
                v-for="(opcion, index) in opcionesValidas" 
                :key="`preview-${index}`"
                class="preview-option"
              >
                <ion-radio></ion-radio>
                <span>{{ opcion }}</span>
              </div>
            </div>
            <div class="preview-info">
              <p>
                <ion-icon name="people-outline"></ion-icon>
                0 votos • 
                <ion-icon name="time-outline"></ion-icon>
                {{ tieneFechaLimite && fechaLimite ? 'Expira ' + formatearFecha(fechaLimite) : 'Sin fecha límite' }}
              </p>
            </div>
          </div>
        </div>

        <!-- Botón de guardar -->
        <div class="action-section">
          <ion-button 
            expand="block" 
            type="submit"
            :disabled="!puedeGuardar || isLoading"
            size="large"
          >
            <ion-icon name="ballot-outline" slot="start"></ion-icon>
            {{ isLoading ? 'Creando votación...' : 'Crear votación' }}
          </ion-button>
        </div>
      </form>

      <!-- Toast para mensajes -->
      <ion-toast
        :is-open="showToast"
        :message="toastMessage"
        :color="toastColor"
        :duration="3000"
        @did-dismiss="showToast = false"
        position="top"
      ></ion-toast>

      <!-- Loading -->
      <ion-loading
        :is-open="isLoading"
        message="Creando votación..."
      ></ion-loading>
    </ion-content>
  </ion-page>
</template>

<script setup>
import {
  IonPage, IonHeader, IonToolbar, IonTitle, IonContent, IonInput, 
  IonTextarea, IonButton, IonIcon, IonButtons, IonBackButton, 
  IonItem, IonLabel, IonToast, IonLoading, IonModal, IonDatetime,
  IonCheckbox, IonRadio
} from '@ionic/vue'
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

// Estado del formulario
const pregunta = ref('')
const opciones = ref(['', ''])
const tieneFechaLimite = ref(false)
const fechaLimite = ref('')
const isLoading = ref(false)

// Estados del UI
const mostrarCalendario = ref(false)

// Toast
const showToast = ref(false)
const toastMessage = ref('')
const toastColor = ref('success')

// Datos del grupo y usuario
const grupoId = ref(null)
const usuario = ref(null)

// Fecha mínima (ahora + 1 hora)
const fechaMinima = new Date(Date.now() + 60 * 60 * 1000).toISOString()

// Computed
const opcionesValidas = computed(() => {
  return opciones.value.filter(opcion => opcion.trim().length > 0)
})

const puedeGuardar = computed(() => {
  const preguntaValida = pregunta.value.trim().length > 0
  const opcionesValidasCount = opcionesValidas.value.length >= 2
  const fechaValida = !tieneFechaLimite.value || fechaLimite.value
  
  return preguntaValida && opcionesValidasCount && fechaValida
})

onMounted(async () => {
  try {
    grupoId.value = route.params.grupoId || route.params.id
    
    const usuarioString = localStorage.getItem('usuario')
    if (!usuarioString) {
      throw new Error('Usuario no autenticado')
    }
    
    usuario.value = JSON.parse(usuarioString)
    
    if (!grupoId.value) {
      throw new Error('Grupo no especificado')
    }
    
  } catch (error) {
    console.error('Error en inicialización:', error)
    mostrarToast('Error al cargar la página', 'danger')
    setTimeout(() => router.back(), 2000)
  }
})

const mostrarToast = (message, color = 'success') => {
  toastMessage.value = message
  toastColor.value = color
  showToast.value = true
}

const formatearFecha = (fechaISO) => {
  const fecha = new Date(fechaISO)
  return fecha.toLocaleString('es-ES', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// Funciones de opciones
const agregarOpcion = () => {
  if (opciones.value.length < 10) {
    opciones.value.push('')
  }
}

const eliminarOpcion = (index) => {
  if (opciones.value.length > 2) {
    opciones.value.splice(index, 1)
  }
}

// Funciones del calendario
const abrirCalendario = () => {
  mostrarCalendario.value = true
}

const cerrarCalendario = () => {
  mostrarCalendario.value = false
}

const onFechaChange = (event) => {
  fechaLimite.value = event.detail.value
}

const confirmarFecha = () => {
  if (fechaLimite.value) {
    cerrarCalendario()
    mostrarToast('Fecha límite establecida', 'success')
  }
}

const crearVotacion = async () => {
  if (!puedeGuardar.value) {
    mostrarToast('Por favor, completa todos los campos obligatorios', 'warning')
    return
  }

  isLoading.value = true

  try {
    const token = localStorage.getItem('token')
    if (!token) {
      throw new Error('Token de autenticación no encontrado')
    }

    // Preparar datos según el DTO del backend
    const votacionData = {
      pregunta: pregunta.value.trim(),
      opciones: opcionesValidas.value,
      creadaPorId: usuario.value.id,
      fechaLimite: tieneFechaLimite.value && fechaLimite.value ? 
        new Date(fechaLimite.value).toISOString() : null
    }

    console.log('Enviando votación:', votacionData)

    const response = await fetch(
      `${import.meta.env.VITE_API_URL}/grupos/${grupoId.value}/votaciones`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(votacionData)
      }
    )

    if (!response.ok) {
      const errorData = await response.text()
      throw new Error(errorData || `Error ${response.status}: ${response.statusText}`)
    }

    const votacionCreada = await response.json()
    console.log('Votación creada:', votacionCreada)

    mostrarToast('Votación creada correctamente', 'success')
    
    // Limpiar formulario
    pregunta.value = ''
    opciones.value = ['', '']
    tieneFechaLimite.value = false
    fechaLimite.value = ''
    
    // Navegar de vuelta después de un delay
    setTimeout(() => {
      router.back()
    }, 1500)

  } catch (error) {
    console.error('Error al crear votación:', error)
    mostrarToast(
      error.message || 'Error al crear la votación. Inténtalo de nuevo.',
      'danger'
    )
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.votacion-form {
  padding: 1rem;
  max-width: 800px;
  margin: 0 auto;
}

.form-section {
  margin-bottom: 2rem;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin: 0 0 1rem 0;
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--ion-color-primary);
}

.opciones-count {
  font-size: 0.9rem;
  color: var(--ion-color-medium);
  font-weight: normal;
}

.textarea-item {
  margin-bottom: 1rem;
}

.opciones-container {
  margin-bottom: 1rem;
}

.opcion-item {
  margin-bottom: 0.5rem;
}

.opciones-actions {
  text-align: center;
  margin-top: 1rem;
}

.opciones-help {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  margin-top: 0.5rem;
  font-size: 0.9rem;
  color: var(--ion-color-warning);
}

.fecha-limite-item {
  margin-top: 1rem;
}

.placeholder-text {
  color: var(--ion-color-medium);
  font-style: italic;
}

.preview-card {
  background: var(--ion-color-light);
  border-radius: 12px;
  padding: 1rem;
  border: 1px solid var(--ion-color-light-shade);
}

.preview-question {
  margin-bottom: 1rem;
}

.preview-question h4 {
  margin: 0;
  font-size: 1.1rem;
  color: var(--ion-color-dark);
}

.preview-options {
  margin-bottom: 1rem;
}

.preview-option {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.5rem 0;
  font-size: 1rem;
}

.preview-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
  color: var(--ion-color-medium);
}

.preview-info ion-icon {
  font-size: 1rem;
}

.action-section {
  padding: 1rem 0 2rem 0;
}

ion-item {
  --padding-start: 0;
  --padding-end: 0;
  margin-bottom: 1rem;
}

ion-input, ion-textarea {
  --padding-top: 12px;
  --padding-bottom: 12px;
}

/* Estilos del calendario */
.calendar-modal {
  padding: 1rem;
}

.calendar-actions {
  padding: 1rem;
  margin-top: 1rem;
}

/* Responsive */
@media (min-width: 768px) {
  .votacion-form {
    padding: 2rem;
  }
}

/* Estados de elementos */
ion-button[disabled] {
  opacity: 0.5;
}

.opcion-item ion-item {
  margin-bottom: 0;
}

/* Animaciones */
.opcion-item {
  transition: all 0.3s ease;
}

.preview-card {
  transition: all 0.3s ease;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>