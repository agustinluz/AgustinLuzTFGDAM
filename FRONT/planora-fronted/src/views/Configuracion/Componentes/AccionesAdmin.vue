<template>
  <div class="acciones-admin">
    <h3 class="section-title">Administración del Grupo</h3>
    
    <!-- Acciones principales -->
    <div class="acciones-principales">
      <div class="accion-card" @click="$emit('agregarParticipante')">
        <div class="accion-icon">
          <i class="icon-user-plus"></i>
        </div>
        <div class="accion-content">
          <h4>Agregar Participante</h4>
          <p>Buscar y agregar nuevos miembros al grupo</p>
        </div>
        <i class="icon-chevron-right"></i>
      </div>

      <div class="accion-card" @click="$emit('invitarUsuario')">
        <div class="accion-icon">
          <i class="icon-mail"></i>
        </div>
        <div class="accion-content">
          <h4>Invitar por Email</h4>
          <p>Enviar invitaciones a usuarios externos</p>
        </div>
        <i class="icon-chevron-right"></i>
      </div>

      <div class="accion-card" @click="$emit('transferirAdmin')">
        <div class="accion-icon">
          <i class="icon-crown"></i>
        </div>
        <div class="accion-content">
          <h4>Transferir Administración</h4>
          <p>Cambiar el administrador principal del grupo</p>
        </div>
        <i class="icon-chevron-right"></i>
      </div>
    </div>

    <!-- Configuración del grupo -->
    <div class="configuracion-grupo">
      <h4 class="subsection-title">Configuración</h4>
      
      <div class="config-options">
        <div class="config-item">
          <div class="config-info">
            <span class="config-label">Grupo público</span>
            <span class="config-desc">Permitir que otros usuarios encuentren este grupo</span>
          </div>
          <label class="toggle">
            <input 
              type="checkbox" 
              v-model="configuracion.publico"
              @change="actualizarConfiguracion('publico', $event.target.checked)"
            >
            <span class="toggle-slider"></span>
          </label>
        </div>

        <div class="config-item">
          <div class="config-info">
            <span class="config-label">Aprobación de miembros</span>
            <span class="config-desc">Requerir aprobación para nuevos participantes</span>
          </div>
          <label class="toggle">
            <input 
              type="checkbox" 
              v-model="configuracion.aprobacionRequerida"
              @change="actualizarConfiguracion('aprobacionRequerida', $event.target.checked)"
            >
            <span class="toggle-slider"></span>
          </label>
        </div>

        <div class="config-item">
          <div class="config-info">
            <span class="config-label">Solo admins pueden agregar</span>
            <span class="config-desc">Restringir adición de miembros a administradores</span>
          </div>
          <label class="toggle">
            <input 
              type="checkbox" 
              v-model="configuracion.soloAdminsAgregan"
              @change="actualizarConfiguracion('soloAdminsAgregan', $event.target.checked)"
            >
            <span class="toggle-slider"></span>
          </label>
        </div>

        <div class="config-item">
          <div class="config-info">
            <span class="config-label">Historial visible para nuevos</span>
            <span class="config-desc">Los nuevos miembros pueden ver mensajes anteriores</span>
          </div>
          <label class="toggle">
            <input 
              type="checkbox" 
              v-model="configuracion.historialVisible"
              @change="actualizarConfiguracion('historialVisible', $event.target.checked)"
            >
            <span class="toggle-slider"></span>
          </label>
        </div>
      </div>
    </div>

    <!-- Acciones peligrosas -->
    <div class="acciones-peligrosas">
      <h4 class="subsection-title danger">Zona Peligrosa</h4>
      
      <button @click="archivarGrupo" class="danger-btn">
        <i class="icon-archive"></i>
        Archivar Grupo
      </button>
      
      <button @click="eliminarGrupo" class="danger-btn">
        <i class="icon-trash"></i>
        Eliminar Grupo
      </button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AccionesAdmin',
  props: {
    grupo: { type: Object, required: true }
  },
  emits: [
    'agregarParticipante', 'invitarUsuario', 'transferirAdmin',
    'actualizarConfiguracion', 'archivarGrupo', 'eliminarGrupo'
  ],
  data() {
    return {
      configuracion: {
        publico: this.grupo.esPublico || false,
        aprobacionRequerida: this.grupo.requiereAprobacion || false,
        soloAdminsAgregan: this.grupo.soloAdminsAgregan || false,
        historialVisible: this.grupo.historialVisible || true
      }
    }
  },
  methods: {
    actualizarConfiguracion(campo, valor) {
      this.$emit('actualizarConfiguracion', { [campo]: valor })
    },
    archivarGrupo() {
      if (confirm('¿Estás seguro de archivar este grupo?')) {
        this.$emit('archivarGrupo')
      }
    },
    eliminarGrupo() {
      if (confirm('¿Estás seguro de eliminar este grupo? Esta acción no se puede deshacer.')) {
        this.$emit('eliminarGrupo')
      }
    }
  }
}
</script>

<style scoped>
.acciones-admin {
  background: white;
  border-radius: 12px;
  padding: 20px;
}

.section-title {
  margin: 0 0 20px 0;
  font-size: 18px;
  font-weight: 600;
}

.acciones-principales {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 24px;
}

.accion-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.accion-card:hover {
  background: var(--color-background);
  border-color: var(--color-primary);
}

.accion-icon {
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

.accion-content {
  flex: 1;
}

.accion-content h4 {
  margin: 0 0 4px 0;
  font-size: 16px;
  font-weight: 600;
}

.accion-content p {
  margin: 0;
  font-size: 14px;
  color: var(--color-text-secondary);
}

.configuracion-grupo {
  margin-bottom: 24px;
}

.subsection-title {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
}

.subsection-title.danger {
  color: var(--color-error);
}

.config-options {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.config-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.config-info {
  flex: 1;
}

.config-label {
  display: block;
  font-weight: 500;
  margin-bottom: 2px;
}

.config-desc {
  font-size: 14px;
  color: var(--color-text-secondary);
}

.toggle {
  position: relative;
  width: 50px;
  height: 24px;
}

.toggle input {
  opacity: 0;
  width: 0;
  height: 0;
}

.toggle-slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #ccc;
  border-radius: 12px;
  transition: 0.2s;
}

.toggle-slider:before {
  position: absolute;
  content: "";
  height: 20px;
  width: 20px;
  left: 2px;
  bottom: 2px;
  background-color: white;
  border-radius: 50%;
  transition: 0.2s;
}

.toggle input:checked + .toggle-slider {
  background-color: var(--color-primary);
}

.toggle input:checked + .toggle-slider:before {
  transform: translateX(26px);
}

.acciones-peligrosas {
  border-top: 1px solid var(--color-border);
  padding-top: 20px;
}

.danger-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  margin-right: 8px;
  margin-bottom: 8px;
  border: 1px solid var(--color-error);
  background: white;
  color: var(--color-error);
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.danger-btn:hover {
  background: var(--color-error);
  color: white;
}
</style>