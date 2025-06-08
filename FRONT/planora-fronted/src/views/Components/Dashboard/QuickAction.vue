<template>
  <ion-card class="quick-access-card">
    <ion-card-header>
      <div class="section-title">
        <ion-icon :icon="appsOutline" color="secondary" />
        <ion-card-title>Acciones rápidas</ion-card-title>
      </div>
    </ion-card-header>
    <ion-card-content>
      <div class="quick-access-grid">
        <div class="access-item" v-for="item in items" :key="item.label" @click="$emit('navigate', item.route)">
          <div class="access-icon">
            <ion-icon :icon="item.icon" />
          </div>
          <span>{{ item.label }}</span>
        </div>
      </div>
    </ion-card-content>
  </ion-card>
</template>

<script setup lang="ts">
import { defineProps } from 'vue'
import { IonCard, IonCardHeader, IonCardTitle, IonCardContent, IonIcon } from '@ionic/vue'
import { appsOutline, cash, calendar, checkboxOutline, images, documentOutline } from 'ionicons/icons'

type Item = { label: string; icon: any; route: string }

const items: Item[] = [
  { label: 'Gastos', icon: cash, route: 'gastos' },
  { label: 'Eventos', icon: calendar, route: 'eventos' },
  { label: 'Votos', icon: checkboxOutline, route: 'votaciones' },
  { label: 'Galería', icon: images, route: 'galeria' },
  { label: 'Notas', icon: documentOutline, route: 'notas' }
]
</script>