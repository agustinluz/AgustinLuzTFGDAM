<template>
  <ion-card class="quick-access-card">
    <ion-card-header class="quick-access-header">
      <ion-icon :icon="appsOutline" color="secondary" />
      <ion-card-title>Acciones rápidas</ion-card-title>
    </ion-card-header>
    <ion-card-content>
      <ion-grid class="quick-access-grid">
        <ion-row>
          <ion-col
            size="4"
            v-for="item in items"
            :key="item.label"
            class="access-item"
            @click="navigate(item)"
          >
            <ion-button fill="clear" class="access-button">
              <ion-icon :icon="item.icon" class="access-icon" />
              <ion-label class="access-label">{{ item.label }}</ion-label>
            </ion-button>
          </ion-col>
        </ion-row>
      </ion-grid>
    </ion-card-content>
  </ion-card>
</template>

<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { appsOutline, cash, calendarOutline, checkboxOutline, images, documentOutline } from 'ionicons/icons'
import { IonCard, IonCardHeader, IonCardTitle, IonCardContent, IonIcon, IonGrid, IonRow, IonCol, IonButton, IonLabel } from '@ionic/vue'

interface Item {
  label: string
  icon: any
  path: string
}

const router = useRouter()
const route = useRoute()
const grupoId = route.params.id as string

const items: Item[] = [
  { label: 'Gastos',    icon: cash,          path: `/dashboard/${grupoId}/gastos` },
  { label: 'Eventos',   icon: calendarOutline, path: `/dashboard/${grupoId}/eventos` },
  { label: 'Votos',     icon: checkboxOutline, path: `/dashboard/${grupoId}/votaciones` },
  { label: 'Galería',   icon: images,        path: `/dashboard/${grupoId}/galeria` },
  { label: 'Notas',     icon: documentOutline, path: `/dashboard/${grupoId}/notas` }
]

function navigate(item: Item) {
  router.push(item.path)
}
</script>

<style scoped lang="scss">
.quick-access-card {
  border-radius: 1rem;
  box-shadow: 0 4px 16px rgba(0,0,0,0.05);
}

.quick-access-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.quick-access-grid {
  --ion-grid-padding: 0;
}

.access-item {
  text-align: center;
}

.access-button {
  flex-direction: column;
}

.access-icon {
  font-size: 1.75rem;
}

.access-label {
  font-size: 0.85rem;
  margin-top: 0.25rem;
}
</style>
