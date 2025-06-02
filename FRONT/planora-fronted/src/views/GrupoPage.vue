<template>
  <ion-page>
    <!-- Header -->
    <ion-header>
      <ion-toolbar color="primary">
        <ion-title>Mis Grupos</ion-title>
        <ion-buttons slot="end">
          <ion-button fill="clear" @click="() => router.push('/perfil')">
            <ion-icon :icon="person" slot="icon-only"></ion-icon>
          </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>

    <ion-content :fullscreen="true" class="ion-padding">
      <!-- Cuando SÍ tiene grupos -->
      <div v-if="grupos.length > 0">
        <ion-grid>
          <ion-row>
            <ion-col 
              size="12" 
              size-md="6" 
              size-lg="4" 
              v-for="grupo in grupos" 
              :key="grupo.id"
            >
              <ion-card 
                button 
                class="grupo-card"
                @click="entrarEnGrupo(grupo.id)"
              >
                <!-- Header del grupo -->
                <ion-card-content class="ion-text-center">
                  <div class="grupo-avatar">
                    <img 
                      v-if="grupo.imagenPerfil" 
                      :src="grupo.imagenPerfil" 
                      :alt="grupo.nombre"
                    />
                    <ion-icon v-else :icon="people" color="primary"></ion-icon>
                  </div>
                  
                  <ion-card-title class="grupo-nombre">{{ grupo.nombre }}</ion-card-title>
                  
                  <div class="codigo-container">
                    <ion-icon :icon="key" size="small"></ion-icon>
                    <span>{{ grupo.codigoInvitacion }}</span>
                    <ion-chip v-if="esAdmin(grupo)" color="primary" outline>
                      <ion-label>Admin</ion-label>
                    </ion-chip>
                  </div>

                  <!-- Stats del grupo -->
                  <div class="grupo-stats">
                    <div class="stat-item">
                      <ion-icon :icon="camera" size="small"></ion-icon>
                      <span>{{ grupo.participantes?.length || 0 }} miembros</span>
                    </div>
                    <div class="stat-item">
                      <ion-icon :icon="calendar" size="small"></ion-icon>
                      <span>{{ grupo.eventos?.length || 0 }} eventos</span>
                    </div>
                  </div>
                </ion-card-content>
              </ion-card>
            </ion-col>
          </ion-row>
        </ion-grid>
      </div>

      <!-- Cuando NO tiene grupos -->
      <div v-else class="estado-vacio">
        <div class="contenido-vacio">
          <ion-icon :icon="people" size="large" color="primary"></ion-icon>
          <h2>¡Bienvenido!</h2>
          <p>Aún no perteneces a ningún grupo. Crea uno nuevo o únete a uno existente.</p>
        </div>

        <div class="acciones-vacias">
          <ion-button 
            expand="block" 
            @click="abrirModalCrear"
            :disabled="cargando"
          >
            <ion-icon :icon="add" slot="start"></ion-icon>
            Crear nuevo grupo
          </ion-button>

          <div class="separador">o</div>

          <ion-item>
            <ion-input
              v-model="codigo"
              placeholder="Código de invitación"
              :disabled="cargando"
            ></ion-input>
          </ion-item>

          <ion-button 
            expand="block" 
            fill="outline" 
            @click="unirseGrupo"
            :disabled="cargando || !codigo.trim()"
          >
            <ion-icon :icon="logIn" slot="start"></ion-icon>
            Unirse al grupo
          </ion-button>

          <ion-text color="danger" v-if="error">
            <p>{{ error }}</p>
          </ion-text>
        </div>
      </div>

      <!-- FAB solo cuando tiene grupos -->
      <ion-fab 
        v-if="grupos.length > 0" 
        vertical="bottom" 
        horizontal="end" 
        slot="fixed"
      >
        <ion-fab-button @click="abrirModal">
          <ion-icon :icon="add"></ion-icon>
        </ion-fab-button>
      </ion-fab>
    </ion-content>

    <!-- Modal para crear grupo -->
    <ion-modal :is-open="modalCrearGrupo" @didDismiss="cerrarModalCrear">
      <ion-header>
        <ion-toolbar>
          <ion-title>Crear Nuevo Grupo</ion-title>
          <ion-buttons slot="end">
            <ion-button @click="cerrarModalCrear">
              <ion-icon :icon="close"></ion-icon>
            </ion-button>
          </ion-buttons>
        </ion-toolbar>
      </ion-header>
      
      <ion-content class="ion-padding">
        <!-- Selector de imagen -->
        <div class="selector-imagen">
          <div class="preview-imagen" @click="$refs.fileInput.click()">
            <img 
              v-if="previsualizacionImagen" 
              :src="previsualizacionImagen" 
              alt="Preview"
            />
            <ion-icon v-else :icon="people" size="large"></ion-icon>
            <div class="overlay-upload">
              <ion-icon :icon="cloudUpload" color="light"></ion-icon>
            </div>
          </div>
          <p>Toca para agregar foto</p>
          <input 
            ref="fileInput"
            type="file" 
            accept="image/*" 
            @change="manejarImagenGrupo"
            style="display: none"
          />
        </div>

        <!-- Nombre del grupo -->
        <ion-item>
          <ion-input
            v-model="nombreGrupo"
            placeholder="Nombre del grupo"
            :clear-input="true"
          ></ion-input>
        </ion-item>

        <ion-text color="danger" v-if="error">
          <p>{{ error }}</p>
        </ion-text>

        <ion-button 
          expand="block" 
          @click="crearGrupo"
          :disabled="cargando || !nombreGrupo.trim()"
          class="ion-margin-top"
        >
          {{ cargando ? 'Creando...' : 'Crear Grupo' }}
        </ion-button>
      </ion-content>
    </ion-modal>

    <!-- Modal para unirse a grupo cuando ya tiene grupos -->
    <ion-modal :is-open="modalAbierto" @didDismiss="cerrarModal">
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
        <ion-button 
          expand="block" 
          @click="() => { cerrarModal(); abrirModalCrear(); }"
          :disabled="cargando"
        >
          <ion-icon :icon="add" slot="start"></ion-icon>
          Crear nuevo grupo
        </ion-button>

        <div class="separador">o</div>

        <ion-item>
          <ion-input
            v-model="codigo"
            placeholder="Código de invitación"
            :disabled="cargando"
            :clear-input="true"
          ></ion-input>
        </ion-item>

        <ion-button 
          expand="block" 
          fill="outline" 
          @click="unirseGrupo"
          :disabled="cargando || !codigo.trim()"
          class="ion-margin-top"
        >
          <ion-icon :icon="logIn" slot="start"></ion-icon>
          Unirse al grupo
        </ion-button>

        <ion-text color="danger" v-if="error">
          <p>{{ error }}</p>
        </ion-text>
      </ion-content>
    </ion-modal>

    <!-- Loading -->
    <ion-loading
      :is-open="cargando"
      message="Cargando..."
    ></ion-loading>
  </ion-page>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  IonPage,
  IonHeader,
  IonToolbar,
  IonTitle,
  IonContent,
  IonButtons,
  IonButton,
  IonIcon,
  IonCard,
  IonCardContent,
  IonCardTitle,
  IonGrid,
  IonRow,
  IonCol,
  IonChip,
  IonLabel,
  IonItem,
  IonInput,
  IonText,
  IonFab,
  IonFabButton,
  IonModal,
  IonLoading,
  toastController
} from '@ionic/vue'
import {
  camera,
  people,
  key,
  calendar,
  add,
  logIn,
  close,
  cloudUpload,
  person
} from 'ionicons/icons'

