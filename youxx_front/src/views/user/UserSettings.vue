<template>
  <div class="page-content">
    <div class="settings-header">
      <div class="header-bg">
        <h2>⚙️ 账户设置</h2>
        <p>管理您的账户偏好和隐私</p>
      </div>
    </div>

    <div class="settings-grid">
      <div class="settings-section" style="animation: slideInLeft 0.6s ease-out 0.1s backwards;">
        <el-card class="setting-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <MiniCartoonCharacter type="orange" class="card-icon-character" />
                通知设置
              </span>
            </div>
          </template>
          <div class="setting-items">
            <div class="setting-item">
              <div class="item-info">
                <span class="item-label">推送通知</span>
                <span class="item-desc">接收订单状态更新和促销信息</span>
              </div>
              <el-switch v-model="localSettings.notifications" />
            </div>
            <div class="setting-item">
              <div class="item-info">
                <span class="item-label">消息提醒</span>
                <span class="item-desc">新消息和系统通知提醒</span>
              </div>
              <el-switch v-model="localSettings.messageAlert" />
            </div>
            <div class="setting-item">
              <div class="item-info">
                <span class="item-label">邮件订阅</span>
                <span class="item-desc">接收每周精选商品推荐邮件</span>
              </div>
              <el-switch v-model="emailSubscribe" />
            </div>
          </div>
        </el-card>

        <el-card class="setting-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <MiniCartoonCharacter type="purple" class="card-icon-character" />
                显示设置
              </span>
            </div>
          </template>
          <div class="setting-items">
            <div class="setting-item">
              <div class="item-info">
                <span class="item-label">语言</span>
                <span class="item-desc">选择界面显示语言</span>
              </div>
              <el-select v-model="localSettings.language" size="default">
                <el-option label="🇨🇳 简体中文" value="zh" />
                <el-option label="🇺🇸 English" value="en" />
              </el-select>
            </div>
            <div class="setting-item">
              <div class="item-info">
                <span class="item-label">主题模式</span>
                <span class="item-desc">选择界面主题风格</span>
              </div>
              <el-radio-group v-model="themeMode" size="small">
                <el-radio-button label="light">浅色</el-radio-button>
                <el-radio-button label="dark">深色</el-radio-button>
                <el-radio-button label="auto">跟随系统</el-radio-button>
              </el-radio-group>
            </div>
          </div>
        </el-card>
      </div>

      <div class="settings-section" style="animation: slideInRight 0.6s ease-out 0.2s backwards;">
        <el-card class="setting-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <MiniCartoonCharacter type="black" class="card-icon-character" />
                安全与隐私
              </span>
            </div>
          </template>
          <div class="setting-items">
            <div class="setting-item clickable" @click="showChangePassword = true">
              <div class="item-info">
                <span class="item-label">修改密码</span>
                <span class="item-desc">定期更换密码以保护账户安全</span>
              </div>
              <el-icon><ArrowRight /></el-icon>
            </div>
            <div class="setting-item clickable">
              <div class="item-info">
                <span class="item-label">两步验证</span>
                <span class="item-desc">为账户添加额外的安全层</span>
              </div>
              <el-tag type="info" size="small">即将推出</el-tag>
            </div>
            <div class="setting-item clickable">
              <div class="item-info">
                <span class="item-label">登录设备管理</span>
                <span class="item-desc">查看和管理已登录的设备</span>
              </div>
              <el-icon><ArrowRight /></el-icon>
            </div>
            <div class="setting-item">
              <div class="item-info">
                <span class="item-label">隐私模式</span>
                <span class="item-desc">隐藏个人购物记录</span>
              </div>
              <el-switch v-model="privacyMode" />
            </div>
          </div>
        </el-card>

        <el-card class="setting-card danger-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <MiniCartoonCharacter type="yellow" class="card-icon-character" />
                账户操作
              </span>
            </div>
          </template>
          <div class="setting-items">
            <div class="setting-item clickable" @click="handleLogout">
              <div class="item-info">
                <span class="item-label logout-text">退出登录</span>
                <span class="item-desc">退出当前账户</span>
              </div>
              <el-button type="danger" plain size="small">
                <el-icon><SwitchButton /></el-icon>
                退出
              </el-button>
            </div>
            <div class="setting-item clickable" @click="clearCache">
              <div class="item-info">
                <span class="item-label">清除缓存</span>
                <span class="item-desc">清除本地存储的数据（{{ cacheSize }}）</span>
              </div>
              <el-button type="warning" plain size="small">
                清除
              </el-button>
            </div>
          </div>
        </el-card>
      </div>
    </div>


    <el-dialog v-model="showChangePassword" title="修改密码" width="420px">
      <el-form :model="passwordForm" label-width="100px">
        <el-form-item label="当前密码">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入当前密码" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showChangePassword = false">取消</el-button>
        <el-button type="primary" @click="handleChangePassword">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowRight,
  SwitchButton
} from '@element-plus/icons-vue'
import MiniCartoonCharacter from '@/components/MiniCartoonCharacter.vue'

