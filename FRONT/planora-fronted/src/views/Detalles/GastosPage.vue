<template>
  <ion-page>
    <GastosHeader
      :grupo="grupo"
      :cantidad="gastos.length"
      :total="formatMonto(totalGastos)"
      @volver="irADashboard"
      @abrirOpciones="mostrarOpciones = true"
    />

    <ion-content :fullscreen="true" class="ion-padding">
      <div v-if="cargando" class="loading-container">
        <ion-spinner name="crescent" color="primary" />
        <p>Cargando gastos...</p>
      </div>

      <div v-else>
        <GastosStats
          :total="formatMonto(totalGastos)"
          :cantidad="gastos.length"
          :pendientes="gastosPendientes"
        />

        <GastosSegment
          :total="gastos.length"
          :pendientes="gastosPendientes"
          :saldados="gastosSaldados"
          :valorInicial="filtro"
          @filtrar="filtro = $event"
        />

        <GastosLista
          v-if="gastosFiltrados.length > 0"
          :gastos="gastosFiltrados"
          @verDetalles="verDetalles"
        />

        <GastosEmpty
          v-else
          :mensaje="mensajeVacio"
          @crear="añadirGasto"
        />
      </div>

      <GastoDetalleModal
        :abierto="modalAbierto"
        :gasto="gastoSeleccionado"
        @cerrar="modalAbierto = false"
        @editar="editarGasto"
        @eliminar="onEliminarGasto"
        @marcarSaldado="onMarcarSaldado"
      />

      <ion-action-sheet
        :is-open="mostrarOpciones"
        header="Opciones"
        :buttons="opcionesMenu"
        @did-dismiss="mostrarOpciones = false"
      />

      <GastosFab @crear="añadirGasto" />
    </ion-content>
  </ion-page>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useGastos } from '@/Composable/useGasto'

import GastosHeader from './Gastos/GastosHeader.vue'
import GastosStats from './Gastos/GastosStats.vue'
import GastosSegment from './Gastos/GastosSegment.vue'
import GastosLista from './Gastos/GastosLista.vue'
import GastosEmpty from './Gastos/GastosEmpty.vue'
import GastoDetalleModal from './Gastos/GastoDetalleModal.vue'
import GastosFab from './Gastos/GastosFab.vue'

const route = useRoute()
const router = useRouter()
const grupoId = Number(route.params.id)
const grupo = ref({ nombre: 'Grupo' })

const mostrarOpciones = ref(false)
const modalAbierto = ref(false)

const {
  gastos,
  gastoSeleccionado,
  cargando,
  filtro,
  totalGastos,
  gastosPendientes,
  gastosSaldados,
  gastosFiltrados,
  cargarGastos,
  seleccionarGasto,
  marcarSaldado,
  eliminarGasto
} = useGastos(grupoId)

const irADashboard = () => {
  router.push(`/dashboard/${grupoId}`)
}

const añadirGasto = () => {
  router.push(`/dashboard/${grupoId}/crear/gasto`)
}

const verDetalles = async (gasto) => {
  await seleccionarGasto(gasto)
  modalAbierto.value = true
}

// --- nuevos wrappers ---
const onMarcarSaldado = async (gastoId, participanteId, metodo, notas) => {
  modalAbierto.value = false        // 1) cerramos modal
  try {
    await marcarSaldado(gastoId, participanteId, metodo, notas)
  } catch (e) {
    console.error(e)
  } finally {
    await cargarGastos()            // 2) refrescamos lista
  }
}

const onEliminarGasto = async () => {
  if (!gastoSeleccionado.value) return
  modalAbierto.value = false
  try {
    await eliminarGasto(gastoSeleccionado.value.id)
  } catch (e) {
    console.error(e)
  } finally {
    await cargarGastos()
  }
}
// -----------------------------

const editarGasto = (id) => {
  modalAbierto.value = false
  router.push(`/dashboard/${grupoId}/gastos/${id}/editar`)
}

const mensajeVacio = computed(() => {
  if (filtro.value === 'pendientes') return 'No hay gastos pendientes'
  if (filtro.value === 'saldados') return 'No hay gastos saldados'
  return 'No hay gastos registrados'
})

const opcionesMenu = [
  { text: 'Resumen completo', handler: () => router.push(`/grupo/${grupoId}/resumen`) },
  { text: 'Crear nuevo gasto', handler: añadirGasto },
  { text: 'Cancelar',         role: 'cancel' }
]

const formatMonto = (monto) =>
  new Intl.NumberFormat('es-ES', {
    style: 'currency',
    currency: 'EUR'
  }).format(Number(monto) || 0)

onMounted(() => {
  cargarGastos()
})
</script>

<style scoped>
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  text-align: center;
}
</style>
