<template>
  <div v-if="visible" class="modal-overlay" @click="cerrar">
    <div class="modal-content" @click.stop>
      <div class="modal-header">
        <h3>Invitar Usuarios</h3>
        <button @click="cerrar" class="close-btn">
          <i class="icon-x"></i>
        </button>
      </div>

      <div class="modal-body">
        <div class="email-section">
          <label>Correos electrónicos</label>
          <textarea 
            v-model="emails"
            placeholder="Ingresa los emails separados por comas o líneas nuevas..."
            class="email-input"
            rows="4"
          ></textarea>
          <div class="email-help">
            Ejemplo: usuario1@email.com, usuario2@email.com
          </div>
        </div>

        <div class="mensaje-section">
          <label>Mensaje de invitación (opcional)</label>
          <textarea 
            v-model="mensaje"
            placeholder="Escribe un mensaje personalizado..."
            class="mensaje-input"
            rows="3"
          ></textarea>
        </div>

        <div class="opciones-section">
          <label class="opcion-item">
            <input type="checkbox" v-model="opciones.requiereAprobacion">
            <span>Requerir aprobación del administrador</span>
          </label>
          <label class="opcion-item">
            <input type="checkbox" v-model="opciones.enviarRecordatorio">
            <span>Enviar recordatorio en 24 horas</span>
          </label>
        </div>

        <div v-if="emailsValidos.length > 0" class="preview-section">
          <h4>Invitaciones a enviar ({{ emailsValidos.length }})</h4>
          <div class="emails-preview">
            <span v-for="email in emailsValidos" :key="email" class="email-tag">
              {{ email }}
            </span>
          </div>
        </div>

        <div v-if="emailsInvalidos.length > 0" class="error-section">
          <h4>Emails inválidos</h4>
          <div class="emails-error">
            <span v-for="email in emailsInvalidos" :key="email" class="email-error">
              {{ email }}
            </span>
          </div>
        </div>
      </div>

      <div class="modal-footer">
        <button @click="cerrar" class="btn-secondary">Cancelar</button>
        <button 
          @click="enviarInvitaciones" 
          :disabled="emailsValidos.length === 0 || enviando"
          class="btn-primary"
        >
          {{ enviando ? 'Enviando...' : `Enviar Invitaciones (${emailsValidos.length})` }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ModalInvitarUsuario',
  props: {
    visible: Boolean,
    grupoId: String
  },
  emits: ['cerrar', 'invitacionesEnviadas'],
  data() {
    return {
      emails: '',
      mensaje: '',
      enviando: false,
      opciones: {
        requiereAprobacion: false,
        enviarRecordatorio: true
      }
    }
  },
  computed: {
    emailsArray() {
      return this.emails
        .split(/[,\n]/)
        .map(email => email.trim())
        .filter(email => email.length > 0)
    },
    emailsValidos() {
      return this.emailsArray.filter(email => this.validarEmail(email))
    },
    emailsInvalidos() {
      return this.emailsArray.filter(email => !this.validarEmail(email))
    }
  },
  methods: {
    validarEmail(email) {
      const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      return regex.test(email)
    },

    async enviarInvitaciones() {
      this.enviando = true
      try {
        const invitaciones = {
          emails: this.emailsValidos,
          mensaje: this.mensaje,
          opciones: this.opciones
        }
        
        await new Promise(resolve => setTimeout(resolve, 1500))
        this.$emit('invitacionesEnviadas', invitaciones)
        this.cerrar()
      } catch (error) {
        console.error('Error enviando invitaciones:', error)
      } finally {
        this.enviando = false
      }
    },

    cerrar() {
      this.emails = ''
      this.mensaje = ''
      this.opciones = {
        requiereAprobacion: false,
        enviarRecordatorio: true
      }
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
  max-width: 600px;
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

.email-section,
.mensaje-section,
.opciones-section {
  margin-bottom: 20px;
}

.email-section label,
.mensaje-section label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
}

.email-input,
.mensaje-input {
  width: 100%;
  padding: 12px;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  font-size: 14px;
  font-family: inherit;
  resize: vertical;
}

.email-help {
  font-size: 12px;
  color: var(--color-text-secondary);
  margin-top: 4px;
}

.opciones-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.opcion-item {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 14px;
}

.preview-section,
.error-section {
  margin-top: 16px;
  padding: 12px;
  border-radius: 8px;
}

.preview-section {
  background: rgba(var(--color-success-rgb), 0.1);
  border: 1px solid var(--color-success);
}

.error-section {
  background: rgba(var(--color-error-rgb), 0.1);
  border: 1px solid var(--color-error);
}

.preview-section h4,
.error-section h4 {
  margin: 0 0 8px 0;
  font-size: 14px;
}

.emails-preview,
.emails-error {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.email-tag {
  background: var(--color-success);
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.email-error {
  background: var(--color-error);
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px;
  border-top: 1px solid var(--color-border);
}

.btn-secondary,
.btn-primary {
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