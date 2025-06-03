<template>
  <ion-page>
    <!-- Header -->
    <ion-header>
      <ion-toolbar color="primary">
        <ion-title>Mis Grupos</ion-title>
        <ion-buttons slot="end">
          <ion-button 
            v-if="grupoActivo" 
            fill="clear" 
            @click="irAConfiguracion"
            class="config-btn"
          >
            <ion-icon :icon="settings" slot="icon-only"></ion-icon>
          </ion-button>
          <ion-button fill="clear" @click="() => router.push('/perfil')">
            <ion-icon :icon="person" slot="icon-only"></ion-icon>
          </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>

    <ion-content :fullscreen="true" class="content-bg">
      <!-- Banner informativo del grupo activo -->
      <div v-if="grupoActivo" class="banner-grupo-activo">
        <div class="banner-content">
          <div class="banner-avatar">
            <img v-if="grupoActivo.imagenPerfil" :src="grupoActivo.imagenPerfil" :alt="grupoActivo.nombre" />
            <ion-icon v-else :icon="people" color="light"></ion-icon>
          </div>
          <div class="banner-info">
            <h3>{{ grupoActivo.nombre }}</h3>
            <p>Grupo activo</p>
          </div>
        </div>
      </div>

      <div class="container-content">
        <!-- Cuando SÍ tiene grupos -->
        <div v-if="grupos.length > 0" class="grupos-section">
          <div class="section-header">
            <h2>Todos mis grupos</h2>
            <p>{{ grupos.length }} grupo{{ grupos.length !== 1 ? 's' : '' }}</p>
          </div>

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
                  :class="{ 'active-group': grupo.id === grupoActivo?.id }"
                  @click="entrarEnGrupo(grupo.id)"
                >
                  <ion-card-content>
                    <div class="card-header">
                      <div class="grupo-avatar">
                        <img 
                          v-if="grupo.imagenPerfil" 
                          :src="grupo.imagenPerfil" 
                          :alt="grupo.nombre"
                        />
                        <ion-icon v-else :icon="people" color="primary"></ion-icon>
                      </div>
                      
                      <div class="grupo-info">
                        <ion-card-title class="grupo-nombre">{{ grupo.nombre }}</ion-card-title>
                        
                        <div class="codigo-container">
                          <ion-icon :icon="key" size="small"></ion-icon>
                          <span>{{ grupo.codigoInvitacion }}</span>
                        </div>
                      </div>

                      <ion-chip v-if="esAdmin(grupo)" color="success" class="admin-chip">
                        <ion-icon :icon="shield" size="small"></ion-icon>
                        <ion-label>Admin</ion-label>
                      </ion-chip>
                    </div>

                    <!-- Stats del grupo -->
                    <div class="grupo-stats">
                      <div class="stat-item">
                        <ion-icon :icon="people" size="small"></ion-icon>
                        <span>{{ grupo.participantes?.length || 0 }}</span>
                      </div>
                      <div class="stat-item">
                        <ion-icon :icon="calendar" size="small"></ion-icon>
                        <span>{{ grupo.eventos?.length || 0 }}</span>
                      </div>
                      <div class="stat-item">
                        <ion-icon :icon="camera" size="small"></ion-icon>
                        <span>{{ grupo.fotos?.length || 0 }}</span>
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
          <div class="empty-illustration">
            <ion-icon :icon="people" color="primary"></ion-icon>
          </div>
          <div class="empty-content">
            <h2>¡Comienza tu aventura!</h2>
            <p>Crea tu primer grupo o únete a uno existente para empezar a compartir momentos increíbles.</p>
          </div>

          <div class="empty-actions">
            <ion-button 
              expand="block" 
              @click="abrirModalCrear"
              :disabled="cargando"
              class="primary-action"
            >
              <ion-icon :icon="add" slot="start"></ion-icon>
              Crear nuevo grupo
            </ion-button>

            <div class="divider">
              <span>o</span>
            </div>

            <ion-item class="input-item">
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
              class="secondary-action"
            >
              <ion-icon :icon="logIn" slot="start"></ion-icon>
              Unirse al grupo
            </ion-button>

            <ion-text color="danger" v-if="error" class="error-text">
              <p>{{ error }}</p>
            </ion-text>
          </div>
        </div>
      </div>

      <!-- FAB solo cuando tiene grupos -->
      <ion-fab 
        v-if="grupos.length > 0" 
        vertical="bottom" 
        horizontal="end" 
        slot="fixed"
      >
        <ion-fab-button @click="abrirModal" color="secondary">
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
        <div class="modal-content">
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
          <ion-item class="input-item">
            <ion-input
              v-model="nombreGrupo"
              placeholder="Nombre del grupo"
              :clear-input="true"
            ></ion-input>
          </ion-item>

          <ion-text color="danger" v-if="error" class="error-text">
            <p>{{ error }}</p>
          </ion-text>

          <ion-button 
            expand="block" 
            @click="crearGrupo"
            :disabled="cargando || !nombreGrupo.trim()"
            class="create-btn"
          >
            {{ cargando ? 'Creando...' : 'Crear Grupo' }}
          </ion-button>
        </div>
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
        <div class="modal-content">
          <ion-button 
            expand="block" 
            @click="() => { cerrarModal(); abrirModalCrear(); }"
            :disabled="cargando"
            class="primary-action"
          >
            <ion-icon :icon="add" slot="start"></ion-icon>
            Crear nuevo grupo
          </ion-button>

          <div class="divider">
            <span>o</span>
          </div>

          <ion-item class="input-item">
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
            class="secondary-action"
          >
            <ion-icon :icon="logIn" slot="start"></ion-icon>
            Unirse al grupo
          </ion-button>

          <ion-text color="danger" v-if="error" class="error-text">
            <p>{{ error }}</p>
          </ion-text>
        </div>
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
import { ref, onMounted, computed } from 'vue'
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
  person,
  settings,
  shield
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

