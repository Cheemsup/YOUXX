<!--
 * @Description: 产品管理页面 - 管理商品的上架、下架、添加等功能
 * @Author: Admin
 * @Date: 2026-04-12
-->

<template>
  <!-- 页面主体容器 -->
  <div class="page-content">
    <!-- 页面头部：包含标题和操作按钮 -->
    <div class="page-header">
      <h2>产品管理</h2>
      <!-- 头部操作区域：添加商品按钮 -->
      <div class="header-actions">
        <el-button type="primary" @click="showAddProductDialog">
          <el-icon><Plus /></el-icon>
          <span>添加商品</span>
        </el-button>
      </div>
    </div>
    
    <!-- 筛选栏：搜索、分类、状态筛选 -->
    <div class="filter-bar">
      <!-- 筛选栏左侧：搜索框、下拉选择等 -->
      <div class="filter-left">
        <!-- 搜索输入框 -->
        <div class="filter-item">
          <label>搜索：</label>
          <el-input
            v-model="productSearch"
            placeholder="请输入产品名称或 ID"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
        
        <!-- 分类选择器 -->
        <div class="filter-item">
          <label>分类：</label>
          <el-select
            v-model="productCategory"
            placeholder="请选择分类"
            clearable
          >
            <el-option label="全部" value="" />
            <el-option
              v-for="cat in categories"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
        </div>
        
        <!-- 状态筛选按钮组 -->
        <div class="filter-item">
          <label>状态：</label>
          <el-button-group class="status-button-group">
            <!-- 全部状态按钮 -->
            <el-button 
              :type="productStatus === '' ? 'primary' : 'default'"
              @click="productStatus = ''"
              :plain="productStatus !== ''"
            >
              <el-icon><CircleCheckFilled /></el-icon>
              <span>全部</span>
            </el-button>
            <!-- 上架中状态按钮 -->
            <el-button 
              :type="productStatus === 'onshelf' ? 'success' : 'default'"
              @click="productStatus = 'onshelf'"
              :plain="productStatus !== 'onshelf'"
            >
              <el-icon><UploadFilled /></el-icon>
              <span>上架中</span>
            </el-button>
            <!-- 已下架状态按钮 -->
            <el-button 
              :type="productStatus === 'offshelf' ? 'danger' : 'default'"
              @click="productStatus = 'offshelf'"
              :plain="productStatus !== 'offshelf'"
            >
              <el-icon><DeleteFilled /></el-icon>
              <span>已下架</span>
            </el-button>
          </el-button-group>
        </div>
      </div>
      
      <!-- 筛选栏右侧：视图切换按钮 -->
      <div class="filter-right">
        <el-button-group>
          <!-- 大图模式按钮 -->
          <el-button :type="viewMode === 'large' ? 'primary' : 'default'" @click="viewMode = 'large'">
            <el-icon><Grid /></el-icon>
          </el-button>
          <!-- 列表模式按钮 -->
          <el-button :type="viewMode === 'small' ? 'primary' : 'default'" @click="viewMode = 'small'">
            <el-icon><List /></el-icon>
          </el-button>
        </el-button-group>
      </div>
    </div>
    
    <!-- 分类导航栏：快速切换商品分类 -->
    <div class="category-bar">
      <div class="category-scroll">
        <!-- 全部分类按钮 -->
        <div 
          class="category-item" 
          :class="{ active: productCategory === '' }"
          @click="productCategory = ''"
        >
          <el-icon class="category-icon"><Goods /></el-icon>
          <span class="category-text">全部</span>
        </div>
        <!-- 动态渲染各个分类 -->
        <div 
          v-for="cat in categories" 
          :key="cat.id"
          class="category-item"
          :class="{ active: productCategory === cat.id }"
          @click="productCategory = cat.id"
        >
          <el-icon class="category-icon"><component :is="getIconComponent(cat.icon)" /></el-icon>
          <span class="category-text">{{ cat.name }}</span>
        </div>
      </div>
      <div class="batch-actions-container">
        <el-button 
          type="success" 
          @click="batchShelf('onshelf')"
          :loading="batchLoading"
          size="small"
        >
          <el-icon><UploadFilled /></el-icon>
          <span>一键上架</span>
        </el-button>
        <el-button 
          type="warning" 
          @click="batchShelf('offshelf')"
          :loading="batchLoading"
          size="small"
        >
          <el-icon><DeleteFilled /></el-icon>
          <span>一键下架</span>
        </el-button>
      </div>
    </div>
    <div class="product-grid" :class="{ 'small-mode': viewMode === 'small' }">
      <div 
        v-for="product in filteredProducts" 
        :key="product.id" 
        class="product-card"
        :class="{ 'offshelf': product.status === 'offshelf' }"
      >
        <div class="product-image">
          <img v-if="product.image" :src="product.image" :alt="product.name" />
          <el-icon v-else><Goods /></el-icon>
        </div>
        <div class="product-info">
          <div class="product-id">ID: {{ product.id }}</div>
          <div class="product-name">{{ product.name }}</div>
          <div class="product-price-wrapper">
            <div v-if="product.discount < 1" class="product-discount-price">¥{{ (product.price * product.discount).toFixed(2) }}</div>
            <div v-else class="product-price">¥{{ product.price }}/{{ product.unit }}</div>
            <div v-if="product.discount < 1" class="product-original-price">¥{{ product.price }}/{{ product.unit }}</div>
            <el-tag v-if="product.discount < 1" type="danger" size="mini" class="discount-tag">{{ (product.discount * 10).toFixed(1) }}折</el-tag>
          </div>
          <div class="product-stock">
            <el-tag :type="product.stock > 50 ? 'success' : product.stock > 20 ? 'warning' : 'danger'" size="small">
              库存: {{ product.stock }}
            </el-tag>
          </div>
          <div class="product-category">
            <el-tag type="info" size="small">
              {{ getCategoryName(product.category) }}
            </el-tag>
          </div>
        </div>
        <div class="product-actions">
          <el-button size="small" type="primary" @click="showProductDetail(product)">详情</el-button>
          <el-button 
            v-if="product.status !== 'offshelf'"
            size="small" 
            type="success" 
            @click="toggleProductStatus(product, 'offshelf')"
          >
            下架
          </el-button>
          <el-button 
            v-else
            size="small" 
            type="warning" 
            @click="toggleProductStatus(product, 'onshelf')"
          >
            上架
          </el-button>
        </div>
      </div>
    </div>

    <el-dialog v-model="detailDialogVisible" title="产品详情" width="700px" class="product-detail-dialog">
      <div v-if="selectedProduct" class="product-detail">
        <div class="detail-layout">
          <div class="detail-left-column">
            <div class="detail-image">
              <img v-if="selectedProduct.image" :src="selectedProduct.image" :alt="selectedProduct.name" />
              <el-icon v-else class="no-image-icon"><Goods /></el-icon>
            </div>
            
            <!-- 折扣管理区域 -->
            <div class="discount-management">
              <div class="discount-content">
                <div class="discount-input-group">
                  <el-input-number 
                    v-model="tempDiscountDisplay" 
                    :min="0" 
                    :max="10" 
                    :step="1"
                    @change="handleDiscountInputChange"
                    class="discount-input"
                    size="small"
                    controls-position="right"
                  />
                  <span class="discount-unit">折</span>
                </div>
                <div class="discount-price-display" v-if="tempDiscount < 1">
                  <span class="price-label">折后价</span>
                  <span class="price-value">¥{{ (selectedProduct.price * tempDiscount).toFixed(2) }}</span>
                  <span class="price-original">¥{{ selectedProduct.price.toFixed(2) }}</span>
                </div>
              </div>
              <div class="discount-button-bar">
                <el-button 
                  v-if="tempDiscount !== 0" 
                  type="danger" 
                  size="small" 
                  @click="removeDiscount"
                  class="remove-discount-btn"
                >
                  移除
                </el-button>
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="saveDiscount" 
                  :disabled="tempDiscount === selectedProduct.discount"
                  class="save-discount-btn"
                >
                  保存
                </el-button>
              </div>
            </div>
          </div>
          
          <div class="detail-info">
            <div class="detail-row">
              <div class="detail-item">
                <span class="detail-label">产品ID</span>
                <span class="detail-value">{{ selectedProduct.id }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">产品名称</span>
                <span class="detail-value">{{ selectedProduct.name }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">分类</span>
                <el-tag type="info" size="small">{{ getCategoryName(selectedProduct.category) }}</el-tag>
              </div>
            </div>
            <div class="detail-row">
              <div class="detail-item">
                <span class="detail-label">价格</span>
                <span class="detail-value price">¥{{ selectedProduct.price }}/{{ selectedProduct.unit }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">库存</span>
                <el-tag :type="selectedProduct.stock > 50 ? 'success' : selectedProduct.stock > 20 ? 'warning' : 'danger'" size="small">
                  {{ selectedProduct.stock }} {{ selectedProduct.unit }}
                </el-tag>
              </div>
              <div class="detail-item">
                <span class="detail-label">折扣</span>
                <el-tag :type="selectedProduct.discount < 1 ? 'warning' : 'info'" size="small">
                  {{ selectedProduct.discount < 1 ? (selectedProduct.discount * 10).toFixed(1) + '折' : '无折扣' }}
                </el-tag>
              </div>
            </div>
            <div class="detail-row">
              <div class="detail-item" v-if="selectedProduct.barCode">
                <span class="detail-label">条码</span>
                <span class="detail-value">{{ selectedProduct.barCode }}</span>
              </div>
              <div class="detail-item" v-if="selectedProduct.description" :span="2">
                <span class="detail-label">描述</span>
                <span class="detail-value">{{ selectedProduct.description }}</span>
              </div>
            </div>
            <div class="detail-row tags-row" v-if="selectedProduct.tags && selectedProduct.tags.length">
              <div class="detail-item full-width">
                <span class="detail-label center-label">标签</span>
                <div class="detail-tags centered">
                  <el-tag v-for="tag in selectedProduct.tags" :key="tag" size="small">
                    {{ tag }}
                  </el-tag>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog 
      v-model="addProductDialogVisible" 
      title="添加商品" 
      width="600px"
      class="add-product-dialog"
    >
      <el-form :model="newProductForm" label-width="100px" class="product-form">
        <el-form-item label="商品ID" required>
          <el-input v-model="newProductForm.id" placeholder="如：P019" />
        </el-form-item>
        <el-form-item label="商品名称" required>
          <el-input v-model="newProductForm.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="商品分类" required>
          <el-select v-model="newProductForm.category" placeholder="请选择分类" style="width: 100%">
            <el-option
              v-for="cat in categories"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="商品图片">
          <div class="image-upload-wrapper">
            <el-upload
              v-if="!newProductForm.imageUrl"
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleImageUpload"
              accept="image/*"
              class="image-uploader"
              :disabled="newProductForm.imageUploading"
            >
              <div class="upload-placeholder">
                <el-icon class="upload-icon"><Upload /></el-icon>
                <span class="upload-text">{{ newProductForm.imageUploading ? '上传中...' : (newProductForm.category ? '点击上传图片' : '请先选择商品分类再上传图片') }}</span>
              </div>
            </el-upload>
            <div v-else class="image-preview">
              <img :src="newProductForm.imageUrl" alt="商品图片预览" />
              <div class="image-preview-overlay" @click="removeImage">
                <el-icon><DeleteFilled /></el-icon>
              </div>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="商品价格" required>
          <el-input-number v-model="newProductForm.price" :min="0" :precision="2" :step="0.1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="计量单位" required>
          <el-input v-model="newProductForm.unit" placeholder="如：瓶、袋、盒" />
        </el-form-item>
        <el-form-item label="库存数量" required>
          <el-input-number v-model="newProductForm.stock" :min="0" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="商品描述">
          <el-input 
            v-model="newProductForm.description" 
            type="textarea" 
            :rows="3"
            placeholder="请输入商品描述" 
          />
        </el-form-item>
        <el-form-item label="条码">
          <el-input v-model="newProductForm.barCode" placeholder="请输入商品条码" />
        </el-form-item>
        <el-form-item label="商品标签">
          <el-input v-model="newProductForm.tagsInput" placeholder="请输入标签，用逗号分隔" />
        </el-form-item>
        <el-form-item label="上架状态">
          <el-switch v-model="newProductForm.onshelf" active-text="上架" inactive-text="下架" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addProductDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddProduct">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
/**
 * 产品管理页面组件
 * 功能：商品列表展示、筛选、上下架管理、添加新商品
 */
import { ref, computed, shallowRef, triggerRef, onMounted } from 'vue'
import { Goods, Search, Grid, List, CircleCheckFilled, UploadFilled, DeleteFilled, Coffee, Sugar, Apple, MilkTea, Food, Plus, Discount, Upload, PictureFilled } from '@element-plus/icons-vue'
import { listProductsApi, listCategoriesApi, addProductApi, uploadProductImageApi, updateProductStatusApi, updateBatchProductStatusApi, updateProductDiscountApi } from '@/services/api.js'
import { getImageUrlByPath } from '@/utils/imageHelper.js'
import { saveProducts, saveCategories } from '@/data/products.js'
import { ElMessage } from 'element-plus'

export default {
  name: 'DashboardProduct',
  components: { 
    Goods, Search, Grid, List, 
    CircleCheckFilled, UploadFilled, DeleteFilled,
    Coffee, Sugar, Apple, MilkTea, Food, Plus, Discount, Upload, PictureFilled
  },
  setup() {
    const products = shallowRef([])
    const categories = ref([])
    
    // 筛选条件
    const productSearch = ref('')
    const productCategory = ref('')
    const productStatus = ref('')
    
    // 分页
    const currentPage = ref(1)
    const pageSize = ref(100)
    const total = ref(0)
    
    // 商品详情相关
    const selectedProduct = ref(null)
    const detailDialogVisible = ref(false)
    
    // 视图模式
    const viewMode = ref('large')
    
    // 批量操作加载状态
    const batchLoading = ref(false)
    
    // 添加商品相关
    const addProductDialogVisible = ref(false)
    const newProductForm = ref({
      id: '',
      name: '',
      category: '',
      price: 0,
      unit: '件',
      stock: 0,
      description: '',
      barCode: '',
      tagsInput: '',
      onshelf: true,
      imageUrl: '',
      imageUploading: false
    })
    
    // 折扣管理相关
    const tempDiscount = ref(1)
    const tempDiscountDisplay = ref(10)

    const iconMap = {
      'Coffee': Coffee,
      'Sugar': Sugar,
      'Goods': Goods,
      'Apple': Apple,
      'MilkTea': MilkTea,
      'Food': Food
    }

    const getIconComponent = (iconName) => {
      return iconMap[iconName] || Goods
    }

    const getCategoryName = (categoryId) => {
      const category = categories.value.find(c => c.id === categoryId)
      return category ? category.name : categoryId
    }

    // 后端字段映射到前端
    const mapProduct = (p) => ({
      ...p,
      category: p.categoryId,
      image: getImageUrlByPath(p.imageUrl),
      tags: p.tags ? p.tags.split(',').filter(t => t) : [],
      status: p.status ? p.status.toLowerCase() : 'onshelf'
    })

    // 前端字段映射到后端
    const mapProductToBackend = (p) => ({
      ...p,
      categoryId: p.category,
      imageUrl: p.image,
      tags: Array.isArray(p.tags) ? p.tags.join(',') : (p.tags || ''),
      status: p.status ? p.status.toUpperCase() : 'ONSHELF'
    })

    const loadProducts = async () => {
      try {
        const statusParam = productStatus.value ? productStatus.value.toUpperCase() : undefined
        const res = await listProductsApi({
          keyword: productSearch.value || undefined,
          categoryId: productCategory.value || undefined,
          status: statusParam,
          page: currentPage.value,
          size: pageSize.value
        })
        const data = res.data
        const mappedProducts = (data.records || []).map(mapProduct)
        products.value = mappedProducts
        total.value = data.total || 0
        triggerRef(products)
        saveProducts(mappedProducts)
      } catch (error) {
        ElMessage.error('加载商品列表失败')
      }
    }

    const loadCategories = async () => {
      try {
        const res = await listCategoriesApi()
        categories.value = res.data || []
        saveCategories(res.data || [])
      } catch (error) {
        console.error('加载分类失败', error)
      }
    }

    const filteredProducts = computed(() => {
      return products.value
    })

    const toggleProductStatus = async (product, newStatus) => {
      try {
        await updateProductStatusApi(product.id, newStatus.toUpperCase())
        product.status = newStatus
        triggerRef(products)
        ElMessage.success(newStatus === 'onshelf' ? '已上架' : '已下架')
      } catch (error) {
        ElMessage.error('状态更新失败')
      }
    }

    const batchShelf = async (status) => {
      batchLoading.value = true
      try {
        const ids = filteredProducts.value.map(p => p.id)
        await updateBatchProductStatusApi(ids, status.toUpperCase())
        await loadProducts()
        ElMessage.success(status === 'onshelf' ? '批量上架成功' : '批量下架成功')
      } catch (error) {
        ElMessage.error('批量操作失败')
      } finally {
        batchLoading.value = false
      }
    }

    const showProductDetail = (product) => {
      selectedProduct.value = product
      tempDiscount.value = product.discount || 1
      tempDiscountDisplay.value = (tempDiscount.value * 10)
      detailDialogVisible.value = true
    }

    const showAddProductDialog = () => {
      newProductForm.value = {
        id: '',
        name: '',
        category: '',
        price: 0,
        unit: '件',
        stock: 0,
        description: '',
        barCode: '',
        tagsInput: '',
        onshelf: true,
        imageUrl: '',
        imageUploading: false
      }
      addProductDialogVisible.value = true
    }

    const handleImageUpload = async (uploadFile) => {
      // 业务约束：图片按商品分类落盘到 upload_resources/products/{categoryId}/，
      // 故上传前必须已选定分类；未选分类直接阻断，提示用户先选分类。
      const categoryId = newProductForm.value.category
      if (!categoryId) {
        ElMessage.warning('请先选择商品分类，再上传图片')
        return false
      }
      // 选图后立即调后端 /product/upload 压缩落盘，拿到 url 再随商品 JSON 提交，避免 base64 入库
      const formData = new FormData()
      formData.append('file', uploadFile.raw)
      newProductForm.value.imageUploading = true
      try {
        const res = await uploadProductImageApi(formData, categoryId)
        newProductForm.value.imageUrl = res.data.url
        ElMessage.success('图片上传成功')
      } catch (err) {
        ElMessage.error(err.response?.data?.msg || '图片上传失败')
      } finally {
        newProductForm.value.imageUploading = false
      }
      return false
    }

    const removeImage = () => {
      newProductForm.value.imageUrl = ''
    }

    const handleAddProduct = async () => {
      if (!newProductForm.value.id || !newProductForm.value.name || !newProductForm.value.category) {
        ElMessage.warning('请填写商品 ID、商品名称和分类')
        return
      }

      const tags = newProductForm.value.tagsInput
        ? newProductForm.value.tagsInput.split(/[,,]/).map(t => t.trim()).filter(t => t)
        : []

      const newProduct = {
        id: newProductForm.value.id,
        name: newProductForm.value.name,
        categoryId: newProductForm.value.category,
        price: newProductForm.value.price,
        unit: newProductForm.value.unit,
        stock: newProductForm.value.stock,
        imageUrl: newProductForm.value.imageUrl || '',
        description: newProductForm.value.description,
        barCode: newProductForm.value.barCode,
        discount: 1,
        isHot: false,
        tags: tags.join(','),
        status: newProductForm.value.onshelf ? 'ONSHELF' : 'OFFSHELF'
      }

      try {
        await addProductApi(newProduct)
        await loadProducts()
        ElMessage.success('商品添加成功')
        addProductDialogVisible.value = false
      } catch (error) {
        const msg = error.response?.data?.msg || '添加商品失败'
        ElMessage.error(msg)
      }
    }

    const handleDiscountChange = (value) => {
      tempDiscountDisplay.value = Math.round(value * 10)
    }

    const handleDiscountInputChange = (value) => {
      tempDiscount.value = value / 10
    }

    const removeDiscount = () => {
      tempDiscount.value = 1
      tempDiscountDisplay.value = 10
    }

    const saveDiscount = async () => {
      if (selectedProduct.value) {
        try {
          await updateProductDiscountApi(selectedProduct.value.id, tempDiscount.value)
          selectedProduct.value.discount = tempDiscount.value
          triggerRef(products)
          ElMessage.success('折扣设置已保存')
          detailDialogVisible.value = false
        } catch (error) {
          ElMessage.error('折扣保存失败')
        }
      }
    }

    onMounted(() => {
      loadCategories()
      loadProducts()
    })

    return {
      products,
      categories,
      productSearch,
      productCategory,
      productStatus,
      filteredProducts,
      selectedProduct,
      detailDialogVisible,
      viewMode,
      batchLoading,
      addProductDialogVisible,
      newProductForm,
      tempDiscount,
      tempDiscountDisplay,
      getCategoryName,
      getIconComponent,
      toggleProductStatus,
      batchShelf,
      showProductDetail,
      showAddProductDialog,
      handleAddProduct,
      handleImageUpload,
      removeImage,
      handleDiscountChange,
      handleDiscountInputChange,
      removeDiscount,
      saveDiscount
    }
  }
}
</script>

<style scoped>
.page-content h2 {
  margin: 0 0 20px 0;
  color: #303133;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  padding: 10px 0;
}

.page-header h2 {
  margin: 0;
  font-family: 'Microsoft YaHei', 'PingFang SC', 'Helvetica Neue', Arial, sans-serif;
  font-size: 28px;
  font-weight: 700;
  color: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: 2px 2px 4px rgba(102, 126, 234, 0.3);
  letter-spacing: 2px;
  left: 40px;
  position: relative;
}

.page-header h2::after {
  content: '';
  position: absolute;
  left: -10px;
  bottom: -8px;
  width: 170px;
  height: 4px;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  border-radius: 2px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.add-product-dialog .product-form {
  padding: 10px 5px;
}

.image-upload-wrapper {
  width: 100%;
}

.image-uploader :deep(.el-upload) {
  width: 100%;
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.image-uploader :deep(.el-upload:hover) {
  border-color: #409eff;
}

.upload-placeholder {
  width: 178px;
  height: 178px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #8c939d;
}

.upload-icon {
  font-size: 40px;
  color: #c0c4cc;
}

.upload-text {
  font-size: 14px;
  color: #909399;
}

.image-preview {
  width: 178px;
  height: 178px;
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  border: 2px solid #e4e7ed;
}

.image-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-preview-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  cursor: pointer;
  color: #fff;
  font-size: 24px;
}

.image-preview:hover .image-preview-overlay {
  opacity: 1;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
  padding: 20px;
  background: #ffffff;
  border-radius: 12px;
  border-color: #764ba2;
}

.filter-left {
  display: flex;
  gap: 20px;
  flex: 1;
  align-items: center;
}

/* 状态按钮组样式 */
.status-button-group {
  display: inline-flex;
}

.status-button-group .el-button {
  padding: 8px 12px;
  font-size: 13px;
}

.status-button-group .el-button .el-icon {
  margin-right: 4px;
}

.filter-right {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.batch-actions-container {
  display: flex;
  gap: 8px;
  padding: 8px 16px;
  flex-shrink: 0;
}

.batch-actions-container .el-button {
  padding: 6px 12px;
  font-size: 13px;
}

.batch-actions-container .el-button .el-icon {
  margin-right: 4px;
}

.category-bar {
  margin-bottom: 20px;
  padding: 0;
  background: #fff;
  border-radius: 8px 8px 0 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.category-scroll {
  display: flex;
  gap: 0;
  overflow-x: auto;
  padding: 0;
  scrollbar-width: thin;
  position: relative;
  flex: 1;
}

.category-scroll::-webkit-scrollbar {
  height: 3px;
}

.category-scroll::-webkit-scrollbar-thumb {
  background: #e4e7ed;
  border-radius: 2px;
}

.category-item {
  flex-shrink: 0;
  position: relative;
  padding: 14px 24px;
  background: #f5f7fa;
  border: none;
  border-radius: 8px 8px 0 0;
  font-size: 14px;
  color: #606266;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
  margin-right: 2px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.category-icon {
  font-size: 18px;
  margin-bottom: 2px;
}

.category-item::before {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: transparent;
  transition: all 0.3s ease;
}

.category-item::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(180deg, rgba(255,255,255,0.3) 0%, transparent 100%);
  opacity: 0;
  transition: all 0.3s ease;
}

.category-item:hover {
  background: #ecf5ff;
  color: #409eff;
  transform: translateY(-2px);
}

.category-item:hover::before {
  background: #409eff;
}

.category-item:hover::after {
  opacity: 1;
}

.category-item.active {
  background: linear-gradient(180deg, #409eff 0%, #66b1ff 100%);
  color: #fff;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  transform: translateY(-2px);
}

.category-item.active::before {
  background: #409eff;
  height: 4px;
}

.category-item.active::after {
  opacity: 1;
}

.category-text {
  position: relative;
  z-index: 1;
  display: inline-block;
  padding: 0 4px;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
}

.filter-item label {
  font-size: 14px;
  color: #666a71;
  font-weight: 500;
  white-space: nowrap;
  min-width: 40px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
  padding: 10px 0;
}

.product-grid.small-mode {
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 10px;
}

.product-grid.small-mode .product-card {
  border-radius: 8px;
}

.product-grid.small-mode .product-image {
  height: 80px;
}

.product-grid.small-mode .product-image .el-icon {
  font-size: 28px;
}

.product-grid.small-mode .product-info {
  padding: 6px 8px;
}

.product-grid.small-mode .product-id {
  font-size: 10px;
}

.product-grid.small-mode .product-name {
  font-size: 12px;
}

.product-grid.small-mode .product-price {
  font-size: 11px;
}

.product-grid.small-mode .product-stock,
.product-grid.small-mode .product-category {
  display: none;
}

.product-grid.small-mode .product-actions {
  margin-top: 4px;
}

.product-grid.small-mode .product-actions .el-button {
  font-size: 12px;
  padding: 4px 8px;
}

.product-card {
  background: #fff;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  position: relative;
}

/* 已下架产品样式 */
.product-card.offshelf {
  opacity: 0.6;
}

.product-card.offshelf::after {
  content: '已下架';
  position: absolute;
  top: 10px;
  right: 10px;
  background: rgba(245, 108, 108, 0.9);
  color: #fff;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  z-index: 10;
}

.product-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
}

.product-image {
  height: 130px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  overflow: hidden;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-image .el-icon {
  font-size: 50px;
  opacity: 0.8;
}

.product-info {
  padding: 10px 12px;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.product-id {
  font-size: 11px;
  color: #909399;
  font-weight: 500;
}

.product-name {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 2px;
}

.product-price {
  font-size: 13px;
  font-weight: 600;
  color: #f56c6c;
}

.product-price-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  flex-wrap: wrap;
  margin: 4px 0;
}

.product-discount-price {
  font-size: 15px;
  font-weight: 700;
  color: #f56c6c;
}

.product-original-price {
  font-size: 11px;
  color: #909399;
  text-decoration: line-through;
  font-weight: 400;
}

.discount-tag {
  margin-left: 2px;
}

.product-stock,
.product-category {
  margin-top: 2px;
}

.product-actions {
  display: flex;
  gap: 0;
  margin-top: 8px;
}

.product-actions .el-button {
  flex: 1;
  margin: 0;
  border-radius: 0;
}

.product-actions .el-button:first-child {
  border-radius: 4px 0 0 4px;
}

.product-actions .el-button:last-child {
  border-radius: 0 4px 4px 0;
}

.product-actions .el-button:not(:first-child):not(:last-child) {
  border-radius: 0;
}

.product-detail {
  padding: 20px 0;
}

.detail-layout {
  display: flex;
  gap: 30px;
  align-items: flex-start;
}

.detail-left-column {
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex-shrink: 0;
}

.detail-image {
  width: 180px;
  height: 180px;
  flex-shrink: 0;
  border-radius: 12px;
  overflow: hidden;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.detail-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.no-image-icon {
  font-size: 60px;
  color: #fff;
  opacity: 0.8;
}

.detail-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 18px;
  min-width: 0;
}

.detail-row {
  display: flex;
  gap: 24px;
  align-items: stretch;
}

.detail-row.tags-row {
  justify-content: center;
  margin-top: 5px;
  border-top: 1px solid #f0f2f5;
  padding-top: 15px;
}

.detail-row .detail-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}

.detail-row .detail-item.full-width {
  flex: 1 1 100%;
  text-align: center;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.detail-label {
  font-size: 12px;
  color: #909399;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  
}

.detail-label.center-label {
  text-align: center;
}

.detail-value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
  line-height: 1.4;
}

.detail-value.price {
  font-size: 20px;
  font-weight: 700;
  color: #f56c6c;
}

.detail-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}

.detail-tags.centered {
  justify-content: center;
}

/* 折扣管理区域样式 */
.discount-management {
  width: 260px;
  height: 60px;
  background: linear-gradient(135deg, #fdf6ec 0%, #fef7ed 100%);
  border-radius: 8px;
  border: 2px solid #f9d785;
  padding: 8px;
  display: flex;
  margin-top: 20px;
  flex-direction: column;
  justify-content: space-between;
  box-shadow: 0 2px 8px rgba(249, 215, 133, 0.3);
}

.discount-content {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.discount-input-group {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  flex-shrink: 0;
}

.discount-input {
  width: 80px;
}

.discount-input :deep(.el-input__inner) {
  padding: 3px 6px;
  font-size: 12px;
  text-align: center;
  font-weight: 600;
  color: #e63f3c;
}

.discount-unit {
  font-size: 12px;
  color: #606266;
  font-weight: 500;
}

.discount-price-display {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 8px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 6px;
  border: 1px solid rgba(249, 215, 133, 0.3);
}

.price-label {
  font-size: 11px;
  color: #909399;
  font-weight: 500;
  white-space: nowrap;
}

.price-value {
  font-size: 16px;
  font-weight: 700;
  color: #f56c6c;
  white-space: nowrap;
}

.price-original {
  font-size: 11px;
  color: #909399;
  text-decoration: line-through;
  white-space: nowrap;
}

.discount-button-bar {
  display: flex;
  gap: 6px;
  justify-content: center;
  margin-top: 4px;
  padding-top: 4px;
  border-top: 1px dashed rgba(249, 215, 133, 0.5);
}

.remove-discount-btn {
  padding: 3px 10px;
  font-size: 11px;
  height: 24px;
  min-width: 45px;
  background: linear-gradient(135deg, #ff7676 0%, #ff5252 100%);
  border: none;
  color: white;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.remove-discount-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(255, 82, 82, 0.4);
}

.save-discount-btn {
  padding: 3px 10px;
  font-size: 11px;
  height: 24px;
  min-width: 45px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.save-discount-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.4);
}

.save-discount-btn:disabled {
  background: linear-gradient(135deg, #c0c4cc 0%, #a8acb2 100%);
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}
</style>

<style>
.product-detail-dialog .el-dialog__header {
  padding: 20px 24px 10px !important;
  text-align: left !important;
}

.product-detail-dialog .el-dialog__title {
  font-size: 18px !important;
  font-weight: 600 !important;
  color: #303133 !important;
}

.product-detail-dialog .el-dialog__body {
  padding: 15px 24px 20px !important;
}

.product-detail-dialog .el-dialog__footer {
  padding: 0 24px 20px !important;
  border-top: 1px solid #f0f2f5;
  margin-top: 0 !important;
}

.product-detail-dialog .el-dialog__headerbtn {
  top: 18px !important;
  right: 20px !important;
}
</style>
