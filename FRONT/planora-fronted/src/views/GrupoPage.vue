<template>
  <ion-page>
    <ion-header>
      <ion-toolbar color="primary">
        <ion-title>Mis Grupos</ion-title>
        <ion-buttons slot="end">
          <ion-button fill="clear" @click="() => router.push('/perfil')">
            <ion-icon :icon="person" slot="icon-only" />
          </ion-button>
          <ion-button fill="clear" @click="() => router.push('/invitaciones')">
            <ion-icon :icon="mailOutline" slot="icon-only" />
          </ion-button>
          <ion-button fill="clear" @click="logout">
            <ion-icon :icon="logOutOutline" slot="icon-only" />
          </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>

    <ion-content :fullscreen="true" class="content-bg">
      <ActiveGroupBanner :grupo="grupoActivo" />
      <div class="container-content">
        <GroupList
          v-if="grupos.length > 0"
          :grupos="grupos"
          :activo-id="grupoActivo?.id ?? null"
          :is-admin="esAdmin"
          @select="entrarEnGrupo"
        />
        <NoGroups
          v-else
          v-model:code="codigo"
          :loading="cargando"
          :error="error"
          @create="abrirModalCrear"
          @join="unirseGrupo"
        />
      </div>

      <ion-fab v-if="grupos.length > 0" vertical="bottom" horizontal="end" slot="fixed">
        <ion-fab-button @click="abrirModal" color="secondary">
          <ion-icon :icon="add" />
        </ion-fab-button>
      </ion-fab>
    </ion-content>

    <CreateGroupModal
      v-model:name="nombreGrupo"
      :open="modalCrearGrupo"
      :preview="previsualizacionImagen"
      :loading="cargando"
      :error="error"
      @close="cerrarModalCrear"
      @pick="manejarImagenGrupo"
      @create="crearGrupo"
    />

    <JoinGroupModal
      v-model:code="codigo"
      :open="modalAbierto"
      :loading="cargando"
      :error="error"
      @close="cerrarModal"
      @create="() => { cerrarModal(); abrirModalCrear(); }"
      @join="unirseGrupo"
    />

    <ion-loading :is-open="cargando" message="Cargando..." />
  </ion-page>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  IonPage, IonHeader, IonToolbar, IonTitle, IonContent, IonButtons, IonButton, IonIcon,
  IonFab, IonFabButton, IonLoading
} from '@ionic/vue'
import { mailOutline, person, logOutOutline, add } from 'ionicons/icons'
import { toastController } from '@ionic/vue'

import ActiveGroupBanner from '@/views/Components/Grupo/ActiveGroupBanner.vue'
import GroupList from '@/views/Components/Grupo/GroupList.vue'
import NoGroups from '@/views/Components/Grupo/NoGroups.vue'
import CreateGroupModal from '@/views/Components/Grupo/CreateGroupModal.vue'
import JoinGroupModal from '@/views/Components/Grupo/JoinGroupModal.vue'

const router = useRouter()
const usuario = ref<any | null>(null)
const codigo = ref('')
const error = ref('')
const grupos = ref<any[]>([])
const cargando = ref(false)
const modalAbierto = ref(false)
const modalCrearGrupo = ref(false)

const nombreGrupo = ref('')
const imagenGrupo = ref('')
const previsualizacionImagen = ref('')

const grupoActivo = computed(() => {
  const grupoActivoId = localStorage.getItem('grupoActivoId')
  return grupos.value.find(g => g.id.toString() === grupoActivoId)
})

onMounted(() => {
  const stored = localStorage.getItem('usuario')
  if (stored) {
    usuario.value = JSON.parse(stored)
    cargarGrupos()
  }
})

const cargarGrupos = async () => {
  if (!usuario.value) return
  cargando.value = true
  try {
    const res = await fetch(`${import.meta.env.VITE_API_URL}/grupos/usuario/${usuario.value.id}`)
    if (res.ok) {
      grupos.value = await res.json()
      for (const g of grupos.value) {
        const [uRes, eRes, fRes] = await Promise.all([
          fetch(`${import.meta.env.VITE_API_URL}/grupos/${g.id}/usuarios`),
          fetch(`${import.meta.env.VITE_API_URL}/eventos/${g.id}/eventos`),
          fetch(`${import.meta.env.VITE_API_URL}/imagenes/grupo/${g.id}`)
        ])
        g.participantesCount = uRes.ok ? (await uRes.json()).length : 0
        g.eventosCount = eRes.ok ? (await eRes.json()).length : 0
        if (fRes.ok) {
          const data = await fRes.json()
          g.fotosCount = data.total || 0
        } else {
          g.fotosCount = 0
        }
      }
    } else {
      grupos.value = []
    }
  } catch (err) {
    console.error('Error al cargar grupos:', err)
  }
  cargando.value = false
}

