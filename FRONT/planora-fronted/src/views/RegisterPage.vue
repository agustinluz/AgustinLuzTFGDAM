<template>
  <ion-page>
    <ion-content :fullscreen="true" class="ion-padding register-page">
      <div class="register-background">
        <div class="register-card">
          <img src="../assets/logoPlanora.png" alt="Logo" class="logo" />
          <h1 class="title">Crea tu cuenta</h1>
          <p class="subtitle">Únete a tu grupo y empieza a organizar</p>

          <ion-input
            v-model="nombre"
            label="Nombre"
            label-placement="floating"
            fill="outline"
            placeholder="Tu nombre"
            class="input"
          >
            <ion-icon name="person-outline" slot="start"></ion-icon>
          </ion-input>

          <ion-input
            v-model="email"
            label="Email"
            label-placement="floating"
            fill="outline"
            type="email"
            placeholder="tu@email.com"
            class="input"
          >
            <ion-icon name="mail-outline" slot="start"></ion-icon>
          </ion-input>

          <ion-input
            v-model="password"
            label="Contraseña"
            label-placement="floating"
            fill="outline"
            type="password"
            placeholder="••••••"
            class="input"
          >
            <ion-icon name="lock-closed-outline" slot="start"></ion-icon>
          </ion-input>

          <ion-button expand="block" class="register-button" @click="registrar">
            Registrarse
          </ion-button>

          <ion-button expand="block" fill="clear" class="login-link" @click="goToLogin">
            ¿Ya tienes cuenta? Inicia sesión
          </ion-button>

          <ion-text color="danger" v-if="error" class="error-text">
            {{ error }}
          </ion-text>
        </div>
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup>
import {
  IonPage,
  IonContent,
  IonInput,
  IonButton,
  IonText,
  IonIcon,
  useIonRouter
} from '@ionic/vue'
import { ref } from 'vue'

const nombre = ref('')
const email = ref('')
const password = ref('')
const error = ref('')
const router = useIonRouter()

const registrar = async () => {
  error.value = ''
  try {
    const response = await fetch(`${import.meta.env.VITE_API_URL}/auth/registro`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ nombre: nombre.value, email: email.value, password: password.value })
    })

    if (!response.ok) throw new Error('Error al registrar')

    const data = await response.json()
    localStorage.setItem('usuario', JSON.stringify(data))
    router.push('/grupo')
  } catch (err) {
    error.value = err.message
  }
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.register-page {
  --background: transparent;
}

.register-background {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(145deg, #f2f6fc, #e3ebf5);
}

.register-card {
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

.register-button {
  margin-top: 24px;
  --background: #3880ff;
  --border-radius: 8px;
}

.login-link {
  margin-top: 8px;
  font-size: 14px;
  text-transform: none;
}

.error-text {
  display: block;
  margin-top: 16px;
  font-weight: 500;
}
</style>
