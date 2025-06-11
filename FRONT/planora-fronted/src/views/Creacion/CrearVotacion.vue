<!-- src/views/CrearVotacionPage.vue -->
<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-buttons slot="start">
          <ion-back-button :default-href="'/dashboard/:grupoId/dashboard'" color="light" ></ion-back-button>
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
      <form @submit.prevent="crearVotacion" class="voting-form">
        <!-- Información básica -->
        <div class="form-section">
          <div class="section-header">
            <ion-icon name="information-circle-outline"></ion-icon>
            <h3>Información básica</h3>
          </div>

          <ion-item>
            <ion-input
              label="Título de la votación"
              label-placement="stacked"
              fill="outline"
              v-model="titulo"
              placeholder="¿Sobre qué se va a votar?"
              :maxlength="100"
              counter
              required
            ></ion-input>
          </ion-item>

          <ion-item class="textarea-item">
            <ion-textarea
              label="Descripción (opcional)"
              label-placement="stacked"
              fill="outline"
              v-model="descripcion"
              placeholder="Proporciona más detalles sobre la votación..."
              rows="3"
              auto-grow
              :maxlength="500"
              counter
            ></ion-textarea>
          </ion-item>
        </div>

        <!-- Opciones de votación -->
        <div class="form-section">
          <div class="section-header">
            <ion-icon name="list-outline"></ion-icon>
            <h3>Opciones de votación</h3>
          </div>

          <div class="opciones-container">
            <ion-item 
              v-for="(opcion, index) in opciones" 
              :key="index"
              class="opcion-item"
            >
              <ion-input
                :label="`Opción ${index + 1}`"
                label-placement="stacked"
                fill="outline"
                v-model="opciones[index]"
                :placeholder="`Ingresa la opción ${index + 1}`"
                :maxlength="100"
                required
              ></ion-input>
              <ion-button
                v-if="opciones.length > 2"
                fill="clear"
                color="danger"
                slot="end"
                @click="eliminarOpcion(index)"
                class="delete-option-btn"
              >
                <ion-icon name="trash-outline" slot="icon-only"></ion-icon>
              </ion-button>
            </ion-item>

            <ion-button
              v-if="opciones.length < 10"
              fill="outline"
              expand="block"
              @click="agregarOpcion"
              class="add-option-btn"
            >
              <ion-icon name="add-outline" slot="start"></ion-icon>
              Agregar opción
            </ion-button>
          </div>
        </div>

        <!-- Configuración -->
        <div class="form-section">
          <div class="section-header">
            <ion-icon name="calendar-outline"></ion-icon>
            <h3>Configuración</h3>
          </div>

          <ion-item button @click="mostrarFechaLimite = true">
            <ion-icon name="calendar-outline" slot="start"></ion-icon>
            <ion-label>
              <h3>Fecha límite (opcional)</h3>
              <p v-if="fechaLimite">{{ formatearFecha(fechaLimite) }}</p>
              <p v-else>Sin fecha límite</p>
            </ion-label>
            <ion-button 
              v-if="fechaLimite"
              fill="clear" 
              color="medium" 
              @click.stop="limpiarFechaLimite"
            >
              <ion-icon name="close-outline" slot="icon-only"></ion-icon>
            </ion-button>
          </ion-item>
        </div>

        <!-- Botón de crear -->
        <div class="action-section">
          <ion-button 
            expand="block" 
            type="submit"
            :disabled="!puedeGuardar || isLoading"
            size="large"
          >
            <ion-icon name="checkmark-circle-outline" slot="start"></ion-icon>
            {{ isLoading ? 'Creando...' : 'Crear votación' }}
          </ion-button>
        </div>
      </form>

      <!-- DateTime picker para fecha límite -->
      <ion-datetime
        :is-open="mostrarFechaLimite"
        v-model="fechaLimite"
        @did-dismiss="mostrarFechaLimite = false"
        presentation="date-time"
        :min="fechaMinima"
        locale="es-ES"
        :show-default-buttons="true"
        done-text="Confirmar"
        cancel-text="Cancelar"
      ></ion-datetime>

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
  IonItem, IonLabel, IonDatetime, IonToast, IonLoading
} from '@ionic/vue'
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

