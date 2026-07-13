const PRODUCTS_KEY = 'systemProducts'
const PRODUCTS_VERSION_KEY = 'systemProductsVersion'
const CATEGORIES_KEY = 'systemCategories'

export const getProducts = () => {
  const productsStr = localStorage.getItem(PRODUCTS_KEY)
  if (productsStr) {
    try {
      return JSON.parse(productsStr)
    } catch (e) {
      console.error('解析商品数据失败', e)
    }
  }
  return []
}

export const saveProducts = (products) => {
  localStorage.setItem(PRODUCTS_KEY, JSON.stringify(products))
}

export const getCategories = () => {
  const categoriesStr = localStorage.getItem(CATEGORIES_KEY)
  if (categoriesStr) {
    try {
      return JSON.parse(categoriesStr)
    } catch (e) {
      console.error('解析分类数据失败', e)
    }
  }
  return []
}

export const saveCategories = (categories) => {
  localStorage.setItem(CATEGORIES_KEY, JSON.stringify(categories))
}

export const addProduct = (product) => {
  const products = getProducts()
  const existing = products.find(p => p.id === product.id)
  if (existing) {
    return false
  }
  products.push(product)
  saveProducts(products)
  return true
}

export const updateProduct = (productId, updates) => {
  const products = getProducts()
  const index = products.findIndex(p => p.id === productId)
  if (index === -1) {
    return false
  }
  products[index] = { ...products[index], ...updates }
  saveProducts(products)
  return true
}

export const deleteProduct = (productId) => {
  const products = getProducts()
  const filtered = products.filter(p => p.id !== productId)
  if (filtered.length === products.length) {
    return false
  }
  saveProducts(filtered)
  return true
}

export const getProductById = (id) => {
  const products = getProducts()
  return products.find(p => p.id === id)
}

export const getProductsByCategory = (categoryId) => {
  const products = getProducts()
  if (!categoryId || categoryId === 'all') {
    return products
  }
  return products.filter(p => p.category === categoryId)
}

export const getHotProducts = () => {
  const products = getProducts()
  return products.filter(p => p.isHot)
}
