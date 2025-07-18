<template>
  <ion-page>
    <ion-header :translucent="true">
      <ion-toolbar>
        <ion-buttons slot="start">
          <ion-button fill="clear" color="light" @click="irDashboard">
            <ion-icon name="arrow-back" />
          </ion-button>
        </ion-buttons>
        <ion-title>Imágenes del Grupo</ion-title>
        <ion-buttons slot="end">
          <ion-button @click="irACrearImagen" fill="clear">
            <ion-icon :icon="addOutline"></ion-icon>
          </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>

    <ion-content :fullscreen="true">
      <!-- Información del contexto actual -->
      <ion-card class="context-card">
        <ion-card-content>
          <ion-grid>
            <ion-row>
              <ion-col size="12" size-md="6">
                <div class="context-info">
                  <ion-chip color="primary" outline>
                    <ion-icon :icon="peopleOutline"></ion-icon>
                    <ion-label>Grupo: {{ contexto.grupoNombre || contexto.grupoId }}</ion-label>
                  </ion-chip>
                  <ion-chip color="secondary" outline>
                    <ion-icon :icon="personOutline"></ion-icon>
                    <ion-label>{{ contexto.usuarioNombre || `Usuario ${contexto.usuarioId}` }}</ion-label>
                  </ion-chip>
                </div>
              </ion-col>
              <ion-col size="12" size-md="6">
                <ion-item fill="outline">
                  <ion-label position="floating">Filtrar por Evento</ion-label>
                  <ion-select 
                    v-model="filtros.eventoId"
                    @ionChange="onEventoChange"
                    placeholder="Todos los eventos"
                    interface="popover"
                  >
                    <ion-select-option :value="null">Todos los eventos</ion-select-option>
                    <ion-select-option 
                      v-for="evento in eventosDisponibles" 
                      :key="evento.id"
                      :value="evento.id"
                    >
                      {{ evento.titulo }} ({{ formatearFecha(evento.fecha) }})
                    </ion-select-option>
                  </ion-select>
                </ion-item>
              </ion-col>
            </ion-row>
          </ion-grid>
        </ion-card-content>
      </ion-card>

      <!-- Loading -->
      <div v-if="estado.cargando" class="loading-container">
        <ion-spinner name="crescent"></ion-spinner>
        <p>Cargando imágenes...</p>
      </div>

      <!-- Estado vacío - Sin imágenes -->
      <div v-if="!estado.cargando && imagenes.length === 0" class="estado-vacio">
        <ion-card class="tarjeta-vacia">
          <ion-card-content class="ion-text-center">
            <ion-icon :icon="imagesOutline" size="large" color="medium" class="icono-vacio"></ion-icon>
            <h2>¡Aún no hay imágenes!</h2>
            <p class="mensaje-vacio">
              Este grupo no tiene imágenes todavía. 
              <br>¡Sé el primero en subir una imagen y compartir tus momentos!
            </p>
            <ion-button 
              expand="block" 
              fill="solid" 
              @click="irACrearImagen"
              class="boton-subir-principal"
            >
              <ion-icon :icon="addOutline" slot="start"></ion-icon>
              Subir Primera Imagen
            </ion-button>
          </ion-card-content>
        </ion-card>
      </div>

      <!-- Grid de Imágenes -->
      <div v-if="!estado.cargando && imagenes.length > 0" class="gallery-container">
        <ion-card class="gallery-header">
          <ion-card-header>
            <ion-card-title>
              <span v-if="filtros.eventoId">
                Imágenes del Evento: {{ getEventoTitulo(filtros.eventoId) }}
              </span>
              <span v-else>
                Todas las Imágenes del Grupo
              </span>
              <ion-chip color="primary" outline>
                {{ imagenes.length }} imagen{{ imagenes.length !== 1 ? 'es' : '' }}
              </ion-chip>
            </ion-card-title>
          </ion-card-header>
        </ion-card>

        <ion-grid>
          <ion-row>
           <ion-col
              class="imagen-col"
              v-for="imagen in imagenes"
              :key="imagen.id"
              size="6"
              size-md="4"
              size-lg="3"
            >
              <ion-card class="imagen-card">
                <div class="imagen-container">
                  <!-- Skeleton loader mientras carga la imagen -->
                  <div v-if="imagen.cargando" class="imagen-skeleton">
                    <ion-spinner name="crescent"></ion-spinner>
                  </div>
                  
                  <!-- Imagen de error si falla la carga -->
                  <div v-else-if="imagen.error" class="imagen-error">
                    <ion-icon :icon="imagesOutline" size="large" color="medium"></ion-icon>
                    <p>Error al cargar</p>
                  </div>
                  
                  <!-- Imagen normal -->
                  <img 
                    v-else
                    :src="imagen.preview"
                    :alt="imagen.nombre"
                    @error="() => manejarErrorImagen(imagen)"
                    @load="() => onImageLoad(imagen)"
                    class="imagen-preview"
                    :class="{ 'imagen-visible': imagen.preview && !imagen.cargando }"
                  />
                  
                  <!-- Overlay con botones -->
                  <div v-if="!imagen.cargando && !imagen.error" class="imagen-overlay">
                    <ion-button 
                      fill="clear" 
                      size="small" 
                      color="light"
                      @click="verImagenCompleta(imagen)"
                    >
                      <ion-icon :icon="eyeOutline"></ion-icon>
                    </ion-button>
                    <ion-button 
                      fill="clear" 
                      size="small" 
                      color="light"
                      @click="descargarImagen(imagen)"
                    >
                      <ion-icon :icon="downloadOutline"></ion-icon>
                    </ion-button>
                    <ion-button 
                      v-if="puedeEliminar(imagen)"
                      fill="clear" 
                      size="small" 
                      color="danger"
                      @click="confirmarEliminacion(imagen)"
                    >
                      <ion-icon :icon="trashOutline"></ion-icon>
                    </ion-button>
                  </div>
                </div>
                <ion-card-content>
                  <div class="image-meta">
                    <ion-chip v-if="imagen.eventoId" size="small" color="tertiary" outline>
                      {{ getEventoTitulo(imagen.eventoId) }}
                    </ion-chip>
                    <ion-chip 
                      v-if="imagen.usuarioId !== contexto.usuarioId" 
                      size="small" 
                      color="secondary" 
                      outline
                    >
                      {{ imagen.nombreUsuario || `Usuario ${imagen.usuarioId}` }}
                    </ion-chip>
                    <ion-chip 
                      v-else 
                      size="small" 
                      color="success" 
                      outline
                    >
                      Tu imagen
                    </ion-chip>
                  </div>
                </ion-card-content>
              </ion-card>
            </ion-col>
          </ion-row>
        </ion-grid>
      </div>

      <!-- Modal de Vista Completa -->
      <ion-modal :is-open="modalVistaAbierto" @did-dismiss="cerrarModalVista">
        <ion-header>
          <ion-toolbar>
            <ion-title>{{ imagenCompletaSeleccionada?.nombre }}</ion-title>
            <ion-buttons slot="end">
              <ion-button @click="cerrarModalVista" fill="clear">
                <ion-icon :icon="closeOutline"></ion-icon>
              </ion-button>
            </ion-buttons>
          </ion-toolbar>
        </ion-header>
        <ion-content>
          <div v-if="cargandoImagenCompleta" class="ion-text-center ion-padding">
            <ion-spinner></ion-spinner>
            <p>Cargando imagen...</p>
          </div>
          <div v-else-if="imagenCompletaSeleccionada" class="ion-padding">
            <img
              :src="imagenCompletaSeleccionada.datos ? `data:${imagenCompletaSeleccionada.tipoContenido};base64,${imagenCompletaSeleccionada.datos}` : imagenCompletaSeleccionada.preview"
              :alt="imagenCompletaSeleccionada.nombre"
              style="width: 100%; height: auto; border-radius: 8px;"
            />
            <ion-card>
              <ion-card-content>
                <h3>{{ imagenCompletaSeleccionada.nombre }}</h3>
                <div class="meta-info">
                  <p><strong>Tipo:</strong> {{ imagenCompletaSeleccionada.tipoContenido }}</p>
                  <p><strong>Subido por:</strong> 
                    {{ imagenCompletaSeleccionada.nombreUsuario || `Usuario ${imagenCompletaSeleccionada.usuarioId}` }}
                    <ion-chip v-if="imagenCompletaSeleccionada.usuarioId === contexto.usuarioId" color="success" size="small">
                      (Tuya)
                    </ion-chip>
                  </p>
                  <p><strong>Tamaño:</strong> {{ formatearTamaño(imagenCompletaSeleccionada.tamaño) }}</p>
                  <p><strong>Fecha:</strong> {{ formatearFecha(imagenCompletaSeleccionada.fechaCreacion) }}</p>
                  <p v-if="imagenCompletaSeleccionada.eventoId">
                    <strong>Evento:</strong> {{ getEventoTitulo(imagenCompletaSeleccionada.eventoId) }}
                  </p>
                </div>
                <ion-button 
                  v-if="puedeEliminar(imagenCompletaSeleccionada)"
                  color="danger" 
                  fill="clear" 
                  @click="confirmarEliminacion(imagenCompletaSeleccionada)"
                >
                  <ion-icon :icon="trashOutline" slot="start"></ion-icon>
                  Eliminar Imagen
                </ion-button>
              </ion-card-content>
            </ion-card>
          </div>
        </ion-content>
      </ion-modal>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  IonPage,
  IonHeader,
  IonToolbar,
  IonTitle,
  IonContent,
  IonButton,
  IonButtons,
  IonIcon,
  IonCard,
  IonCardHeader,
  IonCardTitle,
  IonCardContent,
  IonSelect,
  IonSelectOption,
  IonGrid,
  IonRow,
  IonCol,
  IonSpinner,
  IonModal,
  IonItem,
  IonLabel,
  IonChip,
  alertController,
  toastController,
  
  
} from '@ionic/vue'
import {
  addOutline,
  eyeOutline,
  downloadOutline,
  trashOutline,
  closeOutline,
  imagesOutline,
  peopleOutline,
  personOutline
} from 'ionicons/icons'
import { imageService } from '@/service/imagenService'
import { groupService } from '@/service/GrupoService'
import { EventosService } from '@/service/EventoService'

