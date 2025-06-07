<template>
  <ion-page>
    <ion-header>
      <ion-toolbar color="primary">
        <ion-buttons slot="start">
          <ion-back-button default-href="javascript:void(0)" @click="volver" />
        </ion-buttons>
        <ion-title>Detalle de Deudas</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding">
      <h2>Deudas de {{ usuarioNombre }}</h2>

      <ion-list v-if="deudas.length > 0">
        <ion-item v-for="deuda in deudas" :key="deuda.id">
          <ion-label>
            <p>Le debe a <strong>{{ deuda.acreedorNombre }}</strong></p>
            <p class="text-danger">Monto: {{ formatMonto(deuda.monto) }}</p>
            <p v-if="deuda.saldado" class="text-success">✅ Saldado</p>
            <p v-else class="text-warning">⏳ Pendiente</p>
          </ion-label>
        </ion-item>
      </ion-list>

      <ion-text v-else>No tiene deudas con nadie del grupo.</ion-text>
    </ion-content>
  </ion-page>
</template>

<script setup>
import {
  IonPage, IonHeader, IonToolbar, IonTitle, IonContent,
  IonItem, IonList, IonLabel, IonBackButton, IonButtons, IonText
} from '@ionic/vue'
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/service/api'

const route = useRoute()
const router = useRouter()

const grupoId = route.params.grupoId
const usuarioId = route.params.usuarioId
const usuarioNombre = route.query.nombre

const deudas = ref([])

onMounted(async () => {
  try {
    const { data: gastos } = await api.get(`/gasto/${grupoId}/gastos`)
    const todasDeudas = []

    for (const gasto of gastos) {
      const { data: deudasGasto } = await api.get(`/gasto/${gasto.id}/deudas`)
      const filtradas = deudasGasto.filter(d => d.deudorId == usuarioId)
      todasDeudas.push(...filtradas)
    }

    deudas.value = todasDeudas
  } catch (e) {
    console.error('Error cargando deudas del usuario', e)
  }
})

const formatMonto = monto =>
  new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' }).format(parseFloat(monto) || 0)

const volver = () => {
  router.push({ name: 'ResumenGrupo', params: { grupoId } })
}
</script>

<style scoped>
.text-success { color: green }
.text-warning { color: orange }
.text-danger { color: red }
</style>
