<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-buttons slot="start">
          <ion-back-button :default-href="backHref" @click="$emit('back')" color="light" />
        </ion-buttons>
        <ion-title>Nuevo evento</ion-title>
        <ion-buttons slot="end">
          <ion-button 
            @click="crearEvento" 
            :disabled="!puedeGuardar || isLoading"
            fill="clear"
          >
            <ion-icon name="checkmark" slot="icon-only"></ion-icon>
          </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>

    <ion-content>
      <form @submit.prevent="crearEvento" class="event-form">
        <!-- Información básica -->
        <BasicInfoSection v-model:titulo="titulo" v-model:descripcion="descripcion" />

        <!-- Fecha y hora -->
        <DateTimeSection v-model:fecha="fecha" />

        <!-- Ubicación -->
        <LocationSection
          v-model:ubicacion="ubicacion"
          v-model:coordenadas="coordenadas"
        />

        <!-- Botón de guardar -->
        <div class="action-section">
          <ion-button 
            expand="block" 
            type="submit"
            :disabled="!puedeGuardar || isLoading"
            size="large"
          >
            <ion-icon name="calendar-outline" slot="start"></ion-icon>
            {{ isLoading ? 'Creando evento...' : 'Crear evento' }}
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
        message="Creando evento..."
      ></ion-loading>
    </ion-content>
  </ion-page>
</template>

<script setup>
import {
  IonPage, IonHeader, IonToolbar, IonTitle, IonContent,
  IonButton, IonIcon, IonButtons, IonBackButton,
  IonToast, IonLoading
} from '@ionic/vue'
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BasicInfoSection from '@/views/Components/CrearEvento/BasicInfoSection.vue'
import DateTimeSection from '@/views/Components/CrearEvento/DateTimeSection.vue'
import LocationSection from '@/views/Components/CrearEvento/LocationSection.vue'
import { EventosService } from '@/service/EventoService'

const route = useRoute()
const router = useRouter()

// Estado del formulario
const titulo = ref('')
const descripcion = ref('')
const ubicacion = ref('')
const fecha = ref('')
const coordenadas = ref(null)
const isLoading = ref(false)

// Toast
const showToast = ref(false)
const toastMessage = ref('')
const toastColor = ref('success')

// Datos del grupo
const grupoId = ref(null)
const usuario = ref(null)

// Validación
const puedeGuardar = computed(() => {
  return titulo.value.trim().length > 0 && fecha.value && ubicacion.value.trim().length > 0
})

onMounted(async () => {
  try {
    grupoId.value = route.params.grupoId || route.params.id
    
    const usuarioString = localStorage.getItem('usuario')
    if (!usuarioString) {
      throw new Error('Usuario no autenticado')
    }
    
    usuario.value = JSON.parse(usuarioString)

     const preFecha = route.query.fecha
    if (preFecha) {
      fecha.value = preFecha
    }
    
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
const crearEvento = async () => {
  // Validación de campos obligatorios.
  if (!puedeGuardar.value) {
    mostrarToast('Por favor, completa todos los campos obligatorios', 'warning')
    return
  }

  isLoading.value = true

  try {
    // Se obtiene el token y se verifica.
    const token = localStorage.getItem('token')
    if (!token) {
      throw new Error('Token de autenticación no encontrado')
    }

    // Construimos el objeto eventData. 
    // Nota: El uso de new Date(fecha.value).toISOString() asegura que la fecha se envía en formato ISO 8601.
    const eventData = {
      titulo: titulo.value.trim(),
      descripcion: descripcion.value.trim(),
      ubicacion: ubicacion.value.trim(), // Verifica que en el formulario se llene correctamente con el formato esperado (ej: "lat, lng" o una dirección).
      fecha: new Date(fecha.value).toISOString()
    }

    // Realiza el POST para crear el evento.
    const eventoCreado = await EventosService.crearEvento(grupoId.value, eventData, token)

    console.log('Evento creado:', eventoCreado)

    mostrarToast('Evento creado correctamente', 'success')

    // Limpiar formulario
    titulo.value = ''
    descripcion.value = ''
    ubicacion.value = ''
    fecha.value = ''
    coordenadas.value = null


    setTimeout(() => {
      router.back()
    }, 1500)

  } catch (error) {
    console.error('Error al crear evento:', error)
    mostrarToast(
      error.message || 'Error al crear el evento. Inténtalo de nuevo.',
      'danger'
    )
  } finally {
    isLoading.value = false
  }
}

</script>

<style scoped>
.event-form {
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

.textarea-item {
  margin-top: 1rem;
}

.placeholder-text {
  color: var(--ion-color-medium);
  font-style: italic;
}

.action-section {
  padding: 1rem 0 2rem 0;
}



/* Responsive */
@media (min-width: 768px) {
  .event-form {
    padding: 2rem;
  }
}
</style>