export default {
  name: 'UserSettings',
  components: {
    ArrowRight,
    SwitchButton,
    MiniCartoonCharacter
  },
  props: {
    settings: {
      type: Object,
      required: true
    }
  },
  emits: ['update:settings'],
  setup(props, { emit }) {
    const router = useRouter()
    const currentUsername = sessionStorage.getItem('username') || ''
    const localSettings = ref({ ...props.settings })
    const emailSubscribe = ref(true)
    const themeMode = ref('light')
    const privacyMode = ref(false)
    const showChangePassword = ref(false)
    const passwordForm = ref({
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    })

    const cacheSize = computed(() => {
      let total = 0
      for (let key in localStorage) {
        if (localStorage.hasOwnProperty(key)) {
          total += localStorage.getItem(key).length * 2
        }
      }
      if (total > 1024 * 1024) {
        return (total / (1024 * 1024)).toFixed(2) + ' MB'
      }
      return (total / 1024).toFixed(2) + ' KB'
    })

    const lastUpdate = new Date().toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    })

    watch(() => props.settings, (newVal) => {
      localSettings.value = { ...newVal }
    }, { deep: true })

    const saveSettings = () => {
      emit('update:settings', localSettings.value)
      ElMessage.success('设置已保存')
    }

    watch(localSettings, () => {
      saveSettings()
    }, { deep: true })

    const handleLogout = () => {
      ElMessageBox.confirm(
        '确定要退出登录吗？',
        '提示',
        {
          confirmButtonText: '确定退出',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(() => {
        sessionStorage.removeItem('username')
        sessionStorage.removeItem('userRole')
        ElMessage.success('已成功退出')
        router.push('/login')
      }).catch(() => {})
    }

    const clearCache = () => {
      ElMessageBox.confirm(
        '确定要清除所有本地缓存数据吗？这将删除您的购物车等临时数据。',
        '警告',
        {
          confirmButtonText: '确定清除',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(() => {
        const keysToRemove = []
        const userPrefixes = [
          `cartItems_${currentUsername}`,
          `userAvatar_${currentUsername}`,
          `userAddresses_${currentUsername}`
        ]
        for (let i = 0; i < localStorage.length; i++) {
          const key = localStorage.key(i)
          if (userPrefixes.includes(key)) {
            keysToRemove.push(key)
          }
        }
        keysToRemove.forEach(key => localStorage.removeItem(key))
        ElMessage.success('缓存已清除')
      }).catch(() => {})
    }

    const handleChangePassword = () => {
      if (!passwordForm.value.oldPassword || !passwordForm.value.newPassword || !passwordForm.value.confirmPassword) {
        ElMessage.warning('请填写完整信息')
        return
      }
      if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
        ElMessage.error('两次输入的新密码不一致')
        return
      }
      if (passwordForm.value.newPassword.length < 6) {
        ElMessage.error('新密码长度不能少于6位')
        return
      }
      ElMessage.success('密码修改成功')
      showChangePassword.value = false
      passwordForm.value = {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      }
    }

    return {
      localSettings,
      emailSubscribe,
      themeMode,
      privacyMode,
      showChangePassword,
      passwordForm,
      cacheSize,
      lastUpdate,
      saveSettings,
      handleLogout,
      clearCache,
      handleChangePassword
    }
  }
}
</script>

<style scoped>
.page-content {
  width: 100%;
  animation: fadeIn 0.5s ease-out;
}

.settings-header {
  margin-bottom: 24px;
}

.header-bg {
  background: linear-gradient(135deg, #42b983 0%, #35a070 50%, #2d8f62 100%);
  padding: 32px;
  border-radius: 20px;
  color: #fff;
  position: relative;
  overflow: hidden;
}

.header-bg::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -10%;
  width: 300px;
  height: 300px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
}

.header-bg::after {
  content: '';
  position: absolute;
  bottom: -30%;
  left: -5%;
  width: 200px;
  height: 200px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 50%;
}

.header-bg h2 {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 600;
  position: relative;
  z-index: 1;
}

.header-bg p {
  margin: 0;
  font-size: 15px;
  opacity: 0.9;
  position: relative;
  z-index: 1;
}

.settings-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

@media (max-width: 1200px) {
  .settings-grid {
    grid-template-columns: 1fr;
  }
}

.settings-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.setting-card {
  border: none;
  border-radius: 16px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.setting-card:hover {
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
}

.danger-card {
  border-left: 4px solid #f56c6c;
}

.card-header {
  padding: 16px 20px;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-bottom: 1px solid #ebeef5;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.card-icon {
  font-size: 22px;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
}

.card-icon-character {
  width: 36px;
  height: 36px;
}

.card-icon-character .mini-character {
  width: 100%;
  height: 100%;
}

.card-icon.notification {
  background: linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%);
}

.card-icon.display {
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
}

.card-icon.security {
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
}

.card-icon.logout {
  background: linear-gradient(135deg, #ffebee 0%, #ffcdd2 100%);
}

.card-icon.about {
  background: linear-gradient(135deg, #f3e5f5 0%, #e1bee7 100%);
}

.card-icon-character {
  display: flex;
  align-items: center;
  justify-content: center;
}

.setting-items {
  padding: 8px 0;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 20px;
  border-bottom: 1px solid #f5f7fa;
  transition: all 0.2s ease;
}

.setting-item:last-child {
  border-bottom: none;
}

.setting-item.clickable {
  cursor: pointer;
}

.setting-item.clickable:hover {
  background: #fafbfc;
  padding-left: 24px;
}

.item-info {
  flex: 1;
}

.item-label {
  display: block;
  font-size: 15px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.logout-text {
  color: #f56c6c !important;
}

.item-desc {
  display: block;
  font-size: 13px;
  color: #909399;
}

.about-section {
  margin-top: 0;
}

.about-card {
  border: none;
  border-radius: 16px;
  overflow: hidden;
}

.about-content {
  padding: 12px 0;
}

.about-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 0;
  border-bottom: 1px solid #f5f7fa;
}

.about-item:last-of-type {
  border-bottom: none;
}

.about-label {
  color: #606266;
  font-size: 14px;
}

.about-value {
  color: #303133;
  font-weight: 500;
  font-size: 14px;
}

.about-links {
  display: flex;
  justify-content: center;
  gap: 8px;
  padding-top: 16px;
  margin-top: 8px;
  border-top: 1px solid #f5f7fa;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}
</style>
