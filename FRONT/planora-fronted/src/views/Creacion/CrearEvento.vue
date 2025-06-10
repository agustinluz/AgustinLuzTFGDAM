<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-buttons slot="start">
          <ion-back-button></ion-back-button>
        </ion-buttons>
        <ion-title>Nuevo evento</ion-title>
        <ion-buttons slot="end">
          <ion-button 
            @click="crearEvento" 
            :disabled="!puedeGuardar || isLoading"
            fill="clear"
          >
            <ion-icon name="checkmark" slot="icon-only"></ion-icon>
          </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>

    <ion-content>
      <form @submit.prevent="crearEvento" class="event-form">
        <!-- Información básica -->
        <div class="form-section">
          <h3 class="section-title">
            <ion-icon name="information-circle-outline"></ion-icon>
            Información del evento
          </h3>
          
          <ion-item>
            <ion-input
              label="Título del evento"
              label-placement="stacked"
              fill="outline"
              v-model="titulo"
              placeholder="Ej: Cena de cumpleaños"
              :maxlength="100"
              counter
              required
            ></ion-input>
          </ion-item>

          <ion-item class="textarea-item">
            <ion-textarea
              label="Descripción"
              label-placement="stacked"
              fill="outline"
              v-model="descripcion"
              placeholder="Describe los detalles del evento..."
              rows="4"
              auto-grow
              :maxlength="500"
              counter
            ></ion-textarea>
          </ion-item>
        </div>

        <!-- Fecha y hora -->
        <div class="form-section">
          <h3 class="section-title">
            <ion-icon name="calendar-outline"></ion-icon>
            Fecha y hora
          </h3>
          
          <ion-item button @click="abrirCalendario">
            <ion-label>
              <h3>Fecha del evento</h3>
              <p v-if="fecha">{{ formatearFecha(fecha) }}</p>
              <p v-else class="placeholder-text">Selecciona fecha y hora</p>
            </ion-label>
            <ion-icon name="chevron-forward-outline" slot="end"></ion-icon>
          </ion-item>

          <!-- Modal de calendario -->
          <ion-modal :is-open="mostrarCalendario" @did-dismiss="cerrarCalendario">
            <ion-header>
              <ion-toolbar>
                <ion-title>Seleccionar fecha</ion-title>
                <ion-buttons slot="end">
                  <ion-button @click="cerrarCalendario">Cerrar</ion-button>
                </ion-buttons>
              </ion-toolbar>
            </ion-header>
            <ion-content class="calendar-modal">
              <ion-datetime
                v-model="fecha"
                presentation="date-time"
                :min="fechaMinima"
                locale="es-ES"
                @ion-change="onFechaChange"
              ></ion-datetime>
              <div class="calendar-actions">
                <ion-button expand="block" @click="confirmarFecha" :disabled="!fecha">
                  Confirmar fecha
                </ion-button>
              </div>
            </ion-content>
          </ion-modal>
        </div>

        <!-- Ubicación -->
        <div class="form-section">
          <h3 class="section-title">
            <ion-icon name="location-outline"></ion-icon>
            Ubicación
          </h3>
          
          <ion-item>
            <ion-input
              label="Dirección"
              label-placement="stacked"
              fill="outline"
              v-model="ubicacion"
              placeholder="Ej: Calle Principal 123, Madrid"
              @ion-input="buscarUbicacion"
            ></ion-input>
          </ion-item>

          <ion-item button @click="abrirMapa" v-if="ubicacion">
            <ion-label>
              <h3>Ver en el mapa</h3>
              <p>{{ coordenadas ? `${coordenadas.lat.toFixed(6)}, ${coordenadas.lng.toFixed(6)}` : 'Buscar ubicación en el mapa' }}</p>
            </ion-label>
            <ion-icon name="map-outline" slot="end"></ion-icon>
          </ion-item>

          <!-- Modal de mapa -->
          <ion-modal :is-open="mostrarMapa" @did-dismiss="cerrarMapa">
            <ion-header>
              <ion-toolbar>
                <ion-title>Seleccionar ubicación</ion-title>
                <ion-buttons slot="end">
                  <ion-button @click="cerrarMapa">Cerrar</ion-button>
                </ion-buttons>
              </ion-toolbar>
            </ion-header>
            <ion-content>
              <div class="map-container">
                <div id="mapa" class="mapa"></div>
                <div class="map-search">
                  <ion-searchbar
                    v-model="busquedaUbicacion"
                    placeholder="Buscar dirección..."
                    @ion-input="buscarEnMapa"
                    show-clear-button="focus"
                  ></ion-searchbar>
                </div>
                <div class="map-actions">
                  <ion-button 
                    expand="block" 
                    @click="confirmarUbicacion"
                    :disabled="!coordenadas"
                  >
                    <ion-icon name="checkmark-outline" slot="start"></ion-icon>
                    Confirmar ubicación
                  </ion-button>
                </div>
              </div>
            </ion-content>
          </ion-modal>
        </div>

        <!-- Botón de guardar -->
        <div class="action-section">
          <ion-button 
            expand="block" 
            type="submit"
            :disabled="!puedeGuardar || isLoading"
            size="large"
          >
            <ion-icon name="calendar-outline" slot="start"></ion-icon>
            {{ isLoading ? 'Creando evento...' : 'Crear evento' }}
          </ion-button>
        </div>
      </form>

      <!-- Toast para mensajes -->
      <ion-toast
        :is-open="showToast"
        :message="toastMessage"
        :color="toastColor"
        :duration="3000"
        @did-dismiss="showToast = false"
        position="top"
      ></ion-toast>

      <!-- Loading -->
      <ion-loading
        :is-open="isLoading"
        message="Creando evento..."
      ></ion-loading>
    </ion-content>
  </ion-page>
