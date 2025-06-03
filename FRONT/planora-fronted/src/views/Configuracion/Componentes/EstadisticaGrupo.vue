<template>
  <div class="estadisticas-grupo">
    <h3 class="stats-title">Estadísticas del Grupo</h3>
    
    <div class="stats-grid">
      <!-- Participantes -->
      <div class="stat-card">
        <div class="stat-icon">
          <i class="icon-users"></i>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ estadisticas.totalParticipantes }}</div>
          <div class="stat-label">Participantes</div>
          <div class="stat-detail">
            {{ estadisticas.participantesActivos }} activos
          </div>
        </div>
      </div>

      <!-- Actividad -->
      <div class="stat-card">
        <div class="stat-icon">
          <i class="icon-activity"></i>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ estadisticas.mensajesHoy }}</div>
          <div class="stat-label">Mensajes hoy</div>
          <div class="stat-detail">
            {{ estadisticas.mensajesTotales }} en total
          </div>
        </div>
      </div>

      <!-- Eventos -->
      <div class="stat-card" v-if="estadisticas.eventos">
        <div class="stat-icon">
          <i class="icon-calendar"></i>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ estadisticas.eventosProximos }}</div>
          <div class="stat-label">Eventos próximos</div>
          <div class="stat-detail">
            {{ estadisticas.eventosRealizados }} completados
          </div>
        </div>
      </div>

      <!-- Archivos compartidos -->
      <div class="stat-card">
        <div class="stat-icon">
          <i class="icon-paperclip"></i>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ estadisticas.archivosCompartidos }}</div>
          <div class="stat-label">Archivos</div>
          <div class="stat-detail">
            {{ formatearTamaño(estadisticas.espacioUsado) }}
          </div>
        </div>
      </div>
    </div>

    <!-- Gráfico de actividad semanal -->
    <div class="actividad-semanal" v-if="actividadSemanal.length > 0">
      <h4 class="chart-title">Actividad de la semana</h4>
      <div class="chart-container">
        <div class="chart">
          <div 
            v-for="(dia, index) in actividadSemanal" 
            :key="index"
            class="chart-bar"
            :style="{ height: `${(dia.mensajes / maxMensajes) * 100}%` }"
            :title="`${dia.dia}: ${dia.mensajes} mensajes`"
          >
            <div class="bar-fill"></div>
          </div>
        </div>
        <div class="chart-labels">
          <span v-for="dia in actividadSemanal" :key="dia.dia" class="chart-label">
            {{ dia.dia.substring(0, 3) }}
          </span>
        </div>
      </div>
    </div>

    <!-- Participantes más activos -->
    <div class="top-participantes" v-if="participantesActivos.length > 0">
      <h4 class="section-title">Más activos esta semana</h4>
      <div class="participantes-list">
        <div 
          v-for="(participante, index) in participantesActivos.slice(0, 5)" 
          :key="participante.id"
          class="participante-item"
        >
          <div class="participante-rank">{{ index + 1 }}</div>
          <div class="participante-avatar">
            <img 
              v-if="participante.avatar" 
              :src="participante.avatar" 
              :alt="participante.nombre"
            >
            <div v-else class="avatar-placeholder">
              {{ participante.nombre.charAt(0).toUpperCase() }}
            </div>
          </div>
          <div class="participante-info">
            <div class="participante-nombre">{{ participante.nombre }}</div>
            <div class="participante-stats">{{ participante.mensajes }} mensajes</div>
          </div>
          <div class="participante-badge" v-if="participante.esAdmin">
            <i class="icon-crown"></i>
          </div>
        </div>
      </div>
    </div>

    <!-- Horarios de mayor actividad -->
    <div class="horarios-actividad" v-if="horariosActividad.length > 0">
      <h4 class="section-title">Horarios más activos</h4>
      <div class="horarios-grid">
        <div 
          v-for="horario in horariosActividad" 
          :key="horario.hora"
          class="horario-item"
        >
          <div class="horario-tiempo">{{ horario.hora }}:00</div>
          <div class="horario-bar">
            <div 
              class="horario-fill"
              :style="{ width: `${(horario.actividad / maxActividad) * 100}%` }"
            ></div>
          </div>
          <div class="horario-valor">{{ horario.actividad }}%</div>
        </div>
      </div>
    </div>

    <!-- Resumen temporal -->
    <div class="resumen-temporal">
      <div class="periodo-selector">
        <button 
          v-for="periodo in periodos" 
          :key="periodo.value"
          @click="cambiarPeriodo(periodo.value)"
          :class="['periodo-btn', { active: periodoActual === periodo.value }]"
        >
          {{ periodo.label }}
        </button>
      </div>
      
      <div class="resumen-stats">
        <div class="resumen-item">
          <span class="resumen-label">Promedio mensajes/día:</span>
          <span class="resumen-valor">{{ promedioMensajes }}</span>
        </div>
        <div class="resumen-item">
          <span class="resumen-label">Día más activo:</span>
          <span class="resumen-valor">{{ diaMasActivo }}</span>
        </div>
        <div class="resumen-item">
          <span class="resumen-label">Crecimiento:</span>
          <span class="resumen-valor" :class="crecimiento >= 0 ? 'positivo' : 'negativo'">
            {{ crecimiento >= 0 ? '+' : '' }}{{ crecimiento }}%
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'EstadisticasGrupo',
  props: {
    grupoId: {
      type: String,
      required: true
    },
    estadisticas: {
      type: Object,
      default: () => ({
        totalParticipantes: 0,
        participantesActivos: 0,
        mensajesHoy: 0,
        mensajesTotales: 0,
        eventosProximos: 0,
        eventosRealizados: 0,
        archivosCompartidos: 0,
        espacioUsado: 0
      })
    }
  },
  data() {
    return {
      periodoActual: 'semana',
      periodos: [
        { value: 'semana', label: 'Semana' },
        { value: 'mes', label: 'Mes' },
        { value: 'trimestre', label: '3 Meses' }
      ],
      actividadSemanal: [
        { dia: 'Lunes', mensajes: 45 },
        { dia: 'Martes', mensajes: 32 },
        { dia: 'Miércoles', mensajes: 67 },
        { dia: 'Jueves', mensajes: 23 },
        { dia: 'Viernes', mensajes: 89 },
        { dia: 'Sábado', mensajes: 54 },
        { dia: 'Domingo', mensajes: 12 }
      ],
      participantesActivos: [
        { id: 1, nombre: 'Ana García', mensajes: 89, avatar: null, esAdmin: true },
        { id: 2, nombre: 'Carlos López', mensajes: 67, avatar: null, esAdmin: false },
        { id: 3, nombre: 'María Silva', mensajes: 54, avatar: null, esAdmin: false },
        { id: 4, nombre: 'Pedro Ruiz', mensajes: 45, avatar: null, esAdmin: false },
        { id: 5, nombre: 'Laura Torres', mensajes: 32, avatar: null, esAdmin: false }
      ],
      horariosActividad: [
        { hora: 9, actividad: 15 },
        { hora: 12, actividad: 35 },
        { hora: 15, actividad: 25 },
        { hora: 18, actividad: 45 },
        { hora: 21, actividad: 65 },
        { hora: 22, actividad: 30 }
      ]
    }
  },
  computed: {
    maxMensajes() {
      return Math.max(...this.actividadSemanal.map(d => d.mensajes))
    },
    maxActividad() {
      return Math.max(...this.horariosActividad.map(h => h.actividad))
    },
    promedioMensajes() {
      const total = this.actividadSemanal.reduce((sum, dia) => sum + dia.mensajes, 0)
      return Math.round(total / this.actividadSemanal.length)
    },
    diaMasActivo() {
      const max = this.actividadSemanal.reduce((prev, curr) => 
        prev.mensajes > curr.mensajes ? prev : curr
      )
      return max.dia
    },
    crecimiento() {
      // Simulación de crecimiento basado en período anterior
      return this.periodoActual === 'semana' ? 12 : 
             this.periodoActual === 'mes' ? -3 : 8
    }
  },
  methods: {
    formatearTamaño(bytes) {
      if (bytes === 0) return '0 B'
      const k = 1024
      const sizes = ['B', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
    },
    
    cambiarPeriodo(periodo) {
      this.periodoActual = periodo
      // Aquí cargarías las estadísticas del nuevo período
      this.$emit('cambiarPeriodo', periodo)
    }
  }
}
</script>

