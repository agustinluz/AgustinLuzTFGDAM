<template>
  <ion-page>
    <ion-header>
      <ion-toolbar color="primary">
      <ion-buttons slot="start">
        <ion-button fill="clear" color="light" @click="irDashboard">
          <ion-icon :icon="arrowBack" />
        </ion-button>
      </ion-buttons>
        <ion-title>Resumen de Deudas</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding">
      <ion-list v-if="usuarios.length">
        <ion-item
          v-for="u in usuarios"
          :key="u.id"
          @click="verDetalle(u)"
          button
        >
          <ion-label>
            <h2>{{ u.nombre }}</h2>
            <p v-if="getResumen(u.id).debe" class="text-danger">
              Debe {{ formatMonto(getResumen(u.id).debe) }}
            </p>
            <p v-if="getResumen(u.id).leDeben" class="text-success">
              Le deben {{ formatMonto(getResumen(u.id).leDeben) }}
            </p>
            <p
              v-if="!getResumen(u.id).debe && !getResumen(u.id).leDeben"
              class="text-paid"
            >
              ✅ Sin deudas
            </p>
          </ion-label>
        </ion-item>
      </ion-list>

      <ion-text v-else class="ion-padding">
        Cargando participantes…
      </ion-text>
    </ion-content>
  </ion-page>
</template>

<script setup>
import {
  IonPage, IonHeader, IonToolbar, IonTitle, IonContent,
  IonList, IonItem, IonLabel, IonText, IonBackButton, IonButtons, IonButton, IonIcon
} from '@ionic/vue'
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/service/api'

const route = useRoute()
const router = useRouter()
const grupoId = route.params.grupoId

// Lista de todos los usuarios del grupo
const usuarios = ref([])
// Resumen de deudas por usuario
const resumenes = ref([])
const irDashboard = () => {
  router.push(`/dashboard/${grupoId}`)
}

onMounted(async () => {
  try {
    // 1) Obtener todos los miembros
    const { data: u } = await api.get(`/grupos/${grupoId}/usuarios`)
    usuarios.value = u

    // 2) Obtener resumen de deudas
    const { data: resumen } = await api.get(`/gasto/grupos/${grupoId}/resumen`)
    // resumen: [{ usuarioId, nombre, email, balance }, …]
    resumenes.value = resumen

  } catch (error) {
    console.error('Error al cargar resumen de deudas:', error)
  }
})

// Al pulsar sobre un usuario vamos a su detalle
const verDetalle = (usuario) => {
  router.push({
    name: 'DetalleDeudaUsuario',
    params: {
      grupoId,
      usuarioId: usuario.id
    },
    query: { nombre: usuario.nombre }
  })
}

const volver = () => {
  router.push(`/dashboard/${grupoId}`)
}
const getResumen = (id) =>
  resumenes.value.find(r => r.usuarioId === id) || { debe: 0, leDeben: 0 }

const formatMonto = (m) =>
  new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' }).format(m || 0)
</script>

<style scoped>
.text-success { color: var(--ion-color-success); }
.text-danger  { color: var(--ion-color-danger); }
.text-paid    { color: var(--ion-color-medium); }
</style>
