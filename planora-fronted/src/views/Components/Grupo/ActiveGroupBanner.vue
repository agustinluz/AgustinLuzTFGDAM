<template>
  <div v-if="grupo" class="banner-grupo-activo">
    <div class="banner-content">
      <div class="banner-avatar">
        <img v-if="grupo.imagenPerfil" :src="grupo.imagenPerfil" :alt="grupo.nombre" />
        <ion-icon v-else :icon="people" color="light" />
      </div>
      <div class="banner-info">
        <h3>{{ grupo.nombre }}</h3>
        <p>Grupo activo</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { IonIcon } from '@ionic/vue'
import { people } from 'ionicons/icons'

defineProps<{ grupo: any | null }>()
</script>

<style scoped>
.banner-grupo-activo {
  background: linear-gradient(135deg, var(--ion-color-primary) 0%, var(--ion-color-primary-shade) 100%);
  color: white;
  padding: 16px 20px;
  margin-bottom: 20px;
}
.banner-content {
  display: flex;
  align-items: center;
  gap: 12px;
}
.banner-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
}
.banner-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.banner-info h3 {
  margin: 0 0 4px 0;
  font-size: 1.1em;
  font-weight: 600;
}
.banner-info p {
  margin: 0;
  font-size: 0.85em;
  opacity: 0.8;
}
</style>