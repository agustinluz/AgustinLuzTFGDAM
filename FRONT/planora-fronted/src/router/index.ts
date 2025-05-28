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
  // Ruta de redirección por si alguien va a /dashboard sin ID
  {
    path: '/dashboard',
    redirect: '/grupo'
  },
  {
    path: '/dashboard/:id/crear',
    component: () => import('../views/Creacion/Crear.vue'),
    props: true // Esto permite que el componente reciba el id como prop
  },
  {
    path: '/dashboard/:id/crear/gasto',
    component: () => import('../views/Creacion/CrearGasto.vue'),
    props: true // Esto permite que el componente reciba el id como prop
  },
  {
    path: '/dashboard/:id/crear/evento',
    component: () => import('../views/Creacion/CrearEvento.vue'),
    props: true // Esto permite que el componente reciba el id como prop
  },
   {
    path: '/dashboard/:id/crear/imagen',
    component: () => import('../views/Creacion/CrearImagen.vue'),
    props: true // Esto permite que el componente reciba el id como prop
  },
  {
    path: '/dashboard/:id/crear/nota',
    component: () => import('../views/Creacion/CrearNota.vue'),
    props: true
  },
  {
    path: '/dashboard/:id/crear/votacion',
    component: () => import('../views/Creacion/CrearVotacion.vue'),
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
  },
    {
    path: '/dashboard/:id/gastos/:idGasto/editar',
    component: () => import('../views/Detalles/EditarGasto.vue'),
    props: true
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