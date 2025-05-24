<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-title>Gastos del grupo</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding">
      <ion-list v-if="gastos.length">
        <ion-item v-for="gasto in gastos" :key="gasto.id">
          <ion-label>
            <h2>{{ gasto.titulo }}</h2>
            <p>{{ gasto.monto }} €</p>
            <p>Pagado por: {{ gasto.pagadoPor?.nombre || 'Desconocido' }}</p>
          </ion-label>
        </ion-item>
      </ion-list>
      <ion-text v-else>No hay gastos registrados.</ion-text>
    </ion-content>
  </ion-page>
</template>

<script setup>
import { IonPage, IonHeader, IonToolbar, IonTitle, IonContent, IonList, IonItem, IonLabel, IonText } from '@ionic/vue'
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'

const gastos = ref([])
const grupoId = useRoute().params.id

onMounted(async () => {
  const res = await fetch(`${import.meta.env.VITE_API_URL}/grupos/${grupoId}/gastos`)
  if (res.ok) gastos.value = await res.json()
})
</script>
