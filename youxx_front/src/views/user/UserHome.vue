<template>
  <div class="page-content shopping-content">
    <div class="banner-section">
      <el-carousel height="320px" :interval="4000" arrow="hover" @change="handleBannerChange">
        <el-carousel-item v-for="(item, index) in bannerList" :key="index">
          <div 
            class="banner-item" 
            :style="{ background: item.bgColor }"
            @mouseenter="handleBannerMouseEnter(index)"
            @mouseleave="handleBannerMouseLeave(index)"
          >
            <div class="banner-content">
              <div class="banner-text">
                <h2>{{ item.title }}</h2>
                <p :class="{ 'subtitle-visible': bannerHoverStates[index] }">{{ item.subtitle }}</p>
              </div>
              <div class="banner-character-wrapper">
                <BannerCartoonCharacter 
                  :type="item.characterType"
                  :open-mouth="bannerHoverStates[index]"
                />
              </div>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <div class="category-nav">
      <el-radio-group v-model="activeCategory" size="default">
        <el-radio-button label="all">全部商品</el-radio-button>
        <el-radio-button
          v-for="cat in categories"
          :key="cat.id"
          :label="cat.id"
        >
          <el-icon><component :is="getIconComponent(cat.icon)" /></el-icon>
          {{ cat.name }}
        </el-radio-button>
      </el-radio-group>
      <div class="nav-right">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索商品..."
          clearable
          class="search-input"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-badge :value="cartCount" :hidden="cartCount === 0" class="cart-badge">
          <el-button @click="$emit('openCart')">
            <el-icon><ShoppingCart /></el-icon>
            购物车
          </el-button>
        </el-badge>
      </div>
    </div>

    <div v-if="activeCategory === 'all' && !searchKeyword && activeFilterCount === 0" class="hot-section">
      <h3 class="section-title">
        <el-icon><Star /></el-icon>
        热销推荐
      </h3>
      <div class="products-grid">
        <div
          v-for="product in hotProductsFiltered"
          :key="product.id"
          class="product-card"
          @click="addToCart(product)"
        >
          <div class="product-image">
            <img v-if="product.image" :src="product.image" :alt="product.name" @error="handleImageError" />
            <div v-else class="image-placeholder">
              <el-icon><Goods /></el-icon>
            </div>
            <div v-if="product.discount < 0.99" class="discount-tag">
              {{ (product.discount * 10).toFixed(1) }}折
            </div>
          </div>
          <div class="product-info">
            <h4 class="product-name">{{ product.name }}</h4>
            <p class="product-desc">{{ product.description }}</p>
            <div class="product-price">
              <span class="current-price">¥{{ (product.price * product.discount).toFixed(2) }}</span>
              <span v-if="product.discount < 0.99" class="original-price">¥{{ product.price.toFixed(2) }}</span>
            </div>
            <div class="product-tags">
              <el-tag v-for="tag in product.tags" :key="tag" size="small" type="info">{{ tag }}</el-tag>
            </div>
          </div>
          <div class="product-action">
            <div class="action-buttons">
              <el-button type="primary" size="small" @click.stop="addToCart(product)">
                <el-icon><Plus /></el-icon>
                加入购物车
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="products-section">
      <h3 class="section-title">
        <el-icon><Shop /></el-icon>
        {{ currentCategoryName }}
      </h3>
      <div v-if="filteredProducts.length > 0" class="products-grid">
        <div
          v-for="product in filteredProducts"
          :key="product.id"
          class="product-card"
          @click="addToCart(product)"
        >
          <div class="product-image">
            <img v-if="product.image" :src="product.image" :alt="product.name" @error="handleImageError" />
            <div v-else class="image-placeholder">
              <el-icon><Goods /></el-icon>
            </div>
            <div v-if="product.discount < 0.99" class="discount-tag">
              {{ (product.discount * 10).toFixed(1) }}折
            </div>
          </div>
          <div class="product-info">
            <h4 class="product-name">{{ product.name }}</h4>
            <p class="product-desc">{{ product.description }}</p>
            <div class="product-price">
              <span class="current-price">¥{{ (product.price * product.discount).toFixed(2) }}</span>
              <span v-if="product.discount < 1" class="original-price">¥{{ product.price.toFixed(2) }}</span>
              <span class="unit">/{{ product.unit }}</span>
            </div>
            <div class="product-tags">
              <el-tag v-for="tag in product.tags" :key="tag" size="small" type="info">{{ tag }}</el-tag>
            </div>
          </div>
          <div class="product-action">
            <div class="action-buttons">
              <el-button type="primary" size="small" @click.stop="addToCart(product)">
                <el-icon><Plus /></el-icon>
                加入购物车
              </el-button>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无商品" />
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, onActivated } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Shop,
  ShoppingCart,
  Search,
  Star,
  Goods,
  Plus,
  Coffee,
  Sugar,
  Apple,
  MilkTea,
  Food
} from '@element-plus/icons-vue'
import { categories, getProducts, getHotProducts, getProductsByCategory } from '@/data/products.js'
import BannerCartoonCharacter from '@/components/BannerCartoonCharacter.vue'
import { shallowRef, triggerRef } from 'vue'

