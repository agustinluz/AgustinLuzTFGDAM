<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-title>Votaciones del grupo</ion-title>
        <ion-buttons slot="end">
          <ion-button @click="refrescarVotaciones">
            <ion-icon :icon="refreshOutline"></ion-icon>
          </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding">
      <!-- Loading state -->
      <div v-if="loading" class="ion-text-center ion-padding">
        <ion-spinner></ion-spinner>
        <p>Cargando votaciones...</p>
      </div>

      <!-- Error state -->
      <ion-card v-else-if="error" color="danger">
        <ion-card-content>
          <ion-text color="light">
            <h3>Error al cargar votaciones</h3>
            <p>{{ error }}</p>
          </ion-text>
          <ion-button fill="clear" color="light" @click="refrescarVotaciones">
            Reintentar
          </ion-button>
        </ion-card-content>
      </ion-card>

      <!-- Votaciones list -->
      <div v-else>
        <ion-list v-if="votaciones.length">
          <ion-card v-for="v in votaciones" :key="v.id">
            <ion-card-header>
              <ion-card-title>{{ v.titulo }}</ion-card-title>
              <ion-card-subtitle>{{ v.descripcion }}</ion-card-subtitle>
            </ion-card-header>
            <ion-card-content>
              <div v-if="v.opciones && v.opciones.length">
                <p><strong>Opciones disponibles:</strong></p>
                <ion-radio-group v-model="votoSeleccionado[v.id]">
                  <ion-item v-for="(opcion, index) in v.opciones" :key="index">
                    <ion-label>{{ opcion }}</ion-label>
                    <ion-radio slot="end" :value="opcion"></ion-radio>
                  </ion-item>
                </ion-radio-group>
                
                <div class="ion-margin-top">
                  <ion-button 
                    expand="block" 
                    :disabled="!votoSeleccionado[v.id] || v.estado === 'CERRADA'"
                    @click="enviarVoto(v.id)"
                    :color="v.estado === 'CERRADA' ? 'medium' : 'primary'"
                  >
                    {{ v.estado === 'CERRADA' ? 'Votación Cerrada' : 'Votar' }}
                  </ion-button>
                  
                  <ion-button 
                    fill="outline" 
                    expand="block" 
                    @click="verResultados(v.id)"
                    class="ion-margin-top"
                  >
                    Ver Resultados
                  </ion-button>
                </div>
                
                <!-- Botones del creador -->
                <div v-if="esCreador(v)" class="ion-margin-top">
                  <ion-button 
                    fill="outline" 
                    color="warning"
                    @click="editarVotacion(v)"
                    :disabled="v.estado === 'CERRADA'"
                  >
                    <ion-icon :icon="pencilOutline" slot="start"></ion-icon>
                    Editar
                  </ion-button>
                  
                  <ion-button 
                    fill="outline" 
                    color="success"
                    @click="cerrarVotacion(v.id)"
                    :disabled="v.estado === 'CERRADA'"
                    class="ion-margin-start"
                  >
                    <ion-icon :icon="checkmarkOutline" slot="start"></ion-icon>
                    Cerrar
                  </ion-button>
                  
                  <ion-button 
                    fill="outline" 
                    color="danger"
                    @click="eliminarVotacion(v.id)"
                    class="ion-margin-start"
                  >
                    <ion-icon :icon="trashOutline" slot="start"></ion-icon>
                    Eliminar
                  </ion-button>
                </div>
              </div>
              
              <ion-text v-else color="warning">
                <p>Esta votación no tiene opciones configuradas</p>
              </ion-text>
              
              <!-- Información adicional -->
              <div class="ion-margin-top">
                <ion-chip :color="v.estado === 'ACTIVA' ? 'success' : 'medium'">
                  <ion-label>{{ v.estado }}</ion-label>
                </ion-chip>
                <ion-text color="medium">
                  <p><small>Creada: {{ formatearFecha(v.fechaCreacion) }}</small></p>
                  <p v-if="v.fechaCierre"><small>Cerrada: {{ formatearFecha(v.fechaCierre) }}</small></p>
                  <p><small>Creador: {{ v.creadorNombre || 'Desconocido' }}</small></p>
                </ion-text>
              </div>
            </ion-card-content>
          </ion-card>
        </ion-list>
        
        <ion-card v-else>
          <ion-card-content class="ion-text-center">
            <ion-icon :icon="listOutline" size="large" color="medium"></ion-icon>
            <h3>No hay votaciones activas</h3>
            <p>Aún no se han creado votaciones en este grupo.</p>
          </ion-card-content>
        </ion-card>
      </div>

      <!-- Debug info (remover en producción) -->
      <ion-card v-if="showDebug" color="light">
        <ion-card-header>
          <ion-card-title>Debug Info</ion-card-title>
        </ion-card-header>
        <ion-card-content>
          <p><strong>Grupo ID:</strong> {{ grupoId }}</p>
          <p><strong>URL llamada:</strong> {{ apiUrl }}</p>
          <p><strong>Votaciones encontradas:</strong> {{ votaciones.length }}</p>
          <pre>{{ JSON.stringify(votaciones, null, 2) }}</pre>
        </ion-card-content>
      </ion-card>
    </ion-content>

    <ion-fab vertical="bottom" horizontal="end" slot="fixed">
      <ion-fab-button @click="toggleDebug">
        <ion-icon :icon="bugOutline"></ion-icon>
      </ion-fab-button>
    </ion-fab>
  </ion-page>
