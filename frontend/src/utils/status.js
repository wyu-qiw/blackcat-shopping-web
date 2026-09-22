// 商品状态与订单状态的前端展示映射
export const productStatusMap = {
  ONSALE: { label: '可售', type: 'success' },
  RESERVED: { label: '已预购', type: 'warning' },
  OUT_OF_STOCK: { label: '无库存', type: 'info' }
}

export const productStatusOptions = Object.entries(productStatusMap).map(([value, item]) => ({
  value,
  label: item.label
}))

export const orderStatusMap = {
  PAID: { label: '已支付', type: 'success' },
  CANCELLED: { label: '已取消', type: 'info' },
  REFUNDING: { label: '退款中', type: 'warning' },
  REFUNDED: { label: '已退款', type: 'danger' }
}

export function productStatusTag(status) {
  return productStatusMap[status] || { label: status || '未知', type: 'info' }
}

export function orderStatusTag(status) {
  return orderStatusMap[status] || { label: status || '未知', type: 'info' }
}