export default {
  name: 'UserHome',
  components: {
    Shop,
    ShoppingCart,
    Search,
    Star,
    Goods,
    Plus,
    Coffee,
    Sugar,
    Apple,
    MilkTea,
    Food,
    BannerCartoonCharacter
  },
  props: {
    cartCount: {
      type: Number,
      default: 0
    }
  },
  emits: ['openCart', 'addToCart'],
  setup(props, { emit }) {
    const activeCategory = ref('all')
    const searchKeyword = ref('')
    const priceRange = ref([0, 50])
    const selectedTags = ref([])
    
    const products = shallowRef(getProducts())

    const hotProducts = getHotProducts()

    // 图标映射表：将分类图标名称映射到对应的组件
    const iconMap = {
      'Coffee': Coffee,
      'Sugar': Sugar,
      'Goods': Goods,
      'Apple': Apple,
      'MilkTea': MilkTea,
      'Food': Food
    }

    /**
     * 获取图标组件
     * @param {string} iconName - 图标名称
     * @returns {Component} 图标组件
     */
    const getIconComponent = (iconName) => {
      return iconMap[iconName] || Goods
    }

    const activeBannerIndex = ref(0)
    const bannerHoverStates = ref([false, false, false, false])
    
    const bannerList = ref([
      {
        title: '新鲜直达',
        subtitle: '每日新鲜水果蔬菜，品质保证',
        bgColor: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
        characterType: 'purple'
      },
      {
        title: '限时特惠',
        subtitle: '精选商品，超值折扣',
        bgColor: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
        characterType: 'black'
      },
      {
        title: '健康饮品',
        subtitle: '多种健康饮品，清凉一夏',
        bgColor: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
        characterType: 'orange'
      },
      {
        title: '美味零食',
        subtitle: '追剧必备，美味不停',
        bgColor: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
        characterType: 'yellow'
      }
    ])
    
    const handleBannerChange = (index) => {
      activeBannerIndex.value = index
    }
    
    const handleBannerMouseEnter = (index) => {
      bannerHoverStates.value[index] = true
    }
    
    const handleBannerMouseLeave = (index) => {
      bannerHoverStates.value[index] = false
    }

    const allTags = computed(() => {
      const tags = new Set()
      products.forEach(p => p.tags.forEach(tag => tags.add(tag)))
      return Array.from(tags).sort()
    })

    const activeFilterCount = computed(() => {
      let count = 0
      if (priceRange.value[0] > 0 || priceRange.value[1] < 50) count++
      if (selectedTags.value.length > 0) count += selectedTags.value.length
      return count
    })

    const currentCategoryName = computed(() => {
      if (searchKeyword.value) return '搜索结果'
      if (activeCategory.value === 'all') return '全部商品'
      const cat = categories.find(c => c.id === activeCategory.value)
      return cat ? cat.name : '商品'
    })

    const hotProductsFiltered = computed(() => {
      return hotProducts.filter(p => p.status === 'onshelf')
    })

    const filteredProducts = computed(() => {
      let result = products.value
      
      // 分类筛选
      if (activeCategory.value !== 'all') {
        result = result.filter(p => p.category === activeCategory.value)
      }

      // 只显示上架的商品
      result = result.filter(p => p.status === 'onshelf')

      if (searchKeyword.value) {
        const keyword = searchKeyword.value.toLowerCase()
        result = result.filter(p =>
          p.name.toLowerCase().includes(keyword) ||
          p.description.toLowerCase().includes(keyword) ||
          p.tags.some(tag => tag.toLowerCase().includes(keyword))
        )
      }

      result = result.filter(p => {
        const finalPrice = p.price * p.discount
        return finalPrice >= priceRange.value[0] && finalPrice <= priceRange.value[1]
      })

      if (selectedTags.value.length > 0) {
        result = result.filter(p =>
          selectedTags.value.some(tag => p.tags.includes(tag))
        )
      }

      return result
    })

    const handleImageError = (e) => {
      e.target.style.display = 'none'
      e.target.nextElementSibling && (e.target.nextElementSibling.style.display = 'flex')
    }

    const addToCart = (product) => {
      emit('addToCart', product)
    }

    // 刷新商品数据
    const refreshProducts = () => {
      products.value = getProducts()
      triggerRef(products)
    }

    // 组件挂载时刷新数据
    onMounted(() => {
      refreshProducts()
    })

    // 组件激活时（从其他页面返回）刷新数据
    onActivated(() => {
      refreshProducts()
    })

    return {
      categories,
      products,
      hotProducts,
      hotProductsFiltered,
      bannerList,
      activeBannerIndex,
      bannerHoverStates,
      activeCategory,
      searchKeyword,
      allTags,
      priceRange,
      selectedTags,
      activeFilterCount,
      currentCategoryName,
      filteredProducts,
      getIconComponent,
      handleImageError,
      addToCart,
      handleBannerChange,
      handleBannerMouseEnter,
      handleBannerMouseLeave
    }
  }
}
</script>