</template>

<script setup>
import { 
  IonPage, IonHeader, IonToolbar, IonTitle, IonContent, IonList, IonItem, 
  IonLabel, IonText, IonCard, IonCardContent, IonCardHeader, IonCardTitle, 
  IonCardSubtitle, IonSpinner, IonButton, IonButtons, IonIcon, IonRadioGroup, 
  IonRadio, IonFab, IonFabButton, IonChip
} from '@ionic/vue'
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { refreshOutline, listOutline, bugOutline, pencilOutline, checkmarkOutline, trashOutline } from 'ionicons/icons'

const votaciones = ref([])
const loading = ref(false)
const error = ref(null)
const showDebug = ref(false)
const votoSeleccionado = ref({})
const usuarioActual = ref(null)

const route = useRoute()
const grupoId = route.params.id

const apiUrl = computed(() => `${import.meta.env.VITE_API_URL}/grupos/${grupoId}/votaciones`)
const token = localStorage.getItem('authToken')

// Obtener usuario actual del token
const obtenerUsuarioActual = () => {
  try {
    if (token) {
      // Decodificar JWT para obtener info del usuario
      const payload = JSON.parse(atob(token.split('.')[1]))
      usuarioActual.value = {
        id: payload.userId,
        email: payload.sub,
        nombre: payload.nombre
      }
    }
  } catch (err) {
    console.error('Error al obtener usuario actual:', err)
  }
}

const esCreador = (votacion) => {
  return usuarioActual.value && votacion.creadorId === usuarioActual.value.id
}

const verResultados = async (votacionId) => {
  try {
    const res = await fetch(`${import.meta.env.VITE_API_URL}/votaciones/${votacionId}/resultados`)
    if (res.ok) {
      const resultados = await res.json()
      
      let mensaje = `Resultados de "${resultados.titulo}":\n\n`
      mensaje += `Total de votos: ${resultados.totalVotos}\n\n`
      
      Object.entries(resultados.resultados).forEach(([opcion, votos]) => {
        const porcentaje = resultados.totalVotos > 0 
          ? ((votos / resultados.totalVotos) * 100).toFixed(1) 
          : 0
        mensaje += `${opcion}: ${votos} votos (${porcentaje}%)\n`
      })
      
      alert(mensaje)
    } else {
      throw new Error('Error al obtener resultados')
    }
  } catch (err) {
    console.error('Error al ver resultados:', err)
    alert('Error al obtener los resultados')
  }
}

