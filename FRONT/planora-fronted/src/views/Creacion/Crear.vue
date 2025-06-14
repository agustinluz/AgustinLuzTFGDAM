<!-- src/views/Creacion/Crear.vue -->
<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-buttons slot="start">
          <ion-back-button @click="handleGoBack" color="light"></ion-back-button>
        </ion-buttons>
        <ion-title>Crear nuevo elemento</ion-title>
      </ion-toolbar>
    </ion-header>
    
    <ion-content class="ion-padding">
      <div class="welcome-section">
        <h2>¿Qué deseas crear?</h2>
        <p>Selecciona el tipo de elemento que quieres añadir al grupo</p>
      </div>

      <ion-grid>
        <ion-row>
          <ion-col size="12" size-md="6">
            <ion-card 
              button 
              @click="navigateToCreate('evento')"
              class="creation-card"
            >
              <ion-card-content class="ion-text-center">
                <ion-icon name="calendar-outline" class="card-icon"></ion-icon>
                <h3>Evento</h3>
                <p>Organiza actividades y reuniones</p>
              </ion-card-content>
            </ion-card>
          </ion-col>
          
          <ion-col size="12" size-md="6">
            <ion-card 
              button 
              @click="navigateToCreate('gasto')"
              class="creation-card"
            >
              <ion-card-content class="ion-text-center">
                <ion-icon name="card-outline" class="card-icon"></ion-icon>
                <h3>Gasto</h3>
                <p>Registra gastos compartidos</p>
              </ion-card-content>
            </ion-card>
          </ion-col>
        </ion-row>
        
        <ion-row>
          <ion-col size="12" size-md="6">
            <ion-card 
              button 
              @click="navigateToCreate('votacion')"
              class="creation-card"
            >
              <ion-card-content class="ion-text-center">
                <ion-icon name="checkbox-outline" class="card-icon"></ion-icon>
                <h3>Votación</h3>
                <p>Toma decisiones grupales</p>
              </ion-card-content>
            </ion-card>
          </ion-col>
          
          <ion-col size="12" size-md="6">
            <ion-card 
              button 
              @click="navigateToCreate('nota')"
              class="creation-card"
            >
              <ion-card-content class="ion-text-center">
                <ion-icon name="document-text-outline" class="card-icon"></ion-icon>
                <h3>Nota</h3>
                <p>Comparte información importante</p>
              </ion-card-content>
            </ion-card>
          </ion-col>
        </ion-row>
<ion-row>
  <ion-col  size-md="12">
            <ion-card 
              button 
              @click="navigateToCreate('imagen')"
              class="creation-card"
            >
              <ion-card-content class="ion-text-center">
                <ion-icon name="document-text-outline" class="camera-icon"></ion-icon>
                <h3>IMAGEN</h3>
                <p>Comparte información importante</p>
              </ion-card-content>
            </ion-card>
          </ion-col>
</ion-row>
        
      </ion-grid>
    </ion-content>
  </ion-page>
</template>

<script setup>
import {
  IonPage, IonHeader, IonToolbar, IonTitle, IonContent, IonGrid, 
  IonRow, IonCol, IonCard, IonCardContent, IonIcon, IonButtons, 
  IonBackButton, useIonRouter
} from '@ionic/vue'
import { useRoute } from 'vue-router' // Importar desde vue-router
import { ref, onMounted } from 'vue'

const router = useIonRouter()
const route = useRoute()

const grupoId = ref(null)
const isLoading = ref(true)

onMounted(async () => {
  try {
    // Obtener grupoId desde los parámetros de la ruta
    grupoId.value = route.params.id
    
    if (!grupoId.value) {
      // Fallback: intentar obtener desde localStorage si no está en la ruta
      const usuario = JSON.parse(localStorage.getItem('usuario') || '{}')
      if (usuario.grupoIds && usuario.grupoIds.length > 0) {
        grupoId.value = usuario.grupoIds[0]
      } else {
        throw new Error('No se pudo determinar el grupo')
      }
    }
  } catch (error) {
    console.error('Error al obtener grupo:', error)
    // Navegar de vuelta si hay error
    router.push('/grupo')
  } finally {
    isLoading.value = false
  }
})

const navigateToCreate = (tipo) => {
  if (!grupoId.value) {
    console.error('Grupo ID no disponible')
    return
  }
  
  router.push(`/dashboard/${grupoId.value}/crear/${tipo}`)
}

const handleGoBack = () => {
  router.back()
}
</script>

<style scoped>
.welcome-section {
  text-align: center;
  margin-bottom: 2rem;
}

.welcome-section h2 {
  color: var(--ion-color-primary);
  margin-bottom: 0.5rem;
}

  .welcome-section p {
    color: var(--ion-color-medium);
    font-size: var(--font-size-md);
  }

.creation-card {
  height: 160px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.creation-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

  .card-icon {
    font-size: var(--font-size-xl);
    color: var(--ion-color-primary);
    margin-bottom: 0.5rem;
  }

.creation-card h3 {
  margin: 0.5rem 0;
  color: var(--ion-color-dark);
}

  .creation-card p {
    color: var(--ion-color-medium);
    font-size: var(--font-size-sm);
    margin: 0;
  }
</style>