<style scoped>
.shopping-content {
  width: 100%;
}

.banner-section {
  margin-bottom: 30px;
}

.banner-item {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.banner-content {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  padding: 0 50px;
  box-sizing: border-box;
  padding-bottom: 30px;
}

.banner-text {
  flex: 1;
  color: #fff;
  margin-bottom: 20px;
}

.banner-text h2 {
  font-size: 48px;
  font-weight: bold;
  margin: 0 0 15px 0;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.2);
  transition: all 0.4s ease;
}

.banner-text p {
  font-size: 20px;
  margin: 0;
  opacity: 0;
  max-height: 0;
  overflow: hidden;
  transition: all 0.4s ease;
}

.banner-text p.subtitle-visible {
  opacity: 0.95;
  max-height: 100px;
}

.banner-character-wrapper {
  flex: 0 0 auto;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  height: 280px;
}

.banner-section :deep(.el-carousel__arrow) {
  background-color: rgba(255, 255, 255, 0.8);
  color: #333;
  width: 50px;
  height: 50px;
  font-size: 20px;
}

.banner-section :deep(.el-carousel__arrow:hover) {
  background-color: #fff;
}

.banner-section :deep(.el-carousel__indicator--active .el-carousel__button) {
  background-color: #fff;
  width: 24px;
}

.category-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  padding: 18px 24px;
  background: linear-gradient(135deg, #f5faf7 0%, #e8f5ed 100%);
  border-radius: 12px;
  border: 1px solid #d4edda;
  box-shadow: 0 2px 12px rgba(66, 185, 131, 0.1);
  flex-wrap: wrap;
  gap: 15px;
}

