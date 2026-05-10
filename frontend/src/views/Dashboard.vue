<template>
  <div class="dashboard">
    <h1 class="page-title">🏠 首页概览</h1>

    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon">👥</div>
        <div class="stat-info">
          <div class="stat-value">{{ userCount }}</div>
          <div class="stat-label">系统用户</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon">🎭</div>
        <div class="stat-info">
          <div class="stat-value">{{ roleCount }}</div>
          <div class="stat-label">系统角色</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon">🔐</div>
        <div class="stat-info">
          <div class="stat-value">{{ permCount }}</div>
          <div class="stat-label">权限项</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon">🦁</div>
        <div class="stat-info">
          <div class="stat-value">15</div>
          <div class="stat-label">动物种类</div>
        </div>
      </div>
    </div>

    <div class="welcome-card card">
      <div class="welcome-header">
        <span class="welcome-icon">🎋</span>
        <div>
          <h2>欢迎回来，{{ authStore.user?.name || '管理员' }}！</h2>
          <p>今天是美好的一天，动物园一切正常运行中。</p>
        </div>
      </div>

      <div class="welcome-tips">
        <div class="tip-item">
          <span class="tip-icon">💡</span>
          <span>默认管理员账号: admin / 123</span>
        </div>
        <div class="tip-item">
          <span class="tip-icon">💡</span>
          <span>默认饲养员账号: keeper / 123</span>
        </div>
        <div class="tip-item">
          <span class="tip-icon">🐾</span>
          <span>您当前角色: {{ authStore.roles.join(', ') || '无' }}</span>
        </div>
      </div>
    </div>

    <div class="features-grid">
      <div class="feature-card card" v-for="feature in features" :key="feature.title">
        <span class="feature-icon">{{ feature.icon }}</span>
        <h3>{{ feature.title }}</h3>
        <p>{{ feature.desc }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { getUsers } from '@/api/users'
import { getRoles } from '@/api/roles'
import { getPermissions } from '@/api/permissions'

const authStore = useAuthStore()

const userCount = ref(0)
const roleCount = ref(0)
const permCount = ref(0)

const features = [
  { icon: '👥', title: '用户管理', desc: '管理系统用户，分配角色权限' },
  { icon: '🎭', title: '角色管理', desc: '创建角色，配置权限集合' },
  { icon: '🔐', title: '权限管理', desc: '管理系统权限，精细控制' },
  { icon: '🦁', title: '动物主题', desc: '绿色主题，动物emoji风格' }
]

onMounted(async () => {
  try {
    const [usersRes, rolesRes, permsRes] = await Promise.all([
      getUsers(),
      getRoles(),
      getPermissions()
    ])
    userCount.value = usersRes.data.data?.length || 0
    roleCount.value = rolesRes.data.data?.length || 0
    permCount.value = permsRes.data.data?.length || 0
  } catch (e) {
    console.error('加载统计数据失败', e)
  }
})
</script>

<style scoped>
.dashboard {
  max-width: 1200px;
  margin: 0 auto;
}

.page-title {
  font-size: 24px;
  color: var(--primary-dark);
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: var(--shadow);
  transition: var(--transition);
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.stat-icon {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #e8f5e9, #c8e6c9);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: var(--primary-dark);
}

.stat-label {
  font-size: 14px;
  color: var(--text-muted);
}

.welcome-card {
  margin-bottom: 24px;
  border-left: 4px solid var(--primary-color);
}

.welcome-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.welcome-icon {
  font-size: 48px;
}

.welcome-header h2 {
  font-size: 20px;
  color: var(--primary-dark);
  margin-bottom: 4px;
}

.welcome-header p {
  color: var(--text-muted);
}

.welcome-tips {
  background: linear-gradient(135deg, #e8f5e9, #f1f8e9);
  padding: 16px 20px;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.tip-item {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--text-secondary);
  font-size: 14px;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.feature-card {
  text-align: center;
  padding: 32px 20px;
  transition: var(--transition);
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.feature-icon {
  font-size: 48px;
  display: block;
  margin-bottom: 12px;
}

.feature-card h3 {
  font-size: 16px;
  color: var(--primary-dark);
  margin-bottom: 8px;
}

.feature-card p {
  font-size: 13px;
  color: var(--text-muted);
  line-height: 1.5;
}
</style>
