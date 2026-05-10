<template>
  <div class="layout">
    <span class="paw-decoration" style="top: 5%; right: 5%;">🐾</span>
    <span class="leaf-decoration" style="bottom: 10%; left: 3%;">🌿</span>

    <aside class="sidebar">
      <div class="logo-section">
        <span class="logo-icon">🦁</span>
        <div class="logo-text">
          <h3>动物园后台</h3>
          <p>Zoo Admin</p>
        </div>
      </div>

      <nav class="sidebar-nav">
        <router-link
          v-for="item in filteredMenuItems"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          active-class="active"
        >
          <span class="nav-icon">{{ item.icon }}</span>
          <span class="nav-text">{{ item.title }}</span>
        </router-link>
      </nav>

      <div class="sidebar-footer">
        <button class="logout-btn" @click="handleLogout">
          🚪 退出登录
        </button>
      </div>
    </aside>

    <main class="main-content">
      <header class="header">
        <div class="header-left">
          <span class="breadcrumb">{{ currentTitle }}</span>
        </div>
        <div class="header-right">
          <div class="user-info">
            <span class="user-avatar">{{ authStore.user?.avatar || '👤' }}</span>
            <div class="user-detail">
              <span class="user-name">{{ authStore.user?.name || '用户' }}</span>
              <span class="user-role">
                {{ authStore.roles.includes('ADMIN') ? '系统管理员' : '饲养员' }}
              </span>
            </div>
          </div>
        </div>
      </header>

      <section class="content-area">
        <router-view />
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const allMenuItems = [
  { path: '/dashboard', title: '首页概览', icon: '🏠', permission: null, roles: ['ADMIN'] },
  { path: '/users', title: '用户管理', icon: '👥', permission: 'USER_LIST' },
  { path: '/roles', title: '角色管理', icon: '🎭', permission: 'ROLE_LIST', roles: ['ADMIN'] },
  { path: '/permissions', title: '权限管理', icon: '🔐', permission: 'PERMISSION_LIST', roles: ['ADMIN'] }
]

const filteredMenuItems = computed(() => {
  return allMenuItems.filter(item => {
    if (item.roles && item.roles.length > 0) {
      if (!item.roles.some(role => authStore.hasRole(role))) {
        return false
      }
    }
    if (item.permission === null) {
      return true
    }
    return authStore.hasPermission(item.permission)
  })
})

const currentTitle = computed(() => {
  const matched = route.matched[route.matched.length - 1]
  return matched?.meta?.title || '首页'
})

const handleLogout = async () => {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout {
  display: flex;
  min-height: 100vh;
  background: linear-gradient(135deg, #e8f5e9 0%, #f1f8e9 100%);
  position: relative;
}

.sidebar {
  width: 240px;
  background: linear-gradient(180deg, #1b5e20 0%, #2e7d32 50%, #388e3c 100%);
  color: white;
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 100;
}

.logo-section {
  padding: 24px 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo-icon {
  font-size: 40px;
  animation: bounce 2s infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
}

.logo-text h3 {
  font-size: 16px;
  font-weight: 700;
  margin: 0;
}

.logo-text p {
  font-size: 11px;
  opacity: 0.7;
  margin: 0;
}

.sidebar-nav {
  flex: 1;
  padding: 16px 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  border-radius: 10px;
  transition: all 0.3s ease;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.1);
  color: white;
}

.nav-item.active {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.nav-icon {
  font-size: 20px;
}

.nav-text {
  font-size: 14px;
  font-weight: 500;
}

.sidebar-footer {
  padding: 16px 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.logout-btn {
  width: 100%;
  padding: 12px;
  background: rgba(255, 255, 255, 0.1);
  border: none;
  border-radius: 10px;
  color: white;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
}

.logout-btn:hover {
  background: rgba(255, 82, 82, 0.8);
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.header {
  height: 64px;
  background: white;
  box-shadow: 0 2px 8px rgba(46, 125, 50, 0.1);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 50;
}

.header-left .breadcrumb {
  font-size: 16px;
  font-weight: 600;
  color: var(--primary-dark);
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #c8e6c9, #a5d6a7);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.user-detail {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.user-role {
  font-size: 12px;
  color: var(--text-muted);
}

.content-area {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}
</style>
