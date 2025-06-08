
// DivisionGasto.vue
<template>
  <ion-card class="modern-card">
    <ion-card-header>
      <div class="card-header">
        <ion-icon :icon="calculatorOutline" class="header-icon" />
        <ion-card-title>División del Gasto</ion-card-title>
      </div>
    </ion-card-header>
    <ion-card-content>
      <ion-radio-group v-model="formulario.partesIguales" @ionChange="() => cambio()">
        <ion-item>
          <ion-radio slot="start" :value="true" />
          <ion-label>Partes iguales</ion-label>
        </ion-item>
        <ion-item>
          <ion-radio slot="start" :value="false" />
          <ion-label>Cantidades personalizadas</ion-label>
        </ion-item>
      </ion-radio-group>
      <div v-if="!formulario.partesIguales && participantes.length">
        <div v-for="u in participantes" :key="u.id" class="amount-item">
          <span>{{ u.nombre }}</span>
          <ion-input v-model="formulario.cantidadesPersonalizadas[u.id]" @ionInput="() => calcular()" placeholder="0.00" />
        </div>
        <ion-note v-if="errores.cantidades" color="danger">{{ errores.cantidades }}</ion-note>
      </div>
    </ion-card-content>
  </ion-card>
</template>
<script setup>
import { IonCard, IonCardHeader, IonCardTitle, IonCardContent, IonIcon, IonRadioGroup, IonItem, IonRadio, IonLabel, IonInput, IonNote } from '@ionic/vue';
import { calculatorOutline } from 'ionicons/icons';
const props = defineProps({ formulario: Object, participantes: Array, errores: Object });
const emit = defineEmits(['cambio','calcular']);
const cambio = () => emit('cambio');
const calcular = () => emit('calcular');
</script>
