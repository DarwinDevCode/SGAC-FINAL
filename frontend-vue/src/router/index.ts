import { createRouter, createWebHistory } from 'vue-router'
import CustomerList from '../components/CustomerList.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: CustomerList
    },
    {
      path: '/customers',
      name: 'customers',
      component: CustomerList
    }
  ]
})

export default router
