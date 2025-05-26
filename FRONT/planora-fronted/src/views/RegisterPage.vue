<template>
  <ion-page>
    <ion-content :fullscreen="true" class="ion-padding register-page">
      <div class="register-container">
        <img src="../assets/logo.jpg" alt="Logo" class="logo" />
        <h2 class="ion-text-center">Crea tu cuenta</h2>

        <ion-input
          v-model="nombre"
          label="Nombre"
          label-placement="floating"
          fill="outline"
          placeholder="Tu nombre"
        ></ion-input>

        <ion-input
          v-model="email"
          label="Email"
          label-placement="floating"
          fill="outline"
          type="email"
          placeholder="tu@email.com"
          class="ion-margin-top"
        ></ion-input>

        <ion-input
          v-model="password"
          label="Contraseña"
          label-placement="floating"
          fill="outline"
          type="password"
          placeholder="••••••"
          class="ion-margin-top"
        ></ion-input>

        <ion-button expand="block" class="ion-margin-top" @click="registrar">
          Registrarse
        </ion-button>

        <ion-text color="danger" v-if="error">{{ error }}</ion-text>
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
</script>

<style scoped>
.register-page {
  display: flex;
  align-items: center;
  justify-content: center;
}

.register-container {
  width: 100%;
  max-width: 400px;
  margin: auto;
}

.logo {
  display: block;
  margin: 0 auto 20px;
  width: 120px;
}
</style>
