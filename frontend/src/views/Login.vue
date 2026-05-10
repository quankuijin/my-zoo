<template>
  <div class="login-page page-bg">
    <span class="paw-decoration" style="top: 10%; left: 5%;">🐾</span>
    <span class="paw-decoration" style="top: 20%; right: 10%;">🐾</span>
    <span class="paw-decoration" style="bottom: 15%; left: 15%;">🐾</span>
    <span class="leaf-decoration" style="top: 30%; right: 5%;">🌿</span>
    <span class="leaf-decoration" style="bottom: 30%; left: 8%;">🍀</span>

    <div class="login-container">
      <div class="login-logo">
        <span class="logo-icon">🦁</span>
        <h1>动物园后台管理系统</h1>
        <p>Zoo Administration System</p>
      </div>

      <div class="login-card card">
        <h2>
          <span>🔐</span>
          用户登录
        </h2>

        <form @submit.prevent="handleLogin" class="login-form">
          <div class="form-group">
            <label>
              <span>👤</span>
              用户名
            </label>
            <input
              v-model="form.username"
              type="text"
              class="input-field"
              placeholder="请输入用户名"
              required
            />
          </div>

          <div class="form-group">
            <label>
              <span>🔒</span>
              密码
            </label>
            <input
              v-model="form.password"
              type="password"
              class="input-field"
              placeholder="请输入密码"
              required
            />
          </div>

          <div v-if="captcha" class="captcha-section">
            <label>
              <span>🖼️</span>
              动物验证码 - 请找到并点击:
              <span class="target-animal">{{ animalNames[captcha.targetAnimal] }} {{ getAnimalIcon(captcha.targetAnimal) }}</span>
            </label>
            <div class="animal-grid">
              <button
                v-for="(animal, index) in captcha.animals"
                :key="index"
                type="button"
                class="animal-btn"
                :class="{ selected: selectedAnimalIndex === index }"
                @click="selectAnimal(index)"
              >
                <span class="animal-icon">{{ animal.icon }}</span>
                <span class="animal-name">{{ animal.name }}</span>
              </button>
            </div>
          </div>

          <div class="form-actions">
            <button type="button" class="btn btn-outline" @click="refreshCaptcha">
              🔄 刷新验证码
            </button>
            <button type="submit" class="btn btn-primary" :disabled="loading">
              <span v-if="loading">🚀 登录中...</span>
              <span v-else>✅ 登录</span>
            </button>
          </div>
        </form>

        <div v-if="error" class="error-message">
          ⚠️ {{ error }}
        </div>

        <div class="login-tips">
          <p>💡 默认账号：</p>
          <p>管理员: admin / 123</p>
          <p>饲养员: keeper / 123</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getCaptcha, login } from '@/api/auth'

const router = useRouter()
const authStore = useAuthStore()

const form = ref({
  username: '',
  password: ''
})

const captcha = ref(null)
const selectedAnimalIndex = ref(-1)
const loading = ref(false)
const error = ref('')

const animalNames = {
  panda: '大熊猫',
  tiger: '老虎',
  lion: '狮子',
  elephant: '大象',
  giraffe: '长颈鹿',
  monkey: '猴子',
  bear: '熊',
  deer: '鹿',
  fox: '狐狸',
  rabbit: '兔子',
  koala: '考拉',
  dolphin: '海豚',
  owl: '猫头鹰',
  penguin: '企鹅',
  zebra: '斑马'
}

const animalIcons = {
  panda: '🐼',
  tiger: '🐅',
  lion: '🦁',
  elephant: '🐘',
  giraffe: '🦒',
  monkey: '🐒',
  bear: '🐻',
  deer: '🦌',
  fox: '🦊',
  rabbit: '🐰',
  koala: '🐨',
  dolphin: '🐬',
  owl: '🦉',
  penguin: '🐧',
  zebra: '🦓'
}

const getAnimalIcon = (name) => animalIcons[name] || '🐾'

const refreshCaptcha = async () => {
  try {
    const res = await getCaptcha()
    captcha.value = res.data
    selectedAnimalIndex.value = -1
    error.value = ''
    captcha.value.animals = captcha.value.animals.map(a => ({
      name: a,
      icon: getAnimalIcon(a)
    }))
  } catch (e) {
    error.value = '获取验证码失败'
  }
}

const selectAnimal = (index) => {
  selectedAnimalIndex.value = index
  error.value = ''
}

const handleLogin = async () => {
  if (selectedAnimalIndex.value === -1) {
    error.value = '请先完成动物验证码验证'
    return
  }

  loading.value = true
  error.value = ''

  try {
    const res = await login({
      sessionId: captcha.value.sessionId,
      selectedIndex: selectedAnimalIndex.value,
      username: form.value.username,
      password: form.value.password
    })

    if (res.data.success) {
      authStore.setAuth(res.data)
      router.push('/dashboard')
    } else {
      error.value = res.data.message || '登录失败'
      await refreshCaptcha()
    }
  } catch (e) {
    error.value = e.response?.data?.message || '登录失败，请重试'
    await refreshCaptcha()
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  refreshCaptcha()
})
</script>

<style scoped>
.login-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 20px;
}

.login-container {
  max-width: 450px;
  width: 100%;
  position: relative;
  z-index: 10;
}

.login-logo {
  text-align: center;
  margin-bottom: 30px;
}

.logo-icon {
  font-size: 64px;
  display: block;
  margin-bottom: 10px;
  animation: bounce 2s infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.login-logo h1 {
  color: var(--primary-dark);
  font-size: 28px;
  margin-bottom: 5px;
}

.login-logo p {
  color: var(--text-secondary);
  font-size: 14px;
}

.login-card {
  padding: 32px;
}

.login-card h2 {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--primary-dark);
  font-size: 20px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid var(--border-color);
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group label {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-primary);
  font-weight: 600;
  margin-bottom: 8px;
  font-size: 14px;
}

.target-animal {
  color: var(--secondary-color);
  font-weight: bold;
  font-size: 16px;
}

.captcha-section {
  background: linear-gradient(135deg, #e8f5e9, #c8e6c9);
  padding: 16px;
  border-radius: 12px;
  border: 2px dashed var(--primary-light);
}

.animal-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-top: 12px;
}

.animal-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 12px 8px;
  background: white;
  border: 2px solid var(--border-color);
  border-radius: 12px;
  cursor: pointer;
  transition: var(--transition);
}

.animal-btn:hover {
  border-color: var(--primary-light);
  transform: translateY(-2px);
  box-shadow: var(--shadow);
}

.animal-btn.selected {
  border-color: var(--primary-color);
  background: linear-gradient(135deg, var(--primary-light), var(--primary-color));
  color: white;
}

.animal-icon {
  font-size: 32px;
  margin-bottom: 4px;
}

.animal-name {
  font-size: 11px;
  color: var(--text-muted);
}

.animal-btn.selected .animal-name {
  color: white;
}

.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 8px;
}

.form-actions .btn {
  flex: 1;
  justify-content: center;
  padding: 14px;
}

.error-message {
  background: #ffebee;
  color: #c62828;
  padding: 12px 16px;
  border-radius: 8px;
  margin-top: 16px;
  font-size: 14px;
}

.login-tips {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid var(--border-color);
  font-size: 12px;
  color: var(--text-muted);
  text-align: center;
}

.login-tips p {
  margin: 4px 0;
}
</style>
