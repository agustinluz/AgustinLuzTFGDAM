<template>
  <ion-app>
    <AppMenu />
    <ion-router-outlet id="main-content" :key="route.fullPath" />
  </ion-app>
</template>

<script setup lang="ts">
import { IonApp, IonRouterOutlet } from '@ionic/vue';
import { useRoute } from 'vue-router';
import AppMenu from '@/components/AppMenu.vue';

const route = useRoute();
</script>
