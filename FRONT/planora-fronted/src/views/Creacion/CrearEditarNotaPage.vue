<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-buttons slot="start">
          <ion-button @click="volver()">
            <ion-icon :icon="arrowBack" slot="icon-only" />
          </ion-button>
        </ion-buttons>

        <ion-title>{{ esEdicion ? 'Editar Nota' : 'Nueva Nota' }}</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding">
      <NotaForm :titulo="titulo" :contenido="contenido" :cargando="isLoading" :modo="esEdicion ? 'editar' : 'crear'"
        @guardar="guardarNota" @update:titulo="titulo = $event" @update:contenido="contenido = $event" />

      <ion-toast :is-open="showToast" :message="toastMessage" :color="toastColor" :duration="3000" position="top"
        @did-dismiss="showToast = false" />

      <ion-loading :is-open="isLoading" message="Procesando..." />
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import {
  IonPage, IonHeader, IonToolbar, IonButtons, IonTitle, IonContent,
  IonBackButton, IonToast, IonLoading, IonButton
} from '@ionic/vue'
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NotasService } from '../../service/NotaService'
import NotaForm from '@/views/Components/Nota/NotaForm.vue'
import { arrowBack } from 'ionicons/icons'

const route = useRoute()
const router = useRouter()

const grupoId = ref(route.params.grupoId || route.params.id)
const notaId = ref(route.params.notaId)

const titulo = ref('')
const contenido = ref('')
const isLoading = ref(false)

const esEdicion = computed(() => !!notaId.value)

// Toast
const showToast = ref(false)
const toastMessage = ref('')
const toastColor = ref('success')

const mostrarToast = (message: string, color = 'success') => {
  toastMessage.value = message
  toastColor.value = color
  showToast.value = true
}

onMounted(async () => {
  if (esEdicion.value) {
    try {
      isLoading.value = true
      const token = localStorage.getItem('token')
      if (!token) throw new Error('Token no encontrado')
      const nota = await NotasService.obtenerNotaPorId(Number(notaId.value),token)
      titulo.value = nota.titulo
      contenido.value = nota.contenido
    } catch (error) {
      console.error('Error cargando nota:', error)
      mostrarToast('Error al cargar la nota', 'danger')
      volver()
    } finally {
      isLoading.value = false
    }
  }
})


const guardarNota = async () => {
  if (!titulo.value.trim() || !contenido.value.trim()) {
    mostrarToast('Por favor, completa todos los campos', 'warning')
    return
  }

  isLoading.value = true
  try {
    if (esEdicion.value) {
      const token = localStorage.getItem('token')
      if (!token) throw new Error('Token no encontrado')

      await NotasService.actualizarNota(Number(notaId.value), {
        titulo: titulo.value.trim(),
        contenido: contenido.value.trim()
      }, token)
      mostrarToast('Nota actualizada', 'success')
    } else {
      const token = localStorage.getItem('token')
      if (!token) throw new Error('Token no encontrado')

      await NotasService.crearNota(Number(grupoId.value), {
        titulo: titulo.value.trim(),
        contenido: contenido.value.trim()
      }, token)
      mostrarToast('Nota creada', 'success')
    }

    setTimeout(() => router.back(), 1500)
  } catch (error) {
    console.error('Error guardando nota:', error)
    mostrarToast('Error al guardar la nota', 'danger')
  } finally {
    isLoading.value = false
  }
}

const volver = () => {
  router.replace(`/dashboard/${grupoId.value}/notas`)
}
</script>
