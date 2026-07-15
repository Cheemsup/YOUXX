// 图片资源统一由后端 /upload_resources/** 静态服务提供，前端不再打包商品图片，直接透传后端返回的路径
export const getImageUrl = (imagePath) => imagePath || ''

export const getImageUrlByPath = (path) => {
  if (!path) return ''
  // 已是绝对 URL 或后端静态路径，原样返回
  if (path.startsWith('http://') || path.startsWith('https://') || path.startsWith('/')) {
    return path
  }
  // 兜底：历史相对路径补成 /upload_resources/products/ 前缀
  return '/upload_resources/products/' + path.replace(/^.*?products\//, '')
}
