<template>
  <div class="page-content">
    <div class="page-header">
      <h2>用户管理</h2>
    </div>
    
    <div class="stats-cards">
      <div class="stat-card user-card" @click="filterByRole('')">
        <div class="stat-card-content">
          <div class="stat-icon-wrapper">
            <div class="stat-icon">
              <el-icon><User /></el-icon>
            </div>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ totalUsers }}</div>
            <div class="stat-label">用户总数</div>
          </div>
        </div>
      </div>
      
      <div class="stat-card admin-card" @click="filterByRole('管理员')">
        <div class="stat-card-content">
          <div class="stat-icon-wrapper">
            <div class="stat-icon">
              <el-icon><UserFilled /></el-icon>
            </div>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ totalAdmins }}</div>
            <div class="stat-label">管理员数量</div>
          </div>
        </div>
      </div>
    </div>

    <div class="filter-bar">
      <div class="filter-item">
        <label>搜索：</label>
        <el-input
          v-model="searchKeyword"
          placeholder="请输入 ID 或用户名"
          clearable
          @clear="handleSearchClear"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
      <div class="filter-item">
        <label>身份：</label>
        <el-select
          v-model="roleFilter"
          placeholder="请选择身份"
          clearable
          @change="handleRoleChange"
        >
          <el-option label="全部" value="" />
          <el-option label="管理员" value="管理员" />
          <el-option label="用户" value="用户" />
        </el-select>
      </div>
      <el-button type="primary" @click="showAddDialog">
        <el-icon><Plus /></el-icon>
        添加用户
      </el-button>
    </div>
    <div class="table-wrapper">
      <el-table :data="paginatedUsers" style="width: 100%" :key="tableKey">
        <el-table-column prop="id" label="ID"  />
        <el-table-column prop="username" label="用户名"  />
        <el-table-column prop="phone" label="电话号码"  />
        <el-table-column prop="role" label="角色" >
          <template #default="scope">
            <el-tag :type="scope.row.role === '管理员' ? 'danger' : 'primary'">
              {{ scope.row.role }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" >
          <template #default="scope">
            <el-tag
              v-if="canEditStatus(scope.row)"
              :type="scope.row.status === '正常' ? 'success' : 'danger'"
              style="cursor: pointer"
              @click="handleStatusChange(scope.row)"
            >
              {{ scope.row.status }}
            </el-tag>
            <el-tag v-else :type="scope.row.status === '正常' ? 'success' : 'danger'">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" >
          <template #default="scope">
            <el-button size="small" @click="showEditDialog(scope.row)">编辑</el-button>
            <el-button
              v-if="canDeleteUser(scope.row)"
              size="small"
              type="danger"
              @click="handleDelete(scope.row)"
            >
              删除
            </el-button>
            <el-button
              v-else
              size="small"
              type="info"
              disabled
              class="disabled-delete-btn"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[5, 10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="isEditMode ? '编辑用户' : '添加用户'"
      width="500px"
    >
      <el-form :model="userForm" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="userForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item v-if="!isEditMode" label="密码">
          <el-input v-model="userForm.password" placeholder="请输入密码" type="password" show-password />
        </el-form-item>
        <el-form-item label="电话号码">
          <el-input v-model="userForm.phone" placeholder="请输入电话号码" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="userForm.role" placeholder="请选择角色" :disabled="isEditMode">
            <el-option label="管理员" value="管理员" />
            <el-option label="用户" value="用户" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="userForm.status" placeholder="请选择状态" :disabled="userForm.role === '管理员'">
            <el-option label="正常" value="正常" />
            <el-option label="禁用" value="禁用" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, watch, computed, onMounted } from 'vue'
import { Search, Plus, User, UserFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listUsersApi, addUserApi, updateUserApi, updateUserStatusApi, deleteUserApi } from '@/services/api.js'

export default {
  name: 'DashboardManageUser',
  components: { Search, Plus, User, UserFilled },
  props: {
    currentUser: { type: String, default: '' }
  },
  emits: ['users-updated'],
  setup(props, { emit }) {
    const searchKeyword = ref('')
    const roleFilter = ref('')
    const tableKey = ref(0)
    const dialogVisible = ref(false)
    const isEditMode = ref(false)
    const userForm = ref({
      id: '',
      username: '',
      password: '',
      phone: '',
      email: '',
      role: '',
      status: ''
    })
    const users = ref([])
    const filteredUsers = ref([])
    
    const currentPage = ref(1)
    const pageSize = ref(5)
    const total = ref(0)
    
    const paginatedUsers = computed(() => {
      const start = (currentPage.value - 1) * pageSize.value
      const end = start + pageSize.value
      return filteredUsers.value.slice(start, end)
    })

    const totalUsers = computed(() => {
      return users.value.filter(u => u.role === 'USER').length
    })

    const totalAdmins = computed(() => {
      return users.value.filter(u => u.role === 'ADMIN').length
    })

    const loadUsers = async () => {
      try {
        const res = await listUsersApi({ page: 1, size: 999 })
        users.value = (res.data.records || []).map(u => ({
          ...u,
          role: u.role === 'ADMIN' ? '管理员' : '用户',
          status: u.status === 'NORMAL' ? '正常' : '禁用'
        }))
        filterUsers()
      } catch (e) {
        console.error('加载用户列表失败', e)
      }
    }

    const filterByRole = (role) => {
      roleFilter.value = role
      filterUsers()
    }

    const filterUsers = () => {
      filteredUsers.value = users.value.filter(user => {
        const matchSearch = searchKeyword.value === '' ||
          user.id.toString().toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
          user.username.toLowerCase().includes(searchKeyword.value.toLowerCase())
        const matchRole = roleFilter.value === '' ||
          user.role === roleFilter.value
        return matchSearch && matchRole
      })
      total.value = filteredUsers.value.length
      tableKey.value++
    }

    const handleSearchClear = () => {
      searchKeyword.value = ''
      filterUsers()
    }

    const handleRoleChange = () => {
      filterUsers()
    }

    const showAddDialog = () => {
      isEditMode.value = false
      userForm.value = {
        id: '',
        username: '',
        password: '',
        phone: '',
        email: '',
        role: '用户',
        status: '正常'
      }
      dialogVisible.value = true
    }

    const showEditDialog = (user) => {
      isEditMode.value = true
      userForm.value = { ...user }
      dialogVisible.value = true
    }

    const handleSave = async () => {
      if (!userForm.value.username || !userForm.value.phone) {
        ElMessage.warning('请填写完整信息')
        return
      }

      if (!isEditMode.value && !userForm.value.password) {
        ElMessage.warning('请输入密码')
        return
      }

      const roleMap = { '管理员': 'ADMIN', '用户': 'USER' }
      const statusMap = { '正常': 'NORMAL', '禁用': 'DISABLED' }

      if (isEditMode.value) {
        try {
          await updateUserApi(userForm.value.id, {
            phone: userForm.value.phone,
            email: userForm.value.email
          })
          await loadUsers()
          ElMessage.success('修改成功')
        } catch (e) {
          ElMessage.error(e.response?.data?.msg || '修改失败')
        }
      } else {
        try {
          await addUserApi({
            id: userForm.value.id,
            username: userForm.value.username,
            password: userForm.value.password,
            phone: userForm.value.phone,
            email: userForm.value.email || '',
            role: roleMap[userForm.value.role] || 'USER',
            status: statusMap[userForm.value.status] || 'NORMAL'
          })
          await loadUsers()
          ElMessage.success('添加成功')
        } catch (e) {
          ElMessage.error(e.response?.data?.msg || '添加失败')
        }
      }

      dialogVisible.value = false
    }

    const handleDelete = (user) => {
      ElMessageBox.confirm('确定要删除该用户吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await deleteUserApi(user.id)
          await loadUsers()
          ElMessage.success('删除成功')
        } catch (e) {
          ElMessage.error(e.response?.data?.msg || '删除失败')
        }
      }).catch(() => {})
    }

    const handleStatusChange = async (user) => {
      const newStatus = user.status === '正常' ? '禁用' : '正常'
      const statusMap = { '正常': 'NORMAL', '禁用': 'DISABLED' }
      try {
        await updateUserStatusApi(user.id, statusMap[newStatus])
        await loadUsers()
        ElMessage.success(`状态已更新为${newStatus}`)
      } catch (e) {
        ElMessage.error(e.response?.data?.msg || '状态更新失败')
      }
    }

    const handleSizeChange = () => {
      currentPage.value = 1
    }

    const handleCurrentChange = () => {
    }

    const canEditStatus = (user) => {
      const currentUserInfo = users.value.find(u => u.username === props.currentUser)
      if (!currentUserInfo) return false
      if (currentUserInfo.role !== '管理员') return false
      if (user.role !== '管理员') return true
      return user.username === props.currentUser
    }

    const canDeleteUser = (user) => {
      const currentUserInfo = users.value.find(u => u.username === props.currentUser)
      if (!currentUserInfo) return false
      if (currentUserInfo.role !== '管理员') return false
      if (user.role === '管理员' && user.username !== props.currentUser) return false
      return true
    }

    onMounted(() => {
      loadUsers()
    })

    watch(searchKeyword, () => { filterUsers() }, { immediate: false })

    return {
      searchKeyword,
      roleFilter,
      tableKey,
      dialogVisible,
      isEditMode,
      userForm,
      filteredUsers,
      paginatedUsers,
      currentPage,
      pageSize,
      total,
      totalUsers,
      totalAdmins,
      handleSearchClear,
      handleRoleChange,
      filterByRole,
      showAddDialog,
      showEditDialog,
      handleSave,
      handleDelete,
      handleStatusChange,
      handleSizeChange,
      handleCurrentChange,
      canEditStatus,
      canDeleteUser
    }
  }
}
</script>

