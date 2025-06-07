<template>
    <ion-card class="evento-card">
        <ion-card-header class="evento-header">
            <div class="evento-header-left">
                <ion-icon :name="esFechaFutura(evento.fecha) ? 'calendar-outline' : 'calendar'"
                    :class="['evento-icon', { 'evento-pasado': !esFechaFutura(evento.fecha) }]" />
                <div>
                    <ion-card-title :class="{ 'titulo-pasado': !esFechaFutura(evento.fecha) }">
                        {{ evento.titulo }}
                    </ion-card-title>
                    <ion-card-subtitle class="grupo-nombre">
                        <ion-icon name="people-outline" class="icon-sub" />
                        {{ evento.grupoNombre }}
                    </ion-card-subtitle>
                </div>
            </div>
            <div class="evento-actions">
                <div class="evento-fecha" :class="{ 'fecha-pasada': !esFechaFutura(evento.fecha) }">
                    <ion-icon name="time-outline" class="icon-sub" />
                    {{ formatFecha(evento.fecha) }}
                </div>
                <div class="action-buttons">
                    <ion-button fill="clear" size="small" @click="$emit('editar', evento)" color="success">
                        <ion-icon slot="icon-only" name="pencil-outline"></ion-icon>
                    </ion-button>
                    <ion-button fill="clear" size="small" @click="$emit('eliminar', evento)" color="danger">
                        <ion-icon slot="icon-only" name="trash-bin-outline"></ion-icon>
                    </ion-button>

                </div>
            </div>
        </ion-card-header>

        <ion-card-content class="evento-content">
            <p class="descripcion">{{ evento.descripcion }}</p>
            <div v-if="evento.ubicacion" class="ubicacion">
                <ion-icon name="location-outline" class="icon-sub" />
                <span>{{ evento.ubicacion }}</span>
            </div>
            <div class="tiempo-restante" v-if="esFechaFutura(evento.fecha)">
                <ion-chip color="success" outline>
                    <ion-icon name="alarm-outline"></ion-icon>
                    <ion-label>{{ tiempoRestante(evento.fecha) }}</ion-label>
                </ion-chip>
            </div>
            <div class="tiempo-transcurrido" v-else>
                <ion-chip color="medium" outline>
                    <ion-icon name="checkmark-circle-outline"></ion-icon>
                    <ion-label>Finalizado</ion-label>
                </ion-chip>
            </div>
        </ion-card-content>
    </ion-card>
</template>

<script setup lang="ts">
import {
    IonCard,
    IonCardHeader,
    IonCardTitle,
    IonCardSubtitle,
    IonCardContent,
    IonIcon,
    IonButton,
    IonChip,
    IonLabel
} from '@ionic/vue'
import type { Evento } from '@/service/EventoService'

interface Props {
    evento: Evento
}

interface Emits {
    (e: 'editar', evento: Evento): void
    (e: 'eliminar', evento: Evento): void
}

defineProps<Props>()
defineEmits<Emits>()

const formatFecha = (fecha: string) => {
    const opciones: Intl.DateTimeFormatOptions = {
        weekday: 'short',
        day: 'numeric',
        month: 'short',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    }
    return new Date(fecha).toLocaleDateString('es-ES', opciones)
}

const esFechaFutura = (fecha: string) => {
    return new Date(fecha) > new Date()
}

const tiempoRestante = (fecha: string) => {
    const ahora = new Date()
    const fechaEvento = new Date(fecha)
    const diferencia = fechaEvento.getTime() - ahora.getTime()

    const dias = Math.floor(diferencia / (1000 * 60 * 60 * 24))
    const horas = Math.floor((diferencia % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))

    if (dias > 0) {
        return `${dias} día${dias > 1 ? 's' : ''}`
    } else if (horas > 0) {
        return `${horas} hora${horas > 1 ? 's' : ''}`
    } else {
        return 'Muy pronto'
    }
}
</script>

<style scoped>
.evento-card {
    margin-bottom: 1.2rem;
    border-radius: 16px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.07);
    transition: transform 0.2s ease;
    border-left: 4px solid var(--ion-color-primary);
}

.evento-card:hover {
    transform: scale(1.01);
}

.evento-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    padding-bottom: 0;
}

.evento-header-left {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    flex: 1;
}

.evento-icon {
    font-size: 2rem;
    color: var(--ion-color-primary);
    transition: color 0.3s ease;
}

.evento-icon.evento-pasado {
    color: var(--ion-color-medium);
}

.titulo-pasado {
    color: var(--ion-color-medium);
    text-decoration: line-through;
}

.icon-sub {
    margin-right: 0.3rem;
    color: var(--ion-color-medium);
    font-size: 1.1rem;
}

.evento-actions {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 0.5rem;
}

.evento-fecha {
    font-size: 0.9rem;
    color: var(--ion-color-dark);
    display: flex;
    align-items: center;
    gap: 0.3rem;
    white-space: nowrap;
}

.fecha-pasada {
    color: var(--ion-color-medium);
}

.action-buttons {
    display: flex !important;
}

.evento-content .descripcion {
    margin-bottom: 0.5rem;
    font-size: 1rem;
    line-height: 1.4;
}

.ubicacion {
    color: var(--ion-color-primary);
    font-weight: 500;
    display: flex;
    align-items: center;
    gap: 0.4rem;
    margin-bottom: 0.5rem;
}

.tiempo-restante,
.tiempo-transcurrido {
    margin-top: 0.5rem;
}

@media (max-width: 768px) {
    .evento-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 0.5rem;
    }

    .evento-actions {
        width: 100%;
        flex-direction: row;
        justify-content: space-between;
        align-items: center;
    }

    .action-buttons {
        display: flex !important;
    }
}
</style>