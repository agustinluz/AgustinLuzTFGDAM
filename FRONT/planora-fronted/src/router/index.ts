import { createRouter, createWebHistory } from '@ionic/vue-router';
import { RouteRecordRaw } from 'vue-router';

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    component: () => import('../views/LoginPage.vue')
  },
  {
    path: '/login',
    component: () => import('../views/LoginPage.vue')
  },
  {
    path: '/grupo',
    component: () => import('../views/GrupoPage.vue') // futura pantalla principal del grupo
  },
  {
    path: '/registro',
    component: () => import('../views/RegisterPage.vue')
  },
  {
    path: '/dashboard/:id',
    name: 'dashboard',
    component: () => import('../views/Dashboard.vue'),
    props: true // Esto permite que el componente reciba el id como prop
  },
  {
    path: '/dashboard/:id/crear',
    name: 'crear',
    component: () => import('../views/Creacion/Crear.vue'),
    props: true
  },
  {
    path: '/dashboard/:id/crear/gasto',
    name: 'crear-gasto',
    component: () => import('../views/Creacion/CrearGasto.vue'),
    props: true
  },
  {
    path: '/dashboard/:id/crear/evento',
    name: 'crear-evento',
    component: () => import('../views/Creacion/CrearEvento.vue'),
    props: true
  },
   {
    path: '/dashboard/:id/crear/imagen',
    name: 'crear-imagen',
    component: () => import('../views/Creacion/CrearImagen.vue'),
    props: true
  },
  {
  path: '/dashboard/:grupoId/notas/crear',
  name: 'CrearNota',
  component: () => import('@/views/Creacion/CrearEditarNotaPage.vue')
},
{
  path: '/dashboard/:grupoId/notas/editar/:notaId',
  name: 'EditarNota',
  component: () => import('@/views/Creacion/CrearEditarNotaPage.vue')
},
{ path: '/dashboard/:grupoId/mis-notas', component: () => import('@/views/Detalles/Nota/MisNotasPage.vue'), props: true  },
  {
    path: '/dashboard/:id/crear/votacion',
    name: 'CrearVotacion',
    component: () => import('@/views/Creacion/CrearVotacion.vue'),
    props: true
  },
  {
    path: '/dashboard/:id/eventos',
    component: () => import('../views/Detalles/EventosPage.vue'),
    props: true
  },
  {
    path: '/dashboard/:id/votaciones',
    component: () => import('../views/Detalles/VotacionesPage.vue'),
    props: true
  },
  {
    path: '/dashboard/:id/gastos',
    component: () => import('../views/Detalles/GastosPage.vue'),
    props: true
  },{
    path: '/dashboard/:grupoId/gastos/:gastoId/editar',
    name: 'EditarGasto', 
    component: () => import('@/views/Detalles/EditarGasto.vue')
  },
  {
    path: '/grupo/:grupoId/resumen',
    name: 'ResumenGrupo',
    component: () => import('@/views/Detalles/ResumenDeudas.vue'),
    props: true
  },
  {
  path: '/dashboard/:grupoId/deudas/:usuarioId',
  name: 'DetalleDeudaUsuario',
  props: true,  
  component: () => import('@/views/Detalles/Gastos/DetalleDeudaUsuario.vue')
},
  {
    path: '/dashboard/:id/notas',
    component: () => import('../views/Detalles/NotasPage.vue'),
    props: true
  },
  {
    path: '/dashboard/:id/galeria',
    component: () => import('../views/Detalles/GaleriaPage.vue'),
    props: true
  },
  {
    path: '/perfil',
    name: 'perfil',
    component: () => import('../views/Configuracion/Perfil.vue'),
    props: true 
  },
  {
    path: '/grupo/:grupoId/configuracion',
    name: 'configuracionGrupo',
    component: () => import('../views/Configuracion/Grupo.vue'),
    props: true 
  },
  {
    path: '/invitaciones',
    component: () => import('../views/InvitacionesPage.vue')
  },
  {
    path: '/evento/:eventoId/asistir',
    name: 'asistir-evento',
    component: () => import('../views/Detalles/Evento/AsistirEvento.vue'),
    props: true
  }


]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const publicPages = ['/login', '/registro']

  if (!publicPages.includes(to.path) && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router