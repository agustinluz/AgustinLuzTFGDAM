<template>
  <ion-card class="gastos-card">
    <ion-card-content class="ion-no-padding">
      <ion-item
        v-for="gasto in gastos"
        :key="gasto.id"
        button
        @click="$emit('verDetalles', gasto)"
        class="gasto-item"
      >
        <div class="gasto-icon" slot="start">
          <ion-icon :icon="getIcon(gasto)" :color="getColor(gasto)" />
        </div>
        <ion-label>
          <h2>{{ gasto.titulo }}</h2>
          <p>{{ gasto.pagadoPor?.nombre }} • {{ formatFecha(gasto.fecha) }}</p>
          <p v-if="gasto.evento">{{ gasto.evento.nombre }}</p>
        </ion-label>
        <div slot="end" class="gasto-end">
          <h3>{{ formatMonto(gasto.monto) }}</h3>
          <ion-chip size="small" :color="gasto.pendiente ? 'warning' : 'success'">
            {{ gasto.pendiente ? 'Pendiente' : 'Saldado' }}
          </ion-chip>
        </div>
      </ion-item>
    </ion-card-content>
  </ion-card>
</template>

<script setup>
import { IonCard, IonCardContent, IonItem, IonLabel, IonIcon, IonChip } from '@ionic/vue'
import { defineProps, defineEmits } from 'vue'
import { receiptOutline, home, restaurant, car, ticket } from 'ionicons/icons'

const props = defineProps(['gastos'])
defineEmits(['verDetalles'])

const getIcon = gasto => {
  const t = gasto.titulo?.toLowerCase() || ''
  if (t.includes('hotel')) return home
  if (t.includes('restaurante') || t.includes('comida')) return restaurant
  if (t.includes('taxi') || t.includes('transporte')) return car
  if (t.includes('entrada') || t.includes('ticket')) return ticket
  return receiptOutline
}

const getColor = gasto => {
  const m = parseFloat(gasto.monto || 0)
  if (m > 100) return 'danger'
  if (m > 50) return 'warning'
  return 'primary'
}

const formatMonto = monto =>
  new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' }).format(parseFloat(monto) || 0)

const formatFecha = fecha => {
  const f = new Date(fecha)
  return f.toLocaleDateString('es-ES', { day: 'numeric', month: 'short' })
}
</script>

<style scoped>
.gastos-card {
  margin-top: 1rem;
}
.gasto-item {
  --padding-start: 16px;
  --padding-end: 16px;
}
.gasto-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--ion-color-light);
}
.gasto-end {
  text-align: right;
}
.gasto-end h3 {
  margin: 0 0 0.5rem 0;
  font-size: 1.1rem;
  font-weight: 600;
}
</style>
