<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">🎭 角色管理</h1>
      <button class="btn btn-primary" @click="openModal()">
        ➕ 新增角色
      </button>
    </div>

    <div class="card table-card">
      <table class="data-table">
        <thead>
          <tr>
            <th>图标</th>
            <th>角色名称</th>
            <th>角色编码</th>
            <th>描述</th>
            <th>权限数量</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="role in roles" :key="role.id">
            <td>
              <span class="role-icon-large">🎭</span>
            </td>
            <td>{{ role.name }}</td>
            <td>
              <span class="code-tag">{{ role.code }}</span>
            </td>
            <td>{{ role.description || '-' }}</td>
            <td>
              <span class="perm-count">{{ role.permissionIds?.length || 0 }} 个权限</span>
            </td>
            <td>
              <span class="status-tag" :class="role.enabled ? 'active' : 'inactive'">
                {{ role.enabled ? '启用' : '禁用' }}
              </span>
            </td>
            <td class="actions">
              <button class="action-btn edit" @click="openModal(role)">✏️</button>
              <button class="action-btn delete" @click="deleteRole(role)">🗑️</button>
            </td>
          </tr>
          <tr v-if="roles.length === 0">
            <td colspan="7" class="empty-row">
              <span class="empty-icon">🦒</span>
              <p>暂无角色数据</p>
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
          <h2>{{ editingRole ? '✏️ 编辑角色' : '➕ 新增角色' }}</h2>
          <button class="close-btn" @click="closeModal">✕</button>
        </div>

        <div class="modal-body">
          <div class="form-group">
            <label>角色名称</label>
            <input v-model="form.name" class="input-field" placeholder="请输入角色名称" />
          </div>
          <div class="form-group">
            <label>角色编码</label>
            <input v-model="form.code" class="input-field" placeholder="请输入角色编码(如: ADMIN)" />
          </div>
          <div class="form-group">
            <label>描述</label>
            <input v-model="form.description" class="input-field" placeholder="请输入角色描述" />
          </div>
          <div class="form-group">
            <label>分配权限</label>
            <div class="perm-grid">
              <label v-for="perm in allPermissions" :key="perm.id" class="checkbox-label">
                <input type="checkbox" :value="perm.id" v-model="form.permissionIds" />
                <span>{{ perm.icon }} {{ perm.name }}</span>
              </label>
            </div>
          </div>
          <div class="form-group checkbox-inline">
            <label class="checkbox-label">
              <input type="checkbox" v-model="form.enabled" />
              <span>启用角色</span>
            </label>
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn btn-outline" @click="closeModal">取消</button>
          <button class="btn btn-primary" @click="saveRole">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getRoles, createRole, updateRole, deleteRole as apiDeleteRole } from '@/api/roles'
import { getAllPermissions } from '@/api/permissions'
import Pagination from '@/components/Pagination.vue'

const roles = ref([])
const allPermissions = ref([])
const showModal = ref(false)
const editingRole = ref(null)

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
  totalPages: 1
})

const defaultForm = {
  id: null,
  name: '',
  code: '',
  description: '',
  permissionIds: [],
  enabled: true
}

const form = reactive({ ...defaultForm })

const loadData = async () => {
  try {
    const [rolesRes, permsRes] = await Promise.all([
      getRoles({ page: pagination.page, pageSize: pagination.pageSize }),
      getAllPermissions()
    ])
    roles.value = rolesRes.data.data || []
    allPermissions.value = permsRes.data.data || []
    pagination.total = rolesRes.data.total || 0
    pagination.totalPages = rolesRes.data.totalPages || 1
  } catch (e) {
    console.error('加载数据失败', e)
  }
}

const openModal = (role = null) => {
  editingRole.value = role
  if (role) {
    Object.assign(form, { ...role })
  } else {
    Object.assign(form, { ...defaultForm })
  }
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  editingRole.value = null
}

const saveRole = async () => {
  try {
    if (editingRole.value) {
      await updateRole(form.id, form)
    } else {
      await createRole(form)
    }
    closeModal()
    loadData()
  } catch (e) {
    alert(e.response?.data?.message || '保存失败')
  }
}

const deleteRole = async (role) => {
  if (confirm(`确定要删除角色 "${role.name}" 吗？`)) {
    try {
      await apiDeleteRole(role.id)
      loadData()
    } catch (e) {
      alert('删除失败')
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

.role-icon-large {
  font-size: 28px;
}

.code-tag {
  display: inline-block;
  padding: 4px 10px;
  background: #f3e5f5;
  color: #7b1fa2;
  border-radius: 6px;
  font-family: monospace;
  font-size: 12px;
}

.perm-count {
  display: inline-block;
  padding: 4px 10px;
  background: #e3f2fd;
  color: #1565c0;
  border-radius: 6px;
  font-size: 12px;
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

.perm-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  max-height: 200px;
  overflow-y: auto;
  padding: 8px;
  background: #f5fcf5;
  border-radius: 8px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  font-size: 13px;
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
