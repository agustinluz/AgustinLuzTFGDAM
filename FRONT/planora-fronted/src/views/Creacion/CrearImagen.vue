<template>
  <IonPage>
    <IonHeader>
      <IonToolbar>
        <IonButtons slot="start">
          <IonBackButton :default-href="`/dashboard/${contexto.grupoId}/galeria`" />
        </IonButtons>
        <IonTitle>Galería - {{ contexto.grupoNombre }}</IonTitle>
        <IonButtons slot="end">
          <IonButton @click="abrirModalSubida" fill="clear">
            <IonIcon :icon="addOutline" />
          </IonButton>
        </IonButtons>
      </IonToolbar>
    </IonHeader>

    <IonContent>
      <!-- Filtros -->
      <IonCard>
        <IonCardContent>
          <IonGrid>
            <IonRow>
              <IonCol size="6">
                <IonSelect 
                  v-model="filtros.tipo" 
                  placeholder="Filtrar por"
                  @ionChange="aplicarFiltro"
                >
                  <IonSelectOption value="grupo">Todo el grupo</IonSelectOption>
                  <IonSelectOption value="usuario">Mis imágenes</IonSelectOption>
                  <IonSelectOption value="evento">Por evento</IonSelectOption>
                </IonSelect>
              </IonCol>
              
              <IonCol size="6" v-if="filtros.tipo === 'evento'">
                <IonSelect 
                  v-model="filtros.eventoId" 
                  placeholder="Seleccionar evento"
                  @ionChange="aplicarFiltro"
                >
                  <IonSelectOption 
                    v-for="evento in eventosDisponibles" 
                    :key="evento.id" 
                    :value="evento.id"
                  >
                    {{ evento.titulo }}
                  </IonSelectOption>
                </IonSelect>
              </IonCol>
            </IonRow>
          </IonGrid>
        </IonCardContent>
      </IonCard>

      <!-- Modo selección múltiple -->
      <IonCard v-if="modoSeleccion">
        <IonCardContent>
          <IonGrid>
            <IonRow class="ion-align-items-center">
              <IonCol>
                <IonLabel>{{ imagenesSeleccionadas.length }} imagen(es) seleccionada(s)</IonLabel>
              </IonCol>
              <IonCol size="auto">
                <IonButton @click="confirmarEliminacionMultiple" color="danger" size="small">
                  <IonIcon :icon="trashOutline" slot="start" />
                  Eliminar
                </IonButton>
              </IonCol>
              <IonCol size="auto">
                <IonButton @click="limpiarSeleccion" fill="clear" size="small">
                  Cancelar
                </IonButton>
              </IonCol>
            </IonRow>
          </IonGrid>
        </IonCardContent>
      </IonCard>

      <!-- Loading -->
      <div v-if="estado.cargando" class="ion-text-center ion-padding">
        <IonSpinner />
        <p>Cargando imágenes...</p>
      </div>

      <!-- Grid de imágenes -->
      <IonGrid v-else>
        <IonRow>
          <IonCol 
            v-for="imagen in imagenes" 
            :key="imagen.id" 
            size="6" 
            size-md="4" 
            size-lg="3"
          >
            <IonCard 
              @click="toggleSeleccion(imagen)"
              @long-press="iniciarSeleccionMultiple(imagen)"
              :class="{ 'selected': imagenesSeleccionadas.includes(imagen.id) }"
            >
              <div class="image-container">
                <img 
                  v-if="imagen.datos" 
                  :src="crearImagenUrl(imagen.datos, imagen.tipoContenido)" 
                  :alt="imagen.nombre"
                  class="gallery-image"
                />
                <div v-else class="image-placeholder">
                  <IonIcon :icon="imagesOutline" size="large" />
                </div>
                
                <!-- Checkbox para selección -->
                <IonCheckbox 
                  v-if="modoSeleccion"
                  :checked="imagenesSeleccionadas.includes(imagen.id)"
                  class="selection-checkbox"
                />
              </div>
              
              <IonCardHeader>
                <IonCardTitle>{{ imagen.nombre }}</IonCardTitle>
                <IonCardSubtitle>
                  <div>{{ imagen.nombreUsuario }}</div>
                  <div>{{ formatearFecha(imagen.fechaCreacion) }}</div>
                  <div v-if="imagen.eventoId">{{ getEventoTitulo(imagen.eventoId) }}</div>
                </IonCardSubtitle>
              </IonCardHeader>
              
              <IonCardContent>
                <IonChip color="primary" size="small">
                  {{ formatearTamano(imagen.tamaño) }}
                </IonChip>
                
                <IonButton 
                  v-if="imagen.usuarioId === contexto.usuarioId && !modoSeleccion"
                  @click.stop="confirmarEliminacion(imagen)"
                  fill="clear" 
                  color="danger" 
                  size="small"
                  class="delete-button"
                >
                  <IonIcon :icon="trashOutline" />
                </IonButton>
              </IonCardContent>
            </IonCard>
          </IonCol>
        </IonRow>
      </IonGrid>

      <!-- Sin imágenes -->
      <div v-if="!estado.cargando && imagenes.length === 0" class="ion-text-center ion-padding">
        <IonIcon :icon="imagesOutline" size="large" color="medium" />
        <h3>No hay imágenes</h3>
        <p>Sube la primera imagen del grupo</p>
        <IonButton @click="abrirModalSubida" color="primary">
          <IonIcon :icon="addOutline" slot="start" />
          Subir imagen
        </IonButton>
      </div>
    </IonContent>

    <!-- Modal de subida -->
    <IonModal :is-open="modalSubida.abierto" @did-dismiss="cerrarModalSubida">
      <IonHeader>
        <IonToolbar>
          <IonTitle>Subir imagen</IonTitle>
          <IonButtons slot="end">
            <IonButton @click="cerrarModalSubida" fill="clear">
              <IonIcon :icon="closeOutline" />
            </IonButton>
          </IonButtons>
        </IonToolbar>
      </IonHeader>
      
      <IonContent>
        <IonCard>
          <IonCardContent>
            <!-- Vista previa -->
            <div v-if="modalSubida.vistaPrevia" class="preview-container">
              <img :src="modalSubida.vistaPrevia" alt="Vista previa" class="preview-image" />
              <IonButton @click="limpiarArchivo" fill="clear" color="danger" class="clear-button">
                <IonIcon :icon="closeOutline" />
              </IonButton>
            </div>
            
            <!-- Botón de seleccionar archivo -->
            <div v-else class="upload-area" @click="triggerFileInput">
              <IonIcon :icon="cloudUploadOutline" size="large" color="primary" />
              <p>Toca para seleccionar una imagen</p>
              <p class="ion-text-color-medium">Máximo 5MB</p>
            </div>
            
            <input 
              ref="fileInput"
              type="file" 
              accept="image/*" 
              @change="onFileChange" 
              style="display: none"
            />
            
            <!-- Selector de evento -->
            <IonItem v-if="eventos.length > 0">
              <IonLabel>Evento (opcional)</IonLabel>
              <IonSelect v-model="modalSubida.eventoId" placeholder="Seleccionar evento">
                <IonSelectOption :value="null">Sin evento</IonSelectOption>
                <IonSelectOption 
                  v-for="evento in eventos" 
                  :key="evento.id" 
                  :value="evento.id"
                >
                  {{ evento.titulo }}
                </IonSelectOption>
              </IonSelect>
            </IonItem>
          </IonCardContent>
        </IonCard>
        
        <!-- Botones de acción -->
        <div class="ion-padding">
          <IonButton 
            @click="subirImagen" 
            expand="block" 
            :disabled="!modalSubida.archivo || estado.subiendo"
          >
            <IonSpinner v-if="estado.subiendo" slot="start" />
            <IonIcon v-else :icon="cloudUploadOutline" slot="start" />
            {{ estado.subiendo ? 'Subiendo...' : 'Subir imagen' }}
          </IonButton>
          <IonButton
            @click="cancelarSubida"
            expand="block"
            color="medium"
            class="ion-margin-top"
            :disabled="estado.subiendo"
          >
            <IonIcon :icon="closeOutline" slot="start" />
            Cancelar
          </IonButton>
        </div>
      </IonContent>
    </IonModal>

    <!-- Modal de vista previa -->
    <IonModal :is-open="modalVistaPrevia.abierto" @did-dismiss="cerrarVistaPrevia">
      <IonHeader>
        <IonToolbar>
          <IonTitle>{{ modalVistaPrevia.imagen?.nombre }}</IonTitle>
          <IonButtons slot="end">
            <IonButton @click="cerrarVistaPrevia" fill="clear">
              <IonIcon :icon="closeOutline" />
            </IonButton>
          </IonButtons>
        </IonToolbar>
      </IonHeader>
      
      <IonContent v-if="modalVistaPrevia.imagen">
        <!-- Imagen -->
        <div class="preview-full-container">
          <img 
            v-if="modalVistaPrevia.imagenCompleta?.datos" 
            :src="crearImagenUrl(modalVistaPrevia.imagenCompleta.datos, modalVistaPrevia.imagenCompleta.tipoContenido)"
            :alt="modalVistaPrevia.imagen.nombre"
            class="full-preview-image"
          />
          <div v-else class="ion-text-center ion-padding">
            <IonSpinner />
            <p>Cargando imagen...</p>
          </div>
        </div>
        
        <!-- Información -->
        <IonCard>
          <IonCardHeader>
            <IonCardTitle>{{ modalVistaPrevia.imagen.nombre }}</IonCardTitle>
            <IonCardSubtitle>
              Por {{ modalVistaPrevia.imagen.nombreUsuario }}
            </IonCardSubtitle>
          </IonCardHeader>
          
          <IonCardContent>
            <IonItem>
              <IonLabel>
                <h3>Fecha</h3>
                <p>{{ formatearFechaCompleta(modalVistaPrevia.imagen.fechaCreacion) }}</p>
              </IonLabel>
            </IonItem>
            
            <IonItem>
              <IonLabel>
                <h3>Tamaño</h3>
                <p>{{ formatearTamano(modalVistaPrevia.imagen.tamaño) }}</p>
              </IonLabel>
            </IonItem>
            
            <IonItem>
              <IonLabel>
                <h3>Tipo</h3>
                <p>{{ modalVistaPrevia.imagen.tipoContenido }}</p>
              </IonLabel>
            </IonItem>
            
            <IonItem v-if="modalVistaPrevia.imagen.eventoId">
              <IonLabel>
                <h3>Evento</h3>
                <p>{{ getEventoTitulo(modalVistaPrevia.imagen.eventoId) }}</p>
              </IonLabel>
            </IonItem>
          </IonCardContent>
        </IonCard>
        
        <!-- Botón eliminar -->
        <div v-if="modalVistaPrevia.imagen.usuarioId === contexto.usuarioId" class="ion-padding">
          <IonButton 
            @click="confirmarEliminacion(modalVistaPrevia.imagen!)" 
            expand="block" 
            color="danger"
          >
            <IonIcon :icon="trashOutline" slot="start" />
            Eliminar imagen
          </IonButton>
        </div>
      </IonContent>
    </IonModal>
  </IonPage>
