// src/services/groupService.ts
import api from './api';

// Función helper para obtener el token y usuarioId del localStorage
const getAuthHeaders = () => {
  const token = localStorage.getItem('token');
  const usuario = localStorage.getItem('usuario');
  let usuarioId = localStorage.getItem('usuarioId');
  
  // Si no hay usuarioId pero hay usuario, extraerlo del objeto usuario
  if (!usuarioId && usuario) {
    try {
      const usuarioObj = JSON.parse(usuario);
      usuarioId = usuarioObj.id?.toString();
    } catch (error) {
      console.error('Error al parsear usuario del localStorage:', error);
    }
  }
  
  const headers: any = {};
  
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  
  if (usuarioId) {
    headers['usuarioId'] = usuarioId;
  }
  
  return headers;
};

// Función para validar ID
const validateId = (id: number): boolean => {
  return !isNaN(id) && id > 0;
};

export const groupService = {
  async getGroupById(id: number) {
    if (!validateId(id)) {
      throw new Error(`ID de grupo inválido: ${id}`);
    }
    
    console.log(`Obteniendo grupo con ID: ${id}`);
    
    const response = await api.get(`/grupos/${id}`, {
      headers: getAuthHeaders()
    });
    return response;
  },

  async getGroupStats(id: number) {
    if (!validateId(id)) {
      throw new Error(`ID de grupo inválido: ${id}`);
    }
    
    console.log(`Obteniendo estadísticas para grupo ID: ${id}`);
    
    const response = await api.get(`/grupos/${id}/estadisticas`, {
      headers: getAuthHeaders()
    });
    return response;
  },

  async getParticipantsWithRoles(groupId: number) {
    if (!validateId(groupId)) {
      throw new Error(`ID de grupo inválido: ${groupId}`);
    }
    
    console.log(`Obteniendo participantes para grupo ID: ${groupId}`);
    
    const response = await api.get(`/grupos/${groupId}/participantes-con-roles`, {
      headers: getAuthHeaders()
    });
    return response;
  },

  async updateGroup(id: number, updateData: any) {
    if (!validateId(id)) {
      throw new Error(`ID de grupo inválido: ${id}`);
    }
    
    const response = await api.put(`/grupos/${id}`, updateData, {
      headers: getAuthHeaders()
    });
    return response;
  },

  async generateInviteCode(id: number) {
    if (!validateId(id)) {
      throw new Error(`ID de grupo inválido: ${id}`);
    }
    
    const response = await api.post(`/grupos/${id}/generar-codigo`, {}, {
      headers: getAuthHeaders()
    });
    return response;
  },

  async transferAdmin(groupId: number, newAdminId: number) {
    if (!validateId(groupId) || !validateId(newAdminId)) {
      throw new Error(`IDs inválidos - Grupo: ${groupId}, Nuevo Admin: ${newAdminId}`);
    }
    
    const usuario = localStorage.getItem('usuario');
    const token = localStorage.getItem('token');
    let adminId = localStorage.getItem('usuarioId');
    
    if (!adminId && usuario) {
      try {
        const usuarioObj = JSON.parse(usuario);
        adminId = usuarioObj.id?.toString();
      } catch (error) {
        console.error('Error al obtener adminId:', error);
      }
    }
    
    const headers: any = {};
    if (token) headers['Authorization'] = `Bearer ${token}`;
    if (adminId) headers['adminId'] = adminId;
    
    const response = await api.put(`/grupos/${groupId}/transferir-admin?nuevoAdminId=${newAdminId}`, {}, {
      headers
    });
    return response;
  },

  async leaveGroup(groupId: number) {
    if (!validateId(groupId)) {
      throw new Error(`ID de grupo inválido: ${groupId}`);
    }
    
    const response = await api.delete(`/grupos/${groupId}/salir`, {
      headers: getAuthHeaders()
    });
    return response;
  },

  async deleteGroup(groupId: number) {
    if (!validateId(groupId)) {
      throw new Error(`ID de grupo inválido: ${groupId}`);
    }
    
    const response = await api.delete(`/grupos/${groupId}`, {
      headers: getAuthHeaders()
    });
    return response;
  },

  async getGroupByInviteCode(code: string) {
    if (!code || code.trim() === '') {
      throw new Error('Código de invitación vacío');
    }
    
    const response = await api.get(`/auth/invitacion/${code}`, {
      headers: getAuthHeaders()
    });
    return response;
  },

  async joinGroupByCode(code: string) {
    if (!code || code.trim() === '') {
      throw new Error('Código de invitación vacío');
    }
    
    const response = await api.post(`/grupos/unirse/${code}`, {}, {
      headers: getAuthHeaders()
    });
    return response;
  }
};