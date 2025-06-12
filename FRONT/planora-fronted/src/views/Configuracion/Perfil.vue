<template>
  <ion-page>
    <PageHeader title="Editar Perfil" :showMenu="false" />

    <ion-content :fullscreen="true" class="profile-content">
      <div class="profile-container">

        <!-- Sección de Avatar Principal -->
        <div class="avatar-section">
          <div class="avatar-wrapper" @click="ampliarImagen">
            <ion-avatar class="main-avatar">
              <img :src="formData.fotoPerfil || '/assets/default-avatar.png'" alt="Foto de perfil"
                @error="handleImageError" />
            </ion-avatar>
            <div class="avatar-overlay">
              <ion-icon :icon="cameraOutline" />
            </div>
          </div>
          <div class="upload-controls">
            <label class="upload-btn">
              <input type="file" accept="image/*" @change="onFileChange" />
              <ion-icon :icon="imageOutline" />
              Cambiar foto
            </label>
          </div>
        </div>

        <!-- Formulario de Información Personal -->
        <ion-card class="info-card">
          <ion-card-header>
            <ion-card-title class="section-title">
              <ion-icon :icon="personOutline" />
              Información Personal
            </ion-card-title>
          </ion-card-header>

          <ion-card-content v-if="usuarioCargado">
            <form @submit.prevent="actualizarPerfil" class="profile-form">

              <!-- Nombre -->
              <ion-item lines="none" class="input-field">
                <ion-label position="floating">Nombre completo</ion-label>
                <br>
                <ion-input v-model="formData.nombre" type="text" required></ion-input>
              </ion-item>

              <!-- Email -->
              <ion-item lines="none" class="input-field">
                <ion-label position="floating">Correo electrónico</ion-label>
                <br>
                <ion-input v-model="formData.email" type="email" required></ion-input>
              </ion-item>

            </form>
          </ion-card-content>

          <ion-card-content v-else class="loading-content">
            <div class="skeleton-group">
              <ion-skeleton-text animated style="width: 80%; height: 20px" />
              <ion-skeleton-text animated style="width: 60%; height: 20px; margin-top: 12px;" />
            </div>
          </ion-card-content>
        </ion-card>

        <!-- Sección de Seguridad -->
        <ion-card class="security-card">
          <ion-card-header>
            <ion-card-title class="section-title">
              <ion-icon :icon="lockClosedOutline" />
              Cambiar Contraseña
            </ion-card-title>
            
            <ion-card-subtitle class="section-subtitle">
              Deja los campos vacíos si no deseas cambiar tu contraseña
            </ion-card-subtitle>
          </ion-card-header>

          <ion-card-content v-if="usuarioCargado">
            <!-- Contraseña Actual -->
            <div class="input-group">
              <ion-item lines="none" class="input-field">
                <ion-label position="floating">Contraseña actual</ion-label>
                <br>
                <ion-input v-model="formData.currentPassword"
                  :type="showCurrentPassword ? 'text' : 'password'"></ion-input>
                <ion-button fill="clear" slot="end" @click="showCurrentPassword = !showCurrentPassword"
                  class="password-toggle">
                  <ion-icon :icon="showCurrentPassword ? eyeOffOutline : eyeOutline" />
                </ion-button>
              </ion-item>
            </div>

            <!-- Nueva Contraseña -->
            <div class="input-group">
              <ion-item lines="none" class="input-field">
                <ion-label position="floating">Nueva contraseña</ion-label>
                <br>
                <ion-input v-model="formData.password" :type="showNewPassword ? 'text' : 'password'"></ion-input>
                <ion-button fill="clear" slot="end" @click="showNewPassword = !showNewPassword" class="password-toggle">
                  <ion-icon :icon="showNewPassword ? eyeOffOutline : eyeOutline" />
                </ion-button>
              </ion-item>
            </div>

            <!-- Confirmar Contraseña -->
            <div class="input-group">
              <ion-item lines="none" class="input-field">
                <ion-label position="floating">Confirmar nueva contraseña</ion-label>
                <br>
                <ion-input v-model="confirmPassword" :type="showConfirmPassword ? 'text' : 'password'"></ion-input>
                <ion-button fill="clear" slot="end" @click="showConfirmPassword = !showConfirmPassword"
                  class="password-toggle">
                  <ion-icon :icon="showConfirmPassword ? eyeOffOutline : eyeOutline" />
                </ion-button>
              </ion-item>
            </div>
          </ion-card-content>
        </ion-card>

        <!-- Botones de Acción -->
        <div class="action-buttons">
          <ion-button expand="block" type="submit" :disabled="loading" color="primary" class="save-button"
            @click="actualizarPerfil">
            <ion-spinner v-if="loading" name="crescent" />
            <template v-else>
              <ion-icon :icon="saveOutline" slot="start" />
              Guardar Cambios
            </template>
          </ion-button>

          <ion-button expand="block" fill="outline" @click="cancelar" :disabled="loading" class="cancel-button">
            <ion-icon :icon="closeOutline" slot="start" />
            Cancelar
          </ion-button>
        </div>
      </div>

      <!-- Modal para ampliar imagen -->
      <ion-modal :is-open="showImageModal" @did-dismiss="cerrarModalImagen">
        <ion-header>
          <ion-toolbar>
            <ion-title>Foto de Perfil</ion-title>
            <ion-buttons slot="end">
              <ion-button @click="cerrarModalImagen">
                <ion-icon :icon="closeOutline" />
              </ion-button>
            </ion-buttons>
          </ion-toolbar>
        </ion-header>
        <ion-content class="modal-content">
          <div class="image-container">
            <img :src="formData.fotoPerfil || '/assets/default-avatar.png'" alt="Foto de perfil ampliada"
              class="enlarged-image" />
          </div>
        </ion-content>
      </ion-modal>
    </ion-content>
  </ion-page>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useIonRouter } from '@ionic/vue'