</template>
<script setup lang="ts">
import {
  IonPage, IonHeader, IonToolbar, IonTitle, IonContent, IonCard, IonCardHeader,
  IonCardTitle, IonCardContent, IonItem, IonLabel, IonButton, IonGrid,
  IonRow, IonCol, IonButtons, IonIcon, IonSpinner, IonChip, IonModal,
  IonCardSubtitle, IonSelect, IonSelectOption, IonCheckbox, toastController, 
  alertController
} from '@ionic/vue';
import { 
  addOutline, imagesOutline, eyeOutline, trashOutline, closeOutline,
  cloudUploadOutline, peopleOutline, personOutline, statsChartOutline
} from 'ionicons/icons';
import { useRouter } from 'vue-router';
import { ref, onMounted, reactive, computed } from 'vue';
import axios from 'axios';
import { imageService } from '@/service/imagenService';


// Interfaces corregidas según el backend
interface ImagenDTO {
  id: number;
  nombre: string;
  tipoContenido: string;
  tamaño: number; // Corregido: era tamano
  usuarioId: number;
  grupoId: number;
  eventoId?: number;
  nombreUsuario?: string;
  fechaCreacion: string; // Corregido: era fechaSubida
  datos?: string; // Base64 data
}

interface EventoDTO {
  id: number;
  titulo: string;
  descripcion?: string;
  ubicacion?: string;
  fecha: string;
  grupoId: number;
  grupoNombre?: string;
}