<style scoped>
.estadisticas-grupo {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
}

.stats-title {
  margin: 0 0 20px 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: var(--color-background);
  border-radius: 8px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.stat-icon {
  width: 40px;
  height: 40px;
  background: var(--color-primary);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: var(--color-text);
  line-height: 1;
}

.stat-label {
  color: var(--color-text-secondary);
  font-size: 12px;
  margin-top: 2px;
}

.stat-detail {
  color: var(--color-text-secondary);
  font-size: 10px;
  margin-top: 4px;
}

.actividad-semanal {
  margin-bottom: 24px;
}

.chart-title, .section-title {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text);
}

.chart-container {
  background: var(--color-background);
  border-radius: 8px;
  padding: 16px;
}

.chart {
  display: flex;
  align-items: end;
  height: 100px;
  gap: 4px;
  margin-bottom: 8px;
}

.chart-bar {
  flex: 1;
  min-height: 4px;
  background: var(--color-border);
  border-radius: 2px;
  position: relative;
  cursor: pointer;
}

.bar-fill {
  background: var(--color-primary);
  height: 100%;
  border-radius: 2px;
}

.chart-labels {
  display: flex;
  justify-content: space-between;
}

.chart-label {
  font-size: 10px;
  color: var(--color-text-secondary);
  text-align: center;
}