</template>

<script setup>
import {
  IonPage, IonHeader, IonToolbar, IonTitle, IonContent, IonInput, 
  IonTextarea, IonButton, IonIcon, IonButtons, IonBackButton, 
  IonItem, IonLabel, IonToast, IonLoading, IonModal, IonDatetime,
  IonSearchbar
} from '@ionic/vue'
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

// Estado del formulario
const titulo = ref('')
const descripcion = ref('')
const ubicacion = ref('')
const fecha = ref('')
const coordenadas = ref(null)
const isLoading = ref(false)

// Estados del UI
const mostrarCalendario = ref(false)
const mostrarMapa = ref(false)
const busquedaUbicacion = ref('')

// Toast
const showToast = ref(false)
const toastMessage = ref('')
const toastColor = ref('success')

// Datos del grupo
const grupoId = ref(null)
const usuario = ref(null)

// Mapa
let mapa = null
let marcador = null

// Fecha mínima (hoy)
const fechaMinima = new Date().toISOString()

// Validación
const puedeGuardar = computed(() => {
  return titulo.value.trim().length > 0 && fecha.value && ubicacion.value.trim().length > 0
})

onMounted(async () => {
  try {
    grupoId.value = route.params.grupoId || route.params.id
    
    const usuarioString = localStorage.getItem('usuario')
    if (!usuarioString) {
      throw new Error('Usuario no autenticado')
    }
    
    usuario.value = JSON.parse(usuarioString)

     const preFecha = route.query.fecha
    if (preFecha) {
      fecha.value = preFecha
    }
    
    if (!grupoId.value) {
      throw new Error('Grupo no especificado')
    }
    
  } catch (error) {
    console.error('Error en inicialización:', error)
    mostrarToast('Error al cargar la página', 'danger')
    setTimeout(() => router.back(), 2000)
  }
})

const mostrarToast = (message, color = 'success') => {
  toastMessage.value = message
  toastColor.value = color
  showToast.value = true
}

const formatearFecha = (fechaISO) => {
  const fecha = new Date(fechaISO)
  return fecha.toLocaleString('es-ES', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// Funciones del calendario
const abrirCalendario = () => {
  mostrarCalendario.value = true
}

const cerrarCalendario = () => {
  mostrarCalendario.value = false
}

const onFechaChange = (event) => {
  fecha.value = event.detail.value
}

const confirmarFecha = () => {
  if (fecha.value) {
    cerrarCalendario()
    mostrarToast('Fecha seleccionada correctamente', 'success')
  }
}

// Funciones del mapa
const abrirMapa = async () => {
  mostrarMapa.value = true
  await nextTick()
  inicializarMapa()
}

const cerrarMapa = () => {
  mostrarMapa.value = false
}

const inicializarMapa = () => {
  // Usar OpenStreetMap como alternativa gratuita a Google Maps
  if (typeof L !== 'undefined') {
    // Si ya existe el mapa, eliminarlo
    if (mapa) {
      mapa.remove()
    }

    // Crear el mapa centrado en Madrid por defecto
    mapa = L.map('mapa').setView([40.4168, -3.7038], 13)

    // Añadir capa de tiles
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors'
    }).addTo(mapa)

    // Si ya tenemos coordenadas, centrar ahí
    if (coordenadas.value) {
      mapa.setView([coordenadas.value.lat, coordenadas.value.lng], 15)
      añadirMarcador(coordenadas.value.lat, coordenadas.value.lng)
    }

    // Añadir evento de click al mapa
    mapa.on('click', (e) => {
      const { lat, lng } = e.latlng
      coordenadas.value = { lat, lng }
      añadirMarcador(lat, lng)
      obtenerDireccion(lat, lng)
    })
  } else {
    // Cargar Leaflet si no está disponible
    cargarLeaflet()
  }
}

const cargarLeaflet = () => {
  // Cargar CSS de Leaflet
  const link = document.createElement('link')
  link.rel = 'stylesheet'
  link.href = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'
  document.head.appendChild(link)

  // Cargar JS de Leaflet
  const script = document.createElement('script')
  script.src = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'
  script.onload = () => {
    setTimeout(() => inicializarMapa(), 100)
  }
  document.head.appendChild(script)
}

