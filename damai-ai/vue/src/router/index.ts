import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'Home',
      component: () => import('../views/Home.vue')
    },
    {
      path: '/damai-ai',
      name: 'DaMaiAI',
      component: () => import('../views/DaMaiAi.vue')
    },
    {
      path: '/damai-rag',
      name: 'SmartRag',
      component: () => import('../views/SmartRag.vue')
    }
  ],
})

export default router
