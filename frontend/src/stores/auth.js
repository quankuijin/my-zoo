import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  const roles = ref(JSON.parse(localStorage.getItem('roles') || '[]'))
  const permissions = ref(JSON.parse(localStorage.getItem('permissions') || '[]'))

  const isLoggedIn = computed(() => !!token.value)

  function setAuth(data) {
    token.value = data.token
    user.value = data.user
    roles.value = data.roles || []
    permissions.value = data.permissions || []
    
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify(data.user))
    localStorage.setItem('roles', JSON.stringify(data.roles || []))
    localStorage.setItem('permissions', JSON.stringify(data.permissions || []))
  }

  function logout() {
    token.value = ''
    user.value = null
    roles.value = []
    permissions.value = []
    
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    localStorage.removeItem('roles')
    localStorage.removeItem('permissions')
  }

  function hasPermission(code) {
    return permissions.value.includes(code)
  }

  function hasRole(code) {
    return roles.value.includes(code)
  }

  return {
    token,
    user,
    roles,
    permissions,
    isLoggedIn,
    setAuth,
    logout,
    hasPermission,
    hasRole
  }
})
