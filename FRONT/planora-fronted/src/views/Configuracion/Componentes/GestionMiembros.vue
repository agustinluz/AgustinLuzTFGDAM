<template>
  <div class="gestion-participantes">
    <!-- Header con búsqueda y filtros -->
    <div class="header">
      <div class="header-title">
        <h3>Participantes ({{ participantesFiltrados.length }})</h3>
      </div>
      
      <div class="header-actions">
        <div class="search-box">
          <i class="icon-search"></i>
          <input 
            v-model="busqueda"
            placeholder="Buscar participantes..."
            class="search-input"
          >
        </div>
        
        <div class="filter-dropdown" v-if="esAdmin">
          <button @click="mostrarFiltros = !mostrarFiltros" class="filter-btn">
            <i class="icon-filter"></i>
            Filtros
          </button>
          <div v-if="mostrarFiltros" class="filter-menu">
            <label class="filter-option">
              <input type="checkbox" v-model="filtros.admins"> Solo admins
            </label>
            <label class="filter-option">
              <input type="checkbox" v-model="filtros.activos"> Solo activos
            </label>
            <label class="filter-option">
              <input type="checkbox" v-model="filtros.recientes"> Nuevos (7 días)
            </label>
          </div>
        </div>
      </div>
    </div>

    <!-- Lista de participantes -->
    <div class="participantes-lista">
      <div 
        v-for="participante in participantesFiltrados" 
        :key="participante.id"
        class="participante-item"
        :class="{ offline: !participante.activo }"
      >
        <!-- Avatar y estado -->
        <div class="participante-avatar">
          <img 
            v-if="participante.avatar" 
            :src="participante.avatar" 
            :alt="participante.nombre"
            class="avatar-img"
          >
          <div v-else class="avatar-placeholder">
            {{ participante.nombre.charAt(0).toUpperCase() }}
          </div>
          <div 
            class="status-indicator"
            :class="participante.activo ? 'online' : 'offline'"
            :title="participante.activo ? 'En línea' : 'Sin conexión'"
          ></div>
        </div>

        <!-- Información del participante -->
        <div class="participante-info">
          <div class="nombre-container">
            <span class="participante-nombre">{{ participante.nombre }}</span>
            <div class="badges">
              <span v-if="participante.esAdmin" class="badge admin">
                <i class="icon-crown"></i> Admin
              </span>
              <span v-if="participante.esModerador" class="badge moderador">
                <i class="icon-shield"></i> Mod
              </span>
              <span v-if="esNuevo(participante.fechaIngreso)" class="badge nuevo">
                Nuevo
              </span>
            </div>
          </div>
          
          <div class="participante-meta">
            <span class="meta-item">
              <i class="icon-calendar"></i>
              Se unió {{ formatearFecha(participante.fechaIngreso) }}
            </span>
            <span class="meta-item" v-if="participante.ultimaActividad">
              <i class="icon-clock"></i>
              Última actividad {{ formatearTiempo(participante.ultimaActividad) }}
            </span>
          </div>

          <!-- Estadísticas del participante -->
          <div class="participante-stats" v-if="mostrarEstadisticas">
            <div class="stat">
              <span class="stat-value">{{ participante.mensajes || 0 }}</span>
              <span class="stat-label">mensajes</span>
            </div>
            <div class="stat">
              <span class="stat-value">{{ participante.reacciones || 0 }}</span>
              <span class="stat-label">reacciones</span>
            </div>
            <div class="stat" v-if="participante.archivos">
              <span class="stat-value">{{ participante.archivos }}</span>
              <span class="stat-label">archivos</span>
            </div>
          </div>
        </div>

        <!-- Acciones -->
        <div class="participante-acciones">
          <button 
            @click="iniciarChat(participante)"
            class="accion-btn chat"
            title="Enviar mensaje"
          >
            <i class="icon-message-circle"></i>
          </button>
          
          <button 
            v-if="esAdmin && !participante.esAdmin"
            @click="mostrarOpciones(participante)"
            class="accion-btn opciones"
            title="Más opciones"
          >
            <i class="icon-more-vertical"></i>
          </button>
        </div>
      </div>

      <!-- Estado vacío -->
      <div v-if="participantesFiltrados.length === 0" class="estado-vacio">
        <i class="icon-users"></i>
        <p v-if="busqueda">No se encontraron participantes con "{{ busqueda }}"</p>
        <p v-else>No hay participantes en este grupo</p>
      </div>
    </div>

    <!-- Controles de vista -->
    <div class="controles-vista">
      <button 
        @click="mostrarEstadisticas = !mostrarEstadisticas"
        class="control-btn"
        :class="{ active: mostrarEstadisticas }"
      >
        <i class="icon-bar-chart"></i>
        Estadísticas
      </button>
      
      <button 
        @click="ordenarPor = ordenarPor === 'nombre' ? 'actividad' : 'nombre'"
        class="control-btn"
      >
        <i class="icon-sort"></i>
        {{ ordenarPor === 'nombre' ? 'Por nombre' : 'Por actividad' }}
      </button>
      
      <button 
        @click="vistaCompacta = !vistaCompacta"
        class="control-btn"
        :class="{ active: vistaCompacta }"
      >
        <i class="icon-list"></i>
        Vista compacta
      </button>
    </div>

    <!-- Acciones de administrador -->
    <div v-if="esAdmin" class="admin-actions">
      <button @click="$emit('agregarParticipante')" class="admin-btn primary">
        <i class="icon-user-plus"></i>
        Agregar participante
      </button>
      
      <button @click="$emit('invitarUsuario')" class="admin-btn secondary">
        <i class="icon-mail"></i>
        Invitar usuario
      </button>
      
      <button @click="exportarLista" class="admin-btn secondary">
        <i class="icon-download"></i>
        Exportar lista
      </button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'GestionParticipantes',
  props: {
    participantes: {
      type: Array,
      default: () => []
    },
    esAdmin: {
      type: Boolean,
      default: false
    },
    usuarioActual: {
      type: Object,
      required: true
    }
  },
  emits: [
    'mostrarOpciones', 'iniciarChat', 'agregarParticipante', 
    'invitarUsuario', 'exportarParticipantes'
  ],
  data() {
    return {
      busqueda: '',
      mostrarFiltros: false,
      mostrarEstadisticas: false,
      vistaCompacta: false,
      ordenarPor: 'nombre', // 'nombre' | 'actividad' | 'fecha'
      filtros: {
        admins: false,
        activos: false,
        recientes: false
      }
    }
  },
  computed: {
    participantesFiltrados() {
      let resultado = [...this.participantes]

      // Aplicar búsqueda
      if (this.busqueda) {
        const termino = this.busqueda.toLowerCase()
        resultado = resultado.filter(p => 
          p.nombre.toLowerCase().includes(termino) ||
          p.email?.toLowerCase().includes(termino)
        )
      }

      // Aplicar filtros
      if (this.filtros.admins) {
        resultado = resultado.filter(p => p.esAdmin || p.esModerador)
      }
      
      if (this.filtros.activos) {
        resultado = resultado.filter(p => p.activo)
      }
      
      if (this.filtros.recientes) {
        const hace7Dias = new Date()
        hace7Dias.setDate(hace7Dias.getDate() - 7)
        resultado = resultado.filter(p => 
          new Date(p.fechaIngreso) > hace7Dias
        )
      }

      // Aplicar ordenamiento
      resultado.sort((a, b) => {
        switch (this.ordenarPor) {
          case 'nombre':
            return a.nombre.localeCompare(b.nombre)
          case 'actividad':
            return (b.ultimaActividad || 0) - (a.ultimaActividad || 0)
          case 'fecha':
            return new Date(b.fechaIngreso) - new Date(a.fechaIngreso)
          default:
            return 0
        }
      })

      return resultado
    }
  },
  methods: {
    mostrarOpciones(participante) {
      this.$emit('mostrarOpciones', participante)
    },

    iniciarChat(participante) {
      this.$emit('iniciarChat', participante)
    },

    esNuevo(fechaIngreso) {
      const hace7Dias = new Date()
      hace7Dias.setDate(hace7Dias.getDate() - 7)
      return new Date(fechaIngreso) > hace7Dias
    },

    formatearFecha(fecha) {
      const date = new Date(fecha)
      const ahora = new Date()
      const diferencia = ahora - date
      const dias = Math.floor(diferencia / (1000 * 60 * 60 * 24))

      if (dias === 0) return 'hoy'
      if (dias === 1) return 'ayer'
      if (dias < 7) return `hace ${dias} días`
      if (dias < 30) return `hace ${Math.floor(dias / 7)} semanas`
      if (dias < 365) return `hace ${Math.floor(dias / 30)} meses`
      return `hace ${Math.floor(dias / 365)} años`
    },

    formatearTiempo(timestamp) {
      const date = new Date(timestamp)
      const ahora = new Date()
      const diferencia = ahora - date
      const minutos = Math.floor(diferencia / (1000 * 60))
      const horas = Math.floor(minutos / 60)
      const dias = Math.floor(horas / 24)

      if (minutos < 1) return 'ahora'
      if (minutos < 60) return `hace ${minutos}m`
      if (horas < 24) return `hace ${horas}h`
      if (dias < 7) return `hace ${dias}d`
      return this.formatearFecha(timestamp)
    },

    exportarLista() {
      const datos = this.participantesFiltrados.map(p => ({
        nombre: p.nombre,
        email: p.email,
        rol: p.esAdmin ? 'Admin' : p.esModerador ? 'Moderador' : 'Participante',
        fechaIngreso: p.fechaIngreso,
        ultimaActividad: p.ultimaActividad,
        mensajes: p.mensajes || 0
      }))
      
      this.$emit('exportarParticipantes', datos)
    }
  },
  watch: {
    filtros: {
      deep: true,
      handler() {
        // Cerrar menú de filtros cuando se cambia un filtro
        this.$nextTick(() => {
          this.mostrarFiltros = false
        })
      }
    }
  }
}
</script>

