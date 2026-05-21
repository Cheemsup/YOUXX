const PRODUCTS_KEY = 'systemProducts'
const PRODUCTS_VERSION_KEY = 'systemProductsVersion'
const CURRENT_VERSION = 1

const imagesContext = require.context('../assets/products', true, /\.(png|jpe?g|gif|webp|svg)$/)

const productImages = {}
imagesContext.keys().forEach(key => {
  const fileName = key.replace(/^.*[\\/]/, '').replace(/\.[^.]+$/, '')
  productImages[fileName] = imagesContext(key)
})

const getImage = (imageName) => {
  return productImages[imageName] || ''
}

export const categories = [
  { id: 'drinks', name: '饮料', icon: 'Coffee' },
  { id: 'snacks', name: '零食', icon: 'Sugar' },
  { id: 'daily', name: '日用品', icon: 'Goods' },
  { id: 'fresh', name: '生鲜', icon: 'Apple' },
  { id: 'dairy', name: '乳制品', icon: 'MilkTea' },
  { id: 'instant', name: '速食', icon: 'Food' }
]

const defaultProducts = [
  {
    id: 'P001',
    name: '农夫山泉',
    category: 'drinks',
    price: 2.00,
    unit: '瓶',
    stock: 500,
    image: getImage('water'),
    description: '550ml 饮用天然水',
    barCode: '6920552655001',
    discount: 1,
    isHot: true,
    tags: ['热销', '解渴'],
    status: 'onshelf'
  },
  {
    id: 'P002',
    name: '可口可乐',
    category: 'drinks',
    price: 3.00,
    unit: '瓶',
    stock: 300,
    image: getImage('cola'),
    description: '330ml 可乐',
    barCode: '6920552655002',
    discount: 0.95,
    isHot: true,
    tags: ['热销', '碳酸'],
    status: 'onshelf'
  },
  {
    id: 'P003',
    name: '康师傅冰红茶',
    category: 'drinks',
    price: 3.50,
    unit: '瓶',
    stock: 200,
    image: getImage('iceTea'),
    description: '500ml 冰红茶',
    barCode: '6920552655003',
    discount: 1,
    isHot: false,
    tags: ['茶饮'],
    status: 'onshelf'
  },
  {
    id: 'P004',
    name: '统一绿茶',
    category: 'drinks',
    price: 3.00,
    unit: '瓶',
    stock: 180,
    image: getImage('greenTea'),
    description: '500ml 绿茶',
    barCode: '6920552655004',
    discount: 1,
    isHot: false,
    tags: ['茶饮', '健康'],
    status: 'onshelf'
  },
  {
    id: 'P005',
    name: '乐事薯片',
    category: 'snacks',
    price: 6.50,
    unit: '袋',
    stock: 150,
    image: getImage('chips'),
    description: '组合装 120g',
    barCode: '6920552655005',
    discount: 1,
    isHot: true,
    tags: ['热销', '膨化'],
    status: 'onshelf'
  },
  {
    id: 'P006',
    name: '奥利奥饼干',
    category: 'snacks',
    price: 7.80,
    unit: '盒',
    stock: 120,
    image: getImage('orio'),
    description: '巧克力味夹心饼干 388g',
    barCode: '6920552655006',
    discount: 0.9,
    isHot: false,
    tags: ['饼干', '甜点'],
    status: 'onshelf'
  },
  {
    id: 'P007',
    name: '卫龙辣条',
    category: 'snacks',
    price: 2.50,
    unit: '袋',
    stock: 200,
    image: getImage('lajiao'),
    description: '大面筋 65g',
    barCode: '6920552655007',
    discount: 1,
    isHot: true,
    tags: ['辣味', '童年'],
    status: 'onshelf'
  },
  {
    id: 'P008',
    name: '手抽纸巾',
    category: 'daily',
    price: 9.90,
    unit: '提',
    stock: 100,
    image: getImage('tissue'),
    description: '抽纸 3 层 120 抽*3 包',
    barCode: '6920552655008',
    discount: 1,
    isHot: false,
    tags: ['生活用纸'],
    status: 'onshelf'
  },
  {
    id: 'P009',
    name: '雕牌洗洁精',
    category: 'daily',
    price: 12.80,
    unit: '瓶',
    stock: 80,
    image: getImage('detergent'),
    description: '1.5kg 柠檬香型',
    barCode: '6920552655009',
    discount: 0.85,
    isHot: false,
    tags: ['清洁', '厨房'],
    status: 'onshelf'
  },
  {
    id: 'P010',
    name: '六神花露水',
    category: 'daily',
    price: 15.00,
    unit: '瓶',
    stock: 60,
    image: getImage('deet'),
    description: '195ml 驱蚊型',
    barCode: '6920552655010',
    discount: 1,
    isHot: false,
    tags: ['驱蚊', '夏季'],
    status: 'onshelf'
  },
  {
    id: 'P011',
    name: '新鲜鸡蛋',
    category: 'fresh',
    price: 15.00,
    unit: '盒',
    stock: 50,
    image: getImage('eggs'),
    description: '土鸡蛋 12 枚',
    barCode: '6920552655011',
    discount: 1,
    isHot: true,
    tags: ['生鲜', '营养'],
    status: 'onshelf'
  },
  {
    id: 'P012',
    name: '新鲜西红柿',
    category: 'fresh',
    price: 4.50,
    unit: '斤',
    stock: 40,
    image: getImage('tomato'),
    description: '500g 约 2-3 个',
    barCode: '6920552655012',
    discount: 1,
    isHot: false,
    tags: ['蔬菜', '新鲜'],
    status: 'onshelf'
  },
  {
    id: 'P013',
    name: '伊利纯牛奶',
    category: 'dairy',
    price: 12.00,
    unit: '箱',
    stock: 70,
    image: getImage('milk'),
    description: '250ml*12 盒',
    barCode: '6920552655013',
    discount: 0.95,
    isHot: true,
    tags: ['牛奶', '补钙'],
    status: 'onshelf'
  },
  {
    id: 'P014',
    name: '蒙牛酸奶',
    category: 'dairy',
    price: 15.80,
    unit: '箱',
    stock: 45,
    image: getImage('yogurt'),
    description: '100g*12 杯',
    barCode: '6920552655014',
    discount: 1,
    isHot: false,
    tags: ['酸奶', '益生菌'],
    status: 'onshelf'
  },
  {
    id: 'P015',
    name: '康师傅红烧牛肉面',
    category: 'instant',
    price: 4.50,
    unit: '袋',
    stock: 200,
    image: getImage('noodles'),
    description: '方便面 103g',
    barCode: '6920552655015',
    discount: 1,
    isHot: true,
    tags: ['速食', '泡面'],
    status: 'onshelf'
  },
  {
    id: 'P016',
    name: '统一老坛酸菜面',
    category: 'instant',
    price: 4.50,
    unit: '袋',
    stock: 180,
    image: getImage('suancai'),
    description: '方便面 105g',
    barCode: '6920552655016',
    discount: 0.9,
    isHot: false,
    tags: ['速食', '泡面', '酸菜'],
    status: 'onshelf'
  },
  {
    id: 'P017',
    name: '桂格燕麦片',
    category: 'instant',
    price: 22.00,
    unit: '袋',
    stock: 60,
    image: getImage('oat'),
    description: '即食燕麦片 1kg',
    barCode: '6920552655017',
    discount: 1,
    isHot: false,
    tags: ['早餐', '健康'],
    status: 'onshelf'
  },
  {
    id: 'P018',
    name: '农夫果园',
    category: 'drinks',
    price: 4.20,
    unit: '瓶',
    stock: 100,
    image: getImage('juice'),
    description: '100% 果汁 450ml',
    barCode: '6920552655018',
    discount: 1,
    isHot: false,
    tags: ['果汁', '健康'],
    status: 'onshelf'
  }
]

export const getProducts = () => {
  const savedVersion = localStorage.getItem(PRODUCTS_VERSION_KEY)
  const productsStr = localStorage.getItem(PRODUCTS_KEY)

  if (productsStr && savedVersion === String(CURRENT_VERSION)) {
    return JSON.parse(productsStr)
  }

  saveProducts(defaultProducts)
  localStorage.setItem(PRODUCTS_VERSION_KEY, String(CURRENT_VERSION))
  return defaultProducts
}

export const saveProducts = (products) => {
  localStorage.setItem(PRODUCTS_KEY, JSON.stringify(products))
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

export const resetProducts = () => {
  saveProducts(defaultProducts)
  localStorage.setItem(PRODUCTS_VERSION_KEY, String(CURRENT_VERSION))
  return defaultProducts
}