// Estado del formulario
const titulo = ref('')
const descripcion = ref('')
const opciones = ref(['', ''])
const fechaLimite = ref(null)
const mostrarFechaLimite = ref(false)
const isLoading = ref(false)

// Toast
const showToast = ref(false)
const toastMessage = ref('')
const toastColor = ref('success')

// Datos del grupo y usuario
const grupoId = ref(null)
const usuario = ref(null)

// Fecha mínima (ahora + 1 hora)
const fechaMinima = computed(() => {
  const now = new Date()
  now.setHours(now.getHours() + 1)
  return now.toISOString()
})

// Validación
const puedeGuardar = computed(() => {
  const tituloValido = titulo.value.trim().length > 0
  const opcionesValidas = opciones.value.filter(op => op.trim().length > 0).length >= 2
  return tituloValido && opcionesValidas
})

onMounted(async () => {
  try {
    // Obtener grupoId de la ruta
    grupoId.value = route.params.grupoId || route.params.id
    
    // Obtener usuario del localStorage
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

const limpiarFechaLimite = () => {
  fechaLimite.value = null
}

const formatearFecha = (fecha) => {
  if (!fecha) return ''
  
  const date = new Date(fecha)
  return date.toLocaleDateString('es-ES', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const crearVotacion = async () => {
  if (!puedeGuardar.value) {
    mostrarToast('Por favor, completa el título y al menos 2 opciones', 'warning')
    return
  }

  // Validar opciones
  const opcionesFiltradas = opciones.value.filter(op => op.trim().length > 0)
  if (opcionesFiltradas.length < 2) {
    mostrarToast('Debes tener al menos 2 opciones válidas', 'warning')
    return
  }

  // Validar fecha límite si existe
  if (fechaLimite.value) {
    const fechaSeleccionada = new Date(fechaLimite.value)
    const ahora = new Date()
    if (fechaSeleccionada <= ahora) {
      mostrarToast('La fecha límite debe ser futura', 'warning')
      return
    }
  }

  isLoading.value = true

  try {
    const token = localStorage.getItem('token')
    if (!token) {
      throw new Error('Token de autenticación no encontrado')
    }

    const payload = {
      pregunta: titulo.value.trim(),
      descripcion: descripcion.value.trim() || null,
      opciones: opcionesFiltradas.map(op => op.trim()),
      fechaLimite: fechaLimite.value || null
    }

    console.log('Payload a enviar:', payload)

    const response = await fetch(
      `${import.meta.env.VITE_API_URL}/grupos/${grupoId.value}/votaciones`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(payload)
      }
    )

    if (!response.ok) {
      const errorData = await response.text()
      throw new Error(errorData || `Error ${response.status}: ${response.statusText}`)
    }

    const votacionCreada = await response.json()
    console.log('Votación creada:', votacionCreada)

    mostrarToast('Votación creada correctamente', 'success')
    
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
.voting-form {
  padding: 16px;
}

.form-section {
  margin-bottom: 24px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  padding: 0 16px;
}

.section-header ion-icon {
  font-size: 20px;
  color: var(--ion-color-primary);
}

.section-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--ion-color-primary);
}

.textarea-item {
  --inner-padding-end: 16px;
}

.opciones-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.opcion-item {
  --inner-padding-end: 8px;
}

.delete-option-btn {
  --padding-start: 8px;
  --padding-end: 8px;
}

.add-option-btn {
  margin-top: 8px;
  --border-style: dashed;
}

.action-section {
  margin-top: 32px;
  padding: 0 16px;
}

ion-item {
  --background: transparent;
  --inner-padding-start: 16px;
  --inner-padding-end: 16px;
  margin-bottom: 8px;
}

ion-button[expand="block"] {
  margin: 8px 0;
}

/* Estilos para el datetime picker */
ion-datetime {
  --background: var(--ion-color-light);
}

/* Responsive */
@media (max-width: 768px) {
  .voting-form {
    padding: 12px;
  }
  
  .section-header {
    padding: 0 12px;
  }
  
  .action-section {
    padding: 0 12px;
  }
}
</style>