const route = useRoute()
const router = useRouter()
const grupoId = route.params.id

// Estado reactivo
const imagenes = ref<any[]>([])
const eventos = ref<any[]>([])
const imagenCompletaSeleccionada = ref<any>(null)
const modalVistaAbierto = ref(false)
const cargandoImagenCompleta = ref(false)

// Obtener datos de localStorage
const usuario = JSON.parse(localStorage.getItem('usuario') || '{}')

const contexto = reactive({
  grupoId: parseInt(grupoId as string),
  usuarioId: usuario.id || 0,
  grupoNombre: '',
  usuarioNombre: usuario.nombre || ''
})

const filtros = reactive({
  eventoId: null as number | null
})

const estado = reactive({
  cargando: false
})

// Cache para imágenes completas
const cacheImagenesCompletas = new Map<number, any>()

// URL base de la API
const API_BASE = `${import.meta.env.VITE_API_URL}`

// Computed properties
const eventosDisponibles = computed(() => eventos.value)

const api = {
  async obtenerImagenesPorGrupo(grupoId: number) {
    return await imageService.getByGrupo(grupoId)
  },

  async obtenerImagenCompleta(id: number) {
    return await imageService.getFullImage(id)
  },

  async eliminarImagen(id: number) {
    await imageService.deleteImage(id)
  },

  async obtenerGrupo(id: number) {
    const res = await groupService.getGroupById(id)
    return res.data
  },

  async obtenerEventos(grupoId: number) {
    return await EventosService.obtenerEventosGrupo(grupoId)
  }
}

