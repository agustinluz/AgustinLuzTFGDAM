
// SeleccionParticipantes.vue
<template>
  <ion-card class="modern-card">
    <ion-card-header>
      <div class="card-header">
        <ion-icon :icon="peopleOutline" class="header-icon" />
        <ion-card-title>Participantes</ion-card-title>
      </div>
    </ion-card-header>
    <ion-card-content>
      <ion-item class="modern-item">
        <ion-select
          v-model="formulario.pagadoPorId"
          placeholder="Seleccionar quien pagó"
          interface="popover"
          fill="outline"
          :class="{ 'ion-invalid': errores.pagadoPorId }"
          @ionChange="() => validar('pagadoPorId')"
        >
          <ion-select-option v-for="u in usuarios" :key="u.id" :value="u.id">
            {{ u.nombre }}
          </ion-select-option>
        </ion-select>
      </ion-item>
      <ion-note v-if="errores.pagadoPorId" color="danger" class="error-note">{{ errores.pagadoPorId }}</ion-note>

      <div class="participants-section ion-margin-top">
        <div class="section-label">
          <ion-icon :icon="checkboxOutline" />
          <span>Participantes ({{ formulario.participantesIds.length }})</span>
        </div>
        <div class="participants-grid">
          <div
            v-for="u in usuarios"
            :key="u.id"
            class="participant-card"
            :class="{ selected: formulario.participantesIds.includes(u.id) }"
            @click="() => toggle(u.id)"
          >
            <div class="participant-avatar">{{ getInitials(u.nombre) }}</div>
            <div class="participant-info">
              <h4>{{ u.nombre }}</h4>
            </div>
            <ion-checkbox :checked="formulario.participantesIds.includes(u.id)" />
          </div>
        </div>
        <ion-note v-if="errores.participantes" color="danger" class="error-note">{{ errores.participantes }}</ion-note>
      </div>
    </ion-card-content>
  </ion-card>
</template>
<script setup>
import { IonCard, IonCardHeader, IonCardTitle, IonCardContent, IonItem, IonSelect, IonSelectOption, IonNote, IonIcon, IonCheckbox } from '@ionic/vue';
import { peopleOutline, checkboxOutline } from 'ionicons/icons';
const props = defineProps({ usuarios: Array, formulario: Object, errores: Object });
const emit = defineEmits(['toggle','validar']);
const toggle = id => emit('toggle', id);
const validar = field => emit('validar', field);
const getInitials = name => name.split(' ').map(n=>n[0]).join('').toUpperCase().slice(0,2);
</script>
