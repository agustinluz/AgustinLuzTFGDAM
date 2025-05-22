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
import { useRouter } from 'vue-router'

const router = useRouter()
const usuario = ref(null)
const grupo = ref(null)
const codigo = ref('')
const error = ref('')
const grupos = ref([])

onMounted(async () => {
  const stored = localStorage.getItem('usuario')
  if (stored) {
    usuario.value = JSON.parse(stored)
    await cargarGrupos()
  }
})

const cargarGrupos = async () => {
  if (!usuario.value) return
  
  try {
    // Petición al backend para obtener grupos por ID de usuario
    const res = await fetch(`${import.meta.env.VITE_API_URL}/usuarios/${usuario.value.id}/grupos`)
    if (res.ok) {
      const data = await res.json()
      grupos.value = data
    } else {
      grupos.value = []
    }
  } catch (err) {
    console.error('Error al cargar grupos:', err)
    grupos.value = []
  }
}
const crearGrupo = async () => {
  try {
    // 1. Crear grupo
    const response = await fetch(`${import.meta.env.VITE_API_URL}/grupos`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ nombre: 'Nuevo Grupo' })
    })

    const grupoCreado = await response.json()

    // 2. Asociar usuario al grupo
    await fetch(`${import.meta.env.VITE_API_URL}/grupos/${grupoCreado.id}/usuarios`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        nombre: usuario.value.nombre,
        email: usuario.value.email,
        password: usuario.value.password || '1234'
      })
    })

    // 3. Volver a consultar los grupos del usuario
    const gruposActualizadosRes = await fetch(`${import.meta.env.VITE_API_URL}/usuarios/${usuario.value.id}/grupos`)
    const gruposActualizados = await gruposActualizadosRes.json()

    // 4. Actualizar usuario local
    const updatedUser = {
      ...usuario.value,
      grupos: gruposActualizados // esta clave la usas en dashboard
    }

    localStorage.setItem('usuario', JSON.stringify(updatedUser))
    grupos.value = gruposActualizados

    // Redirigir al grupo recién creado
    router.push(`/dashboard/${grupoCreado.id}`)
  } catch (err) {
    error.value = 'Error al crear el grupo.'
  }
}
const unirseGrupo = async () => {
  try {
    // 1. Buscar grupo por código
    const resGrupo = await fetch(`${import.meta.env.VITE_API_URL}/grupos/invitacion/${codigo.value}`)
    if (!resGrupo.ok) throw new Error('Código no válido')

    const grupoEncontrado = await resGrupo.json()

    // 2. Asociar usuario al grupo
    await fetch(`${import.meta.env.VITE_API_URL}/grupos/${grupoEncontrado.id}/usuarios`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        nombre: usuario.value.nombre,
        email: usuario.value.email,
        password: usuario.value.password || '1234'
      })
    })

    // 3. Obtener lista actualizada de grupos
    const resGrupos = await fetch(`${import.meta.env.VITE_API_URL}/usuarios/${usuario.value.id}/grupos`)
    const gruposActualizados = await resGrupos.json()

    // 4. Actualizar localStorage
    const updatedUser = {
      ...usuario.value,
      grupos: gruposActualizados
    }

    localStorage.setItem('usuario', JSON.stringify(updatedUser))
    grupos.value = gruposActualizados

    // Redirigir al dashboard
    router.push(`/dashboard/${grupoEncontrado.id}`)
  } catch (err) {
    error.value = 'No se pudo unir al grupo.'
  }
}


const entrarEnGrupo = (id) => {
  // Navegar al dashboard del grupo seleccionado
  router.push(`/dashboard/${id}`)
}
</script>