<template>
  <ion-page>
    <PageHeader title="Restablecer Contraseña" :showMenu="false" />
    <ion-content :fullscreen="true" class="ion-padding reset-page">
      <div class="reset-background">
        <div class="reset-card">
          <img src="../assets/logoPlanora.png" alt="Logo" class="logo" />
          <h1 class="title">Recuperar acceso</h1>
          <p class="subtitle">Ingresa tu email y una nueva contraseña</p>

          <ion-input
            v-model="email"
            label="Email"
            label-placement="floating"
            type="email"
            fill="outline"
            placeholder="tu@email.com"
            class="input"
          >
            <ion-icon name="mail-outline" slot="start"></ion-icon>
          </ion-input>

          <ion-input
            v-model="password"
            label="Nueva contraseña"
            label-placement="floating"
            :type="showPassword ? 'text' : 'password'"
            fill="outline"
            placeholder="••••••"
            class="input"
          >
            <ion-icon name="lock-closed-outline" slot="start"></ion-icon>
            <ion-icon
              :name="showPassword ? 'eye-off-outline' : 'eye-outline'"
              slot="end"
              class="toggle-password"
              @click="toggleShowPassword"
            ></ion-icon>
          </ion-input>

          <ion-button expand="block" class="reset-button" @click="reset">
            Restablecer contraseña
          </ion-button>

          <ion-button expand="block" fill="clear" class="login-link" @click="goToLogin">
            Volver al inicio de sesión
          </ion-button>

          <ion-text color="danger" v-if="error" class="error-text">{{ error }}</ion-text>
          <ion-text color="success" v-if="mensaje" class="success-text">{{ mensaje }}</ion-text>
        </div>
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import {
  IonPage,
  IonContent,
  IonInput,
  IonButton,
  IonText,
  IonIcon,
  useIonRouter,
  toastController
} from '@ionic/vue'
import PageHeader from '../components/PageHeader.vue'
import { ref } from 'vue'
import { useAuthStore } from '../service/auth'

const email = ref('')
const password = ref('')
const error = ref('')
const mensaje = ref('')
const router = useIonRouter()
const auth = useAuthStore()
const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/
const showPassword = ref(false)
const toggleShowPassword = () => {
  showPassword.value = !showPassword.value
}


const showToast = async (msg: string, color: 'success' | 'danger' | 'warning' = 'danger') => {
  const toast = await toastController.create({
    message: msg,
    duration: 3000,
    position: 'bottom',
    color
  })
  await toast.present()
}

const reset = async () => {
  error.value = ''
  mensaje.value = ''
  if (!passwordRegex.test(password.value)) {
    await showToast('La contraseña debe tener al menos 8 caracteres, una mayúscula y un número', 'warning')
    return
  }
  try {
    const result = await auth.resetPassword(email.value, password.value)
    if (result.success) {
      mensaje.value = 'Contraseña actualizada'
      await showToast('Contraseña actualizada', 'success')
      router.push('/login')
    } else {
      await showToast(result.message, 'danger')
      throw new Error(result.message)
    }
  } catch (err: any) {
    error.value = err.message
  }
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.reset-page {
  --background: transparent;
}

.reset-background {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(145deg, #f2f6fc, #e3ebf5);
}

.reset-card {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
  padding: 32px 24px;
  width: 100%;
  max-width: 380px;
  text-align: center;
}

.logo {
  width: 100px;
  margin: 0 auto 20px;
}

.title {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 4px;
}

.subtitle {
  color: #6b7280;
  margin-bottom: 24px;
  font-size: 14px;
}

.input {
  margin-top: 16px;
}

.reset-button {
  margin-top: 24px;
  --background: #3880ff;
  --border-radius: 8px;
}

.login-link {
  margin-top: 12px;
  color: #3880ff;
  font-size: 15px;
}

.error-text, .success-text {
  display: block;
  margin-top: 16px;
  font-weight: 500;
}
.toggle-password {
  cursor: pointer;
}
</style>