import {
  IonPage,
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
  IonIcon,
  IonSpinner,
  IonSkeletonText,
  IonAvatar,
  IonModal,
  IonHeader,
  IonToolbar,
  IonTitle,
  IonButtons,
  toastController,
  alertController
} from '@ionic/vue'
import PageHeader from '@/components/PageHeader.vue'
import {
  eyeOutline,
  eyeOffOutline,
  personOutline,
  personCircleOutline,
  mailOutline,
  lockClosedOutline,
  keyOutline,
  checkmarkCircleOutline,
  saveOutline,
  closeOutline,
  cameraOutline,
  imageOutline
} from 'ionicons/icons'

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
const showImageModal = ref(false)

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

  // Validar tamaño del archivo (máximo 5MB)
  if (file.size > 5 * 1024 * 1024) {
    mostrarError('La imagen debe ser menor a 5MB')
    return
  }

  const reader = new FileReader()
  reader.onload = () => {
    formData.fotoPerfil = reader.result
  }
  reader.readAsDataURL(file)
}

const handleImageError = (e) => {
  e.target.src = '/assets/default-avatar.png'
}

const ampliarImagen = () => {
  showImageModal.value = true
}

const cerrarModalImagen = () => {
  showImageModal.value = false
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
.profile-content {
  --background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.profile-container {
  padding: 20px 16px;
  min-height: 100vh;
}

/* Sección de Avatar */
.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 30px;
  position: relative;
}

.avatar-wrapper {
  position: relative;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.avatar-wrapper:hover {
  transform: scale(1.05);
}

.main-avatar {
  width: 120px;
  height: 120px;
  border: 4px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  margin-bottom: 16px;
}

.avatar-overlay {
  position: absolute;
  bottom: 16px;
  right: 8px;
  background: var(--ion-color-primary);
  border-radius: 50%;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.upload-controls {
  margin-top: 12px;
}

.upload-btn {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border: none;
  border-radius: 25px;
  padding: 12px 20px;
  color: var(--ion-color-primary);
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.upload-btn:hover {
  background: rgba(255, 255, 255, 1);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

.upload-btn input {
  display: none;
}

/* Cards */
.info-card,
.security-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  margin-bottom: 20px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 1.3rem;
  font-weight: 700;
  color: var(--ion-color-primary);
  margin-bottom: 8px;
}

.section-subtitle {
  color: var(--ion-color-medium);
  font-size: 0.9rem;
  margin-top: 4px;
}

/* Inputs */
.input-group {
  margin-bottom: 20px;
}

.custom-item {
  --background: rgba(248, 249, 250, 0.8);
  --border-radius: 15px;
  --padding-start: 16px;
  --padding-end: 16px;
  --min-height: 60px;
  margin-bottom: 4px;
  border: 1px solid rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.custom-item:hover {
  --background: rgba(248, 249, 250, 1);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.input-icon {
  color: var(--ion-color-primary);
  margin-right: 8px;
}

.custom-input {
  --padding-start: 0;
  --padding-end: 0;
}

.toggle-password {
  --color: var(--ion-color-medium);
  --padding: 8px;
}

/* Botones de Acción */
.action-buttons {
  margin-top: 40px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.save-button {
  --background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  --border-radius: 15px;
  --padding-top: 16px;
  --padding-bottom: 16px;
  font-weight: 600;
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.3);
  transition: all 0.3s ease;
}

.save-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

.cancel-button {
  --color: var(--ion-color-medium);
  --border-color: rgba(255, 255, 255, 0.3);
  --background: rgba(255, 255, 255, 0.1);
  --border-radius: 15px;
  --padding-top: 16px;
  --padding-bottom: 16px;
  font-weight: 600;
  backdrop-filter: blur(10px);
}

/* Modal */
.modal-content {
  --background: #000;
}

.image-container {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding: 20px;
}

.enlarged-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  border-radius: 10px;
}

/* Loading */
.loading-content {
  padding: 20px;
}

.skeleton-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* Responsive */
@media (max-width: 768px) {
  .profile-container {
    padding: 16px 12px;
  }

  .main-avatar {
    width: 100px;
    height: 100px;
  }

  .section-title {
    font-size: 1.2rem;
  }
}

/* Animaciones */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.info-card,
.security-card {
  animation: fadeIn 0.6s ease-out;
}

.avatar-section {
  animation: fadeIn 0.4s ease-out;
}
</style>