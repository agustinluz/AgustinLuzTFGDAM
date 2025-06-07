<template>
  <ion-page>
    <ion-header>
      <ion-toolbar color="primary">
        <ion-buttons slot="start">
          <ion-back-button default-href="javascript:void(0)" @click="volver" />
        </ion-buttons>
        <ion-title>Resumen de Deudas</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content>
      <ion-list v-if="resumen.length > 0">
        <ion-item
          v-for="usuario in resumen"
          :key="usuario.usuarioId"
          @click="verDetalle(usuario)"
          button
        >
          <ion-label>
            <h2>{{ usuario.nombre }}</h2>
            <p v-if="usuario.balance > 0" class="text-success">Le deben {{ formatMonto(usuario.balance) }}</p>
            <p v-else-if="usuario.balance < 0" class="text-danger">Debe {{ formatMonto(Math.abs(usuario.balance)) }}</p>
            <p v-else>Sin deudas</p>
          </ion-label>
        </ion-item>
      </ion-list>
      <ion-text v-else class="ion-padding">No hay datos de deudas.</ion-text>
    </ion-content>
  </ion-page>
</template>

<script setup>
import {
  IonPage, IonHeader, IonToolbar, IonTitle, IonContent,
  IonList, IonItem, IonLabel, IonText, IonBackButton, IonButtons
} from '@ionic/vue'
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/service/api'

const route = useRoute()
const router = useRouter()
const grupoId = route.params.grupoId
const resumen = ref([])

onMounted(async () => {
  try {
    const { data } = await api.get(`/gasto/grupos/${grupoId}/resumen`)
    resumen.value = data
  } catch (error) {
    console.error('Error al obtener el resumen de deudas:', error)
  }
})

const verDetalle = (usuario) => {
  router.push({
    name: 'DetalleDeudaUsuario',
    params: {
      grupoId,
      usuarioId: usuario.usuarioId
    },
    query: { nombre: usuario.nombre }
  })
}

const volver = () => {
  router.push(`/dashboard/${grupoId}`)
}

const formatMonto = (monto) =>
  new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' }).format(monto || 0)
</script>

<style scoped>
.text-success { color: green; }
.text-danger { color: red; }
</style>
