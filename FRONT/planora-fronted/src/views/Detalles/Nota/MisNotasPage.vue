<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-title>Mis Notas</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding">
      <ion-list v-if="notas.length">
        <ion-item v-for="nota in notas" :key="nota.id">
          <ion-label>
            <h2>{{ nota.titulo }}</h2>
            <p>{{ formatearFecha(nota.fechaCreacion) }}</p>
          </ion-label>
        </ion-item>
      </ion-list>

      <ion-text v-else>
        No has creado ninguna nota todavía.
      </ion-text>
    </ion-content>
  </ion-page>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { NotasService } from '@/service/NotaService'
import { IonPage, IonHeader, IonToolbar, IonTitle, IonContent, IonList, IonItem, IonLabel, IonText } from '@ionic/vue'

const notas = ref([])

onMounted(async () => {
  const token = localStorage.getItem('token')
  if (!token) return

  try {
    notas.value = await NotasService.obtenerNotasDelUsuario(token)
  } catch (error) {
    console.error('Error al cargar tus notas:', error)
  }
})

function formatearFecha(fechaStr) {
  const fecha = new Date(fechaStr)
  return fecha.toLocaleDateString('es-ES', { day: '2-digit', month: 'short', year: 'numeric' })
}
</script>