<style scoped>
.gestion-participantes {
  background: white;
  border-radius: 12px;
  overflow: hidden;
}

.header {
  padding: 20px;
  border-bottom: 1px solid var(--color-border);
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.header-title h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text);
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.search-box {
  position: relative;
  display: flex;
  align-items: center;
}

.search-box i {
  position: absolute;
  left: 12px;
  color: var(--color-text-secondary);
  font-size: 14px;
}

.search-input {
  padding: 8px 12px 8px 36px;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  font-size: 14px;
  width: 200px;
}

.filter-dropdown {
  position: relative;
}

.filter-btn {
  padding: 8px 12px;
  border: 1px solid var(--color-border);
  background: white;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
}

.filter-menu {
  position: absolute;
  top: 100%;
  right: 0;
  background: white;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  z-index: 10;
  min-width: 150px;
}

.filter-option {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px;
  font-size: 14px;
  cursor: pointer;
}

.participantes-lista {
  max-height: 500px;
  overflow-y: auto;
}

.participante-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  border-bottom: 1px solid var(--color-border);
  transition: background-color 0.2s;
}

.participante-item:hover {
  background: var(--color-background);
}

.participante-item.offline {
  opacity: 0.7;
}

.participante-avatar {
  position: relative;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  overflow: hidden;
}

