<template>
  <ion-modal :is-open="open" @didDismiss="emit('close')">
    <ion-header>
      <ion-toolbar>
        <ion-title>Crear Nuevo Grupo</ion-title>
        <ion-buttons slot="end">
          <ion-button @click="emit('close')">
            <ion-icon :icon="close" />
          </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>
    <ion-content class="ion-padding">
      <div class="modal-content">
        <div class="selector-imagen">
          <div class="preview-imagen" @click="() => input?.click()">
            <img v-if="preview" :src="preview" alt="Preview" />
            <ion-icon v-else :icon="people" size="large" />
            <div class="overlay-upload">
              <ion-icon :icon="cloudUpload" color="light" />
            </div>
          </div>
          <p>Toca para agregar foto</p>
          <input ref="input" type="file" accept="image/*" @change="e => emit('pick', e)" style="display:none" />
        </div>
        <ion-item class="input-item">
          <ion-input v-model="name" placeholder="Nombre del grupo" clear-input />
        </ion-item>
        <ion-text color="danger" v-if="error" class="error-text">
          <p>{{ error }}</p>
        </ion-text>
        <ion-button expand="block" class="create-btn" :disabled="loading || !name.trim()" @click="emit('create')">
          {{ loading ? 'Creando...' : 'Crear Grupo' }}
        </ion-button>
      </div>
    </ion-content>
  </ion-modal>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { IonModal, IonHeader, IonToolbar, IonTitle, IonButtons, IonButton, IonIcon, IonContent, IonItem, IonInput, IonText } from '@ionic/vue'
import { people, cloudUpload, close } from 'ionicons/icons'

const props = defineProps<{ open: boolean, preview: string, loading: boolean, error: string }>()
const name = defineModel<string>('name', { default: '' })
const emit = defineEmits(['close', 'create', 'pick'])
const input = ref<HTMLInputElement>()
</script>

<style scoped>
.modal-content { padding: 8px 0; }
.selector-imagen { text-align: center; margin-bottom: 32px; }
.preview-imagen {
  width: 100px; height: 100px; margin: 0 auto 12px; border-radius: 50%; background: var(--ion-color-light);
  display: flex; align-items: center; justify-content: center; overflow: hidden; position: relative; cursor: pointer; border: 3px solid var(--ion-color-primary-tint);
}
.preview-imagen img { width: 100%; height: 100%; object-fit: cover; }
.overlay-upload { position: absolute; inset: 0; background: rgba(0,0,0,0.6); display:flex; align-items:center; justify-content:center; opacity:0; transition:opacity 0.3s ease; }
.preview-imagen:hover .overlay-upload { opacity:1; }
.selector-imagen p { color: var(--ion-color-medium); font-size: var(--font-size-sm); }
.create-btn { --border-radius: 12px; height: 48px; font-weight: 600; margin-top: 24px; }
.input-item { border-radius: 12px; background: white; margin: 8px 0; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
.error-text { text-align: center; margin-top: 12px; }
</style>