<style scoped>
.page-content h2 {
  margin: 0 0 20px 0;
  color: #303133;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
  overflow: hidden;
  position: relative;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, var(--card-gradient-from), var(--card-gradient-to));
  opacity: 0.8;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  border-color: var(--card-border-hover);
}

.stat-card.user-card {
  --card-gradient-from: #667eea;
  --card-gradient-to: #764ba2;
  --card-border-hover: #667eea;
}

.stat-card.admin-card {
  --card-gradient-from: #f093fb;
  --card-gradient-to: #f5576c;
  --card-border-hover: #f093fb;
}

.stat-card-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon-wrapper {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--card-gradient-from), var(--card-gradient-to));
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  flex-shrink: 0;
}

.stat-icon {
  font-size: 32px;
  color: #fff;
}

.stat-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.stat-number {
  font-size: 36px;
  font-weight: 700;
  color: #303133;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  font-weight: 500;
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

.filter-bar {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  padding: 20px;
  background: #ffffff;
  border-radius: 8px;
  align-items: center;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
}

.filter-item label {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
  white-space: nowrap;
  min-width: 60px;
}

:deep(.el-table th.el-table__cell) {
  text-align: center;
}

:deep(.el-table td.el-table__cell) {
  text-align: center;
}

.table-wrapper {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  border: 3px solid #409eff;
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.2);
}

:deep(.el-table) {
  min-height: 250px;
}

:deep(.el-table__body-wrapper) {
  min-height: 200px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 2px solid #f0f0f0;
}

.disabled-delete-btn {
  background-color: #dcdfe6 !important;
  border-color: #dcdfe6 !important;
  color: #909399 !important;
  cursor: not-allowed;
  opacity: 0.6;
}
</style>
