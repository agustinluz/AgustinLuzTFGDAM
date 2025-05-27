<template>
  <ion-page>
    <ion-header :translucent="true">
      <ion-toolbar>
        <ion-title>Galería de Imágenes</ion-title>
        <ion-buttons slot="end">
          <ion-button @click="abrirModalSubida" fill="clear">
            <ion-icon :icon="addOutline"></ion-icon>
          </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>

    <ion-content :fullscreen="true" class="ion-padding">
      <ion-header collapse="condense">
        <ion-toolbar>
          <ion-title size="large">Galería</ion-title>
        </ion-toolbar>
      </ion-header>

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
                    @ionSelectionChange="cargarImagenes"
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

      <!-- Estado de Carga -->
      <div v-if="estado.cargando" class="loading-container">
        <ion-spinner name="crescent"></ion-spinner>
        <p>Cargando imágenes...</p>
      </div>

      <!-- Estado Vacío -->
      <div v-else-if="!imagenes.length" class="empty-state">
        <ion-icon :icon="imagesOutline" class="empty-icon"></ion-icon>
        <h3>No hay imágenes</h3>
        <p v-if="filtros.eventoId">No hay imágenes para este evento específico.</p>
        <p v-else>No se encontraron imágenes para este grupo.</p>
        <ion-button @click="abrirModalSubida" fill="outline">
          <ion-icon :icon="addOutline" slot="start"></ion-icon>
          Subir primera imagen
        </ion-button>
      </div>

      <!-- Galería de Imágenes -->
      <div v-else class="gallery-container">
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

        <ion-grid class="gallery-grid">
          <ion-row>
            <ion-col 
              v-for="imagen in imagenes" 
              :key="imagen.id"
              size="12" 
              size-sm="6" 
              size-md="4" 
              size-lg="3"
            >
              <ion-card class="image-card" @click="abrirVistaPrevia(imagen)">
                <div class="image-container">
                  <img 
                    :src="getImagenUrl(imagen.id)" 
                    :alt="imagen.nombre"
                    @load="onImageLoad"
                    @error="onImageError"
                  />
                  <div class="image-overlay">
                    <ion-button 
                      fill="clear" 
                      color="light"
                      @click.stop="abrirVistaPrevia(imagen)"
                    >
                      <ion-icon :icon="eyeOutline"></ion-icon>
                    </ion-button>
                    <ion-button 
                      fill="clear" 
                      color="danger"
                      @click.stop="confirmarEliminacion(imagen)"
                      v-if="imagen.usuarioId === contexto.usuarioId"
                    >
                      <ion-icon :icon="trashOutline"></ion-icon>
                    </ion-button>
                  </div>
                </div>
                <ion-card-content>
                  <ion-card-subtitle class="image-name">
                    {{ imagen.nombre }}
                  </ion-card-subtitle>
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

      <!-- Modal de Subida -->
      <ion-modal :is-open="modalSubida.abierto" @willDismiss="cerrarModalSubida">
        <ion-header>
          <ion-toolbar>
            <ion-title>Subir Nueva Imagen</ion-title>
            <ion-buttons slot="end">
              <ion-button @click="cerrarModalSubida">Cerrar</ion-button>
            </ion-buttons>
          </ion-toolbar>
        </ion-header>
        <ion-content class="ion-padding">
          <form @submit.prevent="subirImagen">
            <!-- Información del contexto -->
            <ion-card class="context-info-card">
              <ion-card-content>
                <h4>Subiendo imagen como:</h4>
                <div class="upload-context">
                  <ion-chip color="primary">
                    <ion-icon :icon="peopleOutline"></ion-icon>
                    <ion-label>{{ contexto.grupoNombre || `Grupo ${contexto.grupoId}` }}</ion-label>
                  </ion-chip>
                  <ion-chip color="secondary">
                    <ion-icon :icon="personOutline"></ion-icon>
                    <ion-label>{{ contexto.usuarioNombre || `Usuario ${contexto.usuarioId}` }}</ion-label>
                  </ion-chip>
                </div>
              </ion-card-content>
            </ion-card>

            <!-- Vista previa del archivo -->
            <div v-if="modalSubida.vistaPrevia" class="preview-container">
              <img :src="modalSubida.vistaPrevia" alt="Vista previa" class="preview-image"/>
              <ion-button fill="clear" color="medium" @click="limpiarArchivo">
                <ion-icon :icon="closeOutline"></ion-icon>
              </ion-button>
            </div>

            <!-- Selector de archivo -->
            <ion-card>
              <ion-card-content>
                <div class="file-upload-area" @click="triggerFileInput" :class="{ 'has-file': modalSubida.archivo }">
                  <input 
                    ref="fileInput"
                    type="file" 
                    accept="image/*" 
                    @change="onFileChange"
                    style="display: none"
                  />
                  <ion-icon :icon="cloudUploadOutline" class="upload-icon"></ion-icon>
                  <p v-if="!modalSubida.archivo">Haz clic para seleccionar una imagen</p>
                  <p v-else>{{ modalSubida.archivo.name }}</p>
                  <small>Máximo 5MB - JPG, PNG, GIF</small>
                </div>
              </ion-card-content>
            </ion-card>

            <!-- Selector de evento (solo eventos pasados o actuales) -->
            <ion-item fill="outline" class="form-item">
              <ion-label position="floating">Asociar a Evento (Opcional)</ion-label>
              <ion-select 
                v-model="modalSubida.eventoId"
                placeholder="Sin evento específico"
                interface="popover"
              >
                <ion-select-option :value="null">Sin evento específico</ion-select-option>
                <ion-select-option 
                  v-for="evento in eventosParaImagenes" 
                  :key="evento.id"
                  :value="evento.id"
                >
                  {{ evento.titulo }} ({{ formatearFecha(evento.fecha) }})
                </ion-select-option>
              </ion-select>
            </ion-item>

            <ion-button 
              expand="block" 
              type="submit"
              :disabled="!modalSubida.archivo || estado.subiendo"
              class="submit-button"
            >
              <ion-spinner v-if="estado.subiendo" name="crescent" slot="start"></ion-spinner>
              <ion-icon v-else :icon="cloudUploadOutline" slot="start"></ion-icon>
              {{ estado.subiendo ? 'Subiendo...' : 'Subir Imagen' }}
            </ion-button>
          </form>
        </ion-content>
      </ion-modal>

      <!-- Modal de Vista Previa -->
      <ion-modal :is-open="modalVistaPrevia.abierto" @willDismiss="cerrarVistaPrevia">
        <ion-header>
          <ion-toolbar>
            <ion-title>{{ modalVistaPrevia.imagen?.nombre }}</ion-title>
            <ion-buttons slot="end">
              <ion-button @click="cerrarVistaPrevia">Cerrar</ion-button>
            </ion-buttons>
          </ion-toolbar>
        </ion-header>
        <ion-content>
          <div v-if="modalVistaPrevia.imagen" class="preview-modal-content">
            <img 
              :src="getImagenUrl(modalVistaPrevia.imagen.id)" 
              :alt="modalVistaPrevia.imagen.nombre"
              class="modal-image"
            />
            <ion-card>
              <ion-card-content>
                <h3>{{ modalVistaPrevia.imagen.nombre }}</h3>
                <div class="meta-info">
                  <p><strong>Tipo:</strong> {{ modalVistaPrevia.imagen.tipoContenido }}</p>
                  <p><strong>Subida por:</strong> 
                    {{ modalVistaPrevia.imagen.nombreUsuario || `Usuario ${modalVistaPrevia.imagen.usuarioId}` }}
                    <ion-chip v-if="modalVistaPrevia.imagen.usuarioId === contexto.usuarioId" color="success" size="small">
                      (Tuya)
                    </ion-chip>
                  </p>
                  <p v-if="modalVistaPrevia.imagen.eventoId">
                    <strong>Evento:</strong> {{ getEventoTitulo(modalVistaPrevia.imagen.eventoId) }}
                  </p>
                  <p><strong>Grupo:</strong> {{ contexto.grupoNombre || `Grupo ${contexto.grupoId}` }}</p>
                </div>
                <ion-button 
                  v-if="modalVistaPrevia.imagen.usuarioId === contexto.usuarioId"
                  color="danger" 
                  fill="clear" 
                  @click="confirmarEliminacion(modalVistaPrevia.imagen)"
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
import {
  IonPage, IonHeader, IonToolbar, IonTitle, IonContent, IonCard, IonCardHeader,
  IonCardTitle, IonCardContent, IonItem, IonLabel, IonInput, IonButton, IonGrid,
  IonRow, IonCol, IonButtons, IonIcon, IonSpinner, IonChip, IonModal,
  IonCardSubtitle, IonSelect, IonSelectOption, toastController, alertController
} from '@ionic/vue';
import { 
  addOutline, imagesOutline, eyeOutline, trashOutline, closeOutline,
  cloudUploadOutline, peopleOutline, personOutline
} from 'ionicons/icons';
import { ref, onMounted, reactive, computed } from 'vue';
import axios from 'axios';

