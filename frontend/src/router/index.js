import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/',
      redirect: '/overview'
    },
    {
      path: '/dashboard',
      redirect: '/overview'
    },
    {
      path: '/main',
      component: () => import('@/views/MainLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: 'overview',
          name: 'Overview',
          component: () => import('@/views/Overview.vue')
        },
        {
          path: 'simulation',
          name: 'Simulation',
          component: () => import('@/views/Simulation.vue')
        },
        {
          path: 'trace/batch',
          name: 'BatchQuery',
          component: () => import('@/views/BatchQuery.vue')
        },
        {
          path: 'trace/chain',
          name: 'TraceChain',
          component: () => import('@/views/TraceChain.vue')
        },
        {
          path: 'alert/list',
          name: 'AlertList',
          component: () => import('@/views/AlertList.vue')
        },
        {
          path: 'alert/dashboard',
          name: 'AlertDashboard',
          component: () => import('@/views/AlertDashboard.vue')
        }
      ]
    },
    {
      path: '/overview',
      redirect: '/main/overview'
    },
    {
      path: '/simulation',
      redirect: '/main/simulation'
    },
    {
      path: '/trace/batch',
      redirect: '/main/trace/batch'
    },
    {
      path: '/trace/chain',
      redirect: '/main/trace/chain'
    },
    {
      path: '/alert/list',
      redirect: '/main/alert/list'
    },
    {
      path: '/alert/dashboard',
      redirect: '/main/alert/dashboard'
    }
  ]
})

router.beforeEach((to) => {
  const userStore = useUserStore()

  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    return '/login'
  }

  if (to.name === 'Login' && userStore.isLoggedIn) {
    return '/main/overview'
  }
})

export default router