.avatar-img {
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
  font-size: 18px;
  font-weight: bold;
}

.status-indicator {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid white;
}

.status-indicator.online {
  background: var(--color-success);
}

.status-indicator.offline {
  background: var(--color-text-secondary);
}

.participante-info {
  flex: 1;
  min-width: 0;
}

.nombre-container {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.participante-nombre {
  font-weight: 500;
  color: var(--color-text);
  font-size: 16px;
}

.badges {
  display: flex;
  gap: 4px;
}

.badge {
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 10px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 2px;
}

.badge.admin {
  background: var(--color-primary);
  color: white;
}

.badge.moderador {
  background: var(--color-warning);
  color: white;
}

.badge.nuevo {
  background: var(--color-success);
  color: white;
}

.participante-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
  margin-bottom: 8px;
}

.meta-item {
  font-size: 12px;
  color: var(--color-text-secondary);
  display: flex;
  align-items: center;
  gap: 4px;
}

.participante-stats {
  display: flex;
  gap: 16px;
}

.stat {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-value {
  font-weight: bold;
  color: var(--color-text);
  font-size: 14px;
}

.stat-label {
  font-size: 10px;
  color: var(--color-text-secondary);
}

.participante-acciones {
  display: flex;
  gap: 8px;
}

.accion-btn {
  width: 36px;
  height: 36px;
  border: 1px solid var(--color-border);
  background: white;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.2s;
}

.accion-btn:hover {
  background: var(--color-background);
}

.accion-btn.chat {
  color: var(--color-primary);
}

.estado-vacio {
  padding: 40px 20px;
  text-align: center;
  color: var(--color-text-secondary);
}

.estado-vacio i {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.controles-vista {
  padding: 16px 20px;
  border-top: 1px solid var(--color-border);
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.control-btn {
  padding: 6px 12px;
  border: 1px solid var(--color-border);
  background: white;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: all 0.2s;
}

.control-btn:hover,
.control-btn.active {
  background: var(--color-primary);
  color: white;
  border-color: var(--color-primary);
}

.admin-actions {
  padding: 16px 20px;
  border-top: 1px solid var(--color-border);
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.admin-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s;
}

.admin-btn.primary {
  background: var(--color-primary);
  color: white;
}

.admin-btn.secondary {
  background: var(--color-background);
  color: var(--color-text);
  border: 1px solid var(--color-border);
}

.admin-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

@media (max-width: 768px) {
  .header {
    flex-direction: column;
    align-items: stretch;
  }
  
  .header-actions {
    justify-content: space-between;
  }
  
  .search-input {
    width: 150px;
  }
  
  .participante-item {
    padding: 12px 16px;
  }
  
  .participante-stats {
    gap: 12px;
  }
  
  .admin-actions {
    flex-direction: column;
  }
}
</style>