// Estado reactivo - trabajamos directamente con los datos del API
const imagenes = ref<any[]>([]);
const eventos = ref<any[]>([]);

// Obtener datos de localStorage igual que en los otros componentes
const usuario = JSON.parse(localStorage.getItem('usuario') || '{}');
const grupoId = localStorage.getItem('grupoActivoId');

const contexto = reactive({
  grupoId: parseInt(grupoId || '0'),
  usuarioId: usuario.id || 0,
  grupoNombre: '', // Se cargará del API
  usuarioNombre: usuario.nombre || ''
});

const filtros = reactive({
  eventoId: null as number | null
});

const estado = reactive({
  cargando: false,
  subiendo: false,
  cargandoEventos: false
});

const modalSubida = reactive({
  abierto: false,
  archivo: null as File | null,
  vistaPrevia: null as string | null,
  eventoId: null as number | null
});

const modalVistaPrevia = reactive({
  abierto: false,
  imagen: null as any
});

// Referencias
const fileInput = ref<HTMLInputElement>();

// Computed properties
const eventosDisponibles = computed(() => eventos.value);

const eventosParaImagenes = computed(() => {
  const ahora = new Date();
  return eventos.value.filter(evento => {
    const fechaEvento = new Date(evento.fecha);
    // Solo eventos que ya han ocurrido o están ocurriendo ahora
    return fechaEvento <= ahora;
  });
});