const editarVotacion = (votacion) => {
  // Aquí podrías abrir un modal de edición
  console.log('Editando votación:', votacion)
  alert('Función de edición pendiente de implementar')
}

const cerrarVotacion = async (votacionId) => {
  try {
    const confirmar = confirm('¿Estás seguro de que quieres cerrar esta votación?')
    if (!confirmar) return
    
    const res = await fetch(`${import.meta.env.VITE_API_URL}/votaciones/${votacionId}/cerrar`, {
      method: 'PUT',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    
    if (res.ok) {
      alert('Votación cerrada correctamente')
      await cargarVotaciones()
    } else {
      throw new Error(`Error ${res.status}`)
    }
  } catch (err) {
    console.error('Error al cerrar votación:', err)
    alert('Error al cerrar la votación')
  }
}

const eliminarVotacion = async (votacionId) => {
  try {
    const confirmar = confirm('¿Estás seguro de que quieres eliminar esta votación? Esta acción no se puede deshacer.')
    if (!confirmar) return
    
    const token = localStorage.getItem('authToken')
    const res = await fetch(`${import.meta.env.VITE_API_URL}/votaciones/${votacionId}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    
    if (res.ok || res.status === 204) {
      alert('Votación eliminada correctamente')
      await cargarVotaciones()
    } else {
      throw new Error(`Error ${res.status}`)
    }
  } catch (err) {
    console.error('Error al eliminar votación:', err)
    alert('Error al eliminar la votación')
  }
}

const cargarVotaciones = async () => {
  loading.value = true
  error.value = null
  
  try {
    console.log('Cargando votaciones para grupo:', grupoId)
    console.log('URL completa:', apiUrl.value)
    
    const res = await fetch(apiUrl.value, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        // Agregar token si es necesario
         'Authorization': `Bearer ${token}`
      }
    })
    
    console.log('Response status:', res.status)
    console.log('Response ok:', res.ok)
    
    if (!res.ok) {
      throw new Error(`Error ${res.status}: ${res.statusText}`)
    }
    
    const data = await res.json()
    console.log('Datos recibidos:', data)
    
    votaciones.value = data || []
    
  } catch (err) {
    console.error('Error al cargar votaciones:', err)
    error.value = err.message || 'Error desconocido al cargar votaciones'
  } finally {
    loading.value = false
  }
}

const refrescarVotaciones = () => {
  cargarVotaciones()
}

const enviarVoto = async (votacionId) => {
  try {
    const voto = votoSeleccionado.value[votacionId]
    if (!voto) return
    
    console.log('Enviando voto:', { votacionId, voto })
    
    const res = await fetch(`${import.meta.env.VITE_API_URL}/votaciones/${votacionId}/votar`, {
      method: 'POST',
      headers: { 
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({ opcion: voto })
    })
    
    if (res.ok) {
      alert(`¡Voto enviado correctamente: ${voto}!`)
      // Refrescar las votaciones para mostrar estados actualizados
      await cargarVotaciones()
    } else if (res.status === 409) {
      alert('Ya has votado en esta votación')
    } else if (res.status === 403) {
      alert('No tienes permisos para votar en esta votación')
    } else {
      throw new Error(`Error ${res.status}`)
    }
    
  } catch (err) {
    console.error('Error al enviar voto:', err)
    alert('Error al enviar el voto: ' + err.message)
  }
}

const formatearFecha = (fecha) => {
  if (!fecha) return 'Fecha no disponible'
  return new Date(fecha).toLocaleDateString('es-ES', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const toggleDebug = () => {
  showDebug.value = !showDebug.value
}

onMounted(() => {
  console.log('Componente montado, grupo ID:', grupoId)
  obtenerUsuarioActual()
  cargarVotaciones()
})
</script>

<style scoped>
ion-card {
  margin: 8px 0;
}

.debug-info {
  font-family: monospace;
  font-size: 0.8em;
  white-space: pre-wrap;
}
</style>