<template>
  <ion-modal :is-open="open" @didDismiss="emit('close')">
    <ion-header>
      <ion-toolbar>
        <ion-title>Gestionar Grupos</ion-title>
        <ion-buttons slot="end">
          <ion-button @click="emit('close')">
            <ion-icon :icon="close" />
          </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>
    <ion-content class="ion-padding">
      <div class="modal-content">
        <ion-button expand="block" class="primary-action" :disabled="loading" @click="emit('create')">
          <ion-icon :icon="add" slot="start" />
          Crear nuevo grupo
        </ion-button>
        <div class="divider"><span>o</span></div>
        <ion-item class="input-item">
          <ion-input v-model="code" placeholder="Código de invitación" :disabled="loading" clear-input />
        </ion-item>
        <ion-button expand="block" fill="outline" class="secondary-action" :disabled="loading || !code.trim()" @click="emit('join')">
          <ion-icon :icon="logIn" slot="start" />
          Unirse al grupo
        </ion-button>
        <ion-text color="danger" v-if="error" class="error-text">
          <p>{{ error }}</p>
        </ion-text>
      </div>
    </ion-content>
  </ion-modal>
</template>

<script setup lang="ts">
import { IonModal, IonHeader, IonToolbar, IonTitle, IonButtons, IonButton, IonIcon, IonContent, IonItem, IonInput, IonText } from '@ionic/vue'
import { add, logIn, close } from 'ionicons/icons'

const props = defineProps<{ open: boolean, loading: boolean, error: string }>()
const code = defineModel<string>('code', { default: '' })
const emit = defineEmits(['close', 'create', 'join'])
</script>
<style scoped>
.modal-content { padding: 8px 0; }
.divider { text-align: center; margin: 20px 0; position: relative; }
.divider::before { content:''; position:absolute; top:50%; left:0; right:0; height:1px; background: var(--ion-color-light); }
.divider span { background: var(--ion-background-color); padding:0 16px; color: var(--ion-color-medium); font-size:var(--font-size-sm); }
.primary-action { --border-radius:12px; height:48px; font-weight:600; margin-bottom:8px; }
.secondary-action { --border-radius:12px; height:48px; font-weight:600; margin-top:8px; }
.input-item { border-radius:12px; background:white; margin:8px 0; box-shadow:0 2px 8px rgba(0,0,0,0.05); }
.error-text { text-align:center; margin-top:12px; }
</style>