// Métodos principales
const cargarDatosGrupo = async () => {
  try {
    const response = await axios.get(`${import.meta.env.VITE_API_URL}/grupos/${contexto.grupoId}`);
    const grupo = response.data as any;
    if (grupo && grupo.nombre) {
      contexto.grupoNombre = grupo.nombre;
    }
  } catch (error) {
    console.error('Error al cargar datos del grupo:', error);
  }
};

const cargarEventos = async () => {
  estado.cargandoEventos = true;
  try {
    const response = await axios.get(`${import.meta.env.VITE_API_URL}/eventos/${contexto.grupoId}/eventos`);
    eventos.value = response.data as any[];
  } catch (error) {
    console.error('Error al cargar eventos:', error);
    showToast('Error al cargar los eventos', 'warning');
    eventos.value = [];
  } finally {
    estado.cargandoEventos = false;
  }
};

const cargarImagenes = async () => {
  estado.cargando = true;
  try {
    const response = await axios.get(`${import.meta.env.VITE_API_URL}/imagenes/grupo/${contexto.grupoId}`);
    let imagenesData = response.data as any[];
    
    // Aplicar filtro de evento si está seleccionado
    if (filtros.eventoId) {
      imagenesData = imagenesData.filter((img: any) => img.eventoId === filtros.eventoId);
    }
    
    imagenes.value = imagenesData;
  } catch (error) {
    console.error('Error al cargar imágenes:', error);
    showToast('Error al cargar las imágenes', 'danger');
    imagenes.value = [];
  } finally {
    estado.cargando = false;
  }
};

