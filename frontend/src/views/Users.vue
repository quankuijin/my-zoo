<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">👥 用户管理</h1>
      <button 
        v-if="canCreateUser" 
        class="btn btn-primary" 
        @click="openModal()"
      >
        ➕ 新增用户
      </button>
    </div>

    <div class="card table-card">
      <table class="data-table">
        <thead>
          <tr>
            <th>头像</th>
            <th>用户名</th>
            <th>姓名</th>
            <th>邮箱</th>
            <th>电话</th>
            <th>角色</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in users" :key="user.id">
            <td>
              <span class="user-avatar-small">{{ user.avatar || '👤' }}</span>
            </td>
            <td>{{ user.username }}</td>
            <td>{{ user.name }}</td>
            <td>{{ user.email || '-' }}</td>
            <td>{{ user.phone || '-' }}</td>
            <td>
              <span class="role-tag" v-for="roleId in user.roleIds" :key="roleId">
                {{ getRoleName(roleId) }}
              </span>
            </td>
            <td>
              <span class="status-tag" :class="user.enabled ? 'active' : 'inactive'">
                {{ user.enabled ? '启用' : '禁用' }}
              </span>
            </td>
            <td class="actions">
              <button 
                v-if="canEditUser(user)" 
                class="action-btn edit" 
                @click="openModal(user)"
              >✏️</button>
              <button 
                v-if="canDeleteUser(user)" 
                class="action-btn delete" 
                @click="deleteUser(user)"
              >🗑️</button>
              <span v-if="!canEditUser(user) && !canDeleteUser(user)" class="no-permission">—</span>
            </td>
          </tr>
          <tr v-if="users.length === 0">
            <td colspan="8" class="empty-row">
              <span class="empty-icon">🦒</span>
              <p>暂无数据</p>
            </td>
          </tr>
        </tbody>
      </table>

      <Pagination
        v-model:page="pagination.page"
        v-model:pageSize="pagination.pageSize"
        :total="pagination.total"
        :totalPages="pagination.totalPages"
        @change="loadData"
      />
    </div>

    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal card">
        <div class="modal-header">
          <h2>{{ editingUser ? '✏️ 编辑用户' : '➕ 新增用户' }}</h2>
          <button class="close-btn" @click="closeModal">✕</button>
        </div>

        <div class="modal-body">
          <div class="form-group">
            <label>用户名</label>
            <input 
              v-model="form.username" 
              class="input-field" 
              placeholder="请输入用户名" 
              :disabled="!!editingUser"
            />
          </div>
          <div v-if="!editingUser" class="form-group">
            <label>密码</label>
            <input v-model="form.password" type="password" class="input-field" placeholder="请输入密码" />
          </div>
          <div class="form-group">
            <label>姓名</label>
            <input v-model="form.name" class="input-field" placeholder="请输入姓名" />
          </div>
          <div class="form-group">
            <label>邮箱</label>
            <input v-model="form.email" class="input-field" placeholder="请输入邮箱" />
          </div>
          <div class="form-group">
            <label>电话</label>
            <input v-model="form.phone" class="input-field" placeholder="请输入电话" />
          </div>
          <div class="form-group">
            <label>头像</label>
            <div class="avatar-picker">
              <span
                v-for="emoji in avatarOptions"
                :key="emoji"
                class="avatar-option"
                :class="{ selected: form.avatar === emoji }"
                @click="form.avatar = emoji"
              >{{ emoji }}</span>
            </div>
          </div>
          <div v-if="isAdmin" class="form-group">
            <label>角色</label>
            <div class="checkbox-group">
              <label v-for="role in allRoles" :key="role.id" class="checkbox-label">
                <input type="checkbox" :value="role.id" v-model="form.roleIds" />
                <span>{{ role.name }}</span>
              </label>
            </div>
          </div>
          <div v-if="isAdmin" class="form-group checkbox-inline">
            <label class="checkbox-label">
              <input type="checkbox" v-model="form.enabled" />
              <span>启用账号</span>
            </label>
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn btn-outline" @click="closeModal">取消</button>
          <button class="btn btn-primary" @click="saveUser">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { getUsers, createUser, updateUser, deleteUser as apiDeleteUser } from '@/api/users'
import { getAllRoles } from '@/api/roles'
import { useAuthStore } from '@/stores/auth'
import Pagination from '@/components/Pagination.vue'

const authStore = useAuthStore()

const users = ref([])
const allRoles = ref([])
const showModal = ref(false)
const editingUser = ref(null)

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
  totalPages: 1
})

const avatarOptions = ['🐼', '🦁', '🐯', '🐘', '🦒', '🐒', '🐻', '🦊', '🐰', '🐨']

const defaultForm = {
  id: null,
  username: '',
  password: '',
  name: '',
  email: '',
  phone: '',
  avatar: '🐼',
  roleIds: [],
  enabled: true
}