const manejarImagenGrupo = (event: Event) => {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (file) {
    if (file.size > 5 * 1024 * 1024) {
      error.value = 'La imagen no puede ser mayor a 5MB'
      return
    }
    const reader = new FileReader()
    reader.onload = e => {
      const base64 = (e.target as FileReader).result as string
      imagenGrupo.value = base64
      previsualizacionImagen.value = base64
    }
    reader.readAsDataURL(file)
  }
}

const crearGrupo = async () => {
  if (!nombreGrupo.value.trim()) {
    error.value = 'Por favor introduce un nombre para el grupo'
    return
  }
  error.value = ''
  cargando.value = true
  try {
    const res = await fetch(`${import.meta.env.VITE_API_URL}/grupos`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'usuarioId': usuario.value.id.toString()
      },
      body: JSON.stringify({ nombre: nombreGrupo.value, imagenPerfil: imagenGrupo.value })
    })
    if (!res.ok) throw new Error('Error al crear grupo')
    const grupo = await res.json()
    localStorage.setItem('grupoActivoId', grupo.id)
    cerrarModalCrear()
    const toast = await toastController.create({
      message: `Grupo "${grupo.nombre}" creado exitosamente. Código: ${grupo.codigoInvitacion}`,
      duration: 3000,
      position: 'bottom',
      color: 'success'
    })
    toast.present()
    cargarGrupos()
  } catch (err) {
    error.value = 'Error al crear el grupo. Intenta de nuevo.'
  }
  cargando.value = false
}

const unirseGrupo = async () => {
  if (!codigo.value.trim()) {
    error.value = 'Por favor introduce un código válido'
    return
  }
  error.value = ''
  cargando.value = true
  try {
    const resGrupo = await fetch(`${import.meta.env.VITE_API_URL}/auth/invitacion/${codigo.value}`)
    if (!resGrupo.ok) throw new Error('Código no válido')
    const grupo = await resGrupo.json()
    await fetch(`${import.meta.env.VITE_API_URL}/grupos/${grupo.id}/usuarios`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        nombre: usuario.value.nombre,
        email: usuario.value.email,
        password: usuario.value.password
      })
    })
    localStorage.setItem('grupoActivoId', grupo.id)
    cerrarModal()
    const toast = await toastController.create({
      message: `Te has unido al grupo "${grupo.nombre}" exitosamente`,
      duration: 3000,
      position: 'top',
      color: 'success'
    })
    toast.present()
    cargarGrupos()
  } catch (err) {
    error.value = 'No se pudo unir al grupo. Verifica el código.'
  }
  cargando.value = false
}

const entrarEnGrupo = async (id: number) => {
  localStorage.setItem('grupoActivoId', id.toString())
  const toast = await toastController.create({
    message: `Accediendo al grupo...`,
    duration: 1500,
    position: 'bottom',
    color: 'primary'
  })
  await toast.present()
  await router.push(`/dashboard/${id}`)
}

const abrirModalCrear = () => {
  nombreGrupo.value = ''
  imagenGrupo.value = ''
  previsualizacionImagen.value = ''
  error.value = ''
  modalCrearGrupo.value = true
}

const cerrarModalCrear = () => {
  modalCrearGrupo.value = false
  nombreGrupo.value = ''
  imagenGrupo.value = ''
  previsualizacionImagen.value = ''
  error.value = ''
}

const abrirModal = () => {
  codigo.value = ''
  error.value = ''
  modalAbierto.value = true
}

const cerrarModal = () => {
  modalAbierto.value = false
  codigo.value = ''
  error.value = ''
}

const esAdmin = (grupo: any) => grupo.adminId === usuario.value?.id

const logout = () => {
  localStorage.removeItem('usuario')
  localStorage.removeItem('grupoActivoId')
  localStorage.removeItem('token')
  router.push('/login')
}
</script>

<style scoped>
.content-bg {
  --background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}
.container-content { padding: 0 16px 100px; }
</style>