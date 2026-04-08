import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/preferences'
  },
  {
    path: '/accounts',
    name: 'Accounts',
    component: () => import('../views/AccountManagement.vue')
  },
  {
    path: '/accounts/deleted',
    name: 'DeletedAccounts',
    component: () => import('../views/DeletedAccounts.vue')
  },
  {
    path: '/preferences',
    name: 'Preferences',
    component: () => import('../views/PreferenceManagement.vue')
  },
  {
    path: '/preferences/deleted',
    name: 'DeletedPreferences',
    component: () => import('../views/DeletedPreferences.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