// Métodos principales

const irDashboard = () => {
  router.push(`/dashboard/${grupoId}`)
}

const cargarDatosGrupo = async () => {
  try {
    const grupo = await api.obtenerGrupo(contexto.grupoId) as { nombre?: string }
    if (grupo?.nombre) {
      contexto.grupoNombre = grupo.nombre
    }
  } catch (error) {
    console.error('Error al cargar datos del grupo:', error)
  }
}

const cargarEventos = async () => {
  try {
    eventos.value = await api.obtenerEventos(contexto.grupoId)
  } catch (error) {
    console.error('Error al cargar eventos:', error)
    mostrarToast('Error al cargar los eventos', 'warning')
    eventos.value = []
  }
}

const cargarImagenes = async () => {
  estado.cargando = true
  try {
    let imagenesData = await api.obtenerImagenesPorGrupo(contexto.grupoId)
    
    // Aplicar filtro de evento si está seleccionado
    if (filtros.eventoId) {
      imagenesData = imagenesData.filter((img: any) => img.eventoId === filtros.eventoId)
    }

    // Inicializar estado de carga para cada imagen
    const imagenesConEstado = imagenesData.map((img: any) => ({
      ...img,
      preview: '',
      cargando: true,
      error: false
    }))

    imagenes.value = imagenesConEstado

    // Cargar previews de forma asíncrona
    await Promise.all(
      imagenesConEstado.map(async (img: any, index: number) => {
        try {
          const datos = await imageService.getImageData(img.id)
          if (datos) {
            // Actualizar la imagen específica en el array
            imagenes.value[index] = {
              ...imagenes.value[index],
              preview: `data:${img.tipoContenido};base64,${datos}`,
              cargando: false,
              error: false
            }
          } else {
            imagenes.value[index] = {
              ...imagenes.value[index],
              cargando: false,
              error: true
            }
          }
        } catch (error) {
          console.error(`Error al cargar preview de imagen ${img.id}:`, error)
          imagenes.value[index] = {
            ...imagenes.value[index],
            cargando: false,
            error: true
          }
        }
      })
    )

  } catch (error) {
    console.error('Error al cargar imágenes:', error)
    mostrarToast('Error al cargar imágenes', 'danger')
    imagenes.value = []
  } finally {
    estado.cargando = false
  }
}

