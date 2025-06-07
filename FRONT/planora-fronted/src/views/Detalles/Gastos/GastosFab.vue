<template>
  <ion-fab slot="fixed" vertical="bottom" horizontal="end">
    <ion-fab-button color="primary" @click="$emit('crear')">
      <ion-icon :icon="add" />
    </ion-fab-button>
  </ion-fab>
</template>

<script setup>
import { IonFab, IonFabButton, IonIcon } from '@ionic/vue'
import { defineEmits } from 'vue'
import { add } from 'ionicons/icons'

defineEmits(['crear'])
</script>