// Estados reactivos
const router = useRouter()
const usuario = ref(null)
const codigo = ref('')
const error = ref('')
const grupos = ref([])
const cargando = ref(false)
const modalAbierto = ref(false)
const modalCrearGrupo = ref(false)

// Estados para crear grupo
const nombreGrupo = ref('')
const imagenGrupo = ref('')
const previsualizacionImagen = ref('')

// Lifecycle
onMounted(() => {
  const stored = localStorage.getItem('usuario')
  if (stored) {
    usuario.value = JSON.parse(stored)
    cargarGrupos()
  }
})

// Métodos
const cargarGrupos = async () => {
  if (!usuario.value) return

  cargando.value = true
  try {
    const res = await fetch(`${import.meta.env.VITE_API_URL}/grupos/usuario/${usuario.value.id}`)
    if (res.ok) {
      grupos.value = await res.json()
    } else {
      grupos.value = []
    }
  } catch (err) {
    console.error('Error al cargar grupos:', err)
  }
  cargando.value = false
}

const manejarImagenGrupo = (event) => {
  const file = event.target.files[0]
  if (file) {
    if (file.size > 5 * 1024 * 1024) { // 5MB
      error.value = 'La imagen no puede ser mayor a 5MB'
      return
    }
    
    const reader = new FileReader()
    reader.onload = (e) => {
      const base64 = e.target.result
      imagenGrupo.value = base64
      previsualizacionImagen.value = base64
    }
    reader.readAsDataURL(file)
  }
}

