<!-- src/views/CrearNotaPage.vue -->
<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-buttons slot="start">
          <ion-back-button></ion-back-button>
        </ion-buttons>
        <ion-title>Nueva nota</ion-title>
        <ion-buttons slot="end">
          <ion-button 
            @click="guardarNota" 
            :disabled="!puedeGuardar || isLoading"
            fill="clear"
          >
            <ion-icon name="checkmark" slot="icon-only"></ion-icon>
          </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>

    <ion-content>
      <form @submit.prevent="guardarNota" class="note-form">
        <div class="form-section">
          <ion-item>
            <ion-input
              label="Título de la nota"
              label-placement="stacked"
              fill="outline"
              v-model="titulo"
              placeholder="Ingresa un título descriptivo"
              :maxlength="100"
              counter
              required
            ></ion-input>
          </ion-item>

          <ion-item class="textarea-item">
            <ion-textarea
              label="Contenido"
              label-placement="stacked"
              fill="outline"
              v-model="contenido"
              placeholder="Escribe el contenido de tu nota..."
              rows="10"
              auto-grow
              :maxlength="2000"
              counter
              required
            ></ion-textarea>
          </ion-item>
        </div>

        <div class="action-section">
          <ion-button 
            expand="block" 
            type="submit"
            :disabled="!puedeGuardar || isLoading"
          >
            <ion-icon name="save-outline" slot="start"></ion-icon>
            {{ isLoading ? 'Guardando...' : 'Guardar nota' }}
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
        message="Guardando nota..."
      ></ion-loading>
    </ion-content>
  </ion-page>
</template>

<script setup>
import {
  IonPage, IonHeader, IonToolbar, IonTitle, IonContent, IonInput, 
  IonTextarea, IonButton, IonIcon, IonButtons, IonBackButton, 
  IonItem, IonToast, IonLoading
} from '@ionic/vue'
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

// Estado del formulario
const titulo = ref('')
const contenido = ref('')
const isLoading = ref(false)

// Toast
const showToast = ref(false)
const toastMessage = ref('')
const toastColor = ref('success')

// Datos del grupo y usuario
const grupoId = ref(null)
const usuario = ref(null)

// Validación
const puedeGuardar = computed(() => {
  return titulo.value.trim().length > 0 && contenido.value.trim().length > 0
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

const guardarNota = async () => {
  if (!puedeGuardar.value) {
    mostrarToast('Por favor, completa todos los campos', 'warning')
    return
  }

  isLoading.value = true

  try {
    const token = localStorage.getItem('token')
    if (!token) {
      throw new Error('Token de autenticación no encontrado')
    }

    const response = await fetch(
      `${import.meta.env.VITE_API_URL}/nota/${grupoId.value}/nota`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
          titulo: titulo.value.trim(),
          contenido: contenido.value.trim()
        })
      }
    )

    if (!response.ok) {
      const errorData = await response.text()
      throw new Error(errorData || `Error ${response.status}: ${response.statusText}`)
    }

    const notaCreada = await response.json()
    console.log('Nota creada:', notaCreada)

    mostrarToast('Nota guardada correctamente', 'success')
    
    // Limpiar formulario
    titulo.value = ''
    contenido.value = ''
    
    // Navegar de vuelta después de un delay
    setTimeout(() => {
      router.back()
    }, 1500)

  } catch (error) {
    console.error('Error al guardar nota:', error)
    mostrarToast(
      error.message || 'Error al guardar la nota. Inténtalo de nuevo.',
      'danger'
    )
  } finally {
    isLoading.value = false
  }
}
</script>

