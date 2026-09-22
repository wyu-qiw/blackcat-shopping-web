import request from './request'

// 认证接口
export const authApi = {
  login: (data) => request.post('/auth/login', data),
  register: (data) => request.post('/auth/register', data),
  me: () => request.get('/auth/me')
}

// 用户端商品接口
export const productApi = {
  list: (keyword) => request.get('/products', { params: { keyword } }),
  detail: (id) => request.get(`/products/${id}`),
  publish: (data) => request.post('/products', data),
  mine: () => request.get('/products/mine'),
  update: (id, data) => request.put(`/products/${id}`, data),
  toggleListed: (id, listed) => request.put(`/products/${id}/listed`, { listed }),
  remove: (id) => request.delete(`/products/${id}`),
  comments: (id) => request.get(`/products/${id}/comments`),
  addComment: (id, content) => request.post(`/products/${id}/comments`, { content })
}

// 用户端订单接口
export const orderApi = {
  create: (productId) => request.post('/orders', { productId }),
  mine: () => request.get('/orders/mine'),
  cancel: (id) => request.put(`/orders/${id}/cancel`),
  refund: (id) => request.put(`/orders/${id}/refund`)
}

// 用户端购物车接口
export const cartApi = {
  list: () => request.get('/cart'),
  add: (productId) => request.post('/cart/add', { productId }),
  remove: (productId) => request.delete(`/cart/${productId}`),
  clear: () => request.delete('/cart'),
  checkout: (productIds) => request.post('/cart/checkout', { productIds })
}

// 管理员接口
export const adminApi = {
  stats: () => request.get('/admin/stats'),
  products: (params) => request.get('/admin/products', { params }),
  // 管理员编辑任意商品：复用用户端更新接口（后端 checkOwnerOrAdmin 对管理员放行）
  productUpdate: (id, data) => request.put(`/products/${id}`, data),
  updateProductStatus: (id, status) => request.put(`/admin/products/${id}/status`, { status }),
  updateProductListed: (id, listed) => request.put(`/admin/products/${id}/listed`, { listed }),
  moveCategory: (id, categoryId) => request.put(`/admin/products/${id}/category`, { categoryId }),
  moveBrand: (id, brandId) => request.put(`/admin/products/${id}/brand`, { brandId }),
  productsRemove: (id) => request.delete(`/products/${id}`),
  users: () => request.get('/admin/users'),
  orders: () => request.get('/admin/orders'),
  handleRefund: (id, approved) => request.put(`/admin/orders/${id}/refund`, { approved }),
  cancelOrder: (id) => request.put(`/admin/orders/${id}/cancel`),
  deleteOrder: (id) => request.delete(`/admin/orders/${id}`),
  productSales: (limit = 8) => request.get('/admin/stats/product-sales', { params: { limit } }),
  salesTrend: (days = 7) => request.get('/admin/stats/sales-trend', { params: { days } }),
  categoryDistribution: () => request.get('/admin/stats/category-distribution')
}

// 商品分类
export const categoryApi = {
  list: () => request.get('/categories')
}

// 品牌
export const brandApi = {
  list: () => request.get('/brands')
}

// 个人中心：修改信息 / 密码（每月限额）
export const userApi = {
  updateProfile: (data) => request.put('/users/profile', data),
  changePassword: (data) => request.put('/users/password', data),
  profileLimit: () => request.get('/users/profile/limit')
}

// 图片上传
export function uploadFile(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/files/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// AI 能力：助手对话 / 商品描述生成（Key 只配置在后端）
export const aiApi = {
  chat: (message, history) => request.post('/ai/chat', { message, history }),
  describe: (data) => request.post('/ai/describe', data)
}