// Estado reactivo
const imagenes = ref<ImagenDTO[]>([]);
const eventos = ref<EventoDTO[]>([]);
const imagenesSeleccionadas = ref<number[]>([]);
const modoSeleccion = ref(false);

// Obtener datos de localStorage
const usuario = JSON.parse(localStorage.getItem('usuario') || '{}');
const grupoId = localStorage.getItem('grupoActivoId');
const router = useRouter();

const contexto = reactive({
  grupoId: parseInt(grupoId || '0'),
  usuarioId: usuario.id || 0,
  grupoNombre: '',
  usuarioNombre: usuario.nombre || ''
});

const filtros = reactive({
  tipo: 'grupo' as 'grupo' | 'usuario' | 'evento',
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
  imagen: null as ImagenDTO | null,
  imagenCompleta: null as ImagenDTO | null
});

// Referencias
const fileInput = ref<HTMLInputElement>();

// Computed properties
const eventosDisponibles = computed(() => eventos.value);

// Métodos principales
const cargarDatosGrupo = async () => {
  try {
    const response = await axios.get(`${import.meta.env.VITE_API_URL}/grupos/${contexto.grupoId}`);
    const grupo: any = response.data;
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
    // Corregido: usar la ruta correcta del backend
    const response = await axios.get(`${import.meta.env.VITE_API_URL}/eventos/${contexto.grupoId}/eventos`);
    eventos.value = response.data as EventoDTO[];
  } catch (error) {
    console.error('Error al cargar eventos:', error);
    showToast('Error al cargar los eventos', 'warning');
    eventos.value = [];
  } finally {
    estado.cargandoEventos = false;
  }
};
const cancelarSubida = () => {
  cerrarModalSubida();
  router.push(`/dashboard/${contexto.grupoId}/galeria`);
};

