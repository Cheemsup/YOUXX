<template>
  <div class="page-content">
    <div class="profile-header">
      <div class="avatar-section">
        <div class="avatar-wrapper">
          <img v-if="avatarUrl" :src="avatarUrl" alt="头像" class="avatar" />
          <div v-else class="avatar-placeholder">
            <el-icon><User /></el-icon>
          </div>
          <div class="avatar-upload" @click="handleAvatarClick">
            <el-icon><Camera /></el-icon>
          </div>
          <input
            ref="avatarInput"
            type="file"
            accept="image/*"
            @change="handleAvatarChange"
            style="display: none"
          />
        </div>
        <div class="welcome-section">
          <h2>你好，{{ userInfo.username }}！</h2>
          <p>欢迎回来，享受你的购物之旅</p>
        </div>
      </div>
    </div>

    <div class="quick-actions">
      <div class="action-item" @click="$emit('goToOrders')">
        <div class="action-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
          <el-icon><Document /></el-icon>
        </div>
        <span class="action-label">我的订单</span>
      </div>
      <div class="action-item">
        <div class="action-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
          <el-icon><Star /></el-icon>
        </div>
        <span class="action-label">我的收藏</span>
      </div>
      <div class="action-item">
        <div class="action-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
          <el-icon><Ticket /></el-icon>
        </div>
        <span class="action-label">优惠券</span>
      </div>
    </div>

    <div class="stats-section">
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
          <el-icon><Document /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.totalOrders }}</div>
          <div class="stat-label">总订单数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
          <el-icon><Money /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">¥{{ stats.totalAmount }}</div>
          <div class="stat-label">消费金额</div>
        </div>
      </div>
    </div>

    <div class="profile-form-section">
      <el-card class="profile-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span class="card-title">
              <el-icon><User /></el-icon>
              个人信息
            </span>
          </div>
        </template>
        <el-form label-width="100px" class="profile-form">
          <el-form-item label="用户名">
            <el-input v-model="localUserInfo.username" disabled />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="localUserInfo.email" placeholder="请输入邮箱" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="localUserInfo.phone" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveProfile">
              <el-icon><Check /></el-icon>
              保存修改
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <div class="address-section">
      <el-card class="address-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span class="card-title">
              <el-icon><Location /></el-icon>
              地址管理
            </span>
            <el-button type="primary" size="small" @click="showAddressDialog()">
              <el-icon><Plus /></el-icon>
              新增地址
            </el-button>
          </div>
        </template>
        <div v-if="addresses.length === 0" class="empty-address">
          <el-empty description="暂无收货地址" />
        </div>
        <div v-else class="address-list">
          <div v-for="(addr, index) in addresses" :key="index" class="address-item">
            <div class="address-main">
              <div class="address-info">
                <div class="address-top">
                  <span class="address-name">{{ addr.name }}</span>
                  <span class="address-phone">{{ addr.phone }}</span>
                  <el-tag v-if="addr.isDefault" type="success" size="small" effect="dark">默认</el-tag>
                </div>
                <div class="address-detail">{{ addr.detail }}</div>
              </div>
              <div class="address-actions">
                <el-button link type="primary" size="small" @click="showAddressDialog(addr, index)">
                  编辑
                </el-button>
                <el-button link type="primary" size="small" @click="setDefaultAddress(index)" v-if="!addr.isDefault">
                  设为默认
                </el-button>
                <el-button link type="danger" size="small" @click="deleteAddress(index)">
                  删除
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <el-dialog
      v-model="addressDialogVisible"
      :title="editingIndex !== null ? '编辑地址' : '新增地址'"
      width="500px"
    >
      <el-form :model="addressForm" label-width="80px">
        <el-form-item label="收货人">
          <el-input v-model="addressForm.name" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="addressForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="详细地址">
          <el-input
            v-model="addressForm.detail"
            type="textarea"
            :rows="3"
            placeholder="请输入详细地址"
          />
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="addressForm.isDefault" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addressDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAddress">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  User,
  Camera,
  Document,
  Star,
  Ticket,
  Money,
  Check,
  Location,
  Plus
} from '@element-plus/icons-vue'
import { getOrders, getOrdersByUsername } from '@/data/orders.js'
import { getUserByUsername, updateUser } from '@/data/users.js'

