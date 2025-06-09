<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-buttons slot="start">
          <ion-back-button default-href="/grupo" />
        </ion-buttons>
        <ion-title>Invitaciones</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding">
      <ion-list v-if="invitaciones.length">
        <ion-item v-for="inv in invitaciones" :key="inv.id">
          <ion-label class="ion-text-wrap">
            <h2>{{ inv.grupoNombre }}</h2>
            <p>{{ inv.fecha.substring(0,10) }} - {{ inv.estado }}</p>
          </ion-label>
          <ion-button slot="end" color="success" size="small" @click="aceptar(inv.id)" v-if="inv.estado === 'PENDIENTE'">Aceptar</ion-button>
          <ion-button slot="end" color="danger" size="small" @click="rechazar(inv.id)" v-if="inv.estado === 'PENDIENTE'">Rechazar</ion-button>
        </ion-item>
      </ion-list>
      <div v-else class="ion-text-center">
        <p>No tienes invitaciones pendientes.</p>
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { IonPage, IonHeader, IonToolbar, IonTitle, IonContent, IonButtons, IonBackButton, IonList, IonItem, IonLabel, IonButton } from '@ionic/vue'
import { invitacionService, type Invitacion } from '@/service/InvitacionService'

const router = useRouter()
const invitaciones = ref<Invitacion[]>([])
const usuario = JSON.parse(localStorage.getItem('usuario') || 'null')

const cargar = async () => {
  if(!usuario) return
  invitaciones.value = await invitacionService.listar(usuario.id)
}

const aceptar = async (id: number) => {
  await invitacionService.aceptar(id)
  await cargar()
}

const rechazar = async (id: number) => {
  await invitacionService.rechazar(id)
  await cargar()
}

onMounted(cargar)
</script>

<style scoped>
</style>