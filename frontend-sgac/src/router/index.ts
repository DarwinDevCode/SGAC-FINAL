import { createRouter, createWebHistory } from 'vue-router'
import ConvocatoriaList from '../components/ConvocatoriaList.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: ConvocatoriaList
    },
    {
      path: '/convocatorias',
      name: 'convocatorias',
      component: ConvocatoriaList
    }
  ]
})

export default router
