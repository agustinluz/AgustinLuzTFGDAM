<template>
  <div class="estado-vacio">
    <div class="empty-illustration">
      <ion-icon :icon="people" color="primary" />
    </div>
    <div class="empty-content">
      <h2>¡Comienza tu aventura!</h2>
      <p>Crea tu primer grupo o únete a uno existente para empezar a compartir momentos increíbles.</p>
    </div>
    <div class="empty-actions">
      <ion-button expand="block" class="primary-action" :disabled="loading" @click="emit('create')">
        <ion-icon :icon="add" slot="start" />
        Crear nuevo grupo
      </ion-button>
      <div class="divider"><span>o</span></div>
      <ion-item class="input-item">
        <ion-input v-model="code" placeholder="Código de invitación" :disabled="loading" />
      </ion-item>
      <ion-button expand="block" fill="outline" class="secondary-action"
       :disabled="loading || !code.trim()" @click="emit('join')">
        <ion-icon :icon="logIn" slot="start" />
        Unirse al grupo
      </ion-button>
      <ion-text color="danger" v-if="error" class="error-text">
        <p>{{ error }}</p>
      </ion-text>
    </div>
  </div>
</template>

<script setup lang="ts">
import { IonIcon, IonButton, IonItem, IonInput, IonText } from '@ionic/vue'
import { people, add, logIn } from 'ionicons/icons'

const props = defineProps<{ loading: boolean, error: string }>()
const code = defineModel<string>('code', { default: '' })
const emit = defineEmits(['create', 'join'])
</script>

    <style scoped>
    .estado-vacio { display:flex; flex-direction:column; align-items:center; justify-content:center; min-height:500px; text-align:center; padding:40px 20px; }
    .empty-illustration { margin-bottom:24px; }
    .empty-illustration ion-icon { font-size:100px; opacity:0.6; }
    .empty-content h2 { font-size:1.6em; font-weight:700; margin:0 0 12px 0; color:var(--ion-color-dark); }
    .empty-content p { color:var(--ion-color-medium); max-width:280px; line-height:1.5; margin:0 0 32px 0; }
    .empty-actions { width:100%; max-width:300px; }
    .divider { text-align:center; margin:20px 0; position:relative; }
    .divider::before { content:''; position:absolute; top:50%; left:0; right:0; height:1px; background:var(--ion-color-light); }
    .divider span { background:var(--ion-background-color); padding:0 16px; color:var(--ion-color-medium); font-size:0.9em; }
    .primary-action { --border-radius:12px; height:48px; font-weight:600; margin-bottom:8px; }
    .secondary-action { --border-radius:12px; height:48px; font-weight:600; margin-top:8px; }
    .input-item { border-radius:12px; background:white; margin:8px 0; box-shadow:0 2px 8px rgba(0,0,0,0.05); }
    .error-text { text-align:center; margin-top:12px; }
    </style>