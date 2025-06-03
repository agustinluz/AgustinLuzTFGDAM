<template>
  <div v-if="visible" class="modal-overlay" @click="cerrar">
    <div class="modal-content" @click.stop>
      <div class="modal-header">
        <h3>Agregar Participante</h3>
        <button @click="cerrar" class="close-btn">
          <i class="icon-x"></i>
        </button>
      </div>

      <div class="modal-body">
        <div class="search-section">
          <div class="search-input">
            <i class="icon-search"></i>
            <input 
              v-model="busqueda"
              placeholder="Buscar usuarios..."
              @input="buscarUsuarios"
            >
          </div>
        </div>

        <div class="usuarios-lista" v-if="usuarios.length > 0">
          <div 
            v-for="usuario in usuarios" 
            :key="usuario.id"
            class="usuario-item"
            :class="{ seleccionado: usuariosSeleccionados.includes(usuario.id) }"
            @click="toggleUsuario(usuario)"
          >
            <div class="usuario-avatar">
              <img v-if="usuario.avatar" :src="usuario.avatar" :alt="usuario.nombre">
              <div v-else class="avatar-placeholder">
                {{ usuario.nombre.charAt(0) }}
              </div>
            </div>
            <div class="usuario-info">
              <div class="usuario-nombre">{{ usuario.nombre }}</div>
              <div class="usuario-email">{{ usuario.email }}</div>
            </div>
            <div class="check-icon" v-if="usuariosSeleccionados.includes(usuario.id)">
              <i class="icon-check"></i>
            </div>
          </div>
        </div>

        <div v-else-if="busqueda && !cargando" class="estado-vacio">
          <i class="icon-users"></i>
          <p>No se encontraron usuarios</p>
        </div>

        <div v-if="cargando" class="loading">
          <div class="spinner"></div>
        </div>
      </div>

      <div class="modal-footer">
        <button @click="cerrar" class="btn-secondary">Cancelar</button>
        <button 
          @click="agregarUsuarios" 
          :disabled="usuariosSeleccionados.length === 0 || agregando"
          class="btn-primary"
        >
          {{ agregando ? 'Agregando...' : `Agregar (${usuariosSeleccionados.length})` }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ModalAgregarParticipante',
  props: {
    visible: Boolean,
    grupoId: String
  },
  emits: ['cerrar', 'usuariosAgregados'],
  data() {
    return {
      busqueda: '',
      usuarios: [],
      usuariosSeleccionados: [],
      cargando: false,
      agregando: false
    }
  },
  methods: {
    async buscarUsuarios() {
      if (!this.busqueda.trim()) {
        this.usuarios = []
        return
      }
      
      this.cargando = true
      try {
        // Simular API call
        await new Promise(resolve => setTimeout(resolve, 500))
        this.usuarios = [
          { id: 1, nombre: 'Juan Pérez', email: 'juan@email.com', avatar: null },
          { id: 2, nombre: 'Ana García', email: 'ana@email.com', avatar: null },
          { id: 3, nombre: 'Carlos López', email: 'carlos@email.com', avatar: null }
        ].filter(user => 
          user.nombre.toLowerCase().includes(this.busqueda.toLowerCase()) ||
          user.email.toLowerCase().includes(this.busqueda.toLowerCase())
        )
      } catch (error) {
        console.error('Error buscando usuarios:', error)
      } finally {
        this.cargando = false
      }
    },

    toggleUsuario(usuario) {
      const index = this.usuariosSeleccionados.indexOf(usuario.id)
      if (index > -1) {
        this.usuariosSeleccionados.splice(index, 1)
      } else {
        this.usuariosSeleccionados.push(usuario.id)
      }
    },

    async agregarUsuarios() {
      this.agregando = true
      try {
        await new Promise(resolve => setTimeout(resolve, 1000))
        this.$emit('usuariosAgregados', this.usuariosSeleccionados)
        this.cerrar()
      } catch (error) {
        console.error('Error agregando usuarios:', error)
      } finally {
        this.agregando = false
      }
    },

    cerrar() {
      this.busqueda = ''
      this.usuarios = []
      this.usuariosSeleccionados = []
      this.$emit('cerrar')
    }
  }
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  max-width: 500px;
  width: 90%;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid var(--color-border);
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
}

.close-btn {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  padding: 4px;
}

.modal-body {
  padding: 20px;
  flex: 1;
  overflow-y: auto;
}

.search-input {
  position: relative;
  margin-bottom: 16px;
}

.search-input i {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--color-text-secondary);
}

.search-input input {
  width: 100%;
  padding: 12px 12px 12px 40px;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  font-size: 14px;
}

.usuarios-lista {
  max-height: 300px;
  overflow-y: auto;
}

.usuario-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.usuario-item:hover {
  background: var(--color-background);
}

.usuario-item.seleccionado {
  background: rgba(var(--color-primary-rgb), 0.1);
  border: 1px solid var(--color-primary);
}

.usuario-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
}

.usuario-avatar img {
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
  font-weight: bold;
}

.usuario-info {
  flex: 1;
}

.usuario-nombre {
  font-weight: 500;
  margin-bottom: 2px;
}

.usuario-email {
  font-size: 14px;
  color: var(--color-text-secondary);
}

.check-icon {
  color: var(--color-primary);
  font-size: 18px;
}

.estado-vacio {
  text-align: center;
  padding: 40px 20px;
  color: var(--color-text-secondary);
}

.estado-vacio i {
  font-size: 48px;
  margin-bottom: 12px;
  opacity: 0.5;
}

.loading {
  display: flex;
  justify-content: center;
  padding: 20px;
}

.spinner {
  width: 24px;
  height: 24px;
  border: 2px solid var(--color-border);
  border-top: 2px solid var(--color-primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px;
  border-top: 1px solid var(--color-border);
}

.btn-secondary, .btn-primary {
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  border: none;
}

.btn-secondary {
  background: var(--color-background);
  color: var(--color-text);
}

.btn-primary {
  background: var(--color-primary);
  color: white;
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>