<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-title>Galería de Imágenes</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding">
      <!-- Subir Imagen -->
      <ion-card>
        <ion-card-header>
          <ion-card-title>Subir Imagen</ion-card-title>
        </ion-card-header>
        <ion-card-content>
          <ion-item>
            <ion-label position="floating">Seleccionar Imagen</ion-label>
            <input type="file" accept="image/*" @change="onFileChange" />
          </ion-item>

          <ion-item>
            <ion-label position="floating">Grupo ID</ion-label>
            <ion-input type="number" v-model="grupoId" />
          </ion-item>

          <ion-item>
            <ion-label position="floating">Usuario ID</ion-label>
            <ion-input type="number" v-model="usuarioId" />
          </ion-item>

          <ion-item>
            <ion-label position="floating">Evento ID</ion-label>
            <ion-input type="number" v-model="eventoId" />
          </ion-item>

          <ion-button expand="block" @click="subirImagen" :disabled="!archivo">Subir</ion-button>
        </ion-card-content>
      </ion-card>

      <!-- Galería -->
      <ion-card v-if="imagenes.length">
        <ion-card-header>
          <ion-card-title>Imágenes del Grupo</ion-card-title>
        </ion-card-header>
        <ion-card-content>
          <ion-grid>
            <ion-row>
              <ion-col size="6" v-for="img in imagenes" :key="img.id">
                <ion-card>
                  <img :src="getImagenUrl(img.id)" alt="imagen" />
                  <ion-card-content>
                    <p>{{ img.nombre }}</p>
                    <ion-button color="danger" fill="clear" size="small" @click="eliminarImagen(img.id)">
                      Eliminar
                    </ion-button>
                  </ion-card-content>
                </ion-card>
              </ion-col>
            </ion-row>
          </ion-grid>
        </ion-card-content>
      </ion-card>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import {
  IonPage,
  IonHeader,
  IonToolbar,
  IonTitle,
  IonContent,
  IonCard,
  IonCardHeader,
  IonCardTitle,
  IonCardContent,
  IonItem,
  IonLabel,
  IonInput,
  IonButton,
  IonGrid,
  IonRow,
  IonCol,
  toastController,
} from '@ionic/vue';
import { ref, onMounted } from 'vue';
import axios from 'axios';

const archivo = ref<File | null>(null);
const grupoId = ref<number | null>(null);
const usuarioId = ref<number | null>(null);
const eventoId = ref<number | null>(null);
const imagenes = ref<any[]>([]);

const API_URL = import.meta.env.VITE_API_URL + '/imagenes';

const onFileChange = (event: Event) => {
  const target = event.target as HTMLInputElement;
  if (target.files && target.files.length > 0) {
    archivo.value = target.files[0];
  }
};

const subirImagen = async () => {
  if (!archivo.value || !grupoId.value) {
    showToast('Selecciona una imagen y un grupo');
    return;
  }

  const formData = new FormData();
  formData.append('archivo', archivo.value);
  if (eventoId.value) formData.append('eventoId', eventoId.value.toString());
  if (usuarioId.value) formData.append('usuarioId', usuarioId.value.toString());
  formData.append('grupoId', grupoId.value.toString());

  try {
    await axios.post(`${API_URL}/subir`, formData);
    showToast('Imagen subida correctamente');
    obtenerImagenes();
  } catch (error: any) {
    showToast(error?.response?.data || 'Error al subir imagen');
  }
};

const obtenerImagenes = async () => {
  if (!grupoId.value) return;

  try {
    const res = await axios.get(`${API_URL}/grupo/${grupoId.value}`);
    imagenes.value = res.data as any[];
  } catch (error) {
    showToast('Error al obtener imágenes');
  }
};

const eliminarImagen = async (id: number) => {
  try {
    await axios.delete(`${API_URL}/${id}`);
    showToast('Imagen eliminada');
    obtenerImagenes();
  } catch (error) {
    showToast('No se pudo eliminar');
  }
};

const getImagenUrl = (id: number) => `${API_URL}/${id}/datos`;

const showToast = async (message: string) => {
  const toast = await toastController.create({
    message,
    duration: 2000,
    position: 'bottom',
  });
  toast.present();
};

onMounted(() => {
  // Puedes autollenar grupoId si ya está disponible por contexto
});
</script>

<style scoped>
img {
  width: 100%;
  height: 150px;
  object-fit: cover;
  border-radius: 8px;
}
</style>
