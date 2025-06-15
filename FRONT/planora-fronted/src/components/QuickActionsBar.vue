<template>
  <ion-footer v-if="show" class="quick-footer">
    <ion-toolbar>
      <div class="quick-actions-container">
        <div 
          v-for="item in items" 
          :key="item.label" 
          class="quick-action-item"
          :class="{ 'active': isActive(item.path) }"
          @click="navigate(item.path)"
        >
          <ion-icon :icon="item.icon" class="quick-icon" />
          <span class="quick-label">{{ item.label }}</span>
        </div>
      </div>
    </ion-toolbar>
  </ion-footer>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  IonFooter,
  IonToolbar,
  IonIcon
} from '@ionic/vue'
import {
  cash,
  calendarOutline,
  checkboxOutline,
  images,
  documentOutline
} from 'ionicons/icons'

const route = useRoute()
const router = useRouter()
const grupoId = computed(() => localStorage.getItem('grupoActivoId') || '')

interface Item { 
  label: string
  icon: any
  path: string 
}

const items = computed<Item[]>(() => [
  { label: 'Gastos',  icon: cash,            path: `/dashboard/${grupoId.value}/gastos` },
  { label: 'Eventos', icon: calendarOutline, path: `/dashboard/${grupoId.value}/eventos` },
  { label: 'Votos',   icon: checkboxOutline, path: `/dashboard/${grupoId.value}/votaciones` },
  { label: 'Galería', icon: images,          path: `/dashboard/${grupoId.value}/galeria` },
  { label: 'Notas',   icon: documentOutline, path: `/dashboard/${grupoId.value}/notas` }
])

const hide = ['/login', '/registro', '/restablecer', '/grupo']
const show = computed(() => {
  const p = route.path
  if (hide.includes(p) || /\/crear|\/editar/.test(p)) return false
  return !!grupoId.value
})

function navigate(path: string) {
  if (route.path === path) {
    router.go(0)
  } else {
    router.push(path)
  }
}

function isActive(path: string) {
  return route.path === path || route.path.startsWith(path)
}
</script>

<style scoped>
.quick-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  --background: rgba(255, 255, 255, 0.95);
  --border-color: rgba(0, 0, 0, 0.12);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  box-shadow: 0 -1px 10px rgba(0, 0, 0, 0.1);
}

.quick-footer ion-toolbar {
  --background: transparent;
  --border-width: 1px 0 0 0;
  --border-style: solid;
  --border-color: var(--border-color);
  --padding-start: 0;
  --padding-end: 0;
  --padding-top: 8px;
  --padding-bottom: calc(8px + env(safe-area-inset-bottom, 0px));
  --min-height: 70px;
  height: auto;
}

.quick-actions-container {
  display: flex;
  justify-content: space-around;
  align-items: center;
  width: 100%;
  padding: 8px 16px 12px 16px;
  box-sizing: border-box;
}

.quick-action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 8px 12px;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0.0, 0.2, 1);
  min-width: 52px;
  position: relative;
  flex: 1;
  max-width: 80px;
  user-select: none;
  -webkit-tap-highlight-color: transparent;
}

.quick-action-item:active {
  transform: scale(0.95);
}

.quick-action-item.active {
  background-color: rgba(var(--ion-color-primary-rgb), 0.15);
}

.quick-action-item.active::before {
  content: '';
  position: absolute;
  top: -4px;
  left: 50%;
  transform: translateX(-50%);
  width: 24px;
  height: 3px;
  background-color: var(--ion-color-primary);
  border-radius: 2px;
}

.quick-action-item.active .quick-icon {
  color: var(--ion-color-primary);
  transform: scale(1.1);
}

.quick-action-item.active .quick-label {
  color: var(--ion-color-primary);
  font-weight: 600;
}

.quick-icon {
  font-size: 24px;
  margin-bottom: 4px;
  color: var(--ion-color-medium);
  transition: all 0.3s cubic-bezier(0.4, 0.0, 0.2, 1);
}

.quick-label {
  font-size: 11px;
  font-weight: 500;
  color: var(--ion-color-medium-shade);
  text-align: center;
  line-height: 1.2;
  transition: all 0.3s cubic-bezier(0.4, 0.0, 0.2, 1);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
}

/* Responsive adjustments */
@media (max-width: 320px) {
  .quick-actions-container {
    padding: 6px 8px 10px 8px;
  }
  
  .quick-action-item {
    padding: 6px 8px;
    min-width: 44px;
  }
  
  .quick-icon {
    font-size: 22px;
  }
  
  .quick-label {
    font-size: 10px;
  }
  
  .quick-footer ion-toolbar {
    --min-height: 65px;
  }
}

@media (min-width: 768px) {
  .quick-actions-container {
    max-width: 500px;
    margin: 0 auto;
    padding: 10px 20px 14px 20px;
  }
  
  .quick-footer ion-toolbar {
    --min-height: 75px;
  }
}

/* Dark mode support */
@media (prefers-color-scheme: dark) {
  .quick-footer {
    --background: rgba(18, 18, 18, 0.95);
    --border-color: rgba(255, 255, 255, 0.12);
    box-shadow: 0 -1px 10px rgba(0, 0, 0, 0.3);
  }
}

/* iOS safe area support */
@supports (padding-bottom: env(safe-area-inset-bottom)) {
  .quick-footer ion-toolbar {
    --padding-bottom: calc(12px + env(safe-area-inset-bottom));
  }
}

/* Animaciones más fluidas en dispositivos de alta gama */
@media (prefers-reduced-motion: no-preference) {
  .quick-action-item:hover:not(.active) {
    background-color: rgba(var(--ion-color-light-rgb), 0.8);
    transform: translateY(-2px);
  }
}

/* Eliminar animaciones si el usuario lo prefiere */
@media (prefers-reduced-motion: reduce) {
  .quick-action-item,
  .quick-icon,
  .quick-label {
    transition: none;
  }
}
</style>