const irACrearImagen = () => {
  router.push(`/dashboard/${grupoId}/crear/imagen`)
}

const onEventoChange = (ev: CustomEvent) => {
  const value = ev.detail.value
  filtros.eventoId = value !== undefined && value !== null && value !== '' ? Number(value) : null
  cargarImagenes()
}

const verImagenCompleta = async (imagen: any) => {
  modalVistaAbierto.value = true
  cargandoImagenCompleta.value = true
  
  try {
    // Verificar si ya tenemos la imagen completa en cache
    if (cacheImagenesCompletas.has(imagen.id)) {
      imagenCompletaSeleccionada.value = cacheImagenesCompletas.get(imagen.id)
    } else {
      const imagenCompleta = await api.obtenerImagenCompleta(imagen.id)
      cacheImagenesCompletas.set(imagen.id, imagenCompleta)
      imagenCompletaSeleccionada.value = imagenCompleta
    }
  } catch (error) {
    console.error('Error al cargar imagen completa:', error)
    mostrarToast('Error al cargar imagen completa', 'danger')
    cerrarModalVista()
  } finally {
    cargandoImagenCompleta.value = false
  }
}

const cerrarModalVista = () => {
  modalVistaAbierto.value = false
  imagenCompletaSeleccionada.value = null
  cargandoImagenCompleta.value = false
}

const descargarImagen = async (imagen: any) => {
  try {
    // Obtener la imagen completa si no la tenemos
    let imagenCompleta = cacheImagenesCompletas.get(imagen.id)
    if (!imagenCompleta) {
      imagenCompleta = await api.obtenerImagenCompleta(imagen.id)
      cacheImagenesCompletas.set(imagen.id, imagenCompleta)
    }
    
    if (imagenCompleta.datos) {
      const link = document.createElement('a')
      link.href = `data:${imagenCompleta.tipoContenido};base64,${imagenCompleta.datos}`
      link.download = imagen.nombre
      link.click()
    }
  } catch (error) {
    console.error('Error al descargar imagen:', error)
    mostrarToast('Error al descargar imagen', 'danger')
  }
}

const puedeEliminar = (imagen: any): boolean => {
  return imagen.usuarioId === contexto.usuarioId
}

const confirmarEliminacion = async (imagen: any) => {
  if (!puedeEliminar(imagen)) {
    mostrarToast('Solo puedes eliminar tus propias imágenes', 'warning')
    return
  }

  const alert = await alertController.create({
    header: 'Confirmar eliminación',
    message: `¿Estás seguro de que quieres eliminar "${imagen.nombre}"?`,
    buttons: [
      {
        text: 'Cancelar',
        role: 'cancel'
      },
      {
        text: 'Eliminar',
        role: 'destructive',
        handler: () => eliminarImagen(imagen.id)
      }
    ]
  })
  await alert.present()
}

const eliminarImagen = async (id: number) => {
  try {
    await api.eliminarImagen(id)
    // Limpiar del cache si existe
    cacheImagenesCompletas.delete(id)
    mostrarToast('Imagen eliminada correctamente', 'success')
    await cargarImagenes()
    
    // Cerrar modal de vista previa si está abierto
    if (modalVistaAbierto.value && imagenCompletaSeleccionada.value?.id === id) {
      cerrarModalVista()
    }
  } catch (error) {
    console.error('Error al eliminar imagen:', error)
    mostrarToast('Error al eliminar imagen', 'danger')
  }
}

const getEventoTitulo = (eventoId: number) => {
  const evento = eventos.value.find((e: any) => e.id === eventoId)
  return evento ? evento.titulo : `Evento ${eventoId}`
}

