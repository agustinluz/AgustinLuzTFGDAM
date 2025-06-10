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

      <ion-list v-if="resumenDeudas.length">
        <ion-item
          v-for="item in resumenDeudas"
          :key="item.acreedorId"
        >
          <ion-label>
            <p>Le debe a <strong>{{ item.acreedorNombre }}</strong></p>
            <p :class="item.saldado ? 'text-success' : 'text-danger'">
              Monto: {{ formatMonto(item.monto) }}
              <span v-if="item.saldado">✅ Saldo</span>
              <span v-else>⏳ Pendiente</span>
            </p>
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
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/service/api'

const route = useRoute()
const router = useRouter()

const grupoId       = route.params.grupoId
const usuarioId     = route.params.usuarioId
const usuarioNombre = route.query.nombre

// 1) Traemos todas las deudas individuales:
const deudas = ref([])

onMounted(async () => {
  try {
    const { data: gastos } = await api.get(`/gasto/${grupoId}/gastos`)
    const todas = []
    for (const g of gastos) {
      const { data: deudasGasto } = await api.get(`/gasto/${g.id}/deudas`)
      // filtrar solo las de este usuario
      todas.push(...deudasGasto.filter(d => d.deudorId === Number(usuarioId)))
    }
    deudas.value = todas
  } catch (e) {
    console.error('Error cargando deudas del usuario', e)
  }
})

// 2) Computed que agrupa por acreedorId:
const resumenDeudas = computed(() => {
  const mapa = new Map()
  for (const d of deudas.value) {
    const key = d.acreedorId
    if (!mapa.has(key)) {
      mapa.set(key, {
        acreedorId:      key,
        acreedorNombre:  d.acreedorNombre,
        monto:           0,
        saldado:         true
      })
    }
    const acc = mapa.get(key)
    acc.monto   += parseFloat(d.monto)
    if (!d.saldado) acc.saldado = false
  }
  return Array.from(mapa.values())
})

const volver = () => router.push({ name: 'ResumenGrupo', params: { grupoId } })

const formatMonto = monto =>
  new Intl.NumberFormat('es-ES',{style:'currency',currency:'EUR'}).format(monto||0)
</script>

<style scoped>
.text-success { color: green; }
.text-danger  { color: red; }
</style>
