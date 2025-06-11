<template>
  <ion-header>
    <ion-toolbar class="page-header" color="primary">
      <ion-buttons slot="start" v-if="showBack">
        <ion-back-button :default-href="backHref" @click="$emit('back')" color="light" />
      </ion-buttons>
      <ion-title>{{ title }}</ion-title>
      <ion-buttons slot="end">
        
      </ion-buttons>
    </ion-toolbar>
  </ion-header>
</template>

<script setup lang="ts">
import { withDefaults, defineProps } from 'vue'
import {
  IonHeader,
  IonToolbar,
  IonTitle,
  IonButtons,
  IonBackButton
} from '@ionic/vue'

interface Props {
  title: string
  showBack?: boolean
  backHref?: string
}

withDefaults(defineProps<Props>(), {
  showBack: false,
  backHref: '/'
})
</script>

<style scoped lang="scss">
.page-header {
  box-shadow: var(--box-shadow-md);
  padding: 0 var(--spacing-unit);
}
</style>