const crearGrupo = async () => {
  if (!nombreGrupo.value.trim()) {
    error.value = 'Por favor introduce un nombre para el grupo'
    return
  }

  error.value = ''
  cargando.value = true
  
  try {
    const res = await fetch(`${import.meta.env.VITE_API_URL}/grupos`, {
      method: 'POST',
      headers: { 
        'Content-Type': 'application/json',
        'usuarioId': usuario.value.id.toString()
      },
      body: JSON.stringify({ 
        nombre: nombreGrupo.value,
        imagenPerfil: imagenGrupo.value 
      })
    })
    
    if (!res.ok) throw new Error('Error al crear grupo')
    
    const grupo = await res.json()
    localStorage.setItem('grupoActivoId', grupo.id)
    cerrarModalCrear()
    
    // Mostrar toast de éxito
    const toast = await toastController.create({
      message: `Grupo "${grupo.nombre}" creado exitosamente. Código: ${grupo.codigoInvitacion}`,
      duration: 3000,
      position: 'bottom',
      color: 'success'
    })
    toast.present()
    
    cargarGrupos()
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
    const resGrupo = await fetch(`${import.meta.env.VITE_API_URL}/auth/invitacion/${codigo.value}`)
    if (!resGrupo.ok) throw new Error('Código no válido')
    
    const grupo = await resGrupo.json()

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
    
    // Mostrar toast de éxito
    const toast = await toastController.create({
      message: `Te has unido al grupo "${grupo.nombre}" exitosamente`,
      duration: 3000,
      position: 'top',
      color: 'success'
    })
    toast.present()
    
    cargarGrupos()
  } catch (err) {
    error.value = 'No se pudo unir al grupo. Verifica el código.'
  }
  
  cargando.value = false
}

const entrarEnGrupo = async (id) => {
  localStorage.setItem('grupoActivoId', id)
  
  // Mostrar toast de confirmación
  const toast = await toastController.create({
    message: `Accediendo al grupo...`,
    duration: 1500,
    position: 'bottom',
    color: 'primary'
  })
  await toast.present()
  
  // Navegar al dashboard o página principal del grupo
  // Ajusta la ruta según tu configuración de rutas
  await router.push(`/dashboard/${id}`) // o la ruta que corresponda
}

const abrirModalCrear = () => {
  nombreGrupo.value = ''
  imagenGrupo.value = ''
  previsualizacionImagen.value = ''
  error.value = ''
  modalCrearGrupo.value = true
}

const cerrarModalCrear = () => {
  modalCrearGrupo.value = false
  nombreGrupo.value = ''
  imagenGrupo.value = ''
  previsualizacionImagen.value = ''
  error.value = ''
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

const esAdmin = (grupo) => {
  return grupo.adminId === usuario.value?.id
}
</script>

<style scoped>
.grupo-card {
  margin: 8px 0;
  border-radius: 16px;
  transition: all 0.3s ease;
}

.grupo-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.grupo-avatar {
  width: 64px;
  height: 64px;
  margin: 0 auto 16px;
  border-radius: 50%;
  background: var(--ion-color-primary-tint);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.grupo-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.grupo-avatar ion-icon {
  font-size: 28px;
}

.grupo-nombre {
  margin-bottom: 8px;
  font-size: 1.2em;
}

.codigo-container {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  margin-bottom: 16px;
  color: var(--ion-color-medium);
  font-size: 0.9em;
}

.grupo-stats {
  display: flex;
  justify-content: space-around;
  font-size: 0.85em;
  color: var(--ion-color-medium);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.estado-vacio {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  text-align: center;
}

.contenido-vacio {
  margin-bottom: 32px;
}

.contenido-vacio ion-icon {
  font-size: 80px;
  margin-bottom: 16px;
}

.contenido-vacio h2 {
  font-size: 1.5em;
  font-weight: bold;
  margin-bottom: 8px;
  color: var(--ion-color-dark);
}

.contenido-vacio p {
  color: var(--ion-color-medium);
  max-width: 300px;
  line-height: 1.5;
}

.acciones-vacias {
  width: 100%;
  max-width: 350px;
}

.separador {
  text-align: center;
  color: var(--ion-color-medium);
  margin: 16px 0;
}

.selector-imagen {
  text-align: center;
  margin-bottom: 24px;
}

.preview-imagen {
  width: 96px;
  height: 96px;
  margin: 0 auto 8px;
  border-radius: 50%;
  background: var(--ion-color-light);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  position: relative;
  cursor: pointer;
}

.preview-imagen img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-imagen ion-icon {
  font-size: 32px;
  color: var(--ion-color-medium);
}

.overlay-upload {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.preview-imagen:hover .overlay-upload {
  opacity: 1;
}

.selector-imagen p {
  color: var(--ion-color-medium);
  font-size: 0.9em;
}
</style>