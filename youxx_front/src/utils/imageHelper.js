const imagesContext = require.context('../assets/products', true, /\.(png|jpe?g|gif|webp|svg)$/)

const productImages = {}
imagesContext.keys().forEach(key => {
  const fileName = key.replace(/^.*[\\/]/, '').replace(/\.[^.]+$/, '')
  productImages[fileName] = imagesContext(key)
})

export const getImageUrl = (imageName) => {
  return productImages[imageName] || ''
}

export const getImageUrlByPath = (path) => {
  if (!path) return ''
  
  if (path.startsWith('http://') || path.startsWith('https://') || path.startsWith('/')) {
    return path
  }
  
  const fileName = path.replace(/^.*[\\/]/, '').replace(/\.[^.]+$/, '')
  return productImages[fileName] || `/${path}`
}