const aplicarFiltro = async () => {
  estado.cargando = true;
  try {
    let imagenesData: ImagenDTO[] = [];

    switch (filtros.tipo) {
      case 'grupo':
        imagenesData = await imageService.getByGrupo(contexto.grupoId);
        break;
      case 'usuario':
        imagenesData = await imageService.getByUsuario(contexto.usuarioId);
        break;
      case 'evento':
        if (filtros.eventoId) {
          imagenesData = await imageService.getByEvento(filtros.eventoId);
        }
        break;
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
  modalSubida.eventoId = filtros.eventoId;
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
    
    if (file.size > 5 * 1024 * 1024) {
      showToast('El archivo no puede superar los 5MB', 'warning');
      return;
    }
    
    modalSubida.archivo = file;
    
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
    const data = await imageService.uploadImage(formData) as { mensaje?: string };
    showToast(data.mensaje || 'Imagen subida correctamente', 'success');
    cerrarModalSubida();
    await aplicarFiltro();
  } catch (error: any) {
    console.error('Error al subir imagen:', error);
    const mensaje = error?.response?.data?.error || 'Error al subir la imagen';
    showToast(mensaje, 'danger');
  } finally {
    estado.subiendo = false;
  }
};


const abrirVistaPrevia = async (imagen: ImagenDTO) => {
  modalVistaPrevia.imagen = imagen;
  modalVistaPrevia.abierto = true;

  try {
    const fullImage = await imageService.getFullImage(imagen.id) as ImagenDTO;
    modalVistaPrevia.imagenCompleta = fullImage;
  } catch (error) {
    console.error('Error al cargar imagen completa:', error);
  }
};


const cerrarVistaPrevia = () => {
  modalVistaPrevia.abierto = false;
  modalVistaPrevia.imagen = null;
  modalVistaPrevia.imagenCompleta = null;
};

const confirmarEliminacion = async (imagen: ImagenDTO) => {
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
    const data = await imageService.deleteImage(id) as { mensaje?: string };
    showToast(data.mensaje || 'Imagen eliminada correctamente', 'success');
    await aplicarFiltro();

    if (modalVistaPrevia.abierto && modalVistaPrevia.imagen?.id === id) {
      cerrarVistaPrevia();
    }
  } catch (error: any) {
    console.error('Error al eliminar imagen:', error);
    const mensaje = error?.response?.data?.error || 'No se pudo eliminar la imagen';
    showToast(mensaje, 'danger');
  }
};


