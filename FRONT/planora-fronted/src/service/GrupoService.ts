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

// Función helper para obtener headers de admin
const getAdminHeaders = () => {
  const token = localStorage.getItem('token');
  const usuario = localStorage.getItem('usuario');
  let adminId = localStorage.getItem('usuarioId');
  
  if (!adminId && usuario) {
    try {
      const usuarioObj = JSON.parse(usuario);
      adminId = usuarioObj.id?.toString();
    } catch (error) {
      console.error('Error al parsear usuario del localStorage:', error);
    }
  }
  
  const headers: any = {};
  
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  
  if (adminId) {
    headers['adminId'] = adminId;
  }
  
  return headers;
};

// Función para validar ID
const validateId = (id: number): boolean => {
  return !isNaN(id) && id > 0;
};

export const groupService = {
  // Obtener grupo por ID
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

  // Obtener grupos de un usuario
  async getUserGroups(userId: number) {
    if (!validateId(userId)) {
      throw new Error(`ID de usuario inválido: ${userId}`);
    }
    
    const response = await api.get(`/grupos/usuario/${userId}`, {
      headers: getAuthHeaders()
    });
    return response;
  },

  // Crear nuevo grupo
  async createGroup(groupData: any) {
    const response = await api.post('/grupos', groupData, {
      headers: getAuthHeaders()
    });
    return response;
  },

  // Actualizar grupo (solo admin)
  async updateGroup(id: number, updateData: any) {
    if (!validateId(id)) {
      throw new Error(`ID de grupo inválido: ${id}`);
    }
    
    const response = await api.put(`/grupos/${id}`, updateData, {
      headers: getAuthHeaders()
    });
    return response;
  },

  // Eliminar participante del grupo (solo admin)
  async removeParticipant(groupId: number, userId: number) {
    if (!validateId(groupId) || !validateId(userId)) {
      throw new Error(`IDs inválidos - Grupo: ${groupId}, Usuario: ${userId}`);
    }
    
    const response = await api.delete(`/grupos/${groupId}/usuarios/${userId}`, {
      headers: getAdminHeaders()
    });
    return response;
  },

  // Listar usuarios del grupo (información básica)
  async getGroupUsers(groupId: number) {
    if (!validateId(groupId)) {
      throw new Error(`ID de grupo inválido: ${groupId}`);
    }
    
    const response = await api.get(`/grupos/${groupId}/usuarios`, {
      headers: getAuthHeaders()
    });
    return response;
  },

  // Obtener participantes con información básica
  async getGroupParticipants(groupId: number) {
    if (!validateId(groupId)) {
      throw new Error(`ID de grupo inválido: ${groupId}`);
    }
    
    const response = await api.get(`/grupos/${groupId}/participantes`, {
      headers: getAuthHeaders()
    });
    return response;
  },

  // Obtener participantes con información completa (roles incluidos)
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

  // Registrar usuario en grupo (crear usuario si no existe)
  async registerUserInGroup(groupId: number, userData: any) {
    if (!validateId(groupId)) {
      throw new Error(`ID de grupo inválido: ${groupId}`);
    }
    
    const response = await api.post(`/grupos/${groupId}/usuarios`, userData, {
      headers: getAuthHeaders()
    });
    return response;
  },

  // Buscar usuario por email (para invitaciones)
  async searchUserByEmail(email: string) {
    if (!email || email.trim() === '') {
      throw new Error('Email vacío');
    }
    
    const response = await api.get(`/grupos/buscar-usuario?email=${encodeURIComponent(email)}`, {
      headers: getAuthHeaders()
    });
    return response;
  },

  // Invitar usuario existente al grupo
  async inviteExistingUser(groupId: number, userEmail: string) {
    if (!validateId(groupId) || !userEmail || userEmail.trim() === '') {
      throw new Error(`Parámetros inválidos - Grupo: ${groupId}, Email: ${userEmail}`);
    }
    
    const response = await api.post(`/grupos/${groupId}/invitar?emailUsuario=${encodeURIComponent(userEmail)}`, {}, {
      headers: getAdminHeaders()
    });
    return response;
  },

  // Cambiar rol de usuario
  async changeUserRole(groupId: number, userId: number, newRole: string) {
    if (!validateId(groupId) || !validateId(userId) || !newRole) {
      throw new Error(`Parámetros inválidos - Grupo: ${groupId}, Usuario: ${userId}, Rol: ${newRole}`);
    }
    
    const response = await api.put(`/grupos/${groupId}/usuarios/${userId}/rol?nuevoRol=${newRole}`, {}, {
      headers: getAdminHeaders()
    });
    return response;
  },

  // Transferir administración
  async transferAdmin(groupId: number, newAdminId: number) {
    if (!validateId(groupId) || !validateId(newAdminId)) {
      throw new Error(`IDs inválidos - Grupo: ${groupId}, Nuevo Admin: ${newAdminId}`);
    }
    
    const response = await api.put(`/grupos/${groupId}/transferir-admin?nuevoAdminId=${newAdminId}`, {}, {
      headers: getAdminHeaders()
    });
    return response;
  },

  // Salir del grupo
  async leaveGroup(groupId: number) {
    if (!validateId(groupId)) {
      throw new Error(`ID de grupo inválido: ${groupId}`);
    }
    
    const response = await api.delete(`/grupos/${groupId}/salir`, {
      headers: getAuthHeaders()
    });
    return response;
  },

  // Obtener información de un participante específico
  async getParticipantInfo(groupId: number, userId: number) {
    if (!validateId(groupId) || !validateId(userId)) {
      throw new Error(`IDs inválidos - Grupo: ${groupId}, Usuario: ${userId}`);
    }
    
    const usuario = localStorage.getItem('usuario');
    const token = localStorage.getItem('token');
    let solicitanteId = localStorage.getItem('usuarioId');
    
    if (!solicitanteId && usuario) {
      try {
        const usuarioObj = JSON.parse(usuario);
        solicitanteId = usuarioObj.id?.toString();
      } catch (error) {
        console.error('Error al obtener solicitanteId:', error);
      }
    }
    
    const headers: any = {};
    if (token) headers['Authorization'] = `Bearer ${token}`;
    if (solicitanteId) headers['solicitanteId'] = solicitanteId;
    
    const response = await api.get(`/grupos/${groupId}/usuarios/${userId}`, {
      headers
    });
    return response;
  },

  // Generar nuevo código de invitación
  async generateInviteCode(id: number) {
    if (!validateId(id)) {
      throw new Error(`ID de grupo inválido: ${id}`);
    }
    
    const response = await api.post(`/grupos/${id}/generar-codigo`, {}, {
      headers: getAuthHeaders()
    });
    return response;
  },

  // Obtener estadísticas del grupo
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

  // Eliminar grupo (solo admin)
  async deleteGroup(groupId: number) {
    if (!validateId(groupId)) {
      throw new Error(`ID de grupo inválido: ${groupId}`);
    }
    
    const response = await api.delete(`/grupos/${groupId}`, {
      headers: getAuthHeaders()
    });
    return response;
  }
};