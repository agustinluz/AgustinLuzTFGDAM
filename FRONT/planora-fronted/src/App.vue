<template>
  <ion-app>
    <AppMenu />
    <ion-page id="main-page" :class="{ 'has-quick-footer': showQuickActions }">
      <ion-router-outlet 
        id="main-content" 
        :key="route.fullPath"
        class="main-content"
      />
      <QuickActionsBar />
    </ion-page>
  </ion-app>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { IonApp, IonPage, IonRouterOutlet } from '@ionic/vue'
import { useRoute } from 'vue-router'
import AppMenu from '@/components/AppMenu.vue'
import QuickActionsBar from '@/components/QuickActionsBar.vue'

const route = useRoute()

// Determinar si se muestran las acciones rápidas
const hideQuickActions = ['/login', '/registro', '/restablecer', '/grupo']
const showQuickActions = computed(() => {
  const path = route.path
  if (hideQuickActions.includes(path)) return false
  const grupoId = localStorage.getItem('grupoActivoId')
  return !!grupoId
})
</script>

<style scoped>
/* Layout principal con footer fijo */
#main-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  --quick-footer-height: 70px;
}

/* Cuando hay quick actions, ajustar el layout */
#main-page.has-quick-footer {
  padding-bottom: var(--quick-footer-height);
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* Asegurar que ion-content ocupe el espacio disponible */
.main-content ::v-deep(ion-page) {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.main-content ::v-deep(ion-header) {
  flex-shrink: 0;
}

.main-content ::v-deep(ion-content) {
  flex: 1;
  --padding-bottom: 0;
  --offset-bottom: 0;
}

/* Safe areas para dispositivos con notch */
@supports (padding-bottom: env(safe-area-inset-bottom)) {
  #main-page {
    --quick-footer-height: calc(70px + env(safe-area-inset-bottom));
  }
}

/* Responsive adjustments */
@media (max-width: 320px) {
  #main-page {
    --quick-footer-height: 65px;
  }
}

@media (min-width: 768px) {
  #main-page {
    --quick-footer-height: 75px;
  }
}

/* Dark mode support */
@media (prefers-color-scheme: dark) {
  .main-content ::v-deep(ion-page) {
    background: var(--ion-color-dark);
  }
}
</style>