import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { title: '注册' }
  },
  // 首页：沿用原用户顶栏布局（搜索 / 分类 / 右上头像），主页面布局保持不变
  {
    path: '/',
    component: () => import('../layouts/UserLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('../views/Home.vue'),
        meta: { title: '首页' }
      }
    ]
  },
  // 商城工作台：左侧功能菜单（可纵向展开）+ 右侧对应功能页面
  {
    path: '/',
    component: () => import('../layouts/ShopLayout.vue'),
    children: [
      {
        path: 'buy',
        name: 'Buy',
        component: () => import('../views/Buy.vue'),
        meta: { title: '全部商品' }
      },
      {
        path: 'products/:id',
        name: 'ProductDetail',
        component: () => import('../views/ProductDetail.vue'),
        meta: { title: '商品详情' }
      },
      {
        path: 'my/cart',
        name: 'Cart',
        component: () => import('../views/Cart.vue'),
        meta: { title: '购物车', requiresAuth: true }
      },
      {
        path: 'publish',
        name: 'Publish',
        component: () => import('../views/PublishProduct.vue'),
        meta: { title: '发布商品', requiresAuth: true }
      },
      {
        path: 'my/products',
        name: 'MyProducts',
        component: () => import('../views/MyProducts.vue'),
        meta: { title: '我的商品', requiresAuth: true }
      },
      {
        path: 'my/orders',
        name: 'MyOrders',
        component: () => import('../views/MyOrders.vue'),
        meta: { title: '我的订单', requiresAuth: true }
      },
      {
        path: 'my/profile',
        name: 'Profile',
        component: () => import('../views/Profile.vue'),
        meta: { title: '个人中心', requiresAuth: true }
      }
    ]
  },
  {
    path: '/admin',
    component: () => import('../layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: '',
        name: 'AdminDashboard',
        component: () => import('../views/admin/Dashboard.vue'),
        meta: { title: '数据大盘' }
      },
      {
        path: 'products',
        name: 'AdminProducts',
        component: () => import('../views/admin/AdminProducts.vue'),
        meta: { title: '商品管理' }
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('../views/admin/AdminUsers.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'orders',
        name: 'AdminOrders',
        component: () => import('../views/admin/AdminOrders.vue'),
        meta: { title: '订单管理' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：未登录拦截、管理员路由隔离
router.beforeEach(async (to) => {
  const store = useUserStore()

  if (to.meta.requiresAuth && !store.isLogin) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  if (store.isLogin && !store.user) {
    try {
      await store.fetchMe()
    } catch {
      store.logout()
      return { path: '/login' }
    }
  }

  if (to.meta.requiresAdmin && !store.isAdmin) {
    return '/'
  }

  if (to.path === '/login' && store.isLogin) {
    return store.isAdmin ? '/admin' : '/'
  }

  return true
})

router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} - 黑猫优选` : '黑猫优选'
})

export default router