<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">🔐 权限管理</h1>
      <button class="btn btn-primary" @click="openModal()">
        ➕ 新增权限
      </button>
    </div>

    <div class="card table-card">
      <table class="data-table">
        <thead>
          <tr>
            <th>图标</th>
            <th>权限名称</th>
            <th>权限编码</th>
            <th>类型</th>
            <th>排序</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="perm in permissions" :key="perm.id">
            <td>
              <span class="perm-icon">{{ perm.icon || '🔧' }}</span>
            </td>
            <td>{{ perm.name }}</td>
            <td>
              <span class="code-tag">{{ perm.code }}</span>
            </td>
            <td>
              <span class="type-tag" :class="perm.type">{{ perm.type === 'menu' ? '菜单' : '按钮' }}</span>
            </td>
            <td>{{ perm.sort || '-' }}</td>
            <td class="actions">
              <button class="action-btn edit" @click="openModal(perm)">✏️</button>
              <button class="action-btn delete" @click="deletePerm(perm)">🗑️</button>
            </td>
          </tr>
          <tr v-if="permissions.length === 0">
            <td colspan="6" class="empty-row">
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
          <h2>{{ editingPerm ? '✏️ 编辑权限' : '➕ 新增权限' }}</h2>
          <button class="close-btn" @click="closeModal">✕</button>
        </div>

        <div class="modal-body">
          <div class="form-group">
            <label>权限名称</label>
            <input v-model="form.name" class="input-field" placeholder="请输入权限名称" />
          </div>
          <div class="form-group">
            <label>权限编码</label>
            <input v-model="form.code" class="input-field" placeholder="请输入权限编码(如: USER_LIST)" />
          </div>
          <div class="form-group">
            <label>类型</label>
            <select v-model="form.type" class="input-field">
              <option value="menu">菜单</option>
              <option value="button">按钮</option>
            </select>
          </div>
          <div class="form-group">
            <label>排序</label>
            <input v-model.number="form.sort" type="number" class="input-field" placeholder="请输入排序数字" />
          </div>
          <div class="form-group">
            <label>图标</label>
            <div class="icon-picker">
              <span
                v-for="emoji in iconOptions"
                :key="emoji"
                class="icon-option"
                :class="{ selected: form.icon === emoji }"
                @click="form.icon = emoji"
              >{{ emoji }}</span>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn btn-outline" @click="closeModal">取消</button>
          <button class="btn btn-primary" @click="savePerm">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getPermissions, createPermission, updatePermission, deletePermission } from '@/api/permissions'
import Pagination from '@/components/Pagination.vue'

const permissions = ref([])
const showModal = ref(false)
const editingPerm = ref(null)

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
  totalPages: 1
})

const iconOptions = ['👥', '🎭', '🔐', '➕', '✏️', '🗑️', '🏠', '📋', '⚙️', '🔧']

const defaultForm = {
  id: null,
  name: '',
  code: '',
  type: 'button',
  sort: 0,
  icon: '🔧'
}

const form = reactive({ ...defaultForm })

const loadData = async () => {
  try {
    const res = await getPermissions({ page: pagination.page, pageSize: pagination.pageSize })
    permissions.value = (res.data.data || []).sort((a, b) => (a.sort || 0) - (b.sort || 0))
    pagination.total = res.data.total || 0
    pagination.totalPages = res.data.totalPages || 1
  } catch (e) {
    console.error('加载数据失败', e)
  }
}

const openModal = (perm = null) => {
  editingPerm.value = perm
  if (perm) {
    Object.assign(form, { ...perm })
  } else {
    Object.assign(form, { ...defaultForm })
  }
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  editingPerm.value = null
}

const savePerm = async () => {
  try {
    if (editingPerm.value) {
      await updatePermission(form.id, form)
    } else {
      await createPermission(form)
    }
    closeModal()
    loadData()
  } catch (e) {
    alert(e.response?.data?.message || '保存失败')
  }
}

const deletePerm = async (perm) => {
  if (confirm(`确定要删除权限 "${perm.name}" 吗？`)) {
    try {
      await deletePermission(perm.id)
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

.perm-icon {
  font-size: 24px;
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

.type-tag {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
}

.type-tag.menu {
  background: #e3f2fd;
  color: #1565c0;
}

.type-tag.button {
  background: #fff3e0;
  color: #e65100;
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
  width: 450px;
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

.icon-picker {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.icon-option {
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

.icon-option:hover {
  border-color: var(--primary-light);
}

.icon-option.selected {
  border-color: var(--primary-color);
  background: #e8f5e9;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid var(--border-color);
}
</style>