// Computed para grupo activo
const grupoActivo = computed(() => {
  const grupoActivoId = localStorage.getItem('grupoActivoId')
  return grupos.value.find(g => g.id.toString() === grupoActivoId)
})

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

const irAConfiguracion = () => {
  if (grupoActivo.value) {
    router.push(`/grupo/${grupoActivo.value.id}/configuracion`)
  }
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
  
  const toast = await toastController.create({
    message: `Accediendo al grupo...`,
    duration: 1500,
    position: 'bottom',
    color: 'primary'
  })
  await toast.present()
  
  await router.push(`/dashboard/${id}`)
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
.content-bg {
  --background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

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

.container-content {
  padding: 0 16px 100px;
}

.grupos-section {
  margin-top: 8px;
}

.section-header {
  margin-bottom: 20px;
}

.section-header h2 {
  font-size: 1.4em;
  font-weight: 600;
  margin: 0 0 4px 0;
  color: var(--ion-color-dark);
}

.section-header p {
  margin: 0;
  color: var(--ion-color-medium);
  font-size: 0.9em;
}

.grupo-card {
  margin: 8px 0;
  border-radius: 20px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
  background: white;
  border: 2px solid transparent;
}

.grupo-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
}

.grupo-card.active-group {
  border-color: var(--ion-color-primary);
  background: linear-gradient(135deg, #fff 0%, var(--ion-color-primary-tint) 100%);
}

.card-header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 16px;
  position: relative;
}

.grupo-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: var(--ion-color-primary-tint);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  flex-shrink: 0;
}

.grupo-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.grupo-info {
  flex: 1;
}

.grupo-nombre {
  margin: 0 0 6px 0;
  font-size: 1.1em;
  font-weight: 600;
}

.codigo-container {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--ion-color-medium);
  font-size: 0.85em;
}

.admin-chip {
  position: absolute;
  top: 0;
  right: 0;
  height: 24px;
  font-size: 0.75em;
}

.grupo-stats {
  display: flex;
  justify-content: space-between;
  padding: 12px 16px;
  background: rgba(var(--ion-color-primary-rgb), 0.05);
  border-radius: 12px;
  font-size: 0.85em;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--ion-color-medium);
}

.estado-vacio {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 500px;
  text-align: center;
  padding: 40px 20px;
}

.empty-illustration {
  margin-bottom: 24px;
}

.empty-illustration ion-icon {
  font-size: 100px;
  opacity: 0.6;
}

.empty-content h2 {
  font-size: 1.6em;
  font-weight: 700;
  margin: 0 0 12px 0;
  color: var(--ion-color-dark);
}

.empty-content p {
  color: var(--ion-color-medium);
  max-width: 280px;
  line-height: 1.5;
  margin: 0 0 32px 0;
}

.empty-actions {
  width: 100%;
  max-width: 300px;
}

.divider {
  text-align: center;
  margin: 20px 0;
  position: relative;
}

.divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: var(--ion-color-light);
}

.divider span {
  background: var(--ion-background-color);
  padding: 0 16px;
  color: var(--ion-color-medium);
  font-size: 0.9em;
}

.primary-action {
  --border-radius: 12px;
  height: 48px;
  font-weight: 600;
  margin-bottom: 8px;
}

.secondary-action {
  --border-radius: 12px;
  height: 48px;
  font-weight: 600;
  margin-top: 8px;
}

.input-item {
  border-radius: 12px;
  background: white;
  margin: 8px 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.error-text {
  text-align: center;
  margin-top: 12px;
}

.modal-content {
  padding: 8px 0;
}

.selector-imagen {
  text-align: center;
  margin-bottom: 32px;
}

.preview-imagen {
  width: 100px;
  height: 100px;
  margin: 0 auto 12px;
  border-radius: 50%;
  background: var(--ion-color-light);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  position: relative;
  cursor: pointer;
  border: 3px solid var(--ion-color-primary-tint);
}

.preview-imagen img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.overlay-upload {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
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

.create-btn {
  --border-radius: 12px;
  height: 48px;
  font-weight: 600;
  margin-top: 24px;
}

.config-btn {
  margin-right: 8px;
}
</style>