<template>
  <ion-page>
    <ion-header>
      <ion-toolbar color="primary">
        <ion-title>Mis Grupos</ion-title>
        <ion-buttons slot="end">
          <ion-button fill="clear">
            <ion-icon :icon="person" slot="icon-only"></ion-icon>
          </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding">
      <!-- Cuando SÍ tiene grupos -->
      <div v-if="grupos.length > 0">
        <div class="grupos-grid">
          <ion-card 
            v-for="grupo in grupos" 
            :key="grupo.id" 
            class="grupo-card"
            button
            @click="entrarEnGrupo(grupo.id)"
          >
            <ion-card-header>
              <div class="grupo-avatar">
                <ion-icon :icon="people" color="primary"></ion-icon>
              </div>
              <ion-card-title class="grupo-nombre">{{ grupo.nombre }}</ion-card-title>
              <ion-card-subtitle class="grupo-codigo">
                <ion-icon :icon="key" color="medium"></ion-icon>
                {{ grupo.codigoInvitacion }}
              </ion-card-subtitle>
            </ion-card-header>
            <ion-card-content>
              <div class="grupo-stats">
                <div class="stat-item">
                  <ion-icon :icon="person" color="medium"></ion-icon>
                  <span>{{ grupo.participantes?.length || 0 }} miembros</span>
                </div>
                <div class="stat-item">
                  <ion-icon :icon="calendar" color="medium"></ion-icon>
                  <span>{{ grupo.eventos?.length || 0 }} eventos</span>
                </div>
              </div>
            </ion-card-content>
          </ion-card>
        </div>
      </div>

      <!-- Cuando NO tiene grupos -->
      <div v-else class="no-grupos-container">
        <div class="welcome-section">
          <ion-icon :icon="people" class="welcome-icon" color="primary"></ion-icon>
          <ion-text>
            <h2 class="ion-text-center">¡Bienvenido!</h2>
            <p class="ion-text-center welcome-text">
              Aún no perteneces a ningún grupo. Crea uno nuevo o únete a uno existente.
            </p>
          </ion-text>
        </div>

        <div class="actions-section">
          <ion-button 
            expand="block" 
            size="large"
            class="ion-margin-bottom" 
            @click="crearGrupo"
            :disabled="cargando"
          >
            <ion-icon :icon="add" slot="start"></ion-icon>
            Crear nuevo grupo
          </ion-button>

          <div class="divider">
            <span>o</span>
          </div>

          <ion-item class="codigo-input">
            <ion-input
              v-model="codigo"
              label="Código de invitación"
              placeholder="Introduce el código del grupo"
              fill="outline"
              :disabled="cargando"
            ></ion-input>
          </ion-item>

          <ion-button 
            expand="block" 
            size="large"
            fill="outline"
            class="ion-margin-top" 
            @click="unirseGrupo"
            :disabled="cargando || !codigo.trim()"
          >
            <ion-icon :icon="logIn" slot="start"></ion-icon>
            Unirse al grupo
          </ion-button>
        </div>

        <ion-text color="danger" v-if="error" class="error-text">
          <p class="ion-text-center">{{ error }}</p>
        </ion-text>
      </div>

      <!-- Modal para crear/unirse cuando ya tiene grupos -->
      <ion-modal :is-open="modalAbierto" @did-dismiss="cerrarModal">
        <ion-header>
          <ion-toolbar>
            <ion-title>Gestionar Grupos</ion-title>
            <ion-buttons slot="end">
              <ion-button @click="cerrarModal">
                <ion-icon :icon="close"></ion-icon>
              </ion-button>
            </ion-buttons>
          </ion-toolbar>
        </ion-header>
        <ion-content class="ion-padding">
          <div class="modal-actions">
            <ion-button 
              expand="block" 
              size="large"
              class="ion-margin-bottom" 
              @click="crearGrupo"
              :disabled="cargando"
            >
              <ion-icon :icon="add" slot="start"></ion-icon>
              Crear nuevo grupo
            </ion-button>

            <div class="divider">
              <span>o</span>
            </div>

            <ion-item class="codigo-input">
              <ion-input
                v-model="codigo"
                label="Código de invitación"
                placeholder="Introduce el código del grupo"
                fill="outline"
                :disabled="cargando"
              ></ion-input>
            </ion-item>

            <ion-button 
              expand="block" 
              size="large"
              fill="outline"
              class="ion-margin-top" 
              @click="unirseGrupo"
              :disabled="cargando || !codigo.trim()"
            >
              <ion-icon :icon="logIn" slot="start"></ion-icon>
              Unirse al grupo
            </ion-button>

            <ion-text color="danger" v-if="error" class="error-text">
              <p class="ion-text-center">{{ error }}</p>
            </ion-text>
          </div>
        </ion-content>
      </ion-modal>
    </ion-content>

    <!-- FAB solo cuando tiene grupos -->
    <ion-fab v-if="grupos.length > 0" slot="fixed" vertical="bottom" horizontal="end">
      <ion-fab-button @click="abrirModal" color="primary">
        <ion-icon :icon="add"></ion-icon>
      </ion-fab-button>
    </ion-fab>

    <!-- Loading overlay -->
    <ion-loading :is-open="cargando" message="Cargando..."></ion-loading>
  </ion-page>
</template>