// Métodos para selección múltiple
const iniciarSeleccionMultiple = (imagen: ImagenDTO) => {
  modoSeleccion.value = true;
  imagenesSeleccionadas.value = [imagen.id];
};

const toggleSeleccion = (imagen: ImagenDTO) => {
  if (modoSeleccion.value) {
    const index = imagenesSeleccionadas.value.indexOf(imagen.id);
    if (index > -1) {
      imagenesSeleccionadas.value.splice(index, 1);
    } else {
      imagenesSeleccionadas.value.push(imagen.id);
    }
    
    if (imagenesSeleccionadas.value.length === 0) {
      modoSeleccion.value = false;
    }
  } else {
    abrirVistaPrevia(imagen);
  }
};

const limpiarSeleccion = () => {
  imagenesSeleccionadas.value = [];
  modoSeleccion.value = false;
};

const confirmarEliminacionMultiple = async () => {
  const imagenesDelUsuario = imagenes.value.filter(img => 
    imagenesSeleccionadas.value.includes(img.id) && img.usuarioId === contexto.usuarioId
  );

  if (imagenesDelUsuario.length !== imagenesSeleccionadas.value.length) {
    showToast('Solo puedes eliminar tus propias imágenes', 'warning');
    return;
  }

  const alert = await alertController.create({
    header: 'Confirmar eliminación múltiple',
    message: `¿Estás seguro de que quieres eliminar ${imagenesSeleccionadas.value.length} imagen${imagenesSeleccionadas.value.length !== 1 ? 'es' : ''}?`,
    buttons: [
      {
        text: 'Cancelar',
        role: 'cancel'
      },
      {
        text: 'Eliminar todas',
        role: 'destructive',
        handler: () => eliminarImagenesMultiple()
      }
    ]
  });
  
  await alert.present();
};

const eliminarImagenesMultiple = async () => {
  try {
    const promesas = imagenesSeleccionadas.value.map(id => 
      imageService.deleteImage(id)
    );
    
    await Promise.all(promesas);
    
    showToast(`${imagenesSeleccionadas.value.length} imágenes eliminadas correctamente`, 'success');
    limpiarSeleccion();
    await aplicarFiltro();
  } catch (error: any) {
    console.error('Error al eliminar imágenes:', error);
    const mensaje = error?.response?.data?.error || 'Error al eliminar las imágenes';
    showToast(mensaje, 'danger');
  }
};

// Métodos utilitarios
const getEventoTitulo = (eventoId: number | undefined): string => {
  if (!eventoId) return '';
  const evento = eventos.value.find(e => e.id === eventoId);
  return evento ? evento.titulo : `Evento ${eventoId}`;
};

const formatearFecha = (fecha: string): string => {
  return new Date(fecha).toLocaleDateString('es-ES', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  });
};

const formatearFechaCompleta = (fecha: string): string => {
  return new Date(fecha).toLocaleString('es-ES', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
};

const formatearTamano = (bytes: number): string => {
  if (bytes === 0) return '0 Bytes';
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};

const crearImagenUrl = (datos: string, tipoContenido: string): string => {
  return `data:${tipoContenido};base64,${datos}`;
};

const showToast = async (message: string, color: 'success' | 'warning' | 'danger' | 'primary' = 'primary') => {
  const toast = await toastController.create({
    message,
    duration: 3000,
    position: 'bottom',
    color
  });
  await toast.present();
};

// Lifecycle hooks
onMounted(async () => {
  if (!contexto.grupoId || !contexto.usuarioId) {
    showToast('Error: No se encontraron datos del grupo o usuario', 'danger');
    router.replace('/grupo');
    return;
  }
  await cargarDatosGrupo();
  await cargarEventos();
  await aplicarFiltro();
});
</script>