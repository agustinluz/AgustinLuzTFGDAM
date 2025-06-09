<template>
  <ion-card class="quick-access-card">
    <ion-card-header class="header">
      <ion-icon :icon="appsOutline" class="header-icon" />
      <ion-card-title>Acciones rápidas</ion-card-title>
    </ion-card-header>
    <ion-card-content>
      <ion-grid class="grid">
        <ion-row>
          <ion-col
            v-for="item in items"
            :key="item.label"
            size="4" size-sm="6"
            class="col"
          >
            <ion-button
              fill="clear"
              class="btn"
              @click="navigate(item)"
              :aria-label="item.label"
            >
              <ion-icon :icon="item.icon" class="icon" />
              <ion-label class="label">{{ item.label }}</ion-label>
            </ion-button>
          </ion-col>
        </ion-row>
      </ion-grid>
    </ion-card-content>
  </ion-card>
</template>

<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import {
  IonCard, IonCardHeader, IonCardTitle,
  IonCardContent, IonIcon, IonGrid, IonRow, IonCol,
  IonButton, IonLabel
} from '@ionic/vue'
import {
  appsOutline, cash, calendarOutline,
  checkboxOutline, images, documentOutline
} from 'ionicons/icons'

interface Item { label: string; icon: any; path: string }

const router = useRouter()
const route = useRoute()
const grupoId = String(route.params.id)

const items: Item[] = [
  { label: 'Gastos',  icon: cash,            path: `/dashboard/${grupoId}/gastos` },
  { label: 'Eventos', icon: calendarOutline, path: `/dashboard/${grupoId}/eventos` },
  { label: 'Votos',   icon: checkboxOutline, path: `/dashboard/${grupoId}/votaciones` },
  { label: 'Galería', icon: images,          path: `/dashboard/${grupoId}/galeria` },
  { label: 'Notas',   icon: documentOutline, path: `/dashboard/${grupoId}/notas` }
]

function navigate(item: Item) {
  router.push(item.path)
}
</script>

<style scoped lang="scss">
.quick-access-card {
  background: var(--ion-color-surface);
  border-radius: var(--border-radius);
  box-shadow: var(--box-shadow-sm);
  margin-bottom: calc(var(--spacing-unit) * 2);
}
.header {
  display: flex;
  align-items: center;
  gap: var(--spacing-unit);
  padding: var(--spacing-unit);
}
.header-icon {
  font-size: var(--font-size-lg);
  color: var(--ion-color-secondary);
}
.grid {
  --ion-grid-padding: 0;
  padding: 0 var(--spacing-unit) var(--spacing-unit);
}
.col {
  text-align: center;
  margin-bottom: var(--spacing-unit);
}
.btn {
  flex-direction: column;
  transition: transform 0.2s;
}
.btn:hover {
  transform: translateY(-2px);
}
.icon {
  font-size: var(--font-size-xl);
}
.label {
  font-size: var(--font-size-sm);
  margin-top: 4px;
}
</style>
