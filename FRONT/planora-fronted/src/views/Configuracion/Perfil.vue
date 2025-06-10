<template>
  <ion-page>
    <ion-header :translucent="true">
      <ion-toolbar>
        <ion-buttons slot="start">
          <ion-back-button default-href="/grupo"></ion-back-button>
        </ion-buttons>
        <ion-title>Editar Perfil</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content :fullscreen="true">
      <div class="container">
        <ion-card>
          <ion-card-header>
            <ion-card-title>Información Personal</ion-card-title>
            <ion-card-subtitle>Actualiza tus datos de usuario</ion-card-subtitle>
          </ion-card-header>

          <ion-card-content v-if="usuarioCargado">
            <form @submit.prevent="actualizarPerfil">
              <!-- Nombre -->
              <ion-item>
                <ion-label position="stacked">Nombre</ion-label>
                <ion-input v-model="formData.nombre" placeholder="Ingresa tu nombre" type="text" required></ion-input>
              </ion-item>

              <!-- Email -->
              <ion-item>
                <ion-label position="stacked">Correo Electrónico</ion-label>
                <ion-input v-model="formData.email" placeholder="correo@ejemplo.com" type="email" required></ion-input>
              </ion-item>

              <ion-item>
                <ion-label position="stacked">Foto de perfil</ion-label>
                <input type="file" accept="image/*" @change="onFileChange" />
              </ion-item>
              <div v-if="formData.fotoPerfil" class="avatar-preview">
                <ion-avatar>
                  <img :src="formData.fotoPerfil" alt="Previsualización" />
                </ion-avatar>
              </div>

              <!-- Separador -->
              <div class="password-section">
                <h3>Cambiar Contraseña</h3>
                <p class="subtitle">Deja en blanco si no quieres cambiar la contraseña</p>

                <!-- Contraseña Actual -->
                <ion-item>
                  <ion-label position="stacked">Contraseña Actual</ion-label>
                  <ion-input v-model="formData.currentPassword" placeholder="Contraseña actual"
                    :type="showCurrentPassword ? 'text' : 'password'"></ion-input>
                  <ion-button fill="clear" slot="end" @click="showCurrentPassword = !showCurrentPassword">
                    <ion-icon :icon="showCurrentPassword ? eyeOffOutline : eyeOutline"></ion-icon>
                  </ion-button>
                </ion-item>

                <!-- Nueva Contraseña -->
                <ion-item>
                  <ion-label position="stacked">Nueva Contraseña</ion-label>
                  <ion-input v-model="formData.password" placeholder="Nueva contraseña"
                    :type="showNewPassword ? 'text' : 'password'"></ion-input>
                  <ion-button fill="clear" slot="end" @click="showNewPassword = !showNewPassword">
                    <ion-icon :icon="showNewPassword ? eyeOffOutline : eyeOutline"></ion-icon>
                  </ion-button>
                </ion-item>

                <!-- Confirmar Nueva Contraseña -->
                <ion-item>
                  <ion-label position="stacked">Confirmar Nueva Contraseña</ion-label>
                  <ion-input v-model="confirmPassword" placeholder="Confirma la nueva contraseña"
                    :type="showConfirmPassword ? 'text' : 'password'"></ion-input>
                  <ion-button fill="clear" slot="end" @click="showConfirmPassword = !showConfirmPassword">
                    <ion-icon :icon="showConfirmPassword ? eyeOffOutline : eyeOutline"></ion-icon>
                  </ion-button>
                </ion-item>
              </div>

              <!-- Botones -->
              <div class="button-container">
                <ion-button expand="block" type="submit" :disabled="loading" color="primary">
                  <ion-spinner v-if="loading" name="crescent"></ion-spinner>
                  <span v-else>Guardar Cambios</span>
                </ion-button>

                <ion-button expand="block" fill="outline" @click="cancelar" :disabled="loading">
                  Cancelar
                </ion-button>
              </div>
            </form>
          </ion-card-content>

          <ion-card-content v-else>
            <ion-item>
              <ion-label>
                <ion-skeleton-text animated style="width: 80%"></ion-skeleton-text>
              </ion-label>
            </ion-item>
            <ion-item>
              <ion-label>
                <ion-skeleton-text animated style="width: 60%"></ion-skeleton-text>
              </ion-label>
            </ion-item>
          </ion-card-content>
        </ion-card>
      </div>
    </ion-content>

  </ion-page>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useIonRouter } from '@ionic/vue'
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
  IonItem,
  IonLabel,
  IonInput,
  IonButton,
  IonButtons,
  IonBackButton,
  IonIcon,
  IonSpinner,
  IonSkeletonText,
  toastController,
  alertController,
  IonAvatar,
} from '@ionic/vue'
import { eyeOutline, eyeOffOutline } from 'ionicons/icons'

const router = useIonRouter()

// Estado del formulario
const formData = reactive({
  nombre: '',
  email: '',
  fotoPerfil: '',
  currentPassword: '',
  password: ''
  
})

const confirmPassword = ref('')
const loading = ref(false)

// Control de visibilidad de contraseñas
const showCurrentPassword = ref(false)
const showNewPassword = ref(false)
const showConfirmPassword = ref(false)
const usuarioCargado = ref(false)

// Cargar datos del usuario al montar el componente
onMounted(async () => {
  await cargarDatosUsuario()
})

