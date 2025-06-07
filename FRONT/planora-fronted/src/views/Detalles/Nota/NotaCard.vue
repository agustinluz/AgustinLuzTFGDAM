<template>
  <ion-card class="nota-card">
    <ion-card-header>
      <div class="nota-header">
        <ion-card-title>{{ nota.titulo }}</ion-card-title>
        <div class="nota-actions">
          <ion-button @click="$emit('editar', nota)" fill="clear" size="small" v-if="esProietario">
            <ion-icon :icon="createOutline" />
          </ion-button>
          <ion-button @click="$emit('eliminar', nota)" fill="clear" size="small" color="danger" v-if="esProietario">
            <ion-icon :icon="trashOutline" />
          </ion-button>
        </div>
      </div>
      <ion-card-subtitle>
        Por: {{ nota.creadaPorNombre || 'Usuario' }} • {{ formatearFecha(nota.fechaCreacion) }}
      </ion-card-subtitle>
    </ion-card-header>
    <ion-card-content>
      <p class="nota-contenido">{{ nota.contenido }}</p>
      <!-- 
    Mostrar botón "Ver más" solo si el contenido es largo
    Puedes descomentar el v-if para activarlo v-if="nota.contenido.length > 150"
  -->
  <ion-button @click="$emit('ver', nota)" fill="clear" size="small" class="ion-margin-top" color="primary" style="float: right">
        Ver más
      </ion-button>
    </ion-card-content>
  </ion-card>
</template>

<script setup>
import { IonCard, IonCardHeader, IonCardTitle, IonCardSubtitle, IonCardContent, IonButton, IonIcon } from '@ionic/vue'
import { createOutline, trashOutline } from 'ionicons/icons'

const props = defineProps({
  nota: Object,
  esProietario: Boolean
})

defineEmits(['editar', 'eliminar', 'ver'])

const formatearFecha = (fecha) => {
  return new Date(fecha).toLocaleDateString('es-ES', {
    year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit'
  })
}
</script>

<style scoped>
.nota-card {
  margin: 1rem 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.07);
  border-radius: 14px;
}

.nota-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.nota-actions {
  display: flex;
  gap: 0.25rem;
}

.nota-contenido {
  font-size: 1rem;
  line-height: 1.5;
}
</style>
