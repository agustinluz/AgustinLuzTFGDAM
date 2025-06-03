import { defineStore } from 'pinia'
import axios from 'axios'

const API_BASE_URL = `${import.meta.env.VITE_API_URL}`

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
    currentUser: null as User | null,
    token: localStorage.getItem('authToken') || null,
    isAuthenticated: false
  }),

  getters: {
    isLoggedIn: (state) => state.isAuthenticated && !!state.token,
    userGroups: (state) => state.currentUser?.grupoIds || []
  },

  actions: {
    // Login tradicional
    async login(credentials: LoginCredentials) {
      try {
        const response = await axios.post<{ token: string; usuario: User }>(`${API_BASE_URL}/login`, credentials)
        const { token, usuario } = response.data

        this.token = token
        this.currentUser = usuario
        this.isAuthenticated = true

        localStorage.setItem('authToken', token)
        localStorage.setItem('currentUser', JSON.stringify(usuario))

        return { success: true, user: usuario }
      } catch (error: any) {
        console.error('Error en login:', error)
        return { 
          success: false, 
          message: error.response?.data || 'Error al iniciar sesión' 
        }
      }
    },

    // Login con Google
    async loginWithGoogle(googleData: { email: string, nombre?: string }) {
      try {
        const response = await axios.post<{ token: string; usuario: User }>(`${API_BASE_URL}/login-google`, googleData)
        const { token, usuario } = response.data

        this.token = token
        this.currentUser = usuario
        this.isAuthenticated = true

        localStorage.setItem('authToken', token)
        localStorage.setItem('currentUser', JSON.stringify(usuario))

        return { success: true, user: usuario }
      } catch (error: any) {
        console.error('Error en login con Google:', error)
        return { 
          success: false, 
          message: error.response?.data || 'Error al iniciar sesión con Google' 
        }
      }
    },

    // Registro
    async register(credentials: RegisterCredentials) {
      try {
        const response = await axios.post(`${API_BASE_URL}/registro`, credentials)
        return { success: true, user: response.data }
      } catch (error: any) {
        console.error('Error en registro:', error)
        return { 
          success: false, 
          message: error.response?.data || 'Error al registrarse' 
        }
      }
    },

    // Logout
    logout() {
      this.currentUser = null
      this.token = null
      this.isAuthenticated = false
      
      localStorage.removeItem('authToken')
      localStorage.removeItem('currentUser')
    },

    // Inicializar desde localStorage
    initializeFromStorage() {
      const token = localStorage.getItem('authToken')
      const userData = localStorage.getItem('currentUser')

      if (token && userData) {
        try {
          this.token = token
          this.currentUser = JSON.parse(userData)
          this.isAuthenticated = true
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