const abrirModalSubida = () => {
  modalSubida.abierto = true;
  modalSubida.eventoId = filtros.eventoId; // Pre-seleccionar el evento actual si existe
};

const cerrarModalSubida = () => {
  modalSubida.abierto = false;
  limpiarFormularioSubida();
};

const limpiarFormularioSubida = () => {
  modalSubida.archivo = null;
  modalSubida.vistaPrevia = null;
  modalSubida.eventoId = null;
};

const triggerFileInput = () => {
  fileInput.value?.click();
};

const onFileChange = (event: Event) => {
  const target = event.target as HTMLInputElement;
  if (target.files && target.files.length > 0) {
    const file = target.files[0];
    
    // Validar tamaño
    if (file.size > 5 * 1024 * 1024) {
      showToast('El archivo no puede superar los 5MB', 'warning');
      return;
    }
    
    modalSubida.archivo = file;
    
    // Crear vista previa
    const reader = new FileReader();
    reader.onload = (e) => {
      modalSubida.vistaPrevia = e.target?.result as string;
    };
    reader.readAsDataURL(file);
  }
};

const limpiarArchivo = () => {
  modalSubida.archivo = null;
  modalSubida.vistaPrevia = null;
  if (fileInput.value) {
    fileInput.value.value = '';
  }
};

const subirImagen = async () => {
  if (!modalSubida.archivo) {
    showToast('Selecciona una imagen', 'warning');
    return;
  }

  estado.subiendo = true;
  const formData = new FormData();
  formData.append('archivo', modalSubida.archivo);
  formData.append('grupoId', contexto.grupoId.toString());
  formData.append('usuarioId', contexto.usuarioId.toString());
  
  if (modalSubida.eventoId) {
    formData.append('eventoId', modalSubida.eventoId.toString());
  }

  try {
    await axios.post(`${import.meta.env.VITE_API_URL}/imagenes/subir`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    
    showToast('Imagen subida correctamente', 'success');
    cerrarModalSubida();
    await cargarImagenes();
  } catch (error: any) {
    console.error('Error al subir imagen:', error);
    const mensaje = error?.response?.data || 'Error al subir la imagen';
    showToast(mensaje, 'danger');
  } finally {
    estado.subiendo = false;
  }
};

const abrirVistaPrevia = (imagen: any) => {
  modalVistaPrevia.imagen = imagen;
  modalVistaPrevia.abierto = true;
};

const cerrarVistaPrevia = () => {
  modalVistaPrevia.abierto = false;
  modalVistaPrevia.imagen = null;
};

const confirmarEliminacion = async (imagen: any) => {
  // Solo permitir eliminar las propias imágenes
  if (imagen.usuarioId !== contexto.usuarioId) {
    showToast('Solo puedes eliminar tus propias imágenes', 'warning');
    return;
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
  });
  
  await alert.present();
};

const eliminarImagen = async (id: number) => {
  try {
    await axios.delete(`${import.meta.env.VITE_API_URL}/imagenes/${id}`);
    showToast('Imagen eliminada correctamente', 'success');
    await cargarImagenes();
    
    // Cerrar modal de vista previa si está abierto
    if (modalVistaPrevia.abierto && modalVistaPrevia.imagen?.id === id) {
      cerrarVistaPrevia();
    }
  } catch (error) {
    console.error('Error al eliminar imagen:', error);
    showToast('No se pudo eliminar la imagen', 'danger');
  }
};

const getImagenUrl = (id: number) => `${import.meta.env.VITE_API_URL}/imagenes/${id}/datos`;

const getEventoTitulo = (eventoId: number) => {
  const evento = eventos.value.find((e: any) => e.id === eventoId);
  return evento ? evento.titulo : `Evento ${eventoId}`;
};

