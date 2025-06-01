<template>
  <ion-page>
    <ion-header>
      <ion-toolbar color="primary">
        <ion-title>Resumen de Deudas</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content>
      <ion-list v-if="resumen.length > 0">
        <ion-item v-for="usuario in resumen" :key="usuario.usuarioId">
          <ion-label>
            <h2>{{ usuario.nombre }}</h2>
            <p v-if="usuario.balance > 0" class="text-success">
              Le deben {{ usuario.balance.toFixed(2) }} €
            </p>
            <p v-else-if="usuario.balance < 0" class="text-danger">
              Debe {{ Math.abs(usuario.balance).toFixed(2) }} €
            </p>
            <p v-else>
              Sin deudas
            </p>
          </ion-label>
        </ion-item>
      </ion-list>
      <ion-text v-else class="ion-padding">No hay datos de deudas.</ion-text>
    </ion-content>
  </ion-page>
</template>

<script setup>
import { IonPage, IonHeader, IonToolbar, IonTitle, IonContent, IonList, IonItem, IonLabel, IonText } from '@ionic/vue';
import { onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import axios from 'axios';

const route = useRoute();
const resumen = ref([]);

onMounted(async () => {
  const grupoId = route.params.grupoId;
  try {
    const response = await axios.get(`http://localhost:8080/grupos/${grupoId}/deudas/resumen`);
    resumen.value = response.data;
  } catch (error) {
    console.error('Error al obtener el resumen de deudas:', error);
  }
});
</script>

<style scoped>
.text-success {
  color: green;
}
.text-danger {
  color: red;
}
</style>
