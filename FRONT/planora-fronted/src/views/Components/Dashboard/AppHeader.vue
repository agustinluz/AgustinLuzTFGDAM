<template>
  <ion-header>
    <ion-toolbar color="primary">
      <ion-title>{{ title }}</ion-title>
      <ion-buttons slot="end">
        <ion-button fill="clear" @click="$emit('back')">
          <ion-icon :icon="homeOutline" slot="icon-only" />
        </ion-button>
        <ion-button fill="clear" @click="$emit('config')">
          <ion-icon :icon="settingsOutline" slot="icon-only" />
        </ion-button>
        <ion-button fill="clear" @click="$emit('logout')">
          <ion-icon :icon="logOutOutline" slot="icon-only" />
        </ion-button>
      </ion-buttons>
    </ion-toolbar>
  </ion-header>
</template>

<script setup lang="ts">
import { defineProps } from 'vue'
import { IonHeader, IonToolbar, IonTitle, IonButtons, IonButton, IonIcon } from '@ionic/vue'
import { settingsOutline, logOutOutline } from 'ionicons/icons'

const props = defineProps<{ title: string }>()
</script>
