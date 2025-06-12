<template>
  <div class="form-section">
    <h3 class="section-title">
      <ion-icon name="location-outline" />
      Ubicación
    </h3>
    <ion-item>
      <ion-input
        label="Dirección"
        label-placement="stacked"
        fill="outline"
        v-model="modelUbicacion"
        placeholder="Ej: Calle Principal 123, Madrid"
        @ion-input="onSearch"
      />
    </ion-item>
    <ion-item button @click="openMap" v-if="modelUbicacion">
      <ion-label>
        <h3>Ver en el mapa</h3>
        <p>{{ coords ? `${coords.lat.toFixed(6)}, ${coords.lng.toFixed(6)}` : 'Buscar ubicación en el mapa' }}</p>
      </ion-label>
      <ion-icon name="map-outline" slot="end" />
    </ion-item>

    <ion-modal :is-open="showMap" @did-dismiss="closeMap">
      <ion-header>
        <ion-toolbar>
          <ion-title>Seleccionar ubicación</ion-title>
          <ion-buttons slot="end">
            <ion-button @click="closeMap">Cerrar</ion-button>
          </ion-buttons>
        </ion-toolbar>
      </ion-header>
      <ion-content>
        <div class="map-container">
          <div id="map" class="mapa"></div>
          <div class="map-search">
            <ion-searchbar
              v-model="searchQuery"
              placeholder="Buscar dirección..."
              @ion-input="searchOnMap"
              show-clear-button="focus"
            />
          </div>
          <div class="map-actions">
            <ion-button expand="block" @click="confirmLocation" :disabled="!coords">
              <ion-icon name="checkmark-outline" slot="start" />
              Confirmar ubicación
            </ion-button>
          </div>
        </div>
      </ion-content>
    </ion-modal>
  </div>
</template>

<script setup lang="ts">
// @ts-ignore
declare const L: any;
import {
  IonItem,
  IonInput,
  IonLabel,
  IonIcon,
  IonModal,
  IonHeader,
  IonToolbar,
  IonTitle,
  IonButtons,
  IonButton,
  IonContent,
  IonSearchbar
} from '@ionic/vue'
import { ref, computed, nextTick } from 'vue'

const props = defineProps<{ ubicacion: string; coordenadas?: { lat: number; lng: number } | null }>()
const emit = defineEmits<{
  (e: 'update:ubicacion', val: string): void
  (e: 'update:coordenadas', val: { lat: number; lng: number } | null): void
}>()

const showMap = ref(false)
const searchQuery = ref('')
const mapRef = ref<any>(null)
const markerRef = ref<any>(null)

const modelUbicacion = computed({
  get: () => props.ubicacion,
  set: v => emit('update:ubicacion', v)
})
const coords = computed({
  get: () => props.coordenadas || null,
  set: v => emit('update:coordenadas', v)
})

const openMap = async () => {
  showMap.value = true
  await nextTick()
  initMap()
}
const closeMap = () => { showMap.value = false }
const confirmLocation = () => { if (coords.value) closeMap() }

const onSearch = (event: any) => {
  const query = event.target.value
  if (query.length < 3) return
  geocode(query)
}

const searchOnMap = async (event: any) => {
  const query = event.target.value
  if (query.length < 3) return
  const results = await geocode(query)
  if (results && results[0]) {
    const { lat, lon } = results[0]
    const c = { lat: parseFloat(lat), lng: parseFloat(lon) }
    coords.value = c
    if (mapRef.value) {
      mapRef.value.setView([c.lat, c.lng], 15)
      addMarker(c.lat, c.lng)
    }
  }
}

async function geocode(query: string) {
  try {
    const res = await fetch(`https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(query)}&limit=1`)
    return await res.json()
  } catch (e) {
    console.error('Error en geocoding:', e)
    return null
  }
}

function initMap() {
  if (typeof L === 'undefined') {
    const link = document.createElement('link')
    link.rel = 'stylesheet'
    link.href = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'
    document.head.appendChild(link)
    const script = document.createElement('script')
    script.src = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'
    script.onload = () => setTimeout(initMap, 100)
    document.head.appendChild(script)
    return
  }

  if (mapRef.value) {
    mapRef.value.remove()
  }
  mapRef.value = L.map('map').setView([40.4168, -3.7038], 13)
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© OpenStreetMap contributors'
  }).addTo(mapRef.value)
  if (coords.value) {
    mapRef.value.setView([coords.value.lat, coords.value.lng], 15)
    addMarker(coords.value.lat, coords.value.lng)
  }
  mapRef.value.on('click', (e: any) => {
    const { lat, lng } = e.latlng
    coords.value = { lat, lng }
    addMarker(lat, lng)
    reverseGeocode(lat, lng)
  })
}

function addMarker(lat: number, lng: number) {
  if (markerRef.value) {
    mapRef.value.removeLayer(markerRef.value)
  }
  markerRef.value = L.marker([lat, lng]).addTo(mapRef.value)
}

async function reverseGeocode(lat: number, lng: number) {
  try {
    const res = await fetch(`https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}`)
    const result = await res.json()
    if (result.display_name) {
      modelUbicacion.value = result.display_name
    }
  } catch (e) {
    console.error('Error obteniendo dirección:', e)
  }
}
</script>

<style scoped>
.map-container { height: 100%; display: flex; flex-direction: column; }
.mapa { flex: 1; min-height: 400px; z-index: 1; }
.map-search { padding: 1rem; background: var(--ion-color-light); }
.map-actions { padding: 1rem; background: var(--ion-color-light); }
</style>