.participantes-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.participante-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px;
  background: var(--color-background);
  border-radius: 8px;
}

.participante-rank {
  width: 24px;
  height: 24px;
  background: var(--color-primary);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
}

.participante-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  overflow: hidden;
}

.participante-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  background: var(--color-primary);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: bold;
}

.participante-info {
  flex: 1;
}

.participante-nombre {
  font-weight: 500;
  color: var(--color-text);
  font-size: 14px;
}

.participante-stats {
  color: var(--color-text-secondary);
  font-size: 12px;
}

.participante-badge {
  color: var(--color-primary);
  font-size: 16px;
}

.horarios-actividad {
  margin-bottom: 24px;
}

.horarios-grid {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.horario-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px;
  background: var(--color-background);
  border-radius: 6px;
}

.horario-tiempo {
  width: 50px;
  font-size: 12px;
  color: var(--color-text-secondary);
}

.horario-bar {
  flex: 1;
  height: 6px;
  background: var(--color-border);
  border-radius: 3px;
  overflow: hidden;
}

.horario-fill {
  height: 100%;
  background: var(--color-primary);
  border-radius: 3px;
}

.horario-valor {
  width: 40px;
  text-align: right;
  font-size: 12px;
  color: var(--color-text-secondary);
}

.resumen-temporal {
  border-top: 1px solid var(--color-border);
  padding-top: 20px;
}

.periodo-selector {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.periodo-btn {
  padding: 6px 12px;
  border: 1px solid var(--color-border);
  background: white;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.periodo-btn.active {
  background: var(--color-primary);
  color: white;
  border-color: var(--color-primary);
}

.resumen-stats {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.resumen-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.resumen-label {
  color: var(--color-text-secondary);
  font-size: 14px;
}

.resumen-valor {
  font-weight: 600;
  color: var(--color-text);
}

.resumen-valor.positivo {
  color: var(--color-success);
}

.resumen-valor.negativo {
  color: var(--color-error);
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .chart {
    height: 80px;
  }
  
  .resumen-stats {
    gap: 12px;
  }
}
</style>