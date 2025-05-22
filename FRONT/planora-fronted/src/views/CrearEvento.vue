<!-- src/views/CrearEventoPage.vue -->
<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-title>Nuevo evento</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding">
      <ion-input label="Título" label-placement="floating" fill="outline" v-model="titulo"></ion-input>
      <ion-input label="Descripción" label-placement="floating" fill="outline" class="ion-margin-top" v-model="descripcion"></ion-input>
      <ion-input label="Ubicación" label-placement="floating" fill="outline" class="ion-margin-top" v-model="ubicacion"></ion-input>
      <ion-datetime-button class="ion-margin-top" datetime="fechaEvento"></ion-datetime-button>
      <ion-modal keep-contents-mounted>
        <ion-datetime id="fechaEvento" v-model="fecha" presentation="date-time"></ion-datetime>
      </ion-modal>

      <ion-button expand="block" class="ion-margin-top" @click="crearEvento">Guardar evento</ion-button>
      <ion-text color="success" v-if="mensaje">{{ mensaje }}</ion-text>
      <ion-text color="danger" v-if="error">{{ error }}</ion-text>
    </ion-content>
  </ion-page>
</template>

<script setup>
import {
  IonPage, IonHeader, IonToolbar, IonTitle, IonContent, IonInput,
  IonButton, IonText, IonDatetime, IonDatetimeButton, IonModal
} from '@ionic/vue'
import { ref } from 'vue'

const titulo = ref('')
const descripcion = ref('')
const ubicacion = ref('')
const fecha = ref('')
const mensaje = ref('')
const error = ref('')

const crearEvento = async () => {
  mensaje.value = ''
  error.value = ''
  try {
    const usuario = JSON.parse(localStorage.getItem('usuario'))
    const grupoId = usuario.grupos[0]?.id // Ajusta si gestionas múltiples grupos

    const res = await fetch(`${import.meta.env.VITE_API_URL}/grupos/${grupoId}/eventos`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        titulo: titulo.value,
        descripcion: descripcion.value,
        ubicacion: ubicacion.value,
        fecha: fecha.value
      })
    })

    if (!res.ok) throw new Error('Error al crear evento')
    mensaje.value = 'Evento creado correctamente'
  } catch (err) {
    error.value = err.message
  }
}
</script>


