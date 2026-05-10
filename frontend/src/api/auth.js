import api from './index'

export const getCaptcha = () => api.get('/auth/captcha')

export const login = (data) => api.post('/auth/login', data)

export const logout = () => api.post('/auth/logout')
