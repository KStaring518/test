import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/Home.vue')
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/Login.vue')
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/Register.vue')
    },
    {
      path: '/products',
      name: 'products',
      component: () => import('@/views/Products.vue')
    },
    {
      path: '/product/:id',
      name: 'product-detail',
      component: () => import('@/views/ProductDetail.vue')
    },
    {
      path: '/cart',
      name: 'cart',
      component: () => import('@/views/Cart.vue')
    },
    {
      path: '/orders',
      name: 'orders',
      component: () => import('@/views/Orders.vue')
    },
    {
      path: '/order/:id',
      name: 'order-detail',
      component: () => import('@/views/OrderDetail.vue')
    },
    {
      path: '/checkout',
      name: 'Checkout',
      component: () => import('@/views/Checkout.vue'),
      meta: { requiresAuth: true, role: ['USER', 'MERCHANT'] }
    },
    {
      path: '/payment/:id',
      name: 'Payment',
      component: () => import('@/views/Payment.vue'),
      meta: { requiresAuth: true, role: ['USER', 'MERCHANT'] }
    },
    {
      path: '/payment',
      name: 'PaymentQuery',
      component: () => import('@/views/Payment.vue'),
      meta: { requiresAuth: true, role: ['USER', 'MERCHANT'] }
    },
    {
      path: '/address',
      name: 'address',
      component: () => import('@/views/Address.vue')
    },
    {
      path: '/account',
      name: 'account',
      component: () => import('@/views/user/UserCenter.vue'),
      meta: { requiresAuth: true, role: ['USER', 'MERCHANT'] }
    },
    {
      path: '/favorites',
      name: 'favorites',
      component: () => import('@/views/user/Favorites.vue'),
      meta: { requiresAuth: true, role: ['USER', 'MERCHANT'] }
    },
    // 商家相关路由
    {
      path: '/merchant',
      name: 'merchant-dashboard',
      component: () => import('@/views/merchant/MerchantDashboard.vue'),
      meta: { requiresAuth: true, role: 'MERCHANT' }
    },
    {
      path: '/merchant/products',
      name: 'merchant-products',
      component: () => import('@/views/merchant/MerchantProducts.vue'),
      meta: { requiresAuth: true, role: 'MERCHANT' }
    },
    {
      path: '/merchant/settings',
      name: 'merchant-settings',
      component: () => import('@/views/merchant/MerchantSettings.vue'),
      meta: { requiresAuth: true, role: 'MERCHANT' }
    },
    {
      path: '/merchant/orders',
      name: 'merchant-orders',
      component: () => import('@/views/merchant/MerchantOrders.vue'),
      meta: { requiresAuth: true, role: 'MERCHANT' }
    },
    {
      path: '/merchant/orders/:id',
      name: 'merchant-order-detail',
      component: () => import('@/views/merchant/MerchantOrderDetail.vue'),
      meta: { requiresAuth: true, role: 'MERCHANT' }
    },
    
    {
      path: '/merchant-register',
      name: 'MerchantRegister',
      component: () => import('@/views/MerchantRegister.vue'),
      meta: { requiresAuth: true }
    },
    // 管理员相关路由
    {
      path: '/admin',
      name: 'admin-dashboard',
      component: () => import('@/views/admin/AdminDashboard.vue'),
      meta: { requiresAuth: true, role: 'ADMIN' }
    },
    {
      path: '/admin/users',
      name: 'admin-users',
      component: () => import('@/views/admin/AdminUsers.vue'),
      meta: { requiresAuth: true, role: 'ADMIN' }
    },
    {
      path: '/admin/merchants',
      name: 'admin-merchants',
      component: () => import('@/views/admin/AdminMerchants.vue'),
      meta: { requiresAuth: true, role: 'ADMIN' }
    },
    {
      path: '/admin/products',
      name: 'admin-products',
      component: () => import('@/views/admin/AdminCategories.vue'),
      meta: { requiresAuth: true, role: 'ADMIN' }
    },
    {
      path: '/admin/orders',
      name: 'admin-orders',
      component: () => import('@/views/admin/AdminOrders.vue'),
      meta: { requiresAuth: true, role: 'ADMIN' }
    },
    {
      path: '/admin/system',
      name: 'admin-system',
      component: () => import('@/views/admin/AdminSystem.vue'),
      meta: { requiresAuth: true, role: 'ADMIN' }
    },
    {
      path: '/admin/banners',
      name: 'admin-banners',
      component: () => import('@/views/admin/AdminBanners.vue'),
      meta: { requiresAuth: true, role: 'ADMIN' }
    }
  ]
})

export default router

// 路由守卫：鉴权与角色控制
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  const userStr = localStorage.getItem('user')
  let role: string | null = null
  if (userStr) {
    try { role = JSON.parse(userStr).role || null } catch { role = null }
  }

  const requiresAuth = (to.meta as any)?.requiresAuth
  const requiredRole = (to.meta as any)?.role as string | string[] | undefined

  if (requiresAuth && !token) {
    next({ name: 'login', query: { redirect: to.fullPath } })
    return
  }

  if (requiredRole && role) {
    const allowed = Array.isArray(requiredRole) ? requiredRole.includes(role) : requiredRole === role
    if (!allowed) {
      next({ name: 'home' })
      return
    }
  }

  next()
})