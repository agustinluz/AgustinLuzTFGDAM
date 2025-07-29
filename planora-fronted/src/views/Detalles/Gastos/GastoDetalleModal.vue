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
                  <p>{{ gasto.pagadoPor.nombre }}</p>
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

        <ion-card v-if="agrupados.length > 0">
          <ion-card-header>
            <ion-card-title>Participantes ({{ agrupados.length }})</ion-card-title>
          </ion-card-header>
          <ion-card-content>
            <div class="info-item">
              <ion-icon :icon="cashOutline" color="success" />
              <div>
                <strong>Falta por pagar:</strong>
                <p>{{ formatMonto(totalPorPagar) }}</p>
              </div>
            </div>

            <ion-item
              v-for="p in agrupados"
              :key="`${p.deudorId}-${p.total}`"
            >
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
              <div v-if="!p.saldado && !p.esPagador" class="actions-inline">
                <ion-input
                  v-model="form.metodoPago"
                  placeholder="Método de pago"
                  class="input-inline"
                />
                <ion-input
                  v-model="form.notas"
                  placeholder="Notas (opcional)"
                  class="input-inline"
                />
                <ion-button
                  fill="clear"
                  size="small"
                  color="success"
                  @click="emitirSaldado(p)"
                >
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
          <ion-button
            expand="block"
            fill="outline"
            @click="emit('editar', gasto.id)"
          >
            <ion-icon :icon="create" slot="start" />
            Editar
          </ion-button>
          <ion-button
            expand="block"
            fill="clear"
            color="danger"
            @click="emit('eliminar')"
          >
            <ion-icon :icon="trash" slot="start" />
            Eliminar
          </ion-button>
        </div>
      </template>
    </ion-content>
  </ion-modal>
</template>

<script setup lang="ts">
import {
  IonModal,
  IonHeader,
  IonToolbar,
  IonTitle,
  IonButtons,
  IonButton,
  IonIcon,
  IonContent,
  IonCard,
  IonCardHeader,
  IonCardTitle,
  IonCardSubtitle,
  IonCardContent,
  IonItem,
  IonAvatar,
  IonLabel,
  IonInput
} from '@ionic/vue'
import {
  close,
  person,
  calendar,
  star,
  create,
  trash,
  checkmarkCircle,
  medalOutline,
  cashOutline
} from 'ionicons/icons'
import { defineProps, defineEmits, computed, reactive, nextTick } from 'vue'

// ==== Interfaces de tipos ====
interface Usuario {
  id: number
  nombre: string
}
interface Deuda {
  deudorId: number
  deudorNombre: string
  monto: number | string
  saldado: boolean
}
interface Evento {
  id: number
  nombre: string
}
interface GastoDetalle {
  id: number
  titulo: string
  monto: number | string
  fecha: string
  pagadoPor: Usuario
  evento?: Evento
  partesIguales: boolean
  usuarios: Usuario[]
  deudas: Deuda[]
}

// ==== Props y Emits tipados ====
const props = defineProps<{
  abierto: boolean
  gasto: GastoDetalle | null
}>()

const emit = defineEmits<{
  (e: 'cerrar'): void
  (e: 'editar', id: number): void
  (e: 'eliminar'): void
  (
    e: 'marcarSaldado',
    gastoId: number,
    participanteId: number,
    metodoPago: string,
    notas: string
  ): void
}>()

// ==== Estado local ====
const form = reactive({
  metodoPago: 'efectivo',
  notas: ''
})

// ==== Métodos ====
const emitirSaldado = async (p: { deudorId: number; deudorNombre: string }) => {
  if (!props.gasto) return
  emit(
    'marcarSaldado',
    props.gasto.id,
    p.deudorId,
    form.metodoPago,
    form.notas
  )
  await nextTick()
}

// ==== Lógica de agrupación ====
const agrupados = computed(() => {
  const resultado: {
    deudorId: number
    deudorNombre: string
    total: number
    saldado: boolean
    esPagador: boolean
  }[] = []

  const g = props.gasto
  if (!g) return resultado

  const participantes = g.usuarios
  const share = g.partesIguales
    ? Number(g.monto) / participantes.length
    : null

  for (const u of participantes) {
    // omitimos al pagador
    if (u.id === g.pagadoPor.id) continue

    let total = 0
    let saldado = true

    const deuda = g.deudas.find(d => d.deudorId === u.id)

    if (share !== null) {
      total = share
      saldado = deuda ? deuda.saldado : false
    } else {
      total = deuda ? Number(deuda.monto) : 0
      saldado = deuda ? deuda.saldado : false
    }

    resultado.push({
      deudorId: u.id,
      deudorNombre: u.nombre,
      total,
      saldado,
      esPagador: false
    })
  }

  return resultado
})

const totalPorPagar = computed(() =>
  agrupados.value.reduce((sum, p) =>
    !p.saldado ? sum + Number(p.total) : sum, 0)
)
// ==== Helpers de formato ====
const formatMonto = (m: number | string): string =>
  new Intl.NumberFormat('es-ES', {
    style: 'currency',
    currency: 'EUR'
  }).format(Number(m) || 0)

const formatFecha = (f: string | Date): string =>
  new Date(f).toLocaleDateString('es-ES', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })

const getInitials = (nombre: string): string =>
  nombre
    .split(' ')
    .map(n => n[0])
    .join('')
    .toUpperCase()
    .slice(0, 2)
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
.actions-inline {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}
</style>
