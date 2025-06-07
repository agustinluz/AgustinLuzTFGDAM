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
        <GastosStats :total="formatMonto(totalGastos)" :cantidad="gastos.length" :pendientes="gastosPendientes" />

        <GastosSegment
          :total="gastos.length"
          :pendientes="gastosPendientes"
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
  :config="config"
  @cerrar="modalAbierto = false"
  @editar="editarGasto"
  @eliminar="confirmarEliminarGasto"
  @marcarSaldado="marcarSaldado"
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
import { IonPage, IonContent, IonSpinner, IonActionSheet } from '@ionic/vue'
import { useRoute, useRouter } from 'vue-router'
import { ref, onMounted, computed } from 'vue'
import { useGastos } from '@/Composable/useGasto'
import GastosHeader from './Gastos/GastosHeader.vue'
import GastosStats from './Gastos/GastosStats.vue'
import GastosSegment from './Gastos/GastosSegment.vue'
import GastosLista from './Gastos/GastosLista.vue'
import GastosEmpty from './Gastos/GastosEmpty.vue'
import GastoDetalleModal from './Gastos/GastoDetalleModal.vue'
import GastosFab from './Gastos/GastosFab.vue'
import { useConfiguracionGasto } from '@/Composable/UseConfiguracionGasto'

const route = useRoute()
const router = useRouter()
const grupoId = Number(route.params.id)
const grupo = ref({ nombre: 'Grupo' })

const mostrarOpciones = ref(false)
const modalAbierto = ref(false)
const { config, cargarConfig } = useConfiguracionGasto(grupoId)
const {
  gastos,
  gastoSeleccionado,
  cargando,
  filtro,
  totalGastos,
  gastosPendientes,
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
  seleccionarGasto(gasto)
  modalAbierto.value = true
}

const cerrarModal = () => {
  modalAbierto.value = false
}

const editarGasto = (id) => {
  cerrarModal()
  router.push(`/dashboard/${grupoId}/gastos/${id}/editar`)
}

const confirmarEliminarGasto = async () => {
  if (gastoSeleccionado.value) {
    await eliminarGasto(gastoSeleccionado.value.id)
    cerrarModal()
  }
}

const mensajeVacio = computed(() =>
  filtro.value === 'pendientes' ? 'No hay gastos pendientes' : 'No hay gastos registrados'
)

const opcionesMenu = [
  { text: 'Resumen completo', handler: () => router.push(`/grupo/${grupoId}/resumen`) },
  { text: 'Configuración', handler: () => router.push(`/grupo/${grupoId}/config`) },
  { text: 'Cancelar', role: 'cancel' }
]

const formatMonto = (monto) =>
  new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' }).format(Number(monto) || 0)

onMounted(() => {
  cargarGastos()
  cargarConfig()
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