const formatearFecha = (fecha: string | Date) => {
  return new Date(fecha).toLocaleDateString('es-ES', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  });
};

const onImageLoad = (event: Event) => {
  const img = event.target as HTMLImageElement;
  img.style.opacity = '1';
};

const onImageError = (event: Event) => {
  const img = event.target as HTMLImageElement;
  img.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjQiIGhlaWdodD0iMjQiIHZpZXdCb3g9IjAgMCAyNCAyNCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHBhdGggZD0iTTIxIDNIMy4yVjIxSDIxVjNaIiBzdHJva2U9IiM5OTk5OTkiIHN0cm9rZS13aWR0aD0iMSIgZmlsbD0iI0YzRjRGNiIvPgo8cGF0aCBkPSJNOSA5QzkuNTUyMjggOSAxMCA4LjU1MjI4IDEwIDhDMTAgNy40NDc3MiA5LjU1MjI4IDcgOSA3QzguNDQ3NzIgNyA4IDcuNDQ3NzIgOCA4QzggOC41NTIyOCA4LjQ0NzcyIDkgOSA5WiIgZmlsbD0iIzk5OTk5OSIvPgo8cGF0aCBkPSJNMjEgMTVMMTYgMTBMMTMgMTNMOCAxMEwzIDE1VjIxSDIxVjE1WiIgZmlsbD0iIzk5OTk5OSIvPgo8L3N2Zz4K';
};

const showToast = async (message: string, color: 'success' | 'warning' | 'danger' = 'success') => {
  const toast = await toastController.create({
    message,
    duration: 3000,
    position: 'bottom',
    color
  });
  await toast.present();
};

// Lifecycle
onMounted(async () => {
  // Verificar que tenemos los datos necesarios
  if (!contexto.grupoId || !contexto.usuarioId) {
    showToast('Error: No se encontraron datos del grupo o usuario', 'danger');
    return;
  }
  
  await cargarDatosGrupo();
  await cargarEventos();
  await cargarImagenes();
});
</script>

<style scoped>
.context-card {
  margin-bottom: 16px;
}

.context-info {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 200px;
  text-align: center;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  text-align: center;
  padding: 32px;
}

.empty-icon {
  font-size: 64px;
  color: var(--ion-color-medium);
  margin-bottom: 16px;
}

.gallery-container {
  margin-top: 16px;
}

.gallery-header {
  margin-bottom: 16px;
}

.gallery-grid {
  padding: 0;
}

.image-card {
  margin: 0;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.image-card:hover {
  transform: translateY(-2px);
}

.image-container {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
}

.image-container img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.image-card:hover .image-overlay {
  opacity: 1;
}

.image-name {
  font-weight: 600;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.image-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  align-items: center;
}

.context-info-card {
  margin-bottom: 16px;
}

.upload-context {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.preview-container {
  position: relative;
  margin-bottom: 16px;
}

.preview-image {
  width: 100%;
  max-height: 200px;
  object-fit: cover;
  border-radius: 8px;
}

.preview-container ion-button {
  position: absolute;
  top: 8px;
  right: 8px;
}

.file-upload-area {
  border: 2px dashed var(--ion-color-medium);
  border-radius: 8px;
  padding: 32px 16px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.file-upload-area:hover,
.file-upload-area.has-file {
  border-color: var(--ion-color-primary);
  background: var(--ion-color-primary-tint);
}

.upload-icon {
  font-size: 48px;
  color: var(--ion-color-medium);
  margin-bottom: 16px;
}

.form-item {
  margin-bottom: 16px;
}

.submit-button {
  margin-top: 24px;
}

.preview-modal-content {
  padding: 16px;
}

.modal-image {
  width: 100%;
  max-height: 60vh;
  object-fit: contain;
  border-radius: 8px;
  margin-bottom: 16px;
}

.meta-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.meta-info p {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

@media (max-width: 768px) {
  .image-container {
    height: 150px;
  }
  
  .context-info {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>