<script setup>
import {
  IonPage,
  IonHeader,
  IonToolbar,
  IonTitle,
  IonContent,
  IonCard,
  IonCardHeader,
  IonCardTitle,
  IonCardSubtitle,
  IonCardContent,
  IonInput,
  IonItem,
  IonText,
  IonButton,
  IonButtons,
  IonIcon,
  IonFab,
  IonFabButton,
  IonModal,
  IonLoading
} from '@ionic/vue'
import { 
  person, 
  people, 
  key, 
  calendar, 
  add, 
  logIn, 
  close 
} from 'ionicons/icons'
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const usuario = ref(null)
const codigo = ref('')
const error = ref('')
const grupos = ref([])
const cargando = ref(false)
const modalAbierto = ref(false)

onMounted(async () => {
  const stored = localStorage.getItem('usuario')
  if (stored) {
    usuario.value = JSON.parse(stored)
    await cargarGrupos()
  }
})

const cargarGrupos = async () => {
  if (!usuario.value) return

  cargando.value = true
  try {
    const res = await fetch(`${import.meta.env.VITE_API_URL}/grupos/usuarios/${usuario.value.id}/grupos`)
    if (res.ok) {
      grupos.value = await res.json()
    } else {
      grupos.value = []
    }
  } catch (err) {
    console.error('Error al cargar grupos:', err)
    grupos.value = []
    // Datos de ejemplo para mostrar el diseño
    grupos.value = [
      {
        id: 1,
        nombre: 'Familia García',
        codigoInvitacion: 'FAM123',
        participantes: [{ id: 1 }, { id: 2 }, { id: 3 }],
        eventos: [{ id: 1 }, { id: 2 }]
      },
      {
        id: 2,
        nombre: 'Amigos Universidad',
        codigoInvitacion: 'UNI456',
        participantes: [{ id: 1 }, { id: 2 }],
        eventos: [{ id: 1 }]
      }
    ]
  }
  cargando.value = false
}

const crearGrupo = async () => {
  error.value = ''
  cargando.value = true
  
  try {
    // 1. Crear grupo
    const resGrupo = await fetch(`${import.meta.env.VITE_API_URL}/grupos`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ nombre: 'Nuevo Grupo' })
    })
    
    if (!resGrupo.ok) throw new Error('Error al crear grupo')
    
    const grupo = await resGrupo.json()

    // 2. Asociar usuario
    await fetch(`${import.meta.env.VITE_API_URL}/grupos/${grupo.id}/usuarios`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        nombre: usuario.value.nombre,
        email: usuario.value.email,
        password: usuario.value.password || '1234'
      })
    })

    localStorage.setItem('grupoActivoId', grupo.id)
    cerrarModal()
    router.push(`/dashboard/${grupo.id}`)
  } catch (err) {
    error.value = 'Error al crear el grupo. Intenta de nuevo.'
  }
  
  cargando.value = false
}

const unirseGrupo = async () => {
  if (!codigo.value.trim()) {
    error.value = 'Por favor introduce un código válido'
    return
  }

  error.value = ''
  cargando.value = true
  
  try {
    const resGrupo = await fetch(`${import.meta.env.VITE_API_URL}/grupos/invitacion/${codigo.value}`)
    if (!resGrupo.ok) throw new Error('Código no válido')
    
    const grupo = await resGrupo.json()

    // Asociar usuario
    await fetch(`${import.meta.env.VITE_API_URL}/grupos/${grupo.id}/usuarios`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        nombre: usuario.value.nombre,
        email: usuario.value.email,
        password: usuario.value.password 
      })
    })

    localStorage.setItem('grupoActivoId', grupo.id)
    cerrarModal()
    router.push(`/dashboard/${grupo.id}`)
  } catch (err) {
    error.value = 'No se pudo unir al grupo. Verifica el código.'
  }
  
  cargando.value = false
}

const entrarEnGrupo = (id) => {
  localStorage.setItem('grupoActivoId', id)
  router.push(`/dashboard/${id}`)
}

const abrirModal = () => {
  codigo.value = ''
  error.value = ''
  modalAbierto.value = true
}

const cerrarModal = () => {
  modalAbierto.value = false
  codigo.value = ''
  error.value = ''
}
</script>

<style scoped>
.grupos-grid {
  display: grid;
  gap: 16px;
  margin-bottom: 80px; /* Espacio para el FAB */
}

.grupo-card {
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.grupo-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.grupo-avatar {
  width: 60px;
  height: 60px;
  background-color: #f0f4ff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px auto;
}

.grupo-avatar ion-icon {
  font-size: 28px;
}

.grupo-nombre {
  text-align: center;
  font-size: 1.3rem;
  font-weight: 600;
  margin-bottom: 8px;
  color: #2c3e50;
}

.grupo-codigo {
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  font-size: 0.9rem;
  margin-bottom: 16px;
}

.grupo-stats {
  display: flex;
  justify-content: space-around;
  margin-top: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.85rem;
  color: #7f8c8d;
}

.no-grupos-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  text-align: center;
}

.welcome-section {
  margin-bottom: 32px;
}

.welcome-icon {
  font-size: 80px;
  margin-bottom: 16px;
}

.welcome-text {
  color: #7f8c8d;
  font-size: 1rem;
  line-height: 1.5;
  margin: 16px 0;
}

.actions-section {
  width: 100%;
  max-width: 300px;
}

.divider {
  text-align: center;
  margin: 24px 0;
  position: relative;
}

.divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background-color: #e0e0e0;
}

.divider span {
  background-color: white;
  padding: 0 16px;
  color: #7f8c8d;
  font-size: 0.9rem;
}

.codigo-input {
  margin: 16px 0;
  --border-radius: 12px;
}

.error-text {
  margin-top: 16px;
}

.modal-actions {
  max-width: 300px;
  margin: 0 auto;
  padding-top: 20px;
}

/* Responsive */
@media (min-width: 768px) {
  .grupos-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (min-width: 1024px) {
  .grupos-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>