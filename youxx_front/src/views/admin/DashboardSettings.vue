<template>
  <div class="page-content">
    <div class="page-header">
      <h2>系统设置</h2>
    </div>
    
    <!-- 管理员身份卡片 -->
    <div class="admin-card">
      <div class="admin-card-body">
        <!-- 左侧照片区域 -->
        <div class="admin-photo">
          <div class="photo-placeholder">
            <el-icon :size="40"><User /></el-icon>
          </div>
        </div>
        <!-- 右侧信息区域 -->
        <div class="admin-info-section">
          <div class="admin-card-header">
            <span class="admin-title">管理员信息</span>
          </div>
          <div class="admin-card-content">
            <div class="admin-info-item">
              <span class="admin-info-label">管理员 ID：</span>
              <span class="admin-info-value">{{ adminInfo.id }}</span>
            </div>
            <div class="admin-info-item">
              <span class="admin-info-label">账号：</span>
              <span class="admin-info-value">{{ adminInfo.username }}</span>
            </div>
            <div class="admin-info-item">
              <span class="admin-info-label">密码：</span>
              <div class="password-wrapper">
                <span class="admin-info-value password">{{ showPassword ? adminInfo.password : '••••••••' }}</span>
                <el-icon class="password-toggle" @click="showPassword = !showPassword">
                  <View v-if="showPassword" />
                  <View v-else />
                </el-icon>
              </div>
            </div>
            <div class="admin-info-item">
              <span class="admin-info-label">电话号码：</span>
              <span class="admin-info-value">{{ adminInfo.phone }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-form label-width="120px" style="margin-top: 20px;">
      <el-form-item label="系统名称">
        <el-input v-model="settings.systemName" />
      </el-form-item>
      <el-form-item label="管理员邮箱">
        <el-input v-model="settings.adminEmail" />
      </el-form-item>
      <el-form-item label="开启注册">
        <el-switch v-model="settings.allowRegister" />
      </el-form-item>
      <el-form-item label="维护模式">
        <el-switch v-model="settings.maintenanceMode" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary">保存设置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import { reactive, ref, computed } from 'vue'
import { User, View } from '@element-plus/icons-vue'
import { getUsers } from '@/data/users.js'

export default {
  name: 'DashboardSettings',
  components: {
    User,
    View
  },
  setup() {
    const showPassword = ref(false)
    
    // 获取当前管理员信息（从 localStorage 或默认第一个管理员）
    const currentUser = localStorage.getItem('currentUser')
    const users = getUsers()
    const adminUser = users.find(u => u.username === currentUser) || users.find(u => u.role === 'admin') || {}
    
    const adminInfo = reactive({
      id: adminUser.id || '-',
      username: adminUser.username || '-',
      password: adminUser.password || '-',
      phone: adminUser.phone || '-'
    })

    const settings = reactive({
      systemName: '管理系统',
      adminEmail: 'admin@example.com',
      allowRegister: true,
      maintenanceMode: false
    })

    return { 
      showPassword,
      adminInfo,
      settings 
    }
  }
}
</script>

<style scoped>
.page-content h2 {
  margin: 0 0 20px 0;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

/* 管理员身份卡片 */
.admin-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.admin-card-body {
  display: flex;
  gap: 20px;
  padding: 20px;
}

/* 左侧照片区域 */
.admin-photo {
  flex-shrink: 0;
  width: 180px;
  height: 240px;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.photo-placeholder {
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 右侧信息区域 */
.admin-info-section {
  flex: 1;
  min-width: 0;
}

.admin-card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 0;
  margin-bottom: 10px;
  border-bottom: 2px solid #f0f0f0;
}

.admin-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.admin-card-content {
  padding: 10px 0;
}

.admin-info-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f5f5f5;
}

.admin-info-item:last-child {
  border-bottom: none;
}

.admin-info-label {
  width: 100px;
  color: #909399;
  font-size: 14px;
  flex-shrink: 0;
}

.admin-info-value {
  color: #303133;
  font-size: 14px;
  font-weight: 500;
}

.password-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.password {
  font-family: 'Courier New', monospace;
  letter-spacing: 2px;
}

.password-toggle {
  cursor: pointer;
  color: #909399;
  transition: color 0.3s ease;
}

.password-toggle:hover {
  color: #409eff;
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: 2px 2px 4px rgba(102, 126, 234, 0.3);
  letter-spacing: 2px;
  position: relative;
  left: 40px;
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
</style>
