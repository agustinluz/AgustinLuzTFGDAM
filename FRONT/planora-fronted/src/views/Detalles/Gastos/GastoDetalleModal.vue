<template>
  <ion-modal :is-open="abierto" @did-dismiss="emit('cerrar')">
    <ion-header>
      <ion-toolbar>
        <ion-title>Detalles del Gasto</ion-title>
        <ion-buttons slot="end">
          <ion-button fill="clear" @click="emit('cerrar')">
            <ion-icon :icon="close" />
          </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding">
      <template v-if="gasto">
        <ion-card>
          <ion-card-header>
            <ion-card-title>{{ gasto.titulo }}</ion-card-title>
            <ion-card-subtitle>{{ formatMonto(gasto.monto) }}</ion-card-subtitle>
          </ion-card-header>
          <ion-card-content>
            <div class="info-grid">
              <div class="info-item">
                <ion-icon :icon="person" color="primary" />
                <div>
                  <strong>Pagado por:</strong>
                  <p>{{ gasto.pagadoPor?.nombre }}</p>
                </div>
              </div>
              <div class="info-item">
                <ion-icon :icon="calendar" color="primary" />
                <div>
                  <strong>Fecha:</strong>
                  <p>{{ formatFecha(gasto.fecha) }}</p>
                </div>
              </div>
              <div class="info-item" v-if="gasto.evento">
                <ion-icon :icon="star" color="primary" />
                <div>
                  <strong>Evento:</strong>
                  <p>{{ gasto.evento.nombre }}</p>
                </div>
              </div>
            </div>
          </ion-card-content>
        </ion-card>

        <ion-card v-if="agrupados.length">
          <ion-card-header>
            <ion-card-title>Participantes ({{ agrupados.length }})</ion-card-title>
          </ion-card-header>
          <ion-card-content>
            <div class="info-item">
  <ion-icon :icon="cashOutline" color="success" />
  <div>
    <strong>Total pagado:</strong>
    <p>{{ formatMonto(gasto.monto) }}</p>
  </div>
</div>

            <ion-item v-for="p in agrupados" :key="p.deudorId + '-' + p.total">
              <ion-avatar slot="start">
                <div class="avatar-text">{{ getInitials(p.deudorNombre) }}</div>
              </ion-avatar>
              <ion-label>
                <h3>
                  {{ p.deudorNombre }}
                  <ion-icon
                    v-if="p.esPagador"
                    :icon="medalOutline"
                    color="warning"
                    title="Pagador"
                  />
                </h3>
                <p>{{ formatMonto(p.total) }}</p>
                <p v-if="p.esPagador" class="acreedor-label">Pagador</p>
              </ion-label>
              <div v-if="!p.saldado && !p.esPagador">
                <ion-input v-model="form.metodoPago" placeholder="Método de pago" class="input-inline" />
                <ion-input v-model="form.notas" placeholder="Notas (opcional)" class="input-inline" />
                <ion-button fill="clear" size="small" color="success" @click="emitirSaldado(p)">
                  <ion-icon :icon="checkmarkCircle" />
                  <span class="ml-1">Marcar Saldado</span>
                </ion-button>
              </div>
              <ion-icon
                v-else
                :icon="checkmarkCircle"
                color="success"
                title="Saldado"
              />
            </ion-item>
          </ion-card-content>
        </ion-card>

        <div class="modal-actions">
          <ion-button expand="block" fill="outline" @click="emit('editar', gasto.id)">
            <ion-icon :icon="create" slot="start" />
            Editar
          </ion-button>
          <ion-button expand="block" fill="clear" color="danger" @click="emit('eliminar')">
            <ion-icon :icon="trash" slot="start" />
            Eliminar
          </ion-button>
        </div>
      </template>
    </ion-content>
  </ion-modal>
</template>

<script setup>
import {
  IonModal, IonHeader, IonToolbar, IonTitle, IonButtons, IonButton, IonIcon, IonContent,
  IonCard, IonCardHeader, IonCardTitle, IonCardSubtitle, IonCardContent,
  IonItem, IonAvatar, IonLabel, IonInput
} from '@ionic/vue'
import {
  close, person, calendar, star, create, trash,
  checkmarkCircle, medalOutline
} from 'ionicons/icons'
import { defineProps, defineEmits, computed, reactive, nextTick } from 'vue'

const props = defineProps({
  abierto: Boolean,
  gasto: Object
})
const emit = defineEmits(['cerrar', 'editar', 'eliminar', 'marcarSaldado'])

const form = reactive({
  metodoPago: 'efectivo',
  notas: ''
})

const emitirSaldado = async (p) => {
  if (!p?.deudorId || typeof p.deudorId !== 'number' || !props.gasto?.id) {
    console.warn('❌ Datos inválidos para marcar saldado:', p)
    return
  }
  emit('marcarSaldado', props.gasto.id, p.deudorId, form.metodoPago, form.notas)
  console.log(`✅ Deuda de ${p.deudorNombre} marcada como saldada`)
  await nextTick()
}

const agrupados = computed(() => {
  const resultado = []
  if (!props.gasto?.deudas) return resultado

  const agrupacion = new Map()

  for (const deuda of props.gasto.deudas) {
    // ⛔️ Ignorar deudas del pagador hacia sí mismo
    if (deuda.deudorId === deuda.acreedorId) continue

    const key = deuda.deudorId
    if (!agrupacion.has(key)) {
      agrupacion.set(key, {
        deudorId: deuda.deudorId,
        deudorNombre: deuda.deudorNombre,
        total: 0,
        saldado: true,
        esPagador: false
      })
    }
    const entrada = agrupacion.get(key)
    entrada.total += parseFloat(deuda.monto || 0)
    if (!deuda.saldado) {
      entrada.saldado = false
    }
  }

  return Array.from(agrupacion.values())
})


const formatMonto = monto =>
  new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' }).format(parseFloat(monto) || 0)

const formatFecha = fecha =>
  new Date(fecha).toLocaleDateString('es-ES', {
    weekday: 'long', year: 'numeric', month: 'long', day: 'numeric'
  })

const getInitials = nombre => nombre?.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2) || 'NA'
</script>

<style scoped>
.info-grid {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
.info-item {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
}
.avatar-text {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: var(--ion-color-primary);
  color: white;
  font-weight: 600;
  border-radius: 50%;
}
.modal-actions {
  margin-top: 2rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}
.acreedor-label {
  font-size: 0.75rem;
  color: var(--ion-color-warning);
}
.ml-1 {
  margin-left: 0.25rem;
}
.input-inline {
  margin-bottom: 0.25rem;
  --padding-start: 0;
  --padding-end: 0;
  font-size: 0.9rem;
}
</style>