const añadirMarcador = (lat, lng) => {
  if (marcador) {
    mapa.removeLayer(marcador)
  }
  
  if (typeof L !== 'undefined') {
    marcador = L.marker([lat, lng]).addTo(mapa)
    marcador.bindPopup('Ubicación del evento').openPopup()
  }
}

const buscarEnMapa = async (event) => {
  const query = event.target.value
  if (query.length < 3) return

  try {
    // Usar Nominatim (OpenStreetMap) para geocoding
    const response = await fetch(
      `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(query)}&limit=1`
    )
    const results = await response.json()
    
    if (results.length > 0) {
      const { lat, lon } = results[0]
      const coords = { lat: parseFloat(lat), lng: parseFloat(lon) }
      
      coordenadas.value = coords
      mapa.setView([coords.lat, coords.lng], 15)
      añadirMarcador(coords.lat, coords.lng)
    }
  } catch (error) {
    console.error('Error en búsqueda:', error)
  }
}

const obtenerDireccion = async (lat, lng) => {
  try {
    const response = await fetch(
      `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}`
    )
    const result = await response.json()
    
    if (result.display_name) {
      ubicacion.value = result.display_name
    }
  } catch (error) {
    console.error('Error obteniendo dirección:', error)
  }
}

const confirmarUbicacion = () => {
  if (coordenadas.value) {
    cerrarMapa()
    mostrarToast('Ubicación seleccionada correctamente', 'success')
  }
}

const buscarUbicacion = async (event) => {
  const query = event.target.value
  if (query.length < 3) return

  // Búsqueda simple de geocoding
  try {
    const response = await fetch(
      `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(query)}&limit=1`
    )
    const results = await response.json()
    
    if (results.length > 0) {
      const { lat, lon } = results[0]
      coordenadas.value = { lat: parseFloat(lat), lng: parseFloat(lon) }
    }
  } catch (error) {
    console.error('Error en geocoding:', error)
  }
}

const crearEvento = async () => {
  // Validación de campos obligatorios.
  if (!puedeGuardar.value) {
    mostrarToast('Por favor, completa todos los campos obligatorios', 'warning')
    return
  }

  isLoading.value = true

  try {
    // Se obtiene el token y se verifica.
    const token = localStorage.getItem('token')
    if (!token) {
      throw new Error('Token de autenticación no encontrado')
    }

    // Construimos el objeto eventData. 
    // Nota: El uso de new Date(fecha.value).toISOString() asegura que la fecha se envía en formato ISO 8601.
    const eventData = {
      titulo: titulo.value.trim(),
      descripcion: descripcion.value.trim(),
      ubicacion: ubicacion.value.trim(), // Verifica que en el formulario se llene correctamente con el formato esperado (ej: "lat, lng" o una dirección).
      fecha: new Date(fecha.value).toISOString()
    }

    // Realiza el POST para crear el evento.
    const response = await fetch(
      `${import.meta.env.VITE_API_URL}/eventos/${grupoId.value}/crear`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(eventData)
      }
    )

    if (!response.ok) {
      const errorData = await response.text()
      throw new Error(errorData || `Error ${response.status}: ${response.statusText}`)
    }

    const eventoCreado = await response.json()
    console.log('Evento creado:', eventoCreado)

    mostrarToast('Evento creado correctamente', 'success')

    // Limpiar formulario
    titulo.value = ''
    descripcion.value = ''
    ubicacion.value = ''
    fecha.value = ''
    coordenadas.value = null


    setTimeout(() => {
      router.back()
    }, 1500)

  } catch (error) {
    console.error('Error al crear evento:', error)
    mostrarToast(
      error.message || 'Error al crear el evento. Inténtalo de nuevo.',
      'danger'
    )
  } finally {
    isLoading.value = false
  }
}

</script>

<style scoped>
.event-form {
  padding: 1rem;
  max-width: 800px;
  margin: 0 auto;
}

.form-section {
  margin-bottom: 2rem;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin: 0 0 1rem 0;
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--ion-color-primary);
}

.textarea-item {
  margin-top: 1rem;
}

.placeholder-text {
  color: var(--ion-color-medium);
  font-style: italic;
}

.action-section {
  padding: 1rem 0 2rem 0;
}

ion-item {
  --padding-start: 0;
  --padding-end: 0;
  margin-bottom: 1rem;
}

ion-input, ion-textarea {
  --padding-top: 12px;
  --padding-bottom: 12px;
}

/* Estilos del calendario */
.calendar-modal {
  padding: 1rem;
}

.calendar-actions {
  padding: 1rem;
  margin-top: 1rem;
}

/* Estilos del mapa */
.map-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.mapa {
  flex: 1;
  min-height: 400px;
  z-index: 1;
}

.map-search {
  padding: 1rem;
  background: var(--ion-color-light);
}

.map-actions {
  padding: 1rem;
  background: var(--ion-color-light);
}

/* Responsive */
@media (min-width: 768px) {
  .event-form {
    padding: 2rem;
  }
  
  .mapa {
    min-height: 500px;
  }
}
</style>