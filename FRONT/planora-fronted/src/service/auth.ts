import { defineStore } from 'pinia'
import api from './api'

interface User {
  id: number
  nombre: string
  email: string
  grupoIds: number[]
}

interface LoginCredentials {
  email: string
  password: string
}

interface RegisterCredentials {
  nombre: string
  email: string
  password: string
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    usuarioActual: null as User | null,
    token: localStorage.getItem('token') || null,
    isAuthenticated: false
  }),

  getters: {
    isLoggedIn: (state) => state.isAuthenticated && !!state.token,
    gruposUsuario: (state) => state.usuarioActual?.grupoIds || []
  },

  actions: {
    // Login tradicional
    async login(credentials: LoginCredentials) {
      try {
        const response = await api.post<{ token: string; usuario: User }>('/auth/login', credentials)
        const { token, usuario } = response.data

        this.token = token
        this.usuarioActual = usuario
        this.isAuthenticated = true

        localStorage.setItem('token', token)
        localStorage.setItem('currentUser', JSON.stringify(usuario))
         // Mantener compatibilidad con código existente
        localStorage.setItem('usuario', JSON.stringify(usuario))
        localStorage.setItem('usuarioId', usuario.id.toString())

        return { success: true, usuario }
      } catch (error: any) {
        console.error('Error en login:', error)
        return { 
          success: false, 
          message: error.response?.data || 'Error al iniciar sesión' 
        }
      }
    },

    // Registro
    async register(credentials: RegisterCredentials) {
      try {
        const response = await api.post('/auth/registro', credentials)
        return { success: true, usuario: response.data }
      } catch (error: any) {
        console.error('Error en registro:', error)
        return { 
          success: false, 
          message: error.response?.data || 'Error al registrarse' 
        }
      }
    },
    async resetPassword(email: string, password: string) {
      try {
        const response = await api.post('/auth/reset-password', { email, password })
        return { success: true, data: response.data }
      } catch (error: any) {
        console.error('Error al restablecer contraseña:', error)
        return {
          success: false,
          message: error.response?.data?.error || 'Error al restablecer'
        }
      }
    },

    // Logout
    logout() {
      this.usuarioActual = null
      this.token = null
      this.isAuthenticated = false
      
      localStorage.removeItem('token')
      localStorage.removeItem('currentUser')
      localStorage.removeItem('usuario')
      localStorage.removeItem('usuarioId')
    },

    // Inicializar desde localStorage
    initializeFromStorage() {
      const token = localStorage.getItem('token')
      const datosUsuario =
        localStorage.getItem('currentUser') || localStorage.getItem('usuario')

      if (token && datosUsuario) {
        try {
          this.token = token
          this.usuarioActual = JSON.parse(datosUsuario)
          this.isAuthenticated = true

           // Sincronizar claves para mantener compatibilidad
          localStorage.setItem('currentUser', JSON.stringify(this.usuarioActual))
          localStorage.setItem(
            'usuario',
            JSON.stringify(this.usuarioActual)
          )
          if (this.usuarioActual) {
            localStorage.setItem('usuarioId', this.usuarioActual.id.toString())
          }
        } catch (error) {
          console.error('Error al parsear datos de usuario:', error)
          this.logout()
        }
      }
    },

    // Verificar si el token es válido
    async verifyToken() {
      if (!this.token) return false

      try {
        // Aquí podrías hacer una llamada al backend para verificar el token
        // Por ahora, asumimos que si existe es válido
        return true
      } catch (error) {
        this.logout()
        return false
      }
    }
  }
})