// src/services/groupService.ts
import api from './api';

export const groupService = {
  async getGroupById(id: number) {
    const response = await api.get(`/grupos/${id}`);
    return response.data;
  },

  async getGroupStats(id: number) {
    const response = await api.get(`/grupos/${id}/stats`);
    return response.data;
  },

  async getParticipantsWithRoles(groupId: number) {
    const response = await api.get(`/grupos/${groupId}/participantes`);
    return response.data;
  },

  async updateGroup(id: number, updateData: any) {
    const response = await api.put(`/grupos/${id}`, updateData);
    return response.data;
  },

  async generateInviteCode(id: number) {
    const response = await api.post(`/grupos/${id}/generar-codigo`);
    return response.data;
  },

  async transferAdmin(groupId: number, newAdminId: number) {
    const response = await api.post(`/grupos/${groupId}/transferir-admin`, {
      newAdminId
    });
    return response.data;
  },

  async leaveGroup(groupId: number) {
    const response = await api.post(`/grupos/${groupId}/salir`);
    return response.data;
  },

  async deleteGroup(groupId: number) {
    const response = await api.delete(`/grupos/${groupId}`);
    return response.data;
  },

  async getGroupByInviteCode(code: string) {
    const response = await api.get(`/auth/invitacion/${code}`);
    return response.data;
  },

  async joinGroupByCode(code: string) {
    const response = await api.post(`/grupos/unirse/${code}`);
    return response.data;
  }
};
