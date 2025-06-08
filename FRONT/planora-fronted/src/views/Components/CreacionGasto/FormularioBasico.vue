
// FormularioBasico.vue
<template>
  <ion-card class="modern-card">
    <ion-card-header>
      <div class="card-header">
        <ion-icon :icon="receiptOutline" class="header-icon" />
        <ion-card-title>Información del Gasto</ion-card-title>
      </div>
    </ion-card-header>
    <ion-card-content>
      <ion-item class="modern-item">
        <ion-input
          v-model="formulario.titulo"
          placeholder="Título del gasto"
          fill="outline"
          :class="{ 'ion-invalid': errores.titulo }"
          @ionBlur="() => validar('titulo')"
        />
      </ion-item>
      <ion-note v-if="errores.titulo" color="danger" class="error-note">{{ errores.titulo }}</ion-note>

      <ion-item class="modern-item ion-margin-top">
        <ion-input
          v-model="formulario.monto"
          placeholder="0.00"
          type="number"
          step="0.01"
          fill="outline"
          :class="{ 'ion-invalid': errores.monto }"
          @ionBlur="() => validar('monto')"
        >
          <div slot="end" class="currency-symbol">€</div>
        </ion-input>
      </ion-item>
      <ion-note v-if="errores.monto" color="danger" class="error-note">{{ errores.monto }}</ion-note>
    </ion-card-content>
  </ion-card>
</template>
<script setup>
import { IonCard, IonCardHeader, IonCardTitle, IonCardContent, IonItem, IonInput, IonNote, IonIcon } from '@ionic/vue';
import { receiptOutline } from 'ionicons/icons';
const props = defineProps({ formulario: Object, errores: Object });
const emit = defineEmits(['validar']);
const validar = field => emit('validar', field);
</script>