export default {
  name: 'UserProfile',
  components: {
    User,
    Camera,
    Document,
    Star,
    Ticket,
    Money,
    Check,
    Location,
    Plus
  },
  props: {
    userInfo: {
      type: Object,
      required: true
    }
  },
  emits: ['update:userInfo', 'goToOrders'],
  setup(props, { emit }) {
    const avatarInput = ref(null)
    const avatarUrl = ref('')
    const localUserInfo = ref({ ...props.userInfo })
    const currentUsername = sessionStorage.getItem('username') || ''

    const addressDialogVisible = ref(false)
    const editingIndex = ref(null)
    const addressForm = ref({
      name: '',
      phone: '',
      detail: '',
      isDefault: false
    })
    const addresses = ref([])

    const stats = ref({
      totalOrders: 0,
      totalAmount: '0.00'
    })

    const getUserStorageKey = (key) => {
      return `${key}_${currentUsername}`
    }

    watch(() => props.userInfo, (newVal) => {
      localUserInfo.value = { ...newVal }
    }, { deep: true })

    onMounted(() => {
      const user = getUserByUsername(currentUsername)
      if (user) {
        localUserInfo.value.username = user.username
        localUserInfo.value.phone = user.phone || ''
        localUserInfo.value.email = user.email || ''
      }
      
      loadAddresses()
      loadStats()
      const savedAvatar = localStorage.getItem(getUserStorageKey('userAvatar'))
      if (savedAvatar) {
        avatarUrl.value = savedAvatar
      }
    })

    const handleAvatarClick = () => {
      avatarInput.value?.click()
    }

    const handleAvatarChange = (e) => {
      const file = e.target.files[0]
      if (file) {
        const reader = new FileReader()
        reader.onload = (event) => {
          avatarUrl.value = event.target.result
          localStorage.setItem(getUserStorageKey('userAvatar'), event.target.result)
          ElMessage.success('头像已更新')
        }
        reader.readAsDataURL(file)
      }
    }

    const saveProfile = () => {
      const user = getUserByUsername(currentUsername)
      if (user) {
        updateUser(user.id, {
          phone: localUserInfo.value.phone,
          email: localUserInfo.value.email
        })
      }
      emit('update:userInfo', localUserInfo.value)
      ElMessage.success('个人信息已保存')
    }

    const loadAddresses = () => {
      const saved = localStorage.getItem(getUserStorageKey('userAddresses'))
      if (saved) {
        addresses.value = JSON.parse(saved)
      } else {
        addresses.value = []
      }
    }

    const saveAddresses = () => {
      localStorage.setItem(getUserStorageKey('userAddresses'), JSON.stringify(addresses.value))
    }

    const showAddressDialog = (addr = null, index = null) => {
      editingIndex.value = index
      if (addr) {
        addressForm.value = { ...addr }
      } else {
        addressForm.value = {
          name: '',
          phone: '',
          detail: '',
          isDefault: false
        }
      }
      addressDialogVisible.value = true
    }

    const saveAddress = () => {
      if (!addressForm.value.name || !addressForm.value.phone || !addressForm.value.detail) {
        ElMessage.warning('请填写完整信息')
        return
      }

      if (addressForm.value.isDefault) {
        addresses.value.forEach(addr => {
          addr.isDefault = false
        })
      }

      if (editingIndex.value !== null) {
        addresses.value[editingIndex.value] = { ...addressForm.value }
      } else {
        addresses.value.push({ ...addressForm.value })
      }

      if (addresses.value.length === 1) {
        addresses.value[0].isDefault = true
      }

      saveAddresses()
      addressDialogVisible.value = false
      ElMessage.success(editingIndex.value !== null ? '地址已更新' : '地址已添加')
    }

    const setDefaultAddress = (index) => {
      addresses.value.forEach((addr, i) => {
        addr.isDefault = i === index
      })
      saveAddresses()
      ElMessage.success('已设为默认地址')
    }

    const deleteAddress = (index) => {
      ElMessageBox.confirm('确定要删除这个地址吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        addresses.value.splice(index, 1)
        if (addresses.value.length > 0 && !addresses.value.some(a => a.isDefault)) {
          addresses.value[0].isDefault = true
        }
        saveAddresses()
        ElMessage.success('地址已删除')
      }).catch(() => {})
    }

    const loadStats = () => {
      const orders = getOrdersByUsername(currentUsername)
      const completedOrders = orders.filter(o => o.status === '已完成')
      stats.value.totalOrders = orders.length
      stats.value.totalAmount = completedOrders
        .reduce((sum, o) => sum + parseFloat(o.totalAmount), 0)
        .toFixed(2)
    }

    return {
      avatarInput,
      avatarUrl,
      localUserInfo,
      addressDialogVisible,
      editingIndex,
      addressForm,
      addresses,
      stats,
      handleAvatarClick,
      handleAvatarChange,
      saveProfile,
      showAddressDialog,
      saveAddress,
      setDefaultAddress,
      deleteAddress
    }
  }
}
</script>

