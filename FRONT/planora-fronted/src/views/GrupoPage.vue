<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-title>Mi Grupo</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding">
      <ion-card v-if="grupos.length > 0" v-for="grupo in grupos" :key="grupo.id">
        <ion-card-header>
          <ion-card-title>{{ grupo.nombre }}</ion-card-title>
          <ion-card-subtitle>Código: {{ grupo.codigoInvitacion }}</ion-card-subtitle>
        </ion-card-header>
        <ion-card-content>
          <ion-button @click="entrarEnGrupo(grupo.id)">Entrar</ion-button>
        </ion-card-content>
      </ion-card>

      <div v-else>
        <ion-text>
          <h2 class="ion-text-center">¡Aún no perteneces a ningún grupo!</h2>
        </ion-text>

        <ion-button expand="block" class="ion-margin-top" @click="crearGrupo">
          Crear nuevo grupo
        </ion-button>

        <ion-item class="ion-margin-top">
          <ion-input v-model="codigo" label="Código de invitación" placeholder="Introduce el código"
            fill="outline"></ion-input>
        </ion-item>

        <ion-button expand="block" class="ion-margin-top" @click="unirseGrupo">
          Unirse a un grupo existente
        </ion-button>

        <ion-text color="danger" v-if="error">{{ error }}</ion-text>
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup>
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
  IonInput,
  IonItem,
  IonText,
  IonButton
} from '@ionic/vue'
import { ref, onMounted } from 'vue'

const usuario = ref(null)
const grupo = ref(null)
const codigo = ref('')
const error = ref('')
const grupos = ref([])


onMounted(async () => {
  const stored = localStorage.getItem('usuario')
  if (stored) {
    usuario.value = JSON.parse(stored)

    // Petición al backend para obtener grupos por ID de usuario
    const res = await fetch(`${import.meta.env.VITE_API_URL}/usuarios/${usuario.value.id}/grupos`)
    if (res.ok) {
      const data = await res.json()
      grupos.value = data
    } else {
      grupos.value = []
    }
  }
})


const crearGrupo = async () => {
  try {
    const response = await fetch(`${import.meta.env.VITE_API_URL}/grupos`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ nombre: 'Nuevo Grupo' })
    })

    const grupoCreado = await response.json()

    const resUsuario = await fetch(`${import.meta.env.VITE_API_URL}/grupos/${grupoCreado.id}/usuarios`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        nombre: usuario.value.nombre,
        email: usuario.value.email,
        password: usuario.value.password || '1234'
      })
    })

    const updatedUser = await resUsuario.json()
    updatedUser.grupo = grupoCreado
    localStorage.setItem('usuario', JSON.stringify(updatedUser))
    grupo.value = grupoCreado
  } catch (err) {
    error.value = 'Error al crear el grupo.'
  }
}

const unirseGrupo = async () => {
  try {
    const resGrupo = await fetch(`${import.meta.env.VITE_API_URL}/grupos/invitacion/${codigo.value}`)
    if (!resGrupo.ok) throw new Error('Código no válido')

    const grupoEncontrado = await resGrupo.json()

    const resUsuario = await fetch(`${import.meta.env.VITE_API_URL}/grupos/${grupoEncontrado.id}/usuarios`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        nombre: usuario.value.nombre,
        email: usuario.value.email,
        password: usuario.value.password || '1234'
      })
    })

    const updatedUser = await resUsuario.json()
    updatedUser.grupo = grupoEncontrado
    localStorage.setItem('usuario', JSON.stringify(updatedUser))
    grupo.value = grupoEncontrado
  } catch (err) {
    error.value = 'No se pudo unir al grupo.'
  }
}

const entrarEnGrupo = (id) => {
  // Aquí podrías guardar el grupo seleccionado en localStorage
  // y redirigir al dashboard de grupo
  localStorage.setItem('grupoSeleccionadoId', id)
  router.push('/dashboard')
}

</script>