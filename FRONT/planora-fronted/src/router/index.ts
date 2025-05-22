import { createRouter, createWebHistory } from '@ionic/vue-router';
import { RouteRecordRaw } from 'vue-router';

const routes: Array<RouteRecordRaw> = [
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
    component: () => import('../views/Crear.vue'),
    props: true // Esto permite que el componente reciba el id como prop
  },
  {
    path: '/dashboard/:id/crear/gasto',
    component: () => import('../views/CrearGasto.vue'),
    props: true // Esto permite que el componente reciba el id como prop
  },
  {
    path: '/dashboard/:id/crear/evento',
    component: () => import('../views/CrearEvento.vue'),
    props: true // Esto permite que el componente reciba el id como prop
  },

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