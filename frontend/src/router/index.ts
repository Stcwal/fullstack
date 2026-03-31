import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/',
      redirect: '/dashboard'
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: () => import('@/views/DashboardView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/fryser',
      name: 'fryser',
      component: () => import('@/views/FreezerView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/kjoeleskap',
      name: 'kjoeleskap',
      component: () => import('@/views/FridgeView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/generelt',
      name: 'generelt',
      component: () => import('@/views/ChecklistView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/avvik',
      name: 'avvik',
      component: () => import('@/views/DeviationsView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/grafer',
      name: 'grafer',
      component: () => import('@/views/GraphView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/opplaering',
      name: 'opplaering',
      component: () => import('@/views/TrainingView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/alkohol',
      component: () => import('@/views/alkohol/AlkoholView.vue'),
      meta: { requiresAuth: true },
      children: [
        { path: '', redirect: 'alderskontroll' },
        {
          path: 'alderskontroll',
          name: 'alkohol-alderskontroll',
          component: () => import('@/views/alkohol/AlderskontrollTab.vue')
        },
        {
          path: 'sjekklister',
          name: 'alkohol-sjekklister',
          component: () => import('@/views/alkohol/AlkoholSjekklisterTab.vue')
        },
        {
          path: 'hendelser',
          name: 'alkohol-hendelser',
          component: () => import('@/views/alkohol/HendelsesloggTab.vue')
        }
      ]
    },
    {
      path: '/innstillinger',
      component: () => import('@/views/settings/SettingsView.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
      children: [
        { path: '', redirect: 'enheter' },
        {
          path: 'enheter',
          name: 'settings-units',
          component: () => import('@/views/settings/UnitsTab.vue')
        },
        {
          path: 'brukere',
          name: 'settings-users',
          component: () => import('@/views/settings/UsersTab.vue')
        },
        {
          path: 'org',
          name: 'settings-org',
          component: () => import('@/views/settings/OrgTab.vue')
        }
      ]
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/dashboard'
    }
  ]
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  const requiresAuth = to.meta.requiresAuth !== false

  if (requiresAuth && !auth.isAuthenticated) {
    return { name: 'login' }
  }

  if (to.name === 'login' && auth.isAuthenticated) {
    return { name: 'dashboard' }
  }

  if (to.meta.requiresAdmin && auth.user?.role !== 'ADMIN') {
    return { name: 'dashboard' }
  }
})

export default router
