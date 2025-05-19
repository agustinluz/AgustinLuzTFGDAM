<template>
    <ion-page>
      <ion-content :fullscreen="true" class="ion-padding login-page">
        <div class="login-container">
          <img src="../assets/logo.jpg" alt="Logo" class="logo" />
          <h2 class="ion-text-center">Bienvenido a Planicom</h2>
          <p class="ion-text-center">Inicia sesión para continuar</p>
  
          <ion-input
            v-model="email"
            label="Email"
            label-placement="floating"
            type="email"
            fill="outline"
            placeholder="tu@email.com"
          ></ion-input>
  
          <ion-input
            v-model="password"
            label="Contraseña"
            label-placement="floating"
            type="password"
            fill="outline"
            placeholder="••••••"
            class="ion-margin-top"
          ></ion-input>
  
          <ion-button expand="block" class="ion-margin-top" @click="login">
            Entrar
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
  
  const email = ref('')
  const password = ref('')
  const error = ref('')
  const router = useIonRouter()
  
  const login = async () => {
    error.value = ''
    try {
      const response = await fetch(`${import.meta.env.VITE_API_URL}/grupos/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: email.value, password: password.value })
      })
  
      if (!response.ok) throw new Error('Credenciales incorrectas')
  
      const data = await response.json()
      localStorage.setItem('token', data.token)
      localStorage.setItem('usuario', JSON.stringify(data.usuario))
      router.push('/grupo')
    } catch (err) {
      error.value = err.message
    }
  }
  </script>
  
  <style scoped>
  .login-page {
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .login-container {
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
  