const formatearFecha = (fecha: string | Date) => {
  if (!fecha) return 'Fecha desconocida'
  return new Date(fecha).toLocaleDateString('es-ES', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatearTamaño = (bytes?: number): string => {
  if (!bytes) return 'Desconocido'
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  return Math.round(bytes / Math.pow(1024, i) * 100) / 100 + ' ' + sizes[i]
}

const onImageLoad = (imagen: any) => {
  // La imagen se ha cargado correctamente
  console.log(`Imagen ${imagen.id} cargada correctamente`)
}

const manejarErrorImagen = (imagen: any) => {
  console.error(`Error al cargar imagen ${imagen.id}`)
  // Marcar como error
  const index = imagenes.value.findIndex(img => img.id === imagen.id)
  if (index !== -1) {
    imagenes.value[index] = {
      ...imagenes.value[index],
      error: true,
      cargando: false
    }
  }
}

const mostrarToast = async (mensaje: string, color: 'success' | 'warning' | 'danger' = 'success') => {
  const toast = await toastController.create({
    message: mensaje,
    duration: 3000,
    position: 'bottom',
    color
  })
  await toast.present()
}

// Lifecycle
onMounted(async () => {
  // Verificar que tenemos los datos necesarios
  if (!contexto.grupoId || !contexto.usuarioId) {
    mostrarToast('Error: No se encontraron datos del grupo o usuario', 'danger')
    router.replace('/grupo')
    return
  }
  
  await cargarDatosGrupo()
  await cargarEventos()
  await cargarImagenes()
})
</script>

<style scoped>
.context-card {
  margin: 16px;
  --background: var(--ion-color-light);
}

.context-info {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  align-items: center;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 200px;
  color: var(--ion-color-medium);
}

.estado-vacio {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
  padding: 20px;
}

.tarjeta-vacia {
  max-width: 500px;
  width: 100%;
  margin: 0 auto;
}

.icono-vacio {
  margin-bottom: 20px;
}

.mensaje-vacio {
  color: var(--ion-color-medium);
  font-size: 16px;
  line-height: 1.5;
  margin-bottom: 30px;
}

.boton-subir-principal {
  margin-top: 20px;
  --background: var(--ion-color-primary);
  --color: white;
  font-weight: 600;
}

.tarjeta-vacia h2 {
  color: var(--ion-color-dark);
  margin-bottom: 15px;
  font-size: 24px;
}

.gallery-container {
  padding: 0 16px;
}

.gallery-header {
  margin-bottom: 16px;
  --background: var(--ion-color-light);
}

.gallery-header ion-card-title {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.imagen-card {
  margin: 0;
  height: 100%;
  transition: transform 0.2s ease;
}

.imagen-card:hover {
  transform: translateY(-2px);
}

.imagen-container {
  position: relative;
  overflow: hidden;
  width: 120px;
  height: 120px;
  margin: 0 auto;
  border-radius: 8px;
  background-color: var(--ion-color-light);
}

.imagen-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: all 0.3s ease;
  opacity: 0;
  border-radius: 8px;
}

.imagen-preview.imagen-visible {
  opacity: 1;
}

.imagen-skeleton {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background-color: var(--ion-color-light);
  border-radius: 8px;
}

.imagen-error {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background-color: var(--ion-color-light);
  border-radius: 8px;
  color: var(--ion-color-medium);
}

.imagen-error p {
  font-size: 12px;
  margin-top: 8px;
  text-align: center;
}

.imagen-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  opacity: 0;
  transition: opacity 0.3s ease;
  border-radius: 8px;
}

.imagen-container:hover .imagen-overlay {
  opacity: 1;
}

.imagen-container:hover .imagen-preview {
  transform: scale(1.05);
}

.imagen-col {
  display: flex;
  justify-content: center;
}

.image-meta {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
  align-items: center;
}

.meta-info p {
  margin: 8px 0;
  color: var(--ion-color-medium);
}

.meta-info p strong {
  color: var(--ion-color-dark);
}

ion-card-content p {
  margin: 8px 0 0 0;
  color: var(--ion-color-medium);
}

/* Responsive design */
@media (max-width: 768px) {
  .context-info {
    justify-content: center;
  }
  
  .gallery-header ion-card-title {
    justify-content: center;
    text-align: center;
  }
  
  .imagen-container {
    width: 100px;
    height: 100px;
  }
}

@media (min-width: 768px) {
  .imagen-container {
    width: 140px;
    height: 140px;
  }
}
</style>