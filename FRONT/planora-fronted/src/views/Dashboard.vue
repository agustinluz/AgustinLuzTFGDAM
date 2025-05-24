<template>
  <ion-page>
    <ion-header>
      <ion-toolbar color="primary">
        <ion-title>{{ grupo?.nombre || 'Dashboard' }}</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content :fullscreen="true" class="ion-padding">
      <!-- Accesos rápidos -->
      <ion-grid>
        <ion-row>
          <ion-col size="6">
            <ion-card button @click="goTo('eventos')">
              <ion-card-header>
                <ion-card-title>Eventos</ion-card-title>
              </ion-card-header>
            </ion-card>
          </ion-col>
          <ion-col size="6">
            <ion-card button @click="goTo('votaciones')">
              <ion-card-header>
                <ion-card-title>Votaciones</ion-card-title>
              </ion-card-header>
            </ion-card>
          </ion-col>
          <ion-col size="6">
            <ion-card button @click="goTo('gastos')">
              <ion-card-header>
                <ion-card-title>Gastos</ion-card-title>
              </ion-card-header>
            </ion-card>
          </ion-col>
          <ion-col size="6">
            <ion-card button @click="goTo('notas')">
              <ion-card-header>
                <ion-card-title>Notas</ion-card-title>
              </ion-card-header>
            </ion-card>
          </ion-col>
          <ion-col size="12">
            <ion-card button @click="goTo('galeria')">
              <ion-card-header>
                <ion-card-title>Galería</ion-card-title>
              </ion-card-header>
            </ion-card>
          </ion-col>
        </ion-row>
      </ion-grid>

      <!-- Próximos eventos -->
      <ion-card>
        <ion-card-header>
          <ion-card-title>Próximos eventos</ion-card-title>
        </ion-card-header>
        <ion-card-content>
          <ion-list v-if="eventos.length">
            <ion-item v-for="evento in eventos" :key="evento.id">
              <ion-label>
                <h2>{{ evento.titulo }}</h2>
                <p>{{ formatFecha(evento.fecha) }}</p>
              </ion-label>
            </ion-item>
          </ion-list>
          <ion-text v-else>No hay eventos próximos.</ion-text>
        </ion-card-content>
      </ion-card>

      <!-- Donut Chart de gastos -->
      <ion-card v-if="chartData.labels.length">
        <ion-card-header>
          <ion-card-title>Gastos por participante</ion-card-title>
        </ion-card-header>
        <ion-card-content>
          <canvas id="gastosChart"></canvas>
        </ion-card-content>
      </ion-card>
    </ion-content>

    <!-- FAB -->
    <ion-fab slot="fixed" vertical="bottom" horizontal="end">
      <ion-fab-button @click="goTo('crear')">
        <ion-icon :icon="add"></ion-icon>
      </ion-fab-button>
    </ion-fab>
  </ion-page>
</template>

<script setup>
import {
  IonPage,
  IonHeader,
  IonToolbar,
  IonTitle,
  IonContent,
  IonGrid,
  IonRow,
  IonCol,
  IonCard,
  IonCardHeader,
  IonCardTitle,
  IonCardContent,
  IonList,
  IonItem,
  IonLabel,
  IonText,
  IonFab,
  IonFabButton,
  IonIcon,
  useIonRouter
} from '@ionic/vue'
import { add } from 'ionicons/icons'
import { ref, onMounted } from 'vue'
import Chart from 'chart.js/auto'


const router = useIonRouter()
const grupo = ref(null)
const eventos = ref([])
const chartData = ref({ labels: [], datasets: [] })

const usuario = JSON.parse(localStorage.getItem('usuario'))
const grupoId = localStorage.getItem('grupoActivoId')


const fetchEventos = async () => {
  try {
    const res = await fetch(`${import.meta.env.VITE_API_URL}/grupos/${grupoId}/eventos`)
    eventos.value = await res.json()
  } catch (err) {
    console.error('Error cargando eventos:', err)
  }
}

const fetchGastos = async () => {
  try {
    const res = await fetch(`${import.meta.env.VITE_API_URL}/grupos/${grupoId}/gastos`)
    const gastos = await res.json()

    const gastosPorUsuario = {}
    gastos.forEach(g => {
      const nombre = g.pagadoPor?.nombre || 'Desconocido'
      gastosPorUsuario[nombre] = (gastosPorUsuario[nombre] || 0) + g.monto
    })

    chartData.value = {
      labels: Object.keys(gastosPorUsuario),
      datasets: [{
        label: 'Gastos por usuario',
        data: Object.values(gastosPorUsuario),
        backgroundColor: ['#FF6384', '#36A2EB', '#FFCE56', '#9CCC65', '#BA68C8']
      }]
    }

    renderChart()
  } catch (err) {
    console.error('Error cargando gastos:', err)
  }
}
const fetchGrupo = async () => {
  try {
    const res = await fetch(`${import.meta.env.VITE_API_URL}/grupos/${grupoId}`)
    if (res.ok) {
      grupo.value = await res.json()
    } else {
      grupo.value = { nombre: 'Grupo desconocido' }
    }
  } catch (err) {
    console.error('Error al cargar el grupo:', err)
    grupo.value = { nombre: 'Error al cargar grupo' }
  }
}


const renderChart = () => {
  const ctx = document.getElementById('gastosChart')
  if (!ctx) return

  new Chart(ctx, {
    type: 'doughnut',
    data: chartData.value,
    options: {
      responsive: true,
      plugins: {
        legend: {
          position: 'bottom'
        }
      }
    }
  })
}

const goTo = (ruta) => {
  router.push(`/dashboard/${grupoId}/${ruta}`)
}

const formatFecha = (fecha) => {
  return new Date(fecha).toLocaleDateString('es-ES', {
    day: '2-digit', month: 'long', year: 'numeric'
  })
}

onMounted(async () => {
  grupo.value = { nombre: 'Cargando...' }
  await fetchEventos()
  await fetchGastos()
  await fetchGrupo()
})
</script>

<style scoped>
canvas {
  max-width: 100%;
  height: auto;
}
</style>
