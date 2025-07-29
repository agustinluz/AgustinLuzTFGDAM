// src/services/imageService.ts
import api from './api';

export const imageService = {
  async getByGrupo(grupoId: number) {
    const res = await api.get(`/imagenes/grupo/${grupoId}`);
    const data = res.data as { imagenes?: any };
    return data.imagenes || data;
  },

  async getByUsuario(usuarioId: number) {
    const res = await api.get(`/imagenes/usuario/${usuarioId}`);
    const data = res.data as { imagenes?: any };
    return data.imagenes || data;
  },

  async getByEvento(eventoId: number) {
    const res = await api.get(`/imagenes/evento/${eventoId}`);
    const data = res.data as { imagenes?: any };
    return data.imagenes || data;
  },

  async uploadImage(data: FormData) {
    const res = await api.post(`/imagenes/subir`, data, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return res.data;
  },

  async deleteImage(id: number) {
    const res = await api.delete(`/imagenes/${id}`);
    return res.data;
  },

  async getFullImage(id: number) {
    const res = await api.get(`/imagenes/${id}`);
    return res.data;
  },

   async getImageData(id: number) {
    const res = await api.get(`/imagenes/${id}/datos`);
    const data = res.data as { datos?: string };
    return data.datos || '';
  }


};
