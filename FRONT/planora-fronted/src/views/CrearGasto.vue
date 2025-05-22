<!-- src/views/CrearGastoPage.vue -->
<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-title>Nuevo gasto</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding">
      <ion-input label="Título" fill="outline" v-model="titulo"></ion-input>
      <ion-input label="Monto total (€)" fill="outline" type="number" v-model="monto" class="ion-margin-top"></ion-input>

      <ion-item class="ion-margin-top">
        <ion-label>Pagado por</ion-label>
        <ion-select v-model="pagadoPorId" interface="popover">
          <ion-select-option v-for="u in usuarios" :key="u.id" :value="u.id">
            {{ u.nombre }}
          </ion-select-option>
        </ion-select>
      </ion-item>

      <ion-item class="ion-margin-top">
        <ion-label>Participantes</ion-label>
        <ion-select v-model="participantesIds" multiple interface="popover">
          <ion-select-option v-for="u in usuarios" :key="u.id" :value="u.id">
            {{ u.nombre }}
          </ion-select-option>
        </ion-select>
      </ion-item>

      <ion-button expand="block" class="ion-margin-top" @click="crearGasto">
        Guardar gasto
      </ion-button>

      <ion-text color="success" v-if="mensaje">{{ mensaje }}</ion-text>
      <ion-text color="danger" v-if="error">{{ error }}</ion-text>
    </ion-content>
  </ion-page>
</template>

<script setup>
import {
  IonPage, IonHeader, IonToolbar, IonTitle, IonContent,
  IonInput, IonSelect, IonSelectOption, IonLabel, IonItem,
  IonButton, IonText
} from '@ionic/vue'
import { ref, onMounted } from 'vue'

const titulo = ref('')
const monto = ref('')
const pagadoPorId = ref(null)
const participantesIds = ref([])
const usuarios = ref([])

const mensaje = ref('')
const error = ref('')

onMounted(async () => {
  const usuario = JSON.parse(localStorage.getItem('usuario'))
  const grupoId = usuario.grupos[0]?.id

  const res = await fetch(`${import.meta.env.VITE_API_URL}/grupos/${grupoId}/usuarios`)
  const data = await res.json()
  usuarios.value = data
  pagadoPorId.value = usuario.id
})

const crearGasto = async () => {
  try {
    const usuario = JSON.parse(localStorage.getItem('usuario'))
    const grupoId = usuario.grupos[0]?.id

    const res = await fetch(`${import.meta.env.VITE_API_URL}/grupos/grupos/${grupoId}/gastos`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        titulo: titulo.value,
        monto: monto.value,
        pagadoPorId: pagadoPorId.value,
        participantesIds: participantesIds.value
      })
    })

    if (!res.ok) throw new Error('Error al crear gasto')
    mensaje.value = 'Gasto creado correctamente'
  } catch (err) {
    error.value = err.message
  }
}
</script>