const form = reactive({ ...defaultForm })

const isAdmin = computed(() => authStore.hasRole('ADMIN'))

const canCreateUser = computed(() => isAdmin.value && authStore.hasPermission('USER_CREATE'))

const canEditUser = (user) => {
  if (isAdmin.value && authStore.hasPermission('USER_EDIT')) {
    return true
  }
  if (authStore.hasPermission('USER_EDIT') && user.id === authStore.user?.id) {
    return true
  }
  return false
}

const canDeleteUser = (user) => {
  return isAdmin.value && authStore.hasPermission('USER_DELETE')
}

const getRoleName = (id) => {
  const role = allRoles.value.find(r => r.id === id)
  return role?.name || '未知'
}

const loadData = async () => {
  try {
    const [usersRes, rolesRes] = await Promise.all([
      getUsers({ page: pagination.page, pageSize: pagination.pageSize }),
      getAllRoles()
    ])
    users.value = usersRes.data.data || []
    pagination.total = usersRes.data.total || 0
    pagination.totalPages = usersRes.data.totalPages || 1
    allRoles.value = rolesRes.data.data || []
  } catch (e) {
    if (e.response?.status === 403) {
      alert(e.response.data?.message || '无权限')
    } else {
      console.error('加载数据失败', e)
    }
  }
}

const openModal = (user = null) => {
  editingUser.value = user
  if (user) {
    Object.assign(form, { ...user, password: '' })
  } else {
    Object.assign(form, { ...defaultForm })
  }
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  editingUser.value = null
}

const saveUser = async () => {
  try {
    if (editingUser.value) {
      await updateUser(form.id, form)
    } else {
      await createUser(form)
    }
    closeModal()
    loadData()
  } catch (e) {
    if (e.response?.status === 403) {
      alert(e.response.data?.message || '无权限操作')
    } else {
      alert(e.response?.data?.message || '保存失败')
    }
  }
}

const deleteUser = async (user) => {
  if (confirm(`确定要删除用户 "${user.name}" 吗？`)) {
    try {
      await apiDeleteUser(user.id)
      loadData()
    } catch (e) {
      if (e.response?.status === 403) {
        alert(e.response.data?.message || '无权限删除')
      } else {
        alert('删除失败')
      }
    }
  }
}

onMounted(loadData)
</script>

<style scoped>
.page-container {
  max-width: 100%;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  color: var(--primary-dark);
  display: flex;
  align-items: center;
  gap: 10px;
}

.table-card {
  padding: 0;
  overflow: hidden;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 14px 16px;
  text-align: left;
  border-bottom: 1px solid var(--border-color);
}

.data-table th {
  background: linear-gradient(135deg, #e8f5e9, #f1f8e9);
  color: var(--primary-dark);
  font-weight: 600;
  font-size: 13px;
}

.data-table tbody tr:hover {
  background: #f5fcf5;
}

.user-avatar-small {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #c8e6c9, #a5d6a7);
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.role-tag {
  display: inline-block;
  padding: 4px 10px;
  background: linear-gradient(135deg, #fff3e0, #ffe0b2);
  color: #e65100;
  border-radius: 12px;
  font-size: 12px;
  margin-right: 4px;
}

.status-tag {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-tag.active {
  background: linear-gradient(135deg, #e8f5e9, #c8e6c9);
  color: #1b5e20;
}

.status-tag.inactive {
  background: #f5f5f5;
  color: #757575;
}

.actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: var(--transition);
}

.action-btn.edit {
  background: #fff3e0;
}

.action-btn.edit:hover {
  background: #ffe0b2;
}

.action-btn.delete {
  background: #ffebee;
}

.action-btn.delete:hover {
  background: #ffcdd2;
}

.no-permission {
  color: var(--text-muted);
  font-size: 12px;
}

.empty-row {
  text-align: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 48px;
  display: block;
  margin-bottom: 12px;
}

.empty-row p {
  color: var(--text-muted);
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  width: 500px;
  max-height: 90vh;
  overflow-y: auto;
  padding: 0;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-color);
}

.modal-header h2 {
  font-size: 18px;
  color: var(--primary-dark);
  margin: 0;
}

.close-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: #f5f5f5;
  border-radius: 50%;
  cursor: pointer;
  font-size: 16px;
}

.modal-body {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-group label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.avatar-picker {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.avatar-option {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  border: 2px solid var(--border-color);
  border-radius: 8px;
  cursor: pointer;
  transition: var(--transition);
}

.avatar-option:hover {
  border-color: var(--primary-light);
}

.avatar-option.selected {
  border-color: var(--primary-color);
  background: #e8f5e9;
}

.checkbox-group {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  font-size: 14px;
}

.checkbox-inline {
  margin-top: 8px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid var(--border-color);
}
</style>