const cargarDatosUsuario = async () => {
  try {
    const usuario = JSON.parse(localStorage.getItem('usuario'))
    const token = localStorage.getItem('token')
    
    const usuarioId = usuario?.id
    
    if (!usuarioId) {
      mostrarError('No se pudo obtener la información del usuario')
      router.push('/login')
      return
    }

    if (!token) {
      mostrarError('Sesión expirada')
      router.push('/login')
      return
    }

    const url = `${import.meta.env.VITE_API_URL}/usuarios/${usuarioId}`

    const response = await fetch(url, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      }
    })
    
    if (response.ok) {
      const usuarioData = await response.json()
      
      formData.nombre = usuarioData.nombre
      formData.email = usuarioData.email
      formData.fotoPerfil = usuarioData.fotoPerfil || ''
      usuarioCargado.value = true
    } else if (response.status === 401) {
      mostrarError('Sesión expirada')
      router.push('/login')
    } else {
      mostrarError(`Error al cargar los datos del usuario`)
      usuarioCargado.value = true
    }
  } catch (error) {
    mostrarError('Error de conexión al cargar los datos')
    usuarioCargado.value = true
  }
}
const onFileChange = async (e) => {
  const file = e.target.files[0]
  if (!file) return
  const reader = new FileReader()
  reader.onload = () => {
    formData.fotoPerfil = reader.result
  }
  reader.readAsDataURL(file)
}


const validarFormulario = () => {
  // Validar nombre
  if (!formData.nombre.trim()) {
    mostrarError('El nombre es requerido')
    return false
  }

  // Validar email
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!formData.email.trim()) {
    mostrarError('El email es requerido')
    return false
  } else if (!emailRegex.test(formData.email)) {
    mostrarError('El formato del email no es válido')
    return false
  }

  // Validar contraseñas si se están cambiando
  if (formData.password) {
    if (!formData.currentPassword) {
      mostrarError('Debes ingresar tu contraseña actual')
      return false
    }

    if (formData.password.length < 6) {
      mostrarError('La contraseña debe tener al menos 6 caracteres')
      return false
    }

    if (formData.password !== confirmPassword.value) {
      mostrarError('Las contraseñas no coinciden')
      return false
    }
  }

  return true
}

const actualizarPerfil = async () => {
  if (!validarFormulario()) {
    return
  }

  loading.value = true

  try {
    const usuario = JSON.parse(localStorage.getItem('usuario'))
    const token = localStorage.getItem('token')
    const usuarioId = usuario?.id

    if (!usuarioId) {
      mostrarError('No se pudo obtener la información del usuario')
      return
    }

    if (!token) {
      mostrarError('Sesión expirada')
      router.push('/login')
      return
    }

    // Preparar datos para enviar
    const datosActualizacion = {
    nombre: formData.nombre.trim(),
    email: formData.email.trim(),
    fotoPerfil: formData.fotoPerfil,
    ...(formData.password && {
      currentPassword: formData.currentPassword,
      password: formData.password
    })
  }

    const url = `${import.meta.env.VITE_API_URL}/usuarios/${usuarioId}`

    const response = await fetch(url, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
        'Accept': 'application/json'
      },
      body: JSON.stringify(datosActualizacion)
    })

    if (response.ok) {
      const usuarioActualizado = await response.json()

      // Actualizar datos en localStorage
      const usuarioLocal = JSON.parse(localStorage.getItem('usuario'))
      usuarioLocal.nombre = usuarioActualizado.nombre
      usuarioLocal.email = usuarioActualizado.email
      usuarioLocal.fotoPerfil = usuarioActualizado.fotoPerfil
      localStorage.setItem('usuario', JSON.stringify(usuarioLocal))

      mostrarExito('Perfil actualizado correctamente')
      router.back()
    } else if (response.status === 401) {
      mostrarError('Sesión expirada')
      router.push('/login')
    } else {
      const errorText = await response.text()
      
      try {
        const errorData = JSON.parse(errorText)
        mostrarError(errorData.mensaje || 'Error al actualizar el perfil')
      } catch {
        mostrarError('Error al actualizar el perfil')
      }
    }

  } catch (error) {
    mostrarError('Error de conexión al actualizar el perfil')
  } finally {
    loading.value = false
  }
}

const cancelar = async () => {
  const alert = await alertController.create({
    header: 'Confirmar',
    message: '¿Estás seguro de que quieres cancelar? Se perderán los cambios no guardados.',
    buttons: [
      {
        text: 'No',
        role: 'cancel'
      },
      {
        text: 'Sí, cancelar',
        handler: () => {
          router.back()
        }
      }
    ]
  })

  await alert.present()
}

const mostrarExito = async (mensaje) => {
  const toast = await toastController.create({
    message: mensaje,
    duration: 3000,
    position: 'top',
    color: 'success'
  })
  await toast.present()
}

const mostrarError = async (mensaje) => {
  const toast = await toastController.create({
    message: mensaje,
    duration: 4000,
    position: 'top',
    color: 'danger'
  })
  await toast.present()
}
</script>

<style scoped>
.container {
  padding: 16px;
}

.password-section {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid var(--ion-color-light);
}

.password-section h3 {
  margin: 0 0 8px 0;
  color: var(--ion-color-dark);
  font-size: 1.2em;
  font-weight: 600;
}

.subtitle {
  margin: 0 0 16px 0;
  color: var(--ion-color-medium);
  font-size: 0.9em;
}

.button-container {
  margin-top: 32px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

ion-item {
  margin-bottom: 16px;
}

ion-card {
  margin: 0;
}
.avatar-preview {
  display: flex;
  justify-content: center;
  margin-top: 8px;
}
</style>