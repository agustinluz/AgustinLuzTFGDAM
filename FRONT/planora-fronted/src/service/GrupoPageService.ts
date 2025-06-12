import { groupService } from './GrupoService'
import { EventosService } from './EventoService'
import { imageService } from './imagenService'

export const grupoPageService = {
  async listarGruposConStats(usuarioId: number) {
    const res = await groupService.getUserGroups(usuarioId)
    const grupos = res.data as any[]
    for (const g of grupos) {
      const [usuariosRes, eventos, imagenes] = await Promise.all([
        groupService.getGroupUsers(g.id),
        EventosService.obtenerEventosGrupo(g.id),
        imageService.getByGrupo(g.id)
      ])
      g.participantesCount = usuariosRes.data?.length || 0
      g.eventosCount = eventos.length
      g.fotosCount = Array.isArray(imagenes) ? imagenes.length : (imagenes.total || 0)
    }
    return grupos
  },

  async crearGrupo(nombre: string, imagenBase64: string) {
    const res = await groupService.createGroup({ nombre, imagenPerfil: imagenBase64 })
    return res.data
  },

  async unirseConCodigo(codigo: string, usuario: any) {
    const resGrupo = await groupService.getGroupByInviteCode(codigo)
    const grupo = resGrupo.data
    await groupService.registerUserInGroup(grupo.id, usuario)
    return grupo
  }
}