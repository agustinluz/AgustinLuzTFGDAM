<template>
  <div class="info-grupo">
    <!-- Avatar del grupo -->
    <div class="grupo-avatar">
      <div class="avatar-container" @click="$emit('cambiarAvatar')">
        <img 
          v-if="grupo.avatar" 
          :src="grupo.avatar" 
          :alt="grupo.nombre"
          class="avatar-img"
        >
        <div v-else class="avatar-placeholder">
          <i class="icon-group"></i>
        </div>
        <div v-if="esAdmin" class="avatar-overlay">
          <i class="icon-camera"></i>
        </div>
      </div>
    </div>

    <!-- Información básica -->
    <div class="grupo-info">
      <div class="nombre-section">
        <h1 class="grupo-nombre" @click="editarNombre" v-if="!editandoNombre">
          {{ grupo.nombre }}
          <i v-if="esAdmin" class="icon-edit"></i>
        </h1>
        <div v-else class="nombre-edit">
          <input 
            ref="nombreInput"
            v-model="nuevoNombre"
            @blur="guardarNombre"
            @keyup.enter="guardarNombre"
            @keyup.esc="cancelarEdicion"
            class="nombre-input"
            maxlength="50"
          >
          <div class="nombre-actions">
            <button @click="guardarNombre" class="btn-save">
              <i class="icon-check"></i>
            </button>
            <button @click="cancelarEdicion" class="btn-cancel">
              <i class="icon-x"></i>
            </button>
          </div>
        </div>
      </div>

      <div class="descripcion-section">
        <p class="grupo-descripcion" @click="editarDescripcion" v-if="!editandoDescripcion">
          {{ grupo.descripcion || 'Agregar descripción del grupo...' }}
          <i v-if="esAdmin" class="icon-edit"></i>
        </p>
        <div v-else class="descripcion-edit">
          <textarea 
            ref="descripcionInput"
            v-model="nuevaDescripcion"
            @blur="guardarDescripcion"
            @keyup.esc="cancelarEdicionDescripcion"
            class="descripcion-input"
            maxlength="200"
            rows="3"
            placeholder="Descripción del grupo..."
          ></textarea>
          <div class="descripcion-actions">
            <span class="char-count">{{ nuevaDescripcion.length }}/200</span>
            <button @click="guardarDescripcion" class="btn-save">
              <i class="icon-check"></i>
            </button>
            <button @click="cancelarEdicionDescripcion" class="btn-cancel">
              <i class="icon-x"></i>
            </button>
          </div>
        </div>
      </div>

      <!-- Metadatos del grupo -->
      <div class="grupo-meta">
        <div class="meta-item">
          <i class="icon-calendar"></i>
          <span>Creado {{ formatearFecha(grupo.fechaCreacion) }}</span>
        </div>
        <div class="meta-item" v-if="grupo.categoria">
          <i class="icon-tag"></i>
          <span>{{ grupo.categoria }}</span>
        </div>
        <div class="meta-item" v-if="grupo.privacidad">
          <i :class="grupo.privacidad === 'publico' ? 'icon-globe' : 'icon-lock'"></i>
          <span>{{ grupo.privacidad === 'publico' ? 'Público' : 'Privado' }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'InfoGrupo',
  props: {
    grupo: {
      type: Object,
      required: true
    },
    esAdmin: {
      type: Boolean,
      default: false
    }
  },
  emits: ['actualizarGrupo', 'cambiarAvatar'],
  data() {
    return {
      editandoNombre: false,
      editandoDescripcion: false,
      nuevoNombre: '',
      nuevaDescripcion: ''
    }
  },
  methods: {
    editarNombre() {
      if (!this.esAdmin) return
      this.editandoNombre = true
      this.nuevoNombre = this.grupo.nombre
      this.$nextTick(() => {
        this.$refs.nombreInput?.focus()
      })
    },

    async guardarNombre() {
      if (this.nuevoNombre.trim() && this.nuevoNombre !== this.grupo.nombre) {
        try {
          await this.$emit('actualizarGrupo', { nombre: this.nuevoNombre.trim() })
        } catch (error) {
          console.error('Error al actualizar nombre:', error)
        }
      }
      this.editandoNombre = false
    },

    cancelarEdicion() {
      this.editandoNombre = false
      this.nuevoNombre = ''
    },

    editarDescripcion() {
      if (!this.esAdmin) return
      this.editandoDescripcion = true
      this.nuevaDescripcion = this.grupo.descripcion || ''
      this.$nextTick(() => {
        this.$refs.descripcionInput?.focus()
      })
    },

    async guardarDescripcion() {
      if (this.nuevaDescripcion !== this.grupo.descripcion) {
        try {
          await this.$emit('actualizarGrupo', { descripcion: this.nuevaDescripcion })
        } catch (error) {
          console.error('Error al actualizar descripción:', error)
        }
      }
      this.editandoDescripcion = false
    },

    cancelarEdicionDescripcion() {
      this.editandoDescripcion = false
      this.nuevaDescripcion = ''
    },

    formatearFecha(fecha) {
      return new Date(fecha).toLocaleDateString('es-ES', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      })
    }
  }
}
</script>

<style scoped>
.info-grupo {
  padding: 20px;
  background: white;
  border-radius: 12px;
  margin-bottom: 16px;
}

.grupo-avatar {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

.avatar-container {
  position: relative;
  width: 80px;
  height: 80px;
  cursor: pointer;
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
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 32px;
}

.avatar-overlay {
  position: absolute;
  bottom: 0;
  right: 0;
  background: var(--color-primary);
  color: white;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  border: 2px solid white;
}

.grupo-info {
  text-align: center;
}

.nombre-section {
  margin-bottom: 12px;
}

.grupo-nombre {
  font-size: 24px;
  font-weight: bold;
  color: var(--color-text);
  margin: 0;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.grupo-nombre i {
  font-size: 16px;
  opacity: 0.6;
}

.nombre-edit {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.nombre-input {
  font-size: 24px;
  font-weight: bold;
  text-align: center;
  border: 2px solid var(--color-primary);
  border-radius: 8px;
  padding: 8px;
  background: white;
}

.nombre-actions {
  display: flex;
  justify-content: center;
  gap: 8px;
}

.descripcion-section {
  margin-bottom: 16px;
}

.grupo-descripcion {
  color: var(--color-text-secondary);
  margin: 0;
  cursor: pointer;
  font-style: italic;
  min-height: 20px;
}

.descripcion-edit {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.descripcion-input {
  border: 2px solid var(--color-primary);
  border-radius: 8px;
  padding: 12px;
  font-family: inherit;
  resize: vertical;
  min-height: 60px;
}

.descripcion-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.char-count {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.btn-save, .btn-cancel {
  padding: 4px 8px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.btn-save {
  background: var(--color-success);
  color: white;
}

.btn-cancel {
  background: var(--color-error);
  color: white;
}

.grupo-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding-top: 16px;
  border-top: 1px solid var(--color-border);
}

.meta-item {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: var(--color-text-secondary);
  font-size: 14px;
}

.meta-item i {
  font-size: 16px;
}
</style>