.category-nav :deep(.el-radio-group) {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.category-nav :deep(.el-radio-button__inner) {
  border: none;
  background-color: #fff;
  color: #606266;
  border-radius: 8px;
  padding: 10px 20px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.category-nav :deep(.el-radio-button__inner:hover) {
  color: #42b983;
  background-color: #f0f9f4;
}

.category-nav :deep(.el-radio-button:first-child .el-radio-button__inner) {
  border-radius: 8px;
}

.category-nav :deep(.el-radio-button:last-child .el-radio-button__inner) {
  border-radius: 8px;
}

.category-nav :deep(.el-radio-button__orig-radio:checked + .el-radio-button__inner) {
  background: linear-gradient(135deg, #42b983 0%, #35a070 100%);
  color: #fff;
  box-shadow: 0 4px 12px rgba(66, 185, 131, 0.4);
}

.category-nav :deep(.el-radio-button__orig-radio:checked + .el-radio-button__inner:hover) {
  background: linear-gradient(135deg, #35a070 0%, #2d8f62 100%);
}

.category-nav :deep(.el-icon) {
  margin-right: 6px;
  font-size: 16px;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.search-input {
  width: 280px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  background-color: #fff;
}

.search-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 6px 16px rgba(66, 185, 131, 0.15);
  transform: translateY(-1px);
}

.search-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 3px rgba(66, 185, 131, 0.15), 0 6px 16px rgba(66, 185, 131, 0.2);
  transform: translateY(-1px);
}

.cart-badge {
  display: flex;
  align-items: center;
}

.cart-badge :deep(.el-button) {
  background: linear-gradient(135deg, #42b983 0%, #35a070 100%);
  border: none;
  border-radius: 8px;
  padding: 10px 24px;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(66, 185, 131, 0.3);
  transition: all 0.3s ease;
}

.cart-badge :deep(.el-button:hover) {
  background: linear-gradient(135deg, #35a070 0%, #2d8f62 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(66, 185, 131, 0.4);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 20px 0;
  color: #303133;
  font-size: 18px;
}

.section-title .el-icon {
  color: #42b983;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
}

.product-card {
  border: 1px solid #e0e0e0;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  animation: fadeIn 0.5s ease-out backwards;
}

.product-card:nth-child(1) { animation-delay: 0.05s; }
.product-card:nth-child(2) { animation-delay: 0.1s; }
.product-card:nth-child(3) { animation-delay: 0.15s; }
.product-card:nth-child(4) { animation-delay: 0.2s; }
.product-card:nth-child(5) { animation-delay: 0.25s; }
.product-card:nth-child(6) { animation-delay: 0.3s; }
.product-card:nth-child(7) { animation-delay: 0.35s; }
.product-card:nth-child(8) { animation-delay: 0.4s; }

.product-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 12px 32px rgba(66, 185, 131, 0.25);
  border-color: #42b983;
}

.product-image {
  width: 100%;
  height: 160px;
  background-color: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.product-image img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f0f2f5 0%, #e0e0e0 100%);
}

.image-placeholder .el-icon {
  font-size: 60px;
  color: #b0b0b0;
}

.discount-tag {
  position: absolute;
  top: 10px;
  right: 10px;
  background-color: #f56c6c;
  color: #fff;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
}

.product-info {
  padding: 15px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.product-name {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-desc {
  margin: 0 0 10px 0;
  font-size: 12px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-price {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 10px;
}

.current-price {
  font-size: 20px;
  font-weight: bold;
  color: #f56c6c;
}

.original-price {
  font-size: 12px;
  color: #c0c4cc;
  text-decoration: line-through;
}

.unit {
  font-size: 12px;
  color: #909399;
}

.product-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-bottom: 10px;
}

.product-action {
  padding: 0 15px 15px;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.action-buttons .el-button {
  flex: 1;
}

.action-buttons .el-button[type="primary"] {
  background-color: #42b983;
  border-color: #42b983;
}

.action-buttons .el-button[type="primary"]:hover {
  background-color: #35a070;
  border-color: #35a070;
}

.hot-section {
  margin-bottom: 40px;
}

.products-section {
  margin-bottom: 20px;
}
</style>