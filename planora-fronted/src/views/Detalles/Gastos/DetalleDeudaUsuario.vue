<template>
  <ion-page>
    <ion-header>
      <ion-toolbar color="primary">
        <ion-buttons slot="start" color="light">
          <ion-button @click="volver" color="light">
  <ion-icon slot="icon-only" name="arrow-back" />
</ion-button>
        </ion-buttons>
        <ion-title>Detalle de Deudas</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding">
      <h2>Deudas de {{ usuarioNombre }}</h2>

      <ion-list v-if="deudas.length > 0">
        <ion-item v-for="deuda in deudas" :key="`${deuda.gastoId}-${deuda.deudorId}`">
          <ion-label>
            <p>
              Le debe a <strong>{{ deuda.acreedorNombre }}</strong> por
              <strong>"{{ deuda.titulo }}"</strong>
            </p>
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

<script setup lang="ts">
import {
  IonPage, IonHeader, IonToolbar, IonTitle, IonContent,
  IonItem, IonList, IonLabel, IonButton, IonButtons, IonText, IonIcon
} from '@ionic/vue'
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/service/api'

const route = useRoute()
const router = useRouter()

const grupoId = route.params.grupoId as string
const usuarioId = route.params.usuarioId as string
const usuarioNombre = route.query.nombre as string

const deudas = ref<any[]>([])

const cargarDeudas = async () => {
  try {
    const { data: gastos } = await api.get<any[]>(`/gasto/${grupoId}/gastos`)
    const todasDeudas: any[] = []

    for (const gasto of gastos) {
      const { data } = await api.get(`/gasto/${gasto.id}`)
      const detalle = data as { id: string, titulo: string, deudas: any[] }
      const deudasUsuario = detalle.deudas.filter((d: any) => d.deudorId == usuarioId)

      for (const d of deudasUsuario) {
        todasDeudas.push({
          ...d,
          gastoId: detalle.id,
          titulo: detalle.titulo,
          acreedorNombre: d.acreedorNombre
        })
      }
    }

    // Eliminar duplicados por gastoId + deudorId
    const claveSet = new Set()
    const unicas = todasDeudas.filter(d => {
      const clave = `${d.gastoId}-${d.deudorId}`
      if (claveSet.has(clave)) return false
      claveSet.add(clave)
      return true
    })

    deudas.value = unicas
  } catch (e) {
    console.error('Error cargando deudas del usuario', e)
  }
}

onMounted(cargarDeudas)

watch(
  () => [route.params.grupoId, route.params.usuarioId],
  () => cargarDeudas()
)

const formatMonto = (monto: number | string): string =>
  new Intl.NumberFormat('es-ES', {
    style: 'currency',
    currency: 'EUR'
  }).format(parseFloat(monto as string) || 0)

const volver = () => {
  router.push(`/dashboard/${grupoId}/gastos`)
}
</script>

<style scoped>
.text-success { color: green; }
.text-warning { color: orange; }
.text-danger { color: red; }
</style>
