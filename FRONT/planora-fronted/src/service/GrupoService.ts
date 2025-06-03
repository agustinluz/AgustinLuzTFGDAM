import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api'

// Configurar interceptor para incluir token JWT
axios.interceptors.request.use((config) => {
  const token = localStorage.getItem('authToken')
  if (token) {
    if (!config.headers) {
      config.headers = {};
    }
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config
})

export const groupService = {
  // Obtener grupo por ID
  async getGroupById(id: number) {
    const response = await axios.get(`${API_BASE_URL}/grupos/${id}`)
    return response.data
  },

  // Obtener estadísticas del grupo
  async getGroupStats(id: number) {
    const response = await axios.get(`${API_BASE_URL}/grupos/${id}/stats`)
    return response.data
  },

  // Obtener participantes con roles
  async getParticipantsWithRoles(groupId: number) {
    const response = await axios.get(`${API_BASE_URL}/grupos/${groupId}/participantes`)
    return response.data
  },

  // Actualizar grupo
  async updateGroup(id: number, updateData: any) {
    const response = await axios.put(`${API_BASE_URL}/grupos/${id}`, updateData)
    return response.data
  },

  // Generar código de invitación
  async generateInviteCode(id: number) {
    const response = await axios.post(`${API_BASE_URL}/grupos/${id}/generar-codigo`)
    return response.data
  },

  // Transferir administración
  async transferAdmin(groupId: number, newAdminId: number) {
    const response = await axios.post(`${API_BASE_URL}/grupos/${groupId}/transferir-admin`, {
      newAdminId
    })
    return response.data
  },

  // Salir del grupo
  async leaveGroup(groupId: number) {
    const response = await axios.post(`${API_BASE_URL}/grupos/${groupId}/salir`)
    return response.data
  },

  // Eliminar grupo
  async deleteGroup(groupId: number) {
    const response = await axios.delete(`${API_BASE_URL}/grupos/${groupId}`)
    return response.data
  },

  // Obtener grupo por código de invitación
  async getGroupByInviteCode(code: string) {
    const response = await axios.get(`${API_BASE_URL}/auth/invitacion/${code}`)
    return response.data
  },

  // Unirse a grupo por código
  async joinGroupByCode(code: string) {
    const response = await axios.post(`${API_BASE_URL}/grupos/unirse/${code}`)
    return response.data
  }
}