<style scoped>
.page-content {
  width: 100%;
  animation: fadeIn 0.5s ease-out;
}

.page-content :deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
}

.page-content :deep(.el-input__wrapper:hover) {
  box-shadow: 0 4px 12px rgba(66, 185, 131, 0.15);
  transform: translateY(-1px);
}

.page-content :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 3px rgba(66, 185, 131, 0.15), 0 4px 12px rgba(66, 185, 131, 0.2);
  transform: translateY(-1px);
}

.page-content :deep(.el-button--primary) {
  background: linear-gradient(135deg, #42b983 0%, #35a070 100%);
  border: none;
  border-radius: 12px;
  padding: 12px 28px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(66, 185, 131, 0.3);
  transition: all 0.3s ease;
}

.page-content :deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(66, 185, 131, 0.4);
  background: linear-gradient(135deg, #35a070 0%, #2d8f62 100%);
}

.page-content :deep(.el-textarea__inner) {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  border: none;
}

.page-content :deep(.el-textarea__inner:hover) {
  box-shadow: 0 4px 12px rgba(66, 185, 131, 0.15);
  transform: translateY(-1px);
}

.page-content :deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 3px rgba(66, 185, 131, 0.15), 0 4px 12px rgba(66, 185, 131, 0.2);
  transform: translateY(-1px);
}

.page-content :deep(.el-switch) {
  --el-switch-on-color: #42b983;
}

.avatar-section {
  animation: slideInLeft 0.6s ease-out;
}

.quick-actions {
  animation: fadeIn 0.5s ease-out 0.1s backwards;
}

.stats-section {
  animation: fadeIn 0.5s ease-out 0.2s backwards;
}

.profile-form-section {
  animation: slideInRight 0.6s ease-out 0.3s backwards;
}

.address-section {
  animation: slideInLeft 0.6s ease-out 0.4s backwards;
}


.profile-header {
  margin-bottom: 30px;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
}

.avatar-wrapper {
  position: relative;
}

.avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid rgba(255, 255, 255, 0.3);
}

.avatar-placeholder {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 48px;
  border: 4px solid rgba(255, 255, 255, 0.3);
}

.avatar-upload {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 36px;
  height: 36px;
  background: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #667eea;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

.avatar-upload:hover {
  transform: scale(1.1);
}

.welcome-section h2 {
  margin: 0 0 8px 0;
  color: #fff;
  font-size: 28px;
  font-weight: 600;
}

.welcome-section p {
  margin: 0;
  color: rgba(255, 255, 255, 0.9);
  font-size: 16px;
}

.quick-actions {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
}

.action-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 24px 16px;
  background: #fff;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.action-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.action-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.action-label {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.stats-section {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.profile-form-section,
.address-section {
  margin-bottom: 25px;
}

.profile-card,
.address-card {
  border: none;
  border-radius: 16px;
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.card-title .el-icon {
  color: #42b983;
}

.profile-form {
  max-width: 500px;
}

.empty-address {
  padding: 40px 0;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.address-item {
  padding: 20px;
  background: linear-gradient(135deg, #f8f9fa 0%, #f0f9f4 100%);
  border-radius: 12px;
  border: 1px solid #e8f5ed;
  transition: all 0.3s ease;
}

.address-item:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.address-main {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.address-info {
  flex: 1;
}

.address-top {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.address-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.address-phone {
  font-size: 14px;
  color: #606266;
}

.address-detail {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
}

.address-actions {
  display: flex;
  gap